// jq_FakeInstanceMethod.java, created Tue Dec  9 23:43:51 PST 2003
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Class;

import java.util.HashMap;
import java.util.StringTokenizer;
import joeq.UTF.Utf8;

/*
 * @author  Godmar Back <gback@stanford.edu>
 * @version $Id: jq_FakeStaticMethod.java,v 1.2 2006/02/25 08:05:21 livshits Exp $
 */
public class jq_FakeStaticMethod extends jq_StaticMethod {
    private static final String FAKE_POSTFIX = "_fake";
    private static HashMap cache = new HashMap();

    public static jq_Member read(StringTokenizer st) {
        jq_Class c = (jq_Class) jq_Type.read(st);
        if (c == null) return null;
        c.load();
        String name = st.nextToken();
        String desc = st.nextToken();
        return fakeMethod(c, name, desc);
    }

    protected jq_FakeStaticMethod(jq_Class clazz, jq_NameAndDesc nd) {
        super(clazz, 
            new jq_NameAndDesc(
                Utf8.get((nd.getName() + FAKE_POSTFIX)), nd.getDesc()));
        parseMethodSignature();
        state = STATE_PREPARED;
    }

    public static jq_StaticMethod fakeMethod(jq_Class clazz, jq_NameAndDesc nd) {
        return fakeMethod(clazz, nd, true);
    }

    public static jq_StaticMethod fakeMethod(jq_Class clazz, jq_NameAndDesc nd, boolean create) {
        jq_MemberReference mr = new jq_MemberReference(clazz, nd);
        jq_FakeStaticMethod m = (jq_FakeStaticMethod)cache.get(mr);
        if (m == null && create) {
            cache.put(mr, m = new jq_FakeStaticMethod(clazz, nd));
        }
        return m;
    }

    public static jq_StaticMethod fakeMethod(jq_Class clazz, String name, String desc) {
        return fakeMethod(clazz, new jq_NameAndDesc(name, desc));
    }

    public static final jq_Class _class = (jq_Class)PrimordialClassLoader.loader.getOrCreateBSType("Ljoeq/Class/jq_FakeInstanceMethod;");
}
