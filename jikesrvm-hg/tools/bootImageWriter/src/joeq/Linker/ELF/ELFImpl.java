// ELFImpl.java, created Thu May  8 12:49:13 2003 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Linker.ELF;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.io.IOException;

/**
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: ELFImpl.java,v 1.4 2004/09/30 03:35:33 joewhaley Exp $
 */
abstract class ELFImpl implements ELF, ELFConstants {

    protected byte ei_class;
    protected byte ei_data;
    protected int e_type;
    protected int e_machine;
    protected int e_version;
    protected int e_entry;
    //protected int e_phoff;
    //protected int e_shoff;
    protected int e_flags;
    
    protected List<ProgramHeader> program_headers;
    protected List<Section> sections;
    protected Section.StrTabSection section_header_string_table;
    
    /** Creates new ELFImpl */
    public ELFImpl(byte data, int type, int machine, int entry) {
        ei_class = ELFCLASS32;
        ei_data = data;
        e_type = type;
        e_machine = machine;
        e_entry = entry;
        e_version = EV_CURRENT;
        program_headers = new LinkedList<ProgramHeader>();
        sections = new LinkedList<Section>();
    }
    protected ELFImpl() {
        ei_class = ELFCLASS32;
        ei_data = ELFDATA2LSB;
        e_type = ET_REL;
        e_machine = EM_386;
        e_version = EV_CURRENT;
        program_headers = new LinkedList<ProgramHeader>();
        sections = new LinkedList<Section>();
    }

    public Section.StrTabSection getSectionHeaderStringTable() {
        return section_header_string_table;
    }
    
    public void setSectionHeaderStringTable(Section.StrTabSection shstrtab) {
        this.section_header_string_table = shstrtab;
    }
    
    public int getSectionIndex(Section s) {
        if (s == null)
            return SHN_UNDEF;
        return s.getIndex();
    }
    
    public Section getSection(int i) {
        if (i == SHN_ABS) return Section.AbsSection.INSTANCE;
        return sections.get(i);
    }
    
    public List/*<Section>*/<Section> getSections() {
        return sections;
    }
    
    public void addSection(Section s) {
        sections.add(s);
    }
    public void removeSection(Section s) {
        sections.remove(s);
    }
    public void addProgramHeader(ProgramHeader p) {
        program_headers.add(p);
    }
    public void removeProgramHeader(ProgramHeader p) {
        program_headers.remove(p);
    }
    
    public boolean isLittleEndian() { return ei_data == ELFDATA2LSB; }
    public boolean isBigEndian() { return ei_data == ELFDATA2MSB; }
    public void setLittleEndian() { ei_data = ELFDATA2LSB; }
    public void setBigEndian() { ei_data = ELFDATA2MSB; }
    
    public void write() throws IOException {
        // sanity check - sections should include the string tables.
//        if (section_header_string_table != null)
//            Assert._assert(sections.contains(section_header_string_table));
        
        // add section header names to the section header string table.
        if (section_header_string_table != null) {
            Iterator<Section> si = sections.iterator();
            while (si.hasNext()) {
                Section s = si.next();
                section_header_string_table.addString(s.getName());
            }
        }
        
        // file offsets for the program header table and the section table.
        int e_phoff, e_shoff, soff, poff = 0;
        
        // calculate program header table offset.
        if (program_headers.isEmpty()) {
            e_phoff = 0;
            soff = e_shoff = ELFImpl.getHeaderSize();
        } else {
            e_phoff = ELFImpl.getHeaderSize();
            if(sections.isEmpty())
            {
            	soff = 0;
            	e_shoff = 0;
            }
            else
            {
            	poff = soff = e_shoff = e_phoff + (program_headers.size() * ProgramHeader.getSize());
            }
            poff = e_phoff + (program_headers.size() * ProgramHeader.getSize());
        }
                
        // pack all sections and calculate section header offset.
        Iterator<Section> si = sections.iterator();
        Section s = null;
        if(!sections.isEmpty())
        {
	        s = si.next();
	//        Assert._assert(s instanceof Section.NullSection);
	        int i = 0;
	        while (si.hasNext()) {
	            s = si.next();
	            if (s instanceof Section.StrTabSection) {
	                Section.StrTabSection ss = (Section.StrTabSection)s;
	                if (ss.getNumberOfEntries() < 10000)
	                    ss.super_pack();
	                else
	                    ss.pack();
	            } else if (s instanceof Section.SymTabSection) {
	                Section.SymTabSection ss = (Section.SymTabSection)s;
	                ss.setIndices();
	            }
	            if (!(s instanceof Section.NoBitsSection))
	                e_shoff += s.getSize();
	            s.setIndex(++i);
	        }
        }
        // now, actually do the writing.
        // write the header.
        writeHeader(e_phoff, e_shoff);
        
        Iterator pi = program_headers.iterator();
        while (pi.hasNext()) {
            ProgramHeader p = (ProgramHeader)pi.next();
            p.writeHeader(this);
        }
        
        // write the section data
//        soff = poff;
        si = sections.iterator();
        while (si.hasNext()) {
            s = si.next();
            s.writeData(this);
        }
        
        // write the section header table
        si = sections.iterator();
        while (si.hasNext()) {
            s = si.next();
            s.writeHeader(this, soff);
            if (!(s instanceof Section.NoBitsSection))
                soff += s.getSize();
        }
    }

    private void processProgramHeaders() {
	}

    void writeHeader(int e_phoff, int e_shoff) throws IOException {
        writeIdent();
        write_half(e_type);
        write_half(e_machine);
        write_word(e_version);
        write_addr(e_entry);
        write_off(e_phoff);
        write_off(e_shoff);
        write_word(e_flags);
        write_half(getHeaderSize());
        write_half(ProgramHeader.getSize());
        write_half(program_headers.size());
        write_half(Section.getHeaderSize());
        write_half(sections.size());
        write_half(getSectionIndex(section_header_string_table));
    }
    
    void writeIdent() throws IOException {
        writeMagicNumber();
        write_byte(ei_class);
        write_byte(ei_data);
        write_byte((byte)e_version);
        for (int i=7; i<16; ++i)
            write_byte((byte)0);
    }

    void writeMagicNumber() throws IOException {
        write_byte(ELFMAG0);
        write_byte(ELFMAG1);
        write_byte(ELFMAG2);
        write_byte(ELFMAG3);
    }
    
    public static int getHeaderSize() { return 52; }
    
}
