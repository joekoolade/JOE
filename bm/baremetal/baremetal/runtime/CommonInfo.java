/*
 * Created on Sep 24, 2004
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
final public class CommonInfo {
  int length;
  int id;
  byte version;
  int codeAlignmentFactor;
  int dataAlignmentFactor;
  int returnAddressRegister;
  int initialInstructions;
  
  int ptr;
  byte data;
  int instructions;
  int end;
  
  boolean hasSize;
  boolean hasPersonalityEnc;
  boolean hasCodeEnc;
  boolean hasLsdaEnc;
  int augSize;
  byte personalityEnc;
  int personalityRoutine;
  byte codeEnc;
  byte ldsaEnc;

  public final void load(int addr) {
    length = Memory.read32(addr);
    addr += 4;
    end = addr+length;
    id = Memory.read32(addr);
    addr += 4;
    version = (byte)Memory.readByte(addr);
    addr++;
    data = (byte)Memory.readByte(addr);
    while(data!=0) {
      if(data == 'z')
        hasSize = true;
      else if(data == 'P')
        hasPersonalityEnc = true;
      else if(data == 'R')
        hasCodeEnc = true;
      else if(data == 'L')
        hasLsdaEnc = true;
      data = (byte)Memory.readByte(addr);
      addr++;
    }
    int[] buffer = new int[2];
    buffer[0] = addr;
    codeAlignmentFactor = Utils.readUleb128(buffer);
    dataAlignmentFactor = Utils.readUleb128(buffer);
    addr = buffer[0];
    returnAddressRegister = Memory.readByte(addr++);
    buffer[0] = addr;
    if(hasSize) {
      augSize = Utils.readUleb128(buffer);
      addr = buffer[0];
    }
    if(hasPersonalityEnc) {
      personalityEnc = (byte)Memory.readByte(addr);
      addr++;
      personalityRoutine = Memory.read32(addr);
      addr += 4;
    }
    if(hasCodeEnc) {
      codeEnc = (byte)Memory.readByte(addr);
      addr++;
    }
    if(hasLsdaEnc) {
      ldsaEnc = (byte)Memory.readByte(addr);
      addr++;
    }
    
    instructions = addr;
  }
  
  public final int firstFde() {
    return end;
  }
}
