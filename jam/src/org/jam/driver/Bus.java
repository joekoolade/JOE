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
public abstract class Bus implements BusOperations {
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
}
