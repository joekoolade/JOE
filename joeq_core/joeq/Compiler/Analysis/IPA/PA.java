// PA.java, created Oct 16, 2003 3:39:34 PM by joewhaley
// Copyright (C) 2003 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.Analysis.IPA;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Array;
import joeq.Class.jq_Class;
import joeq.Class.jq_FakeInstanceMethod;
import joeq.Class.jq_FakeStaticMethod;
import joeq.Class.jq_Field;
import joeq.Class.jq_Initializer;
import joeq.Class.jq_InstanceField;
import joeq.Class.jq_InstanceMethod;
import joeq.Class.jq_Method;
import joeq.Class.jq_MethodVisitor;
import joeq.Class.jq_NameAndDesc;
import joeq.Class.jq_Reference;
import joeq.Class.jq_Type;
import joeq.Class.jq_Reference.jq_NullType;
import joeq.Compiler.Analysis.BDD.BuildBDDIR;
import joeq.Compiler.Analysis.FlowInsensitive.BogusSummaryProvider;
import joeq.Compiler.Analysis.FlowInsensitive.MethodSummary;
import joeq.Compiler.Analysis.FlowInsensitive.ReflectionInformationProvider;
import joeq.Compiler.Analysis.FlowInsensitive.MethodSummary.ConcreteObjectNode;
import joeq.Compiler.Analysis.FlowInsensitive.MethodSummary.ConcreteTypeNode;
import joeq.Compiler.Analysis.FlowInsensitive.MethodSummary.GlobalNode;
import joeq.Compiler.Analysis.FlowInsensitive.MethodSummary.Node;
import joeq.Compiler.Analysis.FlowInsensitive.MethodSummary.ParamNode;
import joeq.Compiler.Analysis.FlowInsensitive.MethodSummary.UnknownTypeNode;
import joeq.Compiler.Analysis.IPA.ProgramLocation.QuadProgramLocation;
import joeq.Compiler.Quad.CachedCallGraph;
import joeq.Compiler.Quad.CallGraph;
import joeq.Compiler.Quad.CodeCache;
import joeq.Compiler.Quad.ControlFlowGraph;
import joeq.Compiler.Quad.ControlFlowGraphVisitor;
import joeq.Compiler.Quad.LoadedCallGraph;
import joeq.Compiler.Quad.Quad;
import joeq.Compiler.Quad.QuadIterator;
import joeq.Compiler.Quad.Operand.RegisterOperand;
import joeq.Compiler.Quad.Operator.Invoke;
import joeq.Compiler.Quad.RegisterFactory.Register;
import joeq.Main.HostedVM;
import joeq.UTF.Utf8;
import joeq.Util.NameMunger;
import jwutil.collections.IndexMap;
import jwutil.collections.IndexedMap;
import jwutil.collections.Pair;
import jwutil.graphs.DumpDotGraph;
import jwutil.graphs.GlobalPathNumbering;
import jwutil.graphs.Navigator;
import jwutil.graphs.PathNumbering;
import jwutil.graphs.RootPathNumbering;
import jwutil.graphs.SCCPathNumbering;
import jwutil.graphs.SCCTopSortedGraph;
import jwutil.graphs.SCComponent;
import jwutil.graphs.Traversals;
import jwutil.graphs.PathNumbering.Range;
import jwutil.graphs.PathNumbering.Selector;
import jwutil.io.SystemProperties;
import jwutil.io.Textualizable;
import jwutil.io.Textualizer;
import jwutil.util.Assert;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDBitVector;
import net.sf.javabdd.BDDDomain;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;
import net.sf.javabdd.TypedBDDFactory;
import net.sf.javabdd.TypedBDDFactory.TypedBDD;
/**
 * Pointer analysis using BDDs.  Includes both context-insensitive and context-sensitive
 * analyses.  This version corresponds exactly to the description in the paper.
 * All of the inference rules are direct copies.
 * 
 * @author John Whaley
 * @version $Id: PA.java,v 1.355 2006/04/06 02:32:09 mcmartin Exp $
 */
public class PA {
    // Read in default properties.
    static { SystemProperties.read("pa.properties"); }

    public static final boolean VerifyAssertions = false;

    static boolean WRITE_PARESULTS_BATCHFILE = !System.getProperty("pa.writeparesults", "yes").equals("no");

    boolean TRACE = !System.getProperty("pa.trace", "no").equals("no");
    boolean TRACE_SOLVER = !System.getProperty("pa.tracesolver", "no").equals("no");
    boolean TRACE_BIND = !System.getProperty("pa.tracebind", "no").equals("no");
    boolean TRACE_RELATIONS = !System.getProperty("pa.tracerelations", "no").equals("no");
    boolean TRACE_OBJECT = !System.getProperty("pa.traceobject", "no").equals("no");
    boolean TRACE_CONTEXT = !System.getProperty("pa.tracecontext", "no").equals("no");
    boolean TRACE_PLACEHOLDERS = !System.getProperty("pa.traceplaceholders", "no").equals("no");
    boolean TRACE_SELECTORS = !System.getProperty("pa.traceselectors", "no").equals("no");
    PrintStream out = System.out;
    boolean DUMP_INITIAL = !System.getProperty("pa.dumpinitial", "no").equals("no");
    boolean DUMP_RESULTS = !System.getProperty("pa.dumpresults", "yes").equals("no");
    boolean DUMP_CALLGRAPH = true;
    boolean DUMP_FLY = !System.getProperty("pa.dumpfly", "no").equals("no");
    boolean DUMP_SSA = !System.getProperty("pa.dumpssa", "no").equals("no");
    boolean SKIP_SOLVE = !System.getProperty("pa.skipsolve", "no").equals("no");
    static boolean USE_JOEQ_CLASSLIBS = !System.getProperty("pa.usejoeqclasslibs", "no").equals("no");

    boolean INCREMENTAL1 = !System.getProperty("pa.inc1", "yes").equals("no"); // incremental points-to
    boolean INCREMENTAL2 = !System.getProperty("pa.inc2", "yes").equals("no"); // incremental parameter binding
    boolean INCREMENTAL3 = !System.getProperty("pa.inc3", "yes").equals("no"); // incremental invocation binding
    
    boolean ADD_CLINIT = !System.getProperty("pa.clinit", "yes").equals("no");
    boolean ADD_THREADS = !System.getProperty("pa.threads", "yes").equals("no");
    boolean ADD_FINALIZERS = !System.getProperty("pa.finalizers", "yes").equals("no");
    boolean IGNORE_EXCEPTIONS = !System.getProperty("pa.ignoreexceptions", "no").equals("no");
    boolean FILTER_VP = !System.getProperty("pa.vpfilter", "yes").equals("no");
    boolean FILTER_HP = !System.getProperty("pa.hpfilter", "no").equals("no");
    boolean CARTESIAN_PRODUCT = !System.getProperty("pa.cp", "no").equals("no");
    boolean THREAD_SENSITIVE = !System.getProperty("pa.ts", "no").equals("no");
    boolean OBJECT_SENSITIVE = !System.getProperty("pa.os", "no").equals("no");
    boolean CONTEXT_SENSITIVE = !System.getProperty("pa.cs", "no").equals("no");
    boolean BETTER_CONTEXT_NUMBERING = !System.getProperty("pa.bettercontextnumbering", "no").equals("no");
    boolean USE_STRING_METHOD_SELECTOR = !System.getProperty("pa.stringmethodselector", "no").equals("no");
    boolean CS_CALLGRAPH = !System.getProperty("pa.cscg", "no").equals("no");
    boolean DISCOVER_CALL_GRAPH = !System.getProperty("pa.discover", "no").equals("no");
    boolean ALWAYS_START_WITH_A_FRESH_CALLGRAPH = !System.getProperty("pa.startwithfreshcg", "no").equals("no");
    boolean PRINT_CALL_GRAPH_SCCS = !System.getProperty("pa.printsccs", "no").equals("no");
    boolean AUTODISCOVER_CALL_GRAPH = !System.getProperty("pa.autodiscover", "yes").equals("no");
    boolean DUMP_DOTGRAPH = !System.getProperty("pa.dumpdotgraph", "no").equals("no");
    boolean FILTER_NULL = !System.getProperty("pa.filternull", "yes").equals("no");
    boolean LONG_LOCATIONS = !System.getProperty("pa.longlocations", "no").equals("no");
    boolean DUMP_UNMUNGED_NAMES = !System.getProperty("pa.dumpunmunged", "no").equals("no");
    boolean INCLUDE_UNKNOWN_TYPES = !System.getProperty("pa.unknowntypes", "yes").equals("no");
    boolean INCLUDE_ALL_UNKNOWN_TYPES = !System.getProperty("pa.allunknowntypes", "no").equals("no");
    boolean ADD_SUPERTYPES = !System.getProperty("pa.addsupertypes", "no").equals("no");
    boolean ADD_HEAP_FILTER = !System.getProperty("pa.addheapfilter", "no").equals("no");
    int ADD_ROOT_PLACEHOLDERS = Integer.parseInt(System.getProperty("pa.addrootplaceholders", "0"));
    int PUBLIC_PLACEHOLDERS = Integer.parseInt(System.getProperty("pa.publicplaceholders", "0"));
    boolean FULL_CHA = !System.getProperty("pa.fullcha", "no").equals("no");
    static boolean ADD_INSTANCE_METHODS = !System.getProperty("pa.addinstancemethods", "no").equals("no");
    public boolean USE_BOGUS_SUMMARIES = !System.getProperty("pa.usebogussummaries", "no").equals("no");
    boolean USE_REFLECTION_PROVIDER = !System.getProperty("pa.usereflectionprovider", "no").equals("no");
    boolean RESOLVE_REFLECTION = !System.getProperty("pa.resolvereflection", "no").equals("no");
    boolean USE_CASTS_FOR_REFLECTION = !System.getProperty("pa.usecastsforreflection", "no").equals("no");
    boolean RESOLVE_FORNAME = !System.getProperty("pa.resolveforname", "no").equals("no");
    boolean TRACE_BOGUS = !System.getProperty("pa.tracebogus", "no").equals("no");
    boolean FIX_NO_DEST = !System.getProperty("pa.fixnodest", "no").equals("no");
    boolean TRACE_NO_DEST = !System.getProperty("pa.tracenodest", "no").equals("no");
    boolean REFLECTION_STAT = !System.getProperty("pa.reflectionstat", "no").equals("no");
    boolean FORNAME_STAT = !System.getProperty("pa.fornamestat", "no").equals("no");
    String REFLECTION_STAT_FILE = System.getProperty("pa.reflectionstatfile", "reflection.txt");
    String FORNAME_STAT_FILE = System.getProperty("pa.fornamestatfile", "forname.txt");
    public static boolean TRACE_REFLECTION = !System.getProperty("pa.tracereflection", "no").equals("no");
    public static boolean TRACE_REFLECTION_DOMAINS = !System.getProperty("pa.tracereflectiondomains", "no").equals("no");
    boolean TRACE_FORNAME = !System.getProperty("pa.traceforname", "no").equals("no");
    int MAX_PARAMS = Integer.parseInt(System.getProperty("pa.maxparams", "4"));
    boolean SPECIAL_MAP_INFO = !System.getProperty("pa.specialmapinfo", "no").equals("no");
    
    int bddnodes = Integer.parseInt(System.getProperty("bddnodes", "2500000"));
    int bddcache = Integer.parseInt(System.getProperty("bddcache", "200000"));
    double bddminfree = Double.parseDouble(System.getProperty("bddminfree", ".20"));
    static String resultsFileName = System.getProperty("pa.results", "pa");
    static String callgraphFileName = System.getProperty("pa.callgraph", "callgraph");
    static String initialCallgraphFileName = System.getProperty("pa.icallgraph", callgraphFileName);
    
    boolean USE_VCONTEXT;
    boolean USE_HCONTEXT;
    
    Map newMethodSummaries = new HashMap();
    Set rootMethods = new HashSet();
    
    CallGraph cg;
    ObjectCreationGraph ocg;
    
    BDDFactory bdd;
    
    BDDDomain V1, V2, I, I2, H1, H2, Z, F, T1, T2, N, M, M2, C, STR;
    BDDDomain V1c[], V2c[], H1c[], H2c[];
    
    int V_BITS=19, I_BITS=19, H_BITS=16, Z_BITS=6, F_BITS=14, T_BITS=14, N_BITS=16, M_BITS=16, C_BITS=12;
    int STR_BITS=H_BITS;
    int VC_BITS=0, HC_BITS=0;
    int MAX_VC_BITS = Integer.parseInt(System.getProperty("pa.maxvc", "61"));
    int MAX_HC_BITS = Integer.parseInt(System.getProperty("pa.maxhc", "0"));
    
    IndexMap/*Node*/ Vmap;
    IndexMap/*ProgramLocation*/ Imap;
    IndexedMap/*Node*/ Hmap;
    IndexMap/*jq_Field*/ Fmap;
    IndexMap/*jq_Reference*/ Tmap;
    IndexMap/*jq_Method*/ Nmap;
    IndexMap/*jq_Method*/ Mmap;
    IndexMap/*jq_Method*/ Cmap;
    IndexMap/*String*/ STRmap;
    PathNumbering vCnumbering; // for context-sensitive
    PathNumbering hCnumbering; // for context-sensitive
    PathNumbering oCnumbering; // for object-sensitive
    
    BDD A;      // V1xV2, arguments and return values   (+context)
    BDD vP;     // V1xH1, variable points-to            (+context)
    BDD S;      // (V1xF)xV2, stores                    (+context)
    BDD L;      // (V1xF)xV2, loads                     (+context)
    BDD vT;     // V1xT1, variable type                 (no context)
    BDD hT;     // H1xT2, heap type                     (no context)
    BDD aT;     // T1xT2, assignable types              (no context)
    BDD cha;    // T2xNxM, class hierarchy information  (no context)
    BDD actual; // IxZxV2, actual parameters            (no context)
    BDD formal; // MxZxV1, formal parameters            (no context)
    BDD Iret;   // IxV1, invocation return value        (no context)
    BDD Mret;   // MxV2, method return value            (no context)
    BDD Ithr;   // IxV1, invocation thrown value        (no context)
    BDD Mthr;   // MxV2, method thrown value            (no context)
    BDD mI;     // MxIxN, method invocations            (no context)
    BDD mV;     // MxV, method variables                (no context)
    BDD mC;     // MxC, method class                    (no context)
    BDD sync;   // V, synced locations                  (no context)
    BDD mSync;  // M, synchronized, non-static methods  (no context)
    

    BDD fT;     // FxT2, field types                    (no context)
    BDD fC;     // FxT2, field containing types         (no context)

    BDD hP;     // H1xFxH2, heap points-to              (+context)
    BDD IEcs;   // V2cxIxV1cxM, context-sensitive invocation edges
    BDD IE;     // IxM, invocation edges                (no context)
    BDD vPfilter; // V1xH1, type filter                 (no context)
    BDD hPfilter; // H1xFxH2, type filter               (no context)
    BDD NNfilter; // H1, non-null filter                (no context)
    BDD IEfilter; // V2cxIxV1cxM, context-sensitive edge filter
    
    BDD visited; // M, visited methods
    
    // maps to SSA form
    BDD forNameMap; // IxH1, heap allocation sites for forClass.Name
    BuildBDDIR bddIRBuilder;
    BDD vReg; // Vxreg
    BDD iQuad; // Ixquad
    BDD hQuad; // Hxquad
    BDD fMember; //Fxmember
    BDD staticCalls; // V1xIxM, statically-bound calls, only used for object-sensitive and cartesian product

    // For analysis of maps
    BDD stringConstant; // H1xSTR, string constants     (no context)
    BDD overridesEqualsOrHashcode; // T1                (no context) 

    boolean reverseLocal = System.getProperty("bddreverse", "true").equals("true");
    String varorder = System.getProperty("bddordering");
    
    BDDPairing V1toV2, V2toV1, H1toH2, ItoI2, I2toI, H2toH1, V1H1toV2H2, V2H2toV1H1;
    BDDPairing V1ctoV2c, V1cV2ctoV2cV1c, V1cH1ctoV2cV1c;
    BDDPairing T2toT1, T1toT2;
    BDDPairing H1toV1c[], V1ctoH1[]; BDD V1csets[], V1cH1equals[];
    BDD V1set, V2set, H1set, H2set, T1set, T2set, Fset, Mset, Nset, Cset, Iset, I2set, Zset;
    BDD V1V2set, V1Fset, V2Fset, V1FV2set, V1H1set, H1Fset, H2Fset, H1H2set, H1FH2set;
    BDD IMset, INset, INH1set, INT2set, T2Nset, MZset;
    BDD V1cset, V2cset, H1cset, H2cset, V1cV2cset, V1cH1cset, H1cH2cset;
    BDD V1cdomain, V2cdomain, H1cdomain, H2cdomain;
    
    BDDDomain makeDomain(String name, int bits) {
        Assert._assert(bits < 64);
        BDDDomain d = bdd.extDomain(new long[] { 1L << bits })[0];
        d.setName(name);
        return d;
    }
    IndexMap makeMap(String name, int bits) {
        return new IndexMap(name, 1 << bits);
    }
    
    public void initializeBDD(String bddfactory) {
        USE_VCONTEXT = VC_BITS > 0;
        USE_HCONTEXT = HC_BITS > 0;
        
        if (USE_VCONTEXT || USE_HCONTEXT) bddnodes *= 2;
        
        if (bddfactory == null)
            bdd = BDDFactory.init(bddnodes, bddcache);
        else
            bdd = BDDFactory.init(bddfactory, bddnodes, bddcache);
        //bdd.setMaxIncrease(bddnodes/4);
        bdd.setIncreaseFactor(2);
        bdd.setMinFreeNodes(bddminfree);
        
        V1 = makeDomain("V1", V_BITS);
        V2 = makeDomain("V2", V_BITS);
        I = makeDomain("I", I_BITS);
        I2 = makeDomain("I2", I_BITS);
        H1 = makeDomain("H1", H_BITS);
        H2 = makeDomain("H2", H_BITS);
        Z = makeDomain("Z", Z_BITS);
        F = makeDomain("F", F_BITS);
        T1 = makeDomain("T1", T_BITS);
        T2 = makeDomain("T2", T_BITS);
        N = makeDomain("N", N_BITS);
        M = makeDomain("M", M_BITS);
        C = makeDomain("C", C_BITS);
        M2 = makeDomain("M2", M_BITS);
        
        if (CONTEXT_SENSITIVE || OBJECT_SENSITIVE || THREAD_SENSITIVE) {
            V1c = new BDDDomain[1];
            V2c = new BDDDomain[1];
            V1c[0] = makeDomain("V1c", VC_BITS);
            V2c[0] = makeDomain("V2c", VC_BITS);
        } else if (CARTESIAN_PRODUCT && false) {
            V1c = new BDDDomain[MAX_PARAMS];
            V2c = new BDDDomain[MAX_PARAMS];
            for (int i = 0; i < V1c.length; ++i) {
                V1c[i] = makeDomain("V1c"+i, H_BITS + HC_BITS);
            }
            for (int i = 0; i < V2c.length; ++i) {
                V2c[i] = makeDomain("V2c"+i, H_BITS + HC_BITS);
            }
        } else {
            V1c = V2c = new BDDDomain[0];
        }
        if (USE_HCONTEXT) {
            H1c = new BDDDomain[] { makeDomain("H1c", HC_BITS) };
            H2c = new BDDDomain[] { makeDomain("H2c", HC_BITS) };
        } else {
            H1c = H2c = new BDDDomain[0];
        }
                
        if (TRACE) out.println("Variable context domains: "+V1c.length);
        if (TRACE) out.println("Heap context domains: "+H1c.length);
        
        if (varorder == null) {
            // default variable orderings.
            if (CONTEXT_SENSITIVE || THREAD_SENSITIVE || OBJECT_SENSITIVE) {
                if (HC_BITS > 0) {
                    varorder = "C_N_F_Z_I_I2_M2_M_T1_V2xV1_V2cxV1c_H2xH2c_T2_H1xH1c";
                } else {
                    //varorder = "N_F_Z_I_M2_M_T1_V2xV1_V2cxV1c_H2_T2_H1";
                    varorder = "C_N_F_I_I2_M2_M_Z_V2xV1_V2cxV1c_T1_H2_T2_H1";
//                    varorder = "C0_N0_F0_I0_M1_M0_V1xV0_VC1xVC0_T0_Z0_T1_H0_H1";
                }
            } else if (CARTESIAN_PRODUCT && false) {
                varorder = "C_N_F_Z_I_I2_M2_M_T1_V2xV1_T2_H2xH1";
                for (int i = 0; i < V1c.length; ++i) {
                    varorder += "xV1c"+i+"xV2c"+i;
                }
            } else {
//                varorder = "N_F_Z_I_M2_M_T1_V2xV1_H2_T2_H1";
                varorder = "C_N_F_I_I2_M2_M_Z_V2xV1_T1_H2_T2_H1";
            }
        }

        if (SPECIAL_MAP_INFO) {
            STR = makeDomain("STR", STR_BITS);
            varorder += "_STR";
        }
        
        System.out.println("Using variable ordering "+varorder);
        int[] ordering = bdd.makeVarOrdering(reverseLocal, varorder);
        bdd.setVarOrder(ordering);
        
        V1ctoV2c = bdd.makePair();
        V1ctoV2c.set(V1c, V2c);
        V1cV2ctoV2cV1c = bdd.makePair();
        V1cV2ctoV2cV1c.set(V1c, V2c);
        V1cV2ctoV2cV1c.set(V2c, V1c);
        if (OBJECT_SENSITIVE) {
            V1cH1ctoV2cV1c = bdd.makePair();
            V1cH1ctoV2cV1c.set(V1c, V2c);
            V1cH1ctoV2cV1c.set(H1c, V1c);
        }
        T2toT1 = bdd.makePair(T2, T1);
        T1toT2 = bdd.makePair(T1, T2);
        V1toV2 = bdd.makePair();
        V1toV2.set(V1, V2);
        V1toV2.set(V1c, V2c);
        V2toV1 = bdd.makePair();
        V2toV1.set(V2, V1);
        V2toV1.set(V2c, V1c);
        H1toH2 = bdd.makePair();
        H1toH2.set(H1, H2);
        H1toH2.set(H1c, H2c);
        ItoI2 = bdd.makePair();
        ItoI2.set(I, I2);
        I2toI = bdd.makePair();
        I2toI.set(I2, I);
        H2toH1 = bdd.makePair();
        H2toH1.set(H2, H1);
        H2toH1.set(H2c, H1c);
        V1H1toV2H2 = bdd.makePair();
        V1H1toV2H2.set(V1, V2);
        V1H1toV2H2.set(H1, H2);
        V1H1toV2H2.set(V1c, V2c);
        V1H1toV2H2.set(H1c, H2c);
        V2H2toV1H1 = bdd.makePair();
        V2H2toV1H1.set(V2, V1);
        V2H2toV1H1.set(H2, H1);
        V2H2toV1H1.set(V2c, V1c);
        V2H2toV1H1.set(H2c, H1c);
        
        V1set = V1.set();
        if (V1c.length > 0) {
            V1cset = bdd.one();
            V1cdomain = bdd.one();
            for (int i = 0; i < V1c.length; ++i) {
                V1cset.andWith(V1c[i].set());
                V1cdomain.andWith(V1c[i].domain());
            }
            V1set.andWith(V1cset.id());
        }
        V2set = V2.set();
        if (V2c.length > 0) {
            V2cset = bdd.one();
            V2cdomain = bdd.one();
            for (int i = 0; i < V2c.length; ++i) {
                V2cset.andWith(V2c[i].set());
                V2cdomain.andWith(V2c[i].domain());
            }
            V2set.andWith(V2cset.id());
        }
        H1set = H1.set();
        if (H1c.length > 0) {
            H1cset = bdd.one();
            H1cdomain = bdd.one();
            for (int i = 0; i < H1c.length; ++i) {
                H1cset.andWith(H1c[i].set());
                H1cdomain.andWith(H1c[i].domain());
            }
            H1set.andWith(H1cset.id());
        }
        H2set = H2.set();
        if (H2c.length > 0) {
            H2cset = bdd.one();
            H2cdomain = bdd.one();
            for (int i = 0; i < H2c.length; ++i) {
                H2cset.andWith(H2c[i].set());
                H2cdomain.andWith(H2c[i].domain());
            }
            H2set.andWith(H2cset.id());
        }
        T1set = T1.set();
        T2set = T2.set();
        Fset = F.set();
        Mset = M.set();
        Nset = N.set();
        Cset = C.set();
        Iset = I.set();
        I2set = I2.set();
        Zset = Z.set();
        V1cV2cset = (V1c.length > 0) ? V1cset.and(V2cset) : bdd.zero();
        H1cH2cset = (H1c.length > 0) ? H1cset.and(H2cset) : bdd.zero();
        if (V1c.length > 0) {
            V1cH1cset = (H1c.length > 0) ? V1cset.and(H1cset) : V1cset;
        } else {
            V1cH1cset = (H1c.length > 0) ? H1cset : bdd.zero();
        }
        V1V2set = V1set.and(V2set);
        V1FV2set = V1V2set.and(Fset);
        V1H1set = V1set.and(H1set);
        V1Fset = V1set.and(Fset);
        V2Fset = V2set.and(Fset);
        IMset = Iset.and(Mset);
        INset = Iset.and(Nset);
        INH1set = INset.and(H1set);
        INT2set = INset.and(T2set);
        H1Fset = H1set.and(Fset);
        H2Fset = H2set.and(Fset);
        H1H2set = H1set.and(H2set);
        H1FH2set = H1Fset.and(H2set);
        T2Nset = T2set.and(Nset);
        MZset = Mset.and(Zset);
        
        A = bdd.zero();
        vP = bdd.zero();
        S = bdd.zero();
        L = bdd.zero();
        vT = bdd.zero();
        hT = bdd.zero();
        aT = bdd.zero();
        if (FILTER_HP) {
            fT = bdd.zero();
            fC = bdd.zero();
        }
        cha = bdd.zero();
        actual = bdd.zero();
        formal = bdd.zero();
        Iret = bdd.zero();
        Mret = bdd.zero();
        Ithr = bdd.zero();
        Mthr = bdd.zero();
        mI = bdd.zero();
        mV = bdd.zero();
        mC = bdd.zero();
        sync = bdd.zero();
        mSync = bdd.zero();
        IE = bdd.zero();
        hP = bdd.zero();
        visited = bdd.zero();
        forNameMap =  bdd.zero();
        
        if (OBJECT_SENSITIVE || CARTESIAN_PRODUCT) staticCalls = bdd.zero();
        
        if (THREAD_SENSITIVE) threadRuns = bdd.zero();
        
        if (INCREMENTAL1) {
            old1_A = bdd.zero();
            old1_S = bdd.zero();
            old1_L = bdd.zero();
            old1_vP = bdd.zero();
            old1_hP = bdd.zero();
        }
        if (INCREMENTAL2) {
            old2_myIE = bdd.zero();
            old2_visited = bdd.zero();
        }
        if (INCREMENTAL3) {
            old3_t3 = bdd.zero();
            old3_vP = bdd.zero();
            old3_t4 = bdd.zero();
            old3_hT = bdd.zero();
            old3_t6 = bdd.zero();
            old3_t9 = new BDD[MAX_PARAMS];
            for (int i = 0; i < old3_t9.length; ++i) {
                old3_t9[i] = bdd.zero();
            }
        }
        
        reflectiveCalls = bdd.zero();
        
        if (CARTESIAN_PRODUCT && false) {
            H1toV1c = new BDDPairing[MAX_PARAMS];
            V1ctoH1 = new BDDPairing[MAX_PARAMS];
            V1csets = new BDD[MAX_PARAMS];
            V1cH1equals = new BDD[MAX_PARAMS];
            for (int i = 0; i < MAX_PARAMS; ++i) {
                H1toV1c[i] = bdd.makePair(H1, V1c[i]);
                V1ctoH1[i] = bdd.makePair(V1c[i], H1);
                V1csets[i] = V1c[i].set();
                V1cH1equals[i] = H1.buildEquals(V1c[i]);
            }
        }
        
        if (USE_VCONTEXT) {
            IEcs = bdd.zero();
        }
    }
    
    void initializeMaps() {
        Vmap = makeMap("Vars", V_BITS);
        Imap = makeMap("Invokes", I_BITS);
        Hmap = makeMap("Heaps", H_BITS);
        Fmap = makeMap("Fields", F_BITS);
        Tmap = makeMap("Types", T_BITS);
        Nmap = makeMap("Names", N_BITS);
        Mmap = makeMap("Methods", M_BITS);
        Cmap = makeMap("Classes", C_BITS);
        if (SPECIAL_MAP_INFO) {
            STRmap = makeMap("Strings", STR_BITS);
            STRmap.get(new Dummy());
        }
        Mmap.get(new Dummy());
        if (ADD_THREADS) {
            PrimordialClassLoader.getJavaLangThread().prepare();
            PrimordialClassLoader.loader.getOrCreateBSType("Ljava/lang/Runnable;").prepare();
        }
    }
    
    void addToVisited(BDD M_bdd) {
        if (TRACE_RELATIONS) out.println("Adding to visited: "+M_bdd.toStringWithDomains());
        visited.orWith(M_bdd.id());
    }
    
    public void addToForNameMap(ConcreteTypeNode h, BDD i_bdd) {
        BDD H_i = H1.ithVar(Hmap.get(h));
        H_i.andWith(i_bdd.id());
        forNameMap.orWith(H_i);
         
        //System.out.println("forNameMap: " + forNameMap.toStringWithDomains(TS));
    }
    
    void addToFormal(BDD M_bdd, int z, Node v) {
        BDD bdd1 = Z.ithVar(z);
        int V_i = Vmap.get(v);
        bdd1.andWith(V1.ithVar(V_i));
        bdd1.andWith(M_bdd.id());
        if (TRACE_RELATIONS) out.println("Adding to formal: "+bdd1.toStringWithDomains());
        formal.orWith(bdd1);
    }
    
