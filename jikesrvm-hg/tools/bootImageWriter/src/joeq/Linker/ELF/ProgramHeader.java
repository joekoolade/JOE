// ProgramHeader.java, created Wed Mar  6 18:38:47 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Linker.ELF;

import java.io.IOException;

/**
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: ProgramHeader.java,v 1.7 2004/03/09 06:26:56 jwhaley Exp $
 */
public abstract class ProgramHeader implements ELFConstants {

    protected int offset;
    protected int vaddr;
    protected int paddr;
    protected int filesz;
    protected int memsz;
    protected int flags;
    protected int align;
    protected byte[] data;
    
    public abstract int getType();
    public int getOffset() { return offset; }
    public int getVAddr() { return vaddr; }
    public int getPAddr() { return paddr; }
    public int getFileSz() { return filesz; }
    public int getMemSz() { return memsz; }
    public int getFlags() { return flags; }
    public int getAlign() { return align; }

    public void writeHeader(ELFImpl file, int offset) throws IOException {
    	System.out.println("ph writeheader: "+offset);
        file.write_word(this.getType());
        file.write_off(offset);
        file.write_addr(this.getVAddr());
        file.write_addr(this.getPAddr());
        file.write_word(this.getFileSz());
        file.write_word(this.getMemSz());
        file.write_word(this.getFlags());
        file.write_word(this.getAlign());
    }

    public static class NullProgramHeader extends ProgramHeader {
        protected String name;
        protected byte[] desc;
        protected int type;
        public final int getType() { return PT_NULL; }
    }
    public static class LoadProgramHeader extends ProgramHeader {
    	public LoadProgramHeader(int flags, int addr, int addralign, byte[] data, int length)
    	{
    		vaddr = addr;
    		this.flags = flags;
    		paddr = 0;
    		vaddr = addr;
    		align = addralign;
    		this.data = data;
    		filesz = length;
    	}
        public final int getType() { return PT_LOAD; }
        public void writeData(ELF file) throws IOException {
        	System.out.println("LPH writeData: "+filesz);
            file.write_bytes(data, offset, filesz);
        }
    }
    public static class DynamicProgramHeader extends ProgramHeader {
        public final int getType() { return PT_DYNAMIC; }
    }
    public static class InterpProgramHeader extends ProgramHeader {
        public final int getType() { return PT_INTERP; }
    }
    public static class NoteProgramHeader extends ProgramHeader {
        public final int getType() { return PT_NOTE; }
    }
    public static class PHdrProgramHeader extends ProgramHeader {
        public final int getType() { return PT_PHDR; }
    }

    public static int getSize() { return 32; }
	public void writeData(ELF file)  throws IOException{
    	System.out.println("ph writeData");
	}
}
