// TraceFlags.java, created Mon Feb  5 23:23:21 2001 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Main;

import java.util.Collection;
import java.util.Iterator;
import java.lang.reflect.Field;
import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Array;
import joeq.Class.jq_Class;
import joeq.Class.jq_Member;
import joeq.Class.jq_Primitive;
import joeq.Class.jq_StaticField;
import joeq.Class.jq_Type;
import joeq.Runtime.Debug;
import joeq.Runtime.Reflection;
import joeq.UTF.Utf8;
import jwutil.collections.Filter;
import jwutil.reflect.Reflect;
import jwutil.util.Assert;

/**
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: TraceFlags.java,v 1.38 2004/10/08 08:42:42 joewhaley Exp $
 */
public abstract class TraceFlags {

    public static int setTraceFlag(String[] args, int i) {
        if (args[i].equalsIgnoreCase("-TraceCodeAllocator")) {
            makeTrue("joeq.Allocator.CodeAllocator", "TRACE");
            makeTrue("joeq.Allocator.RuntimeCodeAllocator", "TRACE");
            return i+1;
        }
        if (args[i].equalsIgnoreCase("-TraceAssembler")) {
            makeTrue("joeq.Assembler.x86.x86Assembler", "TRACE");
            return i+1;
        }
        if (args[i].equalsIgnoreCase("-TraceBC2Quad")) {
            makeTrue("joeq.Compiler.Quad.BytecodeToQuad","ALWAYS_TRACE");
            makeTrue("joeq.Compiler.Quad.BytecodeToQuad.AbstractState","TRACE");
            return i+1;
        }
        if (args[i].equalsIgnoreCase("-TraceLiveRef")) {
            makeTrue("joeq.Compiler.BytecodeAnalysis.LiveRefAnalysis","ALWAYS_TRACE");
            return i+1;
        }
        if (args[i].equalsIgnoreCase("-TraceBootImage")) {
            makeTrue("joeq.Bootstrap.SinglePassBootImage","TRACE");
            return i+1;
        }
        if (args[i].equalsIgnoreCase("-TraceObjectTraverser")) {
            makeTrue("joeq.Runtime.ObjectTraverser","TRACE");
            return i+1;
        }
        if (args[i].equalsIgnoreCase("-TraceClassLoader")) {
            makeTrue("joeq.Class.PrimordialClassLoader","TRACE");
            return i+1;
        }
        if (args[i].equalsIgnoreCase("-TraceClass")) {
            makeTrue("joeq.Class.jq_Class","TRACE");
            makeTrue("joeq.Class.jq_Array","TRACE");
            return i+1;
        }
        if (args[i].equalsIgnoreCase("-TraceConstantPool")) {
            makeTrue("joeq.Class.jq_ConstantPool","TRACE");
            return i+1;
        }
        if (args[i].equalsIgnoreCase("-TraceExceptions")) {
            makeTrue("joeq.Class.jq_CompiledCode","TRACE");
            makeTrue("joeq.Compiler.Reference.x86.x86ReferenceExceptionDeliverer","TRACE");
            makeTrue("joeq.Runtime.ExceptionDeliverer","TRACE");
            return i+1;
        }
        if (args[i].equalsIgnoreCase("-TraceTrimmer")) {
            makeTrue("joeq.Compiler.BytecodeAnalysis.Trimmer","TRACE");
            makeTrue("joeq.Bootstrap.BootstrapRootSet","TRACE");
            return i+1;
        }
        if (args[i].equalsIgnoreCase("-TraceCompiler")) {
            makeTrue("joeq.Compiler.Reference.x86.x86ReferenceCompiler","ALWAYS_TRACE");
            return i+1;
        }
        if (args[i].equalsIgnoreCase("-TraceCompileStubs")) {
            makeTrue("joeq.Compiler.Reference.x86.x86ReferenceCompiler","TRACE_STUBS");
            return i+1;
        }
        if (args[i].equalsIgnoreCase("-TraceLinker")) {
            makeTrue("joeq.Compiler.Reference.x86.x86ReferenceLinker","TRACE");
            return i+1;
        }
        if (args[i].equalsIgnoreCase("-TraceInterpreter")) {
            makeTrue("joeq.Interpreter.BytecodeInterpreter","ALWAYS_TRACE");
            return i+1;
        }
        if (args[i].equalsIgnoreCase("-TraceQuadInterpreter")) {
            makeTrue("joeq.Interpreter.QuadInterpreter","TRACE");
            return i+1;
        }
        if (args[i].equalsIgnoreCase("-TraceStackWalker")) {
            makeTrue("joeq.Runtime.StackCodeWalker","TRACE");
            return i+1;
        }
        if (args[i].equalsIgnoreCase("-TraceUtf8")) {
            makeTrue("joeq.UTF.Utf8","TRACE");
            return i+1;
        }
        if (args[i].equalsIgnoreCase("-TraceScheduler")) {
            makeTrue("joeq.Scheduler.jq_NativeThread","TRACE");
            makeTrue("joeq.Scheduler.jq_InterrupterThread","TRACE");
            return i+1;
        }
        if (args[i].equalsIgnoreCase("-TraceLocks")) {
            makeTrue("joeq.Runtime.Monitor","TRACE");
            return i+1;
        }
        /* ARGH: Fix this. */
        if (args[i].equalsIgnoreCase("-TraceByMethodName")) {
            addReflect("joeq.Compiler.Reference.x86.x86ReferenceCompiler","TraceMethod_MethodNames", args[++i]);
            return i+1;
        }
        if (args[i].equalsIgnoreCase("-TraceByClassName")) {
            addReflect("joeq.Compiler.Reference.x86.x86ReferenceCompiler","TraceMethod_ClassNames", args[++i]);
            return i+1;
        }
        if (args[i].equalsIgnoreCase("-TraceBCByMethodName")) {
            addReflect("joeq.Compiler.Reference.x86.x86ReferenceCompiler","TraceBytecode_MethodNames", args[++i]);
            return i+1;
        }
        if (args[i].equalsIgnoreCase("-TraceBCByClassName")) {
            addReflect("joeq.Compiler.Reference.x86.x86ReferenceCompiler","TraceBytecode_ClassNames", args[++i]);
            return i+1;
        }
        if (args[i].equalsIgnoreCase("-ReplaceClass")) {
            makeTrue("joeq.Class.jq_Class","REPLACE_CLASS");
            // collect a list of classes to replace
            String s = args[++i];
            for (;;) {
                int index1 = s.indexOf(';');
                int index2 = s.indexOf(':');
                int index = (index1 == -1)?index2:((index2 == -1)?index1:Math.min(index1, index2));
                if (index != -1) {
                    String className = s.substring(0, index);
                    joeq.Class.jq_Class.classToReplace.add(className);
                    s = s.substring(index+1);
                } else {
                    joeq.Class.jq_Class.classToReplace.add(s);
                    break;
                }
            }
            return i+1;
        }
        if (args[i].equalsIgnoreCase("-TraceReplaceClass")) {
            makeTrue("joeq.Class.jq_Class","TRACE_REPLACE_CLASS");
            return i+1;
        }
        if (args[i].equalsIgnoreCase("-SetCompiler")) {
            Reflect.invoke("joeq.Class.Delegates", "setDefaultCompiler", new Object[] { args[++i] });
            return i+1;
        }
        if (args[i].equalsIgnoreCase("-UseCompilerForClasses")) {
            Object d = Reflect.invoke("joeq.Class.Delegates", "getCompiler", new Object[] { args[++i] });
            Object c = new jq_Member.FilterByShortClassName(args[++i]);
            Reflect.invoke("joeq.Class.Delegates", "registerCompiler",
                           new Class[] { Filter.class, 
                                         joeq.Compiler.CompilerInterface.class },
                           new Object[] { c, d });
            return i+1;
        }
        if (args[i].equalsIgnoreCase("-UseCompilerForMethods")) {
            Object d = Reflect.invoke("joeq.Class.Delegates", "getCompiler", new Object[] { args[++i] });
            Object c = new jq_Member.FilterByName(args[++i]);
            Reflect.invoke("joeq.Class.Delegates", "registerCompiler",
                           new Class[] { Filter.class, 
                                         joeq.Compiler.CompilerInterface.class },
                           new Object[] { c, d });
            return i+1;
        }
        if (args[i].equalsIgnoreCase("-Set")) {
            String fullName = args[++i];
            int b = fullName.lastIndexOf('.') + 1;
            String fieldName = fullName.substring(b);
            String className = fullName.substring(0, b - 1);
            try {
                jq_Class c = (jq_Class) jq_Type.parseType(className);
                c.cls_initialize();
                jq_StaticField m = null;
                Utf8 sf_name = Utf8.get(fieldName);
                for (Iterator it = java.util.Arrays.asList(c.getDeclaredStaticFields()).iterator(); it.hasNext();) {
                    jq_StaticField sm = (jq_StaticField) it.next();
                    if (sm.getName() == sf_name) {
                        m = sm;
                        break;
                    }
                }
                if (m != null) {
                    java.lang.reflect.Field f = (java.lang.reflect.Field) Reflection.getJDKMember(m);
                    f.setAccessible(true);
                    Object[] o = new Object[1];
                    i = parseArg(o, 0, m.getType(), args, i);
                    f.set(null, o[0]);
                } else {
                    System.err.println("Class " + fullName.substring(0, b - 1) + " doesn't contain a static field with name " + fieldName);
                }
            } catch (NoClassDefFoundError x) {
                System.err.println("Class " + fullName.substring(0, b - 1) + " (canonical name " + className + ") not found.");
                return i;
            } catch (IllegalAccessException x) {
                System.err.println("Cannot access field: " + x);
                return i+1;
            }
            return i+1;
        }
        return i;
    }

