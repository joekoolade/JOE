// jq_StaticMethod.java, created Mon Feb  5 23:23:20 2001 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Class;

//friend jq_ClassLoader;

import joeq.Main.jq;
import joeq.UTF.Utf8;
import jwutil.util.Assert;

/*
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: jq_StaticMethod.java,v 1.23 2004/09/22 22:17:28 joewhaley Exp $
 */
public class jq_StaticMethod extends jq_Method {

    // clazz, name, desc, access_flags are inherited
    protected jq_StaticMethod(jq_Class clazz, jq_NameAndDesc nd) {
        super(clazz, nd);
    }
    // ONLY TO BE CALLED BY jq_ClassLoader!!!
    static jq_StaticMethod newStaticMethod(jq_Class clazz, jq_NameAndDesc nd) {
        return new jq_StaticMethod(clazz, nd);
    }
    protected void parseMethodSignature() {
        Utf8.MethodDescriptorIterator i = nd.getDesc().getParamDescriptors();
        // count them up
        int num = 0, words = 0;
        while (i.hasNext()) { i.nextUtf8(); ++num; }
        // get them for real
        param_types = new jq_Type[num];
        i = nd.getDesc().getParamDescriptors();
        for (int j=0; j<num; ++j) {
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
    
    public final boolean needsDynamicLink(jq_Method method) {
        if (getDeclaringClass().needsDynamicLink(method)) return true;
        if (!jq.RunningNative) return false;
        return (state < STATE_SFINITIALIZED);
    }

    public final boolean isStatic() { return true; }
    public boolean isClassInitializer() { return false; }

    public final jq_Member resolve() { return resolve1(); }
    public jq_StaticMethod resolve1() {
        this.clazz.load();
        if (this.state >= STATE_LOADED) return this;
        // this reference may be to a superclass or superinterface.
        jq_StaticMethod m = this.clazz.getStaticMethod(nd);
        if (m != null) return m;
        throw new NoSuchMethodError(this.toString());
    }
    
    public final void prepare() { Assert._assert(state == STATE_LOADED); state = STATE_PREPARED; }

    public final void unprepare() { Assert._assert(state == STATE_PREPARED); state = STATE_LOADED; }
    
    public void accept(jq_MethodVisitor mv) {
        mv.visitStaticMethod(this);
        super.accept(mv);
    }
    
    public static final jq_Class _class;
    static {
        _class = (jq_Class)PrimordialClassLoader.loader.getOrCreateBSType("Ljoeq/Class/jq_StaticMethod;");
    }
}
