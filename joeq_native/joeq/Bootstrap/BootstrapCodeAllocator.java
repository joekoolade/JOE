// BootstrapCodeAllocator.java, created Tue Feb 27  3:00:22 2001 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Bootstrap;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;
import joeq.Allocator.CodeAllocator;
import joeq.Class.jq_BytecodeMap;
import joeq.Class.jq_CompiledCode;
import joeq.Class.jq_Method;
import joeq.Class.jq_TryCatch;
import joeq.Memory.Address;
import joeq.Memory.CodeAddress;
import joeq.Runtime.ExceptionDeliverer;
import jwutil.util.Assert;
import jwutil.util.Convert;

/**
 * BootstrapCodeAllocator
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: BootstrapCodeAllocator.java,v 1.17 2004/09/30 03:35:29 joewhaley Exp $
 */
public class BootstrapCodeAllocator extends CodeAllocator {

    public static final BootstrapCodeAllocator DEFAULT = new BootstrapCodeAllocator();
    
    /** Creates new BootstrapCodeAllocator */
    public BootstrapCodeAllocator() {}

    private static final int bundle_shift = 12;
    private static final int bundle_size = 1 << bundle_shift;
    private static final int bundle_mask = bundle_size-1;
    private Vector bundles;
    private byte[] current_bundle;
    private int bundle_idx;
    private int idx;
    
    private List all_code_relocs, all_data_relocs;
    
    public void init() {
        if (TRACE) System.out.println("Initializing "+this);
        bundles = new Vector();
        bundles.addElement(current_bundle = new byte[bundle_size]);
        bundle_idx = 0;
        idx = -1;
        all_code_relocs = new LinkedList();
        all_data_relocs = new LinkedList();
    }
    
    /** Allocate a code buffer of the given estimated size, such that the given
     * offset will have the given alignment.
     * In this allocator, the memory is allocated in chunks, so exceeding the
     * estimated size doesn't cost extra.
     *
     * @param estimatedSize  estimated size, in bytes, of desired code buffer
     * @param offset  desired offset to align to
     * @param alignment  desired alignment, or 0 if don't care
     * @return  the new code buffer
     */
    public x86CodeBuffer getCodeBuffer(int estimatedSize,
                                       int offset,
                                       int alignment) {
        // align pointer first
        int entrypoint = size() + offset;
        if (alignment > 1) {
            entrypoint += alignment-1;
            entrypoint &= ~(alignment-1);
        }
        idx += (entrypoint-offset) - size();
        return new Bootstrapx86CodeBuffer(entrypoint-offset, estimatedSize);
    }
    
    public int size() { return bundle_size*bundle_idx+idx+1; }
    public void ensureCapacity(int size) {
        int i = size >> bundle_shift;
        while (i+1 >= bundles.size()) {
            bundles.addElement(new byte[bundle_size]);
        }
    }

    public List getAllCodeRelocs() { return all_code_relocs; }
    public List getAllDataRelocs() { return all_data_relocs; }

    public void dump(ByteBuffer out) {
        for (int i=0; i<bundle_idx; ++i) {
            byte[] bundle = (byte[]) bundles.elementAt(i);
            out.put(bundle);
        }
        out.put(current_bundle, 0, idx+1);
    }
    
    public void dump(DataOutput out)
    throws IOException {
        for (int i=0; i<bundle_idx; ++i) {
            byte[] bundle = (byte[]) bundles.elementAt(i);
            out.write(bundle);
        }
        out.write(current_bundle, 0, idx+1);
    }
    
    public void patchAbsolute(Address code, Address heap) {
        poke((CodeAddress) code, heap);
    }
    
    public void patchRelativeOffset(CodeAddress code, CodeAddress target) {
        poke4(code, target.difference(code)-4);
    }
    
    public void poke(CodeAddress k, Address v) {
        poke4(k, v.to32BitValue());
    }
    public void poke1(CodeAddress k, byte v) {
        int a = k.to32BitValue();
        int i = a >> bundle_shift;
        int j = a & bundle_mask;
        byte[] b = (byte[])bundles.elementAt(i);
        b[j] = v;
    }
    public void poke2(CodeAddress k, short v) {
        poke1(k, (byte)(v));
        poke1((CodeAddress) k.offset(1), (byte)(v>>8));
    }
    public void poke4(CodeAddress k, int v) {
        poke2(k, (short)(v));
        poke2((CodeAddress) k.offset(2), (short)(v>>16));
    }
    public void poke8(CodeAddress k, long v) {
        poke4(k, (int)(v));
        poke4((CodeAddress) k.offset(4), (int)(v>>32));
    }
    
