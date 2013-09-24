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

	public PcConsoleDevice(int width, int height) {
		super(new LocalBus(), width, height);
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

}
