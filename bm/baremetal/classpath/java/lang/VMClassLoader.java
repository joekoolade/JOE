/* VMClassLoader.java -- Reference implementation of native interface
 required by ClassLoader
 Copyright (C) 1998, 2001, 2002, 2003, 2004 Free Software Foundation

 This file is part of GNU Classpath.

 GNU Classpath is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2, or (at your option)
 any later version.

 GNU Classpath is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with GNU Classpath; see the file COPYING.  If not, write to the
 Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 02111-1307 USA.

 Linking this library statically or dynamically with other modules is
 making a combined work based on this library.  Thus, the terms and
 conditions of the GNU General Public License cover the whole
 combination.

 As a special exception, the copyright holders of this library give you
 permission to link this library with independent modules to produce an
 executable, regardless of the license terms of these independent
 modules, and to copy and distribute the resulting executable under
 terms of your choice, provided that you also meet, for each linked
 independent module, the terms and conditions of the license of that
 module.  An independent module is a module which is not derived from
 or based on this library.  If you modify this library, you may extend
 this exception to your version of the library, but you are not
 obligated to do so.  If you do not wish to do so, delete this
 exception statement from your version. */

package java.lang;

import gnu.java.util.EmptyEnumeration;

import java.io.IOException;
import java.net.URL;
import java.security.Permission;
import java.security.ProtectionDomain;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * java.lang.VMClassLoader is a package-private helper for VMs to implement on
 * behalf of java.lang.ClassLoader.
 * 
 * @author John Keiser
 * @author Mark Wielaard <mark@klomp.org>
 * @author Eric Blake <ebb9@email.byu.edu>
 */
final class VMClassLoader {

  // Protection Domain definitions
  // FIXME: should there be a special protection domain used for native code?

  // The permission required to check what a classes protection domain is.
  static final Permission protectionDomainPermission=null;
  // The protection domain returned if we cannot determine it.
  static ProtectionDomain unknownProtectionDomain;

/*
 *   static {
    Permissions permissions=new Permissions();
    permissions.add(new AllPermission());
    unknownProtectionDomain=new ProtectionDomain(null, permissions);
  }
*/
  /**
   * Helper to define a class using a string of bytes. This assumes that the
   * security checks have already been performed, if necessary.
   * 
   * <strong>For backward compatibility, this just ignores the protection
   * domain; that is the wrong behavior, and you should directly implement this
   * method natively if you can. </strong>
   * 
   * @param name
   *          the name to give the class, or null if unknown
   * @param data
   *          the data representing the classfile, in classfile format
   * @param offset
   *          the offset into the data where the classfile starts
   * @param len
   *          the length of the classfile data in the array
   * @param pd
   *          the protection domain
   * @return the class that was defined
   * @throws ClassFormatError
   *           if data is not in proper classfile format
   */
  static final Class defineClass(ClassLoader cl, String name, byte[] data, int offset, int len,
      ProtectionDomain pd) throws ClassFormatError {
    throw new RuntimeException("VMClassLoader.defineClass");
  }

  static final void linkClass0(Class klass) {
    throw new RuntimeException("vmclassloader.linkClass0");
  }

  static final void markClassErrorState0(Class klass) {
    throw new RuntimeException("vmclassloader.markClassErrorState0");
  }

  /**
   * Helper to resolve all references to other classes from this class.
   * 
   * @param c
   *          the class to resolve
   */
  static final void resolveClass(Class clazz) {
    synchronized (clazz) {
      try {
        linkClass0(clazz);
      } catch (Throwable x) {
        markClassErrorState0(clazz);

        LinkageError e;
        if (x instanceof LinkageError)
          e=(LinkageError) x;
        else if (x instanceof ClassNotFoundException) {
          e=new NoClassDefFoundError("while resolving class: " + clazz.getName());
          e.initCause(x);
        } else {
          e=new LinkageError("unexpected exception during linking: " + clazz.getName());
          e.initCause(x);
        }
        throw e;
      }
    }
  }

