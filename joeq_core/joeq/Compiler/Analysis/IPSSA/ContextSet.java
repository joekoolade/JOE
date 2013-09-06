package joeq.Compiler.Analysis.IPSSA;


public class ContextSet {
    // TODO: fill in the details of the representation
    
    public class ContextLocationPair {
        protected SSALocation _location; 
        protected ContextSet  _context;
    
        public SSALocation getLocation(){return _location;}
        public ContextSet  getContext(){return _context;}
    }
}

