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
  Address base;
  int codeSegment;
  int limit;
  final private static int MAX_VECTORS = 256;
  
  public Idt() {
    base = Address.fromIntZeroExtend(0);
    codeSegment = 8;
    limit = MAX_VECTORS*8-1;
  }
  
  /**
   * 
   * @param table Location of IDT table
   * @param size In number of interrupts vectors
   */
  public Idt(Address table, int size, int segment)
  {
	  base = table;
	  // Convert to bytes
	  limit = size*8-1;
	  codeSegment = segment;
  }
  
  public void init() {
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
}
