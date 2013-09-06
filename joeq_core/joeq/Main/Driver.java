// Driver.java, created Fri Jan 11 17:13:17 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Main;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Array;
import joeq.Class.jq_Class;
import joeq.Class.jq_Method;
import joeq.Class.jq_MethodVisitor;
import joeq.Class.jq_Primitive;
import joeq.Class.jq_StaticField;
import joeq.Class.jq_StaticMethod;
import joeq.Class.jq_Type;
import joeq.Class.jq_TypeVisitor;
import joeq.Compiler.Quad.BasicBlockVisitor;
import joeq.Compiler.Quad.CallGraph;
import joeq.Compiler.Quad.CodeCache;
import joeq.Compiler.Quad.ControlFlowGraphVisitor;
import joeq.Compiler.Quad.LoadedCallGraph;
import joeq.Compiler.Quad.QuadVisitor;
import joeq.Runtime.Reflection;
import joeq.UTF.Utf8;
import jwutil.console.SimpleInterpreter;
import jwutil.strings.Strings;
import jwutil.util.Assert;

/*
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: Driver.java,v 1.36 2004/09/22 22:17:32 joewhaley Exp $
 */
public abstract class Driver {

    public static void main(String[] args) {
        // initialize jq
        HostedVM.initialize();

        try {
            interpreterClass = Class.forName("joeq.Interpreter.QuadInterpreter", false, Driver.class.getClassLoader());
        } catch (ClassNotFoundException x) {
            System.err.println("Warning: interpreter class not found.");
        }

        SimpleInterpreter si = new SimpleInterpreter((URL[])null);
        if ((args.length == 0) || args[0].equals("-i")) {
            // interactive mode
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            for (; ;) {
                String[] commands;
                try {
                    System.out.print("joeq> ");
                    String line = in.readLine();
                    if (line == null) return;
                    StringTokenizer st = new StringTokenizer(line);
                    int size = st.countTokens();
                    commands = new String[size];
                    for (int i = 0; i < size; ++i) {
                        commands[i] = st.nextToken();
                    }
                    Assert._assert(!st.hasMoreTokens());
                } catch (IOException x) {
                    System.err.println(x.toString());
                    return;
                }
                for (int i = 0; i < commands.length; ++i) {
                    i = processCommand(commands, i, si);
                }
            }
        }
        for (int i = 0; i < args.length; ++i) {
            i = processCommand(args, i, si);
        }
    }

    public static List classesToProcess = new LinkedList();
    static HashSet methodNamesToProcess;
    static boolean trace_bb = false;
    static boolean trace_cfg = false;
    static boolean trace_method = false;
    static boolean trace_type = false;
    // public so that "set Main.Driver.ignore_linkage_errors true" works
    public static boolean ignore_linkage_errors = false;

    static Class interpreterClass;

    private static void addClassesInPackage(String pkgName, boolean recursive) {
        String canonicalPackageName = pkgName.replace('.', '/');
        if (!canonicalPackageName.endsWith("/")) canonicalPackageName += '/';
        Iterator i = PrimordialClassLoader.loader.listPackage(canonicalPackageName, recursive);
        if (!i.hasNext()) {
            System.err.println("Package " + canonicalPackageName + " not found.");
        }
        // Because listPackage() may return entries twice, we record loaded
        // entries in 'loaded' and skip dups
        HashSet loaded = new HashSet();
        while (i.hasNext()) {
            String canonicalClassName = canonicalizeClassName((String) i.next());
            if (loaded.contains(canonicalClassName))
                continue;
            loaded.add(canonicalClassName);
            try {
                jq_Class c = (jq_Class) PrimordialClassLoader.loader.getOrCreateBSType(canonicalClassName);
                c.load();
                classesToProcess.add(c);
            } catch (NoClassDefFoundError x) {
                System.err.println("Package " + pkgName + ": Class not found (canonical name " + canonicalClassName + ").");
            } catch (LinkageError le) {
                if (!ignore_linkage_errors)
                    throw le;
                System.err.println("Linkage error occurred while loading class (" + canonicalClassName + "):");
                le.printStackTrace(System.err);
            }
        }
    }

