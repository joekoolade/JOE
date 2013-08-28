// Class.h - Header file for java.lang.Class.  -*- c++ -*-

/* Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003  Free Software Foundation

   This file is part of libgcj.

This software is copyrighted work licensed under the terms of the
Libgcj License.  Please consult the file "LIBGCJ_LICENSE" for
details.  */

// Written primary using compiler source and Class.java as guides.
#ifndef __JAVA_LANG_CLASS_H__
#define __JAVA_LANG_CLASS_H__

#pragma interface

#include <java/lang/Object.h>

struct _Jv_Constants
{
  jint size;
  jbyte *tags;
  void *data;
};

class java::lang::Class : public java::lang::Object
{
public:
void initializeClass(void);
//  jclass class$;
//  int sync_info;

  // Chain for class pool.
  jclass next;
  // Name of class.
  void *name;
  // Access flags for class.
  unsigned short accflags;
  //  unsigned short filler0;

  // The superclass, or null for Object.
  jclass superclass;
  // Class constants.
  _Jv_Constants constants;
  // Methods.  If this is an array class, then this field holds a
  // pointer to the element type.
  void *methods;
  // Number of methods.  If this class is primitive, this holds the
  // character used to represent this type in a signature.
  jshort method_count;
  // Number of methods in the vtable.
  jshort vtable_method_count;
  // The fields.
  void *fields;
  // Size of instance fields, in bytes.
  jint size_in_bytes;
  // Total number of fields (instance and static).
  jshort field_count;
  // Number of static fields.
  jshort static_field_count;
  // The vtbl for all objects of this class.
  void *vtable;
  // Virtual method offset table.
  void *otable;
  // Offset table symbols.
  void *otable_syms;
  void *atable;
  void *atable_syms;
  void *catch_classes;
  // Interfaces implemented by this class.
  jclass *interfaces;
  // The class loader for this class.
  void *loader;
  // Number of interfaces.
  jshort interface_count;
  // State of this class.
  jbyte state;
  // The thread which has locked this class.  Used during class
  // initialization.
  void *thread;
  // How many levels of "extends" this class is removed from Object.
  jshort depth;
  // Vector of this class's superclasses, ordered by decreasing depth.
  jclass *ancestors;
  // Interface Dispatch Table.
  void *idt;
  // Pointer to the class that represents an array of this class.
  jclass arrayclass;
  // Security Domain to which this class belongs (or null).
  void *protectionDomain;
  // Signers of this class (or null).
  void *hack_signers;
  // Used by Jv_PopClass and _Jv_PushClass to communicate with StackTrace.
  jclass chain;

};

#endif /* __JAVA_LANG_CLASS_H__ */
