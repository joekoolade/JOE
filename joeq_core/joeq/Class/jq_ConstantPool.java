// jq_ConstantPool.java, created Fri Jan 11 17:29:36 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Class;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import joeq.Compiler.BytecodeAnalysis.Bytecodes;
import joeq.Runtime.Debug;
import joeq.Runtime.Reflection;
import joeq.UTF.Utf8;
import jwutil.util.Assert;

/**
 * jq_ConstantPool
 * 
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: jq_ConstantPool.java,v 1.35 2004/12/10 10:55:02 joewhaley Exp $
 */
public class jq_ConstantPool implements jq_ClassFileConstants {

    public static /*final*/ boolean TRACE = false;

    private Object[] constant_pool;
    private byte[] constant_pool_tags;
    
    /** Creates new jq_ConstantPool */
    public jq_ConstantPool(int size) {
        constant_pool = new Object[size];
        constant_pool_tags = new byte[size];
    }

    public String toString() {
        return Arrays.asList(constant_pool).toString();
    }

    public void load(DataInput in) throws IOException, ClassFormatError {
        // first pass: read in the constant pool
        int constant_pool_count = constant_pool.length;
        for (int i=1; i<constant_pool_count; ++i) { // CP slot 0 is unused
            switch (constant_pool_tags[i] = in.readByte()) {
            case CONSTANT_Integer:
                constant_pool[i] = new Integer(in.readInt());
                break;
            case CONSTANT_Float:
                constant_pool[i] = new Float(in.readFloat());
                break;
            case CONSTANT_Long:
                constant_pool[i++] = new Long(in.readLong());
                break;
            case CONSTANT_Double:
                constant_pool[i++] = new Double(in.readDouble());
                break;
            case CONSTANT_Utf8: {
                byte utf[] = new byte[in.readUnsignedShort()];
                in.readFully(utf);
                constant_pool[i] = Utf8.get(utf);
                break; }
            case CONSTANT_Class:
            case CONSTANT_String:
                // resolved on the next pass
                constant_pool[i] = new Character((char)in.readUnsignedShort()); // freed later
                break;
            case CONSTANT_NameAndType:
            case CONSTANT_FieldRef:
            case CONSTANT_MethodRef:
            case CONSTANT_InterfaceMethodRef: {
                // resolved on the next pass
                char class_index = (char)in.readUnsignedShort();
                char name_and_type_index = (char)in.readUnsignedShort();
                constant_pool[i] = new PairOfChars(class_index, name_and_type_index); // freed later
                break; }
            default:
                throw new ClassFormatError("bad constant pool entry tag (entry="+i+", tag="+constant_pool_tags[i]);
            }
        }
    }
    
    static class PairOfChars {
        char c1, c2;
        PairOfChars(char c1, char c2) { this.c1 = c1; this.c2 = c2; }
        char getFirst() { return c1; }
        char getSecond() { return c2; }
    }
    
