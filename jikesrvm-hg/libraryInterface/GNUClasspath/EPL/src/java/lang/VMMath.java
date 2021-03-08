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
package java.lang;
import org.jikesrvm.VM;
import org.jikesrvm.runtime.BootRecord;
import org.jikesrvm.runtime.Magic;
import org.vmmagic.pragma.Pure;
import org.vmmagic.pragma.SysCallNative;
import org.vmmagic.unboxed.Address;

/**
 * Pass as much as we can, the work of Math functions onto the C
 * implementations in libm using system call (cheaper) native calls
 */
class VMMath {

  public static double sin(double a) {
    throw new RuntimeException();
  }
  public static double cos(double a) {
      throw new RuntimeException();
  }
  
  public static double tan(double a) {
      throw new RuntimeException();
  }
  
  public static double asin(double a) {
      throw new RuntimeException();
  }
  
  public static double acos(double a) {
      throw new RuntimeException();
  }
  
  public static double atan(double a) {
      throw new RuntimeException();
  }
  
  public static double atan2(double y, double x) {
      throw new RuntimeException();
  }
  
  public static double cosh(double a) {
      throw new RuntimeException();
  }
  
  public static double sinh(double a) {
      throw new RuntimeException();
  }
  
  public static double tanh(double a) {
      throw new RuntimeException();
  }
  
  public static double exp(double a) {
      throw new RuntimeException();
  }
  
  public static double log(double a) {
      throw new RuntimeException();
  }
  
  public static double sqrt(double a) {
    if (VM.BuildForHwFsqrt) {
      return Magic.sqrt(a);
    } else {
        throw new RuntimeException();
    }
  }
  
  public static double pow(double a, double b) {
      throw new RuntimeException();
  }
  
  public static double IEEEremainder(double x, double y) {
      throw new RuntimeException();
  }
  
  public static double ceil(double a) {
    return Magic.dceil(a);
  }
  
  public static double floor(double a) {
      throw new RuntimeException();
  }
  
  public static double rint(double a) {
      throw new RuntimeException();
  }
  
  public static double cbrt(double a) {
      throw new RuntimeException();
  }
  
  public static double expm1(double a) {
      throw new RuntimeException();
  }
  
  public static double hypot(double a, double b) {
      throw new RuntimeException();
  }
  
  public static double log10(double a) {
      throw new RuntimeException();
  }
  
  public static double log1p(double a) {
      throw new RuntimeException();
  }
}


