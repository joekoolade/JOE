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
import org.jikesrvm.classloader.Atom;
import org.jikesrvm.classloader.RVMClass;
import org.jikesrvm.classloader.TypeReference;
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
    private static Idt       idt                            = new Idt(48);
    int                      codeSegment;
    int                      limit;
    final private static int MAX_VECTORS                    = 256;
    final private static int DEFAULT_IDT_DESCRIPTOR_ADDRESS = 0x800;
    final private static int DEFAULT_IDT_VECTOR_TABLE       = 0;
    final private static IrqVector dispatchTable[]          = new IrqVector[MAX_VECTORS];
    private static RVMClass interruptVectorClass            = null;
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
  
	private Idt()
	{
	    this(Address.fromIntZeroExtend(DEFAULT_IDT_VECTOR_TABLE), MAX_VECTORS, 8);
	}
	
	private Idt(int limit)
	{
	    this(Address.fromIntZeroExtend(DEFAULT_IDT_VECTOR_TABLE), limit, 8);
	}
	
	private Idt(Address base, int limit, int codeSegment)
	{
		if (!VM.runningVM)
			return;
		this.base = base;
		this.codeSegment = codeSegment;
		this.limit = limit * 8 - 1;

		idtTableRegister = base;
		idtTableRegister.store((short) this.limit);
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
		
		interruptVectorClass = TypeReference.findOrCreate(InterruptVectors.class).peekType().asClass();
		loadVectors();
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
	    public static void int0()
	    {
	        VM.sysFail("FAULT: Divide by 0");
	    }
	    @InterruptHandler
	    public static void int1()
	    {
	        VM.sysFail("INT1, Reserved");
	    }
	    @InterruptHandler
	    public static void int2()
	    {
	        VM.sysFail("NMI Interrupt");
	    }
	    @InterruptHandler
	    public static void int3()
	    {
	        VM.sysFail("Breakpoint");
	    }
	    @InterruptHandler
	    public static void int4()
	    {
	        VM.sysFail("Overflow");
	    }
        @InterruptHandler
        public static void int5()
        {
            VM.sysFail("BOUND range exceeded");
        }
        @InterruptHandler
        public static void int6()
        {
            VM.sysFail("Invalid Opcode");
        }
        @InterruptHandler
        public static void int7()
        {
            VM.sysFail("Device Not Available");
        }
        @InterruptHandler
        public static void int8()
        {
            VM.sysFail("Double Fault");
        }
        @InterruptHandler
        public static void int9()
        {
            VM.sysFail("Coprocessor Segment Overrun");
        }
        @InterruptHandler
        public static void int10()
        {
            VM.sysFail("Invalid TSS");
        }
       @InterruptHandler
        public static void int11()
        {
            VM.sysFail("Segment Not Present");
        }
       @InterruptHandler
       public static void int12()
       {
           VM.sysFail("Stack Segment Fault");
       }
       @InterruptHandler
       public static void int13()
       {
           VM.sysFail("General Protection");
       }
       @InterruptHandler
       public static void int14()
       {
           VM.sysFail("Page Fault");
       }
       @InterruptHandler
       public static void int15()
       {
           VM.sysFail("INT15, reserved");
       }
       @InterruptHandler
       public static void int16()
       {
           VM.sysFail("x87 FPU Floating-Point Error");
       }
       @InterruptHandler
       public static void int17()
       {
           VM.sysFail("Alignment Check");
       }
       @InterruptHandler
       public static void int18()
       {
           VM.sysFail("Machine Check");
       }
       @InterruptHandler
       public static void int19()
       {
           VM.sysFail("SIMD Floating Point Exception");
       }
       @InterruptHandler
       public static void int20()
       {
           VM.sysFail("Virtualization Exception");
       }
       @InterruptHandler
       public static void int21()
       {
           VM.sysFail("INT21");
       }
       @InterruptHandler
       public static void int22()
       {
           VM.sysFail("INT22");
       }
       @InterruptHandler
       public static void int23()
       {
           VM.sysFail("INT23");
       }
       @InterruptHandler
       public static void int24()
       {
           VM.sysFail("INT24");
       }
       @InterruptHandler
       public static void int25()
       {
           VM.sysFail("INT25");
       }
       @InterruptHandler
       public static void int26()
       {
           VM.sysFail("INT26");
       }
       @InterruptHandler
       public static void int27()
       {
           VM.sysFail("INT27");
       }
       @InterruptHandler
       public static void int28()
       {
           VM.sysFail("INT28");
       }
       @InterruptHandler
       public static void int29()
       {
           VM.sysFail("INT29");
       }
       @InterruptHandler
       public static void int30()
       {
           VM.sysFail("INT30");
       }
       @InterruptHandler
       public static void int31()
       {
           VM.sysFail("INT31");
       }
       @InterruptHandler
       public static void int32()
       {
           if(dispatchTable[32] != null)
           {
               dispatchTable[32].vector.handler();
           }
           else
           {
               VM.sysWriteln("INT32, spurious interrupts");
           }
       }
       @InterruptHandler
       public static void int33()
       {
           VM.sysFail("IRQ1");
       }
       @InterruptHandler
       public static void int34()
       {
           VM.sysFail("IRQ2");
       }
       @InterruptHandler
       public static void int35()
       {
           VM.sysFail("IRQ3");
       }
       @InterruptHandler
       public static void int36()
       {
           VM.sysFail("IRQ4");
       }
       @InterruptHandler
       public static void int37()
       {
           VM.sysFail("IRQ5");
       }
       @InterruptHandler
       public static void int38()
       {
           VM.sysFail("IRQ6");
       }
       @InterruptHandler
       public static void int39()
       {
           VM.sysFail("IRQ7");
       }
       @InterruptHandler
       public static void int40()
       {
           VM.sysFail("TRAP_NULL_POINTER");
       }
       @InterruptHandler
       public static void int41()
       {
           VM.sysFail("TRAP_ARRAY_BOUNDS");
       }
       @InterruptHandler
       public static void int42()
       {
           VM.sysFail("TRAP_STACK_OVERFLOW");
       }
       @InterruptHandler
       public static void int43()
       {
           VM.sysFail("TRAP_CHECKCAST");
       }
       @InterruptHandler
       public static void int44()
       {
           VM.sysFail("TRAP_REGENERATE");
       }
       @InterruptHandler
       public static void int45()
       {
           VM.sysFail("TRAP_MUST_IMPLEMENT");
       }
       @InterruptHandler
       public static void int46()
       {
           VM.sysFail("TRAP_STORE_CHECK");
       }
       @InterruptHandler
       public static void int47()
       {
           VM.sysFail("TRAP_STACK_OVERFLOW_FATAL");
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
	
    final private Address getIrqAddress(Atom method)
	{
	    Address irqAddress=Magic.objectAsAddress(interruptVectorClass.findDeclaredMethod(method).getCurrentEntryCodeArray());
	    return irqAddress;
	}
	
	void loadVectors()
	{
	    Address irqAddress;
		/*
		 * Load the default interrupt handlers
		 */
	    /**
	     * divide error exception
	     */
	    irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int0"));
	    storeVector(0, irqAddress);
	    /**
	     * debug exception
	     */
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int1"));
        storeVector(1, irqAddress);
        /**
         * nmi interrupt
         */
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int2"));
        storeVector(2, irqAddress);
        /**
         * breakpoint exception
         */
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int3"));
        storeVector(3, irqAddress);
        /**
         * overflow exception
         */
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int4"));
        storeVector(4, irqAddress);
        /**
         * BOUND range exceeded exception
         */
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int5"));
        storeVector(5, irqAddress);
        /**
         * invalid opcode exception
         */
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int6"));
        storeVector(6, irqAddress);
        /**
         * device not available exception
         */
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int7"));
        storeVector(7, irqAddress);
        /**
         * double fault exception
         */
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int8"));
        storeVector(8, irqAddress);
        /**
         * coprocessor segment overrun 
         */
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int9"));
        storeVector(9, irqAddress);
        /**
         * invalid TSS
         */
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int10"));
        storeVector(10, irqAddress);
        /**
         * segment not present
         */
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int11"));
        storeVector(11, irqAddress);
        /**
         * stack segment fault
         */
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int12"));
        storeVector(12, irqAddress);
        /**
         * general protection
         */
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int13"));
        storeVector(13, irqAddress);
        /**
         * page fault
         */
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int14"));
        storeVector(14, irqAddress);
        /**
         * Intel reserved
         */
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int15"));
        storeVector(15, irqAddress);
        /**
         * x87 FPU floating point error
         */
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int16"));
        storeVector(16, irqAddress);
        /**
         * alignment check
         */
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int17"));
        storeVector(17, irqAddress);
        /**
         * machine check
         */
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int18"));
        storeVector(18, irqAddress);
        /**
         * SIMD floating point exception
         */
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int19"));
        storeVector(19, irqAddress);
        /**
         * virtualization exception
         */
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int20"));
        storeVector(20, irqAddress);
        /**
         * Intel reserved, 21-31
         */
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int21"));
        storeVector(21, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int22"));
        storeVector(22, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int23"));
        storeVector(23, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int24"));
        storeVector(24, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int25"));
        storeVector(25, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int26"));
        storeVector(26, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int27"));
        storeVector(27, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int28"));
        storeVector(28, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int29"));
        storeVector(29, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int30"));
        storeVector(30, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int31"));
        storeVector(31, irqAddress);
        /**
         * IRQ0
         */
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int32"));
        storeVector(32, irqAddress);
        /**
         * IRQ1
         */
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int33"));
        storeVector(33, irqAddress);
        /**
         * IRQ2
         */
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int34"));
        storeVector(34, irqAddress);
        /**
         * IRQ3
         */
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int35"));
        storeVector(35, irqAddress);
        /**
         * IRQ4
         */
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int36"));
        storeVector(36, irqAddress);
        /**
         * IRQ5
         */
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int37"));
        storeVector(37, irqAddress);
        /**
         * IRQ6
         */
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int38"));
        storeVector(38, irqAddress);
        /**
         * IRQ7
         */
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int39"));
        storeVector(39, irqAddress);
        /**
         * TRAP_NULL_POINTER
         */
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int40"));
        storeVector(40, irqAddress);
        /**
         * TRAP_ARRAY_BOUNDS
         */
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int41"));
        storeVector(41, irqAddress);
        /**
         * TRAP_STACK_OVERFLOW
         */
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int42"));
        storeVector(42, irqAddress);
        /**
         * TRAP_CHECKCAST
         */
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int43"));
        storeVector(43, irqAddress);
        /**
         * TRAP_REGENERATE
         */
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int44"));
        storeVector(44, irqAddress);
        /**
         * TRAP_MUST_IMPLEMENT
         */
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int45"));
        storeVector(45, irqAddress);
        /**
         * TRAP_STORE_CHECK
         */
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int46"));
        storeVector(46, irqAddress);
        /**
         * TRAP_STACK_OVERFLOW_FATAL
         */
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int47"));
        storeVector(47, irqAddress);
	}

    /**
     * @param vector
     * @param irqAddress
     */
    private void storeVector(int vector, Address irqAddress)
    {
        Offset vectorOffset = Offset.fromIntSignExtend(vector * 8);
		base.store((codeSegment << 16) | (irqAddress.toInt() & 0xffff), vectorOffset);
		base.store((irqAddress.toInt() & 0xFFFF0000) | SEGMENT_PRESENT | INTERRUPTGATE,vectorOffset.plus(4));
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
	    IrqHandler vector;
	    byte[]  stack;
	    
	    public IrqVector()
	    {
	        vector = null;
	        stack = null;
	    }
	    
	    public IrqVector(IrqHandler handler, int stackSize)
	    {
	        this.vector = handler;
	        stack = new byte[stackSize];
	    }
	}
}
