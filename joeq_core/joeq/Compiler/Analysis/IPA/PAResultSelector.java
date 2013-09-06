// PAResultSelector.java, created Nov 3, 2003 12:34:24 AM by joewhaley
// Copyright (C) 2003 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.Analysis.IPA;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.math.BigInteger;
import joeq.Class.jq_Field;
import joeq.Class.jq_Method;
import joeq.Class.jq_Type;
import joeq.Compiler.Analysis.FlowInsensitive.MethodSummary.Node;
import jwutil.util.Assert;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDDomain;
import net.sf.javabdd.TypedBDDFactory.TypedBDD;

/**
 * A helper class for PAResults.
 * @see PAResults
 * */
public class PAResultSelector {
    private PAResults _results;
    private PA r;
    private boolean _verbose = false;

    public PAResultSelector(PAResults results){
        _results = results;
        r = results.r;    
    }
    
    public TypedBDD getReturnBDD(jq_Method method) {
        // lookup in MRet
        int idx = r.Mmap.get(method);
        BDD i   = r.M.ithVar(idx);
        BDD restricted = r.Mret.restrictWith(i);
        
        //System.out.println("In getReturnBDD: " + _results.toString((TypedBDD) restricted, -1));
        
        return (TypedBDD)restricted.replaceWith(r.bdd.makePair(r.V2, r.V1));
    }
    
    public TypedBDD getFormalParamBDD(jq_Method method, int index) {
        // lookup in MRet
        int idx = r.Mmap.get(method);
        BDD i   = r.M.ithVar(idx);
    
        // formal is in MxZxV1
        return (TypedBDD)r.formal.restrictWith(i).restrictWith(r.Z.ithVar(index));
    }
    
    public BDD addAllContextToVar(BDD bdd) {
        TypedBDD tbdd = (TypedBDD)bdd;
        Set domains = tbdd.getDomainSet();

        Assert._assert(domains.size() == 1);
        BDDDomain dom = (BDDDomain)domains.iterator().next();
        Assert._assert(dom != r.V1c[0] && dom != r.V2c[0]);        
        
        tbdd.setDomains(dom, r.V1c[0]);
        //BDD result = (TypedBDD)tbd q  d.and(r.V1c.set());
        //tbdd.free();
        
        //return result;
        return tbdd;
    }

    protected Collection getUses(TypedBDD bdd) {
        bdd = (TypedBDD)addAllContextToVar(bdd);
        TypedBDD reach = (TypedBDD)_results.calculateDefUse(bdd);
        BDD vars = reach.exist(r.V1cset);
        
        Collection result = new LinkedList();
        for(Iterator iter = ( (TypedBDD)vars ).iterator(); iter.hasNext(); ) {
            TypedBDD var = (TypedBDD)iter.next();
            
            result.add(getNode(var));
        }
        return result; 
    }
    
    protected Collection getUses(Node node) {
        TypedBDD bdd = (TypedBDD)r.V1.ithVar(_results.getVariableIndex(node));
        
        return getUses(bdd);
    }
    
    public Node getNode(TypedBDD var) {              
        BigInteger[] indeces = r.V1.getVarIndices(var);
        Assert._assert(indeces.length == 1, "There are " + indeces.length + " indeces in " + var.toStringWithDomains());
        BigInteger index = indeces[0];        
        Node node = _results.getVariableNode(index.intValue());
        
        return node;
    }
    
    public void collectReachabilityTraces(TypedBDD start, TypedBDD stop) {
        /*
        try {
            //_results.printDefUseChain(bdd.andWith(r.V1c.set()));
            _results.defUseGraph(bdd.andWith(r.V1c.set()), true, new DataOutputStream(System.out));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        */
        
        LinkedList results = new LinkedList();
        Node root = getNode(start);
        Node sink = getNode(stop);
        System.out.println("Starting with " + root + ", looking for " + sink);
        collectReachabilityTraces(new PAReachabilityTrace(), root, sink, results);
        
        int i = 0;
        for(Iterator iter = results.iterator(); iter.hasNext(); i++) {
            PAReachabilityTrace trace = (PAReachabilityTrace)iter.next();
            
            System.out.println("Trace " + i + ": " + trace.toStringLong());
        }           
    }
    
