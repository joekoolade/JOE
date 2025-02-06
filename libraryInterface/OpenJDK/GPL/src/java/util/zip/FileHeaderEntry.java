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
	short localNameLength;
	short extraFieldLength;
	short localExtraFieldLength;
	short commentLength;
	short disk;
	short intAttr;
	int extAttr;
	int offset;
	String fileName;
	byte[] extraField;
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
	    return extraField;
	}
	
	int commentOffset()
	{
		return extraOffset() + extraFieldLength;
	}
	
    int dataOffset()
    {
        return LOCHDR + localExtraFieldLength + nameLength;
    }

    int data()
	{
	    return offset + LOCHDR + localExtraFieldLength + nameLength;
	}

    public void setLocalExtraFieldSize(byte[] buf)
    {
        localExtraFieldLength = (short)(buf[offset + LOCEXT] | buf[offset + LOCEXT + 1] << 8);
    }
}
