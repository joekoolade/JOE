/*
 * ClassFile.h
 *
 *  Created on: Jul 29, 2010
 *      Author: joe
 */

#ifndef CLASSFILE_H_
#define CLASSFILE_H_

#include <stdio.h>

#include "ZipFile.h"
#include "ConstantPool.h"
#include "AttributeInfo.h"
#include "Fields.h"
#include "Interfaces.h"
#include "Methods.h"

typedef unsigned int u4;
typedef unsigned short u2;
typedef unsigned char u1;

#define JAVA_MAGIC	0xCAFEBABE

class   ClassFile {
private:
    	u4 magic;
    	u2 minor_version;
    	u2 major_version;
    	u2 constant_pool_count;
    	ConstantPool constantPool;
    	u2 access_flags;
    	u2 this_class;
    	u2 super_class;
    	u2 interfaces_count;
    	Interfaces interfaces;
    	u2 fields_count;
    	FieldInfo fields;
    	u2 methods_count;
    	MethodInfo methods;
    	u2 attributes_count;
    	Attributes attributes;

    	uint8_t *zfilePtr;
    	void readMagic();
    	void readVersion();
    	void readConstants();
    	void readAccessFlags();
    	void readClassIndex();
    	void readInterfaces();
    	void readFields();
    	void readMethods();
    	void readAttributes();
public:
    	ClassFile(ZipFile&);
    	virtual ~ClassFile();
    	uint8_t *getFilePtr();
    	void setFilePtr(uint8_t *);
    	int constantPoolCount();
};

#endif /* CLASSFILE_H_ */
