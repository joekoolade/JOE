/*
 * AttributeInfo.cpp
 *
 *  Created on: Nov 11, 2010
 *      Author: joe
 */

#include <iostream>

#include <endian.h>

#include "AttributeInfo.h"
#include "ConstantPool.h"
#include "Fields.h"
#include "ClassFile.h"

AttributeInfo::AttributeInfo() {
	// TODO Auto-generated constructor stub

}

AttributeInfo::AttributeInfo(uint16_t name, uint16_t size) {
	nameIndex = name;
	length = size;
}

AttributeInfo::~AttributeInfo() {
	// TODO Auto-generated destructor stub
}

Attributes::Attributes() {

}

Attributes::~Attributes() {

}

void Attributes::add(ClassFile *file) {

}

void Attributes::addFieldAttributes(ClassFile *file, Fields *field) {
	uint8_t *data;
	uint16_t count, nameIndex;
	int i;
	uint32_t length;
	ConstantPool *cp;
	CPUtf8Info *utf;

	data = file->getFilePtr();
	count = field->getAttributeCount();
	cout << "field attrs added: " << count << endl;
	for(i=0; i < count; i++) {
		nameIndex = be16toh(*(uint16_t *)data);
		data += 2;
		length = be32toh(*(uint32_t *)data);
		data += 4;
		printf("attr %d: %x %x\n", i, nameIndex, length);
		cp = file->getConstant(nameIndex);
		if(!cp->isUtf8())
			goto error;
		utf = static_cast<CPUtf8Info *>(cp);
		if(utf->name() == CONSTANT_ATTR_NAME) {
			uint16_t constantIndex = be16toh(*(uint16_t *)data);
			data += 2;
			printf("constant: %x\n", constantIndex);
			ConstantAttribute *attr = new ConstantAttribute(nameIndex, constantIndex);
			attributes.assign(1, attr);
		} else if(utf->name() == SYNTHETIC_ATTR_NAME) {
			printf("synthetic\n");
			SyntheticAttribute *attr = new SyntheticAttribute(nameIndex);
			attributes.assign(1, attr);
		} else if(utf->name() == DEPRECATED_ATTR_NAME) {
			printf("deprecated\n");
			DeprecatedAttribute *attr = new DeprecatedAttribute(nameIndex);
			attributes.assign(1, attr);
		} else {
			cout << utf->name() << endl;
		}

	}
	cout << "attributes vector size: " << attributes.size() << endl;
	file->setFilePtr(data);
	return;
error:
	printf("Not a UTF8!\n");
	file->setFilePtr(data+length);
	throw(-4);
}

void Attributes::addMethodAttributes(ClassFile *file, Methods *method) {
	uint8_t *data;
	uint16_t count, nameIndex;
	int i;
	uint32_t length;
	ConstantPool *cp;
	CPUtf8Info *utf;

	data = file->getFilePtr();
	count = method->getAttributeCount();
	cout << "method attrs #: " << count << endl;
	for(i=0; i < count; i++) {
		nameIndex = be16toh(*(uint16_t *)data);
		data += 2;
		length = be32toh(*(uint32_t *)data);
		data += 4;
		printf("attr %d: %x %x\n", i, nameIndex, length);
		cp = file->getConstant(nameIndex);
		if(!cp->isUtf8())
			goto error;
		utf = static_cast<CPUtf8Info *>(cp);
		if(utf->name() == CODE_ATTR_NAME) {
			file->setFilePtr(data);
			CodeAttribute *attr = new CodeAttribute(file);
			attributes.assign(1, attr);
		} else if(utf->name() == SYNTHETIC_ATTR_NAME) {
			printf("synthetic\n");
			SyntheticAttribute *attr = new SyntheticAttribute(nameIndex);
			attributes.assign(1, attr);
		} else if(utf->name() == DEPRECATED_ATTR_NAME) {
			printf("deprecated\n");
			DeprecatedAttribute *attr = new DeprecatedAttribute(nameIndex);
			attributes.assign(1, attr);
		} else {
			cout << utf->name() << endl;
		}

	}
	file->setFilePtr(data);
	return;
error:
	printf("Not a UTF8!\n");
	file->setFilePtr(data+length);
	throw(-4);
}

CodeAttribute::CodeAttribute(){

}

CodeAttribute::~CodeAttribute(){

}

CodeAttribute::CodeAttribute(ClassFile *) {

}

ConstantAttribute::ConstantAttribute(uint16_t name, uint16_t constant)
	: AttributeInfo(name, 2)
{
	constantIndex = constant;
}

ConstantAttribute::~ConstantAttribute() {

}

SyntheticAttribute::SyntheticAttribute(uint16_t name)
	: AttributeInfo(name, 0)
{
}

SyntheticAttribute::~SyntheticAttribute() {

}

DeprecatedAttribute::DeprecatedAttribute(uint16_t name)
	: AttributeInfo(name, 0)
{
}

DeprecatedAttribute::~DeprecatedAttribute() {
}
