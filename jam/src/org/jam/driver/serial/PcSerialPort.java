package org.jam.driver.serial;

import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Offset;

public class PcSerialPort {
	Address comPort;
	final private Offset RBR = PcSerialPortRegister.RBR.getOffset();
	final private Offset THR = PcSerialPortRegister.THR.getOffset();
	final private Offset LSR = PcSerialPortRegister.LSR.getOffset();
	final private Offset LCR = PcSerialPortRegister.LCR.getOffset();
	final private Offset DLL = PcSerialPortRegister.DLL.getOffset();
	final private Offset DLH = PcSerialPortRegister.DLH.getOffset();
	
	final private int LSR_TEMT = 0x40;		// empty data holding register
	@SuppressWarnings("unused")
	final private int LSR_THRE = 0x20;		// empty transmitter holding register
	
	final private int LCR_DLAB = 0x80;		// Divisor latch access
	@SuppressWarnings("unused")
	final private int LCR_SB   = 0x40;		// break
	@SuppressWarnings("unused")
	final private int LCR_SP   = 0x20;		// sticky parity
	final private int LCR_EPS  = 0x10;		// even parity select
	final private int LCR_PEN  = 0x08;		// parity enable
	final private int LCR_STB  = 0x04;		// stop bits
	final private int LCR_WL_MASK = 0x3;
	final private int LCR_WL_8BITS = 0x3;
	final private int LCR_WL_7BITS = 0x2;
	final private int LCR_WL_6BITS = 0x1;
	final private int LCR_WL_5BITS = 0x0;
	
	public PcSerialPort(int portAddress)
	{
		comPort = Address.fromIntZeroExtend(portAddress);
	}
	
	public char read() 
	{
		return comPort.ioLoadChar(RBR);
	}
	
	public void write(char val) 
	{
		while((comPort.ioLoadByte(LSR) & LSR_TEMT) == 0)
		{
			// wait until the transmitter is empty
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
