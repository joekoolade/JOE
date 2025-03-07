package org.jikesrvm.tools.bootImageWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.jikesrvm.compilers.common.assembler.ia32.AssemblerConstants;
import org.jikesrvm.compilers.common.CodeArray;
import org.jikesrvm.ia32.RegisterConstants.CR;
import org.jikesrvm.ia32.RegisterConstants.GPR;
import org.jikesrvm.ia32.StackframeLayoutConstants;
import org.jikesrvm.runtime.BootRecord;
import org.jikesrvm.tools.bootImageWriter.JamAssembler.SEG;
import org.vmmagic.unboxed.Address;

public class GenerateIA32EStartup implements ProcessorStartup {
	private static final int X86_LOADADDR = 0x100000;
	private JamAssembler asm = new JamAssembler(1024);
	@SuppressWarnings("unused")
	private static final int CODE_SEGMENT = 1<<3;
	private static final int DATA_SEGMENT = 0;
	private static final int WORDSIZE = 8;
	/**
	 * 
	 * @param stack top of stack
	 * @param vmEntry pointer to VM.boot()
	 * @param jtoc Jikes tables of classes
	 * @param tid  thread id
	 */
	public GenerateIA32EStartup(BootRecord bootRecord) {
		int multibootEntry=1;
		asm.setOrigin(X86_LOADADDR);
		asm.emitJMP_Label(multibootEntry);
		asm.align(4);
		// Insert multiboot header
		Multiboot mbh = new Multiboot();
		int codeIndex = asm.getMachineCodeIndex();
		mbh.setHeaderAddress(X86_LOADADDR+codeIndex);
		mbh.setEntryAddress(X86_LOADADDR);
		mbh.setLoadAddress(X86_LOADADDR);
		mbh.setLoadEndAddress(0);
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
        // table containing system descriptors
        asm.resolveForwardReferences(multibootEntry);
		Address gdtTable = Address.fromIntZeroExtend(0x100810);
		// gdt register info
		Address gdtDesc = Address.fromIntZeroExtend(0x100800);
		Address csDesc = gdtTable.plus(8);
		Address dsDesc = gdtTable.plus(0x10);
		// gdt descriptor has limit of 3 entries; base address of gdtDesc
		// GDT table pointer; limit is 3*N-1 bytes
		asm.emitMOV_Abs_Imm_Word(gdtDesc, (8*3) - 1);
		asm.emitMOV_Abs_Imm(gdtDesc.plus(2), gdtTable.toInt());
		// Fill in the GDT table entries
		// descriptor 0 is null
		asm.emitMOV_Abs_Imm(gdtTable, 0);
		asm.emitMOV_Abs_Imm(gdtTable.plus(4), 0);
		/*
		 * descriptor 1 is the code segment
		 * 128MB segment, base 0, 32bit segment, DPL 0, code execute/read seg type
		 */
		asm.emitMOV_Abs_Imm(csDesc, 0xFFFF);
//		asm.emitMOV_Abs_Imm(csDesc.plus(4), 0x00AF9A00);
		asm.emitMOV_Abs_Imm(csDesc.plus(4), 0xCF9A00);
		/*
		 * descriptor 2 is the data segment
		 * 128MB segment, base 0, 32bit segment, DPL 0, data read/write seg type
		 */
        asm.emitMOV_Abs_Imm(dsDesc, 0xFFFF);
//        asm.emitMOV_Abs_Imm(dsDesc.plus(4), 0x00CF9200);
        asm.emitMOV_Abs_Imm(dsDesc.plus(4), 0xCF9200);
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
		asm.emitMOV_Reg_Imm(GPR.EAX, 0xff);
		asm.emitOUTB(0x21);
		// io delay
		asm.emitOUTB(0x80);
		/*
		 * Setup PML4 table; pointer to PD PTE Table
		 */
		int PAGE_PRESENT = 1;
		int PAGE_WRITE = 2;
		int PAGE_SIZE = 0x80;
		Address pml4Table = Address.fromIntZeroExtend(0x101000);
        Address pdpteTable = Address.fromIntZeroExtend(0x102000);
        Address pdeTable0 = Address.fromIntSignExtend(0x103000); // 0 - 0x3FFF_FFFF
        Address pdeTable3 = Address.fromIntSignExtend(0x104000); // 0xC000_0000 - 0xFFFF_FFFF
		asm.emitMOV_Abs_Imm(pml4Table, pdpteTable.toInt()|PAGE_WRITE|PAGE_PRESENT);
		/*
		 * Setup PDPTE table; pointer to PDE Table
		 */
		asm.emitMOV_Abs_Imm(pdpteTable, pdeTable0.toInt()|PAGE_WRITE|PAGE_PRESENT);
		/*
		 * Setup PDE table
		 * Fill all table entries to map 1GB
		 */
        asm.emitMOV_Reg_Imm(GPR.EAX, PAGE_SIZE|PAGE_WRITE|PAGE_PRESENT);
        asm.emitLEA_Reg_Abs(GPR.EBX, pdeTable0);
		int pdeTableLoop = asm.getMachineCodeIndex();
		// page address; 2MB chunks
		asm.emitMOV_RegInd_Reg(GPR.EBX, GPR.EAX);
		// advance to next pde entry
		asm.emitADD_Reg_Imm(GPR.EBX, WORDSIZE);
		// advance to next page address
		asm.emitADD_Reg_Imm(GPR.EAX, 0x200000);
		// stop when 1GB is reached
		asm.emitCMP_Reg_Imm(GPR.EAX, 0x40000000);
		asm.emitJCC_Cond_Imm(AssemblerConstants.LLT, pdeTableLoop);
		/*
		 * Map 0xF000_0000 to 0xFFFF_FFFF 
		 */
		asm.emitMOV_Abs_Imm(pdpteTable.plus(WORDSIZE*3), pdeTable3.toInt()|PAGE_WRITE|PAGE_PRESENT);;;
 		asm.emitLEA_Reg_Abs(GPR.EBX, pdeTable3.plus(0xC00)); // starting offset for 0xF000_0000
		asm.emitMOV_Reg_Imm(GPR.EAX, 0xF0000000|PAGE_SIZE|PAGE_WRITE|PAGE_PRESENT);
        pdeTableLoop = asm.getMachineCodeIndex();
        // page address; 2MB chunks
        asm.emitMOV_RegInd_Reg(GPR.EBX, GPR.EAX);
        // advance to next pde entry
        asm.emitADD_Reg_Imm(GPR.EBX, WORDSIZE);
        // advance to next page address
        asm.emitADD_Reg_Imm(GPR.EAX, 0x200000);
        // stop count rollover over and is positive
        // asm.emitCMP_Reg_Imm(GPR.EAX, 0x40000000);
        // Keep looping until EAX > 0
        asm.emitJCC_Cond_Imm(AssemblerConstants.S, pdeTableLoop);

        asm.emitLGDT(gdtDesc);
        // Set cr0.MP
        /*
         * Set long and real mode
         */
        asm.emitMOVCR(CR.CR0, GPR.EAX);
        asm.emitOR_Reg_Imm(GPR.EAX, 0x21);
        asm.emitMOVCR(GPR.EAX, CR.CR0);
        /*
         * Activate with a jump
         */
        asm.emitJMPFAR_label(2, CODE_SEGMENT);
        asm.resolveForwardReferences(2);
		/*
		 * Set CR4 with PAE and PGE bits
		 * and OSFXSR,OSXMMEXCPT
		 */
		asm.emitMOV_Reg_Imm(GPR.EAX, 0x6A0);
		asm.emitMOVCR(GPR.EAX, CR.CR4);
		/*
		 * Set CR3 with PML4 table
		 */
		asm.emitLEA_Reg_Abs(GPR.EAX, pml4Table);
		asm.emitMOVCR(GPR.EAX, CR.CR3);
		/*
		 * Read EFER MSR
		 */
		asm.emitMOV_Reg_Imm(GPR.ECX, 0xC0000080);
		asm.emitRDMSR();
		/*
		 * Set the LME
		 */
		asm.emitOR_Reg_Imm(GPR.EAX, 0x100);
		asm.emitWRMSR();
		
		// setup gdt
//		asm.emitLGDT(gdtDesc);
        // Set cr0.MP
        /*
         * Set long and real mode
         */
        asm.emitMOVCR(CR.CR0, GPR.EAX);
        asm.emitOR_Reg_Imm(GPR.EAX, 0x80000001);
        asm.emitMOVCR(GPR.EAX, CR.CR0);
        
        /*
         * Put code segment into 64 bit mode
         */
        asm.emitMOV_Abs_Imm(csDesc, 0xFFFF);
        asm.emitMOV_Abs_Imm(csDesc.plus(4), 0xA09A00);

        /*
         * Activate with a jump
         */
        asm.emitJMPFAR_label(3, CODE_SEGMENT);
        asm.resolveForwardReferences(3);

        // Load the data segment registers
		asm.emitMOV_Reg_Imm(GPR.EAX, DATA_SEGMENT);
		asm.emitMOVSEG(SEG.DS, GPR.EAX);
		asm.emitMOVSEG(SEG.ES, GPR.EAX);
		asm.emitMOVSEG(SEG.FS, GPR.EAX);
		asm.emitMOVSEG(SEG.GS, GPR.EAX);
		asm.emitMOVSEG(SEG.SS, GPR.EAX);

		// setup THREAD ID register; This puts the RVMThread.bootThread object ref into ESI
		asm.emitMOV_Reg_Abs(GPR.ESI, bootRecord.tocRegister.plus(bootRecord.bootThreadOffset));
		// setup top of stack pointer
		asm.emitLEA_Reg_Abs(GPR.ESP, bootRecord.spRegister);
		// Put JTOC, SP, and ESI at 0x1000E0
		asm.emitLEA_Reg_Abs(GPR.EAX, bootRecord.tocRegister);
		Address parameterStorage = Address.fromIntSignExtend(0x1000E0);
		asm.emitMOV_Abs_Reg(parameterStorage, GPR.EAX);
		asm.emitMOV_Abs_Reg(parameterStorage.plus(4), GPR.ESP);
        asm.emitMOV_Abs_Reg(parameterStorage.plus(8), GPR.ESI);
		
		// setup the thread register's frame pointer
		asm.emitMOV_Reg_Reg(GPR.EAX, GPR.ESP);
		asm.emitSUB_Reg_Imm_Byte(GPR.EAX, 8);
		asm.emitMOV_RegInd_Reg(GPR.EAX, GPR.ESI);
		// setup the return address sentinel
		asm.emitPUSH_Imm(0xdeadbabe);
		// setup the frame pointer sentinel
		asm.emitPUSH_Imm(StackframeLayoutConstants.STACKFRAME_SENTINEL_FP.toInt());
		// setup invisible method id
		asm.emitPUSH_Imm(StackframeLayoutConstants.INVISIBLE_METHOD_ID);
		// For null pointer calls; just halt
		Address Zero = Address.zero();
		asm.emitMOV_Abs_Imm(Zero, 0xf4f4f4f4);
		// 
		// 
		// create the primordial object
		
		// setup FRAME POINTER
		// call VM.boot(); we are never coming back
//		asm.emitFARCALL(bootRecord.ipRegister, CODE_SEGMENT);
		asm.emitCALL_Imm(bootRecord.ipRegister.minus(X86_LOADADDR).toInt());
		// asm.emitCALL_Imm(vmEntry.minus(0x100000).toInt());
	}
	
	/* (non-Javadoc)
     * @see org.jikesrvm.tools.bootImageWriter.ProcessorStartup#writeImage(java.lang.String)
     */
	public void writeImage(String filename) {
		// Write startup image out to a file
		CodeArray codeArray = asm.getMachineCodes();
		byte[] code = (byte[]) codeArray.getBacking();
		FileOutputStream out = null;
		File oldFile = new File(filename);
		oldFile.delete();
		try {
			out = new FileOutputStream(filename);
			out.write(code);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	/* (non-Javadoc)
     * @see org.jikesrvm.tools.bootImageWriter.ProcessorStartup#getArray()
     */
	public byte[] getArray() {
		return (byte[]) asm.getMachineCodes().getBacking();
	}
}
