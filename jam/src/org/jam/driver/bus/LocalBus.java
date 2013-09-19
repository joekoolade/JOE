/**
 * Author: Joe Kulig
 * Created: Sep 19, 2013
 *
 * Copyright 2013, Joe Kulig
 * ALL RIGHTS RESERVED.
 */
package org.jam.driver.bus;

import org.jam.driver.Bus;
import org.vmmagic.unboxed.Address;

/**
 * Implements a local memory bus. It provides direct access to devices on this bus.
 *   
 * @author jkulig
 *
 */
public class LocalBus extends Bus {

	LocalBus() {
		super(null);
	}

	@Override
	public int readInt(Address a) {
		
		return a.loadInt();
	}

	@Override
	public short readShort(Address a) {
		return a.loadShort();
	}

	@Override
	public byte readByte(Address a) {
		return a.loadByte();
	}

	@Override
	public long readLong(Address a) {
		return a.loadLong();
	}

	@Override
	public void writeInt(Address a, int value) {
		a.store(value);
	}

	@Override
	public void writeShort(Address a, short value) {
		a.store(value);
	}

	@Override
	public void writeByte(Address a, byte value) {
		a.store(value);
	}

	@Override
	public void writeLong(Address a, long value) {
		a.store(value);
	}

	
}
