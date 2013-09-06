// FileInputStream.java, created Thu Jul  4  4:50:03 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.Common.java.io;

import joeq.Memory.HeapAddress;
import joeq.Runtime.SystemInterface;

/**
 * FileInputStream
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: FileInputStream.java,v 1.11 2004/03/09 21:57:34 jwhaley Exp $
 */
abstract class FileInputStream {
    
    private FileDescriptor fd;
    
    private void open(String name)
    throws java.io.FileNotFoundException {
        int fdnum = SystemInterface.file_open(name, SystemInterface._O_RDONLY | SystemInterface._O_BINARY, 0);
        if (fdnum == -1) throw new java.io.FileNotFoundException(name);
        this.fd.fd = fdnum;
    }
    public int read() throws java.io.IOException {
        byte[] b = new byte[1];
        int v = this.readBytes(b, 0, 1);
        if (v == -1) return -1;
        else if (v != 1) throw new java.io.IOException();
        return b[0]&0xFF;
    }
    private int readBytes(byte b[], int off, int len) throws java.io.IOException {
        return readBytes(b, off, len, this.fd);
    }
    // IBM JDK has this extra fd argument here.
    private int readBytes(byte b[], int off, int len, FileDescriptor fd) throws java.io.IOException {
        int fdnum = fd.fd;
        // check for index out of bounds/null pointer
        if (len < 0) throw new IndexOutOfBoundsException();
        // do bounds check
        byte b2 = b[off+len-1];
        // BUG in Sun's implementation, which we mimic here.  off=b.length and len=0 doesn't throw an error (?)
        if (off < 0) throw new IndexOutOfBoundsException();
        if (len == 0) return 0;
        HeapAddress start = (HeapAddress) HeapAddress.addressOf(b).offset(off);
        int result = SystemInterface.file_readbytes(fdnum, start, len);
        if (result == 0)
            return -1; // EOF
        if (result == -1)
            throw new java.io.IOException();
        return result;
    }
    public long skip(long n) throws java.io.IOException {
        int fdnum = this.fd.fd;
        long curpos = SystemInterface.file_seek(fdnum, 0, SystemInterface.SEEK_CUR);
        long result = SystemInterface.file_seek(fdnum, n, SystemInterface.SEEK_CUR);
        if (result == (long)-1)
            throw new java.io.IOException();
        return result-curpos;
    }
    public int available() throws java.io.IOException {
        int fdnum = this.fd.fd;
        if (fdnum == 0) { // stdin
            int result = SystemInterface.console_available();
            if (result == -1) throw new java.io.IOException();
            return result;
        } else {
            long curpos = SystemInterface.file_seek(fdnum, 0, SystemInterface.SEEK_CUR);
            if (curpos == (long)-1)
                throw new java.io.IOException();
            long endpos = SystemInterface.file_seek(fdnum, 0, SystemInterface.SEEK_END);
            if (endpos == (long)-1)
                throw new java.io.IOException();
            long result = SystemInterface.file_seek(fdnum, curpos, SystemInterface.SEEK_SET);
            if (result == (long)-1)
                throw new java.io.IOException();
            if (endpos-curpos > Integer.MAX_VALUE) return Integer.MAX_VALUE;
            else return (int)(endpos-curpos);
        }
    }
    public void close() throws java.io.IOException {
        int fdnum = this.fd.fd;
        int result = SystemInterface.file_close(fdnum);
        // Sun's "implementation" ignores errors on file close, allowing files to be closed multiple times.
        if (false && result != 0)
            throw new java.io.IOException();
    }
    private static void initIDs() { }
    
}
