package org.jikesrvm.tools.bootImageWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.jikesrvm.ia32.CodeArray;
import org.jikesrvm.ia32.RegisterConstants.GPR;
import org.vmmagic.unboxed.Address;

public class GenerateX86Startup {
	private JamAssembler asm = new JamAssembler(1024);

	public GenerateX86Startup(Address stack, Address vmEntry) {
		int multibootEntry=1;
		asm.emitJMP_Label(multibootEntry);
		asm.align(4);
		// Insert multiboot header
		Multiboot mbh = new Multiboot();
		int codeIndex = asm.getMachineCodeIndex();
		mbh.setHeaderAddress(0x100000+codeIndex);
		mbh.setEntryAddress(0x100000);
		mbh.setLoadAddress(0x100000);
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
		Address gdtTablePtr = Address.fromIntZeroExtend(0x100050);
		Address idtTablePtr = gdtTablePtr.plus(0x40);
		asm.space(0xc0);
		// GDT table pointer; limit is 3*N-1 bytes
		asm.emitImm16((3*8)-1, 0x40);
		asm.emitImm32(gdtTablePtr.toInt(), 0x42);
		// descriptor 0 is null
		// decscriptor 1 is the code segment
		asm.emitImm32(0xffff, 0x58);
		asm.emitImm32(0xcf9a00, 0x60);
		// descriptor 2 is the data segment
		asm.emitImm32(0xffff, 0x68);
		asm.emitImm32(0xcf9200, 0x70);
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
		
		// setup idt, gdt
		asm.emitLGDT(gdtTablePtr);
		// asm.emitLIDT(idtTablePtr);
		
		// enable protected mode
		// setup JTOC pointer
		// setup THREAD ID
		// setup FRAME POINTER
		// call VM.boot()
		asm.emitCALL_Imm(vmEntry.toInt());
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
