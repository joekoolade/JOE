//===------- LockedMap.h - A thread-safe map implementation ---------------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//
//
// This file defines thread-safe maps that must be deallocated by the owning
// object. For example a class loader is responsible for deallocating the
// types stored in a TypeMap.
//
//===----------------------------------------------------------------------===//

#ifndef JMAP_H
#define JMAP_H


#include <map>

#include <cstring>

#include "types.h"

#include "MvmDenseMap.h"
#include "UTF8.h"

namespace j3 {

class ArrayUInt16;
class JavaString;
class Signdef;
class Typedef;
class CommonClass;
class ClassArray;

struct ltarray16 {
  bool operator()(const ArrayUInt16 *const s1, const ArrayUInt16 *const s2) const;
};

class StringMap {
public:
  typedef std::map<const ArrayUInt16 *const, JavaString*, ltarray16>::iterator iterator;
  typedef JavaString* (*funcCreate)(const ArrayUInt16 *const& V);

  std::map<const ArrayUInt16 *const, JavaString*, ltarray16,
           std::allocator<std::pair<const ArrayUInt16 *const, JavaString*> > > map;
  
  inline JavaString* lookupOrCreate(const ArrayUInt16 *const array, funcCreate func) {
    JavaString* res = 0;
    iterator End = map.end();
    iterator I = map.find(array);
    if (I == End) {
      res = func(array);
      map.insert(std::make_pair(array, res));
      return res;
    } else {
      return ((JavaString*)(I->second));
    }
  }
  
  inline void remove(const ArrayUInt16 *const array) {
    map.erase(array);
  }

  inline JavaString* lookup(const ArrayUInt16 *const array) {
    iterator End = map.end();
    iterator I = map.find(array);
    return I != End ? ((JavaString*)(I->second)) : 0; 
  }

  inline void hash(const ArrayUInt16 *const array, JavaString* str) {
   map.insert(std::make_pair(array, str));
  }

  inline void removeUnlocked(const ArrayUInt16 *const array, JavaString* str) {
   iterator End = map.end();
   iterator I = map.find(array);

    if (I != End && I->second == str) map.erase(I); 
  }

  ~StringMap() {}

  void insert(JavaString* str);
};


class ClassMap {
public:
  ClassMap() {}
  ClassMap(MvmDenseMap<const UTF8*, CommonClass*>* precompiled) : map(*precompiled) {}

  MvmDenseMap<const UTF8*, CommonClass*> map;
  typedef MvmDenseMap<const UTF8*, CommonClass*>::iterator iterator;
};

class TypeMap {
public:
  MvmDenseMap<const UTF8*, Typedef*> map;
  typedef MvmDenseMap<const UTF8*, Typedef*>::iterator iterator;
};

class SignMap {
public:
  MvmDenseMap<const UTF8*, Signdef*> map;
  typedef MvmDenseMap<const UTF8*, Signdef*>::iterator iterator;
};

} // end namespace j3

#endif
