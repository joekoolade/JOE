package org.jikesrvm.tools.bootImageWriter;

import org.jikesrvm.compilers.baseline.ia32.BaselineCompilerImpl;
import org.jikesrvm.compilers.common.assembler.ia32.Assembler;
import org.jikesrvm.VM;
import org.vmmagic.unboxed.Address;

public class JamAssembler extends Assembler {

	public static enum SEG {
		ES, CS, SS, DS, FS, GS;
	}
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
	public void align(int mask) {
		/*
		 * Fix the aligment. No shift, mask instead
		 */
		int miStart = mi;
		mi = (mi+(mask-1)) & ~(mask-1);
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
		// load address int ESI
		// emitMOV_Reg_Imm(ESI, tableAddress.toInt());
		// operand size prefix
		// setMachineCodes(mi++, (byte)0x66);
		setMachineCodes(mi++, (byte)0x0f);
		setMachineCodes(mi++, (byte)0x01);
		// emitRegIndirectRegOperands(GPR.getForOpcode(5), GPR.getForOpcode(2)); // opcode /2
		setMachineCodes(mi++, regIndirectRegModRM(GPR.getForOpcode(5), GPR.getForOpcode(2)));
		emitImm32(tableAddress);
		if(lister != null) lister.I(miStart, "LGDT", tableAddress.toInt());
	}

	/**
	 * XXX Hardcode the descriptor address. Only need to modify the table address within the descriptor
	 */
	public void emitLIDT(Address tableAddress) {
		int miStart = mi;
		// operand size prefix
		setMachineCodes(mi++, (byte)0x66);
		setMachineCodes(mi++, (byte)0x0f);
		setMachineCodes(mi++, (byte)0x01);
		emitRegIndirectRegOperands(GPR.getForOpcode(3), GPR.getForOpcode(0)); // opcode /3
		emitImm32(tableAddress);
		if(lister != null) lister.I(miStart, "LIDT", tableAddress.toInt());
	}
	
	/**
	 * Move srcReg to segment register
	 * 
	 * seg := srcReg
	 * 
	 * @param seg
	 * @param srcReg
	 */
	public void emitMOVSEG(SEG seg, GPR srcReg) {
		int miStart = mi;
		setMachineCodes(mi++, (byte)0x8e);
		setMachineCodes(mi++, (byte)(0xc0 | (seg.ordinal()<<3) | srcReg.ordinal()));
		// if(lister != null) lister.
	}
	
	/**
	 * Call to an absolute address
	 * 
	 * pc = absolute address
	 * 
	 * @param abs address of code to jump to 
	 */
	public void emitFARCALL(Address abs, int selector) {
		int miStart = mi;
//		setMachineCodes(mi++, (byte)0x66);
		setMachineCodes(mi++, (byte)0x9a);
		emitImm32(abs);
		emitImm16(selector);
		if(lister != null) lister.I(miStart, "CALL", abs.toInt());
	}
	
	/**
	 * Move control register to a general purpose register; only handles 32bit case
	 * 
	 * <PRE>
	 * dstReg := CR0-CR7
	 * </PRE>
	 * 
	 * @param srcReg
	 * @param dstReg
	 */
	public void emitMOVCR(CR srcReg, GPR dstReg) {
		int miStart = mi;
		setMachineCodes(mi++, (byte)0x0f);
		setMachineCodes(mi++, (byte)0x20);
		emitRegRegOperands(dstReg, srcReg);
		if(lister != null) {
			lister.RR(miStart, "MOV", dstReg, srcReg);
		}
	}
	/**
	 * Move control register to a general purpose register; only handles 32bit case
	 * 
	 * <PRE>
	 * CR0-CR7 := srcReg
	 * </PRE>
	 * 
	 * @param srcReg
	 * @param dstReg
	 */
	public void emitMOVCR(GPR srcReg, CR dstReg) {
		int miStart = mi;
		setMachineCodes(mi++, (byte)0x0f);
		setMachineCodes(mi++, (byte)0x22);
		emitRegRegOperands(srcReg, dstReg);
		if(lister != null) {
			lister.RR(miStart, "MOV", dstReg, srcReg);
		}
	}
	/**
	 * Provide access to Assembler.emitImm16() function.
	 */
	public int emitImm16(int imm, int index) {
		return super.emitImm16(imm, index);
	}
	
	public int emitImm32(int imm, int index) {
		return super.emitImm32(imm, index);
	}
}
