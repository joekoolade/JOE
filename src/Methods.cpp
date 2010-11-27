/*
 * Methods.cpp
 *
 *  Created on: Nov 13, 2010
 *      Author: joe
 */

#include "Methods.h"
#include "ClassFile.h"

Methods::Methods() {
	// TODO Auto-generated constructor stub

}

Methods::~Methods() {
	// TODO Auto-generated destructor stub
}

MethodInfo::MethodInfo() {

}

MethodInfo::~MethodInfo() {

}

uint16_t Methods::getAttributeCount() {
	return attributeCount;
}

void Methods::setAttributeCount(uint16_t count) {
	attributeCount = count;
}

void Methods::setFlags(uint16_t flags) {
	accessFlags = flags;
}

uint16_t Methods::getFlags() {
	return accessFlags;
}

void Methods::setNameIndex(uint16_t index) {
	nameIndex = index;
}

uint16_t Methods::getNameIndex() {
	return nameIndex;
}

void Methods::setDescriptorIndex(uint16_t index) {
	descriptorIndex = index;
}

uint16_t Methods::getDescriptorIndex() {
	return descriptorIndex;
}

void Methods::addAttributes(ClassFile *file) {
	attributes.addMethodAttributes(file, this);
}

void MethodInfo::add(ClassFile *classFile) {
	uint8_t *data;
	uint16_t methodCount;
	Methods *aMethod;
	uint16_t val;
	int i;

	data = classFile->getFilePtr();
	methodCount = classFile->getMethodCount();
	methodsTable.resize(methodCount);
	for(i=0; i<methodCount; i++) {
		aMethod = new Methods();
		aMethod->setFlags(be16toh(*(uint16_t *)data));
		data += 2;
		aMethod->setNameIndex(be16toh(*(uint16_t *)data));
		data += 2;
		aMethod->setDescriptorIndex(be16toh(*(uint16_t *)data));
		data += 2;
		aMethod->setAttributeCount(be16toh(*(uint16_t *)data));
		data +=2;
		printf("method %d: %x %x %x %x\n", i, aMethod->getFlags(), aMethod->getNameIndex(),
				aMethod->getDescriptorIndex(), aMethod->getAttributeCount());
		classFile->setFilePtr(data);
		if(aMethod->getAttributeCount()==0)
			continue;
		aMethod->addAttributes(classFile);
		data = classFile->getFilePtr();
	}
}
