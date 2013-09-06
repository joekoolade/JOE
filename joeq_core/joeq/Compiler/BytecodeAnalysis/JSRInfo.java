// JSRInfo.java, created Thu Jan 31 23:05:20 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.BytecodeAnalysis;

/**
 * Information for a JSR subroutine.
 * 
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: JSRInfo.java,v 1.6 2005/05/28 11:14:27 joewhaley Exp $
 */
public class JSRInfo {

    public BasicBlock entry_block;
    public BasicBlock exit_block;
    public boolean[] changedLocals;
    
    public JSRInfo(BasicBlock entry, BasicBlock exit, boolean[] changed) {
        this.entry_block = entry;
        this.exit_block = exit;
        this.changedLocals = changed;
    }
    
}
