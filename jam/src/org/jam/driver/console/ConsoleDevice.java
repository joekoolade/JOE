/**
 * Author: Joe Kulig
 * Created: Sep 19, 2013
 *
 * Copyright 2013, Joe Kulig
 * ALL RIGHTS RESERVED.
 */
package org.jam.driver.console;

import org.jam.driver.Bus;
import org.jam.driver.Device;

/**
 * @author jkulig
 *
 */
public abstract class ConsoleDevice extends Device {
	int x;
	int y;
	int width;
	int height;
	int[] buffer;		// the text frame buffer
	int position;
	
	public ConsoleDevice(Bus bus, int width, int height) {
		super(bus, "ConsoleDevice");
		this.width = width;
		this.height = height;
		buffer = new int[width*height];
	}
	abstract public void setMode(int mode);
	public void putChar(char c) {
		
	}
	
	/**
	 * Clear out the screen
	 */
	public void clear() {
		/*
		 * Put spaces into the buffer
		 */
		for (int i=0; i < buffer.length; i++) {
			buffer[i] = (int)' ';
		}
	}
	abstract public void setCursor(int x, int y);
	
}
