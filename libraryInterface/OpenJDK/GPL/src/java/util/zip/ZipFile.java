/*
 * Copyright (c) 1995, 2006, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.util.zip;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.io.IOException;
import java.io.EOFException;
import java.io.File;
import java.util.Vector;

import org.jikesrvm.VM;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * This class is used to read entries from a zip file.
 *
 * <p> Unless otherwise noted, passing a <tt>null</tt> argument to a constructor
 * or method in this class will cause a {@link NullPointerException} to be
 * thrown.
 *
 * @author      David Connelly
 */
public
class ZipFile implements ZipConstants {
    private long jzfile;           // address of jzfile data
    private final String name;     // zip file name
    private final int total;       // total number of entries
    private final boolean locsig;  // if zip file starts with LOCSIG (usually true)
    private boolean closeRequested;

    private int endLoc = 0;
    private int zipEntries;
    private int centSize;
    private int centHeader;
    private int zipCommentSize;
    private static final int STORED = ZipEntry.STORED;
    private static final int DEFLATED = ZipEntry.DEFLATED;
    private static final int END_HDR_SIZE = 22;
    private FileHeaderEntry fhEntries[];
    
    /**
     * Mode flag to open a zip file for reading.
     */
    public static final int OPEN_READ = 0x1;

    /**
     * Mode flag to open a zip file and mark it for deletion.  The file will be
     * deleted some time between the moment that it is opened and the moment
     * that it is closed, but its contents will remain accessible via the
     * <tt>ZipFile</tt> object until either the close method is invoked or the
     * virtual machine exits.
     */
    public static final int OPEN_DELETE = 0x4;

    /**
     * Opens a zip file for reading.
     *
     * <p>First, if there is a security
     * manager, its <code>checkRead</code> method
     * is called with the <code>name</code> argument
     * as its argument to ensure the read is allowed.
     *
     * @param name the name of the zip file
     * @throws ZipException if a ZIP format error has occurred
     * @throws IOException if an I/O error has occurred
     * @throws SecurityException if a security manager exists and its
     *         <code>checkRead</code> method doesn't allow read access to the file.
     * @see SecurityManager#checkRead(java.lang.String)
     */
    public ZipFile(String name) throws IOException {
        this(new File(name), OPEN_READ);
    }

    /**
     * Opens a new <code>ZipFile</code> to read from the specified
     * <code>File</code> object in the specified mode.  The mode argument
     * must be either <tt>OPEN_READ</tt> or <tt>OPEN_READ | OPEN_DELETE</tt>.
     *
     * <p>First, if there is a security manager, its <code>checkRead</code>
     * method is called with the <code>name</code> argument as its argument to
     * ensure the read is allowed.
     *
     * @param file the ZIP file to be opened for reading
     * @param mode the mode in which the file is to be opened
     * @throws ZipException if a ZIP format error has occurred
     * @throws IOException if an I/O error has occurred
     * @throws SecurityException if a security manager exists and
     *         its <code>checkRead</code> method
     *         doesn't allow read access to the file,
     *         or its <code>checkDelete</code> method doesn't allow deleting
     *         the file when the <tt>OPEN_DELETE</tt> flag is set.
     * @throws IllegalArgumentException if the <tt>mode</tt> argument is invalid
     * @see SecurityManager#checkRead(java.lang.String)
     * @since 1.3
     */
    public ZipFile(File file, int mode) throws IOException {
        if (((mode & OPEN_READ) == 0) ||
            ((mode & ~(OPEN_READ | OPEN_DELETE)) != 0)) {
            throw new IllegalArgumentException("Illegal mode: 0x"+
                                               Integer.toHexString(mode));
        }
        String name = file.getPath();
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkRead(name);
            if ((mode & OPEN_DELETE) != 0) {
                sm.checkDelete(name);
            }
        }
        jzfile = open(name, mode, file.lastModified());

