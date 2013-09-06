// ObjectOutputStream.java, created Mon Jul  8  0:41:49 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.ClassLib.Common.java.io;

import joeq.Runtime.Unsafe;
import jwutil.util.Assert;
import jwutil.util.Convert;

/**
 * ObjectOutputStream
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: ObjectOutputStream.java,v 1.9 2004/09/30 03:35:32 joewhaley Exp $
 */
public abstract class ObjectOutputStream {

    private static void floatsToBytes(float[] src, int srcpos, byte[] dst, int dstpos, int nfloats) {
        --srcpos;
        while (--nfloats >= 0) {
            Convert.intToFourBytes(Unsafe.floatToIntBits(src[++srcpos]), dst, dstpos);
            dstpos += 4;
        }
    }
    private static void doublesToBytes(double[] src, int srcpos, byte[] dst, int dstpos, int ndoubles) {
        --srcpos;
        while (--ndoubles >= 0) {
            Convert.longToEightBytes(Unsafe.doubleToLongBits(src[++srcpos]), dst, dstpos);
            dstpos += 8;
        }
    }
    private static void getPrimitiveFieldValues(java.lang.Object obj, long[] fieldIDs, char[] typecodes, byte[] data) {
        Assert.TODO();
    }
    private static java.lang.Object getObjectFieldValue(java.lang.Object obj, long fieldID) {
        Assert.TODO();
        return null;
    }
    
}
