// jq_TypeVisitor.java, created Fri Jan 11 17:28:36 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Class;

/**
 * Visitor interface for jq_Type and its various subclasses.
 * 
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: jq_TypeVisitor.java,v 1.7 2005/03/14 20:40:36 joewhaley Exp $
 */
public interface jq_TypeVisitor {

    void visitClass(jq_Class m);
    void visitArray(jq_Array m);
    void visitPrimitive(jq_Primitive m);
    void visitType(jq_Type m);
    
    /**
     * Empty jq_TypeVisitor for convenient subclassing.
     * 
     * @author jwhaley
     * @version $Id: jq_TypeVisitor.java,v 1.7 2005/03/14 20:40:36 joewhaley Exp $
     */
    class EmptyVisitor implements jq_TypeVisitor {
        public void visitClass(jq_Class m) {}
        public void visitArray(jq_Array m) {}
        public void visitPrimitive(jq_Primitive m) {}
        public void visitType(jq_Type m) {}
    }
    
}
