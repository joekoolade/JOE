/*
 * Created on Oct 19, 2004
 * 
 * Copyright (C) Joe Kulig, 2004
 * All rights reserved.
 */
package org.jam.cpu.intel;

import org.jam.board.pc.Platform;
import org.jam.system.Trace;
import org.jikesrvm.VM;
import org.jikesrvm.classloader.Atom;
import org.jikesrvm.classloader.RVMClass;
import org.jikesrvm.classloader.TypeReference;
import org.jikesrvm.runtime.EntrypointHelper;
import org.jikesrvm.runtime.Magic;
import org.jikesrvm.runtime.RuntimeEntrypoints;
import org.jikesrvm.scheduler.RVMThread;
import org.vmmagic.pragma.Entrypoint;
import org.vmmagic.pragma.InterruptHandler;
import org.vmmagic.pragma.NonMoving;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Offset;
import static org.jikesrvm.runtime.Entrypoints.athrowAddressField;
import static org.jikesrvm.runtime.Entrypoints.athrowMethod;

/**
 * @author Joe Kulig
 *
 *         Interrupt Decsciptor Table
 *         A table of 256 vectors that associates each exception/interrupt with a gate descriptor to
 *         a procedure used to service the associated exception/interrupt
 */
@NonMoving
public final class Idt implements SegmentDescriptorTypes {
    private static Idt       idt                             = new Idt(96);
    int                      codeSegment;
    int                      limit;
    final private static int MAX_VECTORS                     = 256;
    final private static int DEFAULT_IDT_VECTOR_TABLE        = 0x1000;
    public static final int STACK_SEGMENT_INT                = 12;
    private static RVMClass interruptVectorClass             = null;
    private static NullPointerException nullPointerExc = new NullPointerException();
    private static ArithmeticException arithmeticExc = new ArithmeticException();
    private static ArrayIndexOutOfBoundsException arrayIndexExc = new ArrayIndexOutOfBoundsException();
    private static InternalError internalError = new InternalError();
    private static StackOverflowError stackError = new StackOverflowError();
    private static ClassCastException castError = new ClassCastException();
    
    @Entrypoint
    public static Address athrowMethodAddress               = null;

    /**
     * Memory location of the IDT vector table
     */
    Address                  base;

