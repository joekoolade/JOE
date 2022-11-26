/*
 * Copyright (c) 1996, 2005, Oracle and/or its affiliates. All rights reserved.
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

package java.util.zip;

/**
 * A class that can be used to compute the CRC-32 of a data stream.
 *
 * @see         Checksum
 * @author      David Connelly
 */
public
class CRC32 implements Checksum {
    private int crc;

    /*
     *  The following logic has come from RFC1952.
     */
    private static int[] crc_table = null;
    static {
      crc_table = new int[256];
      for (int n = 0; n < 256; n++) {
        int c = n;
        for (int k = 8;  --k >= 0; ) {
          if ((c & 1) != 0)
        	  c = 0xedb88320 ^ (c >>> 1);
          else
            c = c >>> 1;
        }
        crc_table[n] = c;
      }
    }
    /**
     * Creates a new CRC32 object.
     */
    public CRC32() {
    }


    /**
     * Updates CRC-32 with specified byte.
     */
    public void update(int b)
    {
    	byte[] buf = new byte[1];
    	buf[0] = (byte)b;
        update(buf, 0, 1);
    }

    /**
     * Updates CRC-32 with specified array of bytes.
     */
    public void update(byte[] b, int off, int len) {
        int c = ~crc;
        while (--len >= 0)
          c = crc_table[(c^b[off++])&0xff]^(c >>> 8);
        crc = ~c;
    }

    /**
     * Updates checksum with specified array of bytes.
     *
     * @param b the array of bytes to update the checksum with
     */
    public void update(byte[] b) {
        update(b, 0, b.length);
    }

    /**
     * Resets CRC-32 to initial value.
     */
    public void reset() {
        crc = 0;
    }

    public void reset(long vv){
        crc = (int)(vv&0xffffffffL);
      }

    /**
     * Returns CRC-32 value.
     */
    public long getValue() {
        return (long)crc & 0xffffffffL;
    }

	// The following logic has come from zlib.1.2.
	private static final int GF2_DIM = 32;

	static long combine(long crc1, long crc2, long len2) 
	{
		long row;
		long[] even = new long[GF2_DIM];
		long[] odd = new long[GF2_DIM];

		// degenerate case (also disallow negative lengths)
		if (len2 <= 0)
			return crc1;

		// put operator for one zero bit in odd
		odd[0] = 0xedb88320L; // CRC-32 polynomial
		row = 1;
		for (int n = 1; n < GF2_DIM; n++) {
			odd[n] = row;
			row <<= 1;
		}

		// put operator for two zero bits in even
		gf2_matrix_square(even, odd);

		// put operator for four zero bits in odd
		gf2_matrix_square(odd, even);

		// apply len2 zeros to crc1 (first square will put the operator for one
		// zero byte, eight zero bits, in even)
		do {
			// apply zeros operator for this bit of len2
			gf2_matrix_square(even, odd);
			if ((len2 & 1) != 0)
				crc1 = gf2_matrix_times(even, crc1);
			len2 >>= 1;

			// if no more bits set, then done
			if (len2 == 0)
				break;

			// another iteration of the loop with odd and even swapped
			gf2_matrix_square(odd, even);
			if ((len2 & 1) != 0)
				crc1 = gf2_matrix_times(odd, crc1);
			len2 >>= 1;

			// if no more bits set, then done
		} while (len2 != 0);

		/* return combined crc */
		crc1 ^= crc2;
		return crc1;
	}

	private static long gf2_matrix_times(long[] mat, long vec) 
	{
		long sum = 0;
		int index = 0;
		while (vec != 0) {
			if ((vec & 1) != 0)
				sum ^= mat[index];
			vec >>= 1;
			index++;
		}
		return sum;
	}

	static final void gf2_matrix_square(long[] square, long[] mat) 
	{
		for (int n = 0; n < GF2_DIM; n++)
			square[n] = gf2_matrix_times(mat, mat[n]);
	}

	public CRC32 copy() 
	{
		CRC32 foo = new CRC32();
		foo.crc = this.crc;
		return foo;
	}

}
