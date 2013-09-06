// Operand.java, created Fri Jan 11 16:42:38 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.Quad;

import joeq.Class.jq_Field;
import joeq.Class.jq_Method;
import joeq.Class.jq_Reference;
import joeq.Class.jq_Type;
import joeq.Class.jq_Reference.jq_NullType;
import joeq.Compiler.BytecodeAnalysis.BytecodeVisitor;
import joeq.Compiler.Quad.RegisterFactory.Register;
import joeq.Memory.Address;
import joeq.Memory.HeapAddress;
import joeq.Runtime.Reflection;
import jwutil.util.Assert;

/*
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: Operand.java,v 1.27 2004/09/22 22:17:27 joewhaley Exp $
 */
public interface Operand {

    Quad getQuad();
    void attachToQuad(Quad q);
    Operand copy();
    boolean isSimilar(Operand that);

    abstract class Util {
        public static boolean isNullConstant(Operand op) {
            return op instanceof AConstOperand && ((AConstOperand)op).getValue() == null;
        }
        public static boolean isConstant(Operand op) {
            return op instanceof AConstOperand ||
                   op instanceof PConstOperand ||
                   op instanceof IConstOperand ||
                   op instanceof FConstOperand ||
                   op instanceof LConstOperand ||
                   op instanceof DConstOperand;
        }
    }
    
    class RegisterOperand implements Operand {
        private Quad instruction;
        private Register register; private jq_Type type; private int flags;
        public static final int PRECISE_TYPE = 0x1;
        public Object scratchObject;
        public RegisterOperand(Register reg, jq_Type type) {
            this(reg, type, 0);
        }
        public RegisterOperand(Register reg, jq_Type type, int flags) {
            this.register = reg; this.type = type; this.flags = flags;
        }
        public Register getRegister() { return register; }
        public void setRegister(Register r) { this.register = r; }
        public jq_Type getType() { return type; }
        public void setType(jq_Type t) { this.type = t; }
        public int getFlags() { return flags; }
        public void setFlags(int f) { flags = f; }
        public void meetFlags(int f) { flags &= f; }
        public boolean isExactType() { return (flags & PRECISE_TYPE) != 0; }
        public void clearExactType() { flags &= ~PRECISE_TYPE; }
        public boolean hasMoreConservativeFlags(RegisterOperand that) { return that.getFlags() == (getFlags() | that.getFlags()); }
        public Operand copy() { return new RegisterOperand(this.register, this.type, this.flags); }
        public void attachToQuad(Quad q) { Assert._assert(instruction == null); instruction = q; }
        public Quad getQuad() { return instruction; }
        public boolean isSimilar(Operand that) { return that instanceof RegisterOperand && ((RegisterOperand)that).getRegister() == this.getRegister(); }
        public String toString() { return register+" "+((type==null)?"<g>":type.shortName()); }
    }
    
    interface ConstOperand extends Operand {
        Object getWrapped();
    }
    
    interface Const4Operand extends ConstOperand {
        int getBits();
    }
    
    interface Const8Operand extends ConstOperand {
        long getBits();
    }
    
    class AConstOperand implements Const4Operand {
        private Quad instruction;
        private Object value;
        public AConstOperand(Object v) { this.value = v; }
        //public int hashCode() { return System.identityHashCode(value); }
        //public boolean equals(Object that) { return equals((AConstOperand)that); }
        //public boolean equals(AConstOperand that) { return this.value == that.value; }
        public Object getValue() { return value; }
        public void setValue(Object o) { this.value = o; }
        public String toString() {
            if (value instanceof String) return "AConst: \""+value+"\"";
            return "AConst: "+value;
        }
        public jq_Reference getType() {
            if (value == null) return jq_NullType.NULL_TYPE;
            return Reflection.getTypeOf(this.value);
        }
        public void attachToQuad(Quad q) { Assert._assert(instruction == null); instruction = q; }
        public Quad getQuad() { return instruction; }
        public Operand copy() { return new AConstOperand(value); }
        public boolean isSimilar(Operand that) { return that instanceof AConstOperand && ((AConstOperand)that).getValue() == this.getValue(); }
        public int getBits() {
            return HeapAddress.addressOf(value).to32BitValue();
        }
        public Object getWrapped() {
            return value;
        }
    }
    
