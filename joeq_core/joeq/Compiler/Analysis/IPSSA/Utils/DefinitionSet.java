/*
 * Created on Dec 4, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package joeq.Compiler.Analysis.IPSSA.Utils;

import java.util.HashSet;
import joeq.Compiler.Analysis.IPSSA.SSAIterator;

/**
 * @author V.Benjamin Livshits
 * @version $Id: DefinitionSet.java,v 1.8 2005/04/29 07:39:00 joewhaley Exp $
 * 
 * Strongly typed definition set.
 */
public class DefinitionSet extends HashSet {
    /**
     * Version ID for serialization.
     */
    private static final long serialVersionUID = 3258130267196831288L;
    
    public DefinitionSet(){
        super();
    }
    public SSAIterator.DefinitionIterator getDefinitionIterator(){
        return new SSAIterator.DefinitionIterator(iterator());
    }
}
