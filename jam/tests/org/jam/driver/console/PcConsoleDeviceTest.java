/**
 * Author: Joe Kulig
 * Created: Oct 28, 2013
 *
 * Copyright 2013, Joe Kulig
 * ALL RIGHTS RESERVED.
 */
package org.jam.driver.console;

import static org.junit.Assert.*;

import org.easymock.Capture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.powermock.api.easymock.PowerMock.*;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.capture;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Offset;

import com.sun.org.apache.bcel.internal.classfile.Attribute;

/**
 * @author jkulig
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Address.class, Offset.class})
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
		mockStatic(Address.class);
		mockStatic(Offset.class);
		Address screenMock = createMock(Address.class);
		Offset currentMock = createMock(Offset.class);
		expect(Address.fromIntZeroExtend(0xb8000)).andReturn(screenMock);
		expect(Offset.zero()).andReturn(currentMock);
		expect(screenMock.store('a',currentMock));
		expect(currentMock.plus(1)).andReturn(null);
		screenMock.store('a',currentMock);
		expect(currentMock.plus(1)).andReturn(null);
		// replay(Address.class, Offset.class, screenMock, currentMock);
		replayAll();
		
		PcConsoleDevice cut = new PcConsoleDevice(80, 25);
		cut.setForeground(VgaColor.CYAN);
		cut.setBackground(VgaColor.LT_GREEN);
		cut.putChar('a');
		//verify(Address.class, Offset.class);
		verifyAll();
		
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
		mockStatic(Address.class);
		mockStatic(Offset.class);
		expect(Address.fromIntZeroExtend(0xb8000)).andReturn(null);
		expect(Offset.zero()).andReturn(null);
		replay(Address.class, Offset.class);
		PcConsoleDevice cut = new PcConsoleDevice(80, 25);
		verify(Address.class, Offset.class);
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
