// Copyright (C) 2004 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.Analysis.BDD;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import joeq.Class.jq_Field;
import joeq.Class.jq_Method;
import joeq.Compiler.Analysis.IPA.ProgramLocation;
import joeq.Compiler.Quad.CodeCache;
import joeq.Compiler.Quad.ControlFlowGraph;
import joeq.Compiler.Quad.ControlFlowGraphVisitor;
import joeq.Compiler.Quad.Operand;
import joeq.Compiler.Quad.Operator;
import joeq.Compiler.Quad.Quad;
import joeq.Compiler.Quad.QuadIterator;
import joeq.Compiler.Quad.Operand.ConstOperand;
import joeq.Compiler.Quad.Operand.FieldOperand;
import joeq.Compiler.Quad.Operand.MethodOperand;
import joeq.Compiler.Quad.Operand.RegisterOperand;
import joeq.Compiler.Quad.Operand.TargetOperand;
import joeq.Compiler.Quad.Operand.TypeOperand;
import joeq.Compiler.Quad.Operator.Invoke;
import joeq.Compiler.Quad.Operator.New;
import joeq.Compiler.Quad.Operator.NewArray;
import joeq.Compiler.Quad.RegisterFactory.Register;
import joeq.Compiler.Quad.SSA.EnterSSA;
import jwutil.collections.IndexMap;
import jwutil.util.Assert;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDDomain;
import net.sf.javabdd.BDDFactory;

/**
 * BuildBDDIR
 * 
 * @author jwhaley
 * @version $Id: BuildBDDIR.java,v 1.59 2005/03/14 05:59:28 joewhaley Exp $
 */
public class BuildBDDIR implements ControlFlowGraphVisitor {
    
    IndexMap methodMap;
    IndexMap opMap;
    IndexMap quadMap;
    IndexMap quadLineMap;
    //IndexMap regMap;
    IndexMap memberMap;
    IndexMap constantMap;
    
    Map/*ProgramLocation,Quad*/ invokeMap;
    Map/*ProgramLocation,Quad*/ allocMap;
    
    String dumpDir = System.getProperty("bdddumpdir", "");
    boolean DUMP_TUPLES = !System.getProperty("dumptuples", "yes").equals("no");

    String varOrderDesc = "method_quadxtargetxfallthrough_member_constant_opc_srcs_dest_srcNum";
    
    int methodBits = 14, quadBits = 19, opBits = 9, regBits = 7, constantBits = 14, memberBits = 15, varargsBits = 4;

    BDDFactory bdd;
    BDDDomain method, quad, opc, dest, src1, src2, constant, fallthrough, target, member, srcNum, srcs;
    BDD methodToQuad;
    BDD methodEntries;
    BDD nullConstant;
    BDD nonNullConstants;
    BDD allQuads;
    BDD currentQuad;
    
    Object theDummyObject;
    
    int totalQuads;
    
    boolean ZERO_FIELDS = !System.getProperty("zerofields", "yes").equals("no");
    boolean GLOBAL_QUAD_NUMBERS = !System.getProperty("globalquadnumber", "yes").equals("no");
    boolean SSA = !System.getProperty("ssa", "no").equals("no");
    boolean USE_SRC12 = !System.getProperty("src12", "no").equals("no");
    
    boolean ENTER_SSA;
    
    public BuildBDDIR()
    {
        bdd = BDDFactory.init(1000000, 50000);
        theDummyObject = new Object();
        methodMap = new IndexMap("method");
        methodMap.get(theDummyObject);
        method = makeDomain("method", methodBits);
        initialize();
        System.out.println("Using variable ordering "+varOrderDesc);
        int [] varOrder = bdd.makeVarOrdering(true, varOrderDesc);
        bdd.setVarOrder(varOrder);
        //bdd.setMaxIncrease(500000);
        bdd.setIncreaseFactor(2);
        ENTER_SSA = true;
    }
    
    public BuildBDDIR(BDDFactory bddFactory, BDDDomain methodDomain, IndexMap _methodMap, Object dummy)
    {
        bdd = bddFactory;
        method = methodDomain;
        methodMap = _methodMap;
        theDummyObject = dummy;
        initialize();
        int index = varOrderDesc.indexOf("method_");
        varOrderDesc = varOrderDesc.substring(0, index) + varOrderDesc.substring(index + "method_".length());
        ENTER_SSA = false;
    }
    
