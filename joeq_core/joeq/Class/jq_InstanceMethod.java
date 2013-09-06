// jq_InstanceMethod.java, created Mon Feb  5 23:23:20 2001 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Class;

//friend jq_ClassLoader;

import joeq.Main.jq;
import joeq.UTF.Utf8;
import jwutil.util.Assert;

/*
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: jq_InstanceMethod.java,v 1.25 2004/09/22 22:17:29 joewhaley Exp $
 */
public class jq_InstanceMethod extends jq_Method {

    // available after preparation
    public static final int INVALID_OFFSET = 0x80000000;
    private int offset;
    private boolean isOverriding, isOverridden;
    
    // inherited: clazz, name, desc, access_flags, attributes
    //            max_stack, max_locals, bytecode, exception_table, codeattribMap,
    //            param_types, return_type
    protected jq_InstanceMethod(jq_Class clazz, jq_NameAndDesc nd) {
        super(clazz, nd);
        offset = INVALID_OFFSET;
    }
    // ONLY TO BE CALLED BY jq_ClassLoader!!!
    static jq_InstanceMethod newInstanceMethod(jq_Class clazz, jq_NameAndDesc nd) {
        return new jq_InstanceMethod(clazz, nd);
    }
    protected void parseMethodSignature() {
        Utf8.MethodDescriptorIterator i = nd.getDesc().getParamDescriptors();
        // count them up
        int num = 1, words = 1;
        while (i.hasNext()) { i.nextUtf8(); ++num; }
        // get them for real
        param_types = new jq_Type[num];
        param_types[0] = clazz;
        i = nd.getDesc().getParamDescriptors();
        for (int j=1; j<num; ++j) {
            Utf8 pd = i.nextUtf8();
            param_types[j] = PrimordialClassLoader.getOrCreateType(clazz.getClassLoader(), pd);
            ++words;
            if ((param_types[j] == jq_Primitive.LONG) ||
                (param_types[j] == jq_Primitive.DOUBLE)) ++words;
        }
        param_words = words;
        Utf8 rd = i.getReturnDescriptor();
        return_type = PrimordialClassLoader.getOrCreateType(clazz.getClassLoader(), rd);
    }
    public final void clearOverrideFlags() { this.isOverridden = false; this.isOverriding = false; }
    public final void overriddenBy(jq_InstanceMethod that) {
        this.isOverridden = true; that.isOverriding = true;
    }
    public final boolean isOverriding() { return isOverriding; }
    public final boolean isOverridden() { return isOverridden; }
    
    public final jq_Member resolve() { return resolve1(); }
    public jq_InstanceMethod resolve1() {
        this.clazz.load();
        if (this.state >= STATE_LOADED) return this;
        // this reference may be to a superclass or superinterface.
        jq_InstanceMethod m = this.clazz.getInstanceMethod(nd);
        if (m != null) return m;
        throw new NoSuchMethodError(this.toString());
    }
    
    public final void prepare() { prepare(INVALID_OFFSET); }
    public final void prepare(int offset) {
        Assert._assert(state == STATE_LOADED); state = STATE_PREPARED; this.offset = offset;
    }
    public final int getOffset() { chkState(STATE_PREPARED); Assert._assert(offset != INVALID_OFFSET); return offset; }
    public final boolean isVirtual() { chkState(STATE_PREPARED); return offset != INVALID_OFFSET; }
    public final boolean needsDynamicLink(jq_Method method) {
        if (!jq.RunningNative) return (state < STATE_PREPARED) || getDeclaringClass().needsDynamicLink(method);
        /*
        if (method.getDeclaringClass() == this.getDeclaringClass())
            return false;
            */
        return state < STATE_SFINITIALIZED;
    }
    public final boolean isStatic() { return false; }
    public final void unprepare() { chkState(STATE_PREPARED); offset = INVALID_OFFSET; state = STATE_LOADED; }
    
    public boolean isInitializer() { return false; }

    public void accept(jq_MethodVisitor mv) {
        mv.visitInstanceMethod(this);
        super.accept(mv);
    }
    
    public static final jq_Class _class = (jq_Class)PrimordialClassLoader.loader.getOrCreateBSType("Ljoeq/Class/jq_InstanceMethod;");
}
