/*
 * Created on Oct 19, 2004
 *
 * Copyright (C) Joe Kulig, 2004
 * All rights reserved.
 * 
 */
package org.jam.cpu.intel;

import org.jikesrvm.ArchitectureSpecific;
import org.jikesrvm.ArchitectureSpecific.Assembler;
import org.jikesrvm.ArchitectureSpecific.CodeArray;
import org.jikesrvm.VM;
import org.jikesrvm.runtime.Magic;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Offset;


/**
 * @author Joe Kulig
 *
 * Interrupt Decsciptor Table
 * A table of 256 vectors that associates each exception/interrupt with a gate descriptor to
 * a procedure used to service the associated exception/interrupt
 */
public final class Idt
implements SegmentDescriptorTypes
{
	private static Idt idt = new Idt();
	int codeSegment;
	int limit;
	final private static int MAX_VECTORS = 256;
	final private static int DEFAULT_IDT_DESCRIPTOR_ADDRESS = 0x800;
	final private static int DEFAULT_IDT_VECTOR_TABLE = 0;

	/**
	 * Memory location of the IDT vector table
	 */
	Address base;

  /**
   *  Memory location of of the IDTR register memory location
   *  
   *  						0       2      6
   *  						+-------+------+
   *  idtTableRegister -->	|limit  | base |
   *  						+-------+------+
   */
  private Address idtTableRegister;
  
	private Idt() {
		if (!VM.runningVM)
			return;
		base = Address.fromIntZeroExtend(DEFAULT_IDT_VECTOR_TABLE);
		codeSegment = 8;
		limit = MAX_VECTORS * 8 - 1;

		idtTableRegister = Address.fromIntZeroExtend(DEFAULT_IDT_DESCRIPTOR_ADDRESS);
		idtTableRegister.store((short) limit);
		idtTableRegister.store(base, Offset.zero().plus(2));

		/*
		 * Generate code to load the IDT register and then call it
		 */
		Assembler asm = new ArchitectureSpecific.Assembler(2, false);
		// load the IDT from an absolute address
		asm.emitLIDT(idtTableRegister);
		// return
		asm.emitRET();
		VM.sysWrite("Loading IDT\n");
		try {
			Magic.invokeClassInitializer(asm.getMachineCodes());
			VM.write("\nIDT loaded!\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
  
	public static Idt getInstance() {
		return idt;
	}

	public void init(Address irqTable, int size) {
		base = irqTable;
		limit = size * 8 - 1;
	}

	/**
	 * Installs irq route at interrupt vector
	 */
	public void loadVector(int vector, Address irq) {
		/*
		 * Write out IDT Interrupt gate descriptor
		 */
		Offset vectorOffset = Offset.fromIntSignExtend(vector * 8);
		base.store((codeSegment << 16) | (irq.toInt() & 0xffff), vectorOffset);
		base.store((irq.toInt() & 0xFFFF0000) | SEGMENT_PRESENT | INTERRUPTGATE,vectorOffset.plus(4));
	}

	/**
	 * @return the base
	 */
	public Address getBase() {
		return base;
	}

	/**
	 * @param base
	 *            the base to set
	 */
	public void setBase(Address base) {
		this.base = base;
	}

	/**
	 * @return the codeSegment
	 */
	public int getCodeSegment() {
		return codeSegment;
	}

	/**
	 * @param codeSegment
	 *            the codeSegment to set
	 */
	public void setCodeSegment(int codeSegment) {
		this.codeSegment = codeSegment;
	}

	/**
	 * @return the limit
	 */
	public int getLimit() {
		return limit;
	}

	/**
	 * @param limit
	 *            the limit to set
	 */
	public void setLimit(int limit) {
		this.limit = limit;
	}
}
