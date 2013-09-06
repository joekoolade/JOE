// jq_RegisterState.java, created Mon Feb  5 23:23:21 2001 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Scheduler;

import joeq.Assembler.x86.x86Constants;
import joeq.Class.jq_DontAlign;
import joeq.Memory.CodeAddress;
import joeq.Memory.StackAddress;

/*
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: jq_x86RegisterState.java,v 1.2 2004/03/10 23:30:32 jwhaley Exp $
 */
public class jq_x86RegisterState extends jq_RegisterState implements x86Constants, jq_DontAlign {

    // WARNING: the layout of this object should match the CONTEXT data structure
    // used in GetThreadContext/SetThreadContext.  see "winnt.h".

    // Used as a param in GetThreadContext/SetThreadContext
    int ContextFlags;
    // debug registers
    int Dr0, Dr1, Dr2, Dr3, Dr6, Dr7;
    // floating point
    int ControlWord, StatusWord, TagWord, ErrorOffset, ErrorSelector, DataOffset, DataSelector;
    long fp0_L; short fp0_H;  // fp are 80 bits, so it is split across two fields.
    long fp1_L; short fp1_H;
    long fp2_L; short fp2_H;
    long fp3_L; short fp3_H;
    long fp4_L; short fp4_H;
    long fp5_L; short fp5_H;
    long fp6_L; short fp6_H;
    long fp7_L; short fp7_H;
    int Cr0NpxState;
    // segment registers
    int SegGs, SegFs, SegEs, SegDs;
    // integer registers
    int Edi, Esi, Ebx, Edx, Ecx, Eax;
    // control registers
    StackAddress Ebp;
    CodeAddress Eip;
    int SegCs, EFlags;
    StackAddress Esp;
    int SegSs;

    public static final int EFLAGS_CARRY      = 0x00000001;
    public static final int EFLAGS_PARITY     = 0x00000004;
    public static final int EFLAGS_AUXCARRY   = 0x00000010;
    public static final int EFLAGS_ZERO       = 0x00000040;
    public static final int EFLAGS_SIGN       = 0x00000080;
    public static final int EFLAGS_TRAP       = 0x00000100;
    public static final int EFLAGS_INTERRUPT  = 0x00000200;
    public static final int EFLAGS_DIRECTION  = 0x00000400;
    public static final int EFLAGS_OVERFLOW   = 0x00000800;
    public static final int EFLAGS_NESTEDTASK = 0x00004000;

    public static final int EFLAGS_IOPRIV_MASK = 0x00003000;
    public static final int EFLAGS_IOPRIV_SHIFT = 12;

    public jq_x86RegisterState() {
        ControlWord = 0x027f;
        StatusWord = 0x4000;
        TagWord = 0xffff;
    }

    public StackAddress getEbp() {
        return Ebp;
    }

    public StackAddress getEsp() {
        return Esp;
    }

    public CodeAddress getEip() {
        return Eip;
    }
    
    public void setEbp(StackAddress a) {
        Ebp = a;
    }

    public void setEip(CodeAddress a) {
        Eip = a;
    }

    public void setEsp(StackAddress a) {
        Esp = a;
    }

    public void setControlWord(int x) {
        ControlWord = x;
    }

    public void setStatusWord(int x) {
        StatusWord = x;
    }

    public void setTagWord(int x) {
        TagWord = x;
    }

    public void setContextFlags(int x) {
        ContextFlags = x;
    }
    
    static {
        initFactory();
    }
    
    public static void initFactory() {
        // Set jq_x86RegisterState as the default type of register state,
        //  if there is no default yet.
        if (factory == null) {
            factory = new Factory() {
                public jq_RegisterState create() {
                    return new jq_x86RegisterState();
                }
            };
        }
    }
    
}
