// jq_ClassInitializer.java, created Mon Feb  5 23:23:20 2001 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Class;

//friend jq_ClassLoader;

import joeq.UTF.Utf8;
import jwutil.util.Assert;

/**
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: jq_ClassInitializer.java,v 1.16 2004/09/22 22:17:29 joewhaley Exp $
 */
public final class jq_ClassInitializer extends jq_StaticMethod {

    // clazz, nd are inherited
    
    private jq_ClassInitializer(jq_Class clazz, jq_NameAndDesc nd) {
        super(clazz, nd);
    }
    // ONLY TO BE CALLED BY jq_ClassLoader!!!
    static jq_ClassInitializer newClassInitializer(jq_Class clazz, jq_NameAndDesc nd) {
        Assert._assert(nd.getName() == Utf8.get("<clinit>"));
        Assert._assert(nd.getDesc() == Utf8.get("()V"));
        return new jq_ClassInitializer(clazz, nd);
    }

    protected final void parseMethodSignature() {
        // no need to parse anything
        param_types = new jq_Type[0];
        return_type = jq_Primitive.VOID;
    }
    
    public final jq_StaticMethod resolve1() {
        this.clazz.load();
        if (this.state >= STATE_LOADED) return this;
        throw new NoSuchMethodError(this.toString());
    }
    
    public final boolean isClassInitializer() { return true; }

    public final void accept(jq_MethodVisitor mv) {
        mv.visitClassInitializer(this);
        super.accept(mv);
    }
    
    public static final jq_Class _class;
    static {
        _class = (jq_Class)PrimordialClassLoader.loader.getOrCreateBSType("Ljoeq/Class/jq_ClassInitializer;");
    }
}
