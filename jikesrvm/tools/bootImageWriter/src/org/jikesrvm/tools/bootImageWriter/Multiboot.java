package org.jikesrvm.tools.bootImageWriter;

public class Multiboot {
	public final static int MAGIC = 0x1BADB002;
	int checksum;
	int header_addr;
	int load_addr;
	int load_addr_end;
	int bss_addr_end;
	int entry_addr;
	int width;
	int type;
	int depth;
	public final static int MEM_FLAG 		= 0x0001;
	public final static int BOOTDEV_FLAG 	= 0x0002;
	public static final int CMDLINE_FLAG 	= 0x0004;
	public static final int BOOTMODS_FLAG 	= 0x0008;
	public final static int AOUTSYMS_FLAG 	= 0x0010;
	public final static int ELFSYMS_FLAG 	= 0x0020;
	public final static int MEMMAP_FLAG 	= 0x0040;
}
