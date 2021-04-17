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
 * @author jkulig
 *
 */
public class PcConsoleDevice extends ConsoleDevice {
	int[] attributeBuffer;
	int charAttrib;
	int mode;
	Address screen;
	Offset current;
	
	public PcConsoleDevice(int width, int height) {
		super(new LocalBus(), width, height);
		attributeBuffer = new int[width*height];
		setForeground(VgaColor.WHITE);
		setBackground(VgaColor.BLACK);
		screen = Address.fromIntZeroExtend(0xb8000);
		current = Offset.zero();
		mode = 3;
		initializeAttributeBuffer();
	}

	void initializeAttributeBuffer() {
		for(int i=0; i<attributeBuffer.length; i++) {
			attributeBuffer[i] = charAttrib;
		}
	}
	/* (non-Javadoc)
	 * @see org.jam.driver.console.ConsoleDevice#setMode(int)
	 */
	public void setMode(int mode) {
		this.mode = mode;
	}

	/* (non-Javadoc)
	 * @see org.jam.driver.console.ConsoleDevice#putChar(char)
	 */
	@Override
	public void putChar(char c) {
		attributeBuffer[x + y*columns] = charAttrib;
		super.putChar(c);
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
		if(doScrollUp()) {
			scrollUp(1);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.jam.driver.console.ConsoleDevice#scrollUp(int)
	 */
	public void scrollUp(int lines) {
		/*
		 * scroll up the attribute buffer; lines 0-23
		 */
		for(int i=0; i<(this.lines-1)*columns; i++) {
			attributeBuffer[i] = attributeBuffer[i+(lines*columns)];
		}
		super.scrollUp(lines);
		/*
		 * Write out the buffer
		 */
		current = Offset.zero();
		for(int i=0; i<this.lines*columns; i++) {
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
	
	/* (non-Javadoc)
	 * @see org.jam.driver.console.ConsoleDevice#clear()
	 */
	@Override
	public void clear() {
		super.clear();
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

	/* (non-Javadoc)
	 * @see org.jam.driver.console.ConsoleDevice#setCursor(int, int)
	 */
	@Override
	public void setCursor(int x, int y) {
		super.setCursor(x, y);
	}

	public void setForeground(VgaColor color) {
		charAttrib &= 0xf0;
		charAttrib |= color.foregroundColor();
	}
	
	public void setBackground(VgaColor color) {
		charAttrib &= 0x0f;
		charAttrib |= color.backgroundColor();
	}

}
