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
	int[] buffer;
	int[] attributeBuffer;
	int position;
	int charAttrib;
	
	public static final int BLACK 		= 0;
	public final static int BLUE  		= 1;
	public final static int GREEN 		= 2;
	public final static int CYAN		= 3;
	public final static int RED			= 4;
	public final static int MAGENTA 	= 5;
	public final static int BROWN		= 6;
	public final static int LT_GRAY 	= 7;
	public final static int DARK_GRAY 	= 8;
	public final static int LT_BLUE 	= 9;
	public final static int LT_GREEN	= 10;
	public final static int LT_CYAN		= 11;
	public final static int LT_RED		= 12;
	public final static int LT_MAGENTA	= 13;
	public final static int YELLOW		= 14;
	public final static int WHITE		= 15;
	
	public ConsoleDevice(Bus bus, int width, int height) {
		super(bus, "ConsoleDevice");
		this.width = width;
		this.height = height;
		buffer = new int[width*height];
		attributeBuffer = new int[width*height];
	}
	abstract public void setMode(int mode);
	public void putChar(char c) {
		
	}
	abstract public void clear();
	abstract public void setCursor(int x, int y);
	
	public void setForeground(int color) {
		charAttrib = color;
	}
	
	public void setBackground(int color) {
		charAttrib |= (color<<4);
	}
}
