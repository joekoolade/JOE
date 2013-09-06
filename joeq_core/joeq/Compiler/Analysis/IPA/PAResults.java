// PAResults.java, created Nov 3, 2003 12:34:24 AM by joewhaley
// Copyright (C) 2003 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.Analysis.IPA;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.URL;
import joeq.Class.PrimordialClassLoader;
import joeq.Class.jq_Class;
import joeq.Class.jq_Field;
import joeq.Class.jq_Method;
import joeq.Class.jq_NameAndDesc;
import joeq.Class.jq_Reference;
import joeq.Class.jq_Type;
import joeq.Compiler.Analysis.FlowInsensitive.MethodSummary;
import joeq.Compiler.Analysis.FlowInsensitive.MethodSummary.Node;
import joeq.Compiler.Analysis.IPA.ProgramLocation.QuadProgramLocation;
import joeq.Compiler.Analysis.IPSSA.ContextSet;
import joeq.Compiler.Analysis.IPSSA.SSALocation;
import joeq.Compiler.Quad.BasicBlock;
import joeq.Compiler.Quad.CallGraph;
import joeq.Compiler.Quad.CodeCache;
import joeq.Compiler.Quad.LoadedCallGraph;
import joeq.Compiler.Quad.Operator;
import joeq.Compiler.Quad.Quad;
import joeq.Compiler.Quad.Operand.RegisterOperand;
import joeq.Compiler.Quad.RegisterFactory.Register;
import joeq.Main.Driver;
import joeq.Main.HostedVM;
import joeq.Util.IO.SourceLister;
import jwutil.collections.HashWorklist;
import jwutil.collections.IndexedMap;
import jwutil.collections.LinearSet;
import jwutil.collections.Pair;
import jwutil.collections.UnmodifiableIterator;
import jwutil.console.SimpleInterpreter;
import jwutil.graphs.Navigator;
import jwutil.graphs.PathNumbering;
import jwutil.graphs.SCCPathNumbering;
import jwutil.graphs.SCCTopSortedGraph;
import jwutil.graphs.SCComponent;
import jwutil.graphs.PathNumbering.Range;
import jwutil.graphs.SCCPathNumbering.Path;
import jwutil.strings.Strings;
import jwutil.util.Assert;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDDomain;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;
import net.sf.javabdd.TypedBDDFactory.TypedBDD;

/**
 * Records results for pointer analysis.  The results can be saved and reloaded.
 * This class also provides methods to query the results.
 * 
 * @author John Whaley
 * @version $Id: PAResults.java,v 1.56 2004/10/16 04:12:48 joewhaley Exp $
 */
public class PAResults implements PointerAnalysisResults {
    final PA r;
    
    CallGraph cg;
    
    public PAResults(PA pa) {
        r = pa;
    }
    
    public PA getPAResults() {
        return r;
    }
    
    public static void main(String[] args) throws IOException {
        initialize(null);
        PAResults r = loadResults(args, null);
        if (System.getProperty("pa.stats") != null)
            r.printStats();
        else
            r.interactive();
    }
    
    public static void initialize(String addToClasspath) {
        // We use bytecode maps.
        CodeCache.AlwaysMap = true;
        if (PA.USE_JOEQ_CLASSLIBS) {
            System.setProperty("joeq.classlibinterface", "joeq.ClassLib.pa.Interface");
            joeq.ClassLib.ClassLibInterface.useJoeqClasslib(true);
        }
        HostedVM.initialize();
        
        if (addToClasspath != null)
            PrimordialClassLoader.loader.addToClasspath(addToClasspath);
    }

    public static PAResults loadResults(String[] args, String addToClasspath) throws IOException {
        String prefix;
        if (args != null && args.length > 0) {
            prefix = args[0];
            String sep = System.getProperty("file.separator");
            if (!prefix.endsWith(sep))
                prefix += sep;
        } else {
            prefix = "";
        }
        String fileName = System.getProperty("pa.results", "pa");
        String bddfactory = "typed";
        PAResults r = loadResults(bddfactory, prefix, fileName);
        return r;
    }
    
    public static PAResults loadResults(String bddfactory,
                                        String prefix,
                                        String fileName) throws IOException {
        PA pa = PA.loadResults(bddfactory, prefix, fileName);
        PAResults r = new PAResults(pa);
        r.loadCallGraph(prefix+"callgraph");
        // todo: load path numbering instead of renumbering.
        if (pa.CONTEXT_SENSITIVE || pa.OBJECT_SENSITIVE) {
            pa.addDefaults();
            pa.numberPaths(r.cg, pa.ocg, false);
        }
        return r;
    }
    
    /** Load call graph from the given file name.
     */
    public void loadCallGraph(String fn) throws IOException {
        cg = new LoadedCallGraph(fn);
    }
    
