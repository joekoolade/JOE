// SinglePassBootImage.java, created Tue Aug 10 23:23:20 2004 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Bootstrap;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import joeq.Allocator.CodeAllocator;
import joeq.Allocator.ObjectLayout;
import joeq.Assembler.Code2CodeReference;
import joeq.Assembler.Code2HeapReference;
import joeq.Assembler.DirectBindCall;
import joeq.Assembler.ExternalReference;
import joeq.Assembler.Heap2CodeReference;
import joeq.Assembler.Heap2HeapReference;
import joeq.Assembler.Reloc;
import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Array;
import joeq.Class.jq_Class;
import joeq.Class.jq_CompiledCode;
import joeq.Class.jq_InstanceField;
import joeq.Class.jq_Member;
import joeq.Class.jq_Method;
import joeq.Class.jq_Primitive;
import joeq.Class.jq_Reference;
import joeq.Class.jq_StaticField;
import joeq.Class.jq_StaticMethod;
import joeq.Class.jq_Type;
import joeq.Linker.ELF.ELF;
import joeq.Linker.ELF.ELFConstants;
import joeq.Linker.ELF.ELFOutput;
import joeq.Linker.ELF.RelocEntry;
import joeq.Linker.ELF.Section;
import joeq.Linker.ELF.SymbolTableEntry;
import joeq.Memory.Address;
import joeq.Memory.CodeAddress;
import joeq.Memory.HeapAddress;
import joeq.Runtime.ExceptionDeliverer;
import joeq.Runtime.Reflection;
import joeq.Runtime.SystemInterface;
import joeq.Runtime.Unsafe;
import joeq.Scheduler.jq_NativeThread;
import jwutil.collections.IdentityHashCodeWrapper;
import jwutil.collections.Pair;
import jwutil.io.DataOutputByteBuffer;
import jwutil.strings.Strings;
import jwutil.util.Assert;

/**
 * SinglePassBootImage
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: SinglePassBootImage.java,v 1.4 2005/04/29 07:41:11 joewhaley Exp $
 */
public class SinglePassBootImage implements ELFConstants {

    public static int MAX_HEAP = 64000000;

    {
        Assert._assert(ObjectLayout.ARRAY_LENGTH_OFFSET == -12);
        Assert._assert(ObjectLayout.STATUS_WORD_OFFSET == -8);
        Assert._assert(ObjectLayout.VTABLE_OFFSET == -4);
        Assert._assert(ObjectLayout.OBJ_HEADER_SIZE == 8);
        Assert._assert(ObjectLayout.ARRAY_HEADER_SIZE == 12);
    }
    
    public static /*final*/ boolean TRACE = false;
    public static final PrintStream out = System.out;
    
    public static final SinglePassBootImage DEFAULT = new SinglePassBootImage(BootstrapCodeAllocator.DEFAULT);
    
    private final Map/*<IdentityHashCodeWrapper, Entry>*/ hash;
    private final LinkedList/*<Entry>*/ forwardRefs;
    private ByteBuffer heapBuffer;
    private final int startAddress;
    private int heapCurrent;

    private BootstrapCodeAllocator bca;
    private Collection data_relocs;
    
    public Set boot_types;
    
    public final Collection toReinitialize;
    
    boolean MULTI_RELOCS = false;
    
    public SinglePassBootImage(BootstrapCodeAllocator bca, int initialCapacity, float loadFactor) {
        this.hash = new HashMap(initialCapacity, loadFactor);
        this.bca = bca;
        this.heapCurrent = this.startAddress = 0;
        this.data_relocs = true // MULTI_RELOCS
            ? (Collection) new HashSet() : new LinkedList();
        this.forwardRefs = new LinkedList();
        this.toReinitialize = new HashSet();
        this.heapBuffer = 
            //ByteBuffer.allocate(MAX_HEAP);
            ByteBuffer.allocateDirect(MAX_HEAP);
        heapBuffer.order(ByteOrder.LITTLE_ENDIAN);
        heapBuffer.limit(MAX_HEAP);
    }
    public SinglePassBootImage(BootstrapCodeAllocator bca, int initialCapacity) {
        this(bca, initialCapacity, 0.75f);
    }
    public SinglePassBootImage(BootstrapCodeAllocator bca) {
        this(bca, 1000000);
    }
    
    public final HeapAddress addressOf(Object o) {
        Assert._assert(!(o instanceof BootstrapAddress));
        return getOrAllocateObject(o);
    }
    
    public final void addCodeReloc(HeapAddress addr, CodeAddress target) {
        if (TRACE) out.println("Adding reloc: heap "+addr.stringRep()+" to code "+target.stringRep());
        Heap2CodeReference r = new Heap2CodeReference(addr, target);
        boolean b = data_relocs.add(r);
        if (!MULTI_RELOCS) Assert._assert(b);
    }
    public final void addDataReloc(HeapAddress addr, HeapAddress target) {
        if (TRACE) out.println("Adding reloc: heap "+addr.stringRep()+" to heap "+target.stringRep());
        Heap2HeapReference r = new Heap2HeapReference(addr, target);
        boolean b = data_relocs.add(r);
        if (!MULTI_RELOCS) Assert._assert(b);
    }
    
    public final void invokeclinit(jq_Class c) {
        // call forName on this type to trigger class initialization
        String cname = c.getName().toString();
        try {
            Class.forName(cname);
        } catch (ClassNotFoundException x) {
            // bootstrapping jvm can't find the class?
            System.err.println("ERROR: bootstrapping jvm cannot find class "+cname);
            Assert.UNREACHABLE();
        }
    }

    private boolean alloc_enabled = false;
    
    public void enableAllocations() { alloc_enabled = true; }
    public void disableAllocations() { alloc_enabled = false; }
    
    public HeapAddress isInitialized(Object o) {
        IdentityHashCodeWrapper k = IdentityHashCodeWrapper.create(o);
        HeapAddress e = (HeapAddress) hash.get(k);
        return e;
    }
    
    public HeapAddress initializeObject(Object o) {
        IdentityHashCodeWrapper k = IdentityHashCodeWrapper.create(o);
        HeapAddress e = (HeapAddress) hash.get(k);
        if (e != null) {
            // Need to re-initialize data.
            layoutObject(o, e, MULTI_RELOCS);
            return e;
        } else {
            // Object not allocated yet, allocate and initialize it now.
            return getOrAllocateObject(o);
        }
    }
    
    public HeapAddress getOrAllocateObject(Object o) {
        if (o == null) return HeapAddress.getNull();
        //jq.Assert(!(o instanceof BootstrapAddress));
        IdentityHashCodeWrapper k = IdentityHashCodeWrapper.create(o);
        HeapAddress e = (HeapAddress) hash.get(k);
        if (e != null) return e;
        // not yet allocated, allocate it.
        Assert._assert(alloc_enabled);
        Class objType = o.getClass();
        try {
            jq_Reference type = (jq_Reference)Reflection.getJQType(objType);
            if (!boot_types.contains(type)) {
                System.err.println("--> class "+type+" is not in the set of boot types!");
                //new Exception().printStackTrace();
                return HeapAddress.getNull();
            }
            Assert._assert(!type.isAddressType());
            //if (!type.isClsInitialized()) {
            //    Assert.UNREACHABLE(type.toString());
            //    return HeapAddress.getNull();
            //}
            int addr;
            int size;
            if (type.isArrayType()) {
                addr = heapCurrent + ObjectLayout.ARRAY_HEADER_SIZE;
                size = ((jq_Array)type).getInstanceSize(Array.getLength(o));
                size = (size+3) & ~3;
                if (TRACE)
                    out.println("Allocating entry "+hash.size()+": "+objType+" length "+Array.getLength(o)+" size "+size+" "+Strings.hex(System.identityHashCode(o))+" at "+Strings.hex(addr));
            } else {
                Assert._assert(type.isClassType());
                addr = heapCurrent + ObjectLayout.OBJ_HEADER_SIZE;
                size = ((jq_Class)type).getInstanceSize();
                if (TRACE)
                    out.println("Allocating entry "+hash.size()+": "+objType+" size "+size+" "+Strings.hex(System.identityHashCode(o))+" at "+Strings.hex(addr)+((o instanceof jq_Type)?": "+o:""));
                Assert._assert(size == ((size+3) & ~3));
            }
            heapCurrent += size;
            BootstrapHeapAddress a = new BootstrapHeapAddress(addr);
            hash.put(k, a);
            layoutObject(o, a, true);
            return a;
        } catch (Exception ie) {
            ie.printStackTrace();
            HashSet visited = new HashSet();
            UnknownObjectException x = new UnknownObjectException(o);
            boolean found = findReferencePath(o, x, visited);
            if (found) {
                SinglePassBootImage.out.println(x);
            }
            return HeapAddress.getNull();
        }
    }
    
