/*
 * Created on Jul 13, 2004
 *
 * Copyright (C) Joe Kulig, 2004
 * All rights reserved.
 * 
 */
package baremetal.vm;

import java.io.File;
import java.util.Properties;

/**
 * @author Joe Kulig
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public final class Runtime {

  /**
   * @return
   */
  public static int availableProcessors() {
    // TODO Auto-generated method stub
    return 0;
  }

  /**
   * @return
   */
  public static long freeMemory() {
    // TODO Auto-generated method stub
    return 0;
  }

  /**
   * @return
   */
  public static long totalMemory() {
    // TODO Auto-generated method stub
    return 0;
  }

  /**
   * @return
   */
  public static long maxMemory() {
    // TODO Auto-generated method stub
    return 0;
  }

  /**
   * @param cmd
   * @param env
   * @param dir
   * @return
   */
  public static Process exec(String[] cmd, String[] env, File dir) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @param p
   */
  public static void systemProperties(Properties p) {
    // TODO Auto-generated method stub
    
  }

}
