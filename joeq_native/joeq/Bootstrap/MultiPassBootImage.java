// BootImage.java, created Mon Feb  5 23:23:20 2001 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Bootstrap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Array;
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
import jwutil.io.ExtendedDataOutput;
import jwutil.strings.Strings;
import jwutil.util.Assert;

/**
 * BootImage
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: MultiPassBootImage.java,v 1.3 2005/04/29 07:41:11 joewhaley Exp $
 */
public class MultiPassBootImage implements ELFConstants {

    public static /*final*/ boolean TRACE = false;
    public static final PrintStream out = System.out;
    
    public static final MultiPassBootImage DEFAULT = new MultiPassBootImage(BootstrapCodeAllocator.DEFAULT);
    
    private final Map/*<IdentityHashCodeWrapper, Entry>*/ hash;
    private final ArrayList/*<Entry>*/ entries;
    private int heapCurrent;
    private final int startAddress;

    private BootstrapCodeAllocator bca;
    private List data_relocs;
    
    public Set boot_types;
    
    public MultiPassBootImage(BootstrapCodeAllocator bca, int initialCapacity, float loadFactor) {
        hash = new HashMap(initialCapacity, loadFactor);
        entries = new ArrayList(initialCapacity);
        this.bca = bca;
        this.heapCurrent = this.startAddress = 0;
        this.data_relocs = new LinkedList();
    }
    public MultiPassBootImage(BootstrapCodeAllocator bca, int initialCapacity) {
        hash = new HashMap(initialCapacity);
        entries = new ArrayList(initialCapacity);
        this.bca = bca;
        this.heapCurrent = this.startAddress = 0;
        this.data_relocs = new LinkedList();
    }
    public MultiPassBootImage(BootstrapCodeAllocator bca) {
        hash = new HashMap();
        entries = new ArrayList();
        this.bca = bca;
        this.heapCurrent = this.startAddress = 0;
        this.data_relocs = new LinkedList();
    }

    public final HeapAddress addressOf(Object o) {
        Assert._assert(!(o instanceof BootstrapAddress));
        return getOrAllocateObject(o);
    }
    
    public final void addCodeReloc(HeapAddress addr, CodeAddress target) {
        Heap2CodeReference r = new Heap2CodeReference(addr, target);
        data_relocs.add(r);
    }
    public final void addDataReloc(HeapAddress addr, HeapAddress target) {
        Heap2HeapReference r = new Heap2HeapReference(addr, target);
        data_relocs.add(r);
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
    
    public HeapAddress getOrAllocateObject(Object o) {
        if (o == null) return HeapAddress.getNull();
        //jq.Assert(!(o instanceof BootstrapAddress));
        IdentityHashCodeWrapper k = IdentityHashCodeWrapper.create(o);
        Entry e = (Entry)hash.get(k);
        if (e != null) return e.getAddress();
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
            int addr;
            int size;
            if (type.isArrayType()) {
                addr = heapCurrent + ObjectLayout.ARRAY_HEADER_SIZE;
                size = ((jq_Array)type).getInstanceSize(Array.getLength(o));
                size = (size+3) & ~3;
                if (TRACE)
                    out.println("Allocating entry "+entries.size()+": "+objType+" length "+Array.getLength(o)+" size "+size+" "+Strings.hex(System.identityHashCode(o))+" at "+Strings.hex(addr));
            } else {
                Assert._assert(type.isClassType());
                addr = heapCurrent + ObjectLayout.OBJ_HEADER_SIZE;
                size = ((jq_Class)type).getInstanceSize();
                if (TRACE)
                    out.println("Allocating entry "+entries.size()+": "+objType+" size "+size+" "+Strings.hex(System.identityHashCode(o))+" at "+Strings.hex(addr)+((o instanceof jq_Type)?": "+o:""));
            }
            heapCurrent += size;
            BootstrapHeapAddress a = new BootstrapHeapAddress(addr);
            e = Entry.create(o, a);
            hash.put(k, e);
            entries.add(e);
            return a;
        } catch (Exception ie) {
            ie.printStackTrace();
            HashSet visited = new HashSet();
            UnknownObjectException x = new UnknownObjectException(o);
            boolean found = findReferencePath(o, x, visited);
            if (found) {
                System.out.println(x);
            }
            return HeapAddress.getNull();
        }
    }
    
    public static boolean IGNORE_UNKNOWN_OBJECTS = false;
    
