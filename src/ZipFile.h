/*
 * ZipFile.h
 *
 *  Created on: Jun 25, 2010
 *      Author: joe
 */

#ifndef ZIPFILE_H_
#define ZIPFILE_H_

// record signatures
#define LFH_SIG		0x504b0304
#define CDS_SIG		0x504b0102

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
	uint32 signature;
	uint16 version;
	uint16 flag;
	uint16 compression;
	uint16 modTime;
	uint16 modDate;
	uint32 crc;
	uint32 compressedSize;
	uint32 uncompressedSize;
	uint16 fileNameLen;
	uint16 extraFieldLen;

	inline bool isZip() {
		return signature == LFH_SIG;
	}
} __attribute__((packed));

struct DataDescriptor {
	uint32 crc;
	uint32 compressedSize;
	uint32 uncompressedSize;
} __attribute__((packed));


struct CentralDirectory {
	uint32 signature;
	uint16 verMadeBy;
	uint16 version;
	uint16 flag;
	uint16 compression;
	uint16 modTime;
	uint16 modDate;
	uint32 crc;
	uint32 compressedSize;
	uint32 uncompressedSize;
	uint16 fileNameLen;
	uint16 extraLen;
	uint16 commentLen;
	uint16 diskNumber;
	uint16 intFileAttr;
	uint32 extFileAttr;
	uint32 offset;
} __attribute__((packed));

class ZipFile {
public:
	string name;
	uint32 size;
	char *data;

	ZipFile(ifstream&, ZipLocalFileHeader&);
};
#endif /* ZIPFILE_H_ */
