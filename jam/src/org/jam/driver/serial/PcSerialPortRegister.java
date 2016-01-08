package org.jam.driver.serial;

import org.vmmagic.unboxed.Offset;

/**
 * This defines the serial port registers for a IBM PC
 * 
 * @author jkulig
 *
 */
public enum PcSerialPortRegister {
	THR(0),
	RBR(0),
	DLL(0),
	IER(1),
	DLH(1),
	IIR(2),
	LCR(3),
	MCR(4),
	LSR(5),
	SCR(7);
	
	private PcSerialPortRegister(int offset)
	{
		registerOffset = offset;
	}
	
	final Offset getOffset()
	{
		return Offset.fromIntZeroExtend(registerOffset);
	}
	private final int registerOffset;
}
