// CallGraph.java, created Mon Mar  3 18:01:32 2003 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.Quad;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import joeq.Class.jq_Method;
import joeq.Compiler.Analysis.IPA.ProgramLocation;
import joeq.Compiler.Quad.Operator.Invoke;
import jwutil.collections.GenericInvertibleMultiMap;
import jwutil.collections.HashWorklist;
import jwutil.collections.InvertibleMultiMap;
import jwutil.collections.MultiMap;
import jwutil.collections.UnmodifiableIterator;
import jwutil.collections.UnmodifiableMultiMap;
import jwutil.graphs.Graph;
import jwutil.graphs.Navigator;
import jwutil.strings.Strings;
import jwutil.util.Assert;

/**
 * Abstract representation of a call graph.
 * 
 * @author John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: CallGraph.java,v 1.16 2005/03/21 05:01:17 joewhaley Exp $
 */
public abstract class CallGraph extends UnmodifiableMultiMap implements Graph {
    
    /**
     * Sets up the root methods to be the given set.  Later call graph queries
     * use the value that you pass in here.  Implementing this method is
     * optional -- it is only necessary if you use methods that require a root
     * set, like getReachableMethods().
     * 
     * @param roots collection of root methods
     */
    public abstract void setRoots(Collection/*<jq_Method>*/ roots);
    
    /** Returns the collection of root methods for this call graph. */
    public abstract Collection/*<jq_Method>*/ getRoots();
    
    /**
     * Returns the collection of all methods in the call graph.
     * The default implementation recalculates the reachable
     * methods based on the root set.
     * 
     * @return Collection of all call sites in the call graph
     */
    public Collection/*<jq_Method>*/ getAllMethods() {
        return calculateReachableMethods(getRoots());
    }
    
    /**
     * Returns the collection of all call sites in the call graph.
     * The default implementation just iterates through all of the methods
     * to build up the collection.
     * 
     * @return Collection of all call sites in the call graph
     */
    public Collection/*<ProgramLocation>*/ getAllCallSites() {
        LinkedList list = new LinkedList();
        for (Iterator i = getAllMethods().iterator(); i.hasNext(); ) {
            jq_Method m = (jq_Method) i.next();
            if (m == null) continue;
            list.addAll(getCallSites(m));
        }
        return list;
    }
    
    /**
     * Returns the possible target methods of the given call site under the given context.
     * The interpretation of the context object is specific to the type of call graph.
     * 
     * @param context
     * @param callSite
     * @return Collection of jq_Methods that are the possible targets
     */
    public abstract Collection/*<jq_Method>*/ getTargetMethods(Object context, ProgramLocation callSite);
    
    /**
     * Returns the possible target methods of the given call site.
     * 
     * @param callSite
     * @return Collection of jq_Methods that are the possible targets
     */
    public Collection/*<jq_Method>*/ getTargetMethods(ProgramLocation callSite) {
        return getTargetMethods(null, callSite);
    }
    
    /**
     * Returns the number of possible target methods of the given call site under
     * the given context. The interpretation of the context object is specific to
     * the type of call graph.
     * 
     * @param context
     * @param callSite
     * @return number of possible targets
     */
    public int numberOfTargetMethods(Object context, ProgramLocation callSite) {
        return getTargetMethods(context, callSite).size();
    }
    
    /**
     * Returns the number of possible target methods of the given call site.
     * 
     * @param callSite
     * @return number of possible targets
     */
    public int numberOfTargetMethods(ProgramLocation callSite) {
        return numberOfTargetMethods(null, callSite);
    }

    /**
     * Returns the target method of the given call site under the given context, assuming
     * that it is a single target.
     * The interpretation of the context object is specific to the type of call graph.
     * 
     * @param context
     * @param callSite
     * @return target method
     */
    public jq_Method getTargetMethod(Object context, ProgramLocation callSite) {
        Collection c = getTargetMethods(context, callSite);
        Assert._assert(c.size() == 1);
        return (jq_Method) c.iterator().next();
    }
    
    /**
     * Returns the set of call sites in the given method.
     * 
     * @param caller
     * @return set of call sites
     */
    public Collection/*<ProgramLocation>*/ getCallSites(jq_Method caller) {
        return getCallSites0(caller);
    }
    public static Collection/*<ProgramLocation>*/ getCallSites0(jq_Method caller) {
        caller.getDeclaringClass().load();
        if (caller.getBytecode() == null) return Collections.EMPTY_SET;
        ControlFlowGraph cfg = CodeCache.getCode(caller);
        return getCallSites0(cfg);
    }
    