    void addToIE(BDD I_bdd, jq_Method target) {
        int M2_i = Mmap.get(target);
        BDD bdd1 = M.ithVar(M2_i);
        bdd1.andWith(I_bdd.id());
        if (TRACE_RELATIONS) out.println("Adding to IE: "+bdd1.toStringWithDomains());
        if (USE_VCONTEXT && IEfilter != null) {
            // When doing context-sensitive analysis, we need to add to IEcs too.
            // This call edge is true under all contexts for this invocation.
            // "and"-ing with IEfilter achieves this.
            IEcs.orWith(bdd1.and(IEfilter));
        }
        IE.orWith(bdd1);
    }
    
    /**
     * Finds all invocation sites with no targets and tries to create targets from them.
     * */
    void analyzeIE(){
        int noTargetCalls = 0;
        for(Iterator iter = Imap.iterator(); iter.hasNext();){
            ProgramLocation mc = (ProgramLocation) iter.next();
            int I_i = Imap.get(mc);
            BDD I_bdd = (BDD) I.ithVar(I_i);
            BDD t = IE.relprod(I_bdd, Iset); 
            
            if(t.isZero()){
                if(TRACE_NO_DEST) {
                    System.out.println("No destination for " + mc.toStringLong());                
                }
                BDD V_bdd = actual.relprod(I_bdd, Iset).restrictWith(Z.ithVar(0));
                int V_i = V_bdd.scanVar(V2).intValue();
                if(V_i == -1){
                    System.out.println("Index " + V_i + " is scanning " + V_bdd.toStringWithDomains(TS));
                    continue;
                }
                if(V_i >= Vmap.size()) {
                    // TODO: this is kind of weird. Why does this happen?
                    System.out.println("Index " + V_i + " is greater than the map size: " + Vmap.size());
                    continue;
                }
                Node n = (Node) Vmap.get(V_i);
                jq_Reference type = n.getDeclaredType();
                if (type instanceof jq_Class) {
                    jq_Class c = (jq_Class) type;
                    c.prepare();
                    if(!provideStubsFor.contains(c)){
                        continue;
                    }
                }else{
                    continue;
                }
                ConcreteTypeNode h = ConcreteTypeNode.get(type, mc);
                addToVP(V_bdd.replace(V2toV1), h);
                V_bdd.free();
                noTargetCalls++;
            }
            t.free();
            I_bdd.free();
        }
        if(TRACE_NO_DEST) {
            System.out.println("There are " + noTargetCalls + " calls without destinations.");
        }
        
        // pick up the new methods
        iterate();
        
        if(TRACE_NO_DEST) {
            traceNoDestanation();
        }
    }
    /**
     *  Finds invocation sites with no destinations.
     */
    void traceNoDestanation() {
        System.out.println("IE consists of " + IE.satCount(Iset.and(Mset)) + " elements.");
        for(Iterator iter = IE.iterator(Iset.and(Mset)); iter.hasNext(); ) {
            BDD b = (BDD) iter.next();
            int I_i = b.scanVar(I).intValue();
            int M_i = b.scanVar(M).intValue();
            System.out.println("\t" + I_i + "(" + Imap.get(I_i) + ")\t->\t" + "(" + Mmap.get(M_i) + ")");
            b.free();
        }
        for(Iterator iter = Imap.iterator(); iter.hasNext();){
            ProgramLocation mc = (ProgramLocation) iter.next();
            int I_i = Imap.get(mc);
            BDD I_bdd = (BDD) I.ithVar(I_i);
            BDD t = IE.relprod(I_bdd, Iset); 
            
            if(t.isZero()){                
                System.out.println("No destination for " + I_i + "(" + mc.toStringLong() + ")");
                
            }
            t.free();
            I_bdd.free();
        }
    }
    
    void addToMI(BDD M_bdd, BDD I_bdd, jq_Method target) {
        int N_i = Nmap.get(target);
        BDD bdd1 = N.ithVar(N_i);
        bdd1.andWith(M_bdd.id());
        bdd1.andWith(I_bdd.id());
        if (TRACE_RELATIONS) out.println("Adding to mI: "+bdd1.toStringWithDomains());
        mI.orWith(bdd1);
    }
    
    void addToActual(BDD I_bdd, int z, Set s) {
        BDD bdd1 = bdd.zero();
        for (Iterator j = s.iterator(); j.hasNext(); ) {
            int V_i = Vmap.get(j.next());
            bdd1.orWith(V2.ithVar(V_i));
        }
        bdd1.andWith(Z.ithVar(z));
        bdd1.andWith(I_bdd.id());
        if (TRACE_RELATIONS) out.println("Adding to actual: "+bdd1.toStringWithDomains());
        actual.orWith(bdd1);
    }
    
    void addEmptyActual(BDD I_bdd, int z) {
        if (CARTESIAN_PRODUCT) {
            BDD bdd1 = V2.ithVar(0); // global node
            bdd1.andWith(Z.ithVar(z));
            bdd1.andWith(I_bdd.id());
            if (TRACE_RELATIONS) out.println("Adding empty to actual: "+bdd1.toStringWithDomains());
            actual.orWith(bdd1);
        }
    }
    
    void addToIret(BDD I_bdd, Node v) {
        int V_i = Vmap.get(v);
        BDD bdd1 = V1.ithVar(V_i);
        bdd1.andWith(I_bdd.id());
        if (TRACE_RELATIONS) out.println("Adding to Iret: "+bdd1.toStringWithDomains());
        Iret.orWith(bdd1);
    }
    
    void addToIthr(BDD I_bdd, Node v) {
        int V_i = Vmap.get(v);
        BDD bdd1 = V1.ithVar(V_i);
        bdd1.andWith(I_bdd.id());
        if (TRACE_RELATIONS) out.println("Adding to Ithr: "+bdd1.toStringWithDomains());
        Ithr.orWith(bdd1);
    }
    
    void addToMV(BDD M_bdd, BDD V_bdd) {
        BDD bdd1 = M_bdd.id();
        bdd1.andWith(V_bdd.id());
        if (TRACE_RELATIONS) out.println("Adding to mV: "+bdd1.toStringWithDomains());
        mV.orWith(bdd1);
    }
    
    void addToMret(BDD M_bdd, Node v) {
        addToMret(M_bdd, Vmap.get(v));
    }
    
    void addToMret(BDD M_bdd, int V_i) {
        BDD bdd1 = V2.ithVar(V_i);
        bdd1.andWith(M_bdd.id());
        if (TRACE_RELATIONS) out.println("Adding to Mret: "+bdd1.toStringWithDomains());
        Mret.orWith(bdd1);
    }
    
    void addToMthr(BDD M_bdd, int V_i) {
        BDD bdd1 = V2.ithVar(V_i);
        bdd1.andWith(M_bdd.id());
        if (TRACE_RELATIONS) out.println("Adding to Mthr: "+bdd1.toStringWithDomains());
        Mthr.orWith(bdd1);
    }
    
    void addToVP(Node p, int H_i) {
        BDD context = bdd.one();
        if (USE_VCONTEXT) context.andWith(V1cdomain.id());
        if (USE_HCONTEXT) context.andWith(H1cdomain.id());
        addToVP(context, p, H_i);
        context.free();
    }
    
    void addToVP(BDD V1H1context, Node p, int H_i) {
        int V1_i = Vmap.get(p);
        BDD bdd1 = V1.ithVar(V1_i);
        bdd1.andWith(H1.ithVar(H_i));
        if (V1H1context != null) bdd1.andWith(V1H1context.id());
        if (TRACE_RELATIONS) out.println("Adding to vP: "+bdd1.toStringWithDomains(TS));
        vP.orWith(bdd1);
    }
    
    void addToVP(BDD V_bdd, Node h) {
        BDD context = bdd.one();
        if (USE_VCONTEXT) context.andWith(V1cdomain.id());
        if (USE_HCONTEXT) context.andWith(H1cdomain.id());
        addToVP(context, V_bdd, h);
        context.free();
    }
    
    void addToVP(BDD V1H1context, BDD V_bdd, Node h) {
        if(TRACE_REFLECTION_DOMAINS) {
            out.println("V_bdd: " + getBDDDomains(V_bdd));
        }
        int H_i = Hmap.get(h);
        BDD bdd1 = H1.ithVar(H_i);
        bdd1.andWith(V_bdd.id());
        if (V1H1context != null) bdd1.andWith(V1H1context.id());
        if (TRACE_RELATIONS) 
            out.println("Adding to vP: "+bdd1.toStringWithDomains(TS));
        vP.orWith(bdd1);
    }
    
    void addToHP(int H_i, int F_i, int H2_i) {
        BDD bdd1 = H1.ithVar(H_i);
        bdd1.andWith(F.ithVar(F_i));
        bdd1.andWith(H2.ithVar(H2_i));
        if (TRACE_RELATIONS) out.println("Adding to hP: "+bdd1.toStringWithDomains());
        hP.orWith(bdd1);
    }
    void addToA(int V1_i, int V2_i) {
        BDD context = USE_VCONTEXT ? V1cdomain.and(V2cdomain) : null;
        addToA(context, V1_i, V2_i);
        if (USE_VCONTEXT) context.free();
    }
    
    void addToA(BDD V1V2context, int V1_i, int V2_i) {
        BDD V_bdd = V1.ithVar(V1_i);
        addToA(V1V2context, V_bdd, V2_i);
        V_bdd.free();
    }
    
    void addToA(BDD V_bdd, int V2_i) {
        BDD context = USE_VCONTEXT ? V1cdomain.and(V2cdomain) : null;
        addToA(context, V_bdd, V2_i);
        if (USE_VCONTEXT) context.free();
    }
    
    void addToA(BDD V1V2context, BDD V_bdd, int V2_i) {
        BDD bdd1 = V2.ithVar(V2_i);
        bdd1.andWith(V_bdd.id());
        if (USE_VCONTEXT) bdd1.andWith(V1V2context.id());
        if (TRACE_RELATIONS) out.println("Adding to A: "+bdd1.toStringWithDomains());
        A.orWith(bdd1);
    }
    
    void addToS(BDD V_bdd, jq_Field f, Collection c) {
        BDD context = USE_VCONTEXT ? V1cdomain.and(V2cdomain) : null;
        addToS(context, V_bdd, f, c);
        if (USE_VCONTEXT) context.free();
    }
    
    void addToS(BDD V1V2context, BDD V_bdd, jq_Field f, Collection c) {
        int F_i = Fmap.get(f);
        BDD F_bdd = F.ithVar(F_i);
        for (Iterator k = c.iterator(); k.hasNext(); ) {
            Node node2 = (Node) k.next();
            if (FILTER_NULL && isNullConstant(node2))
                continue;

            int V2_i = Vmap.get(node2);
            BDD bdd1 = V2.ithVar(V2_i);
            bdd1.andWith(F_bdd.id());
            bdd1.andWith(V_bdd.id());
            if (USE_VCONTEXT) bdd1.andWith(V1V2context.id());
            if (TRACE_RELATIONS) out.println("Adding to S: "+bdd1.toStringWithDomains());
            S.orWith(bdd1);
        }
        F_bdd.free();
    }
    
    void addToL(BDD V1V2context, BDD V_bdd, jq_Field f, Collection c) {
        int F_i = Fmap.get(f);
        BDD F_bdd = F.ithVar(F_i);
        for (Iterator k = c.iterator(); k.hasNext(); ) {
            Node node2 = (Node) k.next();
            int V2_i = Vmap.get(node2);
            BDD bdd1 = V2.ithVar(V2_i);
            bdd1.andWith(F_bdd.id());
            bdd1.andWith(V_bdd.id());
            if (USE_VCONTEXT) bdd1.andWith(V1V2context.id());
            if (TRACE_RELATIONS) out.println("Adding to L: "+bdd1.toStringWithDomains());
            L.orWith(bdd1);
        }
        F_bdd.free();
    }
    
    void addToSync(Node n) {
        int V_i = Vmap.get(n);
        BDD bdd1 = V1.ithVar(V_i);
        if (TRACE_RELATIONS) out.println("Adding to sync: "+bdd1.toStringWithDomains());
        sync.orWith(bdd1);
    }
    
    void addToMSync(jq_Method m) {
        mSync.orWith(M.ithVar(Mmap.get(m)));
    }
    
    BDD getVC(ProgramLocation mc, jq_Method callee) {
        if (CONTEXT_SENSITIVE || THREAD_SENSITIVE) {
            Pair p = new Pair(LoadedCallGraph.mapCall(mc), callee);
            Range r_edge = vCnumbering.getEdge(p);
            Range r_caller = vCnumbering.getRange(mc.getMethod());
            if (r_edge == null) {
                out.println("Cannot find edge "+p);
                return V1cdomain.and(V2cdomain);
            }
            BDD context = buildContextMap(V2c[0],
                                          PathNumbering.toBigInt(r_caller.low),
                                          PathNumbering.toBigInt(r_caller.high),
                                          V1c[0],
                                          PathNumbering.toBigInt(r_edge.low),
                                          PathNumbering.toBigInt(r_edge.high));
            return context;
        } else if (OBJECT_SENSITIVE) {
            // One-to-one match if call is on 'this' pointer.
            boolean one_to_one;
            jq_Method caller = mc.getMethod();
            jq_Method target = mc.getTargetMethod();
            if (target.isStatic()) {
                one_to_one = caller.isStatic();
            } else {
                Quad q = ((ProgramLocation.QuadProgramLocation) mc).getQuad();
                RegisterOperand rop = Invoke.getParam(q, 0);
                System.out.println("rop = "+rop);
                one_to_one = rop.getType() == caller.getDeclaringClass();
            }
            jq_Class c;
            if (caller.isStatic()) c = null;
            else c = caller.getDeclaringClass();
            Range r = (Range) rangeMap.get(c);
            System.out.println("Method call: "+mc);
            System.out.println("Range of "+c+" = "+r);
            BDD V1V2context;
            if (r == null) {
                System.out.println("Warning: when getting VC, "+c+" is not in object creation graph.");
                V1V2context = V1cdomain.and(V2cdomain);
                return V1V2context;
            }
            if (one_to_one) {
                int bits = BigInteger.valueOf(r.high.longValue()).bitLength();
                V1V2context = V1c[0].buildAdd(V2c[0], bits, 0L);
                V1V2context.andWith(V1c[0].varRange(r.low.longValue(), r.high.longValue()));
            } else {
                V1V2context = V1c[0].varRange(r.low.longValue(), r.high.longValue());
                V1V2context.andWith(V2c[0].varRange(r.low.longValue(), r.high.longValue()));
            }
            return V1V2context;
        } else if (CARTESIAN_PRODUCT) {
            throw new Error();
        } else {
            return null;
        }
    }
    
    public static BDD buildContextMap(BDDDomain d1, BigInteger startD1, BigInteger endD1,
                                      BDDDomain d2, BigInteger startD2, BigInteger endD2) {
        BDD r;
        BigInteger sizeD1 = endD1.subtract(startD1);
        BigInteger sizeD2 = endD2.subtract(startD2);
        if (sizeD1.signum() == -1) {
            r = d2.varRange(startD2.longValue(), endD2.longValue());
            r.andWith(d1.ithVar(0));
        } else if (sizeD2.signum() == -1) {
            r = d1.varRange(startD1.longValue(), endD1.longValue());
            r.andWith(d2.ithVar(0));
        } else {
            int bits;
            if (endD1.compareTo(endD2) >= 0) { // >=
                bits = endD1.bitLength();
            } else {
                bits = endD2.bitLength();
            }
            long val = startD2.subtract(startD1).longValue();
            r = d1.buildAdd(d2, bits, val);
            if (sizeD2.compareTo(sizeD1) >= 0) { // >=
                // D2 is bigger, or they are equal.
                r.andWith(d1.varRange(startD1.longValue(), endD1.longValue()));
            } else {
                // D1 is bigger.
                r.andWith(d2.varRange(startD2.longValue(), endD2.longValue()));
            }
        }
        return r;
    }
    
    public boolean alreadyVisited(jq_Method m) {
        int M_i = Mmap.get(m);
        BDD M_bdd = M.ithVar(M_i);
        M_bdd.andWith(visited.id());
        boolean result = !M_bdd.isZero();
        M_bdd.free();
        return result;
    }
    
    int opn;
    
    ConcreteTypeNode addPlaceholderObject(jq_Reference type, int depth, ProgramLocation.PlaceholderParameterProgramLocation location) {
        ConcreteTypeNode h = ConcreteTypeNode.get(type, location, new Integer(++opn));
        if(TRACE_PLACEHOLDERS) System.out.println("Initializing " + location.getLocationLabel() + " of type " + type + " at depth " + depth);
        if (depth > 0) {
            if (type.isClassType()) {
                jq_Class c = (jq_Class) type;
                c.prepare();
                jq_InstanceField[] fields = c.getInstanceFields();
                for (int i = 0; i < fields.length; ++i) {
                    jq_Type ft = fields[i].getType(); 
                    if (ft.isReferenceType() && !ft.isAddressType()) {
                        Node h2 = addPlaceholderObject((jq_Reference) ft, depth-1, 
                            new ProgramLocation.PlaceholderParameterProgramLocation(location, "." + fields[i].getName()));
                        int H_i = Hmap.get(h);
                        int F_i = Fmap.get(fields[i]);
                        int H2_i = Hmap.get(h2);
                        addToHP(H_i, F_i, H2_i);
                    }
                }
            } else if (type.isArrayType()) {
                jq_Type at = ((jq_Array) type).getElementType();
                if (at.isReferenceType() && !at.isAddressType()) {
                    Node h2 = addPlaceholderObject((jq_Reference) at, depth-1, 
                        new ProgramLocation.PlaceholderParameterProgramLocation(location, "[]"));
                    int H_i = Hmap.get(h);
                    int F_i = Fmap.get(null);
                    int H2_i = Hmap.get(h2);
                    addToHP(H_i, F_i, H2_i);
                }
            }
        }
        return h;
    }
    
    public void addPlaceholdersForParams(jq_Method m, int depth) {
        if (m.getBytecode() == null) return;
        MethodSummary ms = MethodSummary.getSummary(m);
        opn = 1;
        for (int i = 0; i < ms.getNumOfParams(); ++i) {
            ParamNode pn = ms.getParamNode(i);
            if (pn == null) continue;
            
            ConcreteTypeNode h = addPlaceholderObject(pn.getDeclaredType(), depth-1, 
                new ProgramLocation.PlaceholderParameterProgramLocation(m, "param#" + i));
            int H_i = Hmap.get(h);
            addToVP(pn, H_i);
            if(TRACE_PLACEHOLDERS) System.out.println("Placeholder object for "+pn+": "+h);
        }
    }
    
    public void visitMethod(jq_Method m) {
        if (alreadyVisited(m)) return;
        if (VerifyAssertions && cg != null)
            Assert._assert(cg.getAllMethods().contains(m), m.toString());
        PAMethodSummary s = new PAMethodSummary(this, m);
        if (VerifyAssertions) Assert._assert(newMethodSummaries.get(m) == s);
    }
    
    public void addAllMethods() {
        if (DUMP_FLY) return;
        for (Iterator i = newMethodSummaries.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry e = (Map.Entry) i.next();
            jq_Method m = (jq_Method) e.getKey();
            if(m instanceof jq_FakeInstanceMethod || m instanceof jq_FakeStaticMethod) {
                //System.out.println("Skipping fake " + m);
                continue;
            }
            PAMethodSummary s = (PAMethodSummary) e.getValue();
            BDD V1V2context = getV1V2Context(m);
            BDD V1H1context = getV1H1Context(m);
            s.registerRelations(V1V2context, V1H1context);
            if (V1V2context != null) V1V2context.free();
            if (V1H1context != null) V1H1context.free();
            s.free();
            i.remove();
        }
    }
    
    Map rangeMap;
    
    public BDD getV1V2Context(jq_Method m) {
        //m = unfake(m);
        if (THREAD_SENSITIVE) {
            BDD b = (BDD) V1H1correspondence.get(m);
            BDD c = b.replace(V1ctoV2c);
            return c.andWith(b.id());
        } else if (CONTEXT_SENSITIVE) {
            Assert._assert(vCnumbering != null);
            Range r = vCnumbering.getRange(m);
            if (r == null) {
                System.out.println("Warning: "+m+" is not in the call graph. The call graph might not match the code.");
                return bdd.one();
            }
            int bits = BigInteger.valueOf(r.high.longValue()).bitLength();
            if (TRACE_CONTEXT) out.println("Range to "+m+" = "+r+" ("+bits+" bits)");
            BDD V1V2context = V1c[0].buildAdd(V2c[0], bits, 0L);
            V1V2context.andWith(V1c[0].varRange(r.low.longValue(), r.high.longValue()));
            return V1V2context;
        } else if (OBJECT_SENSITIVE) {
            jq_Class c;
            if (m.isStatic()) c = null;
            else c = m.getDeclaringClass();
            Range r = (Range) rangeMap.get(c);
            if (TRACE_OBJECT) out.println("Range to "+c+" = "+r);
            BDD V1V2context;
            if (r == null) {
                System.out.println("Warning: when getting V1V2, "+c+" is not in object creation graph!  Assuming global only.");
                V1V2context = V1c[0].ithVar(0);
                V1V2context.andWith(V2c[0].ithVar(0));
                return V1V2context;
            }
            int bits = BigInteger.valueOf(r.high.longValue()).bitLength();
            V1V2context = V1c[0].buildAdd(V2c[0], bits, 0L);
            V1V2context.andWith(V1c[0].varRange(r.low.longValue(), r.high.longValue()));
            return V1V2context;
        } else if (CARTESIAN_PRODUCT && false) {
            BDD V1V2context = bdd.one();
            for (int i = 0; i < MAX_PARAMS; ++i) {
                V1V2context.andWith(V1c[i].buildEquals(V2c[i]));
            }
            return V1V2context;
        } else {
            return null;
        }
    }
    
    public void visitGlobalNode(Node node) {
        if (TRACE) out.println("Visiting node "+node);
        
        int V_i = Vmap.get(node);
        BDD V_bdd = V1.ithVar(V_i);
        
        if (VerifyAssertions)
            Assert._assert(node instanceof ConcreteObjectNode ||
                           node instanceof UnknownTypeNode ||
                           node == GlobalNode.GLOBAL);
        addToVP(V_bdd, node);
        
        for (Iterator j = node.getAllEdges().iterator(); j.hasNext(); ) {
            Map.Entry e = (Map.Entry) j.next();
            jq_Field f = (jq_Field) e.getKey();
            Collection c;
            if (e.getValue() instanceof Collection)
                c = (Collection) e.getValue();
            else
                c = Collections.singleton(e.getValue());
            addToS(V_bdd, f, c);
        }
        
        if (VerifyAssertions)
            Assert._assert(!node.hasAccessPathEdges());
    }

    public boolean isNullConstant(Node node) {
        if (node instanceof ConcreteTypeNode || node instanceof ConcreteObjectNode) {
            jq_Reference type = node.getDeclaredType();
            if (type == null || type == jq_NullType.NULL_TYPE) {
                if (TRACE) out.println("Skipping null constant");
                return true;
            }
        }
        return false;
    }
    
    void addToVT(int V_i, jq_Reference type) {
        BDD bdd1 = V1.ithVar(V_i);
        int T_i = Tmap.get(type);
        bdd1.andWith(T1.ithVar(T_i));
        if (TRACE_RELATIONS) out.println("Adding to vT: "+bdd1.toStringWithDomains());
        vT.orWith(bdd1);
    }
    
    void addToHT(int H_i, jq_Reference type) {
        /*Node n = (Node) Hmap.get(H_i);
        if(!INCLUDE_UNKNOWN_TYPES && n instanceof UnknownTypeNode){
            System.out.println("Skipped " + n);
            return;            
        }*/
        int T_i = Tmap.get(type);
        BDD T_bdd = T2.ithVar(T_i);
        addToHT(H_i, T_bdd);
        T_bdd.free();
    }
    
    void addToHT(int H_i, BDD T_bdd) {
        BDD bdd1 = H1.ithVar(H_i);
        bdd1.andWith(T_bdd.id());
        if (TRACE_RELATIONS) out.println("Adding to hT: "+bdd1.toStringWithDomains());
        hT.orWith(bdd1);
    }
    
    void addToAT(BDD T1_bdd, int T2_i) {
        BDD bdd1 = T2.ithVar(T2_i);
        bdd1.andWith(T1_bdd.id());
        if (TRACE_RELATIONS) out.println("Adding to aT: "+bdd1.toStringWithDomains());
        aT.orWith(bdd1);
    }
    
    void addToFC(BDD T2_bdd, int F_i) {
        BDD bdd1 = F.ithVar(F_i);
        bdd1.andWith(T2_bdd.id());
        if (TRACE_RELATIONS) out.println("Adding to fC: "+bdd1.toStringWithDomains(TS));
        fC.orWith(bdd1);
    }
    
    void addToFT(BDD F_bdd, BDD T2_bdd) {
        BDD bdd1 = F_bdd.and(T2_bdd);
        if (TRACE_RELATIONS) out.println("Adding to fT: "+bdd1.toStringWithDomains(TS));
        fT.orWith(bdd1);
    }
    
    void addToCHA(BDD T_bdd, int N_i, jq_Method m) {
        BDD bdd1 = N.ithVar(N_i);
        int M_i = Mmap.get(m);
        bdd1.andWith(M.ithVar(M_i));
        bdd1.andWith(T_bdd.id());
        if (TRACE_RELATIONS) out.println("Adding to cha: "+bdd1.toStringWithDomains());
        cha.orWith(bdd1);
    }
    
    jq_Class object_class = PrimordialClassLoader.getJavaLangObject();
    jq_Method javaLangObject_clone;
    {
        object_class.prepare();
        javaLangObject_clone = object_class.getDeclaredInstanceMethod(new jq_NameAndDesc("clone", "()Ljava/lang/Object;"));
    }
    
    jq_Class class_class = PrimordialClassLoader.getJavaLangClass();
    jq_Method javaLangClass_newInstance;
    {
        class_class.prepare();
        javaLangClass_newInstance = class_class.getDeclaredInstanceMethod(new jq_NameAndDesc("newInstance", "()Ljava/lang/Object;"));
        Assert._assert(javaLangClass_newInstance != null);
    }
    
    jq_Class cloneable_class = (jq_Class) PrimordialClassLoader.loader.getOrCreateBSType("Ljava/lang/Cloneable;");
    jq_Class throwable_class = (jq_Class) PrimordialClassLoader.getJavaLangThrowable();
    jq_Method javaLangObject_fakeclone = jq_FakeInstanceMethod.fakeMethod(object_class, 
                                                MethodSummary.fakeCloneName, "()Ljava/lang/Object;");

    private jq_Method fakeCloneIfNeeded(jq_Type t) {
        jq_Method m = javaLangObject_clone;
        if (t instanceof jq_Class) {
            jq_Class c = (jq_Class)t;
            if (!c.isInterface() && c.implementsInterface(cloneable_class)) {
                m = jq_FakeInstanceMethod.fakeMethod(c, MethodSummary.fakeCloneName, "()"+t.getDesc());
                boolean mustvisit = (cg != null) ? cg.getAllMethods().contains(m) : true;
                if (mustvisit)
                    visitMethod(m);
            }
        }
        // TODO: handle cloning of arrays
        return m;
    }

    int last_V = 0;
    int last_H = 0;
    int last_T = 0;
    int last_N = 0;
    int last_F = 0;
    
    private static BogusSummaryProvider bogusSummaryProvider = null;

    public static BogusSummaryProvider getBogusSummaryProvider() {
        if(bogusSummaryProvider == null) {
            bogusSummaryProvider = new BogusSummaryProvider();
        }
        
        return bogusSummaryProvider;
    }
    
