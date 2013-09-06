// jq_Initializer.java, created Mon Feb  5 23:23:20 2001 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Class;

//friend jq_ClassLoader;

import joeq.UTF.Utf8;
import jwutil.util.Assert;

/*
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: jq_Initializer.java,v 1.17 2004/09/22 22:17:28 joewhaley Exp $
 */
public final class jq_Initializer extends jq_InstanceMethod {

    // clazz, name, desc are inherited
    
    private jq_Initializer(jq_Class clazz, jq_NameAndDesc nd) {
        super(clazz, nd);
    }
    // ONLY TO BE CALLED BY jq_ClassLoader!!!
    static jq_Initializer newInitializer(jq_Class clazz, jq_NameAndDesc nd) {
        Assert._assert(nd.getName() == Utf8.get("<init>"));
        return new jq_Initializer(clazz, nd);
    }
    protected final void parseMethodSignature() {
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
        return_type = jq_Primitive.VOID;
    }

    public final jq_InstanceMethod resolve1() {
        this.clazz.load();
        if (this.state >= STATE_LOADED) return this;
        throw new NoSuchMethodError(this.toString());
    }
    
    public final boolean isInitializer() { return true; }

    public final void accept(jq_MethodVisitor mv) {
        mv.visitInitializer(this);
        super.accept(mv);
    }
    
    public static final jq_Class _class;
    static {
        _class = (jq_Class)PrimordialClassLoader.loader.getOrCreateBSType("Ljoeq/Class/jq_Initializer;");
    }
}
