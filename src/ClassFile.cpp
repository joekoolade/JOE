/*
 * ClassFile.cpp
 *
 *  Created on: Jul 29, 2010
 *      Author: joe
 */

#include "ClassFile.h"

ClassFile::ClassFile(ZipFile file) {

	zfilePtr = file.getData();
	readMagic();
	readVersion();
	readConstants();
	access_flags = *(uint16_t *)data;
	data += 2;
	this_class = *(uint16_t *)data;
	data += 2;
	super_class = *(uint16_t *)data;
	data += 2;
	interfaces_count = *(uint16_t *)data;
	data += 2;
	data = interfaces.add(data, interfaces_count);
	fields_count = *(uint16_t *)data;
	data += 2;
	data = fields.add(data, fields_count);
	method_count = *(uint16_t *)data;
	data += 2;
	data = methods.add(data, method_count);
	attributes_count = *(uint16_t *)data;
	data += 2;
	attributes.add(data, attributes_count);
}

/**
 * Read and verify class file magic
 */
void ClassFile::readMagic() {
	magic = *(uint32_t *)zfilePtr;
	if(magic != JAVA_MAGIC) {
		fprintf(stderr, "bad magic: %x", magic);
		throw(-1);
	}
	// advance file pointer
	zfilePtr += 4;
}

/*
 * Read and store class file version
 */
void ClassFile::readVersion() {
	minor_version = *(uint16_t *)zfilePtr;
	major_version = *(uint16_t *)(zfilePtr+2);
	printf("class version: %d:%d", major_verison, minor_version);
	zfilePtr += 4;
}

/*
 * Read in the constant pool
 */
void ClassFile::readConstants() {
	constant_pool_count = *(uint16_t *)zfilePtr;
	zfilePtr += 2;
	data = constantPool.add(this);

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
