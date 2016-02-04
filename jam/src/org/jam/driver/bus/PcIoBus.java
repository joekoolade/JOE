/**
 * 
 */
package org.jam.driver.bus;

import org.jam.driver.Bus;
import org.vmmagic.unboxed.Address;

/**
 * @author Joe Kulig
 *
 */
public class PcIoBus extends Bus {

	public PcIoBus(Bus parent) {
		super(parent);
	}

	/* (non-Javadoc)
	 * @see org.jam.driver.Bus#readInt(org.vmmagic.unboxed.Address)
	 */
	@Override
	public int readInt(Address a) {
		return a.ioLoadInt();
	}

	/* (non-Javadoc)
	 * @see org.jam.driver.Bus#readShort(org.vmmagic.unboxed.Address)
	 */
	@Override
	public short readShort(Address a) {
		return a.ioLoadShort();
	}

	/* (non-Javadoc)
	 * @see org.jam.driver.Bus#readByte(org.vmmagic.unboxed.Address)
	 */
	@Override
	public byte readByte(Address a) {
		return a.loadByte();
	}

	/* (non-Javadoc)
	 * @see org.jam.driver.Bus#readLong(org.vmmagic.unboxed.Address)
	 */
	@Override
	public long readLong(Address a) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.jam.driver.Bus#writeInt(org.vmmagic.unboxed.Address, int)
	 */
	@Override
	public void writeInt(Address a, int value) {
		a.ioStore(value);
	}

	/* (non-Javadoc)
	 * @see org.jam.driver.Bus#writeShort(org.vmmagic.unboxed.Address, short)
	 */
	@Override
	public void writeShort(Address a, short value) {
		a.ioStore(value);
	}

	/* (non-Javadoc)
	 * @see org.jam.driver.Bus#writeByte(org.vmmagic.unboxed.Address, byte)
	 */
	@Override
	public void writeByte(Address a, byte value) {
		a.ioStore(value);
	}

	/* (non-Javadoc)
	 * @see org.jam.driver.Bus#writeLong(org.vmmagic.unboxed.Address, long)
	 */
	@Override
	public void writeLong(Address a, long value) {
		throw new UnsupportedOperationException();
	}

}
