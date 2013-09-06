//Bytecodes.java, created Fri Jan 11 16:49:00 2002 by joewhaley
//Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
//Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.BytecodeAnalysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Array;
import joeq.Class.jq_Class;
import joeq.Class.jq_ClassFileConstants;
import joeq.Class.jq_ConstantPool;
import joeq.Class.jq_Field;
import joeq.Class.jq_InstanceField;
import joeq.Class.jq_InstanceMethod;
import joeq.Class.jq_LineNumberBC;
import joeq.Class.jq_Member;
import joeq.Class.jq_MemberReference;
import joeq.Class.jq_Method;
import joeq.Class.jq_NameAndDesc;
import joeq.Class.jq_Primitive;
import joeq.Class.jq_Reference;
import joeq.Class.jq_StaticField;
import joeq.Class.jq_TryCatchBC;
import joeq.Class.jq_Type;
import joeq.Runtime.Reflection;
import joeq.UTF.Utf8;
import jwutil.collections.LinearSet;
import jwutil.io.ByteSequence;
import jwutil.strings.Strings;
import jwutil.util.Assert;

/*
* @author  John Whaley <jwhaley@alum.mit.edu>
* @version $Id: Bytecodes.java,v 1.36 2005/04/29 07:39:00 joewhaley Exp $
*/
public interface Bytecodes {
 
 abstract class Instruction implements Cloneable, Serializable {
     protected short length = 1;  // Length of instruction in bytes
     protected short opcode = -1; // Opcode number
     
     /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     Instruction() {}
     
     public Instruction(short opcode, short length) {
         this.length = length;
         this.opcode = opcode;
     }
     
     /**
      * Dump instruction as byte code to stream out.
      * @param out Output stream
      */
     public void dump(DataOutputStream out) throws IOException {
         out.writeByte(opcode); // Common for all instructions
     }
     
     /**
      * Long output format:
      *
      * &lt;name of opcode&gt; "["&lt;opcode number&gt;"]"
      * "("&lt;length of instruction&gt;")"
      *
      * @param verbose long/short format switch
      * @return mnemonic for instruction
      */
     public String toString(boolean verbose) {
         if(verbose)
             return jq_ClassFileConstants.OPCODE_NAMES[opcode] + "[" + opcode + "](" + length + ")";
         else
             return jq_ClassFileConstants.OPCODE_NAMES[opcode];
     }
     
     /**
      * @return mnemonic for instruction in verbose format
      */
     public String toString() {
         return toString(true);
     }
     
     /**
      * Use with caution, since `BranchInstruction's have a `target' reference which
      * is not copied correctly (only basic types are). This also applies for
      * `Select' instructions with their multiple branch targets.
      *
      * @see joeq.Compiler.BytecodeAnalysis.Bytecodes.BranchInstruction
      * @return (shallow) copy of an instruction
      */
     public Instruction copy() {
         Instruction i = null;
         
         // "Constant" instruction, no need to duplicate
         if(InstructionConstants.INSTRUCTIONS[this.getOpcode()] != null)
             i = this;
         else {
             try {
                 i = (Instruction)clone();
             } catch(CloneNotSupportedException e) {
                 System.err.println(e);
             }
         }
         
         return i;
     }
     
     /**
      * Read needed data (e.g. index) from file.
      *
      * @param cp constant pool of class we are reading
      * @param bytes byte sequence to read from
      * @param wide "wide" instruction flag
      */
     protected void initFromFile(jq_ConstantPool cp, ByteSequence bytes, boolean wide) throws IOException {}
     
     /**
      * Read an instruction from (byte code) input stream and return the
      * appropiate object.
      *
      * @param cp constant pool of class we are reading from
      * @param bytes sequence of bytes to read
      * @return instruction object being read
      */
     public static final Instruction readInstruction(jq_ConstantPool cp, ByteSequence bytes) throws IOException {
         boolean     wide   = false;
         short       opcode = (short)bytes.readUnsignedByte();
         Instruction obj    = null;
         
         if(opcode == jq_ClassFileConstants.jbc_WIDE) { // Read next opcode after wide byte
             wide = true;
             opcode  = (short)bytes.readUnsignedByte();
         }
         
         if(InstructionConstants.INSTRUCTIONS[opcode] != null)
             return InstructionConstants.INSTRUCTIONS[opcode]; // Used predefined immutable object, if available
         
         /* Find appropiate class, instantiate an (empty) instruction object
          * and initialize it by hand.
          */
         Class clazz;
         try {
             clazz = Class.forName(className(opcode));
         }
         catch (ClassNotFoundException cnfe){
             // If a class by that name does not exist, the opcode is illegal.
             // Note that IMPDEP1, IMPDEP2, BREAKPOINT are also illegal in a sense.
             throw new BytecodeException("Illegal opcode detected.");
         }
         try {
             obj = (Instruction)clazz.newInstance();
             
             if(wide && !((obj instanceof LocalVariableInstruction) || (obj instanceof IINC) ||
                          (obj instanceof RET)))
                 throw new Exception("Illegal opcode after wide: " + opcode);
             
             obj.setOpcode(opcode);
             obj.initFromFile(cp, bytes, wide); // Do further initializations, if any
             // Byte code offset set in InstructionList
         } catch(Exception e) {
             e.printStackTrace();
             throw new BytecodeException("Error loading "+clazz+"="+obj+": "+e.toString());
         }
         
         return obj;
     }
     
     private static final String className(short opcode) {
         String name = jq_ClassFileConstants.OPCODE_NAMES[opcode].toUpperCase();
         
         /* ICONST_0, etc. will be shortened to ICONST, etc., since ICONST_0 and the like
          * are not implemented (directly).
          */
         try {
             int  len = name.length();
             char ch1 = name.charAt(len - 2), ch2 = name.charAt(len - 1);
             
             if((ch1 == '_') && (ch2 >= '0')  && (ch2 <= '5'))
                 name = name.substring(0, len - 2);
             
             if(name.equals("ICONST_M1")) // Special case
                 name = "ICONST";
         } catch(StringIndexOutOfBoundsException e) { System.err.println(e); }
         
         return "joeq.Compiler.BytecodeAnalysis.Bytecodes$" + name;
     }
     
     /**
      * @return Number of words consumed from stack by this instruction
      */
     public int consumeStack() { return jq_ClassFileConstants.CONSUME_STACK[opcode]; }
     
     /**
      * @return Number of words produced onto stack by this instruction
      */
     public int produceStack() { return jq_ClassFileConstants.PRODUCE_STACK[opcode]; }
     
     /**
      * @return this instructions opcode
      */
     public short getOpcode() { return opcode; }
     
     /**
      * @return length (in bytes) of instruction
      */
     public int getLength()   { return length; }
     
     /**
      * Needed in readInstruction.
      */
     private void setOpcode(short opcode) { this.opcode = opcode; }
     
     /** Some instructions may be reused, so don't do anything by default.
      */
     void dispose() {  }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public abstract void accept(Visitor v);
 }
 
 class InstructionHandle implements Serializable {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3904965256957670706L;
    
    InstructionHandle next, prev;  // Will be set from the outside
     Instruction       instruction;
     protected int     i_position = -1; // byte code offset of instruction
     private Set       targeters;
     private Map       attributes;
     
     public final InstructionHandle getNext()        { return next; }
     public final InstructionHandle getPrev()        { return prev; }
     public final Instruction       getInstruction() { return instruction; }
     
     /**
      * Replace current instruction contained in this handle.
      * Old instruction is disposed using Instruction.dispose().
      */
     public void setInstruction(Instruction i) { // Overridden in BranchHandle
         if(i == null)
             throw new BytecodeException("Assigning null to handle");
         

         //if((this.getClass() != BranchHandle.class) && (i instanceof BranchInstruction))
        if ((!(this instanceof BranchHandle)) && (i instanceof BranchInstruction))
             throw new BytecodeException("Assigning branch instruction " + i + " to plain handle");
         
         if(instruction != null)
             instruction.dispose();
         
         instruction = i;
     }
     
     /**
      * Temporarily swap the current instruction, without disturbing
      * anything. Meant to be used by a debugger, implementing
      * breakpoints. Current instruction is returned.
      */
     public Instruction swapInstruction(Instruction i) {
         Instruction oldInstruction = instruction;
         instruction = i;
         return oldInstruction;
     }
     
     /*private*/ protected InstructionHandle(Instruction i) {
         setInstruction(i);
     }

     private static InstructionHandle ih_list = null; // List of reusable handles

     /** Factory method.
      */
     static final InstructionHandle getInstructionHandle(Instruction i) {
         if(ih_list == null)
             return new InstructionHandle(i);
         else {
             InstructionHandle ih = ih_list;
             ih_list = ih.next;
             
             ih.setInstruction(i);
             
             return ih;
         }
     }

     /**
      * Called by InstructionList.setPositions when setting the position for every
      * instruction. In the presence of variable length instructions `setPositions()'
      * performs multiple passes over the instruction list to calculate the
      * correct (byte) positions and offsets by calling this function.
      *
      * @param offset additional offset caused by preceding (variable length) instructions
      * @param max_offset the maximum offset that may be caused by these instructions
      * @return additional offset caused by possible change of this instruction's length
      */
     protected int updatePosition(int offset, int max_offset) {
         i_position += offset;
         return 0;
     }
     
     /** @return the position, i.e., the byte code offset of the contained
      * instruction. This is accurate only after
      * InstructionList.setPositions() has been called.
      */
     public int getPosition() { return i_position; }

     /** Set the position, i.e., the byte code offset of the contained
      * instruction.
      */
     void setPosition(int pos) { i_position = pos; }
     
     /** Overridden in BranchHandle
      */
     protected void addHandle() {
         next    = ih_list;
         ih_list = this;
     }
     
     /**
      * Delete contents, i.e., remove user access and make handle reusable.
      */
     void dispose() {
         next = prev = null;
         instruction.dispose();
         instruction = null;
         i_position = -1;
         attributes = null;
         removeAllTargeters();
         addHandle();
     }
     
     /** Remove all targeters, if any.
      */
     public void removeAllTargeters() {
         if(targeters != null)
             targeters.clear();
     }
     
     /**
      * Denote this handle isn't referenced anymore by t.
      */
     public void removeTargeter(InstructionTargeter t) {
         targeters.remove(t);
     }
     
     /**
      * Denote this handle is being referenced by t.
      */
     public void addTargeter(InstructionTargeter t) {
         if(targeters == null)
             targeters = new LinearSet();
         
         targeters.add(t);
     }
     
     public boolean hasTargeters() {
         return (targeters != null) && (targeters.size() > 0);
     }
     
     /**
      * @return null, if there are no targeters
      */
     public Set/*<InstructionTargeter>*/ getTargeters() {
         if(!hasTargeters())
             return null;
         return Collections.unmodifiableSet(targeters);
     }
     
     /** @return a (verbose) string representation of the contained instruction.
      */
     public String toString(boolean verbose) {
         return Strings.format(i_position, 4, false, ' ') + ": " + instruction.toString(verbose);
     }
     
     /** @return a string representation of the contained instruction.
      */
     public String toString() {
         return toString(true);
     }
     
     /** Add an attribute to an instruction handle.
      *
      * @param key the key object to store/retrieve the attribute
      * @param attr the attribute to associate with this handle
      */
     public void addAttribute(Object key, Object attr) {
         if(attributes == null)
             attributes = new HashMap(3);
         
         attributes.put(key, attr);
     }
     
     /** Delete an attribute of an instruction handle.
      *
      * @param key the key object to retrieve the attribute
      */
     public void removeAttribute(Object key) {
         if(attributes != null)
             attributes.remove(key);
     }
     
     /** Get attribute of an instruction handle.
      *
      * @param key the key object to store/retrieve the attribute
      */
     public Object getAttribute(Object key) {
         if(attributes != null)
             return attributes.get(key);
         
         return null;
     }
     
     /** Convenience method, simply calls accept() on the contained instruction.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         instruction.accept(v);
     }
 }
 
 final class BranchHandle extends InstructionHandle {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3834870291158283317L;
    
    private BranchInstruction bi; // An alias in fact, but saves lots of casts
     
     private BranchHandle(BranchInstruction i) {
         super(i);
         bi = i;
     }
     
     /** Factory methods.
      */
     private static BranchHandle bh_list = null; // List of reusable handles
     
     static final BranchHandle getBranchHandle(BranchInstruction i) {
         if(bh_list == null)
             return new BranchHandle(i);
         else {
             BranchHandle bh = bh_list;
             bh_list = (BranchHandle)bh.next;
             
             bh.setInstruction(i);
             
             return bh;
         }
     }
     
     /** Handle adds itself to the list of resuable handles.
      */
     protected void addHandle() {
         next    = bh_list;
         bh_list = this;
     }
     
     /* Override InstructionHandle methods: delegate to branch instruction.
      * Through this overriding all access to the private i_position field should
      * be prevented.
      */
     public int getPosition() { return bi.position; }
     
     void setPosition(int pos) {
         i_position = bi.position = pos;
     }
     
     protected int updatePosition(int offset, int max_offset) {
         int x = bi.updatePosition(offset, max_offset);
         i_position = bi.position;
         return x;
     }
     
     /**
      * Pass new target to instruction.
      */
     public void setTarget(InstructionHandle ih) {
         bi.setTarget(ih);
     }
     
     /**
      * Update target of instruction.
      */
     public void updateTarget(InstructionHandle old_ih, InstructionHandle new_ih) {
         bi.updateTarget(old_ih, new_ih);
     }
     
     /**
      * @return target of instruction.
      */
     public InstructionHandle getTarget() {
         return bi.getTarget();
     }
     
     /**
      * Set new contents. Old instruction is disposed and may not be used anymore.
      */
     public void setInstruction(Instruction i) {
         super.setInstruction(i);
         
         if(!(i instanceof BranchInstruction))
             throw new BytecodeException("Assigning " + i + " to branch handle which is not a branch instruction");
         
         bi = (BranchInstruction)i;
     }
 }

 class InstructionList implements Serializable {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3832624001687433272L;
    
    private InstructionHandle start  = null, end = null;
     private int               length = 0; // number of elements in list
     private int[]             byte_positions; // byte code offsets corresponding to instructions
     
     /**
      * Create (empty) instruction list.
      */
     public InstructionList() {}
     
     /**
      * Create instruction list containing one instruction.
      * @param i initial instruction
      */
     public InstructionList(Instruction i) {
         append(i);
     }
     
     /**
      * Create instruction list containing one instruction.
      * @param i initial instruction
      */
     public InstructionList(BranchInstruction i) {
         append(i);
     }
     
     /**
      * Initialize list with (nonnull) compound instruction. Consumes argument
      * list, i.e., it becomes empty.
      *
      * @param c compound instruction (list)
      */
     public InstructionList(CompoundInstruction c) {
         append(c.getInstructionList());
     }
     
     /**
      * Test for empty list.
      */
     public boolean isEmpty() { return start == null; } // && end == null
     
     /**
      * Find the target instruction (handle) that corresponds to the given target
      * position (byte code offset).
      *
      * @param ihs array of instruction handles, i.e. il.getInstructionHandles()
      * @param pos array of positions corresponding to ihs, i.e. il.getInstructionPositions()
      * @param target target position to search for
      * @return target position's instruction handle if available
      */
     public static InstructionHandle findHandle(List/*<InstructionHandle>*/ ihs, int[] pos, int target) {
         int l=0, r = pos.length-1;
         
         /* Do a binary search since the pos array is ordered.
          */
         do {
             int i = (l + r) / 2;
             int j = pos[i];
             
             // ignore "-1" indices.
             while (j == -1 && i >= l && i > 0) {
                 j = pos[--i];
             }
             if (i < l) {
                 i = (l + r) / 2;
                 j = pos[i];
                 while (j == -1 && i <= r && i < pos.length) {
                     j = pos[++i];
                 }
                 if (i > r) return null;
             }
             
             //System.out.println("i="+i+" l="+l+" r="+r+" j="+j+" target="+target+" ihs[i]="+ihs.get(i));
             if (j == target) // target found
                 return (InstructionHandle)ihs.get(i);
             else if (target < j) // else constrain search area
                 r = i - 1;
             else // target > j
                 l = i + 1;
         } while (l <= r);
         
         return null;
     }
     
     /**
      * Get instruction handle for instruction at byte code position pos.
      * This only works properly, if the list is freshly initialized from a byte array or
      * setPositions() has been called before this method.
      *
      * @param pos byte code position to search for
      * @return target position's instruction handle if available
      */
     public InstructionHandle findHandle(int pos) {
         List/*<InstructionHandle>*/ ihs = getInstructionHandles();
         return findHandle(ihs, byte_positions, pos);
     }
     
     public InstructionList(jq_Method m) {
         this(m.getDeclaringClass().getCP(), m.getBytecode());
     }
     
     /**
      * Initialize instruction list from byte array.
      *
      * @param code byte array containing the instructions
      */
     public InstructionList(jq_ConstantPool cp, byte[] code) {
         ByteSequence        bytes = new ByteSequence(code);
         ArrayList/*<InstructionHandle>*/ ihs = new ArrayList(code.length);
         int[]               pos   = new int[code.length]; // Can't be more than that
         int                 count = 0; // Contains actual length
         
         /* Pass 1: Create an object for each byte code and append them
          * to the list.
          */
         try {
             while(bytes.available() > 0) {
                 // Remember byte offset and associate it with the instruction
                 int off =  bytes.getIndex();
                 pos[count] = off;
                 
                 /* Read one instruction from the byte stream, the byte position is set
                  * accordingly.
                  */
                 Instruction       i = Instruction.readInstruction(cp, bytes);
                 InstructionHandle ih;
                 if(i instanceof BranchInstruction) // Use proper append() method
                     ih = append((BranchInstruction)i);
                 else
                     ih = append(i);
                 
                 ih.setPosition(off);
                 ihs.add(ih);
                 //System.out.println("Instruction handle: "+ih);
                 
                 count++;
             }
         } catch(IOException e) { throw new BytecodeException(e.toString()); }
         
         byte_positions = new int[count]; // Trim to proper size
         System.arraycopy(pos, 0, byte_positions, 0, count);
         
         /* Pass 2: Look for BranchInstruction and update their targets, i.e.,
          * convert offsets to instruction handles.
          */
         for(int i=0; i < count; i++) {
             if(ihs.get(i) instanceof BranchHandle) {
                 BranchInstruction bi = (BranchInstruction)((BranchHandle)ihs.get(i)).instruction;
                 int target = bi.position + bi.getIndex(); /* Byte code position:
                                                            * relative -> absolute. */
                 // Search for target position
                 InstructionHandle ih = findHandle(ihs, byte_positions, target);
                 
                 if(ih == null) // Search failed
                     throw new BytecodeException("Couldn't find target "+target+" for branch: " + bi);
                 
                 bi.setTarget(ih); // Update target
                 
                 // If it is a Select instruction, update all branch targets
                 if(bi instanceof Select) { // Either LOOKUPSWITCH or TABLESWITCH
                     Select s       = (Select)bi;
                     int[]  indices = s.getIndices();
                     
                     for(int j=0; j < indices.length; j++) {
                         target = bi.position + indices[j];
                         ih     = findHandle(ihs, byte_positions, target);
                         
                         if(ih == null) // Search failed
                             throw new BytecodeException("Couldn't find target "+target+" for switch: " + bi);
                         
                         s.setTarget(j, ih); // Update target
                     }
                 }
             }
         }
     }
     
     /**
      * Append another list after instruction (handle) ih contained in this list.
      * Consumes argument list, i.e., it becomes empty.
      *
      * @param ih where to append the instruction list
      * @param il Instruction list to append to this one
      * @return instruction handle pointing to the <B>first</B> appended instruction
      */
     public InstructionHandle append(InstructionHandle ih, InstructionList il) {
         if(il == null)
             throw new BytecodeException("Appending null InstructionList");
         
         if(il.isEmpty()) // Nothing to do
             return ih;
         
         InstructionHandle next = ih.next, ret = il.start;
         
         ih.next = il.start;
         il.start.prev = ih;
         
         il.end.next = next;
         
         if(next != null) // i == end ?
             next.prev = il.end;
         else
             end = il.end; // Update end ...
         
         length += il.length; // Update length
         
         il.clear();
         
         return ret;
     }
     
     /**
      * Append another list after instruction i contained in this list.
      * Consumes argument list, i.e., it becomes empty.
      *
      * @param i  where to append the instruction list
      * @param il Instruction list to append to this one
      * @return instruction handle pointing to the <B>first</B> appended instruction
      */
     public InstructionHandle append(Instruction i, InstructionList il) {
         InstructionHandle ih;
         
         if((ih = findInstruction2(i)) == null) // Also applies for empty list
             throw new BytecodeException("Instruction " + i + " is not contained in this list.");
         
         return append(ih, il);
     }
     
     /**
      * Append another list to this one.
      * Consumes argument list, i.e., it becomes empty.
      *
      * @param il list to append to end of this list
      * @return instruction handle of the <B>first</B> appended instruction
      */
     public InstructionHandle append(InstructionList il) {
         if(il == null)
             throw new BytecodeException("Appending null InstructionList");
         
         if(il.isEmpty()) // Nothing to do
             return null;
         
         if(isEmpty()) {
             start  = il.start;
             end    = il.end;
             length = il.length;
             
             il.clear();
             
             return start;
         } else
             return append(end, il);  // was end.instruction
     }
     
     /**
      * Append an instruction to the end of this list.
      *
      * @param ih instruction to append
      */
     private void append(InstructionHandle ih) {
         if(isEmpty()) {
             start = end = ih;
             ih.next = ih.prev = null;
         }
         else {
             end.next = ih;
             ih.prev  = end;
             ih.next  = null;
             end      = ih;
         }
         
         length++; // Update length
     }
     
     /**
      * Append an instruction to the end of this list.
      *
      * @param i instruction to append
      * @return instruction handle of the appended instruction
      */
     public InstructionHandle append(Instruction i) {
         InstructionHandle ih = InstructionHandle.getInstructionHandle(i);
         append(ih);
         
         return ih;
     }
     
     /**
      * Append a branch instruction to the end of this list.
      *
      * @param i branch instruction to append
      * @return branch instruction handle of the appended instruction
      */
     public BranchHandle append(BranchInstruction i) {
         BranchHandle ih = BranchHandle.getBranchHandle(i);
         append(ih);
         
         return ih;
     }
     
     /**
      * Append a single instruction j after another instruction i, which
      * must be in this list of course!
      *
      * @param i Instruction in list
      * @param j Instruction to append after i in list
      * @return instruction handle of the first appended instruction
      */
     public InstructionHandle append(Instruction i, Instruction j) {
         return append(i, new InstructionList(j));
     }
     
     /**
      * Append a compound instruction, after instruction i.
      *
      * @param i Instruction in list
      * @param c The composite instruction (containing an InstructionList)
      * @return instruction handle of the first appended instruction
      */
     public InstructionHandle append(Instruction i, CompoundInstruction c) {
         return append(i, c.getInstructionList());
     }
     
     /**
      * Append a compound instruction.
      *
      * @param c The composite instruction (containing an InstructionList)
      * @return instruction handle of the first appended instruction
      */
     public InstructionHandle append(CompoundInstruction c) {
         return append(c.getInstructionList());
     }
     
     /**
      * Append a compound instruction.
      *
      * @param ih where to append the instruction list
      * @param c The composite instruction (containing an InstructionList)
      * @return instruction handle of the first appended instruction
      */
     public InstructionHandle append(InstructionHandle ih, CompoundInstruction c) {
         return append(ih, c.getInstructionList());
     }
     
     /**
      * Append an instruction after instruction (handle) ih contained in this list.
      *
      * @param ih where to append the instruction list
      * @param i Instruction to append
      * @return instruction handle pointing to the <B>first</B> appended instruction
      */
     public InstructionHandle append(InstructionHandle ih, Instruction i) {
         return append(ih, new InstructionList(i));
     }
     
     /**
      * Append an instruction after instruction (handle) ih contained in this list.
      *
      * @param ih where to append the instruction list
      * @param i Instruction to append
      * @return instruction handle pointing to the <B>first</B> appended instruction
      */
     public BranchHandle append(InstructionHandle ih, BranchInstruction i) {
         BranchHandle    bh = BranchHandle.getBranchHandle(i);
         InstructionList il = new InstructionList();
         il.append(bh);
         
         append(ih, il);
         
         return bh;
     }
     
     /**
      * Insert another list before Instruction handle ih contained in this list.
      * Consumes argument list, i.e., it becomes empty.
      *
      * @param ih where to append the instruction list
      * @param il instruction list to insert
      * @return instruction handle of the first inserted instruction
      */
     public InstructionHandle insert(InstructionHandle ih, InstructionList il) {
         if(il == null)
             throw new BytecodeException("Inserting null InstructionList");
         
         if(il.isEmpty()) // Nothing to do
             return ih;
         
         InstructionHandle prev = ih.prev, ret = il.start;
         
         ih.prev = il.end;
         il.end.next = ih;
         
         il.start.prev = prev;
         
         if(prev != null) // ih == start ?
             prev.next = il.start;
         else
             start = il.start; // Update start ...
         
         length += il.length; // Update length
         
         il.clear();
         
         return ret;
     }
     
     /**
      * Insert another list.
      *
      * @param il list to insert before start of this list
      * @return instruction handle of the first inserted instruction
      */
     public InstructionHandle insert(InstructionList il) {
         if(isEmpty()) {
             append(il); // Code is identical for this case
             return start;
         }
         else
             return insert(start, il);
     }
     
     /**
      * Insert an instruction at start of this list.
      *
      * @param ih instruction to insert
      */
     private void insert(InstructionHandle ih) {
         if(isEmpty()) {
             start = end = ih;
             ih.next = ih.prev = null;
         } else {
             start.prev = ih;
             ih.next    = start;
             ih.prev    = null;
             start      = ih;
         }
         
         length++;
     }
     
     /**
      * Insert another list before Instruction i contained in this list.
      * Consumes argument list, i.e., it becomes empty.
      *
      * @param i  where to append the instruction list
      * @param il Instruction list to insert
      * @return instruction handle pointing to the first inserted instruction,
      * i.e., il.getStart()
      */
     public InstructionHandle insert(Instruction i, InstructionList il) {
         InstructionHandle ih;
         
         if((ih = findInstruction1(i)) == null)
             throw new BytecodeException("Instruction " + i + " is not contained in this list.");
         
         return insert(ih, il);
     }
     
     /**
      * Insert an instruction at start of this list.
      *
      * @param i instruction to insert
      * @return instruction handle of the inserted instruction
      */
     public InstructionHandle insert(Instruction i) {
         InstructionHandle ih = InstructionHandle.getInstructionHandle(i);
         insert(ih);
         
         return ih;
     }
     
     /**
      * Insert a branch instruction at start of this list.
      *
      * @param i branch instruction to insert
      * @return branch instruction handle of the appended instruction
      */
     public BranchHandle insert(BranchInstruction i) {
         BranchHandle ih = BranchHandle.getBranchHandle(i);
         insert(ih);
         return ih;
     }
     
     /**
      * Insert a single instruction j before another instruction i, which
      * must be in this list of course!
      *
      * @param i Instruction in list
      * @param j Instruction to insert before i in list
      * @return instruction handle of the first inserted instruction
      */
     public InstructionHandle insert(Instruction i, Instruction j) {
         return insert(i, new InstructionList(j));
     }
     
