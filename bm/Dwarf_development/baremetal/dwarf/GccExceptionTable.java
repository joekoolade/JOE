/*
 * Created on Sep 20, 2004
 *
 * Copyright (C) Joe Kulig, 2004
 * All rights reserved.
 * 
 */
package baremetal.dwarf;


/**
 * @author joe
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class GccExceptionTable {
  int offset;
  int lpStart;
  byte lpStartEncoding;
  int ttype;
  int ttypeEncoding;
  int callSiteTableStart;
  int callSiteEncoding;
  int actionTable;
  int actionTableEnd;
  byte[] table;
  
  final byte PE_ABSPTR = 0x0;
  final byte PE_OMIT = (byte)0xff;
  final byte PE_ULEB128 = 0x1;
  final byte PE_UDATA2 = 0x2;
  final byte PE_UDATA4 = 0x3;
  final byte PE_UDATA8 = 0x4;
  final byte PE_SLEB128 = 0x9;
  final byte PE_SDATA2 = 0xa;
  final byte PE_SDATA4 = 0xb;
  final byte PE_SDATA8 = 0xc;
  final byte PE_SIGNED = 0x8;
  
  public GccExceptionTable(byte[] table, int start) {
    this.table = table;
    offset = 0;
    lpStartEncoding = table[offset++];
    if(lpStartEncoding != PE_OMIT)
      ;
    else
      lpStart = start;
    
    ttypeEncoding = table[offset++];
    ArrayPtr array = new ArrayPtr(table, offset);
    if(ttypeEncoding != PE_OMIT)
      ttype = Utils.readUleb128(array);
    
    offset = array.ptr;
    callSiteTableStart = offset;
    ttype += callSiteTableStart;
    callSiteEncoding = table[offset++];
    array.ptr = offset;
    actionTable = Utils.readUleb128(array);
    offset = array.ptr;
    actionTableEnd = actionTable+offset;
    
  }
  
  public boolean hasTable() {
    return lpStartEncoding==PE_OMIT;
  }
  
  public void nextTable() {
    lpStartEncoding = table[offset++];
    if(lpStartEncoding != PE_OMIT)
      return;

    ttypeEncoding = table[offset++];
    ArrayPtr array = new ArrayPtr(table, offset);
    if(ttypeEncoding != PE_OMIT)
      ttype = Utils.readUleb128(array);
    
    offset = array.ptr;
    callSiteTableStart = offset;
    ttype += callSiteTableStart;
    callSiteEncoding = table[offset++];
    array.ptr = offset;
    actionTable = Utils.readUleb128(array);
    offset = array.ptr;
    actionTableEnd = actionTable+offset;
    if(ttypeEncoding == PE_OMIT)
      ttype = actionTableEnd;
  }
  
  public int[] getNextEntry() {
    int[] entry = null;
    
    if(offset<actionTableEnd) {
      entry = new int[7];
      ArrayPtr buffer = new ArrayPtr(table, offset);
      entry[0] = Utils.readUleb128(buffer); // start
      entry[1] = Utils.readUleb128(buffer); // length
      entry[2] = Utils.readUleb128(buffer); // launch pad
      entry[3] = Utils.readUleb128(buffer); // action
      
      // get throw class and displacements
      if(entry[3] != 0) {
        entry[4] = table[actionTableEnd+entry[3]-1];
        entry[5] = table[actionTableEnd+entry[3]];
        entry[6] = Utils.readInt(table, ttype-(entry[4]*4));
      }
      offset = buffer.ptr;
    } else
      offset = ttype;
    return entry;
  }
  
  public String entryToString(int[] entry) {
    return "start: 0x"+Integer.toHexString(entry[0])
    +" len: 0x"+Integer.toHexString(entry[1])
    +" pad: 0x"+Integer.toHexString(entry[2])
    +" action: "+entry[3] +"\n"
    +" filter: "+entry[4] + " disp: 0x"+Integer.toHexString(entry[5])
    +" throw type: 0x"+Integer.toHexString(entry[6]);
  }
  
  public int[] getDisps() {
    int size = (ttype - actionTableEnd)/6;
    int[] disps = new int[size*2];
    int offset = actionTableEnd;
    int i=0;
    while(i<disps.length) {
      disps[i++] = table[offset++];
      disps[i++] = table[offset++];
    }
    return disps;
  }
  
}
