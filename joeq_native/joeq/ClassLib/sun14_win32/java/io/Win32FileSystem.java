// Win32FileSystem.java, created Fri Apr  5 18:36:41 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.sun14_win32.java.io;

import joeq.Memory.HeapAddress;
import joeq.Runtime.SystemInterface;

/*
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: Win32FileSystem.java,v 1.11 2004/03/09 21:57:36 jwhaley Exp $
 */
public abstract class Win32FileSystem {

    public native java.lang.String canonicalize(java.lang.String s) throws java.io.IOException;
    public native int getBooleanAttributes(java.io.File f);
    public native boolean checkAccess(java.io.File f, boolean b);
    public native long getLastModifiedTime(java.io.File f);
    public native long getLength(java.io.File f);
    public native boolean createFileExclusively(java.lang.String s) throws java.io.IOException;
    public native boolean delete(java.io.File f);
    public synchronized native boolean deleteOnExit(java.io.File f);
    public native java.lang.String[] list(java.io.File f);
    public native boolean createDirectory(java.io.File f);
    public native boolean rename(java.io.File f1, java.io.File f2);
    public native boolean setLastModifiedTime(java.io.File f, long t);
    public native boolean setReadOnly(java.io.File f);
    
    // gets the current directory on the named drive.
    String getDriveDirectory(int i) {
        byte[] b = new byte[256];
        int result = SystemInterface.fs_getdcwd(i, b);
        if (result == 0) throw new InternalError();
        String res = SystemInterface.fromCString(HeapAddress.addressOf(b));
        // skip "C:"
        if (res.charAt(1) == ':') return res.substring(2);
        else return res;
    }
    
    
}
