// x86Assembler.java, created Mon Feb  5 23:23:19 2001 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Assembler.x86;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import joeq.Allocator.DefaultCodeAllocator;
import joeq.Allocator.CodeAllocator.x86CodeBuffer;
import joeq.Main.jq;
import joeq.Memory.CodeAddress;
import jwutil.collections.LightRelation;
import jwutil.collections.Relation;
import jwutil.strings.Strings;
import jwutil.util.Assert;

// Referenced classes of package joeq.Assembler.x86:
//            x86Constants, x86CodeBuffer, x86

/**
 * x86Assembler
 *
 * @author John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: x86Assembler.java,v 1.18 2004/09/30 03:37:06 joewhaley Exp $
 */
public class x86Assembler implements x86Constants {

    static class PatchInfo {

        int patchLocation, patchSize;
        
        PatchInfo(int patchLocation, int patchSize) {
            this.patchLocation = patchLocation;
            this.patchSize = patchSize;
        }
        
        void patchTo(x86CodeBuffer mc, int target) {
            if (patchSize == 4) {
                int v = mc.get4_endian(patchLocation - 4);
                Assert._assert(v == 0x44444444 || v == 0x55555555 || v == 0x66666666 || v == 0x77777777, "Location: "+Strings.hex(patchLocation-4)+" value: "+Strings.hex8(v));
                mc.put4_endian(patchLocation - 4, target - patchLocation);
            } else if (patchSize == 1) {
                byte v = mc.get1(patchLocation - 1);
                Assert._assert(v == 0);
                Assert._assert(target - patchLocation <= 127);
                Assert._assert(target - patchLocation >= -128);
                mc.put1(patchLocation - 1, (byte)(target - patchLocation));
            } else
                Assert.TODO();
        }
        
        public String toString() {
            return "loc:"+Strings.hex(patchLocation)+" size:"+patchSize;
        }

    }

    static class AbsPatchInfo extends PatchInfo {

        AbsPatchInfo(int patchLocation, int patchSize) {
            super(patchLocation, patchSize);
        }
        
        void patchTo(x86CodeBuffer mc, int target) {
            if (patchSize == 4) {
                int v = mc.get4_endian(patchLocation - 4);
                Assert._assert(v == 0x44444444 || v == 0x55555555 || v == 0x66666666 || v == 0x77777777, "Location: "+Strings.hex(patchLocation-4)+" value: "+Strings.hex8(v));
                mc.put4_endian(patchLocation - 4, mc.getStartAddress().offset(target).to32BitValue());
            } else
                Assert.TODO();
        }
        
        public String toString() {
            return "loc:"+Strings.hex(patchLocation)+" size:"+patchSize+" (abs)";
        }

    }
    
    public x86CodeBuffer getCodeBuffer() {
        if (!branches_to_patch.isEmpty())
            System.out.println("Error: unresolved forward branches!");
        return mc;
    }
    public int getCurrentOffset() { return mc.getCurrentOffset(); }
    public CodeAddress getCurrentAddress() { return mc.getCurrentAddress(); }
    public CodeAddress getStartAddress() { return mc.getStartAddress(); }
    public void patch1(int offset, byte value) { mc.put1(offset, value); }
    public void patch4_endian(int offset, int value) { mc.put4_endian(offset, value); }

    public x86Assembler(int num_targets, int est_size, int offset, int alignment) {
        mc = DefaultCodeAllocator.getCodeBuffer(est_size, offset, alignment);
        if (TRACE) System.out.println("Assembler start address: "+mc.getCurrentAddress().stringRep());
        branchtargetmap = new HashMap();
        branches_to_patch = new LightRelation();
    }

