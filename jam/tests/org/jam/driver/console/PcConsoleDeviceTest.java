/**
 * Author: Joe Kulig
 * Created: Oct 28, 2013
 *
 * Copyright 2013, Joe Kulig
 * ALL RIGHTS RESERVED.
 */
package org.jam.driver.console;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.org.apache.bcel.internal.classfile.Attribute;

/**
 * @author jkulig
 *
 */
public class PcConsoleDeviceTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.jam.driver.console.PcConsoleDevice#putChar(char)}.
	 */
	@Test
	public void testPutChar() {
		PcConsoleDevice cut = new PcConsoleDevice(80, 25);
		cut.setForeground(VgaColor.CYAN);
		cut.setBackground(VgaColor.LT_GREEN);
		cut.putChar('a');
		
	}

	/**
	 * Test method for {@link org.jam.driver.console.PcConsoleDevice#scrollUp(int)}.
	 */
	@Test
	public void testScrollUp() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.jam.driver.console.PcConsoleDevice#clear()}.
	 */
	@Test
	public void testClear() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.jam.driver.console.PcConsoleDevice#setCursor(int, int)}.
	 */
	@Test
	public void testSetCursor() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.jam.driver.console.PcConsoleDevice#PcConsoleDevice(int, int)}.
	 */
	@Test
	public void testPcConsoleDevice() {
		PcConsoleDevice cut = new PcConsoleDevice(80, 25);
		assertEquals(cut.lines*cut.columns, cut.attributeBuffer.length);
		assertEquals(0x0f, cut.charAttrib);
		assertEquals(3, cut.mode);
	}

	/**
	 * Test method for {@link org.jam.driver.console.PcConsoleDevice#setMode(int)}.
	 */
	@Test
	public void testSetMode() {
		PcConsoleDevice cut = new PcConsoleDevice(80, 25);
		assertEquals(3, cut.mode);
		cut.setMode(4);
		assertEquals(4, cut.mode);
		
	}

	/**
	 * Test method for {@link org.jam.driver.console.PcConsoleDevice#setForeground(org.jam.driver.console.VgaColor)}.
	 */
	@Test
	public void testSetForeground() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.jam.driver.console.PcConsoleDevice#setBackground(org.jam.driver.console.VgaColor)}.
	 */
	@Test
	public void testSetBackground() {
		fail("Not yet implemented");
	}

}
