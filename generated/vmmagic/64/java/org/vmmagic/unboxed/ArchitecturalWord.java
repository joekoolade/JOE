/*
 *  This file is part of the Jikes RVM project (http://jikesrvm.org).
 *
 *  This file is licensed to You under the Eclipse Public License (EPL);
 *  You may not use this file except in compliance with the License. You
 *  may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/eclipse-1.0.php
 *
 *  See the COPYRIGHT.txt file distributed with this work for information
 *  regarding copyright ownership.
 */
package org.vmmagic.unboxed;

/* machine-generated DO NOT EDIT */

import org.jikesrvm.VM;
import org.vmmagic.pragma.Interruptible;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.pragma.RawStorage;

@RawStorage(lengthInWords = true, length = 1)
@Uninterruptible
abstract class ArchitecturalWord {

 //protected final int value;
 protected final long value;

  ArchitecturalWord() {
    this.value = 0;
  }
  ArchitecturalWord(int value, boolean zeroExtend) {
    //this.value = value;
    this.value = (zeroExtend) ? ((long)value) & 0x00000000ffffffffL : value;
  }

  ArchitecturalWord(long value) {
    //if (!(Integer.MIN_VALUE <= value && value <= 0x0ffffffffL)) {
    //  VM.sysWriteln("value ",value," outside 32-bit range");
    //  VM.sysFail("Value too large for 32-bit machine");
    //}
    //this.value = (int)value;
    this.value = value;
  }

  @Override
  @Interruptible
  public String toString() {
    return ""+value;
  }

  @Override
  public int hashCode() {
    return (int)value;
  }

  @Override
  public boolean equals(Object that) {
    if (that instanceof ArchitecturalWord) {
      return value == ((ArchitecturalWord)that).value;
    } else {
      return false;
    } 
  }
}
