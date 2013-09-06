/*
 * Created on Sep 19, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package joeq.Compiler.Analysis.IPSSA;

import java.io.PrintStream;
import joeq.Class.jq_Method;
import joeq.Compiler.Quad.ControlFlowGraph;

/**
 * Goes over all bindings in a method.
 * @author V.Benjamin Livshits
 * @version $Id: SSABindingVisitor.java,v 1.8 2004/09/22 22:17:33 joewhaley Exp $
 * */
public abstract class SSABindingVisitor {
    public abstract void visit(SSABinding b);
    
    /**
     * Applies itself to all bindings in the CFG.
     * */
    public void visitCFG(ControlFlowGraph cfg) {
        jq_Method method = cfg.getMethod();
                
        for (SSAIterator.BindingIterator j=SSAProcInfo.retrieveQuery(method).getBindingIterator(method); j.hasNext(); ) {
            SSABinding b = j.nextBinding();
            b.accept(this);
        }                
    }
    
    public static class EmptySSABindingVisitor extends SSABindingVisitor {
        public EmptySSABindingVisitor(){}
        
        public void visit(SSABinding b){
            // do nothing
        }
    }
    
    public static class SSABindingPrinter extends SSABindingVisitor {
        protected PrintStream _out;
        SSABindingPrinter(PrintStream out){
            this._out = out;
        }
        public void visit(SSABinding b){
            _out.println(b.toString());
        }
    }
}

