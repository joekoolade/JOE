#include <gcj/cni.h>

#include <baremetal/kernel/Debug.h>
#include <baremetal/kernel/Heap.h>
#include <baremetal/kernel/JVM.h>
#include <baremetal/kernel/Memory.h>
#include <baremetal/kernel/Monitor.h>
#include <baremetal/kernel/ObjectFactory.h>
#include <baremetal/kernel/InterruptManager.h>
#include <baremetal/vm/VMClassLoader.h>
#include <baremetal/vm/VMInt.h>
#include <baremetal/vm/VMChar.h>
#include <baremetal/vm/VMBoolean.h>
#include <baremetal/vm/VMLong.h>
#include <baremetal/vm/VMShort.h>
#include <baremetal/vm/VMByte.h>
#include <baremetal/vm/VMFloat.h>
#include <baremetal/vm/VMDouble.h>
#include <baremetal/vm/VMClassLoader.h>
#include <baremetal/vm/VMIntArray.h>
#include <baremetal/vm/VMCharArray.h>
#include <baremetal/vm/VMBooleanArray.h>
#include <baremetal/vm/VMLongArray.h>
#include <baremetal/vm/VMShortArray.h>
#include <baremetal/vm/VMByteArray.h>
#include <baremetal/vm/VMFloatArray.h>
#include <baremetal/vm/VMDoubleArray.h>
#include <baremetal/vm/System.h>
#include <baremetal/vm/Thread.h>
#include <baremetal/vm/VMThrowable.h>
#include <baremetal/vm/VMObject.h>
#include <baremetal/vm/VMVoid.h>
#include <baremetal/runtime/Class.h>
#include <baremetal/runtime/ArrayInterface.h>
#include <baremetal/runtime/Utf8.h>
#include <baremetal/runtime/Idt.h>
#include <baremetal/runtime/Iface.h>
#include <baremetal/runtime/Method.h>
#include <baremetal/runtime/Array.h>
#include <baremetal/runtime/String.h>
#include <baremetal/runtime/CatchClass.h>
#include <baremetal/runtime/Thrower.h>
#include <baremetal/runtime/Mutex.h>
#include <baremetal/runtime/Monitor.h>
#include <baremetal/platform/BIOS.h>
#include <baremetal/platform/Console.h>
#include <baremetal/platform/Multiboot.h>
#include <baremetal/platform/Boot.h>
#include <baremetal/platform/SystemTimer.h>
#include <baremetal/processor/Idt.h>
#include <baremetal/devices/I82c54.h>
#include <java/lang/System.h>
#include <java/lang/Runtime.h>
#include <java/lang/String.h>
#include <java/lang/reflect/Modifier.h>

/* Structure of the virtual table.  */
/*
struct _Jv_VTable

{
  typedef void *vtable_elt;
  jclass clas;

  void *gc_descr;

  // This must be last, as derived classes "extend" this by
  // adding new data members.
  vtable_elt method[1];

/*
  void *get_method(int i) { return method[i]; }
  void set_method(int i, void *fptr) { method[i] = fptr; }
  void *get_finalizer() { return get_method(0); }

  static size_t vtable_elt_size() { return sizeof(vtable_elt); }

  // Given a method index, return byte offset from the vtable pointer.
  static jint idx_to_offset (int index)
  {
    return (2 * sizeof (void *)) + (index * vtable_elt_size ());
  }
  static _Jv_VTable *new_vtable (int count);
*/

/*
};

// Number of virtual methods on object.  FIXME: it sucks that we have
// to keep this up to date by hand.
#define NUM_OBJECT_METHODS 5

// This structure is the type of an array's vtable.
struct _Jv_ArrayVTable : public _Jv_VTable
{
  vtable_elt extra_method[NUM_OBJECT_METHODS - 1];
};
*/

extern "C"
void initPrimClasses(void);
extern "C"
void _Jv_InitPrimClass(jclass,char,int,jclass);
extern "C"
void _Jv_NewArrayClass(jclass,void*,jclass);
extern "C"
void bootstrap(void);
extern "C"
  void timerIntr(void) __attribute((cdecl));

