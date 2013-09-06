package joeq.Compiler.Analysis.IPSSA.Apps;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Class;
import joeq.Class.jq_Field;
import joeq.Class.jq_Method;
import joeq.Class.jq_Type;
import joeq.Compiler.Analysis.FlowInsensitive.MethodSummary;
import joeq.Compiler.Analysis.FlowInsensitive.MethodSummary.ReturnedNode;
import joeq.Compiler.Quad.CallGraph;
import joeq.Compiler.Quad.RootedCHACallGraph;
import joeq.Main.HostedVM;
import jwutil.collections.AppendIterator;
import jwutil.util.Assert;

class ClassHierarchy {
    protected class ClassHieraryNode {
        Set              _children  = new HashSet();
        jq_Class         _class     = null;
        ClassHieraryNode _parent    = null;
        
        ClassHieraryNode(jq_Class c){
            this._class = c;
        }
        private void addChild(ClassHieraryNode n) {
            //if(!_children.contains(n)) {
                // adding twice sh ouldn't matter
                _children.add(n);
            //}
        }
        public int getChildCount() {
            return _children.size();
        }
        public Iterator getChildIterator() {
            return _children.iterator();
        }
        public jq_Class getClas() {
            return _class;
        }
        public void reset() {
            this._parent = null;
            _children = new HashSet();            
        }
        public void setRoot(ClassHieraryNode n) {
            //System.err.println("Connecting " + this + " and " + n);
            this._parent = n;
            n.addChild(this);
        }
        public String toLongString() {
            return _class.getJDKDesc();
        }
        public String toString(){
            return _class.toString();                
        }
        public List getChilden() {
            return Arrays.asList(_children.toArray());
        }
    }
    Set _nodes = new HashSet();     
    ClassHieraryNode _root  = null;
    
    ClassHierarchy(ClassHieraryNode root){
        this._root = root;
        add(_root);
    }

    ClassHierarchy(jq_Class root){
        this._root = new ClassHieraryNode(root);
        add(_root);
    }
    
    public ClassHierarchy(jq_Class root, Collection c) {
        this._root = new ClassHieraryNode(root);
        Assert._assert(_root != null);

        add(_root);
        
        for(Iterator iter = c.iterator(); iter.hasNext();) {
            jq_Class c2 = (jq_Class)iter.next();
            
            add(c2);
        }
    }
    
    private void add(ClassHieraryNode node) {
        _nodes.add(node);
    }

    void add(jq_Class c) {
        if(!hasClass(c)) {
            _nodes.add(new ClassHieraryNode(c));
        }
    }

    private ClassHieraryNode getClassNode(jq_Class c) {
        // lame linear search
        for(Iterator iter = _nodes.iterator(); iter.hasNext();) {
            ClassHieraryNode node = (ClassHieraryNode)iter.next();
            
            if(node.getClas() == c) {
                return node;
            }
        }
        
        return null;
    }
    
    boolean hasClass(jq_Class c) {
        return getClassNode(c) != null;
    }
    
    public void makeHierarchy() {
        if(_nodes.size() <= 1) return;
        Assert._assert(_root != null, "Root is not set in the beginning of makeHierarchy");
        // clear potential all data
        resetNodes();
        // use the nodes currently in the set and reset the links
        for(Iterator iter = _nodes.iterator(); iter.hasNext();) {
            ClassHieraryNode node = (ClassHieraryNode)iter.next();
            jq_Class c = node.getClas();
            
            do {
                if(c instanceof jq_Class && ((jq_Class)c).isInterface()){
                    //System.err.println("Reached interface: " + c);
                }
                // directly supports this interface
                if(c.getDeclaredInterface(_root.getClas().getDesc()) != null){
                    // termination condition
                    //System.err.println("Reached root: " + c);
                    if(node != _root){
                        node.setRoot(_root);
                        Assert._assert(_root.getChildCount() > 0);
                    }
                    break;
                }
                jq_Class superClass = (jq_Class)c.getSuperclass();
                ClassHieraryNode n = getClassNode(superClass);
                if(n != null) {
                    // found the most direct link -- make the connection
                    node.setRoot(n);
                }
                if(superClass == c){
                    break; // self-recursion
                }
                c = superClass;
            } while(c != null);            
        }
        Assert._assert(_root != null, "Root is not set at the end of makeHierarchy");
        Assert._assert(_root.getChildCount() > 0, "Root is not connected to any children");
    }
    
