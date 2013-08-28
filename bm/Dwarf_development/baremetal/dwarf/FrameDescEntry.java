/*
 * Created on Sep 10, 2004
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
public class FrameDescEntry {
  int length;
  int cie;
  int initialLocation;
  int addressRange;
  byte[] instructions;
  
  int ptr;
  byte[] data;
  
  CallFrameInstructions cfi = new CallFrameInstructions();
  
  public FrameDescEntry(int addr) {
    ptr = addr;
  }
  
  public FrameDescEntry(byte[] data, int offset) throws IOException, WrongHeaderException {
    DataInputStream in = new DataInputStream(new ByteArrayInputStream(data, offset, 4));
    length = Utils.le32(in.readInt());
    in = new DataInputStream(new ByteArrayInputStream(data, offset+4, length));
    cie = Utils.le32(in.readInt());
    if(cie==0) throw new WrongHeaderException();
      
    initialLocation = Utils.le32(in.readInt());
    addressRange = Utils.le32(in.readInt());
    
    instructions = new byte[length-12];
    in.read(instructions);
  }
  
  public void printInstructions() {
    cfi.print(instructions, 0, instructions.length);
  }
  
  public String toString() {
    return "size: "+length+" cie: "+cie+" location: 0x"+Integer.toHexString(initialLocation)+" addr range: "+addressRange;
  }
}
