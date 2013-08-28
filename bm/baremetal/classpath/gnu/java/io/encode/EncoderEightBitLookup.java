/* EncoderEightBitLookup.java -- Encodes eight-bit encodings
   Copyright (C) 1998 Free Software Foundation, Inc.

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
import java.io.CharConversionException;
import java.io.IOException;

/**
  * Numerous character encodings utilize only eight bits.  These can
  * be easily and efficiently be converted to characters using lookup tables.
  * This class is the common superclass of all <code>Encoder</code> classes
  * that use eight bit lookup tables.  All a subclass implementor has to
  * do is define an encoding name and create a class consisting of a 
  * static lookup table overriding the default. 
  *
  * @version 0.0
  *
  * @author Aaron M. Renn (arenn@urbanophile.com)
  */
public abstract class EncoderEightBitLookup extends Encoder
{

/*************************************************************************/

/*
 * Class Variables
 */
 
/**
  * This is the lookup table.  Subclasses must allocate a 255 byte char
  * array and put each Unicode character into the slot corresponding to
  * where it is encoded.  For example, if \u3768 corresponds to 0xE3,
  * put \3768 at index 277 (0xE3) in the lookup table.
  */
protected static char[] lookup_table;

/**
  * This is the second generation lookup table that is loaded when the
  * class is loaded and is where the encoding actually takes place.
  */
private static byte[] encoding_table; /* changed this from char[] to byte[] */

/*************************************************************************/

/*
 * Class Methods
 */

/**
  * This method loads the lookup table (actually a decoder lookup table)
  * into a larger encoder table.  Yes, this is slow, but it is only done
  * the first time the class is accessed.
  */
protected static void
loadTable()
{
  /* determine required size of encoding_table: */
  int max = 0; 
  for (int i = 0; i < lookup_table.length; i++)
    {
      int c = lookup_table[i]; 
      max = (c > max) ? c : max;
    }

  encoding_table = new byte[max+1];

  for (int i = 0; i < lookup_table.length; i++)
    {
      int c = lookup_table[i]; 
      if (c != 0) 
	{
	  encoding_table[c] = (byte)i;
	}
    }
}

/*************************************************************************/

/*
 * Constructors
 */
public
EncoderEightBitLookup(OutputStream out)
{
  super(out);
}

/*************************************************************************/

/*
 * Instance Methods
 */

/**
  * This method returns the number of bytes that can be converted out of
  * the <code>len</code> chars in the specified array starting at
  * <code>offset</code>.  This will be identical to the number of chars
  * in that range, i.e., <code>len</code>.
  */
public int
bytesInCharArray(char[] buf, int offset, int len)
{
  return(len);
}

/*************************************************************************/

/**
  * Convert the requested bytes to chars
  */
public byte[]
convertToBytes(char[] buf, int buf_offset, int len, byte[] bbuf, 
               int bbuf_offset) throws CharConversionException
{
  for (int i = 0; i < len; i++)
    {
      // get char to convert
      int c = buf[buf_offset + i];
      
      // lookup byte encodeing
      int b = (c < encoding_table.length) ? encoding_table[c] : 0;

      // Check for bad character
      if (b == 0x00)
        {
          if (c == 0)
            {
              bbuf[bbuf_offset + i] = 0;
            }
          else
            {
              if (bad_char_set)
                bbuf[bbuf_offset + i] = encoding_table[bad_char];
              else
                throw new CharConversionException("Encountered unencodable character: " + buf[buf_offset + i]);
            }
        }
      else
        {
          bbuf[bbuf_offset + i] = (byte) b;
        }
    }

  return(bbuf);
}

/*************************************************************************/

/**
  * Write the requested number of chars to the underlying stream
  */
public void
write(char[] buf, int offset, int len) throws IOException
{
  byte[] bbuf = new byte[len];
  
  convertToBytes(buf, offset, len, bbuf, 0);
  out.write(bbuf);
}

} // class DecoderEightBitLookup

