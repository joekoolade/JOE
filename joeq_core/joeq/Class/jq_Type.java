// jq_Type.java, created Mon Feb  5 23:23:20 2001 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Class;

import java.util.StringTokenizer;
import java.io.IOException;
import joeq.ClassLib.ClassLibInterface;
import joeq.Main.jq;
import joeq.Runtime.Debug;
import joeq.Runtime.Reflection;
import joeq.UTF.Utf8;
import jwutil.io.Textualizable;
import jwutil.io.Textualizer;
import jwutil.util.Assert;

/*
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: jq_Type.java,v 1.35 2004/09/22 22:17:29 joewhaley Exp $
 */
public abstract class jq_Type implements Textualizable {
    
    protected final Utf8 desc;
    private /*final*/ Class class_object;  // pointer to our associated java.lang.Class object

    public static final boolean USE_CLASS_OBJECT_FIELD = true;

    protected jq_Type(Utf8 desc, ClassLoader class_loader) {
        this.desc = desc;
        initializeClassObject();
    }
    
    private void initializeClassObject() {
        if (jq.RunningNative)
            this.class_object = ClassLibInterface.DEFAULT.createNewClass(this);
        else if (USE_CLASS_OBJECT_FIELD)
            this.class_object = Reflection.getJDKType(this);
    }
    
    public abstract String getName();
    public abstract String shortName();
    public final Utf8 getDesc() { return desc; }
    public abstract String getJDKDesc();
    public abstract boolean isClassType();
    public abstract boolean isArrayType();
    public abstract boolean isPrimitiveType();
    public abstract boolean isAddressType();
    public abstract boolean isIntLike();
    public final boolean isReferenceType() { return !isPrimitiveType(); }
    public abstract ClassLoader getClassLoader();
    public abstract int getReferenceSize();
    public final jq_Array getArrayTypeForElementType() {
        return (jq_Array)PrimordialClassLoader.getOrCreateType(getClassLoader(), desc.getAsArrayDescriptor());
    }
    public boolean needsDynamicLink(jq_Method method) { return false; }
    public abstract boolean isInstance(Object o);
    public abstract int getDepth();
    public final Class getJavaLangClassObject() {
        if (jq.RunningNative && this.class_object == null)
            initializeClassObject();
        return class_object;
    }

    public static final int DISPLAY_SIZE = 8;
    
    /** 
     * The first two elements are the positive and negative cache,
     * respectively.  The remainder are the primary supertypes of this type
     * ordered by the tree relation.  This array should be inlined into the
     * jq_Type object, hopefully.
     * 
     * See paper "Fast subtype checking in the HotSpot JVM".
     */
    protected jq_Type[] display;
    
    /** 
     * The offset of our type in the display array if this is a primary type, or
     * 0 or 1 if this is a secondary type.
     * 
     * See paper "Fast subtype checking in the HotSpot JVM".
     */
    protected int offset;
    
    /**
     * A reference to the secondary subtype array for this type.
     * 
     * See paper "Fast subtype checking in the HotSpot JVM".
     */
    protected jq_Reference[] s_s_array;
    
    /**
     * The maximum index used in the secondary subtype array.
     * 
     * See paper "Fast subtype checking in the HotSpot JVM".
     */
    protected int s_s_array_length;
    
    //// useful functions for parsing class and method names
    
    public static String convertPrimitive(String s) {
        if (s.equals("byte")) return "B";
        if (s.equals("char")) return "C";
        if (s.equals("double")) return "D";
        if (s.equals("float")) return "F";
        if (s.equals("int")) return "I";
        if (s.equals("long")) return "J";
        if (s.equals("short")) return "S";
        if (s.equals("void")) return "V";
        if (s.equals("boolean")) return "Z";
        return s;
    }
    
