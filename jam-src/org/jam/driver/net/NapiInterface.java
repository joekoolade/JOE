/**
 * Created on Nov 14, 2017
 *
 * Copyright (C) Joe Kulig, 2017
 * All rights reserved.
 */
package org.jam.driver.net;

/**
 * @author Joe Kulig
 *
 */
public interface NapiInterface {
  void poll();
  /*
   * How much work should be done per poll
   */
  int work();
  /*
   * How often to schedule the poll
   */
  int schedule();
}