    private HashMap/*<String,Object>*/ storedBDDs = new HashMap();
    public void storeBDD(String name, TypedBDD bdd1) {
        storedBDDs.put(name, bdd1);
        System.out.println("Stored BDD under name `" + name + "'");
    }
    public void interactive() {
        int i = 1;
        List results = new ArrayList();
        DataInput in = new DataInputStream(System.in);
        SimpleInterpreter si = new SimpleInterpreter((URL[])null, storedBDDs);
        Stack/*<DataInputStream>*/ inputs = new Stack();     // to support "include" command
        for (;;) {
            boolean increaseCount = true;
            int listHowMany = DEFAULT_NUM_TO_PRINT;
            
            try {
                if (inputs.isEmpty())          // prompt only if stdin
                    System.out.print(i+"> ");
                String s = in.readLine();
                if (s == null) {
                    if (inputs.isEmpty())
                        return;
                    in = (DataInput)inputs.pop();
                    continue;
                }
                if (!inputs.isEmpty())          // echo for user's benefit
                    System.out.println(i+"> "+s);
                StringTokenizer st = new StringTokenizer(s);
                if (!st.hasMoreElements()) continue;
                String command = st.nextToken();
                if (command.equals("quit") || command.equals("exit")) {
                    break;
                } else if (command.equals("include")) {
                    inputs.push(in);
                    in = new DataInputStream(new FileInputStream(st.nextToken()));
                    continue;
                } else if (command.equals("relprod")) {
                    TypedBDD bdd1 = parseBDD(results, st.nextToken());
                    TypedBDD bdd2 = parseBDD(results, st.nextToken());
                    TypedBDD set = parseBDDset(results, st.nextToken());
                    TypedBDD r = (TypedBDD) bdd1.relprod(bdd2, set);
                    results.add(r);
                } else if (command.equals("replace")) {
                    TypedBDD bdd = parseBDD(results, st.nextToken());
                    String ps = st.nextToken();
                    BDDPairing pair = parsePairing(ps);
                    if (pair != null) {
                        results.add(bdd.replace(pair));
                    } else {
                        System.out.println("No such pairing: " + ps);
                        increaseCount = false;
                    }
                } else if (command.equals("restrict")) {
                    TypedBDD bdd1 = parseBDD(results, st.nextToken());
                    TypedBDD r = bdd1;
                    while (st.hasMoreTokens()) {
                        TypedBDD bdd2 = parseBDD(results, st.nextToken());
                        r = (TypedBDD) r.restrict(bdd2);
                    }
                    results.add(r);
                } else if (command.equals("exist")) {
                    TypedBDD bdd1 = parseBDD(results, st.nextToken());
                    TypedBDD r = bdd1;
                    while (st.hasMoreTokens()) {
                        TypedBDD set = parseBDDset(results, st.nextToken());
                        r = (TypedBDD) r.exist(set);
                    }
                    results.add(r);
                } else if (command.equals("diff")) {
                    TypedBDD bdd1 = parseBDD(results, st.nextToken());
                    TypedBDD bdd2 = parseBDD(results, st.nextToken());
                    TypedBDD r = (TypedBDD) bdd1.apply(bdd2, BDDFactory.diff);
                    results.add(r);
                } else if (command.equals("and")) {
                    TypedBDD bdd1 = parseBDD(results, st.nextToken());
                    TypedBDD bdd2 = parseBDD(results, st.nextToken());
                    TypedBDD r = (TypedBDD) bdd1.and(bdd2);
                    results.add(r);
                } else if (command.equals("cmp") || command.equals("equals")) {
                    TypedBDD bdd1 = parseBDD(results, st.nextToken());
                    TypedBDD bdd2 = parseBDD(results, st.nextToken());
                    System.out.println(command + " " + bdd1.equals(bdd2));
                    increaseCount = false;
                } else if (command.equals("or")) {
                    TypedBDD bdd1 = parseBDD(results, st.nextToken());
                    TypedBDD bdd2 = parseBDD(results, st.nextToken());
                    TypedBDD r = (TypedBDD) bdd1.or(bdd2);
                    results.add(r);
                } else if (command.equals("findpath")) {
                    int m1 = Integer.parseInt(st.nextToken());
                    int m2 = Integer.parseInt(st.nextToken());
                    Path trace = findPath(m1, m2);
                    if (trace != null) {
                        System.out.println(getMethod(m2));
                        printTrace(System.out, trace);
                    } else
                        System.out.println("there is no path from " + getMethod(m1) + " to " + getMethod(m2));
                    increaseCount = false;
                } else if (command.equals("store")) {
                    String name = st.nextToken();
                    TypedBDD bdd1 = parseBDD(results, st.nextToken());
                    storeBDD(name, bdd1);
                    increaseCount = false;
                } else if (command.equals("satcount")) {
                    TypedBDD r = parseBDDWithCheck(results, st.nextToken());
                    System.out.println("Domains:  " + r.getDomainSet());
                    System.out.println("satCount: " + r.satCount(getDomains(r)));
                    increaseCount = false;
                } else if (command.equals("showdomains")) {
                    TypedBDD r = parseBDDWithCheck(results, st.nextToken());
                    System.out.println("Domains: " + r.getDomainSet());
                    increaseCount = false;
                } else if (command.equals("source")) {
                    TypedBDD r = parseBDDWithCheck(results, st.nextToken());
                    int before = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : SourceLister.defaultLinesBefore;
                    int after = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : SourceLister.defaultLinesAfter;
                    showSource(r, before, after);
                    increaseCount = false;
                } else if (command.equals("list")) {
                    TypedBDD r = parseBDDWithCheck(results, st.nextToken());
                    results.add(r);
                    listHowMany = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : -1;
                    System.out.println("Domains: " + r.getDomainSet());
                } else if (command.equals("contextvar") || command.equals("stacktracevar")) {
                    int varNum = Integer.parseInt(st.nextToken());
                    Node n = getVariableNode(varNum);
                    if (n == null) {
                        System.out.println("No method for node "+n);
                    } else {
                        jq_Method m = n.getDefiningMethod();
                        Number c = new BigInteger(st.nextToken(), 10);
                        if (m == null) {
                            System.out.println("No method for node "+n);
                        } else {
                            Path trace = ((SCCPathNumbering)r.vCnumbering).getPath(m, c);
                            if (command.equals("stacktracevar")) {
                                System.out.println(m + " called in context #" + c);
                                printTrace(System.out, trace);
                            } else
                                System.out.println(m+" context "+c+":\n"+trace);
                            
                        }
                    }
                    increaseCount = false;
                } else if (command.equals("contextheap") || command.equals("stacktraceheap")) {
                    int varNum = Integer.parseInt(st.nextToken());
                    Node n = (Node) getHeapNode(varNum);
                    if (n == null) {
                        System.out.println("No method for node "+n);
                    } else {
                        jq_Method m = n.getDefiningMethod();
                        Number c = new BigInteger(st.nextToken(), 10);
                        if (m == null) {
                            System.out.println("No method for node "+n);
                        } else {
                            Path trace = ((SCCPathNumbering)r.hCnumbering).getPath(m, c);
                            if (command.equals("stacktraceheap")) {
                                System.out.println(m + " called in context #" + c);
                                printTrace(System.out, trace);
                            } else
                                System.out.println(m+" context "+c+": "+trace);
                        }
                    }
                    increaseCount = false;
                } else if (command.equals("type")) {
                    jq_Class c = parseClassName(st.nextToken());
                    if (c == null || !c.isLoaded()) {
                        System.out.println("Cannot find class");
                        increaseCount = false;
                    } else {
                        System.out.println("Class: "+c);
                        int k = getTypeIndex(c);
                        results.add(r.T1.ithVar(k));
                    }
                } else if (command.equals("comparecc")) {
                    jq_Class c = parseClassName(st.nextToken());
                    if (c == null || !c.isLoaded()) {
                        System.out.println("Cannot find class");
                    } else {
                        String methodname = st.nextToken();
                        jq_Method m;
                        if (st.hasMoreTokens()) m = (jq_Method) c.getDeclaredMember(methodname, st.nextToken());
                        else m = c.getDeclaredMethod(methodname);
                        if (m == null || !m.isLoaded()) {
                            System.out.println("Cannot find method");
                            increaseCount = false;
                        } else {
                            compareCallingContexts(m);
                        }
                    }
                    increaseCount = false;
                } else if (command.equals("method") || command.equals("callsin") || command.equals("summary") || command.equals("params")) {
                    jq_Class c = parseClassName(st.nextToken());
                    if (c == null || !c.isLoaded()) {
                        System.out.println("Cannot find class");
                        increaseCount = false;
                    } else {
                        String methodname = st.nextToken();
                        jq_Method m;
                        if (st.hasMoreTokens()) m = (jq_Method) c.getDeclaredMember(methodname, st.nextToken());
                        else m = c.getDeclaredMethod(methodname);
                        if (m == null || !m.isLoaded()) {
                            System.out.println("Cannot find method");
                            increaseCount = false;
                        } else {
                            if (command.equals("method")) {
                                int k = getMethodIndex(m);
                                int n = getNameIndex(m);
                                System.out.println("Method: "+m+" N("+n+")");
                                if (r.vCnumbering instanceof SCCPathNumbering) {
                                    SCComponent scc = ((SCCPathNumbering)r.vCnumbering).getSCC(m);
                                    Range range = ((SCCPathNumbering)r.vCnumbering).getRange(m);
                                    if (scc != null) {
                                        System.out.println("is located in SCC #" + System.identityHashCode(scc) 
                                            + " of size " + scc.size() + "; context range is " + range);
                                    } 
                                }
                                results.add(r.M.ithVar(k));
                            } else {
                                MethodSummary ms = MethodSummary.getSummary(CodeCache.getCode(m));
                                if (command.equals("callsin")) {
                                    TypedBDD rc = (TypedBDD)r.bdd.zero();
                                    for (Iterator j=ms.getCalls().iterator(); j.hasNext(); ) {
                                        ProgramLocation mc = (ProgramLocation) j.next();
                                        int iidx = getInvokeIndex(mc);
                                        if (iidx == -1)
                                            System.out.println("callsite not in index: " + mc + " at " + mc.toStringLong());
                                        else
                                            rc.orWith(r.I.ithVar(iidx));
                                    }
                                    results.add(rc);
                                } 
                                else if (command.equals("params")) {
                                   for(int j = 0; j < ms.getNumOfParams(); j++) {
                                       Node pn = ms.getParamNode(j);
                                       System.out.println("\t" + pn);
                                   }
                                   // print out all fields in the
                                   increaseCount = false; 
                               }
                               else {
                                   System.out.println(ms);
                                   increaseCount = false;
                               }
                            }
                        }
                    }
                } else if (command.equals("field")) {
                    jq_Class c = parseClassName(st.nextToken());
                    if (c == null || !c.isLoaded()) {
                        System.out.println("Cannot find class");
                        increaseCount = false;
                    } else {
                        jq_Field m = c.getDeclaredField(st.nextToken());
                        if (m == null) {
                            System.out.println("Cannot find field");
                            increaseCount = false;
                        } else {
                            System.out.println("Field: "+m);
                            int k = getFieldIndex(m);
                            results.add(r.F.ithVar(k));
                        }
                    }
                } else if (command.equals("thread")) {
                    int k = Integer.parseInt(st.nextToken());
                    jq_Method run = null;
                    Iterator j = PA.thread_runs.keySet().iterator();
                    while (--k >= 0) {
                        run = (jq_Method) j.next();
                    }
                    System.out.println(k+": "+run);
                    int x = getMethodIndex(run);
                    results.add(r.M.ithVar(x));
                } else if (command.equals("threadlocal")) {
                    TypedBDD r = (TypedBDD) getThreadLocalObjects();
                    results.add(r);
                } else if (command.equals("reachable")) {
                    TypedBDD bdd1 = parseBDD(results, st.nextToken());
                    TypedBDD r = (TypedBDD) getReachableVars(bdd1);
                    results.add(r);
                } else if (command.equals("usedef")) {
                    TypedBDD bdd1 = parseBDD(results, st.nextToken());
                    BDD r = calculateUseDef(bdd1);
                    results.add(r);
                } else if (command.equals("printusedef")) {
                    TypedBDD bdd1 = parseBDD(results, st.nextToken());
                    printUseDefChain(bdd1);
                    increaseCount = false;
                } else if (command.equals("dumpusedef")) {
                    TypedBDD bdd1 = parseBDD(results, st.nextToken());
                    DataOutput out = new DataOutputStream(new FileOutputStream("usedef.dot"));
                    this.defUseGraph(bdd1, false, out);
                    increaseCount = false;
                } else if (command.equals("defuse")) {
                    TypedBDD bdd1 = parseBDD(results, st.nextToken());
                    BDD r = calculateDefUse(bdd1);
                    results.add(r);
                } else if (command.equals("dumpdefuse")) {
                    TypedBDD bdd1 = parseBDD(results, st.nextToken());
                    DataOutput out = new DataOutputStream(new FileOutputStream("defuse.dot"));
                    this.defUseGraph(bdd1, true, out);
                    increaseCount = false;
                } else if (command.equals("encapsulation")) {
                    BDD r = getEncapsulatedHeapObjects();
                    results.add(r);
                } else if (command.equals("indistinguishable")) {
                    BDD r = findIndistinguishablyTypedObjects();
                    results.add(r);
                } else if (command.equals("showargs")) {
                    TypedBDD bdd1 = parseBDD(results, st.nextToken());
                    TypedBDD r = showArguments(bdd1);
                    results.add(r);     // lists points-to sets of arguments for a set of isite
                } else if (command.equals("gini")) {
                    computeGini(r.vCnumbering);
                    increaseCount = false;
                } else if (command.equals("buildsummaries")) {
                    int before = Node.numberOfNodes();
                    for (Iterator ii = r.Mmap.iterator(); ii.hasNext(); ) {
                        jq_Method m = (jq_Method) ii.next();
                        MethodSummary ms = MethodSummary.getSummary(m);
                    }
                    int after = Node.numberOfNodes();
                    System.out.println("before: " + before + " nodes, after: " + after + " nodes.");
                    increaseCount = false;
                } else if (command.equals("stats")) {
                    printStats();
                    increaseCount = false;
                } else if (command.equals("help")) {
                    printHelp(results);
                    increaseCount = false;
                } else {
                    increaseCount = false;
                    if (command.equals("driver"))       // prefix "driver"
                        command = st.nextToken();
                    String []cmds = new String[st.countTokens()+1];
                    cmds[0] = command;
                    for (int j = 1; j < cmds.length; j++)
                        cmds[j] = st.nextToken();
                    si.getStore().put("$paproxy", new PAProxy(r));
                    si.getStore().put("$paresults", this);
                    for (int j = 0; j < cmds.length; j++)
                        j = Driver.processCommand(cmds, j, si);
                }

                if (increaseCount) {
                    TypedBDD r = (TypedBDD) results.get(i-1);
                    System.out.println(i+" -> "+toString(r, listHowMany));
                    Assert._assert(i == results.size());
                    ++i;
                }
            } catch (Exception e) {
                System.err.println("Error: "+e);
                e.printStackTrace();
                increaseCount = false;
            }
        }
    }
    
