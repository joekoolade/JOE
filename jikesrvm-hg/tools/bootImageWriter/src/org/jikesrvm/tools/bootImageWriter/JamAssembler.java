package org.jikesrvm.tools.bootImageWriter;

import org.jikesrvm.compilers.baseline.ia32.BaselineCompilerImpl;
import org.jikesrvm.compilers.common.assembler.ia32.Assembler;
import org.jikesrvm.VM;

public class JamAssembler extends Assembler {

	public JamAssembler(int bytecodeSize) {
		super(bytecodeSize);
	}

	public JamAssembler(int bytecodeSize, boolean shouldPrint) {
		super(bytecodeSize, shouldPrint);
	}

	public JamAssembler(int bytecodeSize, boolean shouldPrint,
			BaselineCompilerImpl comp) {
		super(bytecodeSize, shouldPrint, comp);
	}

	/**
	 * Align location counter to a 2**alignment boundary. The padding bytes will be
	 * zero filled
	 * @param alignment amount of alignment. 2 = 4 byte boundary, 3 = 8 byte boundary
	 */
	public void align(int alignment) {
		if(alignment > 32)
			VM._assert(false, "Bad alignment: "+alignment);

		int miStart = mi;
		mi = mi+(1<<alignment) & (0xffffffff<<alignment);
		for(int mi0=miStart; mi0<mi; mi0++) {
			setMachineCodes(mi0, (byte) 0);
		}
	}
	
	/**
	 * A directive that emits size bytes with a value of zero.
	 * @param size Number of bytes to emit
	 */
	public void space(int size) {
		int miStart = mi;
		mi += size;
		for(int mi0=miStart; mi0<mi; mi0++) {
			setMachineCodes(mi0, (byte) 0);
		}
	}
	
	public void emitOUTB(int imm) {
		
	}
	
	public void emitOUTW(int imm) {
		
	}
	
	public void emitOUTL(int imm) {
		
	}
	
	public void emitOUTB() {
		
	}
	
	public void emitOUTW() {
		
	}
	
	public void emitOUTL() {
		
	}
	
	
}
