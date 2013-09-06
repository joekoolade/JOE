// Instrument.java, created Sun May 25  5 11:14:04 2003 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Class;
import joeq.Class.jq_ConstantPool;
import joeq.Class.jq_Field;
import joeq.Class.jq_Method;
import joeq.Class.jq_MethodVisitor;
import joeq.Class.jq_StaticMethod;
import joeq.Class.jq_Type;
import joeq.Class.jq_TypeVisitor;
import joeq.Compiler.BytecodeAnalysis.Bytecodes;
import jwutil.collections.AppendIterator;
import jwutil.strings.Strings;

/**
 * Instrument
 * 
 * @author John Whaley
 * @version $Id: Instrument.java,v 1.1 2005/04/19 08:55:04 joewhaley Exp $
 */
public abstract class Instrument {

    static JarOutputStream jos;

    public static void main(String[] args) throws Exception {
        HostedVM.initialize();
        
        initialize();
        
        if (args.length == 0) {
            System.out.println("Usage: java joeq.Main.Instrument <classnames>");
            return;
        }
        
        System.out.println("Class path is "+PrimordialClassLoader.loader.classpathToString());
        
        String jarfilename = System.getProperty("jarname", "app.jar");
        FileOutputStream fos = new FileOutputStream(jarfilename);
        jos = new JarOutputStream(fos);
        
        List classes;
        if (args[0].equals("-@")) {
            java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
            classes = new LinkedList();
            for (;;) {
                String s = in.readLine();
                if (s == null || s.length() == 0) break;
                classes.add(s);
            }
        } else {
            classes = Arrays.asList(args);
        }
        
        ClassVisitor cv = new ClassVisitor();
        for (Iterator i = classes.iterator(); i.hasNext(); ) {
            String arg = (String) i.next();
            if (arg.endsWith(".class"))
                arg = arg.substring(0, arg.length()-6);
            int j = arg.indexOf("[Loaded ");
            if (j >= 0) {
                int endIndex = arg.indexOf(" from ");
                if (endIndex < 0) endIndex = arg.length()-1;
                arg = arg.substring(j+8, endIndex);
            }
            try {
                jq_Class c = (jq_Class) jq_Type.parseType(arg);
                c.load();
                cv.visitClass(c);
            } catch (Throwable x) {
                System.err.println("Error while instrumenting class "+arg+": "+x.toString());
                x.printStackTrace();
            }
        }
        
        jos.close();
    }
    
    static void initialize() {
        instrument_class = (jq_Class) PrimordialClassLoader.loader.getOrCreateBSType("LInstrumentationCallbacks;");
        instrument_class.load();
        getstatic = instrument_class.getOrCreateStaticMethod("getstatic", "(Ljava/lang/String;ILjava/lang/String;)V");
        putstatic = instrument_class.getOrCreateStaticMethod("putstatic", "(Ljava/lang/String;ILjava/lang/String;)V");
        getfield = instrument_class.getOrCreateStaticMethod("getfield", "(Ljava/lang/Object;Ljava/lang/String;ILjava/lang/String;)V");
        putfield = instrument_class.getOrCreateStaticMethod("putfield", "(Ljava/lang/Object;Ljava/lang/String;ILjava/lang/String;)V");
        arrayload = instrument_class.getOrCreateStaticMethod("arrayload", "(Ljava/lang/Object;ILjava/lang/String;I)V");
        arraystore = instrument_class.getOrCreateStaticMethod("arraystore", "(Ljava/lang/Object;ILjava/lang/String;I)V");
        entered = instrument_class.getOrCreateStaticMethod("entered", "(Ljava/lang/String;)V");
        returned = instrument_class.getOrCreateStaticMethod("returned", "(Ljava/lang/String;I)V");
        thrown = instrument_class.getOrCreateStaticMethod("thrown", "(Ljava/lang/String;)V");
        beforeinvoke = instrument_class.getOrCreateStaticMethod("beforeinvoke", "(Ljava/lang/String;)V");
        afterinvoke = instrument_class.getOrCreateStaticMethod("afterinvoke", "(Ljava/lang/String;)V");
    }
    
    public static boolean trace = true;
    
