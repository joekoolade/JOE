/*
 * AttributeInfo.h
 *
 *  Created on: Nov 11, 2010
 *      Author: joe
 */

#ifndef ATTRIBUTEINFO_H_
#define ATTRIBUTEINFO_H_

#include <valarray>

#define CONSTANT_ATTR_NAME		"ConstantValue"
#define CODE_ATTR_NAME			"Code"
#define EXCEPTION_ATTR_NAME		"Exceptions"
#define INNERCLASS_ATTR_NAME	"InnerClasses"
#define SYNTHETIC_ATTR_NAME		"Synthetic"
#define SOURCEFILE_ATTR_NAME	"SourceFile"
#define LINENUMTABLE_ATTR_NAME	"LineNumberTable"
#define LOCALVARTABLE_ATTR_NAME	"LocalVariableTable"
#define DEPRECATED_ATTR_NAME	"Deprecated"

class Attributes {
	vector<AttributeInfo> index;
public:
	Attributes();
	virtual ~Attributes();
	uint8_t *add(uint8_t *, uint16_t);
};

class AttributeInfo {
	uint16_t nameIndex;
	uint32_t length;
public:
	AttributeInfo();
	virtual ~AttributeInfo();
};

class ConstantAttribute : public AttributeInfo {
	uint16_t constantIndex;
	static string name = "ConstantValue";
public:
	ConstantAttribute();
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
	vector<InnerClassInfo>;
public:
	InnerClassesAttribute();
	virtual ~InnerClassesAttribute();
};

class SyntheticAttribute : public AttributeInfo {
public:
	SyntheticAttribute();
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
	virtual ~DeprecatedAttribute();
};
#endif /* ATTRIBUTEINFO_H_ */