    public boolean containsTarget(Object target) {
        return branchtargetmap.containsKey(target);
    }
    // backward branches
    public void recordBranchTarget(Object target) {
        Assert._assert(ip == mc.getCurrentOffset());
        branchtargetmap.put(target, new Integer(ip));
    }
    public int getBranchTarget(Object target) {
        Integer i = (Integer)branchtargetmap.get(target);
        if (i == null) {
            Assert.UNREACHABLE("Invalid branch target: "+target+" offset "+getCurrentOffset());
        }
        return i.intValue();
    }
    public Map getBranchTargetMap() {
        return branchtargetmap;
    }

    // forward branches
    public void recordForwardBranch(int patchsize, Object target) {
        if (TRACE) System.out.println("recording forward branch from "+Strings.hex(ip)+" (size "+patchsize+") to "+target);
        branches_to_patch.add(target, new PatchInfo(ip, patchsize));
    }
    public void recordAbsoluteReference(int patchsize, Object target) {
        if (TRACE) System.out.println("recording absolute reference from "+Strings.hex(ip)+" (size "+patchsize+") to "+target);
        branches_to_patch.add(target, new AbsPatchInfo(ip, patchsize));
    }
    public void resolveForwardBranches(Object target) {
        PatchInfo p;
        Iterator it = branches_to_patch.getValues(target).iterator();
        while (it.hasNext()) {
            p = (PatchInfo)it.next();
            if (TRACE) System.out.println("patching branch to "+target+" ("+p+") to point to "+Strings.hex(ip));
            p.patchTo(mc, ip);
        }
        branches_to_patch.removeKey(target);
    }

    // dynamic patch section
    public void startDynamicPatch(int size) {
        if (jq.SMP) {
            int end = ip+size;
            int mask = CACHE_LINE_SIZE-1;
            while ((ip & mask) != (end & mask))
                emit1(x86.NOP);
        }
        dynPatchStart = ip;
        dynPatchSize = size;
    }
    public void endDynamicPatch() {
        Assert._assert(ip <= dynPatchStart + dynPatchSize);
        while (ip < dynPatchStart + dynPatchSize) 
            emit1(x86.NOP);
        dynPatchSize = 0;
    }

    // prefix
    public void emitprefix(byte prefix) {
        mc.add1(prefix);
        ++ip;
    }

    // special case instructions
    public void emitPUSH_i(int imm) {
        if (fits(imm, 8))
            ip += x86.PUSH_i8.emit1_Imm8(mc, imm);
        else
            ip += x86.PUSH_i32.emit1_Imm32(mc, imm);
    }
    public void emit2_SHIFT_Mem_Imm8(x86 x, int off, int base, byte imm) {
        if (base == ESP) {
            if (off == 0) {
                if (imm == 1)
                    ip += x.emit2_Once_SIB_EA(mc, ESP, ESP, SCALE_1);
                else
                    ip += x.emit2_SIB_EA_Imm8(mc, ESP, ESP, SCALE_1, imm);
            } else if (fits_signed(off, 8)) {
                if (imm == 1)
                    ip += x.emit2_Once_SIB_DISP8(mc, ESP, ESP, SCALE_1, (byte)off);
                else
                    ip += x.emit2_SIB_DISP8_Imm8(mc, ESP, ESP, SCALE_1, (byte)off, imm);
            } else {
                if (imm == 1)
                    ip += x.emit2_Once_SIB_DISP32(mc, ESP, ESP, SCALE_1, off);
                else
                    ip += x.emit2_SIB_DISP32_Imm8(mc, ESP, ESP, SCALE_1, off, imm);
            }
        } else if (off == 0 && base != EBP) {
            if (imm == 1)
                ip += x.emit2_Once_EA(mc, base);
            else
                ip += x.emit2_EA_Imm8(mc, base, imm);
        } else if (fits_signed(off, 8)) {
            if (imm == 1)
                ip += x.emit2_Once_DISP8(mc, (byte)off, base);
            else
                ip += x.emit2_DISP8_Imm8(mc, (byte)off, base, imm);
        } else {
            if (imm == 1)
                ip += x.emit2_Once_DISP32(mc, off, base);
            else
                ip += x.emit2_DISP32_Imm8(mc, off, base, imm);
        }
    }

