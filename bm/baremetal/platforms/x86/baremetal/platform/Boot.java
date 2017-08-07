/*
 * Created on Oct 30, 2003
 *
 * Copyright (C) Joe Kulig, 2003
 * All rights reserved.
 * 
 */
package baremetal.platform;

import baremetal.runtime.Class;
import baremetal.runtime.Utf8;
import baremetal.vm.Thread;

/**
 * @author Joe Kulig
 *
 * Do any platform initialization here. All the heavy lifting is 
 * done in the baremetal directive.
 */
public class Boot {
	private static int stack = 0x100000;
	private static boolean booting=true;
   
	public static void init() {
		start();
	}
	
  public static void init(java.lang.Class main) {
    int a=0;
    try {
      start();
      Platform.init();
      Thread.bootThread(main);
    } catch (Throwable t) {
      Console.write(t.getMessage());
    }
    Platform.cpu.halt();
  }
  
	public static void start() {
    Utf8.convertUtfs();
    Class.resolveStringConstants();
    Class.forName("java.lang.Object");
    Class.forName("java.lang.Class");
    Class.forName("java.lang.System");
    Class.forName("java.lang.Runtime");
    
    Class.initializeClasses();
	}

  /**
   * @return
   */
  public static boolean isBooting() {
    return booting;
  }
}
