// jq_InstanceField.java, created Mon Feb  5 23:23:20 2001 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Class;

import java.util.HashMap;
import java.util.Map;
import java.io.DataInput;
import java.io.IOException;
import joeq.Main.jq;
import jwutil.util.Assert;

//friend jq_ClassLoader;

/*
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: jq_InstanceField.java,v 1.22 2004/09/22 22:17:28 joewhaley Exp $
 */
public final class jq_InstanceField extends jq_Field {

    public static final int INVALID_OFFSET = 0x80000000;
    private int offset;
    
    // clazz, name, desc, access_flags are inherited
    private jq_InstanceField(jq_Class clazz, jq_NameAndDesc nd) {
        super(clazz, nd);
        offset = INVALID_OFFSET;
    }
    // ONLY TO BE CALLED BY jq_ClassLoader!!!
    static jq_InstanceField newInstanceField(jq_Class clazz, jq_NameAndDesc nd) {
        return new jq_InstanceField(clazz, nd);
    }
    
    public final int getWidth() {
        return type.getReferenceSize();
    }
    
    public final void load(jq_InstanceField that) {
        this.access_flags = that.access_flags;
        this.attributes = (Map)((HashMap)that.attributes).clone();
        state = STATE_LOADED;
    }
    
    public final void load(char access_flags, DataInput in) 
    throws IOException, ClassFormatError {
        super.load(access_flags, in);
        state = STATE_LOADED;
    }
    
    public final void load(char access_flags, Map attributes) {
        super.load(access_flags, attributes);
        state = STATE_LOADED;
    }
    
    public final jq_Member resolve() { return resolve1(); }
    public final jq_InstanceField resolve1() {
        this.clazz.load();
        if (this.state >= STATE_LOADED) return this;
        // this reference may be to a superclass.
        jq_InstanceField m = this.clazz.getInstanceField(nd);
        if (m != null) return m;
        throw new NoSuchFieldError(this.toString());
    }
    
    public final boolean isUnsignedType() { return type == jq_Primitive.CHAR; }
    public final int getSize() { return type.getReferenceSize(); }
    public final void prepare(int offset) { Assert._assert(state == STATE_LOADED); state = STATE_PREPARED; this.offset = offset; }
    public final int getOffset() { chkState(STATE_PREPARED); Assert._assert(offset != INVALID_OFFSET); return offset; }
    public final boolean needsDynamicLink(jq_Method method) {
        if (!jq.RunningNative) return (state < STATE_PREPARED) || getDeclaringClass().needsDynamicLink(method);
        return state < STATE_PREPARED;
    }
    public final boolean isStatic() { return false; }
    public final void unprepare() { chkState(STATE_PREPARED); offset = INVALID_OFFSET; state = STATE_LOADED; }

    public void accept(jq_FieldVisitor mv) {
        mv.visitInstanceField(this);
        super.accept(mv);
    }
    
    public static final jq_Class _class;
    static {
        _class = (jq_Class)PrimordialClassLoader.loader.getOrCreateBSType("Ljoeq/Class/jq_InstanceField;");
    }
}
