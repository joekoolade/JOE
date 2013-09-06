// LoadedCallGraph.java, created Jun 27, 2003 12:46:40 AM by joewhaley
// Copyright (C) 2003 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.Quad;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.io.BufferedWriter;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import joeq.Class.jq_Class;
import joeq.Class.jq_FakeInstanceMethod;
import joeq.Class.jq_Member;
import joeq.Class.jq_Method;
import joeq.Class.jq_Type;
import joeq.Compiler.Analysis.IPA.ProgramLocation;
import joeq.Compiler.Analysis.IPA.ProgramLocation.BCProgramLocation;
import jwutil.collections.GenericInvertibleMultiMap;
import jwutil.collections.GenericMultiMap;
import jwutil.collections.InvertibleMultiMap;
import jwutil.collections.MapFactory;
import jwutil.collections.MultiMap;
import jwutil.collections.SetFactory;
import jwutil.collections.SortedArraySet;
import jwutil.util.Assert;

/**
 * A call graph that is loaded from a file.
 * 
 * @author John Whaley
 * @version $Id: LoadedCallGraph.java,v 1.24 2006/04/06 02:31:41 mcmartin Exp $
 */
public class LoadedCallGraph extends CallGraph {

    public static Comparator type_comparator = new Comparator() {
        public int compare(Object o1, Object o2) {
            String s1 = ((jq_Type) o1).getDesc().toString();
            String s2 = ((jq_Type) o2).getDesc().toString();
            return s1.compareTo(s2);
        }
    };
    public static Comparator member_comparator = new Comparator() {
        public int compare(Object o1, Object o2) {
            String s1 = ((jq_Member) o1).getNameAndDesc().toString();
            String s2 = ((jq_Member) o2).getNameAndDesc().toString();
            return s1.compareTo(s2);
        }
    };
    public static Comparator callsite_comparator = new Comparator() {
        public int compare(Object o1, Object o2) {
            ProgramLocation p1 = (ProgramLocation) o1;
            ProgramLocation p2 = (ProgramLocation) o2;
            Assert._assert(p1.getMethod() == p2.getMethod());
            int i1 = p1.getBytecodeIndex();
            int i2 = p2.getBytecodeIndex();
            if (i1 < i2) return -1;
            if (i1 > i2) return 1;
//            Assert._assert(o1 == o2);
            return 0;
        }
    };
    
    public static final MapFactory treeMapFactory = new MapFactory() {
        public Map makeMap(Map map) {
            TreeMap m = new TreeMap(type_comparator);
            m.putAll(map);
            return m;
        }
    };
    
    public static final SortedArraySetFactory sortedArraySetFactory = new SortedArraySetFactory();
    
    public static class SortedArraySetFactory extends SetFactory {
        /**
         * Version ID for serialization.
         */
        private static final long serialVersionUID = 3906646414531702833L;
        
        public Set makeSet(Comparator c1, Collection c2) {
            Set s = SortedArraySet.FACTORY.makeSet(c1);
            s.addAll(c2);
            return s;
        }
        public Set makeSet(Collection c) {
            return makeSet(member_comparator, c);
        }
    }
    
    public static void write(CallGraph cg, BufferedWriter out) throws IOException {
        Assert._assert(cg.getAllMethods().containsAll(cg.getRoots()));
        MultiMap classesToMethods = new GenericMultiMap(treeMapFactory, sortedArraySetFactory);
        for (Iterator i = cg.getAllMethods().iterator(); i.hasNext(); ) {
            jq_Method m = (jq_Method) i.next();
            if (m == null) continue;
            classesToMethods.add(m.getDeclaringClass(), m);
        }
        for (Iterator i = classesToMethods.keySet().iterator(); i.hasNext(); ) {
            jq_Class klass = (jq_Class) i.next();
            out.write("CLASS "+klass.getJDKName().replace('.', '/')+"\n");
            for (Iterator j = classesToMethods.getValues(klass).iterator(); j.hasNext(); ) {
                jq_Method m = (jq_Method) j.next();
                out.write(" METHOD "+m.getName()+" "+m.getDesc());
                if (cg.getRoots().contains(m)) out.write(" ROOT");
                if (m instanceof jq_FakeInstanceMethod) out.write(" FAKE");
                out.write('\n');
                // put them in a set for deterministic iteration.
                Set s = sortedArraySetFactory.makeSet(callsite_comparator, cg.getCallSites(m));
                for (Iterator k = s.iterator(); k.hasNext(); ) {
                    ProgramLocation pl = (ProgramLocation) k.next();
                    out.write("  CALLSITE "+pl.getBytecodeIndex()+"\n");
                    // put them in a set for deterministic iteration.
                    Set s2 = sortedArraySetFactory.makeSet(cg.getTargetMethods(pl));
                    for (Iterator l = s2.iterator(); l.hasNext(); ) {
                        jq_Method target = (jq_Method) l.next();
                        if (target == null) continue;
                        out.write("   TARGET "+target.getDeclaringClass().getJDKName().replace('.', '/')+"."+target.getName()+" "+target.getDesc());
                        if (target instanceof jq_FakeInstanceMethod)
                            out.write(" FAKE");
                        out.write("\n");
                    }
                }
            }
        }
    }