    protected void initialize()
    {
        if (!GLOBAL_QUAD_NUMBERS) {
            quadBits = 13;
        }
        if (SSA) {
            regBits = 11;
            varargsBits = 7;
            int index = varOrderDesc.indexOf("xtargetxfallthrough");
            varOrderDesc = varOrderDesc.substring(0, index) + varOrderDesc.substring(index + "xtargetxfallthrough".length());
            
            varOrderDesc = "method_memberxquad_constant_opc_srcs_dest_srcNum";
        }
        if (USE_SRC12) {
            int index = varOrderDesc.indexOf("_srcs");
            varOrderDesc = varOrderDesc.substring(0, index) + "_src2_src1" + varOrderDesc.substring(index);
        }
        loadOpMap();
        quadMap = new IndexMap("quad");
        quadLineMap = new IndexMap("quadloc");
        quadMap.get(theDummyObject);
        quadLineMap.get(theDummyObject);
        //regMap = new IndexMap("reg");
        memberMap = new IndexMap("member");
        memberMap.get(theDummyObject);
        constantMap = new IndexMap("constant");
        constantMap.get(theDummyObject);
        opc = makeDomain("opc", opBits);
        quad = makeDomain("quad", quadBits);
        dest = makeDomain("dest", regBits);
        if (USE_SRC12) {
            src1 = makeDomain("src1", regBits);
            src2 = makeDomain("src2", regBits);
        }
        constant = makeDomain("constant", constantBits);
        if (!SSA) {
            fallthrough = makeDomain("fallthrough", quadBits);
            target = makeDomain("target", quadBits);
        }
        member = makeDomain("member", memberBits);
        srcNum = makeDomain("srcNum", varargsBits);
        srcs = makeDomain("srcs", regBits);
        allQuads = bdd.zero();
        methodToQuad = bdd.zero();
        methodEntries = bdd.zero();
        nullConstant = bdd.zero();
        nonNullConstants = bdd.zero();
        invokeMap = new HashMap();
        allocMap = new HashMap();
    }
    
    BDDDomain makeDomain(String name, int bits) {
        Assert._assert(bits < 64);
        BDDDomain d = bdd.extDomain(new long[] { 1L << bits })[0];
        d.setName(name);
        return d;
    }
    
    void loadOpMap() {
        String fileName = "op.map";
        try {
            BufferedReader in = new BufferedReader(new FileReader(fileName));
            opMap = IndexMap.loadStringMap("op", in);
            in.close();
        } catch (IOException x) {
            System.out.println("Cannot load op map "+fileName);
            opMap = new IndexMap("op");
            opMap.get(new Object());
        }
    }
    
    /* (non-Javadoc)
     * @see joeq.Compiler.Quad.ControlFlowGraphVisitor#visitCFG(joeq.Compiler.Quad.ControlFlowGraph)
     */
    public void visitCFG(ControlFlowGraph cfg) {
        if (SSA && ENTER_SSA) {
            new EnterSSA().visitCFG(cfg);
        }
        QuadIterator i = new QuadIterator(cfg);
        jq_Method m = cfg.getMethod();
        int methodID = getMethodID(m);
        
        if (!GLOBAL_QUAD_NUMBERS) quadMap.clear();
        
        long time = System.currentTimeMillis();
        
        boolean firstQuad = true;
        
        while (i.hasNext()) {
            Quad q = i.nextQuad();
            currentQuad = bdd.one();
            int quadID = getQuadID(q);
            addQuadLoc(m, q);
            //System.out.println("Quad id: "+quadID);
            
            // first quad visited is the entry point
            if (firstQuad) {
                methodEntries.orWith(method.ithVar(methodID).and(quad.ithVar(quadID)));
                firstQuad = false;
            }
            
            currentQuad.andWith(quad.ithVar(quadID));
            if (!GLOBAL_QUAD_NUMBERS) {
                currentQuad.andWith(method.ithVar(methodID));
                methodToQuad.orWith(currentQuad.id());
            } else {
                methodToQuad.orWith(currentQuad.and(method.ithVar(methodID)));
            }
            int opID = getOpID(q.getOperator());
            currentQuad.andWith(opc.ithVar(opID));
            handleQuad(q, m);
            if (!SSA) {
                BDD succ = bdd.zero();
                Iterator j = i.successors();
                if (!j.hasNext()) {
                    succ.orWith(fallthrough.ithVar(0));
                } else do {
                    Quad q2 = (Quad) j.next();
                    int quad2ID = getQuadID(q2);
                    succ.orWith(fallthrough.ithVar(quad2ID));
                } while (j.hasNext());
                currentQuad.andWith(succ);
            }
            //printQuad(currentQuad);
            allQuads.orWith(currentQuad);
        }
        
        time = System.currentTimeMillis() - time;
        totalTime += time;
        System.out.println("Method: " + cfg.getMethod() + " time: " + time);
        int qSize = totalQuads;
        //int nodes = allQuads.nodeCount();
        //System.out.println("Quads: " +qSize+", nodes: "+nodes+", average:
        // "+(float)nodes/qSize);
    }
    
