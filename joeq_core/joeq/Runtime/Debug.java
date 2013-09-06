// Debug.java, created Sat Feb 22 13:35:27 2003 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Runtime;

import joeq.Main.jq;
import joeq.Memory.Address;
import joeq.UTF.Utf8;
import jwutil.util.Assert;

/**
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: Debug.java,v 1.11 2005/02/17 22:17:09 livshits Exp $
 */
public abstract class Debug {

    private static byte[] buffer = new byte[16];
    private static int bufferIndex;

    private static void writeDecimalToBuffer(int i) {
        boolean nonzero_found = false;
        bufferIndex = -1;
        if (i < 0) {
            i = -i;
            buffer[++bufferIndex] = (byte)'-';
        }
        for (int j=1000000000; j > 1; j /= 10) {
            int k = i / j;
            i = i % j;
            if (nonzero_found || k != 0) {
                buffer[++bufferIndex] = (byte)(k + '0');
                nonzero_found = true;
            }
        }
        buffer[++bufferIndex] = (byte)(i + '0');
        buffer[++bufferIndex] = (byte)0;
    }

    private static void writeHexToBuffer(int i) {
        bufferIndex = -1;
        buffer[++bufferIndex] = (byte)'0';
        buffer[++bufferIndex] = (byte)'x';
        for (int j=0; j < 8; ++j) {
            int v = (i & 0xF0000000) >>> 28;
            buffer[++bufferIndex] = (v < 0xa)?((byte)(v+'0')):((byte)(v+'a'-0xa));
            i <<= 4;
        }
        buffer[++bufferIndex] = (byte)0;
        Assert._assert(bufferIndex == 10);
    }

    public static void write(String s) {
        _delegate.write(s);
    }
    
    public static void write(byte[] msg, int size) {
        _delegate.write(msg, size);
    }

    public static void write(Utf8 u) {
        u.debugWrite();
    }
    
    public static void write(char x) {
        buffer[0] = (byte) x;
        buffer[1] = (byte) 0;
        bufferIndex = 1;
        _delegate.write(buffer, bufferIndex);
    }
    
    public static void write(int x) {
        writeDecimalToBuffer(x);
        _delegate.write(buffer, bufferIndex);
    }

    public static void writeHex(int x) {
        writeHexToBuffer(x);
        _delegate.write(buffer, bufferIndex);
    }
    
    public static void write(Address x) {
        writeHex(x.to32BitValue());
    }
    
    public static void write(int x, String s) {
        write(x); write(s);
    }

    public static void write(String s, int x) {
        write(s); write(x);
    }
    
    public static void write(String s, Address x) {
        write(s); write(x);
    }
    
    public static void write(String s1, int x, String s2) {
        write(s1); write(x); write(s2);
    }
    
    public static void write(int x1, String s, int x2) {
        write(x1); write(s); write(x2);
    }
    
    public static void writeln() {
        writeln("");
    }
    
    public static void writeln(String s) {
        _delegate.writeln(s);
    }
    
    public static void writeln(Utf8 u) {
        u.debugWrite();
        writeln();
    }
    
    public static void writeln(int x) {
        writeDecimalToBuffer(x);
        _delegate.writeln(buffer, bufferIndex);
    }
    
    public static void writelnHex(int x) {
        writeHexToBuffer(x);
        _delegate.writeln(buffer, bufferIndex);
    }
    
    public static void writeln(Address x) {
        writelnHex(x.to32BitValue());
    }
    
    public static void writeln(int x, String s) {
        write(x); writeln(s);
    }
    
    public static void writeln(String s, int x) {
        write(s); writeln(x);
    }
    
    public static void writeln(String s, Address x) {
        write(s); writeln(x);
    }
    
    public static void writeln(String s1, int x, String s2) {
        write(s1); write(x); writeln(s2);
    }

    public static void die(int code) {
        _delegate.die(code);
    }

    static interface Delegate {
        void write(byte[] msg, int size);
        void write(String msg);
        void writeln(byte[] msg, int size);
        void writeln(String msg);
        void die(int code);
    }

    private static Delegate _delegate;
    static {
        /* Set up delegates. */
        _delegate = null;
        boolean nullVM = jq.nullVM;
        if (!nullVM) {
            _delegate = attemptDelegate("joeq.Runtime.DebugImpl");
        }
        if (_delegate == null) {
            if(System.getProperty("silentdebug", "no").equals("yes")){
                _delegate = new joeq.Runtime.SilentDebugImpl();
            }else{
                _delegate = new joeq.Runtime.BasicDebugImpl();
            }
        }
    }

    private static Delegate attemptDelegate(String s) {
        String type = "debug delegate";
        try {
            Class c = Class.forName(s);
            return (Delegate)c.newInstance();
        } catch (java.lang.ClassNotFoundException x) {
            //System.err.println("Cannot find "+type+" "+s+": "+x);
        } catch (java.lang.InstantiationException x) {
            System.err.println("Cannot instantiate "+type+" "+s+": "+x);
        } catch (java.lang.IllegalAccessException x) {
            System.err.println("Cannot access "+type+" "+s+": "+x);
        }
        return null;
    }
}
