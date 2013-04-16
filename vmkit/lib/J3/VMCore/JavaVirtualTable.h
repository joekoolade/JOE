/*
 * JavaVirtualTable.h
 *
 *  Created on: Mar 18, 2013
 *      Author: jkulig
 */

#ifndef JAVAVIRTUALTABLE_H_
#define JAVAVIRTUALTABLE_H_

#include "j3/System.h"

extern "C" void EmptyDestructor();

class VirtualTable {
 public:
  word_t destructor;
  word_t operatorDelete;
  word_t tracer;
  word_t specializedTracers[1];

  static uint32_t numberOfBaseFunctions() {
    return 4;
  }

  static uint32_t numberOfSpecializedTracers() {
    return 1;
  }

  word_t* getFunctions() {
    return &destructor;
  }

  VirtualTable(word_t d, word_t o, word_t t) {
    destructor = d;
    operatorDelete = o;
    tracer = t;
  }

  VirtualTable() {
    destructor = reinterpret_cast<word_t>(EmptyDestructor);
  }

  bool hasDestructor() {
    return destructor != reinterpret_cast<word_t>(EmptyDestructor);
  }

  static void emptyTracer(void*) {}
};



#endif /* JAVAVIRTUALTABLE_H_ */
