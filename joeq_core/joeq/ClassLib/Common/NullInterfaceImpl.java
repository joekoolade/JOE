// NullInterfaceImpl.java, created Wed Dec 11 11:59:03 2002 by mcmartin
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.Common;

import joeq.Runtime.ObjectTraverser;
import joeq.Class.jq_InstanceField;
import joeq.Class.jq_StaticField;

/**
 * NullInterfaceImpl
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @author  Michael Martin <mcmartin@stanford.edu>
 * @version $Id: NullInterfaceImpl.java,v 1.8 2004/03/10 22:39:19 jwhaley Exp $
 */
public class NullInterfaceImpl implements joeq.ClassLib.Common.Interface {

    /** Creates new Interface */
    public NullInterfaceImpl() {}

    public java.util.Iterator getImplementationClassDescs(joeq.UTF.Utf8 desc) {
        return java.util.Collections.EMPTY_SET.iterator();
    }
    
    public ObjectTraverser getObjectTraverser() {
        return NullObjectTraverser.INSTANCE;
    }

    public static class NullObjectTraverser extends ObjectTraverser {
        public void initialize () { }
        public Object mapStaticField(jq_StaticField f) { return NO_OBJECT; }
        public Object mapInstanceField(Object o, jq_InstanceField f) { return NO_OBJECT; }
        public Object mapValue(Object o) { return NO_OBJECT; }
        public static final NullObjectTraverser INSTANCE = new NullObjectTraverser();
    }
    
    public java.lang.Class createNewClass(joeq.Class.jq_Type f) {
        throw new UnsupportedOperationException("Using a Null ClassLib Interface!");
    }
    
    public java.lang.reflect.Constructor createNewConstructor(joeq.Class.jq_Initializer f) {
        throw new UnsupportedOperationException("Using a Null ClassLib Interface!");
    }
    
    public void initNewConstructor(java.lang.reflect.Constructor dis, joeq.Class.jq_Initializer f) {
        throw new UnsupportedOperationException("Using a Null ClassLib Interface!");
    }
    
    public java.lang.reflect.Field createNewField(joeq.Class.jq_Field f) {
        throw new UnsupportedOperationException("Using a Null ClassLib Interface!");
    }
    
    public void initNewField(java.lang.reflect.Field dis, joeq.Class.jq_Field f) {
        throw new UnsupportedOperationException("Using a Null ClassLib Interface!");
    }
    
    public java.lang.reflect.Method createNewMethod(joeq.Class.jq_Method f) {
        throw new UnsupportedOperationException("Using a Null ClassLib Interface!");
    }
    
    public void initNewMethod(java.lang.reflect.Method dis, joeq.Class.jq_Method f) {
        throw new UnsupportedOperationException("Using a Null ClassLib Interface!");
    }
    
    public joeq.Class.jq_Field getJQField(java.lang.reflect.Field f) {
        throw new UnsupportedOperationException("Using a Null ClassLib Interface!");
    }
    
    public joeq.Class.jq_Initializer getJQInitializer(java.lang.reflect.Constructor f) {
        throw new UnsupportedOperationException("Using a Null ClassLib Interface!");
    }
    
    public joeq.Class.jq_Method getJQMethod(java.lang.reflect.Method f) {
        throw new UnsupportedOperationException("Using a Null ClassLib Interface!");
    }
    
    public joeq.Class.jq_Type getJQType(java.lang.Class k) {
        throw new UnsupportedOperationException("Using a Null ClassLib Interface!");
    }
    
    public joeq.Class.jq_Type getOrCreateType(java.lang.ClassLoader cl, joeq.UTF.Utf8 desc) {
        throw new UnsupportedOperationException("Using a Null ClassLib Interface!");
    }
    
    public void unloadType(java.lang.ClassLoader cl, joeq.Class.jq_Type t) {
        throw new UnsupportedOperationException("Using a Null ClassLib Interface!");
    }
    
    public void init_zipfile(java.util.zip.ZipFile dis, java.lang.String name) throws java.io.IOException {
        throw new UnsupportedOperationException("Using a Null ClassLib Interface!");
    }
    
    public void init_inflater(java.util.zip.Inflater dis, boolean nowrap) throws java.io.IOException {
        throw new UnsupportedOperationException("Using a Null ClassLib Interface!");
    }
    
    public void initializeSystemClass() throws java.lang.Throwable {
        throw new UnsupportedOperationException("Using a Null ClassLib Interface!");
    }
}
