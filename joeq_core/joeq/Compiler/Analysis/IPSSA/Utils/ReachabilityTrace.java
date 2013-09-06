package joeq.Compiler.Analysis.IPSSA.Utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import joeq.Compiler.Analysis.IPSSA.SSADefinition;
import joeq.Compiler.Analysis.IPSSA.SSAIterator;
import joeq.Compiler.Analysis.IPSSA.SSAValue;

/**
 * Implements a trace of SSADefinitions.
 * @author V.Benjamin Livshits
 * @version $Id: ReachabilityTrace.java,v 1.7 2004/09/22 22:17:25 joewhaley Exp $
 * */
public class ReachabilityTrace implements Cloneable {
    LinkedList _definitions;
    
    ReachabilityTrace(){
        _definitions = new LinkedList();
    }
    
    SSAIterator.DefinitionIterator getDefinitionIterator(){
        return new SSAIterator.DefinitionIterator(_definitions.iterator());
    }
    
    public Object clone() {
        ReachabilityTrace result = new ReachabilityTrace();
        
        for(SSAIterator.DefinitionIterator iter = getDefinitionIterator(); iter.hasNext();) {
            result.appendDefinition(iter.nextDefinition());
        }
        
        return result;
    }
    
    public static class Algorithms {    
        public static Collection collectReachabilityTraces(SSADefinition def1, SSADefinition def2) {
            Collection result = new LinkedList();

            // do DF rechability and fill in result
            followTrace(new ReachabilityTrace(), def1, def2, new HashSet(), result);
                    
            return result;
        }
    }
    
    private static void followTrace(ReachabilityTrace trace, SSADefinition last, SSADefinition stop, Set seen, Collection result) {
        if(last == stop) {
            // terminate
            result.add(trace);
            return;
        }

        if(seen.contains(last)) {
            return;
        } else {
            seen.add(last);
        }
        trace.appendDefinition(last);
        Iterator iter = last.getUseIterator();
        if (iter.hasNext()) { 
            do {
                SSADefinition succ = ((SSAValue)iter.next()).getDestination();
                       
                ReachabilityTrace newTrace = (ReachabilityTrace)trace.clone();
                newTrace.appendDefinition(succ);
                System.err.println("Considering " + succ); 
                followTrace(newTrace, succ, stop, seen, result);
            } while(iter.hasNext());
        } else {
            System.err.println("Definition " + last + " is not used");
        }
    }
    
    void appendDefinition(SSADefinition def) {
        _definitions.addLast(def);
    }
    
    int size() {return _definitions.size();}
    
    public String toString() {
        StringBuffer result = new StringBuffer("[ ");
        for(Iterator iter = _definitions.iterator(); iter.hasNext(); ) {
            SSADefinition def = (SSADefinition)iter.next();
            result.append(def.toString());
            result.append(" ");
        }
        result.append("]");
     
        return result.toString();
    }
}

