// ZipFile.java, created Sat Mar 29  2:34:35 2003 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.ibm13_win32.java.util.zip;

/**
 * ZipFile
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: ZipFile.java,v 1.3 2004/03/09 06:26:44 jwhaley Exp $
 */
public abstract class ZipFile {

    public static java.util.Vector inflaters;
    
    public static void init_inflaters() {
        inflaters = new java.util.Vector();
    }
    
    private String name;
    private java.io.RandomAccessFile raf;
    private java.util.Hashtable entries;
    
    public void __init__(String name) throws java.io.IOException {
        this.name = name;
        java.io.RandomAccessFile raf = new java.io.RandomAccessFile(name, "r");
        this.raf = raf;
        //this.inflaters = new java.util.Vector();
        this.readCEN();
    }
    
    private native void readCEN() throws java.io.IOException;
}