    /**
     * Returns the set of call sites in the given CFG.
     * 
     * @param cfg
     * @return set of call sites
     */
    public Collection/*<ProgramLocation>*/ getCallSites(ControlFlowGraph cfg) {
        return getCallSites0(cfg);
    }
    public static Collection/*<ProgramLocation>*/ getCallSites0(ControlFlowGraph cfg) {
        LinkedList result = new LinkedList();
        for (QuadIterator i = new QuadIterator(cfg); i.hasNext(); ) {
            Quad q = i.nextQuad();
            if (q.getOperator() instanceof Invoke)
                result.add(new ProgramLocation.QuadProgramLocation(cfg.getMethod(), q));
        }
        return result;
    }
    public static Collection/*<ProgramLocation>*/ getCallSites1(ControlFlowGraph cfg) {
        int total = 0;
        for (QuadIterator i = new QuadIterator(cfg); i.hasNext(); ) {
            Quad q = i.nextQuad();
            if (q.getOperator() instanceof Invoke)
                ++total;
        }
        final int size = total;
        final QuadIterator i = new QuadIterator(cfg);
        return new AbstractSet() {
            public int size() { return size; }
            public Iterator iterator() {
                return new UnmodifiableIterator() {
                    Quad _next;
                    { advance(); }
                    private void advance() {
                        while (i.hasNext()) {
                            Quad q = i.nextQuad();
                            if (q.getOperator() instanceof Invoke) {
                                _next = q;
                                return;
                            }
                        }
                        _next = null;
                    }
                    public boolean hasNext() { return _next != null; }
                    public Object next() {
                        Quad n = _next;
                        if (n == null)
                            throw new java.util.NoSuchElementException();
                        advance();
                        return n;
                    }
                };
            }
        };
    }
    
    /**
     * Returns the set of methods that are called by the given method.
     * 
     * @param caller
     * @return set of callee methods
     */
    public Collection getCallees(jq_Method caller) {
        caller.getDeclaringClass().load();
        if (caller.getBytecode() == null) return Collections.EMPTY_SET;
        ControlFlowGraph cfg = CodeCache.getCode(caller);
        return getCallees(cfg);
    }
    
    /**
     * Returns the set of methods that are called by the given CFG.
     * 
     * @param cfg
     * @return set of callee methods
     */
    public Collection getCallees(ControlFlowGraph cfg) {
        LinkedHashSet result = new LinkedHashSet();
        for (QuadIterator i = new QuadIterator(cfg); i.hasNext(); ) {
            Quad q = i.nextQuad();
            if (q.getOperator() instanceof Invoke) {
                ProgramLocation p = new ProgramLocation.QuadProgramLocation(cfg.getMethod(), q);
                result.addAll(getTargetMethods(p));
            }
        }
        return result;
    }
    
    /**
     * Returns the set of call sites that can call the given method.
     * 
     * @param callee
     * @return set of callers
     */
    public Collection/*ProgramLocation*/ getCallers(jq_Method callee) {
        LinkedList result = new LinkedList();
        for (Iterator i = getAllCallSites().iterator(); i.hasNext(); ) {
            ProgramLocation p = (ProgramLocation) i.next();
            if (getTargetMethods(p).contains(callee))
                result.add(p);
        }
        return result;
    }
    
    /**
     * Returns the set of methods that can call the given method.
     * 
     * @param callee
     * @return set of caller methods
     */
    public Collection/*jq_Method*/ getCallerMethods(jq_Method callee) {
        LinkedList result = new LinkedList();
        for (Iterator i = getAllCallSites().iterator(); i.hasNext(); ) {
            ProgramLocation p = (ProgramLocation) i.next();
            if (getTargetMethods(p).contains(callee))
                result.add(p.getMethod());
        }
        return result;
    }
    
    /**
     * Returns the set of methods that are reachable from the given method root set.
     * 
     * @param roots
     * @return set of reachable methods
     */
    public Set/*jq_Method*/ calculateReachableMethods(Collection roots) {
        HashWorklist worklist = new HashWorklist(true);
        worklist.addAll(roots);
        while (!worklist.isEmpty()) {
            jq_Method m = (jq_Method) worklist.pull();
            Collection c = getCallees(m);
            worklist.addAll(c);
        }
        return worklist.getVisitedSet();
    }
    
