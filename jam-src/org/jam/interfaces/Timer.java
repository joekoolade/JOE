/**
 * Created on Apr 18, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.interfaces;

import org.jikesrvm.scheduler.RVMThread;
import org.vmmagic.unboxed.Address;

/**
 * @author Joe Kulig
 *
 */
public interface Timer {
  long getTime();
  void handler();
  void startTimer(long timeNs);
  Address getHandlerStack();
  RVMThread removeTimer(long timeKey);
  boolean removeTimer(RVMThread thread);
}
