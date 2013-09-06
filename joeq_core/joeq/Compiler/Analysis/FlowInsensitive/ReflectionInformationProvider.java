package joeq.Compiler.Analysis.FlowInsensitive;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;
import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Class;
import joeq.Class.jq_Method;
import joeq.Class.jq_Type;
import joeq.Compiler.Analysis.IPA.PA;
import joeq.Compiler.Analysis.IPA.ProgramLocation;
import joeq.Compiler.Analysis.IPA.ProgramLocation.QuadProgramLocation;
import joeq.Compiler.Quad.CodeCache;
import joeq.Main.HostedVM;
import joeq.UTF.Utf8;
import jwutil.util.Assert;

/**
 * @author V.Benjamin Livshits
 * @version $Id: ReflectionInformationProvider.java,v 1.17 2005/04/29 07:38:58 joewhaley Exp $
 * 
 * This class declares methods for resolving reflective calls.
 */
public abstract class ReflectionInformationProvider {
    public class NewInstanceTargets {
        private jq_Method declaredIn;
        private Collection targets = new LinkedList();

        /**
         * @param declaredIn
         */
        public NewInstanceTargets(String declaredIn) {
            this.declaredIn = getMethod(declaredIn);
            if(PA.TRACE_REFLECTION){
                if(this.declaredIn == null) {
                    System.out.println("No method for " + declaredIn + " in NewInstanceTargets. "
                        + " The classpath is [" + PrimordialClassLoader.loader.classpathToString() + "]");
                } else {
                    System.out.println("Created a NewInstanceTarget object for " + this.declaredIn.toString());
                }
            }
        }
        
        public boolean isValid(){
            return getDeclaredIn() != null && targets.size() > 0;
        }

        private jq_Method getMethod(String fullMethodName) {
            int index = fullMethodName.lastIndexOf('.');
            Assert._assert(index != -1);
            
            String className = fullMethodName.substring(0, index);
            String methodName = fullMethodName.substring(index+1, fullMethodName.length());
            
            String classdesc = "L" + className.replace('.', '/') + ";";
            jq_Class clazz = (jq_Class)PrimordialClassLoader.loader.getOrCreateBSType(classdesc);
            
//            jq_Class clazz = (jq_Class) jq_Type.parseType(className);
            try {
                clazz.prepare();
            } catch (NoClassDefFoundError e){
                // no class found
                if(PA.TRACE_REFLECTION) System.err.println("Failed to load class " + className);
                return null;
            }
            jq_Method method = clazz.getDeclaredMethod(methodName);
            Assert._assert(method != null);
            
            if(PA.TRACE_REFLECTION) System.out.println("Retrieved method " + method);
            
            return method;
        }
        
        private jq_Class getClass(String className) {            
            jq_Class clazz = (jq_Class) jq_Type.parseType(className);
            try {
                clazz.prepare();
            } catch (NoClassDefFoundError e){
                return null;
            }
            Assert._assert(clazz != null);
            
            return clazz;
        }

        public void addTarget(String target) {
            jq_Class clazz = getClass(target);
            if(clazz != null){
                addTarget(clazz);
            }
        }

        public void addTarget(jq_Class clazz) {
            jq_Method constructor = clazz.getInitializer(Utf8.get("()V"));
            targets.add(constructor);
        }
        
        public String toString(){
            return declaredIn + " -> " + targets.toString();            
        }

        public jq_Method getDeclaredIn() {
            return this.declaredIn;
        }

        public Collection getTargets() {
            return this.targets;
        }
        
        public void addSubclasses(String className) {
            jq_Class clazz = getClass(className);
            //Assert._assert(clazz != null);
            for(Iterator iter = PrimordialClassLoader.loader.listPackages(); iter.hasNext();){
                //System.out.println("\t" + iter.next());
                String packageName = (String) iter.next();
                
                for(Iterator classIter = PrimordialClassLoader.loader.listPackage(packageName, true); classIter.hasNext();){
                    String className2 = (String) classIter.next();
                    className2 = className2.substring(0, className2.length()-6); 
                    //System.out.println("\tClass: " + className2);
                    jq_Class c = (jq_Class) jq_Type.parseType(className2);
                    try {
                        if(c.isSubtypeOf(clazz)){
                            c.prepare();
                            System.out.println(
                                "Initialized a subclass of " + className + 
                                ", class: " + c);    
                        }                        
                    } catch(Throwable e){
                        continue;
                    }
                    
                    
                }
                
            }
        }        
    }
    
