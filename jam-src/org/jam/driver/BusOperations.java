package org.jam.driver;

import org.vmmagic.unboxed.Address;

public interface BusOperations {
	public int readInt(Address a);
	public short readShort(Address a);
	public byte readByte(Address a);
	public long readLong(Address a);
	public void writeInt(Address a, int value);
	public void writeShort(Address a, short value);
	public void writeByte(Address a, byte value);
	public void writeLong(Address a, long value);
}
