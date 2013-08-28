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
public class CommonInformationEntry {
  int length;
  int id;
  int version;
  int codeAlignmentFactor;
  int dataAlignmentFactor;
  int returnAddressRegister;
  int initialInstructions;
  
  int ptr;
  byte[] data;
  byte[] instructions;
  
  boolean hasSize;
  boolean hasPersonalityEnc;
  boolean hasCodeEnc;
  boolean hasLsdaEnc;
  
  int augSize;
  int personalityEnc;
  int personalityRoutine;
  int codeEnc;
  int ldsaEnc;
  CallFrameInstructions cfi = new CallFrameInstructions();

  public CommonInformationEntry(int address) {
    ptr = address;
  }
  
  public CommonInformationEntry(byte[] data, int offset) throws IOException, WrongHeaderException {
    this.data = data;
    int bytesRead=5;
    DataInputStream in = new DataInputStream(new ByteArrayInputStream(data, offset, 4));
    length = Utils.le32(in.readInt());
    in = new DataInputStream(new ByteArrayInputStream(data, offset+4, length));
    id = Utils.le32(in.readInt());
    if(id != 0) throw new WrongHeaderException();
    version = in.readByte();
    byte augmentationData = in.readByte();
    bytesRead++;
    while(augmentationData!=0)   {
      if(augmentationData == 'z') {
        hasSize = true;
      } else if(augmentationData == 'P') {
        hasPersonalityEnc = true;
      } else if(augmentationData == 'R') {
        hasCodeEnc = true;
      } else if(augmentationData == 'L') {
        hasLsdaEnc = true;
      }
      augmentationData = in.readByte();
      bytesRead++;
    }
    codeAlignmentFactor = Utils.readUleb128(in);
    dataAlignmentFactor = Utils.readSleb128(in);
    returnAddressRegister = in.readByte();
    bytesRead+=3;
    if(hasSize) {
      augSize = Utils.readUleb128(in);
      bytesRead++;
    }
    if(hasPersonalityEnc) {
      personalityEnc = in.readByte();
      personalityRoutine = Utils.le32(in.readInt());
      bytesRead+=5;
    }
    if(hasCodeEnc) {
      codeEnc = in.readByte();
      bytesRead++;
    }
    if(hasLsdaEnc) {
      ldsaEnc = in.readByte();
      bytesRead++;
    }
    
    instructions = new byte[length-bytesRead];
    in.read(instructions);
  }
  
  public byte[] getInstructions() {
    return instructions;
  }
  
  public void printInstructions() {
    cfi.print(instructions, 0, instructions.length);
  }
  
  public String toString() {
    return "size: "+length+" id: "+id+" version: "+version+" code align: "+codeAlignmentFactor
    +" data align: "+dataAlignmentFactor+" return reg: "+returnAddressRegister;
  }
}
