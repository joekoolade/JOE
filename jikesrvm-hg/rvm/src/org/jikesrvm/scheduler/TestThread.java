/**
 * Created on Sep 25, 2016
 *
 * Copyright (C) Joe Kulig, 2016
 * All rights reserved.
 */
package org.jikesrvm.scheduler;
import test.org.jikesrvm.org.gctests.Exhaust;
import test.org.jikesrvm.basic.core.bytecode.*;

/**
 * @author Joe Kulig
 *
 */
public class TestThread extends Thread {
    public void run()
    {
        System.out.println("Exhaust test");
        // Exhaust.main(null);
        TestArithmetic.main(null);
        TestArrayAccess.main(null);
        TestClassHierarchy.main(null);
    }
}