    /** 
     * Reflective methods to be used in isReflective(...)
     */
    private static String[][] methodSpecs = { 
                            {"java.lang.Class", "forName"},
                            {"java.lang.Object", "newInstance"},
                            {"java.lang.reflection.Constructor", "newInstance"},
                            };
    
    /**
     * Checks if method is reflective.
     * 
     * @param method
     */
    public static boolean isReflective(jq_Method method){
        for(int i = 0; i < methodSpecs.length; i++){
            String[] methodSpec = methodSpecs[i];
            String className = methodSpec[0];
            String methodName = methodSpec[1];
            
            if(!className.equals(method.getDeclaringClass().getName())) continue;
            if(!methodName.toString().equals(method.getName())) continue;
            
            return true;
        }
        
        return false;
    }

    /**
     * Checks if mc corresponds to a newInstance call.
     */
    public static boolean isNewInstance(ProgramLocation.QuadProgramLocation mc){
        jq_Method target = mc.getTargetMethod();
        return isNewInstance(target);
    }
    
    /**
     * Checks if target is a newInstance method. 
     */
    public static boolean isNewInstance(jq_Method target) {
        String className = target.getDeclaringClass().getName(); 
        String methodName = target.getName().toString();
        
        if(!className.equals("java.lang.Class")) return false;
        if(!methodName.equals("newInstance")) return false;
        
        return true;
    }
    
    public static boolean isForName(jq_Method target) {
        String className = target.getDeclaringClass().getName(); 
        String methodName = target.getName().toString();
        
        if(!className.equals("java.lang.Class")) return false;
        if(!methodName.equals("forName")) return false;
        
        return true;
    }

    /**
     * Resolves constructors being pointed to by a newInstance() call mc.
     * */
    public abstract Collection/*<jq_Method>*/  getNewInstanceTargets(ProgramLocation.QuadProgramLocation mc);
    
    /**
     * Resolves constructors being pointed to by a newInstance() calls within 
     * method n.
     * 
     * Notice that information may be imprecise because we only have one piece of 
     * data per method.
     * */
    public abstract Collection/*<jq_Method>*/  getNewInstanceTargets(jq_Method n);
    
    /**
     * This implementation of ReflectionInformationProvider 
     * reads answers from a file. 
     * */
    public static class CribSheetReflectionInformationProvider extends ReflectionInformationProvider {
        private static boolean TRACE = true;
        private static final String DEFAULT_CRIB_FILE = "reflection.spec";

        public CribSheetReflectionInformationProvider(String cribSheetFileName){
            try {
                readSpec(cribSheetFileName);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                System.err.println("Error reading " + cribSheetFileName + e.getMessage());                
            }
        }
              
        public CribSheetReflectionInformationProvider() {
            this(DEFAULT_CRIB_FILE);
        }

        public static void main(String[] args) {
            HostedVM.initialize();
            CodeCache.AlwaysMap = true;
            TRACE = true;
            
            CribSheetReflectionInformationProvider provider = 
                new CribSheetReflectionInformationProvider(args[0]);
        }
        
        /**
         * @param cribSheetFileName
         * @throws IOException
         */
        private void readSpec_old(String cribSheetFileName) throws IOException {
            FileReader fileIn = new FileReader(cribSheetFileName);
            LineNumberReader in = new LineNumberReader(fileIn);
            String line = in.readLine();
            do {
                if(!line.startsWith("#") && line.trim().length() > 0){
                    NewInstanceTargets spec = parseSpecLine(line);
                    if(spec.isValid()){
                        if(PA.TRACE_REFLECTION){
                            System.out.println("Adding a reflection spec for " + spec.getDeclaredIn());
                        }
                        specs.add(spec);
                    }
                }
                line = in.readLine();
            } while (line != null);
            in.close();
            
            if(PA.TRACE_REFLECTION) {
                System.out.println(
                    "There are " + specs.size() +            
                    " specifications read from " + cribSheetFileName);
            }
        }
        
