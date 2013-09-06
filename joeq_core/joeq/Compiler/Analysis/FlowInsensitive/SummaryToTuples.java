// SummaryToTuples.java, created Jan 23, 2005 4:54:46 PM by joewhaley
// Copyright (C) 2005 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.Analysis.FlowInsensitive;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.math.BigInteger;
import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Class;
import joeq.Class.jq_ClassInitializer;
import joeq.Class.jq_FakeInstanceMethod;
import joeq.Class.jq_Field;
import joeq.Class.jq_Member;
import joeq.Class.jq_Method;
import joeq.Class.jq_NameAndDesc;
import joeq.Class.jq_Reference;
import joeq.Class.jq_Type;
import joeq.Class.jq_Reference.jq_NullType;
import joeq.Compiler.Analysis.FlowInsensitive.MethodSummary.CheckCastNode;
import joeq.Compiler.Analysis.FlowInsensitive.MethodSummary.ConcreteObjectNode;
import joeq.Compiler.Analysis.FlowInsensitive.MethodSummary.ConcreteTypeNode;
import joeq.Compiler.Analysis.FlowInsensitive.MethodSummary.GlobalNode;
import joeq.Compiler.Analysis.FlowInsensitive.MethodSummary.Node;
import joeq.Compiler.Analysis.FlowInsensitive.MethodSummary.UnknownTypeNode;
import joeq.Compiler.Analysis.IPA.ProgramLocation;
import joeq.Compiler.Quad.CodeCache;
import joeq.Compiler.Quad.LoadedCallGraph;
import joeq.Main.HostedVM;
import jwutil.collections.IndexMap;
import jwutil.collections.Pair;
import jwutil.collections.Tuples;
import jwutil.collections.TuplesArray;
import jwutil.util.Assert;

/**
 * SummaryToTuples
 * 

Definition of domains:

V: Variables.
   Corresponds to variable nodes in the program.  There are variable nodes
   for each formal parameter, result of a object creation (new/newarray/etc.),
   result of a getfield/arrayload, return value at a call site, and thrown
   exception at a call site.  There is also a special type of 'global'
   variable node that is used when loading or storing to static fields.
   Primitive type variables are skipped.

H: Heap.
   Corresponds to a location on the heap.  There is a heap node for each
   object creation site.

T: Type.
   Corresponds to a Java type (class or interface).  There is one for every
   class or interface used in the program.

F: Field.
   Corresponds to a field.  There is one for each dereferenced field in the
   program.  There is also a special 'array' field for array accesses.

I: Invocation.
   Corresponds to a method invocation.  There is one for each method
   invocation in the program.  Note that object creation is not a method
   invocation, but object initialization (a call to the "<init>" method)
   is an invocation.

Z: Integer.
   Corresponds to an integer number.  Used for numbering parameters.

N: Method name.
   Corresponds to a method name and descriptor used for virtual method
   dispatch.  There is one for every method name and descriptor used in
   a virtual method call.  There is also a special 'null' method name that
   is used for non-virtual calls.

M: Methods.
   Corresponds to a method in the program.  This differs from the N domain
   in that this contains actual methods, whereas N simply contains names
   that are used for virtual method lookup, and can therefore contain
   abstract methods.
   
VC: Variable context.
    Path number used for context sensitivity.

Files dumped by SummaryToTuples:

fielddomains.pa :
    contains the domain definitions, sizes, and map file names.

m_formal.tuples : (M,Z,V)
    Contains formal method parameters.
    A tuple (m,z,v) means that parameter #z of method m is represented by
    variable v.  In static methods, parameter #0 is the special 'global'
    variable.  For instance methods, parameter #0 is the 'this' pointer for
    the method.  Primitive type parameters do not have variable nodes and
    therefore are not in m_formal.

m_global.tuples : (M,V)
    Contains the 'global variable node' for each method.
    Each method has a 'global variable node' that it uses when accessing
    static fields.  A tuple (m,v) means that method m has global variable
    node v.

m_vP.tuples : (M,V,H)
    Contains object creation sites within the method.
    A tuple (m,v,h) means that method m contains an object creation site
    that makes variable v point to new heap location h.

m_A.tuples : (M,V,V)
    Contains assignments within a method.
    A tuple (m,v1,v2) means that method m contains an assignment "v1=v2;".
    Note that because assignments are factored away, the only remaining
    assignments will be caused by casts.  This is because variables can
    only have one type, so we need to use different variables to record
    the different types.

m_L.tuples : (M,V,F,V)
    Contains load instructions within a method.
    A tuple (m,v1,f,v2) means that method m contains a load instruction
    "v2=v1.f;", or if f is the special 'array' field, then m contains an
    instruction of the form "v2=v1[_];".  Loads from static fields
    are specified with v1 equal to the 'global variable node' for the method.
    Loads from fields with primitive types are skipped.

m_S.tuples : (M,V,F,V)
    Contains store instructions within a method.
    A tuple (m,v1,f,v2) means that method m contains a store instruction
    "v1.f=v2;", or if f is the special 'array' field, then m contains an
    instruction of the form "v1[_]=v2;".  Stores into static fields
    are specified with v1 equal to the 'global variable node' for the method.
    Stores to fields with primitive types are skipped.

m_calls.tuples : (M,I,N)
    Contains the method invocations within a method.
    A tuple (m,i,n) means that a method m contains an invocation site i
    on method name n.  If the invocation is a non-virtual invocation or can
    be completely statically resolved, the method name is a special null
    method name, otherwise the method name is the virtual method to do the
    lookup upon.  Non-virtual calls will also appear in "m_sc" with their
    real targets.

m_actual.tuples : (M,I,Z,V)
    Contains the parameters for method invocations.
    A tuple (m,i,z,v) means that a method m contains an invocation site i
    with variable v passed in as parameter #z.  Static calls have the
    special 'global' variable as their 0th parameter; instance calls have
    the base object as their 0th parameter.  Primitive-typed parameters
    are skipped.

m_Iret.tuples : (M,I,V)
    Contains the return values for method invocations.
    A tuple (m,i,v) means that a method m contains an invocation site i
    with return value v.  Invocations that do not use the return value or
    that have a primitive return value are skipped.
    
m_Ithr.tuples : (M,I,V)
    Contains the thrown exceptions for method invocations.
    A tuple (m,i,v) means that a method m contains an invocation site i
    with thrown exception v.
    
m_sync.tuples : (M,V)
    Contains the variables that are synchronized in a method.
    A tuple (m,v) means that method m synchronizes on variable v.

m_vars.tuples : (M,V)
    Contains the set of all variables in a method.
    A tuple (m,v) means that method m contains variable v.
    
m_ret.tuples : (M,V)
    Contains the set of variables that are returned from a method.
    A tuple (m,v) means that method m returns variable v.
    
m_thr.tuples : (M,V)
    Contains the set of variables that are thrown from a method.
    A tuple (m,v) means that method m throws variable v.
    
m_sc.tuples : (M,I,M)
    Contains the statically-bound calls in a method.
    A tuple (m1,i,m2) means that m1 contains an invocation site i that
    is statically bound to call m2.

vT.tuples : (V,T)
    Contains the declared type (class or interface) of each variable.
    
hT.tuples : (H,T)
    Contains the type (class) of each object creation site.
    
aT.tuples : (T,T)
    Contains which types are assignable from one to another.
    A tuple (t1,t2) means that you can assign an object of type t2 to
    a variable of type t1 (i.e., t1 is a supertype of t2).

cha.tuples : (T,N,M)
    Contains the virtual method dispatch information.
    A tuple (t,n,m) means that doing a virtual method call with name n
    on an object of type t leads to target method m.

methods.tuples : (T,M)
    Contains the methods that each class defines.
    A tuple (t,m) means that class t defines method m.

fields.tuples : (T,F)
    Contains the fields that each class defines.
    A tuple (t,f) means that class t defines field f.
    
clinit.tuples : (T,M)
    Contains the class initializers.
    A tuple (t,m) means that class t has class initializer method m.
    
m_access.tuples : (M,Z)
    Contains the access modifiers for each of the methods.
    0 = public, 1 = private, 2 = protected, 3 = package-protected

f_access.tuples : (F,Z)
    Contains the access modifiers for each of the fields.
    0 = public, 1 = private, 2 = protected, 3 = package-protected

 * 
 * @author jwhaley
 * @version $Id: SummaryToTuples.java,v 1.4 2005/07/15 00:45:14 joewhaley Exp $
 */
