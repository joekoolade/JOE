// Section.java, created Wed Mar  6 18:38:47 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Linker.ELF;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import jwutil.collections.AppendIterator;
import jwutil.strings.Strings;
import jwutil.util.Assert;

/**
 * Defines a section in an ELF file.
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: Section.java,v 1.23 2004/09/30 03:35:33 joewhaley Exp $
 */
public abstract class Section implements ELFConstants {
    
    public static final String DEFAULT_ENCODING = "ISO-8859-1";
    
    protected String name;
    protected int index;
    protected int flags;
    protected int addr;

    public int getIndex() { return index; }
    public String getName() { return name; }
    public abstract int getType();
    public int getFlags() { return flags; }
    public int getAddr() { return addr; }
    public abstract int getSize();
    public abstract int getLink();
    public abstract int getInfo();
    public abstract int getAddrAlign();
    public abstract int getEntSize();

    public void setIndex(int index) { this.index = index; }
    public void setName(String name) { this.name = name; }
    public void setAddr(int addr) { this.addr = addr; }
    public void setWrite() { this.flags |= SHF_WRITE; }
    public void clearWrite() { this.flags &= ~SHF_WRITE; }
    public void setAlloc() { this.flags |= SHF_ALLOC; }
    public void clearAlloc() { this.flags &= ~SHF_ALLOC; }
    public void setExecInstr() { this.flags |= SHF_EXECINSTR; }
    public void clearExecInstr() { this.flags &= ~SHF_EXECINSTR; }

    public void writeHeader(ELF file, int offset) throws IOException {
        file.write_sectionname(this.getName());
        file.write_word(this.getType());
        file.write_word(this.getFlags());
        file.write_addr(this.getAddr());
        file.write_off(offset);
        file.write_word(this.getSize());
        file.write_word(this.getLink());
        file.write_word(this.getInfo());
        file.write_word(this.getAddrAlign());
        file.write_word(this.getEntSize());
    }
    
    public abstract void writeData(ELF file) throws IOException;
    public abstract void load(UnloadedSection s, ELF file) throws IOException;
    
    /** Creates new Section */
    protected Section(String name, int flags, int addr) {
        this.name = name; this.flags = flags; this.addr = addr;
    }

    protected Section(int flags, int addr) {
        this.flags = flags; this.addr = addr;
    }
    
    public static class UnloadedSection {
        
        int sectionNameIndex;
        int type;
        int flags;
        int addr;
        int offset;
        int size;
        int link;
        int info;
        int addralign;
        int entsize;
        
        public UnloadedSection(ELF file) throws IOException {
            readHeader(file);
        }
        
        public void readHeader(ELF file) throws IOException {
            this.sectionNameIndex = file.read_word();
            this.type = file.read_word();
            this.flags = file.read_word();
            this.addr = file.read_addr();
            this.offset = file.read_off();
            this.size = file.read_word();
            this.link = file.read_word();
            this.info = file.read_word();
            this.addralign = file.read_word();
            this.entsize = file.read_word();
        }

