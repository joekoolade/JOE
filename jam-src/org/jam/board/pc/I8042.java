package org.jam.board.pc;

import org.jam.board.pc.I8042.Subscriber;
import org.jam.cpu.intel.Tsc;
import org.jam.system.DeviceTimeout;
import org.jam.util.InputObserver;
import org.jam.util.Iterator;
import org.jam.util.LinkedList;
import org.jam.util.InputObserver;
import org.jam.util.InputSubject;
import org.jikesrvm.VM;
import org.vmmagic.unboxed.Address;

public class I8042 implements InputSubject {
	
	private final byte DATA_PORT = 0x60;
	private final byte CSR_PORT = 0x64;
	private final Address csrReg = Address.fromIntZeroExtend(CSR_PORT);
	private final Address dataReg = Address.fromIntZeroExtend(DATA_PORT);
	
	/*
	 * Status register flags
	 */
	private final int STR_PARITY  = 0x80;
	private final int STR_TIMEOUT = 0x40;
	private final int STR_AUXDATA = 0x20;
	private final int STR_KEYLOCK = 0x10;
	private final int STR_CMDDAT  = 0x08;
	private final int STR_MUXERR  = 0x04;
	private final int STR_IBF     = 0x02;
	private final int STR_OBF     = 0x01;
	/*
	 * Control status flags
	 */
	private final int CTR_XLATE      = 0x40;
	private final int CTR_AUXDIS     = 0x20;
	private final int CTR_KBDDIS     = 0x10;
	private final int CTR_IGNKEYLOCK = 0x08;
	private final int CTR_AUXINT     = 0x02;
	private final int CTR_KBDINT     = 0x01;
	
	private final int CMD_CTL_RCTR = 0x20;
	private final int CMD_CTL_WCTR = 0x60;
	private final int CMD_CTL_TEST = 0xaa;
	
	private final int CTL_TEST_PASS = 0x55;
	private final int CTL_TEST_FAIL = 0xfc;
	
	private final int CMD_KBD_TEST    = 0xab;
	private final int CMD_KBD_DISABLE = 0xad;
	private final int CMD_KBD_ENABLE  = 0xae;
	
	private final int TIMEOUT = 5;
	private final int BUFFER_SIZE = 16;
	
	private final DeviceTimeout deviceTimeout;
	
	private int initialConfig;
	
	private final boolean INTR_DEBUG = true;
	
	private int queue[];
	private int head, tail;
	
	private LinkedList<InputObserver> observers;
	
	public I8042()
	{
	    VM.sysWriteln("I8042");
	    deviceTimeout = new DeviceTimeout();
	    VM.sysWriteln("deviceTimeout");
	    queue = new int[128];
        VM.sysWriteln("queue");
	    head = tail = 0;
        VM.sysWriteln("head/tail");
	    observers = new LinkedList<InputObserver>();
        VM.sysWriteln("observers");
	}
	
	public void attach(InputObserver o)
	{
	    observers.add(o);
	}
	
	public void detach(InputObserver o)
	{
	    observers.remove(o);
	}
	
	private final void notify(int key)
	{
	    int index = 0;
	    for(; index < observers.size(); index++)
	    {
	        observers.get(index).update(key);
	    }
	}
	/**
	 * Read status from CSR register
	 * @return i8042 status
	 */
	private final int status()
	{
		return (int)csrReg.ioLoadByte() & 0xff;
	}
	
	/**
	 * Write command to status command/status register
	 * @param ctl i8042 command
	 */
	private final  void control(byte ctl)
	{
		csrReg.ioStore(ctl);
	}
	
	/**
	 * Read data from data port register
	 * @return data port register
	 */
	private final int readData()
	{
		return (int)dataReg.ioLoadByte() & 0xff;
	}
	
	/**
	 * Write data to data port register
	 * @param data
	 */
	private final void writeData(byte data)
	{
		dataReg.ioStore(data);
	}
	
	/**
	 * Check if the output buffer is empty
	 * @return true if output buffer is empty
	 */
	private final boolean outputBufferEmpty()
	{
		return (status() & STR_OBF) == 0;
	}
	
	/*
	 * Check if when output buffer has a value to be read
	 */
	private final boolean canRead()
	{
		int i=0;
		while(outputBufferEmpty() && (i < TIMEOUT))
		{
			udelay(50);
			i++;
		}
		return (i<TIMEOUT);
	}
	/*
	 * Check when input buffer is ready to be written
	 */
	private final boolean canWrite()
	{
		int i = 0;
		while(inputBufferFull() && (i < TIMEOUT))
		{
			udelay(50);
			i++;
		}
		return (i < TIMEOUT);
	}
	