    public static boolean IGNORE_UNKNOWN_OBJECTS = false;
    
    public HeapAddress getAddressOf(HeapAddress from, Object to) {
        if (to == null) return HeapAddress.getNull();
        Assert._assert(!(to instanceof BootstrapAddress));
        IdentityHashCodeWrapper k = IdentityHashCodeWrapper.create(to);
        HeapAddress e = (HeapAddress) hash.get(k);
        if (e == null) {
            if (from == null) {
                System.err.println("Unknown object of type: "+to.getClass()+" address: "+Strings.hex(System.identityHashCode(to))+" value: "+to);
                if (IGNORE_UNKNOWN_OBJECTS) return HeapAddress.getNull();
                throw new UnknownObjectException(to);
            } else {
                if (TRACE)
                    out.println("Adding forward reference from "+from.stringRep()+" to "+to.getClass()+" "+Strings.hex(System.identityHashCode(to)));
                forwardRefs.add(new Pair(from, to));
                return HeapAddress.getNull();
            }
        }
        return e;
    }
    
    public void initStaticField(jq_StaticField f) {
        jq_Class k = f.getDeclaringClass();
        jq_Type ftype = f.getType();
        if (ftype.isPrimitiveType()) {
            if (ftype == jq_Primitive.INT) {
                int v = Reflection.getstatic_I(f);
                k.setStaticData(f, v);
            } else if (ftype == jq_Primitive.FLOAT) {
                float v = Reflection.getstatic_F(f);
                k.setStaticData(f, v);
            } else if (ftype == jq_Primitive.LONG) {
                long v = Reflection.getstatic_L(f);
                k.setStaticData(f, v);
            } else if (ftype == jq_Primitive.DOUBLE) {
                double v = Reflection.getstatic_D(f);
                k.setStaticData(f, v);
            } else if (ftype == jq_Primitive.BOOLEAN) {
                int v = Reflection.getstatic_Z(f)?1:0;
                k.setStaticData(f, v);
            } else if (ftype == jq_Primitive.BYTE) {
                byte v = Reflection.getstatic_B(f);
                k.setStaticData(f, v);
            } else if (ftype == jq_Primitive.SHORT) {
                short v = Reflection.getstatic_S(f);
                k.setStaticData(f, v);
            } else if (ftype == jq_Primitive.CHAR) {
                char v = Reflection.getstatic_C(f);
                k.setStaticData(f, v);
            } else
                Assert.UNREACHABLE();
        } else if (ftype.isAddressType()) {
            Address addr = Reflection.getstatic_P(f);
            if (addr == null) addr = HeapAddress.getNull();
            if (TRACE) out.println("Initializing static field "+f+" to "+addr.stringRep());
            k.setStaticData(f, addr);
        } else {
            Object val = Reflection.getstatic_A(f);
            HeapAddress addr = HeapAddress.addressOf(val);
            if (TRACE) out.println("Initializing static field "+f+" to "+addr.stringRep());
            k.setStaticData(f, addr);
        }
    }
    
    public void initStaticFields(jq_Class k) {
        jq_StaticField[] sfs = k.getDeclaredStaticFields();
        for (int j=0; j<sfs.length; ++j) {
            jq_StaticField sf = sfs[j];
            initStaticField(sf);
        }
    }
    
    public void initStaticData(jq_Class k) {
        Object static_data = k.getStaticData();
        if (static_data != null) {
            initializeObject(static_data);
        }
    }
    
    public void addStaticFieldRelocs(jq_Class k) {
        jq_StaticField[] sfs = k.getDeclaredStaticFields();
        for (int j=0; j<sfs.length; ++j) {
            jq_StaticField sf = sfs[j];
            addStaticFieldReloc(sf);
        }
    }
    
    public void addStaticFieldReloc(jq_StaticField f) {
        jq_Type ftype = f.getType();
        if (f.isCodeAddressType()) {
            CodeAddress addr = (CodeAddress)Reflection.getstatic_P(f);
            if (addr != null && !addr.isNull()) {
                if (TRACE)
                    out.println("Adding code reloc for "+f+": "+f.getAddress().stringRep()+" "+addr.stringRep());
                addCodeReloc(f.getAddress(), addr);
            }
        } else if (f.isHeapAddressType()) {
            HeapAddress addr = (HeapAddress)Reflection.getstatic_P(f);
            if (addr != null && !addr.isNull()) {
                if (TRACE)
                    out.println("Adding data reloc for "+f+": "+f.getAddress().stringRep()+" "+addr.stringRep());
                addDataReloc(f.getAddress(), addr);
            }
        } else if (ftype.isReferenceType()) {
            Object val = Reflection.getstatic_A(f);
            if (val != null) {
                if (val instanceof BootstrapAddress) {
                    Assert.UNREACHABLE("Error: "+f+" contains "+((Address)val).stringRep());
                }
                HeapAddress addr = HeapAddress.addressOf(val);
                if (TRACE) out.println("Adding data reloc for "+f+": "+f.getAddress().stringRep()+" "+addr.stringRep());
                addDataReloc(f.getAddress(), addr);
            }
        }
    }
    
    public void initVTable(jq_Reference t) {
        Object vtable = t.getVTable();
        initializeObject(vtable);
    }
    
    public int numOfEntries() { return hash.size(); }

