// PACallGraph.java, created Oct 21, 2003 12:56:45 AM by joewhaley
// Copyright (C) 2003 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.Analysis.IPA;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import joeq.Compiler.Quad.CallGraph;
import joeq.Compiler.Quad.LoadedCallGraph;
import jwutil.collections.IndexMap;
import jwutil.collections.IndexedMap;
import jwutil.collections.UnmodifiableIterator;
import jwutil.util.Assert;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDDomain;
import net.sf.javabdd.BDDFactory;

/**
 * PACallGraph
 * 
 * @author John Whaley
 * @version $Id: PACallGraph.java,v 1.17 2006/03/01 18:37:18 livshits Exp $
 */
public class PACallGraph extends CallGraph {

    public static final boolean TRACE = false;
    
    final BDDFactory bdd;
    final BDDDomain M, I;
    final Collection roots;
    final BDD visited;
    final BDD IE;
    final IndexMap Mmap, Imap;

    private PA pa;
    
    public PACallGraph(PA pa) {
        this.bdd = pa.bdd;
        this.M = pa.M; 
        this.I = pa.I;
        this.roots = pa.rootMethods;
        this.visited = pa.visited;
        //this.IE = pa.IEcs.exist(pa.V1cV2cset);
        this.IE = pa.IE;
        this.Mmap = pa.Mmap;
        this.Imap = pa.Imap;
        this.pa = pa;
    }
    
    /* (non-Javadoc)
     * @see joeq.Compiler.Quad.CallGraph#setRoots(java.util.Collection)
     */
    public void setRoots(Collection roots) {
        Assert.UNREACHABLE();
    }

    /* (non-Javadoc)
     * @see joeq.Compiler.Quad.CallGraph#getRoots()
     */
    public Collection getRoots() {
        return roots;
    }

    /* (non-Javadoc)
     * @see joeq.Compiler.Quad.CallGraph#getTargetMethods(java.lang.Object, joeq.Compiler.Analysis.IPA.ProgramLocation)
     */
    public Collection getTargetMethods(Object context, ProgramLocation callSite) {
        callSite = LoadedCallGraph.mapCall(callSite);
        int I_i = Imap.get(callSite);
        BDD I_bdd = I.ithVar(I_i);
        BDD b = IE.restrict(I_bdd);
        if (TRACE) 
            System.out.println("Target methods of "+callSite+" = "+b.toStringWithDomains(pa.TS));
        I_bdd.free();
        return new BDDSet(b, M, Mmap);
    }
    
    /* (non-Javadoc)
     * @see joeq.Compiler.Quad.CallGraph#getAllMethods()
     */
    public Collection getAllMethods() {
        BDD b = visited.id();
        //b.orWith(pa.IE.exist(pa.Iset));
        //b.orWith(pa.Mret.exist(pa.V2set));
        return new BDDSet(b, M, Mmap);
    }

    public static class BDDSet extends AbstractSet {
        BDD b;
        BDDDomain d;
        BDD dset;
        IndexedMap map;
        public BDDSet(BDD b, BDDDomain d, IndexedMap map) {
            this.b = b;
            this.d = d;
            this.dset = d.set();
            this.map = map;
        }
        public int size() {
            return (int) b.satCount(dset);
        }
        public Iterator iterator() {
            final BDD b1 = b.id();
            return new UnmodifiableIterator() {
                public boolean hasNext() {
                    return !b1.isZero();
                }
                public Object next() {
                    BDD b2 = b1.satOne(dset, false);
                    final int d_i = b2.scanVar(d).intValue();
                    b1.applyWith(b2, BDDFactory.diff);
                    return map.get(d_i);
                }
            };
        }
    }
    
    // Not used.
    public static class PACallTargetMap extends AbstractMap {

        PA pa;
        
        /* (non-Javadoc)
         * @see java.util.Map#get(java.lang.Object)
         */
        public Object get(Object key) {
            int I_i = pa.Imap.get(key);
            BDD m = pa.IE.restrict(pa.I.ithVar(I_i));
            return new BDDSet(m, pa.M, pa.Mmap);
        }
        
        /* (non-Javadoc)
         * @see java.util.AbstractMap#entrySet()
         */
        public Set entrySet() {
            return new AbstractSet() {

                public int size() {
                    return (int) pa.IE.satCount(pa.IMset);
                }

                public Iterator iterator() {
                    final BDD bdd1 = pa.IE.id();
                    return new UnmodifiableIterator() {

                        public boolean hasNext() {
                            return !bdd1.isZero();
                        }

                        public Object next() {
                            BDD bdd2 = bdd1.satOne(pa.IMset, false);
                            final int I_i = bdd2.scanVar(pa.I).intValue();
                            BDD bdd3 = pa.IE.restrict(pa.I.ithVar(I_i));
                            final Collection result = new BDDSet(bdd3, pa.M, pa.Mmap);
                            bdd1.applyWith(bdd2, BDDFactory.diff);
                            return new Map.Entry() {

                                public Object getKey() {
                                    return pa.Imap.get(I_i);
                                }

                                public Object getValue() {
                                    return result;
                                }

                                public Object setValue(Object value) {
                                    throw new UnsupportedOperationException();
                                }
                                
                            };
                        }
                        
                    };
                }
                
            };
        }
        
    }
    
}