    public void printHierarchy() {
        if(size() <= 0) return;
        Assert._assert(_root != null);
        
        System.out.println("Printing a hierarchy of size " + size() + " rooted at " + _root);
        printHierarchyAux(_root, "");
    }
    
    private int size() {
        return _nodes.size() - 1;
    }

    /**
     * Compares class names.
     * */
    public class ClassComparator implements Comparator {
        public int compare(Object arg0, Object arg1) {            
            return arg0.toString().toLowerCase().compareTo(arg1.toString().toLowerCase());
        }
    }
    
    private void printHierarchyAux(ClassHieraryNode node, String string) {        
        System.out.print(string + node.toString());
        System.out.println(node.getChildCount() == 0 ? "" : (" " + node.getChildCount()));
        List children = node.getChilden();
        Comparator comparator = new ClassComparator();        
        Collections.sort(children, comparator);
        for(Iterator iter = children.iterator(); iter.hasNext();) {
            ClassHieraryNode child = (ClassHieraryNode)iter.next();

            Assert._assert(child != node, "Child: " + child + " is the same as " + node);            
            printHierarchyAux(child, string + "\t");
        }
    }

    private void resetNodes() {
        Assert._assert(_root != null, "Root is not set in the beginning of resetNodes");
        for(Iterator iter = _nodes.iterator(); iter.hasNext();) {
            ClassHieraryNode node = (ClassHieraryNode)iter.next();
            node.reset();
        }
        Assert._assert(_root != null, "Root is not set at the end of resetNodes");
    }
}

/**
 * Represents the fact that 
 *  this.method() <= source1.method() + source2.method()... 
 * */
class ResultCorrelation {
    //Collection/*jq_Field*/  _sources;    
    jq_Class                _this;
    jq_Field                _that;
    
    ResultCorrelation(jq_Class c){
        this._this      = c; 
        //this._sources   = new LinkedList();
    }    
    void addSource(jq_Field field) {
        //_sources.add(field);
        _that = field;
    }    
    int getSourceCount() {
        //return _sources.size();
        return 1;
    }    
    public String toString() {
        return "<Correlation: " + _this + " ~ " + _that.getType() + ">";
    }
    public jq_Class getThis() {
       return _this;
    }
    public jq_Field getThat() {
       return _that;
    }
}

public class FindCollectionImplementations {    
    private static CallGraph _cg;
    
    static jq_Class _collectionClass  = null;
    static jq_Class _iteratorClass    = null;
    static jq_Class _setClass         = null;
    static jq_Class _mapClass         = null;
    static jq_Class _enumerationClass = null;
    // filter out non-local classes?
    static final boolean FILTER_LOCAL = false; 
            //System.getProperty("collections.filterNonLocal").equals("yes");

    static final String COLLECTION_SIGNATURE = "Ljava.util.Collection;";
    static final String SET_SIGNATURE        = "Ljava.util.Set;";
    static final String MAP_SIGNATURE        = "Ljava.util.Map;";
    static final String ENUMERATION_SIGNATURE= "Ljava.util.Enumeration;";
    static final String ITERATOR_SIGNATURE   = "Ljava.util.Iterator;";    
    
    public static void main(String[] args) {
        HostedVM.initialize();
        initPredefinedClasses();
        ClassAndMethod.initializeClasses();
        
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

        FindCollectionImplementations finder = new FindCollectionImplementations(i);
        finder.run(true);
    }
    private Set _classes;
    private Set _collections;
    private Set _iterators;
    private Set _sets;
    private Set _maps;
    private Set _enumerations;
    
    public FindCollectionImplementations(Iterator i) {
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
        if(FILTER_LOCAL) _classes = filter(_classes, root_classes);
        
        if(FILTER_LOCAL){
            System.out.println("Considering classes: " + _classes);
        }

        _collections    = new HashSet();
        _iterators      = new HashSet();
        _sets           = new HashSet();
        _maps           = new HashSet();
        _enumerations   = new HashSet();

        // 
        initPredefinedClasses();
        
        Assert._assert(_collectionClass != null);
        Assert._assert(_iteratorClass  != null);
        Assert._assert(_setClass  != null);
    }
    
