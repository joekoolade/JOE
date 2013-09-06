// Utf8.java, created Mon Feb  5 23:23:22 2001 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.UTF;

import java.io.DataOutput;
import java.io.IOException;
import joeq.Class.jq_ClassFileConstants;
import joeq.Runtime.Debug;
import jwutil.collections.UnmodifiableIterator;
import jwutil.util.Assert;

/**
 * TODO: ummm, synchronization?!
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: Utf8.java,v 1.23 2004/09/22 22:17:46 joewhaley Exp $
 */
public class Utf8 implements jq_ClassFileConstants {

    public static /*final*/ boolean TRACE = false;
    
    public static final int STARTING_TABLE_SIZE = 16384;
    public static final int STARTING_HASH_SIZE = 9999;
    public static final int STARTING_CHAIN_SIZE = 4;
    
    public static Utf8[] table = new Utf8[STARTING_TABLE_SIZE];
    public static int size = -1;
    public static int[][] chains = new int[STARTING_HASH_SIZE][]; // for hashing
    
    public static final Utf8 BYTE_DESC      = Utf8.get((char)TC_BYTE+"");
    public static final Utf8 CHAR_DESC      = Utf8.get((char)TC_CHAR+"");
    public static final Utf8 DOUBLE_DESC    = Utf8.get((char)TC_DOUBLE+"");
    public static final Utf8 FLOAT_DESC     = Utf8.get((char)TC_FLOAT+"");
    public static final Utf8 INT_DESC       = Utf8.get((char)TC_INT+"");
    public static final Utf8 LONG_DESC      = Utf8.get((char)TC_LONG+"");
    public static final Utf8 SHORT_DESC     = Utf8.get((char)TC_SHORT+"");
    public static final Utf8 BOOLEAN_DESC   = Utf8.get((char)TC_BOOLEAN+"");
    public static final Utf8 VOID_DESC      = Utf8.get((char)TC_VOID+"");

    public static Utf8 get(String s) {
        return get(toUtf8(s));
    }

    public static Utf8 get(byte[] b) {
        int id = getID(b);
        return table[id];
    }
    
    public static Utf8 get(byte[] b, int startIndex, int endIndex) {
        int id = getID(b, startIndex, endIndex);
        return table[id];
    }
    
    public static int getID(byte[] b) {
        int hash = hashCode(b);
        int chain_index = Math.abs(hash) % chains.length;
        int[] chain = chains[chain_index];
        if (chain == null) {
            chains[chain_index] = chain = new int[STARTING_CHAIN_SIZE];
            return addToTable_helper(b, hash, chain, 0);
        }
        for (int i=0; i<chain.length; ++i) {
            int id = chain[i]-1;
            if (id == -1) { // end of chain
                return addToTable_helper(b, hash, chain, i);
            }
            Utf8 utf8 = table[id];
            // ??? check hash before memcmp ???
            if (memcmp(utf8.data, b)) {
                // ??? swap first one and this one ???
                return id;
            }
        }
        int[] newchain = new int[chain.length<<1];
        System.arraycopy(chain, 0, newchain, 0, chain.length);
        chains[chain_index] = newchain;
        return addToTable_helper(b, hash, newchain, chain.length);
        // free chain
        
        // todo: rehash when the table gets too full...
    }
    
    public static int getID(byte[] b, int startIndex, int endIndex) {
        int hash = hashCode(b, startIndex, endIndex);
        int chain_index = Math.abs(hash) % chains.length;
        int[] chain = chains[chain_index];
        if (chain == null) {
            chains[chain_index] = chain = new int[STARTING_CHAIN_SIZE];
            byte[] b2 = new byte[endIndex-startIndex];
            System.arraycopy(b, startIndex, b2, 0, endIndex-startIndex);
            return addToTable_helper(b2, hash, chain, 0);
        }
        for (int i=0; i<chain.length; ++i) {
            int id = chain[i]-1;
            if (id == -1) { // end of chain
                byte[] b2 = new byte[endIndex-startIndex];
                System.arraycopy(b, startIndex, b2, 0, endIndex-startIndex);
                return addToTable_helper(b2, hash, chain, i);
            }
            Utf8 utf8 = table[id];
            // ??? check hash before memcmp ???
            if (memcmp(utf8.data, b, startIndex, endIndex)) {
                // ??? swap first one and this one ???
                return id;
            }
        }
        int[] newchain = new int[chain.length<<1];
        System.arraycopy(chain, 0, newchain, 0, chain.length);
        chains[chain_index] = newchain;
        byte[] b2 = new byte[endIndex-startIndex];
        System.arraycopy(b, startIndex, b2, 0, endIndex-startIndex);
        return addToTable_helper(b2, hash, newchain, chain.length);
        // free(chain)
        
        // todo: rehash when the table gets too full...
    }
    
