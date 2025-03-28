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

import org.vmmagic.unboxed.Address;

import sun.nio.ch.DirectBuffer;

class DirectByteBufferR
    extends DirectByteBuffer
    implements DirectBuffer
{
    // Primary constructor
    //
    DirectByteBufferR(int cap) {                   // package-private
        super(cap);
    }
    // For memory-mapped buffers -- invoked by FileChannelImpl via reflection
    //
    protected DirectByteBufferR(int cap, long addr, FileDescriptor fd, Runnable unmapper)
    {
        super(cap, addr, fd, unmapper);
    }
    // For duplicates and slices
    //
    DirectByteBufferR(DirectBuffer db,         // package-private
                      int mark, int pos, int lim, int cap,
                      int off)
    {
        super(db, mark, pos, lim, cap, off);
    }

    public ByteBuffer slice() {
        int pos = this.position();
        int lim = this.limit();
        assert (pos <= lim);
        int rem = (pos <= lim ? lim - pos : 0);
        return new DirectByteBufferR(this, -1, 0, rem, rem, pos);
    }

    public ByteBuffer duplicate() {
        return new DirectByteBufferR(this,
                                     this.markValue(),
                                     this.position(),
                                     this.limit(),
                                     this.capacity(),
                                     0);
    }

    public ByteBuffer asReadOnlyBuffer() {
        return duplicate();
    }

    public ByteBuffer put(byte x) {
        throw new ReadOnlyBufferException();
    }

    public ByteBuffer put(int i, byte x) {
        throw new ReadOnlyBufferException();
    }

    public ByteBuffer put(ByteBuffer src) {
        throw new ReadOnlyBufferException();
    }

    public ByteBuffer put(byte[] src, int offset, int length) {
        throw new ReadOnlyBufferException();
    }

    public ByteBuffer compact() {
        throw new ReadOnlyBufferException();
    }

    public boolean isDirect() {
        return true;
    }

    public boolean isReadOnly() {
        return true;
    }

    byte _get(int i) {                          // package-private
        return Address.fromLong(address+i).loadByte();
    }

    void _put(int i, byte b) {                  // package-private
        throw new ReadOnlyBufferException();
    }

    private ByteBuffer putChar(long a, char x) {
        throw new ReadOnlyBufferException();
    }

    public ByteBuffer putChar(char x) {
        throw new ReadOnlyBufferException();
    }

    public ByteBuffer putChar(int i, char x) {
        throw new ReadOnlyBufferException();
    }

    public CharBuffer asCharBuffer() {
        int off = this.position();
        int lim = this.limit();
        assert (off <= lim);
        int rem = (off <= lim ? lim - off : 0);

        int size = rem >> 1;
        if (!unaligned && ((address + off) % (1 << 1) != 0)) {
            return (bigEndian
                    ? (CharBuffer)(new ByteBufferAsCharBufferRB(this, -1, 0, size, size, off))
                    : (CharBuffer)(new ByteBufferAsCharBufferRL(this, -1, 0, size, size, off)));
        } else {
            return (nativeByteOrder
                    ? (CharBuffer)(new DirectCharBufferRU(this, -1, 0, size, size, off))
                    : (CharBuffer)(new DirectCharBufferRS(this, -1, 0, size, size, off)));
        }
    }

    private ByteBuffer putShort(long a, short x) {
        throw new ReadOnlyBufferException();
    }

    public ByteBuffer putShort(short x) {
        throw new ReadOnlyBufferException();
    }

    public ByteBuffer putShort(int i, short x) {
        throw new ReadOnlyBufferException();
    }

    public ShortBuffer asShortBuffer() {
        int off = this.position();
        int lim = this.limit();
        assert (off <= lim);
        int rem = (off <= lim ? lim - off : 0);

        int size = rem >> 1;
        if (!unaligned && ((address + off) % (1 << 1) != 0)) {
            return (bigEndian
                    ? (ShortBuffer)(new ByteBufferAsShortBufferRB(this, -1, 0, size, size, off))
                    : (ShortBuffer)(new ByteBufferAsShortBufferRL(this, -1, 0, size, size, off)));
        } else {
            return (nativeByteOrder
                    ? (ShortBuffer)(new DirectShortBufferRU(this, -1, 0, size, size, off))
                    : (ShortBuffer)(new DirectShortBufferRS(this, -1, 0, size, size, off)));
        }
    }

    private ByteBuffer putInt(long a, int x) {
        throw new ReadOnlyBufferException();
    }

    public ByteBuffer putInt(int x) {
        throw new ReadOnlyBufferException();
    }

    public ByteBuffer putInt(int i, int x) {
        throw new ReadOnlyBufferException();

    }

    public IntBuffer asIntBuffer() {
        int off = this.position();
        int lim = this.limit();
        assert (off <= lim);
        int rem = (off <= lim ? lim - off : 0);

        int size = rem >> 2;
        if (!unaligned && ((address + off) % (1 << 2) != 0)) {
            return (bigEndian
                    ? (IntBuffer)(new ByteBufferAsIntBufferRB(this, -1, 0, size, size, off))
                    : (IntBuffer)(new ByteBufferAsIntBufferRL(this, -1,  0, size, size, off)));
        } else {
            return (nativeByteOrder
                    ? (IntBuffer)(new DirectIntBufferRU(this, -1, 0, size, size, off))
                    : (IntBuffer)(new DirectIntBufferRS(this, -1, 0, size, size, off)));
        }
    }

    private ByteBuffer putLong(long a, long x) {
        throw new ReadOnlyBufferException();
    }

    public ByteBuffer putLong(long x) {
        throw new ReadOnlyBufferException();
    }

    public ByteBuffer putLong(int i, long x) {
        throw new ReadOnlyBufferException();
    }

    public LongBuffer asLongBuffer() {
        int off = this.position();
        int lim = this.limit();
        assert (off <= lim);
        int rem = (off <= lim ? lim - off : 0);

        int size = rem >> 3;
        if (!unaligned && ((address + off) % (1 << 3) != 0)) {
            return (bigEndian
                    ? (LongBuffer)(new ByteBufferAsLongBufferRB(this,
                                                                       -1,
                                                                       0,
                                                                       size,
                                                                       size,
                                                                       off))
                    : (LongBuffer)(new ByteBufferAsLongBufferRL(this,
                                                                       -1,
                                                                       0,
                                                                       size,
                                                                       size,
                                                                       off)));
        } else {
            return (nativeByteOrder
                    ? (LongBuffer)(new DirectLongBufferRU(this,
                                                                 -1,
                                                                 0,
                                                                 size,
                                                                 size,
                                                                 off))
                    : (LongBuffer)(new DirectLongBufferRS(this,
                                                                 -1,
                                                                 0,
                                                                 size,
                                                                 size,
                                                                 off)));
        }
    }






















    private ByteBuffer putFloat(long a, float x) {









        throw new ReadOnlyBufferException();

    }

    public ByteBuffer putFloat(float x) {




        throw new ReadOnlyBufferException();

    }

    public ByteBuffer putFloat(int i, float x) {




        throw new ReadOnlyBufferException();

    }

    public FloatBuffer asFloatBuffer() {
        int off = this.position();
        int lim = this.limit();
        assert (off <= lim);
        int rem = (off <= lim ? lim - off : 0);

        int size = rem >> 2;
        if (!unaligned && ((address + off) % (1 << 2) != 0)) {
            return (bigEndian
                    ? (FloatBuffer)(new ByteBufferAsFloatBufferRB(this,
                                                                       -1,
                                                                       0,
                                                                       size,
                                                                       size,
                                                                       off))
                    : (FloatBuffer)(new ByteBufferAsFloatBufferRL(this,
                                                                       -1,
                                                                       0,
                                                                       size,
                                                                       size,
                                                                       off)));
        } else {
            return (nativeByteOrder
                    ? (FloatBuffer)(new DirectFloatBufferRU(this,
                                                                 -1,
                                                                 0,
                                                                 size,
                                                                 size,
                                                                 off))
                    : (FloatBuffer)(new DirectFloatBufferRS(this,
                                                                 -1,
                                                                 0,
                                                                 size,
                                                                 size,
                                                                 off)));
        }
    }






















    private ByteBuffer putDouble(long a, double x) {









        throw new ReadOnlyBufferException();

    }

    public ByteBuffer putDouble(double x) {




        throw new ReadOnlyBufferException();

    }

    public ByteBuffer putDouble(int i, double x) {




        throw new ReadOnlyBufferException();

    }

    public DoubleBuffer asDoubleBuffer() {
        int off = this.position();
        int lim = this.limit();
        assert (off <= lim);
        int rem = (off <= lim ? lim - off : 0);

        int size = rem >> 3;
        if (!unaligned && ((address + off) % (1 << 3) != 0)) {
            return (bigEndian
                    ? (DoubleBuffer)(new ByteBufferAsDoubleBufferRB(this,
                                                                       -1,
                                                                       0,
                                                                       size,
                                                                       size,
                                                                       off))
                    : (DoubleBuffer)(new ByteBufferAsDoubleBufferRL(this,
                                                                       -1,
                                                                       0,
                                                                       size,
                                                                       size,
                                                                       off)));
        } else {
            return (nativeByteOrder
                    ? (DoubleBuffer)(new DirectDoubleBufferRU(this,
                                                                 -1,
                                                                 0,
                                                                 size,
                                                                 size,
                                                                 off))
                    : (DoubleBuffer)(new DirectDoubleBufferRS(this,
                                                                 -1,
                                                                 0,
                                                                 size,
                                                                 size,
                                                                 off)));
        }
    }

}