     /**
      * Insert a compound instruction before instruction i.
      *
      * @param i Instruction in list
      * @param c The composite instruction (containing an InstructionList)
      * @return instruction handle of the first inserted instruction
      */
     public InstructionHandle insert(Instruction i, CompoundInstruction c) {
         return insert(i, c.getInstructionList());
     }
     
     /**
      * Insert a compound instruction.
      *
      * @param c The composite instruction (containing an InstructionList)
      * @return instruction handle of the first inserted instruction
      */
     public InstructionHandle insert(CompoundInstruction c) {
         return insert(c.getInstructionList());
     }
     
     /**
      * Insert an instruction before instruction (handle) ih contained in this list.
      *
      * @param ih where to insert to the instruction list
      * @param i Instruction to insert
      * @return instruction handle of the first inserted instruction
      */
     public InstructionHandle insert(InstructionHandle ih, Instruction i) {
         return insert(ih, new InstructionList(i));
     }
     
     /**
      * Insert a compound instruction.
      *
      * @param ih where to insert the instruction list
      * @param c The composite instruction (containing an InstructionList)
      * @return instruction handle of the first inserted instruction
      */
     public InstructionHandle insert(InstructionHandle ih, CompoundInstruction c) {
         return insert(ih, c.getInstructionList());
     }
     
     /**
      * Insert an instruction before instruction (handle) ih contained in this list.
      *
      * @param ih where to insert to the instruction list
      * @param i Instruction to insert
      * @return instruction handle of the first inserted instruction
      */
     public BranchHandle insert(InstructionHandle ih, BranchInstruction i) {
         BranchHandle    bh = BranchHandle.getBranchHandle(i);
         InstructionList il = new InstructionList();
         il.append(bh);
         
         insert(ih, il);
         
         return bh;
     }
     
     /**
      * Take all instructions (handles) from "start" to "end" and append them after the
      * new location "target". Of course, "end" must be after "start" and target must
      * not be located withing this range. If you want to move something to the start of
      * the list use null as value for target.<br>
      * Any instruction targeters pointing to handles within the block, keep their targets.
      *
      * @param start  of moved block
      * @param end    of moved block
      * @param target of moved block
      */
     public void move(InstructionHandle start, InstructionHandle end, InstructionHandle target) {
         // Step 1: Check constraints
         
         if((start == null) || (end == null))
             throw new BytecodeException("Invalid null handle: From " + start + " to " + end);
         
         if((target == start) || (target == end))
             throw new BytecodeException("Invalid range: From " + start + " to " + end +
             " contains target " + target);
         
         for(InstructionHandle ih = start; ih != end.next; ih = ih.next) {
             if(ih == null) // At end of list, end not found yet
                 throw new BytecodeException("Invalid range: From " + start + " to " + end);
             else if(ih == target) // target may be null
                 throw new BytecodeException("Invalid range: From " + start + " to " + end +
                 " contains target " + target);
         }
         
         // Step 2: Temporarily remove the given instructions from the list
         
         InstructionHandle prev = start.prev, next = end.next;
         
         if(prev != null)
             prev.next = next;
         else // start == this.start!
             this.start = next;
         
         if(next != null)
             next.prev = prev;
         else // end == this.end!
             this.end = prev;
         
         start.prev = end.next = null;
         
         // Step 3: append after target
         
         if(target == null) { // append to start of list
             end.next = this.start;
             this.start = start;
         } else {
             next = target.next;
             
             target.next = start;
             start.prev  = target;
             end.next    = next;
             
             if(next != null)
                 next.prev = end;
         }
     }
     
     /**
      * Move a single instruction (handle) to a new location.
      *
      * @param ih     moved instruction
      * @param target new location of moved instruction
      */
     public void move(InstructionHandle ih, InstructionHandle target) {
         move(ih, ih, target);
     }
     
     /**
      * Remove from instruction `prev' to instruction `next' both contained
      * in this list. Throws TargetLostException when one of the removed instruction handles
      * is still being targeted.
      *
      * @param prev where to start deleting (predecessor, exclusive)
      * @param next where to end deleting (successor, exclusive)
      */
     private void remove(InstructionHandle prev, InstructionHandle next) throws TargetLostException {
         InstructionHandle first, last; // First and last deleted instruction
         
         if((prev == null) && (next == null)) { // singleton list
             first = last = start;
             start = end = null;
         } else {
             if(prev == null) { // At start of list
                 first = start;
                 start = next;
             } else {
                 first     = prev.next;
                 prev.next = next;
             }
             
             if(next == null) { // At end of list
                 last = end;
                 end  = prev;
             } else {
                 last      = next.prev;
                 next.prev = prev;
             }
         }
         
         first.prev = null; // Completely separated from rest of list
         last.next  = null;
         
         List target_vec = new LinkedList();
         
         for(InstructionHandle ih=first; ih != null; ih = ih.next)
             ih.getInstruction().dispose(); // e.g. BranchInstructions release their targets
         
         StringBuffer buf = new StringBuffer("{ ");
         for(InstructionHandle ih=first; ih != null; ih = next) {
             next = ih.next;
             length--;
             
             if(ih.hasTargeters()) { // Still got targeters?
                 target_vec.add(ih);
                 buf.append(ih.toString(true) + " ");
                 ih.next = ih.prev = null;
             } else
                 ih.dispose();
         }
         
         buf.append("}");
         
         if(!target_vec.isEmpty()) {
             throw new TargetLostException(target_vec, buf.toString());
         }
     }
     
     /**
      * Remove instruction from this list. The corresponding Instruction
      * handles must not be reused!
      *
      * @param ih instruction (handle) to remove
      */
     public void delete(InstructionHandle ih) throws TargetLostException {
         remove(ih.prev, ih.next);
     }
     
     /**
      * Remove instruction from this list. The corresponding Instruction
      * handles must not be reused!
      *
      * @param i instruction to remove
      */
     public void delete(Instruction i) throws TargetLostException {
         InstructionHandle ih;
         
         if((ih = findInstruction1(i)) == null)
             throw new BytecodeException("Instruction " + i +
             " is not contained in this list.");
         delete(ih);
     }
     
     /**
      * Remove instructions from instruction `from' to instruction `to' contained
      * in this list. The user must ensure that `from' is an instruction before
      * `to', or risk havoc. The corresponding Instruction handles must not be reused!
      *
      * @param from where to start deleting (inclusive)
      * @param to   where to end deleting (inclusive)
      */
     public void delete(InstructionHandle from, InstructionHandle to) throws TargetLostException {
         remove(from.prev, to.next);
     }
     
     /**
      * Remove instructions from instruction `from' to instruction `to' contained
      * in this list. The user must ensure that `from' is an instruction before
      * `to', or risk havoc. The corresponding Instruction handles must not be reused!
      *
      * @param from where to start deleting (inclusive)
      * @param to   where to end deleting (inclusive)
      */
     public void delete(Instruction from, Instruction to) throws TargetLostException {
         InstructionHandle from_ih, to_ih;
         
         if((from_ih = findInstruction1(from)) == null)
             throw new BytecodeException("Instruction " + from +
             " is not contained in this list.");
         
         if((to_ih = findInstruction2(to)) == null)
             throw new BytecodeException("Instruction " + to +
             " is not contained in this list.");
         delete(from_ih, to_ih);
     }
     
     /**
      * Search for given Instruction reference, start at beginning of list.
      *
      * @param i instruction to search for
      * @return instruction found on success, null otherwise
      */
     private InstructionHandle findInstruction1(Instruction i) {
         for(InstructionHandle ih=start; ih != null; ih = ih.next)
             if(ih.instruction == i)
                 return ih;
         
         return null;
     }
     
     /**
      * Search for given Instruction reference, start at end of list
      *
      * @param i instruction to search for
      * @return instruction found on success, null otherwise
      */
     private InstructionHandle findInstruction2(Instruction i) {
         for(InstructionHandle ih=end; ih != null; ih = ih.prev)
             if(ih.instruction == i)
                 return ih;
         
         return null;
     }
     
     public boolean contains(InstructionHandle i) {
         if(i == null)
             return false;
         
         for(InstructionHandle ih=start; ih != null; ih = ih.next)
             if(ih == i)
                 return true;
         
         return false;
     }
     
     public boolean contains(Instruction i) {
         return findInstruction1(i) != null;
     }
     
     public void setPositions() {
         setPositions(false);
     }
     
     /**
      * Give all instructions their position number (offset in byte stream), i.e.,
      * make the list ready to be dumped.
      *
      * @param check Perform sanity checks, e.g. if all targeted instructions really belong
      * to this list
      */
     public void setPositions(boolean check) {
         int max_additional_bytes = 0, additional_bytes = 0;
         int index = 0, count = 0;
         int[] pos = new int[length];
         
         /* Pass 0: Sanity checks
          */
         if(check) {
             for(InstructionHandle ih=start; ih != null; ih = ih.next) {
                 Instruction i = ih.instruction;
                 
                 if(i instanceof BranchInstruction) { // target instruction within list?
                     Instruction inst = ((BranchInstruction)i).getTarget().instruction;
                     if(!contains(inst))
                         throw new BytecodeException("Branch target of " +
                         jq_ClassFileConstants.OPCODE_NAMES[i.opcode] + ":" +
                         inst + " not in instruction list");
                     
                     if(i instanceof Select) {
                         List/*<InstructionHandle>*/ targets = ((Select)i).getTargets();
                         
                         for(Iterator j=targets.iterator(); j.hasNext(); ) {
                             inst = (Instruction)j.next();
                             if(!contains(inst))
                                 throw new BytecodeException("Branch target of " +
                                                             jq_ClassFileConstants.OPCODE_NAMES[i.opcode] + ":" +
                                                             inst + " not in instruction list");
                         }
                     }
                     
                     if(!(ih instanceof BranchHandle))
                         throw new BytecodeException("Branch instruction " +
                                                     jq_ClassFileConstants.OPCODE_NAMES[i.opcode] + ":" +
                                                     inst + " not contained in BranchHandle.");
                     
                 }
             }
         }
         
         /* Pass 1: Set position numbers and sum up the maximum number of bytes an
          * instruction may be shifted.
          */
         for(InstructionHandle ih=start; ih != null; ih = ih.next) {
             Instruction i = ih.instruction;
             
             ih.setPosition(index);
             pos[count++] = index;
             
             /* Get an estimate about how many additional bytes may be added, because
              * BranchInstructions may have variable length depending on the target
              * offset (short vs. int) or alignment issues (TABLESWITCH and
              * LOOKUPSWITCH).
              */
             switch(i.getOpcode()) {
                 case jq_ClassFileConstants.jbc_JSR: case jq_ClassFileConstants.jbc_GOTO:
                     max_additional_bytes += 2;
                     break;
                     
                 case jq_ClassFileConstants.jbc_TABLESWITCH: case jq_ClassFileConstants.jbc_LOOKUPSWITCH:
                     max_additional_bytes += 3;
                     break;
             }
             
             index += i.getLength();
         }
         
         /* Pass 2: Expand the variable-length (Branch)Instructions depending on
          * the target offset (short or int) and ensure that branch targets are
          * within this list.
          */
         for(InstructionHandle ih=start; ih != null; ih = ih.next)
             additional_bytes += ih.updatePosition(additional_bytes, max_additional_bytes);
         
         /* Pass 3: Update position numbers (which may have changed due to the
          * preceding expansions), like pass 1.
          */
         index=count=0;
         for(InstructionHandle ih=start; ih != null; ih = ih.next) {
             Instruction i = ih.instruction;
             
             ih.setPosition(index);
             pos[count++] = index;
             index += i.getLength();
         }
         
         byte_positions = new int[count]; // Trim to proper size
         System.arraycopy(pos, 0, byte_positions, 0, count);
     }
     
     /**
      * When everything is finished, use this method to convert the instruction
      * list into an array of bytes.
      *
      * @return the byte code ready to be dumped
      */
     public byte[] getByteCode() {
         // Update position indices of instructions
         setPositions();
         
         ByteArrayOutputStream b   = new ByteArrayOutputStream();
         DataOutputStream      out = new DataOutputStream(b);
         
         try {
             for(InstructionHandle ih=start; ih != null; ih = ih.next) {
                 Instruction i = ih.instruction;
                 i.dump(out); // Traverse list
             }
         } catch(IOException e) {
             System.err.println(e);
             return null;
         }
         
         return b.toByteArray();
     }
     
     /**
      * @return an array of instructions without target information for branch instructions.
      */
     public List/*<Instruction>*/ getInstructions(jq_ConstantPool cp) {
         byte[] bc = getByteCode();
         ByteSequence  bytes        = new ByteSequence(bc);
         ArrayList     instructions = new ArrayList(bc.length);
         
         try {
             while(bytes.available() > 0) {
                 instructions.add(Instruction.readInstruction(cp, bytes));
             }
         } catch(IOException e) { throw new BytecodeException(e.toString()); }
         
         return instructions;
     }
     
     public String toString() {
         return toString(true);
     }
     
     /**
      * @param verbose toggle output format
      * @return String containing all instructions in this list.
      */
     public String toString(boolean verbose) {
         StringBuffer buf = new StringBuffer();
         
         for(InstructionHandle ih=start; ih != null; ih = ih.next) {
             buf.append(ih.toString(verbose) + Strings.lineSep);
         }
         
         return buf.toString();
     }
     
     /**
      * @return Enumeration that lists all instructions (handles)
      */
     public Enumeration elements() {
         return new Enumeration() {
             private InstructionHandle ih = start;
             
             public Object nextElement() {
                 if (ih == null) throw new NoSuchElementException();
                 InstructionHandle i = ih;
                 ih = ih.next;
                 return i;
             }
             
             public boolean hasMoreElements() { return ih != null; }
         };
     }
     
     /**
      * @return Enumeration that lists all instructions (handles)
      */
     public ListIterator iterator() {
         return new ListIterator() {
             private InstructionHandle lastReturned = start;
             private InstructionHandle next = start;
             private int nextIndex = 0;
             
             public boolean hasNext() { return nextIndex != length; }
             public Object next() {
                 if (nextIndex == length) throw new NoSuchElementException();
                 lastReturned = next;
                 next = next.next; ++nextIndex;
                 return lastReturned;
             }
             public boolean hasPrevious() { return nextIndex != 0; }
             public Object previous() {
                 if (nextIndex == 0) throw new NoSuchElementException();
                 if (nextIndex == length) {
                     next = end;
                 } else {
                     next = next.prev;
                 }
                 --nextIndex;
                 return lastReturned = next;
             }
             public int nextIndex() { return nextIndex; }
             public int previousIndex() { return nextIndex-1; }
             public void remove() {
                 if (lastReturned == null) throw new IllegalStateException("remove or add called before remove");
                 if (next == lastReturned) next = lastReturned.next;
                 else --nextIndex;
                 try {
                     delete(lastReturned);
                 } catch (TargetLostException x) {
                     throw new IllegalStateException("Target lost: "+x);
                 }
                 lastReturned = null;
             }
             public void set(Object o) {
                 if (lastReturned == null) throw new IllegalStateException("remove or add called before set");
                 InstructionHandle ih;
                 if (o instanceof BranchInstruction) {
                     ih = insert(lastReturned, (BranchInstruction)o);
                 } else if (o instanceof Instruction) {
                     ih = insert(lastReturned, (Instruction)o);
                 } else {
                     InstructionList il = new InstructionList();
                     il.append((InstructionHandle)o);
                     ih = insert(lastReturned, il);
                 }
                 if (lastReturned == next) next = ih;
                 redirectBranches(lastReturned, ih);
                 // TODO: what about local var, exception handlers?
                 try {
                     delete(lastReturned);
                 } catch (TargetLostException x) {
                     throw new IllegalStateException("Target lost: "+x);
                 }
                 lastReturned = ih;
             }
             public void add(Object o) {
                 if (lastReturned == null) throw new IllegalStateException("remove or add called before add");
                 ++nextIndex;
                 if (o instanceof BranchInstruction) {
                     insert(next, (BranchInstruction)o);
                 } else if (o instanceof Instruction) {
                     insert(next, (Instruction)o);
                 } else {
                     InstructionList il = new InstructionList();
                     il.append((InstructionHandle)o);
                     insert(next, il);
                 }
                 lastReturned = null;
             }
             
         };
     }
     
     /**
      * @return array containing all instructions (handles)
      */
     public List/*<InstructionHandle>*/ getInstructionHandles() {
         ArrayList/*<InstructionHandle>*/ ihs = new ArrayList(length);
         if (byte_positions.length != length)
             byte_positions = new int[length];
         InstructionHandle   ih  = start;
         
         for(int i=0; i < length; i++) {
             ihs.add(ih);
             byte_positions[i] = ih.getPosition();
             ih = ih.next;
         }
         
         return ihs;
     }
     
     /**
      * Get positions (offsets) of all instructions in the list. This relies on that
      * the list has been freshly created from an byte code array, or that setPositions()
      * has been called. Otherwise this may be inaccurate.
      *
      * @return array containing all instruction's offset in byte code
      */
     public int[] getInstructionPositions() { return byte_positions; }
     
     /**
      * @return complete, i.e., deep copy of this list
      */
     public InstructionList copy() {
         HashMap         map = new HashMap();
         InstructionList il  = new InstructionList();
         
         /* Pass 1: Make copies of all instructions, append them to the new list
          * and associate old instruction references with the new ones, i.e.,
          * a 1:1 mapping.
          */
         for(InstructionHandle ih=start; ih != null; ih = ih.next) {
             Instruction i = ih.instruction;
             Instruction c = i.copy(); // Use clone for shallow copy
             
             if(c instanceof BranchInstruction)
                 map.put(ih, il.append((BranchInstruction)c));
             else
                 map.put(ih, il.append(c));
         }
         
         /* Pass 2: Update branch targets.
          */
         InstructionHandle ih=start;
         InstructionHandle ch=il.start;
         
         while(ih != null) {
             Instruction i = ih.instruction;
             Instruction c = ch.instruction;
             
             if(i instanceof BranchInstruction) {
                 BranchInstruction bi      = (BranchInstruction)i;
                 BranchInstruction bc      = (BranchInstruction)c;
                 InstructionHandle itarget = bi.getTarget(); // old target
                 
                 // New target is in hash map
                 bc.setTarget((InstructionHandle)map.get(itarget));
                 
                 if(bi instanceof Select) { // Either LOOKUPSWITCH or TABLESWITCH
                     Select si = (Select)bi;
                     Select sc = (Select)bc;
                     List/*<InstructionHandle>*/ itargets = si.getTargets();
                     
                     int k; Iterator j;
                     for(k=0, j=itargets.iterator(); j.hasNext(); ++k) { // Update all targets
                         sc.setTarget(k, (InstructionHandle)map.get(j.next()));
                     }
                 }
             }
             
             ih = ih.next;
             ch = ch.next;
         }
         
         return il;
     }
     
     private void clear() {
         start = end = null;
         length = 0;
     }
     
     /**
      * Delete contents of list. Provides better memory utilization,
      * because the system then may reuse the instruction handles. This
      * method is typically called right after
      * <href="MethodGen.html#getMethod()">MethodGen.getMethod()</a>.
      */
     public void dispose() {
         // Traverse in reverse order, because ih.next is overwritten
         for(InstructionHandle ih=end; ih != null; ih = ih.prev)
             /* Causes BranchInstructions to release target and targeters, because it
              * calls dispose() on the contained instruction.
              */
             ih.dispose();
         
         clear();
     }
     
     /**
      * @return start of list
      */
     public InstructionHandle getStart() { return start; }
     
     /**
      * @return end of list
      */
     public InstructionHandle getEnd()   { return end; }
     
     /**
      * @return length of list (Number of instructions, not bytes)
      */
     public int getLength() { return length; }
     
     /**
      * @return length of list (Number of instructions, not bytes)
      */
     public int size() { return length; }
     
     /**
      * Redirect all references from old_target to new_target, i.e., update targets
      * of branch instructions.
      *
      * @param old_target the old target instruction handle
      * @param new_target the new target instruction handle
      */
     public void redirectBranches(InstructionHandle old_target, InstructionHandle new_target) {
         for(InstructionHandle ih = start; ih != null; ih = ih.next) {
             Instruction i  = ih.getInstruction();
             
             if(i instanceof BranchInstruction) {
                 BranchInstruction b      = (BranchInstruction)i;
                 InstructionHandle target = b.getTarget();
                 
                 if(target == old_target)
                     b.setTarget(new_target);
                 
                 if(b instanceof Select) { // Either LOOKUPSWITCH or TABLESWITCH
                     List/*<InstructionHandle>*/ targets = ((Select)b).getTargets();
                     Select sb = (Select)b;
                     
                     int k; Iterator j;
                     for(k=0, j=targets.iterator(); j.hasNext(); ++k) // Update targets
                         if(j.next() == old_target)
                             sb.setTarget(k, new_target);
                 }
             }
         }
     }
     
     /**
      * Redirect all references of local variables from old_target to new_target.
      *
      * @param lg array of local variables
      * @param old_target the old target instruction handle
      * @param new_target the new target instruction handle
      */
     /*
     public void redirectLocalVariables(LocalVariableGen[] lg, InstructionHandle old_target, InstructionHandle new_target) {
         for(int i=0; i < lg.length; i++) {
             InstructionHandle start = lg[i].getStart();
             InstructionHandle end   = lg[i].getEnd();
             
             if(start == old_target)
                 lg[i].setStart(new_target);
             
             if(end == old_target)
                 lg[i].setEnd(new_target);
         }
     }
      */
     
     /**
      * Redirect all references of exception handlers from old_target to new_target.
      *
      * @param exceptions array of exception handlers
      * @param old_target the old target instruction handle
      * @param new_target the new target instruction handle
      */
     public void redirectExceptionHandlers(CodeException[] exceptions, InstructionHandle old_target, InstructionHandle new_target) {
         for(int i=0; i < exceptions.length; i++) {
             if(exceptions[i].getStartPC() == old_target)
                 exceptions[i].setStartPC(new_target);
             
             if(exceptions[i].getEndPC() == old_target)
                 exceptions[i].setEndPC(new_target);
             
             if(exceptions[i].getHandlerPC() == old_target)
                 exceptions[i].setHandlerPC(new_target);
         }
     }
     
     private List observers;
     
     /** Add observer for this object.
      */
     public void addObserver(InstructionListObserver o) {
         if(observers == null)
             observers = new LinkedList();
         
         observers.add(o);
     }
     
     /** Remove observer for this object.
      */
     public void removeObserver(InstructionListObserver o) {
         if(observers != null)
             observers.remove(o);
     }
     
     /** Call notify() method on all observers. This method is not called
      * automatically whenever the state has changed, but has to be
      * called by the user after he has finished editing the object.
      */
     public void update() {
         if(observers != null)
             for(Iterator e = observers.iterator(); e.hasNext(); )
                 ((InstructionListObserver)e.next()).notify(this);
     }

     /** Convenience method, simply calls accept() on the contained instructions.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         for (InstructionHandle p = start; p != null; p = p.next) {
             p.accept(v);
         }
     }

 }
 
 final class TargetLostException extends Exception {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3258132461824063281L;
    
    private List/*<InstructionHandle>*/ targets;
     
     TargetLostException(List/*<InstructionHandle>*/ t, String mesg) {
         super(mesg);
         targets = t;
     }
     
     /**
      * @return list of instructions still being targeted.
      */
     public List/*<InstructionHandle>*/ getTargets() { return targets; }
 }

 interface InstructionListObserver {
     void notify(InstructionList list);
 }

 
 
 
 
 
 
 
 
 class ARRAYLENGTH extends Instruction implements ExceptionThrower, StackProducer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3977863973062521397L;

    /**
     * Get length of array
     */
     public ARRAYLENGTH() {
         super(jq_ClassFileConstants.jbc_ARRAYLENGTH, (short)1);
     }
     
