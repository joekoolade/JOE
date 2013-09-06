// PrintCFG.java, created Thu Jan 16 10:53:32 2003 by mcmartin
// Copyright (C) 2001-3 jwhaley
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Main;

import joeq.Class.jq_Class;

/**
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: PrintCFG.java,v 1.7 2004/03/09 21:56:17 jwhaley Exp $
 */
public class PrintCFG {
    public static void main(String[] args) {
        jq_Class[] c = new jq_Class[args.length];
        for (int i = 0; i < args.length; i++) {
            c[i] = (jq_Class)Helper.load(args[i]);
        }

        joeq.Compiler.Quad.PrintCFG pass = new joeq.Compiler.Quad.PrintCFG();

        for (int i = 0; i < args.length; i++) {
            Helper.runPass(c[i], pass);
        }
    }
}
