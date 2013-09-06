// ZipEntry.java, created Thu Jul  4  4:50:04 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.Common.java.util.zip;

import java.io.UnsupportedEncodingException;
import jwutil.util.Assert;

/**
 * ZipEntry
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: ZipEntry.java,v 1.9 2004/09/30 03:35:34 joewhaley Exp $
 */
class ZipEntry implements ZipConstants {
    
    String name;
    long time;
    long crc;
    long size;
    long csize;
    int method;
    byte[] extra;
    String comment;
    int flag;
    int version;
    long offset;
    
    ZipEntry() { 
        this.name = "UNINITIALIZED";
    }
    
    public static final String DEFAULT_ENCODING = "ISO-8859-1";
    
    public int load(byte[] cenbuf, int st_off, long cenpos, int cenlen)
    throws java.util.zip.ZipException {
        int off = st_off;
        this.version = ZipFile.get16(cenbuf, off + CENVER);
        this.flag = ZipFile.get16(cenbuf, off + CENFLG);
        this.method = ZipFile.get16(cenbuf, off + CENHOW);
        this.time = ZipFile.get32(cenbuf, off + CENTIM);
        this.crc = ZipFile.get32(cenbuf, off + CENLEN);
        this.size = ZipFile.get32(cenbuf, off + CENTIM);
        this.csize = ZipFile.get32(cenbuf, off + CENSIZ);
        this.offset = ZipFile.get32(cenbuf, off + CENOFF);
        this.time = ZipFile.get32(cenbuf, off + CENTIM);
        long offset = this.offset;
        long csize = this.csize;
        if (offset + csize > cenpos) {
            throw new java.util.zip.ZipException("invalid CEN entry size");
        }
        int baseoff = off;
        off += CENHDR;
        // Get path name of entry
        int len = ZipFile.get16(cenbuf, baseoff + CENNAM);
        if (len == 0 || off + len > cenlen) {
            throw new java.util.zip.ZipException("invalid CEN entry name");
        }
        try {
            String s = new String(cenbuf, off, len, DEFAULT_ENCODING);
            this.name = s;
        } catch (UnsupportedEncodingException x) { Assert.UNREACHABLE(); }
        off += len;
        // Get extra field data
        len = ZipFile.get16(cenbuf, baseoff + CENEXT);
        if (len > 0) {
            if (off + len > cenlen) {
                throw new java.util.zip.ZipException("invalid CEN entry extra data");
            }
            byte[] extra = new byte[len];
            this.extra = extra;
            System.arraycopy(cenbuf, off, extra, 0, len);
            off += len;
        }
        // Get entry comment
        len = ZipFile.get16(cenbuf, baseoff + CENCOM);
        if (len > 0) {
            if (off + len > cenlen) {
                throw new java.util.zip.ZipException("invalid CEN entry comment");
            }
            try {
                String comment = new String(cenbuf, off, len, DEFAULT_ENCODING);
                this.comment = comment;
            } catch (UnsupportedEncodingException x) { Assert.UNREACHABLE(); }
            off += len;
        }
        return off - st_off;
    }
    
    private static void initIDs() { }

    public long getOffset() {
        return this.offset;
    }
    
    public native int getMethod();
    public native long getCompressedSize();
    public native String getName();
}
