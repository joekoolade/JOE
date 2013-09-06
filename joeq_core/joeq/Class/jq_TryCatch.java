// jq_TryCatch.java, created Mon Feb  5 23:23:20 2001 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Class;

import joeq.Runtime.Debug;
import joeq.Runtime.TypeCheck;
import jwutil.strings.Strings;

/*
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: jq_TryCatch.java,v 1.17 2004/09/22 22:17:28 joewhaley Exp $
 */
public class jq_TryCatch {

    public static final boolean DEBUG = false;
    
    // NOTE: startPC is exclusive, endPC is inclusive (opposite of jq_TryCatchBC)
    // this is because the IP that we check against is IMMEDIATELY AFTER where the exception actually occurred.
    // these are CODE OFFSETS.
    private int startPC, endPC, handlerPC;
    private jq_Class exType;
    // this is the offset from the frame pointer where to put the exception.
    private int exceptionOffset;

    public jq_TryCatch(int startPC, int endPC, int handlerPC, jq_Class exType, int exceptionOffset) {
        this.startPC = startPC;
        this.endPC = endPC;
        this.handlerPC = handlerPC;
        this.exType = exType;
        this.exceptionOffset = exceptionOffset;
    }

    // note: offset is the offset of the instruction after the one which threw the exception.
    public boolean catches(int offset, jq_Class t) {
        if (DEBUG) Debug.writeln(this+": checking "+Strings.hex(offset)+" "+t);
        if (offset <= startPC) return false;
        if (offset > endPC) return false;
        if (exType != null) {
            exType.prepare();
            if (!TypeCheck.isAssignable(t, exType)) return false;
        }
        return true;
    }
    
    public int getStart() { return startPC; }
    public int getEnd() { return endPC; }
    public int getHandlerEntry() { return handlerPC; }
    public jq_Class getExceptionType() { return exType; }
    public int getExceptionOffset() { return exceptionOffset; }

    public String toString() {
        return "(start="+Strings.hex(startPC)+",end="+Strings.hex(endPC)+",handler="+Strings.hex(handlerPC)+",type="+exType+",offset="+Strings.shex(exceptionOffset)+")";
    }
    
}
