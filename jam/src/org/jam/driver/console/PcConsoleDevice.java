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
		clear();
		screen = Address.fromIntZeroExtend(0xb8000);
		current = Offset.zero();
		columns = 0;
	}

	/* (non-Javadoc)
	 * @see org.jam.driver.console.ConsoleDevice#setMode(int)
	 */
	public void setMode(int mode) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.jam.driver.console.ConsoleDevice#putChar(char)
	 */
	@Override
	public void putChar(char c) {
		attributeBuffer[x + y*columns] = charAttrib;
		super.putChar(c);
		current.plus(1);
		// Write to screen buffer
		screen.store(charAttrib, current);
	}

	public void scrollUp(int lines) {
		for(int i=0; i<(this.lines-1)*columns; i++) {
			attributeBuffer[i] = attributeBuffer[i+(lines*columns)];
		}
		
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
		
		current.fromIntSignExtend(0);
		for(int i=0; i<buffer.length; i++) {
			
		}
	}

	/* (non-Javadoc)
	 * @see org.jam.driver.console.ConsoleDevice#setCursor(int, int)
	 */
	@Override
	public void setCursor(int x, int y) {
		super.setCursor(x, y);
	}

	public void setForeground(VgaColor color) {
		charAttrib |= color.foregroundColor();
	}
	
	public void setBackground(VgaColor color) {
		charAttrib |= color.backgroundColor();
	}

}
