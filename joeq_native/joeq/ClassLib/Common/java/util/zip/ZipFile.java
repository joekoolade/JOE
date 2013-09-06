// ZipFile.java, created Thu Jul  4  4:50:04 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.Common.java.util.zip;

import java.util.Enumeration;
import java.util.Hashtable;
import java.io.RandomAccessFile;
import jwutil.util.Assert;

/**
 * ZipFile
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: ZipFile.java,v 1.21 2004/09/30 03:35:34 joewhaley Exp $
 */
public abstract class ZipFile implements ZipConstants {
    public static final boolean TRACE = false;

    // existing instance fields
    private String name;

    // additional instance fields
    private RandomAccessFile raf;
    private Hashtable entries;
    private long cenpos;
    private long pos;

    private static void initIDs() {
    }
    /*
    public static void bootstrap_init(java.util.zip.ZipFile dis, String name) throws java.io.IOException {
        jq_Class _class = (jq_Class)PrimordialClassLoader.loader.getOrCreateBSType("Ljava/util/zip/ZipFile;");
        jq_InstanceField _name = _class.getOrCreateInstanceField("name", "Ljava/lang/String;");
        Reflection.putfield_A(dis, _name, name);
        jq_InstanceField _raf = _class.getOrCreateInstanceField("raf", "Ljava/io/RandomAccessFile;");
        RandomAccessFile raf = new RandomAccessFile(name, "r");
        Reflection.putfield_A(dis, _raf, raf);
        //this.readCEN();
    }
    */
    public native void __init__(java.lang.String name)
        throws java.io.IOException;
    public ZipFile(java.lang.String name) throws java.io.IOException {
        this.__init__(name);
    }
    public ZipFile(java.io.File file, int mode) throws java.io.IOException {
        this(file.getPath());
        // delete mode not yet supported.
        Assert._assert(mode == java.util.zip.ZipFile.OPEN_READ);
    }
    public java.util.zip.ZipEntry getEntry(String name) {
        if (TRACE)
            System.out.println(this +": getting entry " + name);
        Hashtable entries = this.entries;
        if (entries == null) {
            // not yet initialized.
            return null;
        }
        return (java.util.zip.ZipEntry) entries.get(name);
    }
    public Enumeration entries() {
        Hashtable entries = this.entries;
        return entries.elements();
    }
    public int size() {
        Hashtable entries = this.entries;
        if (entries == null) {
            // not yet initialized.
            return 0;
        }
        if (TRACE)
            System.out.println(this +": getting size = " + entries.size());
        return entries.size();
    }
    public void close() throws java.io.IOException {
        if (TRACE)
            System.out.println(this +": closing file");
        RandomAccessFile raf = this.raf;
        if (raf != null) {
            raf.close();
            this.raf = null;
        }
        entries = null;
    }
    private java.io.InputStream getInputStream(java.lang.String name)
        throws java.io.IOException {
        java.lang.Object o = getEntry(name);
        return getInputStream((ZipEntry) o);
    }
    public java.io.InputStream getInputStream(ZipEntry ze)
        throws java.io.IOException {
        if (ze == null)
            return null;
        java.io.InputStream in = new ZipFileInputStream(this, ze);
        if (TRACE)
            System.out.println(
                this +": getting input stream for " + ze + " = " + in);
        switch (ze.getMethod()) {
            case java.util.zip.ZipEntry.STORED :
                return in;
            case java.util.zip.ZipEntry.DEFLATED :
                java.util.zip.Inflater inflater;
                inflater = this.getInflater();
                if (TRACE)
                    System.out.println(this +": using inflater " + inflater);
                // Overridden InflaterInputStream to add a zero byte at the end of the stream.
                return new InflaterInputStreamWrapper(this, in, inflater);
            default :
                throw new java.util.zip.ZipException(
                    "invalid compression method");
        }
    }
    private native java.util.zip.Inflater getInflater();
    private native void releaseInflater(java.util.zip.Inflater inf);
    void releaseInflater0(java.util.zip.Inflater inf) {
        releaseInflater(inf);
    }

    private static class ZipFileInputStream extends java.io.InputStream {
        private ZipFile zf;
        private ZipEntry ze;
        private long pos;
        private long count;

        public ZipFileInputStream(ZipFile zf, ZipEntry ze)
            throws java.io.IOException {
            this.zf = zf;
            this.ze = ze;
            this.readLOC();
        }

        public int read(byte b[], int off, int len)
            throws java.io.IOException {
            long count = this.count;
            if (TRACE)
                System.out.println(
                    this +": reading off=" + off + " len=" + len);
            if (count == 0) {
                return -1;
            }
            if (len > count) {
                len = (int) Math.min(count, Integer.MAX_VALUE);
            }
            long pos = this.pos;
            ZipFile zf = this.zf;
            len = zf.read(pos, b, off, len);
            if (len == -1) {
                throw new java.util.zip.ZipException("premature EOF");
            }
            this.pos = pos + len;
            this.count = count - len;
            return len;
        }

