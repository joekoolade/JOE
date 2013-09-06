/*
 * Created on Sep 25, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package joeq.Compiler.Analysis.IPSSA;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.io.PrintStream;
import joeq.Compiler.Analysis.IPSSA.Utils.IteratorHelper;
import jwutil.collections.LinearSet;
import jwutil.util.Assert;

/**
 * This is a graph consisting of definitions that uses as much sharing as possible.
 * 
 * Provides a dot printer.
 * 
 *  @author V.Benjamin Livshits
 */
public abstract class DefinitionGraph {
    private HashSet _nodes;
    protected int   _edgeCount = 0;

    public DefinitionGraph(){
        _nodes = new HashSet();        
    }
    
    protected void addNode(SSADefinition def){
        _nodes.add(def);        // duplicates don't matter
    }
    
    public int getEdgeCount(){return _edgeCount;}
    public int getNodeCount(){return _nodes.size();}
    
    public abstract void    addEdge(SSADefinition def1, SSADefinition def2, EdgeInfo ei);
    public abstract boolean isRootNode(SSADefinition def);
    public abstract boolean isTerminalNode(SSADefinition def);
    
    /** Retrieves all roots of the forest that we are constructing */
    public Set getRoots(){
        LinearSet set = new LinearSet();
        for(Iterator iter = _nodes.iterator(); iter.hasNext();){
            SSADefinition def = (SSADefinition)iter.next();
            
            if(isRootNode(def)){
                set.add(def);
            }
        }
        return set;
    }
    
    public Set getTerminals(){
        LinearSet set = new LinearSet();
        for(Iterator iter = _nodes.iterator(); iter.hasNext();){
            SSADefinition def = (SSADefinition)iter.next();
        
            if(isTerminalNode(def)){
                set.add(def);
            }
        }
        return set;
    }

    /// Forward links
    public abstract SSAIterator.DefinitionIterator getReached(SSADefinition def);    
    /** All reaching definitions */
    public abstract SSAIterator.DefinitionIterator getAllReached(SSADefinition def);

    
    /// Backward links
    /** One level of pointees */
    public abstract SSAIterator.DefinitionIterator getReaching(SSADefinition def);    
    /** All reaching definitions */
    public abstract SSAIterator.DefinitionIterator getAllReaching(SSADefinition def);
    

    public String toString(){
        return "DefinitionGraph: " + _nodes.size() + "nodes, " + _edgeCount + " edges";
    }
        
    /**
     * Prints the subtree starting at definition def in a dot graph form.
     * */
    public void printDot(PrintStream out){
        printDot(_nodes.iterator(), out, true);
    }
    public void printReachedToDot(SSADefinition def, PrintStream out){
        printDot(new IteratorHelper.SingleIterator(def), out, true);
    }    
    public void printReachingToDot(SSADefinition def, PrintStream out){
        printDot(new IteratorHelper.SingleIterator(def), out, false);
    }
    
    protected void printDot(Iterator iter, PrintStream out, final boolean direction){
        joeq.Util.SyntheticGraphs.Graph g = new joeq.Util.SyntheticGraphs.Graph();
        
        while(iter.hasNext()){
            SSADefinition def = (SSADefinition)iter.next();
        
            (new Object(){
                void makeDotAux(joeq.Util.SyntheticGraphs.Graph g, SSADefinition def){
                    g.addNode(def.getID(), def.toString());
                    Iterator iter = direction ? getReached(def) : getReaching(def);
                    if(iter != null){
                        while(iter.hasNext()){
                            SSADefinition def2 = (SSADefinition) iter.next();
                            g.addEdge(def.getID(), def2.getID());                            
                        
                            makeDotAux(g, def2);                
                        }
                    }
                }            
            }).makeDotAux(g, def);
        
            g.printDot(out);
        }
    }
    
    /** By default a true predicate edge is added */
    public void addEdge(SSADefinition def1, SSADefinition def2){
        addEdge(def1, def2, PredicateEdge.TrueEdge.FACTORY.get());
    }
    
    /** null returned means that there is no edge */
    public abstract EdgeInfo getEdgeInfo(SSADefinition def1, SSADefinition def2);
    
    //public abstract boolean isTerminalNode(SSADefinition def);
        
    public interface EdgeInfo {};
    
    class EmptyEdge implements EdgeInfo {}
    
    static class PredicateEdge implements EdgeInfo {
        SSAValue.Predicate _predicate;
        
        PredicateEdge(SSAValue.Predicate predicate){
            this._predicate = predicate;
        }
        
        static class TrueEdge extends PredicateEdge {
            SSAValue.Predicate _predicate;
            
