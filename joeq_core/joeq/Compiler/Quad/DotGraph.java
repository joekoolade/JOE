// DotGraph.java, created Tue Nov  5 14:16:40 2002 by joewhaley
// Copyright (C) 2001-3 Godmar Back <gback@cs.utah.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Compiler.Quad;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import joeq.Class.jq_Class;
import joeq.Class.jq_Method;
import joeq.Util.Templates.ListIterator;

/**
 * @author Godmar Back <gback@cs.utah.edu, @stanford.edu> 
 *
 * This class is a ControlFlowGraphVisitor.
 * For each CFG, it produces a "dot" file in the output directory. 
 * See or change createMethodName to adapt how the filenames are formed.
 *
 * @see DotGraph#outputDir
 * @see DotGraph#dotFilePrefix
 * @see DotGraph#createMethodName(jq_Method)
 *
 * @version $Id: DotGraph.java,v 1.11 2004/09/22 22:17:26 joewhaley Exp $
 */
public class DotGraph implements ControlFlowGraphVisitor {

    /**
     * The output directory for the dot graph descriptions
     */
    public static String outputDir = "dottedcfgs";

    /**
     * Prefix that goes before the name.
     */
    public static String dotFilePrefix = "joeq-";

    /**
     * Adapt this method to create filenames the way you want them.
     */
    protected String createMethodName(jq_Method mth) {
        String filename = dotFilePrefix + mth.toString();
        filename = filename.replace('/', '_');
        filename = filename.replace(' ', '_');
        filename = filename.replace('<', '_');
        filename = filename.replace('>', '_');
        filename = filename.replace('(', '_');
        filename = filename.replace(')', '_');
        
        return filename;
    }

    /**
     * dot - helper class for outputting graphviz specifications for simple cfgs
     *
     * See http://www.research.att.com/sw/tools/graphviz/
     *
     * Process with, for instance, "dot -Tgif -o graph.gif <inputfile>"
     * or simply "dotgif <inputfile>"
     *
     * @author Godmar Back <gback@cs.utah.edu>
     */
    public static class dot {
        private static PrintWriter containedgraph = null;
        /**
         * The first argument specifies what directory to use for output, the
         * second is the file name.
         */
        public static void openGraph(String the_outputDir, String name) {
            try {
                if (the_outputDir != null)
                    outputDir = the_outputDir;
                String dirname = outputDir;
                File d = new File(dirname);
                if (!d.exists()) {
                    d.mkdir();
                }
                String dirsep = System.getProperty("file.separator");
                containedgraph = new PrintWriter(new FileOutputStream(dirname
                        + dirsep + name));
                containedgraph.println("digraph contained_in_graph {");
                containedgraph
                        .println("\tnode[shape=box,fontname = \"Arial\", fontsize=10];");
                containedgraph
                        .println("\tedge[fontname = \"Arial\", fontcolor=red, fontsize=8];");
                containedgraph.println("\tlabel = \"" + name + "\";");
            } catch (IOException _) {
                _.printStackTrace(System.err);
            }
        }
        public static void openGraph(String name) {
            openGraph(null, name);
        }

        public static String escape(String from) {
            from = from.replace('\t', ' ').trim();
            StringBuffer fb = new StringBuffer();
            for (int i = 0, sucspaces = 0; i < from.length(); i++) {
                char c = from.charAt(i);
                if (c == '"' || c == '\\')
                    fb.append("\\" + c);
                else if (c == '\n')
                    fb.append("\\\\n");
                else if (c == '\r')
                    fb.append("\\\\r");
                else if (sucspaces == 0 || c != ' ')
                    fb.append(c);
                if (c == ' ')
                    sucspaces++;
                else
                    sucspaces = 0;
            }
            return fb.toString();
        }

        private static void outputString(String from) {
            containedgraph.print("\"" + escape(from) + "\"");
        }

        private static void labelEdge(String edge) {
            if (edge != null) {
                containedgraph.print("[label=");
                outputString(edge);
                containedgraph.print(",color=red]");
            }
        }

        public static void userDefined(String useroutput) {
            if (containedgraph != null) {
                containedgraph.print(useroutput);
            }
        }

        private static void makeCircleNode(String to) {
            containedgraph.print("\t");
            outputString(to);
            containedgraph.println("[shape=circle,fontcolor=red,color=red];");
        }

        public static void addEntryEdge(String from, String to, String edge) {
            if (containedgraph != null) {
                makeCircleNode(from);
                containedgraph.print("\t");
                outputString(from);
                containedgraph.print(" -> ");
                outputString(to);
                labelEdge(edge);
                containedgraph.println(";");
            }
        }

        public static void addLeavingEdge(String from, String to, String edge) {
            if (containedgraph != null) {
                makeCircleNode(to);
                containedgraph.print("\t");
                outputString(from);
                containedgraph.print(" -> ");
                outputString(to);
                labelEdge(edge);
                containedgraph.println(";");
            }
        }

        public static void addEdge(String from, String to) {
            addEdge(from, to, null);
        }