    public void buildTypes() {
        // build up 'vT'
        int Vsize = Vmap.size();
        for (int V_i = last_V; V_i < Vsize; ++V_i) {
            Node n = (Node) Vmap.get(V_i);
            jq_Reference type = n.getDeclaredType();
            if (type != null) type.prepare();
            addToVT(V_i, type);
        }
        
        // build up 'hT', 'NNfilter', and identify clinit, thread run, finalizers.
        if (!FILTER_NULL && NNfilter == null) NNfilter = bdd.zero();
        int Hsize = Hmap.size();
        for (int H_i = last_H; H_i < Hsize; ++H_i) {
            Node n = (Node) Hmap.get(H_i);

            if (!FILTER_NULL && !isNullConstant(n))
                NNfilter.orWith(H1.ithVar(H_i));

            jq_Reference type = n.getDeclaredType();
            if (type != null) {
                type.prepare();
                if (n instanceof ConcreteTypeNode && type instanceof jq_Class) {
                    addClassInitializer((jq_Class) type);
                    addFinalizer((jq_Class) type, n);
                }
                if (ADD_THREADS && type != jq_NullType.NULL_TYPE &&
                    (type.isSubtypeOf(PrimordialClassLoader.getJavaLangThread()) ||
                     type.isSubtypeOf(PrimordialClassLoader.loader.getOrCreateBSType("Ljava/lang/Runnable;")))) {
                    addThreadRun(n.getDefiningMethod(), n, (jq_Class) type);
                }
            }
            if (!INCLUDE_UNKNOWN_TYPES && (n instanceof UnknownTypeNode) ) {
                if(TRACE) out.println("Skipping unknown type node: " + n);
                continue;                
            }
            addToHT(H_i, type);
        }

        if (ADD_SUPERTYPES) {
            for (int T_i = 0; T_i < Tmap.size(); ++T_i) {
                jq_Reference t1 = (jq_Reference) Tmap.get(T_i);
                if (t1 == null || t1 instanceof jq_NullType) continue;
                t1.prepare();
                jq_Reference t2 = t1.getDirectPrimarySupertype();
                if (t2 != null) {
                    t2.prepare();
                    Tmap.get(t2);
                }
                jq_Class[] c = t1.getInterfaces();
                for (int i = 0; i < c.length; ++i) {
                    Tmap.get(c[i]);
                }
            }
        }
        
        int Fsize = Fmap.size();
        int Tsize = Tmap.size();
        // build up 'aT'
        for (int T1_i = 0; T1_i < Tsize; ++T1_i) {
            jq_Reference t1 = (jq_Reference) Tmap.get(T1_i);
            int start = (T1_i < last_T)?last_T:0;
            BDD T1_bdd = T1.ithVar(T1_i);
            for (int T2_i = start; T2_i < Tsize; ++T2_i) {
                jq_Reference t2 = (jq_Reference) Tmap.get(T2_i);
                if (t2 == null || (t1 != null && t2.isSubtypeOf(t1))) {
                    addToAT(T1_bdd, T2_i);
                }
            }
            if (FILTER_HP) {
                BDD T2_bdd = T2.ithVar(T1_i);
                if (T1_i >= last_T && t1 == null) {
                    BDD Fdom = F.domain();
                    addToFT(Fdom, T2_bdd);
                    Fdom.free();
                }
                int start2 = (T1_i < last_T)?last_F:0;
                for (int F_i = start2; F_i < Fsize; ++F_i) {
                    jq_Field f = (jq_Field) Fmap.get(F_i);
                    if (f != null) {
                        f.getDeclaringClass().prepare();
                        f.getType().prepare();
                    }
                    BDD F_bdd = F.ithVar(F_i);
                    if ((t1 == null && f != null && f.isStatic()) ||
                        (t1 != null && ((f == null && t1 instanceof jq_Array && ((jq_Array) t1).getElementType().isReferenceType()) ||
                                        (f != null && t1.isSubtypeOf(f.getDeclaringClass()))))) {
                        addToFC(T2_bdd, F_i);
                    }
                    if (f != null && t1 != null && t1.isSubtypeOf(f.getType())) {
                        addToFT(F_bdd, T2_bdd);
                    }
                }
                T2_bdd.free();
            }
            T1_bdd.free();
        }
        
        // add types for UnknownTypeNodes to 'hT'
        if (INCLUDE_UNKNOWN_TYPES) {
            for (int H_i = last_H; H_i < Hsize; ++H_i) {
                Node n = (Node) Hmap.get(H_i);
                if (!(n instanceof UnknownTypeNode))
                    continue;
                jq_Reference type = n.getDeclaredType();
                if (type == null)
                    continue;
                if (!INCLUDE_ALL_UNKNOWN_TYPES && (type == object_class || type == throwable_class || type == class_class)) {
                    System.out.println("warning: excluding UnknownTypeNode "+type.getName()+"* from hT: H1("+H_i+")");
                } else {
                    // conservatively say that it can be any known subtype.
                    BDD T_i = T1.ithVar(Tmap.get(type));
                    BDD Tsub = aT.relprod(T_i, T1set);
                    addToHT(H_i, Tsub);
                    Tsub.free();
                    T_i.free();
                }
            }
        }
        
        // make type filters
        if (FILTER_VP) {
            if (vPfilter != null) vPfilter.free();
            BDD t1 = vT.relprod(aT, T1set); // V1xT1 x T1xT2 = V1xT2
            vPfilter = t1.relprod(hT, T2set); // V1xT2 x H1xT2 = V1xH1
            t1.free();
        }

        if (FILTER_HP) {
            for (int F_i = last_F; F_i < Fsize; ++F_i) {
                jq_Field f = (jq_Field) Fmap.get(F_i);
                if (f == null) {
                    BDD F_bdd = F.ithVar(F_i);
                    BDD T2dom = T2.domain();
                    addToFT(F_bdd, T2dom);
                    T2dom.free();
                    F_bdd.free();
                }
            }
            if (hPfilter != null) hPfilter.free();
            BDD t1 = hT.relprod(fC, T2set); // H1xT2 x FxT2 = H1xF
            hPfilter = hT.relprod(fT, T2set); // H1xT2 x FxT2 = H1xF
            hPfilter.replaceWith(H1toH2); // H2xF
            hPfilter.andWith(t1); // H1xFxH2
        }
        
        // build up 'cha'
        int Nsize = Nmap.size();
        for (int T_i = 0; T_i < Tsize; ++T_i) {
            jq_Reference t = (jq_Reference) Tmap.get(T_i);
            BDD T_bdd = T2.ithVar(T_i);
            int start = (T_i < last_T)?last_N:0;
            for (int N_i = start; N_i < Nsize; ++N_i) {
                jq_Method n = (jq_Method) Nmap.get(N_i);
                if (n == null) continue;
                n.getDeclaringClass().prepare();
                jq_Method m;
                if (n.isStatic()) {
                    if (t != null) continue;
                    m = n;
                } else {
                    if (t == null ||
                        t == jq_NullType.NULL_TYPE ||
                        !t.isSubtypeOf(n.getDeclaringClass())) continue;
                    m = t.getVirtualMethod(n.getNameAndDesc());
                }
                if ((m == javaLangObject_clone && t != object_class) || n == javaLangObject_fakeclone) {
                    m = fakeCloneIfNeeded(t);                                   // for t.clone()
                    addToCHA(T_bdd, Nmap.get(javaLangObject_fakeclone), m);     // for super.clone()
                }
                if (m == null) continue;
                //System.out.println("n = " + n + ", m = " + m);
                
                if(USE_BOGUS_SUMMARIES && m != null) {
                    jq_Method replacement = getBogusSummaryProvider().getReplacementMethod(m);
                    if(replacement != null) {
                        if(TRACE_BOGUS) System.out.println("Replacing a call to " + m + 
                                        " with a call to "+ replacement);
                    
                        addToCHA(T_bdd, Nmap.get(replacement), replacement);     // for replacement methods
                        continue;
                    }                 
                }
                
                if(USE_REFLECTION_PROVIDER && m != null && ReflectionInformationProvider.isNewInstance(n)){
                    if(TRACE_REFLECTION) System.out.println("Found a reflective call to " + m);
                    Collection/*<jq_Method>*/ targets = getReflectionProvider().getNewInstanceTargets(n);
                    if(targets != null){
                        for(Iterator iter = targets.iterator(); iter.hasNext();){
                            jq_Method target = (jq_Method) iter.next();
                            if(TRACE_REFLECTION) System.out.println(
                                "Adding a call to " + target + " instead of "+ m);
                            
                            addToCHA(T_bdd, Nmap.get(target), target);                            
                        }
                        //continue;
                    }else{
                        if(TRACE_REFLECTION) System.out.println("No reflective targets for a call to " + m);    
                    }                    
                }
                
                
                // default case
                addToCHA(T_bdd, N_i, m);            
            }
            T_bdd.free();
        }
        last_V = Vsize;
        last_H = Hsize;
        last_T = Tsize;
        last_N = Nsize;
        last_F = Fsize;
        if (Vsize != Vmap.size() ||
            Hsize != Hmap.size() ||
            Tsize != Tmap.size() ||
            Nsize != Nmap.size() ||
            Fsize != Fmap.size()) {
            if (TRACE) out.println("Elements added, recalculating types...");
            buildTypes();
        }
    }
    static ReflectionInformationProvider reflectionInformationProvider = null;
    public static ReflectionInformationProvider getReflectionProvider() {
        if(reflectionInformationProvider == null){
            reflectionInformationProvider = new ReflectionInformationProvider.CribSheetReflectionInformationProvider();            
        }
        return reflectionInformationProvider;
    }
    public void addClassInitializer(jq_Class c) {
        if (!ADD_CLINIT) return;
        jq_Method m = c.getClassInitializer();
        if (m != null) {
            visitMethod(m);
            rootMethods.add(m);
        }
    }
    
    jq_NameAndDesc finalizer_method = new jq_NameAndDesc("finalize", "()V");
    public void addFinalizer(jq_Class c, Node h) {
        if (!ADD_FINALIZERS) return;
        jq_Method m = c.getVirtualMethod(finalizer_method);
        if (m != null && m.getBytecode() != null) {
            visitMethod(m);
            if (rootMethods.add(m)) {
                // Add placeholder objects for the "this" parameter of the finalizer.
                if (ADD_ROOT_PLACEHOLDERS > 0) {
                    addPlaceholdersForParams(m, ADD_ROOT_PLACEHOLDERS);
                }
            }
            Node p = MethodSummary.getSummary(m).getParamNode(0);
            int H_i = Hmap.get(h);
            addToVP(p, H_i);
        }
    }
    
    BDD threadRuns; // vc1,v1,hc1,h1 :  threadrun v1 matches thread object h1
    
    static jq_NameAndDesc main_method = new jq_NameAndDesc("main", "([Ljava/lang/String;)V");
    static jq_NameAndDesc run_method = new jq_NameAndDesc("run", "()V");
    public void addThreadRun(jq_Method caller, Node h, jq_Class c) {
        if (!ADD_THREADS) return;
        int H_i = Hmap.get(h);
        jq_Method m = c.getVirtualMethod(run_method);
        if (m != null && m.getBytecode() != null) {
            visitMethod(m);
            if (rootMethods.add(m)) {
                // Add placeholder objects for the "this" parameter of the run() method.
                if (ADD_ROOT_PLACEHOLDERS > 0) {
                    addPlaceholdersForParams(m, ADD_ROOT_PLACEHOLDERS);
                }
            }
            Node p = MethodSummary.getSummary(m).getParamNode(0);
            BDD context = null;
            if (THREAD_SENSITIVE) {
                int context_j = getThreadRunIndex(m, h);
                if (context_j == -1) {
                    System.out.println("Unknown thread node "+h+" method "+m);
                    return;
                }
                BDD hcontext = getV1H1Context(caller).exist(V1cset);
                int V_i = Vmap.get(p);
                System.out.println("Thread "+h+" contexts "+hcontext.toStringWithDomains()+" matches vcontext "+context_j+" "+p);
                hcontext.andWith(V1c[0].ithVar(context_j));
                hcontext.andWith(H1.ithVar(H_i));
                hcontext.andWith(V1.ithVar(V_i));
                threadRuns.orWith(hcontext.id());
                if (!DUMP_INITIAL) vP.orWith(hcontext);
            } else if (CONTEXT_SENSITIVE && MAX_HC_BITS > 1) {
                int context_i = getThreadRunIndex(m, h);
                int context_j = context_i + vCnumbering.getRange(m).low.intValue();
                System.out.println("Thread "+h+" index "+context_j);
                //context = H1c.ithVar(context_i);
                context = H1cdomain.id();
                //context.andWith(V1c.ithVar(context_j));
                context.andWith(V1cdomain.id());
                addToVP(context, p, H_i);
                context.free();
            } else {
                addToVP(p, H_i);
            }
        }
    }
    
    public void solvePointsTo() {
        if (INCREMENTAL1) {
            solvePointsTo_incremental();
            return;
        }
        
        BDD old_vP;
        BDD old_hP = bdd.zero();
        for (int outer = 1; ; ++outer) {
            for (int inner = 1; ; ++inner) {
                old_vP = vP.id();
                
                // Rule 1
                BDD t1 = vP.replace(V1toV2); // V2xH1
                if (TRACE_SOLVER) out.println("Inner #"+inner+": rename V1toV2: vP "+vP.nodeCount()+" -> "+t1.nodeCount());
                BDD t2 = A.relprod(t1, V2set); // V1xV2 x V2xH1 = V1xH1
                if (TRACE_SOLVER) out.println("Inner #"+inner+": relprod A "+A.nodeCount()+" -> "+t2.nodeCount());
                t1.free();
                if (FILTER_VP) t2.andWith(vPfilter.id());
                if (FILTER_VP && TRACE_SOLVER) out.println("Inner #"+inner+": and vPfilter "+vPfilter.nodeCount()+" -> "+t2.nodeCount());
                if (TRACE_SOLVER) out.print("Inner #"+inner+": or vP "+vP.nodeCount()+" -> ");
                vP.orWith(t2);
                if (TRACE_SOLVER) out.println(vP.nodeCount());
                
                boolean done = vP.equals(old_vP); 
                old_vP.free();
                if (done) break;
            }
            
            // Rule 2
            BDD t3 = S.relprod(vP, V1set); // V1xFxV2 x V1xH1 = H1xFxV2
            if (!FILTER_NULL) t3.andWith(NNfilter.id());
            BDD t4 = vP.replace(V1H1toV2H2); // V2xH2
            BDD t5 = t3.relprod(t4, V2set); // H1xFxV2 x V2xH2 = H1xFxH2
            t3.free(); t4.free();
            if (FILTER_HP) t5.andWith(hPfilter.id());
            hP.orWith(t5);

            if (TRACE_SOLVER) out.println("Outer #"+outer+": hP "+hP.nodeCount());
            
            boolean done = hP.equals(old_hP); 
            old_hP.free();
            if (done) break;
            old_hP = hP.id();
            
            // Rule 3
            BDD t6 = L.relprod(vP, V1set); // V1xFxV2 x V1xH1 = H1xFxV2
            BDD t7 = t6.relprod(hP, H1Fset); // H1xFxV2 x H1xFxH2 = V2xH2
            t6.free();
            t7.replaceWith(V2H2toV1H1); // V1xH1
            if (FILTER_VP) t7.andWith(vPfilter.id());
            vP.orWith(t7);
            if (TRACE_SOLVER) out.println("Outer #"+outer+": vP "+vP.nodeCount());
        }
    }
    
    BDD old1_A;
    BDD old1_S;
    BDD old1_L;
    BDD old1_vP;
    BDD old1_hP;
    
    public void solvePointsTo_incremental() {
        
        // handle new A
        BDD new_A = A.apply(old1_A, BDDFactory.diff);
        old1_A.free();
        if (!new_A.isZero()) {
            if (TRACE_SOLVER) out.println("New A: "+new_A.nodeCount());
            BDD t1 = vP.replace(V1toV2); // V2xH1
            if (TRACE_SOLVER) out.println("New A: rename V1toV2: vP "+vP.nodeCount()+" -> "+t1.nodeCount());
            BDD t2 = new_A.relprod(t1, V2set); // V1xV2 x V2xH1 = V1xH1
            if (TRACE_SOLVER) out.println("New A: relprod new_A "+new_A.nodeCount()+" -> "+t2.nodeCount());
            new_A.free(); t1.free();
            if (FILTER_VP) t2.andWith(vPfilter.id());
            if (FILTER_VP && TRACE_SOLVER) out.println("New A: and vPfilter "+vPfilter.nodeCount()+" -> "+t2.nodeCount());
            if (TRACE_SOLVER) out.print("New A: or vP "+vP.nodeCount()+" -> ");
            vP.orWith(t2);
            if (TRACE_SOLVER) out.println(vP.nodeCount());
        }
        old1_A = A.id();
        
        // handle new S
        BDD new_S = S.apply(old1_S, BDDFactory.diff);
        old1_S.free();
        if (!new_S.isZero()) {
            if (TRACE_SOLVER) out.println("New S: "+new_S.nodeCount());
            BDD t3 = new_S.relprod(vP, V1set); // V1xFxV2 x V1xH1 = H1xFxV2
            if (TRACE_SOLVER) out.println("New S: relprod: vP "+vP.nodeCount()+" -> "+t3.nodeCount());
            new_S.free();
            if (!FILTER_NULL) t3.andWith(NNfilter.id());
            if (!FILTER_NULL && TRACE_SOLVER) out.println("New S: and NNfilter "+NNfilter.nodeCount()+" -> "+t3.nodeCount());
            BDD t4 = vP.replace(V1H1toV2H2); // V2xH2
            if (TRACE_SOLVER) out.println("New S: replace vP "+vP.nodeCount()+" -> "+t4.nodeCount());
            BDD t5 = t3.relprod(t4, V2set); // H1xFxV2 x V2xH2 = H1xFxH2
            if (TRACE_SOLVER) out.println("New S: relprod -> "+t5.nodeCount());
            t3.free(); t4.free();
            if (FILTER_HP) t5.andWith(hPfilter.id());
            if (FILTER_HP && TRACE_SOLVER) out.println("New S: and hPfilter "+hPfilter.nodeCount()+" -> "+t5.nodeCount());
            if (TRACE_SOLVER) out.print("New S: or hP "+hP.nodeCount()+" -> ");
            hP.orWith(t5);
            if (TRACE_SOLVER) out.println(hP.nodeCount());
        }
        old1_S = S.id();
        
        // handle new L
        BDD new_L = L.apply(old1_L, BDDFactory.diff);
        old1_L.free();
        if (!new_L.isZero()) {
            if (TRACE_SOLVER) out.println("New L: "+new_L.nodeCount());
            BDD t6 = new_L.relprod(vP, V1set); // V1xFxV2 x V1xH1 = H1xFxV2
            if (TRACE_SOLVER) out.println("New L: relprod: vP "+vP.nodeCount()+" -> "+t6.nodeCount());
            BDD t7 = t6.relprod(hP, H1Fset); // H1xFxV2 x H1xFxH2 = V2xH2
            if (TRACE_SOLVER) out.println("New L: relprod: hP "+hP.nodeCount()+" -> "+t7.nodeCount());
            t6.free();
            t7.replaceWith(V2H2toV1H1); // V1xH1
            if (TRACE_SOLVER) out.println("New L: replace: "+t7.nodeCount());
            if (FILTER_VP) t7.andWith(vPfilter.id());
            if (TRACE_SOLVER) out.print("New L: or vP "+vP.nodeCount()+" -> ");
            vP.orWith(t7);
            if (TRACE_SOLVER) out.println(vP.nodeCount());
        }
        old1_L = L.id();
        
        for (int outer = 1; ; ++outer) {
            BDD new_vP_inner = vP.apply(old1_vP, BDDFactory.diff);
            int inner;
            for (inner = 1; !new_vP_inner.isZero() && inner < 256; ++inner) {
                if (TRACE_SOLVER)
                    out.println("Inner #"+inner+": new vP "+new_vP_inner.nodeCount());
                
                // Rule 1
                BDD t1 = new_vP_inner.replace(V1toV2); // V2xH1
                if (TRACE_SOLVER) out.println("Inner #"+inner+": rename V1toV2: "+t1.nodeCount());
                new_vP_inner.free();
                BDD t2 = A.relprod(t1, V2set); // V1xV2 x V2xH1 = V1xH1
                if (TRACE_SOLVER) out.println("Inner #"+inner+": relprod A: "+A.nodeCount()+" -> "+t2.nodeCount());
                t1.free();
                if (FILTER_VP) t2.andWith(vPfilter.id());
                if (FILTER_VP && TRACE_SOLVER) out.println("Inner #"+inner+": and vPfilter "+vPfilter.nodeCount()+" -> "+t2.nodeCount());
                
                BDD old_vP_inner = vP.id();
                vP.orWith(t2);
                if (TRACE_SOLVER) out.println("Inner #"+inner+": or vP "+old_vP_inner.nodeCount()+" -> "+vP.nodeCount());
                new_vP_inner = vP.apply(old_vP_inner, BDDFactory.diff);
                if (TRACE_SOLVER) out.println("Inner #"+inner+": diff vP -> "+new_vP_inner.nodeCount());
                old_vP_inner.free();
            }
            
            BDD new_vP = vP.apply(old1_vP, BDDFactory.diff);
            if (TRACE_SOLVER) out.println("Outer #"+outer+": diff vP "+vP.nodeCount()+" - "+old1_vP.nodeCount()+" = "+new_vP.nodeCount());
            old1_vP.free();
            
            {
                // Rule 2
                BDD t3 = S.relprod(new_vP, V1set); // V1xFxV2 x V1xH1 = H1xFxV2
                if (TRACE_SOLVER) out.println("Outer #"+outer+" S: relprod "+S.nodeCount()+" -> "+t3.nodeCount());
                if (!FILTER_NULL) t3.andWith(NNfilter.id());
                if (!FILTER_NULL && TRACE_SOLVER) out.println("Outer #"+outer+" S: and NNfilter "+NNfilter.nodeCount()+" -> "+t3.nodeCount());
                BDD t4 = vP.replace(V1H1toV2H2); // V2xH2
                if (TRACE_SOLVER) out.println("Outer #"+outer+" S: replace "+vP.nodeCount()+" -> "+t4.nodeCount());
                BDD t5 = t3.relprod(t4, V2set); // H1xFxV2 x V2xH2 = H1xFxH2
                if (TRACE_SOLVER) out.println("Outer #"+outer+" S: relprod -> "+t5.nodeCount());
                t3.free(); t4.free();
                if (FILTER_HP) t5.andWith(hPfilter.id());
                if (FILTER_HP && TRACE_SOLVER) out.println("Outer #"+outer+" S: and hPfilter "+hPfilter.nodeCount()+" -> "+t5.nodeCount());
                if (TRACE_SOLVER) out.print("Outer #"+outer+" S: or hP "+hP.nodeCount()+" -> ");
                hP.orWith(t5);
                if (TRACE_SOLVER) out.println(hP.nodeCount());
            }
            {
                // Rule 2
                BDD t3 = S.relprod(vP, V1set); // V1xFxV2 x V1xH1 = H1xFxV2
                if (TRACE_SOLVER) out.println("Outer #"+outer+" S': relprod "+S.nodeCount()+", "+vP.nodeCount()+" -> "+t3.nodeCount());
                if (!FILTER_NULL) t3.andWith(NNfilter.id());
                if (!FILTER_NULL && TRACE_SOLVER) out.println("Outer #"+outer+" S': and NNfilter "+NNfilter.nodeCount()+" -> "+t3.nodeCount());
                BDD t4 = new_vP.replace(V1H1toV2H2); // V2xH2
                if (TRACE_SOLVER) out.println("Outer #"+outer+" S': replace "+new_vP.nodeCount()+" -> "+t4.nodeCount());
                BDD t5 = t3.relprod(t4, V2set); // H1xFxV2 x V2xH2 = H1xFxH2
                if (TRACE_SOLVER) out.println("Outer #"+outer+" S': relprod -> "+t5.nodeCount());
                t3.free(); t4.free();
                if (FILTER_HP) t5.andWith(hPfilter.id());
                if (FILTER_HP && TRACE_SOLVER) out.println("Outer #"+outer+" S': and hPfilter "+hPfilter.nodeCount()+" -> "+t5.nodeCount());
                if (TRACE_SOLVER) out.print("Outer #"+outer+" S': or hP "+hP.nodeCount()+" -> ");
                hP.orWith(t5);
                if (TRACE_SOLVER) out.println(hP.nodeCount());
            }

            old1_vP = vP.id();
            
            BDD new_hP = hP.apply(old1_hP, BDDFactory.diff);
            if (TRACE_SOLVER) out.println("Outer #"+outer+": diff hP "+hP.nodeCount()+" - "+old1_hP.nodeCount()+" = "+new_hP.nodeCount());
            if (new_hP.isZero() && new_vP.isZero() && inner < 256) break;
            old1_hP = hP.id();
            
            {
                // Rule 3
                BDD t6 = L.relprod(new_vP, V1set); // V1xFxV2 x V1xH1 = H1xFxV2
                if (TRACE_SOLVER) out.println("Outer #"+outer+" L: relprod "+L.nodeCount()+", "+new_vP.nodeCount()+" -> "+t6.nodeCount());
                BDD t7 = t6.relprod(hP, H1Fset); // H1xFxV2 x H1xFxH2 = V2xH2
                if (TRACE_SOLVER) out.println("Outer #"+outer+" L: relprod "+hP.nodeCount()+" -> "+t7.nodeCount());
                t6.free();
                t7.replaceWith(V2H2toV1H1); // V1xH1
                if (TRACE_SOLVER) out.println("Outer #"+outer+" L: replace "+t7.nodeCount());
                if (FILTER_VP) t7.andWith(vPfilter.id());
                if (FILTER_VP && TRACE_SOLVER) out.println("Outer #"+outer+" L: and vPfilter "+vPfilter.nodeCount()+" -> "+t7.nodeCount());
                if (TRACE_SOLVER) out.print("Outer #"+outer+" L: or vP "+vP.nodeCount()+" -> ");
                vP.orWith(t7);
                if (TRACE_SOLVER) out.println(vP.nodeCount());
            }
            {
                // Rule 3
                BDD t6 = L.relprod(vP, V1set); // V1xFxV2 x V1xH1 = H1xFxV2
                if (TRACE_SOLVER) out.println("Outer #"+outer+" L': relprod "+L.nodeCount()+", "+vP.nodeCount()+" -> "+t6.nodeCount());
                BDD t7 = t6.relprod(new_hP, H1Fset); // H1xFxV2 x H1xFxH2 = V2xH2
                if (TRACE_SOLVER) out.println("Outer #"+outer+" L': relprod "+new_hP.nodeCount()+" -> "+t7.nodeCount());
                t6.free();
                t7.replaceWith(V2H2toV1H1); // V1xH1
                if (TRACE_SOLVER) out.println("Outer #"+outer+" L': replace "+t7.nodeCount());
                if (FILTER_VP) t7.andWith(vPfilter.id());
                if (FILTER_VP && TRACE_SOLVER) out.println("Outer #"+outer+" L': and vPfilter "+vPfilter.nodeCount()+" -> "+t7.nodeCount());
                if (TRACE_SOLVER) out.print("Outer #"+outer+" L': or vP "+vP.nodeCount()+" -> ");
                vP.orWith(t7);
                if (TRACE_SOLVER) out.println(vP.nodeCount());
            }
        }
    }
    
    public void dumpWithV1c(BDD z, BDD set) {
        BDD a = z.exist(V1cset);
        for (Iterator i = a.iterator(set); i.hasNext(); ) {
            BDD b = (BDD) i.next();
            System.out.println(b.toStringWithDomains(TS));
            b.andWith(z.id());
            BDD c = b.exist(set);
            if (c.isOne()) {
                System.out.println("    under all contexts");
            } else {
                System.out.print("    under context ");
                Assert._assert(!c.isZero());
                for (int j = 0; j < V1csets.length; ++j) {
                    BDD d = c.id();
                    for (int k = 0; k < V1csets.length; ++k) {
                        if (k == j) continue;
                        BDD e = d.exist(V1csets[k]);
                        d.free();
                        d = e;
                    }
                    if (d.isOne()) System.out.print("*");
                    else if (d.isZero()) System.out.print("0");
                    else if (d.satCount(V1csets[j]) > 10) System.out.print("many");
                    else for (Iterator k = d.iterator(V1csets[j]); k.hasNext(); ) {
                        BDD e = (BDD) k.next();
                        BigInteger v = e.scanVar(V1c[j]);
                        BigInteger mask = BigInteger.ONE.shiftLeft(H_BITS).subtract(BigInteger.ONE);
                        BigInteger val = v.and(mask);
                        if (val.signum() != 0) System.out.print(TS.elementName(H1.getIndex(), val));
                        else System.out.print('_');
                        if (k.hasNext()) System.out.print(',');
                    }
                    if (j < MAX_PARAMS-1) System.out.print("|");
                }
                System.out.println();
            }
        }
    }
    
    public void dumpIEcs() {
        for (Iterator i = IEcs.iterator(Iset); i.hasNext(); ) {
            BDD q = (BDD) i.next(); // V2cxIxV1cxM
            BigInteger I_i = q.scanVar(I);
            System.out.println("Invocation site "+TS.elementName(I.getIndex(), I_i));
            BDD a = q.exist(IMset.and(V1cset)); // V2c
            Iterator k = null;
            boolean bool1;
            if (a.isOne()) {
                System.out.println("    under all contexts");
                bool1 = true;
            } else if (a.satCount(V2cset) > 16) {
                System.out.println("    under many contexts");
                bool1 = true;
            } else {
                k = q.iterator(V2cset);
                bool1 = false;
            }
            if (bool1) k = Collections.singleton(q).iterator();

            for ( ; k.hasNext(); ) {
                BDD s = (BDD) k.next(); // V2cxIxV1cxM
                if (!bool1) {
                    System.out.println("    under context "+s.exist(IMset.and(V1cset)).toStringWithDomains(TS));
                }
                for (Iterator j = s.iterator(Mset); j.hasNext(); ) {
                    BDD r = (BDD) j.next(); // V2cxIxV1cxM
                    BigInteger M_i = r.scanVar(M);
                    System.out.println(" calls "+TS.elementName(M.getIndex(), M_i));
                    BDD b = r.exist(IMset.and(V2cset));
                    if (b.isOne()) {
                        System.out.println("        all contexts");
                    } else if (b.satCount(V1cset) > 16) {
                        System.out.println("        many contexts");
                    } else {
                        for (Iterator m = r.iterator(V1cset); m.hasNext(); ) {
                            BDD t = (BDD) m.next();
                            System.out.println("        context "+s.exist(IMset.and(V2cset)).toStringWithDomains(TS));
                        }
                    }
                }
            }
        }
    }
    
    public void dumpVP(BDD my_vP) {
        for (Iterator i = my_vP.iterator(V1.set()); i.hasNext(); ) {
            BDD q = (BDD) i.next();
            BigInteger V_i = q.scanVar(V1);
            System.out.println("Variable "+TS.elementName(V1.getIndex(), V_i)+" points to:");
            for (Iterator j = q.iterator(H1.set()); j.hasNext(); ) {
                BDD r = (BDD) j.next();
                BigInteger H_i = r.scanVar(H1);
                System.out.println("  "+TS.elementName(H1.getIndex(), H_i));
                if (USE_VCONTEXT) {
                    BDD a = r.exist(V1.set().and(H1set));
                    if (a.isOne()) {
                        System.out.println("    under all contexts");
                    } else {
                        System.out.print("    under context ");
                        for (int m = 0; m < MAX_PARAMS; ++m) {
                            if (m > 0) System.out.print("|");
                            BDD b = a.id();
                            for (int k = 0; k < V1csets.length; ++k) {
                                if (k == m) continue;
                                BDD c = b.exist(V1csets[k]);
                                b.free();
                                b = c;
                            }
                            if (b.isOne()) {
                                System.out.print("*");
                            } else if (b.satCount(V1csets[m]) > 100) {
                                System.out.print("many");
                            } else for (Iterator k = b.iterator(V1csets[m]); k.hasNext(); ) {
                                BDD s = (BDD) k.next();
                                BigInteger foo = s.scanVar(V1c[m]);
                                System.out.print(TS.elementName(H1.getIndex(), foo));
                                if (k.hasNext()) System.out.print(",");
                            }
                        }
                        System.out.println();
                    }
                }
            }
        }
    }
    
