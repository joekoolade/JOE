// ELF.java, created Sat May 25 12:46:16 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Linker.ELF;

import java.util.List;
import java.io.IOException;

/**
 * @author John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: ELF.java,v 1.13 2004/09/30 03:35:33 joewhaley Exp $
 */
public interface ELF {
    
    //// READ-ONLY METHODS
    
    /**
     * Returns the list of ELF sections in this object.
     * @return list of ELF sections in this object
     */
    List getSections();
    
    Section getSection(int index);
    
    /**
     * Returns the section header string table, if one has been defined.
     * Or null otherwise.
     * @return section header string table
     */
    Section.StrTabSection getSectionHeaderStringTable();
    
    /**
     * Returns true if this ELF object is little-endian.
     * @return true if this ELF object is little-endian
     */
    boolean isLittleEndian();
    
    /**
     * Returns true if this ELF object is big-endian.
     * @return true if this ELF object is big-endian
     */
    boolean isBigEndian();
    
    //// UPDATING METHODS
    
    /**
     * Sets the section header string table to be the given section.
     * @param shstrtab new section header string table
     */
    void setSectionHeaderStringTable(Section.StrTabSection shstrtab);
    
    /**
     * Adds the given ELF section to this object.
     * @param s section to add
     */
    void addSection(Section s);
    
    /**
     * Removes the given ELF section from this object.
     * @param s section to remove
     */
    void removeSection(Section s);
    
    /**
     * Adds the given ELF program header to this object.
     * @param p program header to add
     */
    void addProgramHeader(ProgramHeader p);
    
    /**
     * Removes the given ELF program header from this object.
     * @param p program header to remove
     */
    void removeProgramHeader(ProgramHeader p);
    
    void setLittleEndian();
    void setBigEndian();
    
    void write() throws IOException;
    
    void write_byte(byte v) throws IOException;
    void write_bytes(byte[] v) throws IOException;
    void write_half(int v) throws IOException;
    void write_word(int v) throws IOException;
    void write_sword(int v) throws IOException;
    void write_off(int v) throws IOException;
    void write_addr(int v) throws IOException;
    void write_sectionname(String s) throws IOException;
    
    void set_position(int offset) throws IOException;
    byte read_byte() throws IOException;
    void read_bytes(byte[] b) throws IOException;
    int read_half() throws IOException;
    int read_word() throws IOException;
    int read_sword() throws IOException;
    int read_off() throws IOException;
    int read_addr() throws IOException;
    
}
