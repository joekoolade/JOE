/*
 * Created on Feb 23, 2004
 */
package joeq.ClassLib.sun15_linux.java.util.concurrent.atomic;

import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Class;
import joeq.Class.jq_InstanceField;

/**
 * @author jwhaley
 */
public class AtomicLong {
    private static final long valueOffset;
    
    static {
        jq_Class c = (jq_Class) PrimordialClassLoader.loader.getOrCreateBSType("Ljava/util/concurrent/atomic/AtomicLong;");
        c.prepare();
        valueOffset = ((jq_InstanceField) c.getDeclaredMember("value", "J")).getOffset();
    }
}
