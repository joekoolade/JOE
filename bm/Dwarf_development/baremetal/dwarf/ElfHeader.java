/*
 * Created on Sep 8, 2004
 *
 * Copyright (C) Joe Kulig, 2004
 * All rights reserved.
 * 
 */
package baremetal.dwarf;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;


/**
 * @author joe
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ElfHeader {
  byte ident[];
  int type;
  int machine;
  int version;
  int entry;
  int phOff;
  int shOff;
  int flags;
  int ehSize;
  int phEntSize;
  int phNum;
  int shEntSize;
  int shNum;
  int shStrIndex;
  
  private final static int ELFCLASSNONE=0;
  private final static int ELFCLASS32=1;
  private final static int ELFCLASS64=2;
  
  private final static int ELFDATANONE=0;
  private final static int ELFDATA2LSB=1;
  private final static int ELFDATA2MSB=2;
  
  
  public ElfHeader(byte[] header) throws IOException {
    ident = new byte[16];
    DataInputStream in = new DataInputStream(new ByteArrayInputStream(header));
    in.readFully(ident);
    if(ident[0]!=0x7f || ident[1]!='E' || ident[2]!='L' || ident[3]!='F')
      throw new RuntimeException("Not an ELF File!");
    
    type = isLittleEndian() ? Utils.le16(in.readUnsignedShort()) : in.readUnsignedShort();
    machine = isLittleEndian() ? Utils.le16(in.readUnsignedShort()) : in.readUnsignedShort();
    version = isLittleEndian() ? Utils.le32(in.readInt()) : in.readInt();
    entry = isLittleEndian() ? Utils.le32(in.readInt()) : in.readInt();
    phOff = isLittleEndian() ? Utils.le32(in.readInt()) : in.readInt();
    shOff = isLittleEndian() ? Utils.le32(in.readInt()) : in.readInt();
    flags = isLittleEndian() ? Utils.le32(in.readInt()) : in.readInt();
    ehSize = isLittleEndian() ? Utils.le16(in.readUnsignedShort()) : in.readUnsignedShort();
    phEntSize = isLittleEndian() ? Utils.le16(in.readUnsignedShort()) : in.readUnsignedShort();
    phNum = isLittleEndian() ? Utils.le16(in.readUnsignedShort()) : in.readUnsignedShort();
    shEntSize = isLittleEndian() ? Utils.le16(in.readUnsignedShort()) : in.readUnsignedShort();
    shNum = isLittleEndian() ? Utils.le16(in.readUnsignedShort()) : in.readUnsignedShort();
    shStrIndex = isLittleEndian() ? Utils.le16(in.readUnsignedShort()) : in.readUnsignedShort();
  }
  
  public boolean isBigEndian() {
    return ident[5]==ELFDATA2MSB;
  }

  public boolean isLittleEndian() {
    return ident[5]==ELFDATA2LSB;
  }
  
  public int version() {
    return (int)ident[6];
  }
  
}
