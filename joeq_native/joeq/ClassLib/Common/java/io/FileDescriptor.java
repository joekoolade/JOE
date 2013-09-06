// FileDescriptor.java, created Thu Jul  4  4:50:03 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.Common.java.io;

import joeq.Runtime.SystemInterface;

/**
 * FileDescriptor
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: FileDescriptor.java,v 1.6 2004/03/09 21:57:34 jwhaley Exp $
 */
abstract class FileDescriptor {
    
    int fd;
    
    public native boolean valid();
    
    public void sync() throws java.io.SyncFailedException {
        if (!this.valid()) throw new java.io.SyncFailedException("invalid file descriptor");
        int result = SystemInterface.file_sync(this.fd);
        if (result != 0) throw new java.io.SyncFailedException("flush failed");
    }
    private static void initIDs() { }
    
}
