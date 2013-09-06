// jq_Array.java, created Mon Feb  5 23:23:20 2001 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Class;

import joeq.Allocator.ObjectLayout;
import joeq.Main.jq;
import joeq.Memory.Address;
import joeq.Memory.HeapAddress;
import joeq.UTF.Utf8;
import jwutil.util.Assert;

/**
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: jq_Array.java,v 1.33 2004/09/22 22:17:29 joewhaley Exp $
 */
public class jq_Array extends jq_Reference implements jq_ClassFileConstants {

    public static /*final*/ boolean TRACE = false;
    
    public final boolean isClassType() { return false; }
    public final boolean isArrayType() { return true; }
    public final boolean isAddressType() { return false; }
    public final String getName() {
        return element_type.getName()+"[]";
    }
    public final String shortName() {
        return element_type.shortName()+"[]";
    }
    public final String getJDKName() {
        return desc.toString().replace('/','.');
        //return "["+element_type.getJDKDesc();
    }
    public final String getJDKDesc() {
        return getJDKName();
    }
    public final byte getLogElementSize() {
        if (element_type == jq_Primitive.LONG ||
            element_type == jq_Primitive.DOUBLE)
            return 3;
        if (element_type == jq_Primitive.CHAR ||
            element_type == jq_Primitive.SHORT)
            return 1;
        if (element_type == jq_Primitive.BYTE)
            return 0;
        return 2;
    }

    public final Object newInstance(int length) {
        cls_initialize();
        return _delegate.newInstance(this, length, vtable);
    }
    
    public final int getDimensionality() {
        if (element_type.isArrayType())
            return 1+((jq_Array)element_type).getDimensionality();
        else
            return 1;
    }
    
    public final boolean isFinal() { return element_type.isFinal(); }
    
    public static final jq_Class[] array_interfaces = new jq_Class[] {
    (jq_Class)PrimordialClassLoader.loader.getOrCreateBSType("Ljava/lang/Cloneable;"),
    (jq_Class)PrimordialClassLoader.loader.getOrCreateBSType("Ljava/io/Serializable;"),
    };
    public final jq_Class[] getInterfaces() {
        return array_interfaces;
    }
    public final jq_Class getInterface(Utf8 desc) {
        chkState(STATE_PREPARED);
        for (int i=0; i<array_interfaces.length; ++i) {
            jq_Class in = array_interfaces[i];
            if (in.getDesc() == desc)
                return in;
        }
        return null;
    }
    public final boolean implementsInterface(jq_Class k) {
        chkState(STATE_PREPARED);
        return k == array_interfaces[0] || k == array_interfaces[1];
    }
    
    public final jq_InstanceMethod getVirtualMethod(jq_NameAndDesc nd) {
        chkState(STATE_PREPARED);
        jq_Class jlo = PrimordialClassLoader.getJavaLangObject();
        return jlo.getVirtualMethod(nd);
    }
    
    public final jq_Type getElementType() { return element_type; }
    
    private jq_Array(Utf8 desc, ClassLoader class_loader, jq_Type element_type) {
        super(desc, class_loader);
        Assert._assert(desc.isDescriptor(TC_ARRAY));
        Assert._assert(element_type != null);
        this.element_type = element_type;
    }
    // ONLY TO BE CALLED BY ClassLoader!!!
    public static jq_Array newArray(Utf8 descriptor, ClassLoader classLoader, jq_Type element_type) {
        return new jq_Array(descriptor, classLoader, element_type);
    }
    
    public static final jq_Array BYTE_ARRAY =
    new jq_Array(Utf8.get(""+(char)TC_ARRAY+(char)TC_BYTE), PrimordialClassLoader.loader, jq_Primitive.BYTE);
    public static final jq_Array CHAR_ARRAY =
    new jq_Array(Utf8.get(""+(char)TC_ARRAY+(char)TC_CHAR), PrimordialClassLoader.loader, jq_Primitive.CHAR);
    public static final jq_Array DOUBLE_ARRAY =
    new jq_Array(Utf8.get(""+(char)TC_ARRAY+(char)TC_DOUBLE), PrimordialClassLoader.loader, jq_Primitive.DOUBLE);
    public static final jq_Array FLOAT_ARRAY =
    new jq_Array(Utf8.get(""+(char)TC_ARRAY+(char)TC_FLOAT), PrimordialClassLoader.loader, jq_Primitive.FLOAT);
    public static final jq_Array INT_ARRAY =
    new jq_Array(Utf8.get(""+(char)TC_ARRAY+(char)TC_INT), PrimordialClassLoader.loader, jq_Primitive.INT);
    public static final jq_Array LONG_ARRAY =
    new jq_Array(Utf8.get(""+(char)TC_ARRAY+(char)TC_LONG), PrimordialClassLoader.loader, jq_Primitive.LONG);
    public static final jq_Array SHORT_ARRAY =
    new jq_Array(Utf8.get(""+(char)TC_ARRAY+(char)TC_SHORT), PrimordialClassLoader.loader, jq_Primitive.SHORT);
    public static final jq_Array BOOLEAN_ARRAY =
    new jq_Array(Utf8.get(""+(char)TC_ARRAY+(char)TC_BOOLEAN), PrimordialClassLoader.loader, jq_Primitive.BOOLEAN);
    public static final jq_Array OBJECT_ARRAY = (jq_Array) PrimordialClassLoader.loader.getOrCreateBSType("[Ljava/lang/Object;");
    
