// ZipFile.java, created Fri Apr  5 18:36:41 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.sun14_win32.java.util.zip;

/**
 * ZipFile
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: ZipFile.java,v 1.6 2004/03/09 06:26:55 jwhaley Exp $
 */
public abstract class ZipFile {

    private String name;
    private java.util.Vector inflaters;
    private java.io.RandomAccessFile raf;
    private java.util.Hashtable entries;
    
    public void __init__(String name) throws java.io.IOException {
        this.name = name;
        java.io.RandomAccessFile raf = new java.io.RandomAccessFile(name, "r");
        this.raf = raf;
        this.inflaters = new java.util.Vector();
        this.readCEN();
    }
    
    private native void readCEN() throws java.io.IOException;
}
