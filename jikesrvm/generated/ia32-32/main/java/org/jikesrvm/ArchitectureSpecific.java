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
package org.jikesrvm;

import org.vmmagic.unboxed.WordArray;
import org.vmmagic.pragma.NonMoving;

import org.jikesrvm.compilers.baseline.BaselineCompiledMethod;

public class ArchitectureSpecific {
  public static class Assembler extends org.jikesrvm.compilers.common.assembler.ia32.Assembler {
    public Assembler (int bytecodeSize) {
      super(bytecodeSize, false);
    }
    public Assembler (int bytecodeSize, boolean shouldPrint, BaselineCompilerImpl compiler) {
      super(bytecodeSize, shouldPrint, compiler);
    }
    public Assembler (int bytecodeSize, boolean shouldPrint) {
      super(bytecodeSize, shouldPrint);
    }
  }
  public interface ArchConstants extends org.jikesrvm.ia32.ArchConstants {}
  public interface BaselineConstants extends org.jikesrvm.ia32.BaselineConstants {}
  public static final class BaselineExceptionDeliverer extends org.jikesrvm.compilers.baseline.ia32.BaselineExceptionDeliverer {}
  public static final class BaselineGCMapIterator extends org.jikesrvm.compilers.baseline.ia32.BaselineGCMapIterator {
    public BaselineGCMapIterator(WordArray registerLocations) {
      super(registerLocations);
    }}
  public static final class CodeArray extends org.jikesrvm.ia32.CodeArray {
    public CodeArray() { super(0);}
    public CodeArray(int size) { super(size);}
    public static CodeArray create (int size) { // only intended to be called from CodeArray.factory
      if (VM.runningVM) VM._assert(false);  // should be hijacked
      return new CodeArray(size);
    }
  }
  public static final class BaselineCompilerImpl extends org.jikesrvm.compilers.baseline.ia32.BaselineCompilerImpl {
    public BaselineCompilerImpl(BaselineCompiledMethod cm, short[] genLocLoc, short[] floatLocLoc) {
      super(cm /**, genLocLoc, floatLocLoc  */);
    }}
  public static final class DynamicLinkerHelper extends org.jikesrvm.ia32.DynamicLinkerHelper {}
  public static final class InterfaceMethodConflictResolver extends org.jikesrvm.ia32.InterfaceMethodConflictResolver {}
  public static final class LazyCompilationTrampoline extends org.jikesrvm.ia32.LazyCompilationTrampoline {}
  public static final class MachineCode extends org.jikesrvm.ia32.MachineCode {
  
    public MachineCode(ArchitectureSpecific.CodeArray array, int[] bm) {
      super(array, bm);
    }
  //*/
  }
  public static final class MachineReflection extends org.jikesrvm.ia32.MachineReflection {}
  public static final class MultianewarrayHelper extends org.jikesrvm.ia32.MultianewarrayHelper {}
  public static final class OutOfLineMachineCode extends org.jikesrvm.ia32.OutOfLineMachineCode {}
  public static final class ThreadLocalState extends org.jikesrvm.ia32.ThreadLocalState {}
  public interface RegisterConstants extends org.jikesrvm.ia32.RegisterConstants {}
  @NonMoving
  public static final class Registers extends org.jikesrvm.ia32.Registers {}
  public interface StackframeLayoutConstants extends org.jikesrvm.ia32.StackframeLayoutConstants {}
  public interface TrapConstants extends org.jikesrvm.ia32.TrapConstants {}
  public static final class JNICompiler extends org.jikesrvm.jni.ia32.JNICompiler {}
  public static final class JNIGCMapIterator extends org.jikesrvm.jni.ia32.JNIGCMapIterator {
    public JNIGCMapIterator(WordArray registerLocations) {
      super(registerLocations);
    }}
  public static final class JNIHelpers extends org.jikesrvm.jni.ia32.JNIHelpers {}
}
