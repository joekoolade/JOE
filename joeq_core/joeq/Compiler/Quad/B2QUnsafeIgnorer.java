// B2QUnsafeIgnorer.java, created Mon Dec 23 23:00:34 2002 by mcmartin
// Copyright (C) 2001-3 mcmartin
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.Quad;

import joeq.Class.jq_Method;

/*
 * @author  Michael Martin <mcmartin@stanford.edu>
 * @version $Id: B2QUnsafeIgnorer.java,v 1.5 2004/03/09 22:01:45 jwhaley Exp $
 */
class B2QUnsafeIgnorer implements BytecodeToQuad.UnsafeHelper {
    public boolean isUnsafe(jq_Method m) {
        return false;
    }
    public boolean endsBB(jq_Method m) {
        return false;
    }
    public boolean handleMethod(BytecodeToQuad b2q, ControlFlowGraph quad_cfg, BytecodeToQuad.AbstractState current_state, jq_Method m, Operator.Invoke oper) {
        return false;
    }
}
