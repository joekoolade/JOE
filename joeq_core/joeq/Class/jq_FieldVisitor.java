// jq_FieldVisitor.java, created Wed Jun 26 12:26:34 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Class;

import java.util.Arrays;
import java.util.Iterator;
import jwutil.collections.AppendIterator;

/*
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: jq_FieldVisitor.java,v 1.9 2004/09/22 22:17:29 joewhaley Exp $
 */
public interface jq_FieldVisitor {

    void visitStaticField(jq_StaticField m);
    void visitInstanceField(jq_InstanceField m);
    void visitField(jq_Field m);
    
    class EmptyVisitor implements jq_FieldVisitor {
        public void visitStaticField(jq_StaticField m) {}
        public void visitInstanceField(jq_InstanceField m) {}
        public void visitField(jq_Field m) {}
    }
    
    class DeclaredFieldVisitor extends jq_TypeVisitor.EmptyVisitor {
        final jq_FieldVisitor mv; boolean trace;
        public DeclaredFieldVisitor(jq_FieldVisitor mv) { this.mv = mv; }
        public DeclaredFieldVisitor(jq_FieldVisitor mv, boolean trace) { this.mv = mv; this.trace = trace; }
        public void visitClass(jq_Class k) {
            if (trace) System.out.println(k.toString());
            Iterator it = new AppendIterator(Arrays.asList(k.getDeclaredStaticFields()).iterator(),
                                            Arrays.asList(k.getDeclaredInstanceFields()).iterator());
            while (it.hasNext()) {
                jq_Field m = (jq_Field)it.next();
                m.accept(mv);
            }
        }
    }
    
}

