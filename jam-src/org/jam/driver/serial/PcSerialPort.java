package org.jam.driver.serial;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.jam.util.ArrayDeque;
import org.jikesrvm.VM;
import org.jikesrvm.runtime.Magic;
import org.vmmagic.pragma.NonMoving;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Offset;

@NonMoving
public class PcSerialPort {
	Address comPort;
	/*
	 * Interrupt stack
	 */
	private int stack[];
	Address stackTop;
	private final static int STACK_SIZE = 256;
	private ArrayDeque<Character> receiveBuffer;
	private ArrayDeque<Character> transmitBuffer;
	private final static int BUFFER_SIZE = 512;
	private int transmitIndex;
	private int receiveIndex;
	private final static int MAX_RETRY = 3;
	
	final private static Offset RBR = PcSerialPortRegister.RBR.getOffset();
	final private static Offset THR = PcSerialPortRegister.THR.getOffset();
	final private static Offset LSR = PcSerialPortRegister.LSR.getOffset();
	final private static Offset LCR = PcSerialPortRegister.LCR.getOffset();
	final private static Offset DLL = PcSerialPortRegister.DLL.getOffset();
	final private static Offset DLH = PcSerialPortRegister.DLH.getOffset();
	final private static Offset IIR = PcSerialPortRegister.IIR.getOffset();
    final private static Offset IER = PcSerialPortRegister.IER.getOffset();
	final private static Offset MSR = PcSerialPortRegister.MSR.getOffset();
	
	final private static int LSR_TEMT = 0x40;		// empty data holding register
	@SuppressWarnings("unused")
	final private static int LSR_THRE = 0x20;		// empty transmitter holding register
	
	final private static int LCR_DLAB = 0x80;		// Divisor latch access
	@SuppressWarnings("unused")
	final private static int LCR_SB   = 0x40;		// break
	@SuppressWarnings("unused")
	final private static int LCR_SP   = 0x20;		// sticky parity
	final private static int LCR_EPS  = 0x10;		// even parity select
	final private static int LCR_PEN  = 0x08;		// parity enable
	final private static int LCR_STB  = 0x04;		// stop bits
	final private static int LCR_WL_MASK = 0x3;
	final private static int LCR_WL_8BITS = 0x3;
	final private static int LCR_WL_7BITS = 0x2;
	final private static int LCR_WL_6BITS = 0x1;
	final private static int LCR_WL_5BITS = 0x0;
	
	// Interrupt enable register
	final private static int IER_LPM      = 0x20;     // low power mode interrupt enable
	final private static int IER_SM       = 0x10;     // sleep mode interrupt enable
	final private static int IER_MSI      = 0x08;     // modem status interrupt enable
	final private static int IER_RLSI     = 0x04;     // receiver line status interrupt enable
	final private static int IER_THREI    = 0x02;     // xmit holding register empty interrupt enable
	final private static int IER_RDAI     = 0x01;     // receiver data available interrupt enable
	
	// Interrupt identification register
	final private static int IIR_FIFO_NF  = 0x80;     // FIFO enabled but not functioning
	final private static int IIR_FIFO_EN  = 0xC0;     // FIFO enabled
	final private static int IIR_FIFO64   = 0x20;     // 64 byte FIFO enabled (16750 only)
	// bits 1-3
	final private static int IIR_MSI      = 0x00;     // modem status interrupt
	final private static int IIR_THREI    = 0x02;     // xmit holding register empty interrupt
	final private static int IIR_RDAI     = 0x04;     // receiver data available interrupt
	final private static int IIR_RLSI     = 0x06;     // receiver line status interrupt
	final private static int IIR_TIP      = 0x0C;     // timeout interrupt pending (16550)
	// bit 0
	final private static int IIR_IPF      = 0x01;     // interrupt pending flag
	
