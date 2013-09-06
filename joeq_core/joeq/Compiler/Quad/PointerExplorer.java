// PointerExplorer.java, created Tue Aug 27 16:04:29 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.Quad;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import joeq.Class.jq_Class;
import joeq.Class.jq_Field;
import joeq.Class.jq_Method;
import joeq.Class.jq_Type;
import joeq.Compiler.Analysis.FlowInsensitive.MethodSummary;
import joeq.Compiler.Analysis.FlowInsensitive.MethodSummary.CallSite;
import joeq.Compiler.Analysis.FlowInsensitive.MethodSummary.PassedParameter;
import joeq.Compiler.Analysis.IPA.ProgramLocation;
import joeq.Compiler.Quad.AndersenPointerAnalysis.AccessPath;
import joeq.Main.HostedVM;
import jwutil.collections.Filter;
import jwutil.collections.FilterIterator;
import jwutil.collections.LinearSet;
import jwutil.collections.Pair;
import jwutil.util.Assert;

/**
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: PointerExplorer.java,v 1.31 2004/09/22 22:17:26 joewhaley Exp $
 */
public class PointerExplorer {

    public static final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    
    public static jq_Method getMethod(Set/*<jq_Method>*/ set) throws IOException {
        int which, count = 0;
        for (Iterator i=set.iterator(); i.hasNext(); ) {
            jq_Method m = (jq_Method)i.next();
            System.out.println((++count)+": "+m);
        }
        for (;;) {
            System.out.print("Which method? ");
            String s = in.readLine();
            try {
                which = Integer.parseInt(s);
                if ((which >= 1) && (which <= count))
                    break;
                System.out.println("Out of range: "+which);
            } catch (NumberFormatException x) {
                System.out.println("Not a number: "+s);
            }
        }
        for (Iterator i=set.iterator(); ; ) {
            jq_Method m = (jq_Method)i.next();
            if ((++count) == which) return m;
        }
    }
    
    public static jq_Method getMethod() throws IOException {
        return getMethod((String[])null, 0);
    }
    
    public static jq_Method getMethod(String[] args, int start) throws IOException {
        String mainClassName;
        if (args != null && args.length > start) {
            mainClassName = args[start];
        } else {
            System.out.print("Enter the name of the class: ");
            mainClassName = in.readLine();
        }
        jq_Type t = jq_Type.parseType(mainClassName);
        if (!(t instanceof jq_Class)) {
            System.out.println("Error, "+mainClassName+" ("+t+") is not a valid class.");
            System.exit(-1);
        }
        
        jq_Class klass = (jq_Class)t;
        klass.prepare();
        String name = (args != null && args.length > start+1) ? args[start+1] : null;
        return getMethod(klass, name);
    }
    
    public static jq_Method getMethod(jq_Class klass, String name) throws IOException {
        jq_Method method;
        if (name != null) {
            String methodName = name;
            boolean static_or_instance = false;
uphere1:
            for (;;) {
                jq_Method[] m = static_or_instance?(jq_Method[])klass.getDeclaredStaticMethods():(jq_Method[])klass.getDeclaredInstanceMethods();
                for (int i=0; i<m.length; ++i) {
                    if (methodName.equals(m[i].getName().toString())) {
                        method = m[i];
                        break uphere1;
                    }
                }
                if (static_or_instance) {
                    System.out.println("Error, no method named "+methodName+" is declared in class "+klass.getName());
                    System.exit(-1);
                }
                static_or_instance = true;
            }
        } else {
            boolean static_or_instance = true;
uphere2:
            for (;;) {
                System.out.println((static_or_instance?"Static":"Instance")+" methods:");
                jq_Method[] m = static_or_instance?(jq_Method[])klass.getDeclaredStaticMethods():(jq_Method[])klass.getDeclaredInstanceMethods();
                for (int i=0; i<m.length; ++i) {
                    System.out.println((i+1)+": "+m[i]);
                }
                int which;
                for (;;) {
                    System.out.print("Which method, or "+(static_or_instance?"'i' for instance":"'s' for static")+" methods: ");
                    String s = in.readLine();
                    try {
                        if (s.equalsIgnoreCase("s")) {
                            static_or_instance = true;
                            continue uphere2;
                        }
                        if (s.equalsIgnoreCase("i")) {
                            static_or_instance = false;
                            continue uphere2;
                        }
                        which = Integer.parseInt(s);
                        if ((which >= 1) && (which <= m.length))
                            break;
                        System.out.println("Out of range: "+which);
                    } catch (NumberFormatException x) {
                        System.out.println("Not a number: "+s);
                    }
                }
                method = m[which-1];
                break;
            }
        }
        return method;
    }
    
