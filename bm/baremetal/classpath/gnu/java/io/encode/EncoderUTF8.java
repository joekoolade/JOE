/* EncoderUTF8.java -- Encoding class for the UTF-8 scheme
   Copyright (C) 1998, 2002, 2003 Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.
 
GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */


package gnu.java.io.encode;

import java.io.OutputStream;
import java.io.IOException;

/**
  * This class implements an encoder for the UCS Transformation Format 8
  * (UTF-8) encoding scheme.  This is defined in RFC-2279.  We only handle
  * the 1-3 byte encodings for characters in the Unicode set.
  *
  * @version 0.0
  *
  * @author Aaron M. Renn (arenn@urbanophile.com)
  */
public class EncoderUTF8 extends Encoder
{

static
{
  scheme_name = "UTF8";
  scheme_description = "UCS Transformation Format 8 (see RFC-2279)";
}

/*************************************************************************/

/*
 * Constructors
 */

public
EncoderUTF8(OutputStream out)
{
  super(out);
}

/*************************************************************************/

/*
 * Instance Methods
 */

/**
  * Returns the number of bytes the specified char array will be encoded
  * into
  */
public int
bytesInCharArray(char[] buf, int offset, int len)
{
  int num_bytes = 0;

  for (int i = offset; i < offset + len; i++)
    {
      if (buf[i] <= 0x007F)
        ++num_bytes;
      else if (buf[i] <= 0x07FF)
        num_bytes += 2;
      else
        num_bytes += 3;
    }

  return(num_bytes);
}

/*************************************************************************/

/**
  * This method converts a char array to bytes
  */
public byte[]
convertToBytes(char[] buf, int buf_offset, int len, byte[] bbuf,
               int bbuf_offset)
{
  for (int i = buf_offset; i < buf_offset + len; i++)
    {
      if (buf[i] <= 0x007F)
        {
          bbuf[bbuf_offset] = (byte)(buf[i] & 0xFF);
          ++bbuf_offset;
        }
      else if (buf[i] <= 0x07FF)
        {
          bbuf[bbuf_offset] = (byte)(0xC0 | ((buf[i] >> 6) & 0x3F));
          ++bbuf_offset;
          bbuf[bbuf_offset] = (byte)(0x80 | (buf[i] & 0x3F));
          ++bbuf_offset;
        }
      else 
        {
          bbuf[bbuf_offset] = (byte)(0xE0 | ((buf[i] >> 12) & 0x0F));
          ++bbuf_offset;
          bbuf[bbuf_offset] = (byte)(0x80 | ((buf[i] >> 6) & 0x3F));
          ++bbuf_offset;
          bbuf[bbuf_offset] = (byte)(0x80 | (buf[i] & 0x3F));
          ++bbuf_offset;
        }
    }

  return(bbuf);
}

/*************************************************************************/

/**
  * Writes a char array as bytes to the underlying stream.
  */
public void
write(char[] buf, int offset, int len) throws IOException
{
  byte[] bbuf = new byte[bytesInCharArray(buf, offset, len)];
  convertToBytes(buf, offset, len, bbuf, 0);
  out.write(bbuf);
}

} // class EncoderUTF8

