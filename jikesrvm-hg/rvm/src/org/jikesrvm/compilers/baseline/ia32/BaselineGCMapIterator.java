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
package org.jikesrvm.compilers.baseline.ia32;

import org.jikesrvm.VM;
import org.jikesrvm.classloader.MethodReference;
import org.jikesrvm.classloader.NormalMethod;
import org.jikesrvm.classloader.TypeReference;
import org.jikesrvm.compilers.baseline.BaselineCompiledMethod;
import org.jikesrvm.compilers.baseline.ReferenceMaps;
import org.jikesrvm.compilers.common.CompiledMethod;
import org.jikesrvm.compilers.common.CompiledMethods;
import org.jikesrvm.ia32.BaselineConstants;
import org.jikesrvm.mm.mminterface.GCMapIterator;
import org.jikesrvm.runtime.DynamicLink;
import org.jikesrvm.runtime.Magic;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Offset;
import org.vmmagic.unboxed.WordArray;

/**
 * Iterator for stack frame  built by the Baseline compiler.
 * <p>
 * An Instance of this class will iterate through a particular
 * reference map of a method returning the offsets of any references
 * that are part of the input parameters, local variables, and
 * java stack for the stack frame.
 */
@Uninterruptible
public abstract class BaselineGCMapIterator extends GCMapIterator implements BaselineConstants {
  private static final boolean TRACE_ALL = false;
  private static final boolean TRACE_DL = false; // dynamic link frames

  /*
   * Iterator state for mapping any stackframe.
   */
  /** Compiled method for the frame */
  private NormalMethod currentMethod;
  /** Compiled method for the frame */
  private BaselineCompiledMethod currentCompiledMethod;
  private int currentNumLocals;
  /** Current index in current map */
  private int mapIndex;
  /** id of current map out of all maps */
  private int mapId;
  /** set of maps for this method */
  private ReferenceMaps maps;
  /** have we reported the base ptr of the edge counter array? */
  private boolean counterArrayBase;

  /** have we processed all the values in the regular map yet? */
  private boolean finishedWithRegularMap;

  /**
   * Constructor. Remember the location array for registers. This array needs to
   * be updated with the location of any saved registers. This information is
   * not used by this iterator but must be updated for the other types of
   * iterators (ones for the opt compiler built frames) The locations are kept
   * as addresses within the stack.
   */
  public BaselineGCMapIterator(WordArray registerLocations) {
    this.registerLocations = registerLocations; // (in superclass)
  }

  /*
   * Interface
   */

  /**
   * Set the iterator to scan the map at the machine instruction offset
   * provided. The iterator is positioned to the beginning of the map. NOTE: An
   * iterator may be reused to scan a different method and map.
   *
   * @param compiledMethod
   *          identifies the method and class
   * @param instructionOffset
   *          identifies the map to be scanned.
   * @param fp
   *          identifies a specific occurrence of this method and allows for
   *          processing instance specific information i.e JSR return address
   *          values
   */
  @Override
  public void setupIterator(CompiledMethod compiledMethod, Offset instructionOffset, Address fp) {
    currentCompiledMethod = (BaselineCompiledMethod) compiledMethod;
    currentMethod = (NormalMethod) currentCompiledMethod.getMethod();
    currentNumLocals = currentMethod.getLocalWords();

    // setup superclass
    //
    framePtr = fp;

    // setup stackframe mapping
    //
    maps = ((BaselineCompiledMethod) compiledMethod).referenceMaps;
    mapId = maps.locateGCPoint(instructionOffset, currentMethod);
    mapIndex = 0;
    if (mapId < 0) {
      // lock the jsr lock to serialize jsr processing
      ReferenceMaps.jsrLock.lock();
      int JSRindex = maps.setupJSRSubroutineMap(mapId);
      while (JSRindex != 0) {
        Address nextCallerAddress = framePtr.plus(convertIndexToOffset(JSRindex)).loadAddress();
        Offset nextMachineCodeOffset = compiledMethod.getInstructionOffset(nextCallerAddress);
        if (VM.TraceStkMaps) {
          VM.sysWriteln("     setupJSRsubroutineMap- nested jsrs end of loop- = ");
          VM.sysWriteln("      next jsraddress offset = ", JSRindex);
          VM.sysWriteln("      next callers address = ", nextCallerAddress);
          VM.sysWriteln("      next machinecodeoffset = ", nextMachineCodeOffset);
          if (nextMachineCodeOffset.sLT(Offset.zero())) {
            VM.sysWriteln("BAD MACHINE CODE OFFSET");
          }
        }
        JSRindex = maps.getNextJSRAddressIndex(nextMachineCodeOffset, currentMethod);
      }
    }
    if (VM.TraceStkMaps || TRACE_ALL) {
      VM.sysWrite("BaselineGCMapIterator setupIterator mapId = ");
      VM.sysWrite(mapId);
      VM.sysWrite(" for ");
      VM.sysWrite(compiledMethod.getMethod());
      VM.sysWrite(".\n");
    }

    reset();
  }

