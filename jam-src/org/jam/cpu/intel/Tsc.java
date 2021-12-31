/**
 * Created on Sep 13, 2016
 *
 * Copyright (C) Joe Kulig, 2016
 * All rights reserved.
 */
package org.jam.cpu.intel;

import org.jam.board.pc.I82c54;
import org.jam.board.pc.RTC;
import org.jikesrvm.VM;
import org.jikesrvm.runtime.Magic;
import org.vmmagic.unboxed.Address;

/**
 * @author Joe Kulig
 *
 */
public class Tsc {
  public static long       cyclesPerSecond;
  public static long        cyclesPer1000Ns = 2500;
  public final static long NSPERSEC        = 1000000000;

  public static boolean    DEBUG           = false;
    
    public final static long getCycles()
    {
        return Magic.getTimeBase();
    }
    
    public static void rtcCalibrate()
    {
      long t1=0, t2=0;
      
      Magic.disableInterrupts();
      byte second = RTC.getSecond();
      while(RTC.getSecond() == second)
      {
        t1 = getCycles();
      }
      second = RTC.getSecond();
      while(RTC.getSecond() == second)
      {
        t2 = getCycles();
      }
      Magic.enableInterrupts();
      
      VM.sysWrite("t1 t2: ", t1); VM.sysWriteln(" ", t2);
      VM.sysWriteln("cycles: ", t2-t1);
      cyclesPerSecond = (t2-t1);
      VM.sysWriteln("TSC cycle per second = ", cyclesPerSecond);
      cyclesPer1000Ns = (int)(cyclesPerSecond/1000000);
      VM.sysWriteln("TSC cycles per 1000 NS = ", cyclesPer1000Ns);
    }
    
    public static void rtcCalibrate0()
    {
      long t1, t2, tsc;
      
      /*
       * Set a 125ms periodic interrupt
       */
      RTC.rate8Hz();
      Magic.disableInterrupts();
      /*
       * Clear out any previous interrupt flags
       */
      RTC.hasPeriodicInterrupt();
      /*
       * Wait for PI intterupt
       */
      while(RTC.hasPeriodicInterrupt() == false)
      {
        tsc = getCycles();
      }
      t1 = getCycles();
      /*
       * Wait for another interrupt
       */
      while(RTC.hasPeriodicInterrupt() == false)
      {
        tsc = getCycles();
      }
      t2 = getCycles();
      Magic.enableInterrupts();
      
      VM.sysWrite("t1 t2: ", t1); VM.sysWriteln(" ", t2);
      VM.sysWrite("cycles: ", t2-t1);
      cyclesPerSecond = (t2-t1)*8;
      VM.sysWriteln("TSC cycle per second = ", cyclesPerSecond);
      cyclesPer1000Ns = (int)(cyclesPerSecond/1000000);
      VM.sysWriteln("TSC cycles per 1000 NS = ", cyclesPer1000Ns);
      
    }
    public static void calibrate(int calibrateTimeMs)
    {
        long tsc, t1, t2, delta;
        long tscmin, tscmax;
        int pitcnt = 0;
        
        Address keyboardController = Address.fromIntZeroExtend(0x61);
        
        Magic.disableInterrupts();
        /*
         * Enable timer2 gate, disable speaker
         */
        int keyboardControllerValue = keyboardController.ioLoadByte();
        keyboardControllerValue &= ~0x2;
        keyboardControllerValue |= 0x1;
        keyboardController.ioStore(keyboardControllerValue);
        
        /*
         * Convert to a latch time
         */
        int latch = 1193182/(1000/calibrateTimeMs);
                
        tscmax = 0;
        tscmin = Long.MAX_VALUE;
        I82c54.counter2(I82c54.MODE0, latch);
        tsc = t1 = t2 = getCycles();
        // Wait for the gate to go active
        while((keyboardController.ioLoadByte() & 0x20) != 0)
          ;
        while((keyboardController.ioLoadByte() & 0x20) == 0)
        {
            t2 = getCycles();
            delta = t2 - tsc;
            tsc = t2;
            if(pitcnt==0)
            {
                if(delta < tscmin)
                {
                    tscmin = delta;
                }
                if(delta > tscmax)
                {
                    tscmax = delta;
                }
            }
            pitcnt++;
        }
        Magic.enableInterrupts();
        keyboardController.ioStore(0);
        VM.sysWrite("t1 t2: ", t1); VM.sysWriteln(" ", t2);
        VM.sysWrite("cycles: ", t2-t1);
        VM.sysWrite("  min: ", tscmin);
        VM.sysWrite("  max: ", tscmax);
        VM.sysWriteln("  loops: ", pitcnt);
        cyclesPerSecond = (t2-t1)*(1000/calibrateTimeMs);
        VM.sysWriteln("TSC cycle per second = ", cyclesPerSecond);
        cyclesPer1000Ns = (int)(cyclesPerSecond/1000000);
        VM.sysWriteln("TSC cycles per 1000 NS = ", cyclesPer1000Ns);
    }
    
    /**
     * Delay for a set amount of microseconds
     * @param microseconds microseconds to delay
     */
    public static void udelay(long microseconds)
    {
      long count=0, delayCycles;
      long start=getCycles();
      delayCycles = microseconds * (long)cyclesPer1000Ns;
      long end = getCycles() + delayCycles;
      if(DEBUG) 
      { 
    	  VM.sysWrite("udelay: ", start); 
    	  VM.sysWrite(" ", microseconds);
    	  VM.sysWrite(" ", delayCycles);
    	  VM.sysWriteln(" ", end);
      }
      while(getCycles() < end && count < delayCycles)
      {
        // just spin here
    	  count++;
      }
      if(count >= delayCycles)
      {
    	  VM.sysWriteln("udelay: count ", count);
      }
    }
}
