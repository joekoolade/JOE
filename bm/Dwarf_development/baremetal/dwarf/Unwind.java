/*
 * Created on Sep 10, 2004
 *
 * Copyright (C) Joe Kulig, 2004
 * All rights reserved.
 * 
 */
package baremetal.dwarf;

import java.io.EOFException;
import java.io.IOException;


/**
 * @author joe
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class Unwind {
  int ehFramePtr;
  SectionHeader ehFrame;
  SectionHeader gccFrame;
  
  public Unwind() {
    
  }
  
  public static void main(String args[]) throws IOException, WrongHeaderException {
    Unwind uw = new Unwind();
    Elf myElf=new Elf(args[0]);
    uw.gccFrame = myElf.getSection(".gcc_except_table");
    myElf.elf.seek(uw.gccFrame.offset);
    byte[] exceptionTable = new byte[uw.gccFrame.size];
    myElf.elf.read(exceptionTable);
    GccExceptionTable table = new GccExceptionTable(exceptionTable, 0);
    while(table.hasTable()) {
    int[] entry = table.getNextEntry();
    while(entry!=null) {
      System.out.println(table.entryToString(entry));
      entry = table.getNextEntry();
    }
    table.nextTable();
    }
    uw.ehFrame = myElf.getSection(".eh_frame");
    myElf.elf.seek(uw.ehFrame.offset);
    byte[] ehFrameData = new byte[uw.ehFrame.size];
    myElf.elf.read(ehFrameData);
    CommonInformationEntry cie = new CommonInformationEntry(ehFrameData, 0);
    System.out.println(cie);
    cie.printInstructions();
    int offset=cie.length+4;
    while(offset<uw.ehFrame.size) {
      try {
        FrameDescEntry fde = new FrameDescEntry(ehFrameData, offset);
        System.out.println(fde);
        fde.printInstructions();
        offset += fde.length+4;
      } catch(WrongHeaderException e) {
        cie = new CommonInformationEntry(ehFrameData, offset);
        System.out.println(cie);
        cie.printInstructions();
        offset += cie.length+4;
      } catch(EOFException eof) {
        break;
      }
    }
  }
}