    public HeapAddress getAddressOf(Object o) {
        Assert._assert(!(o instanceof BootstrapAddress));
        if (o == null) return HeapAddress.getNull();
        IdentityHashCodeWrapper k = IdentityHashCodeWrapper.create(o);
        Entry e = (Entry)hash.get(k);
        if (e == null) {
            System.err.println("Unknown object of type: "+o.getClass()+" address: "+Strings.hex(System.identityHashCode(o))+" value: "+o);
            if (IGNORE_UNKNOWN_OBJECTS) return HeapAddress.getNull();
            throw new UnknownObjectException(o);
        }
        Class objType = o.getClass();
        jq_Reference type = (jq_Reference)Reflection.getJQType(objType);
        Assert._assert(type.isClsInitialized(), type.toString());
        return e.getAddress();
    }

    public Object getObject(int i) {
        Entry e = (Entry)entries.get(i);
        return e.getObject();
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
    
    public int numOfEntries() { return entries.size(); }

    public static int UPDATE_PERIOD = 10000;

    public void find_reachable(int i) {
        for (; i<entries.size(); ++i) {
            if ((i % UPDATE_PERIOD) == 0) {
                out.print("Scanning: "+i+"/"+entries.size()+" objects, memory used: "+(Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())+"   \r");
            }
            Entry e = (Entry)entries.get(i);
            Object o = e.getObject();
            if (o == null) continue;
            HeapAddress addr = e.getAddress();
            Assert._assert(!addr.isNull());
            Class objType = o.getClass();
            jq_Reference jqType = (jq_Reference)Reflection.getJQType(objType);
            if (TRACE)
                out.println("Entry "+i+": "+objType+" "+Strings.hex(System.identityHashCode(o)));
            addDataReloc((HeapAddress)addr.offset(ObjectLayout.VTABLE_OFFSET), getOrAllocateObject(jqType));
            if (jqType.isArrayType()) {
                jq_Type elemType = ((jq_Array)jqType).getElementType();
                if (elemType.isAddressType()) {
                    // probably a vtable.  skip it -- handled separately.
                } else if (elemType.isReferenceType()) {
                    int length = Array.getLength(o);
                    Object[] v = (Object[])o;
                    for (int k=0; k<length; ++k) {
                        Object o2 = Reflection.arrayload_A(v, k);
                        if (o2 != null) {
                            addDataReloc((HeapAddress)addr.offset(k*HeapAddress.size()), getOrAllocateObject(o2));
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
                    if (f.isCodeAddressType()) {
                        CodeAddress val = (CodeAddress)Reflection.getfield_P(o, f);
                        if (val != null && !val.isNull())
                            addCodeReloc((HeapAddress)addr.offset(f.getOffset()), val);
                    } else if (f.isHeapAddressType()) {
                        HeapAddress val = (HeapAddress)Reflection.getfield_P(o, f);
                        if (val != null && !val.isNull())
                            addDataReloc((HeapAddress)addr.offset(f.getOffset()), val);
                    } else if (f.isStackAddressType()) {
                        // no reloc necessary.
                    } else if (f.getType().isAddressType()) {
                        Assert.UNREACHABLE("Field has untyped Address type: "+f);
                    } else if (ftype.isReferenceType()) {
                        Object val = Reflection.getfield_A(o, f);
                        if (val != null) {
                            addDataReloc((HeapAddress)addr.offset(f.getOffset()), getOrAllocateObject(val));
                        }
                    }
                }
            }
        }
    }

    public int size() { return heapCurrent-startAddress; }
    
    private static class Entry {
        private Object o;            // object in host vm
        private HeapAddress address; // address in target vm
        private Entry(Object o, HeapAddress address) { this.o = o; this.address = address; }
        static Entry create(Object o, HeapAddress address) {
            Assert._assert(o != null);
            return new Entry(o, address);
        }
        Object getObject() { return o; }
        HeapAddress getAddress() { return address; }
    }
    
    public static final char F_RELFLG = (char)0x0001;
    public static final char F_EXEC   = (char)0x0002;
    public static final char F_LNNO   = (char)0x0004;
    public static final char F_LSYMS  = (char)0x0008;
    public static final char F_AR32WR = (char)0x0100;
    
    public void dumpFILHDR(ExtendedDataOutput out, int symptr, int nsyms)
    throws IOException {
        // FILHDR
        out.writeUShort((char)0x014c);  // f_magic
        out.writeUShort((char)2);       // f_nscns
        long ms = System.currentTimeMillis();
        int s = (int)(ms/1000);
        out.writeUInt(s);               // f_timdat
        out.writeUInt(symptr);          // f_symptr
        out.writeUInt(nsyms);           // f_nsyms
        out.writeUShort((char)0);       // f_opthdr
        out.writeUShort((char)(F_LNNO | F_LSYMS | F_AR32WR)); // f_flags
    }
    
    public static final int STYP_TEXT  = 0x00000020;
    public static final int STYP_DATA  = 0x00000040;
    public static final int STYP_BSS   = 0x00000080;
    public static final int STYP_RELOV = 0x01000000;
    public static final int STYP_EXEC  = 0x20000000;
    public static final int STYP_READ  = 0x40000000;
    public static final int STYP_WRITE = 0x80000000;
    
    public void dumpTEXTSCNHDR(ExtendedDataOutput out, int size, int nreloc)
    throws IOException {
        // SCNHDR
        write_bytes(out, ".text", 8);       // s_name
        out.writeUInt(0);                // s_paddr
        out.writeUInt(0);                // s_vaddr
        out.writeUInt(size);             // s_size
        out.writeUInt(20+40+40);         // s_scnptr
        out.writeUInt(20+40+40+size);    // s_relptr
        out.writeUInt(0);                // s_lnnoptr
        if (nreloc > 65535)
            out.writeUShort((char)0xffff); // s_nreloc
        else
            out.writeUShort((char)nreloc); // s_nreloc
        out.writeUShort((char)0);         // s_nlnno
        if (nreloc > 65535)
            out.writeUInt(STYP_TEXT | STYP_READ | STYP_WRITE | STYP_RELOV); // s_flags
        else
            out.writeUInt(STYP_TEXT | STYP_READ | STYP_WRITE); // s_flags
    }
    
    public void dumpDATASCNHDR(ExtendedDataOutput out, int scnptr, int size, int nreloc)
    throws IOException {
        // SCNHDR
        write_bytes(out, ".data", 8);       // s_name
        out.writeUInt(0);                // s_paddr
        out.writeUInt(0);                // s_vaddr
        out.writeUInt(size);             // s_size
        out.writeUInt(scnptr);           // s_scnptr
        out.writeUInt(scnptr+size);      // s_relptr
        out.writeUInt(0);                // s_lnnoptr
        if (nreloc > 65535)
            out.writeUShort((char)0xffff); // s_nreloc
        else
            out.writeUShort((char)nreloc); // s_nreloc
        out.writeUShort((char)0);         // s_nlnno
        if (nreloc > 65535)
            out.writeUInt(STYP_DATA | STYP_READ | STYP_WRITE | STYP_RELOV); // s_flags
        else
            out.writeUInt(STYP_DATA | STYP_READ | STYP_WRITE); // s_flags
    }
    
    public static final char RELOC_ADDR32 = (char)0x0006;
    public static final char RELOC_REL32  = (char)0x0014;
    
    public void dumpLINENO(ExtendedDataOutput out, int addr, char lnno)
    throws IOException {
        out.writeUInt(addr);     // l_symndx / l_paddr
        out.writeUShort(lnno);    // l_lnno
    }
    
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
    
    public void dumpSECTIONSYMENTs(ExtendedDataOutput out)
    throws IOException {
        write_bytes(out, ".text", 8);
        out.writeUInt(0);
        out.writeShort((short)1);
        out.writeUShort((char)0);
        out.writeUByte(C_STAT);
        out.writeUByte(0);
        
        write_bytes(out, ".data", 8);
        out.writeUInt(0);
        out.writeShort((short)2);
        out.writeUShort((char)0);
        out.writeUByte(C_STAT);
        out.writeUByte(0);
    }
    
    public static boolean USE_MICROSOFT_STYLE_MUNGE = true;
    
    public static final int NUM_OF_EXTERNAL_SYMS = 9;
    public void dumpEXTSYMENTs(ExtendedDataOutput out, jq_StaticMethod rootm)
    throws IOException {
        // NOTE!!! If you change anything here, be SURE to change the number above!!!
        String s;
        if (USE_MICROSOFT_STYLE_MUNGE) s = "_entry@0";
        else s = "entry";
        write_bytes(out, s, 8);  // s_name
        CodeAddress addr = rootm.getDefaultCompiledVersion().getEntrypoint();
        out.writeUInt(addr.to32BitValue());
        out.writeShort((short)1);
        out.writeUShort((char)DT_FCN);
        out.writeUByte(C_EXT); // e_sclass
        out.writeUByte(0); // e_numaux
        
        out.writeUInt(0);    // e_zeroes
        if (USE_MICROSOFT_STYLE_MUNGE) s = "_trap_handler@4";
        else s = "trap_handler";
        int idx = alloc_string(s);
        out.writeUInt(idx);  // e_offset
        addr = ExceptionDeliverer._trap_handler.getDefaultCompiledVersion().getEntrypoint();
        out.writeUInt(addr.to32BitValue());
        out.writeShort((short)1);
        out.writeUShort((char)DT_FCN);
        out.writeUByte(C_EXT); // e_sclass
        out.writeUByte(0); // e_numaux

        out.writeUInt(0);    // e_zeroes
        if (USE_MICROSOFT_STYLE_MUNGE) s = "_debug_trap_handler@4";
        else s = "debug_trap_handler";
        idx = alloc_string(s);
        out.writeUInt(idx);  // e_offset
        addr = ExceptionDeliverer._debug_trap_handler.getDefaultCompiledVersion().getEntrypoint();
        out.writeUInt(addr.to32BitValue());
        out.writeShort((short)1);
        out.writeUShort((char)DT_FCN);
        out.writeUByte(C_EXT); // e_sclass
        out.writeUByte(0); // e_numaux
        
        out.writeUInt(0);    // e_zeroes
        if (USE_MICROSOFT_STYLE_MUNGE) s = "_threadSwitch@4";
        else s = "threadSwitch";
        idx = alloc_string(s);
        out.writeUInt(idx);  // e_offset
        addr = jq_NativeThread._threadSwitch.getDefaultCompiledVersion().getEntrypoint();
        out.writeUInt(addr.to32BitValue());
        out.writeShort((short)1);
        out.writeUShort((char)DT_FCN);
        out.writeUByte(C_EXT); // e_sclass
        out.writeUByte(0); // e_numaux
        
        out.writeUInt(0);    // e_zeroes
        if (USE_MICROSOFT_STYLE_MUNGE) s = "_ctrl_break_handler@0";
        else s = "ctrl_break_handler";
        idx = alloc_string(s);
        out.writeUInt(idx);  // e_offset
        addr = jq_NativeThread._ctrl_break_handler.getDefaultCompiledVersion().getEntrypoint();
        out.writeUInt(addr.to32BitValue()); // e_value
        out.writeShort((short)1);
        out.writeUShort((char)DT_FCN);
        out.writeUByte(C_EXT); // e_sclass
        out.writeUByte(0); // e_numaux

        out.writeUInt(0);    // e_zeroes
        if (USE_MICROSOFT_STYLE_MUNGE) s = "_joeq_code_startaddress";
        else s = "joeq_code_startaddress";
        idx = alloc_string(s);
        out.writeUInt(idx);  // e_offset
        out.writeUInt(0); // e_value
        out.writeShort((short)1);
        out.writeUShort((char)(DT_PTR | T_VOID));
        out.writeUByte(C_EXT); // e_sclass
        out.writeUByte(0); // e_numaux

        out.writeUInt(0);    // e_zeroes
        if (USE_MICROSOFT_STYLE_MUNGE) s = "_joeq_code_endaddress";
        else s = "joeq_code_endaddress";
        idx = alloc_string(s);
        out.writeUInt(idx);  // e_offset
        out.writeUInt(bca.size()); // e_value
        out.writeShort((short)1);
        out.writeUShort((char)(DT_PTR | T_VOID));
        out.writeUByte(C_EXT); // e_sclass
        out.writeUByte(0); // e_numaux

        out.writeUInt(0);    // e_zeroes
        if (USE_MICROSOFT_STYLE_MUNGE) s = "_joeq_data_startaddress";
        else s = "joeq_data_startaddress";
        idx = alloc_string(s);
        out.writeUInt(idx); // e_offset
        out.writeUInt(0); // e_value
        out.writeShort((short)2); // e_scnum
        out.writeUShort((char)(DT_PTR | T_VOID)); // e_type
        out.writeUByte(C_EXT); // e_sclass
        out.writeUByte(0); // e_numaux
        
        out.writeUInt(0);    // e_zeroes
        if (USE_MICROSOFT_STYLE_MUNGE) s = "_joeq_data_endaddress";
        else s = "joeq_data_endaddress";
        idx = alloc_string(s);
        out.writeUInt(idx); // e_offset
        out.writeUInt(heapCurrent); // e_value
        out.writeShort((short)2); // e_scnum
        out.writeUShort((char)(DT_PTR | T_VOID)); // e_type
        out.writeUByte(C_EXT); // e_sclass
        out.writeUByte(0); // e_numaux
    }
    
    public void dumpEXTDEFSYMENTs(ExtendedDataOutput out, List extrefs)
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
                out.writeUInt(0);    // e_zeroes
                int idx = alloc_string(name);
                out.writeUInt(idx);  // e_offset
            }
            out.writeUInt(0);
            out.writeShort((short)0);
            out.writeUShort((char)DT_FCN);
            out.writeUByte(C_EXT);
            out.writeUByte(0);
            ++k;
        }
    }
    