    public static jq_Type parseType(String s) {
        s = convertPrimitive (s);
        if (s.length() == 1) {
            jq_Primitive t = (jq_Primitive) PrimordialClassLoader.loader.getBSType(s);
            if (t != null) return t;
            s = "L" + s + ";";
        } else {
            s = s.replace('.', '/');
            int arrayDepth = 0;
            while (s.endsWith("[]")) {
                ++arrayDepth;
                s = s.substring(0, s.length() - 2);
            }
            s = convertPrimitive (s);
            if (s.length() == 1) {
                jq_Primitive t = (jq_Primitive) PrimordialClassLoader.loader.getBSType(s);
                if (t == null)
                    s = "L" + s + ";";
            } else if (!s.startsWith("[") && !s.endsWith(";"))
                s = "L" + s + ";";
            while (--arrayDepth >= 0)
                s = "[" + s;
        }
        return (jq_Reference) PrimordialClassLoader.loader.getOrCreateBSType(s);
    }

    public static final boolean TRACE = false;

    public final boolean isSubtypeOf(jq_Type that) {
        Assert._assert(this.isPrepared());
        Assert._assert(that.isPrepared());
        
        int off = that.offset;
        if (that == this.display[off]) {
            // matches cache or depth
            if (TRACE) {
                Debug.write(this.getDesc());
                Debug.write(" matches ");
                Debug.write(that.getDesc());
                Debug.write(" offset=");
                Debug.writeln(off);
            }
            return off != 1;
        }
        if (this == jq_Reference.jq_NullType.NULL_TYPE) {
            return that.isReferenceType();
        }
        if (off > 1) {
            // other class is a primary type that isn't a superclass.
            if (TRACE) {
                Debug.write(this.getDesc());
                Debug.write(" doesn't match ");
                Debug.write(that.getDesc());
                Debug.write(", offset ");
                Debug.write(off);
                Debug.write(" is ");
                if (this.display[off] == null)
                    Debug.writeln("null");
                else
                    Debug.writeln(this.display[off].getDesc());
            }
            return false;
        }
        if (this == that) {
            // classes are exactly the same.
            return true;
        }
        int n = this.s_s_array_length;
        for (int i=0; i<n; ++i) {
            if (this.s_s_array[i] == that) {
                this.display[0] = that;
                that.offset = 0;
                if (TRACE) {
                    Debug.write(this.getDesc());
                    Debug.write(" matches ");
                    Debug.write(that.getDesc());
                    Debug.writeln(" in s_s_array");
                }
                return true;
            }
        }
        this.display[1] = that;
        that.offset = 1;
        if (TRACE) {
            Debug.write(this.getDesc());
            Debug.write(" doesn't match ");
            Debug.write(that.getDesc());
            Debug.writeln(" in s_s_array");
        }
        return false;
    }
    
    /*
    public boolean isBootType() {
        return jq.boot_types.contains(this);
    }
    */

    public abstract boolean isLoaded();
    public abstract boolean isVerified();
    public abstract boolean isPrepared();
    public abstract boolean isSFInitialized();
    public abstract boolean isCompiled();
    public abstract boolean isClsInitRunning();
    public abstract boolean isClsInitialized();
    
    public abstract boolean isFinal();
    
    public abstract void load();
    public abstract void verify();
    public abstract void prepare();
    public abstract void sf_initialize();
    public abstract void compile();
    public abstract void cls_initialize();

    public void accept(jq_TypeVisitor tv) { tv.visitType(this); }
    
    public String toString() { return getName(); }
    
    public void write(Textualizer t) throws IOException {
        t.writeString(getDesc().toString());
    }
    public void writeEdges(Textualizer t) throws IOException { }
    public void addEdge(String edgeName, Textualizable t) { }
    public static jq_Type read(StringTokenizer st) {
        String desc = st.nextToken();
        if (desc.equals("null")) return null;
        jq_Type r = PrimordialClassLoader.loader.getOrCreateBSType(desc);
        return r;
    }
    public static final jq_Class _class = (jq_Class)PrimordialClassLoader.loader.getOrCreateBSType("Ljoeq/Class/jq_Type;");
}
