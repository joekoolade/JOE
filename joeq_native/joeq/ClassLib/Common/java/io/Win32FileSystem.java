// Win32FileSystem.java, created Thu Jul  4  4:50:03 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.Common.java.io;

import joeq.Memory.Address;
import joeq.Memory.HeapAddress;
import joeq.Runtime.SystemInterface;

/**
 * Win32FileSystem
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: Win32FileSystem.java,v 1.11 2004/03/09 21:57:34 jwhaley Exp $
 */
public abstract class Win32FileSystem {

    public java.lang.String canonicalize(java.lang.String s) throws java.io.IOException {
        // check for and eliminate wildcards.
        if ((s.indexOf('*')>=0) || (s.indexOf('?')>=0))
            throw new java.io.IOException("wildcards not allowed in file name: "+s);
        byte[] b = new byte[256];
        int r = SystemInterface.fs_fullpath(s, b);
        if (r == 0) throw new java.io.IOException("fullpath returned error on: "+s);
        java.lang.String res = SystemInterface.fromCString(HeapAddress.addressOf(b));
        int strlen = res.length();
        java.lang.StringBuffer result = new java.lang.StringBuffer(strlen);
        int curindex = 0;
        if (res.startsWith("\\\\")) {
            // trim trailing "\" on UNC name.
            //if (res.charAt(strlen-1) == '\\') { res = res.substring(0, strlen-1); --strlen; }
            curindex = res.indexOf('\\', 2);
            if (curindex == -1) throw new java.io.IOException("invalid UNC pathname: "+s);
            result.append(res.substring(0, curindex));
        } else if (res.charAt(1) == ':') {
            // change drive letter to upper case.
            result.append(Character.toUpperCase(res.charAt(0)));
            result.append(':');
            curindex = 2;
        }
        while (curindex < strlen) {
            result.append('\\');
            int next_idx = res.indexOf('\\', curindex);
            if (next_idx == -1) {
                result.append(res.substring(curindex));
                return result.toString();
            }
            java.lang.String sub = res.substring(curindex, next_idx);
            Address b3 = SystemInterface.fs_gettruename(sub);
            if (b3.isNull()) {
                // bail out and return what we have.
                result.append(res.substring(curindex));
                return result.toString();
            }
            result.append(SystemInterface.fromCString(b3));
            curindex = next_idx+1;
        }
        // path name ended in "\"
        return result.toString();
    }
    
    /* Constants for simple boolean attributes */
    public static final int BA_EXISTS    = 0x01;
    public static final int BA_REGULAR   = 0x02;
    public static final int BA_DIRECTORY = 0x04;
    public static final int BA_HIDDEN    = 0x08;
    public int getBooleanAttributes(java.io.File file) {
        int res = SystemInterface.fs_getfileattributes(file.getPath());
        if (res == -1) return 0;
        return BA_EXISTS |
               (((res & SystemInterface.FILE_ATTRIBUTE_DIRECTORY) != 0)?BA_DIRECTORY:BA_REGULAR) |
               (((res & SystemInterface.FILE_ATTRIBUTE_HIDDEN) != 0)?BA_HIDDEN:0);
    }

    public boolean checkAccess(java.io.File file, boolean flag) {
        int res = SystemInterface.fs_access(file.getPath(), flag?2:4);
        return res == 0;
    }

    public long getLastModifiedTime(java.io.File file) {
        long res = SystemInterface.fs_getfiletime(file.getPath());
        return res;
    }

    public long getLength(java.io.File file) {
        return SystemInterface.fs_stat_size(file.getPath());
    }
    
    public boolean delete(java.io.File file) {
        int res = SystemInterface.fs_remove(file.getPath());
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

    public boolean rename(java.io.File file, java.io.File file1) {
        int res = SystemInterface.fs_rename(file.getPath(), file1.getPath());
        return res == 0;
    }

    public boolean setLastModifiedTime(java.io.File file, long l) {
        int res = SystemInterface.fs_setfiletime(file.getPath(), l);
        return res != 0;
    }

    public boolean setReadOnly(java.io.File file) {
        int res = SystemInterface.fs_chmod(file.getPath(), SystemInterface._S_IREAD);
        return res == 0;
    }

    private static int listRoots0() {
        return SystemInterface.fs_getlogicaldrives();
    }
    
    private static void initIDs() { }
    
    //public static final jq_Class _class = (jq_Class)PrimordialClassLoader.loader.getOrCreateBSType("Ljava/io/Win32FileSystem;");
    //public static final jq_Initializer _constructor = (jq_Initializer)_class.getOrCreateInstanceMethod("<init>", "()V");
}
