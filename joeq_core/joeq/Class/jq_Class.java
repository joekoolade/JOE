// jq_Class.java, created Mon Feb  5 23:23:20 2001 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Class;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import joeq.Allocator.ObjectLayout;
import joeq.ClassLib.ClassLibInterface;
import joeq.Compiler.CompilationConstants;
import joeq.Compiler.BytecodeAnalysis.Bytecodes;
import joeq.Main.jq;
import joeq.Memory.Address;
import joeq.Memory.CodeAddress;
import joeq.Memory.HeapAddress;
import joeq.Memory.StackAddress;
import joeq.Runtime.Debug;
import joeq.Runtime.Reflection;
import joeq.Runtime.TypeCheck;
import joeq.UTF.UTFDataFormatError;
import joeq.UTF.Utf8;
import jwutil.io.Textualizer;
import jwutil.strings.Strings;
import jwutil.util.Assert;
import jwutil.util.Convert;

/**
 * jq_Class
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: jq_Class.java,v 1.90 2005/03/14 20:48:00 joewhaley Exp $
 */
public final class jq_Class extends jq_Reference implements jq_ClassFileConstants, CompilationConstants {
    
    public static /*final*/ boolean TRACE = false;
    public static /*final*/ boolean WARN_STALE_CLASS_FILES = false;
    
    //CR: used when trying to replace class contents.
    public static boolean REPLACE_CLASS = false;
    public static boolean TRACE_REPLACE_CLASS = false;
    public static List classToReplace = new java.util.LinkedList(); // a set of Strings

    /**** INTERFACE ****/
    
    //// Always available
    public final boolean isClassType() { return true; }
    public final boolean isArrayType() { return false; }
    public final boolean isAddressType() {
        return this == Address._class || this == HeapAddress._class ||
               this == CodeAddress._class || this == StackAddress._class;
        //return TypeCheck.isAssignable_noload(this, Address._class) == TypeCheck.YES;
    }
    public final String getName() { // fully-qualified name, e.g. java.lang.String
        return className(desc);
    }
    public final String shortName() { // name with package removed
        String s = desc.toString();
        int index = s.lastIndexOf('/')+1;
        if (index == 0) index = 1;
        return s.substring(index, s.length()-1);
    }
    public final boolean isInSamePackage(jq_Class that) {
        if (this.getClassLoader() != that.getClassLoader()) return false;
        String s1 = this.getName();
        String s2 = that.getName();
        int ind1 = s1.lastIndexOf('.');
        int ind2 = s2.lastIndexOf('.');
        if (ind1 != ind2) return false;
        if (ind1 != -1) {
            if (!s1.substring(0, ind1).equals(s2.substring(0, ind1)))
                return false;
        }
        return true;
    }
    public final String getJDKName() {
        return getName();
    }
    public final String getJDKDesc() {
        return desc.toString().replace('/','.');
    }
    public jq_Member getDeclaredMember(jq_NameAndDesc nd) {
        return (jq_Member)members.get(nd);
    }
    public jq_Member getDeclaredMember(String name, String desc) {
        return (jq_Member)members.get(new jq_NameAndDesc(Utf8.get(name), Utf8.get(desc)));
    }
    public Collection getMembers() {
        return members.values();
    }
    private void addDeclaredMember(jq_NameAndDesc nd, jq_Member m) {
        Object b = members.put(nd, m);
        if (TRACE) {
            Debug.writeln("Added member to "+this+": "+m+" (old value "+b+")");
            //new InternalError().printStackTrace();
        }
    }
    public void accept(jq_TypeVisitor tv) {
        tv.visitClass(this);
        super.accept(tv);
    }
    
    public static final boolean DETERMINISTIC = true;
    
    public int hashCode() {
        if (DETERMINISTIC)
            return desc.hashCode();
        else
            return System.identityHashCode(this);
    }

    //// Available only after loading
    public final int getMinorVersion() { return minor_version; }
    public final int getMajorVersion() { return major_version; }
    public final char getAccessFlags() { return access_flags; }
    public final boolean isPublic() {
        chkState(STATE_LOADING2);
        return (access_flags & ACC_PUBLIC) != 0;
    }
    public final boolean isFinal() {
        chkState(STATE_LOADING2);
        return (access_flags & ACC_FINAL) != 0;
    }
    public final boolean isSpecial() {
        chkState(STATE_LOADING2);
        return (access_flags & ACC_SUPER) != 0;
    }
    public final boolean isInterface() {
        chkState(STATE_LOADING2);
        return (access_flags & ACC_INTERFACE) != 0;
    }
    public final boolean isAbstract() {
        chkState(STATE_LOADING2);
        return (access_flags & ACC_ABSTRACT) != 0;
    }
    public final jq_Class getSuperclass() {
        chkState(STATE_LOADING3);
        return super_class;
    }
    public final int getDepth() {
        chkState(STATE_LOADED);
        if (super_class == null) return 0;
        jq_Reference t = getDirectPrimarySupertype();
        t.load();
        return 1+getDirectPrimarySupertype().getDepth();
    }
    public final jq_Reference getDirectPrimarySupertype() {
        chkState(STATE_LOADING3);
        if (this.isInterface()) return PrimordialClassLoader.getJavaLangObject();
        return super_class;
    }
    public final jq_Class[] getDeclaredInterfaces() {
        chkState(STATE_LOADING3);
        return declared_interfaces;
    }
    public final jq_Class getDeclaredInterface(Utf8 desc) {
        chkState(STATE_LOADING3);
        for (int i=0; i<declared_interfaces.length; ++i) {
            jq_Class in = declared_interfaces[i];
            if (in.getDesc() == desc)
                return in;
        }
        return null;
    }
    public final jq_InstanceField[] getDeclaredInstanceFields() {
        chkState(STATE_LOADING3);
        return declared_instance_fields;
    }
    public final jq_InstanceField getDeclaredInstanceField(jq_NameAndDesc nd) {
        chkState(STATE_LOADING3);
        return (jq_InstanceField)findByNameAndDesc(declared_instance_fields, nd);
    }
    public final void setDeclaredInstanceFields(jq_InstanceField[] dif) {
        chkState(STATE_LOADED);
        declared_instance_fields = dif;
    }
    public final jq_StaticField[] getDeclaredStaticFields() {
        chkState(STATE_LOADING3);
        return static_fields;
    }
    public final jq_StaticField getDeclaredStaticField(jq_NameAndDesc nd) {
        chkState(STATE_LOADING3);
        return (jq_StaticField)findByNameAndDesc(static_fields, nd);
    }
    public final void setDeclaredStaticFields(jq_StaticField[] dsf) {
        chkState(STATE_LOADED);
        static_fields = dsf;
    }

    public final jq_StaticField getStaticField(jq_NameAndDesc nd) {
        chkState(STATE_LOADING3);
        jq_StaticField f = (jq_StaticField)findByNameAndDesc(static_fields, nd);
        if (f != null) return f;
        
        // check superclasses.
        if (super_class != null) {
            super_class.load();
            f = super_class.getStaticField(nd);
            if (f != null) return f;
        }
        
        // static fields may be in implemented interfaces.
        for (int i=0; i<declared_interfaces.length; ++i) {
            jq_Class in = declared_interfaces[i];
            in.load();
            f = in.getStaticField(nd);
            if (f != null) return f;
        }

        return null;
    }

    public final int getNumberOfStaticFields() {
        chkState(STATE_LOADED);
        int length = static_fields.length;
        if (this.isInterface()) {
            for (int i=0; i<declared_interfaces.length; ++i) {
                jq_Class in = declared_interfaces[i];
                in.load();
                length += in.getNumberOfStaticFields();
            }
        }
        if (super_class != null) {
            super_class.load();
            length += super_class.getNumberOfStaticFields();
        }
        return length;
    }

    private int getStaticFields_helper(jq_StaticField[] sfs, int current) {
        System.arraycopy(static_fields, 0, sfs, current, static_fields.length);
        current += static_fields.length;
        if (this.isInterface()) {
            for (int i=0; i<declared_interfaces.length; ++i) {
                jq_Class in = declared_interfaces[i];
                current = in.getStaticFields_helper(sfs, current);
            }
        }
        if (super_class != null) {
            current = super_class.getStaticFields_helper(sfs, current);
        }
        return current;
    }

    // NOTE: fields in superinterfaces may appear multiple times.
    public final jq_StaticField[] getStaticFields() {
        chkState(STATE_LOADING3);
        int length = this.getNumberOfStaticFields();
        jq_StaticField[] sfs = new jq_StaticField[length];
        int current = this.getStaticFields_helper(sfs, 0);
        Assert._assert(current == sfs.length);
        return sfs;
    }

    public final jq_InstanceMethod[] getDeclaredInstanceMethods() {
        chkState(STATE_LOADING3);
        return declared_instance_methods;
    }
    public final jq_InstanceMethod getDeclaredInstanceMethod(jq_NameAndDesc nd) {
        chkState(STATE_LOADING3);
        return (jq_InstanceMethod)findByNameAndDesc(declared_instance_methods, nd);
    }
    public final void setDeclaredInstanceMethods(jq_InstanceMethod[] dim) {
        chkState(STATE_LOADED);
        declared_instance_methods = dim;
    }
    public final jq_StaticMethod[] getDeclaredStaticMethods() {
        chkState(STATE_LOADING3);
        return static_methods;
    }
    public final jq_StaticMethod getDeclaredStaticMethod(jq_NameAndDesc nd) {
        chkState(STATE_LOADING3);
        return (jq_StaticMethod)findByNameAndDesc(static_methods, nd);
    }
    public final void setDeclaredStaticMethods(jq_StaticMethod[] dsm) {
        chkState(STATE_LOADED);
        static_methods = dsm;
    }
    public final jq_StaticMethod getStaticMethod(jq_NameAndDesc nd) {
        chkState(STATE_LOADING3);
        jq_StaticMethod m = (jq_StaticMethod)findByNameAndDesc(static_methods, nd);
        if (m != null) return m;
        // check superclasses.
        if (super_class != null) {
            super_class.load();
            m = super_class.getStaticMethod(nd);
            if (m != null) return m;
        }
        // static methods may also be in superinterfaces.
        for (int i=0; i<declared_interfaces.length; ++i) {
            jq_Class in = declared_interfaces[i];
            in.load();
            m = in.getStaticMethod(nd);
            if (m != null) return m;
        }
        return null;
    }

    public final int getNumberOfStaticMethods() {
        chkState(STATE_LOADED);
        int length = static_methods.length;
        for (int i=0; i<declared_interfaces.length; ++i) {
            jq_Class in = declared_interfaces[i];
            in.load();
            length += in.getNumberOfStaticMethods();
        }
        if (super_class != null) {
            super_class.load();
            length += super_class.getNumberOfStaticMethods();
        }
        return length;
    }

    private int getStaticMethods_helper(jq_StaticMethod[] sfs, int current) {
        System.arraycopy(static_methods, 0, sfs, current, static_methods.length);
        current += static_methods.length;
        for (int i=0; i<declared_interfaces.length; ++i) {
            jq_Class in = declared_interfaces[i];
            current = in.getStaticMethods_helper(sfs, current);
        }
        if (super_class != null) {
            current = super_class.getStaticMethods_helper(sfs, current);
        }
        return current;
    }

    // NOTE: methods in superinterfaces may appear multiple times.
    public final jq_StaticMethod[] getStaticMethods() {
        chkState(STATE_LOADED);
        int length = this.getNumberOfStaticMethods();
        jq_StaticMethod[] sfs = new jq_StaticMethod[length];
        int current = this.getStaticMethods_helper(sfs, 0);
        Assert._assert(current == sfs.length);
        return sfs;
    }

    public final jq_InstanceMethod getInstanceMethod(jq_NameAndDesc nd) {
        chkState(STATE_LOADING3);
        jq_InstanceMethod m = (jq_InstanceMethod)findByNameAndDesc(declared_instance_methods, nd);
        if (m != null) return m;
        // check superclasses.
        if (super_class != null) {
            super_class.load();
            m = super_class.getInstanceMethod(nd);
            if (m != null) return m;
        }
        // check superinterfaces.
        for (int i=0; i<declared_interfaces.length; ++i) {
            jq_Class in = declared_interfaces[i];
            in.load();
            m = in.getInstanceMethod(nd);
            if (m != null) return m;
        }
        return null;
    }
    public final jq_Initializer getInitializer(Utf8 desc) {
        return getInitializer(new jq_NameAndDesc(Utf8.get("<init>"), desc));
    }
    public final jq_Initializer getInitializer(jq_NameAndDesc nd) {
        chkState(STATE_LOADING3);
        return (jq_Initializer)getDeclaredInstanceMethod(nd);
    }
    public final jq_ClassInitializer getClassInitializer() {
        chkState(STATE_LOADING3);
        return (jq_ClassInitializer)getDeclaredStaticMethod(new jq_NameAndDesc(Utf8.get("<clinit>"), Utf8.get("()V")));
    }
    
    public jq_Field getDeclaredField(String name) {
        return getDeclaredField(Utf8.get(name));
    }
    
    public jq_Field getDeclaredField(Utf8 name) {
        chkState(STATE_LOADED);
        for (int i=0; i<declared_instance_fields.length; ++i) {
            jq_Field m = declared_instance_fields[i];
            if (m.getName() == name) return m;
        }
        for (int i=0; i<static_fields.length; ++i) {
            jq_Field m = static_fields[i];
            if (m.getName() == name) return m;
        }
        return null;
    }
    
    public jq_Method getDeclaredMethod(String name) {
        return getDeclaredMethod(Utf8.get(name));
    }
    
    public jq_Method getDeclaredMethod(Utf8 name) {
        chkState(STATE_LOADED);
        for (int i=0; i<declared_instance_methods.length; ++i) {
            jq_Method m = declared_instance_methods[i];
            if (m.getName() == name) return m;
        }
        for (int i=0; i<static_methods.length; ++i) {
            jq_Method m = static_methods[i];
            if (m.getName() == name) return m;
        }
        return null;
    }
    
    public jq_Method getMethodContainingLine(char lineNum) {
        chkState(STATE_LOADED);
        for (int i=0; i<declared_instance_methods.length; ++i) {
            jq_Method m = declared_instance_methods[i];
            jq_LineNumberBC a = m.getLineNumber(lineNum);
            if (a != null) return m;
        }
        for (int i=0; i<static_methods.length; ++i) {
            jq_Method m = static_methods[i];
            jq_LineNumberBC a = m.getLineNumber(lineNum);
            if (a != null) return m;
        }
        return null;
    }
    