public class SummaryToTuples {
    
    public static boolean TRACE = !System.getProperty("pa.trace", "no").equals("no");
    public static PrintStream out = System.out;
    
    boolean SKIP_NULL = !System.getProperty("pa.skipnull", "yes").equals("no");
    boolean SKIP_EXCEPTIONS = !System.getProperty("pa.skipexceptions", "yes").equals("no");
    boolean ADD_SUPERTYPES = !System.getProperty("pa.addsupertypes", "yes").equals("no");
    
    IndexMap/*<Node*/ Vmap;
    IndexMap/*<ProgramLocation>*/ Imap;
    IndexMap/*<Node>*/ Hmap;
    IndexMap/*<jq_Field>*/ Fmap;
    IndexMap/*<jq_Reference>*/ Tmap;
    IndexMap/*<jq_Method>*/ Nmap;
    IndexMap/*<jq_Method>*/ Mmap;
    
    jq_Class object_class = PrimordialClassLoader.getJavaLangObject();
    jq_Method javaLangObject_clone;
    {
        object_class.prepare();
        javaLangObject_clone = object_class.getDeclaredInstanceMethod(new jq_NameAndDesc("clone", "()Ljava/lang/Object;"));
    }
    jq_Class cloneable_class = (jq_Class) PrimordialClassLoader.loader.getOrCreateBSType("Ljava/lang/Cloneable;");
    jq_Class throwable_class = (jq_Class) PrimordialClassLoader.getJavaLangThrowable();
    jq_Method javaLangObject_fakeclone = jq_FakeInstanceMethod.fakeMethod(object_class, 
                                                MethodSummary.fakeCloneName, "()Ljava/lang/Object;");
    
    Tuples.S2 vT;  // var type (VxT)
    Tuples.S2 hT;  // heap type (HxT)
    Tuples.S2 aT;  // assignable type (TxT)
    Tuples.S3 cha; // vtable (TxNxM)
    Tuples.S2 methods;  // declared methods (TxM)
    Tuples.S2 fields;   // declared fields (TxF)
    Tuples.S2 clinit;   // class initializer (TxM)
    Tuples.S2 m_access; // method access (MxZ)
    Tuples.S2 f_access; // field access (FxZ)
    
