// ZipConstants.java, created Thu Jul  4  4:50:04 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.

package joeq.ClassLib.Common.java.util.zip;

/**
 * ZipConstants
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: ZipConstants.java,v 1.6 2004/03/09 06:26:29 jwhaley Exp $
 */
public interface ZipConstants {

    long LOCSIG = 0x04034b50L;
    long EXTSIG = 0x08074b50L;
    long CENSIG = 0x02014b50L;
    long ENDSIG = 0x06054b50L;
    int LOCHDR = 30;
    int EXTHDR = 16;
    int CENHDR = 46;
    int ENDHDR = 22;
    int LOCVER = 4;
    int LOCFLG = 6;
    int LOCHOW = 8;
    int LOCTIM = 10;
    int LOCCRC = 14;
    int LOCSIZ = 18;
    int LOCLEN = 22;
    int LOCNAM = 26;
    int LOCEXT = 28;
    int EXTCRC = 4;
    int EXTSIZ = 8;
    int EXTLEN = 12;
    int CENVEM = 4;
    int CENVER = 6;
    int CENFLG = 8;
    int CENHOW = 10;
    int CENTIM = 12;
    int CENCRC = 16;
    int CENSIZ = 20;
    int CENLEN = 24;
    int CENNAM = 28;
    int CENEXT = 30;
    int CENCOM = 32;
    int CENDSK = 34;
    int CENATT = 36;
    int CENATX = 38;
    int CENOFF = 42;
    int ENDSUB = 8;
    int ENDTOT = 10;
    int ENDSIZ = 12;
    int ENDOFF = 16;
    int ENDCOM = 20;
}
