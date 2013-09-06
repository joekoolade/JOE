// ObjectInputStream.java, created Mon Jul  8  0:41:49 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.Common.java.io;

import joeq.Class.jq_Array;
import joeq.Class.jq_Class;
import joeq.Class.jq_Initializer;
import joeq.Class.jq_NameAndDesc;
import joeq.Class.jq_Type;
import joeq.ClassLib.Common.ClassUtils;
import joeq.Runtime.Reflection;
import joeq.Runtime.Unsafe;
import joeq.UTF.Utf8;
import jwutil.util.Assert;
import jwutil.util.Convert;

/**
 * ObjectInputStream
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: ObjectInputStream.java,v 1.13 2004/09/30 03:35:32 joewhaley Exp $
 */
public abstract class ObjectInputStream {

    private static java.lang.ClassLoader latestUserDefinedLoader()
        throws java.lang.ClassNotFoundException
    {
        // TODO.
        return null;
    }
    private static void bytesToFloats(byte[] src, int srcpos, float[] dst, int dstpos, int nfloats) {
        --dstpos;
        while (--nfloats >= 0) {
            dst[++dstpos] = Unsafe.intBitsToFloat(Convert.fourBytesToInt(src, srcpos));
            srcpos += 4;
        }
    }
    private static void bytesToDoubles(byte[] src, int srcpos, double[] dst, int dstpos, int ndoubles) {
        --dstpos;
        while (--ndoubles >= 0) {
            dst[++dstpos] = Unsafe.longBitsToDouble(Convert.eightBytesToLong(src, srcpos));
            srcpos += 8;
        }
    }
    private static void setPrimitiveFieldValues(java.lang.Object obj, long[] fieldIDs, char[] typecodes, byte[] data) {
        Assert.TODO();
    }
    private static void setObjectFieldValue(java.lang.Object obj, long fieldID, java.lang.Class type, java.lang.Object val) {
        Assert.TODO();
    }
    private static java.lang.Object allocateNewObject(java.lang.Class aclass, java.lang.Class initclass)
        throws java.lang.InstantiationException, java.lang.IllegalAccessException
    {
        jq_Type t1 = Reflection.getJQType(aclass);
        if (!(t1 instanceof jq_Class))
            throw new java.lang.InstantiationException();
        jq_Type t2 = Reflection.getJQType(initclass);
        if (!(t2 instanceof jq_Class))
            throw new java.lang.InstantiationException();
        jq_Class c1 = (jq_Class)t1;
        c1.load();
        if (c1.isAbstract())
            throw new java.lang.InstantiationException("cannot instantiate abstract "+aclass);
        jq_Class c2 = (jq_Class)t2;
        jq_Initializer i = c2.getInitializer(new jq_NameAndDesc(Utf8.get("<init>"), Utf8.get("()V")));
        if (i == null)
            throw new InstantiationException("no empty arg initializer in "+initclass);
        ClassUtils.checkCallerAccess(i, 3);
        c1.cls_initialize(); 
        Object o = c1.newInstance();
        try {
            Reflection.invokeinstance_V(i, o);
        } catch (Error x) {
            throw x;
        } catch (java.lang.Throwable x) {
            throw new ExceptionInInitializerError(x);
        }
        return o;
    }
    private static java.lang.Object allocateNewArray(java.lang.Class aclass, int length)
        throws java.lang.NegativeArraySizeException, java.lang.IllegalArgumentException {
        jq_Type t = Reflection.getJQType(aclass);
        if (!(t instanceof jq_Array))
            throw new java.lang.IllegalArgumentException(aclass+" is not an array type");
        jq_Array a = (jq_Array)t;
        a.cls_initialize();
        return a.newInstance(length);
    }

}
