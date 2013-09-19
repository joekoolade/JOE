/**
 * Author: Joe Kulig
 * Created: Sep 19, 2013
 *
 * Copyright 2013, Joe Kulig
 * ALL RIGHTS RESERVED.
 */
package org.jam.driver;

import org.vmmagic.unboxed.Address;

/**
 * @author jkulig
 *
 */
public abstract class Bus {
	private final Bus parent;
	private final Device parentDevice;
	
	Bus() {
		this.parent = null;
		this.parentDevice = null;
	}
	
	public Bus(Bus parent) {
		this.parent = parent;
		this.parentDevice = null;
	}
	
	abstract public int readInt(Address a);
	abstract public short readShort(Address a);
	abstract public byte readByte(Address a);
	abstract public long readLong(Address a);
	abstract public void writeInt(Address a, int value);
	abstract public void writeShort(Address a, short value);
	abstract public void writeByte(Address a, byte value);
	abstract public void writeLong(Address a, long value);
	
}