        public Section parseHeader() throws IOException {
            switch (type) {
            case SHT_NULL: {
                if (this.flags != 0)
                    System.err.println("Warning! Null section flags is not 0: "+this.flags);
                if (this.addr != 0)
                    System.err.println("Warning! Null section addr is not 0: "+this.addr);
                if (this.offset != 0)
                    System.err.println("Warning! Null section offset is not 0: "+this.offset);
                if (this.size != 0)
                    System.err.println("Warning! Null section size is not 0: "+this.size);
                if (this.link != SHN_UNDEF)
                    System.err.println("Warning! Null section link is not SHN_UNDEF: "+this.link);
                if (this.info != 0)
                    System.err.println("Warning! Null section info is not 0: "+this.info);
                if (this.addralign != 0)
                    System.err.println("Warning! Null section addralign is not 0: "+this.addralign);
                if (this.entsize != 0)
                    System.err.println("Warning! Null section entsize is not 0: "+this.entsize);
                return NullSection.INSTANCE;
            }
            case SHT_PROGBITS: {
                Assert._assert(this.link == SHN_UNDEF);
                Assert._assert(this.info == 0);
                Assert._assert(this.entsize == 0);
                return ProgBitsSectionImpl.empty(this.flags, this.addr, this.addralign);
            }
            case SHT_SYMTAB: {
                Assert._assert(this.addralign == 4);
                Assert._assert(this.entsize == SymbolTableEntry.getEntrySize());
                return SymTabSection.empty(this.flags, this.addr);
            }
            case SHT_STRTAB: {
                if (this.link != SHN_UNDEF)
                    System.err.println("Warning! Strtab section link is not SHN_UNDEF: "+this.link);
                if (this.info != 0)
                    System.err.println("Warning! Strtab section info is not 0: "+this.info);
                if (this.addralign != 1)
                    System.err.println("Warning! Strtab section addralign is not 0: "+this.addralign);
                if (this.entsize != 0)
                    System.err.println("Warning! Strtab section entsize is not 0: "+this.entsize);
                return StrTabSection.empty(this.flags, this.addr);
            }
            case SHT_RELA:
            case SHT_HASH:
            case SHT_DYNAMIC:
            case SHT_DYNSYM: {
                Assert.TODO(); return null;
            }
            case SHT_NOTE: {
                if (this.link != SHN_UNDEF)
                    System.err.println("Warning! Note section link is not SHN_UNDEF: "+this.link);
                if (this.info != 0)
                    System.err.println("Warning! Note section info is not 0: "+this.info);
                if (this.addralign != 1)
                    System.err.println("Warning! Note section addralign is not 0: "+this.addralign);
                if (this.entsize != 0)
                    System.err.println("Warning! Note section entsize is not 0: "+this.entsize);
                return NoteSection.empty(this.flags, this.addr);
            }
            case SHT_NOBITS: {
                if (this.link != SHN_UNDEF)
                    System.err.println("Warning! Nobits section link is not SHN_UNDEF: "+this.link);
                if (this.info != 0)
                    System.err.println("Warning! Nobits section info is not 0: "+this.info);
                if (this.entsize != 0)
                    System.err.println("Warning! Nobits section entsize is not 0: "+this.entsize);
                return NoBitsSection.empty(this.flags, this.addr, this.size, this.addralign);
            }
            case SHT_REL:
                Assert._assert(this.addralign == 4);
                Assert._assert(this.entsize == RelocEntry.getEntrySize());
                return RelSection.empty(this.flags, this.addr);
            case SHT_SHLIB:
            default:
                // unsupported.
                throw new IOException("bad section type: "+Strings.hex(type));
            }
        }
        
    }
    
    public abstract static class FakeSection extends Section {
        
        FakeSection(String name, int index) { super(name, 0, 0); this.index = index; }
        public int getEntSize() { Assert.UNREACHABLE(); return 0; }
        public int getInfo() { Assert.UNREACHABLE(); return 0; }
        public int getAddrAlign() { Assert.UNREACHABLE(); return 0; }
        public int getLink() { Assert.UNREACHABLE(); return 0; }
        public int getSize() { Assert.UNREACHABLE(); return 0; }
        public int getType() { Assert.UNREACHABLE(); return 0; }
        public int getFlags() { Assert.UNREACHABLE(); return 0; }
        public int getAddr() { Assert.UNREACHABLE(); return 0; }
        public int getIndex() { return index; }
        public void setName(String name) { Assert.UNREACHABLE(); }
        public void setAddr(int addr) { Assert.UNREACHABLE(); }
        public void setWrite() { Assert.UNREACHABLE(); }
        public void clearWrite() { Assert.UNREACHABLE(); }
        public void setAlloc() { Assert.UNREACHABLE(); }
        public void clearAlloc() { Assert.UNREACHABLE(); }
        public void setExecInstr() { Assert.UNREACHABLE(); }
        public void clearExecInstr() { Assert.UNREACHABLE(); }
        public void writeData(ELF file) throws IOException {
            Assert.UNREACHABLE();
        }
        public void load(UnloadedSection s, ELF file) throws IOException {
            Assert.UNREACHABLE();
        }
        
    }
    
