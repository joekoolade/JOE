// RandomAccessFile.java, created Fri Aug 16 18:11:47 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.sun14_linux.java.io;

import joeq.Runtime.SystemInterface;

/**
 * RandomAccessFile
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: RandomAccessFile.java,v 1.6 2004/03/09 21:57:35 jwhaley Exp $
 */
public class RandomAccessFile {

    private FileDescriptor fd;
    
    private static final int O_RDONLY = 1;
    private static final int O_RDWR =   2;
    private static final int O_SYNC =   4;
    private static final int O_DSYNC =  8;

    public void open(java.lang.String name, int mode)
    throws java.io.FileNotFoundException {
        int flags = SystemInterface._O_BINARY;
        if ((mode & O_RDONLY) != 0) flags |= SystemInterface._O_RDONLY;
        if ((mode & O_RDWR) != 0) flags |= SystemInterface._O_RDWR | SystemInterface._O_CREAT;
        if ((mode & O_SYNC) != 0) {
            // todo: require that every update to the file's content or metadata be
            //       written synchronously to the underlying storage device
        }
        if ((mode & O_DSYNC) != 0) {
            // todo: require that every update to the file's content be written
            //       synchronously to the underlying storage device.
        }
        int fdnum = SystemInterface.file_open(name, flags, 0);
        if (fdnum == -1) throw new java.io.FileNotFoundException(name);
        this.fd.fd = fdnum;
    }

}
