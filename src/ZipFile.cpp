/*
 * ZipFile.cpp
 *
 *  Created on: Jun 25, 2010
 *      Author: joe
 */

#include <iostream>
#include <fstream>

#include <stdlib.h>

#include "ZipFile.h"

using namespace std;

/**
 * @param stream reference to input file stream. Should be pointing to the file name field
 * @param fileHeader current zip file being processed
 *
 */
ZipFile::ZipFile(ifstream& stream, ZipLocalFileHeader& fileHeader) {
	char fileName[255];

	// read in file name
	stream.read(fileName, fileHeader.fileNameLen);
	// initialize name
	name = string(fileName);
	size = fileHeader.uncompressedSize;
	// read in the compressed data
	if (fileHeader.compressedSize > 0) {
		data = (char *)malloc(fileHeader.compressedSize);
		if (data == NULL) {
			cerr << "ZipFile: failed to malloc: " << fileHeader.compressedSize;
			exit(-1);
		}
		stream.read(data, fileHeader.compressedSize);
	}
	cout << name << ' ' << size << ' ' << fileHeader.compressedSize << endl;
}
