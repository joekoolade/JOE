/*
 * ZipFile.cpp
 *
 *  Created on: Jun 25, 2010
 *      Author: joe
 */

#include <iostream>
#include <fstream>

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
	size = uncompressesdSize;
	// read in the compressed data
	if (compressedSize > 0) {
		data = malloc(compressedSize);
		if (data == NULL) {
			cerr << "ZipFile: failed to malloc: " << compressedSize;
			exit(-1);
		}
		stream.read(data, compressedSize);
	}
	cout << name << ' ' << size << ' ' << compressedSize << endl;
}