            private TrueEdge(){
                super(SSAValue.Predicate.True());
            }
            
            static class FACTORY {
                static TrueEdge _sample;
                
                /** Use this method instead of the TrueEdge constructor */
                static TrueEdge get(){
                    if(_sample == null){
                        _sample = new TrueEdge();
                    }
                    return _sample;
                }
            }
        }
    }
    
    class ContextEdge implements EdgeInfo {
        ContextSet _context;
    
        ContextEdge(ContextSet context){
            this._context = context;
        }
    }
    
    class IPEdge implements EdgeInfo {
        // TODO: don't really know what the representation might be like
        // maybe this is not necessary...
    
        IPEdge(){
            // TODO: 
        }
    }

    /**
     * The representation consists of an bunch of adjecently lists pointing 
     * to and from a node. We can only have one edge between any two definitions. 
     * */    
    class EfficientDefinitionGraph extends DefinitionGraph {
        private HashMap/*<SSADefinition, Map<SSADefinition, EdgeInfo> >*/ _adjacencyLists = new HashMap();
        private HashMap/*<SSADefinition, LinkedList<SSADefinition> >*/    _reverseAdjacencyLists = new HashMap();
        
        EfficientDefinitionGraph(){}
        
        public void addEdge(SSADefinition def1, SSADefinition def2, EdgeInfo ei){
            // Adjust direct adjancency lists
            HashMap map = (HashMap)_adjacencyLists.get(def2);
            if(map == null){
                map = new HashMap();
                _adjacencyLists.put(def2, map);
            }
            
            Assert._assert(!map.containsKey(def1), "Already have a an edge " + 
                def1.toString() + " -> " + def2.toString() + 
                " with edge information " + ei.toString());
                
            map.put(def1, ei);
            
            // Adjust reverse adjancency lists
            LinkedList list = (LinkedList)_reverseAdjacencyLists.get(def1);
            if(list == null){
                list = new LinkedList();                 
            }
            list.addLast(def2);
            _edgeCount++;
        }
        
        public EdgeInfo getEdgeInfo(SSADefinition def1, SSADefinition def2){
            HashMap map = (HashMap)_adjacencyLists.get(def2);
            if(map == null){
                return null;
            }
            return (EdgeInfo)map.get(def1);        
        }
                
        /** True is this nodes doesn't point to anyone */ 
        public boolean isTerminalNode(SSADefinition def){
            LinkedList list = (LinkedList)_reverseAdjacencyLists.get(def);
            if(list == null){
                return true;
            }
            
            return list.isEmpty();
        }
        
        /** True if nobody points to this node */
        public boolean isRootNode(SSADefinition def){
            HashMap map = (HashMap)_adjacencyLists.get(def);
            if(map == null){
                return true;
            }
            
            return map.isEmpty();
        }

        // Follow the forward links        
        public SSAIterator.DefinitionIterator getReached(SSADefinition def){
            LinkedList list = (LinkedList)_reverseAdjacencyLists.get(def);
            if(list == null){
                return null;
            }
            
            return new SSAIterator.DefinitionIterator(list.iterator());
        }        
        public SSAIterator.DefinitionIterator getAllReached(SSADefinition def){
            HashSet set = new HashSet();
    
            (new Object(){
                Set getReachedAux(SSADefinition def, Set set){
                    set.add(def);
                    Iterator iter = getReached(def);
                    if(iter != null){
                        while(iter.hasNext()){
                            SSADefinition def2 = (SSADefinition)iter.next();
                    
                            getReachedAux(def2, set);
                        }            
                    }            
            
                    return set; 
                }
            }).getReachedAux(def, set);            // fill the set with reaching definitions
    
            return new SSAIterator.DefinitionIterator(set.iterator());
        }
        
        // Follow the backward links        
        public SSAIterator.DefinitionIterator getReaching(SSADefinition def){
            HashMap map = (HashMap)_adjacencyLists.get(def);
            if(map == null){
                return null;
            }
            
            return new SSAIterator.DefinitionIterator(map.values().iterator());
        }
        public SSAIterator.DefinitionIterator getAllReaching(SSADefinition def){
            HashSet set = new HashSet();
            
            (new Object(){
                Set getReachingAux(SSADefinition def, Set set){
                    set.add(def);
                    Iterator iter = getReaching(def);
                    if(iter != null){
                        while(iter.hasNext()){
                            SSADefinition def2 = (SSADefinition)iter.next();
                            
                            getReachingAux(def2, set);
                        }            
                    }            
                    
                    return set; 
                }
            }).getReachingAux(def, set);            // fill the set with reaching definitions
            
            return new SSAIterator.DefinitionIterator(set.iterator());
        }                
    }
}
