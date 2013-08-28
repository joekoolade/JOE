/*
 * Created on Jan 16, 2005
 *
 * Copyright (C) Joe Kulig, 2005
 * All rights reserved.
 * 
 */
package baremetal.runtime;


/**
 * @author joe
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public final class Monitor {
  
  public final static void enter(Object o) {
    Mutex m = getMutex(o);
    if(m==null) {
      m = new Mutex();
      setMutex(o, m);
    }
    m.acquire();
  }
  
  public final static void exit(Object o) {
    Mutex m = getMutex(o);
    m.release();
  }
  
  final static native Mutex getMutex(Object o);
  final static native void setMutex(Object o, Mutex m);
}
