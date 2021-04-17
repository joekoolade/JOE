/**
 * 
 */
package org.jam.cpu.intel;

/**
 * @author jkulig
 *
 */
public interface SegmentDescriptorTypes {
	int TSS16 				= 0x100;
	int LDT   				= 0x200;
	int CALLGATE16 			= 0x400;
	int TASKGATE			= 0x500;
	int INTERRUPTGATE16 	= 0x600;
	int TRAPGATE16			= 0x700;
	int TSS					= 0x900;
	int CALLGATE			= 0xC00;
	int INTERRUPTGATE		= 0xE00;
	int TRAPGATE			= 0xF00;
	
	int SEGMENT_PRESENT		= 0x8000;
}
