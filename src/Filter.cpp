/*
 * Filter.cpp
 *
 *  Created on: Apr 24, 2011
 *      Author: joe
 */

#include "Filter.h"

Filter::Filter() {
	// TODO Auto-generated constructor stub
	add("java/lang/.*");
	add("java/util/.*");
}

Filter::~Filter() {
	// TODO Auto-generated destructor stub
}

bool
Filter::match
