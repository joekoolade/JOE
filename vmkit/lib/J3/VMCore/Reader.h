//===----------------- Reader.h - Open and read files ---------------------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#ifndef JNJVM_READER_H
#define JNJVM_READER_H

#include "types.h"

#include "JavaArray.h"
#include "JavaClass.h"

namespace j3 {

class JnjvmBootstrapLoader;
class JnjvmClassLoader;
class ZipArchive;


class ClassBytes {
 public:
  ClassBytes(int l) {
    size = l;
  }

  void* operator new(size_t sz, mvm::BumpPtrAllocator& allocator, int n) {
    return allocator.Allocate(sizeof(uint32_t) + n * sizeof(uint8_t),
                              "Class bytes");
  }

  uint32_t size;
  uint8_t elements[1];
};

class Reader {
public:
  ClassBytes* bytes;
  uint32 min;
  uint32 cursor;
  uint32 max;

  Reader(Attribut* attr, ClassBytes* bytes) {
    this->bytes = bytes;
    this->cursor = attr->start;
    this->min = attr->start;
    this->max = attr->start + attr->nbb;
  }

  Reader(Reader& r, uint32 nbb) {
    bytes = r.bytes;
    cursor = r.cursor;
    min = r.min;
    max = min + nbb;
  }

  static double readDouble(int first, int second) {
    return mvm::System::ReadDouble(first, second);
   }


  static sint64 readLong(int first, int second) {
    return mvm::System::ReadLong(first, second);
  }

  static const int SeekSet;
  static const int SeekCur;
  static const int SeekEnd;

  static ClassBytes* openFile(JnjvmClassLoader* loader, const char* path);
  static ClassBytes* openZip(JnjvmClassLoader* loader, ZipArchive* archive,
                             const char* filename);
  
  uint8 readU1() {
    ++cursor;
    return bytes->elements[cursor - 1];
  }
  
  sint8 readS1() {
    ++cursor;
    return bytes->elements[cursor - 1];
  }
  
  uint16 readU2() {
    uint16 tmp = ((uint16)(readU1())) << 8;
    return tmp | ((uint16)(readU1()));
  }
  
  sint16 readS2() {
    sint16 tmp = ((sint16)(readS1())) << 8;
    return tmp | ((sint16)(readU1()));
  }
  
  uint32 readU4() {
    uint32 tmp = ((uint32)(readU2())) << 16;
    return tmp | ((uint32)(readU2()));
  }
  
  sint32 readS4() {
    sint32 tmp = ((sint32)(readS2())) << 16;
    return tmp | ((sint32)(readU2()));
  }

  uint64 readU8() {
    uint64 tmp = ((uint64)(readU4())) << 32;
    return tmp | ((uint64)(readU4()));
  }
  
  sint64 readS8() {
    sint64 tmp = ((sint64)(readS4())) << 32;
    return tmp | ((sint64)(readU4()));
  }

  Reader(ClassBytes* array, uint32 start = 0, uint32 end = 0) {
    if (!end) end = array->size;
    this->bytes = array;
    this->cursor = start;
    this->min = start;
    this->max = start + end;
  }

  
  unsigned int tell() {
    return cursor - min;
  }
  
  void seek(uint32 pos, int from);
};

} // end namespace j3

#endif