    // t1 = actual x (z=0)
    // t3 = t1 x mI
    // t4 = t3 x vP
    // t5 = t4 x hT
    // t6 = t5 x cha
    // IE |= t6
    /** Uses points-to information to bind virtual call sites.  (Adds to IE/IEcs.) */
    public void bindInvocations() {
        if (INCREMENTAL3 && !OBJECT_SENSITIVE) {
            bindInvocations_incremental();
            return;
        }
        BDD t1 = actual.restrict(Z.ithVar(0)); // IxV2
        if (USE_VCONTEXT) t1.andWith(V2cdomain.id()); // IxV2cxV2
        t1.replaceWith(V2toV1);
        BDD t3 = t1.relprod(mI, Mset); // IxV1cxV1 & MxIxN = IxV1cxV1xN
        t1.free();
        BDD t4;
        if (CS_CALLGRAPH) {
            // We keep track of where a call goes under different contexts.
            t4 = t3.relprod(vP, V1.set()); // IxV1cxV1xN x V1cxV1xH1cxH1 = V1cxIxH1cxH1xN
        } else {
            // By quantifying out V1c, we merge all contexts.
            t4 = t3.relprod(vP, V1set); // IxV1cxV1xN x V1cxV1xH1cxH1 = IxH1cxH1xN
        }
// 9%
        BDD t5 = t4.relprod(hT, H1set); // (V1cx)IxH1cxH1xN x H1xT2 = (V1cx)IxT2xN
        t4.free();
        BDD t6 = t5.relprod(cha, T2Nset); // (V1cx)IxT2xN x T2xNxM = (V1cx)IxM
        t5.free();
        
        if (TRACE_SOLVER) out.println("Call graph edges before: "+IE.satCount(IMset));
        if (CS_CALLGRAPH) {
            IE.orWith(t6.exist(V1cset));
        }else {
            IE.orWith(t6.id());
        }
        if (TRACE_SOLVER) out.println("Call graph edges after: "+IE.satCount(IMset));
        
        if (CONTEXT_SENSITIVE || THREAD_SENSITIVE) {
            // Add the context for the new call graph edges.
            if (CS_CALLGRAPH) t6.replaceWith(V1ctoV2c); // V2cxIxM
            t6.andWith(IEfilter.id()); // V2cxIxV1cxM
            IEcs.orWith(t6.id());
        } else if (OBJECT_SENSITIVE) {
            // Add the context for the new edges.
            t4 = t3.relprod(vP, V1.set()); // IxV1xN x V1cxV1xH1cxH1 = V1cxIxH1cxH1xN
            t5 = t4.relprod(hT, H1.set()); // V1cxIxH1cxH1xN x H1xT2 = V1cxIxH1cxT2xN
            t4.free();
            BDD t7 = t5.relprod(cha, T2Nset); // V1cxIxH1cxT2xN x T2xNxM = V1cxIxH1cxM
            t5.free();
            t7.replaceWith(V1cH1ctoV2cV1c); // V2cxIxV1cxM
            IEcs.orWith(t7);
            
            // Add the context for statically-bound call edges.
            BDD t8 = staticCalls.relprod(vP, V1.set().and(H1.set())); // V1xIxM x V1cxV1xH1cxH1 = V1cxIxH1cxM
            t8.replaceWith(V1cH1ctoV2cV1c); // V2cxIxV1cxM
            IEcs.orWith(t8);
        } else if (CARTESIAN_PRODUCT) {
            // t6 |= statics
            // for (i=0..k)
            //     t8 = actual x (z=i)
            //     t9 = t8 x vP
            //     context &= t9
            //     t9 &= V1cH1equals[i]
            //     tb = t9 x t6
            //     tc = tb x formal_i
            //     newPt |= tc
            // newPt2 = newPt x context
            // newPt2 &= vPfilter
            // vP |= newPt2
            // context &= t6
            // IEcs |= context
            
            // Add all statically-bound calls to t6.
            // They are true under all contexts.
            BDD statics = staticCalls.exist(V1.set()); // IxM
            if (CS_CALLGRAPH) statics.andWith(V1cdomain.id()); // V1cxIxM
            t6.orWith(statics); // V1cxIxM | V1cxIxM = V1cxIxM
            
            // Edges in the call graph.  Invocation I under context V2c has target method M.
            if (CS_CALLGRAPH) t6.replaceWith(V1ctoV2c); // V2cxIxM
            // The context for the new cg edges are based on the points-to set of every parameter.
            BDD context = bdd.one();
            // We need to add points-to relations for each of the actual parameters.
            BDD newPt = bdd.zero();
            for (int i = MAX_PARAMS - 1; i >= 0; --i) {
                if (TRACE_BIND) System.out.println("Param "+i+":");
                BDD t8 = actual.restrict(Z.ithVar(i)).and(V2cdomain); // IxV2
                t8.replaceWith(V2toV1); // IxV1
                if (TRACE_BIND) System.out.println("t8 = "+t8.toStringWithDomains());
                BDD t9 = t8.relprod(vP, V1.set()); // IxV1 x V1cxV1xH1cxH1 = V1cxIxH1cxH1
                if (TRACE_BIND) {
                    System.out.println("t9 =");
                    dumpWithV1c(t9, Iset.and(H1set));
                }
                t8.free();
                
                t9.replaceWith(V1ctoV2c); // V2cxIxH1cxH1
                BDD ta = t9.replace(H1toV1c[i]); // V2cxIxV1c[i]
                // Invocation I under context V2c leads to context V1c
// 20%
                context.andWith(ta); // V2cxIxV1c[i]
                
                // Calculate new points-to relations for this actual parameter.
                t9.andWith(V1cH1equals[i].id()); // V2cxIxV1c[i]xH1cxH1
                BDD tb = t9.relprod(t6, V2cset); // V2cxIxV1c[i]xH1cxH1 x (V2cx)IxM = IxV1c[i]xMxH1cxH1
                t9.free();
                BDD formal_i = formal.restrict(Z.ithVar(i)); // MxV1
                BDD tc = tb.relprod(formal_i, Mset); // IxV1c[i]xMxH1cxH1 x MxV1 = IxV1c[i]xV1xH1cxH1
                formal_i.free(); tb.free();
                for (int j = 0; j < MAX_PARAMS; ++j) {
                    if (i == j) continue;
                    tc.andWith(V1c[j].domain());
                }
                if (TRACE_BIND) dumpVP(tc.exist(Iset));
                newPt.orWith(tc);
            }
            
            // Now, filter out unrealizables.
            // IxV1c[i]xV1xH1cxH1 x V2cxIxV1c[i] = V1cxV1xH1cxH1
// 13%
            BDD newPt2 = newPt.relprod(context, V2cset.and(Iset));
            newPt.free();
            if (TRACE_BIND) dumpVP(newPt2);
            
            if (FILTER_VP) newPt2.andWith(vPfilter.id());
            vP.orWith(newPt2);
            
            context.andWith(t6.id()); // V2cxIxV1c[k]xM
            if (TRACE_BIND) System.out.println("context = "+context.toStringWithDomains());
            IEcs.orWith(context);
        }
        t3.free();
        t6.free();
    }
    
    Map missingClasses    = new HashMap();
    Map missingConst      = new HashMap();
    Map noConstrClasses   = new HashMap();
    Map cantCastTypes     = new HashMap();
    Map circularClasses   = new HashMap();
    Map wellFormedClasses = new HashMap();
    Set unresolvedCalls   = new HashSet();
    // currently resolved reflective calls, used for iterative computation
    BDD reflectiveCalls;                            // IxM 
    
    public String getBDDDomains(BDD r) {
        if (r.isZero() || r.isOne()) return "[]";
        
        BDDFactory bdd = r.getFactory();
        StringBuffer sb = new StringBuffer();
        int[] set = new int[bdd.varNum()];
        printed = 0;
        fdd_printset_rec(bdd, sb, r, set);
        return sb.toString();
    }
    private static int printed = 0;

    private SubtypeHelper _subtypeHelper;
    private static void fdd_printset_rec(BDDFactory bdd, StringBuffer sb, BDD r, int[] set) {
        int fdvarnum = bdd.numberOfDomains();
        
        int n, m, i;
        boolean used = false;
        int[] var;
        boolean first;
        
        if (r.isZero())
            return;
        else if (r.isOne()) {
            if(printed > 0) return;
            sb.append('<');
            first = true;
            
            for (n=0 ; n<fdvarnum ; n++) {
                used = false;
                
                BDDDomain domain_n = bdd.getDomain(n);
                
                int[] domain_n_ivar = domain_n.vars();
                int domain_n_varnum = domain_n_ivar.length;
                for (m=0 ; m<domain_n_varnum ; m++)
                    if (set[domain_n_ivar[m]] != 0)
                        used = true;
                
                if (used) {
                    if (!first)
                        sb.append(", ");
                    first = false;
                    sb.append(domain_n.getName());
                                        
                    var = domain_n_ivar;
                    
                    BigInteger pos = BigInteger.ZERO;
                    int maxSkip = -1;
                    boolean hasDontCare = false;
                    for (i=0; i<domain_n_varnum; ++i) {
                        int val = set[var[i]];
                        if (val == 0) {
                            hasDontCare = true;
                            if (maxSkip == i-1)
                                maxSkip = i;
                        }
                    }
                    for (i=domain_n_varnum-1; i>=0; --i) {
                        pos = pos.shiftLeft(1);
                        int val = set[var[i]];
                        if (val == 2) {
                            pos = pos.setBit(0);
                        }
                    }
                }
            }
            
            sb.append('>');
            printed = 1;
        } else {
            set[r.var()] = 1;
            BDD lo = r.low();
            fdd_printset_rec(bdd, sb, lo, set);
            lo.free();
            
            set[r.var()] = 2;
            BDD hi = r.high();
            fdd_printset_rec(bdd, sb, hi, set);
            hi.free();
            
            set[r.var()] = 0;
        }
    }
 
    static BDD buildEquals(BDDDomain thiz, BDDDomain that){
        Assert._assert(thiz.size().equals(that.size()));
        BDDFactory factory = thiz.getFactory();
        BDD e = factory.one();
    
        int[] this_ivar = thiz.vars();
        int[] that_ivar = that.vars();
    
        for (int n = 0; n < thiz.varNum(); n++) {
            BDD a = factory.ithVar(this_ivar[n]);
            BDD b = factory.ithVar(that_ivar[n]);
            a.biimpWith(b);
            e.andWith(a);
        }
    
        return e;
    }
    
    
    /** Updates IE/IEcs with new edges obtained from resolving reflective invocations */
    public boolean bindReflection(){
        BDD t1 = actual.restrict(Z.ithVar(0));          // IxV2
        if (USE_VCONTEXT) t1.andWith(V2cdomain.id());   // IxV2cxV2
        BDD t2 = t1.replaceWith(V2toV1);                // IxV1cxV1
        BDD t11 = IE.restrict(M.ithVar(Mmap.get(javaLangClass_newInstance)));   // I
        if(t11.isZero()){
            // no calls to newInstance()
            t11.free();
            
            return false;
        }
        if(REFLECTION_STAT){
            System.out.println("There are " + (int)t11.satCount(Iset) + " calls to Class.newInstance");
        }
        BDD t3  = t2.relprod(t11, bdd.zero());          // IxV1
        t11.free();
        t1.free();
        BDD t31 = t3.replace(ItoI2);                    // I2xV1
        if(TRACE_REFLECTION && TRACE) out.println("t31: " + t31.toStringWithDomains(TS));
                
        BDD t4;
        if (CS_CALLGRAPH) {
            // We keep track of where a call goes under different contexts.
            t4 = t31.relprod(vP, V1.set());              
        } else {
            // By quantifying out V1c, we merge all contexts.
            t4 = t31.relprod(vP, V1set);                  // I2xV1 x V1cxV1xH1 = V1cxI2xH1
        }
        //t4.exist(Iset); 
        if(TRACE_REFLECTION && TRACE) out.println("t4: " + t4.toStringWithDomains(TS) + " of size " + t4.satCount(Iset));
        BDD t41 = t4.relprod(forNameMap, Nset);          // V1cxI2xH1 x H1xI = IxH1cxH1
        t4.free();
        if(TRACE_REFLECTION && TRACE) out.println("t41: " + t41.toStringWithDomains(TS) + " of size " + t41.satCount(Iset));
        
        BDD t6 = t41.relprod(actual, Iset.and(H1set));  // V2xI2xZ
        if(TRACE_REFLECTION_DOMAINS) out.println("t6: " + getBDDDomains(t6));
        t41.free();
        BDD t7 = t6.restrict(Z.ithVar(1));              // V2xI2
        if(TRACE_REFLECTION_DOMAINS) out.println("t7: " + getBDDDomains(t7));
        t6.free();
        if(TRACE_REFLECTION && TRACE) out.println("t7: " + t7.toStringWithDomains(TS) + " of size " + t7.satCount(Iset));
        
        BDD t8 = t7.replace(V2toV1);                    // V1xI2
        if(TRACE_REFLECTION_DOMAINS) out.println("t8: " + getBDDDomains(t8));
        t7.free();
        BDD t9 = t8.relprod(vP, V1set);                 // V1xI2 x V1xH1 = I2xH1
        t8.free();
        
        if(TRACE_REFLECTION && TRACE) out.println("t9: " + t9.toStringWithDomains(TS) + " of size " + t9.satCount(Iset));
        if(TRACE_REFLECTION_DOMAINS) {
            out.println("vP: " + getBDDDomains(vP));
            out.println("t9: " + getBDDDomains(t9));
        }
        BDD constructorIE = bdd.zero(); 
        for(Iterator iter = t9.iterator(H1set.and(I2set)); iter.hasNext();){
            BDD h = (BDD) iter.next();
            //if(TRACE_REFLECTION_DOMAINS) out.println("h: " + getBDDDomains(h));
            int h_i = h.scanVar(H1).intValue();
            Object node = Hmap.get(h_i);
            int i_i = h.scanVar(I2).intValue();
            ProgramLocation mc = (ProgramLocation) Imap.get(i_i);
            if(!(node instanceof ConcreteTypeNode)) {
                //System.err.println("Can't cast " + node + " to ConcreteTypeNode for " + h.toStringWithDomains(TS));
                continue;
            }
            boolean unresolved = false;
            MethodSummary.ConcreteTypeNode n = (ConcreteTypeNode) node;
            String stringConst = (String) MethodSummary.stringNodes2Values.get(n);
            if(stringConst == null){
                unresolved = true;          // not full resolved -- points to something other than a const
                if(missingConst.get(n) == null){
                    if(TRACE_REFLECTION) {
                        System.err.println("No constant string for " + 
                            n + " at " + h.toStringWithDomains(TS));
                    }
                    missingConst.put(n, new Integer(0));
                }                
                continue;
            }
            
            jq_Class c = null;
            try {
                if(!isWellFormed(stringConst)) {
                    if(wellFormedClasses.get(stringConst) == null){
                        if(TRACE_REFLECTION) out.println(stringConst + " is not well-formed.");
                            wellFormedClasses.put(stringConst, new Integer(0));
                        }                

                    continue;
                }
                jq_Type clazz = jq_Type.parseType(stringConst);
                if( clazz instanceof jq_Class && clazz != null){
                    c = (jq_Class) clazz;
            
                    if(TRACE_REFLECTION) out.println("Calling class by name: " + stringConst);
                    c.load();
                    c.prepare();
                    Assert._assert(c != null);
                }else{
                    if(cantCastTypes.get(clazz) == null){
                        if(TRACE_REFLECTION) System.err.println("Can't cast " + clazz + " to jq_Class at " + h.toStringWithDomains(TS) + " -- stringConst: " + stringConst);
                        cantCastTypes.put(clazz, new Integer(0));
                    }
                    continue;
                }
            } catch(NoClassDefFoundError e) {
                if(missingClasses.get(stringConst) == null){
                    if(TRACE_REFLECTION) System.err.println("Resolving reflection: unable to load " + stringConst + 
                        " at " + h.toStringWithDomains(TS));
                    missingClasses.put(stringConst, new Integer(0));
                }
                continue;
            } catch(java.lang.ClassCircularityError e) {
                if(circularClasses.get(stringConst) == null){
                    if(TRACE_REFLECTION) System.err.println("Resolving reflection: circularity error " + stringConst + 
                        " at " + h.toStringWithDomains(TS));
                    circularClasses.put(stringConst, new Integer(0));
                }                
                continue;
            }
            Assert._assert(c != null);            
            
            jq_Method constructor = (jq_Method) c.getDeclaredMember(
                new jq_NameAndDesc(
                    Utf8.get("<init>"), 
                    Utf8.get("()V")));
            //Assert._assert(constructor != null, "No default constructor in class " + c);
            if(constructor == null){
                if(noConstrClasses.get(c) == null){
                    if(TRACE_REFLECTION) System.err.println("No constructor in class " + c);
                    noConstrClasses.put(c, new Integer(0));                    
                }
                continue;
            }
            // add the relation to IE
            BDD constructorCall = M.ithVar(Mmap.get(constructor)).and(h);
            constructorIE.orWith(constructorCall);
            if(unresolved){
                unresolvedCalls.add(mc);
            }
        }
        
        BDD old_reflectiveCalls  = reflectiveCalls.id();
        reflectiveCalls = constructorIE.exist(H1set).replace(I2toI);
        constructorIE.free();
        if(TRACE_REFLECTION && !reflectiveCalls.isZero()){
            out.println("reflectiveCalls: " + reflectiveCalls.toStringWithDomains(TS) + 
                " of size " + reflectiveCalls.satCount(Iset.and(Mset)));
        }
        
        BDD new_reflectiveCalls = reflectiveCalls.apply(old_reflectiveCalls, BDDFactory.diff);
        old_reflectiveCalls.free();
        
        if(!new_reflectiveCalls.isZero()){
            if(TRACE_REFLECTION) {
                out.println("Discovered new_reflectiveCalls: " + 
                    new_reflectiveCalls.toStringWithDomains(TS) + 
                    " of size " + new_reflectiveCalls.satCount(Iset.and(H1set)));
            }
            
            // add the new points-to for reflective calls
            for(Iterator iter = new_reflectiveCalls.iterator(Iset.and(Mset)); iter.hasNext();){
                BDD i_bdd = (BDD) iter.next();
                int I_i = i_bdd.scanVar(I).intValue();
                ProgramLocation mc = (ProgramLocation) Imap.get(I_i);
                int M_i = new_reflectiveCalls.relprod(i_bdd, Iset).scanVar(M).intValue();
                jq_Method target = (jq_Method) Mmap.get(M_i);
                jq_Initializer constructor = (jq_Initializer) target;                
                jq_Type type = constructor.getDeclaringClass();                
                
                visitMethod(target);
            
                MethodSummary ms = MethodSummary.getSummary(mc.getMethod());
                Node node = ms.getRVN(mc);
                if (node != null) {
                    MethodSummary.ConcreteTypeNode h = ConcreteTypeNode.get((jq_Reference) type, mc);
                    int H_i = Hmap.get(h);
                    int V_i = Vmap.get(node);
                    BDD V_arg = V1.ithVar(V_i);
                    
                    if(TRACE_REFLECTION_DOMAINS) {
                        out.println("V_arg: " + getBDDDomains(V_arg));
                    }
                    
                    addToVP(V_arg, h);                    
                }
            }
            
            if (CS_CALLGRAPH){ 
                IE.orWith(new_reflectiveCalls.exist(V1cset));
            } else { 
                IE.orWith(new_reflectiveCalls);
                if (TRACE_SOLVER) {
                    out.println("Call graph edges after: "+IE.satCount(IMset));
                }
            }
            if(TRACE_REFLECTION) out.println("Call graph edges after: "+IE.satCount(IMset));
            
            return true;
        } else {
            return false;
        }
    }
    
    SubtypeHelper retrieveSubtypeHelper(){
        if(this._subtypeHelper == null){
            this._subtypeHelper = SubtypeHelper.newSubtypeHelper(
                    this, System.getProperty("pa.subtypehelpertype")); 
        }
        
        return this._subtypeHelper;
    }
    
    private boolean bindReflectionsWithCasts() {
        if(TRACE_REFLECTION) out.println("Call graph edges before: "+IE.satCount(IMset));
        BDD t1 = IE.restrict(M.ithVar(Mmap.get(javaLangClass_newInstance)));   // I
        if(t1.isZero()){
            // no calls to newInstance()
            t1.free();            
            return false;
        }
        if(REFLECTION_STAT){
            System.out.println("There are " + (int)t1.satCount(Iset) + " calls to Class.newInstance");
        }
        BDD t3  = Iret.relprod(t1, bdd.zero()).replace(V1toV2);         // IxV2                      
        BDD t32 = t3.relprod(A, bdd.zero());                            // I1xV2 x V1xV2 = I1xV1xV2
        //System.out.println("t32: " + t32.toStringWithDomains(TS));
        Assert._assert(T1.size().equals(T2.size()));
        BDD notEqualTypes = (buildEquals(T1, T2)).not();
        BDD t33 = t32.relprod(vT, bdd.zero());                          // I1xV1xV2 x V1xT1 = I1xV1xV2xT1
        //System.out.println("t33: " + t33.toStringWithDomains(TS));
        BDD t34 = t33.relprod(vT.replace(V1toV2).replace(T1toT2), bdd.zero());          // I2xV1xV2xT1 x V2xT2 = I2xV1xV2xT1xT2
        //System.out.println("t34: " + t34.toStringWithDomains(TS));
        BDD tuples = t34.relprod(notEqualTypes, T2set);
        //System.out.println("t35: " + t35.toStringWithDomains(TS));      // V1xV2xIxT1
        t1.free(); t3.free(); t32.free(); t33.free(); t34.free();
        
        BDD constructorIE = bdd.zero();
        for(Iterator iter = tuples.iterator(V1set.and(V2set).and(Iset).and(T1set)); iter.hasNext();){
            BDD tuple = (BDD) iter.next();
            int V1_i = tuple.scanVar(V1).intValue();
            int V2_i = tuple.scanVar(V2).intValue();
            int I_i  = tuple.scanVar(I).intValue();
            int T1_i = tuple.scanVar(T1).intValue();
            
            Node v1 = (Node) Vmap.get(V1_i);
            Node v2 = (Node) Vmap.get(V2_i);
            ProgramLocation mc = (ProgramLocation) Imap.get(I_i);
            jq_Reference t = (jq_Reference) Tmap.get(T1_i);
            if(!(t instanceof jq_Class)){
                System.err.println("Casting to a non-class type: " + t + ", skipping.");
                continue;
            }            
            if(TRACE_REFLECTION) {
                System.out.println("The result of a call at " + mc.toStringLong() + 
                " variable " + v2 + 
                " is cast at " + v1 + 
                " to type " + t);
            }
            
            SubtypeHelper subtypeHelper = retrieveSubtypeHelper();
            Collection subtypes = subtypeHelper.getSubtypes((jq_Class) t);
            if(subtypes == null){
                System.err.println("No subtypes for class " + t.getName());
                continue;
            }

            for(Iterator typeIter = subtypes.iterator(); typeIter.hasNext();){
                jq_Class c = (jq_Class) typeIter.next();    
                jq_Method constructor = (jq_Method) c.getDeclaredMember(
                    new jq_NameAndDesc(
                        Utf8.get("<init>"), 
                        Utf8.get("()V")));
                //Assert._assert(constructor != null, "No default constructor in class " + c);
                if(constructor == null){
                    if(noConstrClasses.get(c) == null){
                        if(TRACE_REFLECTION) System.err.println("No constructor in class " + c);
                        noConstrClasses.put(c, new Integer(0));                    
                    }
                    continue;
                }
                // add the relation to IE
                BDD constructorCall = I.ithVar(I_i).andWith(M.ithVar(Mmap.get(constructor)));
                constructorIE.orWith(constructorCall);
            }
        }
        
        BDD old_reflectiveCalls  = reflectiveCalls.id();
        reflectiveCalls = constructorIE;

        if(TRACE_REFLECTION && !reflectiveCalls.isZero()){
            out.println("reflectiveCalls: " + reflectiveCalls.toStringWithDomains(TS) + 
                " of size " + reflectiveCalls.satCount(Iset.and(Mset)));
        }
        
        BDD new_reflectiveCalls = reflectiveCalls.apply(old_reflectiveCalls, BDDFactory.diff);
        old_reflectiveCalls.free();
        
        if(!new_reflectiveCalls.isZero()){
            if(TRACE_REFLECTION) {
                out.println("Discovered new_reflectiveCalls: " + 
                    new_reflectiveCalls.toStringWithDomains(TS) + 
                    " of size " + new_reflectiveCalls.satCount(Iset.and(Mset)));
            }
            
            // add the new points-to for reflective calls
            for(Iterator iter = new_reflectiveCalls.iterator(Iset.and(Mset)); iter.hasNext();){
                BDD i_bdd = (BDD) iter.next();
                int I_i = i_bdd.scanVar(I).intValue();
                ProgramLocation mc = (ProgramLocation) Imap.get(I_i);
                int M_i = i_bdd.scanVar(M).intValue();
                jq_Method target = (jq_Method) Mmap.get(M_i);
                jq_Initializer constructor = (jq_Initializer) target;                
                jq_Type type = constructor.getDeclaringClass();
                if(TRACE_REFLECTION){
                    System.out.println("Adding a call from " + mc.toStringLong() + " to " + constructor);
                }
                
                visitMethod(target);
            
                MethodSummary ms = MethodSummary.getSummary(mc.getMethod());
                Node node = ms.getRVN(mc);
                if (node != null) {
                    MethodSummary.ConcreteTypeNode h = ConcreteTypeNode.get((jq_Reference) type, mc);
                    int H_i = Hmap.get(h);
                    int V_i = Vmap.get(node);
                    BDD V_arg = V1.ithVar(V_i);
                    
                    if(TRACE_REFLECTION_DOMAINS) {
                        out.println("V_arg: " + getBDDDomains(V_arg));
                    }
                    
                    addToVP(V_arg, h);                    
                }
            }
            
            if (CS_CALLGRAPH){ 
                IE.orWith(new_reflectiveCalls.exist(V1cset));
            } else { 
                IE.orWith(new_reflectiveCalls);
                if (TRACE_SOLVER) {
                    out.println("Call graph edges after: "+IE.satCount(IMset));
                }
            }
            if(TRACE_REFLECTION) out.println("Call graph edges after: "+IE.satCount(IMset));            
            return true;
        } else {
            if(TRACE_REFLECTION) out.println("Call graph edges after: "+IE.satCount(IMset));
            return false;
        }        
    }
    
    boolean bindForName(){
        jq_Method forNameMethod = class_class.getDeclaredMethod("forName");
        Assert._assert(forNameMethod != null);
        int M_i = Mmap.get(forNameMethod);
        BDD M_bdd = M.ithVar(M_i);
        BDD I = IE.relprod(M_bdd, Mset);
        boolean change = false;
        for(Iterator iter = I.iterator(Iset); iter.hasNext();){
            BDD I_bdd = (BDD) iter.next();
            if(TRACE_FORNAME){
                System.out.println("Resolving a forName call at " + I_bdd.toStringWithDomains(TS));
            }
                        
            BDD t = actual.relprod(I_bdd, Iset);
//            System.out.println("t: " + t.toStringWithDomains(TS));            
            BDD t1 = t.restrictWith(Z.ithVar(1)).replace(V2toV1);
            t.free();
//            System.out.println("t1: " + t1.toStringWithDomains(TS));
            BDD t2 = vP.relprod(t1, V1set);
            t1.free();
            
            if(!t2.isZero()){
//                System.out.println("t2: " + t2.toStringWithDomains(TS));
                for(Iterator iter2 = t2.iterator(H1set); iter2.hasNext();){
                    int h_i = ((BDD) iter2.next()).scanVar(H1).intValue();
                    Node n = (Node) Hmap.get(h_i);
                    if(n instanceof MethodSummary.ConcreteTypeNode){
                        ConcreteTypeNode cn = (ConcreteTypeNode) n;
                        String stringConst = (String) MethodSummary.stringNodes2Values.get(n);
                        if(stringConst != null){
//                            System.out.println(I_bdd.toStringWithDomains(TS) + " -> " + stringConst);
                            if(stringConst == null){
                                if(missingConst.get(stringConst) == null){
                                    if(TRACE_FORNAME) System.err.println("No constant string for " + n + " at " + n);                                    
                                    missingConst.put(stringConst, new Integer(0));
                                }                
                                continue;
                            }
                            
                            jq_Class c = null;
                            try {
                                if(!isWellFormed(stringConst)) {
                                    if(wellFormedClasses.get(stringConst) == null){
                                        if(TRACE_FORNAME) out.println(stringConst + " is not well-formed.");
                                            wellFormedClasses.put(stringConst, new Integer(0));
                                        }                

                                    continue;
                                }
                                jq_Type clazz = jq_Type.parseType(stringConst);
                                if( clazz instanceof jq_Class && clazz != null){
                                    c = (jq_Class) clazz;
                            
//                                    if(TRACE_REFLECTION) out.println("Calling class by name: " + stringConst);
                                    c.load();
                                    c.prepare();
                                    Assert._assert(c != null);
                                }else{
                                    if(cantCastTypes.get(clazz) == null){
                                        if(TRACE_FORNAME) System.err.println("Can't cast " + clazz + " to jq_Class at " + I_bdd.toStringWithDomains(TS) + " -- stringConst: " + stringConst);
                                        cantCastTypes.put(clazz, new Integer(0));
                                    }
                                    continue;
                                }
                            } catch(NoClassDefFoundError e) {
                                if(missingClasses.get(stringConst) == null){
                                    if(TRACE_FORNAME) System.err.println("Resolving reflection: unable to load " + stringConst + 
                                        " at " + I_bdd.toStringWithDomains(TS));
                                    missingClasses.put(stringConst, new Integer(0));
                                }
                                continue;
                            } catch(java.lang.ClassCircularityError e) {
                                if(circularClasses.get(stringConst) == null){
                                    if(TRACE_FORNAME) System.err.println("Resolving reflection: circularity error " + stringConst + 
                                        " at " + I_bdd.toStringWithDomains(TS));
                                    circularClasses.put(stringConst, new Integer(0));
                                }                
                                continue;
                            }
                            Assert._assert(c != null);            
                            
                            jq_Method constructor = c.getClassInitializer();
                            
                            if(constructor != null){
                                int M2_i = Mmap.get(constructor);
                                BDD t11 = M.ithVar(M2_i);
                                BDD t22 = t11.andWith(I_bdd.id());
                                
                                if(IE.and(t22).isZero()){                                
                                    IE.orWith(t22);
                                    if(TRACE_FORNAME) {
                                        System.out.println("Calling " + constructor + " as a side-effect of Class.forName at " + 
                                            I_bdd.toStringWithDomains(TS));
                                    }                                                                 
                                    change = true;
                                }else{
                                    t22.free();
                                }
                            }else{
                                if(TRACE_FORNAME) {
                                    System.out.println("No class constructor in " + c);
                                }
                            }
                        }
                    }
                }
            }else{
                if(TRACE_FORNAME){
                    System.out.println("No points-to set at " + I_bdd.toStringWithDomains(TS));
                }
            }
            I_bdd.free();
            t2.free();
        }
        M_bdd.free();
        I.free();
        
        return change;        
    }    
      
