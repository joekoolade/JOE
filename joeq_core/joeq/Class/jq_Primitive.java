// jq_Primitive.java, created Mon Feb  5 23:23:20 2001 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Class;

import joeq.UTF.Utf8;

/*
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: jq_Primitive.java,v 1.19 2004/09/22 22:17:29 joewhaley Exp $
 */
public class jq_Primitive extends jq_Type implements jq_ClassFileConstants {

    public final boolean isClassType() { return false; }
    public final boolean isArrayType() { return false; }
    public final boolean isPrimitiveType() { return true; }
    public final boolean isAddressType() { return false; }
    public final String getName() { return name; }
    public final String shortName() { return name; }
    public final String getJDKDesc() { return desc.toString(); }
    public final int getReferenceSize() { return size; }
    public final ClassLoader getClassLoader() { return PrimordialClassLoader.loader; }
    public final boolean isIntLike() {
        return this == jq_Primitive.INT || this == jq_Primitive.BOOLEAN || this == jq_Primitive.BYTE ||
               this == jq_Primitive.CHAR || this == jq_Primitive.SHORT;
    }
    
    public final boolean isLoaded() { return true; }
    public final boolean isVerified() { return true; }
    public final boolean isPrepared() { return true; }
    public final boolean isSFInitialized() { return true; }
    public final boolean isCompiled() { return true; }
    public final boolean isClsInitRunning() { return true; }
    public final boolean isClsInitialized() { return true; }
    
    public final void load() { }
    public final void verify() { }
    public final void prepare() { }
    public final void sf_initialize() { }
    public final void compile() { }
    public final void cls_initialize() { }
    
    public final boolean isFinal() { return true; }
    
    public final int getDepth() { return -1; }
    public final boolean isInstance(Object o) { return false; }
    
    public void accept(jq_TypeVisitor tv) {
        tv.visitPrimitive(this);
        super.accept(tv);
    }
    
    private final String name;
    private final int size;
    
    /** Creates new jq_Primitive */
    private jq_Primitive(Utf8 desc, String name, int size) {
        super(desc, PrimordialClassLoader.loader);
        this.name = name;
        this.size = size;
        this.display = new jq_Type[DISPLAY_SIZE+2];
        this.display[2] = this;
        this.s_s_array = null;
        this.s_s_array_length = 0;
    }
    // ONLY to be called by PrimordialClassLoader!
    public static jq_Primitive newPrimitive(Utf8 desc, String name, int size) {
        return new jq_Primitive(desc, name, size);
    }

    public static final jq_Primitive BYTE   = (jq_Primitive)PrimordialClassLoader.loader.getOrCreateBSType(Utf8.BYTE_DESC);
    public static final jq_Primitive CHAR   = (jq_Primitive)PrimordialClassLoader.loader.getOrCreateBSType(Utf8.CHAR_DESC);
    public static final jq_Primitive DOUBLE = (jq_Primitive)PrimordialClassLoader.loader.getOrCreateBSType(Utf8.DOUBLE_DESC);
    public static final jq_Primitive FLOAT  = (jq_Primitive)PrimordialClassLoader.loader.getOrCreateBSType(Utf8.FLOAT_DESC);
    public static final jq_Primitive INT    = (jq_Primitive)PrimordialClassLoader.loader.getOrCreateBSType(Utf8.INT_DESC);
    public static final jq_Primitive LONG   = (jq_Primitive)PrimordialClassLoader.loader.getOrCreateBSType(Utf8.LONG_DESC);
    public static final jq_Primitive SHORT  = (jq_Primitive)PrimordialClassLoader.loader.getOrCreateBSType(Utf8.SHORT_DESC);
    public static final jq_Primitive BOOLEAN = (jq_Primitive)PrimordialClassLoader.loader.getOrCreateBSType(Utf8.BOOLEAN_DESC);
    public static final jq_Primitive VOID   = (jq_Primitive)PrimordialClassLoader.loader.getOrCreateBSType(Utf8.VOID_DESC);
    
    public static final jq_Class _class = (jq_Class)PrimordialClassLoader.loader.getOrCreateBSType("Ljoeq/Class/jq_Primitive;");
}
