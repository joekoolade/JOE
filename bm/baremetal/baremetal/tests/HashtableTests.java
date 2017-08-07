/*
 * Created on Jan 26, 2005
 *
 * Copyright (C) Joe Kulig, 2005
 * All rights reserved.
 * 
 */
package baremetal.tests;

import java.util.Hashtable;


/**
 * @author joe
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class HashtableTests {
  public static void main(String args[]) {
    System.out.println("Hashtable tests");
    tests();
  }
  public static final void tests() {
    Integer one,two,three,ten;
    Hashtable h = new Hashtable();
    System.out.println("new hashtable");
    
    one = new Integer(1);
    two = new Integer(2);
    three = new Integer(3);
    ten = new Integer(10);
    
    h.put("one", one);
    System.out.println("insert one");
    h.put("two",  two);
    System.out.println("insert two");
    h.put("three", three);
    System.out.println("insert three");
    h.put("ten", ten);
    System.out.println("insert ten");
    
    if(!h.contains(one))
      System.out.println("h does not contain one!");
    if(!h.contains(two))
      System.out.println("h does not contain two!");
    if(!h.contains(three))
      System.out.println("h does not contain three!");
    if(!h.contains(ten))
      System.out.println("h does not contain ten!");
    
    Integer i = (Integer)h.get("one");
    if(i!=one)
      System.out.println("did not get one!");
    i = (Integer)h.get("two");
    if(i!=two)
      System.out.println("did not get two!");
    i = (Integer)h.get("three");
    if(i!=three)
      System.out.println("did not get three!");
    i = (Integer)h.get("ten");
    if(i!=ten)
      System.out.println("did not get ten!");
  }
}
