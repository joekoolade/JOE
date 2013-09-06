// PrintCFG.java, created Mon Mar 18 16:46:44 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.Quad;

/**
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: PrintCFG.java,v 1.5 2004/03/09 22:01:46 jwhaley Exp $
 */
public class PrintCFG implements ControlFlowGraphVisitor {

    public java.io.PrintStream out = System.out;
    
    /** Creates new PrintCFG */
    public PrintCFG() {}

    /** Sets output stream. */
    public void setOut(java.io.PrintStream out) { this.out = out; }
    
    /** Prints full dump of the given CFG to the output stream. */
    public void visitCFG(ControlFlowGraph cfg) { out.println(cfg.fullDump()); }
    
}
