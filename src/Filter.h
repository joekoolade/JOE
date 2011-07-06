/*
 * Filter.h
 *
 *  Created on: Apr 24, 2011
 *      Author: joe
 */

#ifndef FILTER_H_
#define FILTER_H_

#include <string>

class Filter {
public:
	Filter();
	virtual ~Filter();

	bool match(string);
};

#endif /* FILTER_H_ */