        public int read() throws java.io.IOException {
            long count = this.count;
            if (count == 0) {
                return -1;
            }
            ZipFile zf = this.zf;
            long pos = this.pos;
            if (TRACE)
                System.out.println(this +": reading pos=" + pos);
            int n = zf.read(pos);
            if (n == -1) {
                throw new java.util.zip.ZipException("premature EOF");
            }
            this.pos = pos + 1;
            this.count = count - 1;
            if (TRACE)
                System.out.println(this +": new pos=" + (pos + 1));
            if (TRACE)
                System.out.println(this +": new count=" + (count - 1));
            return n;
        }

        public long skip(long n) {
            long count = this.count;
            if (n > count) {
                n = count;
            }
            if (TRACE)
                System.out.println(this +": skipping " + n);
            long pos = this.pos;
            this.pos = pos + n;
            this.count = count - n;
            if (TRACE)
                System.out.println(this +": new pos=" + (pos + n));
            if (TRACE)
                System.out.println(this +": new count=" + (count - n));
            return n;
        }

        public int available() {
            long count = this.count;
            return (int) Math.min(count, Integer.MAX_VALUE);
        }

        private void cleanup() {
            // nothing to do.
        }

        public void close() {
            cleanup();
        }

        private void readLOC() throws java.io.IOException {
            // Read LOC header and check signature
            byte locbuf[] = new byte[LOCHDR];
            ZipFile zf = this.zf;
            ZipEntry ze = this.ze;
            long offset = ze.getOffset();
            if (TRACE)
                System.out.println(this +": reading LOC, offset=" + offset);
            zf.read(offset, locbuf, 0, LOCHDR);
            if (ZipFile.get32(locbuf, 0) != LOCSIG) {
                throw new java.util.zip.ZipException(
                    "invalid LOC header signature");
            }
            // Get length and position of entry data
            long count = ze.getCompressedSize();
            this.count = count;
            if (TRACE)
                System.out.println(this +": count=" + count);
            long pos =
                ze.getOffset()
                    + LOCHDR
                    + ZipFile.get16(locbuf, LOCNAM)
                    + ZipFile.get16(locbuf, LOCEXT);
            this.pos = pos;
            if (TRACE)
                System.out.println(this +": pos=" + pos);
            long cenpos = zf.cenpos;
            if (TRACE)
                System.out.println(this +": cenpos=" + cenpos);
            if (pos + count > cenpos) {
                throw new java.util.zip.ZipException(
                    "invalid LOC header format");
            }
        }
    }
    private /*synchronized*/
    int read(long pos, byte b[], int off, int len) throws java.io.IOException {
        if (TRACE)
            System.out.println(
                this
                    + ": reading file pos="
                    + pos
                    + " off="
                    + off
                    + " len="
                    + len);
        RandomAccessFile raf = this.raf;
        if (raf == null)
            throw new java.io.IOException();
        if (pos != this.pos) {
            raf.seek(pos);
        }
        int n = raf.read(b, off, len);
        if (TRACE)
            System.out.println(this +": number read=" + n);
        if (TRACE)
            System.out.println(this +": current pos=" + (pos + n));
        if (n > 0) {
            this.pos = pos + n;
        }
        return n;
    }
    private /*synchronized*/
    int read(long pos) throws java.io.IOException {
        if (TRACE)
            System.out.println(this +": read pos=" + pos);
        RandomAccessFile raf = this.raf;
        if (raf == null)
            throw new java.io.IOException();
        if (pos != this.pos) {
            if (TRACE)
                System.out.println(this +": seeking to " + pos);
            raf.seek(pos);
        }
        int n = raf.read();
        if (TRACE)
            System.out.println(this +": byte read=" + n);
        if (n > 0) {
            this.pos = pos + 1;
        }
        return n;
    }