    public static int processCommand(String[] commandBuffer, int index) {
        return processCommand(commandBuffer, index, (SimpleInterpreter)null);
    }

    public static int processCommand(String[] commandBuffer, int index, SimpleInterpreter si) {
        try {
            if (commandBuffer[index].equalsIgnoreCase("addtoclasspath")) {
                String path = commandBuffer[++index];
                PrimordialClassLoader.loader.addToClasspath(path);
            } else if (commandBuffer[index].equalsIgnoreCase("trace")) {
                String which = commandBuffer[++index];
                if (which.equalsIgnoreCase("bb")) {
                    trace_bb = true;
                } else if (which.equalsIgnoreCase("cfg")) {
                    trace_cfg = true;
                } else if (which.equalsIgnoreCase("method")) {
                    trace_method = true;
                } else if (which.equalsIgnoreCase("type")) {
                    trace_type = true;
                } else {
                    System.err.println("Unknown trace option " + which);
                }
            } else if (commandBuffer[index].equalsIgnoreCase("method")) {
                if (methodNamesToProcess == null) methodNamesToProcess = new HashSet();
                methodNamesToProcess.add(commandBuffer[++index]);
            } else if (commandBuffer[index].equalsIgnoreCase("class")) {
                String canonicalClassName = canonicalizeClassName(commandBuffer[++index]);
                try {
                    jq_Class c = (jq_Class) PrimordialClassLoader.loader.getOrCreateBSType(canonicalClassName);
                    c.load();
                    classesToProcess.add(c);
                } catch (NoClassDefFoundError x) {
                    System.err.println("Class " + commandBuffer[index] + " (canonical name " + canonicalClassName + ") not found.");
                }
            } else if (commandBuffer[index].equalsIgnoreCase("package")) {
                addClassesInPackage(commandBuffer[++index], /*recursive=*/false);
            } else if (commandBuffer[index].equalsIgnoreCase("packages")) {
                addClassesInPackage(commandBuffer[++index], /*recursive=*/true);
            } else if (commandBuffer[index].equalsIgnoreCase("callgraph")) {
                String callgraphFile = commandBuffer[++index];
                try {
                    CallGraph cg = new LoadedCallGraph(callgraphFile);
                    HashSet set = new HashSet();
                    for (Iterator i = cg.getAllMethods().iterator(); i.hasNext(); ) {
                        jq_Method m = (jq_Method) i.next();
                        set.add(m.getDeclaringClass());
                    }
                    classesToProcess.addAll(set);
                } catch (IOException x) {
                }
            } else if (commandBuffer[index].equalsIgnoreCase("setinterpreter")) {
                String interpreterClassName = commandBuffer[++index];
                try {
                    Class cl = Class.forName(interpreterClassName);
                    if (Class.forName("joeq.Interpreter.QuadInterpreter").isAssignableFrom(cl)) {
                        interpreterClass = cl;
                        System.out.println("Interpreter class changed to " + interpreterClass);
                    } else {
                        System.err.println("Class " + interpreterClassName + " does not subclass joeq.Interpreter.QuadInterpreter.");
                    }
                } catch (java.lang.ClassNotFoundException x) {
                    System.err.println("Cannot find interpreter named " + interpreterClassName + ".");
                    System.err.println("Check your classpath and make sure you compiled your interpreter.");
                    return index;
                }

            } else if (commandBuffer[index].equalsIgnoreCase("interpret")) {
                String fullName = commandBuffer[++index];
                int b = fullName.lastIndexOf('.') + 1;
                String methodName = fullName.substring(b);
                String className = canonicalizeClassName(fullName.substring(0, b - 1));
                try {
                    jq_Class c = (jq_Class) PrimordialClassLoader.loader.getOrCreateBSType(className);
                    c.cls_initialize();
                    jq_StaticMethod m = null;
                    Utf8 rootm_name = Utf8.get(methodName);
                    for (Iterator it = java.util.Arrays.asList(c.getDeclaredStaticMethods()).iterator(); it.hasNext();) {
                        jq_StaticMethod sm = (jq_StaticMethod) it.next();
                        if (sm.getName() == rootm_name) {
                            m = sm;
                            break;
                        }
                    }
                    if (m != null) {
                        Object[] args = new Object[m.getParamTypes().length];
                        index = parseMethodArgs(args, m.getParamTypes(), commandBuffer, index);
                        joeq.Interpreter.QuadInterpreter s = null;
                        java.lang.reflect.Method im = interpreterClass.getMethod("interpretMethod", new Class[]{Class.forName("Class.jq_Method"), new Object[0].getClass()});
                        s = (joeq.Interpreter.QuadInterpreter) im.invoke(null, new Object[]{m, args});
                        //s = joeq.Interpreter.QuadInterpreter.interpretMethod(m, args);
                        System.out.flush();
                        System.out.println("Result of interpretation: " + s);
                    } else {
                        System.err.println("Class " + fullName.substring(0, b - 1) + " doesn't contain a void static no-argument method with name " + methodName);
                    }
                } catch (NoClassDefFoundError x) {
                    System.err.println("Class " + fullName.substring(0, b - 1) + " (canonical name " + className + ") not found.");
                    return index;
                } catch (NoSuchMethodException x) {
                    System.err.println("Interpreter method in " + interpreterClass + " not found! " + x);
                    return index;
                } catch (ClassNotFoundException x) {
                    System.err.println("Class.jq_Method class not found! " + x);
                    return index;
                } catch (IllegalAccessException x) {
                    System.err.println("Cannot access interpreter " + interpreterClass + ": " + x);
                    return index;
                } catch (java.lang.reflect.InvocationTargetException x) {
                    System.err.println("Interpreter threw exception: " + x.getTargetException());
                    x.getTargetException().printStackTrace();
                    return index;
                }

            } else if (commandBuffer[index].equalsIgnoreCase("set")) {
                String fullName = commandBuffer[++index];
                int b = fullName.lastIndexOf('.') + 1;
                String fieldName = fullName.substring(b);
                String className = canonicalizeClassName(fullName.substring(0, b - 1));
                try {
                    jq_Class c = (jq_Class) PrimordialClassLoader.loader.getOrCreateBSType(className);
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
                        index = parseArg(o, 0, m.getType(), commandBuffer, index);
                        f.set(null, o[0]);
                    } else {
                        System.err.println("Class " + fullName.substring(0, b - 1) + " doesn't contain a static field with name " + fieldName);
                    }
                } catch (NoClassDefFoundError x) {
                    System.err.println("Class " + fullName.substring(0, b - 1) + " (canonical name " + className + ") not found.");
                    return index;
                } catch (IllegalAccessException x) {
                    System.err.println("Cannot access field: " + x);
                    return index;
                }

            } else if (commandBuffer[index].equalsIgnoreCase("addpass")) {
                String passname = commandBuffer[++index];
                ControlFlowGraphVisitor mv = null;
                BasicBlockVisitor bbv = null;
                QuadVisitor qv = null;
                Object o;
                try {
                    Class c = Class.forName(passname);
                    o = c.newInstance();
                    if (o instanceof ControlFlowGraphVisitor) {
                        mv = (ControlFlowGraphVisitor) o;
                    } else {
                        if (o instanceof BasicBlockVisitor) {
                            bbv = (BasicBlockVisitor) o;
                        } else {
                            if (o instanceof QuadVisitor) {
                                qv = (QuadVisitor) o;
                            } else {
                                System.err.println("Unknown pass type " + c);
                                return index;
                            }
                            bbv = new QuadVisitor.AllQuadVisitor(qv, trace_bb);
                        }
                        mv = new BasicBlockVisitor.AllBasicBlockVisitor(bbv, trace_method);
                    }
                    CodeCache.passes.add(mv);
                } catch (java.lang.ClassNotFoundException x) {
                    System.err.println("Cannot find pass named " + passname + ".");
                    System.err.println("Check your classpath and make sure you compiled your pass.");
                    return index;
                } catch (java.lang.InstantiationException x) {
                    System.err.println("Cannot instantiate pass " + passname + ": " + x);
                    return index;
                } catch (java.lang.IllegalAccessException x) {
                    System.err.println("Cannot access pass " + passname + ": " + x);
                    System.err.println("Be sure that you made your class public?");
                    return index;
                }
            } else if (commandBuffer[index].equalsIgnoreCase("runpass")) {
                String passname = commandBuffer[++index];
                jq_TypeVisitor cv = null;
                jq_MethodVisitor mv = null;
                ControlFlowGraphVisitor cfgv = null;
                BasicBlockVisitor bbv = null;
                QuadVisitor qv = null;
                Object o;
                try {
                    passname = passname.replace('/', '.');
                    Class c = Class.forName(passname);
                    o = c.newInstance();
                    if (o instanceof jq_TypeVisitor) {
                        cv = (jq_TypeVisitor) o;
                    } else {
                        if (o instanceof jq_MethodVisitor) {
                            mv = (jq_MethodVisitor) o;
                        } else {
                            if (o instanceof ControlFlowGraphVisitor) {
                                cfgv = (ControlFlowGraphVisitor) o;
                            } else {
                                if (o instanceof BasicBlockVisitor) {
                                    bbv = (BasicBlockVisitor) o;
                                } else {
                                    if (o instanceof QuadVisitor) {
                                        qv = (QuadVisitor) o;
                                    } else {
                                        System.err.println("Unknown pass type " + c);
                                        return index;
                                    }
                                    bbv = new QuadVisitor.AllQuadVisitor(qv, trace_bb);
                                }
                                cfgv = new BasicBlockVisitor.AllBasicBlockVisitor(bbv, trace_cfg);
                            }
                            mv = new ControlFlowGraphVisitor.CodeCacheVisitor(cfgv, trace_method);
                        }
                        cv = new jq_MethodVisitor.DeclaredMethodVisitor(mv, methodNamesToProcess, trace_type);
                    }
                } catch (java.lang.ClassNotFoundException x) {
                    System.err.println("Cannot find pass named " + passname + ".");
                    System.err.println("Check your classpath and make sure you compiled your pass.");
                    return index;
                } catch (java.lang.InstantiationException x) {
                    System.err.println("Cannot instantiate pass " + passname + ": " + x);
                    return index;
                } catch (java.lang.IllegalAccessException x) {
                    System.err.println("Cannot access pass " + passname + ": " + x);
                    System.err.println("Be sure that you made your class public?");
                    return index;
                }
                // Sort classesToProcess
                Collection s = new TreeSet(new Comparator() {
                    public int compare(Object o1, Object o2) {
                        return o1.toString().compareTo(o2.toString());
                    }
                });
                s.addAll(classesToProcess);
                for (Iterator i = s.iterator(); i.hasNext();) {
                    jq_Type t = (jq_Type) i.next();
                    try {
                        t.accept(cv);
                    } catch (LinkageError le) {
                        if (!ignore_linkage_errors)
                            throw le;
                        System.err.println("Linkage error occurred while executing pass on " + t + " : " + le);
                        le.printStackTrace(System.err);
                    } catch (Exception x) {
                        System.err.println("Runtime exception occurred while executing pass on " + t + " : " + x);
                        x.printStackTrace(System.err);
                    }
                }
                System.err.println("Completed pass! " + o);
            } else if (commandBuffer[index].equalsIgnoreCase("run")) {
                String toRun = commandBuffer[++index];
                Runnable runnable = null;
                try {
                    runnable = (Runnable)Class.forName(toRun).newInstance();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                    System.err.println("Can't instantiate a " + toRun);
                    return index;
                } catch (IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    System.err.println("Can't access a field");
                    return index;
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    System.err.println("Class can't be found");
                    return index;
                }
                Assert._assert(runnable != null);
                
                runnable.run();                
            } else if (commandBuffer[index].equalsIgnoreCase("store")) {
                System.out.println(si.getStore());
            } else if (commandBuffer[index].equalsIgnoreCase("loaderpath")) {
                try {
                    si.setClassPath(new URL[] { new File(commandBuffer[++index]).toURL() });
                } catch (java.net.MalformedURLException e) {
                    e.printStackTrace(System.err);
                }
            } else if (commandBuffer[index].equalsIgnoreCase("new")) {    // new name Type arg0 arg1 arg2
                String name = commandBuffer[++index];
                String type = commandBuffer[++index];
                index = si.newObject(name, type, commandBuffer, index);
                printObjectInStore(si, name);
            } else if (commandBuffer[index].indexOf(".") != -1) {              // name.method arg0 arg1 ...
                String fullName = commandBuffer[index];
                int b = fullName.lastIndexOf('.') + 1;
                String objectName = fullName.substring(0, b-1);
                String methodName = fullName.substring(b);
                index = si.invokeMethod(objectName, methodName, commandBuffer, index);
                printObjectInStore(si, "$last");
            } else if (commandBuffer[index].equalsIgnoreCase("exit") || commandBuffer[index].equalsIgnoreCase("quit")) {
                System.exit(0);
            } else if (commandBuffer[index].equalsIgnoreCase("help")) {
                printHelp();
            } else {
                int index2 = TraceFlags.setTraceFlag(commandBuffer, index);
                if (index == index2)
                    System.err.println("Unknown command " + commandBuffer[index]);
                else
                    index = index2 - 1;
            }
        } catch (ArrayIndexOutOfBoundsException x) {
            System.err.println("Incomplete command");
            x.printStackTrace(System.err);
        }
        return index;
    }

    private static void printObjectInStore(SimpleInterpreter si, Object name) {
        Object newobj = si.getStore().get(name);
        if (newobj != null) {
            String s = newobj.toString();
            if (s.length() > 1024) s = s.substring(0, 1024);
            System.out.println(s);
        } else {
            System.err.println("object " + name + " not found - did the operation fail?");
        }
    }

    public static void printHelp() {
        String nl = Strings.lineSep;
        String helpMessage =
                nl+
                "Usage: command1 [args...] [command2 [args...]]..."+nl+nl +
                "1.  addtoclasspath   additional_classpath"+nl +
                "---->   add to the class path"+nl+nl +
                "2.  addpass   pass_class_name"+nl +
                "---->   run compiler pass on all code generated from this point"+nl+nl +
                "3.  class   class_name"+nl +
                "---->   add to the list of classes to process"+nl+nl +
                "4.  exit | quit"+nl +
                "---->   exit interactive mode"+nl+nl +
                "5.  help"+nl +
                "---->   print this message"+nl+nl +
                "6.  interpret   class_name.static_method_name param_list"+nl +
                "---->   interpret a static method with the given arguments"+nl+nl +
                "7.  method   method_name"+nl +
                "---->   only process methods of the given name"+nl+nl +
                "8.  package   package_name"+nl +
                "---->   add all the classes in the specified package to the list of classes to process"+nl+nl +
                "9.  packages   package_name"+nl +
                "---->   add all the classes in the specified package and all sub-packages recursively to the list of classes to process"+nl+nl +
                "10. runpass   pass_class_name"+nl +
                "---->   run compiler pass on classes in list"+nl+nl +
                "11. set   class_name.static_field_name value"+nl +
                "---->   set specified static field to specified value"+nl+nl +
                "12. trace   bb|cfg|method|type"+nl +
                "---->   enable tracing options when processing classes"+nl+nl +
                "Other trace options are available, see Main/TraceFlags.java."+
                nl;
        System.out.println(helpMessage);
    }

    public static String canonicalizeClassName(String s) {
        if (s.endsWith(".class")) s = s.substring(0, s.length() - 6);
        s = s.replace('.', '/');
        String desc = "L" + s + ";";
        return desc;
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

    public static int parseMethodArgs(Object[] args, jq_Type[] paramTypes, String[] s_args, int j) {
        try {
            for (int i = 0, m = 0; i < paramTypes.length; ++i, ++m) {
                j = parseArg(args, m, paramTypes[i], s_args, j);
            }
        } catch (ArrayIndexOutOfBoundsException x) {
            x.printStackTrace();
            Assert.UNREACHABLE("not enough method arguments");
        }
        return j;
    }
}
