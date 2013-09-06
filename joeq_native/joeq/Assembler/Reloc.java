// Reloc.java, created Tue Feb 27  2:59:43 2001 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Assembler;

import java.io.DataOutput;
import java.io.IOException;

/**
 * Reloc
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: Reloc.java,v 1.1 2004/03/10 22:35:52 jwhaley Exp $
 */
public abstract class Reloc {

    public static final char RELOC_ADDR32 = (char)0x0006;
    public static final char RELOC_REL32  = (char)0x0014;
    
    public abstract void dumpCOFF(DataOutput out) throws IOException;
    public abstract void patch();
}
