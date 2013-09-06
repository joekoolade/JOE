package joeq.Compiler.Analysis.IPSSA.Utils;

import java.util.StringTokenizer;
import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Class;
import joeq.Class.jq_Method;
import joeq.Compiler.Analysis.FlowInsensitive.MethodSummary.Node;
import joeq.Compiler.Analysis.IPA.PAResultSelector;
import joeq.Compiler.Analysis.IPSSA.IPSSABuilder;
import joeq.Compiler.Analysis.IPSSA.SSADefinition;
import jwutil.util.Assert;
import net.sf.javabdd.TypedBDDFactory.TypedBDD;

/**
 * Allows referring to analysis objects in a simple textual fashion.
 * Implementation provide parsing facilities.
 * @see AnalysisObjectSpec.PAObjectSpec
 * @see AnalysisObjectSpec.IPSSAObjectSpec
 * @version $Id: AnalysisObjectSpec.java,v 1.8 2005/04/29 07:39:00 joewhaley Exp $
 * */
public abstract class AnalysisObjectSpec {
    /** Some pre-defined object types. Implementations provide more. */
    static final String RETURN = "return";
    static final String PARAM  = "param";                       

    String _type;
    String[] _args;

    private AnalysisObjectSpec(String type, String[] args){
        _type = type;
        _args = (String[])args.clone();
    }                

    public static class PAObjectSpec extends AnalysisObjectSpec {
        TypedBDD _bdd;
        PAResultSelector _sel;
                
        PAObjectSpec(PAResultSelector sel, String type, String args[]){
            super(type, args);
            
            _sel  = sel;
            _bdd = null;
        }
        public static PAObjectSpec create(PAResultSelector sel, String line) {
            // parse the object
            StringTokenizer tok = new StringTokenizer(line, " ");
            Assert._assert(tok.hasMoreTokens());
            String type = tok.nextToken();
            String[] args = new String[tok.countTokens()];
            for(int i = 0; tok.hasMoreTokens(); i++) {
                args[i] = tok.nextToken();
            }
                    
            return new PAObjectSpec(sel, type, args);
        }
                
        public TypedBDD getBDD() throws UnknownAnalysisObjectExeption {
            if(_bdd != null) {
                return _bdd;
            }
                    
            // different depending on type
            if(_type.equals(RETURN)) {
                jq_Method method = getMethodByName(_args[0], _args[1]);
                        
                _bdd = _sel.getReturnBDD(method); 
            }else
            if(_type.equals(PARAM)) {
                jq_Method method = getMethodByName(_args[0], _args[1]);
                int paramIndex = Integer.parseInt(_args[2]);
                _bdd = _sel.getFormalParamBDD(method, paramIndex);
            }else {
                throw new UnknownAnalysisObjectExeption("Unknown type " + _type);
            }
                    
            return _bdd;
        }
                
         public Node getNode() throws UnknownAnalysisObjectExeption  {
            return _sel.getNode(getBDD());
        }
                
        public String toString() {
            return _type + " [" + _args.toString() + "]";
        }
    }
    
    public static class IPSSAObjectSpec extends AnalysisObjectSpec {
        static final String DEFINITION = "definition";
        
        SSADefinition _definition;
        IPSSABuilder _builder;
        
        IPSSAObjectSpec(IPSSABuilder builder, String type, String args[]){
            super(type, args);
            
            _builder = builder;            
            _definition = null;
        }
        
        public static IPSSAObjectSpec create(IPSSABuilder builder, String line) {
            // parse the object
            StringTokenizer tok = new StringTokenizer(line, " ");
            Assert._assert(tok.hasMoreTokens());
            String type = tok.nextToken();
            
            String[] args = new String[tok.countTokens()];
            for(int i = 0; tok.hasMoreTokens(); i++) {
                args[i] = tok.nextToken();
            }
                    
            return new IPSSAObjectSpec(builder, type, args);
        }
        
        public SSADefinition getDefinition() throws UnknownAnalysisObjectExeption {
            if(_definition != null) {
                return _definition;
            }
            if(_type.equals(RETURN)) {
                _definition = null;
            }else
            if(_type.equals(PARAM)) {
                _definition = null;
            }else
            if(_type.equals(DEFINITION)) {
                _definition = SSADefinition.Helper.lookupDefinition(_args[0]);
                if(_definition == null) {
                    throw new UnknownAnalysisObjectExeption("Can't find definition " + _args[0]);
                }
            }else {
                throw new UnknownAnalysisObjectExeption("Unknown type " + _type);
            }                  
            
            return _definition;  
        }
    }
    
    public static class UnknownAnalysisObjectExeption extends Exception {               
        /**
         * Version ID for serialization.
         */
        private static final long serialVersionUID = 3762257417945362481L;

        public UnknownAnalysisObjectExeption(String msg) {             
             super(msg);
         }
    }
    
    static jq_Method getMethodByName(String className, String methodName) {   
        if (className.endsWith(".properties")) return null;
        if (className.endsWith(".class")) className = className.substring(0, className.length()-6);
        String classdesc = "L"+className+";";
        jq_Class c = (jq_Class)PrimordialClassLoader.loader.getOrCreateBSType(classdesc);
        //System.err.println("Preparing class " + c);
        c.prepare();
            
        return c.getDeclaredMethod(methodName);
    }    
}