    public void resolve(ClassLoader cl) {
        // second pass: resolve the non-primitive stuff
        int constant_pool_count = constant_pool.length;
        for (int i=1; i<constant_pool_count; ++i) { // CP slot 0 is unused
            switch (constant_pool_tags[i]) {
            case CONSTANT_Integer:
            case CONSTANT_Float:
            case CONSTANT_Utf8:
                break;
            case CONSTANT_Long:
            case CONSTANT_Double:
                ++i;
                // do nothing, already resolved
                break;
            case CONSTANT_NameAndType:
                // skipped
                break;
            case CONSTANT_Class:
                resolveClass(cl, i);
                break;
            case CONSTANT_ResolvedClass:
                // already forward resolved.
                break;
            case CONSTANT_String:
                char string_index = ((Character)constant_pool[i]).charValue();
                if (constant_pool_tags[string_index] != CONSTANT_Utf8)
                    throw new ClassFormatError("constant pool entry "+(int)string_index+", referred to by "+i+
                                               ", is wrong type tag (expected="+CONSTANT_Utf8+", actual="+constant_pool_tags[string_index]+")");
                Utf8 string = (Utf8)constant_pool[string_index];
                // free (constant_pool[i])
                constant_pool[i] = string.toString();
                break;
            case CONSTANT_FieldRef:
            case CONSTANT_MethodRef:
            case CONSTANT_InterfaceMethodRef: {
                PairOfChars pair = (PairOfChars)constant_pool[i];
                char class_index = pair.getFirst();
                char name_and_type_index = pair.getSecond();
                if (constant_pool_tags[class_index] != CONSTANT_ResolvedClass) {
                    if (constant_pool_tags[class_index] != CONSTANT_Class)
                        throw new ClassFormatError("constant pool entry "+(int)class_index+", referred to by "+i+
                                                   ", is wrong type tag (expected="+CONSTANT_Class+", actual="+constant_pool_tags[class_index]+")");
                    if (class_index > i) {
                        // forward class reference, resolve it now.
                        resolveClass(cl, class_index);
                    }
                } else {
                    // already resolved.
                }
                // note: javac 1.5 can generate array references here. yuck.
                jq_Reference r = (jq_Reference) constant_pool[class_index];
                jq_Class clazz;
                if (r instanceof jq_Class)
                    clazz = (jq_Class) r;
                else
                    clazz = PrimordialClassLoader.getJavaLangObject();
                PairOfChars pair2 = (PairOfChars)constant_pool[name_and_type_index];
                char name_index = pair2.getFirst();
                char desc_index = pair2.getSecond();
                if (constant_pool_tags[name_index] != CONSTANT_Utf8)
                    throw new ClassFormatError("constant pool entry "+(int)name_index+", referred to by "+(int)name_and_type_index+
                                               ", is wrong type tag (expected="+CONSTANT_Utf8+", actual="+constant_pool_tags[name_index]+")");
                if (constant_pool_tags[desc_index] != CONSTANT_Utf8)
                    throw new ClassFormatError("constant pool entry "+(int)desc_index+", referred to by "+(int)name_and_type_index+
                                               ", is wrong type tag (expected="+CONSTANT_Utf8+", actual="+constant_pool_tags[desc_index]+")");
                Utf8 name = (Utf8)constant_pool[name_index];
                Utf8 desc = (Utf8)constant_pool[desc_index];
                if (constant_pool_tags[i] == CONSTANT_FieldRef) {
                    if (!desc.isValidTypeDescriptor())
                        throw new ClassFormatError(desc+" is not a valid type descriptor");
                } else {
                    if (!desc.isValidMethodDescriptor())
                        throw new ClassFormatError(desc+" is not a valid method descriptor");
                }
                jq_NameAndDesc nd = new jq_NameAndDesc(name, desc);
                // free (constant_pool[i])
                if (clazz.isLoaded()) {
                    jq_Member mem = clazz.getDeclaredMember(nd);
                    if (mem == null) {
                        // this constant pool entry refers to a member that doesn't exist in the named class.
                        if (TRACE) Debug.writeln("No such member: "+clazz+"."+nd+", referenced by cp idx "+(int)i);
                        if (false) {
                            // throw resolution exception early
                            String s = ("No such member: "+clazz+"."+nd+", referenced by cp idx "+(int)i);
                            if (constant_pool_tags[i] == CONSTANT_FieldRef)
                                throw new NoSuchFieldError(s);
                            else
                                throw new NoSuchMethodError(s);
                        } else {
                            // NoSuchFieldError/NoSuchMethodError should be thrown when this member is resolved.
                            constant_pool[i] = new jq_MemberReference(clazz, nd);
                        }
                    } else {
                        constant_pool[i] = mem;
                        if (desc.isDescriptor(TC_PARAM)) {
                            if (mem.isStatic()) {
                                constant_pool_tags[i] = CONSTANT_ResolvedSMethodRef;
                                if (TRACE) Debug.writeln("Resolved static method "+mem+", cp idx "+(int)i);
                            } else {
                                constant_pool_tags[i] = CONSTANT_ResolvedIMethodRef;
                                if (TRACE) Debug.writeln("Resolved instance method "+mem+", cp idx "+(int)i);
                            }
                        } else {
                            if (mem.isStatic()) {
                                constant_pool_tags[i] = CONSTANT_ResolvedSFieldRef;
                                if (TRACE) Debug.writeln("Resolved static field "+mem+", cp idx "+(int)i);
                            } else {
                                constant_pool_tags[i] = CONSTANT_ResolvedIFieldRef;
                                if (TRACE) Debug.writeln("Resolved instance field "+mem+", cp idx "+(int)i);
                            }
                        }
                    }
                } else {
                    constant_pool[i] = new jq_MemberReference(clazz, nd);
                }
                break; }
            default:
                Assert.UNREACHABLE();
                return;
            }
        }
    }

    private void resolveClass(ClassLoader cl, int i) throws ClassFormatError {
        char name_index = ((Character)constant_pool[i]).charValue();
        if (constant_pool_tags[name_index] != CONSTANT_Utf8)
            throw new ClassFormatError("constant pool entry "+name_index+", referred to by "+i+
                                       ", is wrong type tag (expected="+CONSTANT_Utf8+", actual="+constant_pool_tags[name_index]);
        Utf8 classname = (Utf8)constant_pool[name_index];
        // free (constant_pool[i])
        if (!classname.isDescriptor(TC_ARRAY)) {
            classname = classname.getAsClassDescriptor();
        }
        constant_pool[i] = PrimordialClassLoader.getOrCreateType(cl, classname);
        constant_pool_tags[i] = CONSTANT_ResolvedClass;
        if (TRACE) Debug.writeln("Resolved class "+constant_pool[i]+", cp idx "+(int)i);
    }

    public final void set(char index, Object o, byte tag) {
        constant_pool[index] = o;
        constant_pool_tags[index] = tag;
    }

