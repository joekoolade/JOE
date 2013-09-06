// OutputWindow.java, created Mon Jul  8  4:06:18 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.Common.java.util.zip;

/**
 * OutputWindow
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: OutputWindow.java,v 1.5 2004/03/09 06:26:29 jwhaley Exp $
 */
class OutputWindow {
  private final int WINDOW_SIZE = 1 << 15;
  private final int WINDOW_MASK = WINDOW_SIZE - 1;

  private byte[] window = new byte[WINDOW_SIZE]; //The window is 2^15 bytes
  private int window_end  = 0;
  private int window_filled = 0;

  public void write(int abyte)
  {
    if (window_filled++ == WINDOW_SIZE)
      throw new java.lang.IllegalStateException("Window full");
    window[window_end++] = (byte) abyte;
    window_end &= WINDOW_MASK;
  }


  private final void slowRepeat(int rep_start, int len, int dist)
  {
    while (len-- > 0)
      {
        window[window_end++] = window[rep_start++];
        window_end &= WINDOW_MASK;
        rep_start &= WINDOW_MASK;
      }
  }

  public void repeat(int len, int dist)
  {
    if ((window_filled += len) > WINDOW_SIZE)
      throw new java.lang.IllegalStateException("Window full");

    int rep_start = (window_end - dist) & WINDOW_MASK;
    int border = WINDOW_SIZE - len;
    if (rep_start <= border && window_end < border)
      {
        if (len <= dist)
          {
            java.lang.System.arraycopy(window, rep_start, window, window_end, len);
            window_end += len;
          }
        else
          {
            /* We have to copy manually, since the repeat pattern overlaps.
             */
            while (len-- > 0)
              window[window_end++] = window[rep_start++];
          }
      }
    else
      slowRepeat(rep_start, len, dist);
  }

  public int copyStored(StreamManipulator input, int len)
  {
    len = java.lang.Math.min(java.lang.Math.min(len, WINDOW_SIZE - window_filled), 
                   input.getAvailableBytes());
    int copied;

    int tailLen = WINDOW_SIZE - window_end;
    if (len > tailLen)
      {
        copied = input.copyBytes(window, window_end, tailLen);
        if (copied == tailLen)
          copied += input.copyBytes(window, 0, len - tailLen);
      }
    else
      copied = input.copyBytes(window, window_end, len);

    window_end = (window_end + copied) & WINDOW_MASK;
    window_filled += copied;
    return copied;
  }

  public void copyDict(byte[] dict, int offset, int len)
  {
    if (window_filled > 0)
      throw new java.lang.IllegalStateException();

    if (len > WINDOW_SIZE)
      {
        offset += len - WINDOW_SIZE;
        len = WINDOW_SIZE;
      }
    java.lang.System.arraycopy(dict, offset, window, 0, len);
    window_end = len & WINDOW_MASK;
  }

  public int getFreeSpace()
  {
    return WINDOW_SIZE - window_filled;
  }

  public int getAvailable()
  {
    return window_filled;
  }

  public int copyOutput(byte[] output, int offset, int len)
  {
    int copy_end = window_end;
    if (len > window_filled)
      len = window_filled;
    else
      copy_end = (window_end - window_filled + len) & WINDOW_MASK;

    int copied = len;
    int tailLen = len - copy_end;

    if (tailLen > 0)
      {
        java.lang.System.arraycopy(window, WINDOW_SIZE - tailLen,
                         output, offset, tailLen);
        offset += tailLen;
        len = copy_end;
      }
    java.lang.System.arraycopy(window, copy_end - len, output, offset, len);
    window_filled -= copied;
    if (window_filled < 0)
      throw new java.lang.IllegalStateException();
    return copied;
  }

  public void reset() {
    window_filled = window_end = 0;
  }
}
