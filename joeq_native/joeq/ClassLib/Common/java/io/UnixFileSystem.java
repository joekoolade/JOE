// UnixFileSystem.java, created Thu Jul  4  4:50:03 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.Common.java.io;

import joeq.Bootstrap.MethodInvocation;
import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Class;
import joeq.Class.jq_DontAlign;
import joeq.Class.jq_Method;
import joeq.Class.jq_NameAndDesc;
import joeq.Main.jq;
import joeq.Memory.Address;
import joeq.Memory.CodeAddress;
import joeq.Memory.HeapAddress;
import joeq.Runtime.SystemInterface;
import joeq.Runtime.Unsafe;
import joeq.Runtime.SystemInterface.ExternalLink;
import joeq.Runtime.SystemInterface.Library;
import jwutil.util.Assert;

/**
 * UnixFileSystem
 *
 * @author John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: UnixFileSystem.java,v 1.10 2004/09/30 03:35:32 joewhaley Exp $
 */
public abstract class UnixFileSystem {

    public long getLength(java.io.File file) {
        return SystemInterface.fs_stat_size(file.getPath());
    }

    public boolean delete(java.io.File file) {
        int res = SystemInterface.fs_remove(file.getPath());
        return res == 0;
    }

    public long getLastModifiedTime(java.io.File file) {
        long res = SystemInterface.fs_getfiletime(file.getPath());
        return res;
    }

    public boolean rename(java.io.File file, java.io.File file1) {
        int res = SystemInterface.fs_rename(file.getPath(), file1.getPath());
        return res == 0;
    }

    public String[] list(java.io.File file) {
        int dir = SystemInterface.fs_opendir(file.getPath());
        if (dir == 0) return null;
        String[] s = new String[16];
        int i;
        Address ptr;
        for (i=0; !(ptr=SystemInterface.fs_readdir(dir)).isNull(); ++i) {
            if (i == s.length) {
                String[] s2 = new String[s.length<<1];
                System.arraycopy(s, 0, s2, 0, s.length);
                s = s2;
            }
            s[i] = SystemInterface.fromCString(ptr.offset(SystemInterface.readdir_name_offset));
            if (s[i].equals(".") || s[i].equals("..")) --i;
        }
        SystemInterface.fs_closedir(dir);
        String[] ret = new String[i];
        System.arraycopy(s, 0, ret, 0, i);
        return ret;
    }

    public boolean createDirectory(java.io.File file) {
        int res = SystemInterface.fs_mkdir(file.getPath());
        return res == 0;
    }

    public boolean setLastModifiedTime(java.io.File file, long l) {
        int res = SystemInterface.fs_setfiletime(file.getPath(), l);
        return res != 0;
    }

    public String canonicalize(String s) throws java.io.IOException {
        // TODO.
        return s;
    }

