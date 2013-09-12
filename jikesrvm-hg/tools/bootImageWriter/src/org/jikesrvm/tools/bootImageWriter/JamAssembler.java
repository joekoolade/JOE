package org.jikesrvm.tools.bootImageWriter;

import org.jikesrvm.compilers.baseline.ia32.BaselineCompilerImpl;
import org.jikesrvm.compilers.common.assembler.ia32.Assembler;
import org.jikesrvm.VM;
import org.vmmagic.unboxed.Address;

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
	
	/**
	 * Generate Output to Port instruction for a byte. The value to output must be in the AL register
	 * 
	 * <PRE>
	 * [i/o port] = AL
	 * </PRE>
	 * 
	 * @param port immediate value containing the I/O port address
	 */
	public void emitOUTB(int port) {
		int miStart = mi;
		setMachineCodes(mi++, (byte)0xe6);
		setMachineCodes(mi++, (byte)port);
		if(lister != null) lister.I(miStart, "OUTB", port);
	}
	
	/**
	 * Generate Output to Port instruction for a word. The value to output must be in the AX register
	 * 
	 * <PRE>
	 * [i/o port] = AX
	 * </PRE>
	 * 
	 * @param port immediate value containing the I/O port address
	 */
	public void emitOUTW(int port) {
		int miStart = mi;
		setMachineCodes(mi++, (byte)0xe7);
		setMachineCodes(mi++, (byte)port);
		if(lister != null) lister.I(miStart, "OUTW", port);
	}
	
	/**
	 * Generate Output to Port instruction for a long. The value to output must be in the EAX register
	 * 
	 * <PRE>
	 * [i/o port] = EAX
	 * </PRE>
	 * 
	 * @param port immediate value containing the I/O port address
	 */
	public void emitOUTL(int port) {
		int miStart = mi;
		setMachineCodes(mi++, (byte)0x66);
		setMachineCodes(mi++, (byte)0xe7);
		setMachineCodes(mi++, (byte)port);
		if(lister != null) lister.I(miStart, "OUTL", port);
	}
	
	/**
	 * Generate Output to Port instruction for a byte. The value to output must be in the AL register
	 * 
	 * <PRE>
	 * [DX] = AL
	 * </PRE>
	 * 
	 */
	public void emitOUTB() {
		int miStart = mi;
		setMachineCodes(mi++, (byte)0xee);
		if(lister != null) lister.RNR(miStart, "OUTB", GPR.EDX, GPR.EAX);
	}
	
	/**
	 * Generate Output to Port instruction for a word. The value to output must be in the AX register
	 * 
	 * <PRE>
	 * [DX] = AX
	 * </PRE>
	 * 
	 */
	public void emitOUTW() {
		int miStart = mi;
		setMachineCodes(mi++, (byte)0xef);
		if(lister != null) lister.RNR(miStart, "OUTW", GPR.EDX, GPR.EAX);
	}
	
	/**
	 * Generate Output to Port instruction for a word. The value to output must be in the AX register
	 * 
	 * <PRE>
	 * [DX] = EAX
	 * </PRE>
	 * 
	 */
	public void emitOUTL() {
		int miStart = mi;
		// operand size prefix
		setMachineCodes(mi++, (byte)0x66);
		setMachineCodes(mi++, (byte)0xef);
		if(lister != null) lister.RNR(miStart, "OUTL", GPR.EDX, GPR.EAX);
	}
	
	/**
	 * Generate an I/O port byte read. Value is stored in the AL register.
	 * 
	 * <PRE>
	 * AL = [i/o port]
	 * </PRE>
	 * 
	 * @param port immediate value specifying the I/O port
	 * 
	 */
	public void emitINB(int port) {
		int miStart = mi;
		setMachineCodes(mi++, (byte)0xe4);
		setMachineCodes(mi++, (byte)port);
		if(lister != null) lister.I(miStart, "INB", port);
	}
	
	/**
	 * Generate an I/O port word read. Value is stored in the AX register.
	 * 
	 * <PRE>
	 * AX = [i/o port]
	 * </PRE>
	 * 
	 * @param port immediate value specifying the I/O port
	 * 
	 */
	public void emitINW(int port) {
		int miStart = mi;
		setMachineCodes(mi++, (byte)0xe5);
		setMachineCodes(mi++, (byte)port);
		if(lister != null) lister.I(miStart, "INW", port);
	}

	/**
	 * Generate an I/O port long read. Value is stored in the EAX register.
	 * 
	 * <PRE>
	 * EAX = [i/o port]
	 * </PRE>
	 * 
	 * @param port immediate value specifying the I/O port
	 * 
	 */
	public void emitINL(int port) {
		int miStart = mi;
		// operand size prefix
		setMachineCodes(mi++, (byte)0x66);
		setMachineCodes(mi++, (byte)0xe5);
		setMachineCodes(mi++, (byte)port);
		if(lister != null) lister.I(miStart, "INL", port);
	}

	/**
	 * Generate an I/O port byte read. Value is stored in the AL register.
	 * 
	 * <PRE>
	 * AL = [DX]
	 * </PRE>
	 * 
	 * @param port immediate value specifying the I/O port
	 * 
	 */
	public void emitINB() {
		int miStart = mi;
		setMachineCodes(mi++, (byte)0xec);
		if(lister != null) lister.RRN(miStart, "INB", GPR.EAX, GPR.EDX);
	}
	/**
	 * Generate an I/O port word read. Value is stored in the AX register.
	 * 
	 * <PRE>
	 * AX = [DX]
	 * </PRE>
	 * 
	 * @param port immediate value specifying the I/O port
	 * 
	 */
	public void emitINW() {
		int miStart = mi;
		setMachineCodes(mi++, (byte)0xed);
		if(lister != null) lister.RRN(miStart, "INW", GPR.EAX, GPR.EDX);
	}

	/**
	 * Generate an I/O port long read. Value is stored in the EAX register.
	 * 
	 * <PRE>
	 * EAX = [i/o port]
	 * </PRE>
	 * 
	 * @param port immediate value specifying the I/O port
	 * 
	 */
	public void emitINL() {
		int miStart = mi;
		// operand size prefix
		setMachineCodes(mi++, (byte)0x66);
		setMachineCodes(mi++, (byte)0xed);
		if(lister != null) lister.RRN(miStart, "INL", GPR.EAX, GPR.EDX);
	}

	public void emitLGDT(Address tableAddress) {
		int miStart = mi;
		// operand size prefix
		setMachineCodes(mi++, (byte)0x66);
		setMachineCodes(mi++, (byte)0x0f);
		setMachineCodes(mi++, (byte)0x01);
		setMachineCodes(mi++, (byte)0x0f);
	}
}