    public final jq_ConstantPool getCP() {
        chkState(STATE_LOADING2);
        return const_pool;
    }
    public final void setCP(jq_ConstantPool cp) {
        this.const_pool = cp;
    }
    public final Object getCP(char index) {
        chkState(STATE_LOADING2);
        return const_pool.get(index);
    }
    public final int getCPCount() {
        chkState(STATE_LOADING2);
        return const_pool.getCount();
    }
    public final byte getCPtag(char index) {
        chkState(STATE_LOADING2);
        return const_pool.getTag(index);
    }
    public final Integer getCPasInt(char index) {
        chkState(STATE_LOADING2);
        return const_pool.getAsInt(index);
    }
    public final Float getCPasFloat(char index) {
        chkState(STATE_LOADING2);
        return const_pool.getAsFloat(index);
    }
    public final Long getCPasLong(char index) {
        chkState(STATE_LOADING2);
        return const_pool.getAsLong(index);
    }
    public final Double getCPasDouble(char index) {
        chkState(STATE_LOADING2);
        return const_pool.getAsDouble(index);
    }
    public final String getCPasString(char index) {
        chkState(STATE_LOADING2);
        return const_pool.getAsString(index);
    }
    public final Object getCPasObjectConstant(char index) {
        chkState(STATE_LOADING2);
        return const_pool.getAsObjectConstant(index);
    }
    public final Utf8 getCPasUtf8(char index) {
        chkState(STATE_LOADING2);
        return const_pool.getAsUtf8(index);
    }
    public final jq_Type getCPasType(char index) {
        chkState(STATE_LOADING2);
        return const_pool.getAsType(index);
    }
    public final jq_Member getCPasMember(char index) {
        chkState(STATE_LOADING2);
        return const_pool.getAsMember(index);
    }
    public jq_StaticField getOrCreateStaticField(String name, String desc) {
        return getOrCreateStaticField(new jq_NameAndDesc(Utf8.get(name), Utf8.get(desc)));
    }
    public jq_StaticField getOrCreateStaticField(jq_NameAndDesc nd) {
        jq_StaticField sf = (jq_StaticField)getDeclaredMember(nd);
        if (sf != null) return sf;
        return createStaticField(nd);
    }
    jq_StaticField createStaticField(jq_NameAndDesc nd) {
        Assert._assert(getDeclaredMember(nd) == null);
        jq_StaticField f = jq_StaticField.newStaticField(this, nd);
        addDeclaredMember(nd, f);
        return f;
    }
    public final jq_StaticField getCPasStaticField(char index) {
        chkState(STATE_LOADING2);
        return const_pool.getAsStaticField(index);
    }
    public jq_InstanceField getOrCreateInstanceField(String name, String desc) {
        return getOrCreateInstanceField(new jq_NameAndDesc(Utf8.get(name), Utf8.get(desc)));
    }
    public jq_InstanceField getOrCreateInstanceField(jq_NameAndDesc nd) {
        jq_InstanceField sf = (jq_InstanceField)getDeclaredMember(nd);
        if (sf != null) return sf;
        return createInstanceField(nd);
    }
    jq_InstanceField createInstanceField(jq_NameAndDesc nd) {
        Assert._assert(getDeclaredMember(nd) == null);
        jq_InstanceField f = jq_InstanceField.newInstanceField(this, nd);
        addDeclaredMember(nd, f);
        return f;
    }
    public final jq_InstanceField getCPasInstanceField(char index) {
        chkState(STATE_LOADING2);
        return const_pool.getAsInstanceField(index);
    }
    public jq_StaticMethod getOrCreateStaticMethod(String name, String desc) {
        return getOrCreateStaticMethod(new jq_NameAndDesc(Utf8.get(name), Utf8.get(desc)));
    }
    public jq_StaticMethod getOrCreateStaticMethod(jq_NameAndDesc nd) {
        jq_StaticMethod sf = (jq_StaticMethod)getDeclaredMember(nd);
        if (sf != null) return sf;
        return createStaticMethod(nd);
    }
    jq_StaticMethod createStaticMethod(jq_NameAndDesc nd) {
        Assert._assert(getDeclaredMember(nd) == null);
        jq_StaticMethod f;
        if (nd.getName() == Utf8.get("<clinit>") &&
            nd.getDesc() == Utf8.get("()V")) {
            f = jq_ClassInitializer.newClassInitializer(this, nd);
        } else {
            f = jq_StaticMethod.newStaticMethod(this, nd);
        }
        addDeclaredMember(nd, f);
        return f;
    }
    public final jq_StaticMethod getCPasStaticMethod(char index) {
        chkState(STATE_LOADING2);
        return const_pool.getAsStaticMethod(index);
    }
    public jq_InstanceMethod getOrCreateInstanceMethod(String name, String desc) {
        return getOrCreateInstanceMethod(new jq_NameAndDesc(Utf8.get(name), Utf8.get(desc)));
    }
    public jq_InstanceMethod getOrCreateInstanceMethod(jq_NameAndDesc nd) {
        jq_InstanceMethod sf = (jq_InstanceMethod)getDeclaredMember(nd);
        if (sf != null) return sf;
        return createInstanceMethod(nd);
    }
    jq_InstanceMethod createInstanceMethod(jq_NameAndDesc nd) {
        Assert._assert(getDeclaredMember(nd) == null);
        jq_InstanceMethod f;
        if (nd.getName() == Utf8.get("<init>")) {
            f = jq_Initializer.newInitializer(this, nd);
        } else {
            f = jq_InstanceMethod.newInstanceMethod(this, nd);
        }
        addDeclaredMember(nd, f);
        return f;
    }
    public final jq_InstanceMethod getCPasInstanceMethod(char index) {
        chkState(STATE_LOADING2);
        return const_pool.getAsInstanceMethod(index);
    }
    public final byte[] getAttribute(Utf8 name) {
        chkState(STATE_LOADING3);
        return (byte[])attributes.get(name);
    }
    public final byte[] getAttribute(String name) {
        chkState(STATE_LOADING3);
        return getAttribute(Utf8.get(name));
    }
    public final Iterator getAttributes() {
        return attributes.keySet().iterator();
    }
    public final Utf8 getSourceFile() {
        chkState(STATE_LOADING3);
        byte[] attrib = getAttribute("SourceFile");
        if (attrib == null) return null;
        if (attrib.length != 2)
            throw new ClassFormatError();
        char cpi = Convert.twoBytesToChar(attrib, 0);
        if (getCPtag(cpi) != CONSTANT_Utf8)
            throw new ClassFormatError("cp tag "+(int)cpi+" is "+(int)getCPtag(cpi));
        return getCPasUtf8(cpi);
    }
    public final boolean isSynthetic() {
        chkState(STATE_LOADING3);
        return getAttribute("Synthetic") != null;
    }
    public final boolean isDeprecated() {
        chkState(STATE_LOADING3);
        return getAttribute("Deprecated") != null;
    }
    public final void removeAttribute(String s) {
        removeAttribute(Utf8.get(s));
    }
    public final void removeAttribute(Utf8 u) {
        attributes.remove(u);
    }
    public final jq_Class[] getInnerClasses() {
        chkState(STATE_LOADING3);
        Assert.TODO();
        return null;
    }
    public final jq_Class[] getSubClasses() {
        chkState(STATE_LOADING3);
        return subclasses;
    }
    public final jq_Class[] getSubInterfaces() {
        chkState(STATE_LOADING3);
        return subinterfaces;
    }
   //// Available after resolving
    public final jq_Class[] getInterfaces() {
        chkState(STATE_PREPARED);
        return interfaces;
    }
    public final jq_Class getInterface(Utf8 desc) {
        chkState(STATE_PREPARED);
        for (int i=0; i<interfaces.length; ++i) {
            jq_Class in = interfaces[i];
            if (in.getDesc() == desc)
                return in;
        }
        return null;
    }
    public final boolean implementsInterface(jq_Class k) {
        chkState(STATE_PREPARED);
        for (int i=0; i<interfaces.length; ++i) {
            if (interfaces[i] == k)
                return true;
        }
        if (false) {
            for (int i=0; i<interfaces.length; ++i) {
                jq_Class k2 = interfaces[i];
                k2.prepare();
                if (k2.implementsInterface(k))
                    return true;
            }
        }
        return false;
    }
    public final jq_InstanceField[] getInstanceFields() {
        chkState(STATE_PREPARED);
        return instance_fields;
    }
    public final int[] getReferenceOffsets() {
        chkState(STATE_PREPARED);
        return reference_offsets;
    }
    public final jq_InstanceField getInstanceField(jq_NameAndDesc nd) {
        chkState(STATE_LOADING3);
        jq_InstanceField m = (jq_InstanceField)findByNameAndDesc(declared_instance_fields, nd);
        if (m != null) return m;
        // check superclasses.
        if (super_class != null) {
            super_class.load();
            m = super_class.getInstanceField(nd);
            if (m != null) return m;
        }
        return null;
    }
    public final jq_InstanceMethod[] getVirtualMethods() {
        chkState(STATE_PREPARED);
        return virtual_methods;
    }
    public final jq_InstanceMethod getVirtualMethod(jq_NameAndDesc nd) {
        chkState(STATE_PREPARED);
        return (jq_InstanceMethod)findByNameAndDesc(virtual_methods, nd);
    }
    public final int getInstanceSize() {
        chkState(STATE_PREPARED);
        return instance_size;
    }
    
    public final int[] getStaticData() {
        chkState(STATE_SFINITIALIZED);
        return static_data;
    }
    public final void setStaticData(jq_StaticField sf, int data) {
        chkState(STATE_SFINITIALIZED);
        Assert._assert(sf.getDeclaringClass() == this);
        Assert._assert(sf.getType().getReferenceSize() != 8);
        int index = sf.getAddress().difference(HeapAddress.addressOf(static_data)) >> 2;
        if (index < 0 || index >= static_data.length) {
            Assert.UNREACHABLE("sf: "+sf+" index: "+index);
        }
        static_data[index] = data;
    }
    public final void setStaticData(jq_StaticField sf, long data) {
        chkState(STATE_SFINITIALIZED);
        Assert._assert(sf.getDeclaringClass() == this);
        Assert._assert(sf.getType().getReferenceSize() == 8);
        int index = sf.getAddress().difference(HeapAddress.addressOf(static_data)) >> 2;
        static_data[index  ] = (int)(data);
        static_data[index+1] = (int)(data >> 32);
    }
    public final void setStaticData(jq_StaticField sf, float data) {
        setStaticData(sf, Float.floatToRawIntBits(data));
    }
    public final void setStaticData(jq_StaticField sf, double data) {
        setStaticData(sf, Double.doubleToRawLongBits(data));
    }
    public final void setStaticData(jq_StaticField sf, Object data) {
        chkState(STATE_SFINITIALIZED);
        Assert._assert(sf.getDeclaringClass() == this);
        Assert._assert(sf.getType().getReferenceSize() != 8);
        int index = sf.getAddress().difference(HeapAddress.addressOf(static_data)) >> 2;
        static_data[index] = HeapAddress.addressOf(data).to32BitValue();
    }
    public final void setStaticData(jq_StaticField sf, Address data) {
        chkState(STATE_SFINITIALIZED);
        Assert._assert(sf.getDeclaringClass() == this);
        Assert._assert(sf.getType().getReferenceSize() != 8);
        int index = sf.getAddress().difference(HeapAddress.addressOf(static_data)) >> 2;
        static_data[index] = data.to32BitValue();
    }

    public final Object newInstance() {
        this.prepare(); // prepare(): to set instance_size and vtable
        return _delegate.newInstance(this, instance_size, vtable);
    }
    
    //// Implementation garbage.
    private Map/*<jq_NameAndDesc->jq_Member>*/ members;
    private char minor_version;
    private char major_version;
    private char access_flags;
    private jq_Class super_class;
    private jq_Class[] subclasses, subinterfaces;
    private jq_Class[] declared_interfaces, interfaces;
    private jq_StaticField[] static_fields;
    private jq_StaticMethod[] static_methods;
    private jq_InstanceField[] declared_instance_fields;
    private jq_InstanceMethod[] declared_instance_methods;
    private Map attributes;
    private jq_ConstantPool const_pool;
    private int static_data_size;
    private int instance_size;
    private jq_InstanceField[] instance_fields;
    private int[] reference_offsets;
    private jq_InstanceMethod[] virtual_methods;
    private int[] static_data;
    private boolean dont_align;

    private static jq_Member findByNameAndDesc(jq_Member[] array, jq_NameAndDesc nd) {
        // linear search
        for (int i=0; i<array.length; ++i) {
            jq_Member m = array[i];
            if (m.getNameAndDesc().equals(nd)) return m;
        }
        return null;
    }
    
    /**
     * Private constructor.
     * Use a ClassLoader to create a jq_Class instance.
     */
    private jq_Class(ClassLoader class_loader, Utf8 desc) {
        super(desc, class_loader);
        this.subclasses = new jq_Class[0];
        this.subinterfaces = new jq_Class[0];
        this.members = new HashMap();
    }
    // ONLY TO BE CALLED BY ClassLoader!!!
    public static jq_Class newClass(ClassLoader classLoader, Utf8 desc) {
        Assert._assert(desc.isDescriptor(TC_CLASS));
        return new jq_Class(classLoader, desc);
    }

