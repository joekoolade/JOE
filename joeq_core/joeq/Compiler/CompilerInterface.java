// CompilerInterface.java, created Mon Feb  5 23:23:20 2001 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler;

import joeq.Class.jq_CompiledCode;
import joeq.Class.jq_Method;
import joeq.Class.jq_StaticMethod;

/*
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: CompilerInterface.java,v 1.9 2004/03/11 03:55:04 jwhaley Exp $
 */
public interface CompilerInterface {
    
    jq_CompiledCode compile(jq_Method m);
    jq_CompiledCode generate_compile_stub(jq_Method m);
    
    jq_StaticMethod getInvokestaticLinkMethod();
    jq_StaticMethod getInvokespecialLinkMethod();
    jq_StaticMethod getInvokeinterfaceLinkMethod();
}
