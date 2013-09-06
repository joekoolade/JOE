/*
 * Created on 12.10.2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package joeq.Compiler.Analysis.FlowInsensitive;

import java.util.HashMap;
import java.util.Iterator;
import joeq.Class.jq_Class;
import joeq.Class.jq_Initializer;
import joeq.Class.jq_Method;
import joeq.Class.jq_Type;
import jwutil.util.Assert;

public class BogusSummaryProvider {
    HashMap classMap              = new HashMap();
    HashMap methodMap             = new HashMap();
    static boolean INLINE_MAPS = !System.getProperty("inline.maps", "no").equals("no");
    
    private static final boolean TRACE = !System.getProperty("pa.tracebogus", "no").equals("no");
    private static jq_Class realString;
    private static jq_Class realStringBuffer;
    private static jq_Class realHashMap;
    private static jq_Class realVector;
    private static jq_Class realHashtable;
    private static jq_Class realArrayList;
    private static jq_Class realLinkedList;
    private static jq_Class realCookie;
    
    private static jq_Class fakeString;
    private static jq_Class fakeStringBuffer;
    private static jq_Class fakeHashMap;
    private static jq_Class fakeVector;
    private static jq_Class fakeHashtable;
    private static jq_Class fakeArrayList;
    private static jq_Class fakeLinkedList;
    private static jq_Class fakeCookie;
    
    public BogusSummaryProvider() {
        realString       = getClassByName("java.lang.String");
        realStringBuffer = getClassByName("java.lang.StringBuffer");
        realHashMap      = getClassByName("java.util.HashMap");
        realVector       = getClassByName("java.util.Vector");
        realHashtable    = getClassByName("java.util.Hashtable");
        realArrayList    = getClassByName("java.util.ArrayList");
        realLinkedList   = getClassByName("java.util.LinkedList");
        realCookie       = getClassByName("javax.servlet.http.Cookie");
        Assert._assert(realString != null && realStringBuffer != null && realHashMap != null && realVector != null && realHashtable != null);
        realString.prepare(); realStringBuffer.prepare(); realHashMap.prepare(); realVector.prepare(); realHashtable.prepare(); realArrayList.prepare(); realLinkedList.prepare();
        
        fakeString       = getClassByName("MyMockLib.MyString");
        fakeStringBuffer = getClassByName("MyMockLib.MyStringBuffer");        
        fakeHashMap      = getClassByName("MyMockLib.MyHashMap");
        fakeVector       = getClassByName("MyMockLib.MyVector");
        fakeHashtable    = getClassByName("MyMockLib.MyHashtable");
        fakeArrayList    = getClassByName("MyMockLib.MyArrayList");
        fakeLinkedList   = getClassByName("MyMockLib.MyLinkedList");
        Assert._assert(fakeString != null && fakeStringBuffer != null && fakeHashMap != null && fakeVector != null && fakeHashtable != null);        
        fakeString.prepare(); fakeStringBuffer.prepare(); fakeHashMap.prepare(); fakeVector.prepare(); fakeHashtable.prepare(); fakeArrayList.prepare(); fakeLinkedList.prepare();
        if(fakeCookie != null) fakeCookie.prepare();
        if(realCookie != null) realCookie.prepare();
        
        classMap.put(realString, fakeString);
        classMap.put(realStringBuffer, fakeStringBuffer);
        if(realCookie != null && fakeCookie != null) {
            classMap.put(realCookie, fakeCookie);
        }
        if(INLINE_MAPS){
            if(TRACE) {
                System.out.println("Inlining maps, etc.");
            }
            classMap.put(realHashMap, fakeHashMap);
            classMap.put(realVector, fakeVector);
            classMap.put(realHashtable, fakeHashtable);
            classMap.put(realArrayList, fakeArrayList);
        } else {
            System.out.println("Not inlining maps, etc.");
        }        
    }
    
    public jq_Method getReplacementMethod(jq_Method m) {
        return getReplacementMethod(m, null);
    }
    /**
     * Caching method to return a replacement for @param m.
     * @param type 
     * 
     * @return replacement for m.
     * */
    public jq_Method getReplacementMethod(jq_Method m, jq_Type type) {
        jq_Method replacement = (jq_Method) methodMap.get(m);
        
        if(replacement == null) {
                jq_Class c = (jq_Class) classMap.get(m.getDeclaringClass());
                if(c == null && type != null) {
                    c = (jq_Class) classMap.get(type);   
                }
                if(c != null) {
                    replacement = findReplacementMethod(c, m);

                    if(replacement == null) {
                        if(TRACE) System.err.println("No replacement for " + m + " found in " + c);
                        return null;
                    }
                    methodMap.put(m, replacement);
                    if(TRACE) System.out.println("Replaced " + m + " with " + replacement);
                    return replacement;
                } else {
                    return null;
                }
        } else {
            return replacement;
        }
    }
    
    private static jq_Method findReplacementMethod(jq_Class clazz, jq_Method originalMethod) {
        for(Iterator iter = clazz.getMembers().iterator(); iter.hasNext();){
            Object o = iter.next();
            if(!(o instanceof jq_Method)) continue;
            jq_Method m = (jq_Method) o;
            
            if(!m.getName().toString().equals(originalMethod.getName().toString())){
                continue;
            }
            
            if(m.getParamTypes().length != originalMethod.getParamTypes().length){
                continue;            
            }
            
            boolean allMatch = true;
            int base = 0;
            if(clazz != fakeString && clazz != fakeStringBuffer){
                base = 1;
            }
            if(originalMethod instanceof jq_Initializer){                
                base = 1;
            }
            for(int i = base; i < originalMethod.getParamTypes().length; i++){
                jq_Type type = m.getParamTypes()[i];
                jq_Type originalType = originalMethod.getParamTypes()[i];
                
                if(type != originalType){
                    allMatch = false;
                    break;
                }
            }
            if(!allMatch) {
                continue;
            }
         
            // done with the tests: m is good
            return m;
        }
        
        return null;
    }
    
    private static jq_Class getClassByName(String className, boolean strict) {
        jq_Class theClass = (jq_Class)jq_Type.parseType(className);
        if(strict) {
            Assert._assert(theClass != null, className + " is not available.");
        } else {
            return null;
        }
        try {
            theClass.prepare();
        } catch (Exception e) {
            if(strict) {
                e.printStackTrace();
            }
            return null;
        }
        
        return theClass;
    }
    
    private static jq_Class getClassByName(String className) {
        return getClassByName(className, true);
    }

    public boolean hasStaticReplacement(jq_Method replacement) {
        jq_Class clazz = replacement.getDeclaringClass();
        
        return clazz == fakeString || clazz == fakeStringBuffer;
    }
}
