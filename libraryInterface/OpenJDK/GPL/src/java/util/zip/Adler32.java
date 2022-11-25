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
 * A class that can be used to compute the Adler-32 checksum of a data stream.
 * An Adler-32 checksum is almost as reliable as a CRC-32 but can be computed
 * much faster.
 *
 * @see Checksum
 * @author David Connelly
 */
public class Adler32 implements Checksum {
	private int adler = 1;
	// largest prime smaller than 65536
	static final private int BASE = 65521;
	// NMAX is the largest n such that 255n(n+1)/2 + (n+1)(BASE-1) <= 2^32-1
	static final private int NMAX = 5552;

	private long s1 = 1L;
	private long s2 = 0L;

	/**
	 * Creates a new Adler32 object.
	 */
	public Adler32() {
	}

	/**
	 * Updates checksum with specified byte.
	 *
	 * @param b an array of bytes
	 */
	public void update(int b) {
		byte[] barray = new byte[1];
		barray[0] = (byte)b;
		update(barray, 0, 1);
	}

	/**
	 * Updates checksum with specified array of bytes.
	 */
	public void update(byte[] buf, int index, int len) {

		if (len == 1) {
			s1 += buf[index++] & 0xff;
			s2 += s1;
			s1 %= BASE;
			s2 %= BASE;
			return;
		}

		int len1 = len / NMAX;
		int len2 = len % NMAX;
		while (len1-- > 0) {
			int k = NMAX;
			len -= k;
			while (k-- > 0) {
				s1 += buf[index++] & 0xff;
				s2 += s1;
			}
			s1 %= BASE;
			s2 %= BASE;
		}

		int k = len2;
		len -= k;
		while (k-- > 0) {
			s1 += buf[index++] & 0xff;
			s2 += s1;
		}
		s1 %= BASE;
		s2 %= BASE;
	}

	/**
	 * Updates checksum with specified array of bytes.
	 */
	public void update(byte[] b) {
		update(b, 0, b.length);
	}

	/**
	 * Resets checksum to initial value.
	 */
	public void reset(long init) {
		s1 = init & 0xffff;
		s2 = (init >> 16) & 0xffff;
	}

	public void reset() {
		s1 = 1L;
		s2 = 0L;
	}

	/**
	 * Returns checksum value.
	 */
	public long getValue() {
		return ((s2 << 16) | s1);
	}

}