    int last_V, last_H, last_T, last_N, last_F, last_M;
    
    public static boolean USE_HMAP = false;
    
    public SummaryToTuples() {
        Vmap = new IndexMap("var");
        Imap = new IndexMap("invoke");
        if (USE_HMAP)
            Hmap = new IndexMap("heap");
        else
            Hmap = Vmap;
        Fmap = new IndexMap("field");
        Tmap = new IndexMap("type");
        Nmap = new IndexMap("name");
        Mmap = new IndexMap("method");
        
        // Some nodes we always want to be index 0.
        Vmap.get(GlobalNode.GLOBAL);
        Hmap.get(GlobalNode.GLOBAL);
        Fmap.get(null);
        Tmap.get(null);
    }
    
    Map cache = new HashMap();
    
    public TupleSummary getSummary(jq_Method m) {
        TupleSummary s = (TupleSummary) cache.get(m);
        if (s == null) cache.put(m, s = new TupleSummary(m));
        return s;
    }
    
    void calcTypes() {
        vT = new TuplesArray.S2(16384);
        hT = new TuplesArray.S2(8192);
        aT = new TuplesArray.S2(8192);
        cha = new TuplesArray.S3(8192);
        methods = new TuplesArray.S2(4096);
        fields = new TuplesArray.S2(4096);
        clinit = new TuplesArray.S2(4096);
        m_access = new TuplesArray.S2(4096);
        f_access = new TuplesArray.S2(4096);
        last_V = 0; last_H = 0; last_T = 0; last_N = 0; last_F = 0; last_M = 0;
        calcTypes_inc();
    }
    
    void calcTypes_inc() {
        // build up 'vT'
        int Vsize = Vmap.size();
        for (int V_i = last_V; V_i < Vsize; ++V_i) {
            Node n = (Node) Vmap.get(V_i);
            jq_Reference type = n.getDeclaredType();
            if (type != null) type.prepare();
            vT.add(V_i, Tmap.get(type));
        }
        
        // build up 'hT'
        int Hsize = Hmap.size();
        for (int H_i = last_H; H_i < Hsize; ++H_i) {
            Node n = (Node) Hmap.get(H_i);
            if (!USE_HMAP) {
                if (n instanceof MethodSummary.FieldNode ||
                    n instanceof MethodSummary.ParamNode ||
                    n instanceof MethodSummary.CheckCastNode ||
                    n instanceof MethodSummary.ReturnValueNode ||
                    n instanceof MethodSummary.ThrownExceptionNode
                    ) continue;
            }
            jq_Reference type = n.getDeclaredType();
            if (type != null) type.prepare();
            hT.add(H_i, Tmap.get(type));
        }

        if (ADD_SUPERTYPES) {
            // add all supertypes to type map
            for (int T_i = 0; T_i < Tmap.size(); ++T_i) {
                jq_Reference t1 = (jq_Reference) Tmap.get(T_i);
                if (t1 == null || t1 instanceof jq_NullType) continue;
                t1.prepare();
                jq_Reference t2 = t1.getDirectPrimarySupertype();
                if (t2 != null) {
                    t2.prepare();
                    Tmap.get(t2);
                }
                jq_Class[] c = t1.getInterfaces();
                for (int i = 0; i < c.length; ++i) {
                    Tmap.get(c[i]);
                }
            }
        }
        
        int Tsize = Tmap.size();
        // build up 'aT'
        for (int T1_i = 0; T1_i < Tsize; ++T1_i) {
            jq_Reference t1 = (jq_Reference) Tmap.get(T1_i);
            int start = (T1_i < last_T)?last_T:0;
            for (int T2_i = start; T2_i < Tsize; ++T2_i) {
                jq_Reference t2 = (jq_Reference) Tmap.get(T2_i);
                if (t2 == null || (t1 != null && t2.isSubtypeOf(t1))) {
                    aT.add(T1_i, T2_i);
                }
            }
        }
        
        // build up 'cha'
        int Nsize = Nmap.size();
        for (int T_i = 0; T_i < Tsize; ++T_i) {
            jq_Reference t = (jq_Reference) Tmap.get(T_i);
            int start = (T_i < last_T)?last_N:0;
            for (int N_i = start; N_i < Nsize; ++N_i) {
                jq_Method n = (jq_Method) Nmap.get(N_i);
                if (n == null) continue;
                n.getDeclaringClass().prepare();
                jq_Method m;
                if (n.isStatic()) {
                    if (t != null) continue;
                    m = n;
                } else {
                    if (t == null ||
                        t == jq_NullType.NULL_TYPE ||
                        !t.isSubtypeOf(n.getDeclaringClass())) continue;
                    m = t.getVirtualMethod(n.getNameAndDesc());
                }
                if ((m == javaLangObject_clone && t != object_class) || n == javaLangObject_fakeclone) {
                    m = fakeCloneIfNeeded(t);                                      // for t.clone()
                    cha.add(T_i, Nmap.get(javaLangObject_fakeclone), Mmap.get(m)); // for super.clone()
                }
                if (m == null) continue;
                cha.add(T_i, N_i, Mmap.get(m));
            }
        }
        
        int Fsize = Fmap.size();
        for (int F_i = last_F; F_i < Fsize; ++F_i) {
            jq_Member f = (jq_Member) Fmap.get(F_i);
            int a;
            if (f == null || f.isPublic()) a = 0;
            else if (f.isPrivate()) a = 1;
            else if (f.isProtected()) a = 2;
            else a = 3;
            f_access.add(F_i, a);
            if (f != null)
                fields.add(Tmap.get(f.getDeclaringClass()), F_i);
        }
        
        int Msize = Mmap.size();
        for (int M_i = last_M; M_i < Msize; ++M_i) {
            jq_Member f = (jq_Member) Mmap.get(M_i);
            int a;
            if (f.isPublic()) a = 0;
            else if (f.isPrivate()) a = 1;
            else if (f.isProtected()) a = 2;
            else a = 3;
            m_access.add(M_i, a);
            methods.add(Tmap.get(f.getDeclaringClass()), M_i);
            if (f instanceof jq_ClassInitializer)
                clinit.add(Tmap.get(f.getDeclaringClass()), M_i);
        }
        
        last_V = Vsize;
        last_H = Hsize;
        last_T = Tsize;
        last_N = Nsize;
        last_F = Fsize;
        last_M = Msize;
        if (Vsize != Vmap.size() ||
            Hsize != Hmap.size() ||
            Tsize != Tmap.size() ||
            Nsize != Nmap.size() ||
            Fsize != Fmap.size() ||
            Msize != Mmap.size()) {
            if (TRACE) out.println("Elements added, recalculating types...");
            calcTypes_inc();
        }
    }
    
