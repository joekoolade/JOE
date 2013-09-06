// ELFRandomAccessFile.java, created Sat May 25 12:46:16 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Linker.ELF;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: ELFRandomAccessFile.java,v 1.9 2004/09/30 03:35:33 joewhaley Exp $
 */
public class ELFRandomAccessFile extends ELFImpl {
    
    protected RandomAccessFile file;
    protected List section_headers;
    public ELFRandomAccessFile(RandomAccessFile file) throws IOException {
        this.file = file;
        this.readHeader();
    }
    public ELFRandomAccessFile(byte data, int type, int machine, int entry, RandomAccessFile file) {
        super(data, type, machine, entry);
        this.file = file;
    }
    
    void readHeader() throws IOException {
        
        byte mag0 = read_byte();
        if (mag0 != ELFMAG0) throw new IOException();
        byte mag1 = read_byte();
        if (mag1 != ELFMAG1) throw new IOException();
        byte mag2 = read_byte();
        if (mag2 != ELFMAG2) throw new IOException();
        byte mag3 = read_byte();
        if (mag3 != ELFMAG3) throw new IOException();
        this.ei_class = read_byte();
        this.ei_data = read_byte();
        byte e_version2 = read_byte();
        for (int i=7; i<16; ++i) {
            byte b = read_byte();
            if (b != 0) throw new IOException();
        }
        this.e_type = read_half();
        this.e_machine = read_half();
        this.e_version = read_word();
        if (e_version2 != (byte)e_version) throw new IOException();
        this.e_entry = read_addr();
        int e_phoff = read_off();
        int e_shoff = read_off();
        this.e_flags = read_word();
        int headersize = read_half();
        if (headersize != ELFImpl.getHeaderSize()) throw new IOException();
        int programheadersize = read_half();
        int n_programheaders = read_half();
        if (n_programheaders > 0 && programheadersize != ProgramHeader.getSize()) throw new IOException();
        int sectionheadersize = read_half();
        int n_sectionheaders = read_half();
        if (n_sectionheaders > 0 && sectionheadersize != Section.getHeaderSize()) throw new IOException();
        int section_header_string_table_index = read_half();
        
        // read and parse section headers
        this.set_position(e_shoff);
        section_headers = new ArrayList(n_sectionheaders);
        for (int i=0; i<n_sectionheaders; ++i) {
            Section.UnloadedSection us = new Section.UnloadedSection(this);
            section_headers.add(us);
            Section ss = us.parseHeader();
            sections.add(ss);
        }
        
        // read section header string table
        if (section_header_string_table_index != 0) {
            this.section_header_string_table = (Section.StrTabSection)sections.get(section_header_string_table_index);
            Section.UnloadedSection us = (Section.UnloadedSection)section_headers.get(section_header_string_table_index);
            section_headers.set(section_header_string_table_index, null);
            this.section_header_string_table.load(us, this);
        }
    }
    
    public Section getSection(int i) {
        if (i == SHN_ABS) return Section.AbsSection.INSTANCE;
        Section s = (Section)sections.get(i);
        Section.UnloadedSection us = (Section.UnloadedSection)section_headers.get(i);
        if (us != null) {
            section_headers.set(i, null);
            try {
                s.load(us, this);
            } catch (IOException x) {
                x.printStackTrace();
            }
        }
        return s;
    }
    
    public void write_byte(byte v) throws IOException {
        file.write(v);
    }
    
    public void write_bytes(byte[] v) throws IOException {
        file.write(v);
    }
    
    public void write_half(int v) throws IOException {
        if (isLittleEndian()) {
            file.write((byte)v);
            file.write((byte)(v>>8));
        } else {
            file.write((byte)(v>>8));
            file.write((byte)v);
        }
    }
    
    public void write_word(int v) throws IOException {
        if (isLittleEndian()) {
            file.write((byte)v);
            file.write((byte)(v>>8));
            file.write((byte)(v>>16));
            file.write((byte)(v>>24));
        } else {
            file.write((byte)(v>>24));
            file.write((byte)(v>>16));
            file.write((byte)(v>>8));
            file.write((byte)v);
        }
    }
    
    public void write_sword(int v) throws IOException {
        write_word(v);
    }
    
    public void write_off(int v) throws IOException {
        write_word(v);
    }
    
    public void write_addr(int v) throws IOException {
        write_word(v);
    }
    
    public void write_sectionname(String s) throws IOException {
        int value;
        if (section_header_string_table == null)
            value = 0;
        else
            value = section_header_string_table.getStringIndex(s);
        write_word(value);
    }
    
    public void set_position(int offset) throws IOException {
        file.seek(offset);
    }
    
    public byte read_byte() throws IOException {
        return file.readByte();
    }
    
    public void read_bytes(byte[] b) throws IOException {
        file.readFully(b);
    }
    
    public int read_half() throws IOException {
        int b1 = file.readByte() & 0xFF;
        int b2 = file.readByte() & 0xFF;
        int r;
        if (isLittleEndian()) {
            r = (b2 << 8) | b1;
        } else {
            r = (b1 << 8) | b2;
        }
        return r;
    }
    
    public int read_word() throws IOException {
        int b1 = file.readByte() & 0xFF;
        int b2 = file.readByte() & 0xFF;
        int b3 = file.readByte() & 0xFF;
        int b4 = file.readByte() & 0xFF;
        int r;
        if (isLittleEndian()) {
            r = (b4 << 24) | (b3 << 16) | (b2 << 8) | b1;
        } else {
            r = (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
        }
        return r;
    }
    
    public int read_sword() throws IOException {
        return read_word();
    }
    
    public int read_off() throws IOException {
        return read_word();
    }
    
    public int read_addr() throws IOException {
        return read_word();
    }
}