    public static class AbsSection extends FakeSection {
        public static final AbsSection INSTANCE = new AbsSection();
        private AbsSection() { super("ABS", SHN_ABS); }
    }
        
    public static class NullSection extends Section {
        public static final NullSection INSTANCE = new NullSection();
        private NullSection() { super("", 0, 0); }
        public int getIndex() { return 0; }
        public int getType() { return SHT_NULL; }
        public int getSize() { return 0; }
        public int getLink() { return SHN_UNDEF; }
        public int getInfo() { return 0; }
        public int getAddrAlign() { return 0; }
        public int getEntSize() { return 0; }
        public void setIndex(int index) { Assert._assert(index == 0); }
        public void setName(String name) { Assert._assert(name.equals("")); }
        public void setAddr(int addr) { Assert._assert(addr == 0); }
        public void setOffset(int offset) { Assert._assert(offset == 0); }
        public void setWrite() { Assert.UNREACHABLE(); }
        public void setAlloc() { Assert.UNREACHABLE(); }
        public void setExecInstr() { Assert.UNREACHABLE(); }
        public void writeData(ELF file) throws IOException { }
        public void load(UnloadedSection s, ELF file) throws IOException { }
    }
    
    public abstract static class ProgBitsSection extends Section {
        public ProgBitsSection(String name, int flags, int addr) {
            super(name, flags, addr);
        }
        protected ProgBitsSection(int flags, int addr) {
            super(flags, addr);
        }
        public final int getType() { return SHT_PROGBITS; }
        public final int getLink() { return SHN_UNDEF; }
        public final int getInfo() { return 0; }
        public final int getEntSize() { return 0; }
        
    }
    
    public static class ProgBitsSectionImpl extends ProgBitsSection {
        protected int addralign;
        protected byte[] data;
        public ProgBitsSectionImpl(String name, int flags, int addr, int addralign, byte[] data) {
            super(name, flags, addr);
            this.addralign = addralign;
            this.data = data;
        }
        protected ProgBitsSectionImpl(int flags, int addr, int addralign) {
            super(flags, addr);
            this.addralign = addralign;
        }
        public final int getSize() { return data.length; }
        public final int getAddr() { return addr; }
        public final int getAddrAlign() { return addralign; }
        public void writeData(ELF file) throws IOException {
            file.write_bytes(data);
        }
        
        public static ProgBitsSectionImpl empty(int flags, int addr, int addralign) {
            return new ProgBitsSectionImpl(flags, addr, addralign);
        }
        
        public void load(UnloadedSection s, ELF file) throws IOException {
            if (s.sectionNameIndex != 0) {
                StrTabSection ss = file.getSectionHeaderStringTable();
                if (ss == null) throw new IOException();
                this.name = ss.getString(s.sectionNameIndex);
            }
            this.data = new byte[s.size];
            file.set_position(s.offset);
            file.read_bytes(data);
        }
    }
    
