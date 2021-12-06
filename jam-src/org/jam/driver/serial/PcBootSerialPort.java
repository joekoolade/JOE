package org.jam.driver.serial;

import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Offset;

public class PcBootSerialPort {
	static Address comPort = Address.fromIntZeroExtend(0x3F8);
	static final private Offset RBR = PcSerialPortRegister.RBR.getOffset();
	static final private Offset THR = PcSerialPortRegister.THR.getOffset();
	static final private Offset LSR = PcSerialPortRegister.LSR.getOffset();
	static final private Offset LCR = PcSerialPortRegister.LCR.getOffset();
	static final private Offset DLL = PcSerialPortRegister.DLL.getOffset();
	static final private Offset DLH = PcSerialPortRegister.DLH.getOffset();
	
	static final private int LSR_TEMT = 0x40;		// empty data holding register
	@SuppressWarnings("unused")
	static final private int LSR_THRE = 0x20;		// empty transmitter holding register
	
	static final private int LCR_DLAB = 0x80;		// Divisor latch access
	@SuppressWarnings("unused")
	static final private int LCR_SB   = 0x40;		// break
	@SuppressWarnings("unused")
	static final private int LCR_SP   = 0x20;		// sticky parity
	static final private int LCR_EPS  = 0x10;		// even parity select
	static final private int LCR_PEN  = 0x08;		// parity enable
	static final private int LCR_STB  = 0x04;		// stop bits
	static final private int LCR_WL_MASK = 0x3;
	static final private int LCR_WL_8BITS = 0x3;
	static final private int LCR_WL_7BITS = 0x2;
	static final private int LCR_WL_6BITS = 0x1;
	static final private int LCR_WL_5BITS = 0x0;
	
	static public char read() 
	{
		return comPort.ioLoadChar(RBR);
	}
	
	@Uninterruptible
	static public void write(char val) 
	{
		byte status = comPort.ioLoadByte(LSR);
		while((status & LSR_TEMT) == 0)
		{
			// wait until the transmitter is empty
			status = comPort.ioLoadByte(LSR);
		}
		comPort.ioStore(THR, val);
	}
	
	@Uninterruptible
	static public void putChar(char val)
	{
		write(val);
	}
	
	static public void setBaudRate(SerialPortBaudRate baudRate)
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
	
	static public void setParityOdd() 
	{
		byte lcr = comPort.ioLoadByte(LCR);
		lcr |= (LCR_PEN|LCR_EPS);
		comPort.ioStore(LCR, lcr);
	}
	
	static public void setParityEven() 
	{
		byte lcr = comPort.ioLoadByte(LCR);
		lcr &= ~LCR_EPS;
		lcr |= LCR_PEN;
		comPort.ioStore(LCR, lcr);
	}
	
	static public void setParity(SerialPortParity parity) 
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
	
	static public void setParityNone() 
	{
		byte lcr = comPort.ioLoadByte(LCR);
		lcr &= ~LCR_PEN;
		comPort.ioStore(LCR, lcr);
	}

	static public void setWordLength8()
	{
		byte lcr = comPort.ioLoadByte(LCR);
		lcr &= ~LCR_STB;
		comPort.ioStore(LCR, lcr);
	}
	
	static public void setWordLength7()
	{
		byte lcr = comPort.ioLoadByte(LCR);
		lcr &= ~LCR_WL_MASK;
		lcr |= LCR_WL_7BITS;
		comPort.ioStore(LCR, lcr);
	}
	
	static public void setWordLength6()
	{
		byte lcr = comPort.ioLoadByte(LCR);
		lcr &= ~LCR_WL_MASK;
		lcr |= LCR_WL_6BITS;
		comPort.ioStore(LCR, lcr);
	}
	
	static public void setWordLength5()
	{
		byte lcr = comPort.ioLoadByte(LCR);
		lcr &= ~LCR_WL_MASK;
		lcr |= LCR_WL_5BITS;
		comPort.ioStore(LCR, lcr);
	}
	
	static public void setStopBits1()
	{
		byte lcr = comPort.ioLoadByte(LCR);
		lcr &= ~LCR_WL_MASK;
		lcr |= LCR_WL_5BITS;
		comPort.ioStore(LCR, lcr);
	}
}