    public void dumpSFIELDSYMENT(ExtendedDataOutput out, jq_StaticField sf)
    throws IOException {
        //String name = sf.getName().toString();
        String name = mungeMemberName(sf);
        if (name.length() <= 8) {
            write_bytes(out, name, 8);  // s_name
        } else {
            out.writeUInt(0);    // e_zeroes
            int idx = alloc_string(name);
            out.writeUInt(idx);  // e_offset
        }
        HeapAddress addr = sf.getAddress();
        out.writeUInt(addr.to32BitValue()); // e_value
        out.writeShort((short)2); // e_scnum
        jq_Type t = sf.getType();
        char type = (char)0;
        if (t.isArrayType()) {
            t = ((jq_Array)t).getElementType();
            type = DT_ARY;
        } else if (t.isReferenceType()) {
            type = DT_PTR;
        }
        if (t.isPrimitiveType()) {
            if (t == jq_Primitive.INT) type |= T_LONG;
            else if (t == jq_Primitive.LONG) type |= T_LNGDBL;
            else if (t == jq_Primitive.FLOAT) type |= T_FLOAT;
            else if (t == jq_Primitive.DOUBLE) type |= T_DOUBLE;
            else if (t == jq_Primitive.BYTE) type |= T_CHAR;
            else if (t == jq_Primitive.BOOLEAN) type |= T_UCHAR;
            else if (t == jq_Primitive.SHORT) type |= T_SHORT;
            else if (t == jq_Primitive.CHAR) type |= T_USHORT;
            else Assert.UNREACHABLE();
        } else {
            type |= T_STRUCT;
        }
        out.writeUShort(type);  // e_type
        out.writeUByte(C_STAT); // e_sclass
        out.writeUByte(0);      // e_numaux
    }