    public static class SymTabSection extends Section {
        List/*<SymbolTableEntry>*/ localSymbols, globalSymbols;
        StrTabSection stringTable;
        public SymTabSection(String name, int flags, int addr, StrTabSection stringTable) {
            super(name, flags, addr);
            this.stringTable = stringTable;
            this.localSymbols = new LinkedList(); this.globalSymbols = new LinkedList();
            addSymbol(SymbolTableEntry.EmptySymbolTableEntry.INSTANCE);
        }
        protected SymTabSection(int flags, int addr) {
            super(flags, addr);
        }
        public void addSymbol(SymbolTableEntry e) {
            stringTable.addString(e.getName());
            if (e.getBind() == SymbolTableEntry.STB_LOCAL) localSymbols.add(e);
            else globalSymbols.add(e);
        }
        public int getSize() { return (localSymbols.size() + globalSymbols.size()) * SymbolTableEntry.getEntrySize(); }
        public int getAddrAlign() { return 4; }
        public final int getType() { return SHT_SYMTAB; }
        public final int getLink() { return stringTable.getIndex(); }
        public final int getInfo() { return localSymbols.size(); }
        public final int getEntSize() { return SymbolTableEntry.getEntrySize(); }
        public void setIndices() {
            Iterator i = new AppendIterator(localSymbols.iterator(), globalSymbols.iterator());
            int j=-1;
            while (i.hasNext()) {
                SymbolTableEntry e = (SymbolTableEntry)i.next();
                e.setIndex(++j);
            }
        }
        public SymbolTableEntry getSymbolTableEntry(int i) {
            if (i < localSymbols.size()) return (SymbolTableEntry)localSymbols.get(i);
            i -= localSymbols.size();
            return (SymbolTableEntry)globalSymbols.get(i);
        }
        public void writeData(ELF file) throws IOException {
            Iterator i = new AppendIterator(localSymbols.iterator(), globalSymbols.iterator());
            while (i.hasNext()) {
                SymbolTableEntry e = (SymbolTableEntry)i.next();
                e.write(file, stringTable);
            }
        }
        
        public static SymTabSection empty(int flags, int addr) {
            return new SymTabSection(flags, addr);
        }
        
        public void load(UnloadedSection s, ELF file) throws IOException {
            if (s.sectionNameIndex != 0) {
                StrTabSection ss = file.getSectionHeaderStringTable();
                if (ss == null) throw new IOException();
                this.name = ss.getString(s.sectionNameIndex);
            }
            this.stringTable = (StrTabSection)file.getSection(s.link);
            file.set_position(s.offset);
            int n = s.size / this.getEntSize();
            if (s.size % this.getEntSize() != 0) throw new IOException();
            this.localSymbols = new LinkedList(); // size is s.info
            this.globalSymbols = new LinkedList(); // size is n - s.info
            while (--n >= 0) {
                SymbolTableEntry e = SymbolTableEntry.read(file, this.stringTable);
                this.addSymbol(e);
            }
            if (this.getInfo() != s.info) throw new IOException();
        }

    }
    
    public static class DynSymSection extends Section {
        List/*<SymbolTableEntry>*/ localSymbols, globalSymbols;
        StrTabSection stringTable;
        public DynSymSection(String name, int flags, int addr, StrTabSection stringTable) {
            super(name, flags, addr);
            this.stringTable = stringTable;
            this.localSymbols = new LinkedList(); this.globalSymbols = new LinkedList();
        }
        protected DynSymSection(int flags, int addr) {
            super(flags, addr);
        }
        public void addSymbol(SymbolTableEntry e) {
            stringTable.addString(e.getName());
            if (e.getBind() == SymbolTableEntry.STB_LOCAL) localSymbols.add(e);
            else globalSymbols.add(e);
        }
        public int getSize() { return (localSymbols.size() + globalSymbols.size()) * SymbolTableEntry.getEntrySize(); }
        public int getAddrAlign() { return 4; }
        public final int getType() { return SHT_DYNSYM; }
        public final int getLink() { return stringTable.getIndex(); }
        public final int getInfo() { return localSymbols.size(); }
        public final int getEntSize() { return SymbolTableEntry.getEntrySize(); }
        public SymbolTableEntry getSymbolTableEntry(int i) {
            if (i < localSymbols.size()) return (SymbolTableEntry)localSymbols.get(i);
            i -= localSymbols.size();
            return (SymbolTableEntry)globalSymbols.get(i);
        }
        public void writeData(ELF file) throws IOException {
            Iterator i = new AppendIterator(localSymbols.iterator(), globalSymbols.iterator());
            while (i.hasNext()) {
                SymbolTableEntry e = (SymbolTableEntry)i.next();
                e.write(file, stringTable);
            }
        }
        
