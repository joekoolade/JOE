/*
 * ZipArchive.cpp
 *
 *  Created on: Jun 24, 2010
 *      Author: joe
 */

#include <iostream>
#include <fstream>
#include <string>

ZipArchive::ZipArchive(string& zipfile) {
	archiveName = string(zipfile);
	ifstream stream(zipfile, ios::in|ios::binary);
	if(!in){
		throw new ZipFileStreamOpenFailure();
	}
	size = in.tellg();
	// Start reading in files
	ZipLocalFileHeader zipHeader;
	while(!stream.read(&zipHeader, sizeof(ZipLocalFileHeader)).eof()) {
		if(!zipHeader.isZip()) {
			cout << "Could not find zip signature!";
			exit(-1);
		}
		ZipFile file = new ZipFile(stream, zipHeader);

	}
}