    public static SortedSet sortByNumberOfTargets(Map callGraph) {
        TreeSet ts = new TreeSet(
            new Comparator() {
                public int compare(Object o1, Object o2) {
                    Map.Entry e1 = (Map.Entry)o1;
                    CallSite cs1 = (CallSite)e1.getKey();
                    Set s1 = (Set)e1.getValue();
                    Map.Entry e2 = (Map.Entry)o2;
                    CallSite cs2 = (CallSite)e2.getKey();
                    Set s2 = (Set)e2.getValue();
                    int s1s = s1.size(); int s2s = s2.size();
                    if (s1s < s2s) return 1;
                    else if (s1s > s2s) return -1;
                    else return cs1.toString().compareTo(cs2.toString());
                }
            });
        ts.addAll(callGraph.entrySet());
        return ts;
    }
    
    public static AndersenPointerAnalysis apa;
    public static Map callGraph;
    public static Set rootSet = new LinkedHashSet();
    public static Set selectedCallSites = new LinkedHashSet();
    public static Map methodToCallSites = new HashMap();
    public static Map toInline = new LinkedHashMap();
    public static List inlineCommands = new LinkedList();
    
    public static void selectCallSites(String desc, Iterator i, Iterator i2) throws IOException {
        System.out.println("Call sites with "+desc+": ");
        int count = 0;
        while (i2.hasNext()) {
            Map.Entry e = (Map.Entry)i2.next();
            Set s = (Set)e.getValue();
            System.out.println((++count)+": "+e.getKey()+"="+s.size()+" targets");
        }
        int which;
        for (;;) {
            System.out.print("Enter your selection, or 'a' for all: ");
            String input = in.readLine();
            if (input.equalsIgnoreCase("a")) {
                which = -1;
                break;
            } else if (input.equalsIgnoreCase("q")) {
                which = -2;
                break;
            } else {
                try {
                    which = Integer.parseInt(input);
                    if ((which >= 1) && (which <= count))
                        break;
                } catch (NumberFormatException x) {
                    System.out.println("Cannot parse number: "+input);
                }
            }
        }
        for (int j=0; j<count; ++j) {
            Map.Entry e = (Map.Entry)i.next();
            if (which == j+1 || which == -1)
                selectedCallSites.add(e.getKey());
            if (which == j+1) {
                System.out.println("Selected "+e);
            }
        }
    }
    
    static void printAllInclusionEdges(HashSet visited, MethodSummary.Node pnode, MethodSummary.Node node, String indent, boolean all, jq_Field f, boolean verbose) throws IOException {
        if (verbose) System.out.print(indent+"Node: "+node);
        if (pnode != null) {
            Object q = apa.edgesToReasons.get(new Pair(pnode, node));
            if (q != null)
                if (verbose) System.out.print(" from "+q);
        }
        if (visited.contains(node)) {
            if (verbose) System.out.println(" <duplicate>, skipping.");
            return;
        }
        visited.add(node);
        if (verbose) System.out.println();
        if (node instanceof MethodSummary.OutsideNode) {
            MethodSummary.OutsideNode onode = (MethodSummary.OutsideNode)node;
            while (onode.skip != null) {
                if (verbose) System.out.println(indent+onode+" equivalent to "+onode.skip);
                onode = onode.skip;
            }
            if (onode instanceof MethodSummary.FieldNode) {
                MethodSummary.FieldNode fnode = (MethodSummary.FieldNode)onode;
                jq_Field field = fnode.getField();
                Set inEdges = fnode.getAccessPathPredecessors();
                System.out.println(indent+"Field "+field.getName().toString()+" Parent nodes: "+inEdges);
                System.out.print(indent+"Type 'w' to find matching writes to parent nodes, 'u' to go up: ");
                String s = in.readLine();
                if (s != null) {
                    if (s.equalsIgnoreCase("u")) {
                        for (Iterator it3 = inEdges.iterator(); it3.hasNext(); ) {
                            MethodSummary.Node node4 = (MethodSummary.Node)it3.next();
                            printAllInclusionEdges(visited, null, node4, indent+"<", all, field, true);
                        }
                    } else if (s.equalsIgnoreCase("w")) {
                        for (Iterator it3 = inEdges.iterator(); it3.hasNext(); ) {
                            MethodSummary.Node node4 = (MethodSummary.Node)it3.next();
                            printAllInclusionEdges(visited, null, node4, indent+"<", all, field, false);
                        }
                    }
                }
            }
            Set outEdges = (Set)apa.nodeToInclusionEdges.get(onode);
            if (outEdges != null) {
                boolean yes = all || !verbose;
                if (!yes) {
                    System.out.print(indent+outEdges.size()+" out edges, print them? ('y' for yes, 'a' for all) ");
                    String s = in.readLine();
                    if (s.equalsIgnoreCase("y")) yes = true;
                    else if (s.equalsIgnoreCase("a")) all = yes = true;
                }
                if (yes) {
                    for (Iterator it3 = outEdges.iterator(); it3.hasNext(); ) {
                        MethodSummary.Node node2 = (MethodSummary.Node)it3.next();
                        printAllInclusionEdges(visited, onode, node2, indent+" ", all, null, verbose);
                    }
                }
            }
        } else {
            Set s = node.getNonEscapingEdges(f);
            if (s.size() > 0) {
                System.out.println(indent+s.size()+" write edges match field "+((f==null)?"[]":f.getName().toString()));
                for (Iterator i=s.iterator(); i.hasNext(); ) {
                    MethodSummary.Node node2 = (MethodSummary.Node)i.next();
                    //Quad quad = node.getSourceQuad(f, node2);
                    //if (quad != null)
                    //    System.out.println(indent+"From instruction: "+quad);
                    printAllInclusionEdges(visited, null, node2, indent+">", all, null, verbose);
                }
            }
        }
    }
    
