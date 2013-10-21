package org.jam.driver.pc;

import org.jam.driver.Bus;
import org.jam.driver.Device;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Offset;

/*
 * todo: make a singleton?
 */
public class RomBios extends Device {
	Address dataArea;
	Offset videoModeOffset;
	Offset screenWidthOffset;
	
	public RomBios(Bus bus, String id) {
		super(bus, id);
		dataArea = Address.fromIntZeroExtend(0x400);
		videoModeOffset = Offset.zero().plus(0x49);
		screenWidthOffset = Offset.zero().plus(0x4a);
	}

	public byte getVideoMode() {
		return dataArea.loadByte(videoModeOffset);
	}
	
	public short getScreenWidth() {
		return dataArea.loadShort(screenWidthOffset);
	}
}
