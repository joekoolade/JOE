/*
 * Created on Sep 10, 2004
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
public class CallFrameInstructions {
  final int advanceLoc = 0x40;
  final int offset = 0x80;
  final int restore = 0xc0;
  final int setLoc = 0x1;
  final int advanceLoc1 = 0x2;
  final int advanceLoc2 = 0x3;
  final int advanceLoc4 = 0x4;
  final int offsetExtended = 0x5;
  final int restoreExtended = 0x6;
  final int undefined = 0x7;
  final int sameValue = 0x8;
  final int register = 0x9;
  final int rememberState = 0xa;
  final int restoreState = 0xb;
  final int defCfa = 0xc;
  final int defCfaRegister = 0xd;
  final int defCfaOffset = 0xe;
  final int gnuArgsSize = 0x2e;
  final int nop = 0x0;
  final int loUser = 0x1c;
  final int hiUser = 0x3f;
  
  public void print(byte[] data, int offset, int length) {
    String instruction;
    int op1, op2, size;
    int i = 0;
    while(i<length) {
      int instr = data[offset+i];
      if((instr&0xc0) == advanceLoc) {
        instruction = "advance_loc delta=" + (0x3f&instr);
        i++;
      } else if((instr&0xc0) == this.offset) {
        op1 = Utils.readUleb128(data, offset+i+1);
        instruction = "offset register=" + (instr&0x3f) + " offset "+ op1;
        i += 1 + leb128size(data, offset+i+1);
      } else if((instr&0xc0) == restore) {
        instruction = "restore register=" + (instr&0x3f);
        i++;
      } else {
        switch(instr&0x3f) {
        case setLoc:
          op1 = get4bytes(data, offset+i+1);
          instruction = "set_loc address=" + op1;
          i+=5;
          break;
        case advanceLoc1:
          instruction = "advance_loc1 delta=" + data[offset+i+1];
          i += 2;
          break;
        case advanceLoc2:
          op1 = get2bytes(data, offset+i+1);
          instruction = "advance_loc2 delta=0x" + Integer.toHexString(op1);
          i+=3;
          break;
        case advanceLoc4:
          op1 = get4bytes(data, offset+i+1);
          instruction = "advance_loc4 delta=0x" + Integer.toHexString(op1);
          i+=5;
          break;
        case offsetExtended:
          op1 = Utils.readUleb128(data, offset+i+1);
          size = leb128size(data, offset+i+1);
          op2 = Utils.readUleb128(data, offset+i+1+size);
          instruction = "offset_extended register=" + op1 + " offset=0x" + Integer.toHexString(op2);
          size += Utils.readUleb128(data, offset+i+1+size );
          i += size+1;
          break;
         case restoreExtended:
           op1 = Utils.readUleb128(data, offset+i+1);
           size = leb128size(data, offset+i+1);
           instruction = "restore_extended register="+op1;
           i += size+1;
           break;
         case undefined:
           op1 = Utils.readUleb128(data, offset+i+1);
           size = leb128size(data, offset+i+1);
           instruction = "undefined register="+op1;
           i += size+1;
           break;
         case sameValue:
           op1 = Utils.readUleb128(data, offset+i+1);
           size = leb128size(data, offset+i+1);
           instruction = "same_value register="+op1;
           i += size+1;
           break;
         case register:
           op1 = Utils.readUleb128(data, offset+i+1);
           size = leb128size(data, offset+i+1);
           op2 = Utils.readUleb128(data, offset+i+1+size);
           size += Utils.readUleb128(data, offset+i+1+size );
           instruction = "register register0="+op1+" register1="+op2;
           i += size+1;
           break;
         case rememberState:
           instruction = "remember_state";
           i++;
           break;
         case restoreState:
           instruction = "restore_state";
           i++;
           break;
         case defCfa:
           op1 = Utils.readUleb128(data, offset+i+1);
           size = leb128size(data, offset+i+1);
           op2 = Utils.readUleb128(data, offset+i+1+size);
           instruction = "def_cfa register=" + op1 + " offset=0x" + Integer.toHexString(op2);
           size += Utils.readUleb128(data, offset+i+1+size);
           i += size+1;
           break;
         case defCfaRegister:
           op1 = Utils.readUleb128(data, offset+i+1);
           size = leb128size(data, offset+i+1);
           instruction = "def_cfa_register="+op1;
           i += size+1;
           break;
         case defCfaOffset:
           op1 = Utils.readUleb128(data, offset+i+1);
           size = leb128size(data, offset+i+1);
           instruction = "def_cfa_offset 0x"+Integer.toHexString(op1);
           i += size+1;
           break;
         case gnuArgsSize:
           op1 = Utils.readUleb128(data, offset+i+1);
           size = leb128size(data, offset+i+1);
           instruction = "gnu_args_size 0x"+op1;
           i += size+1;
           break;
         case nop:
           instruction = "nop";
           i++;
           break;
         default:
           instruction = "Unknown instruction: "+Integer.toHexString(instr);
           i++;
           break;
        }
      }
      System.out.println(instruction);
    }
  }
  
  /**
   * @param data
   * @param offset
   * @return
   */
  private int get4bytes(byte[] data, int offset) {
    int val = (data[offset]&0xff) | ((data[offset+1]&0xff)<<8) | ((data[offset+2]&0xff)<<16) | ((data[offset+3]&0xff)<<24);
    return val;
  }

  private int get2bytes(byte[] data, int offset) {
    int val = (data[offset]&0xff) | ((data[offset+1]&0xff)<<8);
    return val;
  }
  private int leb128size(byte[] data, int offset) {
    int size = 1;
    while((data[offset++]&0x80)!=0)
      size++;
    return size;
  }
}
