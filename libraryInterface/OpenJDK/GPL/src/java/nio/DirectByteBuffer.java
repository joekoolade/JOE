/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

// -- This file was mechanically generated: Do not edit! -- //

package java.nio;

import java.io.FileDescriptor;

import org.jikesrvm.VM;
import org.jikesrvm.runtime.Magic;
import org.jikesrvm.runtime.Memory;
import org.vmmagic.unboxed.Address;

import sun.misc.Cleaner;
import sun.nio.ch.DirectBuffer;


class DirectByteBuffer

    extends MappedByteBuffer



    implements DirectBuffer
{



    // Cached array base offset
    private static final long arrayBaseOffset = 0; //Magic.objectAsAddress(byte[].class).toLong();

    // Cached unaligned-access capability
    protected static final boolean unaligned = Bits.unaligned();

    // Base address, used in all indexing calculations
    // NOTE: moved up to Buffer.java for speed in JNI GetDirectBufferAddress
    //    protected long address;

    // An object attached to this buffer. If this buffer is a view of another
    // buffer then we use this field to keep a reference to that buffer to
    // ensure that its memory isn't freed before we are done with it.
    private final Object att;

    public Object attachment() {
        return att;
    }



    private static class Deallocator
        implements Runnable
    {

        private long address;
        private long size;
        private int capacity;

        private Deallocator(long address, long size, int capacity) {
            assert (address != 0);
            this.address = address;
            this.size = size;
            this.capacity = capacity;
        }

        public void run() {
            if (address == 0) {
                // Paranoia
                return;
            }
//            unsafe.freeMemory(address);
            address = 0;
//            Bits.unreserveMemory(size, capacity);
        }

    }

    private final Cleaner cleaner;

    public Cleaner cleaner() { return cleaner; }











    // Primary constructor
    //
    DirectByteBuffer(int cap) {                   // package-private

        super(-1, 0, cap, cap);
        boolean pa = false; //VM.isDirectMemoryPageAligned();
        int ps = Bits.pageSize();
        long size = Math.max(1L, (long)cap + (pa ? ps : 0));
//        Bits.reserveMemory(size, cap);
//
//        long base = 0;
//        try {
//            base = 0; // unsafe.allocateMemory(size);
//        } catch (OutOfMemoryError x) {
//            Bits.unreserveMemory(size, cap);
//            throw x;
//        }
//        unsafe.setMemory(base, size, (byte) 0);
//        if (pa && (base % ps != 0)) {
//            // Round up to page boundary
//            address = base + ps - (base & (ps - 1));
//        } else {
//            address = base;
//        }
        address = Magic.objectAsAddress(new Byte[(int)size]).toLong();
        cleaner = null;
        att = null;
    }



    // Invoked to construct a direct ByteBuffer referring to the block of
    // memory. A given arbitrary object may also be attached to the buffer.
    //
    DirectByteBuffer(long addr, int cap, Object ob) {
        super(-1, 0, cap, cap);
        address = addr;
        cleaner = null;
        att = ob;
    }


    // Invoked only by JNI: NewDirectByteBuffer(void*, long)
    //
    DirectByteBuffer(long addr, int cap) {
        super(-1, 0, cap, cap);
        address = addr;
        cleaner = null;
        att = null;
    }



    // For memory-mapped buffers -- invoked by FileChannelImpl via reflection
    //
    protected DirectByteBuffer(int cap, long addr,
                                     FileDescriptor fd,
                                     Runnable unmapper)
    {

        super(-1, 0, cap, cap, fd);
        address = addr;
        cleaner = null; // Cleaner.create(this, unmapper);
        att = null;



    }



    // For duplicates and slices
    //
    DirectByteBuffer(DirectBuffer db,         // package-private
                               int mark, int pos, int lim, int cap,
                               int off)
    {

        super(mark, pos, lim, cap);
        address = db.address() + off;

        cleaner = null;

        att = db;



    }

    public ByteBuffer slice() {
        int pos = this.position();
        int lim = this.limit();
        assert (pos <= lim);
        int rem = (pos <= lim ? lim - pos : 0);
        int off = (pos << 0);
        assert (off >= 0);
        return new DirectByteBuffer(this, -1, 0, rem, rem, off);
    }

    public ByteBuffer duplicate() {
        return new DirectByteBuffer(this,
                                              this.markValue(),
                                              this.position(),
                                              this.limit(),
                                              this.capacity(),
                                              0);
    }

    public ByteBuffer asReadOnlyBuffer() {

        return new DirectByteBufferR(this,
                                           this.markValue(),
                                           this.position(),
                                           this.limit(),
                                           this.capacity(),
                                           0);



    }



    public long address() {
        return address;
    }

    private long ix(int i) {
        return address + (i << 0);
    }

    public byte get() {
        return Address.fromLong(ix(nextGetIndex())).loadByte();
    }

    public byte get(int i) {
        return Address.fromLong(ix(checkIndex(i))).loadByte();
    }







    public ByteBuffer get(byte[] dst, int offset, int length) {

        if ((length << 0) > Bits.JNI_COPY_TO_ARRAY_THRESHOLD) {
            checkBounds(offset, length, dst.length);
            int pos = position();
            int lim = limit();
            assert (pos <= lim);
            int rem = (pos <= lim ? lim - pos : 0);
            if (length > rem)
                throw new BufferUnderflowException();
            Bits.copyToArray(ix(pos), dst, arrayBaseOffset,
                             offset << 0,
                             length << 0);
            position(pos + length);
        } else {
            super.get(dst, offset, length);
        }
        return this;



    }



    public ByteBuffer put(byte x) {

        Address.fromLong(ix(nextPutIndex())).store(x);
        return this;



    }

    public ByteBuffer put(int i, byte x) {

        Address.fromLong(ix(checkIndex(i))).store(x);
        return this;



    }

    public ByteBuffer put(ByteBuffer src) {

        if (src instanceof DirectByteBuffer) {
            if (src == this)
                throw new IllegalArgumentException();
            DirectByteBuffer sb = (DirectByteBuffer)src;

            int spos = sb.position();
            int slim = sb.limit();
            assert (spos <= slim);
            int srem = (spos <= slim ? slim - spos : 0);

            int pos = position();
            int lim = limit();
            assert (pos <= lim);
            int rem = (pos <= lim ? lim - pos : 0);

            if (srem > rem)
                throw new BufferOverflowException();
            //Bits.copyMemory(sb.ix(spos), ix(pos), srem << 0);
            Memory.aligned8Copy(Address.fromLong(ix(pos)), Address.fromLong(sb.ix(spos)), srem << 0);
            
            sb.position(spos + srem);
            position(pos + srem);
        } else if (src.hb != null) {

            int spos = src.position();
            int slim = src.limit();
            assert (spos <= slim);
            int srem = (spos <= slim ? slim - spos : 0);

            put(src.hb, src.offset + spos, srem);
            src.position(spos + srem);

        } else {
            super.put(src);
        }
        return this;



    }

    public ByteBuffer put(byte[] src, int offset, int length) {

        if ((length << 0) > Bits.JNI_COPY_FROM_ARRAY_THRESHOLD) {
            checkBounds(offset, length, src.length);
            int pos = position();
            int lim = limit();
            assert (pos <= lim);
            int rem = (pos <= lim ? lim - pos : 0);
            if (length > rem)
                throw new BufferOverflowException();







                Bits.copyFromArray(src, arrayBaseOffset, offset << 0,
                                   ix(pos), length << 0);
            position(pos + length);
        } else {
            super.put(src, offset, length);
        }
        return this;



    }

    public ByteBuffer compact() {

        int pos = position();
        int lim = limit();
        assert (pos <= lim);
        int rem = (pos <= lim ? lim - pos : 0);

        //Bits.copyMemory(ix(pos), ix(0), rem << 0);
        Memory.aligned8Copy(Address.fromLong(ix(0)), Address.fromLong(ix(pos)), rem<<0);
        position(rem);
        limit(capacity());
        discardMark();
        return this;



    }

    public boolean isDirect() {
        return true;
    }

    public boolean isReadOnly() {
        return false;
    }
































































    byte _get(int i) {                          // package-private
        return Address.fromIntZeroExtend(i).loadByte();
    }

    void _put(int i, byte b) {                  // package-private

        Address.fromLong(address+i).store(b);


    }




    private char getChar(long a) {
        if (unaligned) {
            char x = Address.fromLong(a).loadChar();
            return (nativeByteOrder ? x : Bits.swap(x));
        }
        return Bits.getChar(a, bigEndian);
    }

    public char getChar() {
        return getChar(ix(nextGetIndex((1 << 1))));
    }

    public char getChar(int i) {
        return getChar(ix(checkIndex(i, (1 << 1))));
    }



    private ByteBuffer putChar(long a, char x) {

        if (unaligned) {
            char y = (x);
            Address.fromLong(a).store((nativeByteOrder ? y : Bits.swap(y)));
        } else {
            Bits.putChar(a, x, bigEndian);
        }
        return this;



    }

    public ByteBuffer putChar(char x) {

        putChar(ix(nextPutIndex((1 << 1))), x);
        return this;



    }

    public ByteBuffer putChar(int i, char x) {

        putChar(ix(checkIndex(i, (1 << 1))), x);
        return this;



    }

    public CharBuffer asCharBuffer() {
        int off = this.position();
        int lim = this.limit();
        assert (off <= lim);
        int rem = (off <= lim ? lim - off : 0);

        int size = rem >> 1;
        if (!unaligned && ((address + off) % (1 << 1) != 0)) {
            return (bigEndian
                    ? (CharBuffer)(new ByteBufferAsCharBufferB(this,
                                                                       -1,
                                                                       0,
                                                                       size,
                                                                       size,
                                                                       off))
                    : (CharBuffer)(new ByteBufferAsCharBufferL(this,
                                                                       -1,
                                                                       0,
                                                                       size,
                                                                       size,
                                                                       off)));
        } else {
            return (nativeByteOrder
                    ? (CharBuffer)(new DirectCharBufferU(this,
                                                                 -1,
                                                                 0,
                                                                 size,
                                                                 size,
                                                                 off))
                    : (CharBuffer)(new DirectCharBufferS(this,
                                                                 -1,
                                                                 0,
                                                                 size,
                                                                 size,
                                                                 off)));
        }
    }




    private short getShort(long a) {
        if (unaligned) {
            short x = Address.fromLong(a).loadShort();
            return (nativeByteOrder ? x : Bits.swap(x));
        }
        return Bits.getShort(a, bigEndian);
    }

    public short getShort() {
        return getShort(ix(nextGetIndex((1 << 1))));
    }

    public short getShort(int i) {
        return getShort(ix(checkIndex(i, (1 << 1))));
    }



    private ByteBuffer putShort(long a, short x) {

        if (unaligned) {
            short y = (x);
            Address.fromLong(a).store((nativeByteOrder ? y : Bits.swap(y)));
        } else {
            Bits.putShort(a, x, bigEndian);
        }
        return this;



    }

    public ByteBuffer putShort(short x) {

        putShort(ix(nextPutIndex((1 << 1))), x);
        return this;



    }

    public ByteBuffer putShort(int i, short x) {

        putShort(ix(checkIndex(i, (1 << 1))), x);
        return this;



    }

    public ShortBuffer asShortBuffer() {
        int off = this.position();
        int lim = this.limit();
        assert (off <= lim);
        int rem = (off <= lim ? lim - off : 0);

        int size = rem >> 1;
        if (!unaligned && ((address + off) % (1 << 1) != 0)) {
            return (bigEndian
                    ? (ShortBuffer)(new ByteBufferAsShortBufferB(this,
                                                                       -1,
                                                                       0,
                                                                       size,
                                                                       size,
                                                                       off))
                    : (ShortBuffer)(new ByteBufferAsShortBufferL(this,
                                                                       -1,
                                                                       0,
                                                                       size,
                                                                       size,
                                                                       off)));
        } else {
            if(nativeByteOrder)
            {
                System.out.println("asShortBuffer: DirectShortBufferU");
                return (ShortBuffer)(new DirectShortBufferU(this,
                        -1,
                        0,
                        size,
                        size,
                        off));
            }
            else
            {
                System.out.println("asShortBuffer: DirectShortBufferS");
//                VM.TraceClassLoading = true;
//                VM.verboseClassLoading = true;
                return (ShortBuffer)(new DirectShortBufferS(this,
                        -1,
                        0,
                        size,
                        size,
                        off));
            }
//            return (nativeByteOrder
//                    ? (ShortBuffer)(new DirectShortBufferU(this,
//                                                                 -1,
//                                                                 0,
//                                                                 size,
//                                                                 size,
//                                                                 off))
//                    : (ShortBuffer)(new DirectShortBufferS(this,
//                                                                 -1,
//                                                                 0,
//                                                                 size,
//                                                                 size,
//                                                                 off)));
        }
    }




    private int getInt(long a) {
        if (unaligned) {
            int x = Address.fromLong(a).loadInt();
            return (nativeByteOrder ? x : Bits.swap(x));
        }
        return Bits.getInt(a, bigEndian);
    }

    public int getInt() {
        return getInt(ix(nextGetIndex((1 << 2))));
    }

    public int getInt(int i) {
        return getInt(ix(checkIndex(i, (1 << 2))));
    }



    private ByteBuffer putInt(long a, int x) {

        if (unaligned) {
            int y = (x);
            Address.fromLong(a).store((nativeByteOrder ? y : Bits.swap(y)));
        } else {
            Bits.putInt(a, x, bigEndian);
        }
        return this;



    }

    public ByteBuffer putInt(int x) {

        putInt(ix(nextPutIndex((1 << 2))), x);
        return this;



    }

    public ByteBuffer putInt(int i, int x) {

        putInt(ix(checkIndex(i, (1 << 2))), x);
        return this;



    }

    public IntBuffer asIntBuffer() {
        int off = this.position();
        int lim = this.limit();
        assert (off <= lim);
        int rem = (off <= lim ? lim - off : 0);

        int size = rem >> 2;
        if (!unaligned && ((address + off) % (1 << 2) != 0)) {
            return (bigEndian
                    ? (IntBuffer)(new ByteBufferAsIntBufferB(this,
                                                                       -1,
                                                                       0,
                                                                       size,
                                                                       size,
                                                                       off))
                    : (IntBuffer)(new ByteBufferAsIntBufferL(this,
                                                                       -1,
                                                                       0,
                                                                       size,
                                                                       size,
                                                                       off)));
        } else {
            return (nativeByteOrder
                    ? (IntBuffer)(new DirectIntBufferU(this,
                                                                 -1,
                                                                 0,
                                                                 size,
                                                                 size,
                                                                 off))
                    : (IntBuffer)(new DirectIntBufferS(this,
                                                                 -1,
                                                                 0,
                                                                 size,
                                                                 size,
                                                                 off)));
        }
    }




    private long getLong(long a) {
        if (unaligned) {
            long x = Address.fromLong(a).loadLong();
            return (nativeByteOrder ? x : Bits.swap(x));
        }
        return Bits.getLong(a, bigEndian);
    }

    public long getLong() {
        return getLong(ix(nextGetIndex((1 << 3))));
    }

    public long getLong(int i) {
        return getLong(ix(checkIndex(i, (1 << 3))));
    }



    private ByteBuffer putLong(long a, long x) {

        if (unaligned) {
            long y = (x);
            Address.fromLong(a).store((nativeByteOrder ? y : Bits.swap(y)));
        } else {
            Bits.putLong(a, x, bigEndian);
        }
        return this;



    }

    public ByteBuffer putLong(long x) {

        putLong(ix(nextPutIndex((1 << 3))), x);
        return this;



    }

    public ByteBuffer putLong(int i, long x) {

        putLong(ix(checkIndex(i, (1 << 3))), x);
        return this;



    }

    public LongBuffer asLongBuffer() {
        int off = this.position();
        int lim = this.limit();
        assert (off <= lim);
        int rem = (off <= lim ? lim - off : 0);

        int size = rem >> 3;
        if (!unaligned && ((address + off) % (1 << 3) != 0)) {
            return (bigEndian
                    ? (LongBuffer)(new ByteBufferAsLongBufferB(this,
                                                                       -1,
                                                                       0,
                                                                       size,
                                                                       size,
                                                                       off))
                    : (LongBuffer)(new ByteBufferAsLongBufferL(this,
                                                                       -1,
                                                                       0,
                                                                       size,
                                                                       size,
                                                                       off)));
        } else {
            return (nativeByteOrder
                    ? (LongBuffer)(new DirectLongBufferU(this,
                                                                 -1,
                                                                 0,
                                                                 size,
                                                                 size,
                                                                 off))
                    : (LongBuffer)(new DirectLongBufferS(this,
                                                                 -1,
                                                                 0,
                                                                 size,
                                                                 size,
                                                                 off)));
        }
    }




    private float getFloat(long a) {
        if (unaligned) {
            return Address.fromLong(a).loadFloat();
        }
        return Bits.getFloat(a, bigEndian);
    }

    public float getFloat() {
        return getFloat(ix(nextGetIndex((1 << 2))));
    }

    public float getFloat(int i) {
        return getFloat(ix(checkIndex(i, (1 << 2))));
    }



    private ByteBuffer putFloat(long a, float x) {

        if (unaligned) {
            Address.fromLong(a).store((nativeByteOrder ? (int)x : Bits.swap((int)x)));
        } else {
            Bits.putFloat(a, x, bigEndian);
        }
        Address.fromLong(a).store(x);
        return this;



    }

    public ByteBuffer putFloat(float x) {

        putFloat(ix(nextPutIndex((1 << 2))), x);
        return this;



    }

    public ByteBuffer putFloat(int i, float x) {

        putFloat(ix(checkIndex(i, (1 << 2))), x);
        return this;



    }

    public FloatBuffer asFloatBuffer() {
        int off = this.position();
        int lim = this.limit();
        assert (off <= lim);
        int rem = (off <= lim ? lim - off : 0);

        int size = rem >> 2;
        if (!unaligned && ((address + off) % (1 << 2) != 0)) {
            return (bigEndian
                    ? (FloatBuffer)(new ByteBufferAsFloatBufferB(this,
                                                                       -1,
                                                                       0,
                                                                       size,
                                                                       size,
                                                                       off))
                    : (FloatBuffer)(new ByteBufferAsFloatBufferL(this,
                                                                       -1,
                                                                       0,
                                                                       size,
                                                                       size,
                                                                       off)));
        } else {
            return (nativeByteOrder
                    ? (FloatBuffer)(new DirectFloatBufferU(this,
                                                                 -1,
                                                                 0,
                                                                 size,
                                                                 size,
                                                                 off))
                    : (FloatBuffer)(new DirectFloatBufferS(this,
                                                                 -1,
                                                                 0,
                                                                 size,
                                                                 size,
                                                                 off)));
        }
    }




    private double getDouble(long a) {
        if (unaligned) {
            long x = Magic.doubleAsLongBits(Address.fromLong(a).loadDouble());
            return Double.longBitsToDouble(nativeByteOrder ? x : Bits.swap(x));
        }
        return Bits.getDouble(a, bigEndian);
    }

    public double getDouble() {
        return getDouble(ix(nextGetIndex((1 << 3))));
    }

    public double getDouble(int i) {
        return getDouble(ix(checkIndex(i, (1 << 3))));
    }



    private ByteBuffer putDouble(long a, double x) {

        if (unaligned) {
            long y = Magic.doubleAsLongBits(x);
            Address.fromLong(a).store(nativeByteOrder ? y : Bits.swap(y));
        } else {
            Bits.putDouble(a, x, bigEndian);
        }
        return this;



    }

    public ByteBuffer putDouble(double x) {

        putDouble(ix(nextPutIndex((1 << 3))), x);
        return this;



    }

    public ByteBuffer putDouble(int i, double x) {

        putDouble(ix(checkIndex(i, (1 << 3))), x);
        return this;



    }

    public DoubleBuffer asDoubleBuffer() {
        int off = this.position();
        int lim = this.limit();
        assert (off <= lim);
        int rem = (off <= lim ? lim - off : 0);

        int size = rem >> 3;
        if (!unaligned && ((address + off) % (1 << 3) != 0)) {
            return (bigEndian
                    ? (DoubleBuffer)(new ByteBufferAsDoubleBufferB(this,
                                                                       -1,
                                                                       0,
                                                                       size,
                                                                       size,
                                                                       off))
                    : (DoubleBuffer)(new ByteBufferAsDoubleBufferL(this,
                                                                       -1,
                                                                       0,
                                                                       size,
                                                                       size,
                                                                       off)));
        } else {
            return (nativeByteOrder
                    ? (DoubleBuffer)(new DirectDoubleBufferU(this,
                                                                 -1,
                                                                 0,
                                                                 size,
                                                                 size,
                                                                 off))
                    : (DoubleBuffer)(new DirectDoubleBufferS(this,
                                                                 -1,
                                                                 0,
                                                                 size,
                                                                 size,
                                                                 off)));
        }
    }











}
