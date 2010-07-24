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
		if(!zipHeader.isZip()) {
			cout << "Could not find zip signature!";
			exit(-1);
		}
		ZipFile file(in, zipHeader);

	}
}
