// FileDescriptor.java, created Fri Apr  5 18:36:41 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.sun15_win32.java.io;

/*
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: FileDescriptor.java,v 1.2 2004/03/09 06:26:55 jwhaley Exp $
 */
class FileDescriptor {
    int fd;
    private long handle;
    
    static FileDescriptor in;
    static FileDescriptor out;
    static FileDescriptor err;
    
    public static void init() {
        in.fd = 0;
        out.fd = 1;
        err.fd = 2;
    }
    
}
