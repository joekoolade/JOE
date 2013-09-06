// StreamManipulator.java, created Mon Jul  8  4:06:18 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.

package joeq.ClassLib.Common.java.util.zip;

/**
 * StreamManipulator
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: StreamManipulator.java,v 1.5 2004/03/09 06:26:29 jwhaley Exp $
 */
class StreamManipulator {
    
  private byte[] window;
  private int window_start = 0;
  private int window_end = 0;

  private int buffer = 0;
  private int bits_in_buffer = 0;

  /**
   * Get the next n bits but don't increase input pointer.  n must be
   * less or equal 16 and if you if this call succeeds, you must drop
   * at least n-8 bits in the next call.
   * 
   * @return the value of the bits, or -1 if not enough bits available.  */
  public final int peekBits(int n)
  {
    if (bits_in_buffer < n)
      {
        if (window_start == window_end)
          return -1;
        buffer |= (window[window_start++] & 0xff
                   | (window[window_start++] & 0xff) << 8) << bits_in_buffer;
        bits_in_buffer += 16;
      }
    return buffer & ((1 << n) - 1);
  }

  /* Drops the next n bits from the input.  You should have called peekBits
   * with a bigger or equal n before, to make sure that enough bits are in
   * the bit buffer.
   */
  public final void dropBits(int n)
  {
    buffer >>>= n;
    bits_in_buffer -= n;
  }

  /**
   * Gets the next n bits and increases input pointer.  This is equivalent
   * to peekBits followed by dropBits, except for correct error handling.
   * @return the value of the bits, or -1 if not enough bits available. 
   */
  public final int getBits(int n)
  {
    int bits = peekBits(n);
    if (bits >= 0)
      dropBits(n);
    return bits;
  }
  /**
   * Gets the number of bits available in the bit buffer.  This must be
   * only called when a previous peekBits() returned -1.
   * @return the number of bits available.
   */
  public final int getAvailableBits()
  {
    return bits_in_buffer;
  }

  /**
   * Gets the number of bytes available.  
   * @return the number of bytes available.
   */
  public final int getAvailableBytes()
  {
    return window_end - window_start + (bits_in_buffer >> 3);
  }

  /**
   * Skips to the next byte boundary.
   */
  public void skipToByteBoundary()
  {
    buffer >>= (bits_in_buffer & 7);
    bits_in_buffer &= ~7;
  }

  public final boolean needsInput() {
    return window_start == window_end;
  }


  /* Copies length bytes from input buffer to output buffer starting
   * at output[offset].  You have to make sure, that the buffer is
   * byte aligned.  If not enough bytes are available, copies fewer
   * bytes.
   * @param length the length to copy, 0 is allowed.
   * @return the number of bytes copied, 0 if no byte is available.  
   */
  public int copyBytes(byte[] output, int offset, int length)
  {
    if (length < 0)
      throw new java.lang.IllegalArgumentException("length negative");
    if ((bits_in_buffer & 7) != 0)  
      /* bits_in_buffer may only be 0 or 8 */
      throw new java.lang.IllegalStateException("Bit buffer is not aligned!");

    int count = 0;
    while (bits_in_buffer > 0 && length > 0)
      {
        output[offset++] = (byte) buffer;
        buffer >>>= 8;
        bits_in_buffer -= 8;
        length--;
        count++;
      }
    if (length == 0)
      return count;

    int avail = window_end - window_start;
    if (length > avail)
      length = avail;
    java.lang.System.arraycopy(window, window_start, output, offset, length);
    window_start += length;

    if (((window_start - window_end) & 1) != 0)
      {
        /* We always want an even number of bytes in input, see peekBits */
        buffer = (window[window_start++] & 0xff);
        bits_in_buffer = 8;
      }
    return count + length;
  }

  public StreamManipulator()
  {
  }

  public void reset()
  {
    window_start = window_end = buffer = bits_in_buffer = 0;
  }

  public void setInput(byte[] buf, int off, int len)
  {
    if (window_start < window_end)
      throw new java.lang.IllegalStateException
        ("Old input was not completely processed");

    int end = off + len;

    /* We want to throw an ArrayIndexOutOfBoundsException early.  The
     * check is very tricky: it also handles integer wrap around.  
     */
    if (0 > off || off > end || end > buf.length)
      throw new java.lang.ArrayIndexOutOfBoundsException();
    
    if ((len & 1) != 0)
      {
        /* We always want an even number of bytes in input, see peekBits */
        buffer |= (buf[off++] & 0xff) << bits_in_buffer;
        bits_in_buffer += 8;
      }
    
    window = buf;
    window_start = off;
    window_end = end;
  }

}