    private boolean isWellFormed(String stringConst) {
        if(stringConst.equals(".")) {
            return false;
        }
        int dotCount = 0;
        for(int i = 0; i < stringConst.length(); i++){
            char ch = stringConst.charAt(i);
            
            if(ch == '.'){
                dotCount++;                
            } else {
                if(ch != '$' && ch != '_' && !Character.isLetterOrDigit(ch)){
                    return false;                
                }      
            }
        }
        
        //if(dotCount == 0) return false;
        
        return true;
    }

    BDD old3_t3;
    BDD old3_vP;
    BDD old3_t4;
    BDD old3_hT;
    BDD old3_t6;
    BDD old3_t9[];
    
    // t1 = actual x (z=0)
    // t3 = t1 x mI
    // new_t3 = t3 - old_t3
    // new_vP = vP - old_vP
    // t4 = t3 x new_vP
    // old_t3 = t3
    // t4 |= new_t3 x vP
    // new_t4 = t4 - old_t4
    // new_hT = hT - old_hT
    // t5 = t4 x new_hT
    // old_t4 = t4
    // t5 |= new_t4 x hT
    // t6 = t5 x cha
    // IE |= t6
    // old_vP = vP
    // old_hT = hT
    
    public void bindInvocations_incremental() {
        BDD t1 = actual.restrict(Z.ithVar(0)); // IxV2
        if (USE_VCONTEXT) t1.andWith(V2cdomain.id()); // IxV2cxV2
        t1.replaceWith(V2toV1); // IxV1cxV1
        BDD t3 = t1.relprod(mI, Mset); // IxV1cxV1 & MxIxN = IxV1cxV1xN
        t1.free();
        BDD new_t3 = t3.apply(old3_t3, BDDFactory.diff);
        old3_t3.free();
        if (false) out.println("New invokes: "+new_t3.toStringWithDomains());
        BDD new_vP = vP.apply(old3_vP, BDDFactory.diff);
        old3_vP.free();
        if (false) out.println("New vP: "+new_vP.toStringWithDomains());
        BDD t4, new_t4;
        if (CS_CALLGRAPH) {
            // We keep track of where a call goes under different contexts.
            t4 = t3.relprod(new_vP, V1.set()); // IxV1cxV1xN x V1cxV1xH1cxH1 = V1cxIxH1cxH1xN
            old3_t3 = t3;
            t4.orWith(new_t3.relprod(vP, V1.set())); // IxV1cxV1xN x V1cxV1xH1cxH1 = V1cxIxH1cxH1xN
            new_t3.free();
            new_t4 = t4.apply(old3_t4, BDDFactory.diff);
            old3_t4.free();
        } else {
            // By quantifying out V1c, we merge all contexts.
            t4 = t3.relprod(new_vP, V1set); // IxV1cxV1xN x V1cxV1xH1cxH1 = IxH1cxH1xN
            old3_t3 = t3;
            t4.orWith(new_t3.relprod(vP, V1set)); // IxV1cxV1xN x V1cxV1xH1cxH1 = IxH1cxH1xN
            new_t3.free();
            new_t4 = t4.apply(old3_t4, BDDFactory.diff);
            old3_t4.free();
        }
        if (false) out.println("New 'this' objects: "+new_t4.toStringWithDomains());
        BDD new_hT = hT.apply(old3_hT, BDDFactory.diff);
        old3_hT.free();
        BDD t5 = t4.relprod(new_hT, H1set); // (V1cx)IxH1cxH1xN x H1xT2 = (V1cx)IxT2xN
        new_hT.free();
        old3_t4 = t4;
        t5.orWith(new_t4.relprod(hT, H1set)); // (V1cx)IxH1cxH1xN x H1xT2 = (V1cx)IxT2xN
        new_t4.free();
        BDD t6 = t5.relprod(cha, T2Nset); // (V1cx)IxT2xN x T2xNxM = (V1cx)IxM
        t5.free();
        
        if (TRACE_SOLVER) out.println("Call graph edges before: "+IE.satCount(IMset));
        if (CS_CALLGRAPH) IE.orWith(t6.exist(V1cset));
        else IE.orWith(t6.id());
        if (TRACE_SOLVER) out.println("Call graph edges after: "+IE.satCount(IMset));
        
        old3_vP = vP.id();
        old3_hT = hT.id();
        
        if (CONTEXT_SENSITIVE) {
            if (CS_CALLGRAPH) t6.replaceWith(V1ctoV2c); // V2cxIxM
            t6.andWith(IEfilter.id()); // V2cxIxV1cxM
            IEcs.orWith(t6);
        } else if (OBJECT_SENSITIVE) {
            throw new Error();
        } else if (CARTESIAN_PRODUCT) {
            // t6 |= statics
            // new_t6 = t6 - old_t6
            // for (i=0..k)
            //     t8[i] = actual x (z=i)
            //     t9[i] = t8[i] x vP
            //     new_t9[i] = t9[i] - old_t9[i]
            //     new_context &= new_t9[i]
            //     new_t9[i] &= V1cH1equals[i]
            //     tb[i] = new_t9[i] x t6
            //     tb[i] |= t9[i] x new_t6
            //     old_t9[i] = t9[i]
            //     tc[i] = tb[i] x formal_i
            //     newPt |= tc[i]
            // newPt2 = newPt x new_context
            // newPt2 &= vPfilter
            // vP |= newPt2
            // new_context &= t6
            // IEcs |= new_context
            
            // Add all statically-bound calls to t6.
            // They are true under all contexts.
            BDD statics = staticCalls.exist(V1.set()); // IxM
            if (CS_CALLGRAPH) statics.andWith(V1cdomain.id()); // V1cxIxM
            t6.orWith(statics); // V1cxIxM | V1cxIxM = V1cxIxM
            
            // Edges in the call graph.  Invocation I under context V2c has target method M.
            if (CS_CALLGRAPH) t6.replaceWith(V1ctoV2c); // V2cxIxM
            
            BDD new_t6 = t6.apply(old3_t6, BDDFactory.diff);
            
            // The context for the new cg edges are based on the points-to set of every parameter.
            BDD newContext = bdd.one();
            // We need to add points-to relations for each of the actual parameters.
            BDD newPt = bdd.zero();
            for (int i = MAX_PARAMS - 1; i >= 0; --i) {
                if (TRACE_BIND) System.out.println("Param "+i+":");
                BDD t8_i = actual.restrict(Z.ithVar(i)).and(V2cdomain); // IxV2
                t8_i.replaceWith(V2toV1); // IxV1
                if (TRACE_BIND) System.out.println("t8 = "+t8_i.toStringWithDomains());
                BDD t9_i = t8_i.relprod(vP, V1.set()); // IxV1 x V1cxV1xH1cxH1 = V1cxIxH1cxH1
                if (TRACE_BIND) {
                    System.out.println("t9 =");
                    dumpWithV1c(t9_i, Iset.and(H1set));
                }
                t8_i.free();
                
                t9_i.replaceWith(V1ctoV2c); // V2cxIxH1cxH1
                BDD new_t9_i = t9_i.apply(old3_t9[i], BDDFactory.diff);
                old3_t9[i] = t9_i.id();
                // Invocation I under context V2c leads to context V1c
// 20%
                newContext.andWith(new_t9_i.replace(H1toV1c[i])); // V2cxIxV1c[i]
                
                // Calculate new points-to relations for this actual parameter.
                new_t9_i.andWith(V1cH1equals[i].id()); // V2cxIxV1c[i]xH1cxH1
                t9_i.andWith(V1cH1equals[i].id());
                BDD tb_i = new_t9_i.relprod(t6, V2cset); // V2cxIxV1c[i]xH1cxH1 x (V2cx)IxM = IxV1c[i]xMxH1cxH1
                tb_i.orWith(t9_i.relprod(new_t6, V2cset));
                BDD formal_i = formal.restrict(Z.ithVar(i)); // MxV1
                BDD tc_i = tb_i.relprod(formal_i, Mset); // IxV1c[i]xMxH1cxH1 x MxV1 = IxV1c[i]xV1xH1cxH1
                formal_i.free(); tb_i.free();
                for (int j = 0; j < MAX_PARAMS; ++j) {
                    if (i == j) continue;
                    tc_i.andWith(V1c[j].domain());
                }
                if (TRACE_BIND) dumpVP(tc_i.exist(Iset));
                newPt.orWith(tc_i);
            }
            
            // Now, filter out unrealizables.
            // IxV1c[i]xV1xH1cxH1 x V2cxIxV1c[i] = V1cxV1xH1cxH1
// 13%
            BDD newPt2 = newPt.relprod(newContext, V2cset.and(Iset));
            newPt.free();
            if (TRACE_BIND) dumpVP(newPt2);
            
            if (FILTER_VP) newPt2.andWith(vPfilter.id());
            vP.orWith(newPt2);
            
            newContext.andWith(t6.id()); // V2cxIxV1c[k]xM
            old3_t6 = t6;
            if (TRACE_BIND) System.out.println("context = "+newContext.toStringWithDomains());
            IEcs.orWith(newContext);
        }
    }
    
    public boolean handleNewTargets() {
        if (TRACE_SOLVER) out.println("Handling new target methods...");
        BDD targets = IE.exist(Iset); // IxM -> M
        targets.applyWith(visited.id(), BDDFactory.diff);
        if (targets.isZero()) {
            //System.err.println("No targets");
            return false;  
        } 
        if (TRACE_SOLVER) out.println("New target methods: "+targets.satCount(Mset));
        while (!targets.isZero()) {
            BDD target = targets.satOne(Mset, false);
            int M_i = target.scanVar(M).intValue();
            jq_Method method = (jq_Method) Mmap.get(M_i);
            if(method != null){
                if (TRACE) out.println("New target method: "+method);
                visitMethod(method);
            } else {
                System.err.println("NULL method in handleNewTargets");
            }
            targets.applyWith(target, BDDFactory.diff);
        }
        return true;
    }
    
    public void bindParameters() {
        if (INCREMENTAL2) {
            bindParameters_incremental();
            return;
        }
        
        if (TRACE_SOLVER) out.println("Binding parameters...");
        
        BDD my_IE = USE_VCONTEXT ? IEcs : IE;
        
        if (TRACE_SOLVER) out.println("Call graph edges: "+my_IE.nodeCount());
        
        BDD my_formal = CARTESIAN_PRODUCT ? formal.and(Z.varRange(0, MAX_PARAMS-1).not()) : formal;
        BDD my_actual = CARTESIAN_PRODUCT ? actual.and(Z.varRange(0, MAX_PARAMS-1).not()) : actual;
        
        BDD t1 = my_IE.relprod(my_actual, Iset); // V2cxIxV1cxM x IxZxV2 = V1cxMxZxV2cxV2
        BDD t2 = t1.relprod(my_formal, MZset); // V1cxMxZxV2cxV2 x MxZxV1 = V1cxV1xV2cxV2
        t1.free();
        if (TRACE_SOLVER) out.println("A before param bind: "+A.nodeCount());
        A.orWith(t2);
        if (TRACE_SOLVER) out.println("A after param bind: "+A.nodeCount());
        
        if (TRACE_SOLVER) out.println("Binding return values...");
        BDD my_IEr = USE_VCONTEXT ? IEcs.replace(V1cV2ctoV2cV1c) : IE;
        BDD t3 = my_IEr.relprod(Iret, Iset); // V1cxIxV2cxM x IxV1 = V1cxV1xV2cxM
        BDD t4 = t3.relprod(Mret, Mset); // V1cxV1xV2cxM x MxV2 = V1cxV1xV2cxV2
        t3.free();
        if (TRACE_SOLVER) out.println("A before return bind: "+A.nodeCount());
        A.orWith(t4);
        if (TRACE_SOLVER) out.println("A after return bind: "+A.nodeCount());
        
        if (TRACE_SOLVER) out.println("Binding exceptions...");
        BDD t5 = my_IEr.relprod(Ithr, Iset); // V1cxIxV2cxM x IxV1 = V1cxV1xV2cxM
        if (USE_VCONTEXT) my_IEr.free();
        BDD t6 = t5.relprod(Mthr, Mset); // V1cxV1xV2cxM x MxV2 = V1cxV1xV2cxV2
        t5.free();
        if (TRACE_SOLVER) out.println("A before exception bind: "+A.nodeCount());
        A.orWith(t6);
        if (TRACE_SOLVER) out.println("A after exception bind: "+A.nodeCount());
        
    }
    
    BDD old2_myIE;
    BDD old2_visited;
    
    public void bindParameters_incremental() {

        BDD my_IE = USE_VCONTEXT ? IEcs : IE;
        BDD new_myIE = my_IE.apply(old2_myIE, BDDFactory.diff);
        
        BDD new_visited = visited.apply(old2_visited, BDDFactory.diff);
        // add in any old edges targetting newly-visited methods, because the
        // argument/retval binding doesn't occur until the method has been visited.
        new_myIE.orWith(old2_myIE.and(new_visited));
        old2_myIE.free();
        old2_visited.free();
        new_visited.free();
        
        if (TRACE_SOLVER) out.println("New call graph edges: "+new_myIE.nodeCount());
        
        BDD my_formal = CARTESIAN_PRODUCT ? formal.and(Z.varRange(0, MAX_PARAMS-1).not()) : formal;
        BDD my_actual = CARTESIAN_PRODUCT ? actual.and(Z.varRange(0, MAX_PARAMS-1).not()) : actual;
        
        if (TRACE_SOLVER) out.println("Binding parameters...");
        
        BDD t1 = new_myIE.relprod(my_actual, Iset); // V2cxIxV1cxM x IxZxV2 = V1cxMxZxV2cxV2
        BDD t2 = t1.relprod(my_formal, MZset); // V1cxMxZxV2cxV2 x MxZxV1 = V1cxV1xV2cxV2
        t1.free();
        if (TRACE_SOLVER) out.println("A before param bind: "+A.nodeCount());
        A.orWith(t2);
        if (TRACE_SOLVER) out.println("A after param bind: "+A.nodeCount());
        
        if (TRACE_SOLVER) out.println("Binding return values...");
        BDD new_myIEr = USE_VCONTEXT ? new_myIE.replace(V1cV2ctoV2cV1c) : new_myIE;
        BDD t3 = new_myIEr.relprod(Iret, Iset); // V1cxIxV2cxM x IxV1 = V1cxV1xV2cxM
        BDD t4 = t3.relprod(Mret, Mset); // V1cxV1xV2cxM x MxV2 = V1cxV1xV2cxV2
        t3.free();
        if (TRACE_SOLVER) out.println("A before return bind: "+A.nodeCount());
        A.orWith(t4);
        if (TRACE_SOLVER) out.println("A after return bind: "+A.nodeCount());
        
        if (TRACE_SOLVER) out.println("Binding exceptions...");
        BDD t5 = new_myIEr.relprod(Ithr, Iset); // V1cxIxV2cxM x IxV1 = V1cxV1xV2cxM
        if (USE_VCONTEXT) new_myIEr.free();
        BDD t6 = t5.relprod(Mthr, Mset); // V1cxV1xV2cxM x MxV2 = V1cxV1xV2cxV2
        t5.free();
        if (TRACE_SOLVER) out.println("A before exception bind: "+A.nodeCount());
        A.orWith(t6);
        if (TRACE_SOLVER) out.println("A after exception bind: "+A.nodeCount());
        
        new_myIE.free();
        old2_myIE = my_IE.id();
        old2_visited = visited.id();
    }
    
    public void assumeKnownCallGraph() {
        if (VerifyAssertions)
            Assert._assert(!IE.isZero());
        handleNewTargets();
        addAllMethods();
        buildTypes();
        bindParameters();
        long time = System.currentTimeMillis();
        solvePointsTo();
        System.out.println("Solve points-to alone took "+(System.currentTimeMillis()-time)/1000.+"s");
    }
    
    public void iterate() {
        BDD vP_old = vP.id();
        BDD IE_old = IE.id();
        boolean change;
        for (int major = 1; ; ++major) {
            change = false;
            
            out.println("Discovering call graph, iteration "+major+": "+(int)visited.satCount(Mset)+" methods.");
            long time = System.currentTimeMillis();
            buildTypes();
            solvePointsTo();
            bindInvocations();
            if(RESOLVE_REFLECTION){
                if(USE_CASTS_FOR_REFLECTION){
                    if(bindReflectionsWithCasts()){
                        change = true;   
                    }
                }else{
                    if(bindReflection()){
                        change = true;
                    }
                }
            }
            if(RESOLVE_FORNAME){
                if(bindForName()){
                    change = true;
                }
            }
            if (handleNewTargets())
                change = true;
            if (!change && vP.equals(vP_old) && IE.equals(IE_old)) {
                if (TRACE_SOLVER) out.println("Finished after "+major+" iterations.");
                break;
            }
            vP_old.free(); vP_old = vP.id();
            IE_old.free(); IE_old = IE.id();
            addAllMethods();
            bindParameters();
            if (TRACE_SOLVER)
                out.println("Time spent: "+(System.currentTimeMillis()-time)/1000.);
        }
    }
    
    public void numberPaths(CallGraph cg, ObjectCreationGraph ocg, boolean updateBits) {
        System.out.print("Counting size of call graph...");
        long time = System.currentTimeMillis();
        vCnumbering = countCallGraph(cg, ocg, updateBits);
        if(PRINT_CALL_GRAPH_SCCS){
            SCCPathNumbering sccNumbering = (SCCPathNumbering) vCnumbering;
            SCCTopSortedGraph sccGraph = sccNumbering.getSCCGraph();
            System.out.println("Printing the SCC in the call graph (" + sccGraph.list().size()+ ")");
            for(Iterator iter = sccGraph.list().iterator(); iter.hasNext(); ){
                    SCComponent component = (SCComponent) iter.next();

                    if(component.size() < 2) continue;
                    if(component.nodes() == null) continue;

                    System.out.print("\t" + component.getId() + "\t: " + component.nodes().length + "\t");

                    for(int i = 0; i < component.nodes().length; i++){
                        Object node = component.nodes()[i];
                        System.out.print(node.toString() + " ");
                    }
                    System.out.println("");
            }
            System.out.println("Done.");
        }

        if (OBJECT_SENSITIVE) {
            oCnumbering = new SCCPathNumbering(objectPathSelector);
            BigInteger paths = (BigInteger) oCnumbering.countPaths(ocg);
            if (updateBits) {
                HC_BITS = VC_BITS = paths.bitLength();
                System.out.print("Object paths="+paths+" ("+VC_BITS+" bits), ");
            }
        }
        if (CONTEXT_SENSITIVE && MAX_HC_BITS > 1) {
            hCnumbering = countHeapNumbering(cg, updateBits);
        }
        time = System.currentTimeMillis() - time;
        System.out.println("done. ("+time/1000.+" seconds)");
    }
    
    static CallGraph loadCallGraph(Collection roots) {
        if (new File(initialCallgraphFileName).exists()) {
            try {
                System.out.print("Loading initial call graph...");
                long time = System.currentTimeMillis();
                CallGraph cg = new LoadedCallGraph(initialCallgraphFileName);
                time = System.currentTimeMillis() - time;
                System.out.println("done. ("+time/1000.+" seconds)");
                if (cg.getRoots().containsAll(roots)) {    // TODO
                    roots = cg.getRoots();
                    //LOADED_CALLGRAPH = true;
                    return cg;
                } else {
                    System.out.println("Call graph doesn't match named class, rebuilding...");
                    cg = null;
                }
            } catch (IOException x) {
                x.printStackTrace();
            }
        }
        return null;
    }
    
    public void addDefaults() {
        // First, print something because the set of objects reachable via System.out changes
        // depending on whether something has been printed or not!
        System.out.println("Adding default static variables.");
        
        // Add the default static variables (System in/out/err...)
        GlobalNode.GLOBAL.addDefaultStatics();
        
        // If using object-sensitive, initialize the object creation graph.
        this.ocg = null;
        if (OBJECT_SENSITIVE) {
            this.ocg = new ObjectCreationGraph();
            //ocg.handleCallGraph(cg);
            this.ocg.addRoot(null);
            for (Iterator i = ConcreteObjectNode.getAll().iterator(); i.hasNext(); ) {
                ConcreteObjectNode con = (ConcreteObjectNode) i.next();
                if (con.getDeclaredType() == null) continue;
                this.ocg.addEdge(null, (Node) null, con.getDeclaredType());
            }
        }
    }
    
    public void run(CallGraph cg, Collection rootMethods) throws IOException {
        //run(null, cg, rootMethods);
        run("buddy", cg, rootMethods);
    }
    public void run(String bddfactory, CallGraph cg, Collection rootMethods) throws IOException {
        addDefaults();
        initializeStubs();
        
        // If we have a call graph, use it for numbering and calculating domain sizes.
        if (cg != null) {
            numberPaths(cg, ocg, true);
        }
        
        if (CARTESIAN_PRODUCT && false) {
            VC_BITS = (HC_BITS + H_BITS) * MAX_PARAMS;
            System.out.println("Variable context bits = ("+HC_BITS+"+"+H_BITS+")*"+MAX_PARAMS+"="+VC_BITS);
        }
        
        // Now we know domain sizes, so initialize the BDD package.
        initializeBDD(bddfactory);        
        initializeMaps();
        if(ADD_HEAP_FILTER) initializeHeapLocations();
        this.rootMethods.addAll(rootMethods);
        
        if (DUMP_SSA) {
            String dumppath = System.getProperty("pa.dumppath");
            if (dumppath != null) System.setProperty("bdddumpdir", dumppath);
            Object dummy = new Object();
            bddIRBuilder = new BuildBDDIR(bdd, M, Mmap, dummy);
            varorder += "_" + bddIRBuilder.getVarOrderDesc();
            System.out.println("Using variable ordering " + varorder);
            int[] ordering = bdd.makeVarOrdering(reverseLocal, varorder);
            bdd.setVarOrder(ordering);
        } else {
            bddIRBuilder = null;
        }
        
        
        // Use the existing call graph to calculate IE filter
        if (cg != null) {
            System.out.print("Calculating call graph relation...");
            long time = System.currentTimeMillis();
            calculateIEfilter(cg);
            time = System.currentTimeMillis() - time;
            System.out.println("done. ("+time/1000.+" seconds)");
            
            // Build up var-heap correspondence in context-sensitive case.
            if (CONTEXT_SENSITIVE && HC_BITS > 1) {
                System.out.print("Building var-heap context correspondence...");
                time = System.currentTimeMillis();
                buildVarHeapCorrespondence(cg);
                time = System.currentTimeMillis() - time;
                System.out.println("done. ("+time/1000.+" seconds)");
            } else if (THREAD_SENSITIVE) {
                buildThreadV1H1(thread_runs, cg);
            }
            
            // Use the IE filter as the set of invocation edges.
            if (!DISCOVER_CALL_GRAPH) {
                if (VerifyAssertions)
                    Assert._assert(IEfilter != null);
                if (USE_VCONTEXT) {
                    IEcs = IEfilter;
                    IE = IEcs.exist(V1cV2cset);
                } else {
                    IE = IEfilter;
                }
            }
        }
        
        // Start timing.
        long time = System.currentTimeMillis();
        
        // Add the global relations first.
        visitGlobalNode(GlobalNode.GLOBAL);
        for (Iterator i = ConcreteObjectNode.getAll().iterator(); i.hasNext(); ) {
            ConcreteObjectNode con = (ConcreteObjectNode) i.next();
            visitGlobalNode(con);
        }
        
        // Calculate the relations for the root methods.
        for (Iterator i = rootMethods.iterator(); i.hasNext(); ) {
            jq_Method m = (jq_Method) i.next();
            
            // Add placeholder objects for each of the parameters of root methods.
            if (ADD_ROOT_PLACEHOLDERS > 0) {
                addPlaceholdersForParams(m, ADD_ROOT_PLACEHOLDERS);
            }

            visitMethod(m);
        }
        
        // Add placeholder objects for public methods.
        if (PUBLIC_PLACEHOLDERS > 0) {
            Iterator i;
            if (cg != null) i = cg.getAllMethods().iterator();
            else i = rootMethods.iterator();
            while (i.hasNext()) {
                jq_Method m = (jq_Method) i.next();
                if (!m.isPublic()) continue;
                addPlaceholdersForParams(m, PUBLIC_PLACEHOLDERS);
            }
        }
        
        // Calculate the relations for any other methods we know about.
        handleNewTargets();
        
        // For object-sensitivity, build up the context mapping.
        if (OBJECT_SENSITIVE) {
            buildTypes();
            buildObjectSensitiveV1H1(ocg);
        }
        
        // Now that contexts are calculated, add the relations for all methods
        // to the global relation.
        addAllMethods();
        
        System.out.println("Time spent initializing: "+(System.currentTimeMillis()-time)/1000.);
        
        if (DISCOVER_CALL_GRAPH || OBJECT_SENSITIVE || CARTESIAN_PRODUCT) {
            Assert._assert(!SKIP_SOLVE);
            time = System.currentTimeMillis();
            if(CONTEXT_SENSITIVE) {
                //visited = bdd.zero();
            }
            iterate();
            System.out.println("Time spent solving: "+(System.currentTimeMillis()-time)/1000.);
            //traceNoDestanation();
        } else {
            if (DUMP_INITIAL) {
                buildTypes();
                if (SPECIAL_MAP_INFO) buildSpecialMapInfo();

                try {
                    long time2 = System.currentTimeMillis();
                    dumpBDDRelations();
                    System.out.println("Dump took "+(System.currentTimeMillis()-time2)/1000.+"s");
                    if (DUMP_SSA) dumpSSA();
                } catch (IOException x) {  }
            }
            if (SKIP_SOLVE) return;
            time = System.currentTimeMillis();
            assumeKnownCallGraph();
            System.out.println("Time spent solving: "+(System.currentTimeMillis()-time)/1000.);
        }
        
        if(FIX_NO_DEST){
            analyzeIE();
        }
        if(REFLECTION_STAT){
            saveReflectionStats();
        }
        if(FORNAME_STAT){
            saveForNameStats();
        }
        //initializeForNameMapEntries();

        printSizes();
        
        if (DUMP_CALLGRAPH) {
            System.out.println("Writing call graph...");
            time = System.currentTimeMillis();
            dumpCallGraph();
            System.out.println("Time spent writing: " + (System.currentTimeMillis() - time) / 1000.);
        }

        if (DUMP_RESULTS) {
            System.out.println("Writing results...");
            time = System.currentTimeMillis();
            //dumpResults(resultsFileName);
            dumpBDDRelations();
            System.out.println("Time spent writing: "+(System.currentTimeMillis()-time)/1000.);
        }
    }
   
    /**
     * This routine reads locations from a file and adds them to Hmap sequentially.
     * */
    private void initializeHeapLocations() throws IOException {
        BufferedReader r = null;
        r = new BufferedReader(new FileReader("heap_filter.txt"));
        String s = null;
        int lineCount = 0;
        while ((s = r.readLine()) != null) {
            lineCount++;
            ConcreteTypeNode cn = readToStringResult(s);
            if(cn == null) {
                System.err.println("Can't convert " + s + " to a valid node");
                continue;
            }

            int index = Hmap.get(cn);
            if(TRACE) {
                System.out.println("Location '" + s + "' matches " + index);
            }
        }            
        System.out.println("Read and initialized " + lineCount + " locations.");
    }
    
    public static ConcreteTypeNode readToStringResult(String str) {
        StringTokenizer tok = new StringTokenizer(str, ":");
        //String ID = tok.nextToken();
        jq_Reference type = (jq_Reference) jq_Type.parseType(tok.nextToken());
        if(type == null) return null;
        //ProgramLocation pl = ProgramLocation.read(tok);
        String methodName = tok.nextToken();
        jq_Method m = (jq_Method) jq_Method.parseMember(methodName);
        if (m == null) return null;
        QuadProgramLocation pl = null;
        String pls = tok.nextToken();
        StringTokenizer t = new StringTokenizer(pls, " ");
        t.nextToken();
        if (t.nextToken().equals("quad")) {
            int id = new Integer(t.nextToken()).intValue();
            if (m.getBytecode() == null) return null;
            ControlFlowGraph cfg = CodeCache.getCode(m);                
            for (QuadIterator i = new QuadIterator(cfg); i.hasNext(); ) {
                Quad q = i.nextQuad();
                
                if (q.getID() == id) {
                    pl = new QuadProgramLocation(m, q);
                }
            }
        }
        if(pl == null) return null;
        
        String opns = tok.nextToken();
        Integer opn = opns.equals("null") ? null : Integer.decode(opns);
        
        ConcreteTypeNode n = ConcreteTypeNode.get(type, pl, opn);
        return n;
    }
    