extern "C"
void _int104() {
  int context;
  
  asm("int104:");
  asm("pusha");
  /*
   * Create a primitive array on the stack and pass
   * to the interrupt routine.
   */
  asm("push $11");
  asm("push $0");
  asm("push %0" :: "r" (&baremetal::vm::VMIntArray::class$));
  asm("push %esp");
  asm("push $0x68");
  asm("call  *%0" :: "r" (&baremetal::kernel::InterruptManager::dispatcher));
  asm("add $20,%esp");
  asm("popa");
  asm("iret");
}

class java::lang::Throwable : public ::java::lang::Object
{
public:
//   Throwable ();
   static ::java::lang::Class class$;
};

class java::lang::Exception : public ::java::lang::Throwable
{
 public:
//   Exception ();
   static ::java::lang::Class class$;
};

class java::lang::RuntimeException : public ::java::lang::Exception
{
public:
//   RuntimeException ();
  static ::java::lang::Class class$;
};


class java::lang::IndexOutOfBoundsException : public ::java::lang::RuntimeException
{
public:
//   IndexOutOfBoundsException ();
   static ::java::lang::Class class$;
};

class java::lang::ArrayIndexOutOfBoundsException: public ::java::lang::IndexOutOfBoundsException
{
 public:
  ArrayIndexOutOfBoundsException(jint);
   static ::java::lang::Class class$;
};

class java::lang::NullPointerException: public ::java::lang::RuntimeException
{
public:
  //   NullPointerException();
  static ::java::lang::Class class$;
};

void
baremetal::runtime::Class::invokeMethod(jint func) {
  void (*method)();
  method = (void(*)())func;
  method();
}

jclass
baremetal::runtime::Class::toClass(jint addr) {
  return (jclass)addr;
}

jstring
baremetal::runtime::Utf8::toString(jint addr) {
  return (jstring)addr;
}

jstring
baremetal::runtime::Class::getMethodString(jint utfAddr) {
  return (jstring)utfAddr;
}

void
baremetal::runtime::Class::doCatch(jint landingPad, jint frame, jint stack, jint handler, jint unwindhdr) {
  
  /*
   * Overwrite the current return info with a return to
   * the catch clause
   */
  asm("mov %0, %%esp" :: "r" (stack));
  //  asm("mov %[value], 4(%%esp)" :: [value] "r" (landingPad));
  // asm("mov %[value], (%%esp)" :: [value] "r" (frame));
  asm("mov %0, %%ecx" :: "r" (landingPad));
  asm("mov %0, %%edx" :: "r" (handler));
  asm("mov %0, %%eax" :: "r" (unwindhdr));
  asm("mov %0, %%ebp" :: "b" (frame));
  asm("jmp *%ecx");
}

jint
baremetal::vm::Thread::frameAddress2() {
  return (jint)__builtin_frame_address(2);
}

void
baremetal::vm::Thread::returnFromStack(jint stack) {
  asm("mov %0, %%ebp" :: "r" (stack));
  asm("leave");
  asm("ret");
}

void
baremetal::vm::Thread::kernelReturnFromStack(jint stack) {
  asm("mov %0, %%ebp" :: "r" (stack));
  asm("leave");
  asm("iret");
}

void
baremetal::vm::Thread::returnFromInterrupt(jint stack) {
  asm("mov %0, %%esp" :: "r" (stack));
  asm("add $12,%esp");
  asm("popa");
  asm("iret");
}

void
baremetal::vm::Thread::startThread(jint stack) {
  /*
   * end of stack marker
   */
  asm("mov %0, %%esp" :: "r" (stack));
  asm("mov $0xffffffff, %ebp");
  asm("ret");
}


extern "C" void
JvRunMain(jclass klass, int argc, const char **argv) {
  initPrimClasses();
  bootstrap();
  baremetal::platform::Boot::init(klass);
}

extern int _rodata;
extern int _erodata;
extern int __jcr;
extern int __ejcr;
extern int __eh_frame;
extern int __eh_frame_end;
extern int __text;
extern int __etext;
extern int int104;