    public static class InlineSet extends java.util.AbstractSet {
        final Set backing_set;
        boolean is_complete;
        
        public InlineSet(Set s, boolean c) {
            this.backing_set = s;
            this.is_complete = c;
        }
        
        public boolean isComplete() { return is_complete; }
        
        public Iterator iterator() { return backing_set.iterator(); }
        public int size() { return backing_set.size(); }
        public boolean containsAll(Collection arg0) {
            return backing_set.containsAll(arg0);
        }

    }
    
    public static void recalculateInliningCompleteness() {
        int total = 0;
        for (Iterator it = toInline.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry e = (Map.Entry) it.next();
            CallSite cs = (CallSite) e.getKey();
            InlineSet is = (InlineSet) e.getValue();
            
            Set targets = (Set) callGraph.get(cs);
            if (targets != null && is.containsAll(targets)) {
                total++;
                is.is_complete = true;
            }
        }
        System.out.println(total+" inlining sites are complete.");
    }
    
    public static void doInlining(Set inline) {
        for (Iterator it = inline.iterator(); it.hasNext(); ) {
            Map.Entry e = (Map.Entry)it.next();
            CallSite cs = (CallSite)e.getKey();
            MethodSummary caller = MethodSummary.getSummary(CodeCache.getCode((jq_Method)cs.getCaller().getMethod()));
            ProgramLocation mc = cs.getLocation();
            cs = new CallSite(caller, mc);
            InlineSet targets = (InlineSet)e.getValue();
            Iterator it2 = targets.iterator();
            if (!it2.hasNext()) {
                System.out.println("No targets to inline for "+cs);
            } else {
                for (;;) {
                    jq_Method target_m = (jq_Method)it2.next();
                    boolean removeCall = !it2.hasNext() && targets.isComplete();
                    if (target_m.getBytecode() == null) {
                        //System.out.println("Cannot inline target "+target_m+": target has no bytecode");
                    } else {
                        MethodSummary callee = MethodSummary.getSummary(CodeCache.getCode(target_m));
                        MethodSummary caller2 = caller.copy();
                        MethodSummary callee2 = callee.copy();
                        if (caller == callee || callee.getCalls().contains(mc)) {
                            System.out.println("Inlining of recursive call not supported yet: "+cs);
                        } else if (!caller.getCalls().contains(mc)) {
                            System.out.println("Error: cannot find call site "+cs);
                        } else {
                            try {
                                MethodSummary.instantiate(caller, mc, callee, removeCall);
                            } catch (Throwable t) {
                                System.err.println("EXCEPTION while instantiating "+callee+" into "+caller+" mc="+mc);
                                t.printStackTrace();
                                MethodSummary.TRACE_INST = true;
                                MethodSummary.TRACE_INTRA = true;
                                MethodSummary.TRACE_INTER = true;
                                MethodSummary.instantiate(caller2, mc, callee2, removeCall);
                            }
                        }
                    }
                    if (!it2.hasNext()) break;
                }
            }
        }
    }
    
