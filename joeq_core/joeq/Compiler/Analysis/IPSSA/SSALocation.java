package joeq.Compiler.Analysis.IPSSA;

import java.util.HashMap;
import joeq.Class.jq_LocalVarTableEntry;
import joeq.Class.jq_Method;
import joeq.Class.jq_NameAndDesc;
import joeq.Compiler.Analysis.IPA.PA;
import joeq.Compiler.Quad.CodeCache;
import joeq.Compiler.Quad.Quad;
import joeq.Compiler.Quad.RegisterFactory;
import jwutil.util.Assert;

/**
 * @author V.Benjamin Livshits
 * @see joeq.Compiler.Analysis.IPSSA.SSADefinition
 * @see joeq.Compiler.Analysis.IPSSA.SSABinding
 * @version $Id: SSALocation.java,v 1.10 2004/09/22 22:17:33 joewhaley Exp $
 */
public interface SSALocation {

    /**
     *     We need to have "abstract" temporary locations for IPSSA construction purposes 
     * that do not necessarily correspond to anything tangible. 
     * */
    public static class Temporary implements SSALocation {
        private Temporary() {
            // there's no underlying node
        }

        // There's only one Temporary location -- use the FACTORY to retrieve it    
        public static class FACTORY {
            private static Temporary INSTANCE = new Temporary();
            
            public static Temporary get() {
                return INSTANCE;
            }
        }

        public String toString(PA pa) {
            return null;
        }
        
        public String toString() {
            return "temp";
        }
    }
    
    public static class Unique implements SSALocation {
        private static long _count = 0;
        private long _id;
        
        private Unique(long id) {
            this._id = id;
        }

        // There's only one Temporary location -- use the FACTORY to retrieve it    
        public static class FACTORY {
            public static Unique get() {
                return new Unique(_count++);
            }
        }

        public String toString(PA pa) {
            return null;
        }
        
        public String toString() {
            return "uniq" + _id;
        }
    }

    String toString(PA pa);
        
    /**
     * These locations represent local variables.
     * */
    class LocalLocation implements SSALocation {    
        private RegisterFactory.Register _reg;
        private String _name = null;

        public static class FACTORY {
            static HashMap _locationMap = new HashMap();
            public static LocalLocation createLocalLocation(RegisterFactory.Register reg){
                LocalLocation loc = (LocalLocation) _locationMap.get(reg); 
                if(loc == null){
                    loc = new LocalLocation(reg);
                    _locationMap.put(reg, loc);
                }
                return loc;
            }
        }
        public RegisterFactory.Register getRegister(){
            return this._reg;
        }
        private LocalLocation(RegisterFactory.Register reg){
            Assert._assert(reg != null);
            this._reg = reg;
        }    
        public String toString(PA pa) {
            return toString();
        }
    
        // Looking at jq_Method.getLocalVarTableEntry( line number, register number ) may provide better output
        public String toString() {
            return _reg.toString();
        }
        public String getName(jq_Method method, Quad quad) {
            if(_reg.isTemp()) {
                return null;
            }
            if(_name != null) {
                return _name;   // a bit of caching
            }
            Integer i = ((Integer)CodeCache.getBCMap(method).get(quad));
            if(i == null) {
                return null;
            }
            int bci = i.intValue();            
            jq_LocalVarTableEntry entry = method.getLocalVarTableEntry(bci, _reg.getNumber());
            if(entry == null) {
                return null;
            }
            jq_NameAndDesc nd = entry.getNameAndDesc();
            if(nd == null) {
                return null;
            }
            
            _name = nd.getName().toString();
            return _name;
        }
    }
}

