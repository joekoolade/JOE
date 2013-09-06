// ClassLibInterface.java, created Fri Jan 11 17:11:52 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib;

import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Class;
import joeq.Class.jq_InstanceField;
import joeq.Class.jq_InstanceMethod;
import joeq.Class.jq_Member;
import joeq.Class.jq_NameAndDesc;
import joeq.Class.jq_Reference;
import joeq.Class.jq_StaticField;
import joeq.Class.jq_StaticMethod;
import joeq.Main.jq;
import joeq.Runtime.Debug;
import joeq.UTF.Utf8;
import jwutil.util.Assert;

/**
 * ClassLibInterface
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: ClassLibInterface.java,v 1.41 2004/09/22 22:17:44 joewhaley Exp $
 */
public abstract class ClassLibInterface {

    public static boolean USE_JOEQ_CLASSLIB;
    public static final void useJoeqClasslib(boolean b) { USE_JOEQ_CLASSLIB = b; }
    
    public static final joeq.ClassLib.Common.Interface DEFAULT;

    /* Try the three current possibilities for the ClassLibInterface.
       This would probably be rather more general with some kind of
       iterator, but it does for now. */
    static {
        joeq.ClassLib.Common.Interface f = null;
        String classlibinterface = System.getProperty("joeq.classlibinterface");
        boolean nullVM = jq.nullVM;

        if (classlibinterface != null) {
            f = attemptClassLibInterface(classlibinterface);
        }
        if (nullVM) {
            f = new joeq.ClassLib.Common.NullInterfaceImpl();
        }
        if (f == null) {
            String classlibrary = System.getProperty("classlibrary");
            if (classlibrary == null) {
                //String javaversion = System.getProperty("java.version");
                //String javavmversion = System.getProperty("java.vm.version");
                String javavmvendor = System.getProperty("java.vm.vendor");
                String javaruntimeversion = System.getProperty("java.runtime.version");
                String osarch = System.getProperty("os.arch");
                String osname = System.getProperty("os.name");

                if (osarch.equals("x86")) {
                } else if (osarch.equals("i386")) {
                } else {
                    //System.err.println("Warning: architecture "+osarch+" is not yet supported.");
                }
                if (javavmvendor.equals("Sun Microsystems Inc.")) {
                    if (javaruntimeversion.equals("1.3.1_01")) {
                        classlibrary = "sun13_";
                    } else if (javaruntimeversion.equals("1.4.0-b92")) {
                        classlibrary = "sun14_";
                    } else if (javaruntimeversion.startsWith("1.4.2")) {
                        classlibrary = "sun142_";
                    } else if (javaruntimeversion.startsWith("1.5.0")) {
                        classlibrary = "sun15_";
                    } else {
                        if (javaruntimeversion.startsWith("1.5")) {
                            classlibrary = "sun15_";
                        } else if (javaruntimeversion.startsWith("1.4")) {
                            classlibrary = "sun14_";
                        } else {
                            classlibrary = "sun13_";
                        }
                        System.err.println("Warning: class library version "+javaruntimeversion+" is not yet supported, trying default "+classlibrary);
                    }
                } else if (javavmvendor.equals("IBM Corporation")) {
                    if (javaruntimeversion.equals("1.3.0")) {
                        classlibrary = "ibm13_";
                    } else {
                        classlibrary = "ibm13_";
                        System.err.println("Warning: class library version "+javaruntimeversion+" is not yet supported, trying default "+classlibrary);
                    }
                } else if (javavmvendor.equals("Apple Computer, Inc.")) {
                    if (javaruntimeversion.equals("1.3.1")) {
                        classlibrary = "apple13_";
                    } else {
                        classlibrary = "apple13_";
                        System.err.println("Warning: class library version "+javaruntimeversion+" is not yet supported, trying default "+classlibrary);
                    }
                } else {
                    classlibrary = "sun13_";
                    System.err.println("Warning: vm vendor "+javavmvendor+" is not yet supported, trying default "+classlibrary);
                }
                if (osname.startsWith("Windows")) {
                    classlibrary += "win32";
                } else if (osname.equals("Linux")) {
                    classlibrary += "linux";
                } else if (osname.equals("Mac OS X")) {
                    classlibrary += "osx";
                } else {
                    classlibrary += "win32";
                    System.err.println("Warning: OS "+osname+" is not yet supported, trying "+classlibrary);
                }
            }
            f = attemptClassLibInterface("joeq.ClassLib."+classlibrary+".Interface");
        }
        if (f == null) {
            f = new joeq.ClassLib.Common.NullInterfaceImpl();
        }
        
        DEFAULT = f;
    }

