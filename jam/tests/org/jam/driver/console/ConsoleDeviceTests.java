/**
 * Author: Joe Kulig
 * Created: Oct 23, 2013
 *
 * Copyright 2013, Joe Kulig
 * ALL RIGHTS RESERVED.
 */
package org.jam.driver.console;

import static org.junit.Assert.*;

import java.util.Arrays;

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

	private final String testString = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	private int emptyRow[];
	
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
		emptyRow = new int[80];
		Arrays.fill(emptyRow, (int)' ');
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
		Arrays.fill(expected, (int)' ');

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
		// Test writing one character
		cut.putChar('j');
		assertEquals(0, cut.y);
		assertEquals(1, cut.x);
		assertEquals('j', cut.buffer[0]);
		cut.putChar('k');
		assertEquals(0, cut.y);
		assertEquals(2, cut.x);
		assertEquals('j', cut.buffer[0]);
		assertEquals('k', cut.buffer[1]);
		// Test writing a new line
		cut.putChar('\n');
		assertEquals(1, cut.y);
		assertEquals(0, cut.x);
		// Test out the scrolling
		// Write out 24 lines
		char testStringChar[] = testString.toCharArray();
		for(int i=0; i<24; i++) {
			for(int j=0; j<30; j++) {
				cut.putChar(testStringChar[i]);
			}
			if(cut.y!=25) {
				cut.putChar('\n');
				assertEquals(i+2, cut.y);
				assertEquals(0, cut.x);
			}
		}
		assertEquals(25, cut.y);
		// created the expected screen data
		int expectedScreen[] = new int[80*25];
		Arrays.fill(expectedScreen, ' ');
		expectedScreen[0] = 'j';
		expectedScreen[1] = 'k';
		for(int l=1; l<25; l++) {
			Arrays.fill(expectedScreen, l*80, l*80+30, testStringChar[l-1]);
		}
		// verify screen contents
		assertArrayEquals(expectedScreen, cut.buffer);
		// write out a newline
		cut.putChar('\n');
		assertEquals(25, cut.y);
		// verify that jk scrolls off
		Arrays.fill(expectedScreen, ' ');
		for(int l=0; l<24; l++) {
			Arrays.fill(expectedScreen, l*80, l*80+30, testStringChar[l]);
		}
		assertArrayEquals(expectedScreen, cut.buffer);
		// write out another line; line 1 should scroll off
		cut.putChar('\n');
		// verify
		Arrays.fill(expectedScreen, ' ');
		for(int l=0; l<23; l++) {
			Arrays.fill(expectedScreen, l*80, l*80+30, testStringChar[l+1]);
		}
		assertArrayEquals(expectedScreen, cut.buffer);
	}

	/**
	 * Test method for {@link org.jam.driver.console.ConsoleDevice#scrollUp(int)}.
	 */
	@Test
	public void testScrollUp() {
		ConsoleDevice cut = new TestConsole(new LocalBus(), 80, 25);
		char testCharString[] = testString.toCharArray();
		for(int i=0; i<80*25; i++) {
			cut.putChar(testCharString[i%testCharString.length]);
		}
		int lines2to25[] = Arrays.copyOfRange(cut.buffer, 80, (80*25));
		
		cut.scrollUp(1);
		assertArrayEquals(lines2to25, Arrays.copyOf(cut.buffer, (80*24)));
		assertArrayEquals(emptyRow, Arrays.copyOfRange(cut.buffer, (80*24), (80*25)));
	}

	/**
	 * Test method for {@link org.jam.driver.console.ConsoleDevice#clear()}.
	 */
	@Test
	public void testClear() {
		ConsoleDevice cut = new TestConsole(new LocalBus(), 80, 25);
		char testCharString[] = testString.toCharArray();
		for(int i=0; i<80*25; i++) {
			cut.putChar(testCharString[i%testCharString.length]);
		}
		//
		cut.clear();
		// created the expected screen data
		int expectedScreen[] = new int[80*25];
		Arrays.fill(expectedScreen, ' ');
		assertArrayEquals(expectedScreen, cut.buffer);
	}

	/**
	 * Test method for {@link org.jam.driver.console.ConsoleDevice#setCursor(int, int)}.
	 */
	@Test
	public void testSetCursor() {
		ConsoleDevice cut = new TestConsole(new LocalBus(), 80, 25);
		assertEquals(0, cut.x);
		assertEquals(0, cut.y);
		cut.setCursor(10, 10);
		assertEquals(10, cut.x);
		assertEquals(10, cut.y);
		cut.setCursor(-100, -100);
		assertEquals(0, cut.x);
		assertEquals(0, cut.y);
		cut.setCursor(100, 100);
		assertEquals(cut.columns, cut.x);
		assertEquals(cut.lines, cut.y);
	}

}
