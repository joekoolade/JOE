/**
 * Created on Apr 4, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.cpu.intel;

import org.jam.board.pc.I82c54;
import org.jam.board.pc.Platform;
import org.jikesrvm.VM;
import org.jikesrvm.runtime.Magic;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Offset;

/**
 * @author Joe Kulig
 *
 */
public class Apic {
  int frequency;
  
  // Hardcode address for now
  final protected static Address registers = Address.fromIntSignExtend(0xFEE00000);
  final protected static Offset ID = Offset.fromIntSignExtend(0x20);
  final protected static Offset VERSION = Offset.fromIntSignExtend(0x30);
  final protected static Offset TPR = Offset.fromIntSignExtend(0x80);
  final protected static Offset APR = Offset.fromIntSignExtend(0x90);
  final protected static Offset PPR = Offset.fromIntSignExtend(0xA0);
  final protected static Offset EIO = Offset.fromIntSignExtend(0xB0);
  final protected static Offset RRD = Offset.fromIntSignExtend(0xC0);
  final protected static Offset LDR = Offset.fromIntSignExtend(0xD0);
  final protected static Offset DFR = Offset.fromIntSignExtend(0xE0);
  final protected static Offset SPVR = Offset.fromIntSignExtend(0xF0);
  final protected static Offset ISR = Offset.fromIntSignExtend(0x100);
  final protected static Offset TMR = Offset.fromIntSignExtend(0x180);
  final protected static Offset IRR = Offset.fromIntSignExtend(0x200);
  final protected static Offset ESR = Offset.fromIntSignExtend(0x280);
  final protected static Offset CMCI = Offset.fromIntSignExtend(0x2F0);
  final protected static Offset ICR0 = Offset.fromIntSignExtend(0x300);
  final protected static Offset ICR1 = Offset.fromIntSignExtend(0x310);
  final protected static Offset TIMER = Offset.fromIntSignExtend(0x320);
  final protected static Offset TSR = Offset.fromIntSignExtend(0x330);
  final protected static Offset PMCR = Offset.fromIntSignExtend(0x340);
  final protected static Offset LINT0 = Offset.fromIntSignExtend(0x350);
  final protected static Offset LINT1 = Offset.fromIntSignExtend(0x360);
  final protected static Offset ERROR = Offset.fromIntSignExtend(0x370);
  final protected static Offset TIMERICR = Offset.fromIntSignExtend(0x380);
  final protected static Offset TIMERCCR = Offset.fromIntSignExtend(0x390);
  final protected static Offset TIMERDCR = Offset.fromIntSignExtend(0x3E0);
  
  final private static int LVT_PERIODIC = 1<<17;
  final private static int LVT_DEADLINE = 2<<17;
  final private static int LVT_MASK = 1<<16;
  final private static int LVT_PENDING = 1<<12;
  
  final private static int TDR_DIV_1 = 0x0B;
  final private static int TDR_DIV_2 = 0x00;
  final private static int TDR_DIV_4 = 0x01;
  final private static int TDR_DIV_8 = 0x02;
  final private static int TDR_DIV_16 = 0x03;
  final private static int TDR_DIV_32 = 0x08;
  final private static int TDR_DIV_64 = 0x09;
  final private static int TDR_DIV_128 = 0x0A;
  
  final private static int MAX_ICR = 0xFFFFFFFF;
  
  public Apic()
  {
    if(!CpuId.hasAPIC)
    {
      throw new RuntimeException("No APIC on the processor!");
    } 
    if(!MSR.apicIsEnabled())
    {
      throw new RuntimeException("APIC is not enabled!");
    }
    registers.store(LVT_MASK, TIMER);
    registers.store(TDR_DIV_64, TIMERDCR);
  }
  
  public final void calibrate()
  {
    int pitcnt = 0;
    int t2;
    
    I82c54 timer = Platform.timer.timer;
    Address keyboardController = Address.fromIntZeroExtend(0x61);
    
    Magic.disableInterrupts();
    /*
     * Enable timer2 gate, disable speaker
     */
    int keyboardControllerValue = keyboardController.ioLoadByte();
    keyboardControllerValue &= ~0x2;
    keyboardControllerValue |= 0x1;
    keyboardController.ioStore(keyboardControllerValue);
    
    int calibrateTimeMs=100;
    /*
     * Convert to a latch time
     */
    int latch = 1193182/(1000/calibrateTimeMs);
            
    timer.counter2(I82c54.MODE0, latch);
    registers.store(0xFFFFFFFF, TIMERICR);
    // Wait for the gate to go active
    while((keyboardController.ioLoadByte() & 0x20) != 0)
      ;
    while((keyboardController.ioLoadByte() & 0x20) == 0)
    {
        pitcnt++;
    }
    t2 = registers.loadInt(TIMERCCR);
    Magic.enableInterrupts();
    // disable the timer
    registers.store(0, TIMERICR);
    keyboardController.ioStore(0);
    VM.sysWriteln("t2: ", t2);
    long apicCycles = (long)MAX_ICR-t2;
    VM.sysWrite("cycles: ", apicCycles);
    VM.sysWriteln("  loops: ", pitcnt);
    frequency = (int)apicCycles*(1000/calibrateTimeMs);
    VM.sysWriteln("APIC frequency = ", frequency);
  }
}