    public static jq_Array getPrimitiveArrayType(byte atype) {
        switch(atype) {
            case T_BOOLEAN:
                return BOOLEAN_ARRAY;
            case T_CHAR:
                return CHAR_ARRAY;
            case T_FLOAT:
                return FLOAT_ARRAY;
            case T_DOUBLE:
                return DOUBLE_ARRAY;
            case T_BYTE:
                return BYTE_ARRAY;
            case T_SHORT:
                return SHORT_ARRAY;
            case T_INT:
                return INT_ARRAY;
            case T_LONG:
                return LONG_ARRAY;
            default:
                throw new ClassFormatError();
        }
    }

    public static byte getTypecode(jq_Array array) {
        if (array == BOOLEAN_ARRAY) return T_BOOLEAN;
        if (array == CHAR_ARRAY) return T_CHAR;
        if (array == FLOAT_ARRAY) return T_FLOAT;
        if (array == DOUBLE_ARRAY) return T_DOUBLE;
        if (array == BYTE_ARRAY) return T_BYTE;
        if (array == SHORT_ARRAY) return T_SHORT;
        if (array == INT_ARRAY) return T_INT;
        if (array == LONG_ARRAY) return T_LONG;
        throw new ClassFormatError();
    }

    public final int getInstanceSize(int length) {
        int size = ObjectLayout.ARRAY_HEADER_SIZE+(length<<getLogElementSize());
        return (size+3) & ~3;
    }
    
    public final jq_Type getInnermostElementType() {
        if (element_type.isArrayType())
            return ((jq_Array)element_type).getInnermostElementType();
        else
            return element_type;
    }
    
    public final int getDepth() {
        return 1+element_type.getDepth();
    }
    
    public final jq_Reference getDirectPrimarySupertype() {
        jq_Type innermost = getInnermostElementType();
        if (innermost == PrimordialClassLoader.getJavaLangObject()) {
            return (jq_Reference) element_type;
        }
        int dim = getDimensionality();
        jq_Reference type;
        if (innermost.isPrimitiveType()) {
            type = PrimordialClassLoader.getJavaLangObject();
            --dim;
        } else {
            innermost.load();
            type = ((jq_Class) innermost).getDirectPrimarySupertype();
        }
        while (--dim >= 0) {
            type = type.getArrayTypeForElementType();
        }
        return type;
    }
    
    public static jq_Reference[] s_s_array_cache = array_interfaces;
    
    private static int getCacheIndexForDim(int dim) {
        if (dim * 2 > s_s_array_cache.length) {
            jq_Reference[] t = new jq_Reference[dim*2];
            System.arraycopy(s_s_array_cache, 0, t, 0, s_s_array_cache.length);
            t[dim*2-2] = t[dim*2-4].getArrayTypeForElementType();
            t[dim*2-1] = t[dim*2-3].getArrayTypeForElementType();
            s_s_array_cache = t;
        }
        return dim*2;
    }
    
