/*
 * Created on Nov 7, 2003
 *
 * Copyright (C) Joe Kulig, 2003
 * All rights reserved.
 * 
 */
package baremetal.platform;

/**
 * @author Joe Kulig
 *
 * Contains multiboot info passed from the grub loader
 */
public class Multiboot {
	static int flags;
	static int memLower;
	static int memUpper;
	static int bootDevice;
	static int cmdLine;
	static int modsCount;
	static int modsAddress;
	// elf header section
	
	static int mmapLength;
	static int mmapAddress;
}
