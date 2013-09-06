// jq_Reference.java, created Mon Feb  5 23:23:20 2001 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Class;

import joeq.Allocator.ObjectLayout;
import joeq.Main.jq;
import joeq.Memory.HeapAddress;
import joeq.Runtime.Reflection;
import joeq.UTF.Utf8;
import jwutil.util.Assert;

/*
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: jq_Reference.java,v 1.28 2004/09/22 22:17:28 joewhaley Exp $
 */
public abstract class jq_Reference extends jq_Type implements jq_ClassFileConstants {

    public static final jq_Reference getTypeOf(Object o) {
        if (!jq.RunningNative) return Reflection.getTypeOf(o);
        return ((HeapAddress)HeapAddress.addressOf(o).offset(ObjectLayout.VTABLE_OFFSET).peek().peek()).asReferenceType();
    }

    public final int getState() { return state; }
    public final boolean isLoaded() { return state >= STATE_LOADED; }
    public final boolean isVerified() { return state >= STATE_VERIFIED; }
    public final boolean isPrepared() { return state >= STATE_PREPARED; }
    public final boolean isSFInitialized() { return state >= STATE_SFINITIALIZED; }
    public final boolean isCompiled() { return state >= STATE_COMPILED; }
    public final boolean isClsInitRunning() { return state >= STATE_CLSINITRUNNING; }
    public final boolean isClsInitialized() { return state >= STATE_CLSINITIALIZED; }
    
    public final boolean isPrimitiveType() { return false; }
    public final boolean isIntLike() { return false; }
    
    public final ClassLoader getClassLoader() { return class_loader; }
    public final int getReferenceSize() { return 4; }
    public final Object getVTable() { chkState(STATE_PREPARED); return vtable; }
    
    public abstract String getJDKName();
    public abstract jq_Class[] getInterfaces();
    public abstract jq_Class getInterface(Utf8 desc);
    public abstract boolean implementsInterface(jq_Class k);

    public abstract jq_InstanceMethod getVirtualMethod(jq_NameAndDesc nd);
    
    public abstract jq_Reference getDirectPrimarySupertype();
    
    public boolean isInstance(Object o) {
        if (o == null) return false;
        jq_Reference that = jq_Reference.getTypeOf(o);
        return that.isSubtypeOf(this);
    }
    
    public static final boolean TRACE = false;
    
    public final void chkState(byte s) {
        if (state >= s) return;
        Assert.UNREACHABLE(this+" actual state: "+state+" expected state: "+s);
    }

    protected jq_Reference(Utf8 desc, ClassLoader class_loader) {
        super(desc, class_loader);
        Assert._assert(class_loader != null);
        this.class_loader = class_loader;
    }
    protected Object vtable;
    protected int/*byte*/ state; // use an 'int' so we can do cas4 on it
    protected final ClassLoader class_loader;

    public static class jq_NullType extends jq_Reference {
        private jq_NullType() {
            super(Utf8.get("L&NULL;"), PrimordialClassLoader.loader);
            this.state = STATE_CLSINITIALIZED;
            this.display = new jq_Type[DISPLAY_SIZE+2];
            this.s_s_array = new jq_Reference[0];
            this.s_s_array_length = 0;
        }
        public boolean isAddressType() { return false; }
        public String getJDKName() { return desc.toString(); }
        public String getJDKDesc() { return getJDKName(); }
        public jq_Class[] getInterfaces() { Assert.UNREACHABLE(); return null; }
        public jq_Class getInterface(Utf8 desc) { Assert.UNREACHABLE(); return null; }
        public boolean implementsInterface(jq_Class k) { Assert.UNREACHABLE(); return false; }
        public jq_InstanceMethod getVirtualMethod(jq_NameAndDesc nd) { Assert.UNREACHABLE(); return null; }
        public String getName() { Assert.UNREACHABLE(); return null; }
        public String shortName() { return "NULL_TYPE"; }
        public boolean isClassType() { Assert.UNREACHABLE(); return false; }
        public boolean isArrayType() { Assert.UNREACHABLE(); return false; }
        public boolean isFinal() { Assert.UNREACHABLE(); return false; }
        public boolean isInstance(Object o) { return o == null; }
        public int getDepth() { Assert.UNREACHABLE(); return 0; }
        public jq_Reference getDirectPrimarySupertype() { Assert.UNREACHABLE(); return null; }
        public void load() { }
        public void verify() { }
        public void prepare() { }
        public void sf_initialize() { }
        public void compile() { }
        public void cls_initialize() { }
        public String toString() { return "NULL_TYPE"; }
        public static final jq_NullType NULL_TYPE = new jq_NullType();
    }
    
    public static final jq_Class _class;
    public static final jq_InstanceField _vtable;
    public static /*final*/ jq_InstanceField _state; // set after PrimordialClassLoader finishes initialization
    static {
        _class = (jq_Class)PrimordialClassLoader.loader.getOrCreateBSType("Ljoeq/Class/jq_Reference;");
        _vtable = _class.getOrCreateInstanceField("vtable", "Ljava/lang/Object;");
        // primitive types have not yet been created!
        _state = _class.getOrCreateInstanceField("state", "I");
    }
}