    long totalTime;
    
    public String toString() {
        buildNullConstantBdds();
        
        System.out.println("Total time spent building representation: "+totalTime);
        System.out.println("allQuads, node count: " + allQuads.nodeCount());
        System.out.println("methodToQuad, node count: " + methodToQuad.nodeCount());
        
        System.out.println("methodMap size: " + methodMap.size());
        System.out.println("opMap size: " + opMap.size());
        System.out.println("quadMap size: " + quadMap.size());
        System.out.println("quadLineMap size: " + quadLineMap.size());
        //System.out.println("regMap size: " + regMap.size());
        System.out.println("memberMap size: " + memberMap.size());
        System.out.println("constantMap size: " + constantMap.size());
        
        try {
            //print();
            dump();
        } catch (IOException x) {
        }
        return ("BuildBDDIR, node count: " + allQuads.nodeCount());
    }
    
    public int getMethodID(jq_Method m) {
        int x = methodMap.get(m);
        Assert._assert(x > 0);
        return x;
    }
    
    public int getRegisterID(Register r) {
        int x = r.getNumber() + 1;
        return x;
    }
    
    public int getConstantID(Object c) {
        int x = constantMap.get(c);
        Assert._assert(x > 0);
        return x;
    }
    
    public int getQuadID(Quad r) {
        int x = quadMap.get(r);
        Assert._assert(x > 0);
        return x;
    }
    
    public void addQuadLoc(jq_Method m, Quad q) {        
        Map map = CodeCache.getBCMap(m);            
        Integer j = (Integer) map.get(q);
        if (j == null) {
            //Assert.UNREACHABLE("Error: no mapping for quad "+q);
            // some, like PHI nodes may not have a mapping
            return;
        }
        int bcIndex = j.intValue();
        ProgramLocation quadLoc = new ProgramLocation.BCProgramLocation(m, bcIndex);
        quadLineMap.get(q + " @ " + quadLoc.toStringLong());
    }
    
    public int getMemberID(Object r) {
        int x = memberMap.get(r);
        Assert._assert(x > 0);
        return x;
    }
    
    public int getOpID(Operator r) {
        int x = opMap.get(r.toString());
        Assert._assert(x > 0);
        return x;
    }
    
