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
	boolean scrollUp;
	
	public ConsoleDevice(Bus bus, int columns, int lines) {
		super(bus, "ConsoleDevice");
		this.columns = columns;
		this.lines = lines;
		buffer = new int[columns*lines];
	}
	
	public boolean doScrollUp() {
		return scrollUp;
	}
	
	public void putChar(char c) {
		/*
		 * Reset scrollup
		 */
		scrollUp=false;
		/*
		 * Check for a newline
		 */
		if(c == '\n') {
			// reset y to first column
			x = 0;
			// position x to the next row
			y++;
			if(y > lines) {
				// At the bottom row. Need scroll one line up
				scrollUp=true;
				y--;
			}
			return;
		}
		
		/*
		 * Put character into the buffer
		 */
		buffer[x + y*columns] = c;
		/*
		 * Advance to next column
		 */
		x++;
		// book keeping; keep track of x,y positioning and current screen buffer offset
		if(x > columns) {
			// set y to first column
			x = 0;
			// position x to the next row
			y++;
			if(y > lines) {
				// At the bottom row. Need scroll one line up
				scrollUp=true;
			}
		}
	}
	
	public void scrollUp(int lines) {
		for(int i=0; i<(this.lines-1)*columns; i++) {
			buffer[i] = buffer[i+(lines*columns)];
		}
		// Erase the last line with spaces
		for(int i=(this.lines-1)*columns; i<this.lines*columns; i++) {
			buffer[i] = (int)' ';
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
		/*
		 * For now just clamp the x,y when over or under
		 */
		if(x>columns) {
			x=columns;
		}
		if(x<0) {
			x=0;
		}
		if(y>lines) {
			y=lines;
		}
		if(y<0) {
			y=0;
		}
		this.x = x;
		this.y = y;
	}
	
}
