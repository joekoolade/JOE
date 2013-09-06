// jq_RegisterState.java, created Mon Feb  5 23:23:21 2001 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Scheduler;

import joeq.Memory.CodeAddress;
import joeq.Memory.StackAddress;

/*
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: jq_RegisterState.java,v 1.12 2004/03/10 22:35:53 jwhaley Exp $
 */
public abstract class jq_RegisterState {

    public abstract static class Factory {
        public abstract jq_RegisterState create();
    }
    public static Factory factory;
    
    public static jq_RegisterState create() {
        return factory.create();
    }
    
    public abstract CodeAddress getEip();
    public abstract void setEip(CodeAddress a);
    public abstract StackAddress getEsp();
    public abstract void setEsp(StackAddress a);
    public abstract StackAddress getEbp();
    public abstract void setEbp(StackAddress a);
    public abstract void setControlWord(int x);
    public abstract void setStatusWord(int x);
    public abstract void setTagWord(int x);
    public abstract void setContextFlags(int x);
    
    public static final int CONTEXT_i386               = 0x00010000;
    public static final int CONTEXT_CONTROL            = (CONTEXT_i386 | 0x00000001); // SS:SP, CS:IP, FLAGS, BP
    public static final int CONTEXT_INTEGER            = (CONTEXT_i386 | 0x00000002); // AX, BX, CX, DX, SI, DI
    public static final int CONTEXT_SEGMENTS           = (CONTEXT_i386 | 0x00000004); // DS, ES, FS, GS
    public static final int CONTEXT_FLOATING_POINT     = (CONTEXT_i386 | 0x00000008); // 387 state
    public static final int CONTEXT_DEBUG_REGISTERS    = (CONTEXT_i386 | 0x00000010); // DB 0-3,6,7
    public static final int CONTEXT_EXTENDED_REGISTERS = (CONTEXT_i386 | 0x00000020); // cpu specific extensions
    public static final int CONTEXT_FULL = (CONTEXT_CONTROL | CONTEXT_INTEGER | CONTEXT_SEGMENTS);

}