  /**
   * Reset iteration to initial state. This allows a map to be scanned multiple
   * times.
   */
  @Override
  public void reset() {
    mapIndex = 0;
    finishedWithRegularMap = false;

    // setup map to report EBX if this method is holding the base of counter array in it.
    counterArrayBase = currentCompiledMethod.hasCounterArray();
  }

  /**
   * Converts a biased index from a local area into an offset in the stack.
   *
   * @param index index in the local area (biased : local0 has index 1)
   * @return corresponding offset in the stack
   */
  public short convertIndexToLocation(int index) {
    if (index == 0) return 0;
    if (index <= currentNumLocals) { //index is biased by 1;
      return currentCompiledMethod.getGeneralLocalLocation(index - 1);
    } else {
      return currentCompiledMethod.getGeneralStackLocation(index - 1 - currentNumLocals);
    }
  }

  private int convertIndexToOffset(int index) {
    //for ia32: always offset, never registers
    if (index == 0) return 0; //invalid

    // index is biased by 1, index 1 means local 0, this is at offset -BYTES_IN_ADDRESS from startLocalOffset
    int offset = BaselineCompilerImpl.locationToOffset(convertIndexToLocation(index)) - BYTES_IN_ADDRESS; // no jsrbit here
    if (VM.TraceStkMaps) {
      VM.sysWriteln("convertIndexToOffset- input index = ", index, "  offset = ", offset);
    }
    return offset;
  }

  @Override
  public Address getNextReferenceAddress() {
    if (!finishedWithRegularMap) {
      if (counterArrayBase) {
        counterArrayBase = false;
        return registerLocations.get(EBX.value()).toAddress();
      }
      if (mapId < 0) {
        mapIndex = maps.getNextJSRRefIndex(mapIndex);
      } else {
        mapIndex = maps.getNextRefIndex(mapIndex, mapId);
      }

      if (mapIndex != 0)
      {
        int mapOffset = convertIndexToOffset(mapIndex);
        if (VM.TraceStkMaps || TRACE_ALL)
        {
          VM.sysWrite("BaselineGCMapIterator getNextReferenceOffset = ");
          VM.sysWriteHex(mapOffset);
          VM.sysWrite(".\n");
          VM.sysWrite("Reference is ");
        }
        if (VM.TraceStkMaps || TRACE_ALL)
        {
          VM.sysWriteHex(framePtr.plus(mapOffset).loadAddress());
          VM.sysWrite(".\n");
          if (mapId < 0)
          {
            VM.sysWrite("Offset is a JSR return address ie internal pointer.\n");
          }
        }
        return (framePtr.plus(mapOffset));
      }
      else
      {
        // remember that we are done with the map for future calls, and then
        //   drop down to the code below
        finishedWithRegularMap = true;
      }
    }


      // point registerLocations[] to our callers stackframe
      //
      registerLocations.set(EDI.value(), framePtr.plus(EDI_SAVE_OFFSET).toWord());
      registerLocations.set(EBX.value(), framePtr.plus(EBX_SAVE_OFFSET).toWord());
//      if (currentMethod.hasBaselineSaveLSRegistersAnnotation()) {
//        registerLocations.set(EBP.value(), framePtr.plus(EBP_SAVE_OFFSET).toWord());
//      }

    return Address.zero();
  }

  @Override
  public Address getNextReturnAddressAddress() {
    if (mapId >= 0) {
      if (VM.TraceStkMaps || TRACE_ALL) {
        VM.sysWrite("BaselineGCMapIterator getNextReturnAddressOffset mapId = ");
        VM.sysWrite(mapId);
        VM.sysWrite(".\n");
      }
      return Address.zero();
    }
    mapIndex = maps.getNextJSRReturnAddrIndex(mapIndex);
    if (VM.TraceStkMaps || TRACE_ALL) {
      VM.sysWrite("BaselineGCMapIterator getNextReturnAddressOffset = ");
      VM.sysWrite(convertIndexToOffset(mapIndex));
      VM.sysWrite(".\n");
    }
    return (mapIndex == 0) ? Address.zero() : framePtr.plus(convertIndexToOffset(mapIndex));
  }

  /**
   * Cleanup pointers - used with method maps to release data structures early
   * ... they may be in temporary storage i.e. storage only used during garbage
   * collection
   */
  @Override
  public void cleanupPointers() {
    maps.cleanupPointers();
    maps = null;
    if (mapId < 0) {
      ReferenceMaps.jsrLock.unlock();
    }
  }

  @Override
  public int getType() {
    return CompiledMethod.BASELINE;
  }

  /**
   * For debugging (used with checkRefMap)
   */
  public int getStackDepth() {
    return maps.getStackDepth(mapId);
  }
}