    /**
     * Returns a string representation of this call graph.
     */
    public String toString() {
        TreeSet ts = new TreeSet(
            new Comparator() {
                public int compare(Object o1, Object o2) {
                    ProgramLocation cs1 = (ProgramLocation) o1;
                    Collection s1 = getTargetMethods(cs1);
                    ProgramLocation cs2 = (ProgramLocation) o2;
                    Collection s2 = getTargetMethods(cs2);
                    int s1s = s1.size(); int s2s = s2.size();
                    if (s1s < s2s) return 1;
                    else if (s1s > s2s) return -1;
                    else return cs1.toString().compareTo(cs2.toString());
                }
            });
        ts.addAll(getAllCallSites());
        StringBuffer sb = new StringBuffer();
        for (Iterator i=ts.iterator(); i.hasNext(); ) {
            ProgramLocation cs = (ProgramLocation) i.next();
            Collection s = (Collection) getTargetMethods(cs);
            sb.append(cs.toString());
            sb.append(": {");
            int x = s.size();
            sb.append(x);
            sb.append("} ");
            sb.append(s.toString());
            sb.append(Strings.lineSep);
        }
        return sb.toString();
    }
    
    /** Calculate a multimap between methods and their callers. */
    public Map calculateCallerRelation() {
        Collection roots = getRoots();
        Map backEdges = new HashMap();
        LinkedList worklist = new LinkedList();
        for (Iterator i=roots.iterator(); i.hasNext(); ) {
            jq_Method m = (jq_Method) i.next();
            if (m == null) continue;
            backEdges.put(m, new HashSet());
            worklist.add(m);
        }
        while (!worklist.isEmpty()) {
            jq_Method caller = (jq_Method) worklist.removeFirst();
            Collection callsites = this.getCallSites(caller);
            for (Iterator i=callsites.iterator(); i.hasNext(); ) {
                ProgramLocation cs = (ProgramLocation) i.next();
                Collection callees = this.getTargetMethods(cs);
                for (Iterator j=callees.iterator(); j.hasNext(); ) {
                    jq_Method callee = (jq_Method) i.next();
                    if (callee == null) continue;
                    Set s = (Set) backEdges.get(callee);
                    if (s == null) {
                        backEdges.put(callee, s = new HashSet());
                        worklist.add(callee);
                    }
                    s.add(cs);
                }
            }
        }
        return backEdges;
    }
    
    /**
     * Returns the call graph edge relation in the form of an invertible multi-map.
     * The edge relation contains both the relations between methods and their call
     * sites and between call sites and their target methods.
     * 
     * @return set of caller methods
     */
    public InvertibleMultiMap calculateEdgeRelation() {
        InvertibleMultiMap edges = new GenericInvertibleMultiMap();
        for (Iterator i = this.getAllMethods().iterator(); i.hasNext(); ) {
            jq_Method caller = (jq_Method) i.next();
            if (caller == null) continue;
            Collection callsites = edges.getValues(caller);
            Collection callsites2 = this.getCallSites(caller);
            callsites.addAll(callsites2);
            for (Iterator j = callsites2.iterator(); j.hasNext(); ) {
                ProgramLocation cs = (ProgramLocation) j.next();
                Collection callees = edges.getValues(cs);
                Collection callees2 = this.getTargetMethods(cs);
                callees.addAll(callees2);
            }
        }
        return edges;
    }
    
    public Collection[] findDepths() {
        Collection roots = getRoots();
        HashSet visited = new HashSet();
        LinkedList result = new LinkedList();
        LinkedList previous = new LinkedList();
        visited.addAll(roots);
        previous.addAll(roots);
        while (!previous.isEmpty()) {
            result.add(previous);
            LinkedList current = new LinkedList();
            for (Iterator i=previous.iterator(); i.hasNext(); ) {
                jq_Method caller = (jq_Method) i.next();
                if (caller == null) continue;
                for (Iterator j=getCallSites(caller).iterator(); j.hasNext(); ) {
                    ProgramLocation cs = (ProgramLocation) j.next();
                    for (Iterator k=getTargetMethods(cs).iterator(); k.hasNext(); ) {
                        jq_Method callee = (jq_Method) k.next();
                        if (visited.contains(callee)) {
                            // back or cross edge in call graph.
                            continue;
                        }
                        visited.add(callee);
                        current.add(callee);
                    }
                }
            }
            previous = current;
        }
        
        return (Collection[]) result.toArray(new Collection[result.size()]);
    }
    
