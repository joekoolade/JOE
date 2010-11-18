/*
 * AttributeInfo.h
 *
 *  Created on: Nov 11, 2010
 *      Author: joe
 */

#ifndef ATTRIBUTEINFO_H_
#define ATTRIBUTEINFO_H_

#include <valarray>
#include <vector>
#include <string>

#include <stdint.h>

using namespace std;

#define CONSTANT_ATTR_NAME		"ConstantValue"
#define CODE_ATTR_NAME			"Code"
#define EXCEPTION_ATTR_NAME		"Exceptions"
#define INNERCLASS_ATTR_NAME	"InnerClasses"
#define SYNTHETIC_ATTR_NAME		"Synthetic"
#define SOURCEFILE_ATTR_NAME	"SourceFile"
#define LINENUMTABLE_ATTR_NAME	"LineNumberTable"
#define LOCALVARTABLE_ATTR_NAME	"LocalVariableTable"
#define DEPRECATED_ATTR_NAME	"Deprecated"

class AttributeInfo;
class ClassFile;
class Fields;
class Methods;

class Attributes {
	vector<AttributeInfo *> attributes;
public:
	Attributes();
	virtual ~Attributes();
	void add(ClassFile *);
	void addFieldAttributes(ClassFile *, Fields *);
	void addMethodAttributes(ClassFile *, Methods *);
};

class AttributeInfo {
	uint16_t nameIndex;
	uint32_t length;
public:
	AttributeInfo();
	AttributeInfo(uint16_t,uint16_t);
	virtual ~AttributeInfo();
};

class ConstantAttribute : public AttributeInfo {
	uint16_t constantIndex;
public:
	ConstantAttribute();
	ConstantAttribute(uint16_t,uint16_t);
	virtual ~ConstantAttribute();
};

class ExceptionTable {
	uint16_t startpc;
	uint16_t endpc;
	uint16_t handlerpc;
	uint16_t catchType;
public:
	ExceptionTable();
	virtual ~ExceptionTable();
};

class CodeAttribute : public AttributeInfo {
	uint16_t maxStack;
	uint16_t maxLocals;
	uint32_t length;
	valarray<uint8_t> code;
	vector<ExceptionTable> exceptionTable;
	vector<AttributeInfo> attributes;
public:
	CodeAttribute();
	CodeAttribute(ClassFile *);
	virtual ~CodeAttribute();
};

class ExceptionsAttribute : public AttributeInfo {
	valarray<int> exceptionIndexTable;
public:
	ExceptionsAttribute();
	virtual ~ExceptionsAttribute();
};

class InnerClassInfo {
	uint16_t innerClassInfo;
	uint16_t outerClassInfo;
	uint16_t innerName;
	uint16_t innerClassAccessFlags;
};

class InnerClassesAttribute : public AttributeInfo {
	vector<InnerClassInfo> innerClasses;
public:
	InnerClassesAttribute();
	virtual ~InnerClassesAttribute();
};

class SyntheticAttribute : public AttributeInfo {
public:
	SyntheticAttribute();
	SyntheticAttribute(uint16_t);
	virtual ~SyntheticAttribute();
};

class SourceFileAttribute : public AttributeInfo {
	uint16_t sourceFile;
public:
	SourceFileAttribute();
	virtual ~SourceFileAttribute();
};

class LineNumberInfo {
	uint16_t startpc;
	uint16_t lineNumber;
public:
	LineNumberInfo();
	virtual ~LineNumberInfo();
};

class LineNumberTableAttribute : public AttributeInfo {
	vector<LineNumberInfo> lineNumberTable;
public:
	LineNumberTableAttribute();
	virtual ~LineNumberTableAttribute();
};

class LocalVariableInfo {
	uint16_t startpc;
	uint16_t length;
	uint16_t nameIndex;
	uint16_t descriptorIndex;
	uint16_t index;
public:
	LocalVariableInfo();
	virtual ~LocalVariableInfo();
};

class LocalVariableTableAttribute : public AttributeInfo {
	vector<LocalVariableInfo> localVariableTable;
public:
	LocalVariableTableAttribute();
	virtual ~LocalVariableTableAttribute();
};

class DeprecatedAttribute : public AttributeInfo {
public:
	DeprecatedAttribute();
	DeprecatedAttribute(uint16_t);
	virtual ~DeprecatedAttribute();
};
#endif /* ATTRIBUTEINFO_H_ */