    public static int parseArg(Object[] args, int m, jq_Type type, String[] s_args, int j) {
        if (type == PrimordialClassLoader.getJavaLangString())
            args[m] = s_args[++j];
        else if (type == jq_Primitive.BOOLEAN)
            args[m] = Boolean.valueOf(s_args[++j]);
        else if (type == jq_Primitive.BYTE)
            args[m] = Byte.valueOf(s_args[++j]);
        else if (type == jq_Primitive.SHORT)
            args[m] = Short.valueOf(s_args[++j]);
        else if (type == jq_Primitive.CHAR)
            args[m] = new Character(s_args[++j].charAt(0));
        else if (type == jq_Primitive.INT)
            args[m] = Integer.valueOf(s_args[++j]);
        else if (type == jq_Primitive.LONG) {
            args[m] = Long.valueOf(s_args[++j]);
        } else if (type == jq_Primitive.FLOAT)
            args[m] = Float.valueOf(s_args[++j]);
        else if (type == jq_Primitive.DOUBLE) {
            args[m] = Double.valueOf(s_args[++j]);
        } else if (type.isArrayType()) {
            if (!s_args[++j].equals("{"))
                Assert.UNREACHABLE("array parameter doesn't start with {");
            int count = 0;
            while (!s_args[++j].equals("}")) ++count;
            jq_Type elementType = ((jq_Array) type).getElementType();
            if (elementType == PrimordialClassLoader.getJavaLangString()) {
                String[] array = new String[count];
                for (int k = 0; k < count; ++k)
                    array[k] = s_args[j - count + k];
                args[m] = array;
            } else if (elementType == jq_Primitive.BOOLEAN) {
                boolean[] array = new boolean[count];
                for (int k = 0; k < count; ++k)
                    array[k] = Boolean.valueOf(s_args[j - count + k]).booleanValue();
                args[m] = array;
            } else if (elementType == jq_Primitive.BYTE) {
                byte[] array = new byte[count];
                for (int k = 0; k < count; ++k)
                    array[k] = Byte.parseByte(s_args[j - count + k]);
                args[m] = array;
            } else if (elementType == jq_Primitive.SHORT) {
                short[] array = new short[count];
                for (int k = 0; k < count; ++k)
                    array[k] = Short.parseShort(s_args[j - count + k]);
                args[m] = array;
            } else if (elementType == jq_Primitive.CHAR) {
                char[] array = new char[count];
                for (int k = 0; k < count; ++k)
                    array[k] = s_args[j - count + k].charAt(0);
                args[m] = array;
            } else if (elementType == jq_Primitive.INT) {
                int[] array = new int[count];
                for (int k = 0; k < count; ++k)
                    array[k] = Integer.parseInt(s_args[j - count + k]);
                args[m] = array;
            } else if (elementType == jq_Primitive.LONG) {
                long[] array = new long[count];
                for (int k = 0; k < count; ++k)
                    array[k] = Long.parseLong(s_args[j - count + k]);
                args[m] = array;
            } else if (elementType == jq_Primitive.FLOAT) {
                float[] array = new float[count];
                for (int k = 0; k < count; ++k)
                    array[k] = Float.parseFloat(s_args[j - count + k]);
                args[m] = array;
            } else if (elementType == jq_Primitive.DOUBLE) {
                double[] array = new double[count];
                for (int k = 0; k < count; ++k)
                    array[k] = Double.parseDouble(s_args[j - count + k]);
                args[m] = array;
            } else
                Assert.UNREACHABLE("Parsing of type " + type + " is not implemented");
        } else
            Assert.UNREACHABLE("Parsing of type " + type + " is not implemented");
        return j;
    }

    public static void addReflect(String classname, String collectionname, Object toadd) {
        try {
            Class c = Class.forName(classname);
            Field f = c.getField(collectionname);
            Collection col = (Collection)f.get(null);
            col.add(toadd);
        } catch (Exception e) {
            Debug.writeln("Cannot add to collection "+classname+"."+collectionname);
        }
    }
    
    public static void makeTrue(String className, String fieldName) {
        Reflect.setBooleanField(className, fieldName, true);
    }
    
}