    public Navigator getNavigator() {
        return getMethodNavigator();
    }
    
    public Navigator getMethodNavigator() {
        return new CallGraphMethodNavigator();
    }
    
    public Navigator getCallSiteNavigator() {
        return new CallGraphCSNavigator();
    }
    
    public MultiMap getCallSiteMap() {
        Set methods = (Set) getAllMethods();
        CallSiteMap csm = new CallSiteMap(methods);
        return csm;
    }
    
    /*
    public MultiMap getCallTargetMap() {
        Set callSites = (Set) getAllCallSites();
        CallTargetMap ctm = new CallTargetMap(callSites);
        return ctm;
    }
    */
    
    public MultiMap getCallGraphMap() {
        Set methods = (Set) getAllMethods();
        CallSiteMap csm = new CallSiteMap(methods);
        CallTargetMap ctm = new CallTargetMap((Set)csm.values());
        return new CallGraphMap(csm, ctm);
    }
    
    public static class CallSiteMap extends UnmodifiableMultiMap {

        public static final CallSiteMap INSTANCE = new CallSiteMap();

        private final Set methods;
        
        public CallSiteMap(Set methods) {
            Assert._assert(methods != null);
            this.methods = methods;
        }
        private CallSiteMap() {
            this.methods = null;
        }

        /* (non-Javadoc)
         * @see java.util.Map#entrySet()
         */
        public Set entrySet() {
            if (methods == null)
                throw new UnsupportedOperationException();
            return entrySetHelper(methods);
        }

        /* (non-Javadoc)
         * @see jwutil.collections.MultiMap#getValues(java.lang.Object)
         */
        public Collection getValues(Object key) {
            jq_Method m = (jq_Method) key;
            return getCallSites0(m);
        }

        /* (non-Javadoc)
         * @see jwutil.collections.BinaryRelation#contains(java.lang.Object, java.lang.Object)
         */
        public boolean contains(Object a, Object b) {
            ProgramLocation cs = (ProgramLocation) b;
            if (methods != null && !methods.contains(a)) return false;
            return cs.getMethod() == a;
        }
    }
    
    public class CallTargetMap extends UnmodifiableMultiMap {

        private final Set callSites;

        public CallTargetMap(Set callSites) {
            this.callSites = callSites;
        }
        public CallTargetMap() {
            this.callSites = null;
        }

        /* (non-Javadoc)
         * @see java.util.Map#entrySet()
         */
        public Set entrySet() {
            if (callSites == null)
                throw new UnsupportedOperationException();
            return entrySetHelper(callSites);
        }

        /* (non-Javadoc)
         * @see jwutil.collections.MultiMap#getValues(java.lang.Object)
         */
        public Collection getValues(Object key) {
            ProgramLocation cs = (ProgramLocation) key;
            return getTargetMethods(cs);
        }

        /* (non-Javadoc)
         * @see jwutil.collections.BinaryRelation#contains(java.lang.Object, java.lang.Object)
         */
        public boolean contains(Object a, Object b) {
            return getValues(a).contains(b);
        }
    }
    
    public static class CallGraphMap extends UnmodifiableMultiMap {
        
        private final MultiMap methodToCallSite;
        private final MultiMap callSiteToTarget;
        
        public CallGraphMap(MultiMap methodToCallSite, MultiMap callSiteToTarget) {
            this.methodToCallSite = methodToCallSite;
            this.callSiteToTarget = callSiteToTarget;
        }
        
        /* (non-Javadoc)
         * @see java.util.Map#entrySet()
         */
        public Set entrySet() {
            return entrySetHelper(methodToCallSite.keySet());
        }
        
        /* (non-Javadoc)
         * @see jwutil.collections.MultiMap#getValues(java.lang.Object)
         */
        public Collection getValues(Object key) {
            Collection c1 = methodToCallSite.getValues(key);
            Iterator i = c1.iterator();
            if (!i.hasNext()) return c1;
            Object o = i.next();
            if (!i.hasNext()) return callSiteToTarget.getValues(o);
            Collection result = new LinkedHashSet();
            for (;;) {
                result.addAll(callSiteToTarget.getValues(o));
                if (!i.hasNext()) break;
                o = i.next();
            }
            return result;
        }
        
