// AccessController.java, created Thu Jul  4  4:50:03 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.Common.java.security;

/**
 * AccessController
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: AccessController.java,v 1.7 2004/03/09 06:26:27 jwhaley Exp $
 */
abstract class AccessController {

    public static Object doPrivileged(java.security.PrivilegedAction action) {
        // TODO: set privilege level
        return action.run();
    }
    public static Object doPrivileged(java.security.PrivilegedAction action,
                                      java.security.AccessControlContext context) {
        // TODO: set privilege level
        return action.run();
    }
    public static Object doPrivileged(java.security.PrivilegedExceptionAction action)
        throws java.security.PrivilegedActionException {
        // TODO: set privilege level
        try {
            return action.run();
        } catch (RuntimeException x) {
            throw x;
        } catch (Exception x) {
            throw new java.security.PrivilegedActionException(x);
        }
    }
    public static Object doPrivileged(java.security.PrivilegedExceptionAction action,
                                      java.security.AccessControlContext context)
        throws java.security.PrivilegedActionException {
        // TODO: set privilege level
        try {
            return action.run();
        } catch (RuntimeException x) {
            throw x;
        } catch (Exception x) {
            throw new java.security.PrivilegedActionException(x);
        }
    }
    private static java.security.AccessControlContext getStackAccessControlContext() {
        // TODO
        return null;
    }
    static java.security.AccessControlContext getInheritedAccessControlContext() {
        // TODO
        return null;
    }

    //public static final jq_Class _class = (jq_Class)PrimordialClassLoader.loader.getOrCreateBSType("Ljava/security/AccessController;");
}