    private jq_Method fakeCloneIfNeeded(jq_Type t) {
        jq_Method m = javaLangObject_clone;
        if (t instanceof jq_Class) {
            jq_Class c = (jq_Class) t;
            if (!c.isInterface() && c.implementsInterface(cloneable_class)) {
                m = jq_FakeInstanceMethod.fakeMethod(c, MethodSummary.fakeCloneName, "()"+t.getDesc());
            }
        }
        // TODO: handle cloning of arrays
        return m;
    }
    
    public class TupleSummary {
        jq_Method m;
        MethodSummary ms;
        
        int[] formal; // formal parameters (ZxV)
        int global;   // global node (V)
        
        Tuples.S2 vP;     // object creation sites (VxH)
        Tuples.S2 A;      // assignments (VxV)
        Tuples.S3 L;      // loads (VxFxV)
        Tuples.S3 S;      // stores (VxFxV)
        Tuples.S2 calls;  // calls (IxN)
        Tuples.S3 actual; // params to calls (IxZxV)
        Tuples.S2 Iret;   // returns from calls (IxV)
        Tuples.S2 Ithr;   // throws from calls (IxV)
        Tuples.S1 sync;   // syncs (V)
        Tuples.S1 vars;   // all vars in method (V)
        Tuples.S1 ret;    // returned (V)
        Tuples.S1 thr;    // thrown (V)
        Tuples.S2 sc;     // statically-bound calls (IxM)
        
        public TupleSummary(jq_Method m) {
            visitMethod(m);
        }
        
        public TupleSummary(MethodSummary ms) {
            this.m = ms.getMethod();
            visitMethodSummary(ms);
        }
        
        void visitMethod(jq_Method m) {
            Assert._assert(m != null);
            Assert._assert(m.getDeclaringClass() != null);
            
            if (TRACE) out.println("Visiting method "+m);
            m.getDeclaringClass().prepare();
            this.m = m;
            this.ms = MethodSummary.getSummary(m);
            Mmap.get(m);
            
            if (m.getBytecode() == null && ms == null) {
                // initialize tuples.
                init();
                
                // build up 'formal'
                handleFormalParams();
                
                // build up 'Mret'
                jq_Type retType = m.getReturnType();
                if (retType instanceof jq_Reference) {
                    Node node = UnknownTypeNode.get((jq_Reference) retType);
                    int V_i = Vmap.get(node);
                    vars.add(V_i);
                    ret.add(V_i);
                    visitNode(node);
                }
                if (!SKIP_EXCEPTIONS) {
                    Node node = UnknownTypeNode.get(PrimordialClassLoader.getJavaLangThrowable());
                    int V_i = Vmap.get(node);
                    vars.add(V_i);
                    thr.add(V_i);
                    visitNode(node);
                }
                return;
            }
            
            visitMethodSummary(ms);
        }
        
        boolean skipNode(Node node) {
            if (SKIP_NULL && MethodSummary.isNullConstant(node))
                return true;
            if (SKIP_EXCEPTIONS && node instanceof MethodSummary.ThrownExceptionNode)
                return true;
            return false;
        }
        
        private void init() {
            vP = new TuplesArray.S2(2);
            A = new TuplesArray.S2(2);
            L = new TuplesArray.S3(2);
            S = new TuplesArray.S3(2);
            calls = new TuplesArray.S2(2);
            actual = new TuplesArray.S3(2);
            Iret = new TuplesArray.S2(2);
            Ithr = new TuplesArray.S2(2);
            sync = new TuplesArray.S1(2);
            vars = new TuplesArray.S1(2);
            ret = new TuplesArray.S1(2);
            thr = new TuplesArray.S1(2);
            sc = new TuplesArray.S2(2);
        }
        
