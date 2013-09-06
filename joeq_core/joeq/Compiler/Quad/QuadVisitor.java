// QuadVisitor.java, created Fri Jan 11 16:42:38 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.Quad;

/**
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: QuadVisitor.java,v 1.9 2004/03/26 23:20:37 joewhaley Exp $
 */
public interface QuadVisitor {

    /** A potentially excepting instruction.. */
    void visitExceptionThrower(Quad obj);
    /** An instruction that loads from memory. */
    void visitLoad(Quad obj);
    /** An instruction that stores into memory. */
    void visitStore(Quad obj);
    /** An instruction that may branch (not including exceptional control flow). */
    void visitBranch(Quad obj);
    /** A conditional branch instruction. */
    void visitCondBranch(Quad obj);
    /** An exception check instruction. */
    void visitCheck(Quad obj);
    /** An instruction.that accesses a static field. */
    void visitStaticField(Quad obj);
    /** An instruction.that accesses an instance field. */
    void visitInstanceField(Quad obj);
    /** An instruction.that accesses an array. */
    void visitArray(Quad obj);
    /** An instruction.that does an allocation. */
    void visitAllocation(Quad obj);
    /** An instruction.that does a type check. */
    void visitTypeCheck(Quad obj);
    
    /** An array load instruction. */
    void visitALoad(Quad obj);
    /** An array store instruction. */
    void visitAStore(Quad obj);
    /** An array length instruction. */
    void visitALength(Quad obj);
    /** A binary operation instruction. */
    void visitBinary(Quad obj);
    /** An array bounds check instruction. */
    void visitBoundsCheck(Quad obj);
    /** A type cast check instruction. */
    void visitCheckCast(Quad obj);
    /** A get instance field instruction. */
    void visitGetfield(Quad obj);
    /** A get static field instruction. */
    void visitGetstatic(Quad obj);
    /** A goto instruction. */
    void visitGoto(Quad obj);
    /** A type instance of instruction. */
    void visitInstanceOf(Quad obj);
    /** A compare and branch instruction. */
    void visitIntIfCmp(Quad obj);
    /** An invoke instruction. */
    void visitInvoke(Quad obj);
    /** A jump local subroutine instruction. */
    void visitJsr(Quad obj);
    /** A lookup switch instruction. */
    void visitLookupSwitch(Quad obj);
    /** A raw memory load instruction. */
    void visitMemLoad(Quad obj);
    /** A raw memory store instruction. */
    void visitMemStore(Quad obj);
    /** An object monitor lock/unlock instruction. */
    void visitMonitor(Quad obj);
    /** A register move instruction. */
    void visitMove(Quad obj);
    /** An object allocation instruction. */
    void visitNew(Quad obj);
    /** An array allocation instruction. */
    void visitNewArray(Quad obj);
    /** A null pointer check instruction. */
    void visitNullCheck(Quad obj);
    /** A phi instruction. (For SSA.) */
    void visitPhi(Quad obj);
    /** A put instance field instruction. */
    void visitPutfield(Quad obj);
    /** A put static field instruction. */
    void visitPutstatic(Quad obj);
    /** A return from local subroutine instruction. */
    void visitRet(Quad obj);
    /** A return from method instruction. */
    void visitReturn(Quad obj);
    /** A special instruction. */
    void visitSpecial(Quad obj);
    /** An object array store type check instruction. */
    void visitStoreCheck(Quad obj);
    /** A jump table switch instruction. */
    void visitTableSwitch(Quad obj);
    /** A unary operation instruction. */
    void visitUnary(Quad obj);
    /** A divide-by-zero check instruction. */
    void visitZeroCheck(Quad obj);
    
    /** Any quad. */
    void visitQuad(Quad obj);
    
