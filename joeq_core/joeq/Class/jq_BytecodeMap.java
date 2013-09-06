// jq_BytecodeMap.java, created Mon Feb  5 23:23:20 2001 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Class;

import jwutil.util.Assert;

/**
 * This class implements a mapping from code offsets to bytecode indices.
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: jq_BytecodeMap.java,v 1.9 2004/09/22 22:17:28 joewhaley Exp $
 */
public class jq_BytecodeMap {

    /** Stores the code offsets. */
    private final int[] offset;
    /** Stores the bytecode indices. */
    private final int[] bytecode_index;
    
    /** Constructs a new bytecode map, using the given code offset and bytecode index array.
     *  The two arrays are co-indexed.  Each entry in the code offset array corresponds
     *  to an inclusive start offset of the instructions corresponding to the bytecode index
     *  in the co-indexed bytecode array.
     *  The length of the two arrays must be equal.
     *
     * @param offset  code offset array
     * @param bytecode_index  bytecode index array
     */
    public jq_BytecodeMap(int[] offset, int[] bytecode_index) {
        Assert._assert(offset.length == bytecode_index.length);
        this.offset = offset;
        this.bytecode_index = bytecode_index;
    }
    
    /** Returns the bytecode index corresponding to the given code offset, or -1 if the
     *  offset is out of range.
     * @param off  code offset to match
     * @return  bytecode index for the code offset, or -1
     */
    public int getBytecodeIndex(int off) {
        // todo: binary search
        for (int i=offset.length-1; i>=0; --i) {
            if (off > offset[i]) return bytecode_index[i];
        }
        return -1;
    }
}