    public void emit2_SHIFT_Reg_Imm8(x86 x, int r1, byte imm) {
        if (imm == 1)
            ip += x.emit2_Once_Reg(mc, r1);
        else
            ip += x.emit2_Reg_Imm8(mc, r1, imm);
    }

    // swap the order, because it is confusing.
    public void emitSHLD_r_r_rc(int r1, int r2) {
        ip += x86.SHLD_r_r_rc.emit3_Reg_Reg(mc, r2, r1);
    }

    // swap the order, because it is confusing.
    public void emitSHRD_r_r_rc(int r1, int r2) {
        ip += x86.SHRD_r_r_rc.emit3_Reg_Reg(mc, r2, r1);
    }

    // short
    public void emitShort_Reg(x86 x, int r1) {
        ip += x.emitShort_Reg(mc, r1);
    }
    public void emitShort_Reg_Imm(x86 x, int r1, int imm) {
        ip += x.emitShort_Reg_Imm32(mc, r1, imm);
    }

    // length 1
    public void emit1(x86 x) {
        ip += x.emit1(mc);
    }
    public void emit1_Imm8(x86 x, byte imm) {
        ip += x.emit1_Imm8(mc, imm);
    }
    public void emit1_Imm16(x86 x, char imm) {
        ip += x.emit1_Imm16(mc, imm);
    }
    public void emit1_Imm32(x86 x, int imm) {
        ip += x.emit1_Imm32(mc, imm);
    }

    // length 2
    public void emit2(x86 x) {
        ip += x.emit2(mc);
    }
    public void emit2_FPReg(x86 x, int r) {
        ip += x.emit2_FPReg(mc, r);
    }
    public void emit2_Mem(x86 x, int imm) {
        ip += x.emit2_Abs32(mc, imm);
    }
    public void emit2_Mem(x86 x, int off, int base) {
        if (base == ESP) {
            if (off == 0)
                ip += x.emit2_SIB_EA(mc, ESP, ESP, SCALE_1);
            else if (fits_signed(off, 8))
                ip += x.emit2_SIB_DISP8(mc, ESP, ESP, SCALE_1, (byte)off);
            else
                ip += x.emit2_SIB_DISP32(mc, ESP, ESP, SCALE_1, off);
        } else if (off == 0 && base != EBP)
            ip += x.emit2_EA(mc, base);
        else if (fits_signed(off, 8))
            ip += x.emit2_DISP8(mc, (byte)off, base);
        else
            ip += x.emit2_DISP32(mc, off, base);
    }
    public void emit2_Mem(x86 x, int base, int ind, int scale, int off) {
        Assert._assert(ind != ESP);
        Assert._assert(base != ESP);
        if (off == 0)
            ip += x.emit2_SIB_EA(mc, base, ind, scale);
        else if (fits_signed(off, 8))
            ip += x.emit2_SIB_DISP8(mc, base, ind, scale, (byte)off);
        else
            ip += x.emit2_SIB_DISP32(mc, base, ind, scale, off);
    }
    public void emit2_Mem_Imm(x86 x, int off, int base, int imm) {
        if (base == ESP) {
            if (off == 0)
                ip += x.emit2_SIB_EA_Imm32(mc, ESP, ESP, SCALE_1, imm);
            else if (fits_signed(off, 8))
                ip += x.emit2_SIB_DISP8_Imm32(mc, ESP, ESP, SCALE_1, (byte)off, imm);
            else
                ip += x.emit2_SIB_DISP32_Imm32(mc, ESP, ESP, SCALE_1, off, imm);
        } else if (off == 0 && base != EBP)
            ip += x.emit2_EA_Imm32(mc, base, imm);
        else if (fits_signed(off, 8))
            ip += x.emit2_DISP8_Imm32(mc, (byte)off, base, imm);
        else
            ip += x.emit2_DISP32_Imm32(mc, off, base, imm);
    }
    public void emit2_Reg(x86 x, int r1) {
        ip += x.emit2_Reg(mc, r1);
    }
    public void emit2_Reg_Mem(x86 x, int r1, int addr) {
        ip += x.emit2_Reg_Abs32(mc, r1, addr);
    }
    public void emit2_Reg_Mem(x86 x, int r1, int off, int base) {
        if (base == ESP) {
            if (off == 0)
                ip += x.emit2_Reg_SIB_EA(mc, r1, ESP, ESP, SCALE_1);
            else if (fits_signed(off, 8))
                ip += x.emit2_Reg_SIB_DISP8(mc, r1, ESP, ESP, SCALE_1, (byte)off);
            else
                ip += x.emit2_Reg_SIB_DISP32(mc, r1, ESP, ESP, SCALE_1, off);
        } else if (off == 0 && base != EBP)
            ip += x.emit2_Reg_EA(mc, r1, base);
        else if (fits_signed(off, 8))
            ip += x.emit2_Reg_DISP8(mc, r1, (byte)off, base);
        else
            ip += x.emit2_Reg_DISP32(mc, r1, off, base);
    }
    public void emit2_Reg_Mem(x86 x, int r1, int base, int ind, int scale, int off) {
        if (off == 0)
            ip += x.emit2_Reg_SIB_EA(mc, r1, base, ind, scale);
        else if (fits_signed(off, 8))
            ip += x.emit2_Reg_SIB_DISP8(mc, r1, base, ind, scale, (byte)off);
        else
            ip += x.emit2_Reg_SIB_DISP32(mc, r1, base, ind, scale, off);
    }
    public void emit2_Reg_Reg(x86 x, int r1, int r2) {
        ip += x.emit2_Reg_Reg(mc, r1, r2);
    }

