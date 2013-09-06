/*
 * Created on Feb 1, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package joeq.Compiler.Analysis.IPA;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import joeq.Class.jq_Class;
import joeq.Class.jq_Field;
import joeq.Class.jq_Initializer;
import joeq.Class.jq_InstanceMethod;
import joeq.Class.jq_Method;
import joeq.Class.jq_Reference;
import joeq.Class.jq_Type;
import joeq.Compiler.Analysis.FlowInsensitive.MethodSummary;
import joeq.Compiler.Analysis.FlowInsensitive.MethodSummary.Node;
import joeq.Compiler.Analysis.IPSSA.IPSSABuilder;
import joeq.Compiler.Analysis.IPSSA.IPSSABuilder.Application;
import jwutil.util.Assert;
import net.sf.javabdd.BDD;
import net.sf.javabdd.TypedBDDFactory;
import net.sf.javabdd.TypedBDDFactory.TypedBDD;

/**
 * Finds and outputs information about polymorphic:
 *  - fields
 *  - method parameters
 * 
 * The result is output in XML.
 * */
public class CollectionFinder extends Application {    
    static final String OBJECT_SIGNATURE = "Ljava.lang.Object;";

    public static String xmlEscape(String s) {
        s = s.replaceAll("<", "&lt;");
        s = s.replaceAll("<", "&gt;");

        return s;
    }
    private final static String _outFile = "polyclasses.xml";
    private PA          _r               = null;
    private static final boolean TRACE = false;

    public CollectionFinder() {
        this(null,null,null);
    }
    public CollectionFinder(IPSSABuilder builder, String name, String[] args) {
        super(builder, name, args);        
    }
    
    private Collection buildList(BDD e) {
        Collection c = new LinkedList();
        for (Iterator i = e.iterator(_r.T2set); i.hasNext(); ) {
            BDD b = (BDD) i.next();
            BDD d = b.relprod(_r.aT, _r.T2set);
            b.free();
            
            //c.add(_r.getResults().toString((TypedBDDFactory.TypedBDD)d, -1));
            c.add(d.toString());
        }
        
        return c;
    }
    
    public BDD calculateCommonSupertype(BDD types) {
        if (types.isZero()) return _r.bdd.zero();
        BDD bestTypes = _r.T1.domain();
        
        // TODO: need to skip NULL, which is T1(0) = T2(0)
        //System.out.println("Looking for supertype of "+types.toStringWithDomains(r.TS));
        for (Iterator i = types.iterator(_r.T2set); i.hasNext(); ) {
            BDD b = (BDD) i.next();
            BDD c = b.relprod(_r.aT, _r.T2set);
            b.free();
            bestTypes.andWith(c); // T1
        }
        
        //System.err.println( "bestTypes: " + bestTypes.toStringWithDomains() );
        for (Iterator i = bestTypes.iterator(_r.T1set); i.hasNext(); ) {
            BDD b = (BDD) i.next();
            BDD c = b.relprod(_r.aT, _r.T1set); // T2
            b.free();
            
            /*if(!c.and(_r.T2.ithVar(0)).isZero()) {
                System.out.println( 
                    c.toStringWithDomains(_r.TS) + " is " + 
                    !c.and(_r.T2.ithVar(0)).isZero() );

                System.err.println("Before: " + c.toStringWithDomains(_r.TS));
                c.restrictWith(_r.T2.ithVar(0));
                System.err.println("After : " + c.toStringWithDomains(_r.TS));
            }*/
            
            c.replaceWith(_r.T2toT1); // T1
            c.andWith(bestTypes.id()); // T1
                                   
            if (c.satCount(_r.T1set) == 1.0) {
                return c;
            }
        }
        System.out.println("No subtype matches! "+bestTypes.toStringWithDomains(_r.TS));
        return _r.bdd.zero();
    }
    
    public BDD calculateConcreteTypes(BDD types, boolean removeNull) {
        TypedBDD tb = (TypedBDD)types;
        BDD t = tb.getDomainSet().contains(_r.H1c) ? 
                types.relprod(_r.hT, _r.H1set.and(_r.H1cset)) :
                types.relprod(_r.hT, _r.H1set);
        
        // Remove NULL if it's present
        if(removeNull && !t.and(_r.T2.ithVar(0)).isZero()) {
            //System.out.println( t.toStringWithDomains(_r.TS) + " is " + 
            //        !t.and(_r.T2.ithVar(0)).isZero() );

            t.restrictWith(_r.T2.ithVar(0));
        }
        
        //System.err.println("Types: " + t.toStringWithDomains(_r.TS));        
        
        return t;
    }

    public jq_Reference getType(TypedBDD types) {              
        BigInteger[] indeces = _r.T2.getVarIndices(types);
        Assert._assert(indeces.length == 1, "There are " + indeces.length + " indeces in " + types.toStringWithDomains());
        BigInteger index = indeces[0];
        jq_Reference type = (jq_Reference)_r.Tmap.get(index.intValue());
        //System.out.println("Index: " + index + " type: " + type);
 
        return type;        
    }
    