    public boolean isValidMethodDescriptor() {
        if (data.length < 3)
            return false;
        if (data[0] != TC_PARAM)
            return false;
        int i=1;
        while (data[i] != TC_PARAMEND) {
here:
            switch (data[i]) {
                case TC_BYTE:
                case TC_CHAR:
                case TC_DOUBLE:
                case TC_FLOAT:
                case TC_INT:
                case TC_LONG:
                case TC_SHORT:
                case TC_BOOLEAN:
                case TC_ARRAY:
                    break;
                case TC_CLASS:
                    for (;;) {
                        if (++i == data.length)
                            return false;
                        if (data[i] == TC_CLASSEND)
                            break here;
                    }
                default:
                    return false;
            }
            if (++i == data.length)
                return false;
        }
        ++i;
        return ((data[i] == TC_VOID) && (i == data.length-1)) ||
               isValidTypeDescriptor(i);
    }
    public boolean isValidTypeDescriptor() {
        return isValidTypeDescriptor(0);
    }
    private boolean isValidTypeDescriptor(int i) {
        for (;;) {
            if (data.length == i)
                return false;
            switch (data[i]) {
                case TC_BYTE:
                case TC_CHAR:
                case TC_DOUBLE:
                case TC_FLOAT:
                case TC_INT:
                case TC_LONG:
                case TC_SHORT:
                case TC_BOOLEAN:
                    return i == data.length-1;
                case TC_ARRAY:
                    ++i; continue;
                case TC_CLASS:
                    for (;;) {
                        if (++i == data.length)
                            return false;
                        if (data[i] == TC_CLASSEND)
                            return i == data.length-1;
                    }
            default:
                return false;
            }
        }
    }
    
    public boolean isDescriptor(byte desc) {
        return (data.length > 0) && (data[0] == desc);
    }

    public Utf8 getArrayElementDescriptor() {
        Assert._assert(isDescriptor(TC_ARRAY));
        return get(data, 1, data.length);
    }
    
    public Utf8 getClassName() {
        Assert._assert(isDescriptor(TC_CLASS));
        return get(data, 1, data.length-1);
    }
    
    public Utf8 getAsArrayDescriptor() {
        Assert._assert(isValidTypeDescriptor());
        // todo: might need to reevaluate making a new array on every query.
        byte[] b = new byte[data.length+1];
        b[0] = TC_ARRAY;
        System.arraycopy(data, 0, b, 1, data.length);
        return get(b);
    }
    
    public Utf8 getAsClassDescriptor() {
        Assert._assert(data[0] != TC_ARRAY);
        // todo: might need to reevaluate making a new array on every query.
        byte[] b = new byte[data.length+2];
        b[0] = TC_CLASS;
        System.arraycopy(data, 0, b, 1, data.length);
        b[data.length+1] = TC_CLASSEND;
        return get(b);
    }

    public MethodDescriptorIterator getParamDescriptors() {
        return new MethodDescriptorIterator();
    }
    
    public class MethodDescriptorIterator extends UnmodifiableIterator {
        int currentIndex;
        MethodDescriptorIterator() {
            Assert._assert(isDescriptor(TC_PARAM));
            currentIndex = 0;
        }
        public boolean hasNext() {
            return data[currentIndex+1] != TC_PARAMEND;
        }
        public Object next() { return nextUtf8(); }
        public Utf8 nextUtf8() {
            byte b = data[++currentIndex];
            int startIndex = currentIndex;
            while (b == TC_ARRAY) {
                b = data[++currentIndex];
            }
            if (b == TC_CLASS) {
                while (b != TC_CLASSEND) {
                    b = data[++currentIndex];
                }
            } else {
                if ((b != TC_BYTE) &&
                    (b != TC_CHAR) &&
                    (b != TC_DOUBLE) &&
                    (b != TC_FLOAT) &&
                    (b != TC_INT) &&
                    (b != TC_LONG) &&
                    (b != TC_SHORT) &&
                    (b != TC_BOOLEAN)) {
                    throw new ClassFormatError("bad method descriptor: "+fromUtf8(data)+" index: "+currentIndex);
                }
            }
            return get(data, startIndex, currentIndex+1);
        }
        public Utf8 getReturnDescriptor() {
            Assert._assert(!hasNext());
            return get(data, currentIndex+2, data.length);
        }
    }
    
    //// Implementation stuff below.
    
    // Helper function.
    private static boolean memcmp(byte[] b1, byte[] b2) {
        if (b1.length != b2.length) return false;
        for (int i=b1.length; --i>=0; ) {
            if (b1[i] != b2[i]) return false;
        }
        return true;
    }
    private static boolean memcmp(byte[] b1, byte[] b2, int startIndex, int endIndex) {
        if (b1.length != (endIndex-startIndex)) return false;
        for (int i=b1.length; --i>=0; ) {
            if (b1[i] != b2[i+startIndex]) return false;
        }
        return true;
    }
    