    public void dumpIFIELDSYMENT(ExtendedDataOutput out, jq_InstanceField f)
    throws IOException {
        String name = f.getName().toString();
        if (name.length() <= 8) {
            write_bytes(out, name, 8);  // s_name
        } else {
            out.writeUInt(0);    // e_zeroes
            int idx = alloc_string(name);
            out.writeUInt(idx);  // e_offset
        }
        int off = f.getOffset();
        out.writeUInt(off);      // e_value
        out.writeShort((short)2); // e_scnum
        jq_Type t = f.getType();
        char type = (char)0;
        if (t.isArrayType()) {
            t = ((jq_Array)t).getElementType();
            type = DT_ARY;
        } else if (t.isReferenceType()) {
            type = DT_PTR;
        }
        if (t.isPrimitiveType()) {
            if (t == jq_Primitive.INT) type |= T_LONG;
            else if (t == jq_Primitive.LONG) type |= T_LNGDBL;
            else if (t == jq_Primitive.FLOAT) type |= T_FLOAT;
            else if (t == jq_Primitive.DOUBLE) type |= T_DOUBLE;
            else if (t == jq_Primitive.BYTE) type |= T_CHAR;
            else if (t == jq_Primitive.BOOLEAN) type |= T_UCHAR;
            else if (t == jq_Primitive.SHORT) type |= T_SHORT;
            else if (t == jq_Primitive.CHAR) type |= T_USHORT;
            else Assert.UNREACHABLE();
        } else {
            type |= T_STRUCT;
        }
        out.writeUShort(type); // e_type
        out.writeUByte(C_MOS); // e_sclass
        out.writeUByte(0);     // e_numaux
    }
    
