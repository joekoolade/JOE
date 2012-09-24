//===--------------- Reader.cpp - Open and read files ---------------------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#include <cstdio>
#include <cstring>

#include "types.h"

#include "JnjvmClassLoader.h"
#include "Reader.h"
#include "Zip.h"

using namespace j3;

const int Reader::SeekSet = SEEK_SET;
const int Reader::SeekCur = SEEK_CUR;
const int Reader::SeekEnd = SEEK_END;

ClassBytes* Reader::openFile(JnjvmClassLoader* loader, const char* path) {
  ClassBytes* res = NULL;
  FILE* fp = fopen(path, "r");
  if (fp != 0) {
    fseek(fp, 0, SeekEnd);
    long nbb = ftell(fp);
    fseek(fp, 0, SeekSet);
    res = new (loader->allocator, nbb) ClassBytes(nbb);
    if (fread(res->elements, nbb, 1, fp) == 0) {
      fprintf(stderr, "fread error\n");
      abort();  
    }
    fclose(fp);
  }
  return res;
}

ClassBytes* Reader::openZip(JnjvmClassLoader* loader, ZipArchive* archive,
                            const char* filename) {
  ClassBytes* res = 0;
  ZipFile* file = archive->getFile(filename);
  if (file != 0) {
    res = new (loader->allocator, file->ucsize) ClassBytes(file->ucsize);
    if (archive->readFile(res, file) != 0) {
      return res;
    }
  }
  return NULL;
}

void Reader::seek(uint32 pos, int from) {
  uint32 n = 0;
  uint32 start = min;
  uint32 end = max;
  
  if (from == SeekCur) n = cursor + pos;
  else if (from == SeekSet) n = start + pos;
  else if (from == SeekEnd) n = end + pos;
  

  assert(n >= start && n <= end && "out of range");

  cursor = n;
}