    abstract class EmptyVisitor implements QuadVisitor {
        /** A potentially excepting instruction.. */
        public void visitExceptionThrower(Quad obj) {}
        /** An instruction that loads from memory. */
        public void visitLoad(Quad obj) {}
        /** An instruction that stores into memory. */
        public void visitStore(Quad obj) {}
        /** An instruction that may branch (not including exceptional control flow). */
        public void visitBranch(Quad obj) {}
        /** A conditional branch instruction. */
        public void visitCondBranch(Quad obj) {}
        /** An exception check instruction. */
        public void visitCheck(Quad obj) {}
        /** An instruction.that accesses a static field. */
        public void visitStaticField(Quad obj) {}
        /** An instruction.that accesses an instance field. */
        public void visitInstanceField(Quad obj) {}
        /** An instruction.that accesses an array. */
        public void visitArray(Quad obj) {}
        /** An instruction.that does an allocation. */
        public void visitAllocation(Quad obj) {}
        /** An instruction.that does a type check. */
        public void visitTypeCheck(Quad obj) {}

        /** An array load instruction. */
        public void visitALoad(Quad obj) {}
        /** An array store instruction. */
        public void visitAStore(Quad obj) {}
        /** An array length instruction. */
        public void visitALength(Quad obj) {}
        /** A binary operation instruction. */
        public void visitBinary(Quad obj) {}
        /** An array bounds check instruction. */
        public void visitBoundsCheck(Quad obj) {}
        /** A type cast check instruction. */
        public void visitCheckCast(Quad obj) {}
        /** A get instance field instruction. */
        public void visitGetfield(Quad obj) {}
        /** A get static field instruction. */
        public void visitGetstatic(Quad obj) {}
        /** A goto instruction. */
        public void visitGoto(Quad obj) {}
        /** A type instance of instruction. */
        public void visitInstanceOf(Quad obj) {}
        /** A compare and branch instruction. */
        public void visitIntIfCmp(Quad obj) {}
        /** An invoke instruction. */
        public void visitInvoke(Quad obj) {}
        /** A jump local subroutine instruction. */
        public void visitJsr(Quad obj) {}
        /** A lookup switch instruction. */
        public void visitLookupSwitch(Quad obj) {}
        /** A raw memory load instruction. */
        public void visitMemLoad(Quad obj) {}
        /** A raw memory store instruction. */
        public void visitMemStore(Quad obj) {}
        /** An object monitor lock/unlock instruction. */
        public void visitMonitor(Quad obj) {}
        /** A register move instruction. */
        public void visitMove(Quad obj) {}
        /** An object allocation instruction. */
        public void visitNew(Quad obj) {}
        /** An array allocation instruction. */
        public void visitNewArray(Quad obj) {}
        /** A null pointer check instruction. */
        public void visitNullCheck(Quad obj) {}
        /** A phi instruction. (For SSA.) */
        public void visitPhi(Quad obj) {}
        /** A put instance field instruction. */
        public void visitPutfield(Quad obj) {}
        /** A put static field instruction. */
        public void visitPutstatic(Quad obj) {}
        /** A return from local subroutine instruction. */
        public void visitRet(Quad obj) {}
        /** A return from method instruction. */
        public void visitReturn(Quad obj) {}
        /** A special instruction. */
        public void visitSpecial(Quad obj) {}
        /** An object array store type check instruction. */
        public void visitStoreCheck(Quad obj) {}
        /** A jump table switch instruction. */
        public void visitTableSwitch(Quad obj) {}
        /** A unary operation instruction. */
        public void visitUnary(Quad obj) {}
        /** A divide-by-zero check instruction. */
        public void visitZeroCheck(Quad obj) {}
        
        /** Any quad. */
        public void visitQuad(Quad obj) {}
    }
    
    class AllQuadVisitor implements BasicBlockVisitor {
        final QuadVisitor qv;
        boolean trace;
        public AllQuadVisitor(QuadVisitor qv) { this.qv = qv; }
        public AllQuadVisitor(QuadVisitor qv, boolean trace) { this.qv = qv; this.trace = trace; }
        public void visitBasicBlock(BasicBlock bb) {
            if (trace) System.out.println(bb.toString());
            bb.visitQuads(qv);
        }
    }
    
}

