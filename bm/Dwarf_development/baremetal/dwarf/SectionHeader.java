/*
 * Created on Sep 8, 2004
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
public class SectionHeader {
  int name;
  int type;
  int flags;
  int addr;
  int offset;
  int size;
  int link;
  int info;
  int addrAlign;
  int entSize;
  
  final static int NULL = 0;
  final static int PROGBITS = 1;
  final static int SYMTAB = 2;
  final static int STRTAB = 3;
  final static int RELA = 4;
  final static int HASH = 5;
  final static int DYNAMIC = 6;
  final static int NOTE = 7;
  final static int NOBITS = 8;
  final static int REL = 9;
  final static int SHLIB = 10;
  final static int DYNSYM = 11;
  
  String sName;
  
  public SectionHeader(byte[] header) {
    
  }
  
  /*
   * todo: pass in a datainput that interprets little endian
   * 
   */
  public SectionHeader(DataInput in, boolean littleEndian) throws IOException {
    name = littleEndian ? Utils.le32(in.readInt()) : in.readInt();
    type = littleEndian ? Utils.le32(in.readInt()) : in.readInt();
    flags = littleEndian ? Utils.le32(in.readInt()) : in.readInt();
    addr = littleEndian ? Utils.le32(in.readInt()) : in.readInt();
    offset = littleEndian ? Utils.le32(in.readInt()) : in.readInt();
    size = littleEndian ? Utils.le32(in.readInt()) : in.readInt();
    link = littleEndian ? Utils.le32(in.readInt()) : in.readInt();
    info = littleEndian ? Utils.le32(in.readInt()) : in.readInt();
    addrAlign = littleEndian ? Utils.le32(in.readInt()) : in.readInt();
    entSize = littleEndian ? Utils.le32(in.readInt()) : in.readInt();
  }
  
  public void name(String name) {
    sName = name;
  }
  
  public boolean isStringTable() {
    return type==STRTAB;
  }
}