    public void printHelp(List results) {
        System.out.println("BDD manipulation:");
        System.out.println("relprod b1 b2 bs:                 relational product of b1 and b2 w.r.t. set bs");
        System.out.println("replace b1 pair:                  replace b1 according to pair");
        System.out.println("restrict b1 b2 (bi)*:             restrict b2 to bi in b1");
        System.out.println("exist b1 bs1 (bsi)*:              exist bs2 to bsi in b1");
        System.out.println("(and|or|diff) b1 b2:              compute b1 and|or|diff b2");
        System.out.println("(equals|cmp) b1 b2:               compare bdds b1 and b2");
        System.out.println("list b1 [#n]:                     list #n (or all) elements of bdd b1");
        System.out.println("showdomains b1:                   show domains of bdd b1");
        System.out.println("satcount b1:                      print satcount (restricted by domain)");
        System.out.println("store name b1:                    store BDD b1 under name");
        System.out.println("\nAnalysis Results:");
        System.out.println("dumpconnect # <fn>:               dump heap connectivity graph for heap object # to file fn");
        System.out.println("dumpallconnect <fn>:              dump entire heap connectivity graph to file fn");
        System.out.println("dumpdefuse b1:                    dump def/use graph to defuse.dot, bdd must be in V1xV1c");
        System.out.println("dumpusedef b1:                    dump use/def graph to usedef.dot, bdd must be in V1xV1c");
        System.out.println("threadlocal:                      run escape analysis");
        System.out.println("stats:                            print general statistics");
        System.out.println("contextvar #vidx #cidx:           show path in vCnumbering for var #vidx in context #");
        System.out.println("stacktracevar #vidx #cidx:        like contextvar, except print as stacktrace");
        System.out.println("contextheap #hidx #cidx:          show path in hCnumbering for heap obj #hidx in context #");
        System.out.println("stacktraceheap #hidx #cidx:       like contextheap, except print as stacktrace");
        System.out.println("findpath #from #to:               find a path in callgraph from method #from to #to");
        System.out.println("gini:                             compute unbiased Gini-coefficient for callgraph and show SCCs");
        System.out.println("\nProgram Information:");
        System.out.println("method  class name [signature]:   lookup method in class, shows M and N indices");
        System.out.println("callsin class name [signature]:   list all call sites in a given method");
        System.out.println("summary class name [signature]:   list method summary for a given method");
        System.out.println("params class name [signature]:    list method parameters for a given method");
        System.out.println("source bdd [#before [#after]]:    show source code surrounding items in bdd");
        System.out.println("field class name:                 show information about field name in class");
        System.out.println("\nHow to use this driver:");
        System.out.println("include file:                     execute commands in file");
        System.out.println("[driver] arg0 arg1 ...:           pass args to Main.Driver for interpretation");

        printAvailableBDDs(results);
    }

    public void printAvailableBDDs(List results) {
        Collection allbdds = new ArrayList();
        for (int i = 0; i < r.bdd.numberOfDomains(); ++i)
            allbdds.add(r.bdd.getDomain(i));
        Field []f = PA.class.getDeclaredFields();
        for (int i = 0; i < f.length; i++) {
            try {
                if (f[i].getType() == BDD.class && f[i].get(r) != null) {                    
                    if(i % 12 == 0) {
                        // add line breaks
                        allbdds.add("\n" + f[i].getName());
                    } else {
                        allbdds.add(f[i].getName());
                    }
                }
            } catch (IllegalAccessException _) { }
        }
        f = PAResults.class.getDeclaredFields();
        for (int i = 0; i < f.length; i++) {
            try {
                if (f[i].getType() == BDD.class && f[i].get(this) != null)
                    allbdds.add(f[i].getName());
            } catch (IllegalAccessException _) { }
        }
        if (storedBDDs.size() > 0)
            allbdds.add("stored BDDs " + (storedBDDs.keySet()) + "\n");
        if (results.size() >= 1)
            allbdds.add("and previous results 1.." + (results.size()));
        System.out.println("\ncurrently known BDDs are " + allbdds);
    }

    /** Print a Path as if it were a stacktrace. */
    public void printTrace(PrintStream out, Path trace) {
        for (int i = trace.size() - 1; i >= 0; --i) {
            Object o = trace.get(i);
            if (o instanceof ProgramLocation) {
                out.println(" at " + ((ProgramLocation)o).toStringLong());
            }
        }
    }

    /**
     * Find a path from a method to another method in the callgraph.
     */
    Path findPath(int from, int to) {
        jq_Method fm = getMethod(from);
        jq_Method tm = getMethod(to);
        if (fm == null || tm == null)
            return null;

        // breadth-first-search
        Navigator ng = cg.getCallSiteNavigator();
        LinkedList queue = new LinkedList();
        HashMap prev = new HashMap();

        queue.addLast(fm);
        prev.put(fm, null);
    outer:
        while (queue.size() != 0) {
            Object f = queue.removeFirst();
            Collection succ = ng.next(f);
            Iterator it = succ.iterator();
            while (it.hasNext()) {
                Object t = it.next();
                if (!prev.containsKey(t)) {
                    prev.put(t, f);
                    queue.addLast(t);
                }
                if (t == tm)
                    break outer;
            }
        }
        
        if (!prev.containsKey(tm))
            return null;

        Path rc = new Path(tm);
        Object f = prev.get(tm);
        while (f != null) {
            rc = new Path(f, rc);
            f = prev.get(f);
        }
        return rc;
    }

    public jq_Class parseClassName(String className) {
        jq_Class c = (jq_Class) jq_Type.parseType(className);
        if (c != null) return c;
        jq_Type[] types = PrimordialClassLoader.loader.getAllTypes();
        for (int i = 0; i < PrimordialClassLoader.loader.getNumTypes(); ++i) {
            jq_Type t = types[i];
            if (t instanceof jq_Class) {
                c = (jq_Class) t; 
                if (c.getJDKName().endsWith(className))
                    return c;
            }
        }
        return null;
    }
    
    public Node getVariableNode(int v) {
        if (v < 0 || v >= r.Vmap.size())
            return null;
        Node n = (Node) r.Vmap.get(v);
        return n;
    }
    
    public int getVariableIndex(Node n) {
        if (!r.Vmap.contains(n)) return -1;
        int v = r.Vmap.get(n);
        return v;
    }

    public ProgramLocation getInvoke(int v) {
        if (v < 0 || v >= r.Imap.size())
            return null;
        ProgramLocation n = (ProgramLocation) r.Imap.get(v);
        return n;
    }
    
    public int getInvokeIndex(ProgramLocation n) {
        n = LoadedCallGraph.mapCall(n);
        if (!r.Imap.contains(n)) return -1;
        int v = r.Imap.get(n);
        return v;
    }
    
    public Node getHeapNode(int v) {
        if (v < 0 || v >= r.Hmap.size())
            return null;
        Node n = (Node) r.Hmap.get(v);
        return n;
    }
    
    public int getHeapIndex(Node n) {
        if (!r.Hmap.contains(n)) return -1;
        int v = r.Hmap.get(n);
        return v;
    }

    public jq_Field getField(int v) {
        if (v < 0 || v >= r.Fmap.size())
            return null;
        jq_Field n = (jq_Field) r.Fmap.get(v);
        return n;
    }
    
    public int getFieldIndex(jq_Field m) {
        if (!r.Fmap.contains(m)) return -1;
        int v = r.Fmap.get(m);
        return v;
    }
    
    public jq_Reference getType(int v) {
        if (v < 0 || v >= r.Tmap.size())
            return null;
        jq_Reference n = (jq_Reference) r.Tmap.get(v);
        return n;
    }
    
    public int getTypeIndex(jq_Type m) {
        if (!r.Tmap.contains(m)) return -1;
        int v = r.Tmap.get(m);
        return v;
    }
    
    public jq_Method getName(int v) {
        if (v < 0 || v >= r.Nmap.size())
            return null;
        jq_Method n = (jq_Method) r.Nmap.get(v);
        return n;
    }
    
    public int getNameIndex(jq_Method m) {
        if (!r.Nmap.contains(m)) return -1;
        int v = r.Nmap.get(m);
        return v;
    }
    
    public jq_Method getMethod(int v) {
        if (v < 0 || v >= r.Mmap.size())
            return null;
        jq_Method n = (jq_Method) r.Mmap.get(v);
        return n;
    }
    
    public int getMethodIndex(jq_Method m) {
        if (!r.Mmap.contains(m)) return -1;
        int v = r.Mmap.get(m);
        return v;
    }
 
    /** For a given TypedBDD, return its domains.
     * This duplicates TypedBDD.getDomains() which for unknown reasons is not public.
     */
    TypedBDD getDomains(TypedBDD b) {
        TypedBDD dset = (TypedBDD) b.getFactory().one();
        Set domains = b.getDomainSet();
        for (Iterator i = domains.iterator(); i.hasNext(); ) {
            BDDDomain d = (BDDDomain) i.next();
            dset.andWith(d.set());
        }
        return dset;
    }

    BDDDomain parseDomain(String dom) {
        for (int i = 0; i < r.bdd.numberOfDomains(); ++i) {
            if (dom.equals(r.bdd.getDomain(i).getName()))
                return r.bdd.getDomain(i);
        }
        return null;
    }

    TypedBDD parseBDDWithCheck(List results, String bddname) throws Exception {
        TypedBDD r = parseBDD(results, bddname);
        if (r == null) {
            printAvailableBDDs(results);
            throw new Exception("No such BDD: " + bddname);
        }
        return r;
    }

    BDDPairing parsePairing(String s) {
        try {
            Field f = PA.class.getDeclaredField(s);
            if (f.getType() == BDDPairing.class) {
                return (BDDPairing) f.get(r);
            }
        } catch (NoSuchFieldException e) {
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        }
        return null;
    }

    private TypedBDD lookupBDDField(String s, Class clazz, Object r) {
        try {
            Field f = clazz.getDeclaredField(s);
            if (f.getType() == BDD.class) {
                return (TypedBDD) f.get(r);
            }
        } catch (NoSuchFieldException e) {
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        }
        return null;
    }

