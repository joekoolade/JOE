/*
 * Created on Oct 27, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */

package baremetal.platform;

import baremetal.kernel.Memory;
import baremetal.runtime.Utf8;

/**
 * @author joe
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class Console {

  /*
   * Default mode on VGA card
   */
  static int mode=3;
  static int currentLine=0;
  static int rows=25;
  static int columns=80;
  static int x=0;
  static int y=0;
  //	static int attributes=0x1f; // this white on blue
  static int attributes=0x7; // white on black
  private static int videoBuffer=0xb8000;
  // memory position of cursor
  static int pos=videoBuffer;
  static boolean canPrint=true;

  /*
   * Only one console; this should not be instantiated
   */
  private Console() {
  }

  public static void writeCh(int ch) {
    if (!canPrint)
      return;

    if (ch != 10 && ch != 13) {
      x+=1;
      Memory.writeByte(pos, ch);
      Memory.writeByte(pos + 1, attributes);
    }
    if (ch == 10 || x >= 80) {
      scroll();
      pos=(y * columns + x) * 2;
      pos+=videoBuffer;
    } else if (ch == 13) {
      x=0;
      pos=2 * y * columns;
      pos+=videoBuffer;
    } else
      pos+=2;
    setCursor();
  }

  /**
   *  
   */
  private static void scroll() {
    // see if on the last line
    if (y == rows - 1) {
      // we are on the last line
      // so we need to scroll up one
      scrollUp();
      x=0;
    } else {
      currentLine++;
      y++;
      x=0;
    }
  }

  /**
   *  
   */
  private static void scrollUp() {
    Memory.bcopy(videoBuffer + 2 * columns, videoBuffer, (rows - 1) * columns * 2);
    clearLine(25);
  }

  /**
   *  
   */
  private static void setCursor() {
  }

  public final static void writeln(String str) {
    write(str);
    writeln();
  }

  public final static void write(String str) {
    if (!canPrint)
      return;

    for (int i=0; i < str.length(); i++) {
      writeCh(str.charAt(i));
    }

  }

  private final static void clearLine(int line) {
    for (int i=0; i < 2 * columns; i+=2) {
      Memory.writeByte(videoBuffer + (line - 1) * columns * 2 + i, 0x20);
      Memory.writeByte(videoBuffer + (line - 1) * columns * 2 + i + 1, attributes);
    }
  }

  public final static void clearScreen() {
    if (!canPrint)
      return;
    for (int i=0, j=0; i < 2000; i++, j+=2) {
      Memory.writeByte(videoBuffer + j, 0x20);
      Memory.writeByte(videoBuffer + j + 1, attributes);
    }
    x=0;
    y=0;
    pos=videoBuffer;
    currentLine=0;
  }

  /**
   *  
   */
  public static void writeln() {
    writeCh('\n');
  }

  public static void write(int num, int radix) {
    if (radix < 2 || radix > 36)
      radix=10;

    int i=33;
    boolean isNeg=false;
    if (num < 0) {
      isNeg=true;
      num=-num;

      // When the value is MIN_VALUE, it overflows when made positive
      if (num < 0) {
        buffer[--i]=digits[(int) (-(num + radix) % radix)];
        num=-(num / radix);
      }
    }

    do {
      buffer[--i]=digits[num % radix];
      num/=radix;
    } while (num > 0);

    if (isNeg)
      buffer[--i]='-';

    /*
     * Print buffer out
     */
    for (; i < 33; i++)
      writeCh(buffer[i]);
  }

  /**
   * Table for calculating digits, used in Character, Long, and Integer.
   */
  static final char[] digits={'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
      'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
      'w', 'x', 'y', 'z'};
  // For negative numbers, print out the absolute value w/ a leading '-'.
  // Use an array large enough for a binary number.
  static final char[] buffer=new char[33];

  /**
   * @param depth
   */
  public static void write(int val) {
    write(val, 10);

  }

  /**
   * @param hasInterfaces
   */
  public static void writeln(int val) {
    write(val);
    writeln();
  }
}