extern "C" void
bootstrap() {
  
  baremetal::runtime::Class::javaObjectClass = &::java::lang::Object::class$;
  baremetal::runtime::Class::arrayInterfaceClass = &baremetal::runtime::ArrayInterface::class$;


  /*
    Need to run the class initializers explicitly on some classes
    than when they would normally run. 
  */
  using namespace baremetal::kernel;

  // baremetal.kernel
  baremetal::runtime::Class::classInit(&Heap::class$);
  baremetal::runtime::Class::classInit(&Memory::class$);
  using namespace baremetal::processor;
  baremetal::runtime::Class::classInit(&Idt::class$);
  Idt::int104 = (int)&int104;
  using namespace baremetal::vm;
  using namespace baremetal::platform;
  baremetal::runtime::Class::classInit(&Console::class$);
  baremetal::runtime::Class::classInit(&Boot::class$);

  /*
    setup the the utf boudaries
  */
  using namespace baremetal::runtime;
  baremetal::runtime::Class::classInit(&Utf8::class$);
  baremetal::runtime::Class::classInit(&baremetal::runtime::Idt::class$);
  baremetal::runtime::Class::classInit(&Iface::class$);
  baremetal::runtime::Class::classInit(&CatchClass::class$);
  Utf8::utfStart = (int)&_rodata;
  Utf8::utfEnd = (int)&_erodata;
  baremetal::runtime::Class::jcrStart = (int)&__jcr;
  baremetal::runtime::Class::jcrEnd = (int)&__ejcr;
  baremetal::runtime::Thrower::ehSection = (int)&__eh_frame;
  baremetal::runtime::Thrower::ehSectionEnd = (int)&__eh_frame_end;
  baremetal::runtime::Thrower::beginCode = (int)&__text;
  baremetal::runtime::Thrower::endCode = (int)&__etext;
}

extern "C" void
_Jv_Compiler_Properties() {
}

extern "C" jobject
_Jv_AllocObject (jclass klass, jint size) {
  return (jobject)baremetal::kernel::Heap::allocate(klass, size);
}

jobject
_Jv_AllocObjectNoFinalizer (jclass klass, jint size)
{
  /*
  _Jv_InitClass (klass);
  jobject obj = (jobject) _Jv_AllocObj (size, klass);
  jvmpi_notify_alloc (klass, size, obj);
  return obj;
  */
  return (jobject)baremetal::kernel::Heap::allocate(klass, size);
}

extern "C" void
initPrimClasses() {

  using namespace baremetal::vm;

  _Jv_InitPrimClass(&VMBoolean::class$, 'Z', 1, &VMBooleanArray::class$);
  _Jv_InitPrimClass(&VMByte::class$, 'B', 1, &VMByteArray::class$);
  _Jv_InitPrimClass(&VMShort::class$, 'S', 2,&VMShortArray::class$ );
  _Jv_InitPrimClass(&VMInt::class$, 'I', 4, &VMIntArray::class$);
  _Jv_InitPrimClass(&VMLong::class$, 'J', 8, &VMLongArray::class$);
  _Jv_InitPrimClass(&VMChar::class$, 'C', 2, &VMCharArray::class$);
  _Jv_InitPrimClass(&VMFloat::class$, 'F', 4, &VMFloatArray::class$);
  _Jv_InitPrimClass(&VMDouble::class$, 'D', 8, &VMDoubleArray::class$);
  _Jv_InitPrimClass(&VMVoid::class$, 'V', 0, NULL);
}

#define JV_PRIMITIVE_VTABLE -1
#define JV_STATE_DONE 14

extern "C" void
_Jv_InitPrimClass (jclass cl, char sig, int len, jclass array_class)
{    
  // We must set the vtable for the class; the Java constructor
  // doesn't do this.
  //  (*(_Jv_VTable **) cl) = java::lang::Class::class$.vtable;

  // Initialize the fields we care about.  We do this in the same
  // order they are declared in Class.h.
  //  cl->name = _Jv_makeUtf8Const ((char *) cname, -1);
  cl->accflags = 0x411; //Modifier::PUBLIC | Modifier::FINAL | Modifier::ABSTRACT;
  cl->method_count = sig;
  cl->size_in_bytes = len;
  cl->vtable = (void*)JV_PRIMITIVE_VTABLE;
  cl->state = JV_STATE_DONE;
  cl->depth = -1;
  if(sig!='V') {
    _Jv_Utf8Const *utf = (_Jv_Utf8Const*)array_class->name;
    utf->length=2;
    utf->data[0]='[';
    utf->data[1]=sig;
    cl->arrayclass = array_class;
    array_class->methods = cl;
  }
    
  
}

