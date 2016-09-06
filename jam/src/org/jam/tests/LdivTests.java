/**
 * Created on Sep 2, 2016
 *
 * Copyright (C) Joe Kulig, 2016
 * All rights reserved.
 */
package org.jam.tests;

/**
 * @author Joe Kulig
 *
 */
public class LdivTests {
    public static void test1()
    {
        long a,b,c;
        
        a = 0x22000000000L;
        b = 0x2L;
        c = a/b;
        System.out.println("c=0x"+ Long.toHexString(c));
        b = 0x11000000000L;
        c = a/b;
        System.out.println("c=0x" + Long.toHexString(c));
        a = 10 * 1000000000L;
        b = 1000000;
        c = a/b;
        System.out.println("c=0x" + Long.toHexString(c));
    }
    
    public static void main(String[] args)
    {
        test1();
    }
}