    public final int getCount() {
        return constant_pool.length;
    }
    public final byte getTag(char index) {
        return constant_pool_tags[index];
    }
    public final Object get(char index) {
        return constant_pool[index];
    }
    public final Integer getAsInt(char index) {
        Assert._assert(constant_pool_tags[index] == CONSTANT_Integer);
        return (Integer)constant_pool[index];
    }
    public final Float getAsFloat(char index) {
        Assert._assert(constant_pool_tags[index] == CONSTANT_Float);
        return (Float)constant_pool[index];
    }
    public final Long getAsLong(char index) {
        Assert._assert(constant_pool_tags[index] == CONSTANT_Long);
        return (Long)constant_pool[index];
    }
    public final Double getAsDouble(char index) {
        Assert._assert(constant_pool_tags[index] == CONSTANT_Double);
        return (Double)constant_pool[index];
    }
    public final String getAsString(char index) {
        Assert._assert(constant_pool_tags[index] == CONSTANT_String);
        return (String)constant_pool[index];
    }
    public final Utf8 getAsUtf8(char index) {
        Assert._assert(constant_pool_tags[index] == CONSTANT_Utf8);
        return (Utf8)constant_pool[index];
    }
    public final jq_Type getAsType(char index) {
        Assert._assert(constant_pool_tags[index] == CONSTANT_ResolvedClass);
        return (jq_Type)constant_pool[index];
    }
    public final Object getAsObjectConstant(char index) {
        byte c = constant_pool_tags[index];
        Object o = constant_pool[index];
        Assert._assert(c == CONSTANT_String || c == CONSTANT_ResolvedClass);
        if (c == CONSTANT_ResolvedClass) {
            o = Reflection.getJDKType((jq_Reference) o);
        }
        return o;
    }
    public final jq_Member getAsMember(char index) {
        Assert._assert(constant_pool_tags[index] == CONSTANT_ResolvedSFieldRef ||
                  constant_pool_tags[index] == CONSTANT_ResolvedIFieldRef ||
                  constant_pool_tags[index] == CONSTANT_ResolvedSMethodRef ||
                  constant_pool_tags[index] == CONSTANT_ResolvedIMethodRef);
        return (jq_Member)constant_pool[index];
    }
    public final jq_StaticField getAsStaticField(char index) {
        if (constant_pool_tags[index] == CONSTANT_ResolvedSFieldRef)
            return (jq_StaticField)constant_pool[index];
        if (constant_pool_tags[index] != CONSTANT_FieldRef)
            throw new VerifyError();
        if (TRACE) Debug.writeln("Attempting to resolve static field "+constant_pool[index]+" cp idx "+(int)index);
        jq_MemberReference n = (jq_MemberReference)constant_pool[index];
        jq_Class otherclazz = n.getReferencedClass();
        jq_NameAndDesc nd = n.getNameAndDesc();
        if (otherclazz.isInClassLib())
            nd = joeq.ClassLib.ClassLibInterface.convertClassLibNameAndDesc(otherclazz, nd);
        jq_StaticField f;
        if (otherclazz.isLoaded()) {
            f = otherclazz.getStaticField(nd);
            if (f == null) 
                throw new NoSuchFieldError("no such static field "+otherclazz+"."+nd);
        } else {
            // we differ slightly from the vm spec in that when a reference to the member is
            // encountered before the class is loaded, and the member is actually in a
            // superclass/superinterface it will throw a NoSuchFieldError when the member is
            // accessed.
            // Java compilers don't generate such references, unless class files are old.
            jq_Field m = (jq_Field)otherclazz.getDeclaredMember(nd);
            if (m == null) {
                constant_pool[index] = f = otherclazz.createStaticField(nd);
                constant_pool_tags[index] = CONSTANT_ResolvedSFieldRef;
                if (TRACE) Debug.writeln("Resolved static field "+f+", cp idx "+(int)index);
            } else if (!m.isStatic())
                throw new VerifyError("field "+m+" referred to as both static and instance");
            else
                f = (jq_StaticField)m;
        }
        return f;
    }
    public final jq_InstanceField getAsInstanceField(char index) {
        if (constant_pool_tags[index] == CONSTANT_ResolvedIFieldRef)
            return (jq_InstanceField)constant_pool[index];
        if (constant_pool_tags[index] != CONSTANT_FieldRef)
            throw new VerifyError();
        if (TRACE) Debug.writeln("Attempting to resolve instance field "+constant_pool[index]+" cp idx "+(int)index);
        jq_MemberReference n = (jq_MemberReference)constant_pool[index];
        jq_Class otherclazz = n.getReferencedClass();
        jq_NameAndDesc nd = n.getNameAndDesc();
        if (otherclazz.isInClassLib())
            nd = joeq.ClassLib.ClassLibInterface.convertClassLibNameAndDesc(otherclazz, nd);
        jq_InstanceField f;
        if (otherclazz.isLoaded()) {
            f = otherclazz.getInstanceField(nd);
            if (f == null) 
                throw new NoSuchFieldError("no such instance field "+otherclazz+"."+nd);
        } else {
            // we differ slightly from the vm spec in that when a reference to the member is
            // encountered before the class is loaded, and the member is actually in a
            // superclass/superinterface it will throw a NoSuchFieldError when the member is
            // accessed.
            // Java compilers don't generate such references, unless class files are old.
            jq_Field m = (jq_Field)otherclazz.getDeclaredMember(nd);
            if (m == null) {
                constant_pool[index] = f = otherclazz.createInstanceField(nd);
                constant_pool_tags[index] = CONSTANT_ResolvedIFieldRef;
                if (TRACE) Debug.writeln("Resolved instance field "+f+", cp idx "+(int)index);
            } else if (m.isStatic())
                throw new VerifyError("field "+m+" referred to as both static and instance");
            else
                f = (jq_InstanceField)m;
        }
        return f;
    }
    public final jq_StaticMethod getAsStaticMethod(char index) {
        if (constant_pool_tags[index] == CONSTANT_ResolvedSMethodRef)
            return (jq_StaticMethod)constant_pool[index];
        if (constant_pool_tags[index] != CONSTANT_MethodRef)
            throw new VerifyError();
        if (TRACE) Debug.writeln("Attempting to resolve static method "+constant_pool[index]+" cp idx "+(int)index);
        jq_MemberReference n = (jq_MemberReference)constant_pool[index];
        jq_Class otherclazz = n.getReferencedClass();
        jq_NameAndDesc nd = n.getNameAndDesc();
        if (otherclazz.isInClassLib())
            nd = joeq.ClassLib.ClassLibInterface.convertClassLibNameAndDesc(otherclazz, nd);
        jq_StaticMethod f;
        if (otherclazz.isLoaded()) {
            f = otherclazz.getStaticMethod(nd);
            if (f == null) 
                throw new NoSuchMethodError("no such static method "+otherclazz+"."+nd);
        } else {
            // we differ slightly from the vm spec in that when a reference to the member is
            // encountered before the class is loaded, and the member is actually in a
            // superclass/superinterface it will throw a NoSuchFieldError when the member is
            // accessed.
            // Java compilers don't generate such references, unless class files are old.
            jq_Method m = (jq_Method)otherclazz.getDeclaredMember(nd);
            if (m == null) {
                constant_pool[index] = f = otherclazz.createStaticMethod(nd);
                constant_pool_tags[index] = CONSTANT_ResolvedSMethodRef;
                if (TRACE) Debug.writeln("Resolved static method "+f+", cp idx "+(int)index);
            } else if (!m.isStatic())
                throw new VerifyError("method "+m+" referred to as both static and instance");
            else
                f = (jq_StaticMethod)m;
        }
        return f;
    }
    public final jq_InstanceMethod getAsInstanceMethod(char index) {
        if (constant_pool_tags[index] == CONSTANT_ResolvedIMethodRef)
            return (jq_InstanceMethod)constant_pool[index];
        if (constant_pool_tags[index] != CONSTANT_MethodRef &&
            constant_pool_tags[index] != CONSTANT_InterfaceMethodRef)
            throw new VerifyError();
        if (TRACE) Debug.writeln("Attempting to resolve instance method "+constant_pool[index]+" cp idx "+(int)index);
        jq_MemberReference n = (jq_MemberReference)constant_pool[index];
        jq_Class otherclazz = n.getReferencedClass();
        jq_NameAndDesc nd = n.getNameAndDesc();
        if (otherclazz.isInClassLib())
            nd = joeq.ClassLib.ClassLibInterface.convertClassLibNameAndDesc(otherclazz, nd);
        jq_InstanceMethod f;
        if (otherclazz.isLoaded()) {
            f = otherclazz.getInstanceMethod(nd);
            if (f == null) 
                throw new NoSuchMethodError("no such instance method "+otherclazz+"."+nd);
        } else {
            // we differ slightly from the vm spec in that when a reference to the member is
            // encountered before the class is loaded, and the member is actually in a
            // superclass/superinterface it will throw a NoSuchFieldError when the member is
            // accessed.
            // Java compilers don't generate such references, unless class files are old.
            jq_Method m = (jq_Method)otherclazz.getDeclaredMember(nd);
            if (m == null) {
                constant_pool[index] = f = otherclazz.createInstanceMethod(nd);
                constant_pool_tags[index] = CONSTANT_ResolvedIMethodRef;
                if (TRACE) Debug.writeln("Resolved instance method "+f+", cp idx "+(int)index);
            } else if (m.isStatic())
                throw new VerifyError("method "+m+" referred to as both static and instance");
            else
                f = (jq_InstanceMethod)m;
        }
        return f;
    }

