// Comparisons.java, created Sun Feb  8 16:32:41 PST 2004 by gback
// Copyright (C) 2003 Godmar Back <gback@stanford.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Helper application to create proxy classes that encapsulate an object's
 * package-accessible fields and make them public fields. 
 * Currently used to create for Compiler.Analysis.IPA.PAProxy.
 */
public class MakeProxy {
    public static final String suffix = "Proxy";

    public static void main(String []av) throws Exception {
        String fqname = av[0];
        int b = fqname.lastIndexOf('.')+1;
        String cname = fqname.substring(b);
        String pkg = fqname.substring(0, b-1);
        String pcname = cname + suffix;
        System.out.println("// Public proxy class for " + fqname);
        System.out.println("// Generated via " + MakeProxy.class.getName());
        System.out.println("package " + pkg + ";\n");
        System.out.println("public class " + pcname + " {");
        System.out.println("  public " + pcname + "(" + cname + " that) {");
        StringBuffer sb = new StringBuffer();
        Class clazz = Class.forName(fqname);
        while (clazz != null) {
            Field [] ff = clazz.getDeclaredFields();
            for (int i = 0; i < ff.length; i++) {
                Field f = ff[i];
                if (f.getName().startsWith("class$"))
                    continue;
                boolean isPrivate = (f.getModifiers() & Modifier.PRIVATE) != 0;
                if (isPrivate)
                    continue;
                System.out.println("    this." + f.getName() + " = that." + f.getName() + ";");
                boolean isStatic = (f.getModifiers() & Modifier.STATIC) != 0;
                String typeName;
                Class c = f.getType();
                if (f.getType().isArray()) {
                    StringBuffer sb2 = new StringBuffer();
                    while (c.isArray()) {
                        sb2.append("[]");
                        c = c.getComponentType();
                    }
                    typeName = c.getName() + sb2.toString();
                } else {
                    typeName = c.getName();
                }
                sb.append("  public " + (isStatic ? "static " : "") + typeName.replace('$', '.') 
                        + " " + f.getName() + ";\n");
            }
            clazz = clazz.getSuperclass();
        }
        System.out.println("  }");
        System.out.print(sb.toString());
        System.out.println("}");
    }
}