    private void readCEN() throws java.io.IOException {
        if (TRACE)
            System.out.println(this +": reading CEN...");
        // Find and seek to beginning of END header
        long endpos = this.findEND();
        if (TRACE)
            System.out.println(this +": endpos=" + endpos);
        // Read END header and check signature
        byte[] endbuf = new byte[ENDHDR];
        RandomAccessFile raf = this.raf;
        raf.readFully(endbuf);
        if (get32(endbuf, 0) != ENDSIG) {
            throw new java.util.zip.ZipException(
                "invalid END header signature");
        }
        // Get position and length of central directory
        long cenpos = get32(endbuf, ENDOFF);
        if (TRACE)
            System.out.println(this +": cenpos=" + cenpos);
        this.cenpos = cenpos;
        int cenlen = (int) get32(endbuf, ENDSIZ);
        if (TRACE)
            System.out.println(this +": cenlen=" + cenlen);
        if (cenpos + cenlen != endpos) {
            throw new java.util.zip.ZipException("invalid END header format");
        }
        // Get total number of entries
        int nent = get16(endbuf, ENDTOT);
        if (TRACE)
            System.out.println(this +": nent=" + nent);
        if (nent * CENHDR > cenlen) {
            throw new java.util.zip.ZipException("invalid END header format");
        }
        // Check number of drives
        if (get16(endbuf, ENDSUB) != nent) {
            throw new java.util.zip.ZipException(
                "cannot have more than one drive");
        }
        // Seek to first CEN record and read central directory
        raf.seek(cenpos);
        byte cenbuf[] = new byte[cenlen];
        raf.readFully(cenbuf);
        // Scan entries in central directory and build lookup table.
        Hashtable entries = new Hashtable(nent);
        this.entries = entries;
        for (int off = 0; off < cenlen;) {
            // Check CEN header signature
            if (get32(cenbuf, off) != CENSIG) {
                throw new java.util.zip.ZipException(
                    "invalid CEN header signature");
            }
            ZipEntry e = new ZipEntry();
            int entrysize = e.load(cenbuf, off, cenpos, cenlen);
            off += entrysize;
            if (TRACE)
                System.out.println(
                    this +": entrysize=" + entrysize + " offset=" + off);
            // Add entry to the hash table of entries
            String name = e.getName();
            entries.put(name, e);
        }
        if (false) { // zip files can have duplicate entries, so we disable this check.
            // Make sure we got the right number of entries
            if (entries.size() != nent) {
                throw new java.util.zip.ZipException(
                    "invalid CEN header format");
            }
        }
    }

    private static final int INBUFSIZ = 64;

    private long findEND() throws java.io.IOException {
        if (raf == null)
            throw new java.io.IOException();
        // Start searching backwards from end of file
        long len = raf.length();
        if (TRACE)
            System.out.println(this +": findEND len=" + len);
        raf.seek(len);
        // Set limit on how far back we need to search. The END header
        // must be located within the last 64K bytes of the raf.
        long markpos = Math.max(0, len - 0xffff);
        // Search backwards INBUFSIZ bytes at a time from end of file
        // stopping when the END header signature has been found. Since
        // the signature may straddle a buffer boundary, we need to stash
        // the first 4-1 bytes of the previous record at the end of
        // the current record so that the search may overlap.
        byte buf[] = new byte[INBUFSIZ + 4];
        long pos = 0L; // Reflection.getfield_L(dis, _pos);
        for (pos = len; pos > markpos;) {
            int n = Math.min((int) (pos - markpos), INBUFSIZ);
            pos -= n;
            raf.seek(pos);
            raf.readFully(buf, 0, n);
            while (--n > 0) {
                if (get32(buf, n) == ENDSIG) {
                    // Could be END header, but we need to make sure that
                    // the record extends to the end of the raf.
                    long endpos = pos + n;
                    if (len - endpos < ENDHDR) {
                        continue;
                    }
                    raf.seek(endpos);
                    byte endbuf[] = new byte[ENDHDR];
                    raf.readFully(endbuf);
                    int comlen = get16(endbuf, ENDCOM);
                    if (TRACE)
                        System.out.println(this +": findEND comlen=" + comlen);
                    if (endpos + ENDHDR + comlen != len) {
                        continue;
                    }
                    // This is definitely the END record, so position
                    // the file pointer at the header and return.
                    raf.seek(endpos);
                    this.pos = endpos;
                    if (TRACE)
                        System.out.println(
                            this +": findEND pos=endpos=" + endpos);
                    return endpos;
                }
            }
        }
        throw new java.util.zip.ZipException(
            "not a ZIP file (END header not found)");
    }

    /*
     * Fetch unsigned 16-bit value from byte array at specified offset.
     * The bytes are assumed to be in Intel (little-endian) byte order.
     */
    static final int get16(byte b[], int off) {
        return (b[off] & 0xff) | ((b[off + 1] & 0xff) << 8);
    }

    /*
     * Fetch unsigned 32-bit value from byte array at specified offset.
     * The bytes are assumed to be in Intel (little-endian) byte order.
     */
    static final long get32(byte b[], int off) {
        return get16(b, off) | ((long) get16(b, off + 2) << 16);
    }

    // native method that is not used by this implementation.
    private static void freeEntry(long a, long b) {
        Assert.UNREACHABLE();
    }
}
