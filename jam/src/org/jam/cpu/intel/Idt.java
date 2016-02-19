/*
 * Created on Oct 19, 2004
 *
 * Copyright (C) Joe Kulig, 2004
 * All rights reserved.
 * 
 */
package org.jam.cpu.intel;

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
  private static Idt idt0 = new Idt();
  Address base;
  int codeSegment;
  int limit;
  final private static int MAX_VECTORS = 256;
  
  private Idt() {
    base = Address.fromIntZeroExtend(0);
    codeSegment = 8;
    limit = MAX_VECTORS*8-1;
  }
  
  public static Idt getInstance()
  {
	  return idt0;
  }
  
  public void init(Address irqTable, int size) 
  {
	  base = irqTable;
	  limit = size * 8 - 1;
  }
  
  /**
   * Installs irq route at interrupt vector
   */
  public void loadVector(int vector, Address irq) 
  {
	  /*
	   * Write out IDT Interrupt gate descriptor
	   */
	  Offset vectorOffset = Offset.fromIntSignExtend(vector * 8);
	  base.store((codeSegment<<16)|(irq.toInt()&0xffff), vectorOffset);
	  base.store((irq.toInt() & 0xFFFF0000) | SEGMENT_PRESENT | INTERRUPTGATE, vectorOffset.plus(4));
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
