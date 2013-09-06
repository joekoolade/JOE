// PrimordialClassLoader.java, created Mon Feb  5 23:23:20 2001 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Class;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import joeq.ClassLib.ClassLibInterface;
import joeq.Main.jq;
import joeq.UTF.Utf8;
import jwutil.collections.AppendIterator;
import jwutil.collections.Filter;
import jwutil.collections.FilterIterator;
import jwutil.collections.UnmodifiableIterator;
import jwutil.util.Assert;

/**
 * PrimordialClassLoader
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: PrimordialClassLoader.java,v 1.46 2005/02/18 06:07:06 livshits Exp $
 */
public class PrimordialClassLoader extends ClassLoader implements jq_ClassFileConstants {
    
    public static /*final*/ boolean TRACE = false;
    public static final PrintStream out = System.out;
    
    abstract static class ClasspathElement {
        /** Open a stream to read the given resource, or return
         *  <code>null</code> if resource cannot be found. */
        abstract InputStream getResourceAsStream(String resourcename);
        abstract boolean containsResource(String name);
        /** Iterate over all classes in the given package. */
        Iterator listPackage(String packagename) { return listPackage(packagename, false); }
        abstract Iterator listPackage(String packagename, boolean recursive);
        abstract Iterator listPackages();
    }
    /** A .zip or .jar file in the CLASSPATH. */
    static class ZipFileElement extends ClasspathElement {
        ZipFile zf;
        Map entries;
        ZipFileElement(ZipFile zf) {
            this.zf = zf;
        }
        void initializeEntryMap() {
            int size = zf.size();
            entries = new HashMap(size + (size >> 1));
            if (size > 0) {
                for (Enumeration e = zf.entries(); e.hasMoreElements(); ) {
                    ZipEntry ze = (ZipEntry) e.nextElement();
                    entries.put(ze.getName(), ze);
                }
            }
            if (TRACE) out.println(this+" contains: "+entries.keySet());
        }
        public String toString() { return zf.getName(); }
        InputStream getResourceAsStream(String name) {
            if (TRACE) out.println("Getting resource for "+name+" in zip file "+zf.getName());
            if (entries == null) initializeEntryMap();
            if (name.charAt(0) == '/') name = name.substring(1);
            ZipEntry ze = (ZipEntry) entries.get(name);
            try { // look for name in zipfile, return null if something goes wrong.
                return (ze==null)?null:zf.getInputStream(ze);
            } catch (IOException e) { return null; }
        }
        boolean containsResource(String name) {
            if (TRACE) out.println("Searching for "+name+" in zip file "+zf.getName());
            if (entries == null) initializeEntryMap();
            return entries.containsKey(name);
        }
        Iterator listPackage(final String pathname, final boolean recursive) {
            if (TRACE) out.println("Listing package "+pathname+" of zip file "+zf.getName());
            // look for directory name first
            if (entries == null) initializeEntryMap();
            final String filesep   = "/";
            return new FilterIterator(entries.values().iterator(),
            new Filter() {
                public boolean isElement(Object o) {
                    ZipEntry zze = (ZipEntry) o;
                    String name = zze.getName();
                    if (TRACE) out.println("Checking if zipentry "+name+" is in package "+pathname);
                    return (!zze.isDirectory()) && name.startsWith(pathname) &&
                            name.endsWith(".class") &&
                            (recursive || name.lastIndexOf(filesep)==(pathname.length()-1));
                }
                public Object map(Object o) {
                    return ((ZipEntry)o).getName();
                }
            });
        }
        Iterator listPackages() {
            if (TRACE) out.println("Listing packages of zip file "+zf.getName());
            if (entries == null) initializeEntryMap();
            LinkedHashSet result = new LinkedHashSet();
            for (Iterator i=entries.values().iterator(); i.hasNext(); ) {
                ZipEntry zze = (ZipEntry) i.next();
                if (zze.isDirectory()) continue;
                String name = zze.getName();
                if (name.endsWith(".class")) {
                    int index = name.lastIndexOf('/');
                    result.add(name.substring(0, index+1));
                }
            }
            if (TRACE) out.println("Result: "+result);
            return result.iterator();
        }
        /** Close the zipfile when this object is garbage-collected. */
        protected void finalize() throws Throwable {
            // yes, it is possible to finalize an uninitialized object.
            try { if (zf!=null) zf.close(); } finally { super.finalize(); }
        }
    }
    // These should be static so that we don't need to look them up during
    // class loading.
    public static final String pathsep = System.getProperty("path.separator");
    public static final String filesep = System.getProperty("file.separator");
    /** A regular path string in the CLASSPATH. */
    static class PathElement extends ClasspathElement {
        String path;
        Set entries;

