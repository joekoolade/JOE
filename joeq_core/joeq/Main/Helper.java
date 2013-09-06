// Helper.java, created Thu Jan 16 10:53:32 2003 by mcmartin
// Copyright (C) 2001-3 mcmartin
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Main;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Class;
import joeq.Class.jq_Method;
import joeq.Class.jq_MethodVisitor;
import joeq.Class.jq_Type;
import joeq.Class.jq_TypeVisitor;
import joeq.Compiler.Quad.BasicBlock;
import joeq.Compiler.Quad.BasicBlockVisitor;
import joeq.Compiler.Quad.ControlFlowGraph;
import joeq.Compiler.Quad.ControlFlowGraphVisitor;
import joeq.Compiler.Quad.Quad;
import joeq.Compiler.Quad.QuadVisitor;

/**
 * @author  Michael Martin <mcmartin@stanford.edu>
 * @version $Id: Helper.java,v 1.14 2004/06/29 21:06:39 mcmartin Exp $
 */
public class Helper {
    static {
        HostedVM.initialize();
    }

    public static jq_Type load(String classname) {
        try {
            jq_Type c = jq_Type.parseType(classname);
            c.load();
            c.prepare();
            return c;
        } catch (NoClassDefFoundError e) {
            System.err.println("Could not find class " + classname
                + ", skipping.");
        }
        return null;
    }

    public static jq_Type[] loadPackage(String packagename) {
        return loadPackages(packagename, false);
    }

    public static jq_Type[] loadPackages(String packagename) {
        return loadPackages(packagename, true);
    }

    public static jq_Type[] loadPackages(String packagename, boolean recursive) {
        String canonicalPackageName = packagename.replace('.', '/');
        if (!canonicalPackageName.equals("")
            && !canonicalPackageName.endsWith("/")) canonicalPackageName += '/';
        Iterator i = joeq.Class.PrimordialClassLoader.loader.listPackage(
            canonicalPackageName, recursive);
        if (!i.hasNext()) {
            System.err.println("Package " + canonicalPackageName
                + " not found.");
        }
        LinkedList ll = new LinkedList();
        while (i.hasNext()) {
            String c = (String) i.next();
            c = c.substring(0, c.length() - 6);
            jq_Type t = Helper.load(c);
            if (t != null) ll.add(t);
        }
        return (jq_Class[]) ll.toArray(new jq_Class[0]);
    }

    /**
     * Add paths contained in file fileName. 
     *  @param fileName -- name of the file with class paths
     * */
    public static void addToClassPath(String fileName) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            for (;;) {
                String s = br.readLine();
                if (s == null) break;
                if (s.length() == 0) continue;
                if (s.startsWith("%")) continue;
                if (s.startsWith("#")) continue;
                PrimordialClassLoader.loader.addToClasspath(s);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(2);
        }
    }

    public static void addJarDir(String dir) {
        List jars = new LinkedList();

        addJarDir_aux(new File(dir), jars);

        for (Iterator i = jars.iterator(); i.hasNext();) {
            PrimordialClassLoader.loader.addToClasspath(i.next().toString());
        }
    }

    static private void addJarDir_aux(File f, List results) {
        if (f.getPath().endsWith(".jar")) {
            results.add(f.getPath());
        } else if (f.list() != null) {
            String[] contents=f.list();
            for (int i = 0; i<contents.length; i++) {
                addJarDir_aux(new File(f.getPath(), contents[i]), results);
            }
        }
    }

    
    public static void runPass(jq_Class c, jq_TypeVisitor tv) {
        c.accept(tv);
    }

    public static void runPass(jq_Class c, jq_MethodVisitor mv) {
        runPass(c, new jq_MethodVisitor.DeclaredMethodVisitor(mv));
    }

    public static void runPass(jq_Class c, ControlFlowGraphVisitor cfgv) {
        runPass(c, new ControlFlowGraphVisitor.CodeCacheVisitor(cfgv));
    }

    public static void runPass(jq_Class c, BasicBlockVisitor bbv) {
        runPass(c, new BasicBlockVisitor.AllBasicBlockVisitor(bbv));
    }

    public static void runPass(jq_Class c, QuadVisitor qv) {
        runPass(c, new QuadVisitor.AllQuadVisitor(qv));
    }

    public static void runPass(jq_Method m, jq_MethodVisitor mv) {
        m.accept(mv);
    }

    public static void runPass(jq_Method m, ControlFlowGraphVisitor cfgv) {
        runPass(m, new ControlFlowGraphVisitor.CodeCacheVisitor(cfgv));
    }

    public static void runPass(jq_Method m, BasicBlockVisitor bbv) {
        runPass(m, new BasicBlockVisitor.AllBasicBlockVisitor(bbv));
    }

    public static void runPass(jq_Method m, QuadVisitor qv) {
        runPass(m, new QuadVisitor.AllQuadVisitor(qv));
    }

    public static void runPass(ControlFlowGraph c, ControlFlowGraphVisitor cfgv) {
        cfgv.visitCFG(c);
    }

    public static void runPass(ControlFlowGraph c, BasicBlockVisitor bbv) {
        runPass(c, new BasicBlockVisitor.AllBasicBlockVisitor(bbv));
    }

    public static void runPass(ControlFlowGraph c, QuadVisitor qv) {
        runPass(c, new QuadVisitor.AllQuadVisitor(qv));
    }

    public static void runPass(BasicBlock b, BasicBlockVisitor bbv) {
        bbv.visitBasicBlock(b);
    }

    public static void runPass(BasicBlock b, QuadVisitor qv) {
        runPass(b, new QuadVisitor.AllQuadVisitor(qv));
    }

    public static void runPass(Quad q, QuadVisitor qv) {
        q.accept(qv);
    }
}