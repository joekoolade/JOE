// DirectBindCall.java, created Tue Feb 27  2:59:43 2001 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Assembler;

import java.io.DataOutput;
import java.io.IOException;
import joeq.Allocator.DefaultCodeAllocator;
import joeq.Class.jq_CompiledCode;
import joeq.Class.jq_Method;
import joeq.Memory.CodeAddress;
import jwutil.util.Assert;

/**
 * DirectBindCall
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: DirectBindCall.java,v 1.2 2004/09/30 03:35:29 joewhaley Exp $
 */
public class DirectBindCall extends Reloc {

    private CodeAddress source;
    private jq_Method target;

    public DirectBindCall(CodeAddress source, jq_Method target) {
        this.source = source; this.target = target;
    }
    
    public void patch() {
        patchTo(target.getDefaultCompiledVersion());
    }
    
    public void patchTo(jq_CompiledCode cc) {
        Assert._assert(cc != null);
        DefaultCodeAllocator.patchRelativeOffset(source, cc.getEntrypoint());
    }
    
    public CodeAddress getSource() { return source; }
    public jq_Method getTarget() { return target; }

    public String toString() {
        return "from code:"+source.stringRep()+" to method:"+target;
    }

    public void dumpCOFF(DataOutput out) throws IOException {
        out.writeInt(source.to32BitValue());       // r_vaddr
        out.writeInt(0);                           // r_symndx
        out.writeChar(Reloc.RELOC_ADDR32);         // r_type
    }
    
}