	// Line Status Register
	final private static int LSR_FIFOERR  = 0x80;     // RCV FIFO error
	final private static int LSR_EDHR     = 0x40;     // empty data holding registers
	final private static int LSR_ETHR     = 0x20;     // empty xmit holding registers
	final private static int LSR_BI       = 0x10;     // break interrupt
	final private static int LSR_FE       = 0x08;     // framing error
	final private static int LSR_PE       = 0x04;     // parity error
	final private static int LSR_OE       = 0x02;     // overrun error
	final private static int LSR_DR       = 0x01;     // data ready
	
	public int breakCount = 0;
	public int framingError = 0;
	public int parityError = 0;
	public int overrunError = 0;

	private SerialOutputStream outputStream;
	private PrintStream printStream;
	
	public PcSerialPort(int portAddress)
	{
		comPort = Address.fromIntZeroExtend(portAddress);
		// disable all interrupts
		disableInterrupts();
		receiveBuffer = new ArrayDeque<Character>(BUFFER_SIZE);
		transmitBuffer = new ArrayDeque<Character>(BUFFER_SIZE);
		transmitIndex = 0;
		receiveIndex = 0;
		
        /*
         * Allocate irq handler stack
         */
        // stack = MemoryManager.newContiguousIntArray(STACK_SIZE); // new
        // int[STACK_SIZE];
        /*
         * Put in the sentinel
         */
        // stack[STACK_SIZE-1] = 0; // IP = 0
        // stack[STACK_SIZE-2] = 0; // FP = 0
        // stack[STACK_SIZE-3] = 0; // cmid = 0
        
        /*
         * On a stack switch, the new stack is popped so need to count for this
         * in the stackTop field. This space will contain the interrupted thread's
         * stack pointer.
         */
        // stackTop = Magic.objectAsAddress(stack).plus((STACK_SIZE-4)<<2);
        
        outputStream = new SerialOutputStream(this);
        VM.sysWriteln("output stream: ", Magic.objectAsAddress(outputStream));
        printStream = new PrintStream(outputStream);
        VM.sysWriteln("print stream: ", Magic.objectAsAddress(printStream));
	}
	
	private class SerialOutputStream extends OutputStream {
	    PcSerialPort serialPort;
	    
	    public SerialOutputStream(PcSerialPort port)
	    {
	        super();
	        serialPort = port;
	    }
	    
        /* (non-Javadoc)
         * @see java.io.OutputStream#write(int)
         */
        @Override
        public void write(int b) throws IOException
        {
            serialPort.write((char)b);
        }
	    
	}
	
	public PrintStream getPrintStream()
	{
	    return printStream;
	}
	/**
     * Disable all uart interrupts
     */
    private void disableInterrupts()
    {
       comPort.ioStore(IER, 0);
    }

    @Uninterruptible
    public final void handler()
	{
	    int interruptId;
	    
	    interruptId = comPort.ioLoadByte(IIR);
	    if((interruptId & 0x1) == 0x1)
	    {
	        // Either com3 or spurious interrupt
	        return;
	    }
	    if(interruptId == IIR_RLSI)
	    {
	        // Line status interrupt; read the LSR register to reset interrupt
	        int lsrRegister = comPort.ioLoadByte(LSR);
	        if((lsrRegister & LSR_BI) != 0)
	        {
	            breakCount++;
	        }
	        if((lsrRegister & LSR_FE) != 0)
	        {
	            framingError++;
	        }
	        if((lsrRegister & LSR_PE) != 0)
	        {
	            parityError++;
	        }
	        if((lsrRegister & LSR_OE) != 0)
	        {
	            overrunError++;
	        }
	    }
	    if(interruptId == IIR_THREI)
	    {
	        // Transmit register empty
	        if(transmitBuffer.isEmpty())
	        {
	            disableTransmitInterrupts();
	        }
	        else
	        {
	            char val = transmitBuffer.remove();
	            comPort.ioStore(THR, val);
	        }
	    }
	    if(interruptId == IIR_RDAI)
	    {
	        // reset interrupt by reading the RBR register
	        comPort.ioLoadByte(RBR);
	    }
	    if(interruptId == IIR_MSI)
	    {
	        // reset the interrupt
	        comPort.ioLoadByte(MSR);
	    }
//	    Platform.masterPic.eoi();
	}
	