    public static class ClassVisitor
    extends jq_TypeVisitor.EmptyVisitor {
        
        jq_ConstantPool.ConstantPoolRebuilder cpa;
        jq_MethodVisitor mv;
        
        ClassVisitor() {
            this.mv = new MethodVisitor();
        }
        
        public void visitClass(jq_Class k) {
            if (trace) System.out.print(k.toString()+"\r");
            
            k.removeAttribute("InnerClasses");
            
            //cpa = new jq_ConstantPool.ConstantPoolRebuilder();
            cpa = k.rebuildConstantPool(true);
            
            map = new HashMap();
            Iterator it = new AppendIterator(Arrays.asList(k.getDeclaredStaticMethods()).iterator(),
                                             Arrays.asList(k.getDeclaredInstanceMethods()).iterator());
            while (it.hasNext()) {
                method = (jq_Method) it.next();
                mv.visitMethod(method);
                if (method.getBytecode() != null) {
                    method.setMaxStack((char)(method.getMaxStack()+4));
                    method.setMaxLocals((char)(method.getMaxLocals()+2));
                }
            }
            
            jq_ConstantPool new_cp = cpa.finish();
            k.remakeAttributes(cpa);
            k.setCP(new_cp);
            
            for (Iterator i=map.entrySet().iterator(); i.hasNext(); ) {
                Map.Entry e = (Map.Entry) i.next();
                jq_Method m = (jq_Method) e.getKey();
                List v = (List) e.getValue();
                Bytecodes.InstructionList il = (Bytecodes.InstructionList) v.get(0);
                Bytecodes.CodeException[] ex_table = (Bytecodes.CodeException[]) v.get(1);
                Bytecodes.LineNumber[] line_num = (Bytecodes.LineNumber[]) v.get(2);
                m.setCode(il, ex_table, line_num, cpa);
            }
            
            try {
                String filename = k.getName().replace('.', '/') + ".class";
                JarEntry e = new JarEntry(filename);
                jos.putNextEntry(e);
                DataOutputStream d = new DataOutputStream(jos);
                k.dump(d);
                d.flush(); jos.flush();
                jos.closeEntry();
            } catch (IOException x) {
                System.err.println("IO exception when writing to class "+k);
            }
            
            if (trace) System.out.println(Strings.left(k.toString(), 78));
        }
        
        Map map;
        
        public class MethodVisitor extends jq_MethodVisitor.EmptyVisitor {
            public void visitMethod(jq_Method m) {
                m.removeAttribute("Exceptions");
                
                byte[] bc = m.getBytecode();
                if (bc == null) {
                    return;
                }
                if (trace) System.out.print(Strings.left(m.toString(), 78)+"\r");
                // extract instructions of method.
                il = new Bytecodes.InstructionList(m);
                // extract exception tables and line numbers of method.
                List ex_table = new ArrayList(Arrays.asList(m.getExceptionTable(il)));
                Bytecodes.LineNumber[] line_num = m.getLineNumberTable(il);
                // add instrumentation.
                instrument(il, cpa);
                // add catch-all exception handler.
                Bytecodes.InstructionHandle e_start = il.getStart();
                Bytecodes.InstructionHandle e_end = il.getEnd();
                Bytecodes.InstructionHandle eh_entry = addExceptionHandler(il, cpa);
                Bytecodes.CodeException eh = new Bytecodes.CodeException(e_start, e_end, null, eh_entry);
                ex_table.add(eh);
                // update constant pool.
                cpa.addCode(il);
                
                ArrayList my_list = new ArrayList();
                my_list.add(il);
                my_list.add(ex_table.toArray(new Bytecodes.CodeException[ex_table.size()]));
                my_list.add(line_num);
                
                map.put(m, my_list);
            }
        }
    }
    
    static jq_Class instrument_class;
    static jq_StaticMethod getstatic;
    static jq_StaticMethod putstatic;
    static jq_StaticMethod getfield;
    static jq_StaticMethod putfield;
    static jq_StaticMethod arrayload;
    static jq_StaticMethod arraystore;
    static jq_StaticMethod entered;
    static jq_StaticMethod returned;
    static jq_StaticMethod thrown;
    static jq_StaticMethod beforeinvoke;
    static jq_StaticMethod afterinvoke;
    
