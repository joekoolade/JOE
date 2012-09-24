//===--------------------------- Lock.java --------------------------------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

package org.j3.mmtk;

import org.vmmagic.unboxed.*;
import org.vmmagic.pragma.*;

@Uninterruptible public class Lock extends org.mmtk.vm.Lock {

  private int state;
  private String name;        // logical name of lock

  // Diagnosis Instance fields
  public Lock(String name) {
    this();
    this.name = name;
  }

  public Lock() {
    state = 0;
  }

  public void setName(String str) {
    name = str;
  }

  public native void acquire();

  public native void check(int w);

  public native void release();
}