    public Address peek(CodeAddress k) {
        return new BootstrapCodeAddress(peek4(k));
    }
    public byte peek1(CodeAddress k) {
        int a = k.to32BitValue();
        int i = a >> bundle_shift;
        int j = a & bundle_mask;
        byte[] b = (byte[])bundles.elementAt(i);
        return b[j];
    }
    public short peek2(CodeAddress k) {
        return Convert.twoBytesToShort(peek1(k), peek1((CodeAddress) k.offset(1)));
    }
    public int peek4(CodeAddress k) {
        return Convert.fourBytesToInt(peek1(k), peek1((CodeAddress) k.offset(1)), peek1((CodeAddress) k.offset(2)), peek1((CodeAddress) k.offset(3)));
    }
    public long peek8(CodeAddress k) {
        return Convert.twoIntsToLong(peek4(k), peek4((CodeAddress) k.offset(4)));
    }
    
    public class Bootstrapx86CodeBuffer extends CodeAllocator.x86CodeBuffer {

        private int startIndex;
        private int entryIndex;
        
        Bootstrapx86CodeBuffer(int startIndex, int estSize) {
            this.startIndex = startIndex;
            ensureCapacity(startIndex+estSize);
        }
        
        public int getStartIndex() { return startIndex; }
        public int getEntryIndex() { return entryIndex; }
        
        public int getCurrentOffset() { return size()-startIndex; }
        public CodeAddress getStartAddress() { return new BootstrapCodeAddress(getStartIndex()); }
        public CodeAddress getCurrentAddress() { return new BootstrapCodeAddress(size()); }
        
        public void setEntrypoint() { entryIndex = size(); }

        public void checkSize() {
            if (idx >= bundle_size-1) {
                if (bundle_idx < bundles.size()-1) {
                    if (TRACE) System.out.println("getting next bundle idx "+(bundle_idx+1));
                    current_bundle = (byte[])bundles.get(++bundle_idx);
                    idx -= bundle_size;
                } else {
                    if (TRACE) System.out.println("allocing new bundle idx "+(bundle_idx+1));
                    bundles.addElement(current_bundle = new byte[bundle_size]);
                    ++bundle_idx; idx -= bundle_size;
                }
            }
        }

        public void add1(byte i) {
            checkSize(); current_bundle[++idx] = i;
        }
        public void add2_endian(int i) {
            checkSize(); current_bundle[++idx] = (byte)(i);
            checkSize(); current_bundle[++idx] = (byte)(i >> 8);
        }
        public void add2(int i) {
            checkSize(); current_bundle[++idx] = (byte)(i >> 8);
            checkSize(); current_bundle[++idx] = (byte)(i);
        }
        public void add3(int i) {
            checkSize(); current_bundle[++idx] = (byte)(i >> 16);
            checkSize(); current_bundle[++idx] = (byte)(i >> 8);
            checkSize(); current_bundle[++idx] = (byte)(i);
        }
        public void add4_endian(int i) {
            checkSize(); current_bundle[++idx] = (byte)(i);
            checkSize(); current_bundle[++idx] = (byte)(i >> 8);
            checkSize(); current_bundle[++idx] = (byte)(i >> 16);
            checkSize(); current_bundle[++idx] = (byte)(i >> 24);
        }

        public byte get1(int k) {
            return peek1(new BootstrapCodeAddress(k+startIndex));
        }

        public int get4_endian(int k) {
            return peek4(new BootstrapCodeAddress(k+startIndex));
        }

        public void put1(int k, byte instr) {
            poke1(new BootstrapCodeAddress(k+startIndex), instr);
        }

        public void put4_endian(int k, int instr) {
            poke4(new BootstrapCodeAddress(k+startIndex), instr);
        }

        public void skip(int nbytes) {
            Assert._assert(nbytes < bundle_size);
            idx += nbytes;
            //checkSize();
        }

        public jq_CompiledCode allocateCodeBlock(jq_Method m, jq_TryCatch[] ex, jq_BytecodeMap bcm,
                                                 ExceptionDeliverer exd, int stackframesize,
                                                 List code_relocs, List data_relocs) {
            int total = getCurrentOffset();
            int start = getStartIndex();
            int entry = getEntryIndex();
            if (code_relocs != null)
                all_code_relocs.addAll(code_relocs);
            if (data_relocs != null)
                all_data_relocs.addAll(data_relocs);
            jq_CompiledCode cc = new jq_CompiledCode(m, new BootstrapCodeAddress(start),
                                                     total, new BootstrapCodeAddress(entry),
                                                     ex, bcm, exd, stackframesize,
                                                     code_relocs, data_relocs);
            CodeAllocator.registerCode(cc);
            return cc;
        }
    
    }
    
}
