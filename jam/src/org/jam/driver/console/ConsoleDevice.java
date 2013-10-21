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
		/*
		 * Advance to next column
		 */
		y++;
		/*
		 * Check for a newline
		 */
		if(c == '\n') {
			// reset y to first column
			y = 0;
			// position x to the next row
			x++;
			if(x > lines) {
				// At the bottom row. Need scroll one line up
				scrollUp(1);
			}
		}
		// book keeping; keep track of x,y positioning and current screen buffer offset
		y++;  // advance to the next column
		if(y > columns) {
			// set y to first column
			y = 0;
			// position x to the next row
			x++;
			if(x > lines) {
				// At the bottom row. Need scroll one line up
				scrollUp(1);
			}
		}
	}
	
	public void scrollUp(int lines) {
		for(int i=0; i<(this.lines-1)*columns; i++) {
			buffer[i] = buffer[i+(lines*columns)];
		}
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
