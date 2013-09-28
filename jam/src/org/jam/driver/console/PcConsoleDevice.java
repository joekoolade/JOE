/**
 * Author: Joe Kulig
 * Created: Sep 19, 2013
 *
 * Copyright 2013, Joe Kulig
 * ALL RIGHTS RESERVED.
 */
package org.jam.driver.console;

import org.jam.driver.bus.LocalBus;

/**
 * @author jkulig
 *
 */
public class PcConsoleDevice extends ConsoleDevice {
	int[] attributeBuffer;
	int charAttrib;
	int mode;
	
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
	
	public PcConsoleDevice(int width, int height) {
		super(new LocalBus(), width, height);
		attributeBuffer = new int[width*height];
	}

	/* (non-Javadoc)
	 * @see org.jam.driver.console.ConsoleDevice#setMode(int)
	 */
	@Override
	public void setMode(int mode) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.jam.driver.console.ConsoleDevice#putChar(char)
	 */
	@Override
	public void putChar(char c) {
		super.putChar(c);
		
	}

	/* (non-Javadoc)
	 * @see org.jam.driver.console.ConsoleDevice#clear()
	 */
	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.jam.driver.console.ConsoleDevice#setCursor(int, int)
	 */
	@Override
	public void setCursor(int x, int y) {
		// TODO Auto-generated method stub

	}

	public void setForeground(int color) {
		charAttrib = color;
	}
	
	public void setBackground(int color) {
		charAttrib |= (color<<4);
	}

}