    /**
     * Disable the transmit holding register empty interrupt
     */
    final private void disableTransmitInterrupts()
    {
        byte ierReg = comPort.ioLoadByte(IER);
        ierReg &= ~IER_THREI;
        comPort.ioStore(IER, ierReg);
        
    }

    public Address getHandlerStack()
    {
        
        return stackTop;
    }
	
	public char read() 
	{
		return comPort.ioLoadChar(RBR);
	}
	
	/**
	 * Enable the transmit holding register empty interrupt
	 */
    @Uninterruptible
	private void enableTransmitInterrupts()
	{
        byte ierReg = comPort.ioLoadByte(IER);
        ierReg |= IER_THREI;
        comPort.ioStore(IER, ierReg);
	}
    
    
	public void write(char val) 
	{
	    int retry = 0;
	    
	    if(transmitBuffer.isEmpty() == false)
	    {
	        transmitBuffer.add(val);
	        enableTransmitInterrupts();
	        return;
	    }
		while((comPort.ioLoadByte(LSR) & LSR_TEMT) == 0 && retry <= MAX_RETRY)
		{
			retry++;
		}
		if((comPort.ioLoadByte(LSR) & LSR_TEMT) == 0 && retry <= MAX_RETRY)
		{
		    transmitBuffer.add(val);
		    enableTransmitInterrupts();
		    return;
		}
		comPort.ioStore(THR, val);
	}
	
	public void setBaudRate(SerialPortBaudRate baudRate)
	{
		byte lcr = comPort.ioLoadByte(LCR);
		// Set the DLAB bit
		lcr |= LCR_DLAB;
		comPort.ioStore(LCR, lcr);
		comPort.ioStore(DLL, baudRate.getDivisor() & 0xFF);
		comPort.ioStore(DLH, (baudRate.getDivisor() & 0xFF00) >> 8);
		lcr &= ~LCR_DLAB;
		comPort.ioStore(LCR, lcr);
	}
	
	public void setParityOdd() 
	{
		byte lcr = comPort.ioLoadByte(LCR);
		lcr |= (LCR_PEN|LCR_EPS);
		comPort.ioStore(LCR, lcr);
	}
	
	public void setParityEven() 
	{
		byte lcr = comPort.ioLoadByte(LCR);
		lcr &= ~LCR_EPS;
		lcr |= LCR_PEN;
		comPort.ioStore(LCR, lcr);
	}
	
	public void setParity(SerialPortParity parity) 
	{
		if(parity == SerialPortParity.EVEN)
		{
			setParityEven();
		}
		else if(parity == SerialPortParity.ODD)
		{
			setParityOdd();
		}
		else if(parity == SerialPortParity.NONE)
		{
			setParityNone();
		}
	}
	
	public void setParityNone() 
	{
		byte lcr = comPort.ioLoadByte(LCR);
		lcr &= ~LCR_PEN;
		comPort.ioStore(LCR, lcr);
	}

	public void setWordLength8()
	{
		byte lcr = comPort.ioLoadByte(LCR);
		lcr &= ~LCR_WL_MASK;
		lcr |= LCR_WL_8BITS;
		comPort.ioStore(LCR, lcr);
	}
	
	public void setWordLength7()
	{
		byte lcr = comPort.ioLoadByte(LCR);
		lcr &= ~LCR_WL_MASK;
		lcr |= LCR_WL_7BITS;
		comPort.ioStore(LCR, lcr);
	}
	
	public void setWordLength6()
	{
		byte lcr = comPort.ioLoadByte(LCR);
		lcr &= ~LCR_WL_MASK;
		lcr |= LCR_WL_6BITS;
		comPort.ioStore(LCR, lcr);
	}
	
	public void setWordLength5()
	{
		byte lcr = comPort.ioLoadByte(LCR);
		lcr &= ~LCR_WL_MASK;
		lcr |= LCR_WL_5BITS;
		comPort.ioStore(LCR, lcr);
	}
	
	public void setConfig(SerialPortConfig config) {
		
	}
}