    //	@formatter:off
    /**
     *  Memory location of of the IDTR register memory location
     * 
     *  						          0       2      6
     *  						          +-------+------+
     *  idtTableRegister -->	          |limit  | base |
     *  						          +-------+------+
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
		this.limit = limit * 16 - 1;

//		VM.sysWriteln("athrow nullexception ", Magic.objectAsAddress(nullPointerExc));
		athrowMethodAddress = Magic.objectAsAddress(athrowMethod.getCurrentEntryCodeArray());
//        VM.sysWrite("athrow method addr ", athrowMethodAddress);
//        VM.sysWriteln(" ", athrowMethod.getId());
//        VM.sysWriteln("IDT base = ", base);
//        VM.sysWriteln("limit1 = ", this.limit);
		idtTableRegister = Address.fromIntZeroExtend(base.toInt());
		idtTableRegister.store((short) this.limit);
		idtTableRegister.store(base, Offset.zero().plus(2));

		Magic.setIdt(idtTableRegister);   
		interruptVectorClass = TypeReference.findOrCreate(InterruptVectors.class).peekType().asClass();
		loadVectors();
		/*
		 * Put HLT in memory locations 0 - 0x1000
		 */
		Address addr = Address.zero();
		Offset offset = Offset.zero();
		for(int i=0; i < 0x1000; i += 4)
		{
		    addr.store(0, offset.plus(i));
		}
	}
  
	public static Idt getInstance() {
	    if(idt == null)
	    {
	        idt = new Idt(96);
	    }
		return idt;
	}

	public static void init()
	{
	    idt = new Idt(96);
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
	@NonMoving
	@Uninterruptible
	static class InterruptVectors {
	    @InterruptHandler
	    public static void int0()
	    {
	        Magic.throwException(arithmeticExc);
	    }
	    @InterruptHandler
	    public static void int1()
	    {
          VM.sysWriteln("INT1, Reserved");
	      Magic.throwException(internalError);
//            Magic.halt();
//            while(true) ;
//	        VM.sysFail("INT1, Reserved");
	    }
	    @InterruptHandler
	    public static void int2()
	    {
            Magic.throwException(internalError);
//            Magic.halt();
//            while(true) ;
//	        VM.sysFail("NMI Interrupt");
	    }
	    @InterruptHandler
	    public static void int3()
	    {
            Magic.throwException(internalError);
//            Magic.halt();
//            while(true) ;
//	        VM.sysFail("Breakpoint");
	    }
	    @InterruptHandler
	    public static void int4()
	    {
            Magic.throwException(arithmeticExc);
	    }
        @InterruptHandler
        public static void int5()
        {
            Magic.throwException(arrayIndexExc);
        }
        @InterruptHandler
        public static void int6()
        {
            Magic.throwException(internalError);
//            Magic.saveContext();
//            Trace.printLog();
//           Magic.halt();
//           while(true) ;
//          VM.sysFailTrap("Invalid Opcode");
        }
        @InterruptHandler
        public static void int7()
        {
            Magic.throwException(internalError);

//          Magic.halt();
//          while(true) ;
//            VM.sysFail("Device Not Available");
        }
        @InterruptHandler
        public static void int8()
        {
//          Magic.halt();
//          while(true) ;
//            Magic.saveContext();
//            Magic.halt();
            VM.write("Double Fault");
            VM.sysFail("Double Fault");
        }
        @InterruptHandler
        public static void int9()
        {
            Magic.throwException(internalError);
//          Magic.halt();
//          while(true) ;
//            VM.sysFail("Coprocessor Segment Overrun");
        }
        @InterruptHandler
        public static void int10()
        {
            Magic.throwException(internalError);
//          Magic.halt();
//          while(true) ;
//            VM.sysFail("Invalid TSS");
        }
       @InterruptHandler
        public static void int11()
        {
           Magic.throwException(internalError);
//         Magic.halt();
//         while(true) ;
//            VM.sysFail("Segment Not Present");
        }
       @InterruptHandler
       public static void int12()
       {
           VM.sysWriteln("Stack Overflow ", Magic.getFramePointer());
           Magic.throwException(stackError);
//           VM.sysFail("Stack Overflow");
           Magic.halt();
           while(true) ;
//           Magic.throwException(stackError);
//           VM.sysFail("Stack Segment Fault");
       }
       @InterruptHandler
       public static void int13()
       {
           VM.sysWriteln("int13 nullpointer");
           Magic.throwException(nullPointerExc);
           Magic.halt();
       }
       @InterruptHandler
       public static void int14()
       {
           Magic.throwException(nullPointerExc);
       }
       @InterruptHandler
       public static void int15()
       {
           Magic.throwException(internalError);
//           Magic.halt();
//           while(true) ;
//           VM.sysWriteln("Spurious Interrupt");
       }
       @InterruptHandler
       public static void int16()
       {
           VM.sysWriteln("Floating Point Error");
           Magic.throwException(arithmeticExc);
       }
       @InterruptHandler
       public static void int17()
       {
           Magic.throwException(internalError);
//           Magic.halt();
//           while(true) ;
//           VM.sysFail("Alignment Check");
       }
       @InterruptHandler
       public static void int18()
       {
           Magic.throwException(internalError);
//           Magic.halt();
//           while(true) ;
//           VM.sysFail("Machine Check");
       }
       @InterruptHandler
       public static void int19()
       {
           Magic.throwException(internalError);
//           Magic.halt();
//           while(true) ;
//           VM.sysFail("SIMD Floating Point Exception");
       }
       @InterruptHandler
       public static void int20()
       {
           Magic.throwException(internalError);
//           Magic.halt();
//           while(true) ;
//           VM.sysFail("Virtualization Exception");
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
       /**
        * IRQ0, System timer interrupt
        */
       @InterruptHandler
       public static void int32()
       {
           // Save registers on the interrupted stack
           Magic.saveContext();
           RVMThread.interruptLevel++;
           Trace.irqStart(32);
           Platform.timer.handler();
           Platform.apic.eoi();
           Trace.irqEnd(32);
          // Restore back to the interrupt stack and context
           RVMThread.interruptLevel--;
           Magic.restoreThreadContextNoErrCode();
           // The interrupt handler annotation will emit the IRET
           // good bye
       }
       /**
        * IRQ1, keyboard interrupt
        */
       @InterruptHandler
       public static void int33()
       {
           VM.sysFailTrap("int33");       }
       
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
       /**
        * IRQ4, COM1/COM3 serial ports
        */
       @InterruptHandler
       public static void int36()
       {
           // Save registers on the interrupted stack
           Magic.saveContext();
           RVMThread.interruptLevel++;
           Trace.irqStart(36);
           Platform.serialPort.handler();
           Platform.apic.eoi();
           Trace.irqEnd(36);
           RVMThread.interruptLevel--;
           // Restore back to the interrupt stack and context
           Magic.restoreThreadContextNoErrCode();
           // The interrupt handler annotation will emit the IRET
           // good bye
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
         Magic.saveContext();
         VM.sysFailTrap("TRAP_NULL_POINTER");
       }
       @InterruptHandler
       public static void int41()
       {
//           Platform.masterPic.interruptMask(0xFF);
//           Magic.disableInterrupts();
         VM.sysFailTrap("TRAP_ARRAY_BOUNDS");
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
       /**
        * SWI handler; signals that a yield/thread switch needs to take place
        */
       @InterruptHandler
       public static void int48()
       {
           // Save registers on the interrupted stack
           Magic.saveContext();
           RVMThread.interruptLevel++;
//           Trace.irqStart(48);
           RVMThread.isInterrupted = true;
//           VM.sysWrite('Y');
//           Platform.scheduler.addThread(Magic.getThreadRegister());
//           Platform.scheduler.nextThread();
           // Restore back to the interrupt stack and context
           RVMThread.isInterrupted = false;
           RVMThread.interruptLevel--;
//           Trace.irqEnd(48);
           Magic.restoreThreadContextNoErrCode();
//           Magic.halt();
           // The interrupt handler annotation will emit the IRET
           // good bye
       }
       
       @InterruptHandler
       public static void int49()
       {
         VM.sysFailTrap("int49");
       }
       @InterruptHandler
       public static void int50()
       {
         VM.sysFailTrap("int50");
       }
       @InterruptHandler
       public static void int51()
       {
         VM.sysFailTrap("int51");
       }
       @InterruptHandler
       public static void int52()
       {
         VM.sysFailTrap("int52");
       }
       @InterruptHandler
       public static void int53()
       {
         VM.sysFailTrap("int53");
       }
       @InterruptHandler
       public static void int54()
       {
         VM.sysFailTrap("int54");
       }
       @InterruptHandler
       public static void int55()
       {
         VM.sysFailTrap("int55");
       }
       @InterruptHandler
       public static void int56()
       {
         VM.sysFailTrap("int56");
       }
       @InterruptHandler
       public static void int57()
       {
         VM.sysFailTrap("int57");
       }
       @InterruptHandler
       public static void int58()
       {
         VM.sysFailTrap("int58");
       }
       @InterruptHandler
       public static void int59()
       {
         VM.sysFailTrap("int59");
       }
       @InterruptHandler
       public static void int60()
       {
         VM.sysFailTrap("int60");
       }
       @InterruptHandler
       public static void int61()
       {
         VM.sysFailTrap("int61");
       }
       @InterruptHandler
       public static void int62()
       {
         VM.sysFailTrap("int62");
       }
       @InterruptHandler
       public static void int63()
       {
         VM.sysFailTrap("int63");
       }
       @InterruptHandler
       public static void int64()
       {
         Magic.throwExceptionNoErrCode(nullPointerExc);
//         VM.sysFailTrap("int64");
       }
       
       @InterruptHandler
       public static void int65()
       {
         Magic.throwExceptionNoErrCode(arrayIndexExc);
//         VM.sysFailTrap("int65");
       }
       @InterruptHandler
       public static void int66()
       {
         VM.sysFailTrap("int66");
       }
       @InterruptHandler
       public static void int67()
       {
         VM.sysFailTrap("int67");
       }
       @InterruptHandler
       public static void int68()
       {
         Magic.throwExceptionNoErrCode(castError);
//         VM.sysFailTrap("int68");
       }
       @InterruptHandler
       public static void int69()
       {
         VM.sysFailTrap("int69");
       }
       @InterruptHandler
       public static void int70()
       {
         VM.sysFailTrap("int70");
       }
       @InterruptHandler
       public static void int71()
       {
         Magic.throwExceptionNoErrCode(castError);
//         VM.sysFailTrap("int71");
       }
       @InterruptHandler
       public static void int72()
       {
         VM.sysFailTrap("int72");
       }
       @InterruptHandler
       public static void int73()
       {
         VM.sysFailTrap("int73");
       }
       @InterruptHandler
       public static void int74()
       {
         VM.sysFailTrap("int74");
       }
       @InterruptHandler
       public static void int75()
       {
         VM.sysFailTrap("int75");
       }
       @InterruptHandler
       public static void int76()
       {
         VM.sysFailTrap("int76");
       }
       @InterruptHandler
       public static void int77()
       {
         VM.sysFailTrap("int77");
       }
       @InterruptHandler
       public static void int78()
       {
         VM.sysFailTrap("int78");
       }
       @InterruptHandler
       public static void int79()
       {
         VM.sysFailTrap("int79");
       }
       @InterruptHandler
       public static void int80()
       {
         VM.sysFailTrap("int80");
       }
       
       @InterruptHandler
       public static void int81()
       {
         VM.sysFailTrap("int81");
       }
       @InterruptHandler
       public static void int82()
       {
         VM.sysFailTrap("int82");
       }
       @InterruptHandler
       public static void int83()
       {
         VM.sysFailTrap("int83");
       }
       @InterruptHandler
       public static void int84()
       {
         VM.sysFailTrap("int84");
       }
       @InterruptHandler
       public static void int85()
       {
         VM.sysFailTrap("int85");
       }
       @InterruptHandler
       public static void int86()
       {
           Magic.saveContext();
           RVMThread.interruptLevel++;
           Trace.irqStart(33);
           Platform.kbd.interrupt();
           Platform.apic.eoi();
           Trace.irqEnd(33);
          // Restore back to the interrupt stack and context
           RVMThread.interruptLevel--;
           Magic.restoreThreadContextNoErrCode();
           // The interrupt handler annotation will emit the IRET
           // good bye
       }
       @InterruptHandler
       public static void int87()
       {
         // Save registers on the interrupted stack
         Magic.saveContext();
         RVMThread.interruptLevel++;
         Trace.irqStart(87);
         Platform.serialPort.handler();
         Platform.apic.eoi();
         Trace.irqEnd(87);
         // Restore back to the interrupt stack and context
         RVMThread.interruptLevel--;
         Magic.restoreThreadContextNoErrCode();
         // The interrupt handler annotation will emit the IRET
         // good bye
       }
       @InterruptHandler
       public static void int88()
       {
         VM.sysFailTrap("int88");
       }
       @InterruptHandler
       public static void int89()
       {
         VM.sysFailTrap("int89");
       }
       @InterruptHandler
       public static void int90()
       {
         VM.sysFailTrap("int90");
       }
       @InterruptHandler
       public static void int91()
       {
         VM.sysFailTrap("int91");
       }
       @InterruptHandler
       public static void int92()
       {
//         VM.sysFailTrap("int92");
         VM.sysWriteln("virtio net interrupt");
       }
       @InterruptHandler
       public static void int93()
       {
         VM.sysFailTrap("int93");
       }
       @InterruptHandler
       public static void int94()
       {
         VM.sysFailTrap("int94");
       }
       @InterruptHandler
       public static void int95()
       {
         // Save registers on the interrupted stack
         Magic.saveContext();
         RVMThread.interruptLevel++;
         Trace.irqStart(95);
         Platform.timer.handler();
         Trace.irqEnd(95);
         Platform.apic.eoi();
         RVMThread.interruptLevel--;
         // Restore back to the interrupt stack and context
         Magic.restoreThreadContextNoErrCode();
         // The interrupt handler annotation will emit the IRET
         // good bye
       }
       
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
//	    irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int0"));
//	    storeVector(0, irqAddress);
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
        /**
         * SWI; yields processor by calling the scheduler
         */
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int48"));
        storeVector(48, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int49"));
        storeVector(49, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int50"));
        storeVector(50, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int51"));
        storeVector(51, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int52"));
        storeVector(52, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int53"));
        storeVector(53, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int54"));
        storeVector(54, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int55"));
        storeVector(55, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int56"));
        storeVector(56, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int57"));
        storeVector(57, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int58"));
        storeVector(58, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int59"));
        storeVector(59, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int60"));
        storeVector(60, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int61"));
        storeVector(61, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int62"));
        storeVector(62, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int63"));
        storeVector(63, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int64"));
        storeVector(64, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int65"));
        storeVector(65, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int66"));
        storeVector(66, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int67"));
        storeVector(67, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int68"));
        storeVector(68, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int69"));
        storeVector(69, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int70"));
        storeVector(70, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int71"));
        storeVector(71, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int72"));
        storeVector(72, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int73"));
        storeVector(73, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int74"));
        storeVector(74, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int75"));
        storeVector(75, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int76"));
        storeVector(76, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int77"));
        storeVector(77, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int78"));
        storeVector(78, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int79"));
        storeVector(79, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int80"));
        storeVector(80, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int81"));
        storeVector(81, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int82"));
        storeVector(82, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int83"));
        storeVector(83, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int84"));
        storeVector(84, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int85"));
        storeVector(85, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int86"));
        storeVector(86, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int87"));
        storeVector(87, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int88"));
        storeVector(88, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int89"));
        storeVector(89, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int90"));
        storeVector(90, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int91"));
        storeVector(91, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int92"));
        storeVector(92, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int93"));
        storeVector(93, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int94"));
        storeVector(94, irqAddress);
        irqAddress = getIrqAddress(Atom.findOrCreateAsciiAtom("int95"));
        storeVector(95, irqAddress);

	}

    /**
     * @param vector
     * @param irqAddress
     */
    private void storeVector(int vector, Address irqAddress)
    {
        Offset vectorOffset = Offset.fromIntZeroExtend(vector * 16);
		base.store((codeSegment << 16) | (irqAddress.toInt() & 0xffff), vectorOffset);
		base.store((irqAddress.toInt() & 0xFFFF0000) | SEGMENT_PRESENT | INTERRUPTGATE,vectorOffset.plus(4));
		// assuming bits 63-32 are zero
		base.store(0, vectorOffset.plus(8));
        base.store(0, vectorOffset.plus(12));
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
