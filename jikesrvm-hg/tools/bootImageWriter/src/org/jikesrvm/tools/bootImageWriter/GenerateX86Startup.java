package org.jikesrvm.tools.bootImageWriter;

import org.jikesrvm.ia32.RegisterConstants.GPR;
import org.vmmagic.unboxed.Address;

public class GenerateX86Startup {
	public GenerateX86Startup(Address stack, Address vmEntry) {
		JamAssembler asm = new JamAssembler(1024);
		int multibootEntry=1;
		asm.emitJMP_Label(multibootEntry);
		asm.align(2);
		asm.space(Multiboot.HEADER_SIZE);
		asm.resolveForwardReferences(multibootEntry);
		// multiboot entry starts here
		// set the stack pointer
		// asm.emitMOV_Reg_Imm(GPR.ESP, stack.toInt());
		// reset coprocessor
		asm.emitOUTB();
		// ignore all PIC interrupts
		// setup idt, gdt, ldt
		// enable protected mode
		// setup JTOC pointer
		// setup THREAD ID
		// setup FRAME POINTER
		// call VM.boot()
		asm.emitCALL_Imm(vmEntry.toInt());
	}
}
