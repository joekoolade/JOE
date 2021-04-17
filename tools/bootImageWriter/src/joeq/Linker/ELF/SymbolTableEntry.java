// SymbolTableEntry.java, created Wed Mar  6 18:38:47 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Linker.ELF;

import java.io.IOException;

/**
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: SymbolTableEntry.java,v 1.12 2004/09/30 03:35:33 joewhaley Exp $
 */
public class SymbolTableEntry implements ELFConstants {

    protected int index;
    protected String name;
    protected int value;
    protected int size;
    protected byte info;
    protected Section section;
    
    public SymbolTableEntry(String name, int value, int size, byte bind, byte type, Section section) {
        this.name = name; this.value = value; this.size = size; this.info = (byte)((bind<<4) | type); this.section = section;
    }

    public final String getName() { return name; }
    public final int getValue() { return value; }
    public final int getSize() { return size; }
    public final byte getBind() { return (byte)(info>>4); }
    public final byte getType() { return (byte)(info&0xf); }
    public final byte getInfo() { return info; }
    public final byte getOther() { return 0; }
    public final int getSHndx() { return section.getIndex(); }

    public void setIndex(int index) { /*Assert._assert(index != 0);*/ this.index = index; }
    public int getIndex() { /*Assert._assert(this.index != 0);*/ return this.index; }
    
    public void write(ELF file, Section.StrTabSection sts) throws IOException {
        file.write_word(sts.getStringIndex(getName()));
        file.write_addr(getValue());
        file.write_word(getSize());
        file.write_byte((byte)getInfo());
        file.write_byte((byte)getOther());
        file.write_half(getSHndx());
    }
    
    public static SymbolTableEntry read(ELF file, Section.StrTabSection sts) throws IOException {
        int symbolname = file.read_word();
        int value = file.read_addr();
        int size = file.read_word();
        byte info = file.read_byte();
        byte other = file.read_byte();
        int shndx = file.read_half();
        String name;
        if (symbolname != 0) {
            name = sts.getString(symbolname);
        } else {
            name = "";
        }
        Section s = file.getSection(shndx);
        return new SymbolTableEntry(name, value, size, (byte)((info >> 4) & 0xF), (byte)(info & 0xF), s);
    }
    
    public static class EmptySymbolTableEntry extends SymbolTableEntry {
        private EmptySymbolTableEntry() { super("", 0, 0, STB_LOCAL, STT_NOTYPE, Section.NullSection.INSTANCE); }
        public final int getIndex() { return 0; }
        public final void setIndex(int index) { /*Assert._assert(index == 0);*/ }
        public static final EmptySymbolTableEntry INSTANCE = new EmptySymbolTableEntry();
    }

    public static int getEntrySize() { return 16; }
}
