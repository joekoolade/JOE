// Inet4AddressImpl.java, created Fri Mar  7 11:01:56 2003 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.Common.java.net;

import joeq.Bootstrap.MethodInvocation;
import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Class;
import joeq.Class.jq_Method;
import joeq.Class.jq_NameAndDesc;
import joeq.Main.jq;
import joeq.Memory.Address;
import joeq.Memory.CodeAddress;
import joeq.Memory.HeapAddress;
import joeq.Runtime.SystemInterface;
import joeq.Runtime.Unsafe;
import joeq.Runtime.SystemInterface.ExternalLink;
import joeq.Runtime.SystemInterface.Library;
import jwutil.util.Assert;

/**
 * Inet4AddressImpl
 *
 * @author John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: Inet4AddressImpl.java,v 1.6 2004/09/30 03:35:30 joewhaley Exp $
 */
public class Inet4AddressImpl {
    
    static class hostent {
        String   h_name;      /* official name of host */
        String[] h_aliases;   /* alias list */
        int      h_addrtype;  /* host address type */
        int      h_length;    /* length of address */
        String[] h_addr_list; /* list of addresses */
    }
    
    public static hostent get_host_by_name(String name) {
        try {
            CodeAddress a = gethostbyname.resolve();
            byte[] b = SystemInterface.toCString(name);
            HeapAddress c = HeapAddress.addressOf(b);
            Unsafe.pushArgA(c);
            Unsafe.getThreadBlock().disableThreadSwitch();
            Address p = Unsafe.invokeA(a);
            Unsafe.getThreadBlock().enableThreadSwitch();
            hostent r = new hostent();
            r.h_name = SystemInterface.fromCString(p.peek());
            int count = 0;
            Address q = p.offset(HeapAddress.size()).peek();
            while (!q.peek().isNull()) {
                ++count;
                q = q.offset(HeapAddress.size());
            }
            r.h_aliases = new String[count];
            count = 0;
            q = p.offset(HeapAddress.size()).peek();
            while (!q.peek().isNull()) {
                r.h_aliases[count] = SystemInterface.fromCString(q.peek());
                ++count;
                q = q.offset(HeapAddress.size());
            }
            r.h_addrtype = p.offset(HeapAddress.size()*2).peek4();
            r.h_length = p.offset(HeapAddress.size()*2+4).peek4();
            count = 0;
            q = p.offset(HeapAddress.size()*2+8).peek();
            while (!q.peek().isNull()) {
                ++count;
                q = q.offset(HeapAddress.size());
            }
            count = 0;
            q = p.offset(HeapAddress.size()*2+8).peek();
            while (!q.peek().isNull()) {
                r.h_addr_list[count] = SystemInterface.fromCString(q.peek());
                ++count;
                q = q.offset(HeapAddress.size());
            }
            return r;
        } catch (Throwable x) { Assert.UNREACHABLE(); }
        return null;
    }
    
    public static /*final*/ ExternalLink gethostbyname;

    static {
        if (jq.RunningNative) boot();
        else if (jq.on_vm_startup != null) {
            jq_Class c = (jq_Class) PrimordialClassLoader.loader.getOrCreateBSType("Ljava/net/Inet4AddressImpl;");
            jq_Method m = c.getDeclaredStaticMethod(new jq_NameAndDesc("boot", "()V"));
            MethodInvocation mi = new MethodInvocation(m, null);
            jq.on_vm_startup.add(mi);
        }
    }

    public static void boot() {
        Library winsock = SystemInterface.registerLibrary("ws2_32");

        if (winsock != null) {
            gethostbyname = winsock.resolve("gethostbyname");
        } else {
            gethostbyname = null;
        }

    }

    public java.lang.String getLocalHostName() throws java.net.UnknownHostException {
        return null;
    }
    public byte[][] lookupAllHostAddr(java.lang.String hostname) throws java.net.UnknownHostException {
        return null;
    }
    public java.lang.String getHostByAddr(byte[] addr) throws java.net.UnknownHostException {
        return null;
    }
}
