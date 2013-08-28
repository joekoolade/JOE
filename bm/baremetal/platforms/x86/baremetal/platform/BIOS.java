package baremetal.platform;
/*
 * Created on Oct 14, 2003
 *
 * Copyright (C) Joe Kulig, 2003
 * All rights reserved.
 * 
 */

/**
 * @author Joe Kulig
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public final class BIOS {
	
	public static void writeCharacter(int character) {
		baremetal_writeCharacter(character);
	}
	
	public static void writeString(String s) {
		baremetal_writeString(s.toCharArray());
	}

	private static void baremetal_writeString(char[] cs) {
	}

	private static void baremetal_writeCharacter(int character) {
	}
	private static void baremetal_setCursorType(int type) {
	}
	private static void baremetal_setCursorPosition(int page, int row, int column){
	}
}