    void handleQuad(Quad q, jq_Method m) {
        //System.out.println("handling quad: "+q);
        int quadID=0, opcID=0, destID=0, src1ID=0, src2ID=0, constantID=0, targetID=0, memberID=0;
        List srcsID = null;
        quadID = getQuadID(q);
        opcID = getOpID(q.getOperator());
        Iterator i = q.getDefinedRegisters().iterator();
        if (i.hasNext()) {
            RegisterOperand ro = (RegisterOperand) i.next();
            //System.out.println("destination register is "+ro.getRegister());
            destID = getRegisterID(ro.getRegister());
            //System.out.println("destination register id "+destID);
            Assert._assert(!i.hasNext());
        }
        i = q.getUsedRegisters().iterator();
        if (USE_SRC12 && i.hasNext()) {
            RegisterOperand rop;
            rop = (RegisterOperand) i.next();
            if (rop != null) src1ID = getRegisterID(rop.getRegister());
            if (i.hasNext()) {
                rop = (RegisterOperand) i.next();
                if (rop != null) src2ID = getRegisterID(rop.getRegister());
            }
        }
        if (i.hasNext()) {
            srcsID = new LinkedList();
            do {
                RegisterOperand rop = (RegisterOperand) i.next();
                //System.out.println("source register is "+rop.getRegister());                
                if (rop != null) srcsID.add(new Integer(getRegisterID(rop.getRegister())));
                //System.out.println("source register id "+getRegisterID(rop.getRegister()));
            } while (i.hasNext());
        }
        i = q.getAllOperands().iterator();
        while (i.hasNext()) {
            Operand op = (Operand) i.next();
            if (op instanceof RegisterOperand) continue;
            else if (op instanceof ConstOperand) {
                constantID = getConstantID(((ConstOperand) op).getWrapped());
            } else if (op instanceof TargetOperand) {
                if (!SSA)
                    targetID = getQuadID(((TargetOperand) op).getTarget().getQuad(0));
            } else if (op instanceof FieldOperand) {
                memberID = getMemberID(((FieldOperand) op).getField());
            } else if (op instanceof MethodOperand) {
                memberID = getMemberID(((MethodOperand) op).getMethod());
            } else if (op instanceof TypeOperand) {
                memberID = getMemberID(((TypeOperand) op).getType());
            }
        }
        //System.out.println("quadID "+quadID+" destID "+destID);
        if (ZERO_FIELDS || quadID != 0) currentQuad.andWith(quad.ithVar(quadID));
        if (ZERO_FIELDS || opcID != 0) currentQuad.andWith(opc.ithVar(opcID));
        if (ZERO_FIELDS || destID != 0) currentQuad.andWith(dest.ithVar(destID));
        if (USE_SRC12) {
            if (ZERO_FIELDS || src1ID != 0) currentQuad.andWith(src1.ithVar(src1ID));
            if (ZERO_FIELDS || src2ID != 0) currentQuad.andWith(src2.ithVar(src2ID));
        }
        if (ZERO_FIELDS || constantID != 0) currentQuad.andWith(constant.ithVar(((long)constantID) & 0xFFFFFFFFL));
        if (!SSA) {
            if (ZERO_FIELDS || targetID != 0) currentQuad.andWith(target.ithVar(targetID));
        }
        if (ZERO_FIELDS || memberID != 0) currentQuad.andWith(member.ithVar(memberID));
        if (srcsID != null && !srcsID.isEmpty()) {
            BDD temp = bdd.zero();
            int j = 1;
            for (i = srcsID.iterator(); i.hasNext(); ++j) {
                int srcID = ((Integer) i.next()).intValue();
                //System.out.println("source "+j+" srcID "+srcID);
                if (ZERO_FIELDS || srcID != 0) {
                    BDD temp2 = srcNum.ithVar(j);
                    temp2.andWith(srcs.ithVar(srcID));
                    temp.orWith(temp2);
                }
            }
            if (!temp.isZero())
                currentQuad.andWith(temp);
            else
                temp.free();
        } else if (ZERO_FIELDS) {
            BDD temp2 = srcNum.ithVar(0);
            temp2.andWith(srcs.ithVar(0));
            currentQuad.andWith(temp2);
        }
        
        Operator quadOp = q.getOperator();
        if (quadOp instanceof Invoke ||
            quadOp instanceof New ||
            quadOp instanceof NewArray) {
            ProgramLocation quadLoc;
            Map map = CodeCache.getBCMap(m);            
            Integer j = (Integer) map.get(q);
            if (j == null) {
                Assert.UNREACHABLE("Error: no mapping for quad "+q);
            }
            int bcIndex = j.intValue();
            quadLoc = new ProgramLocation.BCProgramLocation(m, bcIndex);

            if (quadOp instanceof Invoke) {
                invokeMap.put(quadLoc, q);
            }
            else {
                allocMap.put(quadLoc, q);
            }
        }
        ++totalQuads;
    }
    
    public void dump() throws IOException {
        System.out.println("Var order: "+varOrderDesc);
        dumpMap(quadMap, dumpDir+"quad.map");
        dumpMap(quadLineMap, dumpDir+"quadloc.map");
        dumpMap(opMap, dumpDir+"op.map");
        //dumpMap(regMap, dumpDir+"reg.map");
        dumpMap(memberMap, dumpDir+"member.map");
        dumpMap(constantMap, dumpDir+"constant.map");
        
        String relationName;
        if (SSA) {
            relationName = "ssa";
        }
        else {
            relationName = "cfg";
        }
        
        dumpBDDConfig(dumpDir+"bdd."+relationName);
        dumpFieldDomains(dumpDir+"fielddomains."+relationName);
        dumpRelations(dumpDir+"relations."+relationName);            
        
        System.out.print("Saving BDDs...");
        bdd.save(dumpDir+relationName+".bdd", allQuads);
        bdd.save(dumpDir+"m2q.bdd", methodToQuad);
        bdd.save(dumpDir+"entries.bdd", methodEntries);
        bdd.save(dumpDir+"nullconstant.bdd", nullConstant);
        bdd.save(dumpDir+"nonnullconstants.bdd", nonNullConstants);
        System.out.println("done.");
        
        if (DUMP_TUPLES) {
            System.out.println("Saving tuples....");
            dumpTuples(bdd, dumpDir+relationName+".tuples", allQuads);
            dumpTuples(bdd, dumpDir+"m2q.tuples", methodToQuad);
            dumpTuples(bdd, dumpDir+"entries.tuples", methodEntries);
            dumpTuples(bdd, dumpDir+"nullconstant.tuples", nullConstant);
            dumpTuples(bdd, dumpDir+"nonnullconstants.tuples", nonNullConstants);
            System.out.println("done.");
        }
    }
    
