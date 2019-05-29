/**
 * Created on Apr 12, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.cpu.intel;

import org.jam.board.pc.I82c54;
import org.jam.board.pc.Platform;
import org.jam.interfaces.Timer;
import org.jikesrvm.VM;
import org.jikesrvm.runtime.Magic;
import org.jikesrvm.scheduler.RVMThread;
import org.vmmagic.unboxed.Address;

/**
 * @author Joe Kulig
 *
 */
public class ApicTimer extends Apic
implements Timer
{
  int frequency;

  private long tick;
  
  final protected static int LVT_PERIODIC = 1<<17;
  final protected static int LVT_DEADLINE = 2<<17;
  final protected static int LVT_MASK = 1<<16;
  final protected static int LVT_PENDING = 1<<12;
  
  final protected static int TDR_DIV_1 = 0x0B;
  final protected static int TDR_DIV_2 = 0x00;
  final protected static int TDR_DIV_4 = 0x01;
  final protected static int TDR_DIV_8 = 0x02;
  final protected static int TDR_DIV_16 = 0x03;
  final protected static int TDR_DIV_32 = 0x08;
  final protected static int TDR_DIV_64 = 0x09;
  final protected static int TDR_DIV_128 = 0x0A;
  
  final protected static int MAX_ICR = 0xFFFFFFFF;
  
  public ApicTimer()
  {
    super();
    setTimerVector(LVT_MASK);
    divideBy64();
  }
  
  public final void mask()
  {
    int timerVectorValue = getTimerVector();
    timerVectorValue |= LVT_MASK;
    setTimerVector(timerVectorValue);
  }
  
  public final boolean isPending()
  {
    int timerVector = getTimerVector();
    return (timerVector & LVT_PENDING) != 0;
  }
  
  public final boolean isDelivered()
  {
    int timerVector = getTimerVector();
    return (timerVector & LVT_PENDING) == 0;
  }
  
  public final void divideBy64()
  {
    setTimerDcr(TDR_DIV_64);
  }
  
  public final void disableTimer()
  {
    setTimerIcr(0);
  }
  public final void calibrate()
  {
    int pitcnt = 0;
    long t2;
    
    I82c54 timer = Platform.pit.timer;
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
    setTimerIcr(0xFFFFFFFF);
    // Wait for the gate to go active
    VM.sysWriteln("Wait for gate to go active ");
    while((keyboardController.ioLoadByte() & 0x20) != 0)
      ;
    VM.sysWriteln('%');
    while((keyboardController.ioLoadByte() & 0x20) == 0)
    {
        pitcnt++;
    }
    t2 = getTimerCcr();
    Magic.enableInterrupts();
    // disable the timer
    disableTimer();
    keyboardController.ioStore(0);
    VM.sysWriteln("t2: ", t2);
    long apicCycles = (long)MAX_ICR-t2;
    VM.sysWrite("cycles: ", apicCycles);
    VM.sysWriteln("  loops: ", pitcnt);
    frequency = (int)apicCycles*(1000/calibrateTimeMs);
    VM.sysWriteln("APIC frequency = ", frequency);
  }

  /* (non-Javadoc)
   * @see org.jam.interfaces.Timer#getTime()
   */
  @Override
  public long getTime()
  {
    // TODO Auto-generated method stub
    return tick;
  }

  /* (non-Javadoc)
   * @see org.jam.interfaces.Timer#handler()
   */
  @Override
  public void handler()
  {
    // TODO Auto-generated method stub
    
  }

  /* (non-Javadoc)
   * @see org.jam.interfaces.Timer#startTimer(long)
   */
  @Override
  public void startTimer(long timeNs)
  {
    // TODO Auto-generated method stub
    
  }

  /* (non-Javadoc)
   * @see org.jam.interfaces.Timer#removeTimer(long)
   */
  @Override
  public RVMThread removeTimer(long timeKey)
  {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.jam.interfaces.Timer#getHandlerStack()
   */
  @Override
  public Address getHandlerStack()
  {
    // TODO Auto-generated method stub
    return null;
  }
  
}