    protected void collectReachabilityTraces(PAReachabilityTrace trace, Node last, Node stop, Collection results) {
        if(trace.contains(last)){
            if(_verbose) System.err.println("Already seen " + last + " in the trace " + trace.toString());
            results.add(trace); return; 
        }
        trace.addNode(last);
        if(stop == last) {
            if(_verbose) System.err.println("Found " + stop);
            results.add(trace); return;
        }
        Collection c = getUses(last);
        if(c.isEmpty()) {
            if(_verbose) System.err.println("Finished with " + last);
            results.add(trace); return;
        }
        
        if(_verbose) {
            System.err.println("Node " + last + " has " + c.size() + " successor(s): " + c +
                        "\t trace: " + trace);
        }
        
        for(Iterator iter = c.iterator(); iter.hasNext();) {
            Node node = (Node)iter.next();
            
            PAReachabilityTrace newTrace = (PAReachabilityTrace)trace.clone();
            collectReachabilityTraces(newTrace, node, stop, results);
        }
    }
    
    class PAReachabilityTrace {
        LinkedList/*Node*/ _nodes;
        
        PAReachabilityTrace(){
            _nodes = new LinkedList();
        }
        public boolean contains(Node node) {
            return _nodes.contains(node);
        }
        void addNode(Node node) {
            _nodes.addLast(node);
        }
        public String toString() {
            StringBuffer buf = new StringBuffer(size() + " [");
            int i = 1;
            for(Iterator iter = _nodes.iterator(); iter.hasNext(); i++) {
                Node node = (Node)iter.next();
                
                buf.append(" (" + i + ")");
                buf.append(node.toString_short());                
            }
            buf.append("]");
            return buf.toString();
        }
        public String toStringLong() {
            StringBuffer buf = new StringBuffer(size() + " [\n");
            int i = 1;
            for(Iterator iter = _nodes.iterator(); iter.hasNext(); i++) {
                Node node = (Node)iter.next();
        
                buf.append("\t(" + i + ")");
                buf.append(node.toString_long());
                buf.append("\n");                
            }
            buf.append("]");
            return buf.toString();
        }
        public Object clone() {
            PAReachabilityTrace result = new PAReachabilityTrace();
            for(Iterator iter = _nodes.iterator(); iter.hasNext(); ) {
                result.addNode((Node)iter.next());
            } 
            Assert._assert(size() == result.size());
            return result;
        }
        int size() {return _nodes.size();}
    }

    public void collectReachabilityTraces2(BDD bdd) {
        bdd = addAllContextToVar(bdd);
        int i = 0;
        BDD reach;
        Assert._assert(_results != null);
        do {
            BDD vars = bdd.exist(r.V1cset);
            System.err.print("Generation " + i + ": ");
            for(Iterator li = ((TypedBDD)vars).iterator(); li.hasNext(); ) {
                System.err.print(((TypedBDD)li.next()).toStringWithDomains() + " ");
            }
            System.err.print("\n");
                        
            // ((TypedBDD)bdd).satCount()
            //System.err.println("Generation " + i + ": " + bdd.toStringWithDomains() /*_pa.toString((TypedBDD)bdd, -1)*/ + "\n");
            reach = _results.calculateDefUse(bdd);
            bdd = reach;
            i++;                
        } while(i < 10 && !bdd.isZero());        
    }
    
    /**
     * @param f pointed to 
     * @return Set of types of objects f could point to
     */
    public Set getFieldPointeeTypes(jq_Field f){
        // Do the bdd magic to produce the answer
        // 1. project f out of H1xFxH2, get a set of H1xH2 pairs, h1 pointing to h2
        int fieldIndex = r.getResults().getFieldIndex(f);
        BDD fBDD = r.F.ithVar(fieldIndex); // F            
        
        // 2. project out H1, get all H2 elements
        BDD h2 = r.H1FH2set.relprod(fBDD, r.H1set); // H2
        
        // 3. get all types of H2 elements
        TypedBDD typesOfH2 = (TypedBDD) h2.replace(r.H2toH1).relprod(r.hT, r.H1set);

        // 4. return them as a set             
        Set result = new HashSet();            
        for(Iterator typeIter = typesOfH2.iterator(); typeIter.hasNext();) {
            TypedBDD typeBDD = (TypedBDD)((TypedBDD)typeIter.next()); // T
            
            BigInteger[] indeces = r.T1.getVarIndices(typeBDD);
            Assert._assert(indeces.length == 1, "There are " + indeces.length + " indeces in " + typeBDD.toStringWithDomains());
            BigInteger index = indeces[0];        
            jq_Type type = r.getResults().getType(index.intValue());
            Assert._assert(type != null);
            
            result.add(type);
        }
        
        // TODO: test this
        return result;
    }
}