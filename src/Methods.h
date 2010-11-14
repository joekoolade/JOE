/*
 * Methods.h
 *
 *  Created on: Nov 13, 2010
 *      Author: joe
 */

#ifndef METHODS_H_
#define METHODS_H_

#include "AttributeInfo.h"

class MethodInfo {
public:
	MethodInfo();
	virtual ~MethodInfo();
	uint8_t *add(uint8_t *, uint16_t);
};

class Methods {
	uint16_t accessFlags;
	uint16_t nameIndex;
	uint16_t descriptorIndex;
	vector<attributeInfo> attributes;
public:
	Methods();
	virtual ~Methods();
};

#endif /* METHODS_H_ */
