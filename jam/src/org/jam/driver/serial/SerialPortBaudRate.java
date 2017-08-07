package org.jam.driver.serial;

public enum SerialPortBaudRate {
	BAUDRATE_300(384),
	BAUDRATE_600(192),
	BAUDRATE_1200(96),
	BAUDRATE_2400(48),
	BAUDRATE_4800(24),
	BAUDRATE_9600(12),
	BAUDRATE_19200(6),
	BAUDRATE_38400(3),
	BAUDRATE_57600(2),
	BAUDRATE_115200(1);

	private SerialPortBaudRate(int divisor)
	{
		baudRateDivisor = divisor;
	}
	
	private final int baudRateDivisor;

	public int getDivisor() {
		// TODO Auto-generated method stub
		return baudRateDivisor;
	}
}