    // length 3
    public void emit3_Reg_Reg(x86 x, int r1, int r2) {
        ip += x.emit3_Reg_Reg(mc, r1, r2);
    }
    public void emit3_Reg_Mem(x86 x, int r1, int addr) {
        ip += x.emit3_Reg_Abs32(mc, r1, addr);
    }
    public void emit3_Reg_Mem(x86 x, int r1, int off, int base) {
        if (base == ESP) {
            if (off == 0)
                ip += x.emit3_Reg_SIB_EA(mc, r1, ESP, ESP, SCALE_1);
            else if (fits_signed(off, 8))
                ip += x.emit3_Reg_SIB_DISP8(mc, r1, ESP, ESP, SCALE_1, (byte)off);
            else
                ip += x.emit3_Reg_SIB_DISP32(mc, r1, ESP, ESP, SCALE_1, off);
        } else if (off == 0 && base != EBP)
            ip += x.emit3_Reg_EA(mc, r1, base);
        else if (fits_signed(off, 8))
            ip += x.emit3_Reg_DISP8(mc, r1, (byte)off, base);
        else
            ip += x.emit3_Reg_DISP32(mc, r1, off, base);
    }
    public void emit3_Reg_Mem(x86 x, int r1, int base, int ind, int mult, int off) {
        if (off == 0)
            ip += x.emit3_Reg_SIB_EA(mc, r1, base, ind, mult);
        else if (fits_signed(off, 8))
            ip += x.emit3_Reg_SIB_DISP8(mc, r1, base, ind, mult, (byte)off);
        else
            ip += x.emit3_Reg_SIB_DISP32(mc, r1, base, ind, mult, off);
    }
    
