package org.jam.mm;

import org.jikesrvm.VM;
import org.jikesrvm.runtime.BootRecord;
import org.jikesrvm.runtime.Memory;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Extent;
import org.vmmagic.unboxed.Offset;
import org.vmmagic.unboxed.Word;

@Uninterruptible
public class MemoryManager {
	static Address freeMemStart;
	static Address freeMemEnd;
	static Address cursor;
	
	public static void boot(BootRecord bootRecord) {
		Word start = bootRecord.bootImageRMapEnd.toWord().plus(Offset.fromIntZeroExtend(0x1000)).and(Word.fromIntZeroExtend(~0xFFF));
		freeMemStart = start.toAddress();
		freeMemEnd = freeMemStart.plus(bootRecord.maximumHeapSize);
		cursor = freeMemStart;
		VM.sysWrite("MemoryManager: start=", freeMemStart);
		VM.sysWrite(" end=", freeMemEnd);
		VM.sysWrite(" maxHeapSize=", bootRecord.maximumHeapSize.toInt());
		VM.sysWriteln();
	}
	
	public static Address alloc(Address address, Extent size) {
		Address cursor = address;
		if(cursor.plus(size).GT(freeMemEnd)) {
			VM.sysWriteln("PANIC: ", cursor.toInt(), " ", size.toInt());
			VM.sysFail("Out of Memory");
		}
		Memory.zero(false, address,  size);
		return cursor;
	}
}
