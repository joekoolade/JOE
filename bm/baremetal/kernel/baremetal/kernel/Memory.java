/*
 * Created on Oct 13, 2003
 *
 * Copyright (C) Joe Kulig, 2003
 * All rights reserved.
 * 
 */
package baremetal.kernel;

import org.dellroad.jc.cgen.C;

/**
 * @author Joe Kulig
 *
 * Class with static methods to read and write memory. This is only
 * usuable by kernel package classes.
 */
public class Memory {

  public final static int read32(int addr) {
	  int value=0;
	  C.include("value = *(unsigned int *)addr;");
	  return value;
  }
  public final static int read8(int addr) {
	  int value=0;
	  C.include("value = *(unsigned char *)addr;");
	  return value;
  }
  public final static int read16(int addr) {
	  int value=0;
	  C.include("value = *(unsigned short *)addr;");
	  return value;
  }
  public final static long read64(int addr) {
	  long value=0;
	  C.include("value = *(unsigned long long *)addr;");
	  return value;
  }
  public final static void write32(int addr, int value) {
	  C.include("*(unsigned int *)addr = value;");
  }
  public final static void write16(int addr, int value) {
	  C.include("*(unsigned short *)addr = value;");
  }
  public final static void write8(int addr, int value) {
	  C.include("*(unsigned char *)addr = value;");
  }
 
	/**
	 * @param src
	 * @param dst
	 * @param size
	 */
	public static void bcopy(int src, int dst, int size) {
		C.include("bcopy(src, dst, size);");
	}

	public final native static void memset(int dst, int value, int size);

}