    public void trim(Set/*<jq_Field>*/ necessaryFields, Set/*<jq_Method>*/ necessaryMethods) {
        for (int i=0; i<constant_pool.length; ++i) {
            byte cpt = constant_pool_tags[i];
            Object cpe = constant_pool[i];
            switch (cpt) {
                case CONSTANT_ResolvedSFieldRef:
                case CONSTANT_ResolvedIFieldRef:
                    if (!necessaryFields.contains(cpe)) {
                        jq_MemberReference mr = new jq_MemberReference(((jq_Member)cpe).getDeclaringClass(), ((jq_Member)cpe).getNameAndDesc());
                        constant_pool[i] = mr;
                        constant_pool_tags[i] = CONSTANT_FieldRef;
                    }
                    break;
                case CONSTANT_ResolvedSMethodRef:
                case CONSTANT_ResolvedIMethodRef:
                    if (!necessaryMethods.contains(cpe)) {
                        jq_MemberReference mr = new jq_MemberReference(((jq_Member)cpe).getDeclaringClass(), ((jq_Member)cpe).getNameAndDesc());
                        constant_pool[i] = mr;
                         // MethodRef and InterfaceMethodRef are treated as equivalent.
                        constant_pool_tags[i] = CONSTANT_MethodRef;
                    }
                    break;
            }
        }
    }
    