        private void readSpec(String cribSheetFileName) throws IOException {
            FileReader fileIn = new FileReader(cribSheetFileName);
            LineNumberReader in = new LineNumberReader(fileIn);
            String line = in.readLine();
            NewInstanceTargets spec = null;
            do {
                if(!line.startsWith("#") && line.trim().length() > 0){                    
                    if(!Character.isWhitespace(line.charAt(0))){
                        int indexBracket = line.indexOf('(');
                        Assert._assert(indexBracket != -1, "No brackets in " + line);
                        String declaredIn = line.substring(0, indexBracket);

                        if(spec != null){
                            if(PA.TRACE_REFLECTION && spec.isValid()){
                                System.out.println("Read " + spec);
                            }
                            if(spec.isValid()){
                                if(PA.TRACE_REFLECTION){
                                    System.out.println(
                                        "Adding a reflection spec for " + spec.getDeclaredIn());
                                }
                                specs.add(spec);
                            }
                        }
                        spec = new NewInstanceTargets(declaredIn);                        
                    }else{
                        line = line.trim();
                        spec.addTarget(line);
                    }
                }
                line = in.readLine();
            } while (line != null);
            in.close();
            
            if(PA.TRACE_REFLECTION) {
                System.out.println(
                    "There are " + specs.size() +            
                    " specifications read from " + cribSheetFileName);
            }
        }
        
        Collection/*<NewInstanceTargets>*/ specs      = new LinkedList();
        private static final Object SUBCLASSES_MARKER = "<";
        private static final Object ELLIPSES          = "...";

        /**
         * Parses one line like this:
             org.roller.presentation.RollerContext.getAuthenticator org.roller.presentation.DefaultAuthenticator ...
        */
        private NewInstanceTargets parseSpecLine(String line) {
            StringTokenizer tok = new StringTokenizer(line);
            String declaredIn = tok.nextToken();
            NewInstanceTargets targets = new NewInstanceTargets(declaredIn);
            while(tok.hasMoreTokens()){
                String token = tok.nextToken();
                if(!token.equals(ELLIPSES)){
                    if(!token.equals(SUBCLASSES_MARKER)){
                        targets.addTarget(token);
                    }else{
                        targets.addSubclasses(tok.nextToken());
                    }
                }else{
                    if(PA.TRACE_REFLECTION){
                        System.err.println("Specification for " + declaredIn + " is incomplete.");
                    }
                }
            }
            if(PA.TRACE_REFLECTION && targets.isValid()){
                System.out.println("Read " + targets);
            }
            
            return targets;
        }
        
        /* (non-Javadoc)
         * @see joeq.Compiler.Analysis.FlowInsensitive.ReflectionInformationProvider#getNewInstanceTargets(joeq.Compiler.Analysis.IPA.ProgramLocation.QuadProgramLocation)
         */
        public Collection getNewInstanceTargets(QuadProgramLocation mc) {
            // TODO
            return null;
        }
        
        public Collection/*<jq_Method>*/ getNewInstanceTargets(jq_Method n) {
            if(PA.TRACE_REFLECTION) System.out.println("There are " + specs.size() + " specs to check against.");
            for(Iterator iter = specs.iterator(); iter.hasNext();){
                NewInstanceTargets spec = (NewInstanceTargets) iter.next();
                if(PA.TRACE_REFLECTION) System.out.println("\tChecking against " + spec.getDeclaredIn());                
                
                if(spec.getDeclaredIn() == n){
                    return spec.getTargets();
                }
            }
            if(PA.TRACE_REFLECTION){
                System.out.println("No information for method " + n);
            }
            return null;            
        }
    }  
}
