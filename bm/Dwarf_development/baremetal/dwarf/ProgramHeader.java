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
public class ProgramHeader {
  int type;
  int offset;
  int vaddr;
  int paddr;
  int fileSize;
  int memorySize;
  int flags;
  int align;
  
  public ProgramHeader(byte[] header) {
  }
  
  public ProgramHeader(DataInput in, boolean littleEndian) throws IOException {
    type = littleEndian ? Utils.le32(in.readInt()) : in.readInt();
    offset = littleEndian ? Utils.le32(in.readInt()) : in.readInt();
    vaddr = littleEndian ? Utils.le32(in.readInt()) : in.readInt();
    paddr = littleEndian ? Utils.le32(in.readInt()) : in.readInt();
    fileSize = littleEndian ? Utils.le32(in.readInt()) : in.readInt();
    memorySize = littleEndian ? Utils.le32(in.readInt()) : in.readInt();;
    flags = littleEndian ? Utils.le32(in.readInt()) : in.readInt();
    align = littleEndian ? Utils.le32(in.readInt()) : in.readInt();
  }
}
