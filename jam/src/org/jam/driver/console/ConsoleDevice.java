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
	int columns;
	int lines;
	int[] buffer;		// the text frame buffer
	int position;
	
	public ConsoleDevice(Bus bus, int columns, int lines) {
		super(bus, "ConsoleDevice");
		this.columns = columns;
		this.lines = lines;
		buffer = new int[columns*lines];
	}
	public void putChar(char c) {
		buffer[x + y*columns] = c;
	}
	
	public void scrollUp(int lines) {
		
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
	public void setCursor(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
}
