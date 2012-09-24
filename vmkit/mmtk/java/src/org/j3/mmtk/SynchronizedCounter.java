//===---------- SynchronizedCounter.java - Atomic counter -----------------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

package org.j3.mmtk;

/**
 * A counter that supports atomic increment and reset.
 */
public final class SynchronizedCounter extends org.mmtk.vm.SynchronizedCounter {

  private int count = 0;

  public native int reset();
  
  public native int increment();

  public int peek() {
    return count;
  }

}