        PathElement(String path) {
            this.path = path;
        }
        
        void initializeEntryMap() {
            this.entries = new HashSet();
            buildEntries(null);
            if (TRACE) out.println(this+" contains: "+entries);
        }
        
        public String toString() { return path; }
        
        InputStream getResourceAsStream(String name) {
            if (TRACE) out.println("Getting resource for "+name+" in path "+path);
            if (entries == null) initializeEntryMap();
            if (name.charAt(0) == '/') name = name.substring(1);
            if (!entries.contains(name))
                return null;
            if (filesep.charAt(0) != '/') name = name.replace('/', filesep.charAt(0));
            try { // try to open the file, starting from path.
                File f = new File(path, name);
                return new FileInputStream(f);
            } catch (FileNotFoundException e) {
                return null; // if anything goes wrong, return null.
            }
        }
        
        boolean containsResource(String name) {
            if (TRACE) out.println("Searching for "+name+" in path "+path);
            if (entries == null) initializeEntryMap();
            return entries.contains(name);
        }
        
        Iterator listPackage(final String pathn, final boolean recursive) {
            if (TRACE) out.println("Listing package "+pathn+" in path "+path);
            if (entries == null) initializeEntryMap();
            final String filesep   = "/";
            return new FilterIterator(entries.iterator(),
                new Filter() {
                    public boolean isElement(Object o) {
                        String name = (String) o;
                        if (TRACE) out.println("Checking if file "+name+" is in package "+pathn);
                        return name.startsWith(pathn) &&
                               name.endsWith(".class") &&
                               (recursive || name.lastIndexOf(filesep)==(pathn.length()-1));
                    }
                });
        }

        Iterator listPackages() {
            if (TRACE) out.println("Listing packages of path "+path);
            HashSet hs = new HashSet();
            listPackages(null, hs);
            return hs.iterator();
        }
        
        private void listPackages(final String dir, final HashSet pkgs) {
            final File f = dir == null ? new File(path) : new File(path, dir);
            if (!f.exists() || !f.isDirectory()) return;
            //pkgs.add(path);    // add the current directory first
            String [] subdirs = f.list(new java.io.FilenameFilter() {
                public boolean accept(File _dir, String name) {
                    if (dir != null && name.endsWith(".class"))
                        pkgs.add(dir);
                    return new File(_dir, name).isDirectory();
                }
            });
            for (int i = 0; i < subdirs.length; i++) {
                String dn = (String)subdirs[i];
                if (dir != null)
                    dn = dir + filesep + dn;
                listPackages(dn, pkgs);
            }
        }
        