extern "C" void
_Jv_NewArrayClass(jclass element, void* loader, jclass array_class) {
}

extern "C" void
_Jv_InitClass(jclass klass) {
  /*
    Ok for know but should modify gcj to generate klass->initializeClass() call
  */
  if(klass == &baremetal::runtime::Class::class$)
    return;
  klass->initializeClass();
}

extern "C" jobject
_Jv_NewMultiArray(jclass type, jint dimensions, jint *sizes) {
  /*
  for (int i = 0; i < dimensions; ++i)
    if (sizes[i] < 0)
      throw new java::lang::NegativeArraySizeException;

  return _Jv_NewMultiArrayUnchecked (type, dimensions, sizes);
  */
  return (jobject)baremetal::kernel::Heap::allocateMultiArray(type, dimensions, (jobject)&sizes);
}

// Allocate a new array of Java objects.  Each object is of type
// `elementClass'.  `init' is used to initialize each slot in the
// array.
extern "C" jobjectArray
_Jv_NewObjectArray (jsize count, jclass elementClass, jobject init)
{
  return (jobjectArray)baremetal::kernel::Heap::newObjectArray(count, elementClass);
}

// Allocate a new array of primitives.  ELTYPE is the type of the
// element, COUNT is the size of the array.
extern "C" jobject
_Jv_NewPrimArray (jclass eltype, jint count)
{
  return (jobject)baremetal::kernel::Heap::allocatePrimArray(eltype, count);
}

extern "C"
jint
_Jv_divI (jint dividend, jint divisor)
{
  return dividend/divisor;
}

extern "C" jint
_Jv_remI (jint dividend, jint divisor)
{
  return dividend%divisor;
}

extern "C" jlong
_Jv_divJ (jlong dividend, jlong divisor)
{
  return dividend/divisor;
}

extern "C" jlong
_Jv_remJ (jlong dividend, jlong divisor)
{
  return dividend%divisor;
}

extern "C" void
_Jv_ThrowBadArrayIndex(jint bad_index)
{
  throw new java::lang::ArrayIndexOutOfBoundsException(bad_index);
}

extern "C" void
_Jv_ThrowNullPointerException ()
{
  throw new java::lang::NullPointerException;
}

extern "C" void
_Jv_CheckArrayStore (jobject arr, jobject obj)
{
}

extern "C" void *
_Jv_CheckCast (jclass c, jobject obj)
{
  return obj;
}

extern "C" jboolean
_Jv_IsInstanceOf(jobject obj, jclass cl)
{
  return baremetal::runtime::Class::isInstanceOf((int)obj, (int)cl);
}

extern "C" void *
_Jv_LookupInterfaceMethodIdx (jclass klass, jclass iface, int methodIdx)
{
  return (void*)baremetal::runtime::Class::lookupInterfaceMethodIdx((int)klass, (int)iface, methodIdx);
//   _Jv_IDispatchTable *cldt = klass->idt;
//   int idx = iface->idt->iface.ioffsets[cldt->cls.iindex] + method_idx;
//   return cldt->cls.itable[idx];
}


void
_Jv_MonitorEnter (jobject obj)
{
	return baremetal::runtime::Monitor::enter(obj);
}

void
_Jv_MonitorExit (jobject obj)
{
	return baremetal::runtime::Monitor::exit(obj);
}

baremetal::runtime::Mutex*
baremetal::runtime::Monitor::getMutex(jobject o) {
	return (baremetal::runtime::Mutex*)o->sync_info;
}

void
baremetal::runtime::Monitor::setMutex(jobject o, baremetal::runtime::Mutex* m) {
	o->sync_info = (int)m;
}
