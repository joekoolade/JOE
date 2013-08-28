/*
 * Created on Oct 21, 2004
 *
 * Copyright (C) Joe Kulig, 2004
 * All rights reserved.
 * 
 */
package baremetal.kernel;



/**
 * @author joe
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class InterruptManager {
  public static IrqHandler irq;
  
  public final static void dispatcher(int vector, int[] context) {
    irq.handler(context);
  }
  
}