        public static void addEdge(String from, String to, String edge) {
            if (containedgraph != null) {
                containedgraph.print("\t");
                outputString(from);
                containedgraph.print(" -> ");
                outputString(to);
                labelEdge(edge);
                containedgraph.println(";");
            }
        }

        public static void closeGraph() {
            containedgraph.println("}");
            containedgraph.close();
            containedgraph = null;
        }
    }

    /**
     * Use the dot helper class to output this cfg as a Graph.
     */
    public void visitCFG(ControlFlowGraph cfg) {
        final HashMap fedge2PEIList = new HashMap();
        try {
            String filename = createMethodName(cfg.getMethod());
            dot.openGraph(filename);
            cfg.visitBasicBlocks(new BasicBlockVisitor() {
                public void visitBasicBlock(BasicBlock bb) {
                    if (bb.isEntry()) {
                        if (bb.getNumberOfSuccessors() != 1)
                            throw new Error("entry bb has != 1 successors "
                                    + bb.getNumberOfSuccessors());
                        dot.addEntryEdge(bb.toString(), bb.getSuccessors()
                                .iterator().next().toString(), null);
                    } else if (!bb.isExit()) {
                        ListIterator.Quad qit = bb.iterator();
                        StringBuffer l = new StringBuffer(" " + bb.toString()
                                + "\\l");
                        HashMap/* <jq_Class,List.Quad> */exceptions2PEIList = new HashMap();
                        while (qit.hasNext()) {
                            l.append(" ");
                            Quad quad = qit.nextQuad();
                            l.append(dot.escape(quad.toString()));
                            l.append("\\l");
                            ListIterator.jq_Class exceptions = quad
                                    .getThrownExceptions().classIterator();
                            while (exceptions.hasNext()) {
                                jq_Class exc = exceptions.nextClass();
                                ArrayList peis = (ArrayList) exceptions2PEIList
                                        .get(exc);
                                if (peis == null)
                                    exceptions2PEIList.put(exc,
                                            peis = new ArrayList());
                                peis.add(quad);
                            }
                        }
                        dot.userDefined("\t" + bb.toString()
                                + " [shape=box,label=\"" + l + "\"];\n");
                        ListIterator.BasicBlock bit = bb.getSuccessors()
                                .basicBlockIterator();
                        while (bit.hasNext()) {
                            BasicBlock nextbb = bit.nextBasicBlock();
                            if (nextbb.isExit()) {
                                dot.addLeavingEdge(bb.toString(), nextbb
                                        .toString(), null);
                            } else {
                                dot.addEdge(bb.toString(), nextbb.toString());
                            }
                        }
                        Iterator eit = exceptions2PEIList.entrySet().iterator();
                        while (eit.hasNext()) {
                            Map.Entry e = (Map.Entry) eit.next();
                            jq_Class exc = (jq_Class) e.getKey();
                            List/* <Quad> */thisPeiList = (List) e.getValue();
                            ListIterator.ExceptionHandler mayCatch;
                            mayCatch = bb.getExceptionHandlers().mayCatch(exc)
                                    .exceptionHandlerIterator();
                            while (mayCatch.hasNext()) {
                                ExceptionHandler exceptionHandler = mayCatch
                                        .nextExceptionHandler();
                                BasicBlock nextbb = exceptionHandler.getEntry();
                                FactoredEdge fe = new FactoredEdge(bb, nextbb,
                                        exceptionHandler.getExceptionType());
                                List factoredPeiList = (List) fedge2PEIList
                                        .get(fe);
                                if (factoredPeiList == null) {
                                    fedge2PEIList.put(fe,
                                            factoredPeiList = new ArrayList());
                                }
                                factoredPeiList.addAll(thisPeiList);
                                // dot.addEdge(bb.toString(),
                                // nextbb.toString(), peis +
                                // exceptionHandler.getExceptionType().toString());
                            }
                            // if (bb.getExceptionHandlers().mustCatch(exc) ==
                            // null) { }
                        }
                    }
                }
            });
        } finally {
            Iterator fedges = fedge2PEIList.entrySet().iterator();
            while (fedges.hasNext()) {
                Map.Entry e = (Map.Entry) fedges.next();
                FactoredEdge fe = (FactoredEdge) e.getKey();
                List factoredPeiList = (List) e.getValue();
                String peis = "[" + ((Quad) factoredPeiList.get(0)).getID();
                for (int i = 1; i < factoredPeiList.size(); i++) {
                    peis += ", " + ((Quad) factoredPeiList.get(i)).getID();
                }
                peis += "] ";
                dot.addEdge(fe.from.toString(), fe.to.toString(), peis
                        + fe.exception);
            }
            dot.closeGraph();
        }
    }

    class FactoredEdge {
        BasicBlock from, to;
        jq_Class exception;
        FactoredEdge(BasicBlock from, BasicBlock to, jq_Class exception) {
            this.from = from;
            this.to = to;
            this.exception = exception;
        }
        public boolean equals(Object that) {
            return equals((FactoredEdge) that);
        }
        public int hashCode() {
            return from.hashCode() ^ to.hashCode();
        }
        public boolean equals(FactoredEdge that) {
            return this.from.equals(that.from) && this.to.equals(that.to)
                    && this.exception.equals(that.exception);
        }
    }
}
