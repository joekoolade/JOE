// jq_LocalVarTableEntry.java, created Thu Aug  1 12:46:55 2002 by gback
// Copyright (C) 2001-3 Godmar Back <gback@cs.utah.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Class;

/*
 * @author  Godmar Back <gback@cs.utah.edu>
 * @version $Id: jq_LocalVarTableEntry.java,v 1.6 2004/03/09 22:01:43 jwhaley Exp $
 */
public class jq_LocalVarTableEntry implements Comparable {

    private char startPC;
    private char length;
    private jq_NameAndDesc nd;
    private char index;

    jq_LocalVarTableEntry(char startPC, char index) {
        this.startPC = startPC;
        this.index = index;
    }

    public jq_LocalVarTableEntry(char startPC, char length, 
                                 jq_NameAndDesc nd, char index) {
        this.startPC = startPC;
        this.length = length;
        this.nd = nd;
        this.index = index;
    }

    public char getStartPC() { return startPC; }
    public char getLength() { return length; }
    public jq_NameAndDesc getNameAndDesc() { return nd; }
    public char getIndex() { return index; }
    
    boolean isInRange(int bci, int index) {
        return this.index == index && startPC <= bci && bci < startPC + length;
    }

    public int compareTo(jq_LocalVarTableEntry that) {
        if (this.equals(that)) return 0;
        if (this.index < that.index) return -1;
        if (this.index > that.index) return 1;
        if (this.startPC < that.startPC) return -1;
        return 1;
    }
    public int compareTo(Object that) {
        return compareTo((jq_LocalVarTableEntry)that);
    }

    public boolean equals(jq_LocalVarTableEntry that) {
        return this.startPC == that.startPC && this.index == that.index;
    }
    public boolean equals(Object that) {
        return equals((jq_LocalVarTableEntry)that);
    }
    public int hashCode() {
        return (startPC << 8) | index;
    }

    public String toString() {
        return "(startPC="+(int)startPC+",length="+(int)length
            +",nd="+nd+",index="+(int)index+")";
    }
}
