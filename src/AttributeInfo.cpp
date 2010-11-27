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
			printf("Code\n");
			file->setFilePtr(data);
			CodeAttribute *attr = new CodeAttribute(file, nameIndex, length);
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
	printf("added %d attributes\n", attributes.size());
	file->setFilePtr(data);
	return;
error:
	printf("Not a UTF8!\n");
	file->setFilePtr(data+length);
	throw(-4);
}

ExceptionTable::ExceptionTable() {

}

ExceptionTable::ExceptionTable(ClassFile *classFile) {
	uint8_t *data;

	data = classFile->getFilePtr();
	startpc = be16toh(*(uint16_t *)data);
	data += 2;
	endpc = be16toh(*(uint16_t *)data);
	data += 2;
	handlerpc = be16toh(*(uint16_t *)data);
	data += 2;
	catchType = be16toh(*(uint16_t *)data);
	data += 2;
	classFile->setFilePtr(data);
}

ExceptionTable::~ExceptionTable(){
}

CodeAttribute::CodeAttribute(){

}

CodeAttribute::~CodeAttribute(){

}

CodeAttribute::CodeAttribute(ClassFile *classFile, uint16_t name, uint16_t size) {
	uint8_t *data;
	int i;

	nameIndex = name;
	length = size;
	data = classFile->getFilePtr();
	maxStack = be16toh(*(uint16_t *)data);
	data += 2;
	maxLocals = be16toh(*(uint16_t *)data);
	data += 2;
	codeLength = be32toh(*(uint32_t *)data);
	data+=4;
	printf("code: %d %d %d\n", maxStack, maxLocals, codeLength);
	code.resize(codeLength);
	for(i=0; i < codeLength; i++, data++) {
		code[i] = *data;
	}
	exceptionTableLength = be16toh(*(uint16_t *)data);
	data += 2;
	printf("exception table: %d\n", exceptionTableLength);
	exceptionTable.resize(exceptionTableLength);
	classFile->setFilePtr(data);
	for(i=0; i < exceptionTableLength; i++) {
		exceptionTable[i] = new ExceptionTable(classFile);
	}
	data = classFile->getFilePtr();
	attributeCount = be16toh(*(uint16_t *)data);
	data += 2;
	printf("code attrs: %d\n", attributeCount);
	attributes.resize(attributeCount);
	for(i=0; i < attributeCount; i++) {
		uint16_t nameIndex = be16toh(*(uint16_t *)data);
		data += 2;
		uint16_t length = be32toh(*(uint32_t *)data);
		data += 4;
		printf("code attr %d: %x %x\n", i, nameIndex, length);
		ConstantPool *cp = classFile->getConstant(nameIndex);
		if(!cp->isUtf8()) {

		}
		CPUtf8Info *utf = static_cast<CPUtf8Info *>(cp);
		if(utf->name() == LINENUMTABLE_ATTR_NAME) {
			printf("Line Number Table\n");
			classFile->setFilePtr(data);
			LineNumberTableAttribute *attr = new LineNumberTableAttribute(classFile, nameIndex, length);
			attributes.assign(1, attr);
		} else if(utf->name() == LOCALVARTABLE_ATTR_NAME) {
			printf("Local Variable Table\n");
			classFile->setFilePtr(data);
			LocalVariableTableAttribute *attr = new LocalVariableTableAttribute(classFile, nameIndex, length);
			attributes.assign(1, attr);
		} else {
			cout << utf->name() << ": IGNORED!" << endl;
		}
	}
}

LocalVariableInfo::~LocalVariableInfo() {
}

LocalVariableInfo::LocalVariableInfo(uint16_t pc, uint16_t length0, uint16_t name, uint16_t desc, uint16_t index0) {
	startpc = pc;
	length = length0;
	nameIndex = name;
	descriptorIndex = desc;
	index = index0;
}

LocalVariableTableAttribute::~LocalVariableTableAttribute() {
}

LocalVariableTableAttribute::LocalVariableTableAttribute(ClassFile *classFile, uint16_t name, uint16_t length)
	: AttributeInfo(name, length)
{
	uint8_t *data;
	int i;
	uint16_t startpc, length0, nameIndex, descriptorIndex, index;

	data = classFile->getFilePtr();
	uint16_t tableLength = be16toh(*(uint16_t *)data);
	localVariableTable.resize(tableLength);
	for(i=0; i < tableLength; i++) {
		startpc = be16toh(*(uint16_t *)data);
		data += 2;
		length0 = be16toh(*(uint16_t *)data);
		data += 2;
		nameIndex = be16toh(*(uint16_t *)data);
		data += 2;
		descriptorIndex = be16toh(*(uint16_t *)data);
		data += 2;
		index = be16toh(*(uint16_t *)data);
		data += 2;
		localVariableTable[i] = new LocalVariableInfo(startpc, length0, nameIndex, descriptorIndex, index);
	}
}

LineNumberTableAttribute::~LineNumberTableAttribute() {
}

LineNumberTableAttribute::LineNumberTableAttribute(ClassFile *, uint16_t, uint16_t) {

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
