// System.java, created Fri Aug 16 18:11:48 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.sun14_linux.java.lang;

import joeq.Class.PrimordialClassLoader;

/**
 * System
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: System.java,v 1.8 2004/03/09 21:57:36 jwhaley Exp $
 */
public abstract class System {
    
    private static java.util.Properties props;
    private static native void setIn0(java.io.InputStream in);
    private static native void setOut0(java.io.PrintStream out);
    private static native void setErr0(java.io.PrintStream err);

    public static java.lang.String mapLibraryName(java.lang.String libname) {
        return libname; // TODO.
    }

    /***
    public static void initializeSystemClass() {
        props = new java.util.Properties();
        initProperties(props);
        sun.misc.Version.init();
        java.io.FileInputStream fdIn = new java.io.FileInputStream(java.io.FileDescriptor.in);
        java.io.FileOutputStream fdOut = new java.io.FileOutputStream(java.io.FileDescriptor.out);
        java.io.FileOutputStream fdErr = new java.io.FileOutputStream(java.io.FileDescriptor.err);
        setIn0(new java.io.BufferedInputStream(fdIn));
        setOut0(new java.io.PrintStream(new java.io.BufferedOutputStream(fdOut, 128), true));
        setErr0(new java.io.PrintStream(new java.io.BufferedOutputStream(fdErr, 128), true));

        //try {
        //    java.util.logging.LogManager.getLogManager().readConfiguration();
        //} catch (java.lang.Exception ex) {
        //}

        //loadLibrary("zip");

        //sun.misc.VM.booted();
    }
    ***/
    public static void loadLibrary(String libname) {
        if (libname.equals("zip")) return;
        Runtime.getRuntime().loadLibrary0(getCallerClass(), libname);
    }
    static native Class getCallerClass();

    private static java.util.Properties initProperties(java.util.Properties props) {
        // TODO: read these properties from environment.
        props.setProperty("java.class.version", "48.0");
        props.setProperty("java.home", "/usr/java/j2sdk1.4.0_01/jre");
        props.setProperty("java.runtime.name", "Java(TM) 2 Runtime Environment, Standard Edition");
        props.setProperty("java.runtime.version", "1.4.0_01-b03");
        props.setProperty("java.specification.name", "Java Platform API Specification");
        props.setProperty("java.specification.vendor", "Sun Microsystems, Inc.");
        props.setProperty("java.specification.version", "1.4");
        props.setProperty("java.vendor", "joeq");
        props.setProperty("java.vendor.url", "http://joeq.sourceforge.net");
        props.setProperty("java.vendor.url.bug", "http://joeq.sourceforge.net");
        props.setProperty("java.version", "1.4.0_01");
        props.setProperty("java.vm.name", "joeq virtual machine");
        props.setProperty("java.vm.specification.name", "Java Virtual Machine Specification");
        props.setProperty("java.vm.specification.vendor", "Sun Microsystems, Inc.");
        props.setProperty("java.vm.specification.version", "1.0");
        props.setProperty("java.vm.vendor", "joeq");
        props.setProperty("java.vm.version", "1.4.0_01-b03");
        props.setProperty("java.util.prefs.PreferencesFactory", "java.util.prefs.FileSystemPreferencesFactory");
        
        props.setProperty("os.arch", "i386");
        props.setProperty("os.name", "Linux");
        props.setProperty("os.version", "2.4.9-31smp");
        
        props.setProperty("file.encoding", "ISO-8859-1");
        props.setProperty("file.encoding.pkg", "sun.io");
        props.setProperty("file.separator", "/");
        
        props.setProperty("line.separator", "\n");
        
        props.setProperty("path.separator", ":");
        
        props.setProperty("user.country", "US");
        props.setProperty("user.dir", "/u/jwhaley/joeq");
        props.setProperty("user.home", "/u/jwhaley");
        props.setProperty("user.language", "en");
        props.setProperty("user.name", "jwhaley");
        props.setProperty("user.timezone", "");

        // must be at end: classpathToString() uses some properties from above.
        props.setProperty("java.class.path", PrimordialClassLoader.loader.classpathToString());

        return props;
    }
    
}
