// ClassLoader.java, created Thu Jul  4  4:50:03 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.Common.java.lang;

import java.util.HashMap;
import java.util.Map;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.security.ProtectionDomain;
import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Array;
import joeq.Class.jq_Class;
import joeq.Class.jq_ClassFileConstants;
import joeq.Class.jq_CompiledCode;
import joeq.Class.jq_Reference;
import joeq.Class.jq_Type;
import joeq.Main.jq;
import joeq.Memory.StackAddress;
import joeq.Runtime.Reflection;
import joeq.Runtime.StackCodeWalker;
import joeq.UTF.Utf8;
import jwutil.util.Assert;

/**
 * ClassLoader
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: ClassLoader.java,v 1.19 2004/09/30 03:35:32 joewhaley Exp $
 */
public abstract class ClassLoader {

    private boolean initialized;
    private java.lang.ClassLoader parent;
    private static ClassLoader scl;

    // additional instance field
    private final Map/*<Utf8, jq_Type>*/ desc2type;

    // overridden instance method
    void addClass(java.lang.Class c) {}

    // overridden constructors
    protected ClassLoader(java.lang.ClassLoader parent) {
        java.lang.SecurityManager security = java.lang.System.getSecurityManager();
        if (security != null) {
            security.checkCreateClassLoader();
        }
        Assert._assert(parent != null);
        this.parent = parent;
        Map m = new HashMap();
        this.desc2type = m;
        this.initialized = true;
    }
    protected ClassLoader() {
        java.lang.SecurityManager security = java.lang.System.getSecurityManager();
        if (security != null) {
            security.checkCreateClassLoader();
        }
        java.lang.ClassLoader parent = getSystemClassLoader();
        Assert._assert(parent != null);
        this.parent = parent;
        Map m = new HashMap();
        this.desc2type = m;
        this.initialized = true;
    }

    native boolean isAncestor(ClassLoader cl);
    static native RuntimePermission getGetClassLoaderPerm();
    public native Class loadClass(java.lang.String name);

    // overridden methods.
    public static java.lang.ClassLoader getSystemClassLoader() {
        java.lang.Object o = PrimordialClassLoader.loader;
        scl = (ClassLoader)o;
        if (scl == null) {
            return null;
        }
        java.lang.SecurityManager sm = java.lang.System.getSecurityManager();
        if (sm != null) {
            ClassLoader ccl = getCallerClassLoader();
            if (ccl != null && ccl != scl) {
                try {
                    if (!scl.isAncestor(ccl)) {
                        sm.checkPermission(getGetClassLoaderPerm());
                    }
                } catch (java.lang.Error x) {
                    throw x;
                } catch (java.lang.Throwable x) {
                    Assert.UNREACHABLE();
                }
            }
        }
        o = scl;
        return (java.lang.ClassLoader)o;
    }

    // native method implementations.
    public java.lang.Class defineClass0(java.lang.String name, byte[] b, int off, int len,
                                         ProtectionDomain pd) {
        // define a new class based on given name and class file structure
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(b, off, len));
        // TODO: what should we do about protection domain???  ignore it???
        if (name == null) throw new java.lang.ClassFormatError("name cannot be null when defining class");
        if (name.startsWith("[")) throw new java.lang.ClassFormatError("cannot define array class with defineClass: "+name);
        Utf8 desc = Utf8.get("L"+name.replace('.','/')+";");
        if (this.getType(desc) != null)
            throw new java.lang.ClassFormatError("class "+name+" already defined");
        java.lang.Object o = this;
        jq_Class c = jq_Class.newClass((java.lang.ClassLoader)o, desc);
        Map desc2type = this.desc2type;
        desc2type.put(desc, c);
        c.load(in);
        //in.close();
        return Reflection.getJDKType(c);
    }
    private void resolveClass0(Class c) {
        jq_Type t = c.jq_type;
        t.load(); t.verify(); t.prepare();
    }
    private java.lang.Class findBootstrapClass(java.lang.String name) throws java.lang.ClassNotFoundException {
        java.lang.Object o = PrimordialClassLoader.loader;
        Assert._assert(this == o);
        if (!name.startsWith("[")) name = "L"+name+";";
        Utf8 desc = Utf8.get(name.replace('.','/'));
        jq_Type k;
        k = this.getOrCreateType(desc);
        try {
            k.load();
        } catch (java.lang.ClassFormatError x) {
            //this.unloadType(k); // ??? should we unload?
            throw x;
        } catch (java.lang.NoClassDefFoundError x) {
            this.unloadType(k);
            throw new java.lang.ClassNotFoundException(name);
        }
        return Reflection.getJDKType(k);
    }
    protected final java.lang.Class findLoadedClass(java.lang.String name) {
        if (!name.startsWith("[")) name = "L"+name+";";
        Utf8 desc = Utf8.get(name.replace('.','/'));
        jq_Reference t = (jq_Reference) this.getType(desc);
        if (t == null) return null;
        // avoid recursive loading, because loading can use "Class.forName()"
        if (t.getState() == jq_ClassFileConstants.STATE_UNLOADED)
            t.load();
        return Reflection.getJDKType(t);
    }
    static ClassLoader getCallerClassLoader() {
        StackCodeWalker sw = new StackCodeWalker(null, StackAddress.getBasePointer());
        sw.gotoNext(); sw.gotoNext(); sw.gotoNext();
        jq_CompiledCode cc = sw.getCode();
        if (cc == null) return null;
        java.lang.Object o = cc.getMethod().getDeclaringClass().getClassLoader();
        return (ClassLoader)o;
    }

    // additional methods
    public jq_Type getType(Utf8 desc) {
        Assert._assert(jq.RunningNative);
        Map desc2type = this.desc2type;
        jq_Type t = (jq_Type)desc2type.get(desc);
        return t;
    }
    public static jq_Type getOrCreateType(java.lang.ClassLoader loader, Utf8 desc) {
        java.lang.Object o = loader;
        return ((ClassLoader)o).getOrCreateType(desc);
    }
    public jq_Type getOrCreateType(Utf8 desc) {
        if (!jq.RunningNative)
            return PrimordialClassLoader.loader.getOrCreateBSType(desc);
        Map desc2type = this.desc2type;
        jq_Type t = (jq_Type)desc2type.get(desc);
        if (t == null) {
            if (desc.isDescriptor(jq_ClassFileConstants.TC_CLASS)) {
                java.lang.Object o = this;
                t = jq_Class.newClass((java.lang.ClassLoader)o, desc);
            } else {
                if (!desc.isDescriptor(jq_ClassFileConstants.TC_ARRAY))
                    Assert.UNREACHABLE("bad descriptor! "+desc);
                Utf8 elementDesc = desc.getArrayElementDescriptor();
                jq_Type elementType;
                elementType = this.getOrCreateType(elementDesc); // recursion
                java.lang.Object o = this;
                t = jq_Array.newArray(desc, (java.lang.ClassLoader)o, elementType);
            }
            desc2type.put(desc, t);
        }
        return t;
    }
    public void unloadType(jq_Type t) {
        if (!jq.RunningNative) {
            PrimordialClassLoader.loader.unloadBSType(t);
            return;
        }
        Map desc2type = this.desc2type;
        desc2type.remove(t.getDesc());
    }

}