        private void buildEntries(final String pathn) {
            File f;
            if (pathn == null) {
                f = new File(path);
            } else if (filesep.charAt(0) == '/') {
                f = new File(path, pathn);
            } else {
                f = new File(path, pathn.replace('/', filesep.charAt(0)));
            }
            if (!f.exists() || !f.isDirectory()) return;
            String[] cls = f.list(new java.io.FilenameFilter() {
                    public boolean accept(File _dir, String name) {
                        return !new File(_dir, name).isDirectory();
                    }
                });
            
            if (cls != null) {
                for (int i = 0; i < cls.length; ++i) {
                    String s = (pathn==null)?(cls[i]):(pathn+cls[i]);
                    entries.add(s);
                }
            }

            String [] subdirs = f.list(new java.io.FilenameFilter() {
                    public boolean accept(File _dir, String name) {
                        return new File(_dir, name).isDirectory();
                    }
                });
            if (subdirs != null) {
                for (int i = 0; i < subdirs.length; i++) {
                    String dn = (String)subdirs[i];
                    if (pathn != null) dn = pathn + dn;
                    buildEntries(dn + '/');
                }
            }
        }
    }

    /** Vector of ClasspathElements corresponding to CLASSPATH entries. */
    public void addToClasspath(String s) {
        //Assert._assert(s.indexOf(pathsep) == -1);
        Set duplicates = new HashSet(); // don't add duplicates.
        duplicates.addAll(classpathList);
        for (Iterator it = classpaths(s); it.hasNext(); ) {
            String path = (String) it.next();
            if (duplicates.contains(path)) continue; // skip duplicate.
            else duplicates.add(path);
            if (path.toLowerCase().endsWith(".zip") ||
                path.toLowerCase().endsWith(".jar"))
                try {
                    if (TRACE) out.println("Adding zip file "+path+" to classpath");
                    classpathList.add(new ZipFileElement(new ZipFile(path)));
                } catch (IOException ex) { /* skip this zip file, then. */ }
            else {
                if (TRACE) out.println("Adding path "+path+" to classpath");
                classpathList.add(new PathElement(path));
            }
        }
        ((ArrayList) classpathList).trimToSize(); // save memory.
    }

    /** Iterate over the components of the system CLASSPATH.
     *  Each element is a <code>String</code> naming one segment of the
     *  CLASSPATH. */
    public static final Iterator classpaths(String classpath) {

        // For convenience, make sure classpath begins with and ends with pathsep.
        if (!classpath.startsWith(pathsep)) classpath = pathsep + classpath;
        if (!classpath.endsWith(pathsep)) classpath = classpath + pathsep;
        final String cp = classpath;

        return new UnmodifiableIterator() {
            int i=0;
            public boolean hasNext() {
                return (cp.length() > (i+pathsep.length()));
            }
            public Object next() {
                i+=pathsep.length(); // cp begins with pathsep.
                String path = cp.substring(i, cp.indexOf(pathsep, i));
                i+=path.length(); // skip over path.
                return path;
            }
        };
    }

    public Iterator listPackage(final String pathname) {
        return listPackage(pathname, false);
    }

    public Iterator listPackage(final String pathname, boolean recursive) {
        Iterator result = null;
        for (Iterator it = classpathList.iterator(); it.hasNext(); ) {
            ClasspathElement cpe = (ClasspathElement)it.next();
            Iterator lp = cpe.listPackage(pathname, recursive);
            if (!lp.hasNext()) continue;
            result = result==null?lp:new AppendIterator(lp, result);
        }
        if (result == null) return Collections.EMPTY_SET.iterator();
        return result;
    }
    
    public Iterator listPackages() {
        Iterator result = null;
        for (Iterator it = classpathList.iterator(); it.hasNext(); ) {
            ClasspathElement cpe = (ClasspathElement)it.next();
            Iterator lp = cpe.listPackages();
            if (!lp.hasNext()) continue;
            result = result==null?lp:new AppendIterator(lp, result);
        }
        if (result == null) return Collections.EMPTY_SET.iterator();
        return result;
    }

    public String classpathToString() {
        StringBuffer result = new StringBuffer(pathsep);
        for (Iterator it = classpathList.iterator(); it.hasNext(); ) {
            ClasspathElement cpe = (ClasspathElement) it.next();
            result.append(cpe.toString());
            result.append(pathsep);
        }
        return result.toString();
    }
    
