/*
 * Created on Oct 13, 2003
 *
 * Copyright (C) Joe Kulig, 2003
 * All rights reserved.
 * 
 */
package baremetal.kernel;

/**
 * @author Joe Kulig
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public interface MemoryIF {
	int readWord();
	int readByte();
	int readHalfWord();
	long readDoubleWord();
	void writeWord(int value);
	void writeByte(int value);
	void writeHalfWord(int value);
	void writeDoubleWord(long value);
}
