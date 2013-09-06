/*
 * Created on Jul 27, 2005
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package joeq.Util;

import joeq.Class.jq_Array;
import joeq.Class.jq_Class;
import joeq.Class.jq_ClassInitializer;
import joeq.Class.jq_Field;
import joeq.Class.jq_Initializer;
import joeq.Class.jq_Method;
import joeq.Class.jq_Primitive;
import joeq.Class.jq_Type;

/**
 * Contains code for converting names of IR entities into their string 
 * representations used by Eclipse.
 * 
 *  @author John Whaley
 *  @version $Id: NameMunger.java,v 1.4 2006/03/09 03:09:28 mcmartin Exp $
 * */
public class NameMunger {
    //  NQueens.<clinit>
    // java/lang/Math/doublepow(doubledouble)
    public static String mungeMethodName(jq_Method m) {
        if (m == null) return "null";
        StringBuffer sb = new StringBuffer();
        String className = mungeTypeName(m.getDeclaringClass());
        String returnType = mungeTypeName(m.getReturnType());
        
        if (m instanceof jq_ClassInitializer) {
            return className+".<clinit>";
        }
        sb.append(className);
        sb.append('/');
        sb.append(returnType);
        if (m instanceof jq_Initializer) {
            
        } else {
            sb.append(m.getName());
        }
        sb.append('(');
        jq_Type[] ps = m.getParamTypes();
        for (int i = 0; i < ps.length; ++i) {
            if (i == 0 && !m.isStatic()) continue;
            jq_Type p = ps[i];
            sb.append(mungeTypeName(p));
        }
        sb.append(')');
        jq_Class[] ex = m.getThrownExceptionsTable();
        if (ex != null) {
            for (int i = 0; i < ex.length; ++i) {
                jq_Class x = ex[i];
                sb.append(mungeTypeName(x));
            }
        }
        return sb.toString();
    }
    
    // java/lang/String/count
    // java/lang/String$1/foo
    // java/lang/String/Iterator/n
    public static String mungeFieldName(jq_Field t) {
        if (t == null) return "null";
        return mungeTypeName(t.getDeclaringClass())+"/"+t.getName();
    }
    
    public static String mungeTypeName2(jq_Type t) {
        if (t == null) return "null";
        if (isAnonymousClass(t.toString())) return mungeTypeName(t);
        String s = t.toString().replace('$','.');
        return s;
    }
    
    public static String mungeTypeName(jq_Type t) {
        if (t == null) return "null";
        if (t instanceof jq_Primitive) {
            return ((jq_Primitive) t).getName();
        }
        if (t instanceof jq_Array) {
            jq_Array a = (jq_Array) t;
            int depth = a.getDimensionality();
            jq_Type elementType = a.getInnermostElementType();
            return mungeTypeName(elementType)+depth;
        }
        String s = t.toString();
        s = s.replace('.', '/');
        if (!isAnonymousClass(s))
            s = s.replace('$', '/');
        return s;
    }
    
    public static boolean isAnonymousClass(String s) {
        int i = s.indexOf('$');
        if (i == -1) return false;
        if (i+1 == s.length()) return false;
        char c = s.charAt(i+1);
        return Character.isDigit(c);
    }
    
    public static String getJavadocSignature(jq_Method method) {
        if (method == null) return "null";
        String jvmSig = method.toString();
        
        return getJavadocSignature(jvmSig, method.getReturnType().getName());
    }
    
    public static String getJavadocSignature(String jvmSig, String returnType) {
        int spaceIdx = jvmSig.indexOf(' ');
        String name = jvmSig.substring(0, spaceIdx);
        String paramSig = jvmSig.substring(spaceIdx+1,jvmSig.length());
        String[] params = DescriptorUtil.getParameters(paramSig);
        
        StringBuffer result = new StringBuffer();
        if(returnType != null) {
            result.append(returnType);
            result.append(" ");
        }
        
        result.append(name);
        result.append("(");
        for (int i = 0; i < params.length; i++) {
            String param = params[i];
            
            result.append(param);
            if(i < params.length-1) result.append(", ");
        }
        result.append(")");
        
        return result.toString();        
    }
    
    private static void test(String sig) {
        System.out.println(getJavadocSignature(sig, null));
    } 

    public static void main(String[] args) {
        test("java.util.Calendar.internalSet (II)V");
        test("MyMockLib.MyString.substring (Ljava/lang/String;I)Ljava/lang/String;");
    }
}
