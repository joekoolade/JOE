package joeq.Compiler.Analysis.IPSSA.Apps;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Class;
import joeq.Class.jq_InstanceMethod;
import joeq.Class.jq_Method;
import joeq.Class.jq_Type;
import joeq.Compiler.Quad.BasicBlock;
import joeq.Compiler.Quad.CodeCache;
import joeq.Compiler.Quad.ControlFlowGraph;
import joeq.Compiler.Quad.Operand;
import joeq.Compiler.Quad.Operator;
import joeq.Compiler.Quad.Quad;
import joeq.Compiler.Quad.RegisterFactory;
import joeq.Main.HostedVM;
import joeq.Util.Templates.List;
import joeq.Util.Templates.ListIterator;
import jwutil.collections.AppendIterator;
import jwutil.collections.HashWorklist;
import jwutil.collections.Worklist;
import jwutil.util.Assert;

public class FindOwnership {
    static class SimpleOwnershipFinder implements Runnable {
        private Collection _classes;
        //private CallGraph _cg;        
        private Map procValue = new HashMap();
        
        private boolean _verbose = false;
        
        public SimpleOwnershipFinder(Iterator classIter){
            _classes = new LinkedList();
            Collection roots = new LinkedList();
            
            while(classIter.hasNext()) {
                jq_Class c = (jq_Class) jq_Type.parseType((String)classIter.next());
                c.load();
                _classes.add(c);
                roots.addAll(Arrays.asList(c.getDeclaredStaticMethods()));
            }
            
            /* 
             _cg = new RootedCHACallGraph();
            _cg.setRoots(roots);
            */
        }
        
        public static void main(String[] args) {
            HostedVM.initialize();
            CodeCache.AlwaysMap = true;
            ///initPredefinedClasses();
            //ClassAndMethod.initializeClasses();
            
            Iterator i = null;
            for (int x=0; x<args.length; ++x) {
                if (args[x].equals("-file")) {
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(args[++x]));
                        LinkedList list = new LinkedList();
                        for (;;) {
                            String s = br.readLine();
                            if (s == null) break;
                            if (s.length() == 0) continue;
                            if (s.startsWith("%")) continue;
                            if (s.startsWith("#")) continue;
                            list.add(s);
                        }
                        i = new AppendIterator(list.iterator(), i);
                    }catch(IOException e) {
                        e.printStackTrace();
                        System.exit(2);
                    }
                    
                } else
                    if (args[x].endsWith("*")) {
                        i = new AppendIterator(PrimordialClassLoader.loader.listPackage(args[x].substring(0, args[x].length()-1)), i);
                    } else 
                        if(args[x].charAt(0) == '-'){
                            System.exit(2);                    
                        }else {
                            String classname = args[x];
                            i = new AppendIterator(Collections.singleton(classname).iterator(), i);
                        }
            }

