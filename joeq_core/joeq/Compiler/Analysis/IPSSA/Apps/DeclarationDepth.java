package joeq.Compiler.Analysis.IPSSA.Apps;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Class;
import joeq.Class.jq_InstanceField;
import joeq.Class.jq_Method;
import joeq.Class.jq_Type;
import joeq.Compiler.Quad.CallGraph;
import joeq.Compiler.Quad.CodeCache;
import joeq.Compiler.Quad.RootedCHACallGraph;
import joeq.Main.HostedVM;
import jwutil.collections.AppendIterator;

public class DeclarationDepth {
    static class DeclarationDepthComputation implements Runnable {
        private Set _classes;
        LinkedList  worklist = new LinkedList();
        HashMap     values   = new HashMap(); 
        
        private boolean _verbose = false;
        
        public DeclarationDepthComputation(Iterator classIter){
            _classes = new HashSet();
            Collection roots = new LinkedList();
            
            while(classIter.hasNext()) {
                jq_Class c = (jq_Class) jq_Type.parseType((String)classIter.next());
                c.load();
                _classes.add(c);
                roots.addAll(Arrays.asList(c.getDeclaredStaticMethods()));
            }
            
            CallGraph cg = new RootedCHACallGraph();
            cg.setRoots(roots);
            for(Iterator iter = cg.getAllMethods().iterator(); iter.hasNext(); ) {
                jq_Class c = ((jq_Method)iter.next()).getDeclaringClass();
                _classes.add(c);
            }
            System.out.println("Processing a total of " + _classes.size() + " class(es)");
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

            DeclarationDepthComputation finder = new DeclarationDepthComputation(i);
            finder.run();
        }
        
        public void run() {
            worklist.addAll(_classes);
            boolean change = false;
            int iterCount = 0;
            do {
                change = false;
                for(Iterator iter = worklist.iterator(); iter.hasNext();) {
                    jq_Class c = (jq_Class)iter.next();
                                    
                    change |= processClass(c);
                }
                System.err.println("Done with the worklist, iteration # " + ++iterCount);
            } while(change && iterCount < 20);
         
            System.out.println("Results:");
            for(Iterator iter = values.keySet().iterator(); iter.hasNext(); ) {
                jq_Class c = (jq_Class) iter.next();
                Integer i = (Integer) values.get(c);
                System.out.println(cutto(c.toString(), 45) + " : " + i);
            }
        }

        private boolean processClass(jq_Class c) {
            //System.out.println("Processing class " + c);
            if(values.get(c) == null) values.put(c, new Integer(0));
            int oldValue = ((Integer)values.get(c)).intValue();
            
            int newValue = 0;
            jq_InstanceField[] fields = c.getDeclaredInstanceFields();
            for(int i = 0; i < fields.length; i++) {
                jq_InstanceField m = fields[i];
                if(! (m.getType() instanceof jq_Class) ) continue;
                
                jq_Class type = (jq_Class) m.getType();
                if(type == c) continue;
                
                if(values.get(type) == null) {
                    // no information for type -- initialize it
                    if(newValue < 1) newValue = 1; 
                } else {
                    int d = ((Integer)values.get(type)).intValue();
                    if(d+1 > newValue) newValue = d+1;  
                }
            }
            if(newValue != oldValue) {
                values.put(c, new Integer(newValue));
                System.err.println("Value for " + c + " changed to " + newValue);
                return true;
            } else {            
                return false;
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
}
