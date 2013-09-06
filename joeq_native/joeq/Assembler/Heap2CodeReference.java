// Heap2CodeReference.java, created Tue Feb 27  2:59:43 2001 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Assembler;

import java.io.DataOutput;
import java.io.IOException;
import joeq.Allocator.DefaultCodeAllocator;
import joeq.Memory.CodeAddress;
import joeq.Memory.HeapAddress;

/**
 * Heap2CodeReference
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: Heap2CodeReference.java,v 1.3 2004/09/30 03:35:29 joewhaley Exp $
 */
public class Heap2CodeReference extends Reloc {

    HeapAddress from_heaploc;
    CodeAddress to_codeloc;
    
    public Heap2CodeReference(HeapAddress from_heaploc, CodeAddress to_codeloc) {
        this.from_heaploc = from_heaploc; this.to_codeloc = to_codeloc;
    }

    public HeapAddress getFrom() { return from_heaploc; }
    public CodeAddress getTo() { return to_codeloc; }
    
    public void patch() {
        DefaultCodeAllocator.patchAbsolute(from_heaploc, to_codeloc);
    }
    
    public void dumpCOFF(DataOutput out) throws IOException {
        out.writeInt(from_heaploc.to32BitValue()); // r_vaddr
        out.writeInt(0);                           // r_symndx
        out.writeChar(Reloc.RELOC_ADDR32);         // r_type
    }
    
    public String toString() {
        return "from heap:"+from_heaploc.stringRep()+" to code:"+to_codeloc.stringRep();
    }

    public boolean equals(Heap2CodeReference that) {
        if (this.from_heaploc.difference(that.from_heaploc) != 0)
            return false;
        return true;
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof Heap2CodeReference) {
            return equals((Heap2CodeReference) obj);
        }
        return false;
    }
    
    public int hashCode() {
        // Note: hash code changes depending on address relocation!
        int v1 = from_heaploc.to32BitValue();
        return v1;
    }
}