    protected void parseParams(String[] args) {}

    private void printFields(PrintStream out, Collection c, String name) {
        out.println("<fieldset name = \"" + name + "\">");
        for(Iterator iter = c.iterator(); iter.hasNext();) {
            jq_Field f = (jq_Field)iter.next();
            
            out.println("\t<field name=\""  + f + "\" type=\"" + f.getType() + "\">");            
            out.println("\t</field>");
        }
        out.println("</fieldset>");
    }
    
    private void printFieldsWithPointeeTypes(PrintStream out, Collection c, Map map, String name) {
        out.println("<fieldset name = \"" + name + "\">");
        for(Iterator iter = c.iterator(); iter.hasNext();) {
            jq_Field f = (jq_Field)iter.next();
            
            out.println("\t<field name=\""  + f + "\" type=\"" + f.getType() + "\">");
            if(map != null && map.containsKey(f)) {
                String s = (String)map.get(f);
                
                //out.println("\t\t<![CDATA[" + s + "]]>");
                out.println(s);
            }
            
            out.println("\t</field>");
        }
        out.println("</fieldset>");
    }

    private void printNodesWithPointeeTypes(PrintStream out, Map methodMap, Map map, String name) {        
        for(Iterator methodIter = methodMap.keySet().iterator(); methodIter.hasNext();) {
            jq_Class clazz = (jq_Class)methodIter.next();
            out.println("<paramset name = \"" + xmlEscape(clazz.toString()) + "\">");
            Collection c = (Collection)methodMap.get(clazz);
            for(Iterator iter = c.iterator(); iter.hasNext();) {
                MethodSummary.ParamNode n = (MethodSummary.ParamNode)iter.next();
                out.println(
                        "\t<param method=\""  + xmlEscape(n.getDefiningMethod().toString()) + 
                        //" \" name=\"" + method. 
                        " \" number=\"" + n.getIndex() + "\">");
                if(map != null && map.containsKey(n)) {
                    String s = (String)map.get(n);
                    
                    //out.println("\t\t<![CDATA[" + s + "]]>");
                    out.println(s);
                }
                
                out.println("\t</param>");
            }
            out.println("</paramset>");
        }        
    }
    
    void printPolyFieldInfo(PrintStream out) throws IOException {
        _r = _builder.getPAResults().getPAResults();
        
        BDD fh2 = _r.hP.exist(_r.H1set); // FxH2
        //int singleTypeFields = 0, singleObjectFields = 0, unusedFields = 0, refinedTypeFields = 0;
        Collection singleTypeFields     = new LinkedList();
        Collection singleObjectFields   = new LinkedList();
        Collection unusedFields         = new LinkedList();
        Collection refinedTypeFields    = new LinkedList();
        Collection nullFields           = new LinkedList();
        
        Map refinedData                 = new HashMap();
        Set polyClasses                 = new HashSet();
        
        for (int i = 0; i < _r.Fmap.size(); ++i) {
            jq_Field f = (jq_Field) _r.Fmap.get(i);
            BDD b = _r.F.ithVar(i);
            BDD c = fh2.restrict(b); // H2
            if (c.isZero()) {
                unusedFields.add(f);
                if(TRACE) System.err.println("Unused field " + f);
                continue;
            }
            c.replaceWith(_r.H2toH1); // H1
            if (c.satCount(_r.H1set) == 1.0) {
                singleObjectFields.add(f);
                if(TRACE) System.err.println("Single object field " + f);
            }
            BDD d = c.relprod(_r.hT, _r.H1set); // T2
            
            if (d.satCount(_r.T2set) == 1.0) {
                singleTypeFields.add(f);
                if(TRACE) System.err.println("Single type field " + f);
            } else {
                if (f != null && !f.isStatic()) {
                    if(TRACE) System.err.println("Poly class " + f.getDeclaringClass());
                    polyClasses.add(f.getDeclaringClass());
                }
            }

            // e is the common supertype of the pointees
            //BDD e = calculateCommonSupertype(d); // T1
            BDD e = calculateConcreteTypes(c, true); // T1
            if(f == null) continue;
            if(e.isOne()) {
                // formerly removed NULL
                nullFields.add(f);
            }else
            if(e.satCount(_r.T2set) > 1){
                refinedTypeFields.add(f);
                
                refinedData.put(f, typesetToString(e));
                //refinedData.put(f, e.toStringWithDomains(_r.TS));
                //System.err.println("Refined type field " + f);
            }
            /*
            if (f != null) {
                int T_i = _r.Tmap.get(f.getType());
                BDD g = _r.T1.ithVar(T_i);      // g is the declared type
                if (!e.equals(g)) {
                    e.replaceWith(_r.T1toT2);
                    // declared type coinsides with the least precise type put into the field
                    //if (e.andWith(g).and(_r.aT).isZero()) {
                        //System.out.println("Field "+f);
                        //System.out.println(" Declared: "+f.getType()+" Computed: "+e.toStringWithDomains(_r.TS));
                    } else {
                        // means that the types of the pointees are different from the declared type --
                        // the declared type is wider than the use type
                                          
                        if(c.satCount(_r.T2set) > 1){
                            refinedTypeFields.add(f);
                            refinedData.put(f, e.toStringWithDomains(_r.TS));
                            //System.err.println("Refined type field " + f);
                        }
                    //}   
                }
                g.free();
            }*/
            d.free();
            e.free();
            c.free();
            b.free();
        }
        
        // output the results
        printFieldsWithPointeeTypes(out, refinedTypeFields, refinedData, "multitype (polymorphic) fields");
        //printFields(out, unusedFields,      "unused fields");
        printFields(out, nullFields,        "null fields");            
        
        if(TRACE) { 
            System.out.println("Refined-type fields: "+refinedTypeFields+" / "+_r.Fmap.size()+" = "+(double)refinedTypeFields.size()/_r.Fmap.size());
            System.out.println("Single-type fields: "+singleTypeFields+" / "+_r.Fmap.size()+" = "+(double)singleTypeFields.size()/_r.Fmap.size());
            System.out.println("Single-object fields: "+singleObjectFields+" / "+_r.Fmap.size()+" = "+(double)singleObjectFields.size()/_r.Fmap.size());
            System.out.println("Unused fields: "+unusedFields+" / "+_r.Fmap.size()+" = "+(double)unusedFields.size()/_r.Fmap.size());
            System.out.println("Poly classes: "+polyClasses.size());
        }
    }
    
