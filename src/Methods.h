/*
 * Methods.h
 *
 *  Created on: Nov 13, 2010
 *      Author: joe
 */

#ifndef METHODS_H_
#define METHODS_H_

#include <iostream>

#include <endian.h>

#include "AttributeInfo.h"

class Methods;

class MethodInfo {
	vector<Methods *> methodsTable;
public:
	MethodInfo();
	virtual ~MethodInfo();
	void add(ClassFile *);

};

class Methods {
	uint16_t accessFlags;
	uint16_t nameIndex;
	uint16_t descriptorIndex;
	uint16_t attributeCount;
	Attributes attributes;
public:
	Methods();
	virtual ~Methods();
	void setFlags(uint16_t);
	uint16_t getFlags();
	void setNameIndex(uint16_t);
	uint16_t getNameIndex();
	void setDescriptorIndex(uint16_t);
	uint16_t getDescriptorIndex();
	void setAttributeCount(uint16_t);
	uint16_t getAttributeCount();
	void addAttributes(ClassFile *);
};

#endif /* METHODS_H_ */
