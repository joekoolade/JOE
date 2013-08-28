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
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;


/**
 * @author joe
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class Elf {
  File elfFile;
  RandomAccessFile elf;
  ElfHeader header;
  ProgramHeader[] pht;
  SectionHeader[] sht;
  SectionHeader stringTable;
  
  public Elf(String fileName) throws IOException {
    elfFile = new File(fileName);
    
    byte[] data = new byte[4096];
    
    elf = new RandomAccessFile(elfFile, "r");
    elf.readFully(data);
    header = new ElfHeader(data);
    
    /*
     * Read in program headers
     */
    DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
    in.skip(header.ehSize);
    pht = new ProgramHeader[header.phNum];
    for(int i=0; i<header.phNum; i++)
      pht[i] = new ProgramHeader(in, header.isLittleEndian());
    /*
     * Read in section headers
     */
    sht = new SectionHeader[header.shNum];
    elf.seek(header.shOff);
    data = new byte[header.shEntSize*header.shNum];
    elf.readFully(data);
    in = new DataInputStream(new ByteArrayInputStream(data));
    for(int i=0; i<header.shNum; i++) {
      sht[i] = new SectionHeader(in, header.isLittleEndian());
    }
    stringTable = sht[header.shStrIndex];
    // set up the section names
    for(int i=1; i<header.shNum; i++)
      setSectionName(sht[i]);
  }
  
  public SectionHeader getSection(String sectionName) {
    for(int i=0; i<sht.length; i++) {
      if(sectionName.equals(sht[i].sName))
        return sht[i];
    }
    throw new RuntimeException("Cannot find section: "+sectionName);
  }
  
  public void setSectionName(SectionHeader section) throws IOException {
    elf.seek(stringTable.offset+section.name);
    byte[] name = new byte[1024];
    int i=0;
    int ch=elf.readByte();
    for(; ch!=0; i++) {
      name[i] = (byte)ch;
      ch = elf.readByte();
    }
    section.name(new String(name, 0, i));
  }
  
  public static void main(String[] args) throws IOException {
    Elf myElf=new Elf(args[0]);
  }
  
}
