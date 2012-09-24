//===--------------------------- Debug.java -------------------------------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

package org.j3.mmtk;

import org.vmmagic.pragma.Uninterruptible;

/**
 * Debugger support for the MMTk harness
 */
@Uninterruptible
public final class Debug extends org.mmtk.vm.Debug {

  /**
   * Enable/disable MMTk debugger support
   */
  @Override
  public boolean isEnabled() {
    return false;
  }

}
