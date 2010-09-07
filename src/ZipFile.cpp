/*
 * ZipFile.cpp
 *
 *  Created on: Jun 25, 2010
 *      Author: joe
 */

#include <iostream>
#include <fstream>

#include <stdlib.h>
#include <zlib.h>
#include <string.h>

#include "ZipFile.h"

using namespace std;

#define WBITS	15		// window bits
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
	compressedSize = fileHeader.compressedSize;
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

#define CHUNK	(256*1024)

uint8_t *ZipFile::inflate() {
	uint8_t *infData, *outBuffer;
	z_stream strm;
	int bytesLeft;

	if(size == 0)
		return NULL;

	infData = (uint8_t *)malloc(size);
	outBuffer = (uint8_t *)malloc(CHUNK);

	// data no deflated, so just copy over
	if(compression == CM_STORED) {
		memcpy(infData, data, size);
		return infData;
	}

	strm.zalloc = Z_NULL;
	strm.zfree = Z_NULL;
	strm.avail_in = compressedSize;
	strm.next_in = (Bytef *)data;
//	printf("data: %02x %02x %02x\n", *data, *(data+1), *(data+2));
	int ret = inflateInit2(&strm, -WBITS);

	if(ret != Z_OK) {
		cout << "inflate: " << zError(ret) << endl;
		free(infData);
		return (uint8_t *)-1;
	}

	strm.avail_in = compressedSize;
	strm.next_in = (Bytef *)data;
	int processed_out = 0;
	while(ret != Z_STREAM_END) {
		strm.next_out = (Bytef *)outBuffer;
		strm.avail_out = CHUNK;
		cout << "out: " << strm.avail_out << " " << hex << (int)strm.next_out << " processed out: " << dec << processed_out
				<< " in: " << hex << (int)strm.next_in << " " << dec << strm.avail_in << endl;
		ret = ::inflate(&strm, Z_SYNC_FLUSH);
		switch(ret) {
		case Z_NEED_DICT:
		case Z_MEM_ERROR:
		case Z_DATA_ERROR:
			cout << "inflate loop: " << zError(ret) << endl;
			::inflateEnd(&strm);
			return (uint8_t *)-1;
		case Z_STREAM_END:
			break;
		default:
			cout << "return: " << zError(ret) << endl;
		}
		int processed_in = compressedSize - strm.avail_in;
		processed_out = CHUNK - strm.avail_out;
		strm.next_in += processed_in;
	}
	::inflateEnd(&strm);
	memcpy(infData, outBuffer, size);
	cout << "Inflated --> " << name << endl;
	return infData;
}
