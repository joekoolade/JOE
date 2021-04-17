package org.jam.net.inet4;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Test;

public class InetAddressTests {
    private Field addrIntField;
    private Field addrByteArrayField;
    
    @Before
    public void setUp() throws NoSuchFieldException
    {
        addrIntField = InetAddress.class.getDeclaredField("addrInt");
        addrIntField.setAccessible(true);
        addrByteArrayField = InetAddress.class.getDeclaredField("addrBytes");
        addrByteArrayField.setAccessible(true);
    }
    
    @Test
    public void testInetAddressInt() throws IllegalAccessException
    {
        byte[] array = { (byte)200, (byte)200, (byte)200, (byte)200 };
        InetAddress inetaddr = new InetAddress(0xc8c8c8c8);
        assertEquals((long)0xc8c8c8c8, (long)addrIntField.getInt(inetaddr));
        assertArrayEquals(array, (byte[])addrByteArrayField.get(inetaddr));
    }

    @Test
    public void testInetAddressInetAddress() throws UnknownHostException, IllegalAccessException
    {
        byte[] array = { (byte)200, (byte)200, (byte)200, (byte)200 };
        InetAddress inetaddr = new InetAddress(java.net.InetAddress.getByName("200.200.200.200"));
        assertEquals((long)0xc8c8c8c8, (long)addrIntField.getInt(inetaddr));
        assertArrayEquals(array, (byte[])addrByteArrayField.get(inetaddr));
    }

    @Test
    public void testInetAddressIntIntIntInt() throws IllegalAccessException
    {
        byte[] array = { (byte)200, (byte)200, (byte)200, (byte)200 };
        InetAddress inetaddr = new InetAddress(200,200,200,200);
        assertEquals((long)0xc8c8c8c8, (long)addrIntField.getInt(inetaddr));
        assertArrayEquals(array, (byte[])addrByteArrayField.get(inetaddr));
    }

    @Test
    public void testInetAddressByteArray() throws IllegalAccessException
    {
        byte[] inetarray = { (byte)200, (byte)200, (byte)200, (byte)200 };
        InetAddress inetaddr = new InetAddress(inetarray);
        assertEquals((long)0xc8c8c8c8, (long)addrIntField.getInt(inetaddr));
        assertArrayEquals(inetarray, (byte[])addrByteArrayField.get(inetaddr));
    }

    @Test
    public void testIsBroadcast()
    {
        InetAddress inetaddress = new InetAddress(0xffffffff);
        assertTrue(inetaddress.isBroadcast());
    }

    @Test
    public void testAsArray()
    {
        byte[] inetarray = { (byte)200, (byte)200, (byte)200, (byte)200 };
        InetAddress inetaddr = new InetAddress(inetarray);
        assertArrayEquals(inetarray, inetaddr.asArray());
    }

}