    public void layoutObject(Object o, HeapAddress addr, boolean addReloc) {
        Class objType = o.getClass();
        jq_Reference jqType = (jq_Reference)Reflection.getJQType(objType);
        if (TRACE)
            out.println("Laying out object @"+addr.stringRep()+": "+objType+" "+Strings.hex(System.identityHashCode(o)));
        int status = 0;
        HeapAddress p_vtable = (HeapAddress) addr.offset(ObjectLayout.VTABLE_OFFSET);
        HeapAddress vtable = getAddressOf(p_vtable, jqType.getVTable());
        if (addReloc) addDataReloc(p_vtable, vtable);
        if (jqType.isArrayType()) {
            int address = addr.offset(-ObjectLayout.ARRAY_HEADER_SIZE).to32BitValue();
            heapBuffer.position(address);
            int length = Array.getLength(o);
            putUInt(heapBuffer, length);
            putUInt(heapBuffer, status);
            putAddress(heapBuffer, vtable);
            jq_Type elemType = ((jq_Array)jqType).getElementType();
            if (elemType.isPrimitiveType()) {
                if (elemType == jq_Primitive.INT) {
                    int[] v = (int[])o;
                    for (int k=0; k<length; ++k)
                        putUInt(heapBuffer, v[k]);
                } else if (elemType == jq_Primitive.FLOAT) {
                    float[] v = (float[])o;
                    for (int k=0; k<length; ++k)
                        putUInt(heapBuffer, Float.floatToRawIntBits(v[k]));
                } else if (elemType == jq_Primitive.LONG) {
                    long[] v = (long[])o;
                    for (int k=0; k<length; ++k)
                        putULong(heapBuffer, v[k]);
                } else if (elemType == jq_Primitive.DOUBLE) {
                    double[] v = (double[])o;
                    for (int k=0; k<length; ++k)
                        putULong(heapBuffer, Double.doubleToRawLongBits(v[k]));
                } else if (elemType == jq_Primitive.BOOLEAN) {
                    boolean[] v = (boolean[])o;
                    for (int k=0; k<length; ++k)
                        putUByte(heapBuffer, v[k]?(byte)1:(byte)0);
                } else if (elemType == jq_Primitive.BYTE) {
                    byte[] v = (byte[])o;
                    for (int k=0; k<length; ++k)
                        putByte(heapBuffer, v[k]);
                } else if (elemType == jq_Primitive.SHORT) {
                    short[] v = (short[])o;
                    for (int k=0; k<length; ++k)
                        putShort(heapBuffer, v[k]);
                } else if (elemType == jq_Primitive.CHAR) {
                    char[] v = (char[])o;
                    for (int k=0; k<length; ++k)
                        putUShort(heapBuffer, v[k]);
                } else Assert.UNREACHABLE();
            } else if (elemType.isAddressType()) {
                Address[] v = (Address[])o;
                for (int k=0; k<length; ++k) {
                    putAddress(heapBuffer, v[k]==null?HeapAddress.getNull():v[k]);
                    // This is probably a vtable; relocations are handled elsewhere.
                }
            } else {
                Object[] v = (Object[])o;
                for (int k=0; k<length; ++k) {
                    Object o2 = Reflection.arrayload_A(v, k);
                    HeapAddress p_a2 = (HeapAddress) addr.offset(ObjectLayout.ARRAY_ELEMENT_OFFSET + k*HeapAddress.size());
                    HeapAddress a2 = getAddressOf(p_a2, o2);
                    putAddress(heapBuffer, a2);
                    if (o2 != null) {
                        if (addReloc) addDataReloc(p_a2, a2);
                    }
                }
            }
        } else {
            int address = addr.offset(-ObjectLayout.OBJ_HEADER_SIZE).to32BitValue();
            heapBuffer.position(address);
            Assert._assert(jqType.isClassType());
            jq_Class clazz = (jq_Class)jqType;
            putUInt(heapBuffer, status);
            putAddress(heapBuffer, vtable);
            if (clazz.isSubtypeOf(jq_Reference._class)) {
                // Need to reinitialize "state" field.
                if (TRACE) SinglePassBootImage.out.println("Marking "+o+"@"+addr.stringRep()+" to be reinitialized later.");
                toReinitialize.add(o);
            }
            if (clazz.isSubtypeOf(jq_Member._class)) {
                // Need to reinitialize "state" field.
                if (TRACE) SinglePassBootImage.out.println("Marking "+o+"@"+addr.stringRep()+" to be reinitialized later.");
                toReinitialize.add(o);
            }
            jq_InstanceField[] fields = clazz.getInstanceFields();
            for (int k=0; k<fields.length; ++k) {
                jq_InstanceField f = fields[k];
                if (f == jq_Method._default_compiled_version) {
                    // Skip this field, as it is handled later.
                    int foffset = f.getOffset();
                    if (TRACE) SinglePassBootImage.out.println("Field "+f+" offset "+Strings.shex(foffset)+": handled later.");
                    //toReinitialize.add(o);
                    continue;
                }
                jq_Type ftype = f.getType();
                int foffset = f.getOffset();
                HeapAddress p_f = (HeapAddress) addr.offset(foffset);
                heapBuffer.position(p_f.to32BitValue());
                if (TRACE) SinglePassBootImage.out.println("Field "+f+" offset "+Strings.shex(foffset)+": "+Strings.hex(System.identityHashCode(Reflection.getfield(o, f))));
                if (ftype.isPrimitiveType()) {
                    if (ftype == jq_Primitive.INT)
                        putUInt(heapBuffer, Reflection.getfield_I(o, f));
                    else if (ftype == jq_Primitive.FLOAT)
                        putUInt(heapBuffer, Float.floatToRawIntBits(Reflection.getfield_F(o, f)));
                    else if (ftype == jq_Primitive.LONG)
                        putULong(heapBuffer, Reflection.getfield_L(o, f));
                    else if (ftype == jq_Primitive.DOUBLE)
                        putULong(heapBuffer, Double.doubleToRawLongBits(Reflection.getfield_D(o, f)));
                    else if (ftype == jq_Primitive.BOOLEAN)
                        putUByte(heapBuffer, Reflection.getfield_Z(o, f)?(byte)1:(byte)0);
                    else if (ftype == jq_Primitive.BYTE)
                        putByte(heapBuffer, Reflection.getfield_B(o, f));
                    else if (ftype == jq_Primitive.SHORT)
                        putShort(heapBuffer, Reflection.getfield_S(o, f));
                    else if (ftype == jq_Primitive.CHAR)
                        putUShort(heapBuffer, Reflection.getfield_C(o, f));
                    else Assert.UNREACHABLE();
                } else if (ftype.isAddressType()) {
                    Address a = Reflection.getfield_P(o, f);
                    if (f.isCodeAddressType()) {
                        if (a != null && !a.isNull()) {
                            putAddress(heapBuffer, a);
                            if (addReloc) addCodeReloc(p_f, (CodeAddress) a);
                        } else {
                            putAddress(heapBuffer, CodeAddress.getNull());
                        }
                    } else if (f.isHeapAddressType()) {
                        if (a != null && !a.isNull()) {
                            putAddress(heapBuffer, a);
                            if (addReloc) addDataReloc((HeapAddress) addr.offset(f.getOffset()), (HeapAddress) a);
                        } else {
                            putAddress(heapBuffer, HeapAddress.getNull());
                        }
                    } else if (f.isStackAddressType()) {
                        if (a != null && !a.isNull()) {
                            putAddress(heapBuffer, a);
                            // no reloc necessary.
                        } else {
                            putAddress(heapBuffer, HeapAddress.getNull());
                        }
                    } else if (f.getType().isAddressType()) {
                        Assert.UNREACHABLE("Field has untyped Address type: "+f);
                    }
                } else {
                    Object val = Reflection.getfield_A(o, f);
                    HeapAddress a = getAddressOf(p_f, val);
                    putAddress(heapBuffer, a);
                    if (val != null) {
                        if (addReloc) addDataReloc(p_f, a);
                    }
                }
            }
        }
    }
    
    public void reinitializeObjects() {
        for (Iterator i = toReinitialize.iterator(); i.hasNext(); ) {
            Object o = i.next();
            HeapAddress addr = getAddressOf(null, o);
            if (o instanceof jq_Class) {
                // Reinitialize state field.
                jq_InstanceField f = jq_Reference._state;
                HeapAddress p_f = (HeapAddress) addr.offset(f.getOffset());
                heapBuffer.position(p_f.to32BitValue());
                putInt(heapBuffer, Reflection.getfield_I(o, f));
                i.remove();
            }
            if (o instanceof jq_Member) {
                // Reinitialize state field.
                jq_InstanceField f = jq_Member._state;
                HeapAddress p_f = (HeapAddress) addr.offset(f.getOffset());
                heapBuffer.position(p_f.to32BitValue());
                putByte(heapBuffer, Reflection.getfield_B(o, f));
            }
            if (o instanceof jq_Method) {
                // Set default_compiled_version field and add reloc.
                jq_Method m = (jq_Method) o;
                jq_InstanceField f = jq_Method._default_compiled_version;
                HeapAddress p_f = (HeapAddress) addr.offset(f.getOffset());
                if (!m.isInitialized()) {
                    if (TRACE) out.println("Skipping initialization of default_compiled_code (at "+p_f.stringRep()+") for "+m+" because it is not initialized.");
                    continue;
                }
                jq_CompiledCode cc = m.getDefaultCompiledVersion();
                if (cc != null) {
                    HeapAddress target = getOrAllocateObject(cc);
                    if (TRACE) out.println("Initializing default_compiled_code (at "+p_f.stringRep()+") for "+m+" to "+target.stringRep());
                    heapBuffer.position(p_f.to32BitValue());
                    putAddress(heapBuffer, target);
                    addDataReloc(p_f, target);
                    i.remove();
                }
            }
        }
    }
    
    public int size() { return heapCurrent-startAddress; }
    
    public static int UPDATE_PERIOD = 10000;

