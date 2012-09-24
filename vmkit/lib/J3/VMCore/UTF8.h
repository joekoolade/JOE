//===------------------- UTF8.h - Utilities for UTF8 ----------------------===//
//
//                            The VMKit project
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//===----------------------------------------------------------------------===//

#ifndef _JNJVM_UTF8_H_
#define _JNJVM_UTF8_H_

#include "types.h"

#include "mvm/UTF8.h"

namespace j3 {
	using mvm::UTF8;
	using mvm::UTF8Map;

/// UTF8Buffer - Helper class to create char* buffers suitable for
/// printf.
///
class UTF8Buffer {
public:
  char* contents;

  /// UTF8Buffer - Create a buffer with the following UTF8.
  UTF8Buffer(const UTF8* val) {
    contents = new char[val->size + 1];
    for (int i = 0; i < val->size; i++) {
      contents[i] = (char)val->elements[i];
    }
    contents[val->size] = 0;
  }

  char* cString() const {
    return contents;
  }

  /// toCompileName - Change the utf8 following JNI conventions.
  ///
  UTF8Buffer* toCompileName(const char* suffix = "") {
		const char *buffer = contents;
    uint32 len = strlen(buffer);
    uint32 suffixLen = strlen(suffix);
    char* newBuffer = new char[(len << 3) + suffixLen + 1];
    uint32 j = 0;
    for (uint32 i = 0; i < len; ++i) {
      if (buffer[i] == '/') {
        newBuffer[j++] = '_';
      } else if (buffer[i] == '_') {
        newBuffer[j++] = '_';
        newBuffer[j++] = '1';
      } else if (buffer[i] == ';') {
        newBuffer[j++] = '_';
        newBuffer[j++] = '2';
      } else if (buffer[i] == '[') {
        newBuffer[j++] = '_';
        newBuffer[j++] = '3';
      } else if (buffer[i] == '$') {
        newBuffer[j++] = '_';
        newBuffer[j++] = '0';
        newBuffer[j++] = '0';
        newBuffer[j++] = '0';
        newBuffer[j++] = '2';
        newBuffer[j++] = '4';
      } else {
        newBuffer[j++] = buffer[i];
      }
    }
    for (uint32 i = 0; i < suffixLen; i++) {
      newBuffer[j++] = suffix[i];
    }
    newBuffer[j] = 0;
    delete[] contents;
		contents = newBuffer;
    return this;
  }

  ~UTF8Buffer() {
    delete[] contents;
  }
};

}

#endif