        public static DynSymSection empty(int flags, int addr) {
            return new DynSymSection(flags, addr);
        }
        
        public void load(UnloadedSection s, ELF file) throws IOException {
            if (s.sectionNameIndex != 0) {
                StrTabSection ss = file.getSectionHeaderStringTable();
                if (ss == null) throw new IOException();
                this.name = ss.getString(s.sectionNameIndex);
            }
            this.stringTable = (StrTabSection)file.getSection(s.link);
            file.set_position(s.offset);
            int n = s.size / this.getEntSize();
            if (s.size % this.getEntSize() != 0) throw new IOException();
            this.localSymbols = new LinkedList(); // size is s.info
            this.globalSymbols = new LinkedList(); // size is n - s.info
            while (--n >= 0) {
                SymbolTableEntry e = SymbolTableEntry.read(file, this.stringTable);
                this.addSymbol(e);
            }
            if (this.getInfo() != s.info) throw new IOException();
        }
        
    }
    
    public static class StrTabSection extends Section {
        protected Map/*<String, Integer>*/ string_map;
        protected byte[] table;
        public StrTabSection(String name, int flags, int addr) {
            super(name, flags, addr);
            string_map = new HashMap();
        }
        protected StrTabSection(int flags, int addr) {
            super(flags, addr);
        }
        public final int getType() { return SHT_STRTAB; }
        public final int getLink() { return SHN_UNDEF; }
        public final int getInfo() { return 0; }
        public final int getEntSize() { return 0; }

        public final int getNumberOfEntries() { return string_map.size(); }

        public void addString(String s) { string_map.put(s, null); }

        public void super_pack() {
            // separate into bins by size
            SortedMap tm = new TreeMap();
            Iterator i = string_map.keySet().iterator();
            while (i.hasNext()) {
                String s = (String)i.next();
                int l = -s.length();
                Integer in = new Integer(l);
                Set set = (Set)tm.get(in);
                if (set == null)
                    tm.put(in, set = new HashSet());
                set.add(s);
            }
            // go through bins in reverse-size order.
            List string_set = new LinkedList();
            i = tm.entrySet().iterator();
            int index = 1;
            while (i.hasNext()) {
                Map.Entry e = (Map.Entry)i.next();
                int in = -((Integer)e.getKey()).intValue();
                if (in == 0) break;
                Set set = (Set)e.getValue();
                for (Iterator j=set.iterator(); j.hasNext(); ) {
                    String s1 = (String)j.next();
                    Assert._assert(s1.length() == in);
                    int index2;
                    for (Iterator k=string_set.iterator(); ; ) {
                        if (!k.hasNext()) {
                            index2 = index;
                            index += in + 1;
                            string_set.add(s1);
                            break;
                        }
                        String s2 = (String)k.next();
                        if (s2.length() == in) {
                            index2 = index;
                            index += in + 1;
                            string_set.add(s1);
                            break;
                        }
                        if (s2.endsWith(s1)) {
                            //System.out.println("String \""+s1+"\" shares ending with \""+s2+"\"");
                            index2 = ((Integer)string_map.get(s2)).intValue();
                            index2 += s2.length();
                            index2 -= in;
                            break;
                        }
                    }
                    string_map.put(s1, new Integer(index2));
                }
            }
            //if (index == 1) index = 0;
            table = new byte[index];
            i = string_map.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry e = (Map.Entry)i.next();
                String s = (String)e.getKey();
                if (e.getValue() == null) continue;
                index = ((Integer)e.getValue()).intValue();
                //System.out.println("Writing "+s.length()+" bytes for \""+s+"\" to table index "+index);
                
                if (false) {
                    // deprecated
                    //s.getBytes(0, s.length(), table, index);
                } else {
                    try {
                        byte[] b = s.getBytes(DEFAULT_ENCODING);
                        System.arraycopy(b, 0, table, index, b.length);
                    } catch (UnsupportedEncodingException x) { Assert.UNREACHABLE(); }
                }
            }
        }
        
