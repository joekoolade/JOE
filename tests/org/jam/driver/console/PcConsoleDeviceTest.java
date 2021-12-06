/**
 * Author: Joe Kulig
 * Created: Oct 28, 2013
 *
 * Copyright 2013, Joe Kulig
 * ALL RIGHTS RESERVED.
 */
package org.jam.driver.console;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.powermock.api.easymock.PowerMock.*;
import static org.easymock.EasyMock.expect;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Offset;

/**
 * @author jkulig
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Address.class, Offset.class})
public class PcConsoleDeviceTest {

	private Offset currentMock;
	private Address screenMock;

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
		replayConsoleDeviceCreation();
		screenMock.store((byte)'a',currentMock);
		expect(currentMock.plus(1)).andReturn(currentMock);
		screenMock.store(0xa3,currentMock);
		expect(currentMock.plus(1)).andReturn(currentMock);
		replayAll();
		
		PcConsoleDevice cut = new PcConsoleDevice(80, 25);
		cut.setForeground(VgaColor.CYAN);
		cut.setBackground(VgaColor.LT_GREEN);
		cut.putChar('a');
		verifyAll();
	}

	@Test
	public void testNewLine() {
		replayConsoleDeviceCreation();
		expect(Offset.fromIntZeroExtend(160)).andReturn(currentMock);
		replayAll();
		PcConsoleDevice cut = new PcConsoleDevice(80, 25);
		cut.putChar('\n');
		verifyAll();
	
	}
	/**
	 * Test method for {@link org.jam.driver.console.PcConsoleDevice#scrollUp(int)}.
	 */
	@Test
	public void testScrollUp() {
		replayConsoleDeviceCreation();
		expect(Offset.fromIntZeroExtend(24*80*2)).andReturn(currentMock);
		expect(Offset.zero()).andReturn(currentMock);
		for(int i=0; i<24*80; i++) {
			screenMock.store((byte)0, currentMock);
			expect(currentMock.plus(1)).andReturn(currentMock);
			// Write to screen buffer
			screenMock.store(15, currentMock);
			expect(currentMock.plus(1)).andReturn(currentMock);
		}
		for(int i=0; i<80; i++) {
			screenMock.store((byte)32, currentMock);
			expect(currentMock.plus(1)).andReturn(currentMock);
			// Write to screen buffer
			screenMock.store(15, currentMock);
			expect(currentMock.plus(1)).andReturn(currentMock);
		}
	
		expect(Offset.fromIntZeroExtend(24*80*2)).andReturn(currentMock);
		replayAll();
		
		PcConsoleDevice cut = new PcConsoleDevice(80, 25);
		cut.setCursor(40, 24);
		cut.putChar('\n');
		verifyAll();
		assertEquals("scrollup should be false!", false, cut.scrollUp);
	}

	/**
	 * Test method for {@link org.jam.driver.console.PcConsoleDevice#clear()}.
	 */
	@Test
	public void testClear() {
		replayConsoleDeviceCreation();
		expect(Offset.zero()).andReturn(currentMock);
		for(int i=0; i<80*25; i++) {
			screenMock.store(32, currentMock);
			expect(currentMock.plus(1)).andReturn(currentMock);
			screenMock.store(0xf, currentMock);
			expect(currentMock.plus(1)).andReturn(currentMock);
		}
		expect(Offset.zero()).andReturn(currentMock);
		replayAll();
		PcConsoleDevice cut = new PcConsoleDevice(80, 25);
		cut.clear();
		int[] expectedBuffer = new int[80*25];
		int[] expectedAttrib = new int[80*25];
		Arrays.fill(expectedBuffer, 32);
		Arrays.fill(expectedAttrib, 0xf);
		assertArrayEquals("Buffer not cleared", expectedBuffer, cut.buffer);
		assertArrayEquals("Attributes not cleared", expectedAttrib, cut.attributeBuffer);
		verifyAll();
	}

	private void replayConsoleDeviceCreation() {
		mockStatic(Address.class);
		mockStatic(Offset.class);
		screenMock = createMock(Address.class);
		currentMock = createMock(Offset.class);
		expect(Address.fromIntZeroExtend(0xb8000)).andReturn(screenMock);
		expect(Offset.zero()).andReturn(currentMock);
	}

	/**
	 * Test method for {@link org.jam.driver.console.PcConsoleDevice#setCursor(int, int)}.
	 */
	@Test
	public void testSetCursor() {
		PcConsoleDevice cut = new PcConsoleDevice(80, 25);
		assertEquals("x default", 0, cut.x);
		assertEquals("y default", 0, cut.y);
		cut.setCursor(40, 24);
		assertEquals("x", 40, cut.x);
		assertEquals("y", 24, cut.y);
	}

	/**
	 * Test method for {@link org.jam.driver.console.PcConsoleDevice#PcConsoleDevice(int, int)}.
	 */
	@Test
	public void testPcConsoleDevice() {
		mockStatic(Address.class);
		mockStatic(Offset.class);
		expect(Address.fromIntZeroExtend(0xb8000)).andReturn(null);
		expect(Offset.zero()).andReturn(null);
		replay(Address.class, Offset.class);
		PcConsoleDevice cut = new PcConsoleDevice(80, 25);
		verify(Address.class, Offset.class);
		assertEquals("Wrong buffer length!", cut.lines*cut.columns, cut.attributeBuffer.length);
		assertEquals("Wrong attribute!", 0x0f, cut.charAttrib);
		assertEquals("Wrong mode!", 3, cut.mode);
		int[] expectedAttributeBuffer = new int[cut.lines*cut.columns];
		Arrays.fill(expectedAttributeBuffer, 0xf);
		assertArrayEquals("default array attribute", expectedAttributeBuffer, cut.attributeBuffer);
	}

	/**
	 * Test method for {@link org.jam.driver.console.PcConsoleDevice#setMode(int)}.
	 */
	@Test
	public void testSetMode() {
		PcConsoleDevice cut = new PcConsoleDevice(80, 25);
		assertEquals("Default mode", 3, cut.mode);
		cut.setMode(4);
		assertEquals("New mode", 4, cut.mode);
		
	}

	/**
	 * Test method for {@link org.jam.driver.console.PcConsoleDevice#setForeground(org.jam.driver.console.VgaColor)}.
	 */
	@Test
	public void testSetForeground() {
		PcConsoleDevice cut = new PcConsoleDevice(80, 25);
		cut.charAttrib = 0xff;
		cut.setForeground(VgaColor.LT_BLUE);
		assertEquals(0xf9, cut.charAttrib);
	}

	/**
	 * Test method for {@link org.jam.driver.console.PcConsoleDevice#setBackground(org.jam.driver.console.VgaColor)}.
	 */
	@Test
	public void testSetBackground() {
		PcConsoleDevice cut = new PcConsoleDevice(80, 25);
		cut.charAttrib = 0xff;
		cut.setBackground(VgaColor.LT_GREEN);
		assertEquals(0xaf, cut.charAttrib);
	}

}