    class PConstOperand implements Const4Operand {
        private Quad instruction;
        private Address value;
        public PConstOperand(Address v) { this.value = v; }
        //public int hashCode() { return System.identityHashCode(value); }
        //public boolean equals(Object that) { return equals((AConstOperand)that); }
        //public boolean equals(AConstOperand that) { return this.value == that.value; }
        public Address getValue() { return value; }
        public void setValue(Address o) { this.value = o; }
        public String toString() {
            return "PConst: "+(value==null?"<null>":value.stringRep());
        }
        public jq_Reference getType() {
            return Address._class;
        }
        public void attachToQuad(Quad q) { Assert._assert(instruction == null); instruction = q; }
        public Quad getQuad() { return instruction; }
        public Operand copy() { return new PConstOperand(value); }
        public boolean isSimilar(Operand o) {
            if (!(o instanceof PConstOperand)) return false;
            PConstOperand that = (PConstOperand)o;
            return (this.getValue() == null && (that.getValue() == null || that.getValue().isNull())) ||
                   this.getValue().difference(that.getValue()) == 0;
        }
        public int getBits() { return value==null?0:value.to32BitValue(); }
        public Object getWrapped() {
            return new Integer(getBits());
        }
    }
    
    class IConstOperand implements Const4Operand {
        private Quad instruction;
        private int value;
        public IConstOperand(int v) { this.value = v; }
        //public int hashCode() { return value; }
        //public boolean equals(Object that) { return equals((IConstOperand)that); }
        //public boolean equals(IConstOperand that) { return this.value == that.value; }
        public int getValue() { return value; }
        public void setValue(int o) { this.value = o; }
        public String toString() { return "IConst: "+value; }
        public void attachToQuad(Quad q) { Assert._assert(instruction == null); instruction = q; }
        public Quad getQuad() { return instruction; }
        public Operand copy() { return new IConstOperand(value); }
        public boolean isSimilar(Operand that) { return that instanceof IConstOperand && ((IConstOperand)that).getValue() == this.getValue(); }
        public int getBits() { return value; }
        public Object getWrapped() {
            return new Integer(value);
        }
    }
    
    class FConstOperand implements Const4Operand {
        private Quad instruction;
        private float value;
        public FConstOperand(float v) { this.value = v; }
        //public int hashCode() { return Float.floatToRawIntBits(value); }
        //public boolean equals(Object that) { return equals((FConstOperand)that); }
        //public boolean equals(FConstOperand that) { return this.value == that.value; }
        public float getValue() { return value; }
        public void setValue(float o) { this.value = o; }
        public String toString() { return "FConst: "+value; }
        public void attachToQuad(Quad q) { Assert._assert(instruction == null); instruction = q; }
        public Quad getQuad() { return instruction; }
        public Operand copy() { return new FConstOperand(value); }
        public boolean isSimilar(Operand that) { return that instanceof FConstOperand && ((FConstOperand)that).getValue() == this.getValue(); }
        public int getBits() { return Float.floatToRawIntBits(value); }
        public Object getWrapped() {
            return new Float(value);
        }
    }

    class LConstOperand implements Const8Operand {
        private Quad instruction;
        private long value;
        public LConstOperand(long v) { this.value = v; }
        //public int hashCode() { return (int)(value>>32) ^ (int)value; }
        //public boolean equals(Object that) { return equals((LConstOperand)that); }
        //public boolean equals(DConstOperand that) { return this.value == that.value; }
        public long getValue() { return value; }
        public void setValue(long o) { this.value = o; }
        public String toString() { return "LConst: "+value; }
        public void attachToQuad(Quad q) { Assert._assert(instruction == null); instruction = q; }
        public Quad getQuad() { return instruction; }
        public Operand copy() { return new LConstOperand(value); }
        public boolean isSimilar(Operand that) { return that instanceof LConstOperand && ((LConstOperand)that).getValue() == this.getValue(); }
        public long getBits() { return value; }
        public Object getWrapped() {
            return new Long(value);
        }
    }

