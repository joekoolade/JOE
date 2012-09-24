//===----------------- Zip.cpp - Interface with zlib ----------------------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#include <zlib.h>

#include "mvm/Allocator.h"

#include "JavaArray.h"
#include "Reader.h"
#include "Zip.h"

using namespace j3;

ZipArchive::ZipArchive(ClassBytes* bytes, mvm::BumpPtrAllocator& A) : allocator(A) {
  this->bytes = bytes;
  findOfscd();
  if (ofscd > -1) addFiles();
}

ZipFile* ZipArchive::getFile(const char* filename) {
  table_iterator End = filetable.end();
  table_iterator I = filetable.find(filename);
  return I != End ? I->second : 0;
}


#define END_CENTRAL_DIRECTORY_FILE_HEADER_SIZE 18
#define CENTRAL_DIRECTORY_FILE_HEADER_SIZE 42
#define LOCAL_FILE_HEADER_SIZE 26

#define C_FILENAME_LENGTH 24
#define C_UCSIZE 20
#define C_CSIZE 16
#define C_EXTRA_FIELD_LENGTH 26
#define C_FILE_COMMENT_LENGTH 28
#define C_ROLH 38
#define C_COMPRESSION_METHOD 6

#define L_FILENAME_LENGTH 22
#define L_EXTRA_FIELD_LENGTH 24

#define E_OFFSET_START_CENTRAL_DIRECTORY 12
#define HDR_ENDCENTRAL "PK\005\006"
#define HDR_CENTRAL "PK\001\002"
#define HDR_LOCAL "PK\003\004"
#define PATH_SEPARATOR '/'
#define ZIP_STORE 0
#define ZIP_DEFLATE 8
#define DEF_WBITS 15

static uint32 readEndianDep4(Reader& reader) {
  uint8 one = reader.readU1();
  uint8 two = reader.readU1();
  uint8 three = reader.readU1();
  uint8 four = reader.readU1();
  return (one + (two << 8) + (three << 16) + (four << 24));
}

static uint16 readEndianDep2(Reader& reader) {
  uint8 one = reader.readU1();
  uint8 two = reader.readU1();
  return (one + (two << 8));
}

void ZipArchive::findOfscd() {
  sint32 curOffs = 0;
  sint32 minOffs = 0;
  sint32 st = END_CENTRAL_DIRECTORY_FILE_HEADER_SIZE + 4;
  
  Reader reader(bytes);
  curOffs = reader.max;
  if (curOffs >= (65535 + END_CENTRAL_DIRECTORY_FILE_HEADER_SIZE + 4)) {
    minOffs = curOffs - (65535 + END_CENTRAL_DIRECTORY_FILE_HEADER_SIZE + 4);
  } else {
    minOffs = 0;
  }

  while (curOffs > minOffs) {
    sint32 searchPos = 0;
    if (curOffs >= (1024 - st)) {
      curOffs = curOffs - (1024 - st);
    } else {
      curOffs = 0;
    }
    reader.cursor += curOffs;

    sint32 diff = reader.max - reader.cursor;
    sint32 temp = reader.cursor;
    if (diff > 1024) {
      searchPos = 1024;
      reader.cursor += 1024;
    } else {
      searchPos = diff;
      reader.cursor = reader.max;
    }

    if (searchPos >= st) {
      sint32 searchPtr = temp + (searchPos - st);
      while (searchPtr > temp) {
        if (bytes->elements[searchPtr] == 'P' && 
          !(memcmp(bytes->elements + searchPtr, HDR_ENDCENTRAL, 4))) {
          sint32 offset = searchPtr + 4 + E_OFFSET_START_CENTRAL_DIRECTORY;
          reader.cursor = offset;
          this->ofscd = readEndianDep4(reader);
          return;
        }
        searchPtr--;
      }
    }
  }
  this->ofscd = -1;
}

