/*
 * ClassFile.h
 *
 *  Created on: Jul 29, 2010
 *      Author: joe
 */

#ifndef CLASSFILE_H_
#define CLASSFILE_H_

typedef unsigned int u4;
typedef unsigned short u2;
typedef unsigned char u1;

class ConstantPool;
class FieldInfo;
class MethodInfo;
class AttributeInfo;
class Interfaces;

#define JAVA_MAGIC	"CAFEBABE"

class   ClassFile {
private:
    	u4 magic;
    	u2 minor_version;
    	u2 major_version;
    	u2 constant_pool_count;
    	ConstantPool constant_pool;
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
    	AttributeInfo attributes;
public:
    	ClassFile(ZipFile);

};

#endif /* CLASSFILE_H_ */
