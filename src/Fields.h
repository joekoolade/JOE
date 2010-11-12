/*
 * Fields.h
 *
 *  Created on: Nov 11, 2010
 *      Author: joe
 */

#ifndef FIELDS_H_
#define FIELDS_H_

#include "AttributeInfo.h"

class AttributeInfo;

class FieldInfo {
	uint16_t accessFlags;
	uint16_t nameIndex;
	vector<AttributeInfo> attributes;
public:
	FieldInfo();
	virtual ~FieldInfo();
};

class Fields {

public:
	Fields();
	virtual ~Fields();
};

#endif /* FIELDS_H_ */