    public char findEqual(Object o, byte tag) {
        for (char i=1; i<constant_pool.length; ++i) {
            if (constant_pool_tags[i] == tag && constant_pool[i].equals(o))
                return i;
        }
        return 0;
    }

    public boolean contains(Object o) {
        for (int i=0; i<constant_pool.length; ++i) {
            if (constant_pool[i] == o)
                return true;
        }
        return false;
    }
    
    private int growCPbyOne() {
        int newsize = constant_pool.length+1;
        Object[] newcp = new Object[newsize];
        System.arraycopy(constant_pool, 0, newcp, 0, constant_pool.length);
        byte[] newcptags = new byte[newsize];
        System.arraycopy(constant_pool_tags, 0, newcptags, 0, constant_pool_tags.length);
        constant_pool = newcp; constant_pool_tags = newcptags;
        return newsize-1;
    }
    
    public int addInteger(int value) {
        int index = growCPbyOne();
        constant_pool[index] = new Integer(value);
        constant_pool_tags[index] = CONSTANT_Integer;
        return index;
    }
    public int addFloat(float value) {
        int index = growCPbyOne();
        constant_pool[index] = new Float(value);
        constant_pool_tags[index] = CONSTANT_Float;
        return index;
    }
    public int addLong(float value) {
        int index = growCPbyOne();
        constant_pool[index] = new Float(value);
        constant_pool_tags[index] = CONSTANT_Float;
        growCPbyOne();
        return index;
    }
    public int addDouble(double value) {
        int index = growCPbyOne();
        constant_pool[index] = new Double(value);
        constant_pool_tags[index] = CONSTANT_Double;
        growCPbyOne();
        return index;
    }
    public int addString(String value) {
        int index = growCPbyOne();
        constant_pool[index] = value;
        constant_pool_tags[index] = CONSTANT_String;
        return index;
    }
    
    Adder getAdder() { return new Adder(); }
    
    class Adder {
        LinkedList toadd_cp = new LinkedList();
        
        public char add(Object o, byte tag) {
            int i;
            for (i=0; i<constant_pool.length; ++i) {
                if (o.equals(get((char)i))) {
                    Assert._assert(getTag((char)i) == tag);
                    return (char)i;
                }
            }
            ConstantPoolEntry cpe = new ConstantPoolEntry(o, tag);
            //Iterator it = toadd_cp.iterator();
            i = toadd_cp.indexOf(cpe);
            if (i != -1) return (char)i;
            i = constant_pool.length + toadd_cp.size();
            toadd_cp.add(cpe);
 
            Assert._assert(i <= Character.MAX_VALUE);
            return (char)i;
        }
        
        public void finish() {
            if (toadd_cp.size() == 0) return;
            
            Object[] new_cp = new Object[constant_pool.length+toadd_cp.size()];
            byte[] new_cptag = new byte[new_cp.length];
            int i = constant_pool.length-1;
            System.arraycopy(constant_pool, 0, new_cp, 0, constant_pool.length);
            System.arraycopy(constant_pool_tags, 0, new_cptag, 0, constant_pool_tags.length);
            for (Iterator it = toadd_cp.iterator(); it.hasNext();) {
                ConstantPoolEntry cpe = (ConstantPoolEntry)it.next();
                new_cp[++i] = cpe.o;
                new_cptag[i] = cpe.tag;
            }
            constant_pool = new_cp;
            constant_pool_tags = new_cptag;
        }
    }

