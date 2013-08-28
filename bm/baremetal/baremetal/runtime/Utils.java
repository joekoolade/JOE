/*
 * Created on Sep 25, 2004
 *
 * Copyright (C) Joe Kulig, 2004
 * All rights reserved.
 * 
 */
package baremetal.runtime;

import baremetal.kernel.Memory;


/**
 * @author joe
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
final public class Utils {
  /**
   * @param buffer
   * @return
   */
  public final static int readUleb128(int[] buffer) {
    int shift=0;
    byte aByte;
    int result=0;
    int i=0;

    buffer[1] = 0;
    do {
      aByte=(byte)Memory.read8(buffer[0]);
      result|=(aByte & 0x7f) << shift;
      shift+=7;
      buffer[0]++;
      buffer[1]++;
    } while ((aByte & 0x80) != 0);

    return result;
  }

  public static int readSleb128(int[] buffer) {
    int shift=0;
    byte aByte;
    int result=0;
    int i=0;
    
    buffer[1] = 0;
    do {
      aByte = (byte)Memory.read8(buffer[0]);
      result |= (aByte&0x7f) << shift;
      shift += 7;
      buffer[0]++;
      buffer[1]++;
    } while((aByte&0x80)!=0);
    
    // sign extend  negative value
    if(shift<32 && (aByte&0x40) != 0)
      result |= -(1<<shift);
    
    return result;
  }
}