    public static boolean NO_NEW = false;

    // Helper function.
    private static int addToTable_helper(byte[] b, int hash, int[] chain, int index) {
        if (NO_NEW) {
            throw new IllegalStateException("Trying to add Utf8 "+fromUtf8(b));
        }
        if (++size == table.length) growTable_helper();
        if (!checkUtf8(b)) {
            fromUtf8(b); // fromUtf8 has more informative error messages.
            Assert.UNREACHABLE(); // fromUtf8 should have thrown an exception.
        }
        table[size] = new Utf8(b, hash);
        chain[index] = size+1;
        if (TRACE) System.out.println("allocated Utf8 ["+size+"] = \""+table[size]+"\"");
        return size;
    }
    
    // Helper function.
    private static void growTable_helper() {
        Utf8[] newtable = new Utf8[size<<1];
        System.arraycopy(table, 0, newtable, 0, size);
        if (TRACE) System.out.println("Growing Utf8 table from "+table.length+" to "+newtable.length);
        table = newtable;
    }
    
    /** Private constructor.  Use the get() method to create a Utf8 object. */
    private Utf8(byte[] data, int hash) {
        this.data = data; this.hash = hash;
        if (DEBUG) cache = fromUtf8(data);
    }
    private byte[] data;
    private int hash;
    
    public static int hashCode(byte[] data) {
        int h = 4999;
        int i=data.length;
        while(--i>=0) {
            h = 2999*h + data[i];
        }
        return h;
    }
    
    public static int hashCode(byte[] data, int startIndex, int endIndex) {
        int h = 4999;
        int i=endIndex;
        while(--i>=startIndex) {
            h = 2999*h + data[i];
        }
        return h;
    }
    
    public int hashCode() {
        return hash;
    }
    
    public static final boolean USE_CACHE = true;
    public static final boolean DEBUG = true;
    private String cache;
    public String toString() {
        if (USE_CACHE) {
            if (cache != null) return cache;
            return cache = fromUtf8(data);
        } else {
            return fromUtf8(data);
        }
    }

    public void dump(DataOutput out) throws IOException {
        Assert._assert(data.length <= Character.MAX_VALUE);
        out.writeChar(data.length);
        out.write(data);
    }
    
    public void debugWrite() {
        Debug.write(data, data.length);
    }
    
    //// Utf8 conversion routines
    
    /**
     * Strictly check the format of the utf8/pseudo-utf8 byte array in
     * fromUtf8.
     */
    static final boolean STRICTLY_CHECK_FORMAT = false;
    /**
     * Set fromUtf8 to not throw an exception when given a normal utf8
     * byte array.
     */
    static final boolean ALLOW_NORMAL_UTF8 = false;
    /**
     * Set fromUtf8 to not throw an exception when given a pseudo utf8
     * byte array.
     */
    static final boolean ALLOW_PSEUDO_UTF8 = true;
    /**
     * Set toUtf8 to write in pseudo-utf8 (rather than normal utf8).
     */
    static final boolean WRITE_PSEUDO_UTF8 = true;

