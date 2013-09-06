// ClassLoader.java, created Jul 5, 2003 2:04:37 AM by joewhaley
// Copyright (C) 2003 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.sun142_linux.java.lang;

/**
 * ClassLoader
 * 
 * @author John Whaley
 * @version $Id: ClassLoader.java,v 1.2 2004/03/09 06:26:54 jwhaley Exp $
 */
public class ClassLoader {

    private static java.lang.RuntimePermission getClassLoaderPerm;
    static java.lang.RuntimePermission getGetClassLoaderPerm() {
        if (getClassLoaderPerm == null) {
            try {
                java.lang.Class c = java.lang.Class.forName("sun.security.util.SecurityConstants");
                java.lang.reflect.Field f = c.getField("GET_CLASSLOADER_PERMISSION");
                getClassLoaderPerm = (java.lang.RuntimePermission) f.get(null);
            } catch (java.lang.ClassNotFoundException x) {
            } catch (java.lang.NoSuchFieldException x) {
            } catch (java.lang.IllegalAccessException x) {
            } catch (java.lang.ClassCastException x) {
            }
        }
        return getClassLoaderPerm;
    }
    
}
