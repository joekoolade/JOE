// ClassName.java, created Oct 24 23:23:21 2003 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Main;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Class;
import joeq.Class.jq_ClassFileConstants;
import joeq.Class.jq_ConstantPool;

/**
 * Extracts the class name from the class file(s) given on the command line.
 * 
 * @author jwhaley
 * @version $Id: ClassName.java,v 1.7 2004/09/22 22:17:31 joewhaley Exp $
 */
public class ClassName implements jq_ClassFileConstants {

    public static void main(String[] args) throws Exception {
        HostedVM.initialize();
        
        for (int i = 0; i < args.length; ++i) {
            try {
                System.err.println(args[i]);
                DataInputStream in = new DataInputStream(new FileInputStream(args[i]));
                int k = in.skipBytes(8);
                if (k != 8) throw new IOException();
                int constant_pool_count = in.readUnsignedShort();
                jq_ConstantPool cp = new jq_ConstantPool(constant_pool_count);
                cp.load(in);
                cp.resolve(PrimordialClassLoader.loader);
                k = in.skipBytes(2);
                if (k != 2) throw new IOException();
                char selfindex = (char)in.readUnsignedShort();
                if (cp.getTag(selfindex) != CONSTANT_ResolvedClass) {
                    System.err.println("constant pool entry "+(int)selfindex+", referred to by field this_class" +
                                       ", is wrong type tag (expected="+CONSTANT_Class+", actual="+cp.getTag(selfindex)+")");
                }
                jq_Class t = (jq_Class) cp.get(selfindex);
                System.out.println(t.getJDKName());
                in.close();
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
    }
}
