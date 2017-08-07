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
public class PcBootConsoleDeviceTest {

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
		resetAll();
	}

	public void resetConsole() {
		PcBootConsoleDevice.attributeBuffer = new int[PcBootConsoleDevice.WIDTH*PcBootConsoleDevice.LINES];// TODO Auto-generated method stub
		PcBootConsoleDevice.mode = 3;
		PcBootConsoleDevice.x = 0;
		PcBootConsoleDevice.y = 0;
		PcBootConsoleDevice.buffer = new int[PcBootConsoleDevice.WIDTH*PcBootConsoleDevice.LINES];
		PcBootConsoleDevice.position = 0;
		PcBootConsoleDevice.scrollUp = false;
		PcBootConsoleDevice.screen = Address.fromIntZeroExtend(0xb8000);
		PcBootConsoleDevice.current = Offset.zero();
		PcBootConsoleDevice.setForeground(VgaColor.WHITE);
		PcBootConsoleDevice.setBackground(VgaColor.BLACK);
		PcBootConsoleDevice.initializeAttributeBuffer();
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
		
		PcBootConsoleDevice.setForeground(VgaColor.CYAN);
		PcBootConsoleDevice.setBackground(VgaColor.LT_GREEN);
		PcBootConsoleDevice.putChar('a');
		verifyAll();
	}

	@Test
	public void testNewLine() {
		replayConsoleDeviceCreation();
		expect(Offset.fromIntZeroExtend(160)).andReturn(currentMock);
		replayAll();
		resetConsole();
		PcBootConsoleDevice.putChar('\n');
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
		resetConsole();
		PcBootConsoleDevice.setCursor(40, 24);
		PcBootConsoleDevice.putChar('\n');
		verifyAll();
		assertEquals("scrollup should be false!", false, PcBootConsoleDevice.scrollUp);
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
		resetConsole();
		PcBootConsoleDevice.clear();
		int[] expectedBuffer = new int[80*25];
		int[] expectedAttrib = new int[80*25];
		Arrays.fill(expectedBuffer, 32);
		Arrays.fill(expectedAttrib, 0xf);
		assertArrayEquals("Buffer not cleared", expectedBuffer, PcBootConsoleDevice.buffer);
		assertArrayEquals("Attributes not cleared", expectedAttrib, PcBootConsoleDevice.attributeBuffer);
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
		assertEquals("x default", 0, PcBootConsoleDevice.x);
		assertEquals("y default", 0, PcBootConsoleDevice.y);
		PcBootConsoleDevice.setCursor(40, 24);
		assertEquals("x", 40, PcBootConsoleDevice.x);
		assertEquals("y", 24, PcBootConsoleDevice.y);
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
		resetConsole();
		verify(Address.class, Offset.class);
		assertEquals("Wrong buffer length!", PcBootConsoleDevice.LINES*PcBootConsoleDevice.WIDTH, PcBootConsoleDevice.attributeBuffer.length);
		assertEquals("Wrong attribute!", 0x0f, PcBootConsoleDevice.charAttrib);
		assertEquals("Wrong mode!", 3, PcBootConsoleDevice.mode);
		int[] expectedAttributeBuffer = new int[PcBootConsoleDevice.LINES*PcBootConsoleDevice.WIDTH];
		Arrays.fill(expectedAttributeBuffer, 0xf);
		assertArrayEquals("default array attribute", expectedAttributeBuffer, PcBootConsoleDevice.attributeBuffer);
	}

	/**
	 * Test method for {@link org.jam.driver.console.PcConsoleDevice#setMode(int)}.
	 */
	@Test
	public void testSetMode() {
		assertEquals("Default mode", 3, PcBootConsoleDevice.mode);
	}

	/**
	 * Test method for {@link org.jam.driver.console.PcConsoleDevice#setForeground(org.jam.driver.console.VgaColor)}.
	 */
	@Test
	public void testSetForeground() {
		PcBootConsoleDevice.charAttrib = 0xff;
		PcBootConsoleDevice.setForeground(VgaColor.LT_BLUE);
		assertEquals(0xf9, PcBootConsoleDevice.charAttrib);
	}

	/**
	 * Test method for {@link org.jam.driver.console.PcConsoleDevice#setBackground(org.jam.driver.console.VgaColor)}.
	 */
	@Test
	public void testSetBackground() {
		PcBootConsoleDevice.charAttrib = 0xff;
		PcBootConsoleDevice.setBackground(VgaColor.LT_GREEN);
		assertEquals(0xaf, PcBootConsoleDevice.charAttrib);
	}

}