    class DConstOperand implements Const8Operand {
        private Quad instruction;
        private double value;
        public DConstOperand(double v) { this.value = v; }
        //public int hashCode() { long v = Double.doubleToRawLongBits(value); return (int)(v>>32) ^ (int)v; }
        //public boolean equals(Object that) { return equals((DConstOperand)that); }
        //public boolean equals(DConstOperand that) { return this.value == that.value; }
        public double getValue() { return value; }
        public void setValue(double o) { this.value = o; }
        public String toString() { return "DConst: "+value; }
        public void attachToQuad(Quad q) { Assert._assert(instruction == null); instruction = q; }
        public Quad getQuad() { return instruction; }
        public Operand copy() { return new DConstOperand(value); }
        public boolean isSimilar(Operand that) { return that instanceof DConstOperand && ((DConstOperand)that).getValue() == this.getValue(); }
        public long getBits() { return Double.doubleToRawLongBits(value); }
        public Object getWrapped() {
            return new Double(value);
        }
    }

    class UnnecessaryGuardOperand implements Operand {
        private Quad instruction;
        public UnnecessaryGuardOperand() {}
        //public int hashCode() { return 67; }
        //public boolean equals(Object that) { return that instanceof UnnecessaryGuardOperand; }
        public String toString() { return "<no guard>"; }
        public Operand copy() { return new UnnecessaryGuardOperand(); }
        public void attachToQuad(Quad q) { Assert._assert(instruction == null); instruction = q; }
        public Quad getQuad() { return instruction; }
        public boolean isSimilar(Operand that) { return that instanceof UnnecessaryGuardOperand; }
    }
    
    class ConditionOperand implements Operand {
        private Quad instruction; byte condition;
        public ConditionOperand(byte c) { condition = c; }
        //public int hashCode() { return condition; }
        //public boolean equals(Object that) { return this.equals((ConditionOperand)that); }
        //public boolean equals(ConditionOperand that) { return this.condition == that.condition; }
        public byte getCondition() { return condition; }
        public void setCondition(byte o) { this.condition = o; }
        public String toString() { return BytecodeVisitor.cmpopnames[condition]; }
        public Operand copy() { return new ConditionOperand(condition); }
        public void attachToQuad(Quad q) { Assert._assert(instruction == null); instruction = q; }
        public Quad getQuad() { return instruction; }
        public boolean isSimilar(Operand that) { return that instanceof ConditionOperand && ((ConditionOperand)that).getCondition() == this.getCondition(); }
    }

    class FieldOperand implements Operand {
        private Quad instruction; jq_Field field;
        public FieldOperand(jq_Field f) { field = f; }
        public jq_Field getField() { return field; }
        public void setField(jq_Field f) { this.field = f; }
        public void resolve() { this.field = (jq_Field) this.field.resolve(); }
        public String toString() { return "."+field.getName(); }
        public Operand copy() { return new FieldOperand(field); }
        public void attachToQuad(Quad q) { Assert._assert(instruction == null); instruction = q; }
        public Quad getQuad() { return instruction; }
        public boolean isSimilar(Operand that) { return that instanceof FieldOperand && ((FieldOperand)that).getField() == this.getField(); }
    }
    
    class TypeOperand implements Operand {
        private Quad instruction; jq_Type type;
        TypeOperand(jq_Type f) { type = f; }
        public jq_Type getType() { return type; }
        public void setType(jq_Type f) { this.type = f; }
        public String toString() { return type.toString(); }
        public Operand copy() { return new TypeOperand(type); }
        public void attachToQuad(Quad q) { Assert._assert(instruction == null); instruction = q; }
        public Quad getQuad() { return instruction; }
        public boolean isSimilar(Operand that) { return that instanceof TypeOperand && ((TypeOperand)that).getType() == this.getType(); }
    }
    
    class TargetOperand implements Operand {
        private Quad instruction; BasicBlock target;
        public TargetOperand(BasicBlock t) { target = t; }
        public BasicBlock getTarget() { return target; }
        public void setTarget(BasicBlock f) { this.target = f; }
        public String toString() { return target.toString(); }
        public Operand copy() { return new TargetOperand(target); }
        public void attachToQuad(Quad q) { Assert._assert(instruction == null); instruction = q; }
        public Quad getQuad() { return instruction; }
        public boolean isSimilar(Operand that) { return that instanceof TargetOperand && ((TargetOperand)that).getTarget() == this.getTarget(); }
    }
    