    TypedBDD parseBDD(List a, String s) {
        int paren_index = s.indexOf('(');
        if (paren_index > 0) {
            int close_index = s.indexOf(')');
            if (close_index <= paren_index) return null;
            String domainName = s.substring(0, paren_index);
            BDDDomain dom = parseDomain(domainName);
            if (dom == null) return null;
            long index = Long.parseLong(s.substring(paren_index+1, close_index));
            return (TypedBDD) dom.ithVar(index);
        }
        TypedBDD rc = lookupBDDField(s, PA.class, r);
        if (rc != null)
            return rc;
        rc = lookupBDDField(s, PAResults.class, this);
        if (rc != null)
            return rc;
        BDDDomain d = parseDomain(s);
        if (d != null) {
            return (TypedBDD) d.domain();
        }
        TypedBDD stored = (TypedBDD)storedBDDs.get(s);
        if (stored != null)
            return stored;
        try {
            int num = Integer.parseInt(s) - 1;
            if (num >= 0 && num < a.size()) {
                return (TypedBDD) a.get(num);
            }
        } catch (NumberFormatException e) { }
        if (s.equals("$last"))
            return (TypedBDD)a.get(a.size()-1);
        if (s.equals("$one"))
            return (TypedBDD)r.bdd.one();
        if (s.equals("$zero"))
            return (TypedBDD)r.bdd.zero();
        return null;
    }

    TypedBDD parseBDDset(List a, String s) {
        BDDDomain d = parseDomain(s);
        if (d != null) {
            return (TypedBDD) d.set();
        }
        return parseBDD(a, s);
    }

    public static final int DEFAULT_NUM_TO_PRINT = 10;
    
    public String toString(TypedBDD b, int numToPrint) {
        if (b == null) return "<you passed 'null' to PAResult.toString>";
        if (b.isZero()) return "<empty>";
        TypedBDD dset = (TypedBDD) b.getFactory().one();
        Set domains = b.getDomainSet();
        for (Iterator i = domains.iterator(); i.hasNext(); ) {
            BDDDomain d = (BDDDomain) i.next();
            dset.andWith(d.set());
        }
        StringBuffer sb = new StringBuffer();
        int j = 0;
        for (Iterator i = b.iterator(); i.hasNext(); ++j) {
            if (numToPrint >= 0 && j > numToPrint - 1) {
                sb.append("\tand "+((long)b.satCount(dset)-numToPrint)+" others.");
                sb.append(Strings.lineSep);
                break;
            }
            TypedBDD b1 = (TypedBDD) i.next();
            sb.append("\t(");
            sb.append(b1.toStringWithDomains(r.TS));
            sb.append(')');
            sb.append(Strings.lineSep);
        }
        return sb.toString();
    }

    /***** COOL OPERATIONS BELOW *****/
    
    int heapConnectivityQueries;
    int heapConnectivitySteps;
    /** Given a heap object (H1xH1c), calculate the set of heap objects (H1xH1c) that
     * are reachable by following a chain of access paths.
     */
    public BDD calculateHeapConnectivity(BDD h1) {
        BDD result = r.bdd.zero();
        BDD h1h2 = r.hP.exist(r.Fset);
        for (;;) {
            BDD b = h1.relprod(h1h2, r.H1set);
            b.replaceWith(r.H2toH1);
            b.applyWith(result.id(), BDDFactory.diff);
            result.orWith(b.id());
            if (b.isZero()) break;
            h1 = b;
            ++heapConnectivitySteps;
        }
        h1h2.free();
        ++heapConnectivityQueries;
        return result;
    }
    
    /** Given a set of types (T2), calculate the tightest common superclass (T2).
     */
    public BDD calculateCommonSupertype(BDD types) {
        if (types.isZero()) return r.bdd.zero();
        BDD bestTypes = r.T1.domain();
        //System.out.println("Looking for supertype of "+types.toStringWithDomains(r.TS));
        for (Iterator i = types.iterator(r.T2set); i.hasNext(); ) {
            BDD b = (BDD) i.next();
            BDD c = b.relprod(r.aT, r.T2set);
            b.free();
            bestTypes.andWith(c); // T1
        }
        for (Iterator i = bestTypes.iterator(r.T1set); i.hasNext(); ) {
            BDD b = (BDD) i.next();
            BDD c = b.relprod(r.aT, r.T1set); // T2
            b.free();
            c.replaceWith(r.T2toT1); // T1
            c.andWith(bestTypes.id()); // T1
            if (c.satCount(r.T1set) == 1.0) {
                return c;
            }
        }
        System.out.println("No subtype matches! "+bestTypes.toStringWithDomains(r.TS));
        return r.bdd.zero();
    }
    
    /** Given a set of uses (V1xV1c), calculate the set of definitions (V1xV1c) that
     * reach that set of uses.  Only does one step at a time; you'll have to iterate
     * to get the transitive closure.
     */
    public BDD calculateUseDef(BDD r_v1) {
        // A: v2=v1;
        BDD b = r.A.relprod(r_v1, r.V1set); // V2
        b.replaceWith(r.V2toV1);
        //System.out.println("Arguments/Return Values = "+b.satCount(r.V2set));
        BDD r_v2 = r_v1.replace(r.V1toV2);
        // L: v2=v1.f;
        BDD c = r.L.relprod(r_v2, r.V2set); // V1xF
        r_v2.free();
        BDD d = r.vP.relprod(c, r.V1set); // H1xF
        c.free();
        BDD e = r.hP.relprod(d, r.H1Fset); // H2
        d.free();
        e.replaceWith(r.H2toH1);
        BDD f = r.vP.relprod(e, r.H1set); // V1
        //System.out.println("Loads/Stores = "+f.satCount(r.V1set));
        e.free();
        f.orWith(b);
        return f;
    }
    
    /** Given a set of definitions (V1xV1c), calculate the set of uses (V1xV1c) that
     * reach that set of definitions.  Only does one step at a time; you'll have to
     * iterate to get the transitive closure.
     */
    public BDD calculateDefUse(BDD r_v1) {
        BDD r_v2 = r_v1.replace(r.V1toV2);
        // A: v2=v1;
        BDD b = r.A.relprod(r_v2, r.V2set);
        //System.out.println("Arguments/Return Values = "+b.satCount(r.V1set));
        // S: v1.f=v2;
        BDD c = r.S.relprod(r_v2, r.V2set); // V1xF
        r_v2.free();
        BDD d = r.vP.relprod(c, r.V1set); // H1xF
        c.free();
        BDD e = r.vP.relprod(d, r.H1set); // V1xF
        d.free();
        // L: v2=v1.f;
        BDD f = r.L.relprod(e, r.V1Fset); // V2
        f.replaceWith(r.V2toV1);
        //System.out.println("Loads/Stores = "+f.satCount(r.V1set));
        f.orWith(b);
        return f;
    }

    /** Output def-use or use-def graph in dot format.
     */
    public void defUseGraph(BDD vPrelation, boolean direction, DataOutput out) throws IOException {
        out.writeBytes("digraph \"");
        if (direction) out.writeBytes("DefUse");
        else out.writeBytes("UseDef");
        out.writeBytes("\" {\n");
        HashWorklist w = new HashWorklist(true);
        BDD c = vPrelation.id();
        int k = -1;
        Node n = null;
        for (;;) {
            while (!c.isZero()) {
                int k2 = c.scanVar(r.V1).intValue();
                Node n2 = getVariableNode(k2);
                if (w.add(n2)) {
                    String name = r.LONG_LOCATIONS ? r.findInMap(r.Vmap, k2) : n2.toString();
                    out.writeBytes("n"+k2+" [label=\""+name+"\"];\n");
                }
                if (n != null) {
                    if (direction) {
                        out.writeBytes("n"+k+
                                       " -> n"+k2+";\n");
                    } else {
                        out.writeBytes("n"+k2+
                                       " -> n"+k+";\n");
                    }
                }
                BDD q = r.V1.ithVar(k2);
                q.andWith(r.V1cdomain);
                c.applyWith(q, BDDFactory.diff);
            }
            if (w.isEmpty()) break;
            n = (Node) w.pull();
            k = getVariableIndex(n);
            BDD b = r.V1.ithVar(k);
            b.andWith(r.V1cdomain);
            c = direction?calculateDefUse(b):calculateUseDef(b);
        }
        out.writeBytes("}\n");
    }
    
    /** Prints out the chain of use-defs, starting from the given uses (V1xV1c).
     */
    public void printUseDefChain(BDD vPrelation) {
        BDD visited = r.bdd.zero();
        vPrelation = vPrelation.id();
        for (int k = 1; !vPrelation.isZero(); ++k) {
            System.out.println("Step "+k+":");
            System.out.println(vPrelation.toStringWithDomains(r.TS));
            visited.orWith(vPrelation.id());
            // A: v2=v1;
            BDD b = r.A.relprod(vPrelation, r.V1set);
            //System.out.println("Arguments/Return Values = "+b.satCount(r.V2set));
            // L: v2=v1.f;
            vPrelation.replaceWith(r.V1toV2);
            BDD c = r.L.relprod(vPrelation, r.V2set); // V1xF
            vPrelation.free();
            BDD d = r.vP.relprod(c, r.V1set); // H1xF
            c.free();
            BDD e = r.hP.relprod(d, r.H1Fset); // H2
            d.free();
            e.replaceWith(r.H2toH1);
            BDD f = r.vP.relprod(e, r.H1set); // V1
            //System.out.println("Loads/Stores = "+f.satCount(r.V1set));
            e.free();
            vPrelation = b;
            vPrelation.replaceWith(r.V2toV1);
            vPrelation.orWith(f);
            vPrelation.applyWith(visited.id(), BDDFactory.diff);
        }
    }
    
    public void printDefUseChain(BDD vPrelation) {
        BDD visited = r.bdd.zero();
        vPrelation = vPrelation.id();
        for (int k = 1; !vPrelation.isZero(); ++k) {
            System.out.println("Step "+k+":");
            System.out.println(vPrelation.toStringWithDomains(r.TS));
            visited.orWith(vPrelation.id());
            // A: v2=v1;
            BDD b = r.A.relprod(vPrelation, r.V2set);
            //System.out.println("Arguments/Return Values = "+b.satCount(r.V2set));
            // L: v2=v1.f;
            vPrelation.replaceWith(r.V2toV1);
            BDD c = r.L.relprod(vPrelation, r.V1set); // V2xF
            vPrelation.free();
            BDD d = r.vP.relprod(c, r.V2set); // H1xF
            c.free();
            BDD e = r.hP.relprod(d, r.H2Fset); // H1
            d.free();
            e.replaceWith(r.H1toH2);
            BDD f = r.vP.relprod(e, r.H2set); // V1
            //System.out.println("Loads/Stores = "+f.satCount(r.V1set));
            e.free();
            vPrelation = b;
            vPrelation.replaceWith(r.V1toV2);
            vPrelation.orWith(f);
            vPrelation.applyWith(visited.id(), BDDFactory.diff);
        }
    }
    
