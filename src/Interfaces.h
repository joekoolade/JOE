/*
 * Interfaces.h
 *
 *  Created on: Nov 11, 2010
 *      Author: joe
 */

#ifndef INTERFACES_H_
#define INTERFACES_H_

#include <vector>

class Interfaces {
private:
	vector<int> interfaces;

public:
	Interfaces();
	virtual ~Interfaces();
	void add(int);
	int get();
};

#endif /* INTERFACES_H_ */