    class MethodOperand implements Operand {
        private Quad instruction; jq_Method target;
        public MethodOperand(jq_Method t) { target = t; }
        public jq_Method getMethod() { return target; }
        public void setMethod(jq_Method f) { this.target = f; }
        public void resolve() { this.target = (jq_Method) this.target.resolve(); }
        public String toString() { return target.toString(); }
        public Operand copy() { return new MethodOperand(target); }
        public void attachToQuad(Quad q) { Assert._assert(instruction == null); instruction = q; }
        public Quad getQuad() { return instruction; }
        public boolean isSimilar(Operand that) { return that instanceof MethodOperand && ((MethodOperand)that).getMethod() == this.getMethod(); }
    }
    
    class IntValueTableOperand implements Operand {
        private Quad instruction; int[] table;
        public IntValueTableOperand(int[] t) { table = t; }
        public void set(int i, int b) { table[i] = b; }
        public int get(int i) { return table[i]; }
        public int size() { return table.length; }
        public String toString() {
            StringBuffer sb = new StringBuffer("{ ");
            if (table.length > 0) {
                sb.append(table[0]);
                for (int i=1; i<table.length; ++i) {
                    sb.append(", ");
                    sb.append(table[i]);
                }
            }
            sb.append(" }");
            return sb.toString();
        }
        public Operand copy() {
            int[] t2 = new int[this.table.length];
            System.arraycopy(this.table, 0, t2, 0, t2.length);
            return new IntValueTableOperand(t2);
        }
        public void attachToQuad(Quad q) { Assert._assert(instruction == null); instruction = q; }
        public Quad getQuad() { return instruction; }
        public boolean isSimilar(Operand that) { return false; }
    }
    
    class BasicBlockTableOperand implements Operand {
        private Quad instruction; BasicBlock[] table;
        public BasicBlockTableOperand(BasicBlock[] t) { table = t; }
        public void set(int i, BasicBlock b) { table[i] = b; }
        public BasicBlock get(int i) { return table[i]; }
        public int size() { return table.length; }
        public String toString() {
            StringBuffer sb = new StringBuffer("{ ");
            if (table.length > 0) {
                sb.append(table[0]);
                for (int i=1; i<table.length; ++i) {
                    sb.append(", ");
                    sb.append(table[i]);
                }
            }
            sb.append(" }");
            return sb.toString();
        }
        public Operand copy() {
            BasicBlock[] t2 = new BasicBlock[this.table.length];
            System.arraycopy(this.table, 0, t2, 0, t2.length);
            return new BasicBlockTableOperand(t2);
        }
        public void attachToQuad(Quad q) { Assert._assert(instruction == null); instruction = q; }
        public Quad getQuad() { return instruction; }
        public boolean isSimilar(Operand that) { return false; }
    }
    
    class ParamListOperand implements Operand {
        private Quad instruction; RegisterOperand[] params;
        public ParamListOperand(RegisterOperand[] t) { params = t; }
        public void set(int i, RegisterOperand b) {
            params[i] = b;
            if (b != null) b.attachToQuad(instruction);
        }
        public RegisterOperand get(int i) { return params[i]; }
        public int length() { return params.length; }
        public int words() {
            int total = 0;
            for (int i=0; i<params.length; ++i) {
                ++total;
                if (params[i].getType().getReferenceSize() == 8)
                    ++total;
            }
            return total;
        }
        public String toString() {
            StringBuffer sb = new StringBuffer("(");
            if (params.length > 0) {
                sb.append(params[0]);
                for (int i=1; i<params.length; ++i) {
                    sb.append(", ");
                    sb.append(params[i]);
                }
            }
            sb.append(")");
            return sb.toString();
        }
        public Operand copy() {
            RegisterOperand[] t2 = new RegisterOperand[this.params.length];
            for (int i=0; i<t2.length; ++i) {
                t2[i] = (RegisterOperand)this.params[i].copy();
            }
            return new ParamListOperand(t2);
        }
        public void attachToQuad(Quad q) { Assert._assert(instruction == null); instruction = q; }
        public Quad getQuad() { return instruction; }
        public boolean isSimilar(Operand that) { return false; }
    }
    
}