    public static String descriptorToResource(String desc) {
        Assert._assert(desc.charAt(0)==TC_CLASS);
        Assert._assert(desc.charAt(desc.length()-1)==TC_CLASSEND);
        Assert._assert(desc.indexOf('.')==-1); // should have '/' separators.
        return desc.substring(1, desc.length()-1) + ".class";
    }
    
    /** Translate a class name into a corresponding resource name.
     * @param classname The class name to translate.
     */
    public static String classnameToResource(String classname) {
        Assert._assert(classname.indexOf('/')==-1); // should have '.' separators.
        // Swap all '.' for '/' & append ".class"
        return classname.replace('.', filesep.charAt(0)) + ".class";
    }

    public String getResourcePath(String name) {
        for (Iterator it = classpathList.iterator(); it.hasNext(); ) {
            ClasspathElement cpe = (ClasspathElement) it.next();
            if (cpe.containsResource(name))
                return cpe.toString();
        }
        // Couldn't find resource.
        return null;
    }

    public String getPackagePath(String name) {
        for (Iterator it = classpathList.iterator(); it.hasNext(); ) {
            ClasspathElement cpe = (ClasspathElement) it.next();
            for (Iterator it2 = cpe.listPackages(); it2.hasNext(); ) {
                if (name.equals(it2.next()))
                    return cpe.toString();
            }
        }
        // Couldn't find resource.
        return null;
    }

    /** Open an <code>InputStream</code> on a resource found somewhere
     *  in the CLASSPATH.
     * @param name The filename of the resource to locate.
     */
    public InputStream getResourceAsStream(String name) {
        //if (!jq.RunningNative && name.startsWith("java/")) {
        //    // hijack loading of java/* to point to bootstrap versions
        //    char[] c = name.toCharArray();
        //    c[3] = '_';
        //    String name2 = new String(c);
        //    for (Iterator it = classpathList.iterator(); it.hasNext(); ) {
        //        ClasspathElement cpe = (ClasspathElement) it.next();
        //        InputStream is = cpe.getResourceAsStream(name2);
        //        if (is!=null) {
        //            return is; // return stream if found.
        //        }
        //    }
        //}
        for (Iterator it = classpathList.iterator(); it.hasNext(); ) {
            ClasspathElement cpe = (ClasspathElement) it.next();
            InputStream is = cpe.getResourceAsStream(name);
            if (is != null) {
                return is; // return stream if found.
            }
        }
        // Couldn't find resource.
        return null;
    }
    
    private PrimordialClassLoader() {
        bs_desc2type = new HashMap();
        allTypes = new jq_Type[1024]; numTypes = 0;
        classpathList = new ArrayList();
    }
    
    private void put_desc2type(Utf8 desc, jq_Type type) {
        Object result = bs_desc2type.put(desc, type);
        Assert._assert(result == null);
        if (numTypes == allTypes.length) {
            jq_Type[] a = new jq_Type[allTypes.length * 2];
            System.arraycopy(allTypes, 0, a, 0, numTypes);
            allTypes = a;
        }
        allTypes[numTypes++] = type;
    }
    
    private static void initPrimitiveTypes() {
        // trigger jq_Primitive clinit
        loader.getOrCreateBSType(jq_Primitive.BYTE.getDesc());
        loader.put_desc2type(jq_Array.BYTE_ARRAY.getDesc(), jq_Array.BYTE_ARRAY);
        loader.put_desc2type(jq_Array.CHAR_ARRAY.getDesc(), jq_Array.CHAR_ARRAY);
        loader.put_desc2type(jq_Array.DOUBLE_ARRAY.getDesc(), jq_Array.DOUBLE_ARRAY);
        loader.put_desc2type(jq_Array.FLOAT_ARRAY.getDesc(), jq_Array.FLOAT_ARRAY);
        loader.put_desc2type(jq_Array.INT_ARRAY.getDesc(), jq_Array.INT_ARRAY);
        loader.put_desc2type(jq_Array.LONG_ARRAY.getDesc(), jq_Array.LONG_ARRAY);
        loader.put_desc2type(jq_Array.SHORT_ARRAY.getDesc(), jq_Array.SHORT_ARRAY);
        loader.put_desc2type(jq_Array.BOOLEAN_ARRAY.getDesc(), jq_Array.BOOLEAN_ARRAY);
    }
    
