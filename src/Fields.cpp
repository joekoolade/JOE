/*
 * Fields.cpp
 *
 *  Created on: Nov 11, 2010
 *      Author: joe
 */

#include <iostream>

#include <endian.h>

#include "Fields.h"
#include "ClassFile.h"

Fields::Fields() {
	// TODO Auto-generated constructor stub

}

Fields::~Fields() {
	// TODO Auto-generated destructor stub
}

FieldInfo::FieldInfo() {
	// TODO Auto-generated constructor stub

}

FieldInfo::~FieldInfo() {
	// TODO Auto-generated destructor stub
}

uint16_t Fields::getAttributeCount() {
	return attributeCount;
}

void Fields::setAttributeCount(uint16_t count) {
	attributeCount = count;
}

void Fields::setFlags(uint16_t flags) {
	accessFlags = flags;
}

void Fields::setNameIndex(uint16_t index) {
	nameIndex = index;
}

void Fields::setDescriptorIndex(uint16_t index) {
	descriptorIndex = index;
}

void Fields::addAttributes(ClassFile *file) {
	attributes.addFieldAttributes(file, this);
}

void FieldInfo::add(ClassFile *classFile) {
	uint8_t *data;
	uint16_t fieldCount;
	Fields *aField;
	int i,j;
	uint16_t attribCount;

	data = classFile->getFilePtr();
	fieldCount = classFile->fieldCount();
	cout << "fields added: " << fieldCount << " ";
	fieldsTable.resize(fieldCount);
	for(i=0; i < fieldCount; i++) {
		aField = new Fields();
		aField->setFlags(be16toh(*(uint16_t *)data));
		data += 2;
		aField->setNameIndex(be16toh(*(uint16_t *)data));
		data += 2;
		aField->setDescriptorIndex(be16toh(*(uint16_t *)data));
		data += 2;
		aField->setAttributeCount(be16toh(*(uint16_t *)data));
		data +=2;
		classFile->setFilePtr(data);
		if(aField->getAttributeCount()==0)
			continue;
		aField->addAttributes(classFile);
	}
}