    private static joeq.ClassLib.Common.Interface attemptClassLibInterface(String s) {
        try {
            Class c = Class.forName(s);
            return (joeq.ClassLib.Common.Interface)c.newInstance();
        } catch (java.lang.ClassNotFoundException x) {
            if (jq.IsBootstrapping) {
                System.err.println("Cannot find class library interface "+s+": "+x);
                System.err.println("Please check the version of your virtual machine.");
            }
        } catch (java.lang.InstantiationException x) {
            System.err.println("Cannot instantiate class library interface "+s+": "+x);
        } catch (java.lang.IllegalAccessException x) {
            System.err.println("Cannot access class library interface "+s+": "+x);
        }
        return null;
    }

    public static final jq_Class _class = (jq_Class)PrimordialClassLoader.loader.getOrCreateBSType("Ljoeq/ClassLib/ClassLibInterface;");
    
    public static /*final*/ boolean TRACE = false;
    
    // utility functions
    public static jq_NameAndDesc convertClassLibNameAndDesc(jq_Class k, jq_NameAndDesc t) {
        Utf8 d = convertClassLibDesc(t.getDesc());
        Utf8 n = t.getName();
        if (k.getDesc().toString().endsWith("/java/lang/Object;")) {
            // trim initial "_", if it exists.
            String s = n.toString();
            if (s.charAt(0) == '_') {
                n = Utf8.get(s.substring(1));
                if (TRACE) Debug.writeln("special case for java.lang.Object: "+n+" "+d);
                return new jq_NameAndDesc(n, d);
            }
        }
        if (d == t.getDesc())
            return t;
        else
            return new jq_NameAndDesc(n, d);
    }
    
    public static Utf8 convertClassLibDesc(Utf8 desc) {
        return Utf8.get(convertClassLibDesc(desc.toString()));
    }
    
    public static String convertClassLibDesc(String desc) {
        int i = desc.indexOf("joeq/ClassLib/");
        if (i != -1) {
            for (;;) {
                int m = desc.indexOf(';', i+15);
                if (m == -1) break;
                int j = desc.indexOf('/', i+15);
                if (j == -1 || j > m) break;
                int k = desc.indexOf(';', j);
                String t = desc.substring(j+1, k).replace('/','.');
                try {
                    Class.forName(t, false, ClassLibInterface.class.getClassLoader());
                    desc = desc.substring(0, i) + desc.substring(j+1);
                } catch (ClassNotFoundException x) {
                }
                i = desc.indexOf("joeq/ClassLib/", i+1);
                if (i == -1) break;
            }
        }
        return desc;
    }
    
    public static jq_Member convertClassLibCPEntry(jq_Member t) {
        jq_NameAndDesc u1 = convertClassLibNameAndDesc(t.getDeclaringClass(), t.getNameAndDesc());
        Utf8 u2 = convertClassLibDesc(t.getDeclaringClass().getDesc());
        if (u1 == t.getNameAndDesc() && u2 == t.getDeclaringClass().getDesc())
            return t;
        else {
            jq_Class c;
            if (u2 != t.getDeclaringClass().getDesc())
                c = (jq_Class)PrimordialClassLoader.getOrCreateType(t.getDeclaringClass().getClassLoader(), u2);
            else
                c = t.getDeclaringClass();
            if (t instanceof jq_InstanceField) {
                return c.getOrCreateInstanceField(u1);
            } else if (t instanceof jq_StaticField) {
                return c.getOrCreateStaticField(u1);
            } else if (t instanceof jq_InstanceMethod) {
                return c.getOrCreateInstanceMethod(u1);
            } else if (t instanceof jq_StaticMethod) {
                return c.getOrCreateStaticMethod(u1);
            } else {
                Assert.UNREACHABLE(); return null;
            }
        }
    }
    
    public static jq_Reference convertClassLibCPEntry(jq_Reference t) {
        Utf8 u = convertClassLibDesc(t.getDesc());
        if (u == t.getDesc())
            return t;
        else
            return (jq_Reference)PrimordialClassLoader.getOrCreateType(t.getClassLoader(), u);
    }
    
    public static void init_zipfile_static(java.util.zip.ZipFile zf, java.lang.String s) {
        try {
            ClassLibInterface.DEFAULT.init_zipfile(zf, s);
        } catch (java.io.IOException x) {
            System.err.println("Note: cannot reopen zip file "+s);
            try {
                zf.close();
            } catch (java.io.IOException y) {}
        }
    }
    
    public static void init_inflater_static(java.util.zip.Inflater i, boolean nowrap)
        throws java.io.IOException {
        ClassLibInterface.DEFAULT.init_inflater(i, nowrap);
    }
}
