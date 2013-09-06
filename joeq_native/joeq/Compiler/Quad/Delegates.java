// Delegates.java, created Mon Dec 23 20:00:01 2002 by mcmartin
// Copyright (C) 2001-3 mcmartin
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.Quad;

import joeq.Compiler.Quad.Operand.RegisterOperand;
import joeq.Interpreter.QuadInterpreter;
import joeq.Main.jq;
import joeq.Runtime.Unsafe;

/*
 * @author  Michael Martin <mcmartin@stanford.edu>
 * @version $Id: Delegates.java,v 1.7 2004/03/09 22:36:58 jwhaley Exp $
 */
class Delegates {
    static class Op implements Operator.Delegate {
        public void interpretGetThreadBlock(Operator.Special op, Quad q, QuadInterpreter s) {
            if (jq.RunningNative)
                s.putReg_A(((RegisterOperand)Operator.Special.getOp1(q)).getRegister(), Unsafe.getThreadBlock());
        }
        public void interpretSetThreadBlock(Operator.Special op, Quad q, QuadInterpreter s) {
            joeq.Scheduler.jq_Thread o = (joeq.Scheduler.jq_Thread)Operator.getObjectOpValue(Operator.Special.getOp2(q), s);
            if (jq.RunningNative)
                Unsafe.setThreadBlock(o);
        }
        public void interpretMonitorEnter(Operator.Monitor op, Quad q, QuadInterpreter s) {
            Object o = Operator.getObjectOpValue(Operator.Monitor.getSrc(q), s);
            if (jq.RunningNative)
                joeq.Runtime.Monitor.monitorenter(o);
        }
        public void interpretMonitorExit(Operator.Monitor op, Quad q, QuadInterpreter s) {
            Object o = Operator.getObjectOpValue(Operator.Monitor.getSrc(q), s);
            if (jq.RunningNative)
                joeq.Runtime.Monitor.monitorexit(o);
        }
    }
}
