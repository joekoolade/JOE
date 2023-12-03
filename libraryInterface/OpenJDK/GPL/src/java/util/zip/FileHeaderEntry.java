package java.util.zip;

class FileHeaderEntry implements ZipConstants {
	short madeVersion;
	short zipVersion;
	short flags;
	short compressionMethod;
	short time;
	short date;
	int crc;
	int compressedSize;
	int uncompressedSize;
	short nameLength;
	short extraFieldLength;
	short commentLength;
	short disk;
	short intAttr;
	int extAttr;
	int offset;
	String fileName;
	String extraField;
	String comment;
	
	int entrySize()
	{
		return CENHDR + nameLength + extraFieldLength + commentLength;
	}
	
	int extraOffset()
	{
		return CENHDR+nameLength;
	}
	
	byte[] getExtra()
	{
	    if(extraField == null) return null;
	    return extraField.getBytes();
	}
	
	int commentOffset()
	{
		return extraOffset() + extraFieldLength;
	}
	
	int dataOffset()
	{
	    return offset + LOCHDR + extraFieldLength + nameLength;
	}
}