    /**
     * Loads the binary data for this class.  See Jvm spec 2.17.2.
     *
     * Throws: ClassFormatError  if the binary data is malformed in some way
     *         UnsupportedClassVersionError  if the binary data is of an unsupported version
     *         ClassCircularityError  if it would be its own superclass or superinterface
     *         NoClassDefFoundError  if the class definition cannot be found
     */
    public void load()
    throws ClassFormatError, UnsupportedClassVersionError, ClassCircularityError, NoClassDefFoundError {
        if (isLoaded()) return; // quick test
        Assert._assert(class_loader == PrimordialClassLoader.loader);
        DataInputStream in = null;
        try {
            in = ((PrimordialClassLoader)class_loader).getClassFileStream(desc);
            if (in == null) throw new NoClassDefFoundError(className(desc));
            try {
                load(in);
            } catch (NoClassDefFoundError x) {
                x.printStackTrace();
                Assert.UNREACHABLE("Class not found error while attempting to load class!");
            }
            in.close();
        } catch (IOException x) {
            x.printStackTrace(); // for debugging
            throw new ClassFormatError(x.toString());
        } finally {
            try { if (in != null) in.close(); } catch (IOException _) { }
        }
    }
    public void load(DataInput in)
    throws ClassFormatError, UnsupportedClassVersionError, ClassCircularityError {
        if (isLoaded()) return; // quick test.
        if (state == STATE_LOADERROR) throw new ClassFormatError();
        synchronized (this) {
            if (isLoaded()) return; // other thread already loaded this type.
            if ((state == STATE_LOADING1) || (state == STATE_LOADING2) || (state == STATE_LOADING3))
                throw new ClassCircularityError(this.toString()); // recursively called load (?)
            state = STATE_LOADING1;
            if (TRACE) Debug.writeln("Beginning loading "+this+"...");
            try {
                int magicNum = in.readInt(); // 0xCAFEBABE
                if (magicNum != 0xCAFEBABE)
                    throw new ClassFormatError("bad magic number: "+Integer.toHexString(magicNum));
                minor_version = (char)in.readUnsignedShort(); // 3 or 0
                major_version = (char)in.readUnsignedShort(); // 45 to 48
                if (((major_version != 45) || (minor_version != 0)) &&
                    ((major_version != 45) || (minor_version != 3)) &&
                    ((major_version != 46) || (minor_version != 0)) &&
                    ((major_version != 47) || (minor_version != 0)) &&
                    ((major_version != 48) || (minor_version != 0)) &&
                    ((major_version != 49) || (minor_version != 0))) {
                    throw new UnsupportedClassVersionError("unsupported version "+(int)major_version+"."+(int)minor_version);
                }

                char constant_pool_count = (char)in.readUnsignedShort();
                const_pool = new jq_ConstantPool(constant_pool_count);
                // read in the constant pool
                const_pool.load(in);
                // resolve the non-primitive stuff
                try {
                    const_pool.resolve(class_loader);
                } catch (NoSuchMethodError x) {
                    throw new NoSuchMethodError("In class "+this+": "+x.getMessage());
                } catch (NoSuchFieldError x) {
                    throw new NoSuchFieldError("In class "+this+": "+x.getMessage());
                }
                
                access_flags = (char)in.readUnsignedShort();
                state = STATE_LOADING2;
                char selfindex = (char)in.readUnsignedShort();
                if (getCPtag(selfindex) != CONSTANT_ResolvedClass) {
                    throw new ClassFormatError("constant pool entry "+(int)selfindex+", referred to by field this_class" +
                                               ", is wrong type tag (expected="+CONSTANT_Class+", actual="+getCPtag(selfindex)+")");
                }
                if (getCP(selfindex) != this && !this.getDesc().toString().startsWith("LREPLACE")) {
                    throw new ClassFormatError("expected class "+this+" but found class "+getCP(selfindex));
                }
                char superindex = (char)in.readUnsignedShort();
                if (superindex != 0) {
                    if (getCPtag(superindex) != CONSTANT_ResolvedClass) {
                        throw new ClassFormatError("constant pool entry "+(int)superindex+", referred to by field super_class" +
                                                   ", is wrong type tag (expected="+CONSTANT_Class+", actual="+getCPtag(superindex)+")");
                    }
                    jq_Type super_type = getCPasType(superindex);
                    if (!super_type.isClassType()) {
                        throw new ClassFormatError("superclass ("+super_class.getName()+") is not a class type");
                    }
                    if (super_type == this) {
                        throw new ClassCircularityError(this.getName()+" has itself as a superclass!");
                    }
                    super_class = (jq_Class)super_type;
                    super_class.addSubclass(this);
                } else {
                    // no superclass --> java.lang.Object
                    if (PrimordialClassLoader.getJavaLangObject() != this) {
                        throw new ClassFormatError("no superclass listed for class "+this);
                    }
                }
                char n_interfaces = (char)in.readUnsignedShort();
                declared_interfaces = new jq_Class[n_interfaces];
                for (int i=0; i<n_interfaces; ++i) {
                    char interface_index = (char)in.readUnsignedShort();
                    if (getCPtag(interface_index) != CONSTANT_ResolvedClass) {
                        throw new ClassFormatError("constant pool entry "+(int)interface_index+", referred to by interfaces["+i+"]"+
                                                   ", is wrong type tag (expected="+CONSTANT_Class+", actual="+getCPtag(interface_index)+")");
                    }
                    declared_interfaces[i] = (jq_Class)getCPasType(interface_index);
                    if (!declared_interfaces[i].isClassType()) {
                        throw new ClassFormatError("implemented interface ("+super_class.getName()+") is not a class type");
                    }
                    if (declared_interfaces[i].isLoaded() && !declared_interfaces[i].isInterface()) {
                        throw new ClassFormatError("implemented interface ("+super_class.getName()+") is not an interface type");
                    }
                    if (declared_interfaces[i] == jq_DontAlign._class) dont_align = true;
                    declared_interfaces[i].addSubinterface(this);
                }

                char n_declared_fields = (char)in.readUnsignedShort();
                char[] temp_declared_field_flags = new char[n_declared_fields];
                jq_Field[] temp_declared_fields = new jq_Field[n_declared_fields];
                int numStaticFields = 0, numInstanceFields = 0;
                for (int i=0; i<n_declared_fields; ++i) {
                    temp_declared_field_flags[i] = (char)in.readUnsignedShort();
                    // TODO: check flags for validity.
                    char field_name_index = (char)in.readUnsignedShort();
                    if (getCPtag(field_name_index) != CONSTANT_Utf8)
                        throw new ClassFormatError("constant pool entry "+(int)field_name_index+", referred to by field "+i+
                                                   ", is wrong type tag (expected="+CONSTANT_Utf8+", actual="+getCPtag(field_name_index)+")");
                    Utf8 field_name = getCPasUtf8(field_name_index);
                    char field_desc_index = (char)in.readUnsignedShort();
                    if (getCPtag(field_desc_index) != CONSTANT_Utf8)
                        throw new ClassFormatError("constant pool entry "+(int)field_desc_index+", referred to by field "+i+
                                                   ", is wrong type tag (expected="+CONSTANT_Utf8+", actual="+getCPtag(field_desc_index)+")");
                    Utf8 field_desc = getCPasUtf8(field_desc_index);
                    if (!field_desc.isValidTypeDescriptor())
                        throw new ClassFormatError(field_desc+" is not a valid type descriptor");
                    jq_NameAndDesc nd = new jq_NameAndDesc(field_name, field_desc);
                    jq_Field field = (jq_Field)getDeclaredMember(nd);
                    if ((temp_declared_field_flags[i] & ACC_STATIC) != 0) {
                        if (field == null) {
                            field = createStaticField(nd);
                        } else if (!field.isStatic())
                            throw new VerifyError("static field "+field+" was referred to as an instance field");
                        ++numStaticFields;
                    } else {
                        if (field == null) {
                            field = createInstanceField(nd);
                        } else if (field.isStatic())
                            throw new VerifyError("instance field "+field+" was referred to as a static field");
                        ++numInstanceFields;
                    }
                    field.load(temp_declared_field_flags[i], in);
                    temp_declared_fields[i] = field;
                }
                static_data_size = 0;
                declared_instance_fields = new jq_InstanceField[numInstanceFields];
                static_fields = new jq_StaticField[numStaticFields];
                for (int i=0, di=-1, si=-1; i<n_declared_fields; ++i) {
                    if ((temp_declared_field_flags[i] & ACC_STATIC) != 0) {
                        static_fields[++si] = (jq_StaticField)temp_declared_fields[i];
                        static_data_size += static_fields[si].getWidth();
                    } else {
                        declared_instance_fields[++di] = (jq_InstanceField)temp_declared_fields[i];
                    }
                }
                if (!dont_align) {
                    // sort instance fields in reverse by their size.
                    Arrays.sort(declared_instance_fields, new Comparator() {
                        public int compare(jq_InstanceField o1, jq_InstanceField o2) {
                            int s1 = o1.getSize(), s2 = o2.getSize();
                            if (s1 > s2) return -1;
                            else if (s1 < s2) return 1;
                            else return 0;
                        }
                        public int compare(Object o1, Object o2) {
                            return compare((jq_InstanceField)o1, (jq_InstanceField)o2);
                        }
                    });
                }

                char n_declared_methods = (char)in.readUnsignedShort();
                char[] temp_declared_method_flags = new char[n_declared_methods];
                jq_Method[] temp_declared_methods = new jq_Method[n_declared_methods];
                int numStaticMethods = 0, numInstanceMethods = 0;
                for (int i=0; i<n_declared_methods; ++i) {
                    temp_declared_method_flags[i] = (char)in.readUnsignedShort();
                    // TODO: check flags for validity.
                    char method_name_index = (char)in.readUnsignedShort();
                    if (getCPtag(method_name_index) != CONSTANT_Utf8)
                        throw new ClassFormatError("constant pool entry "+(int)method_name_index+", referred to by method "+i+
                                                   ", is wrong type tag (expected="+CONSTANT_Utf8+", actual="+getCPtag(method_name_index)+")");
                    Utf8 method_name = getCPasUtf8(method_name_index);
                    char method_desc_index = (char)in.readUnsignedShort();
                    if (getCPtag(method_desc_index) != CONSTANT_Utf8)
                        throw new ClassFormatError("constant pool entry "+(int)method_desc_index+", referred to by method "+i+
                                                   ", is wrong type tag (expected="+CONSTANT_Utf8+", actual="+getCPtag(method_desc_index)+")");
                    Utf8 method_desc = getCPasUtf8(method_desc_index);
                    if (!method_desc.isValidMethodDescriptor())
                        throw new ClassFormatError(method_desc+" is not a valid method descriptor");
                    jq_NameAndDesc nd = new jq_NameAndDesc(method_name, method_desc);
                    jq_Method method = (jq_Method)getDeclaredMember(nd);
                    if ((temp_declared_method_flags[i] & ACC_STATIC) != 0) {
                        if (method == null) {
                            method = createStaticMethod(nd);
                        } else if (!method.isStatic())
                            throw new VerifyError();
                        ++numStaticMethods;
                    } else {
                        if (method == null) {
                            method = createInstanceMethod(nd);
                        } else if (method.isStatic())
                            throw new VerifyError();
                        ++numInstanceMethods;
                    }
                    method.load(temp_declared_method_flags[i], in);
                    temp_declared_methods[i] = method;
                }
                declared_instance_methods = new jq_InstanceMethod[numInstanceMethods];
                static_methods = new jq_StaticMethod[numStaticMethods];
                for (int i=0, di=-1, si=-1; i<n_declared_methods; ++i) {
                    if ((temp_declared_method_flags[i] & ACC_STATIC) != 0) {
                        static_methods[++si] = (jq_StaticMethod)temp_declared_methods[i];
                    } else {
                        declared_instance_methods[++di] = (jq_InstanceMethod)temp_declared_methods[i];
                    }
                }
                // now read class attributes
                attributes = new HashMap();
                readAttributes(in, attributes);

                state = STATE_LOADING3;
                
                // if this is a class library, look for and load our mirror (implementation) class
                // CR: il essaye tous les chemins possibles ou le mirror pourrait se trouver. Ie. ../common/.. ou sun_13/.. par ex.
                Iterator impls = ClassLibInterface.DEFAULT.getImplementationClassDescs(getDesc());
                while (impls.hasNext()) {
                    Utf8 impl_utf = (Utf8)impls.next();
                    jq_Class mirrorclass = (jq_Class)PrimordialClassLoader.getOrCreateType(class_loader, impl_utf);
                    try {
                        if (TRACE) Debug.writeln("Attempting to load mirror class "+mirrorclass);
                        mirrorclass.load();
                    } catch (NoClassDefFoundError x) {
                        // no mirror class
                        PrimordialClassLoader.unloadType(class_loader, mirrorclass);
                        mirrorclass = null;
                    }
                    if (mirrorclass != null) {
                        this.merge(mirrorclass);
                    }
                }
                
                // if this is in the class library, remap method bodies.
                if (this.isInClassLib()) {
                    if (TRACE) Debug.writeln(this+" is in the class library, rewriting method bodies.");
                    final jq_ConstantPool.ConstantPoolRebuilder cpr = this.rebuildConstantPool(false);
                    // visit instance fields
                    for (int i=0; i<this.declared_instance_fields.length; ++i) {
                        jq_InstanceField this_m = this.declared_instance_fields[i];
                        jq_NameAndDesc nd = ClassLibInterface.convertClassLibNameAndDesc(this, this_m.getNameAndDesc());
                        if (this_m.getNameAndDesc() != nd) {
                            if (TRACE) Debug.writeln("Rewriting field signature from "+this_m.getNameAndDesc()+" to "+nd);
                            jq_InstanceField this_m2 = getOrCreateInstanceField(nd);
                            this_m2.load(this_m);
                            this_m.unload(); Object b = this.members.remove(this_m.getNameAndDesc()); cpr.remove(this_m);
                            if (TRACE) Debug.writeln("Removed member "+this_m.getNameAndDesc()+" from member set of "+this+": "+b);
                            this.addDeclaredMember(nd, this_m2);
                            this_m = declared_instance_fields[i] = this_m2;
                        }
                    }
                    // visit static fields
                    for (int i=0; i<this.static_fields.length; ++i) {
                        jq_StaticField this_m = this.static_fields[i];
                        jq_NameAndDesc nd = ClassLibInterface.convertClassLibNameAndDesc(this, this_m.getNameAndDesc());
                        if (this_m.getNameAndDesc() != nd) {
                            if (TRACE) Debug.writeln("Rewriting field signature from "+this_m.getNameAndDesc()+" to "+nd);
                            jq_StaticField this_m2 = getOrCreateStaticField(nd);
                            this_m2.load(this_m);
                            this_m.unload(); Object b = this.members.remove(this_m.getNameAndDesc()); cpr.remove(this_m);
                            if (TRACE) Debug.writeln("Removed member "+this_m.getNameAndDesc()+" from member set of "+this+": "+b);
                            this.addDeclaredMember(nd, this_m2);
                            this_m = static_fields[i] = this_m2;
                        }
                    }
                    // visit all instance methods.
                    LinkedHashMap newInstanceMethods = new LinkedHashMap();
                    for (int i=0; i<this.declared_instance_methods.length; ++i) {
                        jq_InstanceMethod this_m = this.declared_instance_methods[i];
                        jq_NameAndDesc nd = ClassLibInterface.convertClassLibNameAndDesc(this, this_m.getNameAndDesc());
                        if (this_m.getNameAndDesc() != nd) {
                            if (TRACE) Debug.writeln("Rewriting method signature from "+this_m.getNameAndDesc()+" to "+nd);
                            jq_InstanceMethod this_m2 = getOrCreateInstanceMethod(nd);
                            this_m2.load(this_m);
                            this_m.unload(); Object b = this.members.remove(this_m.getNameAndDesc()); cpr.remove(this_m);
                            if (TRACE) Debug.writeln("Removed member "+this_m.getNameAndDesc()+" from member set of "+this+": "+b);
                            this.addDeclaredMember(nd, this_m2);
                            this_m = this_m2;
                        }
                        byte[] bc = this_m.getBytecode();
                        Bytecodes.InstructionList il;
                        if (bc == null) {
                            il = null;
                        } else {
                            // extract instructions of method.
                            if (TRACE) Debug.writeln("Extracting instructions of "+this_m);
                            il = new Bytecodes.InstructionList(this_m);

                            // update constant pool references in the instructions, and add them to our constant pool.
                            rewriteMethod(cpr, il);
                        }
                        
                        // cache the instruction list for later.
                        newInstanceMethods.put(this_m, il);
                    }
                    // visit all static methods.
                    LinkedHashMap newStaticMethods = new LinkedHashMap();
                    for (int i=0; i<this.static_methods.length; ++i) {
                        jq_StaticMethod this_m = this.static_methods[i];
                        jq_NameAndDesc nd = ClassLibInterface.convertClassLibNameAndDesc(this, this_m.getNameAndDesc());
                        if (this_m.getNameAndDesc() != nd) {
                            if (TRACE) Debug.writeln("Rewriting method signature from "+this_m.getNameAndDesc()+" to "+nd);
                            jq_StaticMethod this_m2 = getOrCreateStaticMethod(nd);
                            this_m2.load(this_m);
                            this_m.unload(); Object b = this.members.remove(this_m.getNameAndDesc()); cpr.remove(this_m);
                            if (TRACE) Debug.writeln("Removed member "+this_m.getNameAndDesc()+" from member set of "+this+": "+b);
                            this.addDeclaredMember(nd, this_m2);
                            this_m = this_m2;
                        }
                        byte[] bc = this_m.getBytecode();
                        Bytecodes.InstructionList il;
                        if (bc == null) {
                            il = null;
                        } else {
                            // extract instructions of method.
                            if (TRACE) Debug.writeln("Extracting instructions of "+this_m);
                            il = new Bytecodes.InstructionList(this_m);

                            // update constant pool references in the instructions, and add them to our constant pool.
                            rewriteMethod(cpr, il);
                        }
                        
                        // cache the instruction list for later.
                        newStaticMethods.put(this_m, il);
                    }
                    jq_ConstantPool new_cp = cpr.finish();
                    // rebuild method arrays
                    this.declared_instance_methods = new jq_InstanceMethod[newInstanceMethods.size()];
                    int j = -1;
                    for (Iterator i=newInstanceMethods.entrySet().iterator(); i.hasNext(); ) {
                        Map.Entry e = (Map.Entry)i.next();
                        jq_InstanceMethod i_m = (jq_InstanceMethod)e.getKey();
                        Bytecodes.InstructionList i_l = (Bytecodes.InstructionList)e.getValue();
                        if (i_l != null) {
                            if (TRACE) Debug.writeln("Rebuilding bytecodes for instance method "+i_m+", entry "+(j+1));
                            Bytecodes.CodeException[] ex_table = i_m.getExceptionTable(i_l);
                            Bytecodes.LineNumber[] line_num = i_m.getLineNumberTable(i_l);
                            i_m.setCode(i_l, ex_table, line_num, cpr);
                        } else {
                            if (TRACE) Debug.writeln("No bytecodes for instance method "+i_m+", entry "+(j+1));
                        }
                        //if (TRACE) Debug.writeln("Adding instance method "+i_m+" to array.");
                        this.declared_instance_methods[++j] = i_m;
                    }
                    this.static_methods = new jq_StaticMethod[newStaticMethods.size()];
                    j = -1;
                    for (Iterator i=newStaticMethods.entrySet().iterator(); i.hasNext(); ) {
                        Map.Entry e = (Map.Entry)i.next();
                        jq_StaticMethod i_m = (jq_StaticMethod)e.getKey();
                        Bytecodes.InstructionList i_l = (Bytecodes.InstructionList)e.getValue();
                        if (i_l != null) {
                            if (TRACE) Debug.writeln("Rebuilding bytecodes for static method "+i_m+", entry "+(j+1));
                            Bytecodes.CodeException[] ex_table = i_m.getExceptionTable(i_l);
                            Bytecodes.LineNumber[] line_num = i_m.getLineNumberTable(i_l);
                            i_m.setCode(i_l, ex_table, line_num, cpr);
                        } else {
                            if (TRACE) Debug.writeln("No bytecodes for static method "+i_m+", entry "+(j+1));
                        }
                        //if (TRACE) Debug.writeln("Adding static method "+i_m+" to array.");
                        this.static_methods[++j] = i_m;
                    }
                    this.remakeAttributes(cpr);
                    this.const_pool = new_cp;
                    getSourceFile(); // check for bug.
                    if (TRACE) Debug.writeln("Finished rebuilding constant pool.");
                } else {
                    
                    // make sure that all member references from other classes point to actual members.
                    Iterator it = members.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry e = (Map.Entry)it.next();
                        jq_Member m = (jq_Member)e.getValue();
                        if (m.getState() < STATE_LOADED) {
                            // may be a reference to a member of a superclass or superinterface.
                            // this can happen when using old class files.
                            it.remove();
                            if (WARN_STALE_CLASS_FILES) {
                                Set s = PrimordialClassLoader.loader.getClassesThatReference(m);
                                System.err.println("Warning: classes "+s+" refer to member "+m+", which does not exist. This may indicate stale class files.");
                            }
                            //throw new ClassFormatError("no such member "+m+", referenced by "+s);
                        }
                    }
                    
                }

                // all done!
                if (TRACE) Debug.writeln("Finished loading "+this);
                state = STATE_LOADED;
                
                // classes to be replaced see the system loading a clone which contains the new implementation and
                // whose name is set to "REPLACE<originalclassName>".
                // If the currently loading class is a Replacing one, do the replacement.
                // Old below refers to the class being replaced.
                String thisDesc = this.getDesc().toString();
                if (thisDesc.startsWith("LREPLACE")) {
                    Utf8 oldDesc = Utf8.get("L" + thisDesc.substring( 8 , thisDesc.length() )); // remove the 'LREPLACE' in name and restore 'L'
                    jq_Type old = PrimordialClassLoader.getOrCreateType(class_loader , oldDesc) ;
                    Assert._assert(old instanceof jq_Class);
                    if (((jq_Class)old).getState() < STATE_LOADED) {
                        // old has not been loaded yet, since it was not in the image
                        if (TRACE_REPLACE_CLASS) Debug.writeln("REPLACING Class: " + old.getDesc() + ". This class was not in the original image: doing nothing!");
                        PrimordialClassLoader.unloadType(class_loader , old) ;
                    } else {
                        replaceMethodIn((jq_Class) old);
                    }
                }
            }
            catch (UTFDataFormatError x) {
                state = STATE_LOADERROR;
                //state = STATE_UNLOADED;
                throw new ClassFormatError(x.toString());
            }
            catch (IOException x) {
                state = STATE_LOADERROR;
                //state = STATE_UNLOADED;
                throw new ClassFormatError(x.toString());
            }
            catch (ArrayIndexOutOfBoundsException x) {
                state = STATE_LOADERROR;
                //state = STATE_UNLOADED;
                x.printStackTrace();
                throw new ClassFormatError("bad constant pool index");
            }
            catch (Error x) {
                state = STATE_LOADERROR;
                //state = STATE_UNLOADED;
                throw x;
            }
        } // synchronized
    }
    
    // old : the class already present in the system whose members have to be replaced
    // this: the substitute, ie. the new version that contains the members to be added to OLD.
    private void replaceMethodIn(jq_Class old) {
        // cpa will manipulate and add elements to old.const_pool
        jq_ConstantPool.ConstantPoolAdder cpa =
            new jq_ConstantPool.ConstantPoolAdder(old.const_pool);

        // visit all STATIC methods of OLD to see
        // whether they have changed compared to those of NEW
        for (int i = 0;
            (old.static_methods != null) && i < old.static_methods.length;
            ++i) {
            jq_StaticMethod old_m = old.static_methods[i];
            jq_NameAndDesc nd = old_m.getNameAndDesc();
            jq_StaticMethod new_m = this.getDeclaredStaticMethod(nd);
            if (new_m != null) {
                // verify if method has changed from NEW to OLD
                byte[] new_bc = new_m.getBytecode();
                byte[] old_bc = old_m.getBytecode();
                //good enough to check whether a method has changed.
                // Comparing both byte arrays byte by byte does not work!
                if (new_bc.length == old_bc.length)
                    continue;

                // update OLD according to NEW
                if (TRACE_REPLACE_CLASS)
                    joeq.Runtime.Debug.writeln(
                        Strings.lineSep+Strings.lineSep+"In REPLACE: STARTING REPLACEMENT of:\t" + old_m);

                jq_NameAndDesc old_m_nd = old_m.getNameAndDesc();

                Bytecodes.InstructionList il;
                if (TRACE) Debug.writeln("Extracting instructions of "+new_m);
                il = new Bytecodes.InstructionList(new_m);

                // update constant pool references in instructions, and add them to our constant pool.
                if (TRACE_REPLACE_CLASS)
                    Debug.writeln(
                        "\tIn Replace: Rebuilding CP for static method "
                            + new_m);
                old.rewriteMethodForReplace(cpa, il);
                cpa.finish();
                Bytecodes.CodeException[] ex_table = new_m.getExceptionTable(il);
                Bytecodes.LineNumber[] line_num = new_m.getLineNumberTable(il);
                // as a side-effect cpa will set OLD's cp to its new value.
                new_m.setCode(il, ex_table, line_num, cpa);

                old.remakeAttributes(cpa); // reset sourcefile

                if (TRACE_REPLACE_CLASS)
                    Debug.writeln(
                        "\tIn Replace: Finished Rebuilding CP for static method "
                            + new_m);

                // rename and rattach new_m to old
                new_m.setNameAndDesc(old_m_nd);
                old.addDeclaredMember(old_m_nd, new_m);
                new_m.setDeclaringClass(old);

                //preparing new method
                new_m.prepare(); //state = prepared.

                //compile
                if (TRACE_REPLACE_CLASS)
                    Debug.writeln(
                        "\tIn REPLACE: Compiling stub for: " + new_m);
                new_m.compile();

                old.static_methods[i] = new_m;
                old_m.default_compiled_version.redirect(new_m.default_compiled_version);
                old_m.default_compiled_version =
                    new_m.default_compiled_version;

                if (TRACE_REPLACE_CLASS)
                    Debug.writeln(
                        Strings.lineSep+Strings.lineSep+"In Replace: DONE REPLACEMENT for STATIC method "
                            + old_m);
            } else {
                //TODO:
                // user wants to remove a method from OLD
                // not handled.
            }
        } // end static_methods

        // visit all INSTANCE methods of OLD to see whether they have changed compared to those of NEW
        for (int i = 0;
            (old.declared_instance_methods != null)
                && i < old.declared_instance_methods.length;
            ++i) {
            jq_InstanceMethod old_m = old.declared_instance_methods[i];
            jq_NameAndDesc nd = old_m.getNameAndDesc();
            jq_InstanceMethod new_m = this.getDeclaredInstanceMethod(nd);
            if (new_m != null) {
                // verify if method has changed from NEW to OLD
                byte[] new_bc = new_m.getBytecode();
                byte[] old_bc = old_m.getBytecode();
                
                if (new_bc == null || old_bc == null)
                    continue;
                                    
                //good enough to check whether a method has changed.
                // Comparing both byte arrays byte by byte does not work!
                if (new_bc.length == old_bc.length)
                    continue;
                // take next method

                if (TRACE_REPLACE_CLASS)
                    joeq.Runtime.Debug.writeln(
                        Strings.lineSep+Strings.lineSep+"In REPLACE: STARTING REPLACEMENT of:\t" + old_m);

                //info useful for new_m
                jq_NameAndDesc old_m_nd = old_m.getNameAndDesc();

                Bytecodes.InstructionList il;
                // extract new instructions.
                if (TRACE) Debug.writeln("Extracting instructions of "+new_m);
                il = new Bytecodes.InstructionList(new_m);

                // update constant pool references in the instructions, and add them to our constant pool.
                if (TRACE_REPLACE_CLASS)
                    Debug.writeln(
                        "\tIn Replace: Rebuilding CP for instance method "
                            + new_m);
                old.rewriteMethodForReplace(cpa, il);
                //collect new entries for cp
                cpa.finish(); //side-effect: commit new entries into cp.
                Bytecodes.CodeException[] ex_table = new_m.getExceptionTable(il);
                Bytecodes.LineNumber[] line_num = new_m.getLineNumberTable(il);
                new_m.setCode(il, ex_table, line_num, cpa); // update ref. to new entries.

                old.remakeAttributes(cpa); // reset sourcefile
                //old.getSourceFile();

                if (TRACE_REPLACE_CLASS)
                    Debug.writeln(
                        "\tIn Replace: Finished Rebuilding CP for instance method "
                            + new_m);

                //make new appear as old.
                new_m.setNameAndDesc(old_m_nd);
                old.addDeclaredMember(old_m_nd, new_m);
                new_m.setDeclaringClass(old);

                if (new_m.isInitializer() || new_m.isPrivate()) {
                    new_m.prepare();
                    //compile
                    if (TRACE_REPLACE_CLASS)
                        Debug.writeln(
                            "\tIn REPLACE: Compiling stub for: " + new_m);
                    new_m.compile();
                } else //ovverridable methods.
                    {
                    //prepare new_m to really become old_m.
                    //get old_m position and  get new entrypoint in vtable
                    int old_m_offset = old_m.getOffset();
                    new_m.prepare(old_m_offset);
                    //keep old_m offset and set state = prepared.
                    //compile
                    if (TRACE_REPLACE_CLASS)
                        Debug.writeln(
                            "\tIn REPLACE: Compiling stub for: " + new_m);
                    new_m.compile();

                    int index = (old_m_offset >> 2) - 1;
                    //old_m index in the array of virtualmethods.
                    Assert._assert(old.virtual_methods[index] == old_m);
                    old.virtual_methods[index] = new_m;
                    CodeAddress entryPoint =
                        new_m.getDefaultCompiledVersion().getEntrypoint();
                    ((Address[]) old.vtable)[index + 1] = entryPoint;
                    //+1 since vt[0] is this
                }

                old.declared_instance_methods[i] = new_m;

                old_m.default_compiled_version.redirect(new_m.default_compiled_version);
                old_m.default_compiled_version =
                    new_m.default_compiled_version;

                if (TRACE_REPLACE_CLASS)
                    Debug.writeln(
                        Strings.lineSep+Strings.lineSep+"In Replace: DONE REPLACING instance method "
                            + old_m);
            } else {
                //TODO:
                // user wants to remove a method from OLD
                // not handled yet
            }
        } // end declared_instances_methods

        {
            //TODO:
            // visit all methods in new to see whether there are completely new methods
            // that were NOT present before in old.
        }
    }
    
    public boolean isInClassLib() {
        return (this.getDesc().toString().startsWith("Ljoeq/ClassLib/") &&
                this.getDesc().toString().indexOf('/', 11) != -1);
    }
    
    public boolean doesConstantPoolContain(Object o) {
        if (const_pool == null) return false;
        return const_pool.contains(o);
    }
    
    public jq_StaticMethod generateStaticMethodStub(jq_NameAndDesc nd, jq_StaticMethod m, char access_flags, char classfield_idx, char method_idx) {
        jq_Type[] params = m.getParamTypes();
        Assert._assert(params.length >= 1);
        int size = 3+((params.length-1)*2)+3+1;
        byte[] bc = new byte[size];
        bc[0] = (byte)0xb2; // getstatic
        bc[1] = (byte)(classfield_idx >> 8);
        bc[2] = (byte)classfield_idx;
        int k=2;
        for (int j=1, n=0; j<params.length; ++j, ++n) {
            if (params[j].isReferenceType()) {
                bc[++k] = (byte)0x19; // aload
            } else if (params[j] == jq_Primitive.LONG) {
                bc[++k] = (byte)0x16; // lload
            } else if (params[j] == jq_Primitive.FLOAT) {
                bc[++k] = (byte)0x17; // fload
            } else if (params[j] == jq_Primitive.DOUBLE) {
                bc[++k] = (byte)0x18; // dload
            } else {
                bc[++k] = (byte)0x15; // iload
            }
            bc[++k] = (byte)n;
            if ((params[j] == jq_Primitive.LONG) || (params[j] == jq_Primitive.DOUBLE))
                ++n;
        }
        bc[++k] = (byte)0xb8; // invokestatic
        bc[++k] = (byte)(method_idx>>8);
        bc[++k] = (byte)method_idx;
        jq_Type t = m.getReturnType();
        if (t.isReferenceType()) {
            bc[++k] = (byte)0xb0; // areturn
        } else if (t == jq_Primitive.LONG) {
            bc[++k] = (byte)0xad; // lreturn
        } else if (t == jq_Primitive.FLOAT) {
            bc[++k] = (byte)0xae; // freturn
        } else if (t == jq_Primitive.DOUBLE) {
            bc[++k] = (byte)0xaf; // dreturn
        } else if (t == jq_Primitive.VOID) {
            bc[++k] = (byte)0xb1; // return
        } else {
            bc[++k] = (byte)0xac; // ireturn
        }
        jq_Method stubm = (jq_Method)getDeclaredMember(nd);
        jq_StaticMethod stub;
        if (stubm == null) stub = jq_StaticMethod.newStaticMethod(this, nd);
        else {
            // method that we are overwriting must be static.
            Assert._assert(stubm.isStatic(), stubm.toString());
            stub = (jq_StaticMethod)stubm;
        }
        //char access_flags = (char)(m.getAccessFlags() & ~ACC_NATIVE);
        char max_stack = (char)Math.max(m.getParamWords(), m.getReturnType().getReferenceSize()>>2);
        char max_locals = (char)(m.getParamWords()-1);
        stub.load(access_flags, max_stack, max_locals, bc, new jq_TryCatchBC[0], new jq_LineNumberBC[0], new HashMap());
        return stub;
    }
    
    public jq_InstanceMethod generateInstanceMethodStub(jq_NameAndDesc nd, jq_StaticMethod m, char access_flags, char method_idx) {
        jq_Type[] params = m.getParamTypes();
        Assert._assert(params.length >= 1);
        int size = 1+((params.length-1)*2)+3+1;
        byte[] bc = new byte[size];
        bc[0] = (byte)0x2a; // aload_0
        int k=0;
        for (int j=1, n=1; j<params.length; ++j, ++n) {
            if (params[j].isReferenceType()) {
                bc[++k] = (byte)0x19; // aload
            } else if (params[j] == jq_Primitive.LONG) {
                bc[++k] = (byte)0x16; // lload
            } else if (params[j] == jq_Primitive.FLOAT) {
                bc[++k] = (byte)0x17; // fload
            } else if (params[j] == jq_Primitive.DOUBLE) {
                bc[++k] = (byte)0x18; // dload
            } else {
                bc[++k] = (byte)0x15; // iload
            }
            bc[++k] = (byte)n;
            if ((params[j] == jq_Primitive.LONG) || (params[j] == jq_Primitive.DOUBLE))
                ++n;
        }
        bc[++k] = (byte)0xb8; // invokestatic
        bc[++k] = (byte)(method_idx>>8);
        bc[++k] = (byte)method_idx;
        jq_Type t = m.getReturnType();
        if (t.isReferenceType()) {
            bc[++k] = (byte)0xb0; // areturn
        } else if (t == jq_Primitive.LONG) {
            bc[++k] = (byte)0xad; // lreturn
        } else if (t == jq_Primitive.FLOAT) {
            bc[++k] = (byte)0xae; // freturn
        } else if (t == jq_Primitive.DOUBLE) {
            bc[++k] = (byte)0xaf; // dreturn
        } else if (t == jq_Primitive.VOID) {
            bc[++k] = (byte)0xb1; // return
        } else {
            bc[++k] = (byte)0xac; // ireturn
        }
        jq_Method stubm = (jq_Method)getDeclaredMember(nd);
        jq_InstanceMethod stub;
        if (stubm == null) stub = jq_InstanceMethod.newInstanceMethod(this, nd);
        else {
            // method that we are overwriting must be instance.
            Assert._assert(!stubm.isStatic(), stubm.toString());
            stub = (jq_InstanceMethod)stubm;
        }
        //char access_flags = (char)(m.getAccessFlags() & ~ACC_NATIVE);
        char max_stack = (char)Math.max(m.getParamWords(), m.getReturnType().getReferenceSize()>>2);
        char max_locals = (char)m.getParamWords();
        stub.load(access_flags, max_stack, max_locals, bc, new jq_TryCatchBC[0], new jq_LineNumberBC[0], new HashMap());
        return stub;
    }
    
    // that: mirror
    public void merge(jq_Class that) {
        // initialize constant pool rebuilder
        final jq_ConstantPool.ConstantPoolRebuilder cpr = rebuildConstantPool(true);
        
        // add all instance fields.
        LinkedList newInstanceFields = new LinkedList();
        for (int i=0; i<that.declared_instance_fields.length; ++i) {
            jq_InstanceField that_f = that.declared_instance_fields[i];
            jq_NameAndDesc nd = ClassLibInterface.convertClassLibNameAndDesc(that, that_f.getNameAndDesc());
            jq_InstanceField this_f = this.getDeclaredInstanceField(nd);
            if (this_f != null) {
                if (TRACE) Debug.writeln("Instance field "+this_f+" already exists, skipping.");
                if (this_f.getAccessFlags() != that_f.getAccessFlags()) {
                    if (TRACE) 
                        Debug.writeln("Access flags of instance field "+this_f+" from merged class do not match. ("+
                                                 (int)this_f.getAccessFlags()+"!="+(int)that_f.getAccessFlags()+")");
                }
                continue;
            }
            this_f = getOrCreateInstanceField(nd);
            Assert._assert(this_f.getState() == STATE_UNLOADED);
            this_f.load(that_f);
            that_f.unload(); Object b = that.members.remove(that_f.getNameAndDesc());
            if (TRACE) Debug.writeln("Removed member "+that_f.getNameAndDesc()+" from member set of "+that+": "+b);
            if (TRACE) Debug.writeln("Adding instance field: "+this_f);
            this.addDeclaredMember(nd, this_f);
            newInstanceFields.add(this_f);
            cpr.addUtf8(this_f.getName());
            cpr.addUtf8(this_f.getDesc());
            cpr.addAttributeNames(this_f);
        }
        if (newInstanceFields.size() > 0) {
            jq_InstanceField[] ifs = new jq_InstanceField[this.declared_instance_fields.length+newInstanceFields.size()];
            System.arraycopy(this.declared_instance_fields, 0, ifs, 0, this.declared_instance_fields.length);
            int j = this.declared_instance_fields.length-1;
            for (Iterator i=newInstanceFields.iterator(); i.hasNext(); )
                ifs[++j] = (jq_InstanceField)i.next();
            this.declared_instance_fields = ifs;
        }
        
        // add all static fields.
        LinkedList newStaticFields = new LinkedList();
        for (int i=0; i<that.static_fields.length; ++i) {
            jq_StaticField that_f = that.static_fields[i];
            jq_NameAndDesc nd = ClassLibInterface.convertClassLibNameAndDesc(that, that_f.getNameAndDesc());
            jq_StaticField this_f = this.getDeclaredStaticField(nd);
            if (this_f != null) {
                if (TRACE) Debug.writeln("Static field "+this_f+" already exists, skipping.");
                if (this_f.getAccessFlags() != that_f.getAccessFlags()) {
                    if (TRACE) 
                        Debug.writeln("Access flags of static field "+this_f+" from merged class do not match. ("+
                                                 (int)this_f.getAccessFlags()+"!="+(int)that_f.getAccessFlags()+")");
                }
                continue;
            }
            this_f = getOrCreateStaticField(nd);
            Assert._assert(this_f.getState() == STATE_UNLOADED);
            this_f.load(that_f);
            that_f.unload(); Object b = that.members.remove(that_f.getNameAndDesc());
            if (TRACE) Debug.writeln("Removed member "+that_f.getNameAndDesc()+" from member set of "+that+": "+b);
            if (TRACE) Debug.writeln("Adding static field: "+this_f);
            this.addDeclaredMember(nd, this_f);
            newStaticFields.add(this_f);
            cpr.addUtf8(this_f.getName());
            cpr.addUtf8(this_f.getDesc());
            cpr.addAttributeNames(this_f);
        }
        if (newStaticFields.size() > 0) {
            jq_StaticField[] ifs = new jq_StaticField[this.static_fields.length+newStaticFields.size()];
            System.arraycopy(this.static_fields, 0, ifs, 0, this.static_fields.length);
            int j = this.static_fields.length-1;
            for (Iterator i=newStaticFields.iterator(); i.hasNext(); ) {
                ifs[++j] = (jq_StaticField)i.next();
                this.static_data_size += ifs[j].getWidth();
            }
            this.static_fields = ifs;
        }
        
        // visit all instance methods.
        //
        // CR: visite toutes les methodes du mirror, dans le but de trouver celles qui sont a ajouter
        // a this. Une fois trouvees, ces methodes sont stockees dans newInstancesMethods.
        LinkedHashMap newInstanceMethods = new LinkedHashMap();
        for (int i=0; i<that.declared_instance_methods.length; ++i) {
            jq_InstanceMethod that_m = that.declared_instance_methods[i];
            jq_NameAndDesc nd = that_m.getNameAndDesc();
            //jq_NameAndDesc nd = merge_convertNameAndDesc(that_m.getNameAndDesc());
            Assert._assert(ClassLibInterface.convertClassLibNameAndDesc(that, nd) == nd);
            jq_InstanceMethod this_m = this.getDeclaredInstanceMethod(nd);
            byte[] bc = that_m.getBytecode();
            if (bc == null) {
                if (this_m != null) {
                    if (TRACE) Debug.writeln("Using existing body for instance method "+this_m+".");
                } else {
                    if (TRACE)
                        System.err.println("Body of method "+that_m+" doesn't already exist!");
                }
                continue;
            }
            if (bc.length == 5 && that_m instanceof jq_Initializer && that_m.getDesc() == Utf8.get("()V") &&
                this.getInitializer(Utf8.get("()V")) != null) {
                if (TRACE) Debug.writeln("Skipping default initializer "+that_m+".");
                continue;
            }
            
            // extract instructions of method.
            if (TRACE) Debug.writeln("Extracting instructions of "+that_m);
            Bytecodes.InstructionList il = new Bytecodes.InstructionList(that_m);
            
            // update constant pool references in the instructions, and add them to our constant pool.
            rewriteMethod(cpr, il);
            
            if (false) { //(this_m != null) {
                // method exists, use that one.
                if (TRACE) Debug.writeln("Using existing instance method object "+this_m+".");
            } else {
                if (TRACE) Debug.writeln("Creating new instance method object "+nd+".");
                this_m = this.getOrCreateInstanceMethod(nd);
                this.addDeclaredMember(nd, this_m);
                that_m.unload(); Object b = that.members.remove(that_m.getNameAndDesc());
                if (TRACE) Debug.writeln("Removed member "+that_m.getNameAndDesc()+" from member set of "+that+": "+b);
            }
            //CR: load porte mal son nom ici, car en fait il fait des choses tres basiques comme mettre le correct access code,
            // stack depth etc...
            this_m.load(that_m);
            
            // cache the instruction list for later.
            newInstanceMethods.put(this_m, il);
        }
        // CR: se contente de ramasser les instructions des methodes declarees dans la lib java sans celles qui viennent de Classlib.
        for (int i=0; i<this.declared_instance_methods.length; ++i) {
            jq_InstanceMethod this_m = this.declared_instance_methods[i];
            jq_Member this_m2 = this.getDeclaredMember(this_m.getNameAndDesc());
            if (newInstanceMethods.containsKey(this_m2)) {
                if (TRACE) Debug.writeln("Skipping replaced instance method object "+this_m+".");
                continue;
            }
            Assert._assert(this_m == this_m2);
            byte[] bc = this_m.getBytecode();
            if (bc == null) {
                if (TRACE) Debug.writeln("Skipping native/abstract instance method object "+this_m+".");
                newInstanceMethods.put(this_m, null);
                continue;
            }
            
            // extract instruction list.
            if (TRACE) Debug.writeln("Extracting instructions of "+this_m);
            Bytecodes.InstructionList il = new Bytecodes.InstructionList(this_m);
            
            // add constant pool references from instruction list.
            cpr.addCode(il);
            
            // cache the instruction list for later.
            newInstanceMethods.put(this_m, il);
        }
        
        Bytecodes.InstructionList rebuilt_clinit = null;
        
        // visit all static methods.
        LinkedHashMap newStaticMethods = new LinkedHashMap();
        for (int i=0; i<that.static_methods.length; ++i) {
            jq_StaticMethod that_m = that.static_methods[i];
            Bytecodes.InstructionList il;
            jq_StaticMethod this_m;
            if (that_m instanceof jq_ClassInitializer) {
                if (TRACE) Debug.writeln("Creating special static method for "+that_m+" class initializer.");
                Assert._assert(that_m.getBytecode() != null);
                Utf8 newname = Utf8.get("clinit_"+that.getJDKName());
                jq_NameAndDesc nd = new jq_NameAndDesc(newname, that_m.getDesc());
                this_m = getOrCreateStaticMethod(nd);
                this.addDeclaredMember(nd, this_m);
                this_m.load(that_m);
                
                // add a call to the special method in our class initializer.
                jq_ClassInitializer clinit = getClassInitializer();
                if (clinit == null) {
                    jq_NameAndDesc nd2 = new jq_NameAndDesc(Utf8.get("<clinit>"), Utf8.get("()V"));
                    clinit = (jq_ClassInitializer)getOrCreateStaticMethod(nd2);
                    this.addDeclaredMember(nd2, clinit);
                    clinit.load((char)(ACC_PUBLIC | ACC_STATIC), (char)0, (char)0, new byte[0],
                           new jq_TryCatchBC[0], new jq_LineNumberBC[0], new HashMap());
                    if (TRACE) Debug.writeln("Created class initializer "+clinit);
                    rebuilt_clinit = new Bytecodes.InstructionList();
                    Bytecodes.RETURN re = new Bytecodes.RETURN();
                    rebuilt_clinit.append(re);
                    rebuilt_clinit.setPositions();
                } else {
                    if (TRACE) Debug.writeln("Using existing class initializer "+clinit);
                    rebuilt_clinit = new Bytecodes.InstructionList(clinit);
                }
                Bytecodes.INVOKESTATIC is = new Bytecodes.INVOKESTATIC(this_m);
                rebuilt_clinit.insert(is);
                cpr.addCode(rebuilt_clinit);
                
                // extract instructions of method.
                if (TRACE) Debug.writeln("Extracting instructions of "+that_m);
                il = new Bytecodes.InstructionList(that_m);
                
                newStaticMethods.put(clinit, rebuilt_clinit);
                
                that_m.unload(); Object b = that.members.remove(that_m.getNameAndDesc());
                if (TRACE) Debug.writeln("Removed member "+that_m.getNameAndDesc()+" from member set of "+that+": "+b);
            } else {
                jq_NameAndDesc nd = that_m.getNameAndDesc();
                //jq_NameAndDesc nd = merge_convertNameAndDesc(that_m.getNameAndDesc());
                Assert._assert(ClassLibInterface.convertClassLibNameAndDesc(that, nd) == nd);
                this_m = this.getDeclaredStaticMethod(nd);
                byte[] bc = that_m.getBytecode();
                if (bc == null) {
                    if (this_m != null) {
                        if (TRACE) Debug.writeln("Using existing body for static method "+this_m+".");
                    } else {
                        if (TRACE)
                            System.err.println("Body of method "+that_m+" doesn't already exist!");
                    }
                    continue;
                }
                // extract instructions of method.
                if (TRACE) Debug.writeln("Extracting instructions of "+that_m);
                il = new Bytecodes.InstructionList(that_m);
                
                if (false) { //(this_m != null) {
                    // method exists, use that one.
                    if (TRACE) Debug.writeln("Using existing static method object "+this_m+".");
                } else {
                    this_m = getOrCreateStaticMethod(nd);
                    this.addDeclaredMember(nd, this_m);
                    that_m.unload(); Object b = that.members.remove(that_m.getNameAndDesc());
                    if (TRACE) Debug.writeln("Removed member "+that_m.getNameAndDesc()+" from member set of "+that+": "+b);
                    if (TRACE) Debug.writeln("Created new static method object "+this_m+".");
                }
                this_m.load(that_m);
            }
            
            // update constant pool references in the instructions, and add them to our constant pool.
            rewriteMethod(cpr, il);
            
            // cache the instruction list for later.
            newStaticMethods.put(this_m, il);
        }
        for (int i=0; i<this.static_methods.length; ++i) {
            jq_StaticMethod this_m = this.static_methods[i];
            jq_Member this_m2 = this.getDeclaredMember(this_m.getNameAndDesc());
            if (newStaticMethods.containsKey(this_m2)) {
                //if (TRACE) Debug.writeln("Skipping replaced static method object "+this_m+".");
                continue;
            }
            Assert._assert(this_m == this_m2);
            byte[] bc = this_m.getBytecode();
            if (bc == null) {
                //if (TRACE) Debug.writeln("Skipping native/abstract static method object "+this_m+".");
                newStaticMethods.put(this_m, null);
                continue;
            }
            
            // extract instruction list.
            if (TRACE) Debug.writeln("Extracting instructions of "+this_m);
            Bytecodes.InstructionList il = new Bytecodes.InstructionList(this_m);
            
            // add constant pool references from instruction list.
            cpr.addCode(il);
            
            // cache the instruction list for later.
            newStaticMethods.put(this_m, il);
        }
        
        // nothing more to add to constant pool, finish it.
        jq_ConstantPool new_cp = cpr.finish();
        
        // rebuild method arrays.
        this.declared_instance_methods = new jq_InstanceMethod[newInstanceMethods.size()];
        int j = -1;
        for (Iterator i=newInstanceMethods.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry e = (Map.Entry)i.next();
            jq_InstanceMethod i_m = (jq_InstanceMethod)e.getKey();
            Bytecodes.InstructionList i_l = (Bytecodes.InstructionList)e.getValue();
            if (i_l != null) {
                if (TRACE) Debug.writeln("Rebuilding bytecodes for instance method "+i_m+".");
                Bytecodes.CodeException[] ex_table = i_m.getExceptionTable(i_l);
                Bytecodes.LineNumber[] line_num = i_m.getLineNumberTable(i_l);
                i_m.setCode(i_l, ex_table, line_num, cpr);
            } else {
                if (TRACE) Debug.writeln("No bytecodes for instance method "+i_m+".");
            }
            //if (TRACE) Debug.writeln("Adding instance method "+i_m+" to array.");
            this.declared_instance_methods[++j] = i_m;
        }
        this.static_methods = new jq_StaticMethod[newStaticMethods.size()];
        j = -1;
        for (Iterator i=newStaticMethods.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry e = (Map.Entry)i.next();
            jq_StaticMethod i_m = (jq_StaticMethod)e.getKey();
            Bytecodes.InstructionList i_l = (Bytecodes.InstructionList)e.getValue();
            if (i_l != null) {
                if (TRACE) Debug.writeln("Rebuilding bytecodes for static method "+i_m+".");
                Bytecodes.CodeException[] ex_table = i_m.getExceptionTable(i_l);
                Bytecodes.LineNumber[] line_num = i_m.getLineNumberTable(i_l);
                i_m.setCode(i_l, ex_table, line_num, cpr);
            } else {
                if (TRACE) Debug.writeln("No bytecodes for static method "+i_m+".");
            }
            //if (TRACE) Debug.writeln("Adding static method "+i_m+" to array.");
            this.static_methods[++j] = i_m;
        }
        this.remakeAttributes(cpr);
        this.const_pool = new_cp;
        getSourceFile(); // check for bug.
        if (TRACE) Debug.writeln("Finished rebuilding constant pool.");
        //CR: ??? pourquoi faire ce qui suit a that?
        that.super_class.removeSubclass(that);
        for (int i=0; i<that.declared_interfaces.length; ++i) {
            jq_Class di = that.declared_interfaces[i];
            di.removeSubinterface(that);
        }
        PrimordialClassLoader.unloadType(class_loader, that);
        if (TRACE) Debug.writeln("Finished merging class "+this+".");
    }
    
    public void remakeAttributes(jq_ConstantPool.ConstantPoolRebuilder cpr) {
        Utf8 sf = getSourceFile();
        if (sf != null) {
            byte[] b = new byte[2];
            Convert.charToTwoBytes(cpr.get(sf), b, 0);
            attributes.put(Utf8.get("SourceFile"), b);
            if (TRACE) Debug.writeln("Reset SourceFile attribute to cp idx "+(int)cpr.get(sf)+".");
        }
    }
    
    private void rewriteMethod(jq_ConstantPool.ConstantPoolRebuilder cp, Bytecodes.InstructionList il) {
        final jq_ConstantPool.ConstantPoolRebuilder cpr = cp;
        il.accept(new Bytecodes.EmptyVisitor() {
            public void visitCPInstruction(Bytecodes.CPInstruction x) {
                Object o = x.getObject();
                if (o instanceof String) {
                    cpr.addString((String)o);
                } else if (o instanceof Class) {
                    jq_Type r = Reflection.getJQType((Class)o);
                    if (r instanceof jq_Reference) {
                        r = ClassLibInterface.convertClassLibCPEntry((jq_Reference)r);
                        x.setObject(r.getJavaLangClassObject());
                    }
                    cpr.addType(r);
                } else if (o instanceof jq_Type) {
                    if (o instanceof jq_Reference)
                        x.setObject(o = ClassLibInterface.convertClassLibCPEntry((jq_Reference)o));
                    cpr.addType((jq_Type)o);
                } else if (o instanceof jq_Member) {
                    x.setObject(o = ClassLibInterface.convertClassLibCPEntry((jq_Member)o));
                    cpr.addMember((jq_Member)o);
                } else {
                    cpr.addOther(o);
                }
            }
        });
    }
    
    private void rewriteMethodForReplace(jq_ConstantPool.ConstantPoolRebuilder cp,
                                         Bytecodes.InstructionList il) {
        final jq_ConstantPool.ConstantPoolRebuilder cpr = cp;
        il.accept(new Bytecodes.EmptyVisitor() {
            public void visitCPInstruction(Bytecodes.CPInstruction x) {
                Object o = x.getObject();
                if (o instanceof String) {
                    cpr.addString((String) o);
                } else if (o instanceof Class) {
                    cpr.addType(Reflection.getJQType((Class)o));
                } else if (o instanceof jq_Type) {
                    if (o instanceof jq_Reference)
                        cpr.addType((jq_Type) o);
                } else if (o instanceof jq_Member) {
                    cpr.addMember((jq_Member) o);
                } else {
                    cpr.addOther(o);
                }
            }
        });
    }
    
    public void merge_old(jq_Class that) {
        // initialize constant pool adder
        jq_ConstantPool.Adder cp_adder = const_pool.getAdder();
        
        // generate stubs for each of the methods in the other class.
        Assert._assert(that.declared_instance_methods.length <= 1, that.toString()); // the only instance method should be the fake <init> method.
        LinkedList toadd_instance = new LinkedList();
        LinkedList toadd_static = new LinkedList();
        char classfield_index = 0;
        for (int i=0; i<that.static_methods.length; ++i) {
            jq_StaticMethod sm = that.static_methods[i];
            if (sm.isClassInitializer()) continue;
            jq_Type[] that_param = sm.getParamTypes();
            Assert._assert(that_param.length >= 1, sm.toString());
            Utf8 name_utf = sm.getName();
            if (name_utf == Utf8.get("__init__")) name_utf = Utf8.get("<init>");
            char method_idx = cp_adder.add(sm, CONSTANT_ResolvedSMethodRef);
            if (that_param[0] == jq_Class._class) {
                // overridden static method
                char access_flags = sm.getAccessFlags();
                if (classfield_index == 0) {
                    jq_StaticField that_sf = that.getDeclaredStaticField(new jq_NameAndDesc(Utf8.get("_class"), Utf8.get("Ljoeq/Class/jq_Class;")));
                    Assert._assert(that_sf != null);
                    classfield_index = cp_adder.add(that_sf, CONSTANT_ResolvedSFieldRef);
                }
uphere1:
                for (int j=0; ; ++j) {
                    if (j>=static_methods.length) {
                        StringBuffer desc = new StringBuffer("(");
                        for (int k=1; k<that_param.length; ++k) {
                            desc.append(that_param[k].getDesc().toString());
                        }
                        desc.append(")");
                        desc.append(sm.getReturnType().getDesc().toString());
                        Utf8 desc_utf = Utf8.get(desc.toString());
                        jq_NameAndDesc nd = new jq_NameAndDesc(name_utf, desc_utf);
                        jq_StaticMethod stub = generateStaticMethodStub(nd, sm, access_flags, (char)classfield_index, (char)method_idx);
                        toadd_static.add(stub);
                        break;
                    }
                    jq_StaticMethod f = this.static_methods[j];
                    if (f.getName() == name_utf) {
                        // non-public classes may have "Ljava/lang/Object;", so we need to check element-by-element.
                        jq_Type[] this_param = f.getParamTypes();
                        if (this_param.length+1 != that_param.length) continue;
                        for (int k=0; k<this_param.length; ++k) {
                            if ((this_param[k] != that_param[k+1]) &&
                                (that_param[k+1] != PrimordialClassLoader.getJavaLangObject())) continue uphere1;
                        }
                        jq_NameAndDesc nd = f.getNameAndDesc();
                        access_flags = f.getAccessFlags();
                        jq_StaticMethod stub = generateStaticMethodStub(nd, sm, access_flags, (char)classfield_index, (char)method_idx);
                        if (TRACE) Debug.writeln("Replacing static method: "+stub);
                        this.static_methods[j] = stub;
                        break;
                    }
                }
            } else {
                // overridden instance method
                char access_flags = (char)(sm.getAccessFlags() & ~ACC_STATIC);
                Assert._assert(that_param[0] == PrimordialClassLoader.getJavaLangObject() || that_param[0] == this, sm.toString());
uphere2:
                for (int j=0; ; ++j) {
                    if (j>=declared_instance_methods.length) {
                        StringBuffer desc = new StringBuffer("(");
                        for (int k=1; k<that_param.length; ++k) {
                            desc.append(that_param[k].getDesc().toString());
                        }
                        desc.append(")");
                        desc.append(sm.getReturnType().getDesc().toString());
                        Utf8 desc_utf = Utf8.get(desc.toString());
                        jq_NameAndDesc nd = new jq_NameAndDesc(name_utf, desc_utf);
                        jq_InstanceMethod stub = generateInstanceMethodStub(nd, sm, access_flags, (char)method_idx);
                        toadd_instance.add(stub);
                        break;
                    }
                    jq_InstanceMethod f = this.declared_instance_methods[j];
                    if (f.getName() == name_utf) {
                        // non-public classes may have "Ljava/lang/Object;", so we need to check element-by-element.
                        jq_Type[] this_param = f.getParamTypes();
                        if (this_param.length != that_param.length) continue;
                        for (int k=0; k<this_param.length; ++k) {
                            if ((this_param[k] != that_param[k]) &&
                            (that_param[k] != PrimordialClassLoader.getJavaLangObject())) continue uphere2;
                        }
                        jq_NameAndDesc nd = f.getNameAndDesc();
                        access_flags = f.getAccessFlags();
                        jq_InstanceMethod stub = generateInstanceMethodStub(nd, sm, access_flags, (char)method_idx);
                        if (TRACE) Debug.writeln("Replacing instance method: "+stub);
                        this.declared_instance_methods[j] = stub;
                        break;
                    }
                }
            }
        }
        if (toadd_static.size() > 0) {
            jq_StaticMethod[] sms = new jq_StaticMethod[this.static_methods.length+toadd_static.size()];
            int i = this.static_methods.length-1;
            System.arraycopy(this.static_methods, 0, sms, 0, this.static_methods.length);
            Iterator it = toadd_static.iterator();
            while (it.hasNext()) {
                jq_StaticMethod stub = (jq_StaticMethod)it.next();
                if (TRACE) Debug.writeln("Adding static method stub: "+stub);
                sms[++i] = stub;
            }
            this.static_methods = sms;
        }
        if (toadd_instance.size() > 0) {
            jq_InstanceMethod[] ims = new jq_InstanceMethod[this.declared_instance_methods.length+toadd_instance.size()];
            int i = this.declared_instance_methods.length-1;
            System.arraycopy(this.declared_instance_methods, 0, ims, 0, this.declared_instance_methods.length);
            Iterator it = toadd_instance.iterator();
            while (it.hasNext()) {
                jq_InstanceMethod stub = (jq_InstanceMethod)it.next();
                if (TRACE) Debug.writeln("Adding instance method stub: "+stub);
                ims[++i] = stub;
            }
            this.declared_instance_methods = ims;
        }
        // add all instance fields.
        if (that.declared_instance_fields.length > 0) {
            jq_InstanceField[] ifs = new jq_InstanceField[this.declared_instance_fields.length+that.declared_instance_fields.length];
            System.arraycopy(this.declared_instance_fields, 0, ifs, 0, this.declared_instance_fields.length);
            int i = this.declared_instance_fields.length-1;
            for (int j=0; j<that.declared_instance_fields.length; ++j) {
                jq_InstanceField that_f = that.declared_instance_fields[j];
                jq_InstanceField this_f = getOrCreateInstanceField(that_f.getNameAndDesc());
                Assert._assert(this_f.getState() == STATE_UNLOADED, "conflict in field names in merged class: "+this_f);
                this_f.load(that_f.getAccessFlags(), that_f.getAttributes());
                if (TRACE) Debug.writeln("Adding instance field: "+this_f);
                ifs[++i] = this_f;
            }
            this.declared_instance_fields = ifs;
        }
        cp_adder.finish();
    }

    public void verify() {
        if (isVerified()) return; // quick test.
        synchronized (this) {
            if (isVerified()) return; // other thread already loaded this type.
            if (!isLoaded()) load();
            if (state == STATE_VERIFYING)
                throw new ClassCircularityError(this.toString()); // recursively called verify
            if (state == STATE_VERIFYERROR)
                throw new VerifyError();
            state = STATE_VERIFYING;
            try {
                if (TRACE) Debug.writeln("Beginning verifying "+this+"...");
                if (super_class != null) {
                    super_class.verify();
                }
                // TODO: classfile verification
                if (TRACE) Debug.writeln("Finished verifying "+this);
                state = STATE_VERIFIED;
            } catch (Error x) {
                state = STATE_VERIFYERROR;
                throw x;
            }
        }
    }
    
    public void prepare() {
        if (isPrepared()) return; // quick test.
        synchronized (this) {
            if (isPrepared()) return; // other thread already loaded this type.
            if (!isVerified()) verify();
            if (state == STATE_PREPARING)
                throw new ClassCircularityError(this.toString()); // recursively called prepare (?)
            if (state == STATE_PREPAREERROR)
                throw new ClassFormatError();
            state = STATE_PREPARING;
            try {
                if (TRACE) Debug.writeln("Beginning preparing "+this+"...");
    
                // note: this method is a good candidate for specialization on super_class != null.
                if (super_class != null) {
                    super_class.prepare();
                }
    
                int superfields;
                if (super_class != null) superfields = super_class.instance_fields.length;
                else superfields = 0;
                int numOfInstanceFields = superfields + this.declared_instance_fields.length;
                this.instance_fields = new jq_InstanceField[numOfInstanceFields];
                if (superfields > 0)
                    System.arraycopy(super_class.instance_fields, 0, this.instance_fields, 0, superfields);
    
                int superreferencefields;
                if (super_class != null) superreferencefields = super_class.reference_offsets.length;
                else superreferencefields = 0;
                int numOfReferenceFields = superreferencefields;
                
                // lay out instance fields
                int currentInstanceField = superfields-1;
                int size;
                if (super_class != null) size = super_class.instance_size;
                else size = ObjectLayout.OBJ_HEADER_SIZE;
                if (declared_instance_fields.length > 0) {
                    if (!dont_align) {
                        // align on the largest data type
                        int largestDataType = declared_instance_fields[0].getSize();
                        int align = size & largestDataType-1;
                        if (align != 0) {
                            if (TRACE) Debug.writeln("Gap of size "+align+" has been filled.");
                            // fill in the gap with smaller fields
                            for (int i=1; i<declared_instance_fields.length; ++i) {
                                jq_InstanceField f = declared_instance_fields[i];
                                int fsize = f.getSize();
                                if (fsize <= largestDataType-align) {
                                    instance_fields[++currentInstanceField] = f;
                                    if (TRACE) Debug.writeln("Filling in field #"+currentInstanceField+" "+f+" at offset "+Strings.shex(size - ObjectLayout.OBJ_HEADER_SIZE));
                                    f.prepare(size - ObjectLayout.OBJ_HEADER_SIZE);
                                    if (f.getType().isReferenceType() && !f.getType().isAddressType())
                                        ++numOfReferenceFields;
                                    size += fsize;
                                    align += fsize;
                                }
                                if (align == largestDataType) {
                                    if (TRACE) Debug.writeln("Gap of size "+align+" has been filled.");
                                    break;
                                }
                            }
                        }
                    } else {
                        if (TRACE) Debug.writeln("Skipping field alignment for class "+this);
                    }
                    for (int i=0; i<declared_instance_fields.length; ++i) {
                        jq_InstanceField f = declared_instance_fields[i];
                        if (f.getState() == STATE_LOADED) {
                            instance_fields[++currentInstanceField] = f;
                            if (TRACE) Debug.writeln("Laying out field #"+currentInstanceField+" "+f+" at offset "+Strings.shex(size - ObjectLayout.OBJ_HEADER_SIZE));
                            f.prepare(size - ObjectLayout.OBJ_HEADER_SIZE);
                            if (f.getType().isReferenceType() && !f.getType().isAddressType())
                                ++numOfReferenceFields;
                            size += f.getSize();
                        }
                    }
                }
                this.instance_size = (size+3) & ~3;
    
                this.reference_offsets = new int[numOfReferenceFields];
                int k = -1;
                for (int i=0; i < instance_fields.length; ++i) {
                    jq_InstanceField f = instance_fields[i];
                    if (f.getType().isReferenceType() && !f.getType().isAddressType()) {
                        reference_offsets[++k] = f.getOffset(); 
                    }
                }
                Assert._assert(k+1 == this.reference_offsets.length);
    
                // lay out virtual method table
                int numOfNewVirtualMethods = 0;
                for (int i=0; i<declared_instance_methods.length; ++i) {
                    jq_InstanceMethod m = declared_instance_methods[i];
                    Assert._assert(m.getState() == STATE_LOADED);
                    if (m.isInitializer()) {
                        // initializers cannot override or be overridden
                        continue;
                    }
                    if (super_class != null) {
                        jq_InstanceMethod m2 = super_class.getVirtualMethod(m.getNameAndDesc());
                        if (m2 != null) {
                            if (m.isPrivate() ||
                                m2.isPrivate() || m2.isFinal()) {// should not be overridden
                                System.out.println("error: method "+m+" overrides method "+m2);
                            }
                            m2.overriddenBy(m);
                            if (TRACE) Debug.writeln("Virtual method "+m+" overrides method "+m2+" offset "+Strings.shex(m2.getOffset()));
                            m.prepare(m2.getOffset());
                            continue;
                        }
                    }
                    if (m.isPrivate()) {
                        // private methods cannot override or be overridden
                        continue;
                    }
                    ++numOfNewVirtualMethods;
                }
                int super_virtual_methods;
                if (super_class != null)
                    super_virtual_methods = super_class.virtual_methods.length;
                else
                    super_virtual_methods = 0;
                int num_virtual_methods = super_virtual_methods + numOfNewVirtualMethods;
                virtual_methods = new jq_InstanceMethod[num_virtual_methods];
                if (super_virtual_methods > 0)
                    System.arraycopy(super_class.virtual_methods, 0, this.virtual_methods, 0, super_virtual_methods);
                for (int i=0, j=super_virtual_methods-1; i<declared_instance_methods.length; ++i) {
                    jq_InstanceMethod m = declared_instance_methods[i];
                    if (m.isInitializer() || m.isPrivate()) {
                        // not in vtable
                        if (TRACE) Debug.writeln("Skipping "+m+" in virtual method table.");
                        m.prepare();
                        continue;
                    }
                    if (m.isOverriding()) {
                        if (m.getState() != STATE_PREPARED) {
                            Assert.UNREACHABLE("Method "+m+" overrides superclass, but is not prepared");
                        }
                        int entry = (m.getOffset() >> 2) - 1;
                        virtual_methods[entry] = m;
                        continue;
                    }
                    Assert._assert(m.getState() == STATE_LOADED);
                    virtual_methods[++j] = m;
                    if (TRACE) Debug.writeln("Virtual method "+m+" is new, offset "+Strings.shex((j+1)*CodeAddress.size()));
                    m.prepare((j+1)*CodeAddress.size());
                }
                // allocate space for vtable
                vtable = new Address[num_virtual_methods+1];
    
                // prepare declared superinterfaces
                for (int i=0; i<declared_interfaces.length; ++i) {
                    declared_interfaces[i].prepare();
                }
    
                // calculate interfaces
                int n_super_interfaces;
                if (super_class != null) {
                    n_super_interfaces = super_class.interfaces.length;
                    if (super_class.isInterface())
                        ++n_super_interfaces; // add super_class to the list, too.
                } else
                    n_super_interfaces = 0;
                for (int i=0; i<declared_interfaces.length; ++i) {
                    n_super_interfaces += declared_interfaces[i].interfaces.length;
                }
    
                interfaces = new jq_Class[n_super_interfaces + declared_interfaces.length];
                int n = 0;
                if (n_super_interfaces > 0) {
                    System.arraycopy(super_class.interfaces, 0, this.interfaces, 0, super_class.interfaces.length);
                    n += super_class.interfaces.length;
                    if (super_class.isInterface())
                        this.interfaces[n++] = super_class;
                    for (int i=0; i<declared_interfaces.length; ++i) {
                        System.arraycopy(declared_interfaces[i].interfaces, 0, this.interfaces, n, declared_interfaces[i].interfaces.length);
                        n += declared_interfaces[i].interfaces.length;
                    }
                }
                Assert._assert (n == n_super_interfaces);
                System.arraycopy(declared_interfaces, 0, this.interfaces, n_super_interfaces, declared_interfaces.length);
    
                // set up tables for fast type checking.
                this.display = new jq_Type[DISPLAY_SIZE+2];
                if (!this.isInterface()) {
                    jq_Reference dps = this.getDirectPrimarySupertype();
                    if (dps != null) {
                        Assert._assert(dps.isPrepared());
                        int num = dps.offset;
                        if (num < 2) num = DISPLAY_SIZE+1;
                        System.arraycopy(dps.display, 2, this.display, 2, num-1);
                        this.offset = num + 1;
                        if (this.offset >= DISPLAY_SIZE+2)
                            this.offset = 0;
                    } else {
                        this.offset = 2;
                    }
                    this.display[this.offset] = this;
                } else {
                    this.display[2] = PrimordialClassLoader.getJavaLangObject();
                }
                this.s_s_array = interfaces;
                this.s_s_array_length = interfaces.length;
    
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
                
                // set prepared flags for static methods
                for (int i=0; i<static_methods.length; ++i) {
                    jq_StaticMethod m = static_methods[i];
                    m.prepare();
                }
                // set prepared flags for static fields
                for (int i=0; i<static_fields.length; ++i) {
                    jq_StaticField m = static_fields[i];
                    m.prepare();
                }
                
                if (TRACE) Debug.writeln("Finished preparing "+this);
                state = STATE_PREPARED;
            } catch (Error x) {
                state = STATE_PREPAREERROR;
                throw x;
            }
        }
    }
    
    public void sf_initialize() {
        if (isSFInitialized()) return; // quick test.
        synchronized (this) {
            if (isSFInitialized()) return;
            if (!isPrepared()) prepare();
            if (state == STATE_SFINITIALIZING)
                throw new ClassCircularityError(this.toString()); // recursively called sf_initialize (?)
            if (state == STATE_SFINITERROR)
                throw new NoClassDefFoundError();
            state = STATE_SFINITIALIZING;
            try {
                if (TRACE) Debug.writeln("Beginning SF init "+this+"...");
                if (super_class != null) {
                    super_class.sf_initialize();
                }
                // lay out static fields and set their constant values
                if (static_data_size > 0) {
                    static_data = new int[static_data_size>>2];
                    for (int i=0, j=0; i<static_fields.length; ++i) {
                        jq_StaticField f = static_fields[i];
                        f.sf_initialize(static_data, j << 2);
                        if (f.isConstant()) {
                            Object cv = f.getConstantValue();
                            if (f.getType().isPrimitiveType()) {
                                if (f.getType() == jq_Primitive.LONG) {
                                    long l = ((Long)cv).longValue();
                                    static_data[j  ] = (int)l;
                                    static_data[j+1] = (int)(l >> 32);
                                } else if (f.getType() == jq_Primitive.FLOAT) {
                                    static_data[j] = Float.floatToRawIntBits(((Float)cv).floatValue());
                                } else if (f.getType() == jq_Primitive.DOUBLE) {
                                    long l = Double.doubleToRawLongBits(((Double)cv).doubleValue());
                                    static_data[j  ] = (int)l;
                                    static_data[j+1] = (int)(l >> 32);
                                } else {
                                    static_data[j] = ((Integer)cv).intValue();
                                }
                            } else {
                                // java/lang/String
                                HeapAddress a = HeapAddress.addressOf(cv);
                                if (a != null) {
                                    static_data[j] = a.to32BitValue();
                                }
                            }
                        }
                        j += f.getWidth() >> 2;
                    }
                }
                if (TRACE) Debug.writeln("Finished SF init "+this);
                state = STATE_SFINITIALIZED;
            } catch (Error x) {
                state = STATE_SFINITERROR;
                throw x;
            }
        }
    }
    
    public void compile() {
        if (isCompiled()) return; // quick test.
        synchronized (this) {
            if (isCompiled()) return;
            if (!isSFInitialized()) sf_initialize();
            state = STATE_COMPILING;
            if (TRACE) Debug.writeln("Beginning compilation "+this+"...");
            if (super_class != null) {
                super_class.compile();
            }
            // generate compile stubs for each declared method
            for (int i=0; i<static_methods.length; ++i) {
                jq_StaticMethod m = static_methods[i];
                if (m.getState() == STATE_PREPARED) {
                    if (TRACE) Debug.writeln("Compiling stub for: "+m);
                    jq_CompiledCode cc = m.compile_stub();
                    if (jq.RunningNative) cc.patchDirectBindCalls();
                }
            }
            for (int i=0; i<declared_instance_methods.length; ++i) {
                jq_InstanceMethod m = declared_instance_methods[i];
                if (m.getState() == STATE_PREPARED) {
                    if (TRACE) Debug.writeln("Compiling stub for: "+m);
                    jq_CompiledCode cc = m.compile_stub();
                    if (jq.RunningNative) cc.patchDirectBindCalls();
                }
            }
            Address[] vt = (Address[])vtable;
            // 0th entry of vtable is class pointer
            vt[0] = HeapAddress.addressOf(this);
            for (int i=0; i<virtual_methods.length; ++i) {
                vt[i+1] = virtual_methods[i].getDefaultCompiledVersion().getEntrypoint();
            }
            if (TRACE) Debug.writeln(this+": "+vt[0].stringRep()+" vtable "+HeapAddress.addressOf(vt).stringRep());
            if (TRACE) Debug.writeln("Finished compilation "+this);
            state = STATE_COMPILED;
        }
    }
    
    public void cls_initialize() throws ExceptionInInitializerError, NoClassDefFoundError {
        if (isClsInitialized()) return; // quick test.
        synchronized (this) {
            if (state == STATE_CLSINITERROR) throw new NoClassDefFoundError(this+": clinit failed");
            if (state >= STATE_CLSINITIALIZING) return;
            if (!isCompiled()) compile();
            state = STATE_CLSINITIALIZING;
            if (TRACE) Debug.writeln("Beginning class init "+this+"...");
            if (super_class != null) {
                super_class.cls_initialize();
            }
            state = STATE_CLSINITRUNNING;
            if (jq.RunningNative)
                invokeclinit();
            if (TRACE) Debug.writeln("Finished class init "+this);
            state = STATE_CLSINITIALIZED;
        }
    }

    private void invokeclinit() throws ExceptionInInitializerError {
        try {
            state = STATE_CLSINITRUNNING;
            jq_ClassInitializer clinit = this.getClassInitializer();
            if (clinit != null) 
                Reflection.invokestatic_V(clinit);
        } catch (Error x) {
            state = STATE_CLSINITERROR;
            throw x;
        } catch (Throwable x) {
            state = STATE_CLSINITERROR;
            throw new ExceptionInInitializerError(x);
        }
    }

    public static int NumOfIFieldsKept = 0;
    public static int NumOfSFieldsKept = 0;
    public static int NumOfIMethodsKept = 0;
    public static int NumOfSMethodsKept = 0;
    public static int NumOfIFieldsEliminated = 0;
    public static int NumOfSFieldsEliminated = 0;
    public static int NumOfIMethodsEliminated = 0;
    public static int NumOfSMethodsEliminated = 0;
    
    void readAttributes(DataInput in, Map attribMap) 
    throws IOException {
        char n_attributes = (char)in.readUnsignedShort();
        for (int i=0; i<n_attributes; ++i) {
            char attribute_name_index = (char)in.readUnsignedShort();
            if (getCPtag(attribute_name_index) != CONSTANT_Utf8)
                throw new ClassFormatError("constant pool entry "+attribute_name_index+", referred to by attribute "+i+
                                           ", is wrong type tag (expected="+CONSTANT_Utf8+", actual="+getCPtag(attribute_name_index));
            Utf8 attribute_desc = getCPasUtf8(attribute_name_index);
            int attribute_length = in.readInt();
            // todo: maybe we only want to read in attributes we care about...
            byte[] attribute_data = new byte[attribute_length];
            in.readFully(attribute_data);
            attribMap.put(attribute_desc, attribute_data);
        }
    }
    
    public static String className(Utf8 desc) {
        String temp = desc.toString();
        Assert._assert(temp.startsWith("L"), temp);
        Assert._assert(temp.endsWith(";"), temp);
        return temp.substring(1, temp.length()-1).replace('/','.');
    }

    private void addSubclass(jq_Class subclass) {
        jq_Class[] newsubclasses = new jq_Class[subclasses.length+1];
        System.arraycopy(subclasses, 0, newsubclasses, 0, subclasses.length);
        newsubclasses[subclasses.length] = subclass;
        subclasses = newsubclasses;
    }
    
    private void addSubinterface(jq_Class subinterface) {
        jq_Class[] newsubinterfaces = new jq_Class[subinterfaces.length+1];
        System.arraycopy(subinterfaces, 0, newsubinterfaces, 0, subinterfaces.length);
        newsubinterfaces[subinterfaces.length] = subinterface;
        subinterfaces = newsubinterfaces;
    }
    
    private void removeSubclass(jq_Class subclass) {
        jq_Class[] newsubclasses = new jq_Class[subclasses.length-1];
        for (int i=-1, j=0; i<newsubclasses.length-1; ++j) {
            if (subclass != subclasses[j]) {
                newsubclasses[++i] = subclasses[j];
            }
        }
        subclasses = newsubclasses;
    }
    
    private void removeSubinterface(jq_Class subinterface) {
        jq_Class[] newsubinterfaces = new jq_Class[subinterfaces.length-1];
        for (int i=-1, j=0; i<newsubinterfaces.length-1; ++j) {
            if (subinterface != subinterfaces[j]) {
                newsubinterfaces[++i] = subinterfaces[j];
            }
        }
        subinterfaces = newsubinterfaces;
    }
    
    public static jq_InstanceMethod getInvokespecialTarget(jq_Class clazz, jq_InstanceMethod method)
    throws AbstractMethodError {
        clazz.load();
        if (!clazz.isSpecial())
            return method;
        if (method.isInitializer())
            return method;
        if (TypeCheck.isSuperclassOf(method.getDeclaringClass(), clazz, true) != YES)
            return method;
        jq_NameAndDesc nd = method.getNameAndDesc();
        for (;;) {
            clazz = clazz.getSuperclass();
            if (clazz == null)
                throw new AbstractMethodError();
            clazz.load();
            method = clazz.getDeclaredInstanceMethod(nd);
            if (method != null)
                return method;
        }
    }
    
    public jq_ConstantPool.ConstantPoolRebuilder rebuildConstantPool(boolean addCode) {
        jq_ConstantPool.ConstantPoolRebuilder cpr = new jq_ConstantPool.ConstantPoolRebuilder();
        cpr.addType(this);
        if (this.getSuperclass() != null)
            cpr.addType(this.getSuperclass());
        for (int i=0; i < declared_interfaces.length; ++i) {
            jq_Class f = declared_interfaces[i];
            cpr.addType(f);
        }
        for (int i=0; i < declared_instance_fields.length; ++i) {
            jq_InstanceField f = declared_instance_fields[i];
            cpr.addUtf8(f.getName());
            cpr.addUtf8(f.getDesc());
            cpr.addAttributeNames(f);
        }
        for (int i=0; i < static_fields.length; ++i) {
            jq_StaticField f = static_fields[i];
            cpr.addUtf8(f.getName());
            cpr.addUtf8(f.getDesc());
            cpr.addAttributeNames(f);
            if (f.isConstant()) {
                Object o = f.getConstantValue();
                if (o instanceof String)
                    cpr.addString((String) f.getConstantValue());
                else if (o instanceof Class)
                    cpr.addType(Reflection.getJQType((Class)o));
                else
                    cpr.addOther(f.getConstantValue());
            }
        }
        for (int i=0; i < declared_instance_methods.length; ++i) {
            jq_InstanceMethod f = declared_instance_methods[i];
            cpr.addUtf8(f.getName());
            cpr.addUtf8(f.getDesc());
            cpr.addAttributeNames(f);
            if (addCode) cpr.addCode(f);
            cpr.addExceptions(f);
        }
        for (int i=0; i < static_methods.length; ++i) {
            jq_StaticMethod f = static_methods[i];
            cpr.addUtf8(f.getName());
            cpr.addUtf8(f.getDesc());
            cpr.addAttributeNames(f);
            if (addCode) cpr.addCode(f);
            cpr.addExceptions(f);
        }
        Utf8 sourcefile = getSourceFile();
        if (sourcefile != null) {
            cpr.addUtf8(sourcefile);
        }
        // TODO: InnerClasses
        for (Iterator i = attributes.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry e = (Map.Entry)i.next();
            Utf8 name = (Utf8)e.getKey();
            cpr.addUtf8(name);
        }
        
        return cpr;
    }
    
    public void dump(DataOutput out) throws IOException {
        chkState(STATE_LOADED);
        out.writeInt(0xcafebabe);
        out.writeChar(minor_version);
        out.writeChar(major_version);
        
        jq_ConstantPool.ConstantPoolRebuilder cpr = rebuildConstantPool(true);
        cpr.dump(out);
        
        out.writeChar(access_flags);
        out.writeChar(cpr.get(this));
        char sc;
        if (super_class == null) sc = 0;
        else sc = cpr.get(super_class);
        out.writeChar(sc);
        
        out.writeChar(declared_interfaces.length);
        for(int i=0; i < declared_interfaces.length; i++)
            out.writeChar(cpr.get(declared_interfaces[i]));
        
        int nfields = static_fields.length + declared_instance_fields.length;
        Assert._assert(nfields <= Character.MAX_VALUE);
        out.writeChar(nfields);
        for(int i=0; i < static_fields.length; i++) {
            static_fields[i].dump(out, cpr);
        }
        for(int i=0; i < declared_instance_fields.length; i++) {
            declared_instance_fields[i].dump(out, cpr);
        }
        
        int nmethods = static_methods.length + declared_instance_methods.length;
        out.writeChar(nmethods);
        for(int i=0; i < static_methods.length; i++) {
            static_methods[i].dump(out, cpr);
        }
        for(int i=0; i < declared_instance_methods.length; i++) {
            declared_instance_methods[i].dump(out, cpr);
        }
        
        int nattributes = attributes.size();
        Assert._assert(nattributes <= Character.MAX_VALUE);
        out.writeChar(nattributes);
        for (Iterator i = attributes.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry e = (Map.Entry)i.next();
            Utf8 name = (Utf8)e.getKey();
            out.writeChar(cpr.get(name));
            byte[] value = (byte[])e.getValue();
            if (name == Utf8.get("SourceFile")) {
                char oldIndex = Convert.twoBytesToChar(value, 0);
                Utf8 oldValue = (Utf8)const_pool.get(oldIndex);
                Convert.charToTwoBytes(cpr.get(oldValue), value, 0);
            } else if (name == Utf8.get("InnerClasses")) {
                // TODO
            }
            out.writeInt(value.length);
            out.write(value);
        }
    }

    public static final jq_Class _class = (jq_Class)PrimordialClassLoader.loader.getOrCreateBSType("Ljoeq/Class/jq_Class;");

    static interface Delegate {
        Object newInstance(jq_Class c, int instance_size, Object vtable);
    }

    private static Delegate _delegate;

    static {
        /* Set up delegates. */
        _delegate = null;
        boolean nullVM = jq.nullVM;
        if (!nullVM) {
            _delegate = attemptDelegate("joeq.Class.Delegates$Klass");
        }
        if (_delegate == null) {
            _delegate = new NullDelegates.Klass();
        }
    }

    private static Delegate attemptDelegate(String s) {
        String type = "class delegate";
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

    static final HashMap/*<String,StringConstant>*/ stringConstants = new HashMap();
    public StringConstant findStringConstant(String s) {
        StringConstant sc = (StringConstant)stringConstants.get(s); 
        if (sc == null) {
            char idx = getCP().findEqual(s, CONSTANT_String);
            if (idx != 0)
                stringConstants.put(s, sc = new StringConstant(idx));
        }
        return sc;
    }
    public static StringConstant readStringConstant(StringTokenizer st) {
        jq_Class clazz = (jq_Class)jq_Type.read(st);
        char cpindex = (char)Integer.parseInt(st.nextToken());
        clazz.load();
        return clazz.findStringConstant(clazz.getCPasString(cpindex));
    }

    public class StringConstant {
        char cpindex;
        StringConstant(char cpindex) {
            this.cpindex = cpindex;
        }
        public void write(Textualizer t) throws IOException {
            jq_Class.this.write(t);
            t.writeString(" "+(int)cpindex);
        }
        public String getString() {
            load();
            return getCPasString(cpindex);
        }
        public String toString() {
            return "StringConstant[class="+jq_Class.this+",idx="+(int)cpindex+"]";
        }
    }
}
