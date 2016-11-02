package test.org.jikesrvm.org.gctests;
/*
 *  This file is part of the Jikes RVM project (http://jikesrvm.org).
 *
 *  This file is licensed to You under the Eclipse Public License (EPL);
 *  You may not use this file except in compliance with the License. You
 *  may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/eclipse-1.0.php
 *
 *  See the COPYRIGHT.txt file distributed with this work for information
 *  regarding copyright ownership.
 */

import java.io.PrintStream;

import org.jikesrvm.VM;
import org.jikesrvm.mm.mminterface.MemoryManager;
import org.vmmagic.pragma.NoInline;

public class Exhaust {

    static Object[]  first;
    static Object[]  last;
    static final int itemSize     = 64;
    static double    growthFactor = 10.0;
    static int       rounds       = 5;
    static long      tot          = 0;   // total # allocated
    static long      wasAllocating;      // How much were we allocating?

    public static void main(String[] args)
    {
        PrintStream o = System.out;
        o.println("Exhaust.main");
        long mhs = MemoryManager.getMaxHeapSize().toLong();

        o.println("Max heap size: " + mhs + " bytes");
        if (mhs > 1024 * 1024) o.println("  that's " + mhs / (1024 * 1024) + " megabytes");
        if (mhs > 1024 * 1024 * 1024) o.println("  that's " + mhs / (1024 * 1024 * 1024) + " gigabytes");
//        try
//        {
//            Thread.sleep(1000);
//        }
//        catch (InterruptedException e)
//        {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
        runTest();

        System.exit(0);
    }

    @NoInline
    public static int doInner(int size)
    {
        while (true)
        {
            wasAllocating = size;
            Object[] next = new Object[size / 4];
            last[0] = next;
            last = next;
            tot += size;
            wasAllocating = 0;
        }
    }

    @NoInline
    public static void runTest()
    {
        PrintStream o = System.out;
        int size = itemSize;
        for (int i = 1; i <= rounds; i++)
        {
            o.println("Starting round " + i + " with size = " + size);

            first = new Object[1];
            last = first;
            tot = 0;

            o.println("  Allocating until exception thrown");
            try
            {
                doInner(size);
            }
            catch (OutOfMemoryError e)
            {
                first = last = null;  // kills everything
                o.println("  Caught OutOfMemory - freeing now");  // this allocates; must follow nulling

                // o.println("  Maximum size reached is " + size);

                o.println("  Had " + tot + " bytes allocated; failed trying to allocate " + wasAllocating + " bytes");
            }

            size *= growthFactor;
        }
        System.out.println("Overall: SUCCESS");
    }
}
