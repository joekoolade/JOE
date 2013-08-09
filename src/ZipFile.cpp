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

#define DEBUG_INIT		0x00000001
#define DEBUG_INFLATE 	0x00000002

#define TRACE_INIT	if(debug & DEBUG_INIT)
#define TRACE_INFLATE if(debug & DEBUG_INFLATE)

#define WBITS	15		// window bits

ZipFile::ZipFile() {
	debug = 0;
}

/**
 * @param stream reference to input file stream. Should be pointing to the file name field
 * @param fileHeader current zip file being processed
 *
 */
ZipFile::ZipFile(ifstream& stream, ZipLocalFileHeader& fileHeader) {
	char fileName[255];

	debug = 0;
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
		cdata = (char *)malloc(fileHeader.compressedSize);
		if (cdata == NULL) {
			cerr << "ZipFile: failed to malloc: " << fileHeader.compressedSize << endl;
			exit(-1);
		}
		stream.read(cdata, fileHeader.compressedSize);
	}
	TRACE_INIT cout << name << ' ' << fileHeader.compression << ' ' << size << ' ' << fileHeader.compressedSize << endl;
}

#define CHUNK	(256*1024)

void ZipFile::inflate() {
	uint8_t *infData, *outBuffer;
	z_stream strm;
	int bytesLeft;

	if(size == 0) {
		TRACE_INFLATE cout << "file size is ZERO" << endl;
		return;
	}

	if(size > CHUNK) {
		printf("file is too big for CHUNK: %d\n", size);
		return;
	}
	infData = (uint8_t *)malloc(size);
	outBuffer = (uint8_t *)malloc(CHUNK);

	// data no deflated, so just copy over
	if(compression == CM_STORED) {
		memcpy(infData, cdata, size);
		data = (uint8_t *)cdata;
		return;
	}

	strm.zalloc = Z_NULL;
	strm.zfree = Z_NULL;
	strm.avail_in = compressedSize;
	strm.next_in = (Bytef *)cdata;
	int ret = inflateInit2(&strm, -WBITS);

	if(ret != Z_OK) {
		cout << "inflate: " << zError(ret) << endl;
		free(infData);
		return;
	}

	strm.avail_in = compressedSize;
	strm.next_in = (Bytef *)cdata;
	int processed_out = 0;
	while(ret != Z_STREAM_END) {
		strm.next_out = (Bytef *)outBuffer;
		strm.avail_out = CHUNK;
		TRACE_INFLATE cout << "out: " << strm.avail_out << " " << hex << (unsigned long)strm.next_out << " processed out: " << dec << processed_out
				<< " in: " << hex << (unsigned long)strm.next_in << " " << dec << strm.avail_in << endl;
		ret = ::inflate(&strm, Z_SYNC_FLUSH);
		switch(ret) {
		case Z_NEED_DICT:
		case Z_MEM_ERROR:
		case Z_DATA_ERROR:
			cout << "inflate loop: " << zError(ret) << endl;
			::inflateEnd(&strm);
			return;
		case Z_STREAM_END:
			break;
		default:
			TRACE_INFLATE cout << "return: " << zError(ret) << endl;
		}
		int processed_in = compressedSize - strm.avail_in;
		processed_out = CHUNK - strm.avail_out;
		strm.next_in += processed_in;
	}
	::inflateEnd(&strm);
	memcpy(infData, outBuffer, size);
	free(outBuffer);
	TRACE_INFLATE cout << "Inflated --> " << name << endl;
	data = infData;
}

uint8_t *ZipFile::getData() {
	return data;
}