    protected Set/*<jq_Method>*/ methods;
    protected Set/*<jq_Method>*/ roots;
    protected MultiMap/*<jq_Method,Integer>*/ callSites;
    protected InvertibleMultiMap/*<ProgramLocation,jq_Method>*/ edges;
    protected boolean bcCallSites;
    private static final boolean MAPPING_OFF = false;

    public LoadedCallGraph(String filename) throws IOException {
        this.methods = new LinkedHashSet();
        this.roots = new LinkedHashSet();
        this.callSites = new GenericMultiMap();
        this.edges = new GenericInvertibleMultiMap();
        DataInput in = new DataInputStream(new FileInputStream(filename));
        read(in);
    }
    
    protected void read(DataInput in) throws IOException {
        jq_Class k = null;
        jq_Method m = null;
        int bcIndex = -1;
        for (;;) {
            String s = in.readLine();
            if (s == null)
                break;
            s = s.trim();
            StringTokenizer st = new StringTokenizer(s, ". ");
            if (!st.hasMoreTokens())
                break;
            String id = st.nextToken();
            if (id.equals("CLASS")) {
                if (!st.hasMoreTokens())
                    throw new IOException();
                String className = st.nextToken();
                k = (jq_Class) jq_Type.parseType(className);
                k.load();
                continue;
            }
            if (id.equals("METHOD")) {
                if (!st.hasMoreTokens())
                    throw new IOException();
                String methodName = st.nextToken();
                if (!st.hasMoreTokens())
                    throw new IOException();
                String methodDesc = st.nextToken();
                boolean isroot = false;
                boolean isfake = false;
                while (st.hasMoreTokens()) {
                    String arg = st.nextToken();
                    if (arg.equals("ROOT")) isroot = true;
                    if (arg.equals("FAKE")) isfake = true;
                }
                if (isfake)
                    m = jq_FakeInstanceMethod.fakeMethod(k, methodName, methodDesc);
                else
                    m = (jq_Method) k.getDeclaredMember(methodName, methodDesc);
                if (m == null) {
                    System.err.println("Cannot find \""+methodName+"\" \""+methodDesc+"\" in "+k);
                    continue;
                }
                methods.add(m);
                if (isroot)
                    roots.add(m);
                continue;
            }
            if (id.equals("CALLSITE")) {
                if (!st.hasMoreTokens())
                    throw new IOException();
                String num = st.nextToken();
                bcIndex = Integer.parseInt(num);
                bcCallSites = true;
                continue;
            }
            if (id.equals("TARGET")) {
                if (!st.hasMoreTokens())
                    throw new IOException();
                String className = st.nextToken();
                if (!st.hasMoreTokens())
                    throw new IOException();
                String methodName = st.nextToken();
                if (!st.hasMoreTokens())
                    throw new IOException();
                String methodDesc = st.nextToken();
                boolean isfake = false;
                if (st.hasMoreTokens()) {
                    String arg = st.nextToken();
                    if (arg.equals("FAKE"))
                        isfake = true;
                }
                    
                jq_Class targetClass = (jq_Class) jq_Type.parseType(className);
                targetClass.load();
                jq_Method targetMethod;
                if (isfake)
                    targetMethod = jq_FakeInstanceMethod.fakeMethod(targetClass, methodName, methodDesc);
                else
                    targetMethod = (jq_Method) targetClass.getDeclaredMember(methodName, methodDesc);
                if (m == null) {
                    // reported above.
                    continue;
                }
                if (targetMethod == null) {
                    System.err.println("Cannot find \""+methodName+"\" \""+methodDesc+"\" in "+targetClass);
                    continue;
                }
                add(m, bcIndex, targetMethod);
                continue;
            }
        }
    }

