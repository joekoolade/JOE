// RegisterFactory.java, created Fri Jan 11 16:42:38 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.Quad;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Method;
import joeq.Class.jq_Primitive;
import joeq.Class.jq_Reference;
import joeq.Class.jq_Type;
import joeq.Compiler.Quad.Operand.RegisterOperand;
import joeq.Runtime.TypeCheck;
import jwutil.collections.Pair;
import jwutil.util.Assert;

/**
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: RegisterFactory.java,v 1.21 2004/10/07 00:07:44 joewhaley Exp $
 */
public class RegisterFactory {

    private final ArrayList/*<Register>*/ registers;
    
    private final Map/*<Pair<jq_Type,Integer>,Register>*/ stackNumbering;
    private final Map/*<Pair<jq_Type,Integer>,Register>*/ localNumbering;
    
    //private RegisterFactory() { }

    /** Creates new RegisterFactory */
    public RegisterFactory(jq_Method m) {
        this(m.getMaxStack(), m.getMaxLocals());
    }
    
    public RegisterFactory(int nStack, int nLocal) {
        int capacity = (nStack + nLocal) * 2;
        registers = new ArrayList(capacity);
        stackNumbering = new HashMap(nStack);
        localNumbering = new HashMap(nLocal);
    }
    
    public Register get(int i) {
        return (Register) registers.get(i);
    }

    private short nextNumber() { return (short) registers.size(); }
    
    void skipNumbers(int v) {
        while (--v >= 0)
            registers.add(null);
    }
    
    public Register makeReg(jq_Type type) {
        Register r = new Register(nextNumber(), type, false);
        registers.add(r);
        return r;
    }
    public RegisterOperand makeRegOp(jq_Type type) {
        Register r = makeReg(type);
        registers.add(r);
        return new RegisterOperand(r, type);
    }
    public Register makeTempReg(jq_Type type) {
        Register r = new Register(nextNumber(), type, true);
        registers.add(r);
        return r;
    }
    public RegisterOperand makeTempRegOp(jq_Type type) {
        Register r = makeTempReg(type);
        registers.add(r);
        return new RegisterOperand(r, type);
    }
    public static RegisterOperand makeGuardReg() {
        return new RegisterOperand(new Register(), null);
    }
    public Register makeReg(Register r2) {
        Register r = r2.copy();
        r.index = nextNumber();
        registers.add(r);
        return r;
    }
    public RegisterOperand makeRegOp(Register r2, jq_Type type) {
        Register r = r2.copy();
        r.index = nextNumber();
        registers.add(r);
        Assert._assert(TypeCheck.isAssignable(type, r2.getType()));
        return new RegisterOperand(r, type);
    }
    public Register makePairedReg(RegisterFactory that, Register r2) {
        Assert._assert(this.size() == that.size());
        Register r = makeReg(r2);
        that.registers.add(r);
        return r;
    }

    public Register getOrCreateStack(int i, jq_Type t) {
        if (t.isReferenceType()) t = PrimordialClassLoader.getJavaLangObject();
        if (t.isIntLike()) t = jq_Primitive.INT;
        Pair p = new Pair(t, new Integer(i));
        Register r = (Register) stackNumbering.get(p);
        if (r == null) stackNumbering.put(p, r = makeTempReg(t));
        return r;
    }
    
    public Register getOrCreateLocal(int i, jq_Type t) {
        if (t.isReferenceType()) t = PrimordialClassLoader.getJavaLangObject();
        if (t.isIntLike()) t = jq_Primitive.INT;
        Pair p = new Pair(t, new Integer(i));
        Register r = (Register) localNumbering.get(p);
        if (r == null) localNumbering.put(p, r = makeReg(t));
        return r;
    }
    
    public void renumberRegisters(short n) {
        for (Iterator i = registers.iterator(); i.hasNext(); ) {
            Register r = (Register)i.next();
            r.setNumber((short)(r.getNumber()+n));
        }
        int oldSize = registers.size();
        for (int i = 0; i < n; ++i) {
            registers.add(null);
        }
        for (int i = oldSize - 1; i >= 0; --i) {
            registers.set(i + n, registers.get(i));
        }
        while (--n >= 0) {
            registers.set(n, null);
        }
    }