    public void dumpMETHODSYMENT(ExtendedDataOutput out, jq_CompiledCode cc)
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
            out.writeUInt(0);          // e_zeroes
            int idx = alloc_string(name);
            out.writeUInt(idx);        // e_offset
        }
        CodeAddress addr = cc.getEntrypoint();
        out.writeUInt(addr.to32BitValue()); // e_value
        out.writeShort((short)1);      // e_scnum
        out.writeUShort((char)DT_FCN); // e_type
        out.writeUByte(C_EXT);         // e_sclass
        out.writeUByte(0);             // e_numaux
    }
    
    public void addSystemInterfaceRelocs_COFF(List extref, List dataRelocs) {
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
                if (TRACE) System.out.println("External ref="+f+", symndx="+(total+1)+" address="+f.getAddress().stringRep());
                ExternalReference r = new ExternalReference(f.getAddress(), name);
                r.setSymbolIndex(++total);
                extref.add(r);
                dataRelocs.add(r);
            } else if (f.getType() == HeapAddress._class) {
                String name = f.getName().toString();
                if (USE_MICROSOFT_STYLE_MUNGE)
                    name = "_"+name;
                if (TRACE) System.out.println("External ref="+f+", symndx="+(total+1)+" address="+f.getAddress().stringRep());
                ExternalReference r = new ExternalReference(f.getAddress(), name);
                r.setSymbolIndex(++total);
                extref.add(r);
                dataRelocs.add(r);
            }
        }
        //return total-3;
    }

    public void addSystemInterfaceRelocs_ELF(List extref, List dataRelocs) {
        jq_StaticField[] fs = SystemInterface._class.getDeclaredStaticFields();
        int total = 1+NUM_OF_EXTERNAL_SYMS;
        for (int i=0; i<fs.length; ++i) {
            jq_StaticField f = fs[i];
            if (f.isFinal()) continue;
            if (f.getType() == CodeAddress._class) {
                String name = f.getName().toString();
                int ind = name.lastIndexOf('_');
                name = name.substring(0, ind);
                if (TRACE) System.out.println("External ref="+f+", symndx="+(total+1)+" address="+f.getAddress().stringRep());
                ExternalReference r = new ExternalReference(f.getAddress(), name);
                r.setSymbolIndex(++total);
                extref.add(r);
                dataRelocs.add(r);
            } else if (f.getType() == HeapAddress._class) {
                String name = f.getName().toString();
                if (TRACE) System.out.println("External ref="+f+", symndx="+(total+1)+" address="+f.getAddress().stringRep());
                ExternalReference r = new ExternalReference(f.getAddress(), name);
                r.setSymbolIndex(++total);
                extref.add(r);
                dataRelocs.add(r);
            }
        }
    }
    
    public int addVTableRelocs(List list) {
        int total = 0;
        Iterator i = boot_types.iterator();
        while (i.hasNext()) {
            jq_Type t = (jq_Type)i.next();
            if (t.isReferenceType()) {
                if (t == Unsafe._class) continue;
                try {
                    if (TRACE) System.out.println("Adding vtable relocs for: "+t);
                    Address[] vtable = (Address[])((jq_Reference)t).getVTable();
                    HeapAddress addr = getAddressOf(vtable);
                    //jq.Assert(vtable[0] != 0, t.toString());
                    Heap2HeapReference r1 = new Heap2HeapReference(addr, (HeapAddress) vtable[0]);
                    list.add(r1);
                    for (int j=1; j<vtable.length; ++j) {
                        Heap2CodeReference r2 = new Heap2CodeReference((HeapAddress) addr.offset(CodeAddress.size()*j), (CodeAddress) vtable[j]);
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
    
    public void dumpCOFF(ExtendedDataOutput out, jq_StaticMethod rootm) throws IOException {
        
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
        final int textsize = bca.size();
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
        
        if (TRACE) {
            System.out.println("Text size="+textsize);
            System.out.println("Num text relocs="+ntextreloc);
            System.out.println("Data start="+datastart);
            System.out.println("Data size="+datasize);
            System.out.println("Num of VTable relocs="+numOfVTableRelocs);
            System.out.println("Num data relocs="+ntextreloc);
            System.out.println("Sym tab start="+symtabstart);
            System.out.println("Num syms="+nsyms);
        }
        
        // write file header
        dumpFILHDR(out, symtabstart, nsyms);
        
        // write section headers
        dumpTEXTSCNHDR(out, textsize, ntextreloc);
        dumpDATASCNHDR(out, datastart, datasize, ndatareloc);
        
        // write text section
        bca.dump(out);
        
        // write text relocs
        if (ntextreloc > 65535) {
            out.writeUInt(ntextreloc);
            out.writeUInt(0);
            out.writeUShort((char)0);
        }
        Iterator it = text_relocs1.iterator();
        while (it.hasNext()) {
            Reloc r = (Reloc)it.next();
            r.dumpCOFF(out);
        }
        it = text_relocs2.iterator();
        while (it.hasNext()) {
            Reloc r = (Reloc)it.next();
            r.dumpCOFF(out);
        }
        //out.flush();
        
        // write data section
        try {
            dumpHeap(out);
        } catch (UnknownObjectException x) {
            Object u = x.getObject();
            HashSet visited = new HashSet();
            findReferencePath(u, x, visited);
            throw x;
        }
        
        // write data relocs
        int j=0;
        if (ndatareloc > 65535) {
            out.writeUInt(ndatareloc);
            out.writeUInt(0);
            out.writeUShort((char)0);
            ++j;
        }
        it = data_relocs.iterator();
        while (it.hasNext()) {
            if ((j % UPDATE_PERIOD) == 0) {
                MultiPassBootImage.out.print("Written: "+j+"/"+ndatareloc+" relocations\r");
            }
            Reloc r = (Reloc)it.next();
            r.dumpCOFF(out);
            ++j;
        }
        MultiPassBootImage.out.println("Written: "+ndatareloc+" relocations                    \n");
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
        
        // write string table
        dump_strings(out);
        
        //out.flush();
    }

    static class UnknownObjectException extends RuntimeException {
        /**
         * Version ID for serialization.
         */
        private static final long serialVersionUID = 3257002155398345015L;
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
        Iterator i = entries.iterator();
        while (i.hasNext()) {
            Entry e = (Entry)i.next();
            Object o = e.getObject();
            IdentityHashCodeWrapper w = IdentityHashCodeWrapper.create(o);
            if (visited.contains(w)) continue;
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
    
    private void dumpHeap(ExtendedDataOutput out)
    throws IOException {
        Assert._assert(ObjectLayout.ARRAY_LENGTH_OFFSET == -12);
        Assert._assert(ObjectLayout.STATUS_WORD_OFFSET == -8);
        Assert._assert(ObjectLayout.VTABLE_OFFSET == -4);
        Assert._assert(ObjectLayout.OBJ_HEADER_SIZE == 8);
        Assert._assert(ObjectLayout.ARRAY_HEADER_SIZE == 12);
        Iterator i = entries.iterator();
        int currentAddr=0;
        int j=0;
        while (i.hasNext()) {
        if ((j % UPDATE_PERIOD) == 0) {
        System.out.print("Written: "+j+"/"+entries.size()+" objects, "+currentAddr+"/"+heapCurrent+" bytes\r");
        }
            Entry e = (Entry)i.next();
            Object o = e.getObject();
            HeapAddress addr = e.getAddress();
            Class objType = o.getClass();
            jq_Reference jqType = (jq_Reference)Reflection.getJQType(objType);
            if (TRACE)
                MultiPassBootImage.out.println("Dumping entry "+j+": "+objType+" "+Strings.hex(System.identityHashCode(o))+" addr "+addr.stringRep());
            Assert._assert(!jqType.isAddressType());
            if (!jqType.isClsInitialized()) {
                Assert.UNREACHABLE(jqType.toString());
                return;
            }
            HeapAddress vtable;
            try { vtable = getAddressOf(jqType.getVTable()); }
            catch (UnknownObjectException x) {
                x.appendMessage("vtable for "+jqType);  
                x.setObject(null);
                throw x;
            }
            if (jqType.isArrayType()) {
                while (currentAddr+ObjectLayout.ARRAY_HEADER_SIZE < addr.to32BitValue()) {
                    out.writeByte((byte)0); ++currentAddr;
                }
                int length = Array.getLength(o);
                out.writeUInt(length);
                out.writeUInt(0);
                out.writeUInt(vtable.to32BitValue());
                currentAddr += ObjectLayout.ARRAY_HEADER_SIZE;
                Assert._assert(addr.to32BitValue() == currentAddr);
                jq_Type elemType = ((jq_Array)jqType).getElementType();
                if (elemType.isPrimitiveType()) {
                    if (elemType == jq_Primitive.INT) {
                        int[] v = (int[])o;
                        for (int k=0; k<length; ++k)
                            out.writeUInt(v[k]);
                        currentAddr += length << 2;
                    } else if (elemType == jq_Primitive.FLOAT) {
                        float[] v = (float[])o;
                        for (int k=0; k<length; ++k)
                            out.writeUInt(Float.floatToRawIntBits(v[k]));
                        currentAddr += length << 2;
                    } else if (elemType == jq_Primitive.LONG) {
                        long[] v = (long[])o;
                        for (int k=0; k<length; ++k)
                            out.writeULong(v[k]);
                        currentAddr += length << 3;
                    } else if (elemType == jq_Primitive.DOUBLE) {
                        double[] v = (double[])o;
                        for (int k=0; k<length; ++k)
                            out.writeULong(Double.doubleToRawLongBits(v[k]));
                        currentAddr += length << 3;
                    } else if (elemType == jq_Primitive.BOOLEAN) {
                        boolean[] v = (boolean[])o;
                        for (int k=0; k<length; ++k)
                            out.writeUByte(v[k]?1:0);
                        currentAddr += length;
                    } else if (elemType == jq_Primitive.BYTE) {
                        byte[] v = (byte[])o;
                        for (int k=0; k<length; ++k)
                            out.writeByte(v[k]);
                        currentAddr += length;
                    } else if (elemType == jq_Primitive.SHORT) {
                        short[] v = (short[])o;
                        for (int k=0; k<length; ++k)
                            out.writeShort(v[k]);
                        currentAddr += length << 1;
                    } else if (elemType == jq_Primitive.CHAR) {
                        char[] v = (char[])o;
                        for (int k=0; k<length; ++k)
                            out.writeUShort(v[k]);
                        currentAddr += length << 1;
                    } else Assert.UNREACHABLE();
                } else if (elemType.isAddressType()) {
                    Address[] v = (Address[])o;
                    for (int k=0; k<length; ++k) {
                        out.writeUInt(v[k]==null?0:v[k].to32BitValue());
                    }
                    currentAddr += length << 2;
                } else {
                    Object[] v = (Object[])o;
                    for (int k=0; k<length; ++k) {
                        Object o2 = Reflection.arrayload_A(v, k);
                        try { out.writeUInt(getAddressOf(o2).to32BitValue()); }
                        catch (UnknownObjectException x) {
                            System.err.println("Object array element #"+k);
                            //x.appendMessage("Object array element #"+k+" in ");
                            //x.setObject(v);
                            throw x;
                        }
                    }
                    currentAddr += length << 2;
                }
            } else {
                Assert._assert(jqType.isClassType());
                jq_Class clazz = (jq_Class)jqType;
                while (currentAddr+ObjectLayout.OBJ_HEADER_SIZE < addr.to32BitValue()) {
                    out.writeByte((byte)0); ++currentAddr;
                }
                out.writeUInt(0);
                out.writeUInt(vtable.to32BitValue());
                currentAddr += 8;
                Assert._assert(addr.to32BitValue() == currentAddr);
                jq_InstanceField[] fields = clazz.getInstanceFields();
                for (int k=0; k<fields.length; ++k) {
                    jq_InstanceField f = fields[k];
                    jq_Type ftype = f.getType();
                    int foffset = f.getOffset();
                    if (TRACE) MultiPassBootImage.out.println("Field "+f+" offset "+Strings.shex(foffset)+": "+System.identityHashCode(Reflection.getfield(o, f)));
                    while (currentAddr != addr.offset(foffset).to32BitValue()) {
                        out.writeByte((byte)0); ++currentAddr;
                    }
                    if (ftype.isPrimitiveType()) {
                        if (ftype == jq_Primitive.INT)
                            out.writeUInt(Reflection.getfield_I(o, f));
                        else if (ftype == jq_Primitive.FLOAT)
                            out.writeUInt(Float.floatToRawIntBits(Reflection.getfield_F(o, f)));
                        else if (ftype == jq_Primitive.LONG)
                            out.writeULong(Reflection.getfield_L(o, f));
                        else if (ftype == jq_Primitive.DOUBLE)
                            out.writeULong(Double.doubleToRawLongBits(Reflection.getfield_D(o, f)));
                        else if (ftype == jq_Primitive.BOOLEAN)
                            out.writeUByte(Reflection.getfield_Z(o, f)?1:0);
                        else if (ftype == jq_Primitive.BYTE)
                            out.writeByte(Reflection.getfield_B(o, f));
                        else if (ftype == jq_Primitive.SHORT)
                            out.writeShort(Reflection.getfield_S(o, f));
                        else if (ftype == jq_Primitive.CHAR)
                            out.writeUShort(Reflection.getfield_C(o, f));
                        else Assert.UNREACHABLE();
                    } else if (ftype.isAddressType()) {
                        Address a = Reflection.getfield_P(o, f);
                        out.writeUInt(a==null?0:a.to32BitValue());
                    } else {
                        try { out.writeUInt(getAddressOf(Reflection.getfield_A(o, f)).to32BitValue()); }
                        catch (UnknownObjectException x) {
                            System.err.println("Instance field "+f);
                            //x.appendMessage("field "+f.getName()+" in ");
                            //x.setObject(o);
                            throw x;
                        }
                    }
                    currentAddr += f.getSize();
                }
            }
            ++j;
        }
        while (currentAddr < heapCurrent) {
            out.writeByte((byte)0); ++currentAddr;
        }
        System.out.println("Written: "+j+" objects, "+heapCurrent+" bytes                    ");
    }
    
    public static void write_bytes(ExtendedDataOutput out, String s, int len)
    throws IOException {
        Assert._assert(s.length() <= len);
        int i;
        for (i=0; ; ++i) {
            if (i == s.length()) {
                for (; i<len; ++i) {
                    out.write((byte)0);
                }
                return;
            }
            out.write((byte)s.charAt(i));
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

    private void dump_strings(ExtendedDataOutput out)
    throws IOException {
        Iterator i = stringTable.iterator();
        out.writeUInt(stringTableOffset);
        while (i.hasNext()) {
            byte[] b = (byte[])i.next();
            out.write(b);
        }
    }

    
    public void dumpELF(ExtendedDataOutput out, jq_StaticMethod rootm) throws IOException {
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

        System.out.print("Initializing ELF data structures...");
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
        System.out.println("done. ("+(time/1000.)+" seconds)");
        
        f.write();
        
        //out.flush();
    }

    class TextSection extends Section.ProgBitsSection {
        TextSection() {
            super(".text", Section.SHF_ALLOC | Section.SHF_EXECINSTR | Section.SHF_WRITE, 0);
        }
        public int getSize() { return bca.size(); }
        public int getAddrAlign() { return 64; }
        public void writeData(ELF file) throws IOException {
            ExtendedDataOutput out = (ExtendedDataOutput) ((ELFOutput)file).getOutput();
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
                ExtendedDataOutput out = (ExtendedDataOutput) ((ELFOutput)file).getOutput();
                dumpHeap(out);
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
        jq_Class k = (jq_Class) PrimordialClassLoader.loader.getOrCreateBSType("Ljoeq/Bootstrap/MultiPassBootImage;");
        _DEFAULT = k.getOrCreateStaticField("DEFAULT", "Ljoeq/Bootstrap/MultiPassBootImage;");
    }

}