void ZipArchive::addFiles() {
  sint32 temp = ofscd;
  
  Reader reader(bytes);
  reader.cursor = temp;

  while (true) {
    if (memcmp(bytes->elements + temp, HDR_CENTRAL, 4)) return;
    ZipFile* ptr = new(allocator, "ZipFile") ZipFile();
    reader.cursor = temp + 4 + C_COMPRESSION_METHOD;
    ptr->compressionMethod = readEndianDep2(reader);
    
    reader.cursor = temp + 4 + C_CSIZE;
    
    ptr->csize = readEndianDep4(reader);
    ptr->ucsize = readEndianDep4(reader);
    ptr->filenameLength = readEndianDep2(reader);
    ptr->extraFieldLength = readEndianDep2(reader);
    ptr->fileCommentLength = readEndianDep2(reader);

    reader.cursor = temp + 4 + C_ROLH;
    ptr->rolh = readEndianDep4(reader);

    temp = temp + 4 + CENTRAL_DIRECTORY_FILE_HEADER_SIZE;

    if ((ptr->filenameLength > 1024) || 
        (reader.max - temp) < ptr->filenameLength)
      return;

    ptr->filename = (char*)allocator.Allocate(ptr->filenameLength + 1,
                                              "Zip file name");
    memcpy(ptr->filename, bytes->elements + temp,
           ptr->filenameLength);
    ptr->filename[ptr->filenameLength] = 0;

    if (ptr->filename[ptr->filenameLength - 1] != PATH_SEPARATOR) {
      filetable.insert(std::make_pair(ptr->filename, ptr));
    }

    temp = temp + ptr->filenameLength + ptr->extraFieldLength + 
      ptr->fileCommentLength;
  }
}

sint32 ZipArchive::readFile(ClassBytes* array, const ZipFile* file) {
  uint32 bytesLeft = 0;
  uint32 filenameLength = 0;
  uint32 extraFieldLength = 0;
  uint32 temp = 0;

  Reader reader(bytes);
  reader.cursor = file->rolh;
  
  if (!(memcmp(bytes->elements + file->rolh, HDR_LOCAL, 4))) {
    reader.cursor += 4;
    temp = reader.cursor;
    reader.cursor += L_FILENAME_LENGTH;
    filenameLength = readEndianDep2(reader);
    extraFieldLength = readEndianDep2(reader);

    reader.cursor = 
      temp + extraFieldLength + filenameLength + LOCAL_FILE_HEADER_SIZE;

    if (file->compressionMethod == ZIP_STORE) {
      memcpy(array->elements, bytes->elements + reader.cursor, file->ucsize);
      return 1;
    } else if (file->compressionMethod == ZIP_DEFLATE) {
      z_stream stre;
      sint32 err = 0;
      
      bytesLeft = file->csize;
      stre.next_out = (Bytef*)array->elements;
      stre.avail_out = file->ucsize;
      stre.zalloc = 0;
      stre.zfree = 0;

      err = inflateInit2_(&stre, - DEF_WBITS, zlib_version, sizeof(z_stream));
  
      if (err != Z_OK) {
        return 0;
      }

      while (bytesLeft) {
        uint32 size = 0;
        stre.next_in = bytes->elements + reader.cursor;
        if (bytesLeft > 1024) size = 1024;
        else size = bytesLeft;

        uint32 diff = reader.max - reader.cursor;
        if (diff < size) {
          stre.avail_in = diff;
          reader.cursor = reader.max;
        } else {
          stre.avail_in = size;
          reader.cursor += size;
        }

        if (bytesLeft > size) {
          err = inflate(&stre, Z_PARTIAL_FLUSH);
        } else {
          err = inflate(&stre, Z_FINISH);
        }

        bytesLeft = bytesLeft - size;
      }

      inflateEnd(&stre);

      if ((err != Z_STREAM_END) && 
          (bytesLeft || err != Z_BUF_ERROR || stre.avail_out)) {
        return 0;
      } else {
        return 1;
      }
    } else {
      return 0;
    }
  } else {
    return 0;
  }
  return 0;
}
