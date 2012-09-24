/*
 * ClassFile.cpp
 *
 *  Created on: Jul 29, 2010
 *      Author: joe
 */

#include "ClassFile.h"

#include <iostream>

#include <endian.h>

using namespace std;

ClassFile::~ClassFile() {

}

ClassFile::ClassFile(ZipFile &file) {

	if(file.size==0) {
		printf("%s: file is zero!\n", file.name.c_str());
		return;
	}
	zfilePtr = file.getData();
	data0 = zfilePtr;
	readMagic();
	readVersion();
	readConstants();
	readAccessFlags();
	readClassIndex();
	readInterfaces();
	readFields();
	readMethods();
	readAttributes();
}

/**
 * Read and verify class file magic
 */
void ClassFile::readMagic() {
	magic = be32toh(*(uint32_t *)zfilePtr);
	// cout << "magic: " << hex << magic << endl;
	if(magic != JAVA_MAGIC) {
		printf("bad magic: %x\n", magic);
		throw(-1);
	}
	// advance file pointer
	zfilePtr += 4;
}

/*
 * Read and store class file version
 */
void ClassFile::readVersion() {
	minor_version = be16toh(*(uint16_t *)zfilePtr);
	major_version = be16toh(*(uint16_t *)(zfilePtr+2));
	// printf("class version: %d:%d\n", major_version, minor_version);
	zfilePtr += 4;
}

/*
 * Read in the constant pool
 */
void ClassFile::readConstants() {
	constant_pool_count = be16toh(*(uint16_t *)zfilePtr);
	cout << "# of constants: " << constant_pool_count;
	printf(" 0x%x ", zfilePtr-data0);
	zfilePtr += 2;
	constantPool.add(this);
	printf("0x%x\n", zfilePtr-data0);

}

void ClassFile::readAccessFlags() {
	access_flags = be16toh(*(uint16_t *)zfilePtr);
	printf("flags: %x  ", access_flags);
	zfilePtr += 2;
}

void ClassFile::readClassIndex() {
	this_class = be16toh(*(uint16_t *)zfilePtr);
	super_class = be16toh(*(uint16_t *)(zfilePtr+2));
	printf("classes: %x %x\n", this_class, super_class);
	zfilePtr += 4;
}

void ClassFile::readInterfaces() {
	interfaces_count = be16toh(*(uint16_t *)zfilePtr);
	zfilePtr += 2;
	if(interfaces_count==0)
		return;
	else
		interfaces.add(this);
}

void ClassFile::readFields() {
	fields_count = be16toh(*(uint16_t *)zfilePtr);
	zfilePtr += 2;
	fields.add(this);
}

void ClassFile::readMethods() {
	methods_count = be16toh(*(uint16_t *)zfilePtr);
	zfilePtr += 2;
	printf("methods #: %d\n", methods_count);
	methods.add(this);
}

void ClassFile::readAttributes() {
	attributes_count = be16toh(*(uint16_t *)zfilePtr);
	zfilePtr += 2;
	attributes.add(this);
}


/**
 * Return pointer to class file
 */
uint8_t *ClassFile::getFilePtr() {
	return zfilePtr;
}

/**
 * Set the class file pointer
 */
void ClassFile::setFilePtr(uint8_t *ptr) {
	zfilePtr = ptr;
}

int ClassFile::constantPoolCount() {
	return constant_pool_count;
}

uint16_t ClassFile::interfaceCount() {
	return interfaces_count;
}

uint16_t ClassFile::fieldCount() {
	return fields_count;
}

uint16_t ClassFile::getMethodCount() {
	return methods_count;
}

ConstantPool * ClassFile::getConstant(uint16_t index) {
	return constantPool.getConstant(index);
}
