// ExtendedSystem.java, created Sun Jun  9  6:56:00 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.ibm13_linux.com.ibm.jvm;

import joeq.Class.jq_Array;
import joeq.Class.jq_Reference;
import joeq.Class.jq_Type;
import joeq.Runtime.Reflection;

/**
 * ExtendedSystem
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: ExtendedSystem.java,v 1.10 2004/03/09 21:57:35 jwhaley Exp $
 */
public abstract class ExtendedSystem {
    
    private static boolean isJVMUnresettable() { return false; }

    private static java.lang.Object resizeArray(int newSize, java.lang.Object array, int startIndex, int size) {
        jq_Array a = (jq_Array)jq_Reference.getTypeOf(array);
        java.lang.Object o = a.newInstance(newSize);
        java.lang.System.arraycopy(array, 0, o, startIndex, size);
        return o;
    }
    private static java.lang.Object newArray(java.lang.Class elementType, int size, java.lang.Object enclosingObject) {
        jq_Type t = Reflection.getJQType(elementType);
        jq_Array a = t.getArrayTypeForElementType();
        return a.newInstance(size);
    }

}
