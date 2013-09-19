/**
 * Author: Joe Kulig
 * Created: Sep 19, 2013
 *
 * Copyright 2013, Joe Kulig
 * ALL RIGHTS RESERVED.
 */
package org.jam.driver.console;

import org.jam.driver.Device;

/**
 * @author jkulig
 *
 */
public abstract class ConsoleDevice extends Device {
	protected int x;
	protected int y;
	protected int width;
	protected int height;
	
	public ConsoleDevice(int width, int height) {
		super(null, "ConsoleDevice");
		this.width = width;
		this.height = height;
	}
	abstract public void setMode(int mode);
	abstract public void putChar(char c);
	abstract public void clear();
	abstract public void setCursor(int x, int y);
}
