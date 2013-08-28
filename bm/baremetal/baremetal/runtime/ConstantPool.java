/*
 * Created on Jul 27, 2004
 *
 * Copyright (C) Joe Kulig, 2004
 * All rights reserved.
 * 
 */
package baremetal.runtime;

import baremetal.kernel.Memory;


/**
 * @author joe
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
final class ConstantPool {
  private final static int SIZE=0;
  private final static int TAGS=4;
  private final static int DATA=8;
  
  private final static int UNDEFINED = 0;
  private final static int UTF8 = 1;
  private final static int UNICODE = 2;
  private final static int INTEGER = 3;
  private final static int FLOAT = 4;
  private final static int LONG = 5;
  private final static int DOUBLE = 6;
  private final static int CLASS = 7;
  private final static int STRING = 8;
  private final static int FIELDREF = 9;
  private final static int METHODREF = 10;
  private final static int INTERFACEMETHODREF = 11;
  private final static int NAMEANDTYPE = 12;
  private final static int RESOLVEDFLAG = 16;
  private final static int RESOLVEDSTRING = 24;
  private final static int RESOLVEDCLASS = 23;
  
  final static int getSize(int pool) {
    return Memory.readWord(pool);
  }
  
  final static int getTag(int pool, int index) {
    return Array.get8(Memory.readWord(pool+TAGS), index);
  }
  
  final static int getData(int pool, int index) {
    return Array.get32(Memory.readWord(pool+DATA), index);
  }
  
  final static java.lang.String getString(int pool, int index) {
    return Utf8.toString(getData(pool, index));
  }
  final static void  setTag(int pool, int index, int value) {
    Array.set8(Memory.readWord(pool+TAGS), index, value);
  }
  
  final static void setData(int pool, int index, int value) {
    Array.set32(Memory.readWord(pool+DATA), index, value);
  }
  
  final static boolean isString(int pool, int index) {
  	return getTag(pool, index) == STRING;
  }
  final static boolean isClass(int pool, int index) {
    return getTag(pool, index) == CLASS;
  }
  
  final static void resolved(int pool, int index) {
    setTag(pool, index, getTag(pool, index)|RESOLVEDFLAG);
  }
}