	private final void udelay(int usecs) 
	{
	    Tsc.udelay(usecs);
	}

	/**
	 * Check if input buffer is full
	 * @return true if input buffer is full
	 */
	private final boolean inputBufferFull() {
		return (status() & STR_IBF) > 0;
	}

	public final void interrupt()
	{
		int status = status();
		if((status & STR_OBF) == 0)
		{
		    if(INTR_DEBUG) VM.sysWriteln("kbd intr, no data");
		    return;
		}
		int data = readData();
//		VM.sysWriteln("kbd ", VM.intAsHexString(data));
		notify(data);
		queue[head++] = data;
		head &= 0x7f;
	}
	
	public final int readKey()
	{
	    int key = queue[tail++];
	    tail &= 0x7f;
	    return key;
	}
	public  void init()
	{
		int config;
		
		disableKeyboard();
		flush();
		try {
			initialConfig = readControlConfiguration();
		} catch (DeviceTimeout e) {
			VM.sysWriteln("read config error");
			return;
		}
		config = initialConfig;
		config &= ~CTR_KBDDIS;
		config |= CTR_KBDINT;
		VM.sysWrite("8042 config ", VM.intAsHexString(initialConfig));
		VM.sysWriteln(" ", VM.intAsHexString(config));

		try {
			writeControlConfiguration((byte)config);
		} catch (DeviceTimeout e) {
			VM.sysWriteln("write config error");
			return;
		}
		
		if(selftest() == false) VM.sysWriteln("kbd selftest failed");
		flush();
		enableKeyboard();
	}
	
	private final  void flush() 
	{
	    int val;
	    int count = 0;
	    int data;
	    
	    while(((val = status()) & STR_OBF) > 0)
	    {
	        if(count++ < BUFFER_SIZE)
	        {
	            udelay(50);
	            data = readData();
	            VM.sysWriteln("flushing, " + data);
	        }
	        else
	        {
	            break;
	        }
	    }
	}

	public final  void disableKeyboard() 
	{
		if(canWrite())
		{
		    control((byte)CMD_KBD_DISABLE);
		}
		else
		{
		    VM.sysWriteln("keyboard disable failed");
		}
	}

	public final void enableKeyboard()
	{
		if(canWrite())
		{
		    control((byte)CMD_KBD_ENABLE);
		}
		else
		{
		    VM.sysWriteln("keyboard enable failed");
		}
	}
	
	public final  void setupKeyboard()
	{
		
	}
	
	final boolean selftest()
	{
	    int i=0;
	    int result=0;
	    
	    while(i < 5)
	    {
    	    if(canWrite())
    	    {
    	        control((byte)CMD_CTL_TEST);
    	        if(canRead())
    	        {
    	            result = readData();
    	            if(result == CTL_TEST_PASS)
    	            {
    	                return true;
    	            }
    	        }
    	    }
    	    udelay(50000);
    	    i++;
	    }
	    if(result != CTL_TEST_FAIL)
	    {
	        VM.sysWriteln("unknown test result " + result);
	    }
		return false;
	}
	
	public final boolean keyboardTest()
	{
		return true;
	}
	
	public final int readControlConfiguration()
	throws DeviceTimeout
	{
	    int config;
	    
	    if(canWrite())
	    {
	        control((byte)CMD_CTL_RCTR);
	        if(canRead())
	        {
	            config = readData();
	        }
	        else throw deviceTimeout;
	    }
	    else throw deviceTimeout;
	    
		return config;
	}
	
	public final void writeControlConfiguration(byte config)
	throws DeviceTimeout
	{
        if(canWrite())
        {
            control((byte)CMD_CTL_WCTR);
            if(canWrite())
            {
                writeData(config);
            }
            else throw deviceTimeout;
        }
        else throw deviceTimeout;
	}
	
	public static class Subscriber
	{
	    public final static int SCAN_CODE_QUEUE_SIZE = 32;
	    char codes[] = new char[SCAN_CODE_QUEUE_SIZE];
	    int head, tail, size;
	    
	    Subscriber()
	    {
	        head = 0;
	        tail = 0;
	        size = 0;
	    }
	    public void update(char scanCode)
	    {
	        codes[head] = scanCode;
	        head = (head+1)%SCAN_CODE_QUEUE_SIZE;
	        size++;
	    }
	    
	    public char get()
	    {
	        char scanCode = codes[tail];
	        tail = (tail+1) & SCAN_CODE_QUEUE_SIZE;
	        size--;
	        return scanCode;
	    }
	    
	    public int queueSize()
	    {
	        return size;
	    }
	}
}
