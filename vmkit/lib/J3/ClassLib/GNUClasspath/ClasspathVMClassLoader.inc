//===- ClasspathVMClassLoader.cpp - GNU classpath java/lang/VMClassLoader -===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#include "types.h"

#include "Classpath.h"
#include "JavaArray.h"
#include "JavaClass.h"
#include "JavaObject.h"
#include "JavaString.h"
#include "JavaThread.h"
#include "JavaUpcalls.h"
#include "Jnjvm.h"
#include "Reader.h"

using namespace j3;

extern "C" {

JNIEXPORT JavaObject* JNICALL Java_java_lang_VMClassLoader_getPrimitiveClass(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz,
#endif
jchar byteId) {
  
  JavaObject* res = 0;
  llvm_gcroot(res, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  Jnjvm* vm = JavaThread::get()->getJVM();
  UserClassPrimitive* prim = 
    UserClassPrimitive::byteIdToPrimitive(byteId, vm->upcalls);
  
  if (!prim) {
    fprintf(stderr, "unknown byte primitive %c", byteId);
    abort();
  }
  
  res = prim->getClassDelegatee(vm);

  END_NATIVE_EXCEPTION

  return res;
  
}

JNIEXPORT JavaObject* JNICALL Java_java_lang_VMClassLoader_findLoadedClass(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz,
#endif
JavaObject* loader,
JavaString* name) {
  
  JavaObject* res = 0;
  llvm_gcroot(loader, 0);
  llvm_gcroot(name, 0);
  llvm_gcroot(res, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  Jnjvm* vm = JavaThread::get()->getJVM();
  JnjvmClassLoader* JCL = 
    JnjvmClassLoader::getJnjvmLoaderFromJavaObject(loader, vm);
  UserCommonClass* cl = JCL->lookupClassFromJavaString(name);

  if (cl) res = cl->getClassDelegatee(vm);

  END_NATIVE_EXCEPTION

  return res;
  
  return 0;
}

JNIEXPORT JavaObject* JNICALL Java_java_lang_VMClassLoader_loadClass(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz,
#endif
JavaString* str, 
jboolean doResolve) {

  JavaObject* res = 0;
  llvm_gcroot(res, 0);
  llvm_gcroot(str, 0);

  BEGIN_NATIVE_EXCEPTION(0)

  Jnjvm* vm = JavaThread::get()->getJVM();

  JnjvmClassLoader* JCL = vm->bootstrapLoader;
  UserCommonClass* cl = JCL->loadClassFromJavaString(str, doResolve, false);

  if (cl != 0) res = cl->getClassDelegatee(vm);
  
  END_NATIVE_EXCEPTION

  return res;
}

JNIEXPORT JavaObject* JNICALL Java_java_lang_VMClassLoader_defineClass(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz, 
#endif
JavaObject* loader, 
JavaString* str, 
ArrayUInt8* bytes, 
jint off, 
jint len, 
JavaObject* pd) {
  
  JavaObject* res = 0;
  JavaObject* excp = 0;
  llvm_gcroot(loader, 0);
  llvm_gcroot(str, 0);
  llvm_gcroot(bytes, 0);
  llvm_gcroot(pd, 0);
  llvm_gcroot(res, 0);
  llvm_gcroot(excp, 0);

  BEGIN_NATIVE_EXCEPTION(0)
  
  // We need a name, which is unfortunately not required by the spec.
  verifyNull(str);

  Jnjvm* vm = JavaThread::get()->getJVM();

  JnjvmClassLoader* JCL = 
    JnjvmClassLoader::getJnjvmLoaderFromJavaObject(loader, vm);
 
  int32_t size = JavaArray::getSize(bytes);
  ClassBytes* classBytes = new (JCL->allocator, size) ClassBytes(size);
  memcpy(classBytes->elements, JavaArray::getElements(bytes), size);
   
  // Before creating a class, do a check on the bytes.  
  Reader reader(classBytes);
  uint32 magic = reader.readU4();
  if (magic != Jnjvm::Magic) {
    JavaThread::get()->getJVM()->classFormatError("bad magic number");
  }

  
  const UTF8* name = JavaString::javaToInternal(str, JCL->hashUTF8);
  UserCommonClass* cl = JCL->lookupClass(name);
  
  if (!cl) {
    UserClass* cl = JCL->constructClass(name, classBytes);
    cl->resolveClass();

    res = cl->getClassDelegatee(vm, pd);
  } else {
    excp = vm->CreateLinkageError("duplicate class definition");
    JavaThread::get()->throwException(excp);
  }

  END_NATIVE_EXCEPTION

  return res;
}

JNIEXPORT void JNICALL Java_java_lang_VMClassLoader_resolveClass(
#ifdef NATIVE_JNI
JNIEnv *env,
jclass clazz,
#endif
JavaObject* Cl) {
 
  llvm_gcroot(Cl, 0);
  
  BEGIN_NATIVE_EXCEPTION(0)
  
  verifyNull(Cl);
  Jnjvm* vm = JavaThread::get()->getJVM();
  UserCommonClass::resolvedImplClass(vm, Cl, false);

  END_NATIVE_EXCEPTION
}

#define NUM_BOOT_PACKAGES 168

static const char* bootPackages[NUM_BOOT_PACKAGES] = {
				"java.applet",
				"java.awt",
				"java.awt.color",
				"java.awt.datatransfer",
				"java.awt.dnd",
				"java.awt.dnd.peer",
				"java.awt.event",
				"java.awt.font",
				"java.awt.geom",
				"java.awt.im",
				"java.awt.im.spi",
				"java.awt.image",
				"java.awt.image.renderable",
				"java.awt.peer",
				"java.awt.print",
				"java.beans",
				"java.beans.beancontext",
				"java.io",
				"java.lang",
				"java.lang.annotation",
				"java.lang.instrument",
				"java.lang.management",
				"java.lang.ref",
				"java.lang.reflect",
				"java.math",
				"java.net",
				"java.nio",
				"java.nio.channels",
				"java.nio.channels.spi",
				"java.nio.charset",
				"java.nio.charset.spi",
				"java.rmi",
				"java.rmi.activation",
				"java.rmi.dgc",
				"java.rmi.registry",
				"java.rmi.server",
				"java.security",
				"java.security.acl",
				"java.security.cert",
				"java.security.interfaces",
				"java.security.spec",
				"java.sql",
				"java.text",
				"java.util",
				"java.util.concurrent",
				"java.util.concurrent.atomic",
				"java.util.concurrent.locks",
				"java.util.jar",
				"java.util.logging",
				"java.util.prefs",
				"java.util.regex",
				"java.util.zip",
				"javax.accessibility",
				"javax.activity",
				"javax.crypto",
				"javax.crypto.interfaces",
				"javax.crypto.spec",
				"javax.imageio",
				"javax.imageio.event",
				"javax.imageio.metadata",
				"javax.imageio.plugins.bmp",
				"javax.imageio.plugins.jpeg",
				"javax.imageio.spi",
				"javax.imageio.stream",
				"javax.management",
				"javax.management.loading",
				"javax.management.modelmbean",
				"javax.management.monitor",
				"javax.management.openmbean",
				"javax.management.relation",
				"javax.management.remote",
				"javax.management.remote.rmi",
				"javax.management.timer",
				"javax.naming",
				"javax.naming.directory",
				"javax.naming.event",
				"javax.naming.ldap",
				"javax.naming.spi",
				"javax.net",
				"javax.net.ssl",
				"javax.print",
				"javax.print.attribute",
				"javax.print.attribute.standard",
				"javax.print.event",
				"javax.rmi",
				"javax.rmi.CORBA",
				"javax.rmi.ssl",
				"javax.security.auth",
				"javax.security.auth.callback",
				"javax.security.auth.kerberos",
				"javax.security.auth.login",
				"javax.security.auth.spi",
				"javax.security.auth.x500",
				"javax.security.cert",
				"javax.security.sasl",
				"javax.sound.midi",
				"javax.sound.midi.spi",
				"javax.sound.sampled",
				"javax.sound.sampled.spi",
				"javax.sql",
				"javax.sql.rowset",
				"javax.sql.rowset.serial",
				"javax.sql.rowset.spi",
				"javax.swing",
				"javax.swing.border",
				"javax.swing.colorchooser",
				"javax.swing.event",
				"javax.swing.filechooser",
				"javax.swing.plaf",
				"javax.swing.plaf.basic",
				"javax.swing.plaf.metal",
				"javax.swing.plaf.multi",
				"javax.swing.plaf.synth",
				"javax.swing.table",
				"javax.swing.text",
				"javax.swing.text.html",
				"javax.swing.text.html.parser",
				"javax.swing.text.rtf",
				"javax.swing.tree",
				"javax.swing.undo",
				"javax.transaction",
				"javax.transaction.xa",
				"javax.xml",
				"javax.xml.datatype",
				"javax.xml.namespace",
				"javax.xml.parsers",
				"javax.xml.transform",
				"javax.xml.transform.dom",
				"javax.xml.transform.sax",
				"javax.xml.transform.stream",
				"javax.xml.validation",
				"javax.xml.xpath",
				"org.ietf.jgss",
				"org.omg.CORBA",
				"org.omg.CORBA_2_3",
				"org.omg.CORBA_2_3.portable",
				"org.omg.CORBA.DynAnyPackage",
				"org.omg.CORBA.ORBPackage",
				"org.omg.CORBA.portable",
				"org.omg.CORBA.TypeCodePackage",
				"org.omg.CosNaming",
				"org.omg.CosNaming.NamingContextExtPackage",
				"org.omg.CosNaming.NamingContextPackage",
				"org.omg.Dynamic",
				"org.omg.DynamicAny",
				"org.omg.DynamicAny.DynAnyFactoryPackage",
				"org.omg.DynamicAny.DynAnyPackage",
				"org.omg.IOP",
				"org.omg.IOP.CodecFactoryPackage",
				"org.omg.IOP.CodecPackage",
				"org.omg.Messaging",
				"org.omg.PortableInterceptor",
				"org.omg.PortableInterceptor.ORBInitInfoPackage",
				"org.omg.PortableServer",
				"org.omg.PortableServer.CurrentPackage",
				"org.omg.PortableServer.POAManagerPackage",
				"org.omg.PortableServer.POAPackage",
				"org.omg.PortableServer.portable",
				"org.omg.PortableServer.ServantLocatorPackage",
				"org.omg.SendingContext",
				"org.omg.stub.java.rmi",
				"org.w3c.dom",
				"org.w3c.dom.bootstrap",
				"org.w3c.dom.events",
				"org.w3c.dom.ls",
				"org.xml.sax",
				"org.xml.sax.ext",
				"org.xml.sax.helpers"
};

extern "C" ArrayObject* Java_java_lang_VMClassLoader_getBootPackages__() {
  ArrayObject* obj = 0;
  llvm_gcroot(obj, 0);
  
  BEGIN_NATIVE_EXCEPTION(0)
  
  Jnjvm* vm = JavaThread::get()->getJVM();
  obj = (ArrayObject*)vm->upcalls->ArrayOfString->doNew(NUM_BOOT_PACKAGES, vm);
  for (uint32 i = 0; i < NUM_BOOT_PACKAGES; ++i) {
    ArrayObject::setElement(obj, vm->asciizToStr(bootPackages[i]), i);
  }
  
  END_NATIVE_EXCEPTION

  return obj;
}


}
