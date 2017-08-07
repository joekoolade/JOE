/*
 * Created on Apr 16, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */

package baremetal.runtime;

import java.lang.reflect.Modifier;

import baremetal.kernel.Heap;
import baremetal.kernel.Memory;
import baremetal.platform.Console;
import baremetal.platform.Platform;
import baremetal.vm.VMBoolean;
import baremetal.vm.VMByte;
import baremetal.vm.VMChar;
import baremetal.vm.VMDouble;
import baremetal.vm.VMFloat;
import baremetal.vm.VMInt;
import baremetal.vm.VMLong;
import baremetal.vm.VMShort;
import baremetal.vm.VMVoid;

/**
 * @author joe
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class Class {

  // offsets
  private final static int CLASS_VTABLE=0;
  private final static int SYNC_INFO=4;
  private final static int NEXT=8;
  private final static int NAME=12;
  private final static int ACCFLAGS=16;
  private final static int SUPERCLASS=20;
  private final static int CONSTANTS=24;
  private final static int CONST_SIZE=24;
  private final static int CONST_TAGS=28;
  private final static int CONST_DATA=32;
  private final static int METHODS=36;
  private final static int METHOD_COUNT=40;
  private final static int VTABLE_METHOD_COUNT=42;
  private final static int FIELDS=44;
  private final static int SIZE_IN_BYTES=48;
  private final static int FIELD_COUNT=52;
  private final static int STATIC_FIELD_COUNT=54;
  private final static int VTABLE=56;
  private final static int OTABLE=60;
  private final static int OTABLE_SYMS=64;
  private final static int ATABLE=68;
  private final static int ATABLE_SYMS=72;
  private final static int CATCH_CLASSES=76;
  private final static int INTERFACES=80;
  private final static int LOADER=84;
  private final static int INTERFACE_COUNT=88;
  private final static int STATE=90;
  private final static int THREAD=92;
  private final static int DEPTH=96;
  private final static int ANCESTORS=100;
  private final static int IDT=104;
  private final static int ARRAY_CLASS=108;
  private final static int PROTECTION_DOMAIN=112;
  private final static int HACK_SIGNERS=116;
  private final static int CHAIN=120;
  private final static int CLASS_SIZE=124;
  // Class initialization states
  private final static int JV_STATE_NOTHING=0;
  private final static int JV_STATE_PRELOADING=1;
  private final static int JV_STATE_LOADING=3;
  private final static int JV_STATE_LOADED=5;
  private final static int JV_STATE_COMPILED=6;
  private final static int JV_STATE_PREPARED=7;
  private final static int JV_STATE_LINKED=9;
  private final static int JV_STATE_IN_PROGRESS=10;
  private final static int JV_STATE_ERROR=12;
  private final static int JV_STATE_DONE=14;
  private final static int JV_CONST_UNDEFINED=0;
  private final static int JV_CONST_STRING=8;
  private final static int JV_CONST_RESOLVEDSTRING=24;
  private final static int PRIMITIVE_VTABLE=-1;
  private final static int VTABLE_SIZE=16;
  private final static int VTABLE_CLASS=8;
  private final static int VTABLE_GCDESC=12;
  private final static int VTABLE_METHODS=16;
  private final static int OBJECT_METHODS_NUM=5;
  public static java.lang.Class javaObjectClass;
  public static java.lang.Class arrayInterfaceClass;
  private static int aifIdt;
  private static int aifDepth;
  private static int aifAncestors;
  private static final int SIZEOF_SHORT=2;
  private static final int NULLPTR=0;
  private static final int SIZEOF_METHOD=20;
  private final static boolean DEBUG=false;

  // these will be set by the glue logic
  public static int jcrStart;
  public static int jcrEnd;
  private static final boolean DEBUG1=false;
  private static final boolean DEBUG2=false;
  private static final boolean DEBUG3=false;
  private static final boolean DEBUG4=false;
  // debug resolveConstants()
  private static final boolean DEBUG5=false;
  // debug findClassFromSignature()
  private static final boolean DEBUG6=false;


  public static final void initialize(java.lang.Class c) {
    int address=Memory.getAddress(c);
    initialize(address);
  }

  private final static int getState(int cl) {
    return Memory.readByte(cl + STATE);
  }

  private final static void setState(int cl, int value) {
    Memory.writeByte(cl + STATE, value);
  }

  private final static int findClass(java.lang.String name, int start, int end) {
    /*
     * TODO: implement a caching mechanism
     */
    int numClasses=(jcrEnd - jcrStart) / 4;
    for (int i=0; i < numClasses; i++) {
      int cls=Array.get32(jcrStart, i);
      if (name.regionMatches(0,getName(cls), start, end-start)) {
        return cls;
      }
    }
    return NULLPTR;
  }

  /**
   * @param name
   *          utf string
   * @param start
   *          beginning of string
   * @param end
   *          end of string
   * @return pointer to class
   */
  private final static int findClass(java.lang.String name) {
    /*
     * This is more intensive. Have to compare character by character.
     */
    int numClasses=(jcrEnd - jcrStart) / 4;
    nextClass:
    for (int i=0; i < numClasses; i++) {
      int currClass=Array.get32(jcrStart, i);
      if (name.equals(getName(currClass))) {
        return currClass;
      }
    }
    return NULLPTR;
  }

  private static final void initialize(int cl) {
    if (getState(cl) >= JV_STATE_IN_PROGRESS)
      return;
    setState(cl, JV_STATE_IN_PROGRESS);
    if (isPrimitive(cl))
      return;
    resolveClassConstants(cl);
    prepareConstTimeTables(cl);
    if (getVTable(cl) == NULLPTR)
      makeVTable(cl);
    if (getOTable(cl) != NULLPTR || getATable(cl) != NULLPTR)
      linkSymbolTable(cl);
    linkExceptionClassTable(cl);
    invokeMethod(cl, "<clinit>");
    setState(cl, JV_STATE_DONE);
  }

  /**
   * Intializes all classes that are in .jcr section of the executable.
   *  
   */
  public final static void initializeClasses() {
    int aClass=0;
    for (int next=jcrStart; next < jcrEnd; next+=4) {
      aClass=Memory.readWord(next);
      initialize(aClass);
    }
  }

  /**
   * Resolve all the class String constants
   */
  public final static void resolveStringConstants() {
    int aClass = 0;
    for(int next = jcrStart; next<jcrEnd; next+=4) {
      aClass = Memory.read32(next);
      resolveStringConstants(aClass);
    }
  }

  public final static void resolveStringConstants(int cl) {
    /*
     * Constant data holds a bunch of utfs. These utfs point to
     * a String object. Take that string object and put it into the
     * the constant pool
     * 
     * utf->String object
     * 
     */
    int pool=cl + CONSTANTS;
    int size=ConstantPool.getSize(pool);
    int index;
    for (index=1; index < size; ++index) {
        int utf=ConstantPool.getData(pool, index);
        ConstantPool.setData(pool, index, Memory.read32(utf));
        ConstantPool.resolved(pool, index);
    }
    
  }
  public final static int getMethodCode(java.lang.Class cl1, java.lang.String name) {
    /*
     * Assume this is a utf string
     */
    int cl = Memory.getAddress(cl1);
    int methodCnt=getMethodCount(cl);
    for (int i=0; i < methodCnt; i++) {
      if (name.equals(getMethodName(cl, i))) {
        return Method.getCode(getMethod(cl, i));
      }
    }
    return 0;
    
  }
  public final static void invokeMethod(java.lang.Class cl, java.lang.String name) {
    invokeMethod(Memory.getAddress(cl), name);
  }

  /**
   * @param string
   */
  private final static void invokeMethod(int cl, java.lang.String name) {
    /*
     * Assume this is a utf string
     */
    int methodCnt=getMethodCount(cl);
    for (int i=0; i < methodCnt; i++) {
      if (name.equals(getMethodName(cl, i))) {
        invokeMethod(Method.getCode(getMethod(cl, i)));
        return;
      }
    }
  }

  public static final java.lang.Class forName(java.lang.String name) {
      for (int index=jcrStart; index < jcrEnd; index+=4) {
        int aClass=Memory.readWord(index);
        if (name==getName(aClass)) {
          initialize(aClass);
          return toClass(aClass);
        }
      }
    return null;
  }

  public final static int lookupInterfaceMethodIdx(java.lang.Class cl, java.lang.Class iface, int idx) {
    return lookupInterfaceMethodIdx(Memory.getAddress(cl), Memory.getAddress(iface), idx);
  }
  
  public final static int lookupInterfaceMethodIdx(int cl, int iface, int idx) {
    int clIdt = getIdt(cl);
    int ifaceIdt = getIdt(iface);
    int ioffsets = Idt.getIoffsets(ifaceIdt);
    int iindex = Idt.getIindex(clIdt);
    int idx0 = Array.get16(ioffsets, iindex) + idx;
    
    return Array.get32(Idt.getItable(clIdt), idx0);
  }
  
  private final static native java.lang.Class toClass(int addr);

  private final static void resolveClassConstants(int theClass) {
    /*
     * This only resolves constant classes.
     */
    if (getState(theClass) == JV_STATE_LINKED)
      return;

    int pool=theClass + CONSTANTS;
    int size=ConstantPool.getSize(pool);
    setState(theClass, JV_STATE_LINKED);
    int index;
    for (index=1; index < size; ++index) {
      if (ConstantPool.isClass(pool, index)) {
        java.lang.String name=ConstantPool.getString(pool, index);
        int found=0;
        if (name.charAt(0) == '[')
          found=findClassFromSignature(name);
        else
          found=findClass(name);

        if (found == 0) {
          throw new NoClassDefFoundError();
        }

        ConstantPool.setData(pool, index, found);
        ConstantPool.resolved(pool, index);
      }
    }
  }

  /**
   * 
   * Returns the class from the java signature
   * 
   * @param name
   * @return class address
   */
  private final static int findClassFromSignature(java.lang.String sig, int index) {
    char firstChar=sig.charAt(index);
    switch (firstChar) {
    case 'B' :
      return Memory.getAddress(VMByte.class);
    case 'S' :
      return Memory.getAddress(VMShort.class);
    case 'I' :
      return Memory.getAddress(VMInt.class);
    case 'J' :
      return Memory.getAddress(VMLong.class);
    case 'Z' :
      return Memory.getAddress(VMBoolean.class);
    case 'C' :
      return Memory.getAddress(VMChar.class);
    case 'F' :
      return Memory.getAddress(VMFloat.class);
    case 'D' :
      return Memory.getAddress(VMDouble.class);
    case 'V' :
      return Memory.getAddress(VMVoid.class);
    case 'L' : {
      int i;
      for (i=index + 1;; ++i) {
        char c=sig.charAt(i);
        if (c == 0 || c == ';')
          break;
      }
      return findClass(sig, index + 1, i);
    }
    case '[' :
      int cl=findClassFromSignature(sig, index + 1);
      if (cl == NULLPTR)
        return NULLPTR;
      return getArrayClass(cl);
    }
    return NULLPTR;
  }

  private final static int findClassFromSignature(java.lang.String sig) {
    return findClassFromSignature(sig, 0);
  }

  /**
   * @param address
   */
  private final static void linkExceptionClassTable(int self) {
    //    struct _Jv_CatchClass *catch_record = self->catch_classes;
    int catchRecord=getCatchClasses(self);
    if (catchRecord == 0 || CatchClass.getClassName(catchRecord) != null)
      return;
    //    if (!catch_record || catch_record->classname)
    //      return;
    //    catch_record++;
    catchRecord+=4;
    //    while (catch_record->classname)
    //      {
    //        jclass target_class = _Jv_FindClass (catch_record->classname,
    //                                             self->getClassLoaderInternal ());
    //        *catch_record->address = target_class;
    //        catch_record++;
    //      }
    while (CatchClass.getClassName(catchRecord) != null) {
      int targetClass=findClass(CatchClass.getClassName(catchRecord));
      CatchClass.setAddress(catchRecord, targetClass);
      catchRecord+=4;
    }
    //    self->catch_classes->classname = (_Jv_Utf8Const *)-1;
    CatchClass.setClassName(getCatchClasses(self), -1);
  }

  /**
   * @param self
   * @return
   */
  private final static int getCatchClasses(int self) {
    return Memory.readWord(self + CATCH_CLASSES);
  }

  /**
   * @param address
   */
  private final static void linkSymbolTable(int address) {
    // TODO Auto-generated method stub

  }

  /**
   * @param address
   * @return
   */
  private final static int getATable(int address) {
    // TODO Auto-generated method stub
    return 0;
  }

  /**
   * @param address
   * @return
   */
  private final static int getOTable(int address) {
    // TODO Auto-generated method stub
    return 0;
  }

  /**
   * @param address
   */
  private final static void makeVTable(int cl) {
    if (getVTable(cl) != NULLPTR || isInterface(cl) || Modifier.isAbstract(getAccessFlags(cl))) {
      return;
    }
    if (getVTableMethodCnt(cl) == -1)
      layoutVTableMethods(cl);

    // allocate the table
    int vtableMethods=getVTableMethodCnt(cl);
    int vtable=VTable.allocate(vtableMethods);
    setVTable(cl, vtable);
    boolean flags[]=new boolean[vtableMethods];
    for (int i=0; i < vtableMethods; i++) {
      flags[i]=false;
    }

    // copy vtable of the closet non-abstract superclass
    int superClass=getSuperClass(cl);
    if (superClass != NULLPTR) {
      while (Modifier.isAbstract(getAccessFlags(superClass)))
        superClass=getSuperClass(superClass);
      if (getVTable(superClass) == NULLPTR) {
        makeVTable(superClass);
      }
      int scvtable=getVTable(superClass);
      for (int i=0; i < getVTableMethodCnt(superClass); i++) {
        VTable.setMethod(vtable, i, VTable.getMethod(scvtable, i));
        flags[i]=true;
      }
    }
    VTable.setClass(vtable, cl);
    VTable.setGCDesc(vtable, 0);
    setVTableEntries(cl, vtable, flags);

    /*
     * It's an error to have an abstract method in a concrete class
     */
    if (!Modifier.isAbstract(getAccessFlags(cl))) {
      for (int i=0; i < vtableMethods; i++) {
        if (!flags[i]) {
          // throw abstract Method error
        }
      }
    }
  }

  /**
   * @param cl
   * @param vtable2
   * @param flags
   */
  private final static void setVTableEntries(int cl, int vtable, boolean[] flags) {
    int superClass=getSuperClass(cl);
    if (superClass != NULLPTR && Modifier.isAbstract(getAccessFlags(superClass)))
      setVTableEntries(superClass, vtable, flags);
    int methodCount=getMethodCount(cl);
    for (int i=methodCount - 1; i >= 0; i--) {
      int meth=getMethod(cl, i);
      int index=Method.getIndex(meth);
      if (index == -1)
        continue;
      if (Modifier.isAbstract(Method.getAccessFlags(meth))) {
        /*
         * Set to abstract method err
         */
        VTable.setMethod(vtable, index, 0);
        flags[index]=false;
      } else {
        VTable.setMethod(vtable, index, Method.getCode(meth));
        flags[index]=true;
      }
    }
  }

  /**
   * @param cl
   * @param i
   * @return
   */
  private final static int getMethod(int cl, int i) {
    int methodTable=Memory.readWord(cl + METHODS);
    return methodTable + (i * SIZEOF_METHOD);
  }

  /**
   * @param cl
   * @param vtable2
   */
  private final static void setVTable(int cl, int vtable) {
    Memory.writeWord(cl + VTABLE, vtable);
  }

  /**
   * @param cl
   */
  private final static void layoutVTableMethods(int cl) {
    // TODO Auto-generated method stub

  }

  /**
   * @param cl
   * @return
   */
  private final static int getVTableMethodCnt(int cl) {
    return Memory.readHalfWord(cl + VTABLE_METHOD_COUNT);
  }

  /**
   * @param cl
   * @return
   */
  private final static int getAccessFlags(int cl) {
    return Memory.readHalfWord(cl + ACCFLAGS);
  }

  /**
   * @param address
   * @return
   */
  public final static int getVTable(int cl) {
    return Memory.readWord(cl + VTABLE);
  }

  /*
   * Call the class initialzer and set state to DONE.
   */
  public final static void classInit(java.lang.Class cl) {
    int classAddr=Memory.getAddress(cl);
    int methodTable=Memory.readWord(classAddr + METHODS);
    // Probably should confirm this but it seems like the class
    // initializer, <clinit>, is the last method in the table
    int classInitMethod=Memory.readHalfWord(classAddr + METHOD_COUNT);
    classInitMethod=(classInitMethod - 1) * 20;
    int clinit=Memory.readWord(methodTable + classInitMethod + 12);
    // Set class state to DONE
    invokeMethod(clinit);
    setState(classAddr, JV_STATE_DONE);
  }

  /**
   * @param classInitMethod
   */
  final native static void invokeMethod(int classInitMethod);
  final native static void doCatch(int landingPad, int frame, int stack, int handler, int unwindHdr);
  
  private final static void resolve(int address) {
    int utfPtr;
    int poolSize=Memory.readWord(address + CONST_SIZE);
    int dataPtr=Memory.readWord(address + CONST_DATA);
    int tagPtr=Memory.readWord(address + CONST_TAGS);
    for (int i=0; i < poolSize; i++) {
      int tag=Memory.readByte(tagPtr);
      if (tag == JV_CONST_STRING) {
        utfPtr=Memory.readWord(dataPtr);
        java.lang.String s=newStringUtf8Const(utfPtr);
        Memory.writeWord(dataPtr, Memory.getAddress(s));
        Memory.writeByte(tagPtr, JV_CONST_RESOLVEDSTRING);
      }
      tagPtr+=1;
      dataPtr+=4;
    }
  }

  public final static void resolveStrings(java.lang.Class cl) {
    resolve(Memory.getAddress(cl));
  }

  /**
   * @param utfPtr
   * @return
   */
  private final static java.lang.String newStringUtf8Const(int utfPtr) {
    int size=Memory.readHalfWord(utfPtr + 2);
    char[] chs=new char[size];
    for (int i=0; i < size; i++) {
      chs[i]=(char) Memory.readByte(utfPtr + 4 + i);
    }
    java.lang.String s=new java.lang.String(chs, 0, size, true);
    return s;
  }

  public final static int getArrayClass(java.lang.Class cl) {
    int addr=Memory.getAddress(cl);
    return getArrayClass(addr);
  }

  public final static int getArrayClass(int cl) {
    int arrayClass=Memory.readWord(cl + ARRAY_CLASS);
    if (arrayClass == 0) {
      makeNewArrayClass(cl);
      arrayClass=Memory.readWord(cl + ARRAY_CLASS);
    }
    return arrayClass;
  }

  public final static void setArrayClass(int cl, int value) {
    Memory.writeWord(cl+ARRAY_CLASS, value);
  }
  
  public final static int getVTable(java.lang.Class klass) {
    return Memory.readWord(Memory.getAddress(klass) + VTABLE);
  }

  public final static int getElementSize(java.lang.Class cl) {
    int addr=Memory.getAddress(cl);
    return Memory.readWord(addr + SIZE_IN_BYTES);
  }

  /**
   * @param cl
   * @return
   */
  public final static boolean isPrimitive(java.lang.Class cl) {
    return getVTable(cl) == PRIMITIVE_VTABLE;
  }

  public final static void initPrimitive(java.lang.Class primClass, char sig, int len,
      int arrayTable) {
    int addr=Memory.getAddress(primClass);
    Memory.writeHalfWord(addr + ACCFLAGS, Modifier.PUBLIC | Modifier.FINAL
        | Modifier.ABSTRACT);
    Memory.writeHalfWord(addr + METHOD_COUNT, sig);
    Memory.writeWord(addr + SIZE_IN_BYTES, len);
    Memory.writeWord(addr + VTABLE, PRIMITIVE_VTABLE);
    setState(addr, JV_STATE_DONE);
    setDepth(addr, -1);
//    if (sig != 'V')
//      makeNewArrayClass(primClass, arrayTable);
  }

  private final static java.lang.String getName(int cl) {
    int utf = Memory.readWord(cl + NAME);
    return Utf8.toString(Memory.readWord(utf));
  }

  public final static java.lang.String getName(java.lang.Class c) {
    int cl = Memory.getAddress(c);
    int utf = Memory.readWord(cl + NAME);
    java.lang.String className = Utf8.toString(Memory.readWord(utf));
    if(!(className instanceof java.lang.String)) {
      // must be an array class
      className = getArrayName(utf);
    }
    return className;
  }

  /*
   * There is no indirection for a class array name.
   */
  private final static java.lang.String getArrayName(int cl) {
    return Utf8.toString(Memory.read32(cl+NAME));
  }
  /**
   * @param addr
   * @param arrayTable
   */
  final static void makeNewArrayClass(java.lang.Class cl) {
    makeNewArrayClass(Memory.getAddress(cl));
  }

  private final static void makeNewArrayClass(int cl) {
    if (Memory.readWord(cl + ARRAY_CLASS) != 0)
      return;
    java.lang.String name=getName(cl);
    int len=name.length();
    char[] sig=new char[len + 5];
    int index=0;
    sig[index++]='[';
    int data0=name.charAt(0);
    if (isPrimitive(cl)) {
      sig[index++]=(char) getMethodCount(cl);
    } else {
      if (data0 != '[')
        sig[index++]='L';
      for (int i=0; i < len; i++) {
        sig[index++]=(char) name.charAt(i);
      }
      if (data0 != '[')
        sig[index++]=';';
    }
    java.lang.String arrayName=new java.lang.String(sig, 0, index, true);
    // allocate the array class object
    int arrayClass=Heap.allocate(CLASS_SIZE);
    // set the array class
    setArrayClass(cl, arrayClass);
    // set the name
    setName(arrayClass, arrayName);
    int objectClass=Memory.getAddress(javaObjectClass);
    Memory.writeWord(arrayClass + SUPERCLASS, objectClass);
    // Create a new vtable
    int vtableMethodsCnt=getVTableMethodCnt(cl);
    int arrayVtable=VTable.allocate(vtableMethodsCnt);
    VTable.setClass(arrayVtable, arrayClass);
    // Copy over Object vtable methods
    int objectVtable=Memory.readWord(objectClass + VTABLE);
    for (int i=0; i < OBJECT_METHODS_NUM; i++) {
      int vtableMethod=VTable.getMethod(objectVtable, i);
      VTable.setMethod(arrayVtable, i, vtableMethod);
    }
    Memory.writeWord(arrayClass + VTABLE, arrayVtable);
    Memory.writeHalfWord(arrayClass + VTABLE_METHOD_COUNT, OBJECT_METHODS_NUM);
    Memory.writeWord(arrayClass + METHODS, cl);
    int arrayIfClass=Memory.getAddress(arrayInterfaceClass);
    int arrayInterface=Memory.readWord(arrayIfClass + INTERFACES);
    int arrayInterfaceCnt=Memory.readHalfWord(arrayIfClass + INTERFACE_COUNT);
    // use the ArrayInterface class interface structure
    Memory.writeWord(arrayClass + INTERFACES, arrayInterface);
    Memory.writeWord(arrayClass + INTERFACE_COUNT, arrayInterfaceCnt);
    if (aifIdt == 0) {
      prepareConstTimeTables(arrayClass);
      aifIdt=Memory.readWord(arrayClass + IDT);
      aifDepth=Memory.readWord(arrayClass + DEPTH);
      aifAncestors=Memory.readWord(arrayClass + ANCESTORS);
    }
    Memory.writeWord(arrayClass + IDT, aifIdt);
    Memory.writeWord(arrayClass + DEPTH, aifDepth);
    Memory.writeWord(arrayClass + ANCESTORS, aifAncestors);
  }

  /**
   * @param arrayClass
   * @param arrayName
   */
  private final static void setName(int cl, java.lang.String name) {
    Memory.write32(cl+NAME, Memory.getAddress(name));
  }
  private final static void setDepth(int cl, int val) {
    Memory.writeHalfWord(cl + DEPTH, val);
  }

  private final static void setAncestors(int cl, int val) {
    Memory.writeWord(cl + ANCESTORS, val);
  }

  /**
   * @param arrayIfClass
   */
  private final static void prepareConstTimeTables(int clas) {
    if (isPrimitive(clas) || isInterface(clas))
      return;
    if (getIdt(clas) != 0 || getDepth(clas) != 0)
      return;
    int object=Memory.getAddress(javaObjectClass);
    int klass=clas;
    int hasInterfaces=0;
    int depth=0;
    while (klass != object) {
      hasInterfaces+=getInterfaceCount(klass);
      klass=getSuperClass(klass);
      depth++;
    }
    setDepth(clas, depth);
    if (DEBUG1) {
      Console.write("depth: ");
      Console.writeln(depth);
    }
    int ancestorTable=Heap.allocate(depth * 4);
    setAncestors(clas, ancestorTable);
    klass=clas;
    for (int index=0; index < depth; index++) {
      Array.set32(ancestorTable, index, klass);
      klass=getSuperClass(klass);
    }
    if (DEBUG1)
      printAncestors(clas);
    if (isAbstract(clas))
      return;
    // use a predefined null table
    if (hasInterfaces == 0) {
      setIdt(clas, Idt.nullIdt);
      return;
    }
    if (DEBUG1) {
      Console.write("interface #: ");
      Console.writeln(hasInterfaces);
    }
    int idt=Heap.allocate(Idt.IDT_SIZE);
    setIdt(clas, idt);
    int ifaces=Heap.scratchPad(Iface.IFACES_SIZE);
    Iface.setCount(ifaces, 0);
    Iface.setDefaultLen(ifaces);
    int ifacesList=Heap.scratchPad(Iface.INITIAL_LEN * 4);
    Iface.setList(ifaces, ifacesList);

    int itableSize=getInterfaces(clas, ifaces);
    int ifacesCount=Iface.getCount(ifaces);
    if (DEBUG1) {
      Console.write("ifaces #: ");
      Console.write(ifacesCount);
      Console.writeCh(' ');
      Console.writeln(itableSize);
    }
    if (ifacesCount > 0) {
      int itable=Heap.allocate(itableSize * 4);
      Idt.setItable(idt, itable);
      Idt.setLen(idt, itableSize);
      int itableOffsets=Heap.allocate(ifacesCount * 2);
      generateITable(clas, ifaces, itableOffsets);
      //      ifacesCount = Iface.getCount(ifaces);
      int clsIIndex=findIIndex(ifacesList, itableOffsets, ifacesCount);
      if (DEBUG1) {
        Console.write("Class index: ");
        Console.writeln(clsIIndex);
      }
      for (int i=0; i < ifacesCount; i++) {
        // ifaces.list[i]->idt
        int ifaceIdt=getIdt(Array.get32(ifacesList, i));
        // idt->iface.ioffsets
        int idtIoffsets=Idt.getIoffsets(ifaceIdt); // + (clsIIndex << 1));
        // ioffsets[cls_index] = itable_offsets[i]
        Array.set16(idtIoffsets, clsIIndex, Array.get16(itableOffsets, i));
      }
      Idt.setIindex(idt, clsIIndex);
      Heap.freeScratchPad();
    } else {
      Idt.setIindex(idt, 0xffff);
    }
    //    Console.write("pctt");
  }

  /**
   * @param clas
   */
  private final static void printAncestors(int clas) {
    int ancestors=getAncestors(clas);
    int depth=getDepth(clas);
    for (int i=0; i < depth; i++) {
      int c=Array.get32(ancestors, i);
      Console.write(getName(c));
      Console.writeln();
    }

  }

  /**
   * @param clas
   * @return
   */
  private static int getAncestors(int clas) {
    return Memory.readWord(clas + ANCESTORS);
  }

  /**
   * @param ifacesList
   * @param itableOffsets
   * @param ifacesCount
   * @return
   */
  private final static int findIIndex(int ifacesList, int itableOffsets, int ifacesCount) {
    int i=0, j;
    found : {
      for (i=1;; i++) { // for each potential position in ioffsets
        for (j=0;; j++) { // for each iface
          if (j >= ifacesCount)
            break found;
          int currentIface=Array.get32(ifacesList, j);
          int idt=getIdt(currentIface);
          int offsetsArray=Idt.getIoffsets(idt);
          short offsetLen=(short) Memory.readHalfWord(offsetsArray);
          if (i >= offsetLen)
            continue;
          short ioffset=(short) Array.get16(offsetsArray, i);
          if (ioffset >= 0 && ioffset != (short) Array.get16(itableOffsets, j))
            break; // Nope, try next class
        }
      }
    }
    for (j=0; j < ifacesCount; j++) {
      int currentIface=Array.get32(ifacesList, j);
      int idt=getIdt(currentIface);
      int offsetsArray=Idt.getIoffsets(idt);
      int len=Memory.readHalfWord(offsetsArray);
      if (DEBUG2) {
        Console.write("Iindex: ");
        Console.write(i);
        Console.writeCh(' ');
        Console.write(len);
        Console.writeCh(' ');
        Console.write(getName(currentIface));
        Console.writeln();
      }
      if (i >= len) {
        // resize the offsets array
        int newLen=len << 1;
        if (i > newLen)
          newLen=i + 3;
        int oldOffsets=offsetsArray;
        int newOffsets=Heap.realloc(oldOffsets, len * SIZEOF_SHORT, newLen * SIZEOF_SHORT);
        Memory.writeHalfWord(newOffsets, newLen);
        while (len < newLen) {
          Array.set16(newOffsets, len, -1);
          len++;
        }
        Idt.setIoffsets(idt, newOffsets);
        offsetsArray=newOffsets;
      }
      Array.set16(offsetsArray, i, Array.get16(itableOffsets, j));
    }
    return i;
  }

  /**
   * @param clas
   * @param ifaces
   * @param itableOffsets
   */
  private final static void generateITable(int clas, int ifaces, int itableOffsets) {
    int classIdt=getIdt(clas);
    int itable=Idt.getItable(classIdt);
    int itablePos=0;
    int ifacesCnt=Iface.getCount(ifaces);
    for (int i=0; i < ifacesCnt; i++) {
      int anIface=Iface.getIfaceAt(ifaces, i);
      if (DEBUG3) {
        Console.write("Appending iface: ");
        Console.write(getName(anIface));
        Console.writeln();
      }
      Array.set16(itableOffsets, i, itablePos);
      itablePos=appendPartialITable(clas, anIface, itable, itablePos);
      if (DEBUG3) {
        Console.write("itablePos: ");
        Console.writeln(itablePos);
      }
      // create idt for iface
      if (getIdt(anIface) == 0) {
        if (DEBUG3) {
          Console.write("Creating interface idt for: ");
          Console.write(getName(anIface));
          Console.writeln();
        }
        setIdt(anIface, Heap.allocate(Idt.IDT_SIZE));
        /*
         * Allocate the ioffsets table
         */
        int ioffsets=Heap.allocate(Idt.INITIAL_IOFFSETS_LEN * SIZEOF_SHORT);
        // table length occupies the first slot
        Array.set16(ioffsets, 0, Idt.INITIAL_IOFFSETS_LEN);
        // initialize the rest to -1
        for (int j=1; j < Idt.INITIAL_IOFFSETS_LEN; j++) {
          Array.set16(ioffsets, j, -1);
        }
        Idt.setIoffsets(getIdt(anIface), ioffsets);
      }
    }
  }

  /**
   * @param anIface
   * @param i
   */
  private final static void setIdt(int cl, int value) {
    Memory.writeWord(cl + IDT, value);
  }

  /**
   * @param clas
   * @param iface
   *          interface class
   * @param itable
   * @param itablePos
   * @return
   */
  private final static int appendPartialITable(int clas, int iface, int itable, int pos) {
    Array.set32(itable, pos, iface);
    pos++;
    int meth;
    int methodCnt=getMethodCount(iface);
    for (int j=0; j < methodCnt; j++) {
      meth=NULLPTR;
      for (int cl=clas; cl != 0; cl=getSuperClass(cl)) {
        //        Console.write("Looking for method: ");
        //        Console.writeUtf(getMethodName(iface,j)); Console.writeln();
        meth=getMethodLocal(cl, getMethodName(iface, j), getMethodSig(iface, j));
        if (meth != NULLPTR) {
          break;
        }
      }
      if(meth == NULLPTR) {
        /*
         * Set the table to throw a NoSuchMethodError
         */
        Console.write("appendPartialItable: no such method: ");
        Console.writeln();
        pos++;
        continue;
      }
      java.lang.String methodName=Method.getName(meth);
      if (methodName!=null && methodName.charAt(0) == '<') {
        // leave a place holder for hidden init methods
        Array.set32(itable, pos, NULLPTR);
      } else {
        int accFlags=Method.getAccessFlags(meth);
        if (Modifier.isStatic(accFlags)) {
          Console.writeln("apit: static");
          throw new IncompatibleClassChangeError(methodName);
        }
        if (Modifier.isAbstract(accFlags)) {
          Console.writeln("apit: abstract");
          throw new AbstractMethodError(methodName);
        }
        if (!Modifier.isPublic(accFlags)) {
          Console.writeln("apit: not public");
          throw new IllegalAccessError(methodName);
        }
        Array.set32(itable, pos, Method.getCode(meth));
      }
      pos++;
    }
    return pos;
  }

  /**
   * @param cl
   * @param i
   * @param object
   * @return
   */
  private final static int getMethodLocal(int cl, java.lang.String name, java.lang.String sig) {
    int methodCount=getMethodCount(cl);
    for (int i=0; i < methodCount; i++) {
      if (name == getMethodName(cl, i) && sig == getMethodSig(cl, i)) {
        return getMethod(cl, i);
      }
    }
    return NULLPTR;
  }

  /**
   * @param meth
   *          string address pointer
   * @return
   */
  private final native static java.lang.String getMethodString(int meth);

  /**
   * @param iface
   * @param j
   * @return
   */
  private final static java.lang.String getMethodName(int cl, int methodIndex) {
    int method=getMethod(cl, methodIndex);
    return Method.getName(method);
  }

  /**
   * @param iface
   * @param j
   * @return
   */
  private final static java.lang.String getMethodSig(int cl, int methodIndex) {
    int method=getMethod(cl, methodIndex);
    return Method.getSignature(method);
  }

  /**
   * @param cl
   * @return
   */
  private final static int getSuperClass(int cl) {
    return Memory.readWord(cl + SUPERCLASS);
  }

  /**
   * @param cl
   *          class
   * @return
   */
  private final static int getMethodCount(int cl) {
    return Memory.readHalfWord(cl + METHOD_COUNT);
  }

  /**
   * Returns the class' idt field
   * 
   * @param clas
   * @return
   */
  private final static int getIdt(int clas) {
    return Memory.readWord(clas + IDT);
  }

  private final static int getDepth(int cl) {
    return Memory.readHalfWord(cl + DEPTH);
  }

  private final static short indexOf(int item, int list, int listLen) {
    for (int i=0; i < listLen; i++) {
      if (Array.get32(list, i) == item) {
        return (short) i;
      }
    }
    return -1;
  }

  private final static int getInterfaceCount(int cl) {
    return Memory.readHalfWord(cl + INTERFACE_COUNT);
  }

  private final static int getInterfaces(int cl) {
    return Memory.readWord(cl + INTERFACES);
  }

  /**
   * @param clas
   * @param ifaces
   * @return
   */
  private final static int getInterfaces(int clas, int ifaces) {
    short result=0;
    int ifaceList=Iface.getList(ifaces);
    int interfaceCount=getInterfaceCount(clas);
    int classIfaceTable=getInterfaces(clas);
    for (int i=0; i < interfaceCount; i++) {
      //      int iface=MemoryObject.readWord(clas + INTERFACES + (i << 2));
      int iface=Array.get32(classIfaceTable, i);
      if (DEBUG4) {
        Console.write(getName(iface));
        Console.writeln();
      }
      /*
       * See if iface is in the iface list
       */
      if (indexOf(iface, ifaceList, Iface.getCount(ifaces)) == -1) {
        if (Iface.getCount(ifaces) + 1 >= Iface.getLen(ifaces)) {
          /*
           * This should not happen
           */
          Platform.cpu.halt();
        }
        int ifaceCnt=Iface.getCount(ifaces);
        if (DEBUG4) {
          Console.write("iface: ");
          Console.write(getName(iface));
          Console.writeln();
        }
        Array.set32(ifaceList, ifaceCnt, iface);
        Iface.setCount(ifaces, ifaceCnt + 1);
        result+=getInterfaces(iface, ifaces);
      }
    }
    if (isInterface(clas)) {
      result+=getMethodCount(clas) + 1;
    } else if (getSuperClass(clas) != 0) {
      result+=getInterfaces(getSuperClass(clas), ifaces);
    }
    return result;
  }

  /**
   * @param clas
   * @return
   */
  private final static boolean isAbstract(int clas) {
    return Modifier.isAbstract(getAccessFlags(clas));
  }

  /**
   * @param clas
   * @return
   */
  private final static boolean isInterface(int clas) {
    return Modifier.isInterface(getAccessFlags(clas));
  }

  /**
   * @param clas
   * @return
   */
  private final static boolean isPrimitive(int clas) {
    return getVTable(clas) == PRIMITIVE_VTABLE;
  }

  private final static boolean isArray(int c) {
    java.lang.String name = getArrayName(c);
    if(name instanceof java.lang.String)
      return name.charAt(0) == '[';
    else
      return false;
  }

  private final static int makeUtf8Const(char[] name, int size) {
    return 0;
  }

  /**
   * @return
   */
  public final static java.lang.Class[] getClasses() {
    // TODO
    return null;
  }

  public final static java.lang.Class getClass(Object c) {
    int addr = Memory.getAddress(c);
    return toClass(Memory.readWord(Memory.readWord(addr)));
  }

  /**
   * @param c
   * @return
   */
  public final static java.lang.Class getComponentType(java.lang.Class c) {
    int c0=Memory.getAddress(c);
    return isArray(c0) ? toClass(getElement(c)) : null;
  }

  private final static int getElement(java.lang.Class c) {
    int addr=Memory.getAddress(c);
    return Memory.readWord(addr + METHODS);
  }

  private final static int getElement(int cl) {
    return Memory.readWord(cl + METHODS);
  }
  
  private final static boolean interfaceAssignableFrom(int target, int source) {
    return false;
  }
  
  public final static boolean isAssignableFrom(int target, int source) {
    if(source==target)
      return true;
    
    /*
     * target is an array, and so must be the source
     */
    while(isArray(target)) {
      if(!isArray(source))
        return false;
      target = getElement(target);
      source = getElement(source);
    }
    
    if(isInterface(target)) {
      if(getIdt(source)==NULLPTR || isInterface(source))
        return interfaceAssignableFrom(target, source);
      
      int clIdt = getIdt(source);
      int ifIdt = getIdt(target);
      /*
       * See if interface has been implemented
       */
      if(ifIdt==NULLPTR)
        return false;
      int clIindex = Idt.getIindex(clIdt);
      int ifIoffsets = Idt.getIoffsets(ifIdt);
      if(clIindex < Array.get16(ifIoffsets, 0)) {
        int offset=Array.get16(ifIoffsets, clIindex);
        if(offset!=-1 && offset<Idt.getLen(clIdt) 
            && Array.get32(Idt.getItable(clIdt), offset) == target)
          return true;
      }
      return false;
    }
    
    if(isPrimitive(target) || isPrimitive(source))
      return false;
    
    if(target == Memory.getAddress(javaObjectClass))
      return true;
    
    int targetDepth = getDepth(target);
    int sourceDepth = getDepth(source);
    if(sourceDepth >= targetDepth && Array.get32(getAncestors(source), sourceDepth-targetDepth) == target)
      return true;
    return false;
  }
  
  /*
   * Is object an instance of class cl?
   */
  public final static boolean isInstanceOf(int cl, int object) {
    /*
     * Check the obvious.
     */
    if(object==NULLPTR)
      return false;
    return isAssignableFrom(cl, VTable.getClass(Memory.readWord(object)));
  }
  public final static boolean isInstanceOf(int cl, Throwable t) {
    return isInstanceOf(cl, Memory.getAddress(t));
  }
}