    // used by visitor object.
    static jq_Method method;
    static Bytecodes.InstructionList il;
    static Bytecodes.InstructionHandle ih;
    static int bc_index;
    static String s1;
    static jq_ConstantPool.ConstantPoolRebuilder cpadder;
    
    static Bytecodes.Visitor v = new Bytecodes.EmptyVisitor() {
        Bytecodes.InstructionHandle pushLocation() {
            Bytecodes.PUSH p1 = new Bytecodes.PUSH(cpadder, s1);
            Bytecodes.InstructionHandle ih1 = il.insert(ih, p1);
            Bytecodes.PUSH p2 = new Bytecodes.PUSH(bc_index);
            il.insert(ih, p2);
            return ih1;
        }
        void pushFieldName(Bytecodes.FieldInstruction obj) {
            jq_Field f = obj.getField();
            String s2 = f.toString();
            Bytecodes.PUSH p3 = new Bytecodes.PUSH(cpadder, s2);
            il.insert(ih, p3);
        }
        
        public void visitGETSTATIC(Bytecodes.GETSTATIC obj) {
            Bytecodes.InstructionHandle ih1 = pushLocation();
            pushFieldName(obj);
            Bytecodes.INVOKESTATIC is = new Bytecodes.INVOKESTATIC(getstatic);
            il.insert(ih, is);
            il.redirectBranches(ih, ih1);
        }
        
        public void visitPUTSTATIC(Bytecodes.PUTSTATIC obj) {
            Bytecodes.InstructionHandle ih1 = pushLocation();
            pushFieldName(obj);
            Bytecodes.INVOKESTATIC is = new Bytecodes.INVOKESTATIC(putstatic);
            il.insert(ih, is);
            il.redirectBranches(ih, ih1);
        }
        
        public void visitGETFIELD(Bytecodes.GETFIELD obj) {
            Bytecodes.DUP d = new Bytecodes.DUP();
            Bytecodes.InstructionHandle ih1 = il.insert(ih, d);
            pushLocation();
            pushFieldName(obj);
            Bytecodes.INVOKESTATIC is = new Bytecodes.INVOKESTATIC(getfield);
            il.insert(ih, is);
            il.redirectBranches(ih, ih1);
        }
        
        public void visitPUTFIELD(Bytecodes.PUTFIELD obj) {
            Bytecodes.Instruction d;
            Bytecodes.InstructionHandle ih1;
            if (obj.getType().getReferenceSize() == 8) {
                d = new Bytecodes.DUP2_X1();
                ih1 = il.insert(ih, d);
                d = new Bytecodes.POP2();
                il.insert(ih, d);
                d = new Bytecodes.DUP_X2();
                il.insert(ih, d);
            } else {
                d = new Bytecodes.DUP2();
                ih1 = il.insert(ih, d);
                d = new Bytecodes.POP();
                il.insert(ih, d);
            }
            pushLocation();
            pushFieldName(obj);
            Bytecodes.INVOKESTATIC is = new Bytecodes.INVOKESTATIC(putfield);
            il.insert(ih, is);
            il.redirectBranches(ih, ih1);
        }
        
        public void visitReturnInstruction(Bytecodes.ReturnInstruction obj) {
            Bytecodes.InstructionHandle ih1;
            ih1 = pushLocation();
            Bytecodes.INVOKESTATIC is = new Bytecodes.INVOKESTATIC(returned);
            il.insert(ih, is);
            il.redirectBranches(ih, ih1);
        }
        
        public void visitInvokeInstruction(Bytecodes.InvokeInstruction obj) {
            jq_Class c = obj.getMethod().getDeclaringClass();
            if (c == instrument_class) return;
            Bytecodes.InstructionHandle ih1;
            String s2 = c.getName()+'.'+obj.getMethodName();
            Bytecodes.PUSH p3 = new Bytecodes.PUSH(cpadder, s2);
            ih1 = il.insert(ih, p3);
            Bytecodes.INVOKESTATIC is = new Bytecodes.INVOKESTATIC(beforeinvoke);
            il.insert(ih, is);
            il.redirectBranches(ih, ih1);
            //il.insert(ih, obj);
            Bytecodes.INVOKESTATIC is2 = new Bytecodes.INVOKESTATIC(afterinvoke);
            il.append(ih, is2);
            il.append(ih, p3);
            //try {
            //    il.delete(ih);
            //} catch (Bytecodes.TargetLostException x) {
            //    Assert.UNREACHABLE(x.toString());
            //}
        }
        
        public void visitArrayInstruction(Bytecodes.ArrayInstruction obj) {
            jq_StaticMethod m;
            Bytecodes.InstructionHandle ih1;
            if (obj instanceof Bytecodes.IASTORE ||
                obj instanceof Bytecodes.AASTORE ||
                obj instanceof Bytecodes.FASTORE ||
                obj instanceof Bytecodes.BASTORE ||
                obj instanceof Bytecodes.CASTORE ||
                obj instanceof Bytecodes.SASTORE) {
                m = arraystore;
                Bytecodes.Instruction d;
                d = new Bytecodes.DUP_X2();
                ih1 = il.insert(ih, d);
                d = new Bytecodes.POP();
                il.insert(ih, d);
                d = new Bytecodes.DUP2_X1();
                il.insert(ih, d);
            } else if (obj instanceof Bytecodes.LASTORE) {
                m = arraystore;
                Bytecodes.Instruction d;
                d = new Bytecodes.LSTORE(method.getMaxLocals());
                ih1 = il.insert(ih, d);
                d = new Bytecodes.DUP2();
                il.insert(ih, d);
                pushLocation();
                Bytecodes.INVOKESTATIC is = new Bytecodes.INVOKESTATIC(m);
                il.insert(ih, is);
                d = new Bytecodes.LLOAD(method.getMaxLocals());
                il.insert(ih, d);
                il.redirectBranches(ih, ih1);
                return;
            } else if (obj instanceof Bytecodes.DASTORE) {
                m = arraystore;
                Bytecodes.Instruction d;
                d = new Bytecodes.DSTORE(method.getMaxLocals());
                ih1 = il.insert(ih, d);
                d = new Bytecodes.DUP2();
                il.insert(ih, d);
                pushLocation();
                Bytecodes.INVOKESTATIC is = new Bytecodes.INVOKESTATIC(m);
                il.insert(ih, is);
                d = new Bytecodes.DLOAD(method.getMaxLocals());
                il.insert(ih, d);
                il.redirectBranches(ih, ih1);
                return;
            } else if (obj instanceof Bytecodes.IALOAD ||
                obj instanceof Bytecodes.AALOAD ||
                obj instanceof Bytecodes.FALOAD ||
                obj instanceof Bytecodes.BALOAD ||
                obj instanceof Bytecodes.CALOAD ||
                obj instanceof Bytecodes.SALOAD ||
                obj instanceof Bytecodes.LALOAD ||
                obj instanceof Bytecodes.DALOAD
                       ) {
                m = arrayload;
                Bytecodes.Instruction d;
                d = new Bytecodes.DUP2();
                ih1 = il.insert(ih, d);
            } else {
                return;
            }
            pushLocation();
            Bytecodes.INVOKESTATIC is = new Bytecodes.INVOKESTATIC(m);
            il.insert(ih, is);
            il.redirectBranches(ih, ih1);
        }
    };
    
    public static Bytecodes.InstructionHandle addExceptionHandler(Bytecodes.InstructionList il, jq_ConstantPool.ConstantPoolRebuilder cp) {
        Bytecodes.PUSH p1 = new Bytecodes.PUSH(cpadder, s1);
        Bytecodes.InstructionHandle eh_ih = il.append(p1);
        Bytecodes.INVOKESTATIC is = new Bytecodes.INVOKESTATIC(thrown);
        il.append(is);
        Bytecodes.ATHROW at = new Bytecodes.ATHROW();
        il.append(at);
        return eh_ih;
    }
    public static void instrument(Bytecodes.InstructionList il, jq_ConstantPool.ConstantPoolRebuilder cp) {
        cpadder = cp;
        s1 = method.getDeclaringClass()+"."+method.getName()+method.getDesc();
        
        Bytecodes.INVOKESTATIC is = new Bytecodes.INVOKESTATIC(entered);
        il.insert(is);
        Bytecodes.PUSH p1 = new Bytecodes.PUSH(cpadder, s1+"@-1");
        il.insert(p1);
        
        for (ih = il.getStart(); ih != null; ih = ih.getNext()) {
            bc_index = ih.getPosition();
            ih.accept(v);
        }
    }
    
}

