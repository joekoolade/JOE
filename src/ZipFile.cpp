/*
 * ZipFile.cpp
 *
 *  Created on: Jun 25, 2010
 *      Author: joe
 */

#include <iostream>
#include <fstream>

#include <stdlib.h>
//extern "C" {
#include <zlib.h>
// }

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
	fileName[fileHeader.fileNameLen] = '\0';
	// initialize name
	name = string(fileName);
	size = fileHeader.uncompressedSize;
	compression = fileHeader.compression;
	// read in the compressed data
	if (fileHeader.compressedSize > 0) {
		data = (char *)malloc(fileHeader.compressedSize);
		if (data == NULL) {
			cerr << "ZipFile: failed to malloc: " << fileHeader.compressedSize << endl;
			exit(-1);
		}
		stream.read(data, fileHeader.compressedSize);
	}
	cout << name << ' ' << fileHeader.compression << ' ' << size << ' ' << fileHeader.compressedSize << endl;
}

uint8_t *ZipFile::inflate() {
	uint8_t *infData;
	z_stream strm;
	uLongf destBufSize;

	if(size == 0)
		return NULL;

	infData = (uint8_t *)malloc(size);
	destBufSize = size;
	int ret = uncompress((Bytef *)infData, (uLongf *)&destBufSize, (const Bytef *)data, compressedSize);

	if(ret != Z_OK) {
		cout << "inflate: " << zError(ret) << endl;
		free(infData);
		return (uint8_t *)-1;
	}
	cout << "Inflated --> " << name << endl;
	return infData;
}
