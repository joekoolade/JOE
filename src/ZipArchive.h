/*
 * ZipArchive.h
 *
 *  Created on: Jun 25, 2010
 *      Author: joe
 */

#ifndef ZIPARCHIVE_H_
#define ZIPARCHIVE_H_


class ZipArchive {
private:
	string archiveName;
	uint32 size;
public:
	std::map<string, ZipFile> fileTable;
	ZipArchive(string&);
	~ZipArchive();

};

class ZipFileOpenFailure {
public:
	ZipFileOpenFailure() {};
};

};
#endif /* ZIPARCHIVE_H_ */
