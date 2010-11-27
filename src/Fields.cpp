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

uint16_t Fields::getFlags() {
	return accessFlags;
}

void Fields::setNameIndex(uint16_t index) {
	nameIndex = index;
}

uint16_t Fields::getNameIndex() {
	return nameIndex;
}

void Fields::setDescriptorIndex(uint16_t index) {
	descriptorIndex = index;
}

uint16_t Fields::getDescriptorIndex() {
	return descriptorIndex;
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
	cout << "fields added: " << fieldCount << endl;
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
		printf("field %d: %x %x %x %x\n", i, aField->getFlags(), aField->getNameIndex(),
				aField->getDescriptorIndex(), aField->getAttributeCount());
		if(aField->getAttributeCount()==0)
			continue;
		aField->addAttributes(classFile);
		data = classFile->getFilePtr();
	}
}
