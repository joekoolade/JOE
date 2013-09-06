// Helper.java, created Thu Jan 16 10:53:32 2003 by mcmartin
// Copyright (C) 2001-3 mcmartin
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Class;
import joeq.Class.jq_Type;
import jwutil.util.Assert;

/**
 * author: V.Benjamin Livshits
 * $Id: ClasspathWalker.java,v 1.22 2005/10/08 19:51:20 joewhaley Exp $
 * */
public class ClasspathWalker {
    private static final boolean TRACE = !System.getProperty("trace", "no").equals("no");
    private static PrintWriter pw;
    private static int classCount = 0;
    static boolean SKIP_ABSTRACT = !System.getProperty("skipabstract", "no").equals("no");
    static boolean GC = !System.getProperty("gc", "no").equals("no");
    static boolean AUTOFLUSH = !System.getProperty("autoflush", "no").equals("no");
       
    public static void main(String[] args) throws FileNotFoundException {
        HostedVM.initialize();
        
        System.out.println("Classpath: " + PrimordialClassLoader.loader.classpathToString() + "\n");
        pw = new PrintWriter(new FileOutputStream("subclasses.txt"), AUTOFLUSH);
        processPackages();
        pw.close();
    }
    
    private static void processPackages() {
        for(Iterator iter = listPackages(); iter.hasNext();){
            //System.out.println("\t" + iter.next());
            String packageName = (String) iter.next();
            HashSet loaded = new HashSet();
            if(TRACE) System.out.println("Processing package " + packageName + ", " + classCount + " classes loaded so far.");
            
            for(Iterator classIter = PrimordialClassLoader.loader.listPackage(packageName, true); classIter.hasNext();){
                String className = (String) classIter.next();
                String canonicalClassName = canonicalizeClassName(className);
                if (loaded.contains(canonicalClassName)){
                    //if(TRACE) System.err.println("Skipping " + className);
                    continue;
                }
                loaded.add(canonicalClassName);
                try {
                    jq_Class c = (jq_Class) PrimordialClassLoader.loader.getOrCreateBSType(canonicalClassName);
                    if (canonicalClassName.equals("Ljava/lang/Object;")) {
                        Assert._assert(c == PrimordialClassLoader.getJavaLangObject());
                    }
                    c.load();
                    c.prepare();
                    Collection interfaces = new LinkedList();
                    Collection superclasses = new LinkedList();
                    
                    collectSuperclasses(c, interfaces, superclasses);
                    pw.println("CLASS " + c.getName());
                    pw.println("INTERFACES ");
                    for(Iterator iter2 = interfaces.iterator(); iter2.hasNext();){                        
                        pw.println("\t" + iter2.next().toString());
                    }
                    pw.println("SUPERCLASSES ");
                    for(Iterator iter2 = superclasses.iterator(); iter2.hasNext();){
                        pw.println("\t" + iter2.next().toString());
                    }
                    pw.println();
                    
                    //PrimordialClassLoader.loader.unloadBSType(c);
                    classCount++;
                   //if(TRACE) System.out.println("Processing class # " + classCount + ", " + canonicalClassName);
                } catch (NoClassDefFoundError x) {
                    if(TRACE) System.err.println("\tPackage " + packageName + ": Class not found (canonical name " + canonicalClassName + "): "+x);
                    //x.printStackTrace();
                } catch (ClassFormatError cfe) {
                    if(TRACE) System.err.println("\tClass format error occurred while loading class (" + canonicalClassName + "): " + cfe.toString());
                    //cfe.printStackTrace(System.err);
                } catch (LinkageError le) {
                    if(TRACE) System.err.println("\tLinkage error occurred while loading class (" + canonicalClassName + "): " + le.toString());
                    //le.printStackTrace(System.err);
                } catch (RuntimeException e){
                    if(TRACE) System.err.println("\tSecurity error occurred: " + e.getMessage());
                }
            }
            if(GC && (classCount % 17) == 3){
                if(TRACE) System.err.println("GCing...");
                System.gc();
                if(TRACE) System.err.println("Done GCing.");
            }            
        }
        if(TRACE) System.out.println("Done.");
    }

    static void collectSuperclasses(jq_Class c, Collection interfaces, Collection superclasses) {
        do {
            if(!SKIP_ABSTRACT || !c.isAbstract()){
                if(TRACE) System.out.println("Skipping abstract class " + c);
                superclasses.add(c);
            }
            if(c.getInterfaces() != null){
                for(int i = 0; i < c.getInterfaces().length; i++){
                    jq_Class inter = c.getInterfaces()[i];
                    interfaces.add(inter);
                }
            }
            c = c.getSuperclass();
        } while(c != null && c != PrimordialClassLoader.JavaLangObject);
    }

    static Iterator listPackages(){
        Collection result = new LinkedList();
        for(Iterator iter = PrimordialClassLoader.loader.listPackages(); iter.hasNext();){
            String packageName = (String) iter.next();
            if(packageName.equals(".") || packageName.equals("")){
                continue;
            }
            if(packageName.endsWith("javabdd-1.0b2.jar") || packageName.endsWith("jwutil-1.0.jar")){
                continue;
            }            
            if(packageName.endsWith("joeq_core")){
                continue;
            }
            if(packageName.startsWith("joeq") || packageName.startsWith("jwutil")){
                continue;
            }
            
            result.add(packageName);
        }
        
        return result.iterator();
    }
    
    public static String canonicalizeClassName(String s) {
        if (s.endsWith(".class")) s = s.substring(0, s.length() - 6);
        s = s.replace('.', '/');
        String desc = "L" + s + ";";
        return desc;
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
            jq_Type t = ClasspathWalker.load(c);
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
    
    public static void addClassDir(String dir, boolean recurse) {
        List classes = new LinkedList();

        addClassDir_aux(new File(dir), classes, recurse);

        for (Iterator i = classes.iterator(); i.hasNext();) {
            String className = i.next().toString();
            if(TRACE) System.out.println("Adding class " + className);
            PrimordialClassLoader.loader.addToClasspath(className);
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
    
    static private void addClassDir_aux(File f, List results, boolean recurse) {
        if (f.getPath().endsWith(".class")) {
            results.add(f.getPath());
        } else if (f.list() != null && recurse) {
            // directory 
            String[] contents = f.list();
            for (int i = 0; i<contents.length; i++) {
                addClassDir_aux(new File(f.getPath(), contents[i]), results, recurse);
            }
        }
    }

  
}
