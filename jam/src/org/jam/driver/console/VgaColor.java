/**
 * Author: Joe Kulig
 * Created: Oct 22, 2013
 *
 * Copyright 2013, Joe Kulig
 * ALL RIGHTS RESERVED.
 */
package org.jam.driver.console;

/**
 * @author jkulig
 *
 */
public enum VgaColor {
	BLACK(0), BLUE(1), GREEN(2), CYAN(3), RED(4), MAGENTA(5), BROWN(6),
	LT_GRAY(7), DARK_GRAY(8), LT_BLUE(9), LT_GREEN(10), LT_CYAN(11),
	LT_RED(12), LT_MAGENTA(13), YELLOW(14), WHITE(15);
	
	private final int color;
	
	VgaColor(int color) {
		this.color = color;
	}
	
	public int color() {
		return color;
	}
	
	public int foregroundColor() {
		return color;
	}
	
	public int backgroundColor() {
		return color<<4;
	}
}
