/**
 * Author: Joe Kulig
 * Created: Oct 23, 2013
 *
 * Copyright 2013, Joe Kulig
 * ALL RIGHTS RESERVED.
 */
package org.jam.driver.console;

import static org.junit.Assert.*;

import org.jam.driver.Bus;
import org.jam.driver.bus.LocalBus;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author jkulig
 *
 */
public class ConsoleDeviceTests {

	public class TestConsole extends ConsoleDevice {
		public TestConsole(Bus bus, int columns, int lines) {
			super(bus, columns, lines);
		}
	}
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

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
	 * Test method for {@link org.jam.driver.console.ConsoleDevice#ConsoleDevice(org.jam.driver.Bus, int, int)}.
	 */
	@Test
	public void testConsoleDevice() {
		int expected[] = new int[80*25];
		for(int i=0; i<expected.length; i++) {
			expected[i] = (int)' ';
		}
		ConsoleDevice cut = new TestConsole(new LocalBus(), 80, 25);
		assertEquals(80*25, cut.buffer.length);
		assertEquals(80, cut.columns);
		assertEquals(25, cut.lines);
		assertEquals(0, cut.x);
		assertEquals(0, cut.y);
		assertEquals(0, cut.position);
		assertArrayEquals(expected, cut.buffer);
	}

	/**
	 * Test method for {@link org.jam.driver.console.ConsoleDevice#putChar(char)}.
	 */
	@Test
	public void testPutChar() {
		ConsoleDevice cut = new TestConsole(new LocalBus(), 80, 25);
		cut.putChar('j');
		assertEquals(0, cut.y);
		assertEquals(1, cut.x);
		assertEquals('j', cut.buffer[0]);
	}

	/**
	 * Test method for {@link org.jam.driver.console.ConsoleDevice#scrollUp(int)}.
	 */
	@Test
	public void testScrollUp() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.jam.driver.console.ConsoleDevice#clear()}.
	 */
	@Test
	public void testClear() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.jam.driver.console.ConsoleDevice#setCursor(int, int)}.
	 */
	@Test
	public void testSetCursor() {
		fail("Not yet implemented");
	}

}
