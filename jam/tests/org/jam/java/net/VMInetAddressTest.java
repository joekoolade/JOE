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
		result = VMInetAddress.aton("1.1.1.1");
		assertEquals(1, result[0]);
		assertEquals(1, result[1]);
		assertEquals(1, result[2]);
		assertEquals(1, result[3]);
		result = VMInetAddress.aton("255.255.255.255");
		assertEquals((byte)255, result[0]);
		assertEquals((byte)255, result[1]);
		assertEquals((byte)255, result[2]);
		assertEquals((byte)255, result[3]);
		result = VMInetAddress.aton("192.168.1.100");
		assertEquals((byte)192, result[0]);
		assertEquals((byte)168, result[1]);
		assertEquals((byte)1, result[2]);
		assertEquals((byte)100, result[3]);
		result = VMInetAddress.aton("333.1.1.1");
		assertNull(result);
		result = VMInetAddress.aton("3.555.1.1");
		assertNull(result);
		result = VMInetAddress.aton("3.5.1111.1");
		assertNull(result);
		result = VMInetAddress.aton("3.5.1.1222");
		assertNull(result);
		result = VMInetAddress.aton("3.555.1.1.2.3");
		assertNull(result);
	}

}
