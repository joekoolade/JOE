// RelocAEntry.java, created Wed Mar  6 18:38:47 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Linker.ELF;

import java.io.IOException;

/**
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: RelocAEntry.java,v 1.6 2004/03/09 06:26:56 jwhaley Exp $
 */
public class RelocAEntry extends RelocEntry {

    protected int addend;
    
    public RelocAEntry(int offset, SymbolTableEntry e, byte type, int addend) {
        super(offset, e, type);
        this.addend = addend;
    }

    public final int getAddEnd() { return addend; }
    
    public void write(ELF file) throws IOException {
        file.write_addr(getOffset());
        file.write_word(getInfo());
        file.write_sword(getAddEnd());
    }
    
    public static RelocEntry read(ELF file, Section.SymTabSection s) throws IOException {
        int offset = file.read_addr();
        int info = file.read_word();
        int addend = file.read_sword();
        int stindex = (info >>> 8);
        byte type = (byte)info;
        SymbolTableEntry e = s.getSymbolTableEntry(stindex);
        return new RelocAEntry(offset, e, type, addend);
    }
    
    public static int getEntrySize() { return 12; }
}
