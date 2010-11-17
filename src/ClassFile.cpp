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
	readMagic();
	readVersion();
	readConstants();
	readAccessFlags();
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
	if(magic != JAVA_MAGIC) {
		printf("bad magic: %x", magic);
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
	// cout << "# of constants: " << constant_pool_count << endl;
	zfilePtr += 2;
	constantPool.add(this);

}

void ClassFile::readAccessFlags() {
	access_flags = be16toh(*(uint16_t *)zfilePtr);
	zfilePtr += 2;
}

void ClassFile::readClassIndex() {
	this_class = be16toh(*(uint16_t *)zfilePtr);
	super_class = be16toh(*(uint16_t *)(zfilePtr+2));
	zfilePtr += 4;
}

void ClassFile::readInterfaces() {
	interfaces_count = be16toh(*(uint16_t *)zfilePtr);
	zfilePtr += 2;
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