    void saveReflectionStats() throws IOException {
        PrintWriter w = null;
        try {
            out.println("Saving reflection statistics in " + REFLECTION_STAT_FILE);
            w = new PrintWriter(new FileWriter(REFLECTION_STAT_FILE));                
            BDD newInstanceCalls = IE.restrict(M.ithVar(Mmap.get(javaLangClass_newInstance)));   // I
            
            if(RESOLVE_REFLECTION){
                w.println("Used " + (USE_CASTS_FOR_REFLECTION ? "casts" : "strings") + " for reflection resolution.");
            }else{
                w.println("Reflection wasn't resolved.");
            }
            w.println("There are " + newInstanceCalls.satCount(Iset) + " calls to Class.newInstance");
            
            int pos = 1;
            for(Iterator iter = newInstanceCalls.iterator(Iset); iter.hasNext(); pos++){
                BDD i = (BDD)iter.next();
                int i_i = i.scanVar(I).intValue();
                ProgramLocation mc = (ProgramLocation)Imap.get(i_i);
                
                BDD callees = IE.relprod(i, Iset);
                if(!callees.isZero()){
                    w.println("[" + pos + "]\t" + mc.toStringLong() + ": " + 
                        //(callees.satCount(Mset)==1 ? "UNRESOLVED":""));
                        (unresolvedCalls.contains(mc) ? "UNRESOLVED":""));
                    for(Iterator iter2 = callees.iterator(Mset); iter2.hasNext();){
                        BDD callee = (BDD)iter2.next();
                        
                        int m_i = callee.scanVar(M).intValue();
                        jq_Method m = (jq_Method)Mmap.get(m_i);
                        
                        w.println("\t" + m.toString());
                    }
                    w.println();
                }
            }
        }finally{
            if(w != null) w.close();
        }
    }
    
    void saveForNameStats() throws IOException {
        PrintWriter w = null;
        try {
            out.println("Saving forName statistics in " + FORNAME_STAT_FILE);
            w = new PrintWriter(new FileWriter(FORNAME_STAT_FILE));                
            jq_Method forNameMethod = class_class.getDeclaredMethod("forName");
            BDD forNameCalls = IE.restrict(M.ithVar(Mmap.get(forNameMethod)));   // I
            
            if(RESOLVE_REFLECTION){
                w.println("Used " + (USE_CASTS_FOR_REFLECTION ? "casts" : "strings") + " for reflection resolution.");
            }else{
                w.println("Reflection wasn't resolved.");
            }
            w.println("There are " + forNameCalls.satCount(Iset) + " calls to Class.forName");
            
            int pos = 1;
            for(Iterator iter = forNameCalls.iterator(Iset); iter.hasNext(); pos++){
                BDD i = (BDD)iter.next();
                int i_i = i.scanVar(I).intValue();
                ProgramLocation mc = (ProgramLocation)Imap.get(i_i);
                
                BDD callees = IE.relprod(i, Iset);
                if(!callees.isZero()){
                    w.println("[" + pos + "]\t" + mc.toStringLong() + ": " + 
                        (callees.satCount(Mset)==1 ? "UNRESOLVED":""));
                        //(unresolvedCalls.contains(mc) ? "UNRESOLVED":""));
                    for(Iterator iter2 = callees.iterator(Mset); iter2.hasNext();){
                        BDD callee = (BDD)iter2.next();
                        
                        int m_i = callee.scanVar(M).intValue();
                        jq_Method m = (jq_Method)Mmap.get(m_i);
                        
                        w.println("\t" + m.toString());
                    }
                    w.println();
                }
            }
        }finally{
            if(w != null) w.close();
        }
    }

    Set provideStubsFor = new HashSet();
    /**
     * Initializes provideStubsFor.
     */
    private void initializeStubs() {
        {
            jq_Type c = jq_Class.parseType("java.sql.Connection");
            c.prepare();        
            provideStubsFor.add(c);
        }
        
        {
            jq_Type c = jq_Class.parseType("java.sql.Statement");
            c.prepare();
            provideStubsFor.add(c);
        }
    }
    static Collection readClassesFromFile(String fname) throws IOException {
        BufferedReader r = null;
        try {
            r = new BufferedReader(new FileReader(fname));
            Collection rootMethods = new ArrayList();
            String s = null;
            while ((s = r.readLine()) != null) {
                jq_Class c = (jq_Class) jq_Type.parseType(s);
                c.prepare();
                rootMethods.addAll(Arrays.asList(c.getDeclaredStaticMethods()));
                if (ADD_INSTANCE_METHODS)
                    rootMethods.addAll(Arrays.asList(c.getDeclaredInstanceMethods()));
            }
            return rootMethods;
        } finally {
            if (r != null) r.close();
        }
    }

    public static void main(String[] args) throws IOException {
        if (USE_JOEQ_CLASSLIBS) {
            System.setProperty("joeq.classlibinterface", "joeq.ClassLib.pa.Interface");
            joeq.ClassLib.ClassLibInterface.useJoeqClasslib(true);
        }
        HostedVM.initialize();
        CodeCache.AlwaysMap = true;
        
        Collection rootMethods = null;
        if (args[0].startsWith("@")) {
            rootMethods = readClassesFromFile(args[0].substring(1));
        } else {
            jq_Class c = (jq_Class) jq_Type.parseType(args[0]);
            c.prepare();
        
            rootMethods = Arrays.asList(c.getDeclaredStaticMethods());
            if (ADD_INSTANCE_METHODS) {
                jq_InstanceMethod[] ms = c.getDeclaredInstanceMethods();
                rootMethods = new ArrayList(rootMethods);
                rootMethods.addAll(Arrays.asList(ms));
            }
        }

        if (args.length > 1) {
            for (Iterator i = rootMethods.iterator(); i.hasNext(); ) {
                jq_Method sm = (jq_Method) i.next();
                if (args[1].equals(sm.getName().toString())) {
                    rootMethods = Collections.singleton(sm);
                    break;
                }
            }
        }
        
        PA dis = new PA();
        dis.cg = null;

        if (dis.CONTEXT_SENSITIVE || !dis.DISCOVER_CALL_GRAPH) {
            if(dis.ALWAYS_START_WITH_A_FRESH_CALLGRAPH) {
                dis.cg = null; //loadCallGraph(rootMethods);
            } else {
                dis.cg = loadCallGraph(rootMethods);
            }
            if (dis.cg == null && dis.AUTODISCOVER_CALL_GRAPH) {
                if (dis.CONTEXT_SENSITIVE || dis.OBJECT_SENSITIVE || dis.THREAD_SENSITIVE || dis.SKIP_SOLVE) {
                    System.out.println("Discovering call graph first...");
                    dis.CONTEXT_SENSITIVE = false;
                    dis.OBJECT_SENSITIVE = false;
                    dis.THREAD_SENSITIVE = false;
                    dis.CARTESIAN_PRODUCT = false;
                    dis.DISCOVER_CALL_GRAPH = true;
                    dis.CS_CALLGRAPH = false;
                    dis.DUMP_INITIAL = false;
                    dis.DUMP_RESULTS = false;
                    dis.SKIP_SOLVE = false;
                    dis.DUMP_FLY = false;
               
                    dis.run("java", dis.cg, rootMethods);
                    System.out.println("Finished discovering call graph.");

                    //dis.traceNoDestanation();
                    dis = new PA();

                    initialCallgraphFileName = callgraphFileName;                    
                    dis.cg = loadCallGraph(rootMethods);
                    rootMethods = dis.cg.getRoots();                    
                    
                    //dis.cg = new PACallGraph(dis);                    
                } else if (!dis.DISCOVER_CALL_GRAPH) {
                    System.out.println("Call graph doesn't exist yet, so turning on call graph discovery.");
                    dis.DISCOVER_CALL_GRAPH = true;
                }
            } else if (dis.cg != null) {
                rootMethods = dis.cg.getRoots();
                dis.cg = new CachedCallGraph(dis.cg);
            }
        }
        //return;        
 
        dis.run(dis.cg, rootMethods);

        if (WRITE_PARESULTS_BATCHFILE)
            writePAResultsBatchFile("runparesults");
    }

    /**
     * write a file that when executed by shell runs PAResults in proper environment.
     */
    static void writePAResultsBatchFile(String batchfilename) throws IOException {
        PrintWriter w = null;
        try {
            w = new PrintWriter(new FileWriter(batchfilename));
            Properties p = System.getProperties();
            w.print(p.getProperty("java.home") + File.separatorChar + "bin" + File.separatorChar + "java");
            w.print(" -Xmx512M");
            w.print(" -classpath \"" + System.getProperty("java.class.path")+"\"");
            w.print(" -Djava.library.path=\"" + System.getProperty("java.library.path")+"\"");
            for (Iterator i = p.entrySet().iterator(); i.hasNext(); ) {
                Map.Entry e = (Map.Entry)i.next();
                String key = (String)e.getKey();
                String val = (String)e.getValue();
                if (key.startsWith("ms.") || key.startsWith("pa.")) {
                    w.print(" -D" + key + "=" + val);
                }
            }
            w.println(" joeq.Compiler.Analysis.IPA.PAResults");
        } finally {
            if (w != null) w.close();
        }
    }
    
    public void printSizes() {
        System.out.println("V = "+Vmap.size()+", bits = "+
                           BigInteger.valueOf(Vmap.size()).bitLength());
        System.out.println("I = "+Imap.size()+", bits = "+
                           BigInteger.valueOf(Imap.size()).bitLength());
        System.out.println("H = "+Hmap.size()+", bits = "+
                           BigInteger.valueOf(Hmap.size()).bitLength());
        System.out.println("F = "+Fmap.size()+", bits = "+
                           BigInteger.valueOf(Fmap.size()).bitLength());
        System.out.println("T = "+Tmap.size()+", bits = "+
                           BigInteger.valueOf(Tmap.size()).bitLength());
        System.out.println("N = "+Nmap.size()+", bits = "+
                           BigInteger.valueOf(Nmap.size()).bitLength());
        System.out.println("M = "+Mmap.size()+", bits = "+
                           BigInteger.valueOf(Mmap.size()).bitLength());
        System.out.println("C = "+Cmap.size()+", bits = "+
                           BigInteger.valueOf(Cmap.size()).bitLength());
    }
    
    ToString TS = new ToString();
    
    // XXX should we use an interface here for long location printing?
    public String longForm(Object o) {
        if (o == null || !LONG_LOCATIONS)
            return "";

        // Node is a ProgramLocation
        if (o instanceof ProgramLocation) {
            return " in "+((ProgramLocation)o).toStringLong();
        } else {
            try {
                Class c = o.getClass();
                try {
                    // Node has getLocation() 
                    Method m = c.getMethod("getLocation", new Class[] {});
                    ProgramLocation pl = (ProgramLocation)m.invoke(o, null);
                    if (pl == null)
                        throw new NoSuchMethodException();
                    return " in "+pl.toStringLong();
                } catch (NoSuchMethodException _1) {
                    try {
                        // Node has at least a getMethod() 
                        Method m = c.getMethod("getMethod", new Class[] {});
                        return " " + m.invoke(o, null);
                    } catch (NoSuchMethodException _2) {
                        try {
                            // or getDefiningMethod() 
                            Method m = c.getMethod("getDefiningMethod", new Class[] {});
                            return " " + m.invoke(o, null);
                        } catch (NoSuchMethodException _3) {
                        }
                    }
                }
            } catch (InvocationTargetException _) {
            } catch (IllegalAccessException _) { 
            }
        }
        return "";
    }

    String findInMap(IndexedMap map, int j) {
        String jp = "("+j+")";
        if (j < map.size() && j >= 0) {
            Object o = map.get(j);
            jp += o + longForm(o);
            return jp;
        } else {
            return jp+"<index not in map>";
        }
    }

    public class ToString extends BDD.BDDToString {
        public String elementName(int i, BigInteger j) {
            switch (i) {
                case 0: // fallthrough
                case 1: return findInMap(Vmap, j.intValue());
                case 2: return findInMap(Imap, j.intValue());
                case 3: return findInMap(Imap, j.intValue());// fallthrough
                case 4: return findInMap(Hmap, j.intValue());
                case 5: return j.toString();
                case 6: return findInMap(Fmap, j.intValue());
                case 7: // fallthrough
                case 8: return findInMap(Tmap, j.intValue());
                case 9: return findInMap(Nmap, j.intValue());
                case 10: return findInMap(Mmap, j.intValue());
                case 11: return findInMap(Mmap, j.intValue());
                default: return "("+j+")"+"??";
            }
        }
        public String elementNames(int i, BigInteger j, BigInteger k) {
            // TODO: don't bother printing out long form of big sets.
            return super.elementNames(i, j, k);
        }
    }
   
    private void dumpCallGraphAsDot(CallGraph cg, String dotFileName) throws IOException {
        PathNumbering pn = countCallGraph(cg, null, false);
        dumpCallGraphAsDot(pn, cg, dotFileName);
    }
    private void dumpCallGraphAsDot(PathNumbering pn, CallGraph cg, String dotFileName) throws IOException {
        if (pn != null) {
            BufferedWriter dos = null;
            try {
                dos = new BufferedWriter(new FileWriter(dotFileName));
                pn.dotGraph(dos, cg.getRoots(), cg.getCallSiteNavigator());
            } finally {
                if (dos != null) dos.close();
            }
        }
    }

    public void dumpCallGraph() throws IOException {
        //CallGraph callgraph = CallGraph.makeCallGraph(roots, new PACallTargetMap());
        CachedCallGraph callgraph = new CachedCallGraph(new PACallGraph(this));
        //CallGraph callgraph = callGraph;
        BufferedWriter dos = null;
        try {
            dos = new BufferedWriter(new FileWriter(callgraphFileName));
            LoadedCallGraph.write(callgraph, dos);
        } finally {
            if (dos != null) dos.close();
        }
        
    }
    
    public static List activeDomains(BDD r) {
        BDDFactory bdd = r.getFactory();
        BDD s = r.support();
        int[] a = s.scanSetDomains();
        s.free();
        if (a == null) return null;
        List result = new ArrayList(a.length);
        for (int i = 0; i < a.length; ++i) {
            result.add(bdd.getDomain(a[i]));
        }
        return result;
    }
    
    public static String getName(BDDDomain d) {
        String s = d.getName();
        if (s.equals("V1c")) return "VC0";
        if (s.equals("V2c")) return "VC1";
        if (s.endsWith("2")) return s.substring(0, s.length()-1)+"1";
        if (s.endsWith("1")) return s.substring(0, s.length()-1)+"0";
        return s+"0";
    }
    
    public static void bdd_save(String filename, BDD b) throws IOException {
        List ds = activeDomains(b);
        if (ds == null) {
            b.getFactory().save(filename, b);
        } else {
            bdd_save(filename, b, ds);
        }
    }
    