    static int setDepth(LinkedHashSet path, HashMap visited, jq_Method m) {
        if (path.contains(m)) {
            System.out.println("Attempting to inline recursive cycle: method "+m);
            return -1;
        }
        Integer result = (Integer)visited.get(m);
        if (result != null) return result.intValue();
        path.add(m);
        HashSet s = (HashSet)methodToCallSites.get(m);
        int current = 0;
        if (s != null) {
uphere:
            for (Iterator i=s.iterator(); i.hasNext(); ) {
                CallSite cs = (CallSite)i.next();
                InlineSet t = (InlineSet)toInline.get(cs);
                for (Iterator j=t.iterator(); j.hasNext(); ) {
                    jq_Method m2 = (jq_Method)j.next();
                    int r = setDepth(path, visited, m2);
                    if (r == -1) {
                        System.out.println("Removing call site "+cs+" from inline set");
                        i.remove();
                        toInline.remove(cs);
                        continue uphere;
                    }
                    current = Math.max(current, r+1);
                }
            }
        }
        visited.put(m, result = new Integer(current));
        path.remove(m);
        return current;
    }
    
    public static Set[] reorderInlineSites(Map toInline) {
        if (toInline.isEmpty()) return new Set[0];
        
        System.out.println("Reordering call sites to inline...");
        
        // build a multimap from a method to the inlined call sites it contains.
        methodToCallSites.clear();
        for (Iterator i=toInline.keySet().iterator(); i.hasNext(); ) {
            CallSite cs = (CallSite)i.next();
            HashSet s = (HashSet)methodToCallSites.get(cs.getCaller().getMethod());
            if (s == null) {
                methodToCallSites.put(cs.getCaller().getMethod(), s = new HashSet());
            }
            s.add(cs);
        }
        
        System.out.println(methodToCallSites.size()+" methods contain sites to inline");
        
        HashMap depths = new HashMap();
        LinkedHashSet path = new LinkedHashSet();
        int maxDepth = 0;
        for (Iterator j=methodToCallSites.keySet().iterator(); j.hasNext(); ) {
            jq_Method m = (jq_Method)j.next();
            if (depths.containsKey(m)) continue;
            int depth = setDepth(path, depths, m);
            //System.out.println("Method "+m+": depth "+(depth+1));
            maxDepth = Math.max(maxDepth, depth+1);
        }
        System.out.println("Longest inlining chain: "+maxDepth);
        Set[] result = new Set[maxDepth];
        for (int i=0; i<maxDepth; ++i) {
            result[i] = new LinkedHashSet();
        }
        for (Iterator i=depths.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry e = (Map.Entry)i.next();
            jq_Method m = (jq_Method)e.getKey();
            Integer j = (Integer)e.getValue();
            Set s = (Set)methodToCallSites.get(m);
            if (s != null) {
                for (Iterator k = s.iterator(); k.hasNext(); ) {
                    final CallSite cs = (CallSite)k.next();
                    final InlineSet targets = (InlineSet)toInline.get(cs);
                    result[j.intValue()].add(new Map.Entry() {
                        public Object getKey() { return cs; }
                        public Object getValue() { return targets; }
                        public Object setValue(Object x) { throw new UnsupportedOperationException(); }
                    });
                }
            }
        }
        return result;
    }
    
    public static void doInlining() {
        if (!toInline.isEmpty()) {
            inlineCommands.add(toInline);
        }
        for (Iterator ii=inlineCommands.iterator(); ii.hasNext(); ) {
            toInline = (LinkedHashMap) ii.next();
            System.out.println("Inlining "+toInline.size()+" call sites.");
            Set[] sitesToInline = reorderInlineSites(toInline);
            for (int i=0; i<sitesToInline.length; ++i) {
                doInlining(sitesToInline[i]);
            }
        }
        toInline = new LinkedHashMap();
    }
    
    static int setDepth_clone(HashMap methodToSpecializations,
                              HashMap to_clone,
                              LinkedHashSet path,
                              HashMap visited,
                              jq_Method m) {
        if (path.contains(m)) {
            System.out.println("Attempting to clone recursive cycle: method "+m);
            return -1;
        }
        Integer result = (Integer) visited.get(m);
        if (result != null) return result.intValue();
        path.add(m);
        Set s = (Set) methodToSpecializations.get(m);
        int current = 0;
        if (s != null) {
uphere:
            for (Iterator i=s.iterator(); i.hasNext(); ) {
                Specialization s2 = (Specialization) i.next();
                Assert._assert(s2.target.getMethod() == m);
                Set s3 = (Set) to_clone.get(s2);
                for (Iterator j=s3.iterator(); j.hasNext(); ) {
                    ProgramLocation mc = (ProgramLocation) j.next();
                    jq_Method source_m = mc.getMethod();
                    int r = setDepth_clone(methodToSpecializations, to_clone, path, visited, source_m);
                    if (r == -1) {
                        //System.out.println("Removing edge "+source_m.getName()+"->"+m.getName()+" from clone set");
                        //j.remove();
                        continue;
                    }
                    current = Math.max(current, r+1);
                }
                if (s3.isEmpty()) {
                    System.out.println("Removed all specializations for method "+m);
                    i.remove();
                }
            }
        }
        visited.put(m, result = new Integer(current));
        path.remove(m);
        return current;
    }
    
