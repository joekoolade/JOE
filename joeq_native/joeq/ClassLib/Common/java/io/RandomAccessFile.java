// RandomAccessFile.java, created Thu Jul  4  4:50:03 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.Common.java.io;

import joeq.Memory.HeapAddress;
import joeq.Runtime.SystemInterface;
import jwutil.util.Assert;

/**
 * RandomAccessFile
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: RandomAccessFile.java,v 1.13 2004/09/30 03:35:32 joewhaley Exp $
 */
public abstract class RandomAccessFile {

    private FileDescriptor fd;
    
    // question: should this be private?
    public void open(java.lang.String name, boolean writeable)
    throws java.io.FileNotFoundException {
        int flags = writeable?(SystemInterface._O_RDWR | SystemInterface._O_CREAT | SystemInterface._O_BINARY)
                             :(SystemInterface._O_RDONLY | SystemInterface._O_BINARY);
        int fdnum = SystemInterface.file_open(name, flags, 0);
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
    public void write(int b) throws java.io.IOException {
        int fdnum = this.fd.fd;
        int result = SystemInterface.file_writebyte(fdnum, b);
        if (result != 1)
            throw new java.io.IOException();
    }
    private void writeBytes(byte b[], int off, int len) throws java.io.IOException {
        writeBytes(b, off, len, this.fd);
    }
    // IBM JDK has this extra fd argument here.
    private void writeBytes(byte b[], int off, int len, FileDescriptor fd) throws java.io.IOException {
        int fdnum = fd.fd;
        // check for index out of bounds/null pointer
        if (len < 0) throw new IndexOutOfBoundsException();
        // do bounds check
        byte b2 = b[off+len-1];
        // BUG in Sun's implementation, which we mimic here.  off=b.length and len=0 doesn't throw an error (?)
        if (off < 0) throw new IndexOutOfBoundsException();
        if (len == 0) return;
        HeapAddress start = (HeapAddress) HeapAddress.addressOf(b).offset(off);
        int result = SystemInterface.file_writebytes(fdnum, start, len);
        if (result != len)
            throw new java.io.IOException();
    }
    public long getFilePointer() throws java.io.IOException {
        int fdnum = this.fd.fd;
        long curpos = SystemInterface.file_seek(fdnum, 0, SystemInterface.SEEK_CUR);
        if (curpos == (long)-1)
            throw new java.io.IOException();
        return curpos;
    }
    public void seek(long pos) throws java.io.IOException {
        if (pos < 0L)
            throw new java.io.IOException(pos+" < 0");
        int fdnum = this.fd.fd;
        long result = SystemInterface.file_seek(fdnum, pos, SystemInterface.SEEK_SET);
        if (result == (long)-1)
            throw new java.io.IOException();
    }
    public long length() throws java.io.IOException {
        int fdnum = this.fd.fd;
        long curpos = SystemInterface.file_seek(fdnum, 0, SystemInterface.SEEK_CUR);
        if (curpos == (long)-1)
            throw new java.io.IOException();
        long endpos = SystemInterface.file_seek(fdnum, 0, SystemInterface.SEEK_END);
        if (endpos == (long)-1)
            throw new java.io.IOException();
        long result = SystemInterface.file_seek(fdnum, curpos, SystemInterface.SEEK_SET);
        if (result == (long)-1)
            throw new java.io.IOException();
        return endpos;
    }
    public void setLength(long newLength) throws java.io.IOException {
        Assert.TODO();
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
