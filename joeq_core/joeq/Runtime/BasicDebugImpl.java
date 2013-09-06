// BasicDebugImpl.java, created Sat Feb 22 13:35:27 2003 by joewhaley
// Copyright (C) 2001-3 mcmartin
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Runtime;

/**
 * BasicDebugImpl
 *
 * @author Michael Martin <mcmartin@stanford.edu>
 * @version $Id: BasicDebugImpl.java,v 1.4 2004/03/09 22:01:46 jwhaley Exp $
 */
public class BasicDebugImpl implements Debug.Delegate {

    public void write(byte[] msg, int size) {
        for (int i=0; i<size; ++i)
            System.err.print((char) msg[i]);
    }

    public void write(String msg) {
        System.err.print(msg);
    }

    public void writeln(byte[] msg, int size) {
        write(msg, size);
        System.err.println();
    }

    public void writeln(String msg) {
        System.err.println(msg);
    }

    public void die(int code) {
        new InternalError().printStackTrace();
        System.exit(code);
    }

}