    /** Starting from a method with a context (MxV1c), calculate the set of
     * transitively-reachable variables (V1xV1c).
     */
    public BDD getReachableVars(BDD method_plus_context0) {
        //System.out.println("Method = "+method_plus_context0.toStringWithDomains());
        BDD result = r.bdd.zero();
        BDD allInvokes = r.mI.exist(r.Nset);
        BDD new_m = method_plus_context0.id();
        BDD V2cIset = r.Iset.and(r.V2cset);
        BDD IEcs = (r.CONTEXT_SENSITIVE || r.OBJECT_SENSITIVE) ? r.IEcs : r.IE;
        for (int k=1; ; ++k) {
            //System.out.println("Iteration "+k);
            BDD vars = new_m.relprod(r.mV, r.Mset); // V1cxM x MxV1 = V1cxV1
            result.orWith(vars);
            //System.out.println("Iteration "+k + "result: " + result.toStringWithDomains());
            BDD invokes = new_m.relprod(allInvokes, r.Mset); // V1cxM x MxI = V1cxI
            invokes.replaceWith(r.V1ctoV2c); // V2cxI
            BDD methods = invokes.relprod(IEcs, V2cIset); // V2cxI x V2cxIxV1cxM = V1cxM
            new_m.orWith(methods);
            new_m.applyWith(method_plus_context0.id(), BDDFactory.diff);
            if (new_m.isZero()) break;
            method_plus_context0.orWith(new_m.id());
        }
        return result;
    }
    
    /** Given a starting method and a context (MxV1c), calculate the transitive mod
     * set (H1xH1cxF). */
    public BDD getTransitiveModSet(BDD method_plus_context0) {
        BDD reachableVars = getReachableVars(method_plus_context0); // V1xV1c
        BDD stores = r.S.relprod(reachableVars, r.V2set);           // V1xV1c x V1xV1cxFxV2xV2c = V1xV1cxF
        BDD result = stores.relprod(r.vP, r.V1set);                 // V1xV1cxF x V1xV1cxH1xH1c = H1xH1cxF
        return result;
    }
    
    /** Given a starting method and a context (MxV1c), calculate the transitive ref
     * set (H1xH1cxF). */
    public BDD getTransitiveRefSet(BDD method_plus_context0) {
        BDD reachableVars = getReachableVars(method_plus_context0); // V1xV1c
        BDD loads = r.L.relprod(reachableVars, r.V2set);            // V1xV1c x V1xV1cxFxV2xV2c = V1xV1cxF
        BDD result = loads.relprod(r.vP, r.V1set);                  // V1xV1cxF x V1xV1cxH1xH1c = H1xH1cxF
        return result;
    }
    
    /** Return the set of thread-local objects (H1xH1c).
     */
    public BDD getThreadLocalObjects() {
        jq_NameAndDesc main_nd = new jq_NameAndDesc("main", "([Ljava/lang/String;)V");
        jq_Method main = null;
        for (Iterator i = r.Mmap.iterator(); i.hasNext(); ) {
            jq_Method m = (jq_Method) i.next();
            if (main_nd.equals(m.getNameAndDesc())) {
                main = m;
                System.out.println("Using main() method: "+main);
                break;
            }
        }
        BDD allObjects = r.bdd.zero();
        BDD sharedObjects = r.bdd.zero();
        if (main != null) {
            int M_i = r.Mmap.get(main);
            BDD m = r.M.ithVar(M_i);
            m.andWith(r.V1c[0].ithVar(0));
            System.out.println("Main: "+m.toStringWithDomains());
            BDD b = getReachableVars(m);
            m.free();
            System.out.println("Reachable vars: "+b.satCount(r.V1set));
            BDD b2 = b.relprod(r.vP, r.V1set);
            b.free();
            System.out.println("Reachable objects: "+b2.satCount(r.H1set));
            allObjects.orWith(b2);
        }
        for (Iterator i = PA.thread_runs.keySet().iterator(); i.hasNext(); ) {
            jq_Method run = (jq_Method) i.next();
            int M_i = r.Mmap.get(run);
            Set t_runs = (Set) PA.thread_runs.get(run);
            if (t_runs == null) {
                System.out.println("Unknown run() method: "+run);
                continue;
            }
            Iterator k = t_runs.iterator();
            for (int j = 0; k.hasNext(); ++j) {
                Node q = (Node) k.next();
                BDD m = r.M.ithVar(M_i);
                m.andWith(r.V1c[0].ithVar(j));
                System.out.println("Thread: "+m.toStringWithDomains()+" Object: "+q);
                BDD b = getReachableVars(m);
                m.free();
                System.out.println("Reachable vars: "+b.satCount(r.V1set));
                BDD b2 = b.relprod(r.vP, r.V1set);
                b.free();
                System.out.println("Reachable objects: "+b2.satCount(r.H1set));
                BDD b3 = allObjects.and(b2);
                System.out.println("Shared objects: "+b3.satCount(r.H1set));
                sharedObjects.orWith(b3);
                allObjects.orWith(b2);
            }
        }
        
        System.out.println("All shared objects: "+sharedObjects.satCount(r.H1set));
        allObjects.applyWith(sharedObjects, BDDFactory.diff);
        System.out.println("All local objects: "+allObjects.satCount(r.H1set));
        
        return allObjects;
    }
    
    BDDDomain H3;
    BDDPairing H1toH3, H3toH1;
    BDD H3set;
    
    public void initializeExtraDomains() {
        if (H3 == null) {
            H3 = r.makeDomain("H3", r.H_BITS);
            H1toH3 = r.bdd.makePair(r.H1, H3);
            H3toH1 = r.bdd.makePair(H3, r.H1);
            H3set = H3.set();
        }
    }
    
    public BDD getHashcodeTakenVars() {
        jq_NameAndDesc nd = new jq_NameAndDesc("hashCode", "()I");
        jq_Method m = PrimordialClassLoader.getJavaLangObject().getDeclaredInstanceMethod(nd);
        BDD m_bdd = r.M.ithVar(r.Mmap.get(m));
        BDD invokes = r.IE.relprod(m_bdd, r.Mset);
        invokes.andWith(r.Z.ithVar(0));
        System.out.println("Invokes: "+invokes.toStringWithDomains());
        BDD bar = r.actual.relprod(invokes, r.Iset.and(r.Zset));
        System.out.println("Actual: "+bar.toStringWithDomains());
        bar.replaceWith(r.V2toV1);
        
        nd = new jq_NameAndDesc("identityHashCode", "(Ljava/lang/Object;)I");
        m = PrimordialClassLoader.getJavaLangSystem().getDeclaredStaticMethod(nd);
        m_bdd = r.M.ithVar(r.Mmap.get(m));
        invokes = r.IE.relprod(m_bdd, r.Mset);
        invokes.andWith(r.Z.ithVar(1));
        System.out.println("Invokes: "+invokes.toStringWithDomains());
        BDD bar2 = r.actual.relprod(invokes, r.Iset.and(r.Zset));
        System.out.println("Actual: "+bar2.toStringWithDomains());
        bar2.replaceWith(r.V2toV1);
        bar.orWith(bar2);
        
        if (r.CONTEXT_SENSITIVE || r.OBJECT_SENSITIVE) bar.andWith(r.V1cset);
        return bar;
    }
    
    public BDD getEncapsulatedHeapObjects() {
        initializeExtraDomains();
        
        // find objects that are pointed to by only one object.
        BDD hP_ci = r.hP.exist(r.H1cH2cset).exist(r.Fset);
        BDD set = r.H1.set().and(r.H2.set());
        
        BDD one_to_one = r.H1.buildEquals(H3).andWith(r.H2.domain());
        BDD my_set = r.H1.set().andWith(H3.set());
        
        BDD b = hP_ci.replace(H1toH3); // H3xH2
        b.andWith(hP_ci.id());     // H1xH3xH2
        // find when H1=H3 is the ONLY relation
        BDD a = b.applyAll(one_to_one, BDDFactory.imp, my_set);
        b.free(); one_to_one.free();
        a.andWith(hP_ci.id());
        
        System.out.println("Number = "+a.satCount(set));
        
        BDD result = r.bdd.zero();
        int count = 0;
        for (int i = 0; i < r.Hmap.size(); ++i) {
            BDD x = r.H2.ithVar(i);
            BDD y = hP_ci.restrict(x);
            if (y.satCount(r.H1.set()) == 1.0) {
                ++count;
                result.orWith(x.and(y));
            }
            x.free();
            y.free();
        }
        
        System.out.println("Number = "+result.satCount(set));
        
        if (!a.equals(result)) {
            System.out.println("a has extra: "+a.apply(result, BDDFactory.diff).toStringWithDomains());
            System.out.println("a is missing: "+result.apply(a, BDDFactory.diff).toStringWithDomains());
        }
        
        return result;
    }

    /** Compute all types that override equals() */
    public TypedBDD typesThatOverrideEquals() {
        jq_NameAndDesc equals_nd = new jq_NameAndDesc("equals", "(Ljava/lang/Object;)Z");
        jq_Method equals_m = PrimordialClassLoader.getJavaLangObject().getDeclaredInstanceMethod(equals_nd);
        BDD n_bdd = r.N.ithVar(getNameIndex(equals_m));
        BDD t1 = r.cha.restrict(n_bdd);
        BDD t2 = t1.exist(r.Mset);
        t1.free();
        return (TypedBDD)t2;    // T2
    }
    
