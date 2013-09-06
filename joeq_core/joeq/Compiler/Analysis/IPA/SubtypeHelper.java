package joeq.Compiler.Analysis.IPA;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Class;
import joeq.Class.jq_Reference;
import joeq.Class.jq_Reference.jq_NullType;
import jwutil.util.Assert;
import net.sf.javabdd.BDD;

public abstract class SubtypeHelper {
    protected PA pa;
    protected static boolean TRACE = true;
    protected static final String OFFLINE    = "offline";
    protected static final String ONLINE     = "online";
    protected static final String KNOWN      = "known";

    public SubtypeHelper(PA pa){
        this.pa = pa;
    }   
    
    static String canonicalizeClassName(String s) {
        if (s.endsWith(".class")) s = s.substring(0, s.length() - 6);
        s = s.replace('.', '/');
        String desc = "L" + s + ";";
        return desc;
    }
    
    public abstract Collection getSubtypes(jq_Class clazz);
    
    public static class KnownClassesSubtypeHelper extends SubtypeHelper {
        static final String kind = KNOWN;
        public KnownClassesSubtypeHelper(PA pa) {
            super(pa);
            
            if(TRACE) System.out.println("Instantiating a subtype helper of type " + kind);
        }
        
        public Collection getSubtypes(jq_Class t) {
            if(TRACE) System.out.println("Requesting subtypes of class " + t);
            
            Collection result = new LinkedList();
            int T1_i = pa.Tmap.get(t);
            BDD subtypes = pa.aT.relprod(pa.T1.ithVar(T1_i), pa.T1set);          // T2
            for(Iterator typeIter = subtypes.iterator(pa.T2set); typeIter.hasNext();){
                jq_Reference subtype = (jq_Reference) pa.Tmap.get(((BDD)typeIter.next()).scanVar(pa.T2).intValue());
                if (subtype == null || subtype == jq_NullType.NULL_TYPE) continue;
                if(!(subtype instanceof jq_Class)){
                    System.err.println("Skipping a non-class type: " + t);
                    continue;
                }
                jq_Class c = (jq_Class) subtype;
                result.add(c);
            }
        
            if(TRACE) System.out.println("Returning " + result.size() + " subtypes.");
            return result;
        }
    }
    
    public static class OnlineSubtypeHelper extends SubtypeHelper {    
        private Map/*<jq_Class, Collection<jq_Class>>*/ type2subtypeCache = new HashMap();
        static final String kind = ONLINE;

        public OnlineSubtypeHelper(PA pa) {
            super(pa);
            
            if(TRACE) System.out.println("Instantiating a subtype helper of type " + kind);
        }

        public Collection getSubtypes(jq_Class clazz) {
            if(TRACE) System.out.println("Requesting subtypes of class " + clazz);
            Collection result = (Collection) type2subtypeCache.get(clazz);
            if(result != null) {
                return result;
            }
            result = new LinkedList();
            for(Iterator iter = PrimordialClassLoader.loader.listPackages(); iter.hasNext();){
                //System.out.println("\t" + iter.next());
                String packageName = (String) iter.next();
                HashSet loaded = new HashSet();
                if(TRACE) System.out.println("Processing package " + packageName);
                
                for(Iterator classIter = PrimordialClassLoader.loader.listPackage(packageName, true); classIter.hasNext();){
                    String className = (String) classIter.next();
                    String canonicalClassName = canonicalizeClassName(className);
                    if (loaded.contains(canonicalClassName))
                        continue;
                    loaded.add(canonicalClassName);
                    try {
                        jq_Class c = (jq_Class) PrimordialClassLoader.loader.getOrCreateBSType(canonicalClassName);
                        c.load();
                        c.prepare();
                        if(c.isSubtypeOf(clazz)){                        
                            System.out.println("Initialized a subclass of " + clazz + ", class: " + c);
                            result.add(c);
                        }
                    } catch (NoClassDefFoundError x) {
                        if(TRACE) System.err.println("Package " + packageName + ": Class not found (canonical name " + canonicalClassName + ").");
                    } catch (LinkageError le) {
                        if(TRACE) System.err.println("Linkage error occurred while loading class (" + canonicalClassName + "):" + le.getMessage());
                        //le.printStackTrace(System.err);
                    } catch (RuntimeException e){
                        if(TRACE) System.err.println("Security error occured: " + e.getMessage());
                    }
                }            
            }
         
            type2subtypeCache.put(clazz, result);
            if(TRACE) System.out.println("Returning " + result.size() + " subtypes.");
            return result;   
        }
    }
    
