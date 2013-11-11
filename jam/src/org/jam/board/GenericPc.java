/**
 * 
 */
package org.jam.board;

import org.jam.driver.console.PcConsoleDevice;
import org.jam.runtime.Platform;

/**
 * @author jkulig
 * Copyright Joe Kulig 2013
 */
public class GenericPc implements Platform {
	PcConsoleDevice console;
	
	/* (non-Javadoc)
	 * @see org.jam.runtime.Platform#putChar(char)
	 */
	@Override
	public void putChar(char value) {
		console.putChar(value);
	}

	/* (non-Javadoc)
	 * @see org.jam.runtime.Platform#boot()
	 */
	@Override
	public void boot() {
		console = new PcConsoleDevice(80, 25);
	}

}