    public void handleForwardReferences() {
        int i = 0;
        while (!forwardRefs.isEmpty()) {
            if ((i % UPDATE_PERIOD) == 0) {
                int total = i + forwardRefs.size();
                out.print("Traversing heap: "+i+"/"+total+"\r");
            }
            Pair e = (Pair) forwardRefs.removeFirst();
            HeapAddress from = (HeapAddress) e.left;
            Object to = e.right;
            HeapAddress a = getOrAllocateObject(to);
            if (TRACE)
                out.println("Resolving forward reference from "+from.stringRep()+" to "+to.getClass()+" "+Strings.hex(System.identityHashCode(to))+": "+a.stringRep());
            if (false) {
                // This assertion fails when a field reference has changed
                // and points to a new object.
                int oldValue = heapBuffer.getInt(from.to32BitValue());
                Assert._assert(oldValue == HeapAddress.getNull().to32BitValue() ||
                               oldValue == a.to32BitValue());
            }
            putAddressAt(heapBuffer, from.to32BitValue(), a);
            ++i;
        }
    }
    
    public static final char F_RELFLG = (char)0x0001;
    public static final char F_EXEC   = (char)0x0002;
    public static final char F_LNNO   = (char)0x0004;
    public static final char F_LSYMS  = (char)0x0008;
    public static final char F_AR32WR = (char)0x0100;
    
    static void putUByte(ByteBuffer out, byte b) {
        out.put(b);
    }
    
    static void putByte(ByteBuffer out, byte b) {
        out.put(b);
    }
    
    static void putBytes(ByteBuffer out, byte[] b) {
        out.put(b);
    }
    
    static void putUShort(ByteBuffer out, char c) {
        out.putChar(c);
    }
    
    static void putShort(ByteBuffer out, short s) {
        out.putShort(s);
    }
    
    static void putUInt(ByteBuffer out, int i) {
        out.putInt(i);
    }
    
    static void putInt(ByteBuffer out, int i) {
        out.putInt(i);
    }
    
    static void putULong(ByteBuffer out, long i) {
        out.putLong(i);
    }
    
    static void putAddress(ByteBuffer out, Address a) {
        out.putInt(a.to32BitValue());
    }
    
    static void putAddressAt(ByteBuffer out, int offset, Address a) {
        out.putInt(offset, a.to32BitValue());
    }
    
    public void dumpFILHDR(ByteBuffer out, int symptr, int nsyms)
    throws IOException {
        out.position(0);
        // FILHDR
        putUShort(out, (char)0x014c);  // f_magic
        putUShort(out, (char)2);       // f_nscns
        long ms = System.currentTimeMillis();
        int s = (int)(ms/1000);
        putUInt(out, s);               // f_timdat
        putUInt(out, symptr);          // f_symptr
        putUInt(out, nsyms);           // f_nsyms
        putUShort(out, (char)0);       // f_opthdr
        putUShort(out, (char)(F_LNNO | F_LSYMS | F_AR32WR)); // f_flags
    }
    
    public static final int STYP_TEXT  = 0x00000020;
    public static final int STYP_DATA  = 0x00000040;
    public static final int STYP_BSS   = 0x00000080;
    public static final int STYP_RELOV = 0x01000000;
    public static final int STYP_EXEC  = 0x20000000;
    public static final int STYP_READ  = 0x40000000;
    public static final int STYP_WRITE = 0x80000000;
    
    public void dumpTEXTSCNHDR(ByteBuffer out, int size, int nreloc)
    throws IOException {
        out.position(20);
        // SCNHDR
        write_bytes(out, ".text", 8);       // s_name
        putUInt(out, 0);                // s_paddr
        putUInt(out, 0);                // s_vaddr
        putUInt(out, size);             // s_size
        putUInt(out, 20+40+40);         // s_scnptr
        putUInt(out, 20+40+40+size);    // s_relptr
        putUInt(out, 0);                // s_lnnoptr
        if (nreloc > 65535)
            putUShort(out, (char)0xffff); // s_nreloc
        else
            putUShort(out, (char)nreloc); // s_nreloc
        putUShort(out, (char)0);         // s_nlnno
        if (nreloc > 65535)
            putUInt(out, STYP_TEXT | STYP_READ | STYP_WRITE | STYP_RELOV); // s_flags
        else
            putUInt(out, STYP_TEXT | STYP_READ | STYP_WRITE); // s_flags
    }
    
    public void dumpDATASCNHDR(ByteBuffer out, int scnptr, int size, int nreloc)
    throws IOException {
        out.position(60);
        // SCNHDR
        write_bytes(out, ".data", 8);       // s_name
        putUInt(out, 0);                // s_paddr
        putUInt(out, 0);                // s_vaddr
        putUInt(out, size);             // s_size
        putUInt(out, scnptr);           // s_scnptr
        putUInt(out, scnptr+size);      // s_relptr
        putUInt(out, 0);                // s_lnnoptr
        if (nreloc > 65535)
            putUShort(out, (char)0xffff); // s_nreloc
        else
            putUShort(out, (char)nreloc); // s_nreloc
        putUShort(out, (char)0);         // s_nlnno
        if (nreloc > 65535)
            putUInt(out, STYP_DATA | STYP_READ | STYP_WRITE | STYP_RELOV); // s_flags
        else
            putUInt(out, STYP_DATA | STYP_READ | STYP_WRITE); // s_flags
    }
    
    public static final char RELOC_ADDR32 = (char)0x0006;
    public static final char RELOC_REL32  = (char)0x0014;
    
    public static final short N_UNDEF = 0;
    public static final short N_ABS   = -1;
    public static final short N_DEBUG = -2;
    
    public static final char T_NULL   = 0x00;
    public static final char T_VOID   = 0x01;
    public static final char T_CHAR   = 0x02;
    public static final char T_SHORT  = 0x03;
    public static final char T_INT    = 0x04;
    public static final char T_LONG   = 0x05;
    public static final char T_FLOAT  = 0x06;
    public static final char T_DOUBLE = 0x07;
    public static final char T_STRUCT = 0x08;
    public static final char T_UNION  = 0x09;
    public static final char T_ENUM   = 0x0A;
    public static final char T_MOE    = 0x0B;
    public static final char T_UCHAR  = 0x0C;
    public static final char T_USHORT = 0x0D;
    public static final char T_UINT   = 0x0E;
    public static final char T_ULONG  = 0x0F;
    public static final char T_LNGDBL = 0x10;
    
    public static final char DT_NON = 0x0000;
    public static final char DT_PTR = 0x0100;
    public static final char DT_FCN = 0x0200;
    public static final char DT_ARY = 0x0300;

    public static final byte C_NULL   = 0;
    public static final byte C_AUTO   = 1;
    public static final byte C_EXT    = 2;
    public static final byte C_STAT   = 3;
    public static final byte C_REG    = 4;
    public static final byte C_EXTDEF = 5;
    public static final byte C_LABEL  = 6;
    public static final byte C_ULABEL = 7;
    public static final byte C_MOS     = 8;
    public static final byte C_ARG     = 9;
    public static final byte C_STRTAG  = 10;
    public static final byte C_MOU     = 11;
    public static final byte C_UNTAG   = 12;
    public static final byte C_TPDEF   = 13;
    public static final byte C_USTATIC = 14;
    public static final byte C_ENTAG   = 15;
    public static final byte C_MOE     = 16;
    public static final byte C_REGPARM = 17;
    public static final byte C_FIELD   = 18;
    public static final byte C_AUTOARG = 19;
    public static final byte C_LASTENT = 20;
    public static final byte C_BLOCK   = 100;
    public static final byte C_FCN     = 101;
    public static final byte C_EOS     = 102;
    public static final byte C_FILE    = 103;
    public static final byte C_SECTION = 104;
    public static final byte C_WEAKEXT = 105;
    public static final byte C_EFCN    = -1;
    
    public void dumpSECTIONSYMENTs(ByteBuffer out)
    throws IOException {
        write_bytes(out, ".text", 8);
        putUInt(out, 0);
        putShort(out, (short)1);
        putUShort(out, (char)0);
        putUByte(out, C_STAT);
        putUByte(out, (byte)0);
        
        write_bytes(out, ".data", 8);
        putUInt(out, 0);
        putShort(out, (short)2);
        putUShort(out, (char)0);
        putUByte(out, C_STAT);
        putUByte(out, (byte)0);
    }
    
