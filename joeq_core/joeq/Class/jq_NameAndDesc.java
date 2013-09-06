// jq_NameAndDesc.java, created Mon Feb  5 23:23:20 2001 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Class;

import joeq.UTF.Utf8;

/*
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: jq_NameAndDesc.java,v 1.6 2004/03/09 22:01:43 jwhaley Exp $
 */
public class jq_NameAndDesc {

    private final Utf8 name, desc;
    
    /** Creates new jq_NameAndDesc */
    public jq_NameAndDesc(Utf8 name, Utf8 desc) {
        this.name = name;
        this.desc = desc;
    }
    public jq_NameAndDesc(String name, String desc) {
        this(Utf8.get(name), Utf8.get(desc));
    }
    
    public final Utf8 getName() { return name; }
    public final Utf8 getDesc() { return desc; }
    
    public boolean equals(Object o) { return equals((jq_NameAndDesc)o); }
    public boolean equals(jq_NameAndDesc that) {
        return this.name == that.name && this.desc == that.desc;
    }
    public int hashCode() {
        return name.hashCode() ^ desc.hashCode();
    }
    public String toString() {
        return name+" "+desc;
    }

}
