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
final public class FrameDesc {
  int length;
  int cie;
  int initialLocation;
  int addressRange;
  int instructions;
  int end;
  int endOfFrame;
  int exceptionTable;
  byte data;
  boolean endOfTable;
  
  public void load(int addr) {
    endOfTable = false;
    length = Memory.read32(addr);
    addr += 4;
    endOfFrame = addr+length;
    cie = Memory.read32(addr);
    addr += 4;
    initialLocation = Memory.read32(addr);
    addr += 4;
    addressRange = Memory.read32(addr);
    addr += 4;
    
    instructions = addr;
    data = (byte)Memory.read8(addr);
    /*
     * See if this is an advance_loc4 instruction
     */
    if(data == 0x4) {
      // read the delta
      exceptionTable = Memory.read32(addr+1);
    }
    end = initialLocation+addressRange;
  }
  
  public final boolean hasExceptionTable() {
    return exceptionTable!=0;
  }
  public final boolean endOfTable() {
    return endOfTable;
  }
  public final void nextFde() {
    if(endOfFrame>=Thrower.ehSectionEnd)
      endOfTable=true;
    load(endOfFrame);
  }
  public final boolean inRange(int addr) {
    return (addr>=initialLocation) || endOfTable;
  }
  public final boolean containsHandler(int addr) {
    return addr>=initialLocation && addr<end;
  }
  
}
