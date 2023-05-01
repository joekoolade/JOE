package org.jam.board.pc;

import org.jam.system.DeviceTimeout;
import org.jikesrvm.VM;
import org.vmmagic.unboxed.Address;

public class I8042 {
	
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
	
	private final int CMD_KBD_TEST    = 0xab;
	private final int CMD_KBD_DISABLE = 0xad;
	private final int CMD_KBD_ENABLE  = 0xae;
	
	private final int TIMEOUT = 10000;
	private int initialConfig;
	
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
	
	private final void udelay(int i) {
	}

	/**
	 * Check if input buffer is full
	 * @return true if input buffer is full
	 */
	private final boolean inputBufferFull() {
		return (status() & STR_IBF) > 0;
	}

	public  void interrupt()
	{
		int status = status();
		
	}
	
	public  void init()
	{
		int config;
		
		
		try {
			initialConfig = readControlConfiguration();
		} catch (DeviceTimeout e) {
			VM.sysWriteln("read config error");
			return;
		}
		VM.sysWriteln("8042 config ", VM.intAsHexString(initialConfig));
		config = initialConfig;
		config |= CTR_KBDDIS;
		config &= ~CTR_KBDINT;
		
		try {
			writeControlConfiguration((byte)config);
		} catch (DeviceTimeout e) {
			VM.sysWriteln("write config error");
			return;
		}
		
		flush();
	}
	
	private final  void flush() {
		// TODO Auto-generated method stub
		
	}

	public final  void disableKeyboard() {
		// TODO Auto-generated method stub
		
	}

	public final void enableKeyboard()
	{
		
	}
	public final  void setupKeyboard()
	{
		
	}
	
	public final boolean selftest()
	{
	    
		return true;
	}
	public final boolean keyboardTest()
	{
		return true;
	}
	
	public final int readControlConfiguration()
	throws DeviceTimeout
	{
		return 0;
	}
	
	public final void writeControlConfiguration(byte config)
	throws DeviceTimeout
	{
		
	}
	
	
}
