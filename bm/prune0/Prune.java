
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import soot.Body;
import soot.Local;
import soot.Main;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.ValueBox;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.util.Chain;
import soot.util.queue.QueueReader;

/*
 * Created on Jul 6, 2004
 *
 * Copyright (C) Joe Kulig, 2004
 * All rights reserved.
 * 
 */


/**
 * @author joe
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class Prune {

  private static boolean VERBOSE=false;
  private static boolean AGGRESSIVE_PRUNE=false;
  private static String[] required;
  private static String appDir;
  private static String libDir;
  
  public static void main(String[] args) throws IOException {
    HashSet cls=new HashSet();
    appDir=System.getProperty("user.dir"); // "."; "/home/joe/w/dhry"; //
    libDir=System.getProperty("lib.dir");
    loadProperties();
    Scene.v().setSootClassPath(appDir + ":" + libDir);
    //    Scene.v().setSootClassPath(libDir);
    SootClass main=Scene.v().loadClassAndSupport(args[0]);
    String[] opts={"-f", "none", "-w", args[0]};
    main.setApplicationClass();
    Scene.v().setMainClass(main);
    Main.main(opts);
    ReachableMethods rm=Scene.v().getReachableMethods();
    System.out.println("Reachable methods: " + rm.size());
    QueueReader q=rm.listener();
    if (AGGRESSIVE_PRUNE) {
      /*
       * todo: Write out new classes with only reachable methods. Then Zip those
       * out.
       */
      // get the classes
      while (q.hasNext()) {
        SootMethod m=(SootMethod) q.next();
        SootClass c=m.getDeclaringClass();
        cls.add(c);
        Chain ifaces=c.getInterfaces();
        Iterator iter=ifaces.iterator();
        while (iter.hasNext()) {
          c=(SootClass) iter.next();
          cls.add(c);
        }
      }
    }
    /*
     * Find the classes used by the reachable methods.
     */
    while (q.hasNext()) {
      SootMethod m=(SootMethod) q.next();
      SootClass c=m.getDeclaringClass();
      cls.add(c);
      List ifaces=getSuperInterfaces(c.getInterfaces());
      Iterator iter=ifaces.iterator();
      while (iter.hasNext()) {
        // add the interfaces
        c=(SootClass) iter.next();
        cls.add(c);
      }
    }
    System.out.println("Reachable classes: " + cls.size());
    /*
     * We are including the other class methods. We have to find the classes
     * used by those other methods.
     */
    Set otherClasses=new HashSet();
    Iterator cIter=cls.iterator();
    while (cIter.hasNext()) {
      SootClass c=(SootClass) cIter.next();
      if (c.isInterface())
        continue;
      c.setApplicationClass();
      /*
       * Iteratre thru the class methods
       */
      Iterator mIter=c.getMethods().iterator();
      while (mIter.hasNext()) {
        SootMethod m=(SootMethod) mIter.next();
        if (m.isAbstract() || m.isNative())
          continue;

        Body b;
        try {
          b=m.retrieveActiveBody();
        } catch (RuntimeException e1) {
          System.err.println(c.getName() + ":" + m.getName() + ":" + e1.getMessage());
          continue;
        }
        /*
         * Get method return type
         */
        Type val=m.getReturnType();
        if (val instanceof RefType) {
          SootClass c0=((RefType) val).getSootClass();
          otherClasses.add(c0);
        }
        /*
         * Iterator thru the local vars
         */
        Iterator varIter=b.getLocals().iterator();
        while (varIter.hasNext()) {
          Local var=(Local) varIter.next();
          if (var instanceof RefType) {
            SootClass c0=((RefType) var).getSootClass();
            // c.setApplicationClass();
            otherClasses.add(c0);
          }
        }
        /*
         * Look for new expressions (fixes the SynchronizedRandomList miss)
         */
        varIter=b.getUseBoxes().iterator();
        while (varIter.hasNext()) {
          ValueBox v=(ValueBox) varIter.next();
          if (v.getValue().getType() instanceof RefType) {
            RefType r=(RefType) v.getValue().getType();
            otherClasses.add(r.getSootClass());
          }
        }
      }
    }
    requiredClasses(otherClasses);
    cls.addAll(otherClasses);
    System.out.println("Classes used: " + cls.size());
    List l=sort(cls);
    ZipOutputStream out=null;
    try {
      out=new ZipOutputStream(new FileOutputStream("metal.zip"));
    } catch (FileNotFoundException e) {
      System.out.println(e.getMessage());
      System.exit(-1);
    }
    out.setLevel(5);
    out.setMethod(ZipOutputStream.DEFLATED);
    /*
     * Need to do some reordering
     */
    LinkedList ll=(LinkedList) l;
    SootClass c=Scene.v().getSootClass("java.lang.Class");
    ll.remove(c);
    ll.add(0, c);
    c=Scene.v().getSootClass("java.lang.Object");
    ll.remove(c);
    ll.add(1, c);
    Iterator iter=l.iterator();
    while (iter.hasNext()) {
      c=(SootClass) iter.next();
      if (VERBOSE)
        System.out.println(c.getName());
      String name=c.getName();
      if (name.startsWith("java.") || name.startsWith("gnu.") || name.startsWith("baremetal.")) {
        String zipName=name.replace('.', '/') + ".class";
        String fileName=libDir + "/" + zipName;
        try {
          RandomAccessFile classFile=new RandomAccessFile(fileName, "r");
          byte[] fileContents=new byte[(int) classFile.length()];
          classFile.readFully(fileContents);
          ZipEntry ze=new ZipEntry(zipName);
          out.putNextEntry(ze);
          out.write(fileContents, 0, fileContents.length);
        } catch (FileNotFoundException e1) {
          System.err.println(e1.getMessage());
        } catch (IOException e1) {
          System.err.println(e1.getMessage());
        }
      }
    }
    out.close();
  }

  private final static void loadProperties() {
    String prune = System.getProperty("prune.file", "prune.properties");
    Properties props=null;
    try {
      FileInputStream in=new FileInputStream(prune);
      props=new Properties();
      props.load(in);
      in.close();
    } catch (IOException e) {
      System.err.println(e.getMessage());
      System.exit(-1);
    }
    /*
     * Get the required classes
     */
    String requiredClasses=props.getProperty("required.classes");
    required = requiredClasses.split("\\s");
    
  }
  private final static List sort(Collection c) {
    Object[] array=c.toArray();
    Comparator comp=new Comparator() {

      public int compare(Object c0, Object c1) {
        String n0=((SootClass) c0).getName();
        String n1=((SootClass) c1).getName();
        return n0.compareTo(n1);
      }
    };
    Arrays.sort(array, comp);

    return new LinkedList(Arrays.asList(array));
  }

  private static void requiredClasses(Collection c) {
    for (int i=0; i < required.length; i++) {
      c.add(Scene.v().loadClassAndSupport(required[i]));
    }
  }

  private static List getSuperInterfaces(Chain c) {
    List list=new ArrayList();
    if (c.isEmpty())
      return list;

    Iterator iter=c.iterator();
    while (iter.hasNext()) {
      SootClass cls=(SootClass) iter.next();
      list.add(cls);
      list.addAll(getSuperInterfaces(cls.getInterfaces()));
    }
    return list;
  }
}