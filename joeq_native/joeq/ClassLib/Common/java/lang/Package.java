// Package.java, created Sun Feb 23  2:02:26 2003 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.Common.java.lang;

import joeq.Class.PrimordialClassLoader;

/**
 * Package
 *
 * @author John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: Package.java,v 1.4 2004/03/09 21:57:34 jwhaley Exp $
 */
public abstract class Package {
    private static java.lang.String getSystemPackage0(java.lang.String name) {
        for (int i=0; i<system_packages.length; ++i) {
            if (name.equals(system_packages[i])) {
                return PrimordialClassLoader.loader.getPackagePath(name);
            }
        }
        return null;
    }
    private static java.lang.String[] getSystemPackages0() {
        return system_packages;
    }
    private static java.lang.String[] system_packages = {
        "java/net/",
        "java/nio/",
        "java/beans/",
        "sun/misc/",
        "sun/nio/cs/",
        "sun/reflect/",
        "java/io/",
        "java/security/",
        "java/nio/charset/",
        "sun/io/",
        "java/util/",
        "java/lang/",
        "java/util/logging/",
        "java/security/cert/",
        "java/lang/reflect/",
        "java/util/jar/",
        "java/util/zip/",
        "sun/security/util/",
        "sun/net/www/",
        "java/lang/ref/",
        "sun/net/www/protocol/jar/",
        "java/nio/charset/spi/",
        "sun/net/www/protocol/file/",
        "sun/security/action/" };

}