    // Two objects are indistinguishably typed if they are the same type and
    // each of their fields points to objects that are indistinguishably typed.
    public BDD findIndistinguishablyTypedObjects() {
        initializeExtraDomains();
        BDD t = r.hT.replace(H1toH3); // H3xT2
        BDD sameType = t.relprod(r.hT, r.T2set); // H1xT2 x T2xH3 = H1xH3
        BDD stringType = r.T2.ithVar(r.Tmap.get(PrimordialClassLoader.getJavaLangString()));
        BDD strings = r.hT.restrict(stringType);
        BDD same = r.H1.buildEquals(H3);
        BDD relationsToCheck = sameType.id();
        for (;;) {
            // eliminate things that don't point to anything.
            relationsToCheck.andWith(r.hP.exist(r.H2Fset));
            // eliminate strings
            relationsToCheck.andWith(strings.not());
            // eliminate self-relations
            relationsToCheck.andWith(same.not());
            
            BDD recheck = r.bdd.zero();
            int outer = 0;
            
            while (!relationsToCheck.isZero()) {
                System.out.print((long)relationsToCheck.satCount(r.H1set.and(H3set))+" relations remaining.        \r");
                ++outer;
                BDD h_a = relationsToCheck.satOne(r.H1set, false);
                BDD h1 = h_a.exist(H3set); h_a.free();
                //System.out.println(h1.toStringWithDomains(r.TS));
                BDD foo = relationsToCheck.restrict(h1); // H3
                relationsToCheck.applyWith(h1.and(H3.domain()), BDDFactory.diff);
                
                foo.replaceWith(H3toH1); // H1
                BDD a1 = r.hP.relprod(h1, r.H1set); // FxH2
                BDD a3 = r.hP.and(foo); // H1xFxH2
                
                BDD ok = a3.relprod(a1, r.H2Fset); // H1
                //System.out.println("Match exactly: "+ok.toStringWithDomains(r.TS));
                foo.applyWith(ok, BDDFactory.diff);
                
                if (!foo.isZero()) {
                    BDD sameType23 = sameType.replace(r.H1toH2);
                    BDD r1 = a1.relprod(sameType23, r.H2set); // FxH3
                    BDD r3 = a3.relprod(sameType23, r.H2set); // H1xFxH3
    
                    ok = r3.relprod(r1, r.Fset.and(H3set)); // H1
                    //System.out.println("Match approx: "+ok.toStringWithDomains(r.TS));
                    foo.applyWith(ok, BDDFactory.diff);
                    
                    if (!foo.isZero()) {
                        //System.out.println("Do not match: "+foo.toStringWithDomains(r.TS));
                        BDD n = foo.replace(H1toH3).and(h1);
                        n.orWith(h1.replace(H1toH3).and(foo));
                        sameType.applyWith(n.id(), BDDFactory.diff);
                        relationsToCheck.applyWith(n.id(), BDDFactory.diff);
                        
                        recheck.orWith(h1.id());
                        recheck.orWith(foo.id());
                    }
                }
            }
            //System.out.println("Outer: "+outer);
            //System.out.println("Inner: "+inner);
            System.out.println("Recheck: "+(long)recheck.satCount(r.H1set)+"                ");
            if (recheck.isZero()) break;
            recheck.replaceWith(r.H1toH2);
            BDD checkRelations = recheck.relprod(r.hP, r.H2Fset); // H1
            checkRelations.andWith(sameType.id());
            relationsToCheck.orWith(checkRelations);
        }
        
        
        BDD iter = sameType.id();
        int i = 0;
        while (!iter.isZero()) {
            ++i;
            BDD foo = iter.satOne(r.H1set, false).exist(H3set);
            BDD set = sameType.restrict(foo); // H3
            set.replaceWith(H3toH1); // H1
            System.out.println("Equivalence class "+i+": "+set.toStringWithDomains(r.TS));
            iter.applyWith(set.andWith(H3.domain()), BDDFactory.diff);
        }
        int nObjects = (int) r.hT.exist(r.T2set).satCount(r.H1set);
        int nTypes = (int) r.hT.exist(r.H1set).satCount(r.T2set);
        System.out.println(nObjects+" objects (of "+nTypes+" different types) put into "+i+" equivalence classes.");
        return sameType;
    }
    
    // I -> V1xV1cxH1xH1cxIxZ
    public TypedBDD showArguments(TypedBDD isites) {
        return (TypedBDD)r.actual.and(isites).replaceWith(r.bdd.makePair(r.V2, r.V1)).and(r.vP);
    }

    /***** STATISTICS *****/
    
    public void printStats() throws IOException {
        System.out.println("Number of types="+r.Tmap.size());
        System.out.println("Number of methods="+r.Mmap.size());
        int bytecodes = 0;
        for (Iterator i = r.Mmap.iterator(); i.hasNext(); ) {
            jq_Method m = (jq_Method) i.next();
            if (m.getBytecode() != null)
                bytecodes += m.getBytecode().length;
        }
        System.out.println("Number of bytecodes="+bytecodes);
        System.out.println("Number of virtual call sites="+r.Imap.size());
        System.out.println("Number of virtual call names="+r.Nmap.size());
        System.out.println("Number of variables="+r.Vmap.size());
        System.out.println("Number of heap objects="+r.Hmap.size());
        System.out.println("Number of fields="+r.Fmap.size());
        //System.out.println("Number of stores="+r.S.satCount(r.V1.set().andWith(r.F.set()).andWith(r.V2.set())));
        //System.out.println("Number of loads="+r.L.satCount(r.V1.set().andWith(r.F.set()).andWith(r.V2.set())));
        System.out.println("Number of callgraph edges="+r.IE.satCount(r.Iset.and(r.Mset)));
        
        BDD all_v1 = r.vP.exist(r.H1set);
        all_v1.orWith(r.A.exist(r.V2set));
        all_v1.orWith(r.A.exist(r.V1set).replaceWith(r.V2toV1));
        all_v1.orWith(r.L.exist(r.V2Fset));
        all_v1.orWith(r.L.exist(r.V1Fset).replaceWith(r.V2toV1));
        
        double v1h1_count = r.vP.satCount(r.V1H1set);
        //int v1_count = r.Vmap.size();
        double v1_count = all_v1.satCount(r.V1set);
        System.out.println("Points-to: "+v1h1_count+" / "+v1_count+" = "+(double)v1h1_count/v1_count);
        
        BDD all_h1 = r.vP.exist(r.V1set);
        
        double h1fh2_count = r.hP.satCount(r.H1FH2set);
        double h1f_count = r.hP.exist(r.H2set).satCount(r.H1Fset);
        //int h1_count = r.Hmap.size();
        double h1_count = all_h1.satCount(r.H1set);
        //System.out.println("Heap object points to (each field): "+h1fh2_count+" / "+h1f_count+" = "+(double)h1fh2_count/h1f_count);
        System.out.println("Heap object points to (all fields): "+h1fh2_count+" / "+h1_count+" = "+(double)h1fh2_count/h1_count);
        
        {
            long heapConnectSum = 0L; int heapConnect = 0;
            long heapPointsToSum = 0L; long heapPointsTo = 0L;
            for (int i = 0; i < r.Hmap.size() && i < 4000; ++i) {
                BDD b = r.H1.ithVar(i);
                if (r.CONTEXT_SENSITIVE) b.andWith(r.H1cdomain);
                BDD c = calculateHeapConnectivity(b);
                heapConnectSum += c.satCount(r.H1set);
                ++heapConnect;
                c.free();
                BDD d = r.hP.relprod(b, r.H1set);
                heapPointsToSum += d.satCount(r.H2Fset);
                heapPointsTo += d.exist(r.H2set).satCount(r.Fset);
                b.free();
                //System.out.print("Heap connectivity: "+heapConnectSum+" / "+heapConnect+" = "+(double)heapConnectSum/heapConnect+"\r");
            }
            System.out.println("Heap connectivity: "+heapConnectSum+" / "+heapConnect+" = "+(double)heapConnectSum/heapConnect+"           ");
            System.out.println("Heap chain length: "+heapConnectivitySteps+" / "+heapConnectivityQueries+" = "+(double)heapConnectivitySteps/heapConnectivityQueries);
            System.out.println("Heap points-to, per field: "+heapPointsToSum+" / "+heapPointsTo+" = "+(double)heapPointsToSum/heapPointsTo);
        }
        
        {
            BDD fh2 = r.hP.exist(r.H1set); // FxH2
            int singleTypeFields = 0, singleObjectFields = 0, unusedFields = 0, refinedTypeFields = 0;
            Set polyClasses = new HashSet();
            for (int i = 0; i < r.Fmap.size(); ++i) {
                BDD b = r.F.ithVar(i);
                BDD c = fh2.restrict(b); // H2
                if (c.isZero()) {
                    ++unusedFields;
                    continue;
                }
                c.replaceWith(r.H2toH1); // H1
                if (c.satCount(r.H1set) == 1.0) {
                    ++singleObjectFields;
                }
                BDD d = c.relprod(r.hT, r.H1set); // T2
                jq_Field f = (jq_Field) r.Fmap.get(i);
                if (d.satCount(r.T2set) == 1.0) {
                    ++singleTypeFields;
                } else {
                    if (f != null && !f.isStatic()) {
                        polyClasses.add(f.getDeclaringClass());
                    }
                }
                BDD e = calculateCommonSupertype(d); // T1
                if (f != null) {
                    int T_i = r.Tmap.get(f.getType());
                    BDD g = r.T1.ithVar(T_i);
                    if (!e.equals(g)) {
                        e.replaceWith(r.T1toT2);
                        if (e.andWith(g).and(r.aT).isZero()) {
                            System.out.println("Field "+f);
                            System.out.println(" Declared: "+f.getType()+" Computed: "+e.toStringWithDomains(r.TS));
                        } else {
                            ++refinedTypeFields;
                        }
                    }
                    g.free();
                }
                d.free();
                e.free();
                c.free();
                b.free();
            }
            System.out.println("Refined-type fields: "+refinedTypeFields+" / "+r.Fmap.size()+" = "+(double)refinedTypeFields/r.Fmap.size());
            System.out.println("Single-type fields: "+singleTypeFields+" / "+r.Fmap.size()+" = "+(double)singleTypeFields/r.Fmap.size());
            System.out.println("Single-object fields: "+singleObjectFields+" / "+r.Fmap.size()+" = "+(double)singleObjectFields/r.Fmap.size());
            System.out.println("Unused fields: "+unusedFields+" / "+r.Fmap.size()+" = "+(double)unusedFields/r.Fmap.size());
            System.out.println("Poly classes: "+polyClasses.size());
            
            DataOutputStream out = null;
            try {
                out = new DataOutputStream(new FileOutputStream("polyclasses"));
                for (Iterator i = polyClasses.iterator(); i.hasNext(); ) {
                    jq_Class c = (jq_Class) i.next();
                    out.writeBytes(c.getJDKName()+"\n");
                }
            } finally {
                if (out != null) out.close();
            }
        }
        
        if (r.CONTEXT_SENSITIVE) {
            System.out.println("Thread-local objects: "+countThreadLocalObjects());
        }
        
        {
            BDD h1 = this.getHashcodeTakenVars();
            BDD h2 = h1.relprod(r.vP, r.V1set);
            System.out.println("Hashcode taken objects: "+h2.satCount(r.H1set));
            h2 = h2.exist(r.H1cset);
            System.out.println("Hashcode taken objects (no context): "+h2.satCount(r.H1.set()));
            System.out.println("Hashcode never taken objects (no context): "+(r.Hmap.size()-h2.satCount(r.H1.set())));
        }
        
        {
            BDD h1 = r.sync;
            if (r.CONTEXT_SENSITIVE || r.OBJECT_SENSITIVE) h1.andWith(r.H1cdomain);
            BDD h2 = h1.relprod(r.vP, r.V1set);
            System.out.println("Locked objects: "+h2.satCount(r.H1set));
            h2 = h2.exist(r.H1cset);
            System.out.println("Locked objects (no context): "+h2.satCount(r.H1.set()));
            System.out.println("Never locked objects (no context): "+(r.Hmap.size()-h2.satCount(r.H1.set())));
        }
        
        {
            BDD vCalls = r.mI.exist(r.Mset.and(r.Nset));
            double d = vCalls.satCount(r.Iset);
            System.out.println("Virtual call sites: "+d);
            double e = r.IE.satCount(r.IMset);
            System.out.println("Average vcall targets = "+e+" / "+d+" = "+e/d);
        }
        
        if (false)
        {
            long sum = 0L; int n = 0;
            for (int i = 0; i < r.Vmap.size(); ++i) {
                BDD b = r.V1.ithVar(i);
                if (r.CONTEXT_SENSITIVE || r.OBJECT_SENSITIVE) b.andWith(r.V1cdomain);
                int result = countTransitiveReachingDefs(b);
                sum += result;
                ++n;
                System.out.print("Reaching defs: "+sum+" / "+n+" = "+(double)sum/n+"\r");
            }
            System.out.println("Reaching defs: "+sum+" / "+n+" = "+(double)sum/n);
        }
    }
    
