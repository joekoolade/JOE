//===----------------- Zip.h - Interface with zlib ------------------------===//
//
//                          The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#ifndef JNJVM_ZIP_H
#define JNJVM_ZIP_H

#include <map>

#include "mvm/Allocator.h"

namespace j3 {

class classBytes;
class JnjvmBootstrapLoader;

struct ZipFile : public mvm::PermanentObject {
  char* filename;
  int ucsize;
  int csize;
  uint32 filenameLength;
  uint32 extraFieldLength;
  uint32 fileCommentLength;
  int rolh;
  int compressionMethod;
};



class ZipArchive : public mvm::PermanentObject {
  
  mvm::BumpPtrAllocator& allocator;

  struct ltstr
  {
    bool operator()(const char* s1, const char* s2) const
    {
      return strcmp(s1, s2) < 0;
    }
  };
  
  int ofscd;

public:
  std::map<const char*, ZipFile*, ltstr> filetable;
  typedef std::map<const char*, ZipFile*, ltstr>::iterator table_iterator;
  ClassBytes* bytes;

private:
  
  void findOfscd();
  void addFiles();
  
  void remove();

public:
  
  ~ZipArchive() {
    for (table_iterator I = filetable.begin(), E = filetable.end(); I != E; 
         ++I) {
      allocator.Deallocate((void*)I->first);
      I->second->~ZipFile();
      allocator.Deallocate((void*)I->second);
    }
  }

  int getOfscd() { return ofscd; }
  ZipArchive(ClassBytes* bytes, mvm::BumpPtrAllocator& allocator);
  ZipFile* getFile(const char* filename);
  int readFile(ClassBytes* array, const ZipFile* file);

};

} // end namespace j3

#endif
