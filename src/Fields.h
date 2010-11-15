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
	vector<Fields> fieldsTable;
public:
	FieldInfo();
	virtual ~FieldInfo();
	void add(ClassFile *);
};

class Fields {
	uint16_t accessFlags;
	uint16_t nameIndex;
	vector<AttributeInfo> attributes;
public:
	Fields();
	virtual ~Fields();
};

#endif /* FIELDS_H_ */
