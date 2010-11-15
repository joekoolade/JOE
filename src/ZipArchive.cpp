/*
 * ZipArchive.cpp
 *
 *  Created on: Jun 24, 2010
 *      Author: joe
 */

#include <iostream>
#include <fstream>
#include <string>

#include <stdlib.h>

#include "ZipFile.h"
#include "ZipArchive.h"

using namespace std;

#define ZIP_FILES	0x00000001
#define DEBUG_FILE	if(debug & ZIP_FILES)

void ZipArchive::processCD(ifstream& in)
{
    CentralDirectory cd;
    streamoff backup = sizeof (ZipLocalFileHeader);
    in.seekg(-backup, ios::cur);
    in.read((char*)(&cd), sizeof (CentralDirectory));
    cout << "CDS lens: " << cd.fileNameLen << ' ' << cd.extraLen << ' ' << cd.commentLen << endl;
    if(cd.fileNameLen > 0){
        char name[1024];
        in.read(name, cd.fileNameLen);
        name[cd.fileNameLen] = '\0';
        DEBUG_FILE cout << "CDS name: " << name << endl;
    }
    if(cd.extraLen > 0){
        char extra[1024];
        in.read(extra, cd.extraLen);
        extra[cd.extraLen] = '\0';
        DEBUG_FILE cout << "extra: " << extra << endl;
    }
    if(cd.commentLen > 0){
        char comment[1024];
        in.read(comment, cd.commentLen);
        comment[cd.commentLen] = '\0';
        DEBUG_FILE cout << "comment: " << comment << endl;
    }
    DEBUG_FILE cout << "made by: " << cd.verMadeBy << "  version: " << cd.version;
    DEBUG_FILE cout << "  flag: " << cd.flag << "  compression: " << cd.compression << endl;
    DEBUG_FILE cout << " size: " << cd.uncompressedSize << " compressed to: " << cd.compressedSize << endl;
    DEBUG_FILE cout << " disk#: " << cd.diskNumber << " offset: " << cd.offset << endl;
}

ZipArchive::ZipArchive(char *zipfile) {
	size = 0;
	archiveName = string(zipfile);
	ifstream in(zipfile, ios::in|ios::binary);
	if(!in){
		cerr << "Could not open zipfile: " << zipfile << endl;
	}
	size = in.tellg();
	// Start reading in files
	ZipLocalFileHeader zipHeader;
	while(!in.read((char *)&zipHeader, sizeof(ZipLocalFileHeader)).eof()) {
		if(zipHeader.isZip()) {
			ZipFile file(in, zipHeader);
			file.inflate();
			fileTable.insert(make_pair(file.name, file));
			size++;
		} else if(0 && zipHeader.isCD()) {
			processCD(in);
		} else {
			cout << "End of zip archive!\n";
			break;
		}

	}
	printf("Number of classes is %d\n", size);
}

void ZipArchive::iterator() {
	iter = fileTable.begin();
}

bool ZipArchive::hasNext() {
	return (iter != fileTable.end());
}

ZipFile ZipArchive::next() {
	map<string, ZipFile>::iterator iter0 = iter;
	iter++;
	return (ZipFile)iter0->second;
}