    public static boolean USE_MICROSOFT_STYLE_MUNGE = true;
    
    public static final int NUM_OF_EXTERNAL_SYMS = 9;
    public void dumpEXTSYMENTs(ByteBuffer out, jq_StaticMethod rootm)
    throws IOException {
        // NOTE!!! If you change anything here, be SURE to change the number above!!!
        String s;
        if (USE_MICROSOFT_STYLE_MUNGE) s = "_entry@0";
        else s = "entry";
        write_bytes(out, s, 8);  // s_name
        CodeAddress addr = rootm.getDefaultCompiledVersion().getEntrypoint();
        putUInt(out, addr.to32BitValue());
        putShort(out, (short)1);
        putUShort(out, (char)DT_FCN);
        putUByte(out, C_EXT); // e_sclass
        putUByte(out, (byte)0); // e_numaux
        
        putUInt(out, 0);    // e_zeroes
        if (USE_MICROSOFT_STYLE_MUNGE) s = "_trap_handler@4";
        else s = "trap_handler";
        int idx = alloc_string(s);
        putUInt(out, idx);  // e_offset
        addr = ExceptionDeliverer._trap_handler.getDefaultCompiledVersion().getEntrypoint();
        putUInt(out, addr.to32BitValue());
        putShort(out, (short)1);
        putUShort(out, (char)DT_FCN);
        putUByte(out, C_EXT); // e_sclass
        putUByte(out, (byte)0); // e_numaux

        putUInt(out, 0);    // e_zeroes
        if (USE_MICROSOFT_STYLE_MUNGE) s = "_debug_trap_handler@4";
        else s = "debug_trap_handler";
        idx = alloc_string(s);
        putUInt(out, idx);  // e_offset
        addr = ExceptionDeliverer._debug_trap_handler.getDefaultCompiledVersion().getEntrypoint();
        putUInt(out, addr.to32BitValue());
        putShort(out, (short)1);
        putUShort(out, (char)DT_FCN);
        putUByte(out, C_EXT); // e_sclass
        putUByte(out, (byte)0); // e_numaux
        
        putUInt(out, 0);    // e_zeroes
        if (USE_MICROSOFT_STYLE_MUNGE) s = "_threadSwitch@4";
        else s = "threadSwitch";
        idx = alloc_string(s);
        putUInt(out, idx);  // e_offset
        addr = jq_NativeThread._threadSwitch.getDefaultCompiledVersion().getEntrypoint();
        putUInt(out, addr.to32BitValue());
        putShort(out, (short)1);
        putUShort(out, (char)DT_FCN);
        putUByte(out, C_EXT); // e_sclass
        putUByte(out, (byte)0); // e_numaux
        
        putUInt(out, 0);    // e_zeroes
        if (USE_MICROSOFT_STYLE_MUNGE) s = "_ctrl_break_handler@0";
        else s = "ctrl_break_handler";
        idx = alloc_string(s);
        putUInt(out, idx);  // e_offset
        addr = jq_NativeThread._ctrl_break_handler.getDefaultCompiledVersion().getEntrypoint();
        putUInt(out, addr.to32BitValue()); // e_value
        putShort(out, (short)1);
        putUShort(out, (char)DT_FCN);
        putUByte(out, C_EXT); // e_sclass
        putUByte(out, (byte)0); // e_numaux

        putUInt(out, 0);    // e_zeroes
        if (USE_MICROSOFT_STYLE_MUNGE) s = "_joeq_code_startaddress";
        else s = "joeq_code_startaddress";
        idx = alloc_string(s);
        putUInt(out, idx);  // e_offset
        putUInt(out, 0); // e_value
        putShort(out, (short)1);
        putUShort(out, (char)(DT_PTR | T_VOID));
        putUByte(out, C_EXT); // e_sclass
        putUByte(out, (byte)0); // e_numaux

        putUInt(out, 0);    // e_zeroes
        if (USE_MICROSOFT_STYLE_MUNGE) s = "_joeq_code_endaddress";
        else s = "joeq_code_endaddress";
        idx = alloc_string(s);
        putUInt(out, idx);  // e_offset
        putUInt(out, textsize); // e_value
        putShort(out, (short)1);
        putUShort(out, (char)(DT_PTR | T_VOID));
        putUByte(out, C_EXT); // e_sclass
        putUByte(out, (byte)0); // e_numaux

        putUInt(out, 0);    // e_zeroes
        if (USE_MICROSOFT_STYLE_MUNGE) s = "_joeq_data_startaddress";
        else s = "joeq_data_startaddress";
        idx = alloc_string(s);
        putUInt(out, idx); // e_offset
        putUInt(out, 0); // e_value
        putShort(out, (short)2); // e_scnum
        putUShort(out, (char)(DT_PTR | T_VOID)); // e_type
        putUByte(out, C_EXT); // e_sclass
        putUByte(out, (byte)0); // e_numaux
        
        putUInt(out, 0);    // e_zeroes
        if (USE_MICROSOFT_STYLE_MUNGE) s = "_joeq_data_endaddress";
        else s = "joeq_data_endaddress";
        idx = alloc_string(s);
        putUInt(out, idx); // e_offset
        putUInt(out, heapCurrent); // e_value
        putShort(out, (short)2); // e_scnum
        putUShort(out, (char)(DT_PTR | T_VOID)); // e_type
        putUByte(out, C_EXT); // e_sclass
        putUByte(out, (byte)0); // e_numaux
    }
    
    public void dumpEXTDEFSYMENTs(ByteBuffer out, List extrefs)
    throws IOException {
        Iterator i = extrefs.iterator();
        int k = 2+NUM_OF_EXTERNAL_SYMS;
        while (i.hasNext()) {
            ExternalReference extref = (ExternalReference)i.next();
            Assert._assert(extref.getSymbolIndex() == k);
            String name = extref.getName();
            if (name.length() <= 8) {
                write_bytes(out, name, 8);  // s_name
            } else {
                putUInt(out, 0);    // e_zeroes
                int idx = alloc_string(name);
                putUInt(out, idx);  // e_offset
            }
            putUInt(out, 0);
            putShort(out, (short)0);
            putUShort(out, (char)DT_FCN);
            putUByte(out, C_EXT);
            putUByte(out, (byte)0);
            ++k;
        }
    }
    
    public void dumpMETHODSYMENT(ByteBuffer out, jq_CompiledCode cc)
    throws IOException {
        jq_Method m = cc.getMethod();
        String name;
        if (m == null) {
            name = "unknown@"+cc.getEntrypoint().stringRep();
        } else { 
            //name = m.getName().toString();
            name = mungeMemberName(m);
        }
        if (name.length() <= 8) {
            write_bytes(out, name, 8);    // s_name
        } else {
            putUInt(out, 0);          // e_zeroes
            int idx = alloc_string(name);
            putUInt(out, idx);        // e_offset
        }
        CodeAddress addr = cc.getEntrypoint();
        putUInt(out, addr.to32BitValue()); // e_value
        putShort(out, (short)1);      // e_scnum
        putUShort(out, (char)DT_FCN); // e_type
        putUByte(out, C_EXT);         // e_sclass
        putUByte(out, (byte)0);             // e_numaux
    }
    
    public void addSystemInterfaceRelocs_COFF(Collection extref, Collection dataRelocs) {
        jq_StaticField[] fs = SystemInterface._class.getDeclaredStaticFields();
        int total = 1+NUM_OF_EXTERNAL_SYMS;
        for (int i=0; i<fs.length; ++i) {
            jq_StaticField f = fs[i];
            if (f.isFinal()) continue;
            if (f.getType() == CodeAddress._class) {
                String name = f.getName().toString();
                int ind = name.lastIndexOf('_');
                if (USE_MICROSOFT_STYLE_MUNGE)
                    name = "_"+name.substring(0, ind)+"@"+name.substring(ind+1);
                else
                    name = name.substring(0, ind);
                if (TRACE) SinglePassBootImage.out.println("External ref="+f+", symndx="+(total+1)+" address="+f.getAddress().stringRep());
                ExternalReference r = new ExternalReference(f.getAddress(), name);
                r.setSymbolIndex(++total);
                extref.add(r);
                dataRelocs.add(r);
            } else if (f.getType() == HeapAddress._class) {
                String name = f.getName().toString();
                if (USE_MICROSOFT_STYLE_MUNGE)
                    name = "_"+name;
                if (TRACE) SinglePassBootImage.out.println("External ref="+f+", symndx="+(total+1)+" address="+f.getAddress().stringRep());
                ExternalReference r = new ExternalReference(f.getAddress(), name);
                r.setSymbolIndex(++total);
                extref.add(r);
                dataRelocs.add(r);
            }
        }
        //return total-3;
    }