            SimpleOwnershipFinder finder = new SimpleOwnershipFinder(i);
            finder.run();
        }
        
        public void run() {
            for(Iterator iter = _classes.iterator(); iter.hasNext();) {
                jq_Class c = (jq_Class)iter.next();
                                
                processClass(c);
            }
        }

        private void processClass(jq_Class c) {
            System.out.println("Processing class " + c);
            jq_InstanceMethod[] methods = c.getDeclaredInstanceMethods();
            for(int i = 0; i < methods.length; i++) {
                jq_InstanceMethod m = methods[i];
                
                if(m.isInitializer()) {
                    computeInitializations(m);                    
                }
            }
        }
        
        void computeInitializations(jq_Method m){
            if(_verbose) System.out.println("Processing constructor " + m);
            
            ControlFlowGraph cfg = CodeCache.getCode(m);
            // Collection/*jq_Method*/ targets = _cg.getCallees(m);
            Worklist/*BasicBlock*/ worklist = new HashWorklist();
            worklist.push(cfg.entry());
            Map valueAtEnd = new HashMap();
            // initialize the parameters as unowned
            OwnershipValue initValue = getInitValue(m);

            worklist.push(cfg.entry());
            while(!worklist.isEmpty()) {
                BasicBlock b = (BasicBlock)worklist.pull();
                OwnershipValue predValue = null;
                if(b.getNumberOfPredecessors() > 0) {
                    // merge node -- merge the values at predecessors
                    List.BasicBlock l = b.getPredecessors();
                    for(joeq.Util.Templates.ListIterator.BasicBlock i = l.basicBlockIterator(); i.hasNext();) {
                        BasicBlock pred = i.nextBasicBlock();
                        OwnershipValue v_pred = (OwnershipValue)valueAtEnd.get(pred);
                        if(v_pred != null) {
                            if(predValue == null) {
                                predValue = new OwnershipValue(v_pred);
                                //changed = true;
                            }else {
                                OwnershipValue.meet(predValue, v_pred); 
                            }
                        }
                    }
                    //System.err.println("predValue for " + b + " is " + predValue);
                }else
                if(b.getNumberOfPredecessors() == 0) {
                    // first Basic Block
                    predValue = initValue;
                }
                
                OwnershipValue v_b = processBlock(m, b, predValue);
                OwnershipValue v_b_old = (OwnershipValue)valueAtEnd.get(b);
                boolean changed = false;
                if(v_b_old != null) {
                    changed |= OwnershipValue.meet(v_b, v_b_old);
                }else{
                    changed = true;
                    valueAtEnd.put(b, v_b);                    
                }
                
                if(changed) {
                    // enlist the successors
                    List.BasicBlock l = b.getSuccessors();
                    for(joeq.Util.Templates.ListIterator.BasicBlock i = l.basicBlockIterator(); i.hasNext();) {
                        BasicBlock succ = i.nextBasicBlock();
                        if(!worklist.contains(succ)) {
                            worklist.push(succ);
                        }
                    }
                }
            }
            
            procValue.put(m, valueAtEnd.get(cfg.exit()));
            System.out.println(cutto("\tValue for " + m + ": ", 50) + getExitValue(m));
        }
        
        private OwnershipValue getExitValue(jq_Method m) {
            return (OwnershipValue)procValue.get(m);
        }

        private OwnershipValue getInitValue(jq_Method m) {
            OwnershipValue result = new OwnershipValue();
            //RegisterFactory rf = CodeCache.getRegisterFactory(m);
            //System.out.println("Method: " + m + "; " + rf);
            
            /*for(Iterator iter = rf.iterator(); iter.hasNext();) {
                RegisterFactory.Register reg = (Register)iter.next();
                if(!reg.isTemp()) {
                    System.out.println("\t" + reg);
                }
            }
            */            
            return null;
        }

        OwnershipValue processBlock(jq_Method m, BasicBlock block, OwnershipValue predValue) {
            // start with the input value
            OwnershipValue result = (predValue != null) ? new OwnershipValue(predValue) : new OwnershipValue();
            for(ListIterator.Quad qiter = block.iterator(); qiter.hasNext();) {
                Quad q = qiter.nextQuad();
                //System.out.println("\tProcessing " + q);
                                
                if(q.getOperator() instanceof Operator.New){
                    if(_verbose) System.out.println("Processing initialization " + q);
                    Operand.RegisterOperand o = Operator.New.getDest(q);
                    jq_Type type = Operator.New.getType(q).getType();
                    
                    result.addValue(o.getRegister(), new OwnershipLattice(OwnershipLattice.OWNED, type));
                }else
                if(q.getOperator() instanceof Operator.Move){
                    joeq.Util.Templates.ListIterator.RegisterOperand uiter = q.getUsedRegisters().registerOperandIterator();
                    joeq.Util.Templates.ListIterator.RegisterOperand diter = q.getDefinedRegisters().registerOperandIterator();
                    
                    if(!uiter.hasNext() || !diter.hasNext()) continue;
                    Operand.RegisterOperand use = (Operand.RegisterOperand)uiter.nextOperand();
                    Operand.RegisterOperand def = (Operand.RegisterOperand)diter.nextOperand();
                    if(_verbose) System.err.println("Processing move from " + use + " to " + def);
                    
                    if(result.hasValue(use.getRegister())) {
                        // copy the type of the use to the type of the definition 
                        result.putValue(def.getRegister(), result.getValue(use.getRegister()));                        
                    } else {
                        result.putValue(def.getRegister(), new OwnershipLattice(OwnershipLattice.UNOWNED));
                    }
                    if(_verbose) System.err.println("Saved data for " + def);
                }else
                if(q.getOperator() instanceof Operator.Putfield) {
                    if(_verbose) System.out.println("Processing store: " + q);
                    
                    joeq.Util.Templates.ListIterator.RegisterOperand uiter = q.getUsedRegisters().registerOperandIterator();
                    uiter.nextOperand();
                    Operand.RegisterOperand use = (Operand.RegisterOperand)uiter.nextOperand();
                    
                    if(result.hasValue(use.getRegister())) {
                        result.putValue(Operator.Putfield.getField(q).getField(), result.getValue(use.getRegister()));    
                    }else {
                        if(_verbose) System.err.println("No data for RHS of a store: " + q + "( " + use + " ), result: " + result);
                        result.putValue(Operator.Putfield.getField(q).getField(), new OwnershipLattice(OwnershipLattice.UNOWNED));
                    }                    
                }else
                if(q.getOperator() instanceof Operator.Invoke) {
                    if(_verbose) System.out.println("Processing call: " + q);
                    
                    jq_Method callee = Operator.Invoke.getMethod(q).getMethod();
                    OwnershipValue value = (OwnershipValue)procValue.get(callee);
                    if(value != null) {
                        OwnershipValue.meet(result, value);
                    }                    
                }
            }
            
            result = result.removeRegisters();
            
            if(_verbose) System.err.println("result for " + block + result);
            
            return result;
        }
        
        /**
         * Lattice of values -- types go to values.
         * */
        static class OwnershipValue {
            HashMap/*<Object, LatticeValue>*/ _values = new HashMap();
            
            OwnershipValue(){}
            
            public OwnershipValue removeRegisters() {
                OwnershipValue result = new OwnershipValue();
                for(Iterator iter = _values.keySet().iterator(); iter.hasNext();) {
                    Object key = iter.next();
                    
                    if(!(key instanceof RegisterFactory.Register)) {
                        result.addValue(key, getValue(key));
                    }
                }
                return result;
            }
            public void putValue(Object o, OwnershipLattice value) {
                _values.put(o, value);
            }
            public OwnershipLattice getValue(Object o) {
                return (OwnershipLattice)_values.get(o);
            }
            public boolean hasValue(Object o) {
                return _values.containsKey(o);
            }

            OwnershipValue(OwnershipValue that){
                for(Iterator iter = that._values.keySet().iterator(); iter.hasNext();) {
                    Object o  = iter.next();
                    OwnershipLattice value = (OwnershipLattice)that._values.get(o);
                    Assert._assert(value != null);
                    
                    addValue(o, value);
                }
            }
            
            static boolean meet(OwnershipValue This, OwnershipValue That) {
                boolean changed = false;
                OwnershipValue result = new OwnershipValue(This);
                for(Iterator iter = That._values.keySet().iterator(); iter.hasNext(); ) {
                    Object o = iter.next();
                    
                    if(!This._values.containsKey(o)) {
                        changed = true;
                        OwnershipLattice t = (OwnershipLattice)That._values.get(o);
                        Assert._assert(t != null);
                        This._values.put(o, t);
                    }
                }
                
                return changed;
            }
            
            void addValue(Object o, OwnershipLattice value) {                
                _values.put(o, value);
            }
            
            public String toString() {
                return _values.toString();
            }
        }
        
        static class OwnershipLattice {
            static final String OWNED   = "OWNED";
            static final String UNOWNED = "UNOWNED";
            
            private String  _value;
            private jq_Type _type;
            
            OwnershipLattice(String value, jq_Type type){
                _value = value;
                _type = type;
            }            
            OwnershipLattice(String value){
                this(value, null);
            }
            
            public String toString() {return _value;}
        }
    }
    private static String cutto(String string, int to) {
        return string.length() < to ? 
                                     string + repeat(" ", to - string.length()) : 
                                         string.substring(0, to - 3) + "..."; 
    }
    private static String repeat(String string, int to) {
        StringBuffer result = new StringBuffer();
        for(int i = 0; i < to; i++) {
            result.append(string);  
        }
        
        return result.toString();
    }
}
