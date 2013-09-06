// ControlFlowGraphVisitor.java, created Mon Feb 11  0:24:01 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.Quad;

import joeq.Class.jq_Method;
import joeq.Class.jq_MethodVisitor;

/**
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: ControlFlowGraphVisitor.java,v 1.8 2004/03/09 22:01:46 jwhaley Exp $
 */
public interface ControlFlowGraphVisitor {
    void visitCFG(ControlFlowGraph cfg);
    
    class CodeCacheVisitor extends jq_MethodVisitor.EmptyVisitor {
        private final ControlFlowGraphVisitor bbv;
        boolean trace;
        public CodeCacheVisitor(ControlFlowGraphVisitor bbv) { this.bbv = bbv; }
        public CodeCacheVisitor(ControlFlowGraphVisitor bbv, boolean trace) { this.bbv = bbv; this.trace = trace; }
        public void visitMethod(jq_Method m) {
            if (m.getBytecode() == null) return;
            if (trace) System.out.println(m.toString());
            ControlFlowGraph cfg = joeq.Compiler.Quad.CodeCache.getCode(m);
            bbv.visitCFG(cfg);
        }
    }

}
