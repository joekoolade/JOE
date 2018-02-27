package org.jam.java.net;

import static org.junit.Assert.*;

import org.junit.Test;

public class VMInetAddressTest {

	@Test
	public void testAton() {
		byte[] result = VMInetAddress.aton("0.0.0.0");
		assertEquals(0, result[0]);
		assertEquals(0, result[1]);
		assertEquals(0, result[2]);
		assertEquals(0, result[3]);
	}

}
