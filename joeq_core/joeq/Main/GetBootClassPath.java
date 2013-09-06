// GetBootClassPath.java, created Fri Aug 16 16:04:04 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Main;

/*
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: GetBootClassPath.java,v 1.5 2004/03/09 01:48:45 jwhaley Exp $
 */
public abstract class GetBootClassPath {
    public static void main (String args[]) {
        for (int i=0; i<args.length; ++i) {
            System.out.print(args[i]+System.getProperty("path.separator"));
        }
        System.out.print(System.getProperty("sun.boot.class.path"));
        //System.out.print(System.getProperty("path.separator")+System.getProperty("java.class.path"));
    }
}