    void printPolyParamInfo(PrintStream out) throws IOException {
        _r = _builder.getPAResults().getPAResults();
        
        Map polyMethods     = new HashMap();
        Map polyParamsData  = new HashMap();
 
        for(int i = 0; i < _r.Vmap.size(); ++i) {
            Node node = (Node)_r.Vmap.get(i);
            if(node instanceof MethodSummary.ParamNode) {
                BDD param = _r.V1.ithVar(i);
                MethodSummary.ParamNode pnode = (MethodSummary.ParamNode)node;
                
                BDD h = param.relprod(_r.vP, _r.V1set);
                BDD e = calculateConcreteTypes(h, true);
                jq_Method method = node.getDefiningMethod();
                if(method instanceof jq_Initializer) {
                    // skip the constructors
                    continue;
                }
                
                if(pnode.getIndex() == 0 && method instanceof jq_InstanceMethod && 
                        !((jq_InstanceMethod)method).isStatic())
                {              
                    // polymorphic in "this" argument
                    continue;
                }              
                if( !((TypedBDD)e).getDomainSet().isEmpty() && e.satCount(_r.T2set) > 1 ){
                    //System.out.println(
                    //    method + ": " + node + ": " + method.getParamTypes()[pnode.getIndex()] + " " +
                    //    e.toStringWithDomains(_r.TS));

                    jq_Class clazz = method.getDeclaringClass();
                    Collection c = (Collection)polyMethods.get(clazz); 
                    if(c == null) {
                        c = new LinkedList();
                        polyMethods.put(clazz, c);
                    }
                    c.add(node);                    
                    polyParamsData.put(node, typesetToString(e));
                    //polyParamsData.put(node, e.toStringWithDomains(_r.TS));
                    //System.err.println("Refined type field " + f);
                }
                param.free();
                h.free();
            }
        }
        
        printNodesWithPointeeTypes(out, polyMethods, polyParamsData, "polymorphic parameters");   
    }

    public void run() {
        try {
            PrintStream out = new PrintStream(new DataOutputStream(new FileOutputStream(_outFile)));
            out.println("<?xml version=\"1.0\"?>");
            out.println("<root>");            
                // do the field stat
                printPolyFieldInfo(out);
                // do the param stat
                printPolyParamInfo(out);
            out.println("</root>");
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
        
    private String typesetToString(BDD e) {
        TypedBDDFactory.TypedBDD te = (TypedBDD)e;
        //System.err.println(te.getDomainSet());
        if(te.getDomainSet().isEmpty()){
            return "";
        }
        Assert._assert(te.getDomainSet().contains(_r.T2));

        StringBuffer result = 
            new StringBuffer("\t\t<typeset size=\"" + (int)e.satCount(_r.T2set) + "\"> \n"); 
        for(Iterator iter2 = e.iterator(_r.T2set); iter2.hasNext();) {
            BDD tt = (BDD)iter2.next();
            jq_Type type = getType((TypedBDD)tt);
            Assert._assert(type != null);
            boolean isAbstract  = type instanceof jq_Class && ((jq_Class)type).isAbstract();
            boolean isInterface = type instanceof jq_Class && ((jq_Class)type).isInterface();
            
            result.append(
                    "\t\t\t<type " +
                    "abstract=\""  + (isAbstract ?"yes":"no") + "\" " + 
                    "interface=\"" + (isInterface?"yes":"no") +
                    "\">" +  
                    "<![CDATA[" + type.toString() + "]]>"
                    + "</type> \n");
        }
        
        result.append("\t\t</typeset>\n");
        
        return result.toString();
    }
}

