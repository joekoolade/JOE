package joeq.Compiler.Analysis.IPSSA;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Vector;
import joeq.Class.jq_Method;
import joeq.Compiler.Analysis.IPSSA.SSAIterator.DefinitionIterator;
import joeq.Compiler.Analysis.IPSSA.Utils.DefinitionSet;
import joeq.Compiler.Analysis.IPSSA.Utils.IteratorHelper;
import joeq.Compiler.Quad.Quad;
import jwutil.util.Assert;

/**
 * The RHS of a binding. Has multiple subclasses.
 * @see joeq.Compiler.Analysis.IPSSA.SSADefinition
 * @version $Id: SSAValue.java,v 1.13 2004/09/22 22:17:33 joewhaley Exp $
 * */
public abstract class  SSAValue {
    protected SSADefinition _destination;
    
    public SSADefinition getDestination(){
        return _destination;
    }
    
    void setDestination(SSADefinition def){
        _destination = def;
    }
    
    public Quad getQuad() {
        return getDestination().getQuad();
    }
    
    public abstract SSAIterator.DefinitionIterator getUsedDefinitionIterator();
    
    public abstract String toString();
        
    /**
     * This value is just a reference to a definition.
     * TODO: do we still have copies in the reduced representation?..
     * */
    public static class Copy extends SSAValue {
        SSADefinition _definition;

        public static class FACTORY {
            // TODO: maybe add caching of these to increase sharing?..
            static Copy create_copy(SSADefinition def){
                return new Copy(def);
            }
        }
        
        private Copy(SSADefinition def){
            this._definition = def;
            
            def.appendUse(this);
        }
        
        public SSAIterator.DefinitionIterator getUsedDefinitionIterator(){
            return new SSAIterator.DefinitionIterator(new IteratorHelper.SingleIterator(_definition));
        }
        
        public SSADefinition getDefinition(){
            return _definition;
        }

        public String toString() {
            return "(" + _definition + ")";
        }        
    }
    
    public static class Alloc extends SSAValue {
        Quad _quad;
        private Alloc(Quad quad) {
            this._quad = quad;
        }
        
        public static class FACTORY {
            public static Alloc createAlloc(Quad quad) {
                return new Alloc(quad);
            }
        }

        public DefinitionIterator getUsedDefinitionIterator() {
            return new SSAIterator.DefinitionIterator(IteratorHelper.EmptyIterator.FACTORY.get());
        }

        public String toString() {
            return "Alloc @ " + _quad;
        }
    }


    public static abstract class Terminal extends SSAValue {}
    
    public static abstract class Constant extends Terminal {
        public SSAIterator.DefinitionIterator getUsedDefinitionIterator(){
            return new SSAIterator.DefinitionIterator(IteratorHelper.EmptyIterator.FACTORY.get());
        }
    }
    
    public static class UnknownConstant extends Constant {
        /** Use UnknownContant.FACTORY */
        private UnknownConstant(){}
        
        public static class FACTORY {
            static UnknownConstant _sample = null;                    
            public static UnknownConstant create_unknown_contant(){
                if(_sample == null){
                    _sample = new UnknownConstant();
                }
                return _sample;                
            }
        }
        
        public String toString(){return "<Unknown>";}
    }
    
    public static class NullConstant extends Constant {
        /** Use NullContant.FACTORY */
        private NullConstant(){}

        public static class FACTORY {
            static NullConstant _sample = null;                    
            public static NullConstant create_null_contant(){
                if(_sample == null){
                    _sample = new NullConstant();
                }
                return _sample;                
            }
        }
        public String toString(){return "<Null>";}
    }
    
    public static abstract class Normal extends Terminal {
        // TODO: this may contain arbitrary expressions
        public abstract SSAIterator.DefinitionIterator getUsedDefinitionIterator();
    }
    
    
    public static class UseCollection extends Normal {
        DefinitionSet _usedDefinitions;
        
        private UseCollection() {
            _usedDefinitions = new DefinitionSet();
        }
        
        public static class FACTORY {
            static UseCollection createUseCollection() {
                return new UseCollection();
            }
        }

        public DefinitionIterator getUsedDefinitionIterator() {
            return _usedDefinitions.getDefinitionIterator();
        }

        public void addUsedDefinition(SSADefinition def) {
            _usedDefinitions.add(def);
            def.appendUse(this);            
        }
        
        public String toString() {
            StringBuffer buf = new StringBuffer("{ ");
            for(SSAIterator.DefinitionIterator iter = getUsedDefinitionIterator(); iter.hasNext(); ) {
                SSADefinition def = iter.nextDefinition();
                buf.append(def.toString());
                buf.append(" ");
            }
            buf.append("}");
            
            return buf.toString();            
        }        
    }
    
    public static abstract class Phi extends  SSAValue {
        protected Vector/*<SSADefinition>*/         _definitions          = new Vector();
        protected LinkedHashSet /*<SSADefinition>*/ _usedDefinitions     = new LinkedHashSet(); 
        
