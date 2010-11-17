/*
 * Fields.h
 *
 *  Created on: Nov 11, 2010
 *      Author: joe
 */

#ifndef FIELDS_H_
#define FIELDS_H_

#include <vector>

#include "AttributeInfo.h"

class AttributeInfo;
class Fields;

using namespace std;

class FieldInfo {
	vector<Fields *> fieldsTable;
public:
	FieldInfo();
	virtual ~FieldInfo();
	void add(ClassFile *);
};

class Fields {
	uint16_t accessFlags;
	uint16_t nameIndex;
	uint16_t descriptorIndex;
	uint16_t attributeCount;
	Attributes attributes;
public:
	Fields();
	virtual ~Fields();
	void setFlags(uint16_t);
	void setNameIndex(uint16_t);
	void setDescriptorIndex(uint16_t);
	void setAttributeCount(uint16_t);
	uint16_t getAttributeCount();
	void addAttributes(ClassFile *);
};

#endif /* FIELDS_H_ */
