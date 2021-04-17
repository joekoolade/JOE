/**
 * Author: Joe Kulig
 * Created: Sep 19, 2013
 *
 * Copyright 2013, Joe Kulig
 * ALL RIGHTS RESERVED.
 */
package org.jam.driver;

/**
 * @author jkulig
 *
 */
public abstract class Device {
	/*
	 * Bus device is connected to
	 */
	private final Bus bus;
	
	/*
	 * device identifier
	 */
	private String id;
	
	private boolean started;	// default is false
	
	public Device(Bus bus, String id) {
		this.id = id;
		this.bus = bus;
	}
	
	public void start() {
		
	}
	
	public void stop() {
		
	}
}
