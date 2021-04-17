// Browser.java, created Sat May 25 12:46:16 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Linker.ELF;

import java.util.Iterator;
import java.util.List;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Browser
 * 
 * @author John Whaley
 * @version $Id: Browser.java,v 1.4 2004/10/08 08:43:02 joewhaley Exp $
 */
public class Browser {

    public static void main(String[] args) throws IOException {
        RandomAccessFile f = new RandomAccessFile(args[0], "r");
        browseFile(f);
    }
    
    public static void browseFile(RandomAccessFile file) throws IOException {
        ELFRandomAccessFile f = new ELFRandomAccessFile(file);
        List sections = f.getSections();
        for (Iterator i=sections.iterator(); i.hasNext(); ) {
            Section s = (Section) i.next();
            System.out.println(s);
        }
    }
    
}
