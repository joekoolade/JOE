// PointerAnalysisResults.java, created Mon Sep 22 17:38:25 2003 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.Analysis.IPA;

import java.util.Set;
import joeq.Class.jq_Method;
import joeq.Compiler.Analysis.IPA.ProgramLocation.QuadProgramLocation;
import joeq.Compiler.Analysis.IPSSA.ContextSet;
import joeq.Compiler.Analysis.IPSSA.SSALocation;
import joeq.Compiler.Quad.BasicBlock;
import joeq.Compiler.Quad.CallGraph;

/**
 * This interface summarizes all the relevant results of the 
 * external pointer analysis in one convenient place.
 * 
 * @author Vladimir Livshits
 * @author John Whaley
 * @version $Id: PointerAnalysisResults.java,v 1.12 2004/09/22 22:17:30 joewhaley Exp $
 */
public interface PointerAnalysisResults {
    
    /*
     * This interface summarizes all the relevant results of the 
     * external pointer analysis in one convenient place.
     * 
     * The following things are necessary:
     */
     
    //-------------- 1. Transitive MOD and REF sets for each call       --------------//
    
    /** Returns the set of potentially-modified locations of the
     * given call (and transitively any calls the target may make).  Each
     * location is represented by an SSALocation.
     * 
     * a.f = b;  everything that 'a.f' can point to
     * 
     * loc must be in the basic block
     */
    Set/*<SSALocation>*/ mod(QuadProgramLocation loc, BasicBlock bb);
    
    /**
     *  Returns the set of potentially-referenced locations of the
     * given call (and transitively any calls the target may make).  Each
     * location is represented by an SSALocation.
     * 
     * a = b.f;  everything that 'b.f' can point to
     * 
     * loc must be in the basic block
     */
    Set/*<SSALocation>*/ ref(QuadProgramLocation loc, BasicBlock block);
    
    
    //-------------- 2. Aliasing of parameters                          --------------//
    
    /**
     * Returns a set of location/contextset pairs of locations that may be
     * aliased with the given location, along with the set of contexts under
     * which each alias can occur.
     */
    Set/*<ContextSet.ContextLocationPair>*/ getAliases(jq_Method method, SSALocation loc);
    
    /**
     * Returns whether the given location may have aliases in the given set of
     * contexts.
     */
    boolean hasAliases(jq_Method method, SSALocation loc, ContextSet contextSet);
    
    /**
     * Returns whether the given location may have aliases in any context.
     */
    boolean hasAliases(jq_Method method, SSALocation loc);
    
    PA getPAResults();

    Set/*jq_Method*/ getCallTargets(QuadProgramLocation loc);

    CallGraph getCallGraph();
}
