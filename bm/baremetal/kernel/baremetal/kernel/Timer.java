/*
 * Created on Jul 14, 2004
 *
 * Copyright (C) Joe Kulig, 2004
 * All rights reserved.
 * 
 */
package baremetal.kernel;

/**
 * @author Joe Kulig
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public interface Timer {
  void tick();
  void set(long ms);
  long getTime();
}
