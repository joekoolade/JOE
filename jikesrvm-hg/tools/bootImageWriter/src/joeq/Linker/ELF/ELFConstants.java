// ELFConstants.java, created Sat May 25 12:46:16 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Linker.ELF;

/**
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: ELFConstants.java,v 1.5 2004/03/09 06:26:55 jwhaley Exp $
 */
public interface ELFConstants {

    byte ELFMAG0    = (byte)0x7f;
    byte ELFMAG1    = (byte)'E';
    byte ELFMAG2    = (byte)'L';
    byte ELFMAG3    = (byte)'F';
    
    // ei_class
    byte ELFCLASSNONE   = (byte)0;
    byte ELFCLASS32     = (byte)1;
    byte ELFCLASS64     = (byte)2;
    
    // ei_data
    byte ELFDATANONE   = (byte)0;
    byte ELFDATA2LSB   = (byte)1;
    byte ELFDATA2MSB   = (byte)2;
    
    // e_type
    int ET_NONE     = 0;
    int ET_REL      = 1;
    int ET_EXEC     = 2;
    int ET_DYN      = 3;
    int ET_CORE     = 4;
    int ET_LOPROC   = 0xff00;
    int ET_HIPROC   = 0xffff;

    // e_machine
    int EM_M32      = 1;
    int EM_SPARC    = 2;
    int EM_386      = 3;
    int EM_68K      = 4;
    int EM_88K      = 5;
    int EM_860      = 7;
    int EM_MIPS     = 8;
    int EM_MIPS_RS4_BE = 10;
    int EM_ARM		= 40; // ARM 32bit architecture
    int EM_IA_64	= 50; // Intel IA-64 processor arch
    int EM_AARCH64	= 183; // ARM 64bit arch
    
    // e_version
    int EV_NONE       = (byte)0;
    int EV_CURRENT    = (byte)1;
    
    // Segment Types
    int PT_NULL     = 0;
    int PT_LOAD     = 1;
    int PT_DYNAMIC  = 2;
    int PT_INTERP   = 3;
    int PT_NOTE     = 4;
    int PT_SHLIB    = 5;
    int PT_PHDR     = 6;
    int PT_LOPROC   = 0x70000000;
    int PT_HIPROC   = 0x7fffffff;

    // Reloc Types
    byte R_386_NONE = 0;
    byte R_386_32   = 1;
    byte R_386_PC32 = 2;
    
    // Special Section Indexes
    int SHN_UNDEF       = 0;
    int SHN_LORESERVE   = 0xff00;
    int SHN_LOPROC      = 0xff00;
    int SHN_HIPROC      = 0xff1f;
    int SHN_ABS         = 0xfff1;
    int SHN_COMMON      = 0xfff2;
    int SHN_HIRESERVE   = 0xffff;
    int SHN_INVALID     = -1;
    
    // Section Types.
    int SHT_NULL        = 0;
    int SHT_PROGBITS    = 1;
    int SHT_SYMTAB      = 2;
    int SHT_STRTAB      = 3;
    int SHT_RELA        = 4;
    int SHT_HASH        = 5;
    int SHT_DYNAMIC     = 6;
    int SHT_NOTE        = 7;
    int SHT_NOBITS      = 8;
    int SHT_REL         = 9;
    int SHT_SHLIB       = 10;
    int SHT_DYNSYM      = 11;
    int SHT_LOPROC      = 0x70000000;
    int SHT_HIPROC      = 0x7fffffff;
    int SHT_LOUSER      = 0x80000000;
    int SHT_HIUSER      = 0xffffffff;
    
    // Section Attribute Flags
    int SHF_WRITE       = 0x1;
    int SHF_ALLOC       = 0x2;
    int SHF_EXECINSTR   = 0x4;
    int SHF_MASKPROC    = 0xf0000000;
    
    // Symbol Binding
    byte STB_LOCAL   = 0;
    byte STB_GLOBAL  = 1;
    byte STB_WEAK    = 2;
    byte STB_LOPROC  = 13;
    byte STB_HIPROC  = 15;
    
    // Symbol Types
    byte STT_NOTYPE  = 0;
    byte STT_OBJECT  = 1;
    byte STT_FUNC    = 2;
    byte STT_SECTION = 3;
    byte STT_FILE    = 4;
    byte STT_LOPROC  = 13;
    byte STT_HIPROC  = 15;
    
    // Segment flags
    int PF_X		= 0x1;
    int PF_W		= 0x2;
    int PF_R		= 0x4;
}
