/**
 * Author: Joe Kulig
 * Created: Sep 19, 2013
 *
 * Copyright 2013, Joe Kulig
 * ALL RIGHTS RESERVED.
 */
package org.jam.driver.console;

import org.jam.driver.bus.LocalBus;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Offset;

/**
 * 
 * @author jkulig
 *
 */
public class PcBootConsoleDevice {
	final public static int WIDTH = 80;
	final public static int LINES = 25;
	public static int[] attributeBuffer = new int[WIDTH*LINES];
	public static int charAttrib;
	public static int mode = 3;
	public static Address screen = Address.fromIntZeroExtend(0xb8000);
	public static Offset current = Offset.zero();
	public static int x;
	public static int y;
	public static int[] buffer = new int[WIDTH*LINES];		// the text frame buffer
	public static int position;
	public static boolean scrollUp;
	
	static {
		setForeground(VgaColor.WHITE);
		setBackground(VgaColor.BLACK);
		initializeAttributeBuffer();
	};

	static public void initializeAttributeBuffer() {
		for(int i=0; i<attributeBuffer.length; i++) {
			attributeBuffer[i] = charAttrib;
		}
	}

	public static void putChar(char c) {
		attributeBuffer[x + y*WIDTH] = charAttrib;
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
			if(y > (LINES-1)) {
				// At the bottom row. Need scroll one line up
				scrollUp=true;
				y--;
			}
		} else {
		
		/*
		 * Put character into the buffer
		 */
		buffer[x + y*WIDTH] = c;
		/*
		 * Advance to next column
		 */
		x++;
		// book keeping; keep track of x,y positioning and current screen buffer offset
		if(x > WIDTH) {
			// set y to first column
			x = 0;
			// position x to the next row
			y++;
			if(y > LINES) {
				// At the bottom row. Need scroll one line up
				scrollUp=true;
			}
		}
		}
		/*
		 * Advance current pointer on a new line
		 */
		if(c == '\n') {
			current=Offset.fromIntZeroExtend(y*WIDTH*2);
		} else {
			screen.store((byte)c, current);
			current.plus(1);
			// Write to screen buffer
			screen.store(charAttrib, current);
			current.plus(1);
		}
		if(scrollUp) {
			scrollUp(1);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.jam.driver.console.ConsoleDevice#scrollUp(int)
	 */
	public static void scrollUp(int lines) {
		/*
		 * scroll up the attribute buffer; lines 0-23
		 */
		for(int i=0; i<(LINES-1)*WIDTH; i++) {
			attributeBuffer[i] = attributeBuffer[i+(lines*WIDTH)];
		}
		/*
		 * move lines 1-24 to 0-23
		 */
		for(int i=0; i<(LINES-1)*WIDTH; i++) {
			buffer[i] = buffer[i+(lines*WIDTH)];
		}
		// Erase the last line, 24,  with spaces
		for(int i=(LINES-1)*WIDTH; i<LINES*WIDTH; i++) {
			buffer[i] = (int)' ';
		}
		/*
		 * Write out the buffer
		 */
		current = Offset.zero();
		for(int i=0; i<LINES*WIDTH; i++) {
			screen.store((byte)buffer[i], current);
			current.plus(1);
			// Write to screen buffer
			screen.store(attributeBuffer[i], current);
			current.plus(1);
		}
		/*
		 * Set new current position
		 */
		current = Offset.fromIntZeroExtend((x + y*WIDTH)*2);
		scrollUp = false;
	}
	
	public static void clear() {
		/*
		 * Reset the attribute buffer to the current attribute
		 */
		for(int i=0; i<attributeBuffer.length; i++) {
			attributeBuffer[i] = charAttrib;
		}
		/*
		 * Put spaces into the buffer
		 */
		for (int i=0; i < buffer.length; i++) {
			buffer[i] = (int)' ';
		}
		
		current = Offset.zero();
		for(int i=0; i<buffer.length; i++) {
			screen.store(buffer[i], current);
			current.plus(1);
			screen.store(attributeBuffer[i], current);
			current.plus(1);
		}
		current = Offset.zero();
	}

	public static void setCursor(int x, int y) {
		PcBootConsoleDevice.x = x;
		PcBootConsoleDevice.y = y;
	}

	public static void setForeground(VgaColor color) {
		charAttrib &= 0xf0;
		charAttrib |= color.foregroundColor();
	}
	
	public static void setBackground(VgaColor color) {
		charAttrib &= 0x0f;
		charAttrib |= color.backgroundColor();
	}

}
