/*
 * Interfaces.cpp
 *
 *  Created on: Nov 11, 2010
 *      Author: joe
 */

#include <iostream>

#include <stdint.h>

#include "Interfaces.h"
#include "ClassFile.h"

Interfaces::Interfaces() {
	// TODO Auto-generated constructor stub

}

Interfaces::~Interfaces() {
	// TODO Auto-generated destructor stub
}

void Interfaces::add(ClassFile *file) {
	int i;
	uint8_t *data;

	data = file->getFilePtr();
	interfaces.resize(file->interfaceCount());
	cout << "interface count: " << file->interfaceCount() << endl;
	for(i=0; i<file->interfaceCount(); i++) {
		interfaces[i] = *(uint16_t *)data;
		data += 2;
	}
	file->setFilePtr(data);
}