        void handleFormalParams() {
            int offset;
            Node thisparam;
            if (m.isStatic()) {
                thisparam = GlobalNode.GLOBAL;
                offset = 0;
            } else {
                if (ms != null)
                    thisparam = ms.getParamNode(0);
                else
                    thisparam = MethodSummary.ParamNode.get(m, 0, m.getDeclaringClass());
                offset = 1;
            }
            int nParams;
            if (ms != null) nParams = ms.getNumOfParams();
            else nParams = m.getParamTypes().length;
            formal = new int[nParams+1-offset];
            formal[0] = Vmap.get(thisparam);
            for (int i = offset; i < nParams; ++i) {
                Node node;
                if (ms != null) {
                    node = ms.getParamNode(i);
                } else {
                    jq_Type t = m.getParamTypes()[i];
                    if (!t.isReferenceType()) continue;
                    node = MethodSummary.ParamNode.get(m, i, (jq_Reference) t);
                }
                formal[i+1-offset] = node==null?-1:Vmap.get(node);
            }
            
            if (m.isSynchronized()) {
                if (TRACE) out.println("Synchronized method, sync on: "+thisparam);
                sync.add(formal[0]);
                // TODO: correctly handle static synchronized methods
            }
            
            if (ms != null) {
                global = Vmap.get(ms.getGlobal());
            } else {
                global = Vmap.get(GlobalNode.GLOBAL);
            }
        }
        
        void visitMethodSummary(MethodSummary ms) {
            
            if (TRACE) out.println("Visiting method summary "+ms);
            
            if (vP == null) {
                int nNodes = ms.nodes.size();
                int nCalls = ms.calls.size();
                vP = new TuplesArray.S2(nNodes/2+1);
                A = new TuplesArray.S2(nNodes/4+1);
                L = new TuplesArray.S3(nNodes/2+1);
                S = new TuplesArray.S3(nNodes/4+1);
                calls = new TuplesArray.S2(nCalls+1);
                actual = new TuplesArray.S3(nCalls*3+1);
                Iret = new TuplesArray.S2(nCalls+1);
                Ithr = new TuplesArray.S2(nCalls+1);
                sync = new TuplesArray.S1(2);
                vars = new TuplesArray.S1(nNodes+1);
                ret = new TuplesArray.S1(8);
                thr = new TuplesArray.S1(8);
                sc = new TuplesArray.S2(nCalls+1);
            }
            
            // build up 'formal'
            handleFormalParams();
            
            // build up 'sync'
            for (Iterator i = ms.getSyncedVars().iterator(); i.hasNext(); ) {
                Node node = (Node) i.next();
                if (skipNode(node)) continue;
                if (TRACE) out.println("Sync on: "+node);
                sync.add(Vmap.get(node));
            }
            
            // build up 'mI', 'actual', 'Iret', 'Ithr'
            for (Iterator i = ms.getCalls().iterator(); i.hasNext(); ) {
                ProgramLocation mc = (ProgramLocation) i.next();
                if (TRACE) out.println("Visiting call site "+mc);
                int I_i = Imap.get(LoadedCallGraph.mapCall(mc));
                jq_Method target = mc.getTargetMethod();

                Set thisptr;
                int offset;
                if (target.isStatic()) {
                    thisptr = Collections.singleton(GlobalNode.GLOBAL);
                    offset = 0;
                } else {
                    thisptr = ms.getNodesThatCall(mc, 0);
                    offset = 1;
                }
                for (Iterator j = thisptr.iterator(); j.hasNext(); ) {
                    Node n = (Node) j.next();
                    if (!skipNode(n)) {
                        actual.add(I_i, 0, Vmap.get(n));
                    }
                }
                
                if (mc.isSingleTarget()) {
                    if (target != javaLangObject_clone) {
                        // statically-bound, single target call
                        calls.add(I_i, Nmap.get(null));
                        sc.add(I_i, Mmap.get(target));
                    } else {
                        // super.clone()
                        calls.add(I_i, Nmap.get(javaLangObject_fakeclone));
                    }
                } else {                
                    // virtual call
                    calls.add(I_i, Nmap.get(target));
                }
                
                jq_Type[] params = mc.getParamTypes();
                int k = offset;
                for ( ; k < params.length; ++k) {
                    if (!params[k].isReferenceType()) {
                        continue;
                    }
                    Set s = ms.getNodesThatCall(mc, k);
                    for (Iterator j = s.iterator(); j.hasNext(); ) {
                        Node n = (Node) j.next();
                        if (skipNode(n)) continue;
                        actual.add(I_i, k+1-offset, Vmap.get(n));
                    }
                }
                Node node = ms.getRVN(mc);
                if (node != null && !skipNode(node)) {
                    Iret.add(I_i, Vmap.get(node));
                }
                if (!SKIP_EXCEPTIONS) {
                    node = ms.getTEN(mc);
                    if (node != null && !skipNode(node)) {
                        Ithr.add(I_i, Vmap.get(node));
                    }
                }
            }
           
            // build up 'mV', 'm_vP', 'S', 'L', 'Mret', 'Mthr'
            for (Iterator i = ms.nodeIterator(); i.hasNext(); ) {
                Node node = (Node) i.next();
                if (skipNode(node)) continue;
                
                int V_i = Vmap.get(node);
                vars.add(V_i);
                
                if (ms.getReturned().contains(node)) {
                    ret.add(V_i);
                }
                
                if (!SKIP_EXCEPTIONS && ms.getThrown().contains(node)) {
                    thr.add(V_i);
                }
                
                visitNode(node);
            }

            // build up 'A' from cast operations
            for (Iterator i = ms.getCastMap().entrySet().iterator(); i.hasNext(); ) {
                Map.Entry e = (Map.Entry)i.next();
                Node from = (Node)((Pair)e.getKey()).left;
                if (skipNode(from)) continue;
                CheckCastNode to = (CheckCastNode)e.getValue();
                int V_i = Vmap.get(to);
                A.add(V_i, Vmap.get(from));
            }
        }

