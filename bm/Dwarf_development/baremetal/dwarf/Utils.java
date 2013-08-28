/*
 * Created on Sep 21, 2004
 *
 * Copyright (C) Joe Kulig, 2004
 * All rights reserved.
 * 
 */
package baremetal.dwarf;

import java.io.DataInput;
import java.io.IOException;


/**
 * @author joe
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class Utils {

  /**
   * @param array
   * @return
   */
  public static int readUleb128(ArrayPtr array) {
    int shift=0;
    byte aByte;
    int result=0;
    int i=0;

    do {
      aByte=array.data[array.ptr++];
      result|=((int) aByte & 0x7f) << shift;
      shift+=7;
    } while ((aByte & 0x80) != 0);

    return result;
  }

  public static int le16(int v) {
    return (v>>8) | ((v&0xff)<<8);
  }

  public static int le32(int v) {
    return ((v>>24) & 0xff) | ((v>>8) & 0xff00) | ((v<<8) & 0xff0000) | (v<<24);
    
  }

  public static int readSleb128(byte[] data) {
    return Utils.readSleb128(data, 0);
  }

  public static int readSleb128(byte[] data, int offset) {
    int shift=0;
    byte aByte;
    int result=0;
    int i=0;
    
    do {
      aByte = data[offset+i++];
      result |= ((int)aByte&0x7f) << shift;
      shift += 7;
    } while((aByte&0x80)!=0);
    
    // sign extend  negative value
    if(shift<32 && (aByte&0x40) != 0)
      result |= -(1<<shift);
    
    return result;
  }

  public static int readSleb128(DataInput in) throws IOException {
    int shift=0;
    byte aByte;
    int result=0;
    int i=0;
    
    do {
      aByte = in.readByte();
      result |= ((int)aByte&0x7f) << shift;
      shift += 7;
    } while((aByte&0x80)!=0);
    
    // sign extend  negative value
    if(shift<32 && (aByte&0x40) != 0)
      result |= -(1<<shift);
    
    return result;
  }

  public static int readUleb128(byte[] data) {
    return Utils.readUleb128(data, 0);
  }

  public static int readUleb128(byte[] data, int offset) {
    int shift=0;
    byte aByte;
    int result=0;
    int i=0;
    
    do {
      aByte = data[offset+i++ ];
      result |= ((int)aByte&0x7f) << shift;
      shift += 7;
    } while((aByte&0x80)!=0);
    
    return result;
  }

  public static int readUleb128(DataInput in) throws IOException {
    int shift=0;
    byte aByte;
    int result=0;
    int i=0;
    
    do {
      aByte = in.readByte();
      result |= ((int)aByte&0x7f) << shift;
      shift += 7;
    } while((aByte&0x80)!=0);
    
    return result;
  }

  /**
   * @param table
   * @param i
   * @return
   */
  public static int readInt(byte[] table, int i) {
    return table[i]&0xff | ((table[i+1]&0xff)<<8) | ((table[i+2]&0xff)<<16) | ((table[i+3]&0xff)<<24);
  }

}
