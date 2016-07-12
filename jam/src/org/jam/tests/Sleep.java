/**
 * Created on Jul 1, 2016
 *
 * Copyright (C) Joe Kulig, 2016
 * All rights reserved.
 */
package org.jam.tests;

/**
 * @author Joe Kulig
 *
 */
public final class Sleep implements Runnable {

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        Sleep sleep = new Sleep();

        new Thread(sleep).start();
    }
    
    public final void sleepTest()
    {
        int i=0;
        
        while(true)
        {
            try
            {
                System.out.print(i);
                System.out.print(' ');
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            i++;
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run()
    {
        sleepTest();
    }
}