    public DataInputStream getClassFileStream(Utf8 descriptor)
    throws IOException {
        String resourceName = descriptorToResource(descriptor.toString());
        InputStream is = getResourceAsStream(resourceName);
        if (is == null) return null;
        return new DataInputStream(is);
    }

    public static final PrimordialClassLoader loader;
    public static final jq_Class JavaLangObject;
    public static final jq_Class JavaLangClass;
    public static final jq_Class JavaLangString;
    public static final jq_Class JavaLangSystem;
    public static final jq_Class JavaLangThrowable;
    public static final jq_Array AddressArray;
    static {
        loader = new PrimordialClassLoader();
        initPrimitiveTypes();
        JavaLangObject = (jq_Class)loader.getOrCreateBSType("Ljava/lang/Object;");
        JavaLangClass = (jq_Class)loader.getOrCreateBSType("Ljava/lang/Class;");
        JavaLangString = (jq_Class)loader.getOrCreateBSType("Ljava/lang/String;");
        JavaLangSystem = (jq_Class)loader.getOrCreateBSType("Ljava/lang/System;");
        JavaLangThrowable = (jq_Class)loader.getOrCreateBSType("Ljava/lang/Throwable;");
        AddressArray = (jq_Array)loader.getOrCreateBSType("[Ljoeq/Memory/Address;");
    }
    
    public static jq_Class getJavaLangObject() { return JavaLangObject; }
    public static jq_Class getJavaLangClass() { return JavaLangClass; }
    public static jq_Class getJavaLangString() { return JavaLangString; }
    public static jq_Class getJavaLangSystem() { return JavaLangSystem; }
    public static jq_Class getJavaLangThrowable() { return JavaLangThrowable; }
    public static jq_Array getAddressArray() { return AddressArray; }
    public static jq_Class getJavaLangException() { return (jq_Class)loader.getOrCreateBSType("Ljava/lang/Exception;"); }
    public static jq_Class getJavaLangError() { return (jq_Class)loader.getOrCreateBSType("Ljava/lang/Error;"); }
    public static jq_Class getJavaLangRuntimeException() { return (jq_Class)loader.getOrCreateBSType("Ljava/lang/RuntimeException;"); }
    public static jq_Class getJavaLangNullPointerException() { return (jq_Class)loader.getOrCreateBSType("Ljava/lang/NullPointerException;"); }
    public static jq_Class getJavaLangIndexOutOfBoundsException() { return (jq_Class)loader.getOrCreateBSType("Ljava/lang/IndexOutOfBoundsException;"); }
    public static jq_Class getJavaLangArrayIndexOutOfBoundsException() { return (jq_Class)loader.getOrCreateBSType("Ljava/lang/ArrayIndexOutOfBoundsException;"); }
    public static jq_Class getJavaLangArrayStoreException() { return (jq_Class)loader.getOrCreateBSType("Ljava/lang/ArrayStoreException;"); }
    public static jq_Class getJavaLangNegativeArraySizeException() { return (jq_Class)loader.getOrCreateBSType("Ljava/lang/NegativeArraySizeException;"); }
    public static jq_Class getJavaLangArithmeticException() { return (jq_Class)loader.getOrCreateBSType("Ljava/lang/ArithmeticException;"); }
    public static jq_Class getJavaLangIllegalMonitorStateException() { return (jq_Class)loader.getOrCreateBSType("Ljava/lang/IllegalMonitorStateException;"); }
    public static jq_Class getJavaLangClassCastException() { return (jq_Class)loader.getOrCreateBSType("Ljava/lang/ClassCastException;"); }
    public static jq_Class getJavaLangClassLoader() { return (jq_Class)loader.getOrCreateBSType("Ljava/lang/ClassLoader;"); }
    public static jq_Class getJavaLangReflectField() { return (jq_Class)loader.getOrCreateBSType("Ljava/lang/reflect/Field;"); }
    public static jq_Class getJavaLangReflectMethod() { return (jq_Class)loader.getOrCreateBSType("Ljava/lang/reflect/Method;"); }
    public static jq_Class getJavaLangReflectConstructor() { return (jq_Class)loader.getOrCreateBSType("Ljava/lang/reflect/Constructor;"); }
    public static jq_Class getJavaLangThread() { return (jq_Class)loader.getOrCreateBSType("Ljava/lang/Thread;"); }
    public static jq_Class getJavaLangRefFinalizer() { return (jq_Class)loader.getOrCreateBSType("Ljava/lang/ref/Finalizer;"); }
    private final Map/*<Utf8, jq_Type>*/ bs_desc2type;
    private jq_Type[] allTypes; private int numTypes;
    private final List/*<ClasspathElement>*/ classpathList;