    static class ConstantPoolEntry {
        Object o; byte tag;
        ConstantPoolEntry(Object o, byte tag) { this.o = o; this.tag = tag; }
        public boolean equals(ConstantPoolEntry that) {
            return this.o == that.o && this.tag == that.tag;
        }
        public boolean equals(Object that) {
            if (that instanceof ConstantPoolEntry)
                return equals((ConstantPoolEntry)that);
            return false;
        }
        public int hashCode() { return o.hashCode(); }
    }
    
    public static class ConstantPoolRebuilder {
        HashMap new_entries = new HashMap();

        private int renumber() {
            int j = 0;
            Set entrySet = new_entries.entrySet();
            Iterator i = entrySet.iterator();
            while (i.hasNext()) {
                Map.Entry e = (Map.Entry)i.next();
                Assert._assert(j < Character.MAX_VALUE);
                e.setValue(new Character((char)(++j)));
                if ((e.getKey() instanceof Long) ||
                    (e.getKey() instanceof Double))
                    ++j;
            }
            if (TRACE) Debug.writeln("After renumbering constant pool: "+(j+1)+" entries.");
            return j+1;
        }

        public jq_ConstantPool finish() {
            if (TRACE) Debug.writeln("Finishing rebuilding constant pool...");
            int cp_size = renumber();
            int j = 0;
            Assert._assert(cp_size <= Character.MAX_VALUE);
            jq_ConstantPool newcp = new jq_ConstantPool(cp_size);
            Set entrySet = new_entries.entrySet();
            Iterator i = entrySet.iterator();
            while (i.hasNext()) {
                Map.Entry e = (Map.Entry)i.next();
                Object o = e.getKey();
                char index = ((Character)e.getValue()).charValue();
                ++j; Assert._assert(index == j, (int)index + "!=" + (int)j);
                newcp.constant_pool[j] = o;
                if (o instanceof Utf8) {
                    newcp.constant_pool_tags[j] = CONSTANT_Utf8;
                } else if (o instanceof Integer) {
                    newcp.constant_pool_tags[j] = CONSTANT_Integer;
                } else if (o instanceof Float) {
                    newcp.constant_pool_tags[j] = CONSTANT_Float;
                } else if (o instanceof Long) {
                    newcp.constant_pool_tags[j] = CONSTANT_Long;
                    ++j;
                } else if (o instanceof Double) {
                    newcp.constant_pool_tags[j] = CONSTANT_Double;
                    ++j;
                } else if (o instanceof jq_Type) {
                    newcp.constant_pool_tags[j] = CONSTANT_ResolvedClass;
                } else if (o instanceof String) {
                    newcp.constant_pool_tags[j] = CONSTANT_String;
                } else if (o instanceof jq_NameAndDesc) {
                    newcp.constant_pool_tags[j] = CONSTANT_NameAndType;
                } else if (o instanceof jq_InstanceMethod) {
                    newcp.constant_pool_tags[j] = CONSTANT_ResolvedIMethodRef;
                } else if (o instanceof jq_StaticMethod) {
                    newcp.constant_pool_tags[j] = CONSTANT_ResolvedSMethodRef;
                } else if (o instanceof jq_InstanceField) {
                    newcp.constant_pool_tags[j] = CONSTANT_ResolvedIFieldRef;
                } else if (o instanceof jq_StaticField) {
                    newcp.constant_pool_tags[j] = CONSTANT_ResolvedSFieldRef;
                } else {
                    Assert.UNREACHABLE("CP Entry "+j+": "+o);
                }
            }
            return newcp;
        }

        public void addCode(jq_Method m) {
            if (TRACE) Debug.writeln("Adding code for "+m);
            byte[] bc = m.getBytecode();
            if (bc == null) return;
            Bytecodes.InstructionList il = new Bytecodes.InstructionList(m.getDeclaringClass().getCP(), bc);
            this.addCode(il);
        }

        public void addCode(Bytecodes.InstructionList il) {
            if (TRACE) Debug.writeln("Adding code for "+il);
            RebuildCPVisitor v = new RebuildCPVisitor();
            il.accept(v);
        }
        
        public void addExceptions(jq_Method m) {
            byte[] bc = m.getBytecode();
            if (bc == null) return;
            jq_TryCatchBC[] t = m.getExceptionTable();
            for (int i=0; i<t.length; ++i) {
                jq_Class type = t[i].getExceptionType();
                if (type != null)
                    addType(type);
            }
        }

        public void addAttributeNames(jq_Member f) {
            if (TRACE) Debug.writeln("Adding attribute names for "+f);
            Map m = f.getAttributes();
            for (Iterator i = m.entrySet().iterator(); i.hasNext(); ) {
                Map.Entry e = (Map.Entry)i.next();
                Assert._assert(e.getKey() instanceof Utf8);
                new_entries.put(e.getKey(), null);
            }
        }
        
