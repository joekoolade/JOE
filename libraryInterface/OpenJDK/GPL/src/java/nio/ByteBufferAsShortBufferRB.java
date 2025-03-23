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


class ByteBufferAsShortBufferRB                  // package-private
    extends ByteBufferAsShortBufferB
{
    ByteBufferAsShortBufferRB(ByteBuffer bb) {   // package-private
        super(bb);
    }

    ByteBufferAsShortBufferRB(ByteBuffer bb,
                              int mark, int pos, int lim, int cap,
                              int off)
    {
        super(bb, mark, pos, lim, cap, off);
    }

    public ShortBuffer slice() {
        int pos = this.position();
        int lim = this.limit();
        assert (pos <= lim);
        int rem = (pos <= lim ? lim - pos : 0);
        int off = (pos << 1) + offset;
        assert (off >= 0);
        return new ByteBufferAsShortBufferRB(bb, -1, 0, rem, rem, off);
    }

    public ShortBuffer duplicate() {
        return new ByteBufferAsShortBufferRB(bb,
                                             this.markValue(),
                                             this.position(),
                                             this.limit(),
                                             this.capacity(),
                                             offset);
    }

    public ShortBuffer asReadOnlyBuffer() {
        return duplicate();
    }

    public ShortBuffer put(short x) {
        throw new ReadOnlyBufferException();
    }

    public ShortBuffer put(int i, short x) {
        throw new ReadOnlyBufferException();
    }

    public ShortBuffer compact() {
        throw new ReadOnlyBufferException();
    }

    public boolean isDirect() {
        return bb.isDirect();
    }

    public boolean isReadOnly() {
        return true;
    }

    public ByteOrder order() {
        return ByteOrder.BIG_ENDIAN;
    }

}
