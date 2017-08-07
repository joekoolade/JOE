package org.jikesrvm.tools.bootImageWriter;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.vmmagic.unboxed.Address;

public class Multiboot {
	public final static int MAGIC = 0x1BADB002;
	int checksum;
	Address headerAddr;
	Address loadAddr;
	Address loadAddrEnd;
	Address bssAddrEnd;
	Address entryAddr;
	int width;
	int type;
	int depth;
	public final static int BOOT_MEM_FLAG 		= 0x0001;
	public final static int BOOT_BOOTDEV_FLAG 	= 0x0002;
	public static final int BOOT_CMDLINE_FLAG 	= 0x0004;
	public static final int BOOT_BOOTMODS_FLAG 	= 0x0008;
	public final static int BOOT_AOUTSYMS_FLAG 	= 0x0010;
	public final static int BOOT_ELFSYMS_FLAG 	= 0x0020;
	public final static int BOOT_MEMMAP_FLAG 	= 0x0040;
	
	// flags for the header file
	public static final int HEADER_ALIGN4K_FLAG 	= 0x00001;
	public final static int HEADER_MEM_FLAG 		= 0x00002;
	public final static int HEADER_VIDEO_FLAG 		= 0x00004;
	public final static int HEADER_LOADADDR_FLAG 	= 0x10000;
	public static final int HEADER_SIZE = 48;
	private static final int MAGIC_OFFSET = 0;
	private static final int FLAGS_OFFSET = 1;
	private static final int CHECKSUM_OFFSET = 2;
	private static final int HEADERADDR_OFFSET = 3;
	private static final int LOADADDR_OFFSET = 4;
	private static final int LOADADDREND_OFFSET = 5;
	private static final int BSSENDADDR_OFFSET = 6;
	private static final int ENTRYADDR_OFFSET = 7;
	
	
	int[] headerCode;
	
	public void setHeaderAddress(int addr) {
		headerAddr = Address.fromIntZeroExtend(addr); 
	}
	
	public void setLoadAddress(int addr) {
		loadAddr = Address.fromIntZeroExtend(addr);
	}
	
	public void setLoadEndAddress(int addr) {
		loadAddrEnd = Address.fromIntZeroExtend(addr);
	}
	
	public void setBssAddrEnd(int addr) {
		bssAddrEnd = Address.fromIntZeroExtend(addr);
	}
	
	public void setEntryAddress(int addr) {
		entryAddr = Address.fromIntZeroExtend(addr);
	}
	
	public void writeMultibootHeader() {
		headerCode = new int[12];	// array is initialized to 0
		
		headerCode[MAGIC_OFFSET] = MAGIC;
		headerCode[FLAGS_OFFSET] = HEADER_MEM_FLAG;
		headerCode[CHECKSUM_OFFSET] = -(headerCode[0]+headerCode[1]);
		headerCode[HEADERADDR_OFFSET] = headerAddr.toInt();
		headerCode[LOADADDR_OFFSET] = loadAddr.toInt();
		headerCode[LOADADDREND_OFFSET] = loadAddrEnd.toInt();
		headerCode[BSSENDADDR_OFFSET] = bssAddrEnd.toInt();
		headerCode[ENTRYADDR_OFFSET] = entryAddr.toInt();
	}
	
	public int[] getIntArray() {
		return headerCode;
	}
	
	public byte[] getByteArray() {
		ByteBuffer bytes = ByteBuffer.allocate(HEADER_SIZE);
		IntBuffer ib = bytes.asIntBuffer();
		ib.put(headerCode);
		return bytes.array();
	}
	
	public void writeMultiInfoHeader() {
		
	}
}