    public void addAll(RegisterFactory that) {
        for (Iterator i = that.registers.iterator(); i.hasNext(); ) {
            Register r = (Register)i.next();
            r.setNumber((short) nextNumber());
            this.registers.add(r);
        }
    }
    
    public RegisterFactory deepCopy() {
        RegisterFactory that = new RegisterFactory(this.stackNumbering.size(), this.localNumbering.size());
        deepCopyInto(that);
        return that;
    }
    
    public Map deepCopyInto(RegisterFactory that) {
        Map m = new HashMap();
        for (Iterator i = iterator(); i.hasNext(); ) {
            Register r = (Register) i.next();
            Register r2 = r.copy();
            that.registers.add(r2);
            m.put(r, r2);
        }
        renumber(m, this.stackNumbering, that.stackNumbering);
        renumber(m, this.localNumbering, that.localNumbering);
        return m;
    }
    
    private void renumber(Map map, Map fromNumbering, Map toNumbering) {
        for (Iterator i = fromNumbering.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry e = (Map.Entry) i.next();
            Pair p = (Pair) e.getKey();
            Register r = (Register) e.getValue();
            Register r2 = (Register) map.get(r);
            toNumbering.put(p, r2);
        }
    }

    public int numberOfStackRegisters() {
        return stackNumbering.size();
    }
    
    public int numberOfLocalRegisters() {
        return localNumbering.size();
    }
    
    public int size() {
        return registers.size();
    }
    
    public Iterator iterator() {
        return registers.iterator();
    }
    
    public String toString() {
        return "Registers: "+registers.size();
    }

    public String fullDump() {
        return "Registers: "+registers.toString();
    }

    public static class Register {
        private short index;
        private byte flags;
        public static final byte TEMP     = (byte)0x20;
        public static final byte SSA      = (byte)0x40;
        public static final byte TYPEMASK = (byte)0x07;
        public static final byte INT      = (byte)0x01;
        public static final byte FLOAT    = (byte)0x02;
        public static final byte LONG     = (byte)0x03;
        public static final byte DOUBLE   = (byte)0x04;
        public static final byte OBJECT   = (byte)0x05;
        public static final byte GUARD    = (byte)0x06;
        public static final byte PHYSICAL = (byte)0x07;
        private Register() {
            this.index = -1;
            this.flags = GUARD | TEMP;
        }
        private Register(short id, byte flags) {
            this.index = id;
            this.flags = flags;
        }
        private Register(short id, jq_Type type, boolean isTemp) {
            this.index = id;
            if (isTemp) flags = TEMP;
            if (type instanceof jq_Reference) flags |= OBJECT;
            else if (type.isIntLike()) flags |= INT;
            else if (type == jq_Primitive.FLOAT) flags |= FLOAT;
            else if (type == jq_Primitive.LONG) flags |= LONG;
            else if (type == jq_Primitive.DOUBLE) flags |= DOUBLE;
            else Assert.UNREACHABLE(type.toString());
        }
        public int getNumber() {
            return index;
        }
        public void setNumber(short id) {
            this.index = id;
        }
        public boolean isTemp() {
            return (flags & TEMP) != 0;
        }
        public void setSSA() {
            flags |= SSA;
        }
        public void clearSSA() {
            flags &= ~SSA;
        }
        public boolean isSSA() {
            return (flags & SSA) != 0;
        }
        public boolean isGuard() {
            return (flags & TYPEMASK) == GUARD;
        }
        public boolean isPhysical() {
            return (flags & TYPEMASK) == PHYSICAL;
        }
        public jq_Type getType() {
            int t = flags & TYPEMASK;
            switch (t) {
                case INT:
                    return jq_Primitive.INT;
                case FLOAT:
                    return jq_Primitive.FLOAT;
                case LONG:
                    return jq_Primitive.LONG;
                case DOUBLE:
                    return jq_Primitive.DOUBLE;
                case OBJECT:
                    return PrimordialClassLoader.getJavaLangObject();
            }
            return null;
        }
        public String toString() {
            return (isTemp()?"T":"R")+index;
        }
        public Register copy() {
            return new Register(this.index, this.flags);
        }
    }

}