    public void addSystemInterfaceRelocs_ELF(Collection extref, Collection dataRelocs) {
        jq_StaticField[] fs = SystemInterface._class.getDeclaredStaticFields();
        int total = 1+NUM_OF_EXTERNAL_SYMS;
        for (int i=0; i<fs.length; ++i) {
            jq_StaticField f = fs[i];
            if (f.isFinal()) continue;
            if (f.getType() == CodeAddress._class) {
                String name = f.getName().toString();
                int ind = name.lastIndexOf('_');
                name = name.substring(0, ind);
                if (TRACE) SinglePassBootImage.out.println("External ref="+f+", symndx="+(total+1)+" address="+f.getAddress().stringRep());
                ExternalReference r = new ExternalReference(f.getAddress(), name);
                r.setSymbolIndex(++total);
                extref.add(r);
                dataRelocs.add(r);
            } else if (f.getType() == HeapAddress._class) {
                String name = f.getName().toString();
                if (TRACE) SinglePassBootImage.out.println("External ref="+f+", symndx="+(total+1)+" address="+f.getAddress().stringRep());
                ExternalReference r = new ExternalReference(f.getAddress(), name);
                r.setSymbolIndex(++total);
                extref.add(r);
                dataRelocs.add(r);
            }
        }
    }
    
    public int addVTableRelocs(Collection list) {
        int total = 0;
        Iterator i = boot_types.iterator();
        while (i.hasNext()) {
            jq_Type t = (jq_Type)i.next();
            if (t.isReferenceType()) {
                if (t == Unsafe._class) continue;
                try {
                    if (TRACE) out.println("Adding vtable relocs for: "+t);
                    Address[] vtable = (Address[])((jq_Reference)t).getVTable();
                    HeapAddress addr = getAddressOf(null, vtable);
                    //jq.Assert(vtable[0] != 0, t.toString());
                    Heap2HeapReference r1 = new Heap2HeapReference(addr, (HeapAddress) vtable[0]);
                    list.add(r1);
                    if (TRACE) out.println("Adding reloc: heap "+addr.stringRep()+" to heap "+vtable[0].stringRep());
                    for (int j=1; j<vtable.length; ++j) {
                        HeapAddress from = (HeapAddress) addr.offset(CodeAddress.size()*j);
                        Heap2CodeReference r2 = new Heap2CodeReference(from, (CodeAddress) vtable[j]);
                        if (TRACE) out.println("Adding reloc: heap "+from.stringRep()+" to code "+vtable[j].stringRep());
                        list.add(r2);
                    }
                    total += vtable.length;
                } catch (UnknownObjectException x) {
                    x.appendMessage("vtable for "+t);
                    x.setObject(null);
                    throw x;
                }
            }
        }
        return total;
    }
    
    int textsize;
    
    public void dumpCOFF(FileChannel fc, jq_StaticMethod rootm) throws IOException {
        
        final List text_relocs1 = bca.getAllCodeRelocs();
        final List text_relocs2 = bca.getAllDataRelocs();
        
        Iterator i = text_relocs1.iterator();
        while (i.hasNext()) {
            Object r = i.next();
            ((Reloc)r).patch();
            // directly bound calls do not need to be relocated,
            // because they are relative offsets, not absolute addresses.
            if (r instanceof DirectBindCall)
                i.remove();
        }
        
        // calculate sizes/offsets
        textsize = bca.size();
        final List exts = new LinkedList();
        final int nlinenum = 0;
        int ntextreloc = text_relocs1.size() + text_relocs2.size();
        if (ntextreloc > 65535) ++ntextreloc;
        final int datastart = 20+40+40+textsize+(10*ntextreloc);
        final int datasize = heapCurrent;
        final int numOfVTableRelocs = addVTableRelocs(data_relocs);
        addSystemInterfaceRelocs_COFF(exts, data_relocs);
        int ndatareloc = data_relocs.size();
        if (ndatareloc > 65535) ++ndatareloc;
        final int symtabstart = datastart+datasize+(10*ndatareloc)+(10*nlinenum);
        final int num_ccs = CodeAllocator.getNumberOfCompiledMethods();
        final int nsyms = 2+NUM_OF_EXTERNAL_SYMS+num_ccs+exts.size();
        final int strtabstart = symtabstart+(18*nsyms);
        
        if (TRACE) {
            SinglePassBootImage.out.println("Text size="+textsize);
            SinglePassBootImage.out.println("Num text relocs="+ntextreloc);
            SinglePassBootImage.out.println("Data start="+datastart);
            SinglePassBootImage.out.println("Data size="+datasize);
            SinglePassBootImage.out.println("Num of VTable relocs="+numOfVTableRelocs);
            SinglePassBootImage.out.println("Num data relocs="+ntextreloc);
            SinglePassBootImage.out.println("Sym tab start="+symtabstart);
            SinglePassBootImage.out.println("Num syms="+nsyms);
            SinglePassBootImage.out.println("Str tab start="+strtabstart);
        }
        
        out.println("Writing bytes "+0+".."+(datastart-1));
        MappedByteBuffer out = fc.map(MapMode.READ_WRITE, 0, datastart);
        out.order(ByteOrder.LITTLE_ENDIAN);
        
        // write file header
        dumpFILHDR(out, symtabstart, nsyms);
        
        // write section headers
        dumpTEXTSCNHDR(out, textsize, ntextreloc);
        dumpDATASCNHDR(out, datastart, datasize, ndatareloc);
        
        // write text section
        bca.dump(out);
        bca = null; // help GC
        
        // write text relocs
        if (ntextreloc > 65535) {
            putUInt(out, ntextreloc);
            putUInt(out, 0);
            putUShort(out, (char)0);
        }
        
        DataOutput dout = new DataOutputByteBuffer(out);
        
        Iterator it = text_relocs1.iterator();
        while (it.hasNext()) {
            Reloc r = (Reloc)it.next();
            r.dumpCOFF(dout);
        }
        it = text_relocs2.iterator();
        while (it.hasNext()) {
            Reloc r = (Reloc)it.next();
            r.dumpCOFF(dout);
        }
        out.force();
        Assert._assert(out.remaining() == 0, "remaining="+out.remaining()+" limit="+out.limit()+" pos="+out.position());
        
        // write data section
        SinglePassBootImage.out.println("Writing bytes "+datastart+".."+(datastart+datasize-1));
        heapBuffer.position(0);
        heapBuffer.limit(datasize);
        fc.write(heapBuffer, datastart);
        heapBuffer = null; // to help GC
        
        SinglePassBootImage.out.println("Writing bytes "+(datastart+datasize)+".."+(strtabstart-1));
        out = fc.map(MapMode.READ_WRITE, datastart+datasize, strtabstart - (datastart+datasize));
        out.order(ByteOrder.LITTLE_ENDIAN);
        dout = new DataOutputByteBuffer(out);
        
        // write data relocs
        int j=0;
        if (ndatareloc > 65535) {
            putUInt(out, ndatareloc);
            putUInt(out, 0);
            putUShort(out, (char)0);
            ++j;
        }
        it = data_relocs.iterator();
        while (it.hasNext()) {
            if ((j % UPDATE_PERIOD) == 0) {
                int bytes = out.position();
                int end = 10*ndatareloc;
                SinglePassBootImage.out.print("Written: "+j+"/"+ndatareloc+" relocations, "+bytes+"/"+end+" bytes\r");
            }
            Reloc r = (Reloc)it.next();
            r.dumpCOFF(dout);
            ++j;
        }
        SinglePassBootImage.out.println("Written: "+ndatareloc+" relocations                                  \n");
        Assert._assert(j == ndatareloc);
        
        // write line numbers
        
        // write symbol table
        dumpSECTIONSYMENTs(out);
        dumpEXTSYMENTs(out, rootm);
        dumpEXTDEFSYMENTs(out, exts);
        it = CodeAllocator.getCompiledMethods();
        j=0;
        while (it.hasNext()) {
            jq_CompiledCode r = (jq_CompiledCode)it.next();
            dumpMETHODSYMENT(out, r);
            ++j;
        }
        Assert._assert(j == num_ccs);
        
        out.force();
        Assert._assert(out.remaining() == 0, "remaining="+out.remaining()+" limit="+out.limit()+" pos="+out.position());
        
        // write string table
        int strTabSize = stringTable_size();
        SinglePassBootImage.out.println("Writing bytes "+strtabstart+".."+(strtabstart+strTabSize));
        out = fc.map(MapMode.READ_WRITE, strtabstart, strTabSize);
        out.order(ByteOrder.LITTLE_ENDIAN);
        dump_strings(out);
        
        out.force();
        Assert._assert(out.remaining() == 0, "remaining="+out.remaining()+" limit="+out.limit()+" pos="+out.position());
        
        fc.force(true);
    }