    void dumpBDDConfig(String fileName) throws IOException {
        BufferedWriter dos = null;
        try {
            dos = new BufferedWriter(new FileWriter(fileName));
            for (int i = 0; i < bdd.numberOfDomains(); ++i) {
                BDDDomain d = bdd.getDomain(i);
                dos.write(d.getName()+" "+d.size()+"\n");
            }
        } finally {
            if (dos != null) dos.close();
        }
    }
    
    public void dumpFieldDomains(BufferedWriter dos) throws IOException {
        dos.write("method "+(1L<<methodBits)+"\n");
        dos.write("quad "+(1L<<quadBits)+"\n");
        dos.write("op "+(1L<<opBits)+" op.map\n");
        dos.write("reg "+(1L<<regBits)+"\n");
        dos.write("constant "+(1L<<constantBits)+" constant.map\n");
        dos.write("member "+(1L<<memberBits)+"\n");
        dos.write("varargs "+(1L<<varargsBits)+"\n");
    }
    
    public void dumpFieldDomains(String fileName) throws IOException {
        BufferedWriter dos = null;
        try {
            dos = new BufferedWriter(new FileWriter(fileName));
            dumpFieldDomains(dos);
        } finally {
            if (dos != null) dos.close();
        }
    }
    
    void dumpRelation(BufferedWriter dos, String name, BDD relation) throws IOException {
        int[] a = relation.support().scanSetDomains();
        dos.write(name+" ( ");
        for (int i = 0; i < a.length; ++i) {
            if (i > 0) dos.write(", ");
            BDDDomain d = bdd.getDomain(a[i]);
            dos.write(d.toString()+" : ");
            if (d == quad || d == fallthrough || d == target) dos.write("quad ");
            else if (d == method) dos.write("method ");
            else if (d == opc) dos.write("op ");
            else if (d == dest || d == srcs || d == src1 || d == src2) dos.write("reg ");
            else if (d == constant) dos.write("constant ");
            else if (d == member) dos.write("member ");
            else if (d == srcNum) dos.write("varargs ");
            else dos.write("??? ");
        }
        dos.write(")\n");
    }
    
    void dumpRelations(String fileName) throws IOException {
        BufferedWriter dos = new BufferedWriter(new FileWriter(fileName));
        dumpRelation(dos, "m2q", methodToQuad);
        if (SSA) {
            dumpRelation(dos, "ssa", allQuads);
        } else {
            dumpRelation(dos, "cfg", allQuads);
        }
        dumpRelation(dos, "entries", methodEntries);
        dumpRelation(dos, "nullconstant", nullConstant);
        dumpRelation(dos, "nonnullconstants", nonNullConstants);
        dos.close();
    }
    
    void dumpMap(IndexMap map, String fileName) throws IOException {
        BufferedWriter dos = null;
        try {
            dos = new BufferedWriter(new FileWriter(fileName));
            for (int i = 0; i < map.size(); ++i) {
                Object o = map.get(i);
                String s;
                if (o != null) {
                   s = o.toString();
                }
                else {
                   s = "(null)"; 
                }
                // suppress nonprintables in the output
                StringBuffer sb = new StringBuffer(s);
                for (int j=0; j<sb.length(); ++j) {
                    if (sb.charAt(j) < 32) {
                        sb.setCharAt(j, ' ');
                    }
                    else if (sb.charAt(j) > 127) {
                        sb.setCharAt(j, ' ');
                    }
                }
                s = new String(sb);
                dos.write(s + "\n");
            }
        } finally {
            if (dos != null) dos.close();
        }
    }
    