    public static void bdd_save(String filename, BDD b, List ds) throws IOException {
        //System.err.println("Saving " + filename + " of size " + b.satCount());
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter(filename));
            out.write('#');
            for (Iterator i = ds.iterator(); i.hasNext(); ) {
                BDDDomain d = (BDDDomain) i.next();
                out.write(' ');
                out.write(getName(d));
                out.write(':');
                out.write(Integer.toString(d.varNum()));
            }
            out.write('\n');
            for (Iterator i = ds.iterator(); i.hasNext(); ) {
                BDDDomain d = (BDDDomain) i.next();
                out.write('#');
                int[] vars = d.vars();
                for (int j = 0; j < vars.length; ++j) {
                    out.write(' ');
                    out.write(Integer.toString(vars[j]));
                }
                out.write('\n');
            }
            b.getFactory().save(out, b);
        } finally {
            if (out != null) try { out.close(); } catch (IOException _) { }
        }
    }
    
    public void dumpResults(String dumpfilename) throws IOException {
        System.out.println("A: "+(long) A.satCount(V1V2set)+" relations, "+A.nodeCount()+" nodes");
        bdd_save(dumpfilename+".A", A);
        System.out.println("vP: "+(long) vP.satCount(V1H1set)+" relations, "+vP.nodeCount()+" nodes");
        bdd_save(dumpfilename+".vP", vP);
        //BuildBDDIR.dumpTuples(bdd, dumpfilename+".vP.tuples", vP);
        System.out.println("S: "+(long) S.satCount(V1FV2set)+" relations, "+S.nodeCount()+" nodes");
        bdd_save(dumpfilename+".S", S);
        System.out.println("L: "+(long) L.satCount(V1FV2set)+" relations, "+L.nodeCount()+" nodes");
        bdd_save(dumpfilename+".L", L);
        System.out.println("vT: "+(long) vT.satCount(V1.set().and(T1set))+" relations, "+vT.nodeCount()+" nodes");
        bdd_save(dumpfilename+".vT", vT);
        System.out.println("hT: "+(long) hT.satCount(H1.set().and(T2set))+" relations, "+hT.nodeCount()+" nodes");
        bdd_save(dumpfilename+".hT", hT);
        System.out.println("aT: "+(long) aT.satCount(T1set.and(T2set))+" relations, "+aT.nodeCount()+" nodes");
        bdd_save(dumpfilename+".aT", aT);
        System.out.println("cha: "+(long) cha.satCount(T2Nset.and(Mset))+" relations, "+cha.nodeCount()+" nodes");
        bdd_save(dumpfilename+".cha", cha);
        System.out.println("actual: "+(long) actual.satCount(Iset.and(Zset).and(V2.set()))+" relations, "+actual.nodeCount()+" nodes");
        bdd_save(dumpfilename+".actual", actual);
        System.out.println("formal: "+(long) formal.satCount(MZset.and(V1.set()))+" relations, "+formal.nodeCount()+" nodes");
        bdd_save(dumpfilename+".formal", formal);
        System.out.println("Iret: "+(long) Iret.satCount(Iset.and(V1.set()))+" relations, "+Iret.nodeCount()+" nodes");
        bdd_save(dumpfilename+".Iret", Iret);
        System.out.println("Mret: "+(long) Mret.satCount(Mset.and(V2.set()))+" relations, "+Mret.nodeCount()+" nodes");
        bdd_save(dumpfilename+".Mret", Mret);
        System.out.println("Ithr: "+(long) Ithr.satCount(Iset.and(V1.set()))+" relations, "+Ithr.nodeCount()+" nodes");
        bdd_save(dumpfilename+".Ithr", Ithr);
        System.out.println("Mthr: "+(long) Mthr.satCount(Mset.and(V2.set()))+" relations, "+Mthr.nodeCount()+" nodes");
        bdd_save(dumpfilename+".Mthr", Mthr);
        System.out.println("mI: "+(long) mI.satCount(INset.and(Mset))+" relations, "+mI.nodeCount()+" nodes");
        bdd_save(dumpfilename+".mI", mI);
        System.out.println("mV: "+(long) mV.satCount(Mset.and(V1.set()))+" relations, "+mV.nodeCount()+" nodes");
        bdd_save(dumpfilename+".mV", mV);
        System.out.println("mC: "+(long) mC.satCount(Mset.and(C.set()))+" relations, "+mC.nodeCount()+" nodes");
        bdd_save(dumpfilename+".mC", mC);
        System.out.println("sync: "+(long) sync.satCount(V1.set())+" relations, "+sync.nodeCount()+" nodes");
        bdd_save(dumpfilename+".sync", sync);
        
        System.out.println("hP: "+(long) hP.satCount(H1FH2set)+" relations, "+hP.nodeCount()+" nodes");
        bdd_save(dumpfilename+".hP", hP);
        //BuildBDDIR.dumpTuples(bdd, dumpfilename+".hP.tuples", hP);
        System.out.println("IE: "+(long) IE.satCount(IMset)+" relations, "+IE.nodeCount()+" nodes");
        bdd_save(dumpfilename+".IE", IE);
        BuildBDDIR.dumpTuples(bdd, dumpfilename+".IE.tuples", IE);
        if (IEcs != null) {
            System.out.println("IEcs: "+(long) IEcs.satCount(IMset.and(V1cV2cset))+" relations, "+IEcs.nodeCount()+" nodes");
            bdd_save(dumpfilename+".IEcs", IEcs);
        }
        if (vPfilter != null) {
            System.out.println("vPfilter: "+(long) vPfilter.satCount(V1.set().and(H1.set()))+" relations, "+vPfilter.nodeCount()+" nodes");
            bdd_save(dumpfilename+".vPfilter", vPfilter);
        }
        if (hPfilter != null) {
            System.out.println("hPfilter: "+(long) hPfilter.satCount(H1.set().and(Fset).and(H1.set()))+" relations, "+hPfilter.nodeCount()+" nodes");
            bdd_save(dumpfilename+".hPfilter", hPfilter);
        }
        if (IEfilter != null) {
            System.out.println("IEfilter: "+IEfilter.nodeCount()+" nodes");
            bdd_save(dumpfilename+".IEfilter", IEfilter);
        }
        if (NNfilter != null) {
            System.out.println("NNfilter: "+NNfilter.nodeCount()+" nodes");
            bdd_save(dumpfilename+".NNfilter", NNfilter);
        }
        System.out.println("visited: "+(long) visited.satCount(Mset)+" relations, "+visited.nodeCount()+" nodes");
        bdd_save(dumpfilename+".visited", visited);
        
        BufferedWriter dos = null;
        try {
            dos = new BufferedWriter(new FileWriter(dumpfilename+".config"));
            dumpConfig(dos);
        } finally {
            if (dos != null) dos.close();
        }
        
        System.out.print("Dumping maps...");
        dos = null;
        try {
            dos = new BufferedWriter(new FileWriter(dumpfilename+".Vmap"));
            Vmap.dump(dos);
        } finally {
            if (dos != null) dos.close();
        }
        dos = null;
        try {
            dos = new BufferedWriter(new FileWriter(dumpfilename+".Imap"));
            Imap.dump(dos);
        } finally {
            if (dos != null) dos.close();
        }
        dos = null;
        try {
            dos = new BufferedWriter(new FileWriter(dumpfilename+".Hmap"));
            Hmap.dump(dos);
        } finally {
            if (dos != null) dos.close();
        }
        dos = null;
        try {
            dos = new BufferedWriter(new FileWriter(dumpfilename+".Fmap"));
            Fmap.dump(dos);
        } finally {
            if (dos != null) dos.close();
        }
        dos = null;
        try {
            dos = new BufferedWriter(new FileWriter(dumpfilename+".Tmap"));
            Tmap.dump(dos);
        } finally {
            if (dos != null) dos.close();
        }
        dos = null;
        try {
            dos = new BufferedWriter(new FileWriter(dumpfilename+".Nmap"));
            Nmap.dump(dos);
        } finally {
            if (dos != null) dos.close();
        }
        dos = null;
        try {
            dos = new BufferedWriter(new FileWriter(dumpfilename+".Mmap"));
            Mmap.dump(dos);
        } finally {
            if (dos != null) dos.close();
        }
        System.out.println("done.");
    }

    public static PA loadResults(String bddfactory, String loaddir, String loadfilename) throws IOException {
        PA pa = new PA();
        BufferedReader di = null;
        try {
            di = new BufferedReader(new FileReader(loaddir+loadfilename+".config"));
            pa.loadConfig(di);
        } finally {
            if (di != null) di.close();
        }
        System.out.print("Initializing...");
        pa.initializeBDD(bddfactory);
        System.out.println("done.");
        
        System.out.print("Loading results from "+loaddir+loadfilename+"...");
        if (loaddir.length() == 0) loaddir = "."+System.getProperty("file.separator");
        File dir = new File(loaddir);
        final String prefix = loadfilename + ".";
        File[] files = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith(prefix);
            }
        });
        for (int i = 0; i < files.length; ++i) {
            File f = files[i];
            if (f.isDirectory()) continue;
            String name = f.getName().substring(prefix.length());
            try {
                Field field = PA.class.getDeclaredField(name);
                if (field == null) continue;
                if (field.getType() == net.sf.javabdd.BDD.class) {
                    System.out.print(name+": ");
                    BDD b = pa.bdd.load(f.getAbsolutePath());
                    System.out.print(b.nodeCount()+" nodes, ");
                    field.set(pa, b);
                } else if (field.getType() == IndexMap.class) {
                    System.out.print(name+": ");
                    di = null;
                    try {
                        di = new BufferedReader(new FileReader(f));
                        IndexMap m = IndexMap.load(name, di);
                        System.out.print(m.size()+" entries, ");
                        field.set(pa, m);
                    } finally {
                        if (di != null) di.close();
                    }
                } else {
                    System.out.println();
                    System.out.println("Cannot load field: "+field);
                }
            } catch (NoSuchFieldException e) {
            } catch (IllegalArgumentException e) {
                Assert.UNREACHABLE();
            } catch (IllegalAccessException e) {
                Assert.UNREACHABLE();
            }
        }
        System.out.println("done.");
        
        // Set types for loaded BDDs.
        if (pa.A instanceof TypedBDD) {
            Set set = TypedBDDFactory.makeSet();
            set.add(pa.V1); set.add(pa.V2);
            set.addAll(Arrays.asList(pa.V1c)); set.addAll(Arrays.asList(pa.V2c));
            ((TypedBDD) pa.A).setDomains(set);
        }
        if (pa.vP instanceof TypedBDD) {
            Set set = TypedBDDFactory.makeSet();
            set.add(pa.V1); set.add(pa.H1);
            set.addAll(Arrays.asList(pa.V1c)); set.addAll(Arrays.asList(pa.H1c));
            ((TypedBDD) pa.vP).setDomains(set);
        }
        if (pa.S instanceof TypedBDD) {
            Set set = TypedBDDFactory.makeSet();
            set.add(pa.V1); set.add(pa.V2); set.add(pa.F);
            set.addAll(Arrays.asList(pa.V1c)); set.addAll(Arrays.asList(pa.V2c));
            ((TypedBDD) pa.S).setDomains(set);
        }
        if (pa.L instanceof TypedBDD) {
            Set set = TypedBDDFactory.makeSet();
            set.add(pa.V1); set.add(pa.V2); set.add(pa.F);
            set.addAll(Arrays.asList(pa.V1c)); set.addAll(Arrays.asList(pa.V2c));
            ((TypedBDD) pa.L).setDomains(set);
        }
        if (pa.vT instanceof TypedBDD)
            ((TypedBDD) pa.vT).setDomains(pa.V1, pa.T1);
        if (pa.hT instanceof TypedBDD)
            ((TypedBDD) pa.hT).setDomains(pa.H1, pa.T2);
        if (pa.aT instanceof TypedBDD)
            ((TypedBDD) pa.aT).setDomains(pa.T1, pa.T2);
        if (pa.cha instanceof TypedBDD)
            ((TypedBDD) pa.cha).setDomains(pa.T2, pa.N, pa.M);
        if (pa.actual instanceof TypedBDD)
            ((TypedBDD) pa.actual).setDomains(pa.I, pa.Z, pa.V2);
        if (pa.formal instanceof TypedBDD)
            ((TypedBDD) pa.formal).setDomains(pa.M, pa.Z, pa.V1);
        if (pa.Iret instanceof TypedBDD)
            ((TypedBDD) pa.Iret).setDomains(pa.I, pa.V1);
        if (pa.Mret instanceof TypedBDD)
            ((TypedBDD) pa.Mret).setDomains(pa.M, pa.V2);
        if (pa.Ithr instanceof TypedBDD)
            ((TypedBDD) pa.Ithr).setDomains(pa.I, pa.V1);
        if (pa.Mthr instanceof TypedBDD)
            ((TypedBDD) pa.Mthr).setDomains(pa.M, pa.V2);
        if (pa.mI instanceof TypedBDD)
            ((TypedBDD) pa.mI).setDomains(pa.M, pa.I, pa.N);
        if (pa.mV instanceof TypedBDD)
            ((TypedBDD) pa.mV).setDomains(pa.M, pa.V1);
        if (pa.mC instanceof TypedBDD)
            ((TypedBDD) pa.mC).setDomains(pa.M, pa.C);
        if (pa.sync instanceof TypedBDD)
            ((TypedBDD) pa.sync).setDomains(pa.V1);
        if (pa.mSync instanceof TypedBDD)
            ((TypedBDD) pa.sync).setDomains(pa.M);
        
        if (pa.fT instanceof TypedBDD)
            ((TypedBDD) pa.fT).setDomains(pa.F, pa.T2);
        if (pa.fC instanceof TypedBDD)
            ((TypedBDD) pa.fC).setDomains(pa.F, pa.T2);

        if (pa.hP instanceof TypedBDD) {
            Set set = TypedBDDFactory.makeSet();
            set.add(pa.H1); set.add(pa.H2); set.add(pa.F);
            set.addAll(Arrays.asList(pa.H1c)); set.addAll(Arrays.asList(pa.H2c));
            ((TypedBDD) pa.hP).setDomains(set);
        }
        if (pa.IE instanceof TypedBDD)
            ((TypedBDD) pa.IE).setDomains(pa.I, pa.M);
        if (pa.IEcs instanceof TypedBDD) {
            Set set = TypedBDDFactory.makeSet();
            set.add(pa.I); set.add(pa.M);
            set.addAll(Arrays.asList(pa.V2c)); set.addAll(Arrays.asList(pa.V1c));
            ((TypedBDD) pa.IEcs).setDomains(set);
        }
        if (pa.vPfilter instanceof TypedBDD)
            ((TypedBDD) pa.vPfilter).setDomains(pa.V1, pa.H1);
        if (pa.hPfilter instanceof TypedBDD)
            ((TypedBDD) pa.hPfilter).setDomains(pa.H1, pa.F, pa.H2);
        if (pa.IEfilter instanceof TypedBDD) {
            Set set = TypedBDDFactory.makeSet();
            set.add(pa.I); set.add(pa.M);
            set.addAll(Arrays.asList(pa.V2c)); set.addAll(Arrays.asList(pa.V1c));
            ((TypedBDD) pa.IEfilter).setDomains(set);
        }
        if (pa.NNfilter instanceof TypedBDD)
            ((TypedBDD) pa.NNfilter).setDomains(pa.H1);
        
        if (pa.visited instanceof TypedBDD)
            ((TypedBDD) pa.visited).setDomains(pa.M);
        
        return pa;
    }
    
    private void dumpConfig(BufferedWriter out) throws IOException {
        out.write("V="+V_BITS+"\n");
        out.write("I="+I_BITS+"\n");
        out.write("H="+H_BITS+"\n");
        out.write("Z="+Z_BITS+"\n");
        out.write("F="+F_BITS+"\n");
        out.write("T="+T_BITS+"\n");
        out.write("N="+N_BITS+"\n");
        out.write("M="+M_BITS+"\n");
        out.write("VC="+VC_BITS+"\n");
        out.write("HC="+HC_BITS+"\n");
        out.write("CS="+(CONTEXT_SENSITIVE?"yes":"no")+"\n");
        out.write("OS="+(OBJECT_SENSITIVE?"yes":"no")+"\n");
        out.write("TS="+(THREAD_SENSITIVE?"yes":"no")+"\n");
        out.write("CP="+(CARTESIAN_PRODUCT?"yes":"no")+"\n");
        out.write("Order="+varorder+"\n");
        out.write("Reverse="+reverseLocal+"\n");
    }
    
    private void loadConfig(BufferedReader in) throws IOException {
        for (;;) {
            String s = in.readLine();
            if (s == null) break;
            int index = s.indexOf('=');
            if (index == -1) index = s.length();
            String s1 = s.substring(0, index);
            String s2 = index < s.length() ? s.substring(index+1) : null;
            if (s1.equals("V")) {
                V_BITS = Integer.parseInt(s2);
            } else if (s1.equals("I")) {
                I_BITS = Integer.parseInt(s2);
            } else if (s1.equals("H")) {
                H_BITS = Integer.parseInt(s2);
            } else if (s1.equals("Z")) {
                Z_BITS = Integer.parseInt(s2);
            } else if (s1.equals("F")) {
                F_BITS = Integer.parseInt(s2);
            } else if (s1.equals("T")) {
                T_BITS = Integer.parseInt(s2);
            } else if (s1.equals("N")) {
                N_BITS = Integer.parseInt(s2);
            } else if (s1.equals("M")) {
                M_BITS = Integer.parseInt(s2);
            } else if (s1.equals("VC")) {
                VC_BITS = Integer.parseInt(s2);
            } else if (s1.equals("HC")) {
                HC_BITS = Integer.parseInt(s2);
            } else if (s1.equals("CS")) {
                CONTEXT_SENSITIVE = s2.equals("yes");
            } else if (s1.equals("OS")) {
                OBJECT_SENSITIVE = s2.equals("yes");
            } else if (s1.equals("TS")) {
                THREAD_SENSITIVE = s2.equals("yes");
            } else if (s1.equals("CP")) {
                CARTESIAN_PRODUCT = s2.equals("yes");
            } else if (s1.equals("Order")) {
                varorder = s2;
            } else if (s1.equals("Reverse")) {
                reverseLocal = s2.equals("true");
            } else {
                System.err.println("Unknown config option "+s);
            }
        }
        if (VC_BITS > 1 || HC_BITS > 1) {
            MAX_VC_BITS = VC_BITS;
            MAX_HC_BITS = HC_BITS;
        }
    }
    
    public static class ThreadRootMap extends AbstractMap {
        Map map;
        ThreadRootMap(Map s) {
            map = s;
        }
        public Object get(Object o) {
            Set s = (Set) map.get(o);
            if (s == null) return new Integer(0);
            return new Integer(s.size()-1);
        }
        /* (non-Javadoc)
         * @see java.util.AbstractMap#entrySet()
         */
        public Set entrySet() {
            HashMap m = new HashMap();
            for (Iterator i = map.keySet().iterator(); i.hasNext(); ) {
                Object o = i.next();
                m.put(o, get(o));
            }
            return m.entrySet();
        }
    }
    
    // Map between thread run() methods and the ConcreteTypeNodes of the corresponding threads.
    static Map thread_runs = new HashMap();
    IndexMap threadNumbers;
    
    public int getThreadRunIndex(jq_Method m, Node n) {
        if (!threadNumbers.contains(n)) {
            //Assert.UNREACHABLE("Method "+m+" Node "+n);
            return -1;
        }
        return threadNumbers.get(n);
    }
    
    public PathNumbering countCallGraph(CallGraph cg, ObjectCreationGraph ocg, boolean updateBits) {
        jq_Class jlt = PrimordialClassLoader.getJavaLangThread();
        jlt.prepare();
        jq_Class jlr = (jq_Class) PrimordialClassLoader.loader.getOrCreateBSType("Ljava/lang/Runnable;");
        jlr.prepare();
        Set fields = new HashSet();
        IndexMap classes = new IndexMap("classes");
        int vars = 0, heaps = 0, bcodes = 0, methods = 0, calls = 0;
        int threads = 0;
        for (Iterator i = cg.getAllMethods().iterator(); i.hasNext(); ) {
            jq_Method m = (jq_Method) i.next();
            if (TRACE_OBJECT) out.println("Counting "+m);
            ++methods;
            jq_Class c = m.isStatic() ? null : m.getDeclaringClass();
            if (m.getBytecode() == null) {
                jq_Type retType = m.getReturnType();
                if (retType instanceof jq_Reference) {
                    boolean b = !classes.contains(retType);
                    classes.get(retType);
                    if (b)
                        ++heaps;
                    if (ocg != null) {
                        ocg.addEdge(null, (Node) null, (jq_Reference) retType);
                    }
                }
                continue;
            }
            bcodes += m.getBytecode().length;
            MethodSummary ms = MethodSummary.getSummary(m);
            for (Iterator j = ms.nodeIterator(); j.hasNext(); ) {
                Node n = (Node) j.next();
                ++vars;
                if (n instanceof ConcreteTypeNode ||
                    n instanceof UnknownTypeNode ||
                    n instanceof ConcreteObjectNode) {
                    ++heaps;
                    jq_Reference type = n.getDeclaredType(); 
                    if (type != null && type != jq_NullType.NULL_TYPE) {
                        type.prepare();
                        if (type.isSubtypeOf(jlt) ||
                            type.isSubtypeOf(jlr)) {
                            jq_Method rm = type.getVirtualMethod(run_method);
                            Set s = (Set) thread_runs.get(rm);
                            if (s == null) thread_runs.put(rm, s = new HashSet());
                            s.add(n);
                            ++threads;
                        }
                        if (ocg != null) {
                            ocg.addEdge(c, n, type);
                        }
                    }
                }
                fields.addAll(n.getAccessPathEdgeFields());
                fields.addAll(n.getNonEscapingEdgeFields());
                if (n instanceof GlobalNode) continue;
                jq_Reference r = (jq_Reference) n.getDeclaredType();
                classes.get(r);
            }
            calls += ms.getCalls().size()*8;
        }
        if (ADD_SUPERTYPES) {
            for (int i = 0; i < classes.size(); ++i) {
                jq_Reference t1 = (jq_Reference) classes.get(i);
                if (t1 == null || t1 instanceof jq_NullType) continue;
                t1.prepare();
                jq_Reference t2 = t1.getDirectPrimarySupertype();
                if (t2 != null) {
                    t2.prepare();
                    classes.get(t2);
                }
                jq_Class[] c = t1.getInterfaces();
                for (int j = 0; j < c.length; ++j) {
                    classes.get(c[j]);
                }
            }
        }
        System.out.println();
        System.out.println("Methods="+methods+" Bytecodes="+bcodes+" Call sites="+calls);
        System.out.println("Vars="+vars+" Heaps="+heaps+" Classes="+classes.size()+" Fields="+fields.size());
        PathNumbering pn = null;
        if (CONTEXT_SENSITIVE) {
            if(BETTER_CONTEXT_NUMBERING){
                Set sccs = SCComponent.buildSCC(cg);
                SCCTopSortedGraph graph = SCCTopSortedGraph.topSort(sccs);
  
                Selector selector = null; 
                if(USE_STRING_METHOD_SELECTOR) {
                    System.err.println("Using a StringMethodSelector");
                    selector = new StringMethodSelector(graph);
                } else {
                    System.err.println("Using a VarPathSelector");
                    selector = varPathSelector; 
                }
                System.err.println("Using GlobalPathNumbering");
                pn = new GlobalPathNumbering(selector);
            } else {
                pn = new SCCPathNumbering(varPathSelector);
            }
        } else {
            pn = null;
        }
        Map initialCounts = null; //new ThreadRootMap(thread_runs);
        BigInteger paths = null;
        if (pn != null) {
            DumpDotGraph ddg = new DumpDotGraph();
            ddg.setNavigator(cg.getNavigator());
            ddg.setNodeSet(new HashSet(cg.getAllMethods()));
            try {
                ddg.dump("cg.txt");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            paths = (BigInteger) pn.countPaths(cg.getRoots(), cg.getCallSiteNavigator(), initialCounts);
        }
        if (updateBits) {
            V_BITS = BigInteger.valueOf(vars+256).bitLength();
            I_BITS = BigInteger.valueOf(calls).bitLength();
            H_BITS = BigInteger.valueOf(heaps+256).bitLength();
            F_BITS = BigInteger.valueOf(fields.size()+64).bitLength();
            T_BITS = BigInteger.valueOf(classes.size()+64).bitLength();
            N_BITS = I_BITS;
            M_BITS = BigInteger.valueOf(methods).bitLength() + 1;            
            if (CONTEXT_SENSITIVE) {
                System.out.println("Thread runs="+thread_runs);
                VC_BITS = paths.bitLength();
                if (VC_BITS > MAX_VC_BITS)
                    System.out.println("Trimming var context bits from "+VC_BITS);
                VC_BITS = Math.min(MAX_VC_BITS, VC_BITS);
                System.out.println("Paths="+paths+", Var context bits="+VC_BITS);
            } else if (THREAD_SENSITIVE) {
                ++threads; // for main()
                VC_BITS = HC_BITS = BigInteger.valueOf(threads).bitLength();
                System.out.println("Threads="+threads+", context bits="+VC_BITS);
            }
            System.out.println(" V="+V_BITS+" I="+I_BITS+" H="+H_BITS+
                               " F="+F_BITS+" T="+T_BITS+" N="+N_BITS+
                               " M="+M_BITS+" VC="+VC_BITS);
        }
        if (DUMP_DOTGRAPH) {
            try {
                dumpCallGraphAsDot(pn, cg, callgraphFileName + ".dot");
            } catch (IOException x) {
                x.printStackTrace();
            }
        }
        
        return pn;
    }

    public final VarPathSelector varPathSelector = new VarPathSelector(MAX_VC_BITS);
    
    public static boolean THREADS_ONLY = false;
    public static class VarPathSelector implements Selector {

        int maxBits;
        
        VarPathSelector(int max_bits) {
            this.maxBits = max_bits;
        }
        
        /* (non-Javadoc)
         * @see jwutil.graphs.PathNumbering.Selector#isImportant(java.lang.Object, java.lang.Object, java.math.BigInteger)
         */
        public boolean isImportant(Object a, Object b, BigInteger num) {
            if (num.bitLength() > maxBits) return false;
            if (THREADS_ONLY) {
                if (b instanceof ProgramLocation) return true;
                jq_Method m = (jq_Method) a;
                if (m.getNameAndDesc() == main_method) return true;
                if (m.getNameAndDesc() == run_method) return true;
                return false;
            }
            return true;
        }
        
        /* (non-Javadoc)
         * @see jwutil.graphs.PathNumbering.Selector#isImportant(jwutil.graphs.SCComponent, jwutil.graphs.SCComponent, java.math.BigInteger)
         */
        public boolean isImportant(SCComponent scc1, SCComponent scc2, BigInteger num) {
            if (num.bitLength() > maxBits) return false;
            if (THREADS_ONLY) {
                Set s = scc2.nodeSet();
                Iterator i = s.iterator();
                Object o = i.next();
                if (i.hasNext()) return false;
                jq_Method m = (jq_Method) scc1.nodes()[0];
                return isImportant(m, o, num);
            }
            return true;
        }
    }
    
    public final HeapPathSelector heapPathSelector = new HeapPathSelector();
    
    static Set polyClasses;
    static void initPolyClasses() {
        if (polyClasses != null) return;
        polyClasses = new HashSet();
        File f = new File("polyclasses");
        if (f.exists()) {
            BufferedReader in = null;
            try {
                in = new BufferedReader(new FileReader(f));
                for (;;) {
                    String s = in.readLine();
                    if (s == null) break;
                    polyClasses.add(jq_Type.parseType(s));
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (in != null) try { in.close(); } catch (IOException _) {}
            }
        }
    }
    
    public static boolean MATCH_FACTORY = false;
    
    public class HeapPathSelector implements Selector {

        jq_Class collection_class = (jq_Class) PrimordialClassLoader.loader.getOrCreateBSType("Ljava/util/Collection;");
        jq_Class map_class = (jq_Class) PrimordialClassLoader.loader.getOrCreateBSType("Ljava/util/Map;");
        jq_Class throwable_class = (jq_Class) PrimordialClassLoader.getJavaLangThrowable();
        HeapPathSelector() {
            initPolyClasses();
            collection_class.prepare();
            map_class.prepare();
            throwable_class.prepare();
        }
        
        /* (non-Javadoc)
         * @see jwutil.graphs.PathNumbering.Selector#isImportant(jwutil.graphs.SCComponent, jwutil.graphs.SCComponent, java.math.BigInteger)
         */
        public boolean isImportant(SCComponent scc1, SCComponent scc2, BigInteger num) {
            if (num.bitLength() > MAX_HC_BITS) return false;
            Set s = scc2.nodeSet();
            Iterator i = s.iterator();
            Object o = i.next();
            if (i.hasNext()) return false;
            return isImportant(scc1, o, num);
        }
        
        /* (non-Javadoc)
         * @see jwutil.graphs.PathNumbering.Selector#isImportant(java.lang.Object, java.lang.Object, java.math.BigInteger)
         */
        public boolean isImportant(Object p, Object o, BigInteger num) {
            if (num.bitLength() > MAX_HC_BITS) return false;
            if (o instanceof ProgramLocation) return true;
            jq_Method m = (jq_Method) o;
            if (m.getNameAndDesc() == main_method) return true;
            if (m.getNameAndDesc() == run_method) return true;
            if (m.getBytecode() == null) return false;
            if (MATCH_FACTORY) {
                if (!m.getReturnType().isReferenceType()) return false;
                MethodSummary ms = MethodSummary.getSummary(CodeCache.getCode(m));
                for (Iterator i = ms.getReturned().iterator(); i.hasNext(); ) {
                    Node n = (Node) i.next();
                    if (!(n instanceof ConcreteTypeNode)) {
                        //return false;
                    }
                    jq_Reference type = n.getDeclaredType();
                    if (type == null) {
                        return false;
                    }
                    type.prepare();
                    if (!polyClasses.isEmpty() && !polyClasses.contains(type))
                        return false;
                    if (type.isSubtypeOf(throwable_class))
                        return false;
                    //if (!type.isSubtypeOf(collection_class) &&
                    //    !type.isSubtypeOf(map_class))
                    //    return false;
                }
            }
            return true;
        }
    }

    public final ObjectPathSelector objectPathSelector = new ObjectPathSelector();
    
    public class ObjectPathSelector implements Selector {

        jq_Class throwable_class = (jq_Class) PrimordialClassLoader.getJavaLangThrowable();
        ObjectPathSelector() {
            throwable_class.prepare();
        }
        
        /* (non-Javadoc)
         * @see jwutil.graphs.PathNumbering.Selector#isImportant(java.lang.Object, java.lang.Object, java.math.BigInteger)
         */
        public boolean isImportant(Object p, Object o, BigInteger num) {
            if (o instanceof jq_Array) {
                if (!((jq_Array) o).getElementType().isReferenceType()) {
                    if (TRACE_OBJECT) out.println("No object sensitivity for "+o+": PRIMITIVE ARRAY");
                    return false;
                }
            } else if (o instanceof jq_Class) {
                jq_Class c = (jq_Class) o;
                if (c == PrimordialClassLoader.getJavaLangString()) {
                    if (TRACE_OBJECT) out.println("No object sensitivity for "+c+": STRING");
                    return false;
                }
                c.prepare();
                if (c.isSubtypeOf(throwable_class)) {
                    if (TRACE_OBJECT) out.println("No object sensitivity for "+c+": THROWABLE");
                    return false;
                }
                boolean hasReferenceMember = false;
                jq_InstanceField[] f = c.getInstanceFields();
                for (int j = 0; j < f.length; ++j) {
                    if (f[j].getType().isReferenceType()) {
                        hasReferenceMember = true;
                        break;
                    }
                }
                if (!hasReferenceMember) {
                    if (TRACE_OBJECT) out.println("No object sensitivity for "+c+": NO REF FIELDS");
                    return false;
                }
            }
            return true;
        }
        
        /* (non-Javadoc)
         * @see jwutil.graphs.PathNumbering.Selector#isImportant(jwutil.graphs.SCComponent, jwutil.graphs.SCComponent, java.math.BigInteger)
         */
        public boolean isImportant(SCComponent scc1, SCComponent scc2, BigInteger num) {
            Set s = scc2.nodeSet();
            Iterator i = s.iterator();
            Object o = i.next();
            if (i.hasNext()) {
                if (TRACE_OBJECT) out.println("No object sensitivity for "+s+": CYCLE");
                return false;
            }
            return isImportant(scc1, o, num);
        }
    }
    
    public class StringMethodSelector implements Selector {
        SCComponent select;
        
        StringMethodSelector(SCCTopSortedGraph sccGraph){
            this.select = getSelectSCC(sccGraph);
        }
        
        private SCComponent getSelectSCC(SCCTopSortedGraph sccGraph) {
            jq_Class string_buffer_class = (jq_Class) PrimordialClassLoader.loader.getOrCreateBSType("Ljava/lang/StringBuffer;");
            Assert._assert(string_buffer_class != null);
            string_buffer_class.prepare();
            jq_InstanceMethod string_buffer_append = null;
            jq_InstanceMethod[] methods = string_buffer_class.getDeclaredInstanceMethods();
            
            for(int i = 0; i < methods.length; i++) {
                jq_InstanceMethod m = methods[i];
                if(m.getNameAndDesc().toString().equals("append (Ljava/lang/String;)Ljava/lang/StringBuffer;")) {
                    string_buffer_append = m;
                    break;
                }else{
                     //System.err.println("Skipping " + m.getNameAndDesc().toString());
                }
            }
            Assert._assert(string_buffer_append != null, "No append method found in " + string_buffer_class);
                        
            // initialize the necessary SCC #
            SCComponent string_buffer_append_scc = null;
            for(SCComponent c = sccGraph.getFirst(); c.nextTopSort() != null; c = c.nextTopSort()) {
                //System.err.println("Component " + c.toString());
                if(c.contains(string_buffer_append)) {
                    string_buffer_append_scc = c;
                    break;
                }
            }
            Assert._assert(string_buffer_append_scc != null, "Can't find method " + string_buffer_append + " in any SCC");
            if(TRACE_SELECTORS) System.err.println("SCC # " + string_buffer_append_scc + " contains " + string_buffer_append);
            
            return string_buffer_append_scc;
        }

        StringMethodSelector(SCComponent select){
            this.select = select;
        }
        /**
         * Return true if the edge scc1->scc2 is important.
         */
        public boolean isImportant(SCComponent scc1, SCComponent scc2, BigInteger num) {
            Assert._assert(false);
            
            return false;
        }
        
        /**
         * Return true if the edge a->b is important.
         */
        public boolean isImportant(Object a, Object b, BigInteger num) {
            boolean result;
            //if (num.bitLength() > maxBits) return false;
            if (b instanceof ProgramLocation) {
                result = true;
            } else {
                jq_Method m = (jq_Method) b;               
                result = select.contains(m);
            }
            if(TRACE_SELECTORS && result) System.err.println("isImportant(" + a + ", " + b + ") = " + result);
            
            return result;         
        }
    }
    
    public PathNumbering countHeapNumbering(CallGraph cg, boolean updateBits) {
        if (VerifyAssertions)
            Assert._assert(CONTEXT_SENSITIVE);
        PathNumbering pn;
        if (THREAD_SENSITIVE) pn = new RootPathNumbering();
        else pn = new SCCPathNumbering(heapPathSelector);
        Map initialCounts = null;//new ThreadRootMap(thread_runs);
        BigInteger paths = (BigInteger) pn.countPaths(cg.getRoots(), cg.getCallSiteNavigator(), initialCounts);
        System.out.println("Number of paths for heap context sensitivity: "+paths);
        if (updateBits) {
            HC_BITS = paths.bitLength();
            if (HC_BITS > MAX_HC_BITS)
                System.out.println("Trimming heap context bits from "+HC_BITS);
            HC_BITS = Math.min(HC_BITS, MAX_HC_BITS);
            System.out.println("Heap context bits="+HC_BITS);
        }
        return pn;
    }
    
    void calculateIEfilter(CallGraph cg) {
        IEfilter = bdd.zero();
        IEcs = bdd.zero();
        if(TRACE) System.out.println("Roots: " + cg.getRoots());
        for (Iterator i = cg.getAllCallSites().iterator(); i.hasNext(); ) {
            ProgramLocation mc = (ProgramLocation) i.next();
            mc = LoadedCallGraph.mapCall(mc);

            int I_i = Imap.get(mc);
            for (Iterator j = cg.getTargetMethods(mc).iterator(); j.hasNext(); ) {
                jq_Method callee = /*unfake*/((jq_Method) j.next());
                int M_i = Mmap.get(callee);
                BDD context;
                if (CONTEXT_SENSITIVE) {
                    Pair p = new Pair(mc, callee);
                    Range r_edge = vCnumbering.getEdge(p);
                    jq_Method m = mc.getMethod();
                    Range r_caller = vCnumbering.getRange(m);
                    if(r_edge == null) {
                        SCCPathNumbering sccNumbering = (SCCPathNumbering) vCnumbering;
                        SCComponent scc = sccNumbering.getSCC(callee);
                        SCComponent sccCallee = sccNumbering.getSCC(mc.getMethod());
                        System.out.println("SCC: " + scc);
                        System.out.println("SCCCalee: " + sccCallee);
                        BDD t = IE.andWith(M.ithVar(Mmap.get(callee)).andWith(I.ithVar(Imap.get(mc))));
                        if(t.isZero()) {
                            System.err.println("Edge " + p + " is missing from IE");
                        }
                    }
                    Assert._assert(r_edge != null, "No edge for " + p + " when considering " + mc);
                    Assert._assert(r_caller != null, "No range for " + mc.getMethod() + " when considering " + mc);
                    context = buildContextMap(V2c[0],
                                              PathNumbering.toBigInt(r_caller.low),
                                              PathNumbering.toBigInt(r_caller.high),
                                              V1c[0],
                                              PathNumbering.toBigInt(r_edge.low),
                                              PathNumbering.toBigInt(r_edge.high));
                } else {
                    if (USE_VCONTEXT) context = V1cdomain.and(V2cdomain);
                    else context = bdd.one();
                }
                context.andWith(I.ithVar(I_i));
                context.andWith(M.ithVar(M_i));
                IEfilter.orWith(context);
            }
        }
    }
    
    BDDPairing V1ctoH1c;
    public BDD getV1H1Context(jq_Method m) {
        //m = unfake(m);
        if (THREAD_SENSITIVE) {
            BDD b = (BDD) V1H1correspondence.get(m);
            if (b == null) System.out.println("Unknown method "+m);
            if (V1ctoH1c == null) V1ctoH1c = bdd.makePair(V1c[0], H1c[0]);
            BDD c = b.replace(V1ctoH1c);
            return c.andWith(b.id());
        } else if (CONTEXT_SENSITIVE) {
            if (V1H1correspondence != null)
                return (BDD) V1H1correspondence.get(m);
            Range r1 = vCnumbering.getRange(m);
            BDD b = V1c[0].varRange(r1.low.longValue(), r1.high.longValue());
            if (USE_HCONTEXT)
                b.andWith(H1c[0].ithVar(0));
            return b;
        } else if (OBJECT_SENSITIVE) {
            jq_Class c = m.isStatic() ? null : m.getDeclaringClass(); 
            BDD result = (BDD) V1H1correspondence.get(c);
            if (result == null) {
                if (TRACE_OBJECT) out.println("Note: "+c+" is not in object creation graph.");
                //result = V1c.ithVar(0);
                //result.andWith(H1c.ithVar(0));
                return result;
            }
            return result.id();
        } else if (CARTESIAN_PRODUCT && false) {
            // todo! heap context sensitivity for cartesian product.
            BDD context;
            if (USE_HCONTEXT) context = V1cdomain.and(H1cdomain);
            else context = V1cdomain.id();
            return context;
        } else {
            return null;
        }
    }
    
    public void buildThreadV1H1(Map threadNodes, CallGraph g) {
        out.println("Building thread map.");
        threadNumbers = new IndexMap("Thread");
        V1H1correspondence = new HashMap();
        Navigator nav = g.getMethodNavigator();
        threadNumbers.get("main"); // "main" context.
        for (Iterator i = g.getRoots().iterator(); i.hasNext(); ) {
            Object o = i.next();
            if (threadNodes.containsKey(o)) continue;
            BDD b = V1c[0].ithVar(0);
            V1H1correspondence.put(o, b);
        }
        for (Iterator i = threadNodes.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry e = (Map.Entry) i.next();
            Object o = e.getKey();
            BDD b;
            V1H1correspondence.put(o, b = bdd.zero());
            Collection s = (Collection) e.getValue();
            for (Iterator j = s.iterator(); j.hasNext(); ) {
                Node n = (Node) j.next();
                int k = threadNumbers.get(n);
                if (TRACE) System.out.println("Thread "+k+": "+n);
                b.orWith(V1c[0].ithVar(k));
            }
        }
        boolean change;
        do {
            change = false;
            for (Iterator i = Traversals.reversePostOrder(nav, g.getRoots()).iterator(); i.hasNext(); ) {
                Object o = i.next();
                BDD b = (BDD) V1H1correspondence.get(o);
                if (b == null) {
                    V1H1correspondence.put(o, b = bdd.zero());
                    Assert._assert(!threadNodes.containsKey(o));
                    change = true;
                }
                for (Iterator j2 = nav.prev(o).iterator(); j2.hasNext(); ) {
                    Object o2 = j2.next();
                    BDD b2 = (BDD) V1H1correspondence.get(o2);
                    if (b2 != null) {
                        BDD old = b.id();
                        b.orWith(b2.id());
                        change |= !old.equals(b);
                        old.free();
                    }
                }
                if (TRACE) out.println("Map for "+o+": "+b.toStringWithDomains());
            }
        } while (change);
        // Global threads have context 0.
        V1H1correspondence.put(null, V1c[0].ithVar(0));
    }
    
    public void buildObjectSensitiveV1H1(ObjectCreationGraph g) {
        if (TRACE_OBJECT) out.println("Building object-sensitive V1H1");
        V1H1correspondence = new HashMap();
        rangeMap = new HashMap();
        rangeMap.put(null, new Range(BigInteger.ZERO, BigInteger.ZERO));
        Navigator nav = g.getNavigator();
        for (Iterator i = Traversals.reversePostOrder(nav, g.getRoots()).iterator();
             i.hasNext(); ) {
            Object o = i.next();
            if (o instanceof Node) {
                if (TRACE_OBJECT) out.println("Skipping "+o);
                continue;
            }
            jq_Reference c1 = (jq_Reference) o;
            Range r1 = oCnumbering.getRange(c1);
            if (c1 instanceof jq_Class) {
                jq_Class c = (jq_Class) c1;
                while (c != null) {
                    Range r = (Range) rangeMap.get(c);
                    if (r == null || r.high.longValue() < r1.high.longValue()) {
                        rangeMap.put(c, r1);
                    }
                    c = c.getSuperclass();
                }
            }
            if (TRACE_OBJECT) out.println(c1+" Range "+r1);
            
            BDD b = bdd.zero();
            for (Iterator j = nav.next(c1).iterator(); j.hasNext(); ) {
                Object p = j.next();
                Node node;
                jq_Reference c2;
                Range r2;
                if (TRACE_OBJECT) out.println("Edge "+c1+" -> "+p);
                if (p instanceof jq_Reference) {
                    // unknown creation site.
                    node = null;
                    c2 = (jq_Reference) p;
                    r2 = oCnumbering.getEdge(c1, c2);
                } else {
                    node = (Node) p;
                    Collection next = nav.next(node);
                    if (VerifyAssertions)
                        Assert._assert(next.size() == 1);
                    if (VerifyAssertions)
                        Assert._assert(r1.equals(oCnumbering.getEdge(c1, node)));
                    c2 = (jq_Reference) next.iterator().next();
                    r2 = oCnumbering.getEdge(node, c2);
                }
                
                int T_i = Tmap.get(c2);
                // class c1 creates a c2 object
                BDD T_bdd = T2.ithVar(T_i);
                BDD heap;
                if (node == null) {
                    // we don't know which creation site, so just use all sites that
                    // have the same type.
                    heap = hT.restrict(T_bdd);
                    if (VerifyAssertions)
                        Assert._assert(!heap.isZero(), c2.toString());
                } else {
                    int H_i = Hmap.get(node);
                    heap = H1.ithVar(H_i);
                }
                T_bdd.free();
                if (TRACE_OBJECT) out.println(c1+" creation site "+node+" "+c2+" Range: "+r2);
                BDD cm;
                cm = buildContextMap(V1c[0],
                                     PathNumbering.toBigInt(r1.low),
                                     PathNumbering.toBigInt(r1.high),
                                     H1c[0],
                                     PathNumbering.toBigInt(r2.low),
                                     PathNumbering.toBigInt(r2.high));
                cm.andWith(heap);
                b.orWith(cm);
            }
            if (TRACE_OBJECT) out.println("Registering V1H1 for "+c1);
            V1H1correspondence.put(c1, b);
        }
    }
    
    public void buildObjectSensitiveV1H1_(ObjectCreationGraph g) {
        V1H1correspondence = new HashMap();
        rangeMap = new HashMap();
        rangeMap.put(null, new Range(BigInteger.ZERO, BigInteger.ZERO));
        Navigator nav = g.getNavigator();
        for (Iterator i = Traversals.reversePostOrder(nav, g.getRoots()).iterator();
             i.hasNext(); ) {
            jq_Reference c1 = (jq_Reference) i.next();
            Range r1 = oCnumbering.getRange(c1);
            if (c1 instanceof jq_Class) {
                jq_Class c = (jq_Class) c1;
                while (c != null) {
                    Range r = (Range) rangeMap.get(c);
                    if (r == null || r.high.longValue() < r1.high.longValue()) {
                        rangeMap.put(c, r1);
                    }
                    c = c.getSuperclass();
                }
            }
            
            BDD b = bdd.zero();
            for (Iterator j = nav.next(c1).iterator(); j.hasNext(); ) {
                jq_Reference c2 = (jq_Reference) j.next();
                int T_i = Tmap.get(c2);
                // class c1 creates a c2 object
                BDD T_bdd = T2.ithVar(T_i);
                BDD heap = hT.restrict(T_bdd);
                T_bdd.free();
                Range r2 = oCnumbering.getEdge(c1, c2);
                BDD cm;
                cm = buildContextMap(V1c[0],
                                     PathNumbering.toBigInt(r1.low),
                                     PathNumbering.toBigInt(r1.high),
                                     H1c[0],
                                     PathNumbering.toBigInt(r2.low),
                                     PathNumbering.toBigInt(r2.high));
                cm.andWith(heap);
                b.orWith(cm);
            }
            V1H1correspondence.put(c1, b);
        }
    }
    
    Map V1H1correspondence;
    
    public void buildVarHeapCorrespondence(CallGraph cg) {
        if (VerifyAssertions)
            Assert._assert(CONTEXT_SENSITIVE);
        BDDPairing V2cH2ctoV1cH1c = bdd.makePair();
        V2cH2ctoV1cH1c.set(V2c, V1c);
        V2cH2ctoV1cH1c.set(H2c, H1c);
        
        V1H1correspondence = new HashMap();
        for (Iterator i = cg.getAllMethods().iterator(); i.hasNext(); ) {
            jq_Method m = (jq_Method) i.next();
            Range r1 = vCnumbering.getRange(m);
            Range r2 = hCnumbering.getRange(m);
            BDD relation;
            if (r1.equals(r2)) {
                relation = V1c[0].buildAdd(H1c[0], BigInteger.valueOf(r1.high.longValue()).bitLength(), 0);
                relation.andWith(V1c[0].varRange(r1.low.longValue(), r1.high.longValue()));
            } else {
                long v_val = r1.high.longValue()+1;
                long h_val = r2.high.longValue()+1;
                
                if (h_val == 1L) {
                    relation = V1c[0].varRange(r1.low.longValue(), r1.high.longValue());
                    relation.andWith(H1c[0].ithVar(0));
                } else {
                    int v_bits = BigInteger.valueOf(v_val).bitLength();
                    int h_bits = BigInteger.valueOf(h_val).bitLength();
                    // make it faster.
                    h_val = 1 << h_bits;
                    
                    int[] v = new int[v_bits];
                    for (int j = 0; j < v_bits; ++j) {
                        v[j] = V1c[0].vars()[j];
                    }
                    BDDBitVector v_vec = bdd.buildVector(v);
                    BDDBitVector z = v_vec.divmod(h_val, false);
                    
                    //int h_bits = BigInteger.valueOf(h_val).bitLength();
                    //int[] h = new int[h_bits];
                    //for (int j = 0; j < h_bits; ++j) {
                    //    h[j] = H1c.vars()[j];
                    //}
                    //BDDBitVector h_vec = bdd.buildVector(h);
                    BDDBitVector h_vec = bdd.buildVector(H1c[0]);
                    
                    relation = bdd.one();
                    int n;
                    for (n = 0; n < h_vec.size() || n < v_vec.size(); n++) {
                        BDD a = (n < v_vec.size()) ? z.getBit(n) : bdd.zero();
                        BDD b = (n < h_vec.size()) ? h_vec.getBit(n) : bdd.zero();
                        relation.andWith(a.biimp(b));
                    }
                    for ( ; n < V1c[0].varNum() || n < H1c[0].varNum(); n++) {
                        if (n < V1c[0].varNum())
                            relation.andWith(bdd.nithVar(V1c[0].vars()[n]));
                        if (n < H1c[0].varNum())
                            relation.andWith(bdd.nithVar(H1c[0].vars()[n]));
                    }
                    relation.andWith(V1c[0].varRange(r1.low.longValue(), r1.high.longValue()));
                    //System.out.println(v_val+" / "+h_val+" = "+relation.and(V1c.varRange(0, 100)).toStringWithDomains());
                    v_vec.free(); h_vec.free(); z.free();
                }
            }
            V1H1correspondence.put(m, relation);
        }
    }
    
    public void buildExactVarHeapCorrespondence(CallGraph cg) {
        if (VerifyAssertions)
            Assert._assert(CONTEXT_SENSITIVE);
        BDDPairing V2cH2ctoV1cH1c = bdd.makePair();
        V2cH2ctoV1cH1c.set(V2c, V1c);
        V2cH2ctoV1cH1c.set(H2c, H1c);
        BDDPairing V2ctoV1c = bdd.makePair();
        V2ctoV1c.set(V2c, V1c);
        BDDPairing H2ctoH1c = bdd.makePair();
        H2ctoH1c.set(H2c, H1c);
        
        V1H1correspondence = new HashMap();
        for (Iterator i = cg.getRoots().iterator(); i.hasNext(); ) {
            jq_Method root = (jq_Method) i.next();
            Range r1 = vCnumbering.getRange(root);
            Range r2 = hCnumbering.getRange(root);
            BDD relation;
            if (r1.equals(r2)) {
                relation = V1c[0].buildAdd(H1c[0], BigInteger.valueOf(r1.high.longValue()).bitLength(), 0);
                relation.andWith(V1c[0].varRange(r1.low.longValue(), r1.high.longValue()));
                System.out.println("Root "+root+" numbering: "+relation.toStringWithDomains());
            } else {
                System.out.println("Root numbering doesn't match: "+root);
                // just intermix them all, because we don't know the mapping.
                relation = V1c[0].varRange(r1.low.longValue(), r1.high.longValue());
                relation.andWith(H1c[0].varRange(r2.low.longValue(), r2.high.longValue()));
            }
            V1H1correspondence.put(root, relation);
        }
        List rpo = Traversals.reversePostOrder(cg.getMethodNavigator(), cg.getRoots());
        for (Iterator i = rpo.iterator(); i.hasNext(); ) {
            jq_Method callee = (jq_Method) i.next();
            //Assert._assert(!V1H1correspondence.containsKey(callee));
            BDD calleeRelation;
            calleeRelation = (BDD) V1H1correspondence.get(callee);
            if (calleeRelation == null)
                calleeRelation = bdd.zero();
            for (Iterator j = cg.getCallers(callee).iterator(); j.hasNext(); ) {
                ProgramLocation cs = (ProgramLocation) j.next();
                jq_Method caller = cs.getMethod();
                BDD callerRelation = (BDD) V1H1correspondence.get(caller);
                if (callerRelation == null) continue;
                Range r1_caller = vCnumbering.getRange(caller);
                Range r1_edge = vCnumbering.getEdge(cs, callee);
                Range r2_caller = hCnumbering.getRange(caller);
                Range r2_edge = hCnumbering.getEdge(cs, callee);
                BDD cm1;
                BDD tmpRel;
                boolean r1_same = r1_caller.equals(r1_edge);
                boolean r2_same = r2_caller.equals(r2_edge);
                if (!r1_same) {
                    cm1 = buildContextMap(V1c[0],
                                          PathNumbering.toBigInt(r1_caller.low),
                                          PathNumbering.toBigInt(r1_caller.high),
                                          V2c[0],
                                          PathNumbering.toBigInt(r1_edge.low),
                                          PathNumbering.toBigInt(r1_edge.high));
                    tmpRel = callerRelation.relprod(cm1, V1cset);
                    cm1.free();
                } else {
                    tmpRel = callerRelation.id();
                }
                BDD tmpRel2;
                if (!r2_same) {
                    cm1 = buildContextMap(H1c[0],
                                          PathNumbering.toBigInt(r2_caller.low),
                                          PathNumbering.toBigInt(r2_caller.high),
                                          H2c[0],
                                          PathNumbering.toBigInt(r2_edge.low),
                                          PathNumbering.toBigInt(r2_edge.high));
                    tmpRel2 = tmpRel.relprod(cm1, H1cset);
                    tmpRel.free();
                    cm1.free();
                } else {
                    tmpRel2 = tmpRel;
                }
                if (!r1_same) {
                    if (!r2_same) {
                        tmpRel2.replaceWith(V2cH2ctoV1cH1c);
                    } else {
                        tmpRel2.replaceWith(V2ctoV1c);
                    }
                } else if (!r2_same) {
                    tmpRel2.replaceWith(H2ctoH1c);
                }
                calleeRelation.orWith(tmpRel2);
            }
            V1H1correspondence.put(callee, calleeRelation);
        }
    }
    
    public PAResults getResults() {
        PAResults r = new PAResults(this);
        r.cg = this.cg;
        return r;
    }
    
    BDD mS;
    BDD mL;
    BDD mvP;
    BDD mIE;
    BDD visitedFly;


    void initFly() {
        mS = bdd.zero();
        mL = bdd.zero();
        mvP = bdd.zero();
        mIE = bdd.zero();
        visitedFly = bdd.zero();
        for (Iterator i = rootMethods.iterator(); i.hasNext(); ) {
            jq_Method m = (jq_Method) i.next();
            int m_i = Mmap.get(m);
            visitedFly.orWith(M.ithVar(m_i));
        }
        
        for (Iterator i = newMethodSummaries.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry e = (Map.Entry) i.next();
            jq_Method m = (jq_Method) e.getKey();
            PAMethodSummary s = (PAMethodSummary) e.getValue();
            int m_i = Mmap.get(m);
            BDD b = M.ithVar(m_i);
            mS.orWith(b.and(s.S));
            mL.orWith(b.and(s.L));
            mvP.orWith(b.and(s.vP));
            mIE.orWith(b.and(s.IE));
            s.free(); b.free();
        }
    }
    
    BDD getRoots() {
        BDD b = bdd.zero();
        if (cg == null) {
            return b;
        }
        for (Iterator i = cg.getRoots().iterator(); i.hasNext(); ) {
            jq_Method m = (jq_Method) i.next();
            b.orWith(M.ithVar(Mmap.get(m)));
        }
        return b;
    }
    
    Collection removedCalls = new ArrayList();

    void addToNmap(jq_Method m) {
        Assert._assert(!m.isStatic());
        Assert._assert(!m.isPrivate());
        Nmap.get(m);
        jq_Class c = m.getDeclaringClass().getSuperclass();
        if (c != null) {
            jq_Method m2 = c.getVirtualMethod(m.getNameAndDesc());
            if (m2 != null) addToNmap(m2);
        }
        jq_Class[] cs = m.getDeclaringClass().getDeclaredInterfaces();
        for (int i = 0; i < cs.length; ++i) {
            jq_Class interf = cs[i];
            jq_Method m3 = interf.getVirtualMethod(m.getNameAndDesc());
            if (m3 != null) addToNmap(m3);
        }
    }
    
    public void dumpBDDRelations() throws IOException {
        if (FULL_CHA) {
            for (Iterator i = Mmap.iterator(); i.hasNext(); ) {
                Object o = i.next();
                if (o instanceof jq_Method) {
                    jq_Method m = (jq_Method) o;
                    if (m.isStatic() || m.isPrivate()) continue;
                    addToNmap(m);
                }
            }
            buildTypes();
        }
        
        // difference in compatibility
        BDD S0 = S.exist(V1cV2cset);
        BDD L0 = L.exist(V1cV2cset);        
        
        String dumpPath = System.getProperty("pa.dumppath", "");
        if (dumpPath.length() > 0) {
            File f = new File(dumpPath);
            if (!f.exists()) f.mkdirs();
            String sep = System.getProperty("file.separator", "/");
            if (!dumpPath.endsWith(sep))
                dumpPath += sep;
        }
        System.out.println("Dumping to path "+dumpPath);
        
        BufferedWriter dos = null;
        try {
            dos = new BufferedWriter(new FileWriter(dumpPath+"bddinfo"));
            for (int i = 0; i < bdd.numberOfDomains(); ++i) {
                BDDDomain d = bdd.getDomain(i);
                if (d == V1 || d == V2)
                    dos.write("V\n");
                else if (d == H1 || d == H2)
                    dos.write("H\n");
                else if (d == T1 || d == T2)
                    dos.write("T\n");
                else if (d == F)
                    dos.write("F\n");
                else if (d == I || d == I2)
                    dos.write("I\n");
                else if (d == Z)
                    dos.write("Z\n");
                else if (d == N)
                    dos.write("N\n");
                else if (d == C)
                    dos.write("C\n");
                else if (d == M || d == M2)
                    dos.write("M\n");
                else if (d == STR)
                    dos.write("STR\n");
                else if (Arrays.asList(V1c).contains(d)
                        || Arrays.asList(V2c).contains(d))
                    dos.write("VC\n");
                else if (Arrays.asList(H1c).contains(d)
                        || Arrays.asList(H2c).contains(d))
                    dos.write("HC\n");
                else if (DUMP_SSA) {
                    dos.write(bddIRBuilder.getDomainName(d)+"\n");
                } else
                    dos.write(d.toString() + "\n");
            }
        } finally {
            if (dos != null) dos.close();
        }
        
        dos = null;
        try {
            dos = new BufferedWriter(new FileWriter(dumpPath+"fielddomains.pa"));
            dos.write("V "+(1L<<V_BITS)+" var.map\n");
            dos.write("H "+(1L<<H_BITS)+" heap.map\n");
            dos.write("T "+(1L<<T_BITS)+" type.map\n");
            dos.write("F "+(1L<<F_BITS)+" field.map\n");
            dos.write("I "+(1L<<I_BITS)+" invoke.map\n");
            dos.write("Z "+(1L<<Z_BITS)+"\n");
            dos.write("N "+(1L<<N_BITS)+" name.map\n");
            dos.write("M "+(1L<<M_BITS)+" method.map\n");
            dos.write("C "+(1L<<C_BITS)+" class.map\n");
            dos.write("VC "+(1L<<VC_BITS)+"\n");
            dos.write("HC "+(1L<<HC_BITS)+"\n");
            if (SPECIAL_MAP_INFO) {
                dos.write("STR "+(1L<<STR_BITS)+" string.map\n");
            }
            if (bddIRBuilder != null) bddIRBuilder.dumpFieldDomains(dos);
        } finally {
            if (dos != null) dos.close();
        }
        
        BDD mC = bdd.zero();
        for (Iterator i = visited.iterator(Mset); i.hasNext(); ) {
            BDD m = (BDD) i.next();
            int m_i = m.scanVar(M).intValue();
            jq_Method method = (jq_Method) Mmap.get(m_i);
            BDD c = getV1V2Context(method);
            if (c != null) {
                BDD d = c.exist(V2cset); c.free();
                m.andWith(d);
            }
            mC.orWith(m);
        }
        bdd_save(dumpPath+"IE0.bdd", IE.exist(V1cV2cset));        
        bdd_save(dumpPath+"vP0.bdd", vP.exist(V1cH1cset));
        bdd_save(dumpPath+"hP0.bdd", hP);
        bdd_save(dumpPath+"L.bdd", L0);
        bdd_save(dumpPath+"S.bdd", S0);
        if (CONTEXT_SENSITIVE) {
            bdd_save(dumpPath+"cA.bdd", A, Arrays.asList(new BDDDomain[] { V1, V2, V1c[0], V2c[0] }));
        } else {
            bdd_save(dumpPath+"A.bdd", A);
        }
        bdd_save(dumpPath+"vT.bdd", vT);
        bdd_save(dumpPath+"hT.bdd", hT);
        bdd_save(dumpPath+"aT.bdd", aT);
        bdd_save(dumpPath+"cha.bdd", cha);
        bdd_save(dumpPath+"actual.bdd", actual);
        bdd_save(dumpPath+"formal.bdd", formal);
        bdd_save(dumpPath+"mV.bdd", mV);
        bdd_save(dumpPath+"mC.bdd", this.mC);
        bdd_save(dumpPath+"mI.bdd", mI);
        bdd_save(dumpPath+"Mret.bdd", Mret);
        bdd_save(dumpPath+"Mthr.bdd", Mthr);
        bdd_save(dumpPath+"Iret.bdd", Iret);
        bdd_save(dumpPath+"Ithr.bdd", Ithr);
        bdd_save(dumpPath+"sync.bdd", sync);
        bdd_save(dumpPath+"mSync.bdd", mSync);
        if (SPECIAL_MAP_INFO) {
            bdd_save(dumpPath+"stringConstant.bdd", stringConstant);
            bdd_save(dumpPath+"overridesEqualsOrHashcode.bdd", overridesEqualsOrHashcode);
        }

        if (threadRuns != null)
            bdd_save(dumpPath+"threadRuns.bdd", threadRuns);
        if (IEfilter != null) {
            bdd_save(dumpPath+"IEfilter.bdd", IEfilter);
        }
        bdd_save(dumpPath+"roots.bdd", getRoots());
        
        if (V1c.length > 0 && H1c.length > 0) {
            bdd_save(dumpPath+"eq.bdd", V1c[0].buildEquals(H1c[0]));
        }
        
        if (DUMP_FLY) {
            initFly();
            bdd_save(dumpPath+"visited.bdd", visitedFly);
            bdd_save(dumpPath+"mS.bdd", mS);
            bdd_save(dumpPath+"mL.bdd", mL);
            bdd_save(dumpPath+"mvP.bdd", mvP);
            bdd_save(dumpPath+"mIE.bdd", mIE);
        }
        
        dos = null;
        try {
            dos = new BufferedWriter(new FileWriter(dumpPath+"var.map"));
            for (int j = 0; j < Vmap.size(); ++j) {
                Node o = (Node)Vmap.get(j);
                dos.write(o.id+": "+o+"\n");
            }
        } finally {
            if (dos != null) dos.close();
        }
        
        dos = null;
        try {
            dos = new BufferedWriter(new FileWriter(dumpPath+"heap.map"));
            //Hmap.dumpStrings(dos);
            for (int j = 0; j < Hmap.size(); ++j) {
                Node o = (Node) Hmap.get(j);

                dos.write(o.id+": "+ o+"\n");
            }
        } finally {
            if (dos != null) dos.close();
        }
        
        dos = null;
        try {
            dos = new BufferedWriter(new FileWriter(dumpPath+"type.map"));
            for (int j = 0; j < Tmap.size(); ++j) {
                jq_Type o = (jq_Type) Tmap.get(j);
                dos.write(NameMunger.mungeTypeName2(o)+"\n");
            }
        } finally {
            if (dos != null) dos.close();
        }
        
        dos = null;
        try {
            dos = new BufferedWriter(new FileWriter(dumpPath+"field.map"));
            for (int j = 0; j < Fmap.size(); ++j) {
                jq_Field o = (jq_Field) Fmap.get(j);
                dos.write(NameMunger.mungeFieldName(o)+"\n");
            }
        } finally {
            if (dos != null) dos.close();
        }
        
        dos = null;
        try {
            dos = new BufferedWriter(new FileWriter(dumpPath+"invoke.map"));
            //Imap.dumpStrings(dos);
            for (int j = 0; j < Imap.size(); ++j) {
                ProgramLocation o = (ProgramLocation)Imap.get(j);
                //if(!removedCalls.contains(o)){
                    dos.write(o.hashCode()+": "+o+"\n");
//                }else{
//                    System.out.println("Skipping " + o);
//                    ProgramLocation o2 = LoadedCallGraph.mapCall(o);
//                    dos.write(o2.hashCode()+": "+o2+"\n");
//                }
           }
        } finally {
            if (dos != null) dos.close();
        }
        
        dos = null;
        try {
            dos = new BufferedWriter(new FileWriter(dumpPath+"name.map"));
            for (int j = 0; j < Nmap.size(); ++j) {
                jq_Method o = (jq_Method) Nmap.get(j);
                dos.write(NameMunger.mungeMethodName(o)+"\n");
            }
        } finally {
            if (dos != null) dos.close();
        }
        
        dos = null;
        try {
            dos = new BufferedWriter(new FileWriter(dumpPath+"method.map"));
            for (int j = 0; j < Mmap.size(); ++j) {
                Object o = Mmap.get(j);
                if (o instanceof Dummy) {
                    dos.write(o.toString()+"\n");
                    continue;
                }
                jq_Method m = (jq_Method) o;
                dos.write(NameMunger.mungeMethodName(m)+"\n");
            }
        } finally {
            if (dos != null) dos.close();
        }
        
        if(DUMP_UNMUNGED_NAMES) {
            dos = null;
            try {
                dos = new BufferedWriter(new FileWriter(dumpPath+"unmunged_method.map"));
                for (int j = 0; j < Mmap.size(); ++j) {
                    Object o = Mmap.get(j);
                    if (o instanceof Dummy) {
                        dos.write(o.toString()+"\n");
                        continue;
                    }
                    jq_Method m = (jq_Method) o;
                    dos.write(NameMunger.getJavadocSignature(m) + "\n");
                }
            } finally {
                if (dos != null) dos.close();
            }
            dos = null;
            try {
                dos = new BufferedWriter(new FileWriter(dumpPath+"unmunged_name.map"));
                for (int j = 0; j < Nmap.size(); ++j) {
                    Object o = Nmap.get(j);
                    if (o instanceof Dummy) {
                        dos.write(o.toString()+"\n");
                        continue;
                    }
                    jq_Method m = (jq_Method) o;
                    dos.write(NameMunger.getJavadocSignature(m) + "\n");
                }
            } finally {
                if (dos != null) dos.close();
            }
        }

        if(SPECIAL_MAP_INFO) {
            dos = null;
            try {
                dos = new BufferedWriter(new FileWriter(dumpPath+"string.map"));
                for (int j = 0; j < STRmap.size(); ++j) {
                    String s = STRmap.get(j).toString();

                    // suppress nonprintables in the output
                    StringBuffer sb = new StringBuffer(s);
                    for (int i=0; i<sb.length(); ++i) {
                        if (sb.charAt(i) < 32) {
                            sb.setCharAt(i, ' ');
                        }
                        else if (sb.charAt(i) > 127) {
                            sb.setCharAt(i, ' ');
                        }
                    }

                    dos.write(sb.toString());
                    dos.write("\n");
                }
            } finally {
                if (dos != null) dos.close();
            }
        }
    }
    
    class Dummy implements Textualizable {
        public void addEdge(String edge, Textualizable t) {
        }
        public void write(Textualizer t) throws IOException {
            t.writeString("(dummy object)");
        }
        public void writeEdges(Textualizer t) throws IOException {
        }
        public String toString() {
            return "DummyMethod";
        }
    }
    
    private void makeVRegbdd(CallGraph callgraph) {
        vReg = bdd.zero();
        BDDDomain reg = bddIRBuilder.getDestDomain();
        Collection s = new TreeSet(new Comparator() {
            public int compare(Object o1, Object o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
        s.addAll(callgraph.getAllMethods());
        for (Iterator i = s.iterator(); i.hasNext();) {
            BDD b = bdd.zero();
            jq_Method m = (jq_Method) i.next();
            if (m.getBytecode() == null)
                continue;
            ControlFlowGraph cfg = joeq.Compiler.Quad.CodeCache.getCode(m);
//            System.out.println("method " + m + " has "
//                    + cfg.getRegisterFactory().numberOfLocalRegisters()
//                    + " registers");
            MethodSummary ms = MethodSummary.getSummary(m);
            //boolean printCfg = false;
            for (Iterator j = cfg.getRegisterFactory().iterator(); j.hasNext();) {
                Register r = (Register) j.next();
                if (r == null) {
//                    System.out.println("register " + j + " is null");
                    continue;
                }
 //               System.out.println("register is "+j);
                Collection nodes = ms.getRegisterAtLocation(cfg.exit(), null, r);
                if (nodes == null)
                    continue;
                for (Iterator ni = nodes.iterator(); ni.hasNext();) {
                    Node n = (Node) ni.next();
                    b.orWith(reg.ithVar(bddIRBuilder.getRegisterID(r)).and(
                            V1.ithVar(Vmap.get(n))));
                }
            }
            b.andWith(M.ithVar(Mmap.get(m)));
            vReg.orWith(b);
        }
    }
    private void makeIQuadbdd() {
        iQuad = bdd.zero();
        BDDDomain quad = bddIRBuilder.getQuadDomain();
        ProgramLocation pl;
        BDD b;
        for (int i = 0; i < Imap.size(); ++i) {
            pl = (ProgramLocation) Imap.get(i);
            ProgramLocation.BCProgramLocation bcpl = (ProgramLocation.BCProgramLocation) LoadedCallGraph
                    .mapCall(pl);
            int quadID = bddIRBuilder.quadIdFromInvokeBCLocation(bcpl);
            b = I.ithVar(i);
            b.andWith(quad.ithVar(quadID));
            iQuad.orWith(b);
        }
    }
    
    private void makeHQuadbdd() {
        Assert.UNREACHABLE();
        // this doesn't work--BuildBDDIR doesn't have all the allocation
        // sites in its allocMap.
        hQuad = bdd.zero();
        BDDDomain quad = bddIRBuilder.getQuadDomain();
        BDD b;
        for (int i = 0; i < Hmap.size(); ++i) {
            ProgramLocation pl = (ProgramLocation) Imap.get(i);
            ProgramLocation.BCProgramLocation bcpl = (ProgramLocation.BCProgramLocation) LoadedCallGraph
                    .mapCall(pl);
            int quadID = bddIRBuilder.quadIdFromAllocBCLocation(bcpl);
            b = I.ithVar(i);
            b.andWith(quad.ithVar(quadID));
            iQuad.orWith(b);
        }
    }
    
    private void makeFMemberbdd() {
        fMember = bdd.zero();
        BDDDomain member = bddIRBuilder.getMemberDomain();
        BDD b;
        for (int i = 0; i < Fmap.size(); ++i) {
            Object o = Fmap.get(i);
            if (o instanceof jq_Field) {
                int memberID = bddIRBuilder.memberIdFromField((jq_Field)o);
                if (memberID == 0) continue;
                b = F.ithVar(i).and(member.ithVar(memberID));
                fMember.orWith(b);
            }
        }
    }
    
    private void dumpSSA() throws IOException {
        Assert._assert(DUMP_SSA);
        String dumpPath = System.getProperty("pa.dumppath", "");
        jq_MethodVisitor mv = null;
        ControlFlowGraphVisitor cfgv = null;
        Assert._assert(bddIRBuilder != null);
        mv = new ControlFlowGraphVisitor.CodeCacheVisitor(bddIRBuilder,
                true);
        //cv = new jq_MethodVisitor.DeclaredMethodVisitor(mv, methodNamesToProcess, false);
        Collection s = new TreeSet(new Comparator() {
            public int compare(Object o1, Object o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
        CallGraph callgraph = new CachedCallGraph(new PACallGraph(this));
        if (callgraph.getAllMethods() == null) {
            System.out.println("call graph has no methods!");
        }
        s.addAll(callgraph.getAllMethods());
        for (Iterator i = s.iterator(); i.hasNext();) {
            jq_Method m = (jq_Method) i.next();
            try {
                m.accept(mv);
            } catch (LinkageError le) {
                System.err
                        .println("Linkage error occurred while executing pass on "
                                + m + " : " + le);
                le.printStackTrace(System.err);
            } catch (Exception x) {
                System.err
                        .println("Runtime exception occurred while executing pass on "
                                + m + " : " + x);
                x.printStackTrace(System.err);
            }
        }
        System.err.println("Completed pass! " + bddIRBuilder);
        makeVRegbdd(callgraph);
        makeIQuadbdd();
        makeFMemberbdd();
        //makeHQuadbdd();
        BDDDomain reg = bddIRBuilder.getDestDomain();
        BDDDomain quad = bddIRBuilder.getQuadDomain();
        BDDDomain member = bddIRBuilder.getMemberDomain();
        System.out.println("vReg: "
                + (long) vReg.satCount(V1.set().and(reg.set().and(M.set())))
                + " relations, " + vReg.nodeCount() + " nodes");
        bdd_save(dumpPath + "/vReg.bdd", vReg);
        System.out.println("iQuad: "
                + (long) iQuad.satCount(I.set().and(quad.set()))
                + " relations, " + iQuad.nodeCount() + " nodes");
        bdd_save(dumpPath + "/iQuad.bdd", iQuad);
        System.out.println("fMember: "
                + (long) fMember.satCount(F.set().and(member.set()))
                + " relations, " + fMember.nodeCount() + " nodes");
        bdd_save(dumpPath + "/fMember.bdd", fMember);
        //System.out.println("hQuad: "+(long) sync.satCount(H1.set().and(quad.set()))+" relations, "+hQuad.nodeCount()+" nodes");

        //bdd_save(resultsFileName+".hQuad", sync);
    }    
    
    private void buildSpecialMapInfo() {
       stringConstant = bdd.zero();
        
        Object dummy = STRmap.get(0);
        
        for (Iterator i=Hmap.iterator(); i.hasNext();) {
            Node node = (Node) i.next();
            if(!(node instanceof ConcreteTypeNode)) {
                continue;
            }
            ConcreteTypeNode ctn = (ConcreteTypeNode) node;
            if (!(ctn.getDeclaredType() instanceof jq_Class)) {
                continue;
            }
            jq_Class c = (jq_Class) ctn.getDeclaredType();
            if (!c.getName().equals("java.lang.String")) {
                continue;
            }
            
            Object stringConst = MethodSummary.stringNodes2Values.get(node);
            if (stringConst == null) {
                stringConst = dummy;
            }
            
            stringConstant.orWith(       H1.ithVar( Hmap.get(node) )
                                   .and( STR.ithVar( STRmap.get(stringConst) ) ) );
        }

        jq_NameAndDesc equalsNd = new jq_NameAndDesc("equals", "(Ljava/lang/Object;)Z");
        jq_NameAndDesc hashCodeNd = new jq_NameAndDesc("hashCode", "()I");
        
        overridesEqualsOrHashcode = bdd.zero();
        for (Iterator i=Tmap.iterator(); i.hasNext();) {
            jq_Reference r = (jq_Reference) i.next();
            if (!(r instanceof jq_Class)) continue;
            jq_Class c = (jq_Class) r;
            boolean hasOverride = false;
            
            // These should happen at the same, but check both anyway.
            jq_Method equalsMethod = c.getInstanceMethod(equalsNd);
            if (!equalsMethod.getDeclaringClass().getName().equals("java.lang.Object")) {
                hasOverride = true;
            }
            
            jq_Method hashCodeMethod = c.getInstanceMethod(hashCodeNd); 
            if (!hashCodeMethod.getDeclaringClass().getName().equals("java.lang.Object")) {
                hasOverride = true;
            }
            
            if (hasOverride) {
                overridesEqualsOrHashcode.orWith( T1.ithVar( Tmap.get(r) ) );
            }
        }
    }
}