        public int getDefinitionCount(){
            return _definitions.size();
        }
        public SSADefinition getDefinition(int pos){
            return (SSADefinition)_definitions.get(pos);
        }
        public Iterator/*<SSADefinition>*/ getDefinitionIterator(){
            return _definitions.iterator();
        }
        
        public SSAIterator.DefinitionIterator getUsedDefinitionIterator(){
            return new SSAIterator.DefinitionIterator(_usedDefinitions.iterator());
        }
        
        abstract public String getLetter();
        
        public String toString(){
            String result = getLetter() + "(";
            for(int i = 0; i < _definitions.size(); i++){
                SSADefinition def = getDefinition(i);
                
                result += def.toString() + ", ";
            }
            if(_definitions.size()>0){
                result = result.substring(0, result.length() - 2);
            }
            
            return result + ")";
        }
    }
    
    /**
     *     The representation of predicates is yet to be determined. It's currently pretty lame.
     * */
    public static class Predicate {
        private String _predicate;
        public static String UNKNOWN = "<unknown>";

        public Predicate(String predicate){
            this._predicate = predicate;
        }
        public String toString(){
            return _predicate;
        }
        public static Predicate True() {
            return null;        // TODO
        }
    }
    
    public static abstract class Predicated extends Phi {
        protected Vector/* <SSAPredicate> */ _predicates = new Vector();            
        
        public Predicate getPredicate(int pos){
            return (Predicate)_predicates.get(pos);
        }
        
        public void add(SSADefinition def, String predicate){
            _definitions.addElement(def);
            _predicates.addElement(predicate);
            
            def.appendUse(this);
            _usedDefinitions.add(def);
        }
        
        public String toString(){
            Assert._assert(_predicates.size() == _definitions.size());
            String result = getLetter() + "(";
            for(int i = 0; i < _definitions.size(); i++){
                SSADefinition def = getDefinition(i);
                Predicate pred = getPredicate(i);
        
                if(pred == null){ 
                    result += "<" + def + ">, ";
                }else{
                    result += "<" + def + ", " + pred.toString() + ">, ";
                }
            }
            if(_definitions.size() > 0){
                result = result.substring(0, result.length() - 2);
            }
    
            return result + ")";
        }        
    }
        
    /**
     * This represents a merge of definitions but without any further 
     * information such as predicates. 
     * */
    public static class OmegaPhi extends Phi {
        public String getLetter(){return "omega";}
        
        public void addUsedDefinition(SSADefinition def){
            _definitions.addElement(def);
            
            def.appendUse(this);
            _usedDefinitions.add(def);
        }
    }
    
    public static class SigmaPhi extends Phi {
        private ContextSet _context;
        
        public SigmaPhi(ContextSet context, SSADefinition newDef, SSADefinition oldDef){
            setContext(context);
            _definitions.add(newDef);
            _definitions.add(oldDef);
            
            _usedDefinitions.add(newDef);
            _usedDefinitions.add(oldDef);
            
            oldDef.appendUse(this);
            newDef.appendUse(this);
        }
        public String getLetter(){return "sigma";}

        protected void setContext(ContextSet _context) {
            this._context = _context;
        }
        protected ContextSet getContext() {
            return _context;
        }
    }

    public static class Gamma extends Predicated {
        public String getLetter(){return "gamma";}    
    }
        
    public static abstract class IPPhi extends Phi {

    }
    
    public static class FormalIn extends IPPhi {
        protected Vector/*<Quad>*/ _callers;
        
        FormalIn(){
            _callers = new Vector();
        }
        
        Quad getCaller(int pos){
            return (Quad)_callers.get(pos); 
        }
        void add(SSADefinition def, Quad caller){
            _definitions.addElement(def);
            _callers.addElement(caller);
            
            _usedDefinitions.add(def);
            def.appendUse(this);
        }
        public String getLetter(){return "iota";}
        public String toString(){
            String result = getLetter() + "(";
            for(int i = 0; i < _definitions.size(); i++){
                SSADefinition def = getDefinition(i);
                Quad caller = getCaller(i);

                result += "<" + def.toString() + ", " + caller + ">, ";
            }
            if(_definitions.size()>0){
                result = result.substring(0, result.length() - 2);
            }

            return result + ")";
        }

        public boolean hasCallSite(Quad quad) {
            return _callers.contains(quad);
        }
    }
    
    public static class ActualOut extends IPPhi {
        protected Vector/*<jq_Method>*/ _callees;
        
        ActualOut(){
            _callees = new Vector();
        }
        
        jq_Method getCallee(int pos){
            return (jq_Method)_callees.get(pos); 
        }
        void add(SSADefinition def, jq_Method method){
            _definitions.addElement(def);
            _callees.addElement(method);
            
            _usedDefinitions.add(def);
            def.appendUse(this);
        }
        public String getLetter(){return "rho";}
        
        public String toString(){
            String result = getLetter() + "(";
            for(int i = 0; i < _definitions.size(); i++){
                SSADefinition def = getDefinition(i);
                jq_Method method = getCallee(i);

                result += "<" + def.toString() + ", " + method + ">, ";
            }
            if(_definitions.size()>0){
                result = result.substring(0, result.length() - 2);
            }

            return result + ")";
        }
    }
}

