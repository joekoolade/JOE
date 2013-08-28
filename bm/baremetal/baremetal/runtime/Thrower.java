/*
 * Created on Sep 24, 2004
 *
 * Copyright (C) Joe Kulig, 2004
 * All rights reserved.
 * 
 */
package baremetal.runtime;

import baremetal.kernel.Memory;
import baremetal.platform.Console;

/**
 * @author joe
 * 
 * fixme: This will eventually end up being allocate in a Thread object
 *  
 */
final public class Thrower {
  // Exception header section pointers. Set in the initial startup.
  public static int ehSection;
  public static int ehSectionEnd;

  //  GCC exception table section
  public static int getSection;
  FrameDesc fde;
  CommonInfo cie;
  ExceptionTable get;

  //  ExceptionTable et;
  int returnAddress;
  int frame;
  int stack;
  Throwable exc;
  private static Thrower theThrower;
  public static int beginCode;
  public static int endCode;

  private int[] unwindHeader;
  private int unwindHeaderPtr;
  
  private Thrower(int returnAddress, int frame, int stack, Throwable exc) {
    this.returnAddress = returnAddress;
    this.frame = frame;
    this.stack = stack;
    this.exc = exc;
    unwindHeader = new int[2];
    unwindHeaderPtr = Memory.getAddress(unwindHeader);
    cie = new CommonInfo();
    fde = new FrameDesc();
    get = new ExceptionTable();
  }

  public final static void throwException(int returnAddress, int frame,
      int stack, Throwable thr) {
    if (theThrower == null)
      theThrower = new Thrower(returnAddress, frame, stack, thr);
    else {
      theThrower.returnAddress = returnAddress;
      theThrower.frame = frame;
      theThrower.stack = stack;
      theThrower.exc = thr;
    }
    theThrower.findFrame();
  }

  /**
   * Find the fde and gcc exc frames that handle the throw
   * 
   * @param returnAddress
   * @return
   */
  private void findFrame() {
    loadCIE();
    findFde();
  }

  private final void tryThrow() {
    get.iterate();
    while (get.hasEntry()) {
      get.nextEntry();
      if (get.canCatch(returnAddress, exc)) {
        unwindHeader[0] = Memory.getAddress(exc);
        Class.doCatch(get.launchPad, frame, stack, get.filter, unwindHeaderPtr+0x10);
      }
    }
  }

  private final void unwind() {
    /*
     * Go up one frame
     */
    int currentFrame = frame;
    stack = currentFrame+8;
    // load the new frame
    frame = Memory.read32(currentFrame);
    // load the new return address
    returnAddress = Memory.read32(currentFrame+4);
  }

  /**
   */
  private final void findFde() {
    // first loop is when we are finished unwinding the the stack
    while (returnAddress >= beginCode && returnAddress < endCode) {
      // need a second loop to go thru the eh section
      while (fde.inRange(returnAddress)) {
        if (fde.containsHandler(returnAddress)) {
          if (fde.hasExceptionTable()) {
            loadGet();
            tryThrow();
          }
        }
        fde.nextFde();
      }
      // unwind the stack to the previous call
      unwind();
      // get the fde
      fde.load(cie.firstFde());
    }
    vmExceptionHandler();
  }

  private final void getNextFde() {
    int addr = cie.firstFde();
    fde.load(addr);
    while (!fde.inRange(returnAddress)) {
      fde.nextFde();
      if(fde.endOfTable())
        vmExceptionHandler();
    }
  }

  /*
   * This is the last chance exception handler. No handler
   * could be found in the tables so the exception is handled
   * as gracefully as can be right here!
   * 
   */
  private final void vmExceptionHandler() {
    Console.writeln("Thrower: Cannot find exception handler!");
  }
  private final void loadGet() {
    get.load(fde.exceptionTable, fde.initialLocation);
  }

  /**
   *  
   */
  private final void loadCIE() {
    cie.load(ehSection);
    fde.load(cie.firstFde());
  }

}