    public jq_Type[] getAllTypes() {
        return allTypes;
    }
    
    public int getNumTypes() {
        return numTypes;
    }
    
    public final Set/*<jq_Class>*/ getClassesThatReference(jq_Member m) {
        HashSet s = new HashSet();
        for (int i = 0; i < numTypes; ++i) {
            jq_Type t = allTypes[i];
            if (t instanceof jq_Class) {
                jq_Class k = (jq_Class) t;
                if (k.doesConstantPoolContain(m))
                    s.add(k);
            }
        }
        return s;
    }
    
    public final jq_Class getOrCreateClass(String desc, DataInput in) {
        jq_Class t = (jq_Class)getOrCreateBSType(Utf8.get(desc));
        t.load(in);
        return t;
    }

    public final jq_Type getBSType(String desc) { return getBSType(Utf8.get(desc)); }
    public final jq_Type getBSType(Utf8 desc) {
        return (jq_Type)bs_desc2type.get(desc);
    }
    public final jq_Type getOrCreateBSType(String desc) { return getOrCreateBSType(Utf8.get(desc)); }
    public final jq_Type getOrCreateBSType(Utf8 desc) {
        if (jq.RunningNative)
            return ClassLibInterface.DEFAULT.getOrCreateType(this, desc);
        jq_Type t = (jq_Type)bs_desc2type.get(desc);
        if (t == null) {
            if (desc.isDescriptor(jq_ClassFileConstants.TC_CLASS)) {
                // as a side effect, the class type is registered.
                if (TRACE) out.println("Adding class type "+desc);
                t = jq_Class.newClass(this, desc);
            } else if (desc.isDescriptor(jq_ClassFileConstants.TC_ARRAY)) {
                if (TRACE) out.println("Adding array type "+desc);
                Utf8 elementDesc = desc.getArrayElementDescriptor();
                jq_Type elementType = getOrCreateBSType(elementDesc); // recursion
                // as a side effect, the array type is registered.
                t = jq_Array.newArray(desc, this, elementType);
            } else {
                // this code only gets executed at the very beginning, when creating primitive types.
                if (desc == Utf8.BYTE_DESC)
                    t = jq_Primitive.newPrimitive(desc, "byte", 1);
                else if (desc == Utf8.CHAR_DESC)
                    t = jq_Primitive.newPrimitive(desc, "char", 2);
                else if (desc == Utf8.DOUBLE_DESC)
                    t = jq_Primitive.newPrimitive(desc, "double", 8);
                else if (desc == Utf8.FLOAT_DESC)
                    t = jq_Primitive.newPrimitive(desc, "float", 4);
                else if (desc == Utf8.INT_DESC)
                    t = jq_Primitive.newPrimitive(desc, "int", 4);
                else if (desc == Utf8.LONG_DESC)
                    t = jq_Primitive.newPrimitive(desc, "long", 8);
                else if (desc == Utf8.SHORT_DESC)
                    t = jq_Primitive.newPrimitive(desc, "short", 2);
                else if (desc == Utf8.BOOLEAN_DESC)
                    t = jq_Primitive.newPrimitive(desc, "boolean", 1);
                else if (desc == Utf8.VOID_DESC)
                    t = jq_Primitive.newPrimitive(desc, "void", 0);
                /*
                else if (desc == jq_Array.BYTE_ARRAY.getDesc()) return jq_Array.BYTE_ARRAY;
                else if (desc == jq_Array.CHAR_ARRAY.getDesc()) return jq_Array.CHAR_ARRAY;
                else if (desc == jq_Array.DOUBLE_ARRAY.getDesc()) return jq_Array.DOUBLE_ARRAY;
                else if (desc == jq_Array.FLOAT_ARRAY.getDesc()) return jq_Array.FLOAT_ARRAY;
                else if (desc == jq_Array.INT_ARRAY.getDesc()) return jq_Array.INT_ARRAY;
                else if (desc == jq_Array.LONG_ARRAY.getDesc()) return jq_Array.LONG_ARRAY;
                else if (desc == jq_Array.SHORT_ARRAY.getDesc()) return jq_Array.SHORT_ARRAY;
                else if (desc == jq_Array.BOOLEAN_ARRAY.getDesc()) return jq_Array.BOOLEAN_ARRAY;
                 */
                else Assert.UNREACHABLE("bad descriptor! "+desc);
            }
            put_desc2type(desc, t);
        }
        return t;
    }
    
