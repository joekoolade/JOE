/*
 * ZipArchive.h
 *
 *  Created on: Jun 25, 2010
 *      Author: joe
 */

#ifndef ZIPARCHIVE_H_
#define ZIPARCHIVE_H_

#include <map>
#include <string>

#include <stdint.h>

#include "ZipFile.h"

using namespace std;

class ZipArchive {
private:
	string archiveName;
	uint32_t size;
public:
	std::map<string, ZipFile> fileTable;
	ZipArchive(char *);
	~ZipArchive() {};
};

class ZipFileOpenFailure {
public:
	ZipFileOpenFailure() {};
};

#endif /* ZIPARCHIVE_H_ */