    static void initPredefinedClasses() {
        _collectionClass  = (jq_Class)jq_Type.parseType(COLLECTION_SIGNATURE);
        _iteratorClass    = (jq_Class)jq_Type.parseType(ITERATOR_SIGNATURE);  
        _setClass         = (jq_Class)jq_Type.parseType(SET_SIGNATURE);
        _mapClass         = (jq_Class)jq_Type.parseType(MAP_SIGNATURE);
        _enumerationClass = (jq_Class)jq_Type.parseType(ENUMERATION_SIGNATURE);
        
        _collectionClass.prepare();
        _iteratorClass.prepare();
        _setClass.prepare();
        _mapClass.prepare();
        _enumerationClass.prepare();
    }

    private Set filter(Set classes, Collection roots) {
        Set prefixes = new HashSet();
        for(Iterator iter = roots.iterator(); iter.hasNext();) {
            jq_Class root  = (jq_Class)iter.next();
            StringTokenizer t = new StringTokenizer(root.getJDKDesc(), ".");
            String prefix = t.nextToken();
            prefixes.add(prefix);
        }
        System.out.println("Recognized prefixes: " + prefixes);
        
        Set result = new HashSet();
        for(Iterator iter = classes.iterator(); iter.hasNext();) {
            jq_Class c          = (jq_Class)iter.next();
            StringTokenizer t   = new StringTokenizer(c.getJDKDesc(), ".");
            String prefix       = t.nextToken();
            
            if(prefixes.contains(prefix)) {
                result.add(c);
            }
        }
        
        return result;
    }