    /* from bits/stat.h on Redhat 9 */
    public static class linux_stat extends SystemInterface.Stat implements jq_DontAlign {
        long /*dev_t    */ st_dev;      /* device */
        int __pad1;
        int  /*ino_t    */ st_ino;      /* inode */
        int  /*mode_t   */ st_mode;     /* protection */
        int  /*nlink_t  */ st_nlink;    /* number of hard links */
        int  /*uid_t    */ st_uid;      /* user ID of owner */
        int  /*gid_t    */ st_gid;      /* group ID of owner */
        long /*dev_t    */ st_rdev;     /* device type (if inode device) */
        int __pad2;
        int  /*off_t    */ st_size;     /* total size, in bytes */
        int  /*blksize_t*/ st_blksize;  /* blocksize for filesystem I/O */
        int  /*blkcnt_t */ st_blocks;   /* number of blocks allocated */
        int  /*time_t   */ st_atime;    /* time of last access */
        int __unused1;
        int  /*time_t   */ st_mtime;    /* time of last modification */
        int __unused2;
        int  /*time_t   */ st_ctime;    /* time of last change */
        int __unused3;
        int __unused4;
        int __unused5;
        
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("{st_dev="); sb.append(st_dev);
            sb.append(",st_ino="); sb.append(st_ino);
            sb.append(",st_mode="); sb.append(st_mode);
            sb.append(",st_nlink="); sb.append(st_nlink);
            sb.append(",st_uid="); sb.append(st_uid);
            sb.append(",st_gid="); sb.append(st_gid);
            sb.append(",st_rdev="); sb.append(st_rdev);
            sb.append(",st_size="); sb.append(st_size);
            sb.append(",st_blksize="); sb.append(st_blksize);
            sb.append(",st_blocks="); sb.append(st_blocks);
            sb.append(",st_atime="); sb.append(st_atime);
            sb.append(",st_mtime="); sb.append(st_mtime);
            sb.append(",st_ctime="); sb.append(st_ctime);
            sb.append('}');
            return sb.toString();
        }
    }
    /* Flags for st_mode field, from man page: */
    static final int S_IFMT = 0170000;  //bitmask for the file type bitfields
    static final int S_IFSOCK = 0140000;//socket
    static final int S_IFLNK = 0120000; //symbolic link
    static final int S_IFREG = 0100000; //regular file
    static final int S_IFBLK = 0060000; //block device
    static final int S_IFDIR = 0040000; //directory
    static final int S_IFCHR = 0020000; //character device
    static final int S_IFIFO = 0010000; //fifo
    static final int S_ISUID = 0004000; //set UID bit
    static final int S_ISGID = 0002000; //set GID bit (see below)
    static final int S_ISVTX = 0001000; //sticky bit (see below)
    static final int S_IRWXU = 00700;   //mask for file owner permissions
    static final int S_IRUSR = 00400;   //owner has read permission
    static final int S_IWUSR = 00200;   //owner has write permission
    static final int S_IXUSR = 00100;   //owner has execute permission
    static final int S_IRWXG = 00070;   //mask for group permissions
    static final int S_IRGRP = 00040;   //group has read permission
    static final int S_IWGRP = 00020;   //group has write permission
    static final int S_IXGRP = 00010;   //group has execute permission
    static final int S_IRWXO = 00007;   //mask for permissions for others (not in group)
    static final int S_IROTH = 00004;   //others have read permission
    static final int S_IWOTH = 00002;   //others have write permisson
    static final int S_IXOTH = 00001;   //others have execute permission

    // Copied from FileSystem.java
    public static final int BA_EXISTS    = 0x01;
    public static final int BA_REGULAR   = 0x02;
    public static final int BA_DIRECTORY = 0x04;
    public static final int BA_HIDDEN    = 0x08;

    public int getBooleanAttributes0(java.io.File file) {
        linux_stat s = new linux_stat();
        int res = SystemInterface.file_stat(file.getPath(), s);
        if (res != 0) return 0;
        int result = BA_EXISTS;
        if ((s.st_mode & S_IFDIR) != 0) result |= BA_DIRECTORY;
        if ((s.st_mode & S_IFREG) != 0) result |= BA_REGULAR;
        // hidden is handled by the caller.
        return result;
    }

    static final int R_OK = 4;
    static final int W_OK = 2;
    static final int X_OK = 1;
    static final int F_OK = 0;

    public static int access(String fn, int mode) {
        if (_access == null) return -1;
        byte[] filename = SystemInterface.toCString(fn);
        try {
            CodeAddress a = _access.resolve();
            Unsafe.pushArg(mode);
            HeapAddress b = HeapAddress.addressOf(filename);
            Unsafe.pushArgA(b);
            Unsafe.getThreadBlock().disableThreadSwitch();
            int rc = (int) Unsafe.invoke(a);
            Unsafe.getThreadBlock().enableThreadSwitch();
            return rc;
        } catch (Throwable x) { Assert.UNREACHABLE(); }
        return 0;
    }

    public static /*final*/ ExternalLink _access;

    public boolean checkAccess(java.io.File file, boolean flag) {
        int f = flag ? W_OK : R_OK;
        int res = access(file.getPath(), f);
        return res == 0;
    }
    /*
    public native boolean createFileExclusively(String s)
        throws IOException;
    public synchronized native boolean deleteOnExit(File file);
    public native boolean setReadOnly(File file);
    */

    static {
        if (jq.RunningNative) boot();
        else if (jq.on_vm_startup != null) {
            jq_Class c = (jq_Class) PrimordialClassLoader.loader.getOrCreateBSType("Ljava/io/UnixFileSystem;");
            jq_Method m = c.getDeclaredStaticMethod(new jq_NameAndDesc("boot", "()V"));
            MethodInvocation mi = new MethodInvocation(m, null);
            jq.on_vm_startup.add(mi);
        }
    }

    public static void boot() {
        Library c = SystemInterface.registerLibrary("libc.so.6");

        if (c != null) {
            _access = c.resolve("access");
        } else {
            _access = null;
        }

    }

    private static void initIDs() { }
    
    //public static final jq_Class _class = (jq_Class)PrimordialClassLoader.loader.getOrCreateBSType("Ljava/io/UnixFileSystem;");
    //public static final jq_Initializer _constructor = (jq_Initializer)_class.getOrCreateInstanceMethod("<init>", "()V");
}