        this.name = name;
        this.total = getTotal(jzfile);
        this.locsig = startsWithLOC(jzfile);
    }

    public ZipFile(byte buffer[]) throws IOException
    {
        name = "";
        
        /*
         * Check for LOC header
         */
        if(buffer[0]=='P' && buffer[1]=='K' && buffer[2]==3 && buffer[3]==4)
        {
        	locsig = true;
        }
        else
        {
        	locsig = false;
        }
        findEndSig(buffer);
        total = buffer.length;
    }
    
    /*
     * Is this a central directory file header
     */
    private boolean isFileHeader(int signature)
    {
    	return signature == CENSIG;
    }
    private void findEndSig(byte buf[]) throws GZIPException
    {
        int offset;
        /*
         * quick check for 'PK' local signature
         */
        if(buf[0] != 'P' && buf[1] != 'K') throw new GZIPException("Not a ZIP/JAR file");
        
        /*
         * Locate the End Central Directory signature. Will scan the last 50 end blocks of
         * the zip file for the End signature
         */
        int block = 0;
findEndSig:
        for(block=1; block < 50; block++)
        {
            offset=buf.length - (ENDHDR * block);
            int endScan = offset + ENDHDR;
            /*
             * Scan for the END signature
             */
            VM.sysWrite("scanning from ", offset);
            VM.sysWriteln(" to ", endScan);
            for(; offset < endScan; offset++)
            {
                if(buf[offset]=='P' &&
                   buf[offset+1]=='K' &&
                   buf[offset+2]=='\005' &&
                   buf[offset+3]=='\006')
                {
                    endLoc = offset;
                    break findEndSig;
                }
            }
        }
        if(endLoc == 0) throw new GZIPException("Not End Location found");
        /*
         * Found the End signature. Now lets fill in some information
         */
//        VM.sysWriteln("endloc ", endLoc);
        ByteBuffer endBuf = ByteBuffer.wrap(buf, endLoc, ENDHDR);
//        for(int i=0; i < ENDHDR; i++)
//        {
//            VM.writeHex(buf[endLoc+i] & 0xFF); VM.sysWrite(' ');
//        }
//        VM.sysWriteln();
//        for(int i=0; i < ENDHDR; i++)
//        {
//            VM.writeHex(endBuf.get(i) & 0xFF); VM.sysWrite(' ');
//        }
//        VM.sysWriteln();
//        VM.sysWriteln("array offset ", endBuf.arrayOffset());
        endBuf.order(ByteOrder.LITTLE_ENDIAN);
        zipEntries = endBuf.getShort(ENDTOT) & 0xFFFF;
        centSize = endBuf.getInt(ENDSIZ);
        centHeader = endBuf.getInt(ENDOFF);
        zipCommentSize = endBuf.getShort(ENDCOM);
        VM.sysWrite("Entries ", zipEntries);
        VM.sysWrite(" CDsize "); VM.writeHex(centSize);
        VM.sysWrite(" CDoffset "); VM.writeHex(centHeader);
        VM.sysWriteln(" comment size ", zipCommentSize);
        ByteBuffer centralDir = ByteBuffer.wrap(buf, centHeader, centSize);
        centralDir.order(ByteOrder.LITTLE_ENDIAN);
        fhEntries = new FileHeaderEntry[zipEntries];
        
        /*
         * Read in central directory
         */
        ByteBuffer cdBuf = ByteBuffer.wrap(buf, centHeader, centSize);
        cdBuf.order(ByteOrder.LITTLE_ENDIAN);
        int entryPos = 0;
        int zipEntry = 0;
        for(; entryPos < centSize; zipEntry++)
        {
        	int signature = cdBuf.getInt(entryPos);
        	if(isFileHeader(signature)==false)
        	{
        		VM.sysWriteln("Bad file header:", entryPos);
        		VM.sysWriteln(" ", entryPos+centHeader);
        		continue;
        	}
        	
        	FileHeaderEntry entry = new FileHeaderEntry();
        	entry.madeVersion = cdBuf.getShort(entryPos+CENVEM);
        	entry.zipVersion = cdBuf.getShort(entryPos+CENVER);
        	entry.flags = cdBuf.getShort(entryPos+CENFLG);
        	entry.compressionMethod = cdBuf.getShort(entryPos+CENHOW);
        	entry.time = cdBuf.getShort(entryPos+CENTIM);
        	entry.date = cdBuf.getShort(entryPos+CENDAT);
        	entry.crc = cdBuf.getShort(entryPos+CENCRC);
        	entry.compressedSize = cdBuf.getInt(entryPos+CENSIZ);
        	entry.uncompressedSize = cdBuf.getInt(entryPos+CENLEN);
        	entry.nameLength = cdBuf.getShort(entryPos+CENNAM);
        	entry.extraFieldLength = cdBuf.getShort(entryPos+CENEXT);
        	entry.commentLength = cdBuf.getShort(entryPos+CENCOM);
        	entry.disk = cdBuf.getShort(entryPos+CENDSK);
        	entry.intAttr = cdBuf.getShort(entryPos+CENATT);
        	entry.extAttr = cdBuf.getInt(entryPos+CENATX);
        	entry.offset = cdBuf.getInt(entryPos+CENOFF);
        	if(entry.nameLength > 0) 
        	{
        		entry.fileName = new String(buf, centHeader+entryPos+CENHDR, entry.nameLength);
        	}
        	if(entry.extraFieldLength > 0)
        	{
        		entry.extraField = new String(buf, centHeader+entryPos+CENHDR, entry.extraFieldLength);
        	}
        	if(entry.commentLength > 0)
        	{
        		entry.comment = new String(buf, centHeader+entryPos+CENHDR, entry.commentLength);
        	}
        	entryPos += entry.entrySize();
//        	VM.sysWriteln(entry.fileName);
        	fhEntries[zipEntry] = entry;
        }
    }
    static {
//        sun.misc.SharedSecrets.setJavaUtilZipFileAccess(
//            new sun.misc.JavaUtilZipFileAccess() {
//                public boolean startsWithLocHeader(ZipFile zip) {
//                    return zip.startsWithLocHeader();
//                }
//             }
//        );
    }

    /**
     * Returns {@code true} if, and only if, the zip file begins with {@code
     * LOCSIG}.
     */
    private boolean startsWithLocHeader() {
        return locsig;
    }

    private static long open(String name, int mode, long lastModified)
    {
        return 0;
    }
    private static int getTotal(long jzfile)
    {
        return 0;
    }
    private static boolean startsWithLOC(long jzfile)
    {
        return false;
    }


    /**
     * Opens a ZIP file for reading given the specified File object.
     * @param file the ZIP file to be opened for reading
     * @throws ZipException if a ZIP error has occurred
     * @throws IOException if an I/O error has occurred
     */
    public ZipFile(File file) throws ZipException, IOException {
        this(file, OPEN_READ);
    }

    /**
     * Returns the zip file entry for the specified name, or null
     * if not found.
     *
     * @param name the name of the entry
     * @return the zip file entry, or null if not found
     * @throws IllegalStateException if the zip file has been closed
     */
    public ZipEntry getEntry(String name) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        long jzentry = 0;
        synchronized (this) {
            ensureOpen();
            jzentry = getEntry(jzfile, name, true);
            if (jzentry != 0) {
                ZipEntry ze = new ZipEntry(name, jzentry);
                freeEntry(jzfile, jzentry);
                return ze;
            }
        }
        return null;
    }

    private static long getEntry(long jzfile, String name,  boolean addSlash)
    {
        return 0;
    }

    // freeEntry releases the C jzentry struct.
    private static void freeEntry(long jzfile, long jzentry) {}

    /**
     * Returns an input stream for reading the contents of the specified
     * zip file entry.
     *
     * <p> Closing this ZIP file will, in turn, close all input
     * streams that have been returned by invocations of this method.
     *
     * @param entry the zip file entry
     * @return the input stream for reading the contents of the specified
     * zip file entry.
     * @throws ZipException if a ZIP format error has occurred
     * @throws IOException if an I/O error has occurred
     * @throws IllegalStateException if the zip file has been closed
     */
    public InputStream getInputStream(ZipEntry entry) throws IOException {
        return getInputStream(entry.name);
    }

    /**
     * Returns an input stream for reading the contents of the specified
     * entry, or null if the entry was not found.
     */
    private InputStream getInputStream(String name) throws IOException {
        if (name == null) {
            throw new NullPointerException("name");
        }
        long jzentry = 0;
        ZipFileInputStream in = null;
        synchronized (this) {
            ensureOpen();
            jzentry = getEntry(jzfile, name, false);
            if (jzentry == 0) {
                return null;
            }

            in = new ZipFileInputStream(jzentry);

        }
        final ZipFileInputStream zfin = in;
        switch (getMethod(jzentry)) {
        case STORED:
            return zfin;
        case DEFLATED:
            // MORE: Compute good size for inflater stream:
            long size = getSize(jzentry) + 2; // Inflater likes a bit of slack
            if (size > 65536) size = 8192;
            if (size <= 0) size = 4096;
            return new InflaterInputStream(zfin, getInflater(), (int)size) {
                private boolean isClosed = false;

                public void close() throws IOException {
                    if (!isClosed) {
                         releaseInflater(inf);
                        this.in.close();
                        isClosed = true;
                    }
                }
                // Override fill() method to provide an extra "dummy" byte
                // at the end of the input stream. This is required when
                // using the "nowrap" Inflater option.
                protected void fill() throws IOException {
                    if (eof) {
                        throw new EOFException(
                            "Unexpected end of ZLIB input stream");
                    }
                    len = this.in.read(buf, 0, buf.length);
                    if (len == -1) {
                        buf[0] = 0;
                        len = 1;
                        eof = true;
                    }
                    inf.setInput(buf, 0, len, false);
                }
                private boolean eof;

                public int available() throws IOException {
                    if (isClosed)
                        return 0;
                    long avail = zfin.size() - inf.getTotalOut();
                    return avail > (long) Integer.MAX_VALUE ?
                        Integer.MAX_VALUE : (int) avail;
                }
            };
        default:
            throw new ZipException("invalid compression method");
        }
    }

    private static int getMethod(long jzentry)
    {
        return 0;
    }

    /*
     * Gets an inflater from the list of available inflaters or allocates
     * a new one.
     */
    private Inflater getInflater() throws IOException {
        synchronized (inflaters) {
            int size = inflaters.size();
            if (size > 0) {
                Inflater inf = (Inflater)inflaters.remove(size - 1);
                inf.init();
                return inf;
            } else {
                try 
                {
                    return new Inflater(true);
                }
                catch(GZIPException e)
                {
                    throw new IOException(e);
                }
            }
        }
    }

    /*
     * Releases the specified inflater to the list of available inflaters.
     */
    private void releaseInflater(Inflater inf) {
        synchronized (inflaters) {
            inflaters.add(inf);
        }
    }

    // List of available Inflater objects for decompression
    private Vector inflaters = new Vector();

    /**
     * Returns the path name of the ZIP file.
     * @return the path name of the ZIP file
     */
    public String getName() {
        return name;
    }

    /**
     * Returns an enumeration of the ZIP file entries.
     * @return an enumeration of the ZIP file entries
     * @throws IllegalStateException if the zip file has been closed
     */
    public Enumeration<? extends ZipEntry> entries() {
        ensureOpen();
        return new Enumeration<ZipEntry>() {
                private int i = 0;
                public boolean hasMoreElements() {
                    synchronized (ZipFile.this) {
                        ensureOpen();
                        return i < total;
                    }
                }
                public ZipEntry nextElement() throws NoSuchElementException {
                    synchronized (ZipFile.this) {
                        ensureOpen();
                        if (i >= total) {
                            throw new NoSuchElementException();
                        }
                        long jzentry = getNextEntry(jzfile, i++);
                        if (jzentry == 0) {
                            String message;
                            if (closeRequested) {
                                message = "ZipFile concurrently closed";
                            } else {
                                message = getZipMessage(ZipFile.this.jzfile);
                            }
                            throw new ZipError("jzentry == 0" +
                                               ",\n jzfile = " + ZipFile.this.jzfile +
                                               ",\n total = " + ZipFile.this.total +
                                               ",\n name = " + ZipFile.this.name +
                                               ",\n i = " + i +
                                               ",\n message = " + message
                                );
                        }
                        ZipEntry ze = new ZipEntry(jzentry);
                        freeEntry(jzfile, jzentry);
                        return ze;
                    }
                }
            };
    }

    private static long getNextEntry(long jzfile, int i)
    {
        return 0;
    }

    /**
     * Returns the number of entries in the ZIP file.
     * @return the number of entries in the ZIP file
     * @throws IllegalStateException if the zip file has been closed
     */
    public int size() {
        ensureOpen();
        return total;
    }

    /**
     * Closes the ZIP file.
     * <p> Closing this ZIP file will close all of the input streams
     * previously returned by invocations of the {@link #getInputStream
     * getInputStream} method.
     *
     * @throws IOException if an I/O error has occurred
     */
    public void close() throws IOException {
        synchronized (this) {
            closeRequested = true;

            if (jzfile != 0) {
                // Close the zip file
                long zf = this.jzfile;
                jzfile = 0;

                close(zf);

                // Release inflaters
                synchronized (inflaters) {
                    int size = inflaters.size();
                    for (int i = 0; i < size; i++) {
                        Inflater inf = (Inflater)inflaters.get(i);
                        inf.end();
                    }
                }
            }
        }
    }


    /**
     * Ensures that the <code>close</code> method of this ZIP file is
     * called when there are no more references to it.
     *
     * <p>
     * Since the time when GC would invoke this method is undetermined,
     * it is strongly recommended that applications invoke the <code>close</code>
     * method as soon they have finished accessing this <code>ZipFile</code>.
     * This will prevent holding up system resources for an undetermined
     * length of time.
     *
     * @throws IOException if an I/O error has occurred
     * @see    java.util.zip.ZipFile#close()
     */
    protected void finalize() throws IOException {
        close();
    }

    private static void close(long jzfile)
    {
    }

    private void ensureOpen() {
        if (closeRequested) {
            throw new IllegalStateException("zip file closed");
        }

        if (jzfile == 0) {
            throw new IllegalStateException("The object is not initialized.");
        }
    }

    private void ensureOpenOrZipException() throws IOException {
        if (closeRequested) {
            throw new ZipException("ZipFile closed");
        }
    }

    /*
     * Inner class implementing the input stream used to read a
     * (possibly compressed) zip file entry.
     */
   private class ZipFileInputStream extends InputStream {
        protected long jzentry; // address of jzentry data
        private   long pos;     // current position within entry data
        protected long rem;     // number of remaining bytes within entry
        protected long size;    // uncompressed size of this entry

        ZipFileInputStream(long jzentry) {
            pos = 0;
            rem = getCSize(jzentry);
            size = getSize(jzentry);
            this.jzentry = jzentry;
        }

        public int read(byte b[], int off, int len) throws IOException {
            if (rem == 0) {
                return -1;
            }
            if (len <= 0) {
                return 0;
            }
            if (len > rem) {
                len = (int) rem;
            }
            synchronized (ZipFile.this) {
                ensureOpenOrZipException();

                len = ZipFile.read(ZipFile.this.jzfile, jzentry, pos, b,
                                   off, len);
            }
            if (len > 0) {
                pos += len;
                rem -= len;
            }
            if (rem == 0) {
                close();
            }
            return len;
        }

        public int read() throws IOException {
            byte[] b = new byte[1];
            if (read(b, 0, 1) == 1) {
                return b[0] & 0xff;
            } else {
                return -1;
            }
        }

        public long skip(long n) {
            if (n > rem)
                n = rem;
            pos += n;
            rem -= n;
            if (rem == 0) {
                close();
            }
            return n;
        }

        public int available() {
            return rem > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) rem;
        }

        public long size() {
            return size;
        }

        public void close() {
            rem = 0;
            synchronized (ZipFile.this) {
                if (jzentry != 0 && ZipFile.this.jzfile != 0) {
                    freeEntry(ZipFile.this.jzfile, jzentry);
                    jzentry = 0;
                }
            }
        }

    }

    private static int read(long jzfile, long jzentry, long pos, byte[] b, int off, int len)
    {
        return 0;
    }

    private static long getCSize(long jzentry)
    {
        return 0;
    }

    private static long getSize(long jzentry)
    {
        return 0;
    }

    // Temporary add on for bug troubleshooting
    private static String getZipMessage(long jzfile)
    {
        return null;
    }
}