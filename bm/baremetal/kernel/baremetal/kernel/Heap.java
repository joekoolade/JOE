/*
 * Created on Oct 13, 2003
 *
 * Copyright (C) Joe Kulig, 2003
 * All rights reserved.
 * 
 */
package baremetal.kernel;

import baremetal.platform.Console;
import baremetal.runtime.Array;
import baremetal.runtime.Class;


/**
 * @author Joe Kulig
 * 
 * The class that allocates memory.
 * 
 * GC is not implemented so this is rather simplistic, but it will grow more
 * complex as time goes by.
 */
public class Heap {
  static int scratchPadStart = 0x500000;
  static int scratch = scratchPadStart;
  static int memoryStart=0x510000;
	static int heapStart = 0x510000;
	static int heapEnd=32*1024*1024;
	static int heapSize;
  private static final int SIZEOF_OBJECT_PTR = 4;
  
  /*
   * Amount of memory that is free.
   */
  public final static int free() {
    return heapEnd-heapStart;
  }
  
  /*
   * Memory that is allocated
   */
  public final static int used() {
    return heapStart-memoryStart;
  }
  
	/**
	 * @param size
	 * @return
	 */
	public final static int allocate(java.lang.Class klass, int size) {
		int memory = heapStart;
    // zero out the memory
    Memory.memset(memory, 0, size);
		heapStart += size;
    int vtable = Class.getVTable(klass);
    // put vtable in first slot
    Memory.writeWord(memory, vtable);
		return memory;
	}
  
  public final static int allocate(int addr, int size) {
    int memory = heapStart;
    // zero out the memory
    Memory.memset(memory, 0, size);
    heapStart += size;
    // put vtable in first slot
    if(addr != 0) {
      int vtable = Class.getVTable(addr);
      Memory.writeWord(memory, vtable);
    }
    // align heap to an 4 byte boundary
    heapStart = (heapStart+3) & ~3;
    return memory;
    
  }
	public final static int allocatePrimArray(java.lang.Class klass, int count) {
		int array = 0;
		if (count < 0)
			throw new NegativeArraySizeException();
    int eltSize = Class.getElementSize(klass);
    int arrayClass = Class.getArrayClass(klass);
    array = allocate(arrayClass, 12 + (eltSize*count));
    // set up the array length
    Memory.writeWord(array+8, count);
		return array;
	}
  
  public final static int allocateMultiArray(java.lang.Class type, int dim, Object sizeArray) {
    return allocateMultiArray(type, dim, Memory.getAddress(sizeArray));
  }

  public final static int allocateMultiArray(java.lang.Class type, int dim, int sizeArray) {
    /*
     * Check the sizes
     */
    for (int i=0; i < dim; i++) {
      if (Array.get32(sizeArray, i) < 0)
        throw new NegativeArraySizeException();
    }
    java.lang.Class elementType=Class.getComponentType(type);
    int contents;
    if (Class.isPrimitive(elementType)) {
      contents=allocatePrimArray(elementType, Array.get32(sizeArray, 0));
    } else {
      contents=newObjectArray(Array.get32(sizeArray, 0), elementType);
    }

    int size0=Array.get32(sizeArray, 0);
    if (dim > 1) {
      for (int i=0; i < size0; i++) {
        Array.set32(contents, i+3, allocateMultiArray(elementType, dim - 1, sizeArray + 4));
      }
    }
    return contents;
  }
  
	public final static int allocate(int size) {
		return allocate(0, size);
	}
  
  public final static int newObjectArray(int count, java.lang.Class cl) {
   int obj=0;
   
   if(count < 0)
   	throw new NegativeArraySizeException();
   
   /*
    * Array layout is:
    *   0
    *   vtable
    *   length
    *   data 0
    *   ...
    *   data N
    */
   int size = 12;  // vtable and length storage
   
   size += count*SIZEOF_OBJECT_PTR;  // 4 indicates the size of Object
   obj = allocate(Class.getArrayClass(cl), size);
   Memory.writeWord(obj+8, count);
   return obj;
  }
  
  public final static int scratchPad(int size) {
   int start = scratch;
   if(start>=heapStart) {
     Console.writeln("Scratch pad spilled into HEAP!!!");
   }
   // zero out the memory
   Memory.memset(start, 0, size);
   scratch += (size + 3) & ~3;
   return start;
  }
  
  public final static void freeScratchPad() {
   scratch = scratchPadStart; 
  }

  /**
   * @param addr
   * @param size
   * @return
   */
  public static int realloc(int addr, int previousSize, int newSize) {
    int newMem = allocate(newSize);
    Memory.bcopy(addr, newMem, previousSize);
    return newMem;
  }
}
