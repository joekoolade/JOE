/*
 * ConstantPool.h
 *
 *  Created on: Jul 29, 2010
 *      Author: joe
 */

#ifndef CONSTANTPOOL_H_
#define CONSTANTPOOL_H_

#include <vector>
#include <string>

#include <stdint.h>

typedef unsigned int u4;
typedef unsigned short u2;
typedef unsigned char u1;

#define CONSTANT_Utf8		1
#define CONSTANT_Integer 	3
#define CONSTANT_Float		4
#define CONSTANT_Long		5
#define CONSTANT_Double		6
#define CONSTANT_Class		7
#define CONSTANT_String		8
#define CONSTANT_Fieldref	9
#define CONSTANT_Methodref	10
#define CONSTANT_InterfaceMethodref	11
#define CONSTANT_NameAndType		12

class ClassFile;

using namespace std;

class ConstantPool {
private:
	u1 tag;
	vector<ConstantPool> constants;
public:
	ConstantPool();
	void getConstantPool(uint8_t *);
	ConstantPool getConstant(uint16_t);
	void add(ClassFile *);
};

class CPClassInfo : public ConstantPool {
private:
	u2 nameIndex;
public:
	CPClassInfo(ClassFile *);

};

class CPFieldref : public ConstantPool {
private:
	u2 classIndex;
	u2 nameTypeIndex;
public:
	CPFieldref(ClassFile *);
};

class CPMethodref : public ConstantPool {
private:
	u2 classIndex;
	u2 nameTypeIndex;
public:
	CPMethodref(ClassFile *);
};

class CPInterfaceref : public ConstantPool {
private:
	u2 classIndex;
	u2 nameTypeIndex;
public:
	CPInterfaceref(ClassFile *);
};

class CPStringInfo : public ConstantPool {
private:
	u2 stringIndex;
public:
	CPStringInfo(ClassFile *);
};

class CPIntegerInfo : public ConstantPool {
private:
	u4 bytes;
public:
	CPIntegerInfo(ClassFile *);
};

class CPFloatInfo : public ConstantPool {
private:
	u4 bytes;
public:
	CPFloatInfo(ClassFile *);
};

class CPLongInfo : public ConstantPool {
private:
	uint64_t bytes;
public:
	CPLongInfo(ClassFile *);
};

class CPDoubleInfo : public ConstantPool {
private:
	uint64_t bytes;
public:
	CPDoubleInfo(ClassFile *);
};

class CPNameTypeInfo : public ConstantPool {
private:
	u2 nameIndex;
	u2 descriptorIndex;
public:
	CPNameTypeInfo(ClassFile *);
};

class CPUtf8Info : public ConstantPool {
private:
	u2 length;
	string aString;
public:
	CPUtf8Info(ClassFile *);
};

#endif /* CONSTANTPOOL_H_ */