    // arithmetic (with special EAX, Imm forms and 8-bit sign-extended immediates)
    public void emitARITH_Mem_Imm(x86 x, int off, int base, int imm) {
        if (base == ESP) {
            if (off == 0) {
                if (x != x86.TEST_r_i32 && fits_signed(imm, 8))
                    ip += x.emit2_SIB_EA_SEImm8(mc, ESP, ESP, SCALE_1, (byte)imm);
                else
                    ip += x.emit2_SIB_EA_Imm32(mc, ESP, ESP, SCALE_1, imm);
            } else if (fits_signed(off, 8)) {
                if (x != x86.TEST_r_i32 && fits_signed(imm, 8))
                    ip += x.emit2_SIB_DISP8_SEImm8(mc, ESP, ESP, SCALE_1, (byte)off, (byte)imm);
                else
                    ip += x.emit2_SIB_DISP8_Imm32(mc, ESP, ESP, SCALE_1, (byte)off, imm);
            } else {
                if (x != x86.TEST_r_i32 && fits_signed(imm, 8))
                    ip += x.emit2_SIB_DISP32_SEImm8(mc, ESP, ESP, SCALE_1, off, (byte)imm);
                else
                    ip += x.emit2_SIB_DISP32_Imm32(mc, ESP, ESP, SCALE_1, off, imm);
            }
        } else if (off == 0 && base != 5) {
            if (x != x86.TEST_r_i32 && fits_signed(imm, 8))
                ip += x.emit2_EA_SEImm8(mc, base, (byte)imm);
            else
                ip += x.emit2_EA_Imm32(mc, base, imm);
        } else if (fits_signed(off, 8)) {
            if (x != x86.TEST_r_i32 && fits_signed(imm, 8))
                ip += x.emit2_DISP8_SEImm8(mc, (byte)off, base, (byte)imm);
            else
                ip += x.emit2_DISP8_Imm32(mc, (byte)off, base, imm);
        } else {
            if (x != x86.TEST_r_i32 && fits_signed(imm, 8))
                ip += x.emit2_DISP32_SEImm8(mc, off, base, (byte)imm);
            else
                ip += x.emit2_DISP32_Imm32(mc, off, base, imm);
        }
    }
    public void emitARITH_Reg_Imm(x86 x, int r1, int imm) {
        //if (r1 == EAX)
        //    ip += x.emit1_RA_Imm32(mc, imm);
        //else
        if (x != x86.TEST_r_i32 && fits_signed(imm, 8))
            ip += x.emit2_Reg_SEImm8(mc, r1, (byte)imm);
        else
            ip += x.emit2_Reg_Imm32(mc, r1, imm);
    }
    public void emitARITH_Reg_Reg(x86 x, int r1, int r2) {
        ip += x.emit2_Reg_Reg(mc, r1, r2);
    }
    public void emitARITH_Reg_Mem(x86 x, int r1, int off, int base) {
        if (base == ESP) {
            if (off == 0)
                ip += x.emit2_Reg_SIB_EA(mc, r1, ESP, ESP, SCALE_1);
            else if (fits_signed(off, 8))
                ip += x.emit2_Reg_SIB_DISP8(mc, r1, ESP, ESP, SCALE_1, (byte)off);
            else
                ip += x.emit2_Reg_SIB_DISP32(mc, r1, ESP, ESP, SCALE_1, off);
        } else if (off == 0 && base != 5)
            ip += x.emit2_Reg_EA(mc, r1, base);
        else if (fits_signed(off, 8))
            ip += x.emit2_Reg_DISP8(mc, r1, (byte)off, base);
        else
            ip += x.emit2_Reg_DISP32(mc, r1, off, base);
    }