    public static class Specialization {
        ControlFlowGraph target;
        Set/*<SpecializationParameter>*/ set;
        Specialization(ControlFlowGraph t, SpecializationParameter s) {
            this.target = t; this.set = new LinearSet(); this.set.add(s);
        }
        Specialization(ControlFlowGraph t, Set s) {
            this.target = t; this.set = s;
        }
        public boolean equals(Object o) {
            return equals((Specialization) o);
        }
        public boolean equals(Specialization that) {
            if (this.target != that.target) {
                return false;
            }
            if (!this.set.equals(that.set)) {
                return false;
            }
            return true;
        }
        public int hashCode() { return target.hashCode() ^ set.hashCode(); }
        
        public String toString() {
            return "Specialization of "+target.getMethod()+" on "+set;
        }
    }
    
    public static class SpecializationParameter {
        int paramNum;
        AccessPath ap;
        Set types;
        SpecializationParameter(int paramNum, AccessPath ap, Set types) {
            this.paramNum = paramNum; this.ap = ap; this.types = types;
        }
        public boolean equals(Object o) {
            return equals((SpecializationParameter) o);
        }
        public boolean equals(SpecializationParameter that) {
            if (this.paramNum != that.paramNum || !this.types.equals(that.types)) return false;
            if (this.ap == that.ap) return true;
            if (this.ap == null || that.ap == null) return false;
            return this.ap.equals(that.ap);
        }
        public int hashCode() {
            int aphash = ap==null?0:ap.hashCode();
            return paramNum ^ types.hashCode() ^ aphash;
        }
        public String toString() {
            if (ap == null)
                return "Param#"+paramNum+" types: "+types;
            return "Param#"+paramNum+ap.toString()+" types: "+types;
        }
    }
    
    public static void buildCloneCache(HashMap/*<Specialization,Set<ProgramLocation>>*/ to_clone) {
        System.out.println(to_clone.size()+" specializations");
        HashMap methodToSpecializations = new HashMap();
        for (Iterator i = to_clone.keySet().iterator(); i.hasNext(); ) {
            Specialization s = (Specialization) i.next();
            jq_Method target_m = s.target.getMethod();
            Set s2 = (Set) methodToSpecializations.get(target_m);
            if (s2 == null) methodToSpecializations.put(target_m, s2 = new LinkedHashSet());
            boolean change = s2.add(s);
            Assert._assert(change, s.toString());
        }
        System.out.println(methodToSpecializations.size()+" different methods are to be specialized");
        
        LinkedHashSet path = new LinkedHashSet();
        HashMap visited = new HashMap();
        int maxdepth = 0;
        for (Iterator i = methodToSpecializations.keySet().iterator(); i.hasNext(); ) {
            jq_Method m = (jq_Method) i.next();
            int depth = setDepth_clone(methodToSpecializations, to_clone, path, visited, m);
            maxdepth = Math.max(maxdepth, depth);
        }
        
        System.out.println("Max cloning depth: "+maxdepth);
        Collection[] cloneme = new Collection[maxdepth+1];
        for (int i=0; i<cloneme.length; ++i) {
            cloneme[i] = new LinkedList();
        }
        for (Iterator i = visited.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry e = (Map.Entry)i.next();
            jq_Method m = (jq_Method) e.getKey();
            Integer ii = (Integer) e.getValue();
            cloneme[ii.intValue()].add(m);
        }
        
        HashMap specialToMS = new HashMap();
        for (int i=0; i < cloneme.length; ++i) {
            Collection c = cloneme[i];
            //System.out.println("Depth "+i+": "+c);
            for (Iterator j=c.iterator(); j.hasNext(); ) {
                jq_Method m = (jq_Method) j.next();
                Set s2 = (Set) methodToSpecializations.get(m);
                if (s2 == null) continue;
                ControlFlowGraph cfg = CodeCache.getCode(m);
                for (Iterator k=s2.iterator(); k.hasNext(); ) {
                    Specialization special = (Specialization) k.next();
                    MethodSummary ms = MethodSummary.getSummary(cfg).copy();
                    Assert._assert(specialToMS.get(special) == null);
                    specialToMS.put(special, ms);
                }
            }
        }
        int totalCallSites = 0;
        for (Iterator i=specialToMS.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry e = (Map.Entry)i.next();
            Specialization s = (Specialization) e.getKey();
            MethodSummary target_ms = (MethodSummary) e.getValue();
            Set s2 = (Set) to_clone.get(s);
            totalCallSites += s2.size();
            //System.out.println("Method summary "+target_ms.getMethod()+" has "+s2+" to specialize.");
            for (Iterator j=s2.iterator(); j.hasNext(); ) {
                ProgramLocation mc = (ProgramLocation) j.next();
                jq_Method source_m = mc.getMethod();
                Set s3 = (Set) methodToSpecializations.get(source_m);
                if (s3 != null) {
                    for (Iterator k=s3.iterator(); k.hasNext(); ) {
                        Specialization s4 = (Specialization) k.next();
                        MethodSummary source_ms = (MethodSummary) specialToMS.get(s4);
                        CallSite cs = new CallSite(source_ms, mc);
                        ControlFlowGraph target_cfg = CodeCache.getCode((jq_Method)target_ms.getMethod());
                        MethodSummary.clone_cache.put(new Pair(target_cfg, cs), target_ms);
                        //System.out.println(source_m+" is also specialized, adding special edges to "+target_ms.getMethod());
                    }
                }
                ControlFlowGraph target_cfg = CodeCache.getCode((jq_Method)target_ms.getMethod());
                ControlFlowGraph source_cfg = CodeCache.getCode((jq_Method)source_m);
                MethodSummary source_ms = MethodSummary.getSummary(source_cfg);
                CallSite cs = new CallSite(source_ms, mc);
                MethodSummary.clone_cache.put(new Pair(target_cfg, cs), target_ms);
            }
        }
        System.out.println("Specializing a total of "+totalCallSites+" call sites.");
    }
    
