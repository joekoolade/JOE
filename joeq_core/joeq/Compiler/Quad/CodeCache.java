// CodeCache.java, created Wed Jan 30 22:33:28 2002 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.Quad;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import joeq.Class.jq_Method;

/**
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: CodeCache.java,v 1.20 2006/04/06 02:32:10 mcmartin Exp $
 */
public class CodeCache {

    public static final CodeCache cache = new CodeCache();

    protected Map map = new HashMap();
    protected Map bcmap = new HashMap();

    public static List/*<ControlFlowGraphVisitor>*/ passes = new LinkedList();
    
    
    /** Creates new CodeCache */
    public CodeCache() { }

    public static ControlFlowGraph getCode(jq_Method m) { return cache._get(m); }
    public static Map getBCMap(jq_Method m) { return cache._getmap(m); }
    public static void free(ControlFlowGraph cfg) {
        if (getCode(cfg.getMethod()) == cfg)
            cache._delete(cfg.getMethod());
    }

    public static boolean TRACE = false;
    public static boolean AlwaysMap = false;
    
    public static void addDefaultPass(ControlFlowGraphVisitor pass) {
        passes.add(pass);
    }

    public static void clearDefaultPasses() {
        passes.clear();        
    }
    
    protected ControlFlowGraph _get(jq_Method m) {
        ControlFlowGraph cfg = (ControlFlowGraph)map.get(m);
        if (cfg == null) {
            if (TRACE) System.out.println("Generating quads for "+m);
            BytecodeToQuad b2q = new BytecodeToQuad(m);
            cfg = b2q.convert();
            map.put(m, cfg);
            if (AlwaysMap) {
                bcmap.put(m, b2q.getQuadToBytecodeMap());
            }
            for (java.util.Iterator i = passes.iterator(); i.hasNext(); ) {
                ControlFlowGraphVisitor v = (ControlFlowGraphVisitor)i.next();
                v.visitCFG(cfg);
            }
            //System.out.println("Done creating representation for " + m);            
        }
        //if(m.getName().toString().)
        return cfg;
    }

    protected java.util.Map _getmap(jq_Method m) {
        Map result = (Map) bcmap.get(m);
        if (result == null) {
            if (TRACE) System.out.println("Generating quads for "+m);
            BytecodeToQuad b2q = new BytecodeToQuad(m);
            ControlFlowGraph cfg = b2q.convert();
            map.put(m, cfg);
            result = b2q.getQuadToBytecodeMap();
            bcmap.put(m, result);
        }
        return result;
    }
    
    protected void _delete(jq_Method m) {
        map.remove(m);
        bcmap.remove(m);
    }
    
    public void invalidateCache() {
        map.clear();
        bcmap.clear();
    }
    
    static public void invalidate() {
        cache.invalidateCache();
    }

    public static void invalidateBCMap(jq_Method m) {
        cache._invalidateBCMap(m);        
    }

    private void _invalidateBCMap(jq_Method m) {
        if(bcmap.get(m) != null) {
            // System.err.println("Replacing " + m);
        }
        bcmap.put(m, null);        
    }
}