     /**
      * @return exceptions this instruction may cause
      */
     public Set/*<jq_Class>*/ getExceptions() {
         //return new jq_Class[] { ClassLib.sun13.java.lang.NullPointerException._class };
         return null;
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitExceptionThrower(this);
         v.visitStackProducer(this);
         v.visitARRAYLENGTH(this);
     }
 }
 
 class AALOAD extends ArrayInstruction implements StackProducer, ExceptionThrower, TypedInstruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3256723991774572857L;

    /**
     * Load reference from array
     */
     public AALOAD() {
         super(jq_ClassFileConstants.jbc_AALOAD);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitStackProducer(this);
         v.visitExceptionThrower(this);
         v.visitTypedInstruction(this);
         v.visitArrayInstruction(this);
         v.visitAALOAD(this);
     }
     
 }
 
 class AASTORE extends ArrayInstruction implements StackConsumer, ExceptionThrower, TypedInstruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 4123103961466286131L;

    /**
     * Store into reference array
     */
     public AASTORE() {
         super(jq_ClassFileConstants.jbc_AASTORE);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitStackConsumer(this);
         v.visitExceptionThrower(this);
         v.visitTypedInstruction(this);
         v.visitArrayInstruction(this);
         v.visitAASTORE(this);
     }
 }
 
 class ACONST_NULL extends Instruction implements StackProducer, PushInstruction, TypedInstruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3258688827559458096L;

    /**
      * Push null reference
      */
     public ACONST_NULL() {
         super(jq_ClassFileConstants.jbc_ACONST_NULL, (short)1);
     }
     
     /** @return jq_NullType.NULL
      */
     public jq_Type getType() {
         return jq_Reference.jq_NullType.NULL_TYPE;
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitStackProducer(this);
         v.visitPushInstruction(this);
         v.visitTypedInstruction(this);
         v.visitACONST_NULL(this);
     }
 }
 
 class ALOAD extends LoadInstruction implements StackProducer, PushInstruction, TypedInstruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3617853079836242227L;

    /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     ALOAD() {
         super(jq_ClassFileConstants.jbc_ALOAD, jq_ClassFileConstants.jbc_ALOAD_0);
     }
     
     /** Load reference from local variable
      * @param n index of local variable
      */
     public ALOAD(int n) {
         super(jq_ClassFileConstants.jbc_ALOAD, jq_ClassFileConstants.jbc_ALOAD_0, n);
     }
     
     /*
     public jq_Type getType() {
         return PrimordialClassLoader.loader.getJavaLangObject();
     }
     */
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         super.accept(v);
         v.visitALOAD(this);
     }
 }
 
 class ANEWARRAY extends CPInstruction implements /*LoadClass,*/ AllocationInstruction, ExceptionThrower, StackProducer, TypedInstruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3616730495593756729L;

    /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     ANEWARRAY() {}
     
     public ANEWARRAY(jq_Array a) {
         super(jq_ClassFileConstants.jbc_ANEWARRAY, a);
     }
     
     public Set/*<jq_Class>*/ getExceptions() {
         /*
         Class[] cs = new Class[1 + ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION.length];
         
         System.arraycopy(ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION, 0,
                          cs, 0, ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION.length);
         cs[ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION.length] = ExceptionConstants.NEGATIVE_ARRAY_SIZE_EXCEPTION;
         return cs;
         */
         return null;
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         //v.visitLoadClass(this);
         v.visitAllocationInstruction(this);
         v.visitExceptionThrower(this);
         v.visitStackProducer(this);
         v.visitTypedInstruction(this);
         v.visitCPInstruction(this);
         v.visitANEWARRAY(this);
     }
     
     /*
     public jq_Class getLoadClassType() {
         jq_Type t = getType(cpg);
         
         if (t instanceof jq_Array){
             t = ((jq_Array) t).getInnermostElementType();
         }
         
         return (t instanceof jq_Class)? (jq_Class) t : null;
     }
      */
 }
 
 class ARETURN extends ReturnInstruction implements TypedInstruction, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 4050204120013355318L;

    /**
      * Return reference from method
      */
     public ARETURN() {
         super(jq_ClassFileConstants.jbc_ARETURN);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         //v.visitExceptionThrower(this);
         v.visitTypedInstruction(this);
         v.visitStackConsumer(this);
         v.visitReturnInstruction(this);
         v.visitARETURN(this);
     }
 }
 
 class ASTORE extends StoreInstruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3906086762995594038L;

    /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     ASTORE() {
         super(jq_ClassFileConstants.jbc_ASTORE, jq_ClassFileConstants.jbc_ASTORE_0);
     }
     
     /** Store reference into local variable
      * @param n index of local variable
      */
     public ASTORE(int n) {
         super(jq_ClassFileConstants.jbc_ASTORE, jq_ClassFileConstants.jbc_ASTORE_0, n);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         super.accept(v);
         v.visitASTORE(this);
     }
 }
 
 class ATHROW extends Instruction implements UnconditionalBranch, ExceptionThrower {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3906926768488265008L;

    /**
      *  Throw exception
      */
     public ATHROW() {
         super(jq_ClassFileConstants.jbc_ATHROW, (short)1);
     }
     
     /** @return exceptions this instruction may cause
      */
     public Set/*<jq_Class>*/ getExceptions() {
         //return new jq_Class[] { PrimordialClassLoader.loader.getJavaLangThrowable() };
         return null;
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitUnconditionalBranch(this);
         v.visitExceptionThrower(this);
         v.visitATHROW(this);
     }
 }
 
 class BALOAD extends ArrayInstruction implements StackProducer, ExceptionThrower, TypedInstruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3835149541292194102L;

    /**
     * Load byte or boolean from array
     */
     public BALOAD() {
         super(jq_ClassFileConstants.jbc_BALOAD);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitStackProducer(this);
         v.visitExceptionThrower(this);
         v.visitTypedInstruction(this);
         v.visitArrayInstruction(this);
         v.visitBALOAD(this);
     }
 }
 
 class BASTORE extends ArrayInstruction implements StackConsumer, ExceptionThrower, TypedInstruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3690192157323311159L;

    /**
     * Store byte or boolean into array
     */
     public BASTORE() {
         super(jq_ClassFileConstants.jbc_BASTORE);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitStackConsumer(this);
         v.visitExceptionThrower(this);
         v.visitTypedInstruction(this);
         v.visitArrayInstruction(this);
         v.visitBASTORE(this);
     }
 }
 
 class BIPUSH extends Instruction implements PushInstruction, StackProducer, TypedInstruction, ConstantPushInstruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3256999947701598520L;
    
    private byte b;
     
     /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     BIPUSH() {}
     
     /** Push byte on stack
      */
     public BIPUSH(byte b) {
         super(jq_ClassFileConstants.jbc_BIPUSH, (short)2);
         this.b = b;
     }
     
     /**
      * Dump instruction as byte code to stream out.
      */
     public void dump(DataOutputStream out) throws IOException {
         super.dump(out);
         out.writeByte(b);
     }
     
     /**
      * @return mnemonic for instruction
      */
     public String toString(boolean verbose) {
         return super.toString(verbose) + " " + b;
     }
     
     /**
      * Read needed data (e.g. index) from file.
      */
     protected void initFromFile(jq_ConstantPool cp, ByteSequence bytes, boolean wide) throws IOException {
         length = 2;
         b      = bytes.readByte();
     }
     
     public Number getValue() { return new Integer(b); }
     
     /** @return jq_Primitive.BYTE
      */
     public jq_Type getType() {
         return jq_Primitive.BYTE;
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitPushInstruction(this);
         v.visitStackProducer(this);
         v.visitTypedInstruction(this);
         v.visitConstantPushInstruction(this);
         v.visitBIPUSH(this);
     }
 }
 
 class BREAKPOINT extends Instruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3544676178515406900L;

    public BREAKPOINT() {
         super(jq_ClassFileConstants.jbc_BREAKPOINT, (short)1);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitBREAKPOINT(this);
     }
 }
 
 class CALOAD extends ArrayInstruction implements StackProducer, ExceptionThrower, TypedInstruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3258134643684161592L;

    /**
     * Load char from array
     */
     public CALOAD() {
         super(jq_ClassFileConstants.jbc_CALOAD);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitStackProducer(this);
         v.visitExceptionThrower(this);
         v.visitTypedInstruction(this);
         v.visitArrayInstruction(this);
         v.visitCALOAD(this);
     }
 }
 
 class CASTORE extends ArrayInstruction implements StackConsumer, ExceptionThrower, TypedInstruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3979272425999776057L;

    /**
     * Store char into array
     */
     public CASTORE() {
         super(jq_ClassFileConstants.jbc_CASTORE);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitStackConsumer(this);
         v.visitExceptionThrower(this);
         v.visitTypedInstruction(this);
         v.visitArrayInstruction(this);
         v.visitCASTORE(this);
     }
 }
 
 class CHECKCAST extends CPInstruction implements LoadClass, ExceptionThrower, StackProducer, StackConsumer, TypedInstruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3834306250300471602L;

    /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     CHECKCAST() {}
     
     /** Check whether object is of given type
      * @param f type to check
      */
     public CHECKCAST(jq_Type f) {
         super(jq_ClassFileConstants.jbc_CHECKCAST, f);
     }
     
     /** @return exceptions this instruction may cause
      */
     public Set/*<jq_Class>*/ getExceptions() {
         /*
         jq_Class[] cs = new Class[1 + ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION.length];
         
         System.arraycopy(ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION, 0,
                          cs, 0, ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION.length);
         cs[ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION.length] =
             ExceptionConstants.CLASS_CAST_EXCEPTION;
         return cs;
         */
         return null;
     }
     
     public jq_Class getLoadClassType() {
         jq_Type t = getType();
         
         if(t instanceof jq_Array)
             t = ((jq_Array) t).getInnermostElementType();
         
         return (t instanceof jq_Class) ? (jq_Class) t : null;
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitLoadClass(this);
         v.visitExceptionThrower(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitTypedInstruction(this);
         v.visitCPInstruction(this);
         v.visitCHECKCAST(this);
     }
 }
 
 class D2F extends ConversionInstruction implements TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3545521707369838132L;

    /**
     * Convert double to float
     */
     public D2F() {
         super(jq_ClassFileConstants.jbc_D2F);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitConversionInstruction(this);
         v.visitD2F(this);
     }
 }
 
 class D2I extends ConversionInstruction implements TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3257282517928064057L;

    /**
     * Convert double to int
     */
     public D2I() {
         super(jq_ClassFileConstants.jbc_D2I);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitConversionInstruction(this);
         v.visitD2I(this);
     }
 }
 
 class D2L extends ConversionInstruction implements TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3688508792102858802L;

    /**
     * Convert double to long
     */
     public D2L() {
         super(jq_ClassFileConstants.jbc_D2L);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitConversionInstruction(this);
         v.visitD2L(this);
     }
 }
 
 class DADD extends ArithmeticInstruction implements TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3979270244139218744L;

    /**
     * Add doubles
     */
     public DADD() {
         super(jq_ClassFileConstants.jbc_DADD);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitArithmeticInstruction(this);
         v.visitDADD(this);
     }
 }
 
 class DALOAD extends ArrayInstruction implements StackProducer, ExceptionThrower, TypedInstruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3761124950999709239L;

    /**
     * Load double from array
     */
     public DALOAD() {
         super(jq_ClassFileConstants.jbc_DALOAD);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitStackProducer(this);
         v.visitExceptionThrower(this);
         v.visitTypedInstruction(this);
         v.visitArrayInstruction(this);
         v.visitDALOAD(this);
     }
 }
 
 class DASTORE extends ArrayInstruction implements StackConsumer, ExceptionThrower, TypedInstruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3618977897344414514L;

    /**
     * Store double into array
     */
     public DASTORE() {
         super(jq_ClassFileConstants.jbc_DASTORE);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitStackConsumer(this);
         v.visitExceptionThrower(this);
         v.visitTypedInstruction(this);
         v.visitArrayInstruction(this);
         v.visitDASTORE(this);
     }
 }
 
 class DCMPG extends Instruction implements TypedInstruction, StackProducer, StackConsumer {
     
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3257848775023013941L;

    public DCMPG() {
         super(jq_ClassFileConstants.jbc_DCMPG, (short)1);
     }
     
     /** @return jq_Primitive.DOUBLE
      */
     public jq_Type getType() {
         return jq_Primitive.DOUBLE;
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitDCMPG(this);
     }
 }
 
 class DCMPL extends Instruction implements TypedInstruction, StackProducer, StackConsumer {
     
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3258135760375984179L;

    public DCMPL() {
         super(jq_ClassFileConstants.jbc_DCMPL, (short)1);
     }
     
     /** @return jq_Primitive.DOUBLE
      */
     public jq_Type getType() {
         return jq_Primitive.DOUBLE;
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitDCMPL(this);
     }
 }
 
 class DCONST extends Instruction implements PushInstruction, StackProducer, TypedInstruction, ConstantPushInstruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3905242312396387385L;
    private double value;
     
     /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     DCONST() {}
     
     public DCONST(double f) {
         super(jq_ClassFileConstants.jbc_DCONST_0, (short)1);
         
         if(f == 0.0)
             opcode = jq_ClassFileConstants.jbc_DCONST_0;
         else if(f == 1.0)
             opcode = jq_ClassFileConstants.jbc_DCONST_1;
         else
             throw new BytecodeException("DCONST can be used only for 0.0 and 1.0: " + f);
         
         value = f;
     }
     
     public Number getValue() { return new Double(value); }
     
     /** @return jq_Primitive.DOUBLE
      */
     public jq_Type getType() {
         return jq_Primitive.DOUBLE;
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitPushInstruction(this);
         v.visitStackProducer(this);
         v.visitTypedInstruction(this);
         v.visitConstantPushInstruction(this);
         v.visitDCONST(this);
     }
 }
 
 class DDIV extends ArithmeticInstruction implements TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3618699703773377592L;

    /**
     * Divide doubles
     */
     public DDIV() {
         super(jq_ClassFileConstants.jbc_DDIV);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitArithmeticInstruction(this);
         v.visitDDIV(this);
     }
 }
 
 class DLOAD extends LoadInstruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3544673992393895987L;

    /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     DLOAD() {
         super(jq_ClassFileConstants.jbc_DLOAD, jq_ClassFileConstants.jbc_DLOAD_0);
     }
     
     /** Load double from local variable
      * @param n index of local variable
      */
     public DLOAD(int n) {
         super(jq_ClassFileConstants.jbc_DLOAD, jq_ClassFileConstants.jbc_DLOAD_0, n);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         super.accept(v);
         v.visitDLOAD(this);
     }
 }
 
 class DMUL extends ArithmeticInstruction implements TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3689915058885047096L;

    /**
     * Multiply doubles
     */
     public DMUL() {
         super(jq_ClassFileConstants.jbc_DMUL);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitArithmeticInstruction(this);
         v.visitDMUL(this);
     }
 }
 
 class DNEG extends ArithmeticInstruction implements TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3256719585154447409L;

    public DNEG() {
         super(jq_ClassFileConstants.jbc_DNEG);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitArithmeticInstruction(this);
         v.visitDNEG(this);
     }
 }
 
 class DREM extends ArithmeticInstruction implements TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3257562893443873076L;

    /**
     * Remainder of doubles
     */
     public DREM() {
         super(jq_ClassFileConstants.jbc_DREM);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitArithmeticInstruction(this);
         v.visitDREM(this);
     }
 }
 
 class DRETURN extends ReturnInstruction implements TypedInstruction, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3618696392235758390L;

    /**
     * Return double from method
     */
     public DRETURN() {
         super(jq_ClassFileConstants.jbc_DRETURN);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         //v.visitExceptionThrower(this);
         v.visitTypedInstruction(this);
         v.visitStackConsumer(this);
         v.visitReturnInstruction(this);
         v.visitDRETURN(this);
     }
 }
 
 class DSTORE extends StoreInstruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3616446787167335478L;

    /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     DSTORE() {
         super(jq_ClassFileConstants.jbc_DSTORE, jq_ClassFileConstants.jbc_DSTORE_0);
     }
     
     /** Store double into local variable
      * @param n index of local variable
      */
     public DSTORE(int n) {
         super(jq_ClassFileConstants.jbc_DSTORE, jq_ClassFileConstants.jbc_DSTORE_0, n);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         super.accept(v);
         v.visitDSTORE(this);
     }
 }
 
 class DSUB extends ArithmeticInstruction implements TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization
     */
    private static final long serialVersionUID = 3978420312960547126L;

    /**
     * Substract doubles
     */
     public DSUB() {
         super(jq_ClassFileConstants.jbc_DSUB);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitArithmeticInstruction(this);
         v.visitDSUB(this);
     }
 }
 
 class DUP2 extends StackInstruction implements StackProducer, PushInstruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3257849900220757556L;

    public DUP2() {
         super(jq_ClassFileConstants.jbc_DUP2);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitStackProducer(this);
         v.visitPushInstruction(this);
         v.visitStackInstruction(this);
         v.visitDUP2(this);
     }
 }
 
 class DUP2_X1 extends StackInstruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3256442525387534902L;

    public DUP2_X1() {
         super(jq_ClassFileConstants.jbc_DUP2_X1);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitStackInstruction(this);
         v.visitDUP2_X1(this);
     }
 }
 
 class DUP2_X2 extends StackInstruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3691038772737225011L;

    public DUP2_X2() {
         super(jq_ClassFileConstants.jbc_DUP2_X2);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitStackInstruction(this);
         v.visitDUP2_X2(this);
     }
 }
 
 class DUP extends StackInstruction implements StackProducer, PushInstruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3257571698076234552L;

    public DUP() {
         super(jq_ClassFileConstants.jbc_DUP);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitStackProducer(this);
         v.visitPushInstruction(this);
         v.visitStackInstruction(this);
         v.visitDUP(this);
     }
 }
 
 class DUP_X1 extends StackInstruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3258135760375986488L;

    public DUP_X1() {
         super(jq_ClassFileConstants.jbc_DUP_X1);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitStackInstruction(this);
         v.visitDUP_X1(this);
     }
 }
 
 class DUP_X2 extends StackInstruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3256720693155868723L;

    public DUP_X2() {
         super(jq_ClassFileConstants.jbc_DUP_X2);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitStackInstruction(this);
         v.visitDUP_X2(this);
     }
 }
 
 class F2D extends ConversionInstruction implements TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3257853198772484147L;

    /**
     * Convert float to double
     */
     public F2D() {
         super(jq_ClassFileConstants.jbc_F2D);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitConversionInstruction(this);
         v.visitF2D(this);
     }
 }
 
 class F2I extends ConversionInstruction implements TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3832617404583917363L;

    /**
     * Convert float to int
     */
     public F2I() {
         super(jq_ClassFileConstants.jbc_F2I);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitConversionInstruction(this);
         v.visitF2I(this);
     }
 }
 
 class F2L extends ConversionInstruction implements TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3256445793790406707L;

    /**
     * Convert float to long
     */
     public F2L() {
         super(jq_ClassFileConstants.jbc_F2L);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitConversionInstruction(this);
         v.visitF2L(this);
     }
 }
 
 class FADD extends ArithmeticInstruction implements TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3256438110195104052L;

    /**
     * Add floats
     */
     public FADD() {
         super(jq_ClassFileConstants.jbc_FADD);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitArithmeticInstruction(this);
         v.visitFADD(this);
     }
 }
 
 class FALOAD extends ArrayInstruction implements StackProducer, ExceptionThrower, TypedInstruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3688789167651958840L;

    /**
     * Load float from array
     */
     public FALOAD() {
         super(jq_ClassFileConstants.jbc_FALOAD);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitStackProducer(this);
         v.visitExceptionThrower(this);
         v.visitTypedInstruction(this);
         v.visitArrayInstruction(this);
         v.visitFALOAD(this);
     }
 }
 
 class FASTORE extends ArrayInstruction implements StackConsumer, ExceptionThrower, TypedInstruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3833747672556188983L;

    /**
     * Store float into array
     */
     public FASTORE() {
         super(jq_ClassFileConstants.jbc_FASTORE);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitStackConsumer(this);
         v.visitExceptionThrower(this);
         v.visitTypedInstruction(this);
         v.visitArrayInstruction(this);
         v.visitFASTORE(this);
     }
 }
 
 class FCMPG extends Instruction implements TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3618699716641043510L;

    public FCMPG() {
         super(jq_ClassFileConstants.jbc_FCMPG, (short)1);
     }
     
     /** @return jq_Primitive.FLOAT
      */
     public jq_Type getType() {
         return jq_Primitive.FLOAT;
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitFCMPG(this);
     }
 }
 
 class FCMPL extends Instruction implements TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3761688983301795896L;

    public FCMPL() {
         super(jq_ClassFileConstants.jbc_FCMPL, (short)1);
     }
     
     /** @return jq_Primitive.FLOAT
      */
     public jq_Type getType() {
         return jq_Primitive.FLOAT;
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitFCMPL(this);
     }
 }
 
 class FCONST extends Instruction implements PushInstruction, StackProducer, TypedInstruction, ConstantPushInstruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3258411750780383544L;
    
    private float value;
     
     /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     FCONST() {}
     
     public FCONST(float f) {
         super(jq_ClassFileConstants.jbc_FCONST_0, (short)1);
         
         if(f == 0.0)
             opcode = jq_ClassFileConstants.jbc_FCONST_0;
         else if(f == 1.0)
             opcode = jq_ClassFileConstants.jbc_FCONST_1;
         else if(f == 2.0)
             opcode = jq_ClassFileConstants.jbc_FCONST_2;
         else
             throw new BytecodeException("FCONST can be used only for 0.0, 1.0 and 2.0: " + f);
         
         value = f;
     }
     
     public Number getValue() { return new Float(value); }
     
     /** @return jq_Primitive.FLOAT
      */
     public jq_Type getType() {
         return jq_Primitive.FLOAT;
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitPushInstruction(this);
         v.visitStackProducer(this);
         v.visitTypedInstruction(this);
         v.visitConstantPushInstruction(this);
         v.visitFCONST(this);
     }
 }
 
 class FDIV extends ArithmeticInstruction implements TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3617295618767272240L;

    /**
     * Divide floats
     */
     public FDIV() {
         super(jq_ClassFileConstants.jbc_FDIV);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitArithmeticInstruction(this);
         v.visitFDIV(this);
     }
 }
 
 class FLOAD extends LoadInstruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3256440317707497522L;

    /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     FLOAD() {
         super(jq_ClassFileConstants.jbc_FLOAD, jq_ClassFileConstants.jbc_FLOAD_0);
     }
     
     /**
      * Load float from local variable
      * @param n index of local variable
      */
     public FLOAD(int n) {
         super(jq_ClassFileConstants.jbc_FLOAD, jq_ClassFileConstants.jbc_FLOAD_0, n);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         super.accept(v);
         v.visitFLOAD(this);
     }
 }
 
 class FMUL extends ArithmeticInstruction implements TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3256727298848797488L;

    /**
     * Multiply floats
     */
     public FMUL() {
         super(jq_ClassFileConstants.jbc_FMUL);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitArithmeticInstruction(this);
         v.visitFMUL(this);
     }
 }
 
 class FNEG extends ArithmeticInstruction implements TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3544387015597831478L;

    public FNEG() {
         super(jq_ClassFileConstants.jbc_FNEG);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitArithmeticInstruction(this);
         v.visitFNEG(this);
     }
 }
 
 class FREM extends ArithmeticInstruction implements TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3257850986897749561L;

    /**
     * Remainder of floats
     */
     public FREM() {
         super(jq_ClassFileConstants.jbc_FREM);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitArithmeticInstruction(this);
         v.visitFREM(this);
     }
 }
 
 class FRETURN extends ReturnInstruction implements TypedInstruction, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3257286933171484213L;

    /**
     * Return float from method
     */
     public FRETURN() {
         super(jq_ClassFileConstants.jbc_FRETURN);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         //v.visitExceptionThrower(this);
         v.visitTypedInstruction(this);
         v.visitStackConsumer(this);
         v.visitReturnInstruction(this);
         v.visitFRETURN(this);
     }
 }
 
 class FSTORE extends StoreInstruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3833748763461432884L;

    /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     FSTORE() {
         super(jq_ClassFileConstants.jbc_FSTORE, jq_ClassFileConstants.jbc_FSTORE_0);
     }
     
     /** Store float into local variable
      * @param n index of local variable
      */
     public FSTORE(int n) {
         super(jq_ClassFileConstants.jbc_FSTORE, jq_ClassFileConstants.jbc_FSTORE_0, n);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         super.accept(v);
         v.visitFSTORE(this);
     }
 }
 
 class FSUB extends ArithmeticInstruction implements TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3256725069727019317L;

    /** Substract floats
      */
     public FSUB() {
         super(jq_ClassFileConstants.jbc_FSUB);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitArithmeticInstruction(this);
         v.visitFSUB(this);
     }
 }
 
 class GETFIELD extends FieldInstruction implements ExceptionThrower, StackConsumer, StackProducer, TypedInstruction, LoadClass {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3256442503862761017L;

    /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     GETFIELD() {}
     
     public GETFIELD(jq_InstanceField f) {
         super(jq_ClassFileConstants.jbc_GETFIELD, f);
     }
     
     public int produceStack() { return getFieldSize(); }
     
     /**
      * Read needed data (i.e., index) from file.
      * @param bytes input stream
      * @param wide wide prefix?
      */
     protected void initFromFile(jq_ConstantPool cp, ByteSequence bytes, boolean wide) throws IOException {
         o = cp.getAsInstanceField((char)bytes.readUnsignedShort());
         length = 3;
     }
     
     public Set/*<jq_Class>*/ getExceptions() {
         /*
         Class[] cs = new Class[2 + ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION.length];
         
         System.arraycopy(ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION, 0,
         cs, 0, ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION.length);
         
         cs[ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION.length+1] =
         ExceptionConstants.INCOMPATIBLE_CLASS_CHANGE_ERROR;
         cs[ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION.length] =
         ExceptionConstants.NULL_POINTER_EXCEPTION;
         return cs;
         */
         return null;
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitExceptionThrower(this);
         v.visitStackConsumer(this);
         v.visitStackProducer(this);
         v.visitTypedInstruction(this);
         v.visitLoadClass(this);
         v.visitCPInstruction(this);
         v.visitFieldOrMethod(this);
         v.visitFieldInstruction(this);
         v.visitGETFIELD(this);
     }
 }
 
 class GETSTATIC extends FieldInstruction implements ExceptionThrower, StackProducer, PushInstruction, TypedInstruction, LoadClass {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3258128076763510581L;

    /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     GETSTATIC() {}
     
     public GETSTATIC(jq_StaticField f) {
         super(jq_ClassFileConstants.jbc_GETSTATIC, f);
     }
     
     public int produceStack() { return getFieldSize(); }
     
     /**
      * Read needed data (i.e., index) from file.
      * @param bytes input stream
      * @param wide wide prefix?
      */
     protected void initFromFile(jq_ConstantPool cp, ByteSequence bytes, boolean wide) throws IOException {
         o = cp.getAsStaticField((char)bytes.readUnsignedShort());
         length = 3;
     }
     
     public Set/*<jq_Class>*/ getExceptions() {
         /*
         Class[] cs = new Class[1 + ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION.length];
         
         System.arraycopy(ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION, 0,
         cs, 0, ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION.length);
         cs[ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION.length] =
         ExceptionConstants.INCOMPATIBLE_CLASS_CHANGE_ERROR;
         
         return cs;
         */
         return null;
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitExceptionThrower(this);
         v.visitStackProducer(this);
         v.visitPushInstruction(this);
         v.visitTypedInstruction(this);
         v.visitLoadClass(this);
         v.visitCPInstruction(this);
         v.visitFieldOrMethod(this);
         v.visitFieldInstruction(this);
         v.visitGETSTATIC(this);
     }
 }
 
 class GOTO extends GotoInstruction implements VariableLengthInstruction, UnconditionalBranch {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3256440322019242036L;

    /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     GOTO() {}
     
     public GOTO(InstructionHandle target) {
         super(jq_ClassFileConstants.jbc_GOTO, target);
     }
     
     /**
      * Dump instruction as byte code to stream out.
      * @param out Output stream
      */
     public void dump(DataOutputStream out) throws IOException {
         index = getTargetOffset();
         if(opcode == jq_ClassFileConstants.jbc_GOTO)
             super.dump(out);
         else { // GOTO_W
             index = getTargetOffset();
             out.writeByte(opcode);
             out.writeInt(index);
         }
     }
     
     /** Called in pass 2 of InstructionList.setPositions() in order to update
      * the branch target, that may shift due to variable length instructions.
      */
     protected int updatePosition(int offset, int max_offset) {
         int i = getTargetOffset(); // Depending on old position value
         
         position += offset; // Position may be shifted by preceding expansions
         
         if(Math.abs(i) >= (32767 - max_offset)) { // to large for short (estimate)
             opcode = jq_ClassFileConstants.jbc_GOTO_W;
             length = 5;
             return 2; // 5 - 3
         }
         
         return 0;
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitVariableLengthInstruction(this);
         v.visitUnconditionalBranch(this);
         v.visitBranchInstruction(this);
         v.visitGotoInstruction(this);
         v.visitGOTO(this);
     }
 }
 
 class GOTO_W extends GotoInstruction implements UnconditionalBranch {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3689912864307884082L;

    /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     GOTO_W() {}
     
     public GOTO_W(InstructionHandle target) {
         super(jq_ClassFileConstants.jbc_GOTO_W, target);
         length = 5;
     }
     
     /**
      * Dump instruction as byte code to stream out.
      * @param out Output stream
      */
     public void dump(DataOutputStream out) throws IOException {
         index = getTargetOffset();
         out.writeByte(opcode);
         out.writeInt(index);
     }
     
     /**
      * Read needed data (e.g. index) from file.
      */
     protected void initFromFile(jq_ConstantPool cp, ByteSequence bytes, boolean wide) throws IOException {
         index  = bytes.readInt();
         length = 5;
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitUnconditionalBranch(this);
         v.visitBranchInstruction(this);
         v.visitGotoInstruction(this);
         v.visitGOTO_W(this);
     }
 }
 
 class I2B extends ConversionInstruction implements TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3257572788930885170L;

    /**
     * Convert int to byte
     */
     public I2B() {
         super(jq_ClassFileConstants.jbc_I2B);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitConversionInstruction(this);
         v.visitI2B(this);
     }
 }
 
 class I2C extends ConversionInstruction implements TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3257009838977331257L;

    /**
     * Convert int to char
     */
     public I2C() {
         super(jq_ClassFileConstants.jbc_I2C);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitConversionInstruction(this);
         v.visitI2C(this);
     }
 }
 
 class I2D extends ConversionInstruction implements TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3258409538788340793L;

    /**
     * Convert int to double
     */
     public I2D() {
         super(jq_ClassFileConstants.jbc_I2D);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitConversionInstruction(this);
         v.visitI2D(this);
     }
 }
 
 class I2F extends ConversionInstruction implements TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3256720667536732721L;

    /**
     * Convert int to float
     */
     public I2F() {
         super(jq_ClassFileConstants.jbc_I2F);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitConversionInstruction(this);
         v.visitI2F(this);
     }
 }
 
 class I2L extends ConversionInstruction implements TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3688789184748009521L;

    /**
     * Convert int to long
     */
     public I2L() {
         super(jq_ClassFileConstants.jbc_I2L);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitConversionInstruction(this);
         v.visitI2L(this);
     }
 }
 
 class I2S extends ConversionInstruction implements TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3617573790998017588L;

    public I2S() {
         super(jq_ClassFileConstants.jbc_I2S);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitConversionInstruction(this);
         v.visitI2S(this);
     }
 }
 
 class IADD extends ArithmeticInstruction implements TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 4049919380911567408L;

    /** Add ints
      */
     public IADD() {
         super(jq_ClassFileConstants.jbc_IADD);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitArithmeticInstruction(this);
         v.visitIADD(this);
     }
 }
 
 class IALOAD extends ArrayInstruction implements StackProducer, ExceptionThrower, TypedInstruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3977866184903242032L;

    /**
      * Load int from array
      */
     public IALOAD() {
         super(jq_ClassFileConstants.jbc_IALOAD);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitStackProducer(this);
         v.visitExceptionThrower(this);
         v.visitTypedInstruction(this);
         v.visitArrayInstruction(this);
         v.visitIALOAD(this);
     }
 }
 
 class IAND extends ArithmeticInstruction implements TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3762250838071850548L;

    public IAND() {
         super(jq_ClassFileConstants.jbc_IAND);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitArithmeticInstruction(this);
         v.visitIAND(this);
     }
 }
 
 class IASTORE extends ArrayInstruction implements StackConsumer, ExceptionThrower, TypedInstruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 4049070549395256631L;

    /**
      * Store into int array
      */
     public IASTORE() {
         super(jq_ClassFileConstants.jbc_IASTORE);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitStackConsumer(this);
         v.visitExceptionThrower(this);
         v.visitTypedInstruction(this);
         v.visitArrayInstruction(this);
         v.visitIASTORE(this);
     }
 }
 
 class ICONST extends Instruction implements PushInstruction, StackProducer, TypedInstruction, ConstantPushInstruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3760844566994892595L;
    
    private int value;
     
     /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     ICONST() {}
     
     public ICONST(int i) {
         super(jq_ClassFileConstants.jbc_ICONST_0, (short)1);
         
         if((i >= -1) && (i <= 5))
             opcode = (short)(jq_ClassFileConstants.jbc_ICONST_0 + i); // Even works for i == -1
         else
             throw new BytecodeException("ICONST can be used only for value between -1 and 5: " +
             i);
         value = i;
     }
     
     public Number getValue() { return new Integer(value); }
     
     /** @return jq_Primitive.INT
      */
     public jq_Type getType() {
         return jq_Primitive.INT;
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitPushInstruction(this);
         v.visitStackProducer(this);
         v.visitTypedInstruction(this);
         v.visitConstantPushInstruction(this);
         v.visitICONST(this);
     }
 }
 
 class IDIV extends ArithmeticInstruction implements ExceptionThrower, TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3258411746401007415L;

    /**
     * Divide ints
     */
     public IDIV() {
         super(jq_ClassFileConstants.jbc_IDIV);
     }
     
     /** @return exceptions this instruction may cause
      */
     public Set/*<jq_Class>*/ getExceptions() {
         //return new jq_Class[] { ClassLib.sun13.java.lang.DivideByZeroException._class };
         return null;
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitExceptionThrower(this);
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitArithmeticInstruction(this);
         v.visitIDIV(this);
     }
 }
 
 class IF_ACMPEQ extends IfInstruction implements StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3257281431267718709L;

    /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     IF_ACMPEQ() {}
     
     public IF_ACMPEQ(InstructionHandle target) {
         super(jq_ClassFileConstants.jbc_IF_ACMPEQ, target);
     }
     
     /**
      * @return negation of instruction
      */
     public IfInstruction negate() {
         return new IF_ACMPNE(target);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitStackConsumer(this);
         v.visitBranchInstruction(this);
         v.visitIfInstruction(this);
         v.visitIF_ACMPEQ(this);
     }
 }
 
 class IF_ACMPNE extends IfInstruction implements StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3256718502722222130L;

    /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     IF_ACMPNE() {}
     
     public IF_ACMPNE(InstructionHandle target) {
         super(jq_ClassFileConstants.jbc_IF_ACMPNE, target);
     }
     
     /**
      * @return negation of instruction
      */
     public IfInstruction negate() {
         return new IF_ACMPEQ(target);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitStackConsumer(this);
         v.visitBranchInstruction(this);
         v.visitIfInstruction(this);
         v.visitIF_ACMPNE(this);
     }
 }
 
 class IFEQ extends IfInstruction implements StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3906090070137058610L;

    /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     IFEQ() {}
     
     public IFEQ(InstructionHandle target) {
         super(jq_ClassFileConstants.jbc_IFEQ, target);
     }
     
     /**
      * @return negation of instruction, e.g. IFEQ.negate() == IFNE
      */
     public IfInstruction negate() {
         return new IFNE(target);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitStackConsumer(this);
         v.visitBranchInstruction(this);
         v.visitIfInstruction(this);
         v.visitIFEQ(this);
     }
 }
 
 class IFGE extends IfInstruction implements StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3258407335536703282L;

    /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     IFGE() {}
     
     public IFGE(InstructionHandle target) {
         super(jq_ClassFileConstants.jbc_IFGE, target);
     }
     
     /**
      * @return negation of instruction
      */
     public IfInstruction negate() {
         return new IFLT(target);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitStackConsumer(this);
         v.visitBranchInstruction(this);
         v.visitIfInstruction(this);
         v.visitIFGE(this);
     }
 }
 
 class IFGT extends IfInstruction implements StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3545520624970642740L;

    /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     IFGT() {}
     
     public IFGT(InstructionHandle target) {
         super(jq_ClassFileConstants.jbc_IFGT, target);
     }
     
     /**
      * @return negation of instruction
      */
     public IfInstruction negate() {
         return new IFLE(target);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitStackConsumer(this);
         v.visitBranchInstruction(this);
         v.visitIfInstruction(this);
         v.visitIFGT(this);
     }
 }
 
 class IF_ICMPEQ extends IfInstruction implements StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3257007652906349880L;

    /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     IF_ICMPEQ() {}
     
     public IF_ICMPEQ(InstructionHandle target) {
         super(jq_ClassFileConstants.jbc_IF_ICMPEQ, target);
     }
     
     /**
      * @return negation of instruction
      */
     public IfInstruction negate() {
         return new IF_ICMPNE(target);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitStackConsumer(this);
         v.visitBranchInstruction(this);
         v.visitIfInstruction(this);
         v.visitIF_ICMPEQ(this);
     }
 }
 
 class IF_ICMPGE extends IfInstruction implements StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3834586591372325176L;

    /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     IF_ICMPGE() {}
     
     public IF_ICMPGE(InstructionHandle target) {
         super(jq_ClassFileConstants.jbc_IF_ICMPGE, target);
     }
     
     /**
      * @return negation of instruction
      */
     public IfInstruction negate() {
         return new IF_ICMPLT(target);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitStackConsumer(this);
         v.visitBranchInstruction(this);
         v.visitIfInstruction(this);
         v.visitIF_ICMPGE(this);
     }
 }
 
 class IF_ICMPGT extends IfInstruction implements StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 4051322353537726518L;

    /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     IF_ICMPGT() {}
     
     public IF_ICMPGT(InstructionHandle target) {
         super(jq_ClassFileConstants.jbc_IF_ICMPGT, target);
     }
     
     /**
      * @return negation of instruction
      */
     public IfInstruction negate() {
         return new IF_ICMPLE(target);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitStackConsumer(this);
         v.visitBranchInstruction(this);
         v.visitIfInstruction(this);
         v.visitIF_ICMPGT(this);
     }
 }
 
 class IF_ICMPLE extends IfInstruction implements StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3257854268169008433L;

    /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     IF_ICMPLE() {}
     
     public IF_ICMPLE(InstructionHandle target) {
         super(jq_ClassFileConstants.jbc_IF_ICMPLE, target);
     }
     
     /**
      * @return negation of instruction
      */
     public IfInstruction negate() {
         return new IF_ICMPGT(target);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitStackConsumer(this);
         v.visitBranchInstruction(this);
         v.visitIfInstruction(this);
         v.visitIF_ICMPLE(this);
     }
 }
 
 class IF_ICMPLT extends IfInstruction implements StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3256718481348309561L;

    /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     IF_ICMPLT() {}
     
     public IF_ICMPLT(InstructionHandle target) {
         super(jq_ClassFileConstants.jbc_IF_ICMPLT, target);
     }
     
     /**
      * @return negation of instruction
      */
     public IfInstruction negate() {
         return new IF_ICMPGE(target);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitStackConsumer(this);
         v.visitBranchInstruction(this);
         v.visitIfInstruction(this);
         v.visitIF_ICMPLT(this);
     }
 }
 
 class IF_ICMPNE extends IfInstruction implements StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3258131340904641584L;

    /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     IF_ICMPNE() {}
     
     public IF_ICMPNE(InstructionHandle target) {
         super(jq_ClassFileConstants.jbc_IF_ICMPNE, target);
     }
     
     /**
      * @return negation of instruction
      */
     public IfInstruction negate() {
         return new IF_ICMPEQ(target);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitStackConsumer(this);
         v.visitBranchInstruction(this);
         v.visitIfInstruction(this);
         v.visitIF_ICMPNE(this);
     }
 }
 
 class IFLE extends IfInstruction implements StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3545230323870021686L;

    /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     IFLE() {}
     
     public IFLE(InstructionHandle target) {
         super(jq_ClassFileConstants.jbc_IFLE, target);
     }
     
     /**
      * @return negation of instruction
      */
     public IfInstruction negate() {
         return new IFGT(target);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitStackConsumer(this);
         v.visitBranchInstruction(this);
         v.visitIfInstruction(this);
         v.visitIFLE(this);
     }
 }
 
 class IFLT extends IfInstruction implements StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3689345542060913718L;

    /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     IFLT() {}
     
     public IFLT(InstructionHandle target) {
         super(jq_ClassFileConstants.jbc_IFLT, target);
     }
     
     /**
      * @return negation of instruction
      */
     public IfInstruction negate() {
         return new IFGE(target);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitStackConsumer(this);
         v.visitBranchInstruction(this);
         v.visitIfInstruction(this);
         v.visitIFLT(this);
     }
 }
 
 class IFNE extends IfInstruction implements StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3977582476510311735L;

    /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     IFNE() {}
     
     public IFNE(InstructionHandle target) {
         super(jq_ClassFileConstants.jbc_IFNE, target);
     }
     
     /**
      * @return negation of instruction
      */
     public IfInstruction negate() {
         return new IFEQ(target);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitStackConsumer(this);
         v.visitBranchInstruction(this);
         v.visitIfInstruction(this);
         v.visitIFNE(this);
     }
 }
 
 class IFNONNULL extends IfInstruction implements StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3257853194460608562L;

    /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     IFNONNULL() {}
     
     public IFNONNULL(InstructionHandle target) {
         super(jq_ClassFileConstants.jbc_IFNONNULL, target);
     }
     
     /**
      * @return negation of instruction
      */
     public IfInstruction negate() {
         return new IFNULL(target);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitStackConsumer(this);
         v.visitBranchInstruction(this);
         v.visitIfInstruction(this);
         v.visitIFNONNULL(this);
     }
 }
 
 class IFNULL extends IfInstruction implements StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3977298824035382320L;

    /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     IFNULL() {}
     
     public IFNULL(InstructionHandle target) {
         super(jq_ClassFileConstants.jbc_IFNULL, target);
     }
     
     /**
      * @return negation of instruction
      */
     public IfInstruction negate() {
         return new IFNONNULL(target);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitStackConsumer(this);
         v.visitBranchInstruction(this);
         v.visitIfInstruction(this);
         v.visitIFNULL(this);
     }
 }
 
 class IINC extends LocalVariableInstruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3256441395794751798L;
    
    private boolean wide;
     private int     c;
     
     /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     IINC() {}
     
     public IINC(int n, int c) {
         super(); // Default behaviour of LocalVariableInstruction causes error
         
         this.opcode = jq_ClassFileConstants.jbc_IINC;
         this.length = (short)3;
         
         setIndex(n);    // May set wide as side effect
         setIncrement(c);
     }
     
     /**
      * Dump instruction as byte code to stream out.
      * @param out Output stream
      */
     public void dump(DataOutputStream out) throws IOException {
         if(wide) // Need WIDE prefix ?
             out.writeByte(jq_ClassFileConstants.jbc_WIDE);
         
         out.writeByte(opcode);
         
         if(wide) {
             out.writeShort(n);
             out.writeShort(c);
         } else {
             out.writeByte(n);
             out.writeByte(c);
         }
     }
     
     private final void setWide() {
         wide = (n > Short.MAX_VALUE) ||
                (Math.abs(c) > Byte.MAX_VALUE);
         if (wide)
             length = 6; // wide byte included
         else
             length = 3;
     }
     
     /**
      * Read needed data (e.g. index) from file.
      */
     protected void initFromFile(jq_ConstantPool cp, ByteSequence bytes, boolean wide) throws IOException {
         this.wide = wide;
         
         if(wide) {
             length = 6;
             n = bytes.readUnsignedShort();
             c = bytes.readShort();
         } else {
             length = 3;
             n = bytes.readUnsignedByte();
             c = bytes.readByte();
         }
     }
     
     /**
      * @return mnemonic for instruction
      */
     public String toString(boolean verbose) {
         return super.toString(verbose) + " " + c;
     }
     
     /**
      * Set index of local variable.
      */
     public final void setIndex(int n) {
         if(n < 0)
             throw new BytecodeException("Negative index value: " + n);
         
         this.n = n;
         setWide();
     }
     
     /**
      * @return increment factor
      */
     public final int getIncrement() { return c; }
     
     /**
      * Set increment factor.
      */
     public final void setIncrement(int c) {
         this.c = c;
         setWide();
     }
     
     /** @return jq_Primitive.INT
      */
     /*
     public jq_Type getType() {
         return jq_Primitive.INT;
     }
     */
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitLocalVariableInstruction(this);
         v.visitIINC(this);
     }
 }
 
 class ILOAD extends LoadInstruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3257570607070983990L;

    /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     ILOAD() {
         super(jq_ClassFileConstants.jbc_ILOAD, jq_ClassFileConstants.jbc_ILOAD_0);
     }
     
     /** Load int from local variable
      * @param n index of local variable
      */
     public ILOAD(int n) {
         super(jq_ClassFileConstants.jbc_ILOAD, jq_ClassFileConstants.jbc_ILOAD_0, n);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         super.accept(v);
         v.visitILOAD(this);
     }
 }
 
 class IMUL extends ArithmeticInstruction implements TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 4049078241681617461L;

    /**
     * Multiply ints
     */
     public IMUL() {
         super(jq_ClassFileConstants.jbc_IMUL);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitArithmeticInstruction(this);
         v.visitIMUL(this);
     }
 }
 
 class INEG extends ArithmeticInstruction implements TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3258409530181301552L;

    public INEG() {
         super(jq_ClassFileConstants.jbc_INEG);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitArithmeticInstruction(this);
         v.visitINEG(this);
     }
 }
 
 class INSTANCEOF extends CPInstruction implements LoadClass, ExceptionThrower, StackProducer, StackConsumer, TypedInstruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 4121976949213574449L;

    /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     INSTANCEOF() {}
     
     public INSTANCEOF(jq_Type f) {
         super(jq_ClassFileConstants.jbc_INSTANCEOF, f);
     }
     
     public Set/*<jq_Class>*/ getExceptions() {
         /*
         return de.fub.bytecode.ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION;
         */
         return null;
     }
     
     public jq_Class getLoadClassType() {
         jq_Type t = getType();
         
         if(t instanceof jq_Array)
             t = ((jq_Array) t).getInnermostElementType();
         
         return (t instanceof jq_Class)? (jq_Class) t : null;
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitLoadClass(this);
         v.visitExceptionThrower(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitTypedInstruction(this);
         v.visitCPInstruction(this);
         v.visitINSTANCEOF(this);
     }
 }
 
 final class INVOKEINTERFACE extends InvokeInstruction implements ExceptionThrower, TypedInstruction, StackConsumer, StackProducer, LoadClass {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 4049633499382493239L;
    
    private int nargs; // Number of arguments on stack (number of stack slots), called "count" in vmspec2
     
     /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     INVOKEINTERFACE() {}
     
     public INVOKEINTERFACE(jq_Method f, int nargs) {
         super(jq_ClassFileConstants.jbc_INVOKEINTERFACE, f);
         length = 5;
         
         if(nargs < 1)
             throw new BytecodeException("Number of arguments must be > 0 " + nargs);
         
         this.nargs = nargs;
     }
     
     /**
      * Dump instruction as byte code to stream out.
      * @param out Output stream
      */
     public void dump(DataOutputStream out) throws IOException {
         out.writeByte(opcode);
         out.writeShort(index);
         out.writeByte(nargs);
         out.writeByte(0);
     }
     
     /**
      * The Java Virtual Machine Specification, First Edition was a little
      * bit unprecise about the naming. In the Java Virtual Machine Specification,
      * Second Edition, the value returned here is called &quot;count&quot;.
      *
      * @deprecated Use getCount().
      */
     public int getNoArguments() { return nargs; }
     
     /**
      * The <B>count</B> argument according to the Java Language Specification,
      * Second Edition.
      */
     public int getCount() { return nargs; }
     
     /**
      * Read needed data (i.e., index) from file.
      * @param bytes input stream
      * @param wide wide prefix?
      */
     protected void initFromFile(jq_ConstantPool cp, ByteSequence bytes, boolean wide) throws IOException {
         o = cp.getAsInstanceMethod((char)bytes.readUnsignedShort());
         length = 5;
         nargs = bytes.readUnsignedByte();
         bytes.readByte(); // Skip 0 byte
     }
     
     /**
      * @return mnemonic for instruction with symbolic references resolved
      */
     public String toString() {
         return super.toString() + " " + nargs;
     }
     
     public int consumeStack() { // nargs is given in byte-code
         return nargs;  // nargs includes this reference
     }
     
     public Set/*<jq_Class>*/ getExceptions() {
         /*
         Class[] cs = new Class[4 + ExceptionConstants.EXCS_INTERFACE_METHOD_RESOLUTION.length];
         
         System.arraycopy(ExceptionConstants.EXCS_INTERFACE_METHOD_RESOLUTION, 0,
                          cs, 0, ExceptionConstants.EXCS_INTERFACE_METHOD_RESOLUTION.length);
         
         cs[ExceptionConstants.EXCS_INTERFACE_METHOD_RESOLUTION.length+3] = ExceptionConstants.INCOMPATIBLE_CLASS_CHANGE_ERROR;
         cs[ExceptionConstants.EXCS_INTERFACE_METHOD_RESOLUTION.length+2] = ExceptionConstants.ILLEGAL_ACCESS_ERROR;
         cs[ExceptionConstants.EXCS_INTERFACE_METHOD_RESOLUTION.length+1] = ExceptionConstants.ABSTRACT_METHOD_ERROR;
         cs[ExceptionConstants.EXCS_INTERFACE_METHOD_RESOLUTION.length]   = ExceptionConstants.UNSATISFIED_LINK_ERROR;
         
         return cs;
         */
         return null;
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitExceptionThrower(this);
         v.visitTypedInstruction(this);
         v.visitStackConsumer(this);
         v.visitStackProducer(this);
         v.visitLoadClass(this);
         v.visitCPInstruction(this);
         v.visitFieldOrMethod(this);
         v.visitInvokeInstruction(this);
         v.visitINVOKEINTERFACE(this);
     }
 }
 
 class INVOKESPECIAL extends InvokeInstruction implements ExceptionThrower, TypedInstruction, StackConsumer, StackProducer, LoadClass {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3617011949079310389L;

    /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     INVOKESPECIAL() {}
     
     public INVOKESPECIAL(jq_Method f) {
         super(jq_ClassFileConstants.jbc_INVOKESPECIAL, f);
     }
     
     /**
      * Read needed data (i.e., index) from file.
      * @param bytes input stream
      * @param wide wide prefix?
      */
     protected void initFromFile(jq_ConstantPool cp, ByteSequence bytes, boolean wide) throws IOException {
         o = cp.getAsInstanceMethod((char)bytes.readUnsignedShort());
         length = 3;
     }
     
     public Set/*<jq_Class>*/ getExceptions() {
         /*
         Class[] cs = new Class[4 + ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION.length];
         
         System.arraycopy(ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION, 0,
         cs, 0, ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION.length);
         
         cs[ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION.length+3] = ExceptionConstants.UNSATISFIED_LINK_ERROR;
         cs[ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION.length+2] = ExceptionConstants.ABSTRACT_METHOD_ERROR;
         cs[ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION.length+1] = ExceptionConstants.INCOMPATIBLE_CLASS_CHANGE_ERROR;
         cs[ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION.length]   = ExceptionConstants.NULL_POINTER_EXCEPTION;
         
         return cs;
         */
         return null;
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitExceptionThrower(this);
         v.visitTypedInstruction(this);
         v.visitStackConsumer(this);
         v.visitStackProducer(this);
         v.visitLoadClass(this);
         v.visitCPInstruction(this);
         v.visitFieldOrMethod(this);
         v.visitInvokeInstruction(this);
         v.visitINVOKESPECIAL(this);
     }
 }
 
 class INVOKESTATIC extends InvokeInstruction implements ExceptionThrower, TypedInstruction, StackConsumer, StackProducer, LoadClass {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3258689918497927219L;

    /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     INVOKESTATIC() {}
     
     public INVOKESTATIC(jq_Method f) {
         super(jq_ClassFileConstants.jbc_INVOKESTATIC, f);
     }
     
     /**
      * Read needed data (i.e., index) from file.
      * @param bytes input stream
      * @param wide wide prefix?
      */
     protected void initFromFile(jq_ConstantPool cp, ByteSequence bytes, boolean wide) throws IOException {
         o = cp.getAsStaticMethod((char)bytes.readUnsignedShort());
         length = 3;
     }
     
     public Set/*<jq_Class>*/ getExceptions() {
         /*
         Class[] cs = new Class[2 + ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION.length];
         
         System.arraycopy(ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION, 0,
         cs, 0, ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION.length);
         
         cs[ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION.length] = ExceptionConstants.UNSATISFIED_LINK_ERROR;
         cs[ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION.length+1] = ExceptionConstants.INCOMPATIBLE_CLASS_CHANGE_ERROR;
         
         return cs;
         */
         return null;
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitExceptionThrower(this);
         v.visitTypedInstruction(this);
         v.visitStackConsumer(this);
         v.visitStackProducer(this);
         v.visitLoadClass(this);
         v.visitCPInstruction(this);
         v.visitFieldOrMethod(this);
         v.visitInvokeInstruction(this);
         v.visitINVOKESTATIC(this);
     }
 }
 
 class INVOKEVIRTUAL extends InvokeInstruction implements ExceptionThrower, TypedInstruction, StackConsumer, StackProducer, LoadClass {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3761971579332080945L;

    /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     INVOKEVIRTUAL() {}
     
     public INVOKEVIRTUAL(jq_Method f) {
         super(jq_ClassFileConstants.jbc_INVOKEVIRTUAL, f);
     }
     
     /**
      * Read needed data (i.e., index) from file.
      * @param bytes input stream
      * @param wide wide prefix?
      */
     protected void initFromFile(jq_ConstantPool cp, ByteSequence bytes, boolean wide) throws IOException {
         o = cp.getAsInstanceMethod((char)bytes.readUnsignedShort());
         length = 3;
     }
     
     public Set/*<jq_Class>*/ getExceptions() {
         /*
         Class[] cs = new Class[4 + ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION.length];
         
         System.arraycopy(ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION, 0,
         cs, 0, ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION.length);
         
         cs[ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION.length+3] = ExceptionConstants.UNSATISFIED_LINK_ERROR;
         cs[ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION.length+2] = ExceptionConstants.ABSTRACT_METHOD_ERROR;
         cs[ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION.length+1] = ExceptionConstants.INCOMPATIBLE_CLASS_CHANGE_ERROR;
         cs[ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION.length]   = ExceptionConstants.NULL_POINTER_EXCEPTION;
         
         return cs;
         */
         return null;
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitExceptionThrower(this);
         v.visitTypedInstruction(this);
         v.visitStackConsumer(this);
         v.visitStackProducer(this);
         v.visitLoadClass(this);
         v.visitCPInstruction(this);
         v.visitFieldOrMethod(this);
         v.visitInvokeInstruction(this);
         v.visitINVOKEVIRTUAL(this);
     }
 }
 
 class IOR extends ArithmeticInstruction implements TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3256441387170805305L;

    public IOR() {
         super(jq_ClassFileConstants.jbc_IOR);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitArithmeticInstruction(this);
         v.visitIOR(this);
     }
 }
 
 class IREM extends ArithmeticInstruction implements ExceptionThrower, TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3256446919122303286L;

    /**
     * Remainder of ints
     */
     public IREM() {
         super(jq_ClassFileConstants.jbc_IREM);
     }
     
     /** @return exceptions this instruction may cause
      */
     public Set/*<jq_Class>*/ getExceptions() {
         /*
         return new Class[] { de.fub.bytecode.ExceptionConstants.ARITHMETIC_EXCEPTION };
         */
         return null;
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitExceptionThrower(this);
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitArithmeticInstruction(this);
         v.visitIREM(this);
     }
 }
 
 class IRETURN extends ReturnInstruction implements TypedInstruction, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3256723974544044343L;

    /**
     * Return int from method
     */
     public IRETURN() {
         super(jq_ClassFileConstants.jbc_IRETURN);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackConsumer(this);
         v.visitReturnInstruction(this);
         v.visitIRETURN(this);
     }
 }
 
 class ISHL extends ArithmeticInstruction implements TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3257562919129396537L;

    public ISHL() {
         super(jq_ClassFileConstants.jbc_ISHL);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitArithmeticInstruction(this);
         v.visitISHL(this);
     }
 }
 
 class ISHR extends ArithmeticInstruction implements TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3258408426374902576L;

    public ISHR() {
         super(jq_ClassFileConstants.jbc_ISHR);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitArithmeticInstruction(this);
         v.visitISHR(this);
     }
 }
 
 class ISTORE extends StoreInstruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3258410651234612016L;

    /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     ISTORE() {
         super(jq_ClassFileConstants.jbc_ISTORE, jq_ClassFileConstants.jbc_ISTORE_0);
     }
     
     /** Store int into local variable
      * @param n index of local variable
      */
     public ISTORE(int n) {
         super(jq_ClassFileConstants.jbc_ISTORE, jq_ClassFileConstants.jbc_ISTORE_0, n);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         super.accept(v);
         v.visitISTORE(this);
     }
 }
 
 class ISUB extends ArithmeticInstruction implements TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3256440309167632692L;

    /**
     * Substract ints
     */
     public ISUB() {
         super(jq_ClassFileConstants.jbc_ISUB);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitArithmeticInstruction(this);
         v.visitISUB(this);
     }
 }
 
 class IUSHR extends ArithmeticInstruction implements TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3546923584611301680L;

    public IUSHR() {
         super(jq_ClassFileConstants.jbc_IUSHR);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitArithmeticInstruction(this);
         v.visitIUSHR(this);
     }
 }
 
 class IXOR extends ArithmeticInstruction implements TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 4121136935214724919L;

    public IXOR() {
         super(jq_ClassFileConstants.jbc_IXOR);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitArithmeticInstruction(this);
         v.visitIXOR(this);
     }
 }
 
 class JSR extends JsrInstruction implements StackProducer, VariableLengthInstruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 4048789048665518649L;

    /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     JSR() {}
     
     public JSR(InstructionHandle target) {
         super(jq_ClassFileConstants.jbc_JSR, target);
     }
     
     /**
      * Dump instruction as byte code to stream out.
      * @param out Output stream
      */
     public void dump(DataOutputStream out) throws IOException {
         index = getTargetOffset();
         if(opcode == jq_ClassFileConstants.jbc_JSR)
             super.dump(out);
         else { // JSR_W
             index = getTargetOffset();
             out.writeByte(opcode);
             out.writeInt(index);
         }
     }
     
     protected int updatePosition(int offset, int max_offset) {
         int i = getTargetOffset(); // Depending on old position value
         
         position += offset; // Position may be shifted by preceding expansions
         
         if(Math.abs(i) >= (32767 - max_offset)) { // to large for short (estimate)
             opcode  = jq_ClassFileConstants.jbc_JSR_W;
             length = 5;
             return 2; // 5 - 3
         }
         
         return 0;
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitStackProducer(this);
         v.visitVariableLengthInstruction(this);
         v.visitBranchInstruction(this);
         v.visitJsrInstruction(this);
         v.visitJSR(this);
     }
 }
 
 class JSR_W extends JsrInstruction implements StackProducer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 4050486720355185968L;

    /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     JSR_W() {}
     
     public JSR_W(InstructionHandle target) {
         super(jq_ClassFileConstants.jbc_JSR_W, target);
         length = 5;
     }
     
     /**
      * Dump instruction as byte code to stream out.
      * @param out Output stream
      */
     public void dump(DataOutputStream out) throws IOException {
         index = getTargetOffset();
         out.writeByte(opcode);
         out.writeInt(index);
     }
     
     /**
      * Read needed data (e.g. index) from file.
      */
     protected void initFromFile(jq_ConstantPool cp, ByteSequence bytes, boolean wide) throws IOException {
         index = bytes.readInt();
         length = 5;
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitStackProducer(this);
         v.visitBranchInstruction(this);
         v.visitJsrInstruction(this);
         v.visitJSR_W(this);
     }
 }
 
 class L2D extends ConversionInstruction implements TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3257291331100227385L;

    public L2D() {
         super(jq_ClassFileConstants.jbc_L2D);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitConversionInstruction(this);
         v.visitL2D(this);
     }
 }
 
 class L2F extends ConversionInstruction implements TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3834028043743409968L;

    public L2F() {
         super(jq_ClassFileConstants.jbc_L2F);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitConversionInstruction(this);
         v.visitL2F(this);
     }
 }
 
 class L2I extends ConversionInstruction implements TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3257283613178344503L;

    public L2I() {
         super(jq_ClassFileConstants.jbc_L2I);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitConversionInstruction(this);
         v.visitL2I(this);
     }
 }
 
 class LADD extends ArithmeticInstruction implements TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3257003241907434544L;

    public LADD() {
         super(jq_ClassFileConstants.jbc_LADD);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitArithmeticInstruction(this);
         v.visitLADD(this);
     }
 }
 
 class LALOAD extends ArrayInstruction implements StackProducer, ExceptionThrower, TypedInstruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3977016232367109688L;

    /**
     * Load long from array
     */
     public LALOAD() {
         super(jq_ClassFileConstants.jbc_LALOAD);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitStackProducer(this);
         v.visitExceptionThrower(this);
         v.visitTypedInstruction(this);
         v.visitArrayInstruction(this);
         v.visitLALOAD(this);
     }
 }
 
 class LAND extends ArithmeticInstruction implements TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3257007670069639480L;

    public LAND() {
         super(jq_ClassFileConstants.jbc_LAND);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitArithmeticInstruction(this);
         v.visitLAND(this);
     }
 }
 
 class LASTORE extends ArrayInstruction implements StackConsumer, ExceptionThrower, TypedInstruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3258417235319403575L;

    /**
     * Store long into array
     */
     public LASTORE() {
         super(jq_ClassFileConstants.jbc_LASTORE);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitStackConsumer(this);
         v.visitExceptionThrower(this);
         v.visitTypedInstruction(this);
         v.visitArrayInstruction(this);
         v.visitLASTORE(this);
     }
 }
 
 class LCMP extends Instruction implements TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3544389218915923254L;

    public LCMP() {
         super(jq_ClassFileConstants.jbc_LCMP, (short)1);
     }

     /** @return jq_Primitive.LONG
      */
     public jq_Type getType() {
         return jq_Primitive.LONG;
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitLCMP(this);
     }
 }
 
 class LCONST extends Instruction implements PushInstruction, StackProducer, TypedInstruction, ConstantPushInstruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 4050760472946882360L;
    
    private long value;
     
     /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     LCONST() {}
     
     public LCONST(long l) {
         super(jq_ClassFileConstants.jbc_LCONST_0, (short)1);
         
         if(l == 0)
             opcode = jq_ClassFileConstants.jbc_LCONST_0;
         else if(l == 1)
             opcode = jq_ClassFileConstants.jbc_LCONST_1;
         else
             throw new BytecodeException("LCONST can be used only for 0 and 1: " + l);
         
         value = l;
     }
     
     public Number getValue() { return new Long(value); }
     
     /** @return jq_Primitive.LONG
      */
     public jq_Type getType() {
         return jq_Primitive.LONG;
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitPushInstruction(this);
         v.visitStackProducer(this);
         v.visitTypedInstruction(this);
         v.visitConstantPushInstruction(this);
         v.visitLCONST(this);
     }
 }
 
 class LDC2_W extends CPInstruction implements StackProducer, PushInstruction, TypedInstruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3618978966757980470L;

    /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     LDC2_W() {}
     
     public LDC2_W(Object o) {
         super(jq_ClassFileConstants.jbc_LDC2_W, o);
     }
     
     public jq_Type getType() {
         if (o instanceof Long) return jq_Primitive.LONG;
         if (o instanceof Double) return jq_Primitive.DOUBLE;
         throw new RuntimeException("Unknown constant type " + o.getClass());
     }
     
     public Number getValue() {
         return (Number)getObject();
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitStackProducer(this);
         v.visitPushInstruction(this);
         v.visitTypedInstruction(this);
         v.visitCPInstruction(this);
         v.visitLDC2_W(this);
     }
 }
 
 class LDC extends CPInstruction implements PushInstruction, ExceptionThrower, TypedInstruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3832907645656183091L;

    /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     LDC() {}
     
     public LDC(Object o) {
         super(jq_ClassFileConstants.jbc_LDC_W, o);
         setSize();
     }
     
     // Adjust to proper size
     protected final void setSize() {
         if(index <= Byte.MAX_VALUE) { // Fits in one byte?
             opcode = jq_ClassFileConstants.jbc_LDC;
             length = 2;
         } else {
             opcode = jq_ClassFileConstants.jbc_LDC_W;
             length = 3;
         }
     }
     
     /**
      * Dump instruction as byte code to stream out.
      * @param out Output stream
      */
     public void dump(DataOutputStream out) throws IOException {
         out.writeByte(opcode);
         
         if(length == 2)
             out.writeByte(index);
         else // Applies for LDC_W
             out.writeShort(index);
     }
     
     /**
      * Set the index to constant pool and adjust size.
      */
     public final void setIndex(jq_ConstantPool.ConstantPoolRebuilder cpr) {
         super.setIndex(cpr);
         setSize();
     }
     
     /**
      * Read needed data (e.g. index) from file.
      */
     protected void initFromFile(jq_ConstantPool cp, ByteSequence bytes, boolean wide) throws IOException {
         char c = (char) bytes.readUnsignedByte();
         o = cp.get(c);
         if (cp.getTag(c) == (char) jq_ClassFileConstants.CONSTANT_ResolvedClass)
             o = Reflection.getJDKType((jq_Type) o);
         else 
             o = cp.get(c);
         length = 2;
     }
     
     public Object getValue() {
         return getObject();
     }
     
     public jq_Type getType() {
         if (o instanceof String) return PrimordialClassLoader.getJavaLangString();
         if (o instanceof Float) return jq_Primitive.FLOAT;
         if (o instanceof Integer) return jq_Primitive.INT;
         throw new RuntimeException("Unknown or invalid constant type "+o.getClass()+" at "+index);
     }
     
     public Set/*<jq_Class>*/ getExceptions() {
         /*
         return de.fub.bytecode.ExceptionConstants.EXCS_STRING_RESOLUTION;
         */
         return null;
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitStackProducer(this);
         v.visitPushInstruction(this);
         v.visitExceptionThrower(this);
         v.visitTypedInstruction(this);
         v.visitCPInstruction(this);
         v.visitLDC(this);
     }
 }
 
 class LDC_W extends LDC {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3904680492002260018L;

    /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     LDC_W() {}
     
     public LDC_W(Object o) {
         super(o);
     }
     
     /**
      * Read needed data (i.e., index) from file.
      */
     protected void initFromFile(jq_ConstantPool cp, ByteSequence bytes, boolean wide) throws IOException {
         char c = (char) bytes.readUnsignedShort();
         o = cp.get(c);
         if (cp.getTag(c) == (char) jq_ClassFileConstants.CONSTANT_ResolvedClass)
             o = Reflection.getJDKType((jq_Type) o);
         else 
             o = cp.get(c);
         length = 3;
     }
 }
 
 class LDIV extends ArithmeticInstruction implements ExceptionThrower, TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3257572793276512304L;

    public LDIV() {
         super(jq_ClassFileConstants.jbc_LDIV);
     }
     
     public Set/*<jq_Class>*/ getExceptions() {
         /*
         return new Class[] { de.fub.bytecode.ExceptionConstants.ARITHMETIC_EXCEPTION };
         */
         return null;
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitExceptionThrower(this);
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitArithmeticInstruction(this);
         v.visitLDIV(this);
     }
 }
 
 class LLOAD extends LoadInstruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3978425823470499637L;

    /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     LLOAD() {
         super(jq_ClassFileConstants.jbc_LLOAD, jq_ClassFileConstants.jbc_LLOAD_0);
     }
     
     public LLOAD(int n) {
         super(jq_ClassFileConstants.jbc_LLOAD, jq_ClassFileConstants.jbc_LLOAD_0, n);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         super.accept(v);
         v.visitLLOAD(this);
     }
 }
 
 class LMUL extends ArithmeticInstruction implements TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3688785869033125680L;

    public LMUL() {
         super(jq_ClassFileConstants.jbc_LMUL);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitArithmeticInstruction(this);
         v.visitLMUL(this);
     }
 }
 
 class LNEG extends ArithmeticInstruction implements TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3256725078350770736L;

    public LNEG() {
         super(jq_ClassFileConstants.jbc_LNEG);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitArithmeticInstruction(this);
         v.visitLNEG(this);
     }
 }
 
 class LOOKUPSWITCH extends Select {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3979268049427836981L;

    /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     LOOKUPSWITCH() {}
     
     public LOOKUPSWITCH(int[] match, ArrayList/*<InstructionHandle>*/ targets, InstructionHandle target) {
         super(jq_ClassFileConstants.jbc_LOOKUPSWITCH, match, targets, target);
         
         length = (short)(9 + match_length * 8); /* alignment remainder assumed
                                                  * 0 here, until dump time. */
         fixed_length = length;
     }
     
     /**
      * Dump instruction as byte code to stream out.
      * @param out Output stream
      */
     public void dump(DataOutputStream out) throws IOException {
         super.dump(out);
         out.writeInt(match_length);       // npairs
         
         for(int i=0; i < match_length; i++) {
             out.writeInt(match[i]);         // match-offset pairs
             out.writeInt(indices[i] = getTargetOffset((InstructionHandle)targets.get(i)));
         }
     }
     
     /**
      * Read needed data (e.g. index) from file.
      */
     protected void initFromFile(jq_ConstantPool cp, ByteSequence bytes, boolean wide) throws IOException {
         super.initFromFile(cp, bytes, wide); // reads padding
         
         match_length = bytes.readInt();
         fixed_length = (short)(9 + match_length * 8);
         length       = (short)(fixed_length + padding);
         
         match   = new int[match_length];
         indices = new int[match_length];
         targets = new ArrayList/*<InstructionHandle>*/(match_length);
         
         for(int i=0; i < match_length; i++) {
             match[i]   = bytes.readInt();
             indices[i] = bytes.readInt();
             targets.add(null);
         }
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitVariableLengthInstruction(this);
         v.visitStackProducer(this);
         v.visitBranchInstruction(this);
         v.visitSelect(this);
         v.visitLOOKUPSWITCH(this);
     }
 }
 
 class LOR extends ArithmeticInstruction implements TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3257854272497726513L;

    public LOR() {
         super(jq_ClassFileConstants.jbc_LOR);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitArithmeticInstruction(this);
         v.visitLOR(this);
     }
 }
 
 class LREM extends ArithmeticInstruction implements ExceptionThrower, TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3256723987479148082L;

    public LREM() {
         super(jq_ClassFileConstants.jbc_LREM);
     }
     
     public Set/*<jq_Class>*/ getExceptions() {
         /*
         return new Class[] { de.fub.bytecode.ExceptionConstants.ARITHMETIC_EXCEPTION };
         */
         return null;
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitExceptionThrower(this);
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitArithmeticInstruction(this);
         v.visitLREM(this);
     }
 }
 
 class LRETURN extends ReturnInstruction implements TypedInstruction, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3760561988110923576L;

    public LRETURN() {
         super(jq_ClassFileConstants.jbc_LRETURN);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         //v.visitExceptionThrower(this);
         v.visitTypedInstruction(this);
         v.visitStackConsumer(this);
         v.visitReturnInstruction(this);
         v.visitLRETURN(this);
     }
 }
 
 class LSHL extends ArithmeticInstruction implements TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3256439201133310514L;

    public LSHL() {
         super(jq_ClassFileConstants.jbc_LSHL);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitArithmeticInstruction(this);
         v.visitLSHL(this);
     }
 }
 
 class LSHR extends ArithmeticInstruction implements TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3258412824370688816L;

    public LSHR() {
         super(jq_ClassFileConstants.jbc_LSHR);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitArithmeticInstruction(this);
         v.visitLSHR(this);
     }
 }
 
 class LSTORE extends StoreInstruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3257566204763058230L;

    /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     LSTORE() {
         super(jq_ClassFileConstants.jbc_LSTORE, jq_ClassFileConstants.jbc_LSTORE_0);
     }
     
     public LSTORE(int n) {
         super(jq_ClassFileConstants.jbc_LSTORE, jq_ClassFileConstants.jbc_LSTORE_0, n);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         super.accept(v);
         v.visitLSTORE(this);
     }
 }
 
 class LSUB extends ArithmeticInstruction implements TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3834874680631636793L;

    public LSUB() {
         super(jq_ClassFileConstants.jbc_LSUB);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitArithmeticInstruction(this);
         v.visitLSUB(this);
     }
 }
 
 class LUSHR extends ArithmeticInstruction implements TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3258126938496316217L;

    public LUSHR() {
         super(jq_ClassFileConstants.jbc_LUSHR);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitArithmeticInstruction(this);
         v.visitLUSHR(this);
     }
 }
 
 class LXOR extends ArithmeticInstruction implements TypedInstruction, StackProducer, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3257565126675870519L;

    public LXOR() {
         super(jq_ClassFileConstants.jbc_LXOR);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackProducer(this);
         v.visitStackConsumer(this);
         v.visitArithmeticInstruction(this);
         v.visitLXOR(this);
     }
 }
 
 class MONITORENTER extends Instruction implements ExceptionThrower, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3761131530872960054L;

    public MONITORENTER() {
         super(jq_ClassFileConstants.jbc_MONITORENTER, (short)1);
     }
     
     public Set/*<jq_Class>*/ getExceptions() {
         /*
         return new Class[] { de.fub.bytecode.ExceptionConstants.NULL_POINTER_EXCEPTION };
         */
         return null;
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitExceptionThrower(this);
         v.visitStackConsumer(this);
         v.visitMONITORENTER(this);
     }
 }
 
 class MONITOREXIT extends Instruction implements ExceptionThrower, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3689071746502244658L;

    public MONITOREXIT() {
         super(jq_ClassFileConstants.jbc_MONITOREXIT, (short)1);
     }
     
     public Set/*<jq_Class>*/ getExceptions() {
         /*
         return new Class[] { de.fub.bytecode.ExceptionConstants.NULL_POINTER_EXCEPTION };
         */
         return null;
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitExceptionThrower(this);
         v.visitStackConsumer(this);
         v.visitMONITOREXIT(this);
     }
 }
 
 class MULTIANEWARRAY extends CPInstruction implements AllocationInstruction, ExceptionThrower, TypedInstruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 4051049674586665785L;
    
    private short dimensions;
     
     /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     MULTIANEWARRAY() {}
     
     public MULTIANEWARRAY(jq_Type array, short dimensions) {
         super(jq_ClassFileConstants.jbc_MULTIANEWARRAY, array);
         
         if(dimensions < 1)
             throw new BytecodeException("Invalid dimensions value: " + dimensions);
         
         this.dimensions = dimensions;
         length = 4;
     }
     
     /**
      * Dump instruction as byte code to stream out.
      * @param out Output stream
      */
     public void dump(DataOutputStream out) throws IOException {
         out.writeByte(opcode);
         out.writeShort(index);
         out.writeByte(dimensions);
     }
     
     /**
      * Read needed data (i.e., no. dimension) from file.
      */
     protected void initFromFile(jq_ConstantPool cp, ByteSequence bytes, boolean wide) throws IOException
     {
         super.initFromFile(cp, bytes, wide);
         dimensions = bytes.readByte();
         length     = 4;
     }
     
     /**
      * @return number of dimensions to be created
      */
     public final short getDimensions() { return dimensions; }
     
     /**
      * @return mnemonic for instruction
      */
     public String toString(boolean verbose) {
         return super.toString(verbose) + " " + index + " " + dimensions;
     }
     
     /**
      * @return mnemonic for instruction with symbolic references resolved
      */
     public String toString() {
         return super.toString() + " " + dimensions;
     }
     
     /**
      * Also works for instructions whose stack effect depends on the
      * constant pool entry they reference.
      * @return Number of words consumed from stack by this instruction
      */
     public int consumeStack() { return dimensions; }
     
     public Set/*<jq_Class>*/ getExceptions() {
         /*
         Class[] cs = new Class[2 + ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION.length];
         
         System.arraycopy(ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION, 0,
         cs, 0, ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION.length);
         
         cs[ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION.length+1] = ExceptionConstants.NEGATIVE_ARRAY_SIZE_EXCEPTION;
         cs[ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION.length]   = ExceptionConstants.ILLEGAL_ACCESS_ERROR;
         
         return cs;
         */
         return null;
     }
     
     /*
     public ObjectType getLoadClassType() {
         Type t = getType(cpg);
         
         if (t instanceof ArrayType){
             t = ((ArrayType) t).getBasicType();
         }
         
         return (t instanceof ObjectType)? (ObjectType) t : null;
     }
     */
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitAllocationInstruction(this);
         v.visitExceptionThrower(this);
         v.visitTypedInstruction(this);
         v.visitCPInstruction(this);
         v.visitMULTIANEWARRAY(this);
     }
 }
 
 class NEWARRAY extends Instruction implements AllocationInstruction, ExceptionThrower, StackProducer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3258126964265988406L;
    
    // todo: make this serializable.
     private transient jq_Array type;
     
     /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     NEWARRAY() {}
     
     public NEWARRAY(byte type) {
         super(jq_ClassFileConstants.jbc_NEWARRAY, (short)2);
         this.type = jq_Array.getPrimitiveArrayType(type);
     }
     
     public NEWARRAY(jq_Array type) {
         super(jq_ClassFileConstants.jbc_NEWARRAY, (short)2);
         Assert._assert(type.getElementType().isPrimitiveType());
         this.type = type;
     }
     
     /**
      * Dump instruction as byte code to stream out.
      * @param out Output stream
      */
     public void dump(DataOutputStream out) throws IOException {
         out.writeByte(opcode);
         out.writeByte(jq_Array.getTypecode(type));
     }
     
     /**
      * @return numeric code for basic element type
      */
     public final byte getTypecode() { return jq_Array.getTypecode(type); }
     
     /**
      * @return type of constructed array
      */
     public final jq_Type getType() {
         return type;
     }
     
     /**
      * @return mnemonic for instruction
      */
     public String toString(boolean verbose) {
         return super.toString(verbose) + " " + type;
     }
     /**
      * Read needed data (e.g. index) from file.
      */
     protected void initFromFile(jq_ConstantPool cp, ByteSequence bytes, boolean wide) throws IOException
     {
         type = jq_Array.getPrimitiveArrayType(bytes.readByte());
         length = 2;
     }
     
     public Set/*<jq_Class>*/ getExceptions() {
         /*
         return new jq_Class[] { de.fub.bytecode.ExceptionConstants.NEGATIVE_ARRAY_SIZE_EXCEPTION };
         */
         return null;
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitAllocationInstruction(this);
         v.visitExceptionThrower(this);
         v.visitStackProducer(this);
         v.visitNEWARRAY(this);
     }
 }
 
 class NEW extends CPInstruction implements LoadClass, AllocationInstruction, ExceptionThrower, StackProducer, TypedInstruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3258694316594573877L;

    /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     NEW() {}
     
     public NEW(jq_Type f) {
         super(jq_ClassFileConstants.jbc_NEW, f);
     }
     
     public Set/*<jq_Class>*/ getExceptions() {
         /*
         Class[] cs = new Class[2 + ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION.length];
         
         System.arraycopy(ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION, 0,
         cs, 0, ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION.length);
         
         cs[ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION.length+1] = ExceptionConstants.INSTANTIATION_ERROR;
         cs[ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION.length]   = ExceptionConstants.ILLEGAL_ACCESS_ERROR;
         
         return cs;
         */
         return null;
     }
     
     public jq_Class getLoadClassType() {
         return (jq_Class)getObject();
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitLoadClass(this);
         v.visitAllocationInstruction(this);
         v.visitExceptionThrower(this);
         v.visitStackProducer(this);
         v.visitTypedInstruction(this);
         v.visitCPInstruction(this);
         v.visitNEW(this);
     }
 }
 
 class NOP extends Instruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3257567299962875957L;

    public NOP() {
         super(jq_ClassFileConstants.jbc_NOP, (short)1);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitNOP(this);
     }
 }
 
 class POP2 extends StackInstruction implements PopInstruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3546360617595123251L;

    public POP2() {
         super(jq_ClassFileConstants.jbc_POP2);
     }
     
    /**
     * Call corresponding visitor method(s). The order is:
     * Call visitor methods of implemented interfaces first, then
     * call methods according to the class hierarchy in descending order,
     * i.e., the most specific visitXXX() call comes last.
     *
     * @param v Visitor object
     */
     public void accept(Visitor v) {
         v.visitStackConsumer(this);
         v.visitPopInstruction(this);
         v.visitStackInstruction(this);
         v.visitPOP2(this);
     }
 }
 
 class POP extends StackInstruction implements PopInstruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3257002172494461239L;

    public POP() {
         super(jq_ClassFileConstants.jbc_POP);
     }
     
    /**
     * Call corresponding visitor method(s). The order is:
     * Call visitor methods of implemented interfaces first, then
     * call methods according to the class hierarchy in descending order,
     * i.e., the most specific visitXXX() call comes last.
     *
     * @param v Visitor object
     */
     public void accept(Visitor v) {
         v.visitStackConsumer(this);
         v.visitPopInstruction(this);
         v.visitStackInstruction(this);
         v.visitPOP(this);
     }
 }
 
 final class PUSH implements CompoundInstruction, VariableLengthInstruction {
     private Instruction instruction;
     
     /**
      * This constructor also applies for values of type short, char, byte
      *
      * @param value to be pushed
      */
     public PUSH(int value) {
         if((value >= -1) && (value <= 5)) // Use ICONST_n
             instruction = InstructionConstants.INSTRUCTIONS[jq_ClassFileConstants.jbc_ICONST_0 + value];
         else if((value >= -128) && (value <= 127)) // Use BIPUSH
             instruction = new BIPUSH((byte)value);
         else if((value >= -32768) && (value <= 32767)) // Use SIPUSH
             instruction = new SIPUSH((short)value);
         else { // If everything fails create a Constant pool entry
             Integer i = new Integer(value);
             //cpr.addOther(i);
             instruction = new LDC(i);
         }
     }
     
     /**
      * @param value to be pushed
      */
     public PUSH(boolean value) {
         instruction = InstructionConstants.INSTRUCTIONS[jq_ClassFileConstants.jbc_ICONST_0 + (value? 1 : 0)];
     }
     
     /**
      * @param value to be pushed
      */
     public PUSH(float value) {
         if(value == 0.0)
             instruction = InstructionConstants.FCONST_0;
         else if(value == 1.0)
             instruction = InstructionConstants.FCONST_1;
         else if(value == 2.0)
             instruction = InstructionConstants.FCONST_2;
         else { // If everything fails create a Constant pool entry
             Float i = new Float(value);
             //cpr.addOther(i);
             instruction = new LDC(i);
         }
     }
     
     /**
      * @param value to be pushed
      */
     public PUSH(long value) {
         if(value == 0)
             instruction = InstructionConstants.LCONST_0;
         else if(value == 1)
             instruction = InstructionConstants.LCONST_1;
         else { // If everything fails create a Constant pool entry
             Long i = new Long(value);
             //cpr.addOther(i);
             instruction = new LDC2_W(i);
         }
     }
     
     /**
      * @param value to be pushed
      */
     public PUSH(double value) {
         if(value == 0.0)
             instruction = InstructionConstants.DCONST_0;
         else if(value == 1.0)
             instruction = InstructionConstants.DCONST_1;
         else { // If everything fails create a Constant pool entry
             Double i = new Double(value);
             //cpr.addOther(i);
             instruction = new LDC2_W(i);
         }
     }
     
     /**
      * @param value to be pushed
      */
     public PUSH(jq_ConstantPool.ConstantPoolRebuilder cpr, String value) {
         if(value == null)
             instruction = InstructionConstants.ACONST_NULL;
         else { // If everything fails create a Constant pool entry
             cpr.addString(value);
             instruction = new LDC(value);
         }
     }
     
     /**
      * @param value to be pushed
      */
     public PUSH(Number value) {
         if((value instanceof Integer) || (value instanceof Short) || (value instanceof Byte))
             instruction = new PUSH(value.intValue()).instruction;
         else if(value instanceof Double)
             instruction = new PUSH(value.doubleValue()).instruction;
         else if(value instanceof Float)
             instruction = new PUSH(value.floatValue()).instruction;
         else if(value instanceof Long)
             instruction = new PUSH(value.longValue()).instruction;
         else
             throw new BytecodeException("What's this: " + value);
     }
     
     /**
      * @param value to be pushed
      */
     public PUSH(Character value) {
         this((int)value.charValue());
     }
     
     /**
      * @param value to be pushed
      */
     public PUSH(Boolean value) {
         this(value.booleanValue());
     }
     
     public final InstructionList getInstructionList() {
         return new InstructionList(instruction);
     }
     
     public final Instruction getInstruction() {
         return instruction;
     }
     
     /**
      * @return mnemonic for instruction
      */
     public String toString() {
         return instruction.toString() + " (PUSH)";
     }
 }
 
 class PUTFIELD extends FieldInstruction implements ExceptionThrower, TypedInstruction, LoadClass {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3257571719517777968L;

    /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     PUTFIELD() {}
     
     public PUTFIELD(jq_InstanceField f) {
         super(jq_ClassFileConstants.jbc_PUTFIELD, f);
     }
     
     public int consumeStack() { return getFieldSize() + 1; }
     
     /**
      * Read needed data (i.e., index) from file.
      * @param bytes input stream
      * @param wide wide prefix?
      */
     protected void initFromFile(jq_ConstantPool cp, ByteSequence bytes, boolean wide) throws IOException {
         o = cp.getAsInstanceField((char)bytes.readUnsignedShort());
         length = 3;
     }
     
     public Set/*<jq_Class>*/ getExceptions() {
         /*
         Class[] cs = new Class[2 + ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION.length];
         
         System.arraycopy(ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION, 0,
         cs, 0, ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION.length);
         
         cs[ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION.length+1] =
         ExceptionConstants.INCOMPATIBLE_CLASS_CHANGE_ERROR;
         cs[ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION.length] =
         ExceptionConstants.NULL_POINTER_EXCEPTION;
         
         return cs;
         */
         return null;
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitExceptionThrower(this);
         v.visitTypedInstruction(this);
         v.visitLoadClass(this);
         v.visitCPInstruction(this);
         v.visitFieldOrMethod(this);
         v.visitFieldInstruction(this);
         v.visitPUTFIELD(this);
     }
 }
 
 class PUTSTATIC extends FieldInstruction implements ExceptionThrower, StackConsumer, PopInstruction, TypedInstruction, LoadClass {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3833745473583462711L;

    /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     PUTSTATIC() {}
     
     public PUTSTATIC(jq_StaticField f) {
         super(jq_ClassFileConstants.jbc_PUTSTATIC, f);
     }
     
     public int consumeStack() { return getFieldSize(); }
     
     /**
      * Read needed data (i.e., index) from file.
      * @param bytes input stream
      * @param wide wide prefix?
      */
     protected void initFromFile(jq_ConstantPool cp, ByteSequence bytes, boolean wide) throws IOException {
         o = cp.getAsStaticField((char)bytes.readUnsignedShort());
         length = 3;
     }
     
     public Set/*<jq_Class>*/ getExceptions() {
         /*
         Class[] cs = new Class[1 + ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION.length];
         
         System.arraycopy(ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION, 0,
         cs, 0, ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION.length);
         cs[ExceptionConstants.EXCS_FIELD_AND_METHOD_RESOLUTION.length] =
         ExceptionConstants.INCOMPATIBLE_CLASS_CHANGE_ERROR;
         
         return cs;
         */
         return null;
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitExceptionThrower(this);
         v.visitStackConsumer(this);
         v.visitPopInstruction(this);
         v.visitTypedInstruction(this);
         v.visitLoadClass(this);
         v.visitCPInstruction(this);
         v.visitFieldOrMethod(this);
         v.visitFieldInstruction(this);
         v.visitPUTSTATIC(this);
     }
 }
 
 class RET extends Instruction implements IndexedInstruction, TypedInstruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3905525986396092729L;
    
    private boolean wide;
     private int     index; // index to local variable containg the return address
     
     /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     RET() {}
     
     public RET(int index) {
         super(jq_ClassFileConstants.jbc_RET, (short)2);
         setIndex(index);   // May set wide as side effect
     }
     
     /**
      * Dump instruction as byte code to stream out.
      * @param out Output stream
      */
     public void dump(DataOutputStream out) throws IOException {
         if(wide)
             out.writeByte(jq_ClassFileConstants.jbc_WIDE);
         
         out.writeByte(opcode);
         
         if(wide)
             out.writeShort(index);
         else
             out.writeByte(index);
     }
     
     private final void setWide() {
         wide = index > Byte.MAX_VALUE;
         if (wide)
             length = 4; // Including the wide byte
         else
             length = 2;
     }
     
     /**
      * Read needed data (e.g. index) from file.
      */
     protected void initFromFile(jq_ConstantPool cp, ByteSequence bytes, boolean wide) throws IOException {
         this.wide = wide;
         
         if(wide) {
             index  = bytes.readUnsignedShort();
             length = 4;
         } else {
             index = bytes.readUnsignedByte();
             length = 2;
         }
     }
     
     /**
      * @return index of local variable containg the return address
      */
     public final int getIndex() { return index; }
     
     /**
      * Set index of local variable containg the return address
      */
     public final void setIndex(int n) {
         if(n < 0)
             throw new BytecodeException("Negative index value: " + n);
         
         index = n;
         setWide();
     }
     
     /**
      * @return mnemonic for instruction
      */
     public String toString(boolean verbose) {
         return super.toString(verbose) + " " + index;
     }
     
     /** @return return address type
      */
     public jq_Type getType() {
         return jq_ReturnAddressType.NO_TARGET;
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitRET(this);
     }
 }
 
 class RETURN extends ReturnInstruction implements TypedInstruction, StackConsumer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3904958664166290739L;

    public RETURN() {
         super(jq_ClassFileConstants.jbc_RETURN);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitTypedInstruction(this);
         v.visitStackConsumer(this);
         v.visitReturnInstruction(this);
         v.visitRETURN(this);
     }
 }
 
 class SALOAD extends ArrayInstruction implements StackProducer, ExceptionThrower, TypedInstruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3905518285402616886L;

    public SALOAD() {
         super(jq_ClassFileConstants.jbc_SALOAD);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitStackProducer(this);
         v.visitExceptionThrower(this);
         v.visitTypedInstruction(this);
         v.visitArrayInstruction(this);
         v.visitSALOAD(this);
     }
 }
 
 class SASTORE extends ArrayInstruction implements StackConsumer, ExceptionThrower, TypedInstruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3257284716884538421L;

    public SASTORE() {
         super(jq_ClassFileConstants.jbc_SASTORE);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitStackConsumer(this);
         v.visitExceptionThrower(this);
         v.visitTypedInstruction(this);
         v.visitArrayInstruction(this);
         v.visitSASTORE(this);
     }
 }
 
 class SIPUSH extends Instruction implements PushInstruction, StackProducer, TypedInstruction, ConstantPushInstruction {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3978422512050845237L;
    
    private short b;
     
     /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     SIPUSH() {}
     
     public SIPUSH(short b) {
         super(jq_ClassFileConstants.jbc_SIPUSH, (short)3);
         this.b = b;
     }
     
     /**
      * Dump instruction as short code to stream out.
      */
     public void dump(DataOutputStream out) throws IOException {
         super.dump(out);
         out.writeShort(b);
     }
     
     /**
      * @return mnemonic for instruction
      */
     public String toString(boolean verbose) {
         return super.toString(verbose) + " " + b;
     }
     
     /**
      * Read needed data (e.g. index) from file.
      */
     protected void initFromFile(jq_ConstantPool cp, ByteSequence bytes, boolean wide) throws IOException
     {
         length = 3;
         b      = bytes.readShort();
     }
     
     public Number getValue() { return new Integer(b); }
     
     /** @return jq_Primitive.SHORT
      */
     public jq_Type getType() {
         return jq_Primitive.SHORT;
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitPushInstruction(this);
         v.visitStackProducer(this);
         v.visitTypedInstruction(this);
         v.visitConstantPushInstruction(this);
         v.visitSIPUSH(this);
     }
 }
 
 class SWAP extends StackInstruction implements StackConsumer, StackProducer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3257008743760803633L;

    public SWAP() {
         super(jq_ClassFileConstants.jbc_SWAP);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitStackConsumer(this);
         v.visitStackProducer(this);
         v.visitStackInstruction(this);
         v.visitSWAP(this);
     }
 }
 
 final class SWITCH implements CompoundInstruction {
     private int[]               match;
     private ArrayList/*<InstructionHandle>*/ targets;
     private Select              instruction;
     private int                 match_length;
     
     /**
      * Template for switch() constructs. If the match array can be
      * sorted in ascending order with gaps no larger than max_gap
      * between the numbers, a TABLESWITCH instruction is generated, and
      * a LOOKUPSWITCH otherwise. The former may be more efficient, but
      * needs more space.
      *
      * Note, that the key array always will be sorted, though we leave
      * the original arrays unaltered.
      *
      * @param match array of match values (case 2: ... case 7: ..., etc.)
      * @param targets the instructions to be branched to for each case
      * @param target the default target
      * @param max_gap maximum gap that may between case branches
      */
     public SWITCH(int[] match, ArrayList/*<InstructionHandle>*/ targets, InstructionHandle target, int max_gap) {
         this.match   = (int[])match.clone();
         this.targets = (ArrayList/*<InstructionHandle>*/)targets.clone();
         
         if((match_length = match.length) < 2) // (almost) empty switch, or just default
             instruction = new TABLESWITCH(match, targets, target);
         else {
             sort(0, match_length - 1);
             
             if(matchIsOrdered(max_gap)) {
                 fillup(max_gap, target);
                 
                 instruction = new TABLESWITCH(this.match, this.targets, target);
             }
             else
                 instruction = new LOOKUPSWITCH(this.match, this.targets, target);
         }
     }
     
     public SWITCH(int[] match, ArrayList/*<InstructionHandle>*/ targets, InstructionHandle target) {
         this(match, targets, target, 1);
     }
     
     private final void fillup(int max_gap, InstructionHandle target) {
         int                 max_size = match_length + match_length * max_gap;
         int[]               m_vec    = new int[max_size];
         ArrayList/*<InstructionHandle>*/ t_vec = new ArrayList(max_size);
         int                 count    = 1;
         
         m_vec[0] = match[0];
         t_vec.add(targets.get(0));
         
         for(int i=1; i < match_length; i++) {
             int prev = match[i-1];
             int gap  = match[i] - prev;
             
             for(int j=1; j < gap; j++) {
                 m_vec[count] = prev + j;
                 t_vec.add(target);
                 count++;
             }
             
             m_vec[count] = match[i];
             t_vec.add(targets.get(i));
             count++;
         }
         
         match   = new int[count];
         System.arraycopy(m_vec, 0, match, 0, count);
         
         targets = t_vec;
         t_vec.trimToSize();
     }
     
     /**
      * Sort match and targets array with QuickSort.
      */
     private final void sort(int l, int r) {
         int i = l, j = r;
         int h, m = match[(l + r) / 2];
         Object h2;
         
         do {
             while(match[i] < m) i++;
             while(m < match[j]) j--;
             
             if(i <= j) {
                 h=match[i]; match[i]=match[j]; match[j]=h; // Swap elements
                 h2=targets.get(i); targets.set(i, targets.get(j)); targets.set(j, h2); // Swap instructions, too
                 i++; j--;
             }
         } while(i <= j);
         
         if(l < j) sort(l, j);
         if(i < r) sort(i, r);
     }
     
     /**
      * @return match is sorted in ascending order with no gap bigger than max_gap?
      */
     private final boolean matchIsOrdered(int max_gap) {
         for(int i=1; i < match_length; i++)
             if(match[i] - match[i-1] > max_gap)
                 return false;
         
         return true;
     }
     
     public final InstructionList getInstructionList() {
         return new InstructionList(instruction);
     }
     
     public final Instruction getInstruction() {
         return instruction;
     }
 }
 
 class TABLESWITCH extends Select implements VariableLengthInstruction, StackProducer {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3257286937416250672L;

    /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     TABLESWITCH() {}
     
     /**
      * @param match sorted array of match values, match[0] must be low value,
      * match[match_length - 1] high value
      * @param targets where to branch for matched values
      * @param target default branch
      */
     public TABLESWITCH(int[] match, ArrayList/*<InstructionHandle>*/ targets, InstructionHandle target) {
         super(jq_ClassFileConstants.jbc_TABLESWITCH, match, targets, target);
         
         length = (short)(13 + match_length * 4); /* Alignment remainder assumed
                                                   * 0 here, until dump time */
         fixed_length = length;
     }
     
     /**
      * Dump instruction as byte code to stream out.
      * @param out Output stream
      */
     public void dump(DataOutputStream out) throws IOException {
         super.dump(out);
         
         int low = (match_length > 0)? match[0] : 0;
         out.writeInt(low);
         
         int high = (match_length > 0)? match[match_length - 1] : 0;
         out.writeInt(high);
         
         for(int i=0; i < match_length; i++)     // jump offsets
             out.writeInt(indices[i] = getTargetOffset((InstructionHandle)targets.get(i)));
     }
     
     /**
      * Read needed data (e.g. index) from file.
      */
     protected void initFromFile(jq_ConstantPool cp, ByteSequence bytes, boolean wide) throws IOException
     {
         super.initFromFile(cp, bytes, wide);
         
         int low    = bytes.readInt();
         int high   = bytes.readInt();
         
         match_length = high - low + 1;
         fixed_length = (short)(13 + match_length * 4);
         length       = (short)(fixed_length + padding);
         
         match   = new int[match_length];
         indices = new int[match_length];
         targets = new ArrayList/*<InstructionHandle>*/(match_length);
         
         for(int i=low; i <= high; i++)
             match[i - low] = i;
         
         for(int i=0; i < match_length; i++) {
             indices[i] = bytes.readInt();
             targets.add(null);
         }
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitVariableLengthInstruction(this);
         v.visitStackProducer(this);
         v.visitBranchInstruction(this);
         v.visitSelect(this);
         v.visitTABLESWITCH(this);
     }
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 interface AllocationInstruction {}
 
 abstract class ArithmeticInstruction extends Instruction
 implements TypedInstruction, StackProducer, StackConsumer {
     /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     ArithmeticInstruction() {}
     
/**
* @param opcode of instruction
*/
     protected ArithmeticInstruction(short opcode) {
         super(opcode, (short)1);
     }
     
/** @return type associated with the instruction
*/
     public jq_Type getType() {
         switch(opcode) {
             case jq_ClassFileConstants.jbc_DADD: case jq_ClassFileConstants.jbc_DDIV: case jq_ClassFileConstants.jbc_DMUL:
             case jq_ClassFileConstants.jbc_DNEG: case jq_ClassFileConstants.jbc_DREM: case jq_ClassFileConstants.jbc_DSUB:
                 return jq_Primitive.DOUBLE;
                 
             case jq_ClassFileConstants.jbc_FADD: case jq_ClassFileConstants.jbc_FDIV: case jq_ClassFileConstants.jbc_FMUL:
             case jq_ClassFileConstants.jbc_FNEG: case jq_ClassFileConstants.jbc_FREM: case jq_ClassFileConstants.jbc_FSUB:
                 return jq_Primitive.FLOAT;
                 
             case jq_ClassFileConstants.jbc_IADD: case jq_ClassFileConstants.jbc_IAND: case jq_ClassFileConstants.jbc_IDIV:
             case jq_ClassFileConstants.jbc_IMUL: case jq_ClassFileConstants.jbc_INEG: case jq_ClassFileConstants.jbc_IOR: case jq_ClassFileConstants.jbc_IREM:
             case jq_ClassFileConstants.jbc_ISHL: case jq_ClassFileConstants.jbc_ISHR: case jq_ClassFileConstants.jbc_ISUB:
             case jq_ClassFileConstants.jbc_IUSHR: case jq_ClassFileConstants.jbc_IXOR:
                 return jq_Primitive.INT;
                 
             case jq_ClassFileConstants.jbc_LADD: case jq_ClassFileConstants.jbc_LAND: case jq_ClassFileConstants.jbc_LDIV:
             case jq_ClassFileConstants.jbc_LMUL: case jq_ClassFileConstants.jbc_LNEG: case jq_ClassFileConstants.jbc_LOR: case jq_ClassFileConstants.jbc_LREM:
             case jq_ClassFileConstants.jbc_LSHL: case jq_ClassFileConstants.jbc_LSHR: case jq_ClassFileConstants.jbc_LSUB:
             case jq_ClassFileConstants.jbc_LUSHR: case jq_ClassFileConstants.jbc_LXOR:
                 return jq_Primitive.LONG;
                 
             default: // Never reached
                 throw new BytecodeException("Unknown type " + opcode);
         }
     }
 }
 
 abstract class ArrayInstruction extends Instruction implements ExceptionThrower, TypedInstruction {
     /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     ArrayInstruction() {}
     
     /**
      * @param opcode of instruction
      */
     protected ArrayInstruction(short opcode) {
         super(opcode, (short)1);
     }
     
     public Set/*<jq_Class>*/ getExceptions() {
         /*
         return de.fub.bytecode.ExceptionConstants.EXCS_ARRAY_EXCEPTION;
         */
         return null;
     }
     
     /** @return type associated with the instruction
      */
     public jq_Type getType() {
         switch(opcode) {
             case jq_ClassFileConstants.jbc_IALOAD: case jq_ClassFileConstants.jbc_IASTORE:
                 return jq_Primitive.INT;
             case jq_ClassFileConstants.jbc_CALOAD: case jq_ClassFileConstants.jbc_CASTORE:
                 return jq_Primitive.CHAR;
             case jq_ClassFileConstants.jbc_BALOAD: case jq_ClassFileConstants.jbc_BASTORE:
                 return jq_Primitive.BYTE;
             case jq_ClassFileConstants.jbc_SALOAD: case jq_ClassFileConstants.jbc_SASTORE:
                 return jq_Primitive.SHORT;
             case jq_ClassFileConstants.jbc_LALOAD: case jq_ClassFileConstants.jbc_LASTORE:
                 return jq_Primitive.LONG;
             case jq_ClassFileConstants.jbc_DALOAD: case jq_ClassFileConstants.jbc_DASTORE:
                 return jq_Primitive.DOUBLE;
             case jq_ClassFileConstants.jbc_FALOAD: case jq_ClassFileConstants.jbc_FASTORE:
                 return jq_Primitive.FLOAT;
             case jq_ClassFileConstants.jbc_AALOAD: case jq_ClassFileConstants.jbc_AASTORE:
                 return PrimordialClassLoader.getJavaLangObject();
                 
             default: throw new BytecodeException("Oops: unknown case in switch" + opcode);
         }
     }
 }
 
 abstract class BranchInstruction extends Instruction implements InstructionTargeter {
     protected int               index;    // Branch target relative to this instruction
     protected InstructionHandle target;   // Target object in instruction list
     protected int               position; // Byte code offset
     
     /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     BranchInstruction() {}
     
     /** Common super constructor.
      * @param opcode instruction opcode
      * @param target instruction to branch to
      */
     protected BranchInstruction(short opcode, InstructionHandle target) {
         super(opcode, (short)3);
         setTarget(target);
     }
     
     /**
      * Dump instruction as byte code to stream out.
      * @param out Output stream
      */
     public void dump(DataOutputStream out) throws IOException {
         out.writeByte(opcode);
         
         index = getTargetOffset();
         
         if(Math.abs(index) >= 32767) // too large for short
             throw new BytecodeException("Branch target offset too large for short");
         
         out.writeShort(index); // May be negative, i.e., point backwards
     }
     
     /**
      * @param target branch target
      * @return the offset to  `target' relative to this instruction
      */
     protected int getTargetOffset(InstructionHandle target) {
         if(target == null)
             throw new BytecodeException("Target of " + super.toString(true) +
             " is invalid null handle");
         
         int t = target.getPosition();
         
         if(t < 0)
             throw new BytecodeException("Invalid branch target position offset for " +
             super.toString(true) + ":" + t + ":" + target);
         
         return t - position;
     }
     
     /**
      * @return the offset to this instruction's target
      */
     protected int getTargetOffset() { return getTargetOffset(target); }
     
     /**
      * Called by InstructionList.setPositions when setting the position for every
      * instruction. In the presence of variable length instructions `setPositions'
      * performs multiple passes over the instruction list to calculate the
      * correct (byte) positions and offsets by calling this function.
      *
      * @param offset additional offset caused by preceding (variable length) instructions
      * @param max_offset the maximum offset that may be caused by these instructions
      * @return additional offset caused by possible change of this instruction's length
      */
     protected int updatePosition(int offset, int max_offset) {
         position += offset;
         return 0;
     }
     
     /**
      * Long output format:
      *
      * &lt;position in byte code&gt;
      * &lt;name of opcode&gt; "["&lt;opcode number&gt;"]"
      * "("&lt;length of instruction&gt;")"
      * "&lt;"&lt;target instruction&gt;"&gt;" "@"&lt;branch target offset&gt;
      *
      * @param verbose long/short format switch
      * @return mnemonic for instruction
      */
     public String toString(boolean verbose) {
         String s = super.toString(verbose);
         String t = "null";
         
         if(verbose) {
             if(target != null) {
                 if(target.getInstruction() == this)
                     t = "<points to itself>";
                 else if(target.getInstruction() == null)
                     t = "<null instruction!!!?>";
                 else
                     t = target.getInstruction().toString(false); // Avoid circles
             }
         } else {
             if(target != null) {
                 index = getTargetOffset();
                 t = "" + (index + position);
             }
         }
         
         return s + " -> " + t;
     }
     
     /**
      * Read needed data (e.g. index) from file. Conversion to a InstructionHandle
      * is done in InstructionList(byte[]).
      *
      * @param bytes input stream
      * @param wide wide prefix?
      * @see joeq.Compiler.BytecodeAnalysis.Bytecodes.InstructionList
      */
     protected void initFromFile(jq_ConstantPool cp, ByteSequence bytes, boolean wide) throws IOException {
         length = 3;
         index  = bytes.readShort();
     }
     
     /**
      * @return target offset in byte code
      */
     public final int getIndex() { return index; }
     
     /**
      * @return target of branch instruction
      */
     public InstructionHandle getTarget() { return target; }
     
     /**
      * Set branch target
      * @param target branch target
      */
     public void setTarget(InstructionHandle target) {
         notifyTarget(this.target, target, this);
         this.target = target;
     }
     
     /**
      * Used by BranchInstruction, LocalVariable, CodeException
      */
     static final void notifyTarget(InstructionHandle old_ih, InstructionHandle new_ih, InstructionTargeter t) {
         if(old_ih != null)
             old_ih.removeTargeter(t);
         if(new_ih != null)
             new_ih.addTargeter(t);
     }
     
     /**
      * @param old_ih old target
      * @param new_ih new target
      */
     public void updateTarget(InstructionHandle old_ih, InstructionHandle new_ih) {
         if(target == old_ih)
             setTarget(new_ih);
         else
             throw new BytecodeException("Not targeting " + old_ih + ", but " + target);
     }
     
     /**
      * @return true, if ih is target of this instruction
      */
     public boolean containsTarget(InstructionHandle ih) {
         return (target == ih);
     }
     
     /**
      * Inform target that it's not targeted anymore.
      */
     void dispose() {
         setTarget(null);
         index=-1;
         position=-1;
     }
 }
 
 interface CompoundInstruction {
     InstructionList getInstructionList();
 }
 
 interface ConstantPushInstruction extends PushInstruction, TypedInstruction {
     Number getValue();
 }
 
 abstract class ConversionInstruction extends Instruction implements TypedInstruction, StackProducer, StackConsumer {
     /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     ConversionInstruction() {}
     
     /**
      * @param opcode opcode of instruction
      */
     protected ConversionInstruction(short opcode) {
         super(opcode, (short)1);
     }
     
     /** @return type associated with the instruction
      */
     public jq_Type getType() {
         switch(opcode) {
             case jq_ClassFileConstants.jbc_D2I: case jq_ClassFileConstants.jbc_F2I: case jq_ClassFileConstants.jbc_L2I:
                 return jq_Primitive.INT;
             case jq_ClassFileConstants.jbc_D2F: case jq_ClassFileConstants.jbc_I2F: case jq_ClassFileConstants.jbc_L2F:
                 return jq_Primitive.FLOAT;
             case jq_ClassFileConstants.jbc_D2L: case jq_ClassFileConstants.jbc_F2L: case jq_ClassFileConstants.jbc_I2L:
                 return jq_Primitive.LONG;
             case jq_ClassFileConstants.jbc_F2D: case jq_ClassFileConstants.jbc_I2D: case jq_ClassFileConstants.jbc_L2D:
                 return jq_Primitive.DOUBLE;
             case jq_ClassFileConstants.jbc_I2B:
                 return jq_Primitive.BYTE;
             case jq_ClassFileConstants.jbc_I2C:
                 return jq_Primitive.CHAR;
             case jq_ClassFileConstants.jbc_I2S:
                 return jq_Primitive.SHORT;
                 
             default: // Never reached
                 throw new BytecodeException("Unknown type " + opcode);
         }
     }
 }
 
 abstract class CPInstruction extends Instruction implements TypedInstruction {
     protected Object o;   // constant pool value
     protected char index; // index into constant pool
     
     /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     CPInstruction() {}
     
     /**
      * @param opcode instruction opcode
      * @param o referred to in constant pool
      */
     protected CPInstruction(short opcode, Object o) {
         super(opcode, (short)3);
         this.o = o;
         Assert._assert(o != null);
     }
     
     /**
      * Dump instruction as byte code to stream out.
      * @param out Output stream
      */
     public void dump(DataOutputStream out) throws IOException {
         Assert._assert(index != 0);
         out.writeByte(opcode);
         out.writeChar(index);
     }
     
     /**
      * Long output format:
      *
      * &lt;name of opcode&gt; "["&lt;opcode number&gt;"]"
      * "("&lt;length of instruction&gt;")" "&lt;"&lt; constant pool index&gt;"&gt;"
      *
      * @param verbose long/short format switch
      * @return mnemonic for instruction
      */
     public String toString(boolean verbose) {
         return super.toString(verbose) + " " + (int)index;
     }
     
     /**
      * @return mnemonic for instruction with symbolic references resolved
      */
     public String toString() {
         return jq_ClassFileConstants.OPCODE_NAMES[opcode] + " " + getObject();
     }
     
     /**
      * Read needed data (i.e., index) from file.
      * @param bytes input stream
      * @param wide wide prefix?
      */
     protected void initFromFile(jq_ConstantPool cp, ByteSequence bytes, boolean wide) throws IOException {
         o = cp.get((char)bytes.readUnsignedShort());
         Assert._assert(!(o instanceof jq_MemberReference));
         length = 3;
     }
     
     /**
      * @return index in constant pool referred by this instruction.
      */
     public final int getIndex() { return index; }
     
     /**
      * Set the index to constant pool.
      * @param cpr constant pool rebuilder
      */
     public void setIndex(jq_ConstantPool.ConstantPoolRebuilder cpr) {
         this.index = cpr.get(o);
         
         if(index == 0)
             throw new BytecodeException("Zero constant pool index");
     }

     public Object getObject() {
         return o;
     }

     public void setObject(Object o) {
         this.o = o;
     }
     
     /** @return type related with this instruction.
      */
     public jq_Type getType() {
         return (jq_Type)getObject();
     }

 }
 
 interface ExceptionThrower {
     Set/*<jq_Class>*/ getExceptions();
 }
 
 abstract class FieldInstruction extends FieldOrMethod implements TypedInstruction {
     /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     FieldInstruction() {}
     
     /**
      * @param opcode instruction opcode
      * @param f field
      */
     protected FieldInstruction(short opcode, jq_Field f) {
         super(opcode, f);
     }
     
     /**
      * @return mnemonic for instruction with symbolic references resolved
      */
     public String toString() {
         return jq_ClassFileConstants.OPCODE_NAMES[opcode] + " " + getField();
     }

     public jq_Field getField() { return (jq_Field)getObject(); }

     /** @return size of field (1 or 2)
      */
     protected int getFieldSize() {
         return getField().getWidth() >> 2;
     }
     
     /** @return return type of referenced field
      */
     public jq_Type getType() {
         return getFieldType();
     }
     
     /** @return type of field
      */
     public jq_Type getFieldType() {
         return getField().getType();
     }
     
     /** @return name of referenced field.
      */
     public String getFieldName() {
         return getName();
     }
 }
 
 abstract class FieldOrMethod extends CPInstruction implements LoadClass {
     /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     FieldOrMethod() {}

     /**
      * @param opcode instruction opcode
      * @param f field or method
      */
     protected FieldOrMethod(short opcode, jq_Member f) {
         super(opcode, f);
     }

     /** @return signature of referenced method/field.
      */
     public String getSignature() {
         jq_Member member = (jq_Member)getObject();
         return member.getDesc().toString();
     }
     
     /** @return name of referenced method/field.
      */
     public String getName() {
         jq_Member member = (jq_Member)getObject();
         return member.getName().toString();
     }
     
     /** @return name of the referenced class/interface
      */
     public String getClassName() {
         jq_Member member = (jq_Member)getObject();
         return member.getDeclaringClass().getName().toString();
     }
     
     /** @return type of the referenced class/interface
      */
     public jq_Class getClassType() {
         jq_Member member = (jq_Member)getObject();
         return member.getDeclaringClass();
     }
     
     /** @return type of the referenced class/interface
      */
     public jq_Class getLoadClassType() {
         return getClassType();
     }
     
     protected abstract void initFromFile(jq_ConstantPool cp, ByteSequence bytes, boolean wide) throws IOException;
 }

 abstract class GotoInstruction extends BranchInstruction implements UnconditionalBranch
 {
     GotoInstruction(short opcode, InstructionHandle target) {
         super(opcode, target);
     }
     
     /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     GotoInstruction() {}
 }
 
 abstract class IfInstruction extends BranchInstruction implements StackConsumer {
     /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     IfInstruction() {}
     
     /**
      * @param opcode opcode of this instruction
      * @param target target instruction to branch to
      */
     protected IfInstruction(short opcode, InstructionHandle target) {
         super(opcode, target);
     }
     
     /**
      * @return negation of instruction, e.g. IFEQ.negate() == IFNE
      */
     public abstract IfInstruction negate();
 }
 
 interface IndexedInstruction {
     int getIndex();
     void setIndex(int index);
 }
 
 interface InstructionTargeter {
     boolean containsTarget(InstructionHandle ih);
     void updateTarget(InstructionHandle old_ih, InstructionHandle new_ih);
 }
 
 abstract class InvokeInstruction extends FieldOrMethod implements ExceptionThrower, TypedInstruction, StackConsumer, StackProducer {
     /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     InvokeInstruction() {}
     
     /**
      * @param opcode instruction opcode
      * @param f method
      */
     protected InvokeInstruction(short opcode, jq_Method f) {
         super(opcode, f);
     }
     
     /**
      * @return mnemonic for instruction with symbolic references resolved
      */
     public String toString() {
         return jq_ClassFileConstants.OPCODE_NAMES[opcode] + " " + getMethod();
     }

     public jq_Method getMethod() { return (jq_Method)getObject(); }

     /**
      * Also works for instructions whose stack effect depends on the
      * constant pool entry they reference.
      * @return Number of words consumed from stack by this instruction
      */
     public int consumeStack() {
         return getMethod().getParamWords();
     }
     
     /**
      * Also works for instructions whose stack effect depends on the
      * constant pool entry they reference.
      * @return Number of words produced onto stack by this instruction
      */
     public int produceStack() {
         return getMethod().getReturnWords();
     }
     
     /** @return return type of referenced method.
      */
     public jq_Type getType() {
         return getMethod().getReturnType();
     }
     
     /** @return name of referenced method.
      */
     public String getMethodName() {
         return getName();
     }
     
     /** @return return type of referenced method.
      */
     public jq_Type getReturnType() {
         return getMethod().getReturnType();
     }
     
     /** @return argument types of referenced method.
      */
     public jq_Type[] getArgumentTypes() {
         return getMethod().getParamTypes();
     }
 }
 
 abstract class JsrInstruction extends BranchInstruction implements UnconditionalBranch, TypedInstruction, StackProducer {
     JsrInstruction(short opcode, InstructionHandle target) {
         super(opcode, target);
     }
     
     /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     JsrInstruction(){}
     
     /** @return return address type
      */
     public jq_Type getType() {
         return new jq_ReturnAddressType(physicalSuccessor());
     }
     
     /**
      * Returns an InstructionHandle to the physical successor
      * of this JsrInstruction. <B>For this method to work,
      * this JsrInstruction object must not be shared between
      * multiple InstructionHandle objects!</B>
      * Formally, there must not be InstructionHandle objects
      * i, j where i != j and i.getInstruction() == this ==
      * j.getInstruction().
      * @return an InstructionHandle to the "next" instruction that
      * will be executed when RETurned from a subroutine.
      */
     public InstructionHandle physicalSuccessor(){
         InstructionHandle ih = this.target;
         
         // Rewind!
         while(ih.getPrev() != null)
             ih = ih.getPrev();
         
         // Find the handle for "this" JsrInstruction object.
         while(ih.getInstruction() != this)
             ih = ih.getNext();
         
         InstructionHandle toThis = ih;
         
         while(ih != null){
             ih = ih.getNext();
             if ((ih != null) && (ih.getInstruction() == this))
                 throw new RuntimeException("physicalSuccessor() called on a shared JsrInstruction.");
         }
         
         // Return the physical successor
         return toThis.getNext();
     }
 }
 
 interface LoadClass {
     /**
      * Returns the jq_Class of the referenced class or interface
      * that may be loaded and resolved.
      * @return object type that may be loaded or null if a primitive is
      * referenced
      */
     jq_Class getLoadClassType();
     
     /**
      * Returns the type associated with this instruction.
      * LoadClass instances are always typed, but this type
      * does not always refer to the type of the class or interface
      * that it possibly forces to load. For example, GETFIELD would
      * return the type of the field and not the type of the class
      * where the field is defined.
      * If no class is forced to be loaded, <B>null</B> is returned.
      * An example for this is an ANEWARRAY instruction that creates
      * an int[][].
      * @see #getLoadClassType()
      */
     jq_Type getType();
 }
 
 abstract class LoadInstruction extends LocalVariableInstruction implements PushInstruction {
     /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      * tag and length are defined in readInstruction and initFromFile, respectively.
      */
     LoadInstruction(short canon_tag, short c_tag) {
         super(canon_tag, c_tag);
     }
     
     /**
      * @param opcode Instruction opcode
      * @param c_tag Instruction number for compact version, ALOAD_0, e.g.
      * @param n local variable index (unsigned short)
      */
     protected LoadInstruction(short opcode, short c_tag, int n) {
         super(opcode, c_tag, n);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitStackProducer(this);
         v.visitPushInstruction(this);
         v.visitTypedInstruction(this);
         v.visitLocalVariableInstruction(this);
         v.visitLoadInstruction(this);
     }
 }
 
 abstract class LocalVariableInstruction extends Instruction implements TypedInstruction, IndexedInstruction {
     protected int     n         = -1; // index of referenced variable
     private short     c_tag     = -1; // compact version, such as ILOAD_0
     private short     canon_tag = -1; // canonical tag such as ILOAD
     
     private final boolean wide() { return n > Byte.MAX_VALUE; }
     
     /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      * tag and length are defined in readInstruction and initFromFile, respectively.
      */
     LocalVariableInstruction(short canon_tag, short c_tag) {
         super();
         this.canon_tag = canon_tag;
         this.c_tag     = c_tag;
     }
     
     /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Also used by IINC()!
      */
     LocalVariableInstruction() {}
     
     /**
      * @param opcode Instruction opcode
      * @param c_tag Instruction number for compact version, ALOAD_0, e.g.
      * @param n local variable index (unsigned short)
      */
     protected LocalVariableInstruction(short opcode, short c_tag, int n) {
         super(opcode, (short)2);
         
         this.c_tag = c_tag;
         canon_tag  = opcode;
         
         setIndex(n);
     }
     
     /**
      * Dump instruction as byte code to stream out.
      * @param out Output stream
      */
     public void dump(DataOutputStream out) throws IOException {
         if(wide()) // Need WIDE prefix ?
             out.writeByte(jq_ClassFileConstants.jbc_WIDE);
         
         out.writeByte(opcode);
         
         if(length > 1) { // Otherwise ILOAD_n, instruction, e.g.
             if(wide())
                 out.writeShort(n);
             else
                 out.writeByte(n);
         }
     }
     
     /**
      * Long output format:
      *
      * &lt;name of opcode&gt; "["&lt;opcode number&gt;"]"
      * "("&lt;length of instruction&gt;")" "&lt;"&lt; local variable index&gt;"&gt;"
      *
      * @param verbose long/short format switch
      * @return mnemonic for instruction
      */
     public String toString(boolean verbose) {
         if(((opcode >= jq_ClassFileConstants.jbc_ILOAD_0) && (opcode <= jq_ClassFileConstants.jbc_ALOAD_3)) || ((opcode >= jq_ClassFileConstants.jbc_ISTORE_0) && (opcode <= jq_ClassFileConstants.jbc_ASTORE_3)))
             return super.toString(verbose);
         else
             return super.toString(verbose) + " " + n;
     }
     
     /**
      * Read needed data (e.g. index) from file.
      * PRE: (ILOAD <= tag <= ALOAD_3) || (ISTORE <= tag <= ASTORE_3)
      */
     protected void initFromFile(jq_ConstantPool cp, ByteSequence bytes, boolean wide) throws IOException {
         if(wide) {
             n         = bytes.readUnsignedShort();
             length    = 4;
         } else if(((opcode >= jq_ClassFileConstants.jbc_ILOAD) &&
                    (opcode <= jq_ClassFileConstants.jbc_ALOAD)) ||
                   ((opcode >= jq_ClassFileConstants.jbc_ISTORE) &&
                    (opcode <= jq_ClassFileConstants.jbc_ASTORE))) {
             n      = bytes.readUnsignedByte();
             length = 2;
         } else if(opcode <= jq_ClassFileConstants.jbc_ALOAD_3) { // compact load instruction such as ILOAD_2
             n      = (opcode - jq_ClassFileConstants.jbc_ILOAD_0) % 4;
             length = 1;
         } else { // Assert ISTORE_0 <= tag <= ASTORE_3
             n      = (opcode - jq_ClassFileConstants.jbc_ISTORE_0) % 4;
             length = 1;
         }
     }
     
     /**
      * @return local variable index  referred by this instruction.
      */
     public final int getIndex() { return n; }
     
     /**
      * Set the local variable index
      */
     public void setIndex(int n) {
         if((n < 0) || (n > Short.MAX_VALUE))
             throw new BytecodeException("Illegal value: " + n);
         
         this.n = n;
         
         if(n >= 0 && n <= 3) { // Use more compact instruction xLOAD_n
             opcode = (short)(c_tag + n);
             length = 1;
         } else {
             opcode = canon_tag;
             
             if(wide()) // Need WIDE prefix ?
                 length = 4;
             else
                 length = 2;
         }
     }
     
     /** @return canonical tag for instruction, e.g., ALOAD for ALOAD_0
      */
     public short getCanonicalTag() {
         return canon_tag;
     }
     
     /**
      * Returns the type associated with the instruction -
      * in case of ALOAD or ASTORE Type.OBJECT is returned.
      * This is just a bit incorrect, because ALOAD and ASTORE
      * may work on every ReferenceType (including Type.NULL) and
      * ASTORE may even work on a ReturnaddressType .
      * @return type associated with the instruction
      */
     public final jq_Type getType() {
         switch(canon_tag) {
             case jq_ClassFileConstants.jbc_ILOAD: case jq_ClassFileConstants.jbc_ISTORE:
                 return jq_Primitive.INT;
             case jq_ClassFileConstants.jbc_LLOAD: case jq_ClassFileConstants.jbc_LSTORE:
                 return jq_Primitive.LONG;
             case jq_ClassFileConstants.jbc_DLOAD: case jq_ClassFileConstants.jbc_DSTORE:
                 return jq_Primitive.DOUBLE;
             case jq_ClassFileConstants.jbc_FLOAD: case jq_ClassFileConstants.jbc_FSTORE:
                 return jq_Primitive.FLOAT;
             case jq_ClassFileConstants.jbc_ALOAD: case jq_ClassFileConstants.jbc_ASTORE:
                 return PrimordialClassLoader.getJavaLangObject();

             default: throw new BytecodeException("Oops: unknown case in switch" + canon_tag);
         }
     }
 }
 
 interface PopInstruction extends StackConsumer {}
 
 interface PushInstruction extends StackProducer {}
 
 abstract class ReturnInstruction extends Instruction implements ExceptionThrower, TypedInstruction, StackConsumer {
     /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     ReturnInstruction() {}
     
     /**
      * @param opcode of instruction
      */
     protected ReturnInstruction(short opcode) {
         super(opcode, (short)1);
     }
     
     public jq_Type getType() {
         switch(opcode) {
             case jq_ClassFileConstants.jbc_IRETURN: return jq_Primitive.INT;
             case jq_ClassFileConstants.jbc_LRETURN: return jq_Primitive.LONG;
             case jq_ClassFileConstants.jbc_FRETURN: return jq_Primitive.FLOAT;
             case jq_ClassFileConstants.jbc_DRETURN: return jq_Primitive.DOUBLE;
             case jq_ClassFileConstants.jbc_ARETURN: return PrimordialClassLoader.getJavaLangObject();
             case jq_ClassFileConstants.jbc_RETURN:  return jq_Primitive.VOID;
             
             default: // Never reached
                 throw new BytecodeException("Unknown type " + opcode);
         }
     }
     
     public Set/*<jq_Class>*/ getExceptions() {
         //return new jq_Class[] { ClassLib.sun13.java.lang.IllegalMonitorState._class };
         return null;
     }
     
 }
 
 abstract class Select extends BranchInstruction implements VariableLengthInstruction, StackProducer {
     protected int[]               match;        // matches, i.e., case 1: ...
     protected int[]               indices;      // target offsets
     protected ArrayList/*<InstructionHandle>*/ targets;      // target objects in instruction list
     protected int                 fixed_length; // fixed length defined by subclasses
     protected int                 match_length; // number of cases
     protected int                 padding = 0;  // number of pad bytes for alignment
     
     /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     Select() {}
     
     /**
      * (Match, target) pairs for switch.
      * `Match' and `targets' must have the same length of course.
      *
      * @param match array of matching values
      * @param targets instruction targets
      * @param target default instruction target
      */
     Select(short opcode, int[] match, ArrayList/*<InstructionHandle>*/ targets, InstructionHandle target) {
         super(opcode, target);
         
         this.targets = targets;
         for(Iterator i=targets.iterator(); i.hasNext();)
             notifyTarget(null, (InstructionHandle)i.next(), this);
         
         this.match = match;
         
         if((match_length = match.length) != targets.size())
             throw new BytecodeException("Match and target array have not the same length");
         
         indices = new int[match_length];
     }
     
     /**
      * Since this is a variable length instruction, it may shift the following
      * instructions which then need to update their position.
      *
      * Called by InstructionList.setPositions when setting the position for every
      * instruction. In the presence of variable length instructions `setPositions'
      * performs multiple passes over the instruction list to calculate the
      * correct (byte) positions and offsets by calling this function.
      *
      * @param offset additional offset caused by preceding (variable length) instructions
      * @param max_offset the maximum offset that may be caused by these instructions
      * @return additional offset caused by possible change of this instruction's length
      */
     protected int updatePosition(int offset, int max_offset) {
         position += offset; // Additional offset caused by preceding SWITCHs, GOTOs, etc.
         
         short old_length = length;
         
         /* Alignment on 4-byte-boundary, + 1, because of tag byte.
          */
         padding = (4 - ((position + 1) % 4)) % 4;
         length  = (short)(fixed_length + padding); // Update length
         
         return length - old_length;
     }
     
     /**
      * Dump instruction as byte code to stream out.
      * @param out Output stream
      */
     public void dump(DataOutputStream out) throws IOException {
         out.writeByte(opcode);
         
         for(int i=0; i < padding; i++) // Padding bytes
             out.writeByte(0);
         
         index = getTargetOffset();     // Write default target offset
         out.writeInt(index);
     }
     
     /**
      * Read needed data (e.g. index) from file.
      */
     protected void initFromFile(jq_ConstantPool cp, ByteSequence bytes, boolean wide) throws IOException {
         padding = (4 - (bytes.getIndex() % 4)) % 4; // Compute number of pad bytes
         
         for(int i=0; i < padding; i++) {
             byte b;
             if((b=bytes.readByte()) != 0)
                 throw new BytecodeException("Padding byte != 0: " + b);
         }
         
         // Default branch target common for both cases (TABLESWITCH, LOOKUPSWITCH)
         index = bytes.readInt();
     }
     
     /**
      * @return mnemonic for instruction
      */
     public String toString(boolean verbose) {
         StringBuffer buf = new StringBuffer(super.toString(verbose));
         
         if(verbose) {
             for(int i=0; i < match_length; i++) {
                 String s = "null";
                 
                 if(targets.get(i) != null)
                     s = ((InstructionHandle)targets.get(i)).getInstruction().toString();
                 
                 buf.append("(" + match[i] + ", " + s + " = {" + indices[i] + "})");
             }
         }
         else
             buf.append(" ...");
         
         return buf.toString();
     }
     
     /**
      * Set branch target for `i'th case
      */
     public void setTarget(int i, InstructionHandle target) {
         notifyTarget((InstructionHandle)targets.get(i), target, this);
         targets.set(i, target);
     }
     
     /**
      * @param old_ih old target
      * @param new_ih new target
      */
     public void updateTarget(InstructionHandle old_ih, InstructionHandle new_ih) {
         boolean targeted = false;
         
         if(target == old_ih) {
             targeted = true;
             setTarget(new_ih);
         }
         
         for(int i=0; i<targets.size(); ++i) {
             if (targets.get(i) == old_ih) {
                 targeted = true;
                 setTarget(i, new_ih);
             }
         }
         
         if(!targeted)
             throw new BytecodeException("Not targeting " + old_ih);
     }
     
     /**
      * @return true, if ih is target of this instruction
      */
     public boolean containsTarget(InstructionHandle ih) {
         if(target == ih)
             return true;
         
         for(int i=0; i < targets.size(); i++)
             if(targets.get(i) == ih)
                 return true;
         
         return false;
     }
     
     /**
      * Inform targets that they're not targeted anymore.
      */
     void dispose() {
         super.dispose();
         
         for(int i=0; i < targets.size(); i++)
             ((InstructionHandle)targets.get(i)).removeTargeter(this);
     }
     
     /**
      * @return array of match indices
      */
     public int[] getMatchs() { return match; }
     
     /**
      * @return array of match target offsets
      */
     public int[] getIndices() { return indices; }
     
     /**
      * @return array of match targets
      */
     public List/*<InstructionHandle>*/ getTargets() { return targets; }
 }
 
 interface StackConsumer {
     /** @return how many words are consumed from stack
      */
     int consumeStack();
 }
 
 abstract class StackInstruction extends Instruction {
     /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      */
     StackInstruction() {}
     
     /**
      * @param opcode instruction opcode
      */
     protected StackInstruction(short opcode) {
         super(opcode, (short)1);
     }
     
 }
 
 interface StackProducer {
     /** @return how many words are produced on stack
      */
     int produceStack();
 }
 
 abstract class StoreInstruction extends LocalVariableInstruction implements PopInstruction
 {
     /**
      * Empty constructor needed for the Class.newInstance() statement in
      * Instruction.readInstruction(). Not to be used otherwise.
      * tag and length are defined in readInstruction and initFromFile, respectively.
      */
     StoreInstruction(short canon_tag, short c_tag) {
         super(canon_tag, c_tag);
     }
     
     /**
      * @param opcode Instruction opcode
      * @param c_tag Instruction number for compact version, ASTORE_0, e.g.
      * @param n local variable index (unsigned short)
      */
     protected StoreInstruction(short opcode, short c_tag, int n) {
         super(opcode, c_tag, n);
     }
     
     /**
      * Call corresponding visitor method(s). The order is:
      * Call visitor methods of implemented interfaces first, then
      * call methods according to the class hierarchy in descending order,
      * i.e., the most specific visitXXX() call comes last.
      *
      * @param v Visitor object
      */
     public void accept(Visitor v) {
         v.visitStackConsumer(this);
         v.visitPopInstruction(this);
         v.visitStoreInstruction(this);
         v.visitTypedInstruction(this);
         v.visitLocalVariableInstruction(this);
         v.visitStoreInstruction(this);
     }
 }
 
 interface TypedInstruction {
     jq_Type getType();
 }
 
 interface UnconditionalBranch {}
 
 interface VariableLengthInstruction {}
 
 
 
 class BytecodeException extends RuntimeException {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3689634696422175796L;
    public BytecodeException() { super(); }
     public BytecodeException(String s) { super(s); }
 }

 
 class jq_ReturnAddressType extends jq_Reference {
     public static final jq_ReturnAddressType NO_TARGET = new jq_ReturnAddressType();
     private InstructionHandle returnTarget;
     private jq_ReturnAddressType() { super(Utf8.get("L&ReturnAddress;"), PrimordialClassLoader.loader); }
     private jq_ReturnAddressType(InstructionHandle returnTarget) {
         super(Utf8.get("L&ReturnAddress;"), PrimordialClassLoader.loader);
         this.returnTarget = returnTarget;
     }
     public boolean isAddressType() { return false; }
     public String getJDKName() { return desc.toString(); }
     public String getJDKDesc() { return getJDKName(); }
     public jq_Class[] getInterfaces() { Assert.UNREACHABLE(); return null; }
     public jq_Class getInterface(Utf8 desc) { Assert.UNREACHABLE(); return null; }
     public boolean implementsInterface(jq_Class k) { Assert.UNREACHABLE(); return false; }
     public jq_InstanceMethod getVirtualMethod(jq_NameAndDesc nd) { Assert.UNREACHABLE(); return null; }
     public String getName() { Assert.UNREACHABLE(); return null; }
     public String shortName() { Assert.UNREACHABLE(); return null; }
     public boolean isClassType() { Assert.UNREACHABLE(); return false; }
     public boolean isArrayType() { Assert.UNREACHABLE(); return false; }
     public boolean isFinal() { Assert.UNREACHABLE(); return false; }
     public jq_Reference getDirectPrimarySupertype() { Assert.UNREACHABLE(); return null; }
     public int getDepth() { Assert.UNREACHABLE(); return 0; }
     public void load() { Assert.UNREACHABLE(); }
     public void verify() { Assert.UNREACHABLE(); }
     public void prepare() { Assert.UNREACHABLE(); }
     public void sf_initialize() { Assert.UNREACHABLE(); }
     public void compile() { Assert.UNREACHABLE(); }
     public void cls_initialize() { Assert.UNREACHABLE(); }
     public String toString() { return "ReturnAddress (target="+returnTarget+")"; }
     public boolean equals(Object rat) {
         if (!(rat instanceof jq_ReturnAddressType)) return false;
         return ((jq_ReturnAddressType)rat).returnTarget.equals(this.returnTarget);
     }
     public int hashCode() {
         return returnTarget.hashCode();
     }
 }
 
 class CodeException {
     private InstructionHandle start, end, handler;
     private jq_Class type;
     
     public CodeException(InstructionList il, byte[] b, jq_TryCatchBC tc) {
         this.start = il.findHandle(tc.getStartPC());
         if (tc.getEndPC() == b.length) {
             this.end = il.getEnd();
         } else {
             this.end = il.findHandle(tc.getEndPC());
             this.end = this.end.getPrev();
         }
         this.handler = il.findHandle(tc.getHandlerPC());
         this.type = tc.getExceptionType();
     }
     public CodeException(InstructionHandle start, InstructionHandle end, jq_Class type, InstructionHandle handler) {
         this.start = start;
         this.end = end;
         this.type = type;
         this.handler = handler;
     }
     
     public InstructionHandle getStartPC() { return start; }
     public void setStartPC(InstructionHandle i) { this.start = i; }
     public InstructionHandle getEndPC() { return end; }
     public void setEndPC(InstructionHandle i) { this.end = i; }
     public InstructionHandle getHandlerPC() { return handler; }
     public void setHandlerPC(InstructionHandle i) { this.handler = i; }
     
     public jq_TryCatchBC finish() {
         Assert._assert(this.start.getPosition() >= 0);
         Assert._assert(this.end.getPosition()+this.end.getInstruction().getLength() > 0);
         Assert._assert(this.handler.getPosition() >= 0);
         return new jq_TryCatchBC((char)this.start.getPosition(),
                                  (char)(this.end.getPosition()+this.end.getInstruction().getLength()),
                                  (char)this.handler.getPosition(),
                                  this.type);
     }
 }
 
 class LineNumber {
     private InstructionHandle start;
     private char num;
     
     public LineNumber(InstructionList il, jq_LineNumberBC tc) {
         this.start = il.findHandle(tc.getStartPC());
         if (this.start == null) {
             System.out.println("Cannot find index "+(int)tc.getStartPC()+" in "+il);
         }
         this.num = tc.getLineNum();
     }
     
     public InstructionHandle getStartPC() { return start; }
     public void setStartPC(InstructionHandle i) { this.start = i; }
     
     public jq_LineNumberBC finish() {
         char c;
         if (this.start == null) c = 0;
         else c = (char)this.start.getPosition();
         return new jq_LineNumberBC(c, this.num);
     }
 }
 
 interface InstructionConstants {
     /** Predefined instruction objects
      */
     Instruction           NOP          = new NOP();
     Instruction           ACONST_NULL  = new ACONST_NULL();
     Instruction           ICONST_M1    = new ICONST(-1);
     Instruction           ICONST_0     = new ICONST(0);
     Instruction           ICONST_1     = new ICONST(1);
     Instruction           ICONST_2     = new ICONST(2);
     Instruction           ICONST_3     = new ICONST(3);
     Instruction           ICONST_4     = new ICONST(4);
     Instruction           ICONST_5     = new ICONST(5);
     Instruction           LCONST_0     = new LCONST(0);
     Instruction           LCONST_1     = new LCONST(1);
     Instruction           FCONST_0     = new FCONST(0);
     Instruction           FCONST_1     = new FCONST(1);
     Instruction           FCONST_2     = new FCONST(2);
     Instruction           DCONST_0     = new DCONST(0);
     Instruction           DCONST_1     = new DCONST(1);
     ArrayInstruction      IALOAD       = new IALOAD();
     ArrayInstruction      LALOAD       = new LALOAD();
     ArrayInstruction      FALOAD       = new FALOAD();
     ArrayInstruction      DALOAD       = new DALOAD();
     ArrayInstruction      AALOAD       = new AALOAD();
     ArrayInstruction      BALOAD       = new BALOAD();
     ArrayInstruction      CALOAD       = new CALOAD();
     ArrayInstruction      SALOAD       = new SALOAD();
     ArrayInstruction      IASTORE      = new IASTORE();
     ArrayInstruction      LASTORE      = new LASTORE();
     ArrayInstruction      FASTORE      = new FASTORE();
     ArrayInstruction      DASTORE      = new DASTORE();
     ArrayInstruction      AASTORE      = new AASTORE();
     ArrayInstruction      BASTORE      = new BASTORE();
     ArrayInstruction      CASTORE      = new CASTORE();
     ArrayInstruction      SASTORE      = new SASTORE();
     StackInstruction      POP          = new POP();
     StackInstruction      POP2         = new POP2();
     StackInstruction      DUP          = new DUP();
     StackInstruction      DUP_X1       = new DUP_X1();
     StackInstruction      DUP_X2       = new DUP_X2();
     StackInstruction      DUP2         = new DUP2();
     StackInstruction      DUP2_X1      = new DUP2_X1();
     StackInstruction      DUP2_X2      = new DUP2_X2();
     StackInstruction      SWAP         = new SWAP();
     ArithmeticInstruction IADD         = new IADD();
     ArithmeticInstruction LADD         = new LADD();
     ArithmeticInstruction FADD         = new FADD();
     ArithmeticInstruction DADD         = new DADD();
     ArithmeticInstruction ISUB         = new ISUB();
     ArithmeticInstruction LSUB         = new LSUB();
     ArithmeticInstruction FSUB         = new FSUB();
     ArithmeticInstruction DSUB         = new DSUB();
     ArithmeticInstruction IMUL         = new IMUL();
     ArithmeticInstruction LMUL         = new LMUL();
     ArithmeticInstruction FMUL         = new FMUL();
     ArithmeticInstruction DMUL         = new DMUL();
     ArithmeticInstruction IDIV         = new IDIV();
     ArithmeticInstruction LDIV         = new LDIV();
     ArithmeticInstruction FDIV         = new FDIV();
     ArithmeticInstruction DDIV         = new DDIV();
     ArithmeticInstruction IREM         = new IREM();
     ArithmeticInstruction LREM         = new LREM();
     ArithmeticInstruction FREM         = new FREM();
     ArithmeticInstruction DREM         = new DREM();
     ArithmeticInstruction INEG         = new INEG();
     ArithmeticInstruction LNEG         = new LNEG();
     ArithmeticInstruction FNEG         = new FNEG();
     ArithmeticInstruction DNEG         = new DNEG();
     ArithmeticInstruction ISHL         = new ISHL();
     ArithmeticInstruction LSHL         = new LSHL();
     ArithmeticInstruction ISHR         = new ISHR();
     ArithmeticInstruction LSHR         = new LSHR();
     ArithmeticInstruction IUSHR        = new IUSHR();
     ArithmeticInstruction LUSHR        = new LUSHR();
     ArithmeticInstruction IAND         = new IAND();
     ArithmeticInstruction LAND         = new LAND();
     ArithmeticInstruction IOR          = new IOR();
     ArithmeticInstruction LOR          = new LOR();
     ArithmeticInstruction IXOR         = new IXOR();
     ArithmeticInstruction LXOR         = new LXOR();
     ConversionInstruction I2L          = new I2L();
     ConversionInstruction I2F          = new I2F();
     ConversionInstruction I2D          = new I2D();
     ConversionInstruction L2I          = new L2I();
     ConversionInstruction L2F          = new L2F();
     ConversionInstruction L2D          = new L2D();
     ConversionInstruction F2I          = new F2I();
     ConversionInstruction F2L          = new F2L();
     ConversionInstruction F2D          = new F2D();
     ConversionInstruction D2I          = new D2I();
     ConversionInstruction D2L          = new D2L();
     ConversionInstruction D2F          = new D2F();
     ConversionInstruction I2B          = new I2B();
     ConversionInstruction I2C          = new I2C();
     ConversionInstruction I2S          = new I2S();
     Instruction           LCMP         = new LCMP();
     Instruction           FCMPL        = new FCMPL();
     Instruction           FCMPG        = new FCMPG();
     Instruction           DCMPL        = new DCMPL();
     Instruction           DCMPG        = new DCMPG();
     ReturnInstruction     IRETURN      = new IRETURN();
     ReturnInstruction     LRETURN      = new LRETURN();
     ReturnInstruction     FRETURN      = new FRETURN();
     ReturnInstruction     DRETURN      = new DRETURN();
     ReturnInstruction     ARETURN      = new ARETURN();
     ReturnInstruction     RETURN       = new RETURN();
     Instruction           ARRAYLENGTH  = new ARRAYLENGTH();
     Instruction           ATHROW       = new ATHROW();
     Instruction           MONITORENTER = new MONITORENTER();
     Instruction           MONITOREXIT  = new MONITOREXIT();
     
     /** You can use these constants in multiple places safely, if you can guarantee
      * that you will never alter their internal values, e.g. call setIndex().
      */
     LocalVariableInstruction THIS    = new ALOAD(0);
     LocalVariableInstruction ALOAD_0 = THIS;
     LocalVariableInstruction ALOAD_1 = new ALOAD(1);
     LocalVariableInstruction ALOAD_2 = new ALOAD(2);
     LocalVariableInstruction ILOAD_0 = new ILOAD(0);
     LocalVariableInstruction ILOAD_1 = new ILOAD(1);
     LocalVariableInstruction ILOAD_2 = new ILOAD(2);
     LocalVariableInstruction ASTORE_0 = new ASTORE(0);
     LocalVariableInstruction ASTORE_1 = new ASTORE(1);
     LocalVariableInstruction ASTORE_2 = new ASTORE(2);
     LocalVariableInstruction ISTORE_0 = new ISTORE(0);
     LocalVariableInstruction ISTORE_1 = new ISTORE(1);
     LocalVariableInstruction ISTORE_2 = new ISTORE(2);
     
     /** Get object via its opcode, for immutable instructions like
      * branch instructions entries are set to null.
      */
     Instruction[] INSTRUCTIONS = new Instruction[256];
     
     /** Interfaces may have no static initializers, so we simulate this
      * with an inner class.
      */
     Clinit bla = new Clinit();
     
     class Clinit {
         Clinit() {
             INSTRUCTIONS[jq_ClassFileConstants.jbc_NOP] = NOP;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_ACONST_NULL] = ACONST_NULL;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_ICONST_M1] = ICONST_M1;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_ICONST_0] = ICONST_0;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_ICONST_1] = ICONST_1;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_ICONST_2] = ICONST_2;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_ICONST_3] = ICONST_3;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_ICONST_4] = ICONST_4;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_ICONST_5] = ICONST_5;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_LCONST_0] = LCONST_0;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_LCONST_1] = LCONST_1;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_FCONST_0] = FCONST_0;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_FCONST_1] = FCONST_1;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_FCONST_2] = FCONST_2;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_DCONST_0] = DCONST_0;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_DCONST_1] = DCONST_1;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_IALOAD] = IALOAD;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_LALOAD] = LALOAD;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_FALOAD] = FALOAD;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_DALOAD] = DALOAD;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_AALOAD] = AALOAD;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_BALOAD] = BALOAD;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_CALOAD] = CALOAD;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_SALOAD] = SALOAD;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_IASTORE] = IASTORE;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_LASTORE] = LASTORE;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_FASTORE] = FASTORE;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_DASTORE] = DASTORE;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_AASTORE] = AASTORE;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_BASTORE] = BASTORE;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_CASTORE] = CASTORE;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_SASTORE] = SASTORE;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_POP] = POP;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_POP2] = POP2;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_DUP] = DUP;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_DUP_X1] = DUP_X1;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_DUP_X2] = DUP_X2;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_DUP2] = DUP2;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_DUP2_X1] = DUP2_X1;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_DUP2_X2] = DUP2_X2;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_SWAP] = SWAP;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_IADD] = IADD;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_LADD] = LADD;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_FADD] = FADD;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_DADD] = DADD;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_ISUB] = ISUB;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_LSUB] = LSUB;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_FSUB] = FSUB;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_DSUB] = DSUB;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_IMUL] = IMUL;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_LMUL] = LMUL;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_FMUL] = FMUL;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_DMUL] = DMUL;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_IDIV] = IDIV;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_LDIV] = LDIV;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_FDIV] = FDIV;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_DDIV] = DDIV;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_IREM] = IREM;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_LREM] = LREM;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_FREM] = FREM;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_DREM] = DREM;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_INEG] = INEG;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_LNEG] = LNEG;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_FNEG] = FNEG;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_DNEG] = DNEG;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_ISHL] = ISHL;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_LSHL] = LSHL;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_ISHR] = ISHR;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_LSHR] = LSHR;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_IUSHR] = IUSHR;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_LUSHR] = LUSHR;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_IAND] = IAND;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_LAND] = LAND;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_IOR] = IOR;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_LOR] = LOR;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_IXOR] = IXOR;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_LXOR] = LXOR;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_I2L] = I2L;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_I2F] = I2F;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_I2D] = I2D;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_L2I] = L2I;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_L2F] = L2F;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_L2D] = L2D;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_F2I] = F2I;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_F2L] = F2L;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_F2D] = F2D;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_D2I] = D2I;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_D2L] = D2L;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_D2F] = D2F;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_I2B] = I2B;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_I2C] = I2C;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_I2S] = I2S;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_LCMP] = LCMP;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_FCMPL] = FCMPL;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_FCMPG] = FCMPG;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_DCMPL] = DCMPL;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_DCMPG] = DCMPG;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_IRETURN] = IRETURN;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_LRETURN] = LRETURN;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_FRETURN] = FRETURN;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_DRETURN] = DRETURN;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_ARETURN] = ARETURN;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_RETURN] = RETURN;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_ARRAYLENGTH] = ARRAYLENGTH;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_ATHROW] = ATHROW;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_MONITORENTER] = MONITORENTER;
             INSTRUCTIONS[jq_ClassFileConstants.jbc_MONITOREXIT] = MONITOREXIT;
         }
     }
 }
 
 interface Visitor {
     void visitStackInstruction(StackInstruction obj);
     void visitLocalVariableInstruction(LocalVariableInstruction obj);
     void visitBranchInstruction(BranchInstruction obj);
     void visitLoadClass(LoadClass obj);
     void visitFieldInstruction(FieldInstruction obj);
     void visitIfInstruction(IfInstruction obj);
     void visitConversionInstruction(ConversionInstruction obj);
     void visitPopInstruction(PopInstruction obj);
     void visitStoreInstruction(StoreInstruction obj);
     void visitTypedInstruction(TypedInstruction obj);
     void visitSelect(Select obj);
     void visitJsrInstruction(JsrInstruction obj);
     void visitGotoInstruction(GotoInstruction obj);
     void visitUnconditionalBranch(UnconditionalBranch obj);
     void visitPushInstruction(PushInstruction obj);
     void visitArithmeticInstruction(ArithmeticInstruction obj);
     void visitCPInstruction(CPInstruction obj);
     void visitInvokeInstruction(InvokeInstruction obj);
     void visitArrayInstruction(ArrayInstruction obj);
     void visitAllocationInstruction(AllocationInstruction obj);
     void visitReturnInstruction(ReturnInstruction obj);
     void visitFieldOrMethod(FieldOrMethod obj);
     void visitConstantPushInstruction(ConstantPushInstruction obj);
     void visitExceptionThrower(ExceptionThrower obj);
     void visitLoadInstruction(LoadInstruction obj);
     void visitVariableLengthInstruction(VariableLengthInstruction obj);
     void visitStackProducer(StackProducer obj);
     void visitStackConsumer(StackConsumer obj);
     void visitACONST_NULL(ACONST_NULL obj);
     void visitGETSTATIC(GETSTATIC obj);
     void visitIF_ICMPLT(IF_ICMPLT obj);
     void visitMONITOREXIT(MONITOREXIT obj);
     void visitIFLT(IFLT obj);
     void visitLSTORE(LSTORE obj);
     void visitPOP2(POP2 obj);
     void visitBASTORE(BASTORE obj);
     void visitISTORE(ISTORE obj);
     void visitCHECKCAST(CHECKCAST obj);
     void visitFCMPG(FCMPG obj);
     void visitI2F(I2F obj);
     void visitATHROW(ATHROW obj);
     void visitDCMPL(DCMPL obj);
     void visitARRAYLENGTH(ARRAYLENGTH obj);
     void visitDUP(DUP obj);
     void visitINVOKESTATIC(INVOKESTATIC obj);
     void visitLCONST(LCONST obj);
     void visitDREM(DREM obj);
     void visitIFGE(IFGE obj);
     void visitCALOAD(CALOAD obj);
     void visitLASTORE(LASTORE obj);
     void visitI2D(I2D obj);
     void visitDADD(DADD obj);
     void visitINVOKESPECIAL(INVOKESPECIAL obj);
     void visitIAND(IAND obj);
     void visitPUTFIELD(PUTFIELD obj);
     void visitILOAD(ILOAD obj);
     void visitDLOAD(DLOAD obj);
     void visitDCONST(DCONST obj);
     void visitNEW(NEW obj);
     void visitIFNULL(IFNULL obj);
     void visitLSUB(LSUB obj);
     void visitL2I(L2I obj);
     void visitISHR(ISHR obj);
     void visitTABLESWITCH(TABLESWITCH obj);
     void visitIINC(IINC obj);
     void visitDRETURN(DRETURN obj);
     void visitFSTORE(FSTORE obj);
     void visitDASTORE(DASTORE obj);
     void visitIALOAD(IALOAD obj);
     void visitDDIV(DDIV obj);
     void visitIF_ICMPGE(IF_ICMPGE obj);
     void visitLAND(LAND obj);
     void visitIDIV(IDIV obj);
     void visitLOR(LOR obj);
     void visitCASTORE(CASTORE obj);
     void visitFREM(FREM obj);
     void visitLDC(LDC obj);
     void visitBIPUSH(BIPUSH obj);
     void visitDSTORE(DSTORE obj);
     void visitF2L(F2L obj);
     void visitFMUL(FMUL obj);
     void visitLLOAD(LLOAD obj);
     void visitJSR(JSR obj);
     void visitFSUB(FSUB obj);
     void visitSASTORE(SASTORE obj);
     void visitALOAD(ALOAD obj);
     void visitDUP2_X2(DUP2_X2 obj);
     void visitRETURN(RETURN obj);
     void visitDALOAD(DALOAD obj);
     void visitSIPUSH(SIPUSH obj);
     void visitDSUB(DSUB obj);
     void visitL2F(L2F obj);
     void visitIF_ICMPGT(IF_ICMPGT obj);
     void visitF2D(F2D obj);
     void visitI2L(I2L obj);
     void visitIF_ACMPNE(IF_ACMPNE obj);
     void visitPOP(POP obj);
     void visitI2S(I2S obj);
     void visitIFEQ(IFEQ obj);
     void visitSWAP(SWAP obj);
     void visitIOR(IOR obj);
     void visitIREM(IREM obj);
     void visitIASTORE(IASTORE obj);
     void visitNEWARRAY(NEWARRAY obj);
     void visitINVOKEINTERFACE(INVOKEINTERFACE obj);
     void visitINEG(INEG obj);
     void visitLCMP(LCMP obj);
     void visitJSR_W(JSR_W obj);
     void visitMULTIANEWARRAY(MULTIANEWARRAY obj);
     void visitDUP_X2(DUP_X2 obj);
     void visitSALOAD(SALOAD obj);
     void visitIFNONNULL(IFNONNULL obj);
     void visitDMUL(DMUL obj);
     void visitIFNE(IFNE obj);
     void visitIF_ICMPLE(IF_ICMPLE obj);
     void visitLDC2_W(LDC2_W obj);
     void visitGETFIELD(GETFIELD obj);
     void visitLADD(LADD obj);
     void visitNOP(NOP obj);
     void visitFALOAD(FALOAD obj);
     void visitINSTANCEOF(INSTANCEOF obj);
     void visitIFLE(IFLE obj);
     void visitLXOR(LXOR obj);
     void visitLRETURN(LRETURN obj);
     void visitFCONST(FCONST obj);
     void visitIUSHR(IUSHR obj);
     void visitBALOAD(BALOAD obj);
     void visitDUP2(DUP2 obj);
     void visitIF_ACMPEQ(IF_ACMPEQ obj);
     void visitMONITORENTER(MONITORENTER obj);
     void visitLSHL(LSHL obj);
     void visitDCMPG(DCMPG obj);
     void visitD2L(D2L obj);
     void visitL2D(L2D obj);
     void visitRET(RET obj);
     void visitIFGT(IFGT obj);
     void visitIXOR(IXOR obj);
     void visitINVOKEVIRTUAL(INVOKEVIRTUAL obj);
     void visitFASTORE(FASTORE obj);
     void visitIRETURN(IRETURN obj);
     void visitIF_ICMPNE(IF_ICMPNE obj);
     void visitFLOAD(FLOAD obj);
     void visitLDIV(LDIV obj);
     void visitPUTSTATIC(PUTSTATIC obj);
     void visitAALOAD(AALOAD obj);
     void visitD2I(D2I obj);
     void visitIF_ICMPEQ(IF_ICMPEQ obj);
     void visitAASTORE(AASTORE obj);
     void visitARETURN(ARETURN obj);
     void visitDUP2_X1(DUP2_X1 obj);
     void visitFNEG(FNEG obj);
     void visitGOTO_W(GOTO_W obj);
     void visitD2F(D2F obj);
     void visitGOTO(GOTO obj);
     void visitISUB(ISUB obj);
     void visitF2I(F2I obj);
     void visitDNEG(DNEG obj);
     void visitICONST(ICONST obj);
     void visitFDIV(FDIV obj);
     void visitI2B(I2B obj);
     void visitLNEG(LNEG obj);
     void visitLREM(LREM obj);
     void visitIMUL(IMUL obj);
     void visitIADD(IADD obj);
     void visitLSHR(LSHR obj);
     void visitLOOKUPSWITCH(LOOKUPSWITCH obj);
     void visitDUP_X1(DUP_X1 obj);
     void visitFCMPL(FCMPL obj);
     void visitI2C(I2C obj);
     void visitLMUL(LMUL obj);
     void visitLUSHR(LUSHR obj);
     void visitISHL(ISHL obj);
     void visitLALOAD(LALOAD obj);
     void visitASTORE(ASTORE obj);
     void visitANEWARRAY(ANEWARRAY obj);
     void visitFRETURN(FRETURN obj);
     void visitFADD(FADD obj);
     void visitBREAKPOINT(BREAKPOINT obj);
 }
 
 abstract class EmptyVisitor implements Visitor {
     public void visitStackInstruction(StackInstruction obj) { }
     public void visitLocalVariableInstruction(LocalVariableInstruction obj) { }
     public void visitBranchInstruction(BranchInstruction obj) { }
     public void visitLoadClass(LoadClass obj) { }
     public void visitFieldInstruction(FieldInstruction obj) { }
     public void visitIfInstruction(IfInstruction obj) { }
     public void visitConversionInstruction(ConversionInstruction obj) { }
     public void visitPopInstruction(PopInstruction obj) { }
     public void visitJsrInstruction(JsrInstruction obj) { }
     public void visitGotoInstruction(GotoInstruction obj) { }
     public void visitStoreInstruction(StoreInstruction obj) { }
     public void visitTypedInstruction(TypedInstruction obj) { }
     public void visitSelect(Select obj) { }
     public void visitUnconditionalBranch(UnconditionalBranch obj) { }
     public void visitPushInstruction(PushInstruction obj) { }
     public void visitArithmeticInstruction(ArithmeticInstruction obj) { }
     public void visitCPInstruction(CPInstruction obj) { }
     public void visitInvokeInstruction(InvokeInstruction obj) { }
     public void visitArrayInstruction(ArrayInstruction obj) { }
     public void visitAllocationInstruction(AllocationInstruction obj) { }
     public void visitReturnInstruction(ReturnInstruction obj) { }
     public void visitFieldOrMethod(FieldOrMethod obj) { }
     public void visitConstantPushInstruction(ConstantPushInstruction obj) { }
     public void visitExceptionThrower(ExceptionThrower obj) { }
     public void visitLoadInstruction(LoadInstruction obj) { }
     public void visitVariableLengthInstruction(VariableLengthInstruction obj) { }
     public void visitStackProducer(StackProducer obj) { }
     public void visitStackConsumer(StackConsumer obj) { }
     public void visitACONST_NULL(ACONST_NULL obj) { }
     public void visitGETSTATIC(GETSTATIC obj) { }
     public void visitIF_ICMPLT(IF_ICMPLT obj) { }
     public void visitMONITOREXIT(MONITOREXIT obj) { }
     public void visitIFLT(IFLT obj) { }
     public void visitLSTORE(LSTORE obj) { }
     public void visitPOP2(POP2 obj) { }
     public void visitBASTORE(BASTORE obj) { }
     public void visitISTORE(ISTORE obj) { }
     public void visitCHECKCAST(CHECKCAST obj) { }
     public void visitFCMPG(FCMPG obj) { }
     public void visitI2F(I2F obj) { }
     public void visitATHROW(ATHROW obj) { }
     public void visitDCMPL(DCMPL obj) { }
     public void visitARRAYLENGTH(ARRAYLENGTH obj) { }
     public void visitDUP(DUP obj) { }
     public void visitINVOKESTATIC(INVOKESTATIC obj) { }
     public void visitLCONST(LCONST obj) { }
     public void visitDREM(DREM obj) { }
     public void visitIFGE(IFGE obj) { }
     public void visitCALOAD(CALOAD obj) { }
     public void visitLASTORE(LASTORE obj) { }
     public void visitI2D(I2D obj) { }
     public void visitDADD(DADD obj) { }
     public void visitINVOKESPECIAL(INVOKESPECIAL obj) { }
     public void visitIAND(IAND obj) { }
     public void visitPUTFIELD(PUTFIELD obj) { }
     public void visitILOAD(ILOAD obj) { }
     public void visitDLOAD(DLOAD obj) { }
     public void visitDCONST(DCONST obj) { }
     public void visitNEW(NEW obj) { }
     public void visitIFNULL(IFNULL obj) { }
     public void visitLSUB(LSUB obj) { }
     public void visitL2I(L2I obj) { }
     public void visitISHR(ISHR obj) { }
     public void visitTABLESWITCH(TABLESWITCH obj) { }
     public void visitIINC(IINC obj) { }
     public void visitDRETURN(DRETURN obj) { }
     public void visitFSTORE(FSTORE obj) { }
     public void visitDASTORE(DASTORE obj) { }
     public void visitIALOAD(IALOAD obj) { }
     public void visitDDIV(DDIV obj) { }
     public void visitIF_ICMPGE(IF_ICMPGE obj) { }
     public void visitLAND(LAND obj) { }
     public void visitIDIV(IDIV obj) { }
     public void visitLOR(LOR obj) { }
     public void visitCASTORE(CASTORE obj) { }
     public void visitFREM(FREM obj) { }
     public void visitLDC(LDC obj) { }
     public void visitBIPUSH(BIPUSH obj) { }
     public void visitDSTORE(DSTORE obj) { }
     public void visitF2L(F2L obj) { }
     public void visitFMUL(FMUL obj) { }
     public void visitLLOAD(LLOAD obj) { }
     public void visitJSR(JSR obj) { }
     public void visitFSUB(FSUB obj) { }
     public void visitSASTORE(SASTORE obj) { }
     public void visitALOAD(ALOAD obj) { }
     public void visitDUP2_X2(DUP2_X2 obj) { }
     public void visitRETURN(RETURN obj) { }
     public void visitDALOAD(DALOAD obj) { }
     public void visitSIPUSH(SIPUSH obj) { }
     public void visitDSUB(DSUB obj) { }
     public void visitL2F(L2F obj) { }
     public void visitIF_ICMPGT(IF_ICMPGT obj) { }
     public void visitF2D(F2D obj) { }
     public void visitI2L(I2L obj) { }
     public void visitIF_ACMPNE(IF_ACMPNE obj) { }
     public void visitPOP(POP obj) { }
     public void visitI2S(I2S obj) { }
     public void visitIFEQ(IFEQ obj) { }
     public void visitSWAP(SWAP obj) { }
     public void visitIOR(IOR obj) { }
     public void visitIREM(IREM obj) { }
     public void visitIASTORE(IASTORE obj) { }
     public void visitNEWARRAY(NEWARRAY obj) { }
     public void visitINVOKEINTERFACE(INVOKEINTERFACE obj) { }
     public void visitINEG(INEG obj) { }
     public void visitLCMP(LCMP obj) { }
     public void visitJSR_W(JSR_W obj) { }
     public void visitMULTIANEWARRAY(MULTIANEWARRAY obj) { }
     public void visitDUP_X2(DUP_X2 obj) { }
     public void visitSALOAD(SALOAD obj) { }
     public void visitIFNONNULL(IFNONNULL obj) { }
     public void visitDMUL(DMUL obj) { }
     public void visitIFNE(IFNE obj) { }
     public void visitIF_ICMPLE(IF_ICMPLE obj) { }
     public void visitLDC2_W(LDC2_W obj) { }
     public void visitGETFIELD(GETFIELD obj) { }
     public void visitLADD(LADD obj) { }
     public void visitNOP(NOP obj) { }
     public void visitFALOAD(FALOAD obj) { }
     public void visitINSTANCEOF(INSTANCEOF obj) { }
     public void visitIFLE(IFLE obj) { }
     public void visitLXOR(LXOR obj) { }
     public void visitLRETURN(LRETURN obj) { }
     public void visitFCONST(FCONST obj) { }
     public void visitIUSHR(IUSHR obj) { }
     public void visitBALOAD(BALOAD obj) { }
     public void visitDUP2(DUP2 obj) { }
     public void visitIF_ACMPEQ(IF_ACMPEQ obj) { }
     public void visitMONITORENTER(MONITORENTER obj) { }
     public void visitLSHL(LSHL obj) { }
     public void visitDCMPG(DCMPG obj) { }
     public void visitD2L(D2L obj) { }
     public void visitL2D(L2D obj) { }
     public void visitRET(RET obj) { }
     public void visitIFGT(IFGT obj) { }
     public void visitIXOR(IXOR obj) { }
     public void visitINVOKEVIRTUAL(INVOKEVIRTUAL obj) { }
     public void visitFASTORE(FASTORE obj) { }
     public void visitIRETURN(IRETURN obj) { }
     public void visitIF_ICMPNE(IF_ICMPNE obj) { }
     public void visitFLOAD(FLOAD obj) { }
     public void visitLDIV(LDIV obj) { }
     public void visitPUTSTATIC(PUTSTATIC obj) { }
     public void visitAALOAD(AALOAD obj) { }
     public void visitD2I(D2I obj) { }
     public void visitIF_ICMPEQ(IF_ICMPEQ obj) { }
     public void visitAASTORE(AASTORE obj) { }
     public void visitARETURN(ARETURN obj) { }
     public void visitDUP2_X1(DUP2_X1 obj) { }
     public void visitFNEG(FNEG obj) { }
     public void visitGOTO_W(GOTO_W obj) { }
     public void visitD2F(D2F obj) { }
     public void visitGOTO(GOTO obj) { }
     public void visitISUB(ISUB obj) { }
     public void visitF2I(F2I obj) { }
     public void visitDNEG(DNEG obj) { }
     public void visitICONST(ICONST obj) { }
     public void visitFDIV(FDIV obj) { }
     public void visitI2B(I2B obj) { }
     public void visitLNEG(LNEG obj) { }
     public void visitLREM(LREM obj) { }
     public void visitIMUL(IMUL obj) { }
     public void visitIADD(IADD obj) { }
     public void visitLSHR(LSHR obj) { }
     public void visitLOOKUPSWITCH(LOOKUPSWITCH obj) { }
     public void visitDUP_X1(DUP_X1 obj) { }
     public void visitFCMPL(FCMPL obj) { }
     public void visitI2C(I2C obj) { }
     public void visitLMUL(LMUL obj) { }
     public void visitLUSHR(LUSHR obj) { }
     public void visitISHL(ISHL obj) { }
     public void visitLALOAD(LALOAD obj) { }
     public void visitASTORE(ASTORE obj) { }
     public void visitANEWARRAY(ANEWARRAY obj) { }
     public void visitFRETURN(FRETURN obj) { }
     public void visitFADD(FADD obj) { }
     public void visitBREAKPOINT(BREAKPOINT obj) { }
 }
 
}
