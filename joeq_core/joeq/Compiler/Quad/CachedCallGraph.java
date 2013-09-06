// CachedCallGraph.java, created Sat Mar 29  0:56:01 2003 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.Quad;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import joeq.Class.jq_Method;
import joeq.Compiler.Analysis.IPA.ProgramLocation;
import jwutil.collections.GenericInvertibleMultiMap;
import jwutil.collections.GenericMultiMap;
import jwutil.collections.InvertibleMultiMap;
import jwutil.collections.MultiMap;

/**
 * @author John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: CachedCallGraph.java,v 1.13 2004/09/22 22:17:26 joewhaley Exp $
 */
public class CachedCallGraph extends CallGraph {

    private final CallGraph delegate;

    private Set/*<jq_Method>*/ methods;
    private MultiMap/*<jq_Method,ProgramLocation>*/ callSites;
    private InvertibleMultiMap/*<ProgramLocation,jq_Method>*/ edges;

    public CachedCallGraph(CallGraph cg) {
        this.delegate = cg;
    }

    public void invalidateCache() {
        this.edges = new GenericInvertibleMultiMap();
        this.callSites = new GenericMultiMap();
        for (Iterator i = delegate.getAllCallSites().iterator(); i.hasNext(); ) {
            ProgramLocation p = (ProgramLocation) i.next();
            Collection callees = this.edges.getValues(p);
            Collection callees2 = delegate.getTargetMethods(p);
            if (!callees2.isEmpty())
                callees.addAll(callees2);
            else
                callSites.add(p.getMethod(), p);
        }
        this.methods = new HashSet(delegate.getRoots());
        for (Iterator i = this.edges.keySet().iterator(); i.hasNext(); ) {
            ProgramLocation p = (ProgramLocation) i.next();
            jq_Method m = (jq_Method) p.getMethod();
            methods.add(m);
            methods.addAll(this.edges.getValues(p));
            Collection c = callSites.getValues(m);
            c.add(p);
        }
    }

    /* (non-Javadoc)
     * @see joeq.Compiler.Quad.CallGraph#setRoots(java.util.Collection)
     */
    public void setRoots(Collection roots) {
        delegate.setRoots(roots);
        invalidateCache();
    }

    /* (non-Javadoc)
     * @see joeq.Compiler.Quad.CallGraph#entrySet()
     */
    public Set entrySet() {
        if (edges == null) invalidateCache();
        return edges.entrySet();
    }

    /* (non-Javadoc)
     * @see joeq.Compiler.Quad.CallGraph#getAllCallSites()
     */
    public Collection getAllCallSites() {
        if (edges == null) invalidateCache();
        if (true) {
            return edges.keySet();
        } else {
            return callSites.values();
        }
    }

    /* (non-Javadoc)
     * @see joeq.Compiler.Quad.CallGraph#getAllMethods()
     */
    public Collection getAllMethods() {
        if (edges == null) invalidateCache();
        if (true) {
            return methods;
        } else {
            LinkedHashSet allMethods = new LinkedHashSet(edges.values());
            allMethods.addAll(delegate.getRoots());
            return allMethods;
        }
    }

    /* (non-Javadoc)
     * @see joeq.Compiler.Quad.CallGraph#getCallees(joeq.Compiler.Quad.ControlFlowGraph)
     */
    public Collection getCallees(ControlFlowGraph cfg) {
        return getCallees(cfg.getMethod());
    }

    /* (non-Javadoc)
     * @see joeq.Compiler.Quad.CallGraph#getCallees(joeq.Class.jq_Method)
     */
    public Collection getCallees(jq_Method caller) {
        if (edges == null) invalidateCache();
        return getFromMultiMap(callSites, edges, caller);
    }

    public static Collection getFromMultiMap(MultiMap m1, MultiMap m2, jq_Method method) {
        Collection c1 = m1.getValues(method);
        Iterator i = c1.iterator();
        if (!i.hasNext()) return Collections.EMPTY_SET;
        Object o = i.next();
        if (!i.hasNext()) return m2.getValues(o);
        Set result = new LinkedHashSet();
        for (;;) {
            result.addAll(m2.getValues(o));
            if (!i.hasNext()) break;
            o = i.next();
        }
        return result;
    }
    
    /* (non-Javadoc)
     * @see joeq.Compiler.Quad.CallGraph#getCallerMethods(joeq.Class.jq_Method)
     */
    public Collection getCallers(jq_Method callee) {
        if (edges == null) invalidateCache();
        MultiMap m1 = edges.invert();
        Collection c1 = m1.getValues(callee);
        return c1;
    }

    /* (non-Javadoc)
     * @see joeq.Compiler.Quad.CallGraph#getCallerMethods(joeq.Class.jq_Method)
     */
    public Collection getCallerMethods(jq_Method callee) {
        if (edges == null) invalidateCache();
        MultiMap m1 = edges.invert();
        Collection c1 = m1.getValues(callee);
        Iterator i = c1.iterator();
        if (!i.hasNext()) return Collections.EMPTY_SET;
        ProgramLocation o = (ProgramLocation) i.next();
        if (!i.hasNext()) return Collections.singleton(o.getMethod());
        Set result = new LinkedHashSet();
        for (;;) {
            result.add(o.getMethod());
            if (!i.hasNext()) break;
            o = (ProgramLocation) i.next();
        }
        return result;
    }

    /* (non-Javadoc)
     * @see joeq.Compiler.Quad.CallGraph#getCallSites(joeq.Compiler.Quad.ControlFlowGraph)
     */
    public Collection getCallSites(ControlFlowGraph cfg) {
        return getCallSites(cfg.getMethod());
    }

    /* (non-Javadoc)
     * @see joeq.Compiler.Quad.CallGraph#getCallSites(joeq.Class.jq_Method)
     */
    public Collection getCallSites(jq_Method caller) {
        if (callSites == null) invalidateCache();
        return callSites.getValues(caller);
    }

    /* (non-Javadoc)
     * @see joeq.Compiler.Quad.CallGraph#getTargetMethods(java.lang.Object, joeq.Compiler.Analysis.IPA.ProgramLocation)
     */
    public Collection getTargetMethods(Object context, ProgramLocation callSite) {
        if (edges == null) invalidateCache();
        return edges.getValues(callSite);
    }
    
    /**
     * Inline the given edge in the call graph.
     * 
     * @param caller  caller method
     * @param callSite  call site to inline
     * @param callee  callee method
     */
    public void inlineEdge(jq_Method caller, ProgramLocation callSite, jq_Method callee) {
        if (false) System.out.println("Inlining edge "+callSite+" -> "+callee);
        // remove call site from caller.
        callSites.remove(caller, callSite);
        // add all call sites in callee into caller.
        callSites.addAll(caller, callSites.getValues(callee));
    }

    /* (non-Javadoc)
     * @see java.util.AbstractMap#keySet()
     */
    public Set keySet() {
        if (edges == null) invalidateCache();
        return edges.keySet();
    }

    /* (non-Javadoc)
     * @see joeq.Compiler.Quad.CallGraph#getRoots()
     */
    public Collection getRoots() {
        return delegate.getRoots();
    }

}
