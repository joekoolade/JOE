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

ZipArchive::ZipArchive(char *zipfile) {
	archiveName = string(zipfile);
	ifstream in(zipfile, ios::in|ios::binary);
	if(!in){
		cerr << "Could not open zipfile" << zipfile << endl;
	}
	size = in.tellg();
	// Start reading in files
	ZipLocalFileHeader zipHeader;
	while(!in.read((char *)&zipHeader, sizeof(ZipLocalFileHeader)).eof()) {
		if(zipHeader.isZip()) {
			ZipFile file(in, zipHeader);
		} else if(zipHeader.isCD()) {
			CentralDirectory cd;
			// TODO need the correct seek call
			streamoff backup = sizeof(ZipLocalFileHeader);
			in.seekg(-backup, ios::cur);
			in.read((char *)&cd, sizeof(CentralDirectory));
			cout << "CDS lens: " << cd.fileNameLen << ' ' << cd.extraLen << ' '	<< cd.commentLen << endl;
			if (cd.fileNameLen > 0) {
				char name[1024];
				in.read(name, cd.fileNameLen);
				name[cd.fileNameLen] = '\0';
				cout << "CDS name: " << name << endl;
			}
			if(cd.extraLen > 0) {
				char extra[1024];
				in.read(extra, cd.extraLen);
				extra[cd.extraLen] = '\0';
				cout << "extra: " << extra << endl;
			}
			if(cd.commentLen > 0) {
				char comment[1024];
				in.read(comment, cd.commentLen);
				comment[cd.commentLen] = '\0';
				cout << "comment: " << comment << endl;
			}

			cout << "made by: " << cd.verMadeBy << "  version: " << cd.version;
			cout << "  flag: " << cd.flag << "  compression: " << cd.compression << endl;
			cout << " size: " << cd.uncompressedSize << " compressed to: " << cd.compressedSize << endl;
			cout << " disk#: " << cd.diskNumber << " offset: " << cd.offset << endl;

		} else {
			cout << "Could not find zip signature!";
		}

	}
}