        public void dump(DataOutput out) throws IOException {
            // note: this relies on the fact that the two iterators return the same order
            int cp_size = renumber();
            int j = 0;
            Assert._assert(cp_size <= Character.MAX_VALUE);
            out.writeChar(cp_size);
            Set entrySet = new_entries.entrySet();
            Iterator i = entrySet.iterator();
            while (i.hasNext()) {
                Map.Entry e = (Map.Entry)i.next();
                Object o = e.getKey();
                char index = ((Character)e.getValue()).charValue();
                ++j; Assert._assert(index == j);
                if (o instanceof Utf8) {
                    out.writeByte(CONSTANT_Utf8);
                    ((Utf8)o).dump(out);
                } else if (o instanceof Integer) {
                    out.writeByte(CONSTANT_Integer);
                    out.writeInt(((Integer)o).intValue());
                } else if (o instanceof Float) {
                    out.writeByte(CONSTANT_Float);
                    out.writeFloat(((Float)o).floatValue());
                } else if (o instanceof Long) {
                    out.writeByte(CONSTANT_Long);
                    out.writeLong(((Long)o).longValue());
                    ++j;
                } else if (o instanceof Double) {
                    out.writeByte(CONSTANT_Double);
                    out.writeDouble(((Double)o).doubleValue());
                    ++j;
                } else if (o instanceof jq_Type) {
                    out.writeByte(CONSTANT_Class);
                    Utf8 u = ((jq_Type)o).getDesc();
                    if (o instanceof jq_Class)
                        u = u.getClassName();
                    out.writeChar(get(u));
                } else if (o instanceof String) {
                    out.writeByte(CONSTANT_String);
                    out.writeChar(get(Utf8.get((String)o)));
                } else if (o instanceof jq_NameAndDesc) {
                    out.writeByte(CONSTANT_NameAndType);
                    jq_NameAndDesc f = (jq_NameAndDesc)o;
                    out.writeChar(get(f.getName()));
                    out.writeChar(get(f.getDesc()));
                } else if (o instanceof jq_Member) {
                    byte b = CONSTANT_MethodRef;
                    jq_Member f = (jq_Member)o;
                    if (f instanceof jq_Field) {
                        b = CONSTANT_FieldRef;
                    } else if (f instanceof jq_InstanceMethod) {
                        f.getDeclaringClass().load();
                        if (f.getDeclaringClass().isInterface()) {
                            b = CONSTANT_InterfaceMethodRef;
                        }
                    }
                    out.writeByte(b);
                    out.writeChar(get(f.getDeclaringClass()));
                    out.writeChar(get(f.getNameAndDesc()));
                } else {
                    Assert.UNREACHABLE();
                }
            }
        }

        public char get(Object o) {
            Assert._assert(o != null);
            if (o instanceof Class) o = Reflection.getJQType((Class)o);
            Character c = (Character)new_entries.get(o);
            if (c == null) {
                Assert.UNREACHABLE("No such constant pool entry: type "+o.getClass()+" value "+o);
            }
            return c.charValue();
        }

        public void addString(String o) {
            if (TRACE) Debug.writeln("Adding string "+o);
            new_entries.put(Utf8.get(o), null);
            new_entries.put(o, null);
        }
        public void addType(jq_Type o) {
            if (TRACE) Debug.writeln("Adding type "+o);
            Utf8 u = o.getDesc();
            if (o instanceof jq_Class)
                u = u.getClassName();
            new_entries.put(u, null);
            new_entries.put(o, null);
        }
        public void addMember(jq_Member o) {
            if (TRACE) Debug.writeln("Adding member "+o);
            new_entries.put(o.getName(), null);
            new_entries.put(o.getDesc(), null);
            new_entries.put(o.getNameAndDesc(), null);
            addType(o.getDeclaringClass());
            new_entries.put(o, null);
        }
        public void addUtf8(Utf8 o) {
            if (TRACE) Debug.writeln("Adding Utf8 "+o);
            new_entries.put(o, null);
        }
        public void addOther(Object o) {
            Assert._assert(!(o instanceof String));
            Assert._assert(!(o instanceof Class));
            if (o == null) return;
            if (TRACE) Debug.writeln("Adding other ("+o.getClass().getName()+") "+o);
            new_entries.put(o, null);
        }
        public void remove(Object o) {
            if (TRACE) Debug.writeln("Removing "+o);
            new_entries.remove(o);
        }
        
        public void resetIndices(Bytecodes.InstructionList il) {
            if (TRACE) Debug.writeln("Resetting indices of "+il);
            final jq_ConstantPool.ConstantPoolRebuilder my_cpr = this;
            Bytecodes.EmptyVisitor v = new Bytecodes.EmptyVisitor() {
                public void visitCPInstruction(Bytecodes.CPInstruction i) {
                    i.setIndex(my_cpr);
                    Assert._assert(i.getIndex() != 0);
                }
            };
            il.accept(v);
        }
        