    // conditional jumps
    public void emitCJUMP_Back(x86 x, Object target) {
        Assert._assert(x.length == 1);
        int offset = getBranchTarget(target) - ip - 2;
        if (offset >= -128) {
            if (TRACE) System.out.println("Short cjump back from offset "+Strings.hex(ip+2)+" to "+target+" offset "+getBranchTarget(target)+" (relative offset "+Strings.shex(offset)+")");
            ip += x.emitCJump_Short(mc, (byte)offset);
        } else {
            if (TRACE) System.out.println("Near cjump back from offset "+Strings.hex(ip+6)+" to "+target+" offset "+getBranchTarget(target)+" (relative offset "+Strings.shex(offset-4)+")");
            ip += x.emitCJump_Near(mc, offset - 4);
        }
    }
    public void emitCJUMP_Short(x86 x, byte offset) {
        Assert._assert(x.length == 1);
        ip += x.emitCJump_Short(mc, offset);
    }
    public void emitCJUMP_Forw_Short(x86 x, Object target) {
        Assert._assert(x.length == 1);
        ip += x.emitCJump_Short(mc, (byte)0);
        recordForwardBranch(1, target);
    }
    public void emitCJUMP_Forw(x86 x, Object target) {
        Assert._assert(x.length == 1);
        ip += x.emitCJump_Near(mc, 0x66666666);
        recordForwardBranch(4, target);
    }

    // unconditional jumps
    public void emitJUMP_Back(x86 x, Object target) {
        Assert._assert(x.length == 1);
        int offset = getBranchTarget(target) - ip - 2;
        if(offset >= -128) {
            if (TRACE) System.out.println("Short jump back from offset "+Strings.hex(ip+2)+" to "+target+" offset "+getBranchTarget(target)+" (relative offset "+Strings.shex(offset)+")");
            ip += x.emitJump_Short(mc, (byte)offset);
        } else {
            if (TRACE) System.out.println("Near jump back from offset "+Strings.hex(ip+5)+" to "+target+" offset "+getBranchTarget(target)+" (relative offset "+Strings.shex(offset-3)+")");
            ip += x.emitJump_Near(mc, offset - 3);
        }
    }
    public void emitJUMP_Short(x86 x, byte offset) {
        Assert._assert(x.length == 1);
        ip += x.emitJump_Short(mc, offset);
    }
    public void emitJUMP_Forw_Short(x86 x, Object target) {
        Assert._assert(x.length == 1);
        ip += x.emitJump_Short(mc, (byte)0);
        recordForwardBranch(1, target);
    }
    public void emitJUMP_Forw(x86 x, Object target) {
        Assert._assert(x.length == 1);
        ip += x.emitJump_Near(mc, 0x55555555);
        recordForwardBranch(4, target);
    }

    // relative calls
    public void emitCALL_rel32(x86 x, int address) {
        Assert._assert(x.length == 1);
        ip += x.emitCall_Near(mc, address);
    }
    public void emitCALL_Back(x86 x, Object target) {
        Assert._assert(x.length == 1);
        int offset = getBranchTarget(target) - ip - 5;
        ip += x.emitCall_Near(mc, offset);
    }
    public void emitCALL_Forw(x86 x, Object target) {
        Assert._assert(x.length == 1);
        ip += x.emitCall_Near(mc, 0x44444444);
        recordForwardBranch(4, target);
    }
    
    public void emitDATA(int data) {
        mc.add4_endian(data);
        ip += 4;
    }

    public void skip(int nbytes) {
        if (TRACE) System.out.println("skipping "+nbytes+" bytes");
        mc.skip(nbytes);
        ip += nbytes;
    }
    
    public void setEntrypoint() {
        mc.setEntrypoint();
    }
    
    public static boolean fits(int val, int bits) {
        val >>= bits - 1;
        return val == 0;
    }

    public static boolean fits_signed(int val, int bits) {
        val >>= bits - 1;
        return val == 0 || val == -1;
    }

    public static /*final*/ boolean TRACE = false;
    
    private int ip;                     // current instruction pointer
    private x86CodeBuffer mc;           // code repository
    private Map/*<Object,Integer>*/ branchtargetmap;
    private Relation/*<Object,Set<PatchInfo>>*/ branches_to_patch;
    private int dynPatchStart, dynPatchSize;
}
