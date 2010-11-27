/*
 * ZipFile.h
 *
 *  Created on: Jun 25, 2010
 *      Author: joe
 */

#ifndef ZIPFILE_H_
#define ZIPFILE_H_

#include <string>
#include <stdint.h>

using namespace std;

// record signatures
#define LFH_SIG		0x04034b50
#define CDS_SIG		0x02014b50

// flag bits
#define	F_ENCRYPTED				0x0001
#define F_IMPLODE_8K			0x0002
#define F_IMPLODE_3SFT			0x0004
#define F_DEFLATE_MAX			0x0002
#define F_DEFLATE_FAST  		0x0004
#define F_DEFLATE_SUPER_FAST	0x0006
#define F_LZMA_EOS_MARKER		0x0002
#define F_DATA_DESC				0x0008
#define F_CMP_PATCHED_DATA		0x0020
#define F_STRONG_ENC			0x0040
#define F_EFS					0x0800
#define F_CD_ENC				0x2000

#define CM_STORED			0
#define CM_SHRUNK			1
#define CM_CF1				2
#define CM_CF2				3
#define CM_CF3				4
#define CM_CF4				5
#define CM_IMPLODED			6
#define CM_DEFLATED			8
#define CM_DEFLATED64		9
#define CM_PKWARE_LIB		10
#define CM_BZIP2			12
#define CM_LZMA				14
#define CM_IBM_TERSE		18
#define CM_LZ77				19
#define CM_WAVPACK			97
#define CM_PPMD				98

struct ZipLocalFileHeader {
	uint32_t signature;
	uint16_t version;
	uint16_t flag;
	uint16_t compression;
	uint16_t modTime;
	uint16_t modDate;
	uint32_t crc;
	uint32_t compressedSize;
	uint32_t uncompressedSize;
	uint16_t fileNameLen;
	uint16_t extraFieldLen;

	inline bool isZip() {
		return signature == LFH_SIG;
	}
	inline bool isCD() {
		return signature == CDS_SIG;
	}

} __attribute__((packed));

struct DataDescriptor {
	uint32_t crc;
	uint32_t compressedSize;
	uint32_t uncompressedSize;
} __attribute__((packed));


struct CentralDirectory {
	uint32_t signature;
	uint16_t verMadeBy;
	uint16_t version;
	uint16_t flag;
	uint16_t compression;
	uint16_t modTime;
	uint16_t modDate;
	uint32_t crc;
	uint32_t compressedSize;
	uint32_t uncompressedSize;
	uint16_t fileNameLen;
	uint16_t extraLen;
	uint16_t commentLen;
	uint16_t diskNumber;
	uint16_t intFileAttr;
	uint32_t extFileAttr;
	uint32_t offset;

} __attribute__((packed));

class ZipFile {
private:
	int debug;
public:
	string name;
	uint32_t size;
	// compressed data
	char *cdata;
	// inflated data
	uint8_t *data;
	uint16_t compression;
	uint16_t modTime;
	uint32_t crc;
	uint32_t compressedSize;

	ZipFile(ifstream&, ZipLocalFileHeader&);
	void inflate();
};
#endif /* ZIPFILE_H_ */
