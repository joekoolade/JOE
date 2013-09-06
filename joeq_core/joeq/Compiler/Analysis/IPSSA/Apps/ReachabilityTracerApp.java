/*
 * Created on Dec 8, 2003
 */
package joeq.Compiler.Analysis.IPSSA.Apps;

import java.util.Collection;
import java.util.Iterator;
import joeq.Class.jq_Method;
import joeq.Compiler.Analysis.IPA.PAResultSelector;
import joeq.Compiler.Analysis.IPA.PAResults;
import joeq.Compiler.Analysis.IPSSA.IPSSABuilder;
import joeq.Compiler.Analysis.IPSSA.SSADefinition;
import joeq.Compiler.Analysis.IPSSA.Utils.AnalysisObjectSpec;
import joeq.Compiler.Analysis.IPSSA.Utils.ReachabilityTrace;
import joeq.Compiler.Analysis.IPSSA.Utils.AnalysisObjectSpec.UnknownAnalysisObjectExeption;
import jwutil.util.Assert;
import net.sf.javabdd.TypedBDDFactory.TypedBDD;

/**
 * @author V.Benjamin Livshits
 * This is a sample application that prints all paths between two definitions.
 * Use one of the subclasses that rely on different sources for def-use data.
 * 
 * @see IPSSABuilder.Application
 * @version $Id: ReachabilityTracerApp.java,v 1.8 2004/10/16 04:11:52 joewhaley Exp $
 */
public abstract class ReachabilityTracerApp extends IPSSABuilder.Application {
    protected String _def1_str;
    protected String _def2_str;
    private static boolean _verbose = false;

    ReachabilityTracerApp(IPSSABuilder builder, String name, String[] args) {
        super(builder, name, args);
    }
    
    private static void usage(String[] argv) {
        System.err.print("Invalid parameters: ");
        for(int i = 0; i < argv.length; i++) {
            System.err.print(argv[i] + " ");
        }
        System.err.println("");     
        
        System.exit(1);   
    }

    /**
     * This will be added by implementations.
     * */
    protected abstract void printPath(String def1_str, String def2_str);

    protected void parseParams(String[] argv) {
        if(argv == null) return;
        if(argv.length < 2) usage(argv);
        StringBuffer buf = new StringBuffer(); 
        for(int i = 0; i < argv.length; i++) {
            String arg = argv[i];
            if(arg.equals("->")) {
                _def1_str = buf.toString();
                buf.setLength(0);                    
            }else{
                buf.append(arg);
                buf.append(" ");
            }                
        }
        _def2_str = buf.toString();        
    }
        
    public void run() {
        printPath(_def1_str, _def2_str);        
    }
    
    /**
     * This one uses PA results directly.
     * */
    public static class PAReachabilityTracerApp extends ReachabilityTracerApp {
        private PAResults _pa;

        public PAReachabilityTracerApp(){
             this(null, null, null);
        }
     
        PAReachabilityTracerApp(IPSSABuilder builder, String name, String[] args) {
            super(builder, name, args);
        }
        
        public void initialize() {
            //test();
        }        

        protected void printPath(String def1_str, String def2_str) {
            System.err.println("In printPath("+ def1_str+ ", " + def2_str + ")");
            Assert._assert(_builder != null);
            _pa = _builder.getPAResults();
            PAResultSelector sel = new PAResultSelector(_pa);
            
            TypedBDD src, dst;

            try {
                // return value node
                src = AnalysisObjectSpec.PAObjectSpec.create(sel, def1_str).getBDD();
                // parameter
                dst = AnalysisObjectSpec.PAObjectSpec.create(sel, def2_str).getBDD();
            } catch (AnalysisObjectSpec.UnknownAnalysisObjectExeption e) {
                System.err.println(e);
                return;
            }
            
            sel.collectReachabilityTraces(src, dst);            
        }
        
        /**
         * Get a BDD for the return node of method.
         * */                
        public void test() {
            Assert._assert(_builder != null);
            _pa = _builder.getPAResults();
            Assert._assert(_pa != null);
            PAResultSelector sel = new PAResultSelector(_pa);

            TypedBDD ret, param;

            try {
                // return value node
                ret = AnalysisObjectSpec.PAObjectSpec.create(sel, "return Main/TT getF").getBDD();
                // parameter
                param = AnalysisObjectSpec.PAObjectSpec.create(sel, "param Main/TT setF 1").getBDD();
            }catch(AnalysisObjectSpec.UnknownAnalysisObjectExeption e) {
                System.err.println(e);
                return;                
            }
           
            sel.collectReachabilityTraces(ret, param);
        }        
    }
    
    /**
     * This is one that works on IPSSA.
     * */
    public static class IPSSAReachabilityTracerApp extends ReachabilityTracerApp {
        public IPSSAReachabilityTracerApp(){
            this(null, null, null);
        }
        IPSSAReachabilityTracerApp(IPSSABuilder builder, String name, String[] args) {
            super(builder, name, args);
        }
    
        protected void printPath(String def1_str, String def2_str) {
            SSADefinition def1, def2;             
            System.err.println("In printPath("+ def1_str+ ", " + def2_str + ")");
            try {
                def1 = AnalysisObjectSpec.IPSSAObjectSpec.create(_builder, def1_str).getDefinition();                               
                def2 = AnalysisObjectSpec.IPSSAObjectSpec.create(_builder, def2_str).getDefinition();
            } catch (UnknownAnalysisObjectExeption e) {
                System.err.println(e);
                return;
            }

            Assert._assert(def1 != null); Assert._assert(def2 != null);

            jq_Method method1 = def1.getMethod();
            jq_Method method2 = def2.getMethod();
            if(_verbose) {
                System.err.println("Computing all paths between " + def1_str + " in " + method1 + " and " + def2_str + " in " + method2);
            }
        
            printPath(def1, def2);          
        }

        protected void printPath(SSADefinition def1, SSADefinition def2) {
            System.err.println("Calculating paths between " + def1 + " and " + def2);
            Collection/*ReachabilityTrace*/ traces = ReachabilityTrace.Algorithms.collectReachabilityTraces(def1, def2);
            for(Iterator iter = traces.iterator(); iter.hasNext(); ) {
                ReachabilityTrace trace = (ReachabilityTrace)iter.next();
                
                System.out.println("\t" + trace.toString());
            }                       
        }
    }
}
