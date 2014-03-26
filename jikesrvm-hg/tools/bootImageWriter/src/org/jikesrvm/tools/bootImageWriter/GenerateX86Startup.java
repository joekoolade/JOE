package org.jikesrvm.tools.bootImageWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.jikesrvm.ia32.CodeArray;
import org.jikesrvm.ia32.RegisterConstants.CR;
import org.jikesrvm.ia32.RegisterConstants.GPR;
import org.jikesrvm.ia32.StackframeLayoutConstants;
import org.jikesrvm.tools.bootImageWriter.JamAssembler.SEG;
import org.vmmagic.unboxed.Address;

public class GenerateX86Startup {
	private JamAssembler asm = new JamAssembler(1024);
	private static final int CODE_SEGMENT = 1<<3;
	private static final int DATA_SEGMENT = 2<<3;
	
	/**
	 * 
	 * @param stack top of stack
	 * @param vmEntry pointer to VM.boot()
	 * @param jtoc Jikes tables of classes
	 * @param tid  thread id
	 */
	public GenerateX86Startup(Address stack, Address vmEntry, Address jtoc, Address tid) {
		int multibootEntry=1;
		asm.emitJMP_Label(multibootEntry);
		asm.align(4);
		// Insert multiboot header
		Multiboot mbh = new Multiboot();
		int codeIndex = asm.getMachineCodeIndex();
		mbh.setHeaderAddress(0x100000+codeIndex);
		mbh.setEntryAddress(0x100000);
		mbh.setLoadAddress(0x100000);
		mbh.setLoadEndAddress(0x171c000);
		mbh.setBssAddrEnd(0);
		mbh.writeMultibootHeader();
		asm.space(Multiboot.HEADER_SIZE);
		int[] header = mbh.getIntArray();
		for (int v : header) {
			System.out.println("mbh: "+Integer.toHexString(codeIndex)+" "+Integer.toHexString(v));
			codeIndex = asm.emitImm32(v, codeIndex);
		}
		/*
		 * Reserve space for GDT and IDT tables
		 */
		asm.align(0x40);
		Address gdtTablePtr = Address.fromIntZeroExtend(0x100050);
		Address gdtDesc = Address.fromIntZeroExtend(0x100040);
		asm.space(0xc0);
		// GDT table pointer; limit is 3*N-1 bytes
		asm.emitImm16((3*8)-1, 0x40);
		asm.emitImm32(gdtTablePtr.toInt(), 0x42);
		// descriptor 0 is null
		// decscriptor 1 is the code segment
		asm.emitImm32(0x5000, 0x58);
		asm.emitImm32(0xc09a00, 0x5c);
		// descriptor 2 is the data segment
		asm.emitImm32(0x6000, 0x60);
		asm.emitImm32(0xc09200, 0x64);
		// IDT table pointer
		// should be at 0x100 for the multibootEntry
		asm.resolveForwardReferences(multibootEntry);
		// multiboot entry starts here
		// set the stack pointer
		// asm.emitMOV_Reg_Imm(GPR.ESP, stack.toInt());
		// reset coprocessor
		asm.emitMOV_Reg_Imm(GPR.EAX, 0);
		asm.emitOUTB(0xf0);
		// io delay
		asm.emitOUTB(0x80);
		asm.emitOUTB(0xf1);
		// io delay
		asm.emitOUTB(0x80);
		// mask all interrupts on secondary PIC
		asm.emitMOV_Reg_Imm(GPR.EAX, 0xff);
		asm.emitOUTB(0xa1);
		// io delay
		asm.emitOUTB(0x80);
		// mask all interrupts but cascad on primary PIC
		asm.emitMOV_Reg_Imm(GPR.EAX, 0xfb);
		asm.emitOUTB(0xf1);
		// io delay
		asm.emitOUTB(0x80);
		
		// setup gdt
		asm.emitLGDT(gdtDesc);
		// asm.emitLIDT(idtTablePtr);
		// Load the data segment registers
		asm.emitMOV_Reg_Imm(GPR.EAX, DATA_SEGMENT);
		asm.emitMOVSEG(SEG.DS, GPR.EAX);
		asm.emitMOVSEG(SEG.ES, GPR.EAX);
		asm.emitMOVSEG(SEG.FS, GPR.EAX);
		asm.emitMOVSEG(SEG.GS, GPR.EAX);
		asm.emitMOVSEG(SEG.SS, GPR.EAX);

		// enable protected mode; not needed for qemu -kernel option
		// Set cr0.MP
		asm.emitMOV_Reg_Imm(GPR.EAX, 0x3);
		asm.emitMOVCR(GPR.EAX, CR.CR0);
		// Set cr4.OSFXSR
		asm.emitMOV_Reg_Imm(GPR.EAX, 0x200);
		asm.emitMOVCR(GPR.EAX, CR.CR4);
		
		// setup THREAD ID register
		asm.emitLEA_Reg_Abs(GPR.ESI, tid);
		// setup top of stack pointer
		asm.emitLEA_Reg_Abs(GPR.ESP, stack);
		// setup the thread register's frame pointer
		asm.emitMOV_Reg_Reg(GPR.EAX, GPR.ESP);
		asm.emitSUB_Reg_Imm_Byte(GPR.EAX, 8);
		asm.emitMOV_RegInd_Reg(GPR.ESI, GPR.EAX);
		// setup the return address sentinel
		asm.emitPUSH_Imm(0xdeadbabe);
		// setup the frame pointer sentinel
		asm.emitPUSH_Imm(StackframeLayoutConstants.STACKFRAME_SENTINEL_FP.toInt());
		// setup invisible method id
		asm.emitPUSH_Imm(StackframeLayoutConstants.INVISIBLE_METHOD_ID);
		// 
		// 
		// create the primordial object
		
		// setup FRAME POINTER
		// call VM.boot(); we are never coming back
		asm.emitFARCALL(vmEntry, 0x8);
		// asm.emitCALL_Imm(vmEntry.minus(0x100000).toInt());
	}
	
	public void writeImage(String filename) {
		// Write startup image out to a file
		CodeArray codeArray = asm.getMachineCodes();
		byte[] code = (byte[]) codeArray.getBacking();
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(filename);
			out.write(code);
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