    public static class OfflineSubtypeHelper extends SubtypeHelper {
        static final String subtypeFileName = "reversed_subclasses.txt";
        Map classes2subclasses = new HashMap(); 
        private boolean initialized = false;
        final static String kind = OFFLINE;
        public OfflineSubtypeHelper(PA pa) {
            super(pa);
            
            if(TRACE) System.out.println("Instantiating a subtype helper of type " + kind);            
        }
        
        void initializeSubclasses() throws IOException {
            if(initialized) return;
            BufferedReader r = new BufferedReader(new FileReader(subtypeFileName));
            String s = null;
            String className = null;
            Collection subclassList = null;
            while ((s = r.readLine()) != null) {
                if(s.startsWith("CLASS ")){                    
                    className = s.substring("CLASS ".length(), s.indexOf(" ", "CLASS ".length() + 1));
                    subclassList = new LinkedList();
                    // add the class itself to the list of subclasses
                    subclassList.add(className);
                    classes2subclasses.put(className, subclassList);
                }else{
                    int index = s.indexOf("SUBCLASS ");
                    if(index != -1){
                        String subclass = s.substring(index + "SUBCLASS ".length(), s.length());
                        subclassList.add(subclass);
                    }
                }
            }
            initialized = true;
        }

        /* (non-Javadoc)
         * @see joeq.Compiler.Analysis.IPA.SubtypeHelper#getSubtypes(joeq.Class.jq_Class)
         */
        public Collection getSubtypes(jq_Class clazz) {
            if(TRACE) System.out.println("Requesting subtypes of class " + clazz);
            String className = clazz.getName();
            try {
                initializeSubclasses();         // lazily initialize the subclasses
            } catch (IOException e) {
                Assert._assert(false, e.toString());
                return null;
            }
            
            Collection subtypeNames = (Collection) classes2subclasses.get(className);
            if(subtypeNames == null){
                System.err.println("No match for class \"" + className + "\" in " + subtypeFileName);
                return null;
            }
            Collection result = new LinkedList();
            for(Iterator iter = subtypeNames.iterator(); iter.hasNext();){
                String subtypeName = (String) iter.next();                
                String canonicalName = canonicalizeClassName(subtypeName.trim());
                
                try {
                    jq_Class subtypeClass = (jq_Class) jq_Class.parseType(canonicalName);
                    
                    if(!subtypeClass.isPrepared()){
    //                    if(TRACE){
    //                        System.out.println("Preparing class " + subtypeClass + " by name " + canonicalName);
    //                    }
                        subtypeClass.prepare();
                    }
                    result.add(subtypeClass);
                } catch (java.lang.NoClassDefFoundError e){
                    if(TRACE) System.err.println("Can't load " + subtypeName + ": " + e);
                    continue;
                }
            }
            Assert._assert(result.size() <= subtypeNames.size());
            
            if(TRACE) System.out.println("Returning " + result.size() + " subtypes.");
            return result;
        }
    }

    public static SubtypeHelper newSubtypeHelper(PA pa, String kind) {
        if(kind.equals(OFFLINE)) {
            return new OfflineSubtypeHelper(pa);
        }
        if(kind.equals(ONLINE)) {
            return new OnlineSubtypeHelper(pa);
        }
        if(kind.equals(KNOWN)) {
            return new KnownClassesSubtypeHelper(pa);
        }
        
        Assert._assert(false, "Unknown kind: " + kind);
        return null;
    }
}
