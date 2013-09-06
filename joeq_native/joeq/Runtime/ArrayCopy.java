// ArrayCopy.java, created Mon Feb  5 23:23:21 2001 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Runtime;

/*
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: ArrayCopy.java,v 1.7 2004/03/09 22:36:58 jwhaley Exp $
 */
public abstract class ArrayCopy {

    public static void arraycopy(Object src, int src_position,
                                 Object dst, int dst_position,
                                 int length) {
        if (dst instanceof char[]) {
            if (src instanceof char[]) {
                ArrayCopy.arraycopy((char[])src, src_position, (char[])dst, dst_position, length);
                return;
            }
        } else if (dst instanceof Object[]) {
            if (src instanceof Object[]) {
                ArrayCopy.arraycopy((Object[])src, src_position, (Object[])dst, dst_position, length);
                return;
            }
        } else if (dst instanceof byte[]) {
            if (src instanceof byte[]) {
                ArrayCopy.arraycopy((byte[])src, src_position, (byte[])dst, dst_position, length);
                return;
            }
        } else if (dst instanceof short[]) {
            if (src instanceof short[]) {
                ArrayCopy.arraycopy((short[])src, src_position, (short[])dst, dst_position, length);
                return;
            }
        } else if (dst instanceof int[]) {
            if (src instanceof int[]) {
                ArrayCopy.arraycopy((int[])src, src_position, (int[])dst, dst_position, length);
                return;
            }
        } else if (dst instanceof float[]) {
            if (src instanceof float[]) {
                ArrayCopy.arraycopy((float[])src, src_position, (float[])dst, dst_position, length);
                return;
            }
        } else if (dst instanceof long[]) {
            if (src instanceof long[]) {
                ArrayCopy.arraycopy((long[])src, src_position, (long[])dst, dst_position, length);
                return;
            }
        } else if (dst instanceof double[]) {
            if (src instanceof double[]) {
                ArrayCopy.arraycopy((double[])src, src_position, (double[])dst, dst_position, length);
                return;
            }
        }
        throw new ArrayStoreException("destination array type "+dst.getClass()+" does not match source array type "+src.getClass());
    }
    
    public static void arraycopy(Object[] src, int src_position,
                                 Object[] dst, int dst_position,
                                 int length) {
        if ((src_position < 0) || (dst_position < 0) || (length < 0) ||
            (src_position+length > src.length) || (dst_position+length > dst.length))
            throw new ArrayIndexOutOfBoundsException();
        if ((src == dst) && (src_position < dst_position) && (src_position+length > dst_position)) {
            // overlapping case
            for (int i=length-1; i>=0; --i) {
                dst[dst_position+i] = src[src_position+i];
            }
            return;
        }
        for (int i=0; i<length; ++i) {
            dst[dst_position+i] = src[src_position+i];
        }
    }
    public static void arraycopy(byte[] src, int src_position,
                                 byte[] dst, int dst_position,
                                 int length) {
        if ((src_position < 0) || (dst_position < 0) || (length < 0) ||
            (src_position+length > src.length) || (dst_position+length > dst.length))
            throw new ArrayIndexOutOfBoundsException();
        if ((src == dst) && (src_position < dst_position) && (src_position+length > dst_position)) {
            // overlapping case
            for (int i=length-1; i>=0; --i) {
                dst[dst_position+i] = src[src_position+i];
            }
            return;
        }
        for (int i=0; i<length; ++i) {
            dst[dst_position+i] = src[src_position+i];
        }
    }
    public static void arraycopy(char[] src, int src_position,
                                 char[] dst, int dst_position,
                                 int length) {
        if ((src_position < 0) || (dst_position < 0) || (length < 0) ||
            (src_position+length > src.length) || (dst_position+length > dst.length))
            throw new ArrayIndexOutOfBoundsException();
        if ((src == dst) && (src_position < dst_position) && (src_position+length > dst_position)) {
            // overlapping case
            for (int i=length-1; i>=0; --i) {
                dst[dst_position+i] = src[src_position+i];
            }
            return;
        }
        for (int i=0; i<length; ++i) {
            dst[dst_position+i] = src[src_position+i];
        }
    }
    public static void arraycopy(short[] src, int src_position,
                                 short[] dst, int dst_position,
                                 int length) {
        if ((src_position < 0) || (dst_position < 0) || (length < 0) ||
            (src_position+length > src.length) || (dst_position+length > dst.length))
            throw new ArrayIndexOutOfBoundsException();
        if ((src == dst) && (src_position < dst_position) && (src_position+length > dst_position)) {
            // overlapping case
            for (int i=length-1; i>=0; --i) {
                dst[dst_position+i] = src[src_position+i];
            }
            return;
        }
        for (int i=0; i<length; ++i) {
            dst[dst_position+i] = src[src_position+i];
        }
    }
    public static void arraycopy(int[] src, int src_position,
                                 int[] dst, int dst_position,
                                 int length) {
        if ((src_position < 0) || (dst_position < 0) || (length < 0) ||
            (src_position+length > src.length) || (dst_position+length > dst.length))
            throw new ArrayIndexOutOfBoundsException();
        if ((src == dst) && (src_position < dst_position) && (src_position+length > dst_position)) {
            // overlapping case
            for (int i=length-1; i>=0; --i) {
                dst[dst_position+i] = src[src_position+i];
            }
            return;
        }
        for (int i=0; i<length; ++i) {
            dst[dst_position+i] = src[src_position+i];
        }
    }
    public static void arraycopy(float[] src, int src_position,
                                 float[] dst, int dst_position,
                                 int length) {
        if ((src_position < 0) || (dst_position < 0) || (length < 0) ||
            (src_position+length > src.length) || (dst_position+length > dst.length))
            throw new ArrayIndexOutOfBoundsException();
        if ((src == dst) && (src_position < dst_position) && (src_position+length > dst_position)) {
            // overlapping case
            for (int i=length-1; i>=0; --i) {
                dst[dst_position+i] = src[src_position+i];
            }
            return;
        }
        for (int i=0; i<length; ++i) {
            dst[dst_position+i] = src[src_position+i];
        }
    }
    public static void arraycopy(long[] src, int src_position,
                                 long[] dst, int dst_position,
                                 int length) {
        if ((src_position < 0) || (dst_position < 0) || (length < 0) ||
            (src_position+length > src.length) || (dst_position+length > dst.length))
            throw new ArrayIndexOutOfBoundsException();
        if ((src == dst) && (src_position < dst_position) && (src_position+length > dst_position)) {
            // overlapping case
            for (int i=length-1; i>=0; --i) {
                dst[dst_position+i] = src[src_position+i];
            }
            return;
        }
        for (int i=0; i<length; ++i) {
            dst[dst_position+i] = src[src_position+i];
        }
    }
    public static void arraycopy(double[] src, int src_position,
                                 double[] dst, int dst_position,
                                 int length) {
        if ((src_position < 0) || (dst_position < 0) || (length < 0) ||
            (src_position+length > src.length) || (dst_position+length > dst.length))
            throw new ArrayIndexOutOfBoundsException();
        if ((src == dst) && (src_position < dst_position) && (src_position+length > dst_position)) {
            // overlapping case
            for (int i=length-1; i>=0; --i) {
                dst[dst_position+i] = src[src_position+i];
            }
            return;
        }
        for (int i=0; i<length; ++i) {
            dst[dst_position+i] = src[src_position+i];
        }
    }
    
}
