// jq_StaticField.java, created Mon Feb  5 23:23:20 2001 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Class;

//friend jq_ClassLoader;

import java.util.HashMap;
import java.util.Map;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import joeq.Memory.Address;
import joeq.Memory.HeapAddress;
import joeq.UTF.Utf8;
import jwutil.util.Assert;
import jwutil.util.Convert;

/*
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: jq_StaticField.java,v 1.30 2004/09/22 22:17:29 joewhaley Exp $
 */
public final class jq_StaticField extends jq_Field {

    // null if not a constant.
    private Object constantValue;
    
    private HeapAddress address;
    
    // clazz, name, desc, access_flags are inherited
    private jq_StaticField(jq_Class clazz, jq_NameAndDesc nd) {
        super(clazz, nd);
    }
    // ONLY TO BE CALLED BY jq_ClassLoader!!!
    static jq_StaticField newStaticField(jq_Class clazz, jq_NameAndDesc nd) {
        return new jq_StaticField(clazz, nd);
    }
    
    public final int getWidth() {
        if (type == jq_Primitive.LONG || type == jq_Primitive.DOUBLE)
            return 8;
        else
            return 4;
    }
    
    public final void load(jq_StaticField that) {
        this.access_flags = that.access_flags;
        this.attributes = (Map)((HashMap)that.attributes).clone();
        this.constantValue = that.constantValue;
        state = STATE_LOADED;
    }
    
    public final void load(char access_flags, Map attributes) {
        super.load(access_flags, attributes);
        parseAttributes();
    }
    
    public final void load(char access_flags, DataInput in) 
    throws IOException, ClassFormatError {
        super.load(access_flags, in);
        parseAttributes();
    }
    
    private final void parseAttributes() throws ClassFormatError {
        byte[] a = getAttribute("ConstantValue");
        if (a != null) {
            if (a.length != 2) throw new ClassFormatError();
            char cpidx = Convert.twoBytesToChar(a, 0);
            jq_Class clazz = getDeclaringClass();
            switch (clazz.getCPtag(cpidx)) {
                case CONSTANT_Long:
                    if (type != jq_Primitive.LONG)
                        throw new ClassFormatError();
                    constantValue = clazz.getCPasLong(cpidx);
                    break;
                case CONSTANT_Float:
                    if (type != jq_Primitive.FLOAT)
                        throw new ClassFormatError();
                    constantValue = clazz.getCPasFloat(cpidx);
                    break;
                case CONSTANT_Double:
                    if (type != jq_Primitive.DOUBLE)
                        throw new ClassFormatError();
                    constantValue = clazz.getCPasDouble(cpidx);
                    break;
                case CONSTANT_String:
                    if (type != PrimordialClassLoader.getJavaLangString())
                        throw new ClassFormatError();
                    constantValue = clazz.getCPasString(cpidx);
                    break;
                case CONSTANT_Integer:
                    if (!type.isPrimitiveType() ||
                        type == jq_Primitive.LONG ||
                        type == jq_Primitive.FLOAT ||
                        type == jq_Primitive.DOUBLE)
                        throw new ClassFormatError();
                    constantValue = clazz.getCPasInt(cpidx);
                    break;
                default:
                    throw new ClassFormatError("Unknown tag "+clazz.getCPtag(cpidx)+" at cp index "+(int)cpidx);
            }
        }
        state = STATE_LOADED;
    }
    
    public final jq_Member resolve() { return resolve1(); }
    public final jq_StaticField resolve1() {
        this.clazz.load();
        if (this.state >= STATE_LOADED) return this;
        // this reference may be to a superclass or superinterface.
        jq_StaticField m = this.clazz.getStaticField(nd);
        if (m != null) return m;
        throw new NoSuchFieldError(this.toString());
    }
    
    public void dumpAttributes(DataOutput out, jq_ConstantPool.ConstantPoolRebuilder cpr) throws IOException {
        if (constantValue != null) {
            byte[] b = new byte[2]; Convert.charToTwoBytes(cpr.get(constantValue), b, 0);
            attributes.put(Utf8.get("ConstantValue"), b);
        }
        super.dumpAttributes(out, cpr);
    }

    public final void sf_initialize(int[] static_data, int offset) {
        Assert._assert(state == STATE_PREPARED);
        state = STATE_SFINITIALIZED;
        HeapAddress a = HeapAddress.addressOf(static_data);
        if (a != null)
            this.address = (HeapAddress) a.offset(offset);
    }
    public final HeapAddress getAddress() {
        chkState(STATE_SFINITIALIZED);
        return address;
    }
    public final void setValue(int v) {
        getDeclaringClass().setStaticData(this, v);
    }
    public final void setValue(float v) {
        getDeclaringClass().setStaticData(this, v);
    }
    public final void setValue(long v) {
        getDeclaringClass().setStaticData(this, v);
    }
    public final void setValue(double v) {
        getDeclaringClass().setStaticData(this, v);
    }
    public final void setValue(Object v) {
        getDeclaringClass().setStaticData(this, v);
    }
    public final void setValue(Address v) {
        getDeclaringClass().setStaticData(this, v);
    }
    
    public final boolean needsDynamicLink(jq_Method method) {
        return getDeclaringClass().needsDynamicLink(method);
    }
    public final boolean isConstant() {
        chkState(STATE_LOADED);
        return constantValue != null;
    }
    public final Object getConstantValue() {
        return constantValue;
    }
    public final boolean isStatic() {
        return true;
    }

    public final void prepare() {
        Assert._assert(state == STATE_LOADED);
        state = STATE_PREPARED;
    }
    public final void unprepare() {
        Assert._assert(state == STATE_PREPARED);
        state = STATE_LOADED;
    }
    
    public void accept(jq_FieldVisitor mv) {
        mv.visitStaticField(this);
        super.accept(mv);
    }
    
    public static final jq_Class _class;
    static {
        _class = (jq_Class)PrimordialClassLoader.loader.getOrCreateBSType("Ljoeq/Class/jq_StaticField;");
    }
}