    /*
     * @param cName a string, not a descriptor.
     * @author Chrislain Razafimahefa <razafima@cui.unige.ch>
     */
    public final void replaceClass(String cName)
    {
        Utf8 oldDesc = Utf8.get("L"+cName.replace('.', '/')+";") ;
        jq_Type old = PrimordialClassLoader.getOrCreateType(this, oldDesc);
        Assert._assert(old != null);
        Assert._assert(oldDesc.isDescriptor(jq_ClassFileConstants.TC_CLASS));

        // now load 'new' with a fake name
        Utf8 newDesc = Utf8.get("LREPLACE"+cName.replace('.', '/')+";") ;
        jq_Class new_c = jq_Class.newClass(this, newDesc);
        put_desc2type(newDesc, new_c);

        // take inputstream on OLD class, but load in NEW class.
        DataInputStream in = null;
        try {
            in = getClassFileStream(oldDesc);
            if (in == null) throw new NoClassDefFoundError(jq_Class.className(oldDesc));
            new_c.load(in); // will generate the replacement
        } catch (IOException x) {
            x.printStackTrace(); // for debugging
            throw new ClassFormatError(x.toString());
        } finally {
            try { if (in != null) in.close(); } catch (IOException _) { }
        }
    }
    
    public void unloadBSType(jq_Type t) {
        bs_desc2type.remove(t.getDesc());
        for (int i = 0; ; ++i) {
            if (allTypes[i] == t) {
                numTypes--;
                System.arraycopy(allTypes, i+1, allTypes, i, numTypes - i);
                allTypes[numTypes] = null;
                break;
            }
        }
    }
    
    public static final jq_Type getOrCreateType(ClassLoader cl, Utf8 desc) {
        if (jq.RunningNative)
            return ClassLibInterface.DEFAULT.getOrCreateType(cl, desc);
        Assert._assert(cl == PrimordialClassLoader.loader);
        return PrimordialClassLoader.loader.getOrCreateBSType(desc);
    }
    
    public static final void unloadType(ClassLoader cl, jq_Type t) {
        if (jq.RunningNative) {
            ClassLibInterface.DEFAULT.unloadType(cl, t);
            return;
        }
        Assert._assert(cl == PrimordialClassLoader.loader);
        PrimordialClassLoader.loader.unloadBSType(t);
    }
}
