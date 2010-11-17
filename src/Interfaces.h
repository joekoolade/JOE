/*
 * Interfaces.h
 *
 *  Created on: Nov 11, 2010
 *      Author: joe
 */

#ifndef INTERFACES_H_
#define INTERFACES_H_

#include <vector>

class ClassFile;

using namespace std;

class Interfaces {
private:
	vector<uint16_t> interfaces;

public:
	Interfaces();
	virtual ~Interfaces();
	void add(int);
	int get();
	void add(ClassFile *);
};

#endif /* INTERFACES_H_ */