        void visitNode(Node node) {
            if (TRACE) 
                out.println("Visiting node "+node);
            
            Assert._assert(!skipNode(node));
            
            int V_i = Vmap.get(node);
            
            if (node instanceof ConcreteTypeNode ||
                node instanceof ConcreteObjectNode ||
                node instanceof UnknownTypeNode ||
                node == GlobalNode.GLOBAL) {
                int H_i = Hmap.get(node);
                vP.add(V_i, H_i);
            }
            
            for (Iterator j = node.getAllEdges().iterator(); j.hasNext(); ) {
                Map.Entry e = (Map.Entry) j.next();
                jq_Field f = (jq_Field) e.getKey();
                Collection c;
                if (e.getValue() instanceof Collection)
                    c = (Collection) e.getValue();
                else
                    c = Collections.singleton(e.getValue());
                int F_i = Fmap.get(f);
                for (Iterator k = c.iterator(); k.hasNext(); ) {
                    Node node2 = (Node) k.next();
                    if (skipNode(node2)) continue;
                    int V2_i = Vmap.get(node2);
                    S.add(V_i, F_i, V2_i);
                }
            }
            
            for (Iterator j = node.getAccessPathEdges().iterator(); j.hasNext(); ) {
                Map.Entry e = (Map.Entry) j.next();
                jq_Field f = (jq_Field) e.getKey();
                Collection c;
                if (e.getValue() instanceof Collection)
                    c = (Collection) e.getValue();
                else
                    c = Collections.singleton(e.getValue());
                int F_i = Fmap.get(f);
                for (Iterator k = c.iterator(); k.hasNext(); ) {
                    Node node2 = (Node) k.next();
                    int V2_i = Vmap.get(node2);
                    L.add(V_i, F_i, V2_i);
                }
            }
        }
        
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append(m);
            sb.append('\n');
            sb.append("Params: ");
            for (int i = 0; i < formal.length; ++i) {
                if (i > 0) sb.append(',');
                sb.append(formal[i]);
            }
            sb.append("\nGlobal: ");
            sb.append(global);
            if (!vP.isEmpty()) {
                sb.append("\nvP: ");
                sb.append(vP);
            }
            if (!A.isEmpty()) {
                sb.append("\nA: ");
                sb.append(A);
            }
            if (!L.isEmpty()) {
                sb.append("\nL: ");
                sb.append(L);
            }
            if (!S.isEmpty()) {
                sb.append("\nS: ");
                sb.append(S);
            }
            if (!calls.isEmpty()) {
                sb.append("\ncalls: ");
                sb.append(calls);
            }
            if (!actual.isEmpty()) {
                sb.append("\nactual: ");
                sb.append(actual);
            }
            if (!Iret.isEmpty()) {
                sb.append("\nIret: ");
                sb.append(Iret);
            }
            if (!Ithr.isEmpty()) {
                sb.append("\nIthr: ");
                sb.append(Ithr);
            }
            if (!sync.isEmpty()) {
                sb.append("\nsync: ");
                sb.append(sync);
            }
            if (!vars.isEmpty()) {
                sb.append("\nvars: ");
                sb.append(vars);
            }
            if (!ret.isEmpty()) {
                sb.append("\nret: ");
                sb.append(ret);
            }
            if (!thr.isEmpty()) {
                sb.append("\nthr: ");
                sb.append(thr);
            }
            if (!sc.isEmpty()) {
                sb.append("\nsc: ");
                sb.append(sc);
            }
            sb.append('\n');
            return sb.toString();
        }
    }
    
    String mungeMethodName(jq_Method m) {
        if (m == null) return "null";
        return m.toString();
    }
    String mungeFieldName(jq_Field m) {
        if (m == null) return "null";
        return m.toString();
    }
    String mungeTypeName2(jq_Type m) {
        if (m == null) return "null";
        return m.toString();
    }
    
    public void dump() throws IOException {
        String dumpPath = System.getProperty("pa.dumppath", "");
        if (dumpPath.length() > 0) {
            File f = new File(dumpPath);
            if (!f.exists()) f.mkdirs();
            String sep = System.getProperty("file.separator", "/");
            if (!dumpPath.endsWith(sep))
                dumpPath += sep;
        }
        dump(dumpPath);
    }
    
    public void dump(String dumpPath) throws IOException {
        System.out.println("Dumping to path "+dumpPath);
        
        int V_BITS = BigInteger.valueOf(Vmap.size()).bitLength();
        int H_BITS = BigInteger.valueOf(Hmap.size()).bitLength();
        int T_BITS = BigInteger.valueOf(Tmap.size()).bitLength();
        int F_BITS = BigInteger.valueOf(Fmap.size()).bitLength();
        int I_BITS = BigInteger.valueOf(Imap.size()).bitLength();
        int Z_BITS = 6;
        int N_BITS = BigInteger.valueOf(Nmap.size()).bitLength();
        int M_BITS = BigInteger.valueOf(Mmap.size()).bitLength();
        int VC_BITS = 62;
        
        BufferedWriter dos = null;
        try {
            dos = new BufferedWriter(new FileWriter(dumpPath+"fielddomains.pa"));
            dos.write("V "+(1L<<V_BITS)+" var.map\n");
            dos.write("H "+(1L<<H_BITS)+" heap.map\n");
            dos.write("T "+(1L<<T_BITS)+" type.map\n");
            dos.write("F "+(1L<<F_BITS)+" field.map\n");
            dos.write("I "+(1L<<I_BITS)+" invoke.map\n");
            dos.write("Z "+(1L<<Z_BITS)+"\n");
            dos.write("N "+(1L<<N_BITS)+" name.map\n");
            dos.write("M "+(1L<<M_BITS)+" method.map\n");
            dos.write("VC "+(1L<<VC_BITS)+"\n");
        } finally {
            if (dos != null) dos.close();
        }
        
        int tuples = 0;
        dos = null;
        try {
            dos = new BufferedWriter(new FileWriter(dumpPath+"m_formal.tuples"));
            dos.write("# M0:"+M_BITS+" Z0:"+Z_BITS+" V0:"+V_BITS+"\n");
            for (Iterator i = cache.values().iterator(); i.hasNext(); ) {
                TupleSummary s = (TupleSummary) i.next();
                String prefix = Mmap.get(s.m)+" ";
                for (int j = 0; j < s.formal.length; ++j) {
                    if (s.formal[j] == -1) continue;
                    dos.write(prefix+j+" "+s.formal[j]+"\n");
                    ++tuples;
                }
            }
            System.out.println("Wrote formal.tuples"+": "+tuples+" elements");
        } finally {
            if (dos != null) dos.close();
        }
        
        tuples = 0;
        dos = null;
        try {
            dos = new BufferedWriter(new FileWriter(dumpPath+"m_global.tuples"));
            dos.write("# M0:"+M_BITS+" V0:"+V_BITS+"\n");
            for (Iterator i = cache.values().iterator(); i.hasNext(); ) {
                TupleSummary s = (TupleSummary) i.next();
                if (s.global == -1) continue;
                dos.write(Mmap.get(s.m)+" "+s.global+"\n");
                ++tuples;
            }
            System.out.println("Wrote global.tuples"+": "+tuples+" elements");
        } finally {
            if (dos != null) dos.close();
        }
        
        Field[] fs = TupleSummary.class.getDeclaredFields();
        for (int k = 0; k < fs.length; ++k) {
            if (!Tuples.Interface.class.isAssignableFrom(fs[k].getType())) {
                continue;
            }
            tuples = 0;
            dos = null;
            try {
                dos = new BufferedWriter(new FileWriter(dumpPath+"m_"+fs[k].getName()+".tuples"));
                for (Iterator i = cache.values().iterator(); i.hasNext(); ) {
                    TupleSummary s = (TupleSummary) i.next();
                    String prefix = Mmap.get(s.m)+" ";
                    Tuples.Interface t;
                    try {
                        t = (Tuples.Interface) fs[k].get(s);
                        t.dump(prefix, dos);
                        tuples += t.size();
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("Wrote "+fs[k].getName()+".tuples"+": "+tuples+" elements");
            } finally {
                if (dos != null) dos.close();
            }
        }
        
        fs = SummaryToTuples.class.getDeclaredFields();
        for (int k = 0; k < fs.length; ++k) {
            if (!Tuples.Interface.class.isAssignableFrom(fs[k].getType())) {
                continue;
            }
            dos = null;
            try {
                dos = new BufferedWriter(new FileWriter(dumpPath+fs[k].getName()+".tuples"));
                Tuples.Interface t;
                try {
                    t = (Tuples.Interface) fs[k].get(this);
                    t.dump(dos);
                    System.out.println("Wrote "+fs[k].getName()+".tuples: "+t.size()+" elements");
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            } finally {
                if (dos != null) dos.close();
            }
        }
        
        dos = null;
        try {
            dos = new BufferedWriter(new FileWriter(dumpPath+"var.map"));
            for (int j = 0; j < Vmap.size(); ++j) {
                Node o = (Node)Vmap.get(j);
                dos.write(o.id+": "+o+"\n");
            }
        } finally {
            if (dos != null) dos.close();
        }
        
        if (USE_HMAP) {
            dos = null;
            try {
                dos = new BufferedWriter(new FileWriter(dumpPath+"heap.map"));
                for (int j = 0; j < Hmap.size(); ++j) {
                    Node o = (Node) Hmap.get(j);
                    dos.write(o.id+": "+o+"\n");
                }
            } finally {
                if (dos != null) dos.close();
            }
        }
        
        dos = null;
        try {
            dos = new BufferedWriter(new FileWriter(dumpPath+"type.map"));
            for (int j = 0; j < Tmap.size(); ++j) {
                jq_Type o = (jq_Type) Tmap.get(j);
                dos.write(mungeTypeName2(o)+"\n");
            }
        } finally {
            if (dos != null) dos.close();
        }
        
        dos = null;
        try {
            dos = new BufferedWriter(new FileWriter(dumpPath+"field.map"));
            for (int j = 0; j < Fmap.size(); ++j) {
                jq_Field o = (jq_Field) Fmap.get(j);
                dos.write(mungeFieldName(o)+"\n");
            }
        } finally {
            if (dos != null) dos.close();
        }
        
        dos = null;
        try {
            dos = new BufferedWriter(new FileWriter(dumpPath+"invoke.map"));
            for (int j = 0; j < Imap.size(); ++j) {
                ProgramLocation o = (ProgramLocation)Imap.get(j);
                dos.write(o.hashCode()+": "+o+"\n");
            }
        } finally {
            if (dos != null) dos.close();
        }
        
        dos = null;
        try {
            dos = new BufferedWriter(new FileWriter(dumpPath+"name.map"));
            for (int j = 0; j < Nmap.size(); ++j) {
                jq_Method o = (jq_Method) Nmap.get(j);
                dos.write(mungeMethodName(o)+"\n");
            }
        } finally {
            if (dos != null) dos.close();
        }
        
        dos = null;
        try {
            dos = new BufferedWriter(new FileWriter(dumpPath+"method.map"));
            for (int j = 0; j < Mmap.size(); ++j) {
                Object o = Mmap.get(j);
                jq_Method m = (jq_Method) o;
                dos.write(mungeMethodName(m)+"\n");
            }
        } finally {
            if (dos != null) dos.close();
        }

    }
    
    public static Collection/*<jq_Method>*/ parseMethod(String[] args) throws IOException {
        jq_Class c = (jq_Class) jq_Type.parseType(args[0]);
        c.load();
        String name = null, desc = null;
        if (args.length > 1) {
            name = args[1];
            if (args.length > 2) {
                desc = args[2];
            }
        }
        Collection methods;
        if (name != null) {
            jq_Method m;
            if (desc != null) {
                m = (jq_Method) c.getDeclaredMember(name, desc);
            } else {
                m = c.getDeclaredMethod(name);
            }
            methods = Collections.singleton(m);
        } else {
            methods = new LinkedList();
            methods.addAll(Arrays.asList(c.getDeclaredStaticMethods()));
            methods.addAll(Arrays.asList(c.getDeclaredInstanceMethods()));
        }
        return methods;
    }
    
    public static String canonicalizeClassName(String s) {
        if (s.endsWith(".class")) s = s.substring(0, s.length() - 6);
        s = s.replace('.', '/');
        String desc = "L" + s + ";";
        return desc;
    }
    
    private static Collection getClassesInPackage(String pkgName, boolean recursive) {
        String canonicalPackageName = pkgName.replace('.', '/');
        if (!canonicalPackageName.endsWith("/")) canonicalPackageName += '/';
        Iterator i = PrimordialClassLoader.loader.listPackage(canonicalPackageName, recursive);
        if (!i.hasNext()) {
            System.err.println("Package " + canonicalPackageName + " not found.");
        }
        // Because listPackage() may return entries twice, we record loaded
        // entries in 'loaded' and skip dups
        Collection result = new LinkedList();
        HashSet loaded = new HashSet();
        while (i.hasNext()) {
            String canonicalClassName = canonicalizeClassName((String) i.next());
            if (loaded.contains(canonicalClassName))
                continue;
            loaded.add(canonicalClassName);
            try {
                //System.out.println("Loading "+canonicalClassName);
                jq_Class c = (jq_Class) PrimordialClassLoader.loader.getOrCreateBSType(canonicalClassName);
                c.load();
                result.add(c);
            } catch (SecurityException x) {
                System.err.println("Security exception occurred while loading class (" + canonicalClassName + "):");
            } catch (NoClassDefFoundError x) {
                System.err.println("Package " + pkgName + ": Class not found (canonical name " + canonicalClassName + ").");
            } catch (LinkageError le) {
                System.err.println("Linkage error occurred while loading class (" + canonicalClassName + "):");
                le.printStackTrace(System.err);
            }
        }
        return result;
    }
    
    public static Collection/*<jq_Method>*/ parseClass(String arg) throws IOException {
        Collection classes;
        if (arg.endsWith(".**")) {
            String packageName = arg.substring(0, arg.length()-3);
            classes = getClassesInPackage(packageName, true);
        } else if (arg.endsWith(".*")) {
            String packageName = arg.substring(0, arg.length()-2);
            classes = getClassesInPackage(packageName, false);
        } else {
            String canonicalClassName = canonicalizeClassName(arg);
            jq_Class c = (jq_Class) PrimordialClassLoader.loader.getOrCreateBSType(canonicalClassName);
            c.load();
            classes = Collections.singleton(c);
        }
        Collection methods = new LinkedList();
        for (Iterator i = classes.iterator(); i.hasNext(); ) {
            jq_Class c = (jq_Class) i.next();
            methods.addAll(Arrays.asList(c.getDeclaredStaticMethods()));
            methods.addAll(Arrays.asList(c.getDeclaredInstanceMethods()));
        }
        return methods;
    }
    
    public static void main(String[] args) throws IOException {
        HostedVM.initialize();
        CodeCache.AlwaysMap = true;
        
        Collection methods = new LinkedList();
        for (int i = 0; i < args.length; ++i) {
            Collection methods2 = parseClass(args[i]);
            methods.addAll(methods2);
        }
        System.out.println("Processing "+methods.size()+" methods...");
        SummaryToTuples dis = new SummaryToTuples();
        for (Iterator i = methods.iterator(); i.hasNext(); ) {
            jq_Method m = (jq_Method) i.next();
            if (m.getBytecode() == null) continue;
            TupleSummary s = dis.getSummary(m);
            //System.out.println(s);
        }
        dis.calcTypes();
        dis.dump();
    }
    
}