    public void add(jq_Method caller, int bcIndex, jq_Method callee) {
        ProgramLocation pl = new BCProgramLocation(caller, bcIndex);
        callSites.add(caller, pl);
        edges.add(pl, callee);
    }

    /* (non-Javadoc)
     * @see joeq.Compiler.Quad.CallGraph#setRoots(java.util.Collection)
     */
    public void setRoots(Collection roots) {
        // Root set should be the same!
        Assert._assert(this.roots.equals(roots));
    }

    /* (non-Javadoc)
     * @see joeq.Compiler.Quad.CallGraph#getRoots()
     */
    public Collection getRoots() {
        return roots;
    }

    /* (non-Javadoc)
     * @see joeq.Compiler.Quad.CallGraph#getTargetMethods(java.lang.Object, Compiler.Quad.ProgramLocation)
     */
    public Collection getTargetMethods(Object context, ProgramLocation callSite) {
        if (callSite instanceof ProgramLocation.QuadProgramLocation) {
            callSite = mapCall(callSite);
        }
        return edges.getValues(callSite);
    }

    /* (non-Javadoc)
     * @see joeq.Compiler.Quad.CallGraph#entrySet()
     */
    public Set entrySet() {
        return edges.entrySet();
    }

    /* (non-Javadoc)
     * @see joeq.Compiler.Quad.CallGraph#getAllCallSites()
     */
    public Collection getAllCallSites() {
        return edges.keySet();
    }

    /* (non-Javadoc)
     * @see joeq.Compiler.Quad.CallGraph#getAllMethods()
     */
    public Collection getAllMethods() {
        return methods;
    }

    /* (non-Javadoc)
     * @see joeq.Compiler.Quad.CallGraph#getCallees(joeq.Class.jq_Method)
     */
    public Collection getCallees(jq_Method caller) {
        Collection c = CachedCallGraph.getFromMultiMap(callSites, edges, caller);
        return c;
    }
    
    /* (non-Javadoc)
     * @see joeq.Compiler.Quad.CallGraph#getCallers(joeq.Class.jq_Method)
     */
    public Collection getCallers(jq_Method callee) {
        MultiMap m1 = edges.invert();
        Collection c1 = m1.getValues(callee);
        return c1;
    }
    
    /* (non-Javadoc)
     * @see joeq.Compiler.Quad.CallGraph#getCallerMethods(joeq.Class.jq_Method)
     */
    public Collection getCallerMethods(jq_Method callee) {
        MultiMap m1 = edges.invert();
        Collection c1 = m1.getValues(callee);
        Iterator i = c1.iterator();
        if (!i.hasNext()) {
            return Collections.EMPTY_SET;
        }
        ProgramLocation o = (ProgramLocation) i.next();
        if (!i.hasNext()) {
            return Collections.singleton(o.getMethod());
        }
        Set result = new LinkedHashSet();
        for (;;) {
            result.add(o.getMethod());
            if (!i.hasNext()) break;
            o = (ProgramLocation) i.next();
        }
        return result;
    }
    
    /* (non-Javadoc)
     * @see joeq.Compiler.Quad.CallGraph#getCallSites(joeq.Class.jq_Method)
     */
    public Collection getCallSites(jq_Method caller) {
        Collection c = callSites.getValues(caller);
        return c;
    }

    /* (non-Javadoc)
     * @see java.util.AbstractMap#keySet()
     */
    public Set keySet() {
        return edges.keySet();
    }

    public static ProgramLocation mapCall(ProgramLocation callSite) {
        if(!MAPPING_OFF){
            if (callSite instanceof ProgramLocation.QuadProgramLocation) {
                jq_Method m = (jq_Method) callSite.getMethod();
                Map map = CodeCache.getBCMap(m);
                //CodeCache.invalidateBCMap(m);
                Quad q = ((ProgramLocation.QuadProgramLocation) callSite).getQuad();
                if (q == null) {
                    Assert.UNREACHABLE("Error: cannot find call site "+callSite);
                }
                Integer i = (Integer) map.get(q);
                if (i == null) {
//                    Assert.UNREACHABLE("Error: no mapping for quad "+q);
                    return new ProgramLocation.FakeProgramLocation(m, "Fake location " + callSite.toString());
                }
                int bcIndex = i.intValue();
                callSite = new ProgramLocation.BCProgramLocation(m, bcIndex);
            }
        }
        return callSite;        
    }    
}