    /**
     * Convert the given sequence of (pseudo-)utf8 formatted bytes
     * into a String.
     *
     * The acceptable input formats are controlled by the
     * STRICTLY_CHECK_FORMAT, ALLOW_NORMAL_UTF8, and ALLOW_PSEUDO_UTF8
     * flags.
     *
     * @param utf8 (pseudo-)utf8 byte array
     * @throws UTFDataFormatError if the (pseudo-)utf8 byte array is not valid (pseudo-)utf8
     * @return unicode string
     */
    public static String fromUtf8(byte[] utf8)
    throws UTFDataFormatError {
        char[] result = new char[utf8.length];
        int result_index = 0;
        for (int i=0, n=utf8.length; i<n; ) {
            byte b = utf8[i++];
            if (STRICTLY_CHECK_FORMAT && !ALLOW_NORMAL_UTF8)
                if (b == 0)
                    throw new UTFDataFormatError("0 byte encountered at location "+(i-1));
            if (b >= 0) {  // < 0x80 unsigned
                // in the range '\001' to '\177'
                result[result_index++] = (char)b;
                continue;
            }
            try {
                byte nb = utf8[i++];
                if (b < -32) {  // < 0xe0 unsigned
                    // '\000' or in the range '\200' to '\u07FF'
                    char c = result[result_index++] =
                        (char)(((b & 0x1f) << 6) | (nb & 0x3f));
                    if (STRICTLY_CHECK_FORMAT) {
                        if (((b & 0xe0) != 0xc0) ||
                            ((nb & 0xc0) != 0x80))
                            throw new UTFDataFormatError("invalid marker bits for double byte char at location "+(i-2));
                        if (c < '\200') {
                            if (!ALLOW_PSEUDO_UTF8 || (c != '\000'))
                                throw new UTFDataFormatError("encountered double byte char that should have been single byte at location "+(i-2));
                        } else if (c > '\u07FF')
                            throw new UTFDataFormatError("encountered double byte char that should have been triple byte at location "+(i-2));
                    }
                } else {
                    byte nnb = utf8[i++];
                    // in the range '\u0800' to '\uFFFF'
                    char c = result[result_index++] =
                        (char)(((b & 0x0f) << 12) |
                               ((nb & 0x3f) << 6) |
                               (nnb & 0x3f));
                    if (STRICTLY_CHECK_FORMAT) {
                        if (((b & 0xf0) != 0xe0) ||
                            ((nb & 0xc0) != 0x80) ||
                            ((nnb & 0xc0) != 0x80))
                            throw new UTFDataFormatError("invalid marker bits for triple byte char at location "+(i-3));
                        if (c < '\u0800')
                            throw new UTFDataFormatError("encountered triple byte char that should have been fewer bytes at location "+(i-3));
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new UTFDataFormatError("unexpected end at location "+i);
            }
        }
        return new String(result, 0, result_index);
    }

    /**
     * Convert the given String into a sequence of (pseudo-)utf8
     * formatted bytes.
     *
     * The output format is controlled by the WRITE_PSEUDO_UTF8 flag.
     *
     * @param s String to convert
     * @return array containing sequence of (pseudo-)utf8 formatted bytes
     */
    public static byte[] toUtf8(String s) {
        byte[] result = new byte[lengthUtf8(s)];
        int result_index = 0;
        for (int i = 0, n = s.length(); i < n; ++i) {
            char c = (char)s.charAt(i);
            // in all shifts below, c is an (unsigned) char,
            // so either >>> or >> is ok
            if (((!WRITE_PSEUDO_UTF8) || (c >= 0x0001)) && (c <= 0x007F))
                result[result_index++] = (byte)c;
            else if (c > 0x07FF) {
                result[result_index++] = (byte)(0xe0 | (byte)(c >> 12));
                result[result_index++] = (byte)(0x80 | ((c & 0xfc0) >> 6));
                result[result_index++] = (byte)(0x80 | (c & 0x3f));
            } else {
                result[result_index++] = (byte)(0xc0 | (byte)(c >> 6));
                result[result_index++] = (byte)(0x80 | (c & 0x3f));
            }
        }
        return result;
    }

    /**
     * Returns the length of a string's utf8 encoded form.
     */
    public static int lengthUtf8(String s) {
        int utflen = 0;
        for (int i = 0, n = s.length(); i < n; ++i) {
            int c = s.charAt(i);
            if (((!WRITE_PSEUDO_UTF8) || (c >= 0x0001)) && (c <= 0x007F))
                ++utflen;
            else if (c > 0x07FF)
                utflen += 3;
            else
                utflen += 2;
        }
        return utflen;
    }

    /**
     * Check whether the given sequence of bytes is valid (pseudo-)utf8.
     *
     * @param bytes byte array to check
     * @return true iff the given sequence is valid (pseudo-)utf8.
     */
    public static boolean checkUtf8(byte[] bytes) {
        for (int i=0, n=bytes.length; i<n; ) {
            byte b = bytes[i++];
            if (STRICTLY_CHECK_FORMAT && !ALLOW_NORMAL_UTF8)
                if (b == 0) return false;
            if (b >= 0) {  // < 0x80 unsigned
                // in the range '\001' to '\177'
                continue;
            }
            try {
                byte nb = bytes[i++];
                if (b < -32) {  // < 0xe0 unsigned
                    // '\000' or in the range '\200' to '\u07FF'
                    char c = (char)(((b & 0x1f) << 6) | (nb & 0x3f));
                    if (STRICTLY_CHECK_FORMAT) {
                        if (((b & 0xe0) != 0xc0) ||
                            ((nb & 0xc0) != 0x80))
                            return false;
                        if (c < '\200') {
                            if (!ALLOW_PSEUDO_UTF8 || (c != '\000'))
                                return false;
                            } else if (c > '\u07FF')
                                return false;
                    }
                } else {
                    byte nnb = bytes[i++];
                    // in the range '\u0800' to '\uFFFF'
                    char c = (char)(((b & 0x0f) << 12) |
                                    ((nb & 0x3f) << 6) |
                                    (nnb & 0x3f));
                    if (STRICTLY_CHECK_FORMAT) {
                        if (((b & 0xf0) != 0xe0) ||
                            ((nb & 0xc0) != 0x80) ||
                            ((nnb & 0xc0) != 0x80))
                            return false;
                        if (c < '\u0800')
                            return false;
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                return false;
            }
        }
        return true;
    }

}
