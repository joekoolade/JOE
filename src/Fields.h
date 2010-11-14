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

class FieldInfo {
	vector<Fields> fieldsTable;
public:
	FieldInfo();
	virtual ~FieldInfo();
	uint8_t *add(uint8_t *, uint16_t);
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
