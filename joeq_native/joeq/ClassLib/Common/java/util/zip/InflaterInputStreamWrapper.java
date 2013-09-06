// InflaterInputStreamWrapper.java, created Thu Jul  4  4:50:04 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.Common.java.util.zip;

/**
 * InflaterInputStreamWrapper
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: InflaterInputStreamWrapper.java,v 1.6 2004/03/09 06:26:29 jwhaley Exp $
 */
public class InflaterInputStreamWrapper extends java.util.zip.InflaterInputStream {
    private boolean isClosed;
    private boolean eof;
    private final ZipFile zf;
    public InflaterInputStreamWrapper(ZipFile zf, java.io.InputStream in, java.util.zip.Inflater inflater) {
        super(in, inflater);
        this.zf = zf;
        this.isClosed = false; this.eof = false;
    }
    public void close() throws java.io.IOException {
        if (!this.isClosed) {
            zf.releaseInflater0(inf);
            in.close();
            isClosed = true;
        }
    }
    protected void fill() throws java.io.IOException {
        if (eof) throw new java.io.EOFException("Unexpected end of ZLIB input stream");
        len = this.in.read(buf, 0, buf.length);
        if (len == -1) {
            buf[0] = 0;
            len = 1;
            eof = true;
        }
        inf.setInput(buf, 0, len);
    }
    public int available() throws java.io.IOException {
        if (super.available() != 0) return this.in.available();
        return 0;
    }
    
}
