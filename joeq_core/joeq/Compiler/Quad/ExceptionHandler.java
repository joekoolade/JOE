// ExceptionHandler.java, created Fri Jan 11 16:42:38 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.Quad;

import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Class;
import joeq.Runtime.TypeCheck;
import joeq.Util.Templates.List;
import joeq.Util.Templates.ListWrapper;

/**
 * Exception handler for basic blocks.  Each exception handler handles a type of
 * exception.  When an exception is raised at run time, a routine looks up the list
 * of exception handlers that guard the location where the exception was raised.
 * It checks each of the exception handlers in order.  Control flow branches to the
 * first exception handler whose type matches the type of the raised exception.
 * Note that the type check is a Java "assignable" type check, and therefore
 * inheritance and interface checks may be necessary.
 * 
 * @see  ExceptionHandlerList
 * @see  joeq.Runtime.TypeCheck
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version  $Id: ExceptionHandler.java,v 1.15 2004/04/28 17:36:09 joewhaley Exp $
 */

public class ExceptionHandler {

    /** Type of exception that this exception handler catches. */
    private jq_Class exception_type;
    /** List of handled basic blocks. */
    private java.util.List/*<BasicBlock>*/ handled_blocks;
    /** Exception handler entry point. */
    private BasicBlock entry;
    
    /** Creates new ExceptionHandler.
     * @param ex_type  type of exception to catch.
     * @param numOfHandledBlocks  estimated number of handled basic blocks.
     * @param entry  exception handler entry point. */
    public ExceptionHandler(jq_Class ex_type, int numOfHandledBlocks, BasicBlock entry) {
        if (ex_type == null)
            this.exception_type = PrimordialClassLoader.getJavaLangThrowable();
        else
            this.exception_type = ex_type;
        this.handled_blocks = new java.util.ArrayList(numOfHandledBlocks);
        this.entry = entry;
    }
    ExceptionHandler(jq_Class ex_type) {
        if (ex_type == null)
            this.exception_type = PrimordialClassLoader.getJavaLangThrowable();
        else
            this.exception_type = ex_type;
        this.handled_blocks = new java.util.ArrayList();
    }

    /** Returns the type of exception that this exception handler catches.
     * @return  the type of exception that this exception handler catches. */
    public jq_Class getExceptionType() { return exception_type; }
    /** Returns an iteration of the handled basic blocks.
     * @return  an iteration of the handled basic blocks. */
    public List.BasicBlock getHandledBasicBlocks() { return new ListWrapper.BasicBlock(handled_blocks); }
    /** Returns the entry point for this exception handler.
     * @return  the entry point for this exception handler. */
    public BasicBlock getEntry() { return entry; }
    public void setEntry(BasicBlock entry) {this.entry = entry; }

    public boolean mustCatch(jq_Class exType) {
        exType.prepare();
        exception_type.prepare();
        return TypeCheck.isAssignable(exType, exception_type);
    }
    public boolean mayCatch(jq_Class exType) {
        exType.prepare();
        exception_type.prepare();
        return TypeCheck.isAssignable(exType, exception_type) ||
              TypeCheck.isAssignable(exception_type, exType);
    }
    public String toString() { return "Type: "+exception_type+" Entry: "+entry; }
    
    /** Add a handled basic block to the list of handled basic blocks.
     * @param bb  basic block to add. */
    void addHandledBasicBlock(BasicBlock bb) { handled_blocks.add(bb); }
}