    public final void load() {
        if (isLoaded()) return;
        synchronized (this) {
            if (TRACE) System.out.println("Loading "+this+"...");
            state = STATE_LOADED;
        }
    }
    public final void verify() {
        if (isVerified()) return;
        if (!isLoaded()) load();
        synchronized (this) {
            if (TRACE) System.out.println("Verifying "+this+"...");
            state = STATE_VERIFIED;
        }
    }
    public final void prepare() {
        if (isPrepared()) return;
        if (!isVerified()) verify();
        synchronized (this) {
            if (TRACE) System.out.println("Preparing "+this+"...");
            state = STATE_PREPARING;
            
            jq_Type innermost = this.getInnermostElementType();
            innermost.load();
            this.display = new jq_Type[DISPLAY_SIZE+2];
            if (!(innermost instanceof jq_Class) ||
                !((jq_Class) innermost).isInterface()) {
                jq_Reference dps = this.getDirectPrimarySupertype();
                dps.prepare();
                int num = dps.offset;
                if (num < 2) num = DISPLAY_SIZE+1;
                System.arraycopy(dps.display, 2, this.display, 2, num-1);
                this.offset = num + 1;
                if (this.offset >= DISPLAY_SIZE+2)
                    this.offset = 0;
                this.display[this.offset] = this;
                // todo: if innermost element type implements some interfaces,
                // we need to add some more to s_s_array.
            } else {
                jq_Reference r = PrimordialClassLoader.getJavaLangObject();
                this.display[2] = r;
                int dim = this.getDimensionality();
                for (int i=0; i<dim; ++i) {
                    if (i >= DISPLAY_SIZE-1) break;
                    r = r.getArrayTypeForElementType();
                    this.display[i+3] = r;
                }
            }
            this.s_s_array_length = getCacheIndexForDim(this.getDimensionality());
            // todo: when s_s_array_cache changes, previously prepared types still
            // refer to the old copy.  this is a waste of memory.
            this.s_s_array = s_s_array_cache;
            if (innermost instanceof jq_Class) {
                jq_Class c = (jq_Class) innermost;
                c.prepare();
                jq_Class[] interfaces = c.getInterfaces();
                if (interfaces.length > 0) {
                    jq_Reference[] a = new jq_Reference[this.s_s_array_length + interfaces.length];
                    System.arraycopy(this.s_s_array, 0, a, 0, this.s_s_array_length);
                    for (int i=0; i<interfaces.length; ++i) {
                        jq_Reference c2 = interfaces[i];
                        for (int j=0, n=this.getDimensionality(); j<n; ++j)
                            c2 = c2.getArrayTypeForElementType();
                        a[i+this.s_s_array_length] = c2;
                    }
                    this.s_s_array_length = a.length;
                    this.s_s_array = a;
                }
            }
            
            if (TRACE) {
                System.out.println(this+" offset="+this.offset);
                if (this.offset != 0) {
                    for (int i=0; i<this.display.length; ++i) {
                        System.out.println(this+" display["+i+"] = "+this.display[i]);
                    }
                }
                for (int i=0; i<this.s_s_array_length; ++i) {
                    System.out.println(this+" s_s_array["+i+"] = "+this.s_s_array[i]);
                }
            }
            
            // vtable is a copy of Ljava/lang/Object;
            jq_Class jlo = PrimordialClassLoader.getJavaLangObject();
            jlo.prepare();
            Address[] jlovtable = (Address[])jlo.getVTable();
            vtable = new Address[jlovtable.length];
            state = STATE_PREPARED;
        }
    }
    public final void sf_initialize() {
        if (isSFInitialized()) return;
        if (!isPrepared()) prepare();
        synchronized (this) {
            if (TRACE) System.out.println("SF init "+this+"...");
            state = STATE_SFINITIALIZED;
        }
    }
    public final void compile() {
        if (isCompiled()) return;
        if (!isSFInitialized()) sf_initialize();
        synchronized (this) {
            if (TRACE) System.out.println("Compile "+this+"...");
            state = STATE_COMPILING;
            jq_Class jlo = PrimordialClassLoader.getJavaLangObject();
            jlo.compile();
            Address[] jlovtable = (Address[])jlo.getVTable();
            Address[] vt = (Address[])this.vtable;
            vt[0] = HeapAddress.addressOf(this);
            System.arraycopy(jlovtable, 1, vt, 1, jlovtable.length-1);
            if (TRACE) System.out.println(this+": "+vt[0].stringRep()+" vtable "+HeapAddress.addressOf(vt).stringRep());
            state = STATE_COMPILED;
        }
    }
    public final void cls_initialize() {
        if (isClsInitialized()) return;
        if (!isCompiled()) compile();
        synchronized (this) {
            if (TRACE) System.out.println("Class init "+this+"...");
            state = STATE_CLSINITIALIZING;
            jq_Class jlo = PrimordialClassLoader.getJavaLangObject();
            jlo.cls_initialize();
            state = STATE_CLSINITIALIZED;
        }
    }
    
    public void accept(jq_TypeVisitor tv) {
        tv.visitArray(this);
        super.accept(tv);
    }
    
    private final jq_Type element_type;

    public static final jq_Class _class;
    static interface Delegate {
        Object newInstance(jq_Array a, int length, Object vtable);
    }

    private static Delegate _delegate;

    static {
        _class = (jq_Class)PrimordialClassLoader.loader.getOrCreateBSType("Ljoeq/Class/jq_Array;");
        /* Set up delegates. */
        _delegate = null;
        boolean nullVM = jq.nullVM;
        if (!nullVM) {
            _delegate = attemptDelegate("joeq.Class.Delegates$Array");
        }
        if (_delegate == null) {
            _delegate = new NullDelegates.Array();
        }
    }

    private static Delegate attemptDelegate(String s) {
        String type = "array delegate";
        try {
            Class c = Class.forName(s);
            return (Delegate)c.newInstance();
        } catch (java.lang.ClassNotFoundException x) {
            //System.err.println("Cannot find "+type+" "+s+": "+x);
        } catch (java.lang.InstantiationException x) {
            //System.err.println("Cannot instantiate "+type+" "+s+": "+x);
        } catch (java.lang.IllegalAccessException x) {
            //System.err.println("Cannot access "+type+" "+s+": "+x);
        }
        return null;
    }
}
