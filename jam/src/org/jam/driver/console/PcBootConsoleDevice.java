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
	final public static int width = 80;
	final public static int height = 25;
	public static int[] attributeBuffer = new int[width*height];
	public static int charAttrib;
	public static int mode = 3;
	public static Address screen = Address.fromIntZeroExtend(0xb8000);
	public static Offset current = Offset.zero();
	public static int x;
	public static int y;
	public static int columns;
	public static int lines;
	public static int[] buffer = new int[width*height];		// the text frame buffer
	public static int position;
	public static boolean scrollUp;
	
	static {
		setForeground(VgaColor.WHITE);
		setBackground(VgaColor.BLACK);
		initializeAttributeBuffer();
	};

	static void initializeAttributeBuffer() {
		for(int i=0; i<attributeBuffer.length; i++) {
			attributeBuffer[i] = charAttrib;
		}
	}

	public static void putChar(char c) {
		attributeBuffer[x + y*columns] = charAttrib;
//		super.putChar(c);
		/*
		 * Advance current pointer on a new line
		 */
		if(c == '\n') {
			current=Offset.fromIntZeroExtend(y*columns*2);
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
		for(int i=0; i<(lines-1)*columns; i++) {
			attributeBuffer[i] = attributeBuffer[i+(lines*columns)];
		}
		/*
		 * Write out the buffer
		 */
		current = Offset.zero();
		for(int i=0; i<lines*columns; i++) {
			screen.store((byte)buffer[i], current);
			current.plus(1);
			// Write to screen buffer
			screen.store(attributeBuffer[i], current);
			current.plus(1);
		}
		/*
		 * Set new current position
		 */
		current = Offset.fromIntZeroExtend((x + y*columns)*2);
		scrollUp = false;
	}
	
	public static void clear() {
		/*
		 * Reset the attribute buffer to the current attribute
		 */
		for(int i=0; i<attributeBuffer.length; i++) {
			attributeBuffer[i] = charAttrib;
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