        /* (non-Javadoc)
         * @see jwutil.collections.BinaryRelation#contains(java.lang.Object, java.lang.Object)
         */
        public boolean contains(Object a, Object b) {
            return getValues(a).contains(b);
        }
        
    }
    
    public class CallGraphMethodNavigator implements Navigator {

        /**
         * @see jwutil.graphs.Navigator#next(java.lang.Object)
         */
        public Collection next(Object node) {
            jq_Method caller = (jq_Method) node;
            Collection s = getCallees(caller);
            return s;
        }

        /**
         * @see jwutil.graphs.Navigator#prev(java.lang.Object)
         */
        public Collection prev(Object node) {
            jq_Method callee = (jq_Method) node;
            Collection s = getCallerMethods(callee);
            return s;
        }
        
    }
    
    public class CallGraphCSNavigator implements Navigator {

        /**
         * @see jwutil.graphs.Navigator#next(java.lang.Object)
         */
        public Collection next(Object node) {
            if (node instanceof jq_Method)
                return getCallSites((jq_Method) node);
            else
                return getTargetMethods((ProgramLocation) node);
        }

        /**
         * @see jwutil.graphs.Navigator#prev(java.lang.Object)
         */
        public Collection prev(Object node) {
            if (node instanceof jq_Method)
                return getCallers((jq_Method) node);
            else
                return Collections.singleton(((ProgramLocation) node).getMethod());
        }
        
    }
    
    /* (non-Javadoc)
     * @see java.util.Map#entrySet()
     */
    public Set entrySet() {
        return entrySetHelper((Set) getAllCallSites());
    }

    /* (non-Javadoc)
     * @see jwutil.collections.BinaryRelation#contains(java.lang.Object, java.lang.Object)
     */
    public boolean contains(Object a, Object b) {
        ProgramLocation p = (ProgramLocation) a;
        return getTargetMethods(p).contains(b);
    }

    /* (non-Javadoc)
     * @see jwutil.collections.MultiMap#getValues(java.lang.Object)
     */
    public Collection getValues(Object key) {
        ProgramLocation p = (ProgramLocation) key;
        return getTargetMethods(p);
    }

    public static CallGraph makeCallGraph(Collection rootMethods, Map callsToTargets) {
        
        final Collection roots = rootMethods;
        final Map callSiteToTargets = callsToTargets;
        
        return new CallGraph() {

            /**
             * @see joeq.Compiler.Quad.CallGraph#getTargetMethods(java.lang.Object, joeq.Compiler.Analysis.IPA.ProgramLocation)
             */
            public Collection getTargetMethods(Object context, ProgramLocation callSite) {
                jq_Method method = (jq_Method) callSite.getTargetMethod();
                if (callSite.isSingleTarget()) {
                    return Collections.singleton(method);
                }
                Collection targets = (Collection) callSiteToTargets.get(callSite);
                if (targets != null) {
                    return targets;
                } else {
                    return Collections.EMPTY_SET;
                } 
            }

            /* (non-Javadoc)
             * @see joeq.Compiler.Quad.CallGraph#setRoots(java.util.Collection)
             */
            public void setRoots(Collection newRoots) {
                Assert._assert(roots.equals(newRoots));
            }

            /* (non-Javadoc)
             * @see joeq.Compiler.Quad.CallGraph#getRoots()
             */
            public Collection getRoots() {
                return roots;
            }
        
            /* (non-Javadoc)
             * @see joeq.Compiler.Quad.CallGraph#getAllCallSites()
             */
            public Collection getAllCallSites() {
                return callSiteToTargets.keySet();
            }

            /* (non-Javadoc)
             * @see joeq.Compiler.Quad.CallGraph#getAllMethods()
             */
            public Collection getAllMethods() {
                LinkedHashSet s = new LinkedHashSet();
                s.addAll(roots);
                for (Iterator i = callSiteToTargets.values().iterator(); i.hasNext(); ) {
                    Collection c = (Collection) i.next();
                    s.addAll(c);
                }
                return s;
            }
            
        };
    }

}
