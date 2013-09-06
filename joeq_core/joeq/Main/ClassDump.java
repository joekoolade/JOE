// ClassDump.java, created Mon Feb  5 23:23:21 2001 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Main;

import java.util.Arrays;
import java.util.Iterator;
import java.io.PrintStream;
import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Class;
import joeq.Class.jq_InstanceField;
import joeq.Class.jq_InstanceMethod;
import joeq.Class.jq_StaticField;
import joeq.Class.jq_StaticMethod;
import joeq.Class.jq_Type;
import joeq.UTF.Utf8;

/**
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: ClassDump.java,v 1.22 2004/09/22 22:17:32 joewhaley Exp $
 */
public abstract class ClassDump {
    
    public static void main(String[] args) {
        HostedVM.initialize();
        
        String classname;
        if (args.length > 0) classname = args[0];
        else classname = "Ljoeq/Main/jq;";
        
        jq_Class c = (jq_Class)PrimordialClassLoader.loader.getOrCreateBSType(classname);
        System.out.println("Loading "+c+"...");
        c.load();
        System.out.println("Verifying "+c+"...");
        c.verify();
        System.out.println("Preparing "+c+"...");
        c.prepare();
        System.out.println("Initializing static fields of "+c+"...");
        c.sf_initialize();
        //System.out.println("Compiling "+c+"...");
        //c.compile();
        dumpClass(System.out, c);
        //jq_Class c2 = (jq_Class)PrimordialClassLoader.loader.getOrCreateType("Ljava/lang/Exception;");
        //System.out.println(Runtime.TypeCheck.isAssignable(c, c2));
        //System.out.println(Runtime.TypeCheck.isAssignable(c2, c));
        //Allocator.DefaultCodeAllocator.default_allocator = new BootstrapCodeAllocator();
        //Allocator.DefaultCodeAllocator.default_allocator.init();
        compileClass(System.out, c);
    }

    public static void compileClass(PrintStream out, jq_Class t) {
        Iterator it;
        for(it = Arrays.asList(t.getDeclaredStaticMethods()).iterator();
            it.hasNext(); ) {
            jq_StaticMethod c = (jq_StaticMethod)it.next();
            if (c.getBytecode() == null) continue;
            //if (c.getName().toString().equals("right"))
            {
                out.println(c.toString());
                joeq.Compiler.Quad.ControlFlowGraph cfg = joeq.Compiler.Quad.CodeCache.getCode(c);
                System.out.println(cfg.fullDump());
            }
        }
        for(it = Arrays.asList(t.getDeclaredInstanceMethods()).iterator();
            it.hasNext(); ) {
            jq_InstanceMethod c = (jq_InstanceMethod)it.next();
            if (c.isAbstract()) continue;
            if (c.getBytecode() == null) continue;
            //if (c.getName().toString().equals("right"))
            {
                out.println(c.toString());
                joeq.Compiler.Quad.ControlFlowGraph cfg = joeq.Compiler.Quad.CodeCache.getCode(c);
                System.out.println(cfg.fullDump());
            }
        }
    }
    
    public static void dumpType(PrintStream out, jq_Type t) {
        if (t.isClassType()) out.print("class ");
        if (t.isArrayType()) out.print("array ");
        if (t.isPrimitiveType()) out.print("primitive ");
        out.print(t.getName());
    }

    public static void dumpClass(PrintStream out, jq_Class t) {
        dumpType(out, t);
        out.println();
        out.println("state: "+t.getState());
        
        if (t.isLoaded()) {
            out.println("java class file version "+(int)t.getMajorVersion()+"."+(int)t.getMinorVersion());
            out.println("source file name: "+t.getSourceFile());
            out.print("access flags: ");
            if (t.isPublic()) out.print("public ");
            if (t.isFinal()) out.print("final ");
            if (t.isSpecial()) out.print("special ");
            if (t.isInterface()) out.print("interface ");
            if (t.isAbstract()) out.print("abstract ");
            if (t.isSynthetic()) out.print("synthetic ");
            if (t.isDeprecated()) out.print("deprecated ");
            out.println();
            out.println("superclass: "+t.getSuperclass().getName());
            Iterator it;
            out.print("known subclasses: ");
            for(it = Arrays.asList(t.getSubClasses()).iterator();
                it.hasNext(); ) {
                jq_Class c = (jq_Class)it.next();
                out.print(c.getName()+" ");
            }
            out.println();
            out.print("declared interfaces: ");
            for(it = Arrays.asList(t.getDeclaredInterfaces()).iterator();
                it.hasNext(); ) {
                jq_Class c = (jq_Class)it.next();
                out.print(c.getName()+" ");
            }
            out.println();
            out.print("declared instance fields: ");
            for(it = Arrays.asList(t.getDeclaredInstanceFields()).iterator();
                it.hasNext(); ) {
                jq_InstanceField c = (jq_InstanceField)it.next();
                out.print(c.getName()+" ");
            }
            out.println();
            out.print("declared static fields: ");
            for(it = Arrays.asList(t.getDeclaredStaticFields()).iterator();
                it.hasNext(); ) {
                jq_StaticField c = (jq_StaticField)it.next();
                out.print(c.getName()+" ");
            }
            out.println();
            out.print("declared instance methods: ");
            for(it = Arrays.asList(t.getDeclaredInstanceMethods()).iterator();
                it.hasNext(); ) {
                jq_InstanceMethod c = (jq_InstanceMethod)it.next();
                out.println(c.getName()+" ");
                out.println("method attributes:");
                for(Iterator it2 = c.getAttributes().keySet().iterator();
                    it2.hasNext(); ) {
                    Utf8 key = (Utf8)it2.next();
                    out.print("\t"+key);
                    byte[] val = t.getAttribute(key);
                    out.println(": "+((val!=null)?"(length "+val.length+")\t":"\t")+val);
                }
            }
            out.println();
            out.print("declared static methods: ");
            for(it = Arrays.asList(t.getDeclaredStaticMethods()).iterator();
                it.hasNext(); ) {
                jq_StaticMethod c = (jq_StaticMethod)it.next();
                out.println(c.getName()+" ");
                out.println("method attributes:");
                for(Iterator it2 = c.getAttributes().keySet().iterator();
                    it2.hasNext(); ) {
                    Utf8 key = (Utf8)it2.next();
                    out.print("\t"+key);
                    byte[] val = t.getAttribute(key);
                    out.println(": "+((val!=null)?"(length "+val.length+")\t":"\t")+val);
                }
            }
            out.println();
            out.print("class initializer: ");
            if (t.getClassInitializer() != null) out.println("present");
            else out.println("absent");
            out.println("constant pool size: "+t.getCPCount());
            out.println("attributes:");
            for(it = t.getAttributes();
                it.hasNext(); ) {
                Utf8 key = (Utf8)it.next();
                byte[] val = t.getAttribute(key);
                out.println("\t"+key+": (length "+val.length+")\t"+val);
            }
        }
        if (t.isPrepared()) {
            Iterator it;
            out.print("interfaces: ");
            for(it = Arrays.asList(t.getInterfaces()).iterator();
                it.hasNext(); ) {
                jq_Class c = (jq_Class)it.next();
                out.print(c+" ");
            }
            out.println();
            out.print("virtual methods: ");
            for(it = Arrays.asList(t.getVirtualMethods()).iterator();
                it.hasNext(); ) {
                jq_InstanceMethod c = (jq_InstanceMethod)it.next();
                out.print(c+" ");
            }
            out.println();
        }
        if (t.isSFInitialized()) {
        }
        if (t.isClsInitialized()) {
        }
        
    }
}
