// jq_MemberReference.java, created Mon Feb  5 23:23:20 2001 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Class;

import joeq.UTF.Utf8;

/**
 * Objects of this class represent unresolved references to class members.
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: jq_MemberReference.java,v 1.7 2004/03/09 22:01:43 jwhaley Exp $
 */
public final class jq_MemberReference {

    private jq_Class clazz;
    private jq_NameAndDesc nd;
    
    /** Creates new member reference to the named member in the given class.
     * @param clazz  class of the referenced member
     * @param nd  name and descriptor of the referenced member
     */
    public jq_MemberReference(jq_Class clazz, jq_NameAndDesc nd) {
        this.clazz = clazz;
        this.nd = nd;
    }
    
    /** Returns the class of the referenced member.
     * @return  class of referenced member
     */
    public final jq_Class getReferencedClass() { return clazz; }
    /** Returns the name and descriptor of the referenced member.
     * @return  name and descriptor of referenced member
     */
    public final jq_NameAndDesc getNameAndDesc() { return nd; }
    /** Returns the name of the referenced member.
     * @return  name of referenced member
     */
    public final Utf8 getName() { return nd.getName(); }
    /** Returns the descriptor of the referenced member.
     * @return  descriptor of referenced member
     */
    public final Utf8 getDesc() { return nd.getDesc(); }
    
    public boolean equals(Object o) { return equals((jq_MemberReference)o); }
    public boolean equals(jq_MemberReference that) {
        return this.clazz == that.clazz && this.nd.equals(that.nd);
    }
    public int hashCode() {
        return clazz.hashCode() ^ nd.hashCode();
    }

    public String toString() {
        return "ref to "+clazz+"."+nd;
    }

}
