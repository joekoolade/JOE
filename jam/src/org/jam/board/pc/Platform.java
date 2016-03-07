/**
 * Created on Mar 6, 2016
 *
 * Copyright (C) Joe Kulig, 2016
 * All rights reserved.
 */
package org.jam.board.pc;

/**
 * @author Joe Kulig
 *
 */
public class Platform {
    public static PcSystemTimer timer;
    
    public void init()
    {
        timer = new PcSystemTimer();
    }
}