    private void findCollections() {      
        for(Iterator iter = _classes.iterator(); iter.hasNext(); ) {
            jq_Class c = (jq_Class)iter.next();
            
            if(
                    c.getDeclaredInterface(_collectionClass.getDesc()) != null && 
                    c.getDeclaredInterface(c.getDesc()) == null) 
            {        
                _collections.add(c);
            }
        }        
    }
    private void findIterators() {        
        for(Iterator iter = _classes.iterator(); iter.hasNext(); ) {
            jq_Class c = (jq_Class)iter.next();
            
            if(c.getDeclaredInterface(_iteratorClass.getDesc()) != null) {
                _iterators.add(c);
            }
        }        
    }
    private void findSets() {        
        for(Iterator iter = _classes.iterator(); iter.hasNext(); ) {
            jq_Class c = (jq_Class)iter.next();
            
            if(c.getDeclaredInterface(_setClass.getDesc()) != null) {
                _sets.add(c);
            }
        }        
    }
    private void findMaps() {
        for(Iterator iter = _classes.iterator(); iter.hasNext(); ) {
            jq_Class c = (jq_Class)iter.next();
            
            if(c.getDeclaredInterface(_mapClass.getDesc()) != null) {
                _maps.add(c);
            }
        }        
    }
    private void findEnumerations() {
        for(Iterator iter = _classes.iterator(); iter.hasNext(); ) {
            jq_Class c = (jq_Class)iter.next();
            
            if(c.getDeclaredInterface(_enumerationClass.getDesc()) != null) {
                _enumerations.add(c);
            }
        }        
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

    private void printCollection(Collection collection) {
        Iterator iter = collection.iterator();
        while(iter.hasNext()) {
            jq_Class c = (jq_Class)iter.next();
            
            System.out.println("\t" + c);
        }
    }
    
    private void reportStats(boolean verbose) {
        if(verbose) {
            System.out.println("Found " + _collections.size() + " collections:");
            //printCollection(_collections);
            ClassHierarchy h = new ClassHierarchy(_collectionClass, _collections);
            h.makeHierarchy();
            h.printHierarchy();
            
            System.out.println("Found " + _sets.size() + " sets");
            //printCollection(_iterators);
            h = new ClassHierarchy(_setClass, _sets);
            h.makeHierarchy();
            h.printHierarchy();
            
            System.out.println("Found " + _maps.size() + " maps");
            //printCollection(_iterators);
            h = new ClassHierarchy(_mapClass, _maps);
            h.makeHierarchy();
            h.printHierarchy();
            
            System.out.println("Found " + _enumerations.size() + " enumerations");
            //printCollection(_iterators);
            h = new ClassHierarchy(_enumerationClass, _enumerations);
            h.makeHierarchy();
            h.printHierarchy();
    
            System.out.println("Found " + _iterators.size() + " iterators");
            //printCollection(_iterators);
            h = new ClassHierarchy(_iteratorClass, _iterators);
            h.makeHierarchy();
            h.printHierarchy();
        }
        
        System.out.println("Found " + 
               _collections.size() + " collections, " + 
               _sets.size() + " sets, " +
               _maps.size() + " maps, " +
               _maps.size() + " enumerations, " +
               _iterators.size() + " iterators "
               );
    }
    
    protected void run(boolean verbose){        
        //System.out.println("Looking for subclasses of " + _collectionClass + " and " + _iteratorClass);
        
        // detect the interesting classes
        findCollections();
        findSets();
        findMaps();
        findEnumerations();
        findIterators();                 
        if(verbose) {
            final String LINE = repeat("-", 100);
            // for each of the classes, mark the variables that are reachable from them
            System.out.println(LINE);
            System.out.println("Collections:");
            findReachable(_collections);
    
            System.out.println(LINE);
            System.out.println("Sets:");
            findReachable(_sets);
            
            System.out.println(LINE);
            System.out.println("Maps:");
            findReachable(_maps);
            
            System.out.println(LINE);
            System.out.println("Enumerations:");
            findReachable(_enumerations);
            
            System.out.println(LINE);
            System.out.println("Iterators:");
            findReachable(_iterators);        
            System.out.println(LINE);
        }
        
        findCorrelations(_iterators);
        // do the statistics
        reportStats(verbose);
    }

    static class ClassAndMethod {
        jq_Class _c;
        //jq_Method _method;
        String _methodName;
        static Map/*<jq_Class, ClassAndMethod>*/ _data = null;

        ClassAndMethod(jq_Class c, String m){
            this._c = c;
            this._methodName = m;    // TODO
        }

        static void initializeClasses(){
            if(_data != null) return;
            _data = new HashMap();
            
            _data.put(_iteratorClass,       new ClassAndMethod(_iteratorClass,    "next") );
            _data.put(_collectionClass,     new ClassAndMethod(_collectionClass,  "iterator") );
            _data.put(_enumerationClass,    new ClassAndMethod(_enumerationClass, "nextElement") );
            _data.put(_setClass,            new ClassAndMethod(_setClass,         "iterator") );
            _data.put(_mapClass,            new ClassAndMethod(_mapClass,         "get") );

            System.out.println("Initialized information about " + _data.size() + " classes");  
        }

        public static ClassAndMethod retriveClassAndMethod(jq_Class c) {
            return (ClassAndMethod)_data.get(c);             
        }

        public String getMethodName() {
            return _methodName;
        }
    }

    private void findCorrelations(Set collections) {
        for(Iterator iter = collections.iterator(); iter.hasNext();) {
            jq_Class c = (jq_Class)iter.next();
            jq_Field[] fields = c.getDeclaredInstanceFields();
            Collection eligibleFields = new LinkedList();
            //System.out.println("Considering " + c);

            for(int i = 0; i < fields.length; i++) {
                jq_Field field = fields[i];
                jq_Type type = field.getType();
                if(!type.isClassType()) continue;
                if(!isStandardClass((jq_Class)type)) continue;
                
                eligibleFields.add(field);
            }
            
            if(eligibleFields.size() == 1) {
                jq_Field that = (jq_Field)eligibleFields.iterator().next();

                // now we need to correlate between c and that
                
                ResultCorrelation r = new ResultCorrelation(c);
                r.addSource(that);
                
                // try to prove that the correlation holds
                System.out.println("Considering " + r);

                try {
                    tryToProve(r);
                }catch(ClassCastException e) {
                    //System.out.println("Skipping " + r);
                }catch(RuntimeException e) {
                    //System.out.println("Skipping " + r);
                }
            }
        }
    }

    private boolean isStandardClass(jq_Class c) {
        return getStandardClass(c) != null;
    }

    /**
        Prove the correlation between the results.
    */
    private void tryToProve(ResultCorrelation r) {
        jq_Class thisClass = r.getThis();
        jq_Class thatClass = (jq_Class)r.getThat().getType();

        ClassAndMethod thisCAM = getClassAndMethod(thisClass);
        ClassAndMethod thatCAM = getClassAndMethod(thatClass);
        
        jq_Method thisMethod = thisClass.getDeclaredMethod(thisCAM.getMethodName());
        jq_Method thatMethod = thatClass.getDeclaredMethod(thatCAM.getMethodName());

        if(thisMethod == null) throw new RuntimeException("Can't find the method " + thisCAM.getMethodName() + " in " + thisClass + ": " + thisClass.getMembers());
        if(thatMethod == null) throw new RuntimeException("Can't find the method " + thatCAM.getMethodName() + " in " + thatClass + ": " + thatClass.getMembers());

        System.out.println("\t" + "Comparing the result of " + thisMethod + " and " + thatMethod);

        // this return node
        Set thisNodeReturns = MethodSummary.getSummary(thisMethod).getReturned();
        if(thisNodeReturns.size() != 1) {
            throw new RuntimeException("There are " + thisNodeReturns.size() + " nodes for " + thisMethod);
        }
        ReturnedNode thisNodeReturn = (ReturnedNode)thisNodeReturns.iterator().next();        
        System.out.println("thisNodeReturn: " + thisNodeReturns);

        // that return node        
        Set thatNodeReturns = MethodSummary.getSummary(thatMethod).getReturned();
        System.out.println("thatNodeReturns: " + thatNodeReturns);
        if(thatNodeReturns.size() != 1) {
            throw new RuntimeException("There are " + thatNodeReturns.size() + " nodes for " + thatMethod);
        }
        //System.out.println("Set: " + thatNodeReturns);
        ReturnedNode thatNodeReturn = (ReturnedNode)thatNodeReturns.iterator().next();
        System.out.println("thatNodeReturn: " + thatNodeReturn);
        
        
        
    }

    protected ClassAndMethod getClassAndMethod(jq_Class classType) {
        jq_Class stdClassThat = getStandardClass(classType);
        Assert._assert(stdClassThat != null, "Unexpected class " + classType);
        ClassAndMethod cam = ClassAndMethod.retriveClassAndMethod(stdClassThat);
        Assert._assert(cam != null, "Can't find a method for " + stdClassThat);
        
        return cam; 
    }

    private jq_Class getStandardClass(jq_Class c) {
        Assert._assert(_setClass.isPrepared() && _collectionClass.isPrepared() && _mapClass.isPrepared());
        c.prepare();
        
        if(c.implementsInterface(_setClass) || c == _setClass) {        
            return _setClass;
        } else {
            if(c.implementsInterface(_collectionClass) || c == _collectionClass) { 
                return _collectionClass;
            }
        }            
        if(c.implementsInterface(_iteratorClass) || c == _iteratorClass) {
            return _iteratorClass;
        }else        
        if(c.implementsInterface(_mapClass) || c == _mapClass) {
            return _mapClass;
        }else
        if(c.implementsInterface(_enumerationClass) || c == _enumerationClass) {
            return _enumerationClass;
        }

        return null;
    }

    private void findReachable(Set classes) {
        for(Iterator iter = classes.iterator(); iter.hasNext();) {
            jq_Class c = (jq_Class)iter.next();
            
            Collection reachable = findReachable(c);
            if(!reachable.isEmpty()) {
                System.out.println(cutto(c.toString(), 40) + ": [");
                for(Iterator iter2 = reachable.iterator(); iter2.hasNext();) {
                    Object o = iter2.next();
                    if(o instanceof jq_Field) {
                        jq_Field field = (jq_Field)o;
                        jq_Type type = field.getType();
                        
                        if(type.isClassType()) {
                            System.out.println(repeat(" ", 40) + "\t" + 
                                    cutto(field.getName().toString(), 30) + 
                                    " : " + type + typeSig(type));
                        }
                    } else {
                        System.out.println(repeat(" ", 40) + "\t" + o + " ");
                    }
                }
                System.out.println(repeat(" ", 40) + "]");
            } else {
                System.out.println(cutto(c.toString(), 40) + ": []");
            }
        }
    }

    private String typeSig(jq_Type type) {        
        if(!(type instanceof jq_Class)) return "";
        StringBuffer buf = new StringBuffer();
        jq_Class c = (jq_Class)type;        
        
        if(c.implementsInterface(_setClass) || c == _setClass) {        
            buf.append(" [Set]");
        } else {
            if(c.implementsInterface(_collectionClass) || c == _collectionClass) { 
                buf.append(" [Collection]");
            }
        }            
        if(c.implementsInterface(_iteratorClass) || c == _iteratorClass) {
            buf.append(" [Iterator]");
        }else        
        if(c.implementsInterface(_mapClass) || c == _mapClass) {
            buf.append(" [Map]");
        }else
        if(c.implementsInterface(_enumerationClass) || c == _enumerationClass) {
            buf.append(" [Enumeration]");
        }
        
        return buf.toString();
    }

    private Collection findReachable(jq_Class c) {
        Collection result = new LinkedList();
        // add the declared fields
        result.addAll(Arrays.asList(c.getDeclaredInstanceFields()));
        
        return result;        
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