    static class UnknownObjectException extends RuntimeException {
        /**
         * Version ID for serialization.
         */
        private static final long serialVersionUID = 3258695407449421621L;
        Object o; StringBuffer message;
        UnknownObjectException(Object o) {
            this.o = o;
            this.message = new StringBuffer();
            this.message.append("type: ");
            this.message.append(o.getClass().toString());
            this.message.append(" address: ");
            this.message.append(Strings.hex(System.identityHashCode(o)));
            this.message.append(' ');
        }
        void setObject(Object o) { this.o = o; }
        Object getObject() { return o; }
        void prependMessage(String s) {
            StringBuffer sb = new StringBuffer();
            sb.append(s);
            sb.append(this.message);
            this.message = sb;
        }
        void appendMessage(String s) { this.message.append(s); }
        public String toString() { return this.message.toString(); }
    }

    private jq_StaticField searchStaticVariables(Object p) {
        int num = PrimordialClassLoader.loader.getNumTypes();
        jq_Type[] types = PrimordialClassLoader.loader.getAllTypes();
        for (int i = 0; i < num; ++i) {
            Object o = types[i];
            if (!(o instanceof jq_Class)) continue;
            jq_Class k = (jq_Class) o;
            if (!k.isLoaded()) continue;
            jq_StaticField[] fs = k.getDeclaredStaticFields();
            for (int j=0; j<fs.length; ++j) {
                jq_StaticField f = fs[j];
                if (f.getType().isAddressType()) {
                    // not a possible path.
                } else if (f.getType().isReferenceType()) {
                    Object val = Reflection.getstatic_A(f);
                    if (val == p) return f;
                }
            }
        }
        return null;
    }