    public static void dumpTuples(BDDFactory bdd, String fileName, BDD relation) throws IOException {
        BufferedWriter dos = null;
        try {
            dos = new BufferedWriter(new FileWriter(fileName));
            if (relation.isZero()) {
                return;
            }
            Assert._assert(!relation.isOne());
            BDD rsup = relation.support();
            int[] a = rsup.scanSetDomains();
            rsup.free();
            BDD allDomains = bdd.one();
            System.out.print(fileName+" domains {");
            dos.write("#");
            for (int i = 0; i < a.length; ++i) {
                BDDDomain d = bdd.getDomain(a[i]);
                System.out.print(" "+d.toString());
                dos.write(" "+d.toString()+":"+d.varNum());
                allDomains.andWith(d.set());
            }
            dos.write("\n");
            System.out.println(" } = "+relation.nodeCount()+" nodes");
            BDDDomain primaryDomain = bdd.getDomain(a[0]);
            int lines = 0;
            BDD foo = relation.exist(allDomains.exist(primaryDomain.set()));
            for (Iterator i = foo.iterator(primaryDomain.set()); i.hasNext(); ) {
                BDD q = (BDD) i.next();
                q.andWith(relation.id());
                while (!q.isZero()) {
                    BDD sat = q.satOne(allDomains, false);
                    BDD sup = q.support();
                    int[] b = sup.scanSetDomains();
                    sup.free();
                    BigInteger[] v = sat.scanAllVar();
                    sat.free();
                    BDD t = bdd.one();
                    for (int j = 0, k = 0, l = 0; j < bdd.numberOfDomains(); ++j) {
                        BDDDomain d = bdd.getDomain(j);
                        if (k >= a.length || a[k] != j) {
                            Assert._assert(v[j].signum() == 0, "v["+j+"] is "+v[j]);
                            //dos.write("* ");
                            t.andWith(d.domain());
                            continue;
                        } else {
                            ++k;
                        }
                        if (l >= b.length || b[l] != j) {
                            Assert._assert(v[j].signum() == 0, "v["+j+"] is "+v[j]);
                            dos.write("* ");
                            t.andWith(d.domain());
                            continue;
                        } else {
                            ++l;
                        }
                        dos.write(v[j]+" ");
                        t.andWith(d.ithVar(v[j]));
                    }
                    q.applyWith(t, BDDFactory.diff);
                    dos.write("\n");
                    ++lines;
                }
                q.free();
            }
            System.out.println("Done printing "+lines+" lines.");
        } finally {
            if (dos != null) dos.close();
        }
    }
    
    void print() {
        for (int i = 0; i < quadMap.size(); ++i) {
            BDD q = quad.ithVar(i).andWith(allQuads.id());
            printQuad(q);
        }
    }
    
    void printQuad(BDD q) {
        BigInteger id = q.scanVar(quad);
        if (id.signum() < 0) return;
        System.out.println("Quad id "+id);
        System.out.println("        "+quadMap.get(id.intValue()));
        System.out.println(q.toStringWithDomains());
    }
    
    void buildNullConstantBdds() {
        for (int i = 0; i < constantMap.size(); ++i) {
            Object c = constantMap.get(i);
            if (c == null) {
                nullConstant.orWith(constant.ithVar(i));
            }
            else if (!(c instanceof Integer) &&
                     !(c instanceof Float) &&
                     !(c instanceof Long) &&
                     !(c instanceof Double) &&
                     c != theDummyObject) {
                nonNullConstants.orWith(constant.ithVar(i));                    
            }
        }
    }
    
    public String getVarOrderDesc() {
        return varOrderDesc;
    }
    
    public BDDDomain getDestDomain() {
        return dest;
    }
    
    public BDDDomain getQuadDomain() {
        return quad;
    }
    
    public String getDomainName(BDDDomain d) {
        if (d == quad || d == fallthrough || d == target) return "quad";
        else if (d == method) return "method";
        else if (d == opc) return "op";
        else if (d == dest || d == srcs || d == src1 || d == src2) return "reg";
        else if (d == constant) return "constant";
        else if (d == member) return "member";
        else if (d == srcNum) return "varargs";
        else return d.getName();
    }
    
    public int quadIdFromInvokeBCLocation(ProgramLocation.BCProgramLocation bc) {
        Object o = invokeMap.get(bc);
        Assert._assert(o != null);
        return quadMap.get(o);
    }
    
    public int quadIdFromAllocBCLocation(ProgramLocation.BCProgramLocation bc) {
        Object o = allocMap.get(bc);
        Assert._assert(o != null);
        return quadMap.get(o);
    }
    
    public int memberIdFromField(jq_Field f) {
        if (memberMap.contains(f)) {
            return memberMap.get(f);
        }
        else {
            return 0;
        }
    }

     public BDDDomain getMemberDomain() {
        return member;
    }
}
