package joeq.Compiler.Analysis.IPSSA.Apps;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Class;
import joeq.Class.jq_Field;
import joeq.Class.jq_Method;
import joeq.Class.jq_Type;
import joeq.Compiler.Analysis.IPA.PAResultSelector;
import joeq.Compiler.Analysis.IPSSA.IPSSABuilder;
import joeq.Compiler.Quad.CallGraph;
import joeq.Compiler.Quad.RootedCHACallGraph;
import joeq.Main.HostedVM;
import jwutil.collections.AppendIterator;
import jwutil.util.Assert;

public class FindBadStores extends IPSSABuilder.Application {    
    private static CallGraph _cg         = null;
    private Set _classes                                  = null;
    
    // filter out non-local classes?
    static final boolean FILTER_LOCAL          = false;
    static jq_Class  _serializableClass  = null;
    private jq_Class _httpSessionClass   = null;
    private PAResultSelector _sel; 
    

    FindBadStores(IPSSABuilder builder, String name, String[] args) {
        super(builder, name, args);
    }    
    
    protected void parseParams(String[] argv) {
        // TODO    
    }
    
    public static void main(String[] args) {
        HostedVM.initialize();
        
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

        FindBadStores finder = new FindBadStores(i);
        finder.run();
    }
    
    public FindBadStores(Iterator i) {
        Collection roots = new LinkedList();
        Collection root_classes = new LinkedList();
        while(i.hasNext()) {
            jq_Class c = (jq_Class) jq_Type.parseType((String)i.next());
            c.load();
            root_classes.add(c);

            roots.addAll(Arrays.asList(c.getDeclaredStaticMethods()));
        }
        
        //System.out.println("Classes: " + classes);
        System.out.println("Roots: " + roots);
        
        System.out.print("Building call graph...");
        long time = System.currentTimeMillis();
        _cg = new RootedCHACallGraph();
        _cg.setRoots(roots);
        //_cg = new CachedCallGraph(_cg);
        
        time = System.currentTimeMillis() - time;
        System.out.println("done. ("+(time/1000.)+" seconds)");
        _classes = getClasses(_cg.getAllMethods());
        //if(FILTER_LOCAL) _classes = filter(_classes, root_classes);
        
        if(FILTER_LOCAL){
            System.out.println("Considering classes: " + _classes);
        }        
        
        ////
        _serializableClass = (jq_Class)jq_Type.parseType("Ljava.io.Serializable");
        Assert._assert(_serializableClass != null);
        _serializableClass.prepare();
        ////
        _httpSessionClass  = (jq_Class)jq_Type.parseType("Ljavax.servlet.HttpSession");        
        Assert._assert(_httpSessionClass != null);
        _httpSessionClass.prepare();
        
        _sel = new PAResultSelector(_builder.getPAResults());
    }

    private Set getClasses(Collection collection) {
        HashSet result = new HashSet(); 
        for(Iterator iter = collection.iterator(); iter.hasNext(); ) {
            jq_Method method = (jq_Method)iter.next();
            //System.err.println("Saw " + method);
         
            jq_Class c = method.getDeclaringClass();
            if(c != null) {
                result.add(c);
            }
        }
        
        return result;
    }
    
    private void processClasses() {      
        for(Iterator iter = _classes.iterator(); iter.hasNext(); ) {
            jq_Class c = (jq_Class)iter.next();
            
            if(!c.isSubtypeOf(_httpSessionClass)) continue;
            
            System.out.println("Looking at " + c);
            jq_Field[] instanceFields = c.getDeclaredInstanceFields();
            jq_Field[] staticFields = c.getStaticFields();
            
            processFields(c, instanceFields);
            processFields(c, staticFields);
        }        
    }
    
    /**
     * @param c
     * @param fields
     */
    private void processFields(jq_Class c, jq_Field[] fields) {
        for(int i = 0; i < fields.length; i++){
            jq_Field f = fields[i];
            
            processField(c, f);
        }
    }

    /**
     * @param c
     * @param f
     */
    private void processField(jq_Class c, jq_Field f){
        //         1. find heap objects it can point to
        //         2. get their types
        Set types = _sel.getFieldPointeeTypes(f);
        //  3. figure out which ones are *not* serializable
        for(Iterator typeIter = types.iterator(); typeIter.hasNext();){
            jq_Type type = (jq_Type) typeIter.next();
            if(!(type instanceof jq_Class)){
                // skip basic types
                continue;
            }
            
            jq_Class typeClass = (jq_Class) type;
            if(typeClass.getDeclaredInterface(_serializableClass.getDesc()) == null){
                System.err.println(c + "." + f + "\ttype " + c + " is not serializable");
            }
        }
    }

    public void run(){        
        processClasses();
    }
}