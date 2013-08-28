/*
 * Created on Oct 19, 2004
 *
 * Copyright (C) Joe Kulig, 2004
 * All rights reserved.
 * 
 */
package baremetal.processor;

import baremetal.kernel.Memory;
import baremetal.runtime.Class;

/**
 * @author joe
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public final class Idt {
  int base;
  int codeSegment;
  int limit;
  public static int int104;
  
  public Idt() {
    base = 0;
    codeSegment = 8;
    limit = 0x80;
  }
  
  public void init() {
    int irq = Class.getMethodCode(Pentium.class, "halt");
    for(int i=0; i<limit; i++) {
      loadVector(i, irq);
    }
    loadVector(104, int104);
  }
  
  public void loadVector(int vector, java.lang.Class c, String irq) {
    int irq0 = Class.getMethodCode(c, irq);
    loadVector(vector, irq0);
  }
  /*
   * Writes irq out into an interrupt vector
   */
  public void loadVector(int vector, int irq) {
    Memory.writeWord(base+(vector*8), (codeSegment<<16)|(irq&0xffff));
    Memory.writeWord(base+4+(vector*8), (irq&0xffff0000)|0x8e00);
  }
}
