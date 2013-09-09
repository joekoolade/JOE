package org.jikesrvm.tools.bootImageWriter;

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
		
	}
	
	public void writeMultiInfoHeader() {
		
	}
}