    public int countPointsTo(BDD v) {
        BDD p = r.vP.relprod(v, r.V1set);
        double result = p.satCount(r.H1.set());
        return (int) result;
    }
    
    public int countTransitiveReachingDefs(BDD vPrelation) {
        BDD visited = r.bdd.zero();
        vPrelation = vPrelation.id(); // V1
        for (int k = 1; !vPrelation.isZero(); ++k) {
            visited.orWith(vPrelation.id()); // V1
            // A: v2=v1;
            BDD b = r.A.relprod(vPrelation, r.V1set); // V2
            // L: v2=v1.f;
            vPrelation.replaceWith(r.V1toV2); // V2
            BDD c = r.L.relprod(vPrelation, r.V2set); // V1xF
            vPrelation.free();
            BDD d = r.vP.relprod(c, r.V1set); // H1xF
            c.free();
            BDD e = r.hP.relprod(d, r.H1Fset); // H2
            d.free();
            e.replaceWith(r.H2toH1);
            BDD f = r.vP.relprod(e, r.H1set); // V1
            e.free();
            vPrelation = b;
            vPrelation.replaceWith(r.V2toV1);
            vPrelation.orWith(f);
            vPrelation.applyWith(visited.id(), BDDFactory.diff);
        }
        double result = visited.satCount(r.V1.set());
        return (int) result;
    }
    
    public int countThreadLocalObjects() {
        BDD b = getThreadLocalObjects();
        double result = b.satCount(r.H1set);
        return (int) result;
    }
    
    /**
     * Compute the set of results based on the BDD results.
     * */
    public Set getCallTargets(QuadProgramLocation invoke) {
        //Assert._assert(invoke.getOperator() instanceof Operator.Invoke);
        ProgramLocation loc = LoadedCallGraph.mapCall(invoke); invoke = null;
        
        if(loc.isSingleTarget()) {
            Set result = new LinearSet();
            result.add(loc.getTargetMethod());
            //System.err.println("loc: " + loc);
            
            return result;
        }else 
        if(!r.Imap.contains(loc)){
            System.err.println("No call information about " + loc.toString());
            return new LinearSet();
        }
        
        BDD t1 = r.actual.restrict(r.Z.ithVar(0));  // IxV2
        t1.replaceWith(r.bdd.makePair(r.V2, r.V1)); // IxV1
        BDD t2 = r.mI.exist(r.Mset);                // IxN
        BDD t3 = t1.and(t2);                        // IxV1 & IxN = IxV1xN
        t1.free(); t2.free();
        BDD t4 = t3.relprod(r.vP, r.V1set);         // IxV1xN x V1cxV1xH1cxH1 = IxH1cxH1xN
        BDD t5 = t4.relprod(r.hT, r.H1set);         // IxH1cxH1xN x H1xT2 = IxT2xN
        t4.free();
        BDD t6 = t5.relprod(r.cha, r.T2Nset);       // IxT2xN x T2xNxM = IxM
        t5.free();
    
        r.IE.orWith(t6.id());
    
        BDD t7;
        if ((r.CONTEXT_SENSITIVE || r.THREAD_SENSITIVE)) {
            // Add the context for the new call graph edges.
            t6.andWith(r.IEfilter.id());
            r.IEcs.orWith(t6.id());
            t7 = t6.exist(r.V1cV2cset);
            t6.free();
        }else {
            t7 = t6;
        }        
        
        // t7 is the result we need in I X M
        int oldSize = r.Imap.size();

        int I_idx = r.Imap.get(loc);
        //Assert._assert(r.Imap.size() == oldSize, "old size: " + oldSize + ", new size: " + r.Imap.size());
        BDD isite = r.I.ithVar(I_idx);
        
        //System.out.println("Site: " + toString((TypedBDD)isite, -1));
        t7.restrictWith(isite);
        
        System.err.println(((TypedBDD)t7).getDomainSet().toString() + ": " + t7.satCount());
        
        //System.out.println("Target methods of " + loc + " = " + toString((TypedBDD)t6, -1)); //t6.toStringWithDomains());        
        
        return new PACallGraph.BDDSet(t7, r.M, r.Mmap);
    }
    
    public Set getCallTargets2(ProgramLocation invoke) {
        Collection c = this.cg.getTargetMethods(invoke);
        
        return new HashSet(c);
    }
    
    public Set mod(QuadProgramLocation invoke, BasicBlock bb) {
        if (invoke.isCall()) {
            ProgramLocation loc = LoadedCallGraph.mapCall(invoke);
            //Assert._assert(r.Imap.contains(loc), "No information about " + loc.toString());
            if(!r.Imap.contains(loc)){
                //System.err.println("No mod information about " + loc.toString() + "; Imap has " + r.Imap.size() + " elements \n");
                return null;
            }
            int I_i = r.Imap.get(loc);
            BDD i   = r.I.ithVar(I_i);
            BDD m_c = r.IEcs.relprod(i, r.V2cset.and(r.Iset));
            // get transitive mod set for this particular method call   
            BDD s   = getTransitiveModSet(m_c);             
            BDD q   = s.exist(r.H1cset);

            return new HeapLocationSet(q);
        } else {
            // in case this is a store, need to get the results for that store
            jq_Method m = invoke.getMethod();
            Quad q = ((QuadProgramLocation) invoke).getQuad();
            Assert._assert(bb != null);
            
            return mod(m, bb, q);
        }        
    }
    
    public Set ref(QuadProgramLocation invoke, BasicBlock bb) {
        if (invoke.isCall()) {
            ProgramLocation loc = LoadedCallGraph.mapCall(invoke);
            //Assert._assert(r.Imap.contains(loc), "No information about " + loc.toString());
            if(!r.Imap.contains(loc)){
                //System.err.println("No ref information about " + invoke.toString() + "; Imap has " + r.Imap.size() + " elements \n");
                return null;
            }
            int I_i = r.Imap.get(invoke);
            BDD i   = r.I.ithVar(I_i);
            BDD m_c = r.IEcs.relprod(i, r.V2cset.and(r.Iset));
            BDD s   = getTransitiveRefSet(m_c);
            BDD q   = s.exist(r.H1cset);
                
            return new HeapLocationSet(q);
        }else {
            /*
            // in case this is a load, need to get the results for that store
            jq_Method m = invoke.getMethod();
            Quad q = ((QuadProgramLocation) invoke).getQuad();
            Assert._assert(bb != null);
    
            return ref(m, bb, q);
            */
            return null;
        }
    }
    
    public Set mod(jq_Method m, BasicBlock bb, Quad quad) {
        MethodSummary ms = MethodSummary.getSummary(CodeCache.getCode(m));
        Register reg;
        jq_Field f;
        if (quad.getOperator() instanceof Operator.AStore) {
            reg = ((RegisterOperand) Operator.AStore.getBase(quad)).getRegister();
            f = null;
        } else {
            Assert._assert(quad.getOperator() instanceof Operator.Putfield);
            reg = ((RegisterOperand) Operator.Putfield.getBase(quad)).getRegister();
            f = Operator.Putfield.getField(quad).getField();
        }
        Collection c = ms.getRegisterAtLocation(bb, quad, reg);
        BDD b = r.bdd.zero();
        for (Iterator i = c.iterator(); i.hasNext(); ) {
            Node n = (Node) i.next();
            Assert._assert(r.Vmap.contains(n));
            int V_i = r.Vmap.get(n);
            b.orWith(r.V1.ithVar(V_i));
        }
        b.andWith(r.V1cdomain);
        BDD s = r.vP.relprod(b, r.V1set);
        BDD q = s.exist(r.H1cset);
        q.andWith(r.F.ithVar(r.Fmap.get(f)));
        return new HeapLocationSet(q);
    }
    
