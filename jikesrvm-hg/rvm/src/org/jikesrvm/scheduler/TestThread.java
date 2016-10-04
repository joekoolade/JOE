/**
 * Created on Sep 25, 2016
 *
 * Copyright (C) Joe Kulig, 2016
 * All rights reserved.
 */
package org.jikesrvm.scheduler;
import test.org.jikesrvm.org.gctests.Exhaust;

/**
 * @author Joe Kulig
 *
 */
public class TestThread extends Thread {
    public void run()
    {
        System.out.println("Exhaust test");
        Exhaust.main(null);
    }
}