        public void pack() {
            int size = 1;
            Iterator i = string_map.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry e = (Map.Entry)i.next();
                String s = (String)e.getKey();
                if (s.length() == 0) {
                    e.setValue(new Integer(0));
                } else {
                    e.setValue(new Integer(size));
                    size += s.length() + 1;
                }
            }
            if (size == 1) size = 0;
            // todo: combine strings that have the same endings.
            table = new byte[size];
            int index = 1;
            i = string_map.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry e = (Map.Entry)i.next();
                String s = (String)e.getKey();
                if (s.length() == 0) {
                    Assert._assert(((Integer)e.getValue()).intValue() == 0);
                    continue;
                }
                //System.out.println("Writing "+s.length()+" bytes for \""+s+"\" to table index "+index);
                
                if (false) {
                    // deprecated
                    //s.getBytes(0, s.length(), table, index);
                } else {
                    try {
                        byte[] b = s.getBytes(DEFAULT_ENCODING);
                        Assert._assert(b.length == s.length(), s);
                        System.arraycopy(b, 0, table, index, b.length);
                    } catch (UnsupportedEncodingException x) { Assert.UNREACHABLE(); }
                }
                Assert._assert(((Integer)e.getValue()).intValue() == index, s);
                index += s.length() + 1;
            }
            Assert._assert(size == 0 || size == index);
        }
        
        public int getStringIndex(String s) {
            Integer i = (Integer)string_map.get(s);
            if (i == null)
                return 0;
            Assert._assert(getString(i.intValue()).equals(s), s);
            return i.intValue();
        }
        public String getString(int i) {
            int n=0;
            for (;;) {
                if (table[n+i] == '\0') break;
                ++n;
            }
            try {
                return new String(table, i, n, DEFAULT_ENCODING);
            } catch (UnsupportedEncodingException x) { Assert.UNREACHABLE(); return null; }
        }
        public int getSize() { return table.length; }
        public int getAddrAlign() { return 1; }
        public void writeData(ELF file) throws IOException {
            file.write_bytes(table);
        }
        
        public static StrTabSection empty(int flags, int addr) {
            return new StrTabSection(flags, addr);
        }
        
        public void load(UnloadedSection s, ELF file) throws IOException {
            this.table = new byte[s.size];
            file.set_position(s.offset);
            file.read_bytes(this.table);
            if (s.sectionNameIndex != 0) {
                StrTabSection ss = file.getSectionHeaderStringTable();
                if (ss == null) throw new IOException();
                this.name = ss.getString(s.sectionNameIndex);
            }
        }
    }
    
    public static class RelASection extends Section {
        protected List/*<RelocAEntry>*/ relocs;
        protected SymTabSection symbolTable;
        protected Section targetSection;
        public RelASection(String name, int flags, int addr, SymTabSection symbolTable, Section targetSection) {
            super(name, flags, addr);
            this.symbolTable = symbolTable; this.targetSection = targetSection;
            this.relocs = new LinkedList();
        }
        protected RelASection(int flags, int addr) {
            super(flags, addr);
        }
        public final int getType() { return SHT_RELA; }
        public final int getLink() { return symbolTable.getIndex(); }
        public final int getInfo() { return targetSection.getIndex(); }
        public final int getEntSize() { return RelocAEntry.getEntrySize(); }
        public int getSize() { return relocs.size() * RelocAEntry.getEntrySize(); }
        public int getAddrAlign() { return 4; }
        public void addReloc(RelocAEntry e) { relocs.add(e); }
        public void writeData(ELF file) throws IOException {
            Iterator i = relocs.iterator();
            while (i.hasNext()) {
                RelocAEntry e = (RelocAEntry)i.next();
                e.write(file);
            }
        }
        
        public static RelASection empty(int flags, int addr) {
            return new RelASection(flags, addr);
        }
        
        public void load(UnloadedSection s, ELF file) throws IOException {
            if (s.sectionNameIndex != 0) {
                StrTabSection ss = file.getSectionHeaderStringTable();
                if (ss == null) throw new IOException();
                this.name = ss.getString(s.sectionNameIndex);
            }
            this.symbolTable = (SymTabSection)file.getSection(s.link);
            this.targetSection = file.getSection(s.info);
            int n = s.size / getEntSize();
            if (n % getEntSize() != 0) throw new IOException();
            this.relocs = new LinkedList(); // size = n
            file.set_position(s.offset);
            while (--n >= 0) {
                RelocAEntry e = (RelocAEntry)RelocAEntry.read(file, this.symbolTable);
                this.addReloc(e);
            }
        }
        
    }
    
    public static class HashSection extends Section {
        protected int sectionIndex;
        public HashSection(String name, int flags, int addr, int sectionIndex) {
            super(name, flags, addr);
            this.sectionIndex = sectionIndex;
        }
        public final int getType() { return SHT_HASH; }
        public final int getLink() { return sectionIndex; }
        public final int getInfo() { return 0; }
        public final int getEntSize() { return 0; }
        public int getSize() { return 0; } // WRITE ME
        public int getAddrAlign() { return 0; }
        public void writeData(ELF file) throws IOException {
            // WRITE ME
        }
        public void load(UnloadedSection s, ELF file) throws IOException {
            // WRITE ME
        }
        
    }
    
    public static class DynamicSection extends Section {
        protected int stringTableIndex;
        public DynamicSection(String name, int flags, int addr, int stringTableIndex) {
            super(name, flags, addr);
            this.stringTableIndex = stringTableIndex;
        }
        public final int getType() { return SHT_DYNAMIC; }
        public final int getLink() { return stringTableIndex; }
        public final int getInfo() { return 0; }
        public final int getEntSize() { return 0; }
        public int getSize() { return 0; } // WRITE ME
        public int getAddrAlign() { return 0; }
        public void writeData(ELF file) throws IOException {
            // WRITE ME
        }
        
        public void load(UnloadedSection s, ELF file) throws IOException {
        }
        
    }
    
    public static class NoteSection extends Section {
        protected String notename;
        protected byte[] notedesc;
        protected int notetype;
        public NoteSection(String sectionname, int flags, int addr, String notename, byte[] notedesc, int notetype) {
            super(sectionname, flags, addr);
            this.notename = notename; this.notedesc = notedesc; this.notetype = notetype;
        }
        protected NoteSection(int flags, int addr) {
            super(flags, addr);
        }
        public final int getType() { return SHT_NOTE; }
        public final int getLink() { return SHN_UNDEF; }
        public final int getInfo() { return 0; }
        public final int getEntSize() { return 0; }
        protected int getNameLength() { return (notename.length()+4)&~3; }
        public int getSize() { return 12 + getNameLength() + notedesc.length; }
        public int getAddrAlign() { return 1; }
        public void writeData(ELF file) throws IOException {
            file.write_word(getNameLength());
            file.write_word(notedesc.length);
            file.write_word(notetype);
            byte[] notename_b = new byte[getNameLength()];
            if (false) {
                // deprecated
                //notename.getBytes(0, notename.length(), notename_b, 0);
            } else {
                try {
                    byte[] b = notename.getBytes(DEFAULT_ENCODING);
                    System.arraycopy(b, 0, notename_b, 0, b.length);
                } catch (UnsupportedEncodingException x) { Assert.UNREACHABLE(); }
            }
            file.write_bytes(notename_b);
            file.write_bytes(notedesc);
        }
        
        public static NoteSection empty(int flags, int addr) {
            return new NoteSection(flags, addr);
        }
        
        public void load(UnloadedSection s, ELF file) throws IOException {
            if (s.sectionNameIndex != 0) {
                StrTabSection ss = file.getSectionHeaderStringTable();
                if (ss == null) throw new IOException();
                this.name = ss.getString(s.sectionNameIndex);
            }
            file.set_position(s.offset);
            int nlength = file.read_word();
            int dlength = file.read_word();
            this.notetype = file.read_word();
            byte[] notename_b = new byte[nlength];
            file.read_bytes(notename_b);
            try {
                this.notename = new String(notename_b, DEFAULT_ENCODING);
            } catch (UnsupportedEncodingException x) { Assert.UNREACHABLE(); }
            this.notedesc = new byte[dlength];
            file.read_bytes(notedesc);
            if (this.getSize() != s.size) throw new IOException();
        }
    }
    
    public static class NoBitsSection extends Section {
        protected int size; protected int addralign;
        public NoBitsSection(String name, int flags, int addr, int size, int addralign) {
            super(name, flags, addr);
            this.size = size; this.addralign = addralign;
        }
        protected NoBitsSection(int flags, int addr, int size, int addralign) {
            super(flags, addr);
            this.size = size; this.addralign = addralign;
        }
        public final int getType() { return SHT_NOBITS; }
        public final int getLink() { return SHN_UNDEF; }
        public final int getInfo() { return 0; }
        public final int getEntSize() { return 0; }
        public int getSize() { return size; }
        public int getAddrAlign() { return addralign; }
        public void writeData(ELF file) throws IOException { }
        
        public static NoBitsSection empty(int flags, int addr, int size, int addralign) {
            return new NoBitsSection(flags, addr, size, addralign);
        }
        
        public void load(UnloadedSection s, ELF file) throws IOException {
            if (s.sectionNameIndex != 0) {
                StrTabSection ss = file.getSectionHeaderStringTable();
                if (ss == null) throw new IOException();
                this.name = ss.getString(s.sectionNameIndex);
            }
        }
    }
    
    public static class RelSection extends Section {
        protected List/*<RelocEntry>*/ relocs;
        protected SymTabSection symbolTable;
        protected Section targetSection;
        public RelSection(String name, int flags, int addr, SymTabSection symbolTable, Section targetSection) {
            super(name, flags, addr);
            this.symbolTable = symbolTable; this.targetSection = targetSection;
            this.relocs = new LinkedList();
        }
        protected RelSection(int flags, int addr) {
            super(flags, addr);
        }
        public final int getType() { return SHT_REL; }
        public final int getLink() { return symbolTable.getIndex(); }
        public final int getInfo() { return targetSection.getIndex(); }
        public final int getEntSize() { return RelocEntry.getEntrySize(); }
        public int getSize() { return relocs.size() * RelocEntry.getEntrySize(); }
        public int getAddrAlign() { return 4; }
        public void addReloc(RelocEntry e) { relocs.add(e); }
        public void writeData(ELF file) throws IOException {
            Iterator i = relocs.iterator();
            while (i.hasNext()) {
                RelocEntry e = (RelocEntry)i.next();
                e.write(file);
            }
        }
        
        public static RelSection empty(int flags, int addr) {
            return new RelSection(flags, addr);
        }
        
        public void load(UnloadedSection s, ELF file) throws IOException {
            if (s.sectionNameIndex != 0) {
                StrTabSection ss = file.getSectionHeaderStringTable();
                if (ss == null) throw new IOException();
                this.name = ss.getString(s.sectionNameIndex);
            }
            this.symbolTable = (SymTabSection)file.getSection(s.link);
            this.targetSection = file.getSection(s.info);
            int n = s.size / getEntSize();
            if (n % getEntSize() != 0) throw new IOException();
            this.relocs = new LinkedList(); // size = n
            file.set_position(s.offset);
            while (--n >= 0) {
                RelocEntry e = RelocEntry.read(file, this.symbolTable);
                this.addReloc(e);
            }
        }
    }
    
    public static int getHeaderSize() { return 40; }
}
