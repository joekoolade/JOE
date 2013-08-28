/*
 * Created on Oct 16, 2003
 *
 * Copyright (C) Joe Kulig, 2003
 * All rights reserved.
 * 
 */
package baremetal.processor;

import baremetal.platform.Console;
import baremetal.platform.Platform;
import baremetal.platform.SystemTimer;
import baremetal.runtime.SystemConfig;

/**
 * @author Joe Kulig
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class Pentium {
  public long freq = SystemConfig.clockFrequency;
  
  public final static int CR0_PE=0x00000001;
  public final static int CR0_MP=0x00000002;
  public final static int CR0_EM=0x00000004;
  public final static int CR0_TS=0x00000008;
  public final static int CR0_ET=0x00000010;
  public final static int CR0_NE=0x00000020;
  public final static int CR0_WP=0x00010000;
  public final static int CR0_AM=0x00040000;
  public final static int CR0_NW=0x20000000;
  public final static int CR0_CD=0x40000000;
  public final static int CR0_PG=0x80000000;
  
  public final static int CR3_PWT=0x00000008;
  public final static int CR3_PCD=0x00000010;
  
  public final static int CR4_VME=0x00000001;
  public final static int CR4_PVI=0x00000002;
  public final static int CR4_TSD=0x00000004;
  public final static int CR4_DE =0x00000008;
  public final static int CR4_PSE=0x00000010;
  public final static int CR4_PAE=0x00000020;
  public final static int CR4_MCE=0x00000040;
  public final static int CR4_PGE=0x00000080;
  public final static int CR4_PCE=0x00000100;
  public final static int CR4_OSFXSR=0x00000200;
  public final static int CR4_OSXMMEXCPT=0x00000400;
  
  public Pentium() {
  }
  
  public void init() {
    /*
     * Initialize the fpu
     */
    int val = cr0();
    val |= (CR0_NE|CR0_MP);
    val &= ~CR0_EM;
    cr0(val);
    fninit();
  }
  
	public void setRegisters(int[] values) {
	}
	public int[] getRegisters() {
		return null;
	}
	public static void enableInterrupts() {
		
	}
	public static void disableInterrupts() {
	}

  /**
   * 
   */
  public final void halt() {
    Console.writeln("Halting CPU!");
    hlt();
    while(true)
      ;
  }
	
  private final native void hlt();
  
  public final long readTimestamp() {
    return rdtsc();
  }
  
  public final void detectCpuFrequency() {
    long startTime = SystemTimer.tick;
    long start=rdtsc();
    while(Platform.systemTimer.getTime()<startTime+10)
      ;
    long end=rdtsc();
    freq = (end-start)*SystemTimer.counterDivisor*10;
    System.out.print("Frequency: ");
    System.out.print(freq/1000000);
    System.out.println("Mhz");
  }
  
  public final native void cli();
  public final native void sti();
  
  public final native long rdtsc();
  public final native void fninit();
  
  public final native int cr0();
  public final native int cr2();
  public final native int cr3();
  public final native int cr4();
  
  public final native void cr0(int value);
  public final native void cr2(int value);
  public final native void cr3(int value);
  public final native void cr4(int value);
}