  /**
   * FIXME: 
   * Helper to load a class from the bootstrap class loader.
   * 
   * @param name
   *          the class name to load
   * @param resolve
   *          whether to resolve it
   * @return the class, loaded by the bootstrap classloader or null if the class
   *         wasn't found. Returning null is equivalent to throwing a
   *         ClassNotFoundException (but a possible performance optimization).
   */
  static final Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
    throw new RuntimeException("vmclassloader.loadClass");
  }

  /**
   * Helper to load a resource from the bootstrap class loader.
   * 
   * In libgcj, this does nothing, as the default system loader knows how to
   * find resources that have been linked in.
   * 
   * @param name
   *          the resource to find
   * @return the URL to the resource
   */
  
  // FIXME: 
  static URL getResource(String name) {
    return null;
  }

  /**
   * Helper to get a list of resources from the bootstrap class loader.
   * 
   * In libgcj, this does nothing, as the default system loader knows how to
   * find resources that have been linked in.
   * 
   * @param name
   *          the resource to find
   * @return an enumeration of resources
   * @throws IOException
   *           if one occurs
   */
  static Enumeration getResources(String name) throws IOException {
    return EmptyEnumeration.getInstance();
  }

  /** FIXME:
   * Helper to get a package from the bootstrap class loader. The default
   * implementation of returning null may be adequate, or you may decide that
   * this needs some native help.
   * 
   * @param name
   *          the name to find
   * @return the named package, if it exists
   */
  static Package getPackage(String name) {
    return null;
  }

  /**
   * Helper to get all packages from the bootstrap class loader. The default
   * implementation of returning an empty array may be adequate, or you may
   * decide that this needs some native help.
   * 
   * @return all named packages, if any exist
   */
  static Package[] getPackages() {
    return new Package[0];
  }

  /** FIXME:
   * The system default for assertion status. This is used for all system
   * classes (those with a null ClassLoader), as well as the initial value for
   * every ClassLoader's default assertion status.
   * 
   * XXX - Not implemented yet; this requires native help.
   * 
   * @return the system-wide default assertion status
   */
  static final boolean defaultAssertionStatus() {
    return true;
  }

  /**
   * The system default for package assertion status. This is used for all
   * ClassLoader's packageAssertionStatus defaults. It must be a map of package
   * names to Boolean.TRUE or Boolean.FALSE, with the unnamed package
   * represented as a null key.
   * 
   * XXX - Not implemented yet; this requires native help.
   * 
   * @return a (read-only) map for the default packageAssertionStatus
   */
  static final Map packageAssertionStatus() {
    return new HashMap();
  }

  /**
   * The system default for class assertion status. This is used for all
   * ClassLoader's classAssertionStatus defaults. It must be a map of class
   * names to Boolean.TRUE or Boolean.FALSE
   * 
   * XXX - Not implemented yet; this requires native help.
   * 
   * @return a (read-only) map for the default classAssertionStatus
   */
  static final Map classAssertionStatus() {
    return new HashMap();
  }

  // FIXME:
  static ClassLoader getSystemClassLoaderInternal() {
    return null;
  }

  // FIXME:
  static ClassLoader getSystemClassLoader() {
    // This method is called as the initialization of systemClassLoader,
    // so if there is a null value, this is the first call and we must check
    // for java.system.class.loader.
//    String loader=System.getProperty("java.system.class.loader");
//    ClassLoader default_sys=getSystemClassLoaderInternal();
//    if (loader != null) {
//      try {
//        Class load_class=Class.forName(loader, true, default_sys);
//        Constructor c=load_class.getConstructor(new Class[]{ClassLoader.class});
//        default_sys=(ClassLoader) c.newInstance(new Object[]{default_sys});
//      } catch (Exception e) {
//        System.err.println("Requested system classloader " + loader + " failed, using "
//            + "gnu.gcj.runtime.VMClassLoader");
//        e.printStackTrace();
//      }
//    }
    return null;
  }
}