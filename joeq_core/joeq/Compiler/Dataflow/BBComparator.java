// BBComparator.java, created Mar 22, 2004 2:04:07 PM 2004 by jwhaley
// Copyright (C) 2004 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.Dataflow;

import java.util.Comparator;
import joeq.Compiler.Quad.BasicBlock;
import jwutil.collections.Pair;

public class BBComparator implements Comparator {

    public static final BBComparator INSTANCE = new BBComparator();
    private BBComparator() {}

    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(Object o1, Object o2) {
        if (o1 == o2) return 0;
        int r;
        BasicBlock a, b;
        if (o1 instanceof Pair) {
            a = (BasicBlock) ((Pair) o1).left;
            if (o2 instanceof Pair) {
                BasicBlock a2 = (BasicBlock) ((Pair) o1).right;
                b = (BasicBlock) ((Pair) o2).left;
                BasicBlock b2 = (BasicBlock) ((Pair) o2).right;
                r = compare(a, b);
                if (r == 0) r = compare(a2, b2);
                return r;
            } else {
                b = (BasicBlock) o2;
            }
        } else {
            a = (BasicBlock) o1;
            if (o2 instanceof Pair) {
                b = (BasicBlock) ((Pair) o2).left;
            } else {
                b = (BasicBlock) o2;
            }
        }
        r = compare(a, b);
        if (r == 0) r = (o2 instanceof Pair)?1:-1;
        return r;
    }

    public int compare(BasicBlock bb1, BasicBlock bb2) {
        if (bb1 == bb2) return 0;
        else if (bb1.getID() < bb2.getID()) return -1;
        else return 1;
    }
    
}