    public boolean isAliased(SSALocation a, SSALocation b) {
        // (1, .f)   (1, .f)   ==>   true
        // (1, .f)   (2, .f)   ==>   false
        return a.equals(b);
    }

    /* (non-Javadoc)
     * @see joeq.Compiler.Analysis.IPA.PointerAnalysisResults#getAliases(joeq.Class.jq_Method, joeq.Compiler.Analysis.IPA.SSALocation)
     */
    public Set/*<ContextSet.ContextLocationPair>*/ getAliases(jq_Method method, SSALocation loc) {
        return Collections.EMPTY_SET;
    }

    /* (non-Javadoc)
     * @see joeq.Compiler.Analysis.IPA.PointerAnalysisResults#hasAliases(joeq.Class.jq_Method, Compiler.Analysis.IPA.SSALocation, joeq.Compiler.Analysis.IPA.ContextSet)
     */
    public boolean hasAliases(jq_Method method, SSALocation loc, ContextSet contextSet) {
        return false;
    }

    /* (non-Javadoc)
     * @see joeq.Compiler.Analysis.IPA.PointerAnalysisResults#hasAliases(joeq.Class.jq_Method, Compiler.Analysis.IPA.SSALocation)
     */
    public boolean hasAliases(jq_Method method, SSALocation loc) {
        return false;
    }
    
    public class HeapLocationSet extends AbstractSet {

        BDD heapLocations; // H1 x F
        
        HeapLocationSet(BDD b) {
            this.heapLocations = b;
        }
        
        /* (non-Javadoc)
         * @see java.util.AbstractCollection#size()
         */
        public int size() {
            return (int) heapLocations.satCount(r.H1.set().and(r.Fset));
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#iterator()
         */
        public Iterator iterator() {
            final Iterator i = heapLocations.iterator(r.H1.set().and(r.Fset));
            return new UnmodifiableIterator() {
                public boolean hasNext() {
                    return i.hasNext();
                }
                public Object next() {
                    BDD b = (BDD) i.next();
                    int h = b.scanVar(r.H1).intValue();
                    int f = b.scanVar(r.F).intValue();
                    Node hn = (Node) r.Hmap.get(h);
                    jq_Field fn = (jq_Field) r.Fmap.get(f);
                    return new HeapLocation(hn, fn);
                }
            };
        }
        
    }
    
    public static class HeapLocation implements SSALocation {
        Node n;      // allocation site
        jq_Field f;  // field
        
        public static class FACTORY {
            static HashMap _locationMap;
            HeapLocation createHeapLocation(Node n, jq_Field f){
                Pair pair = new Pair(n, f); 
                HeapLocation loc = (HeapLocation) _locationMap.get(pair); 
                if(loc == null){
                    loc = new HeapLocation(n, f);
                    _locationMap.put(pair, loc);
                }
                return loc;
            }
        }
        
        private HeapLocation(Node n, jq_Field f) {
            this.n = n;
            this.f = f;
            
            Assert._assert(n != null);            
        }
        
        public boolean equals(HeapLocation o) {
            return n.equals(o.n) && f == o.f;
        }
        
        public boolean equals(Object o) {
            return equals((HeapLocation) o);
        }
        
        public int hashCode() {
            int x = n.hashCode();
            if (f != null) x ^= f.hashCode();
            return x;
        }
        
        public String toString() {
            String fname = (f == null) ? "[]" : "."+f.getName().toString();
            return n.toString_short()+fname;
        }
        
        public String toString(PA r) {
            String fname = (f == null) ? "[]" : "."+f.getName().toString();
            //return n.toString_short()+fname;
            //return r.findInMap(r.Vmap, r.getHeapIndex(n)) + fname;
            return r.longForm(n) + fname;
        }
    }   
    
    /**
     * Given a typedbdd that contains variables, callsites, or heap objects,
     * display the source code for each item.
     *
     * Works for ProgramLocations (callsites), but only for those variable and heap 
     * nodes that have implemented getLocation() methods.
     *
     * @see joeq.Util.IO.SourceLister
     */
    public void showSource(TypedBDD b, final int before, final int after) {
        final PA fr = r;
        // construct a new one every time, uses defaults in joeq.Util.IO.
        final SourceLister sourceLister = new SourceLister();

        BDD.BDDToString ts = new BDD.BDDToString() {
            String findInMap(IndexedMap map, int i) {
                if (i >= map.size())
                    return "not in map\n";
                Object o = map.get(i);
                if (o instanceof ProgramLocation)
                    return sourceLister.list((ProgramLocation)o, true, before, after);
                else {
                    Class c = o.getClass();
                    try {
                        Method m = c.getMethod("getLocation", new Class[] {});
                        ProgramLocation pl = (ProgramLocation)m.invoke(o, null);
                        if (pl != null)
                            return sourceLister.list(pl, /*withnumbers=*/true, before, after); 
                    } catch (NoSuchMethodException _) {
                    } catch (InvocationTargetException _) {
                    } catch (IllegalAccessException _) {
                    }
                }
                return "no source available for "+o+"\n";
            }
            public String elementName(int i, long j) {
                String n = j+"\n";
                switch (i) {
                    case 0: // fallthrough
                    case 1: return n+findInMap(fr.Vmap, (int)j);
                    case 2: return n+findInMap(fr.Imap, (int)j);
                    case 3: // fallthrough
                    case 4: return n+findInMap(fr.Hmap, (int)j);
                    default: return n+"does not implement map index#"+i+"\n";
                }
            }
            public String elementNames(int i, long j, long k) { 
                return "big sets not implemented"; 
            }
        };

        for (Iterator i = b.iterator(); i.hasNext(); ) {
            TypedBDD bb = (TypedBDD)i.next();
            System.out.println(bb.toStringWithDomains(ts));
        }
    }

    /**
     * Compute coefficient of concentration of SCC-sizes for given path numbering.
     * 0 means there is no SCC of size greater than 1.
     * 1 mean all nodes are in one SCC.
     */
    public void computeGini(PathNumbering pn) {
        if (!(pn instanceof SCCPathNumbering)) {
            System.out.println(pn.getClass() + " is not using a SCC numbering");
            return;
        }
        SCCTopSortedGraph graph = ((SCCPathNumbering)pn).getSCCGraph();
        List sccs = graph.list();
        int []sccs_sizes = new int[sccs.size()];
        int n = 0;
        for (int i = 0; i < sccs.size(); i++) {
            SCComponent scc = (SCComponent)sccs.get(i);
            sccs_sizes[i] = scc.size();
            n += scc.size();
        }
        int x[] = new int[n];
        Arrays.sort(sccs_sizes);
        System.arraycopy(sccs_sizes, 0, x, n - sccs_sizes.length, sccs_sizes.length);
        double gini = 0.0;
        // for sorted sets, the gini is g=\sum_{i=1}^n (2*i-n-1)*x_i/n^2
        for (int i = 0; i < n; i++) {
            gini += (2*(i+1)-n-1)*x[i];
        }
        // correct for bias by multiplying by n/(n-1)
        gini = gini / n / (n - 1);

        System.out.println("Gini-Coefficient is " + gini);
        int PRINTMAX = 20;
        System.out.print(PRINTMAX + " largest SCCs are :");
        for (int i = sccs_sizes.length - 1; i>=0 && i>sccs_sizes.length-1-PRINTMAX; --i) {
            System.out.print(" " + sccs_sizes[i]);
        }
        System.out.println();
    }

    public void compareCallingContexts(jq_Method m) {
        // find callers
        int m_i = getMethodIndex(m);
        System.out.println("Calls to method "+m);
        BDD m_bdd = r.M.ithVar(m_i);
        BDD b = r.IEcs.restrict(m_bdd); // V2cxIxV1c
        BDD c = b.exist(r.V1cV2cset); // I
        for (Iterator i = c.iterator(r.Iset); i.hasNext(); ) {
            BDD i_bdd = (BDD) i.next(); // I
            int i_i = i_bdd.scanVar(r.I).intValue();
            ProgramLocation invoke = getInvoke(i_i);
            System.out.println(" Call site "+i_i+": "+invoke);
            BDD d = b.exist(r.Iset); // V2cxV1c
            BDD e = d.exist(r.V2cset); // V1c
            // check each parameter
            MethodSummary ms = MethodSummary.getSummary(m);
            for (int j = 0; j < ms.getNumOfParams(); ++j) {
                Node pn = ms.getParamNode(j);
                if (pn == null) continue;
                int p_i = getVariableIndex(pn);
                BDD f = r.V1.ithVar(p_i); // V1
                f.andWith(e.id()); // V1cxV1
                BDD g = r.vP.relprod(f, r.V1set); // H1cxH1
                BDD h = g.exist(r.H1cset);
                System.out.println("  Param "+j+": ");
                for (Iterator z = g.iterator(r.H1.set()); z.hasNext(); ) {
                    BDD x = (BDD) z.next();
                    int heap_i = x.scanVar(r.H1).intValue();
                    Node heap = getHeapNode(heap_i);
                    System.out.println("   "+heap);
                }
            }
        }
    }

    /**
     * Generic z-statistic stuff.
     * Default p0. Set with set Compiler.Analysis.PAResults.p0 0.5
     */
    public static double p0 = 0.85;

    public static double zcompute(double e1, double c1, double p_0) {
        double p_hat, q_0, n;

        q_0 = 1. - p_0;
        n = e1 + c1;
        p_hat = e1 / n;
        return (p_hat - p_0) / Math.sqrt((p_0 * q_0) / n);
    }

    public static double zcompute(double e1, double c1) {
        return zcompute(e1, c1, p0);
    }

    public CallGraph getCallGraph() {
        return this.cg;
    }
}