    private boolean findReferencePath(Object p, UnknownObjectException x, HashSet visited) {
        jq_StaticField sf = searchStaticVariables(p);
        if (sf != null) {
            x.appendMessage(sf.getDeclaringClass()+"."+sf.getName());
            return true;
        }
        Iterator i = hash.keySet().iterator();
        while (i.hasNext()) {
            IdentityHashCodeWrapper w = (IdentityHashCodeWrapper) i.next();
            if (visited.contains(w)) continue;
            Object o = w.getObject();
            Class objType = o.getClass();
            jq_Reference jqType = (jq_Reference)Reflection.getJQType(objType);
            if (jqType.isArrayType()) {
                jq_Type elemType = ((jq_Array)jqType).getElementType();
                if (elemType.isAddressType()) {
                    // not a possible path.
                } else if (elemType.isReferenceType()) {
                    int length = Array.getLength(o);
                    Object[] v = (Object[])o;
                    for (int k=0; k<length; ++k) {
                        Object o2 = Reflection.arrayload_A(v, k);
                        if (o2 == p) {
                            System.err.println("Possible path: ["+k+"]");
                            visited.add(w);
                            if (findReferencePath(o, x, visited)) {
                                x.appendMessage("["+k+"]");
                                return true;
                            } else {
                                System.err.println("Backtracking ["+k+"]");
                            }
                        }
                    }
                }
            } else {
                Assert._assert(jqType.isClassType());
                jq_Class clazz = (jq_Class)jqType;
                jq_InstanceField[] fields = clazz.getInstanceFields();
                for (int k=0; k<fields.length; ++k) {
                    jq_InstanceField f = fields[k];
                    jq_Type ftype = f.getType();
                    if (ftype.isAddressType()) {
                        // not a possible path.
                    } else if (ftype.isReferenceType()) {
                        Object val = Reflection.getfield_A(o, f);
                        if (val == p) {
                            System.err.println("Possible path: ."+f.getName());
                            visited.add(w);
                            if (findReferencePath(o, x, visited)) {
                                x.appendMessage("."+f.getName());
                                return true;
                            } else {
                                System.err.println("Backtracking ."+f.getName());
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
    
    public static void write_bytes(ByteBuffer out, String s, int len)
    throws IOException {
        Assert._assert(s.length() <= len);
        int i;
        for (i=0; ; ++i) {
            if (i == s.length()) {
                for (; i<len; ++i) {
                    putByte(out, (byte)0);
                }
                return;
            }
            putByte(out, (byte)s.charAt(i));
        }
    }
    
    private String mungeMemberName(jq_Member m) {
        String name = m.getDeclaringClass().getName().toString() +
                      "_"+m.getName()+
                      "_"+m.getDesc();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < name.length(); ++i) {
            char c = name.charAt(i);
            switch (c) {
                case '.':
                case '/':
                case '(':
                case ')':
                case ';':
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        return sb.toString();
    }
    
    int stringTableOffset = 4;
    List stringTable = new LinkedList();
    private int alloc_string(String name) {
        int off = stringTableOffset;
        byte[] b = SystemInterface.toCString(name);
        stringTable.add(b);
        stringTableOffset += b.length;
        return off;
    }

    private void dump_strings(ByteBuffer out)
    throws IOException {
        Iterator i = stringTable.iterator();
        putUInt(out, stringTableOffset);
        while (i.hasNext()) {
            byte[] b = (byte[])i.next();
            putBytes(out, b);
        }
    }

    private int stringTable_size() {
        int total = 0;
        Iterator i = stringTable.iterator();
        total += 4;
        while (i.hasNext()) {
            byte[] b = (byte[])i.next();
            total += b.length;
        }
        return total;
    }
    
    public void dumpELF(FileChannel fc, jq_StaticMethod rootm) throws IOException {
        // todo!
        MappedByteBuffer bb = fc.map(MapMode.READ_WRITE, 0, 80000000);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        
        final List text_relocs1 = bca.getAllCodeRelocs();
        final List text_relocs2 = bca.getAllDataRelocs();
        Iterator i = text_relocs1.iterator();
        while (i.hasNext()) {
            Object r = i.next();
            ((Reloc)r).patch();
            // directly bound calls do not need to be relocated,
            // because they are relative offsets, not absolute addresses.
            if (r instanceof DirectBindCall)
                i.remove();
        }

        DataOutput out = new DataOutputByteBuffer(bb);
        SinglePassBootImage.out.print("Initializing ELF data structures...");
        long time = System.currentTimeMillis();
        //final int datasize = heapCurrent;
        ELFOutput f = new ELFOutput(ELFDATA2LSB, ET_REL, EM_386, 0, out);
        f.setLittleEndian();
        Section.NullSection empty = Section.NullSection.INSTANCE;
        Section.StrTabSection shstrtab = new Section.StrTabSection(".shstrtab", 0, 0);
        Section.StrTabSection strtab = new Section.StrTabSection(".strtab", 0, 0);
        Section.SymTabSection symtab = new Section.SymTabSection(".symtab", 0, 0, strtab);
        Section.ProgBitsSection text = new TextSection();
        Section.ProgBitsSection data = new DataSection();
        Section.RelSection textrel = new Section.RelSection(".rel.text", 0, 0, symtab, text);
        Section.RelSection datarel = new Section.RelSection(".rel.data", 0, 0, symtab, data);
        f.setSectionHeaderStringTable(shstrtab);
        //f.setSymbolStringTable(strtab);
        f.addSection(empty);
        f.addSection(shstrtab);
        f.addSection(strtab);
        f.addSection(symtab);
        f.addSection(text);
        f.addSection(data);
        f.addSection(textrel);
        f.addSection(datarel);

        final List exts = new LinkedList();
        final int numOfVTableRelocs = addVTableRelocs(data_relocs);
        addSystemInterfaceRelocs_ELF(exts, data_relocs);

        symtab.addSymbol(new SymbolTableEntry("", 0, 0, SymbolTableEntry.STB_LOCAL, SymbolTableEntry.STT_NOTYPE, empty));
        
        SymbolTableEntry textsyment = new SymbolTableEntry("", 0, 0, SymbolTableEntry.STB_LOCAL, SymbolTableEntry.STT_SECTION, text);
        SymbolTableEntry datasyment = new SymbolTableEntry("", 0, 0, SymbolTableEntry.STB_LOCAL, SymbolTableEntry.STT_SECTION, data);
        symtab.addSymbol(textsyment);
        symtab.addSymbol(datasyment);

        Iterator it = exts.iterator();
        while (it.hasNext()) {
            ExternalReference r = (ExternalReference)it.next();
            SymbolTableEntry e = new SymbolTableEntry(r.getName(), 0, 0, SymbolTableEntry.STB_GLOBAL, SymbolTableEntry.STT_FUNC, empty);
            symtab.addSymbol(e);
            datarel.addReloc(new RelocEntry(r.getAddress().to32BitValue(), e, RelocEntry.R_386_32));
        }

        it = CodeAllocator.getCompiledMethods();
        while (it.hasNext()) {
            jq_CompiledCode cc = (jq_CompiledCode)it.next();
            jq_Method m = cc.getMethod();
            String name;
            if (m == null) {
                name = "unknown@"+cc.getEntrypoint().stringRep();
            } else {
                name = mungeMemberName(m);
            }
            SymbolTableEntry e = new SymbolTableEntry(name, cc.getEntrypoint().to32BitValue(), cc.getLength(), STB_LOCAL, STT_FUNC, text);
            symtab.addSymbol(e);
        }

        {
            jq_CompiledCode cc = rootm.getDefaultCompiledVersion();
            SymbolTableEntry e = new SymbolTableEntry("entry", cc.getEntrypoint().to32BitValue(), cc.getLength(), STB_GLOBAL, STT_FUNC, text);
            symtab.addSymbol(e);

            cc = ExceptionDeliverer._trap_handler.getDefaultCompiledVersion();
            e = new SymbolTableEntry("trap_handler", cc.getEntrypoint().to32BitValue(), cc.getLength(), STB_GLOBAL, STT_FUNC, text);
            symtab.addSymbol(e);

            cc = ExceptionDeliverer._debug_trap_handler.getDefaultCompiledVersion();
            e = new SymbolTableEntry("debug_trap_handler", cc.getEntrypoint().to32BitValue(), cc.getLength(), STB_GLOBAL, STT_FUNC, text);
            symtab.addSymbol(e);
            
            cc = jq_NativeThread._threadSwitch.getDefaultCompiledVersion();
            e = new SymbolTableEntry("threadSwitch", cc.getEntrypoint().to32BitValue(), cc.getLength(), STB_GLOBAL, STT_FUNC, text);
            symtab.addSymbol(e);

            cc = jq_NativeThread._ctrl_break_handler.getDefaultCompiledVersion();
            e = new SymbolTableEntry("ctrl_break_handler", cc.getEntrypoint().to32BitValue(), cc.getLength(), STB_GLOBAL, STT_FUNC, text);
            symtab.addSymbol(e);

            e = new SymbolTableEntry("joeq_code_startaddress", 0, 0, STB_GLOBAL, STT_OBJECT, text);
            symtab.addSymbol(e);

            e = new SymbolTableEntry("joeq_data_startaddress", 0, 0, STB_GLOBAL, STT_OBJECT, data);
            symtab.addSymbol(e);
        }

        it = text_relocs1.iterator();
        while (it.hasNext()) {
            Reloc r = (Reloc)it.next();
            if (r instanceof Code2CodeReference) {
                Code2CodeReference cr = (Code2CodeReference)r;
                textrel.addReloc(new RelocEntry(cr.getFrom().to32BitValue(), datasyment, RelocEntry.R_386_32));
            } else {
                Assert.UNREACHABLE(r.toString());
            }
        }
        
        it = text_relocs2.iterator();
        while (it.hasNext()) {
            Reloc r = (Reloc)it.next();
            if (r instanceof Code2HeapReference) {
                Code2HeapReference cr = (Code2HeapReference)r;
                textrel.addReloc(new RelocEntry(cr.getFrom().to32BitValue(), datasyment, RelocEntry.R_386_32));
            } else {
                Assert.UNREACHABLE(r.toString());
            }
        }
        
        it = data_relocs.iterator();
        while (it.hasNext()) {
            Reloc r = (Reloc)it.next();
            if (r instanceof Heap2HeapReference) {
                Heap2HeapReference cr = (Heap2HeapReference)r;
                datarel.addReloc(new RelocEntry(cr.getFrom().to32BitValue(), datasyment, RelocEntry.R_386_32));
            } else if (r instanceof Heap2CodeReference) {
                Heap2CodeReference cr = (Heap2CodeReference)r;
                datarel.addReloc(new RelocEntry(cr.getFrom().to32BitValue(), textsyment, RelocEntry.R_386_32));
            } else if (r instanceof ExternalReference) {
                // already done.
            } else {
                Assert.UNREACHABLE(r.toString());
            }
        }
        
        time = System.currentTimeMillis() - time;
        SinglePassBootImage.out.println("done. ("+(time/1000.)+" seconds)");
        
        f.write();
        int pos = bb.position();
        bb.force();
        fc.truncate(pos);
    }

    class TextSection extends Section.ProgBitsSection {
        TextSection() {
            super(".text", Section.SHF_ALLOC | Section.SHF_EXECINSTR | Section.SHF_WRITE, 0);
        }
        public int getSize() { return bca.size(); }
        public int getAddrAlign() { return 64; }
        public void writeData(ELF file) throws IOException {
            DataOutput out = (DataOutput) ((ELFOutput)file).getOutput();
            bca.dump(out);
        }
        public void load(Section.UnloadedSection s, ELF file) throws IOException {
            Assert.UNREACHABLE();
        }
    }

    class DataSection extends Section.ProgBitsSection {
        DataSection() {
            super(".data", Section.SHF_ALLOC | Section.SHF_WRITE, 0);
        }
        public int getSize() { return heapCurrent; }
        public int getAddrAlign() { return 64; }
        public void writeData(ELF file) throws IOException {
            try {
                DataOutput out = (DataOutput) ((ELFOutput)file).getOutput();
                byte[] b;
                if (heapBuffer.hasArray()) b = heapBuffer.array();
                else {
                    b = new byte[heapBuffer.position()];
                    heapBuffer.position(0);
                    heapBuffer.get(b);
                }
                out.write(b);
            } catch (UnknownObjectException x) {
                Object u = x.getObject();
                HashSet visited = new HashSet();
                findReferencePath(u, x, visited);
                throw x;
            }
        }
        public void load(Section.UnloadedSection s, ELF file) throws IOException {
            Assert.UNREACHABLE();
        }
    }
    
    public static final jq_StaticField _DEFAULT;
    static {
        jq_Class k = (jq_Class) PrimordialClassLoader.loader.getOrCreateBSType("Ljoeq/Bootstrap/SinglePassBootImage;");
        _DEFAULT = k.getOrCreateStaticField("DEFAULT", "Ljoeq/Bootstrap/SinglePassBootImage;");
    }

}