        class RebuildCPVisitor extends Bytecodes.EmptyVisitor {
            public void visitCPInstruction(Bytecodes.CPInstruction i) {
                Object o = i.getObject();
                if (o instanceof String) {
                    addString((String)o);
                } else if (o instanceof Utf8) {
                    addUtf8((Utf8)o);
                } else if (o instanceof jq_Type) {
                    addType((jq_Type)o);
                } else if (o instanceof jq_Member) {
                    addMember((jq_Member)o);
                } else if (o instanceof Class) {
                    addType(Reflection.getJQType((Class)o));
                } else {
                    addOther(o);
                }
            }
        }
    }
    
    /**
     * As opposed to <code>ConstantPoolRebuilder<\code>, this class
     * does not COMPLETELY rebuild the constant pool. It does instead add new entries
     * to the constantpool given as arguments to its constructor
     * @author Chrislain Razafimahefa <razafima@cui.unige.ch>
     */
    public static class ConstantPoolAdder extends ConstantPoolRebuilder {
        jq_ConstantPool cp;

        public ConstantPoolAdder(jq_ConstantPool cp) {
            this.cp = cp;
        }

        public jq_ConstantPool finish() {
            // use this cp adder to add entrys.
            Adder adder = cp.getAdder();
            int index = -1;

            //int index = cp.length; // index where to start for adding
            //jq.assert(index <= Character.MAX_VALUE);
            //jq_ConstantPool newcp = new jq_ConstantPool(cp_size);
            Set entrySet = new_entries.entrySet();
            Iterator i = entrySet.iterator();
            while (i.hasNext()) {
                Map.Entry e = (Map.Entry) i.next();
                Object o = e.getKey();

                //char index = ((Character)e.getValue()).charValue();
                //++j; jq.assert(index == j, (int)index + "!=" + (int)j);
                //System.out.println("CP Entry "+j+": "+o);
                //newcp.constant_pool[j] = o;

                if (o instanceof Utf8) {
                    index = adder.add(o, CONSTANT_Utf8);
                    //newcp.constant_pool_tags[j] = CONSTANT_Utf8;
                } else if (o instanceof Integer) {
                    index = adder.add(o, CONSTANT_Integer);
                    //newcp.constant_pool_tags[j] = CONSTANT_Integer;
                } else if (o instanceof Float) {
                    index = adder.add(o, CONSTANT_Float);
                    //newcp.constant_pool_tags[j] = CONSTANT_Float;
                } else if (o instanceof Long) {
                    index = adder.add(o, CONSTANT_Long);
                    //newcp.constant_pool_tags[j] = CONSTANT_Long;
                    //++j;
                } else if (o instanceof Double) {
                    index = adder.add(o, CONSTANT_Double);
                    //newcp.constant_pool_tags[j] = CONSTANT_Double;
                    //++j;
                } else if (o instanceof jq_Type) {
                    index = adder.add(o, CONSTANT_ResolvedClass);
                    //newcp.constant_pool_tags[j] = CONSTANT_ResolvedClass;
                } else if (o instanceof String) {
                    index = adder.add(o, CONSTANT_String);
                    //newcp.constant_pool_tags[j] = CONSTANT_String;
                } else if (o instanceof jq_NameAndDesc) {
                    index = adder.add(o, CONSTANT_NameAndType);
                    //newcp.constant_pool_tags[j] = CONSTANT_NameAndType;
                } else if (o instanceof jq_InstanceMethod) {
                    index = adder.add(o, CONSTANT_ResolvedIMethodRef);
                    //newcp.constant_pool_tags[j] = CONSTANT_ResolvedIMethodRef;
                } else if (o instanceof jq_StaticMethod) {
                    index = adder.add(o, CONSTANT_ResolvedSMethodRef);
                    //newcp.constant_pool_tags[j] = CONSTANT_ResolvedSMethodRef;
                } else if (o instanceof jq_InstanceField) {
                    index = adder.add(o, CONSTANT_ResolvedIFieldRef);
                    //newcp.constant_pool_tags[j] = CONSTANT_ResolvedIFieldRef;
                } else if (o instanceof jq_StaticField) {
                    index = adder.add(o, CONSTANT_ResolvedSFieldRef);
                    //newcp.constant_pool_tags[j] = CONSTANT_ResolvedSFieldRef;
                } else {
                    Assert.UNREACHABLE();
                }

                e.setValue(new Character((char) (index++)));
            }

            adder.finish();
            return cp;
        }

        public char get(Object o) {
            if (o instanceof Class) o = Reflection.getJQType((Class)o);
            Character c = (Character) new_entries.get(o);
            if (c == null) {
                new_entries.put(o, null);
                finish();
                c = (Character) new_entries.get(o);
            }
            return c.charValue();
        }

        public void remove(Object o) {
            Assert.UNREACHABLE(
                "No remove allowed in jq_ConstantPool.ConstantPoolAdder! ");
        }

        public void dump(DataOutput out) throws IOException {
            Assert.UNREACHABLE(
                "TODO: implement Dump in jq_ConstantPool.ConstantPoolAdder! ");
        }

    }
}
