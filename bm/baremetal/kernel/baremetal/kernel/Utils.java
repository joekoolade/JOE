/*
 * Created on Sep 2, 2004
 *
 * Copyright (C) Joe Kulig, 2004
 * All rights reserved.
 * 
 */
package baremetal.kernel;


/**
 * @author joe
 *
 * Common kernel utilities needed by various kernel classes.
 * 
 */
public class Utils {
  
  public static final void ioDelay() {
    for(int i=0; i<500; i++) ;
  }
}
