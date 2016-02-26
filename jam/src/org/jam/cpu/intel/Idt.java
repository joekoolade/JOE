/*
 * Created on Oct 19, 2004
 * 
 * Copyright (C) Joe Kulig, 2004
 * All rights reserved.
 */
package org.jam.cpu.intel;

import org.jikesrvm.ArchitectureSpecific;
import org.jikesrvm.ArchitectureSpecific.Assembler;
import org.jikesrvm.ArchitectureSpecific.CodeArray;
import org.jikesrvm.VM;
import org.jikesrvm.runtime.Magic;
import org.vmmagic.pragma.InterruptHandler;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Offset;

/**
 * @author Joe Kulig
 *
 *         Interrupt Decsciptor Table
 *         A table of 256 vectors that associates each exception/interrupt with a gate descriptor to
 *         a procedure used to service the associated exception/interrupt
 */
public final class Idt implements SegmentDescriptorTypes {
    private static Idt       idt                            = new Idt();
    int                      codeSegment;
    int                      limit;
    final private static int MAX_VECTORS                    = 256;
    final private static int DEFAULT_IDT_DESCRIPTOR_ADDRESS = 0x800;
    final private static int DEFAULT_IDT_VECTOR_TABLE       = 0;
    final private static IrqVector dispatchTable[] = new IrqVector[MAX_VECTORS];
    
    /**
     * Memory location of the IDT vector table
     */
    Address                  base;

    //	@formatter:off
    /**
     *  Memory location of of the IDTR register memory location
     * 
     *  						0       2      6
     *  						+-------+------+
     *  idtTableRegister -->	|limit  | base |
     *  						+-------+------+
     */
	// formatter:on
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
	 * 
	 * @author Joe Kulig
	 *
	 * This class provides static methods that can be dispatched from the IDT vector table
	 * 
	 */
	static class InterruptVectors {
	    @InterruptHandler
	    public static void irq0()
	    {
	        VM.sysFail("FAULT: Divide by 0");
	    }
	    @InterruptHandler
	    public static void irq1()
	    {
	        VM.sysFail("INT1, Reserved");
	    }
	    @InterruptHandler
	    public static void irq2()
	    {
	        VM.sysFail("NMI Interrupt");
	    }
	    @InterruptHandler
	    public static void irq3()
	    {
	        VM.sysFail("Breakpoint");
	    }
	    @InterruptHandler
	    public static void irq4()
	    {
	        VM.sysFail("Overflow");
	    }
        @InterruptHandler
        public static void irq5()
        {
            VM.sysFail("BOUND range exceeded");
        }
        @InterruptHandler
        public static void irq6()
        {
            VM.sysFail("Invalid Opcode");
        }
        @InterruptHandler
        public static void irq7()
        {
            VM.sysFail("Device Not Available");
        }
        @InterruptHandler
        public static void irq8()
        {
            VM.sysFail("Double Fault");
        }
        @InterruptHandler
        public static void irq9()
        {
            VM.sysFail("Coprocessor Segment Overrun");
        }
        @InterruptHandler
        public static void irq10()
        {
            VM.sysFail("Invalid TSS");
        }
       @InterruptHandler
        public static void irq11()
        {
            VM.sysFail("Segment Not Present");
        }
       @InterruptHandler
       public static void irq12()
       {
           VM.sysFail("Stack Segment Fault");
       }
       @InterruptHandler
       public static void irq13()
       {
           VM.sysFail("General Protection");
       }
       @InterruptHandler
       public static void irq14()
       {
           VM.sysFail("Page Fault");
       }
       @InterruptHandler
       public static void irq15()
       {
           VM.sysFail("INT15, reserved");
       }
       @InterruptHandler
       public static void irq16()
       {
           VM.sysFail("x87 FPU Floating-Point Error");
       }
       @InterruptHandler
       public static void irq17()
       {
           VM.sysFail("Alignment Check");
       }
       @InterruptHandler
       public static void irq18()
       {
           VM.sysFail("Machine Check");
       }
       @InterruptHandler
       public static void irq19()
       {
           VM.sysFail("SIMD Floating Point Exception");
       }
       @InterruptHandler
       public static void irq20()
       {
           VM.sysFail("Virtualization Exception");
       }
       @InterruptHandler
       public static void irq21()
       {
           VM.sysFail("INT21");
       }
       @InterruptHandler
       public static void irq22()
       {
           VM.sysFail("INT22");
       }
       @InterruptHandler
       public static void irq23()
       {
           VM.sysFail("INT23");
       }
       @InterruptHandler
       public static void irq24()
       {
           VM.sysFail("INT24");
       }
       @InterruptHandler
       public static void irq25()
       {
           VM.sysFail("INT25");
       }
       @InterruptHandler
       public static void irq26()
       {
           VM.sysFail("INT26");
       }
       @InterruptHandler
       public static void irq27()
       {
           VM.sysFail("INT27");
       }
       @InterruptHandler
       public static void irq28()
       {
           VM.sysFail("INT28");
       }
       @InterruptHandler
       public static void irq29()
       {
           VM.sysFail("INT29");
       }
       @InterruptHandler
       public static void irq30()
       {
           VM.sysFail("INT30");
       }
       @InterruptHandler
       public static void irq31()
       {
           VM.sysFail("INT31");
       }

	}
	/**
	 * Installs irq route at interrupt vector
	 */
	public void registerHandler(int vector, IrqHandler irq, int stackSize)
	{
	    IrqVector tableVector = new IrqVector(irq, stackSize);
	    dispatchTable[vector] = tableVector; 
	}
	
	void loadVectors()
	{
	    Address irq=Address.zero();
	    int vector=0;
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
	
	class IrqVector {
	    IrqHandler handler;
	    byte[]  stack;
	    
	    public IrqVector()
	    {
	        handler = null;
	        stack = null;
	    }
	    
	    public IrqVector(IrqHandler handler, int stackSize)
	    {
	        this.handler = handler;
	        stack = new byte[stackSize];
	    }
	}
}
