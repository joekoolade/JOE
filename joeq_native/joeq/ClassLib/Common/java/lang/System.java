// System.java, created Thu Jul  4  4:50:03 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.Common.java.lang;

import java.io.InputStream;
import java.io.PrintStream;
import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Class;
import joeq.Class.jq_CompiledCode;
import joeq.Class.jq_StaticField;
import joeq.Memory.StackAddress;
import joeq.Runtime.ArrayCopy;
import joeq.Runtime.HashCode;
import joeq.Runtime.Reflection;
import joeq.Runtime.StackCodeWalker;
import joeq.Runtime.SystemInterface;

/**
 * System
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: System.java,v 1.12 2004/09/30 03:35:32 joewhaley Exp $
 */
public abstract class System {

    private static void registerNatives() { }
    private static void setIn0(InputStream in) {
        Reflection.putstatic_A(_in, in);
    }
    private static void setOut0(PrintStream out) {
        Reflection.putstatic_A(_out, out);
    }
    private static void setErr0(PrintStream err) {
        Reflection.putstatic_A(_err, err);
    }
    public static long currentTimeMillis() {
        return SystemInterface.currentTimeMillis();
    }
    public static void arraycopy(java.lang.Object src, int src_position,
                                 java.lang.Object dst, int dst_position,
                                 int length) {
        ArrayCopy.arraycopy(src, src_position, dst, dst_position, length);
    }
    public static int identityHashCode(java.lang.Object x) {
        return HashCode.identityHashCode(x);
    }
    public static native void initializeSystemClass();
    static java.lang.Class getCallerClass() {
        StackCodeWalker sw = new StackCodeWalker(null, StackAddress.getBasePointer());
        sw.gotoNext(); sw.gotoNext(); sw.gotoNext();
        jq_CompiledCode cc = sw.getCode();
        if (cc == null) return null;
        return Reflection.getJDKType(cc.getMethod().getDeclaringClass());
    }
    public static final jq_Class _class = (jq_Class)PrimordialClassLoader.loader.getOrCreateBSType("Ljava/lang/System;");
    public static final jq_StaticField _in = _class.getOrCreateStaticField("in", "Ljava/io/InputStream;");
    public static final jq_StaticField _out = _class.getOrCreateStaticField("out", "Ljava/io/PrintStream;");
    public static final jq_StaticField _err = _class.getOrCreateStaticField("err", "Ljava/io/PrintStream;");
    //public static final jq_StaticField _props = _class.getOrCreateStaticField("props", "Ljava/util/Properties;");
    //public static final jq_StaticMethod _initializeSystemClass = _class.getOrCreateStaticMethod("initializeSystemClass", "()V");

}