    public static void main(String[] args) throws IOException {
        HostedVM.initialize();
        
        int index = 0; int index0 = -1;
        while (index != index0 && index < args.length) {
            index = joeq.Main.TraceFlags.setTraceFlag(args, index0 = index);
        }
        
        System.out.println("Select the root method.");
        jq_Method m = getMethod(args, index);
        rootSet.add(m);
        ControlFlowGraph cfg = CodeCache.getCode(m);
        apa = new AndersenPointerAnalysis(false);
        apa.addToRootSet(cfg);
        System.out.println("Performing initial context-insensitive analysis...");
        long time = System.currentTimeMillis();
        apa.iterate();
        time = System.currentTimeMillis() - time;
        System.out.println("Time to complete: "+time);
        callGraph = apa.getCallGraph();
        SortedSet sorted = sortByNumberOfTargets(callGraph);
        
        for (;;) {
            System.out.print("Enter command: ");
            String s = in.readLine();
            if (s == null) {
                System.out.println("Exiting.");
                System.exit(0);
            }
            if (s.startsWith("histogram")) {
                //System.out.println("Cloned call graph:");
                //System.out.println(AndersenPointerAnalysis.computeHistogram(callGraph));
                //System.out.println("Original call graph:");
                //System.out.println(AndersenPointerAnalysis.computeHistogram2(callGraph));
                Map original = AndersenPointerAnalysis.buildOriginalCallGraph(callGraph);
                System.out.println("Comparison:");
                System.out.println(AndersenPointerAnalysis.compareWithOriginal(callGraph, original));
                continue;
            }
            if (s.startsWith("stats")) {
                System.out.println(apa.computeStats());
                continue;
            }
            if (s.startsWith("callgraph")) {
                System.out.println(apa.getCallGraph());
                continue;
            }
            if (s.startsWith("addroot")) {
                m = getMethod();
                rootSet.add(m);
                //cfg = CodeCache.getCode(m);
                //apa.addToRootSet(cfg);
                continue;
            }
            if (s.startsWith("trace summary ")) {
                boolean b = s.substring(14).equals("on");
                MethodSummary.TRACE_INTRA = b;
                System.out.println("Trace summary: "+b);
                continue;
            }
            if (s.startsWith("trace inline ")) {
                boolean b = s.substring(13).equals("on");
                MethodSummary.TRACE_INTER = b;
                MethodSummary.TRACE_INST = b;
                System.out.println("Trace inline: "+b);
                continue;
            }
            if (s.startsWith("trace andersen ")) {
                boolean b = s.substring(15).equals("on");
                AndersenPointerAnalysis.TRACE = b;
                System.out.println("Trace Andersen: "+b);
                continue;
            }
            if (s.startsWith("inline")) {
                System.out.println("Marking "+selectedCallSites.size()+" call sites for inlining.");
                int size=0;
                for (Iterator it = selectedCallSites.iterator(); it.hasNext(); ) {
                    CallSite cs = (CallSite)it.next();
                    Set set = (Set)callGraph.get(cs);
                    if (set == null) {
                        System.out.println("Error: call site "+cs+" not found in call graph.");
                    } else {
                        InlineSet is = new InlineSet(set, true);
                        toInline.put(cs, is);
                        size += set.size();
                    }
                }
                System.out.println("Total number of target methods: "+size);
                continue;
            }
            if (s.startsWith("run")) {
                MethodSummary.clone_cache = null;
                MethodSummary.clearSummaryCache();
                doInlining();
                selectedCallSites.clear();
                System.gc();
                apa = new AndersenPointerAnalysis(false);
                for (Iterator it = rootSet.iterator(); it.hasNext(); ) {
                    m = (jq_Method)it.next();
                    cfg = CodeCache.getCode(m);
                    apa.addToRootSet(cfg);
                }
                System.out.println("Re-running context-insensitive analysis...");
                time = System.currentTimeMillis();
                try {
                    apa.iterate();
                } catch (Throwable t) {
                    System.err.println("EXCEPTION while iterating: "+t);
                    t.printStackTrace();
                }
                time = System.currentTimeMillis() - time;
                System.out.println("Time to complete: "+time);
                callGraph = apa.getCallGraph();
                sorted = sortByNumberOfTargets(callGraph);
                continue;
            }
            if (s.startsWith("source")) {
                final jq_Method m2 = getMethod();
                Filter f = new Filter() {
                        public boolean isElement(Object o) {
                            Map.Entry e = (Map.Entry)o;
                            CallSite cs = (CallSite)e.getKey();
                            return (cs.getCaller().getMethod() == m2);
                        }
                };
                FilterIterator it1 = new FilterIterator(sorted.iterator(), f);
                FilterIterator it2 = new FilterIterator(sorted.iterator(), f);
                selectCallSites("caller="+m, it1, it2);
                continue;
            }
            if (s.startsWith("targets")) {
                m = getMethod();
                int total = 0;
                for (Iterator it = callGraph.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry e = (Map.Entry)it.next();
                    Set set = (Set)e.getValue();
                    if (set.contains(m)) {
                        selectedCallSites.add(e.getKey());
                        ++total;
                    }
                }
                System.out.println("Selected "+total+" call sites.");
                continue;
            }
            if (s.startsWith("basepointers")) {
                for (Iterator it = selectedCallSites.iterator(); it.hasNext(); ) {
                    CallSite cs = (CallSite)it.next();
                    System.out.println("For call site: "+cs);
                    MethodSummary ms = cs.getCaller();
                    LinkedHashSet set = new LinkedHashSet();
                    PassedParameter pp = new PassedParameter(cs.getLocation(), 0);
                    ms.getNodesThatCall(pp, set);
                    for (Iterator it2 = set.iterator(); it2.hasNext(); ) {
                        MethodSummary.Node node = (MethodSummary.Node)it2.next();
                        printAllInclusionEdges(new HashSet(), null, node, "", false, null, true);
                    }
                }
                continue;
            }
            if (s.startsWith("clearselection")) {
                selectedCallSites.clear();
                continue;
            }
            if (s.startsWith("printselection")) {
                System.out.println(selectedCallSites);
                continue;
            }
            if (s.startsWith("summary")) {
                m = getMethod();
                MethodSummary ms = MethodSummary.getSummary(CodeCache.getCode(m));
                System.out.println(ms);
                continue;
            }
            if (s.startsWith("printsize ")) {
                try {
                    final int size = Integer.parseInt(s.substring(10));
                    Filter f = new Filter() {
                            public boolean isElement(Object o) {
                                Map.Entry e = (Map.Entry)o;
                                Set set = (Set)e.getValue();
                                return set.size() >= size;
                            }
                    };
                    FilterIterator it1 = new FilterIterator(sorted.iterator(), f);
                    while (it1.hasNext()) {
                        System.out.println(it1.next());
                    }
                } catch (NumberFormatException x) {
                    System.out.println("Invalid size: "+s.substring(5));
                }
                continue;
            }
            if (s.startsWith("selectmultitarget")) {
                Filter f = new Filter() {
                        public boolean isElement(Object o) {
                            Map.Entry e = (Map.Entry)o;
                            Set set = (Set)e.getValue();
                            return set.size() > 1;
                        }
                };
                FilterIterator it1 = new FilterIterator(sorted.iterator(), f);
                while (it1.hasNext()) {
                    Map.Entry e = (Map.Entry) it1.next();
                    selectedCallSites.add(e.getKey());
                }
                System.out.println(selectedCallSites.size()+" call sites selected");
                continue;
            }
            if (s.startsWith("size ")) {
                try {
                    final int size = Integer.parseInt(s.substring(5));
                    Filter f = new Filter() {
                            public boolean isElement(Object o) {
                                Map.Entry e = (Map.Entry)o;
                                Set set = (Set)e.getValue();
                                return set.size() == size;
                            }
                    };
                    FilterIterator it1 = new FilterIterator(sorted.iterator(), f);
                    FilterIterator it2 = new FilterIterator(sorted.iterator(), f);
                    selectCallSites("size="+size, it1, it2);
                } catch (NumberFormatException x) {
                    System.out.println("Invalid size: "+s.substring(5));
                }
                continue;
            }
            /*
            if (s.startsWith("selectiveinlining")) {
                if (selectedCallSites.size() == 0) {
                    System.out.println("Selecting multi-target methods...");
                    FilterIterator.Filter f = new FilterIterator.Filter() {
                            public boolean isElement(Object o) {
                                Map.Entry e = (Map.Entry)o;
                                Set set = (Set)e.getValue();
                                return set.size() > 1;
                            }
                    };
                    FilterIterator it1 = new FilterIterator(sorted.iterator(), f);
                    while (it1.hasNext()) {
                        Map.Entry e = (Map.Entry) it1.next();
                        selectedCallSites.add(e.getKey());
                    }
                    System.out.println(selectedCallSites.size()+" call sites selected");
                }
                SelectiveCloning.pa = apa;
                time = System.currentTimeMillis();
                SelectiveCloning.searchForCloningOpportunities(toInline, selectedCallSites);
                time = System.currentTimeMillis() - time;
                System.out.println("Time to complete: "+time);
                System.out.println(toInline.size()+" inlining candidates found");
                recalculateInliningCompleteness();
                
                for (Iterator it = selectedCallSites.iterator(); it.hasNext(); ) {
                    CallSite cs = (CallSite)it.next();
                    System.out.println("For call site: "+cs);
                    Set targets = (Set) callGraph.get(cs);
                    MethodSummary ms = cs.caller;
                    PassedParameter pp = new PassedParameter(cs.m, 0);
                    LinkedHashSet set = new LinkedHashSet();
                    ms.getNodesThatCall(pp, set);
                    for (Iterator it2 = set.iterator(); it2.hasNext(); ) {
                        MethodSummary.Node node = (MethodSummary.Node)it2.next();
                        if (node instanceof MethodSummary.OutsideNode) {
                            SelectiveCloning.pa = apa;
                            SelectiveCloning.searchForCloningOpportunities(toInline, (MethodSummary.OutsideNode) node, cs.m);
                        }
                    }
                }
                System.out.println(toInline.size()+" inlining candidates found");
                
                continue;
            }
            if (s.startsWith("selectivecloning")) {
                if (selectedCallSites.size() == 0) {
                    System.out.println("Selecting multi-target methods...");
                    FilterIterator.Filter f = new FilterIterator.Filter() {
                            public boolean isElement(Object o) {
                                Map.Entry e = (Map.Entry)o;
                                Set set = (Set)e.getValue();
                                return set.size() > 1;
                            }
                    };
                    FilterIterator it1 = new FilterIterator(sorted.iterator(), f);
                    while (it1.hasNext()) {
                        Map.Entry e = (Map.Entry) it1.next();
                        selectedCallSites.add(e.getKey());
                    }
                    System.out.println(selectedCallSites.size()+" call sites selected");
                }
                
                SelectiveCloning.pa = apa;
                time = System.currentTimeMillis();
                SelectiveCloning.searchForCloningOpportunities3(selectedCallSites);
                time = System.currentTimeMillis() - time;
                System.out.println("Time to complete: "+time);
                MethodSummary.clearSummaryCache();
                System.gc();
                MethodSummary.clone_cache = new HashMap();
                buildCloneCache(SelectiveCloning.to_clone);
                selectedCallSites.clear();
                System.gc();
                System.out.println("Number of cloned summaries: "+MethodSummary.clone_cache.size());
                apa = new AndersenPointerAnalysis(false);
                for (Iterator it = rootSet.iterator(); it.hasNext(); ) {
                    m = (jq_Method)it.next();
                    cfg = CodeCache.getCode(m);
                    apa.addToRootSet(cfg);
                }
                System.out.println("Re-running context-insensitive analysis...");
                time = System.currentTimeMillis();
                try {
                    apa.iterate();
                } catch (Throwable t) {
                    System.err.println("EXCEPTION while iterating: "+t);
                    t.printStackTrace();
                }
                time = System.currentTimeMillis() - time;
                System.out.println("Time to complete: "+time);
                callGraph = apa.getCallGraph();
                sorted = sortByNumberOfTargets(callGraph);
                continue;
            }
            */
            if (s.startsWith("exit") || s.startsWith("quit")) {
                System.exit(0);
            }
            System.out.println("Unknown command: "+s);
        }
        
    }
    
}
