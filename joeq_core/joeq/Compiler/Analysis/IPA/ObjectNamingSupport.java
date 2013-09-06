/*
 * Created on Apr 25, 2005
 * 
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package joeq.Compiler.Analysis.IPA;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;
import jwutil.graphs.DominanceFrontier;
import jwutil.graphs.Dominators;
import jwutil.graphs.Navigator;

/**
 * @author V.Benjamin Livshits
 * 
 */
public class ObjectNamingSupport {
    private AnnotatedDirectedGraph g;
    private static final Object HEAD = "HEAD";
    private HashMap names = new HashMap();
    private Set sources = new HashSet();
    private static final boolean SHOW_PRED = false;
    private Set frontierNodes = new HashSet();
    private Dominators dominators;
    private DominanceFrontier df;
    private static String DIR = "";
    private boolean TRACE = false;

    public static void main(String[] args) {
        if(args.length > 0){
            DIR = args[0];
            if(!DIR.endsWith(File.separator)){
                DIR += File.separator;
            }
            System.out.println("Using directory " + DIR);
        }
        
        ObjectNamingSupport md = new ObjectNamingSupport();
        md.run();
    }

    private void run() {
        try {
            readGraph(DIR + "flows.tuples");
            readNames(DIR + "heap2.map");
            String firstLine = readSources(DIR + "source_h2.tuples");
            //sources.addAll(Arrays.asList(new String[] { "1587" }));            
            
            //g.printGraph();

            dominators = new Dominators(true, HEAD, g.getNavigator());
            df = new DominanceFrontier(HEAD, g.getNavigator(), dominators);
            printDF(df);
            dumpFrontierNodes(DIR + "frontier.tuples", firstLine);
        }catch(IOException e){
            System.err.println(e.getMessage());
        }
    }

    private void dumpFrontierNodes(String frontierFile, String firstLine) {
        System.out.println("DF set: " + frontierNodes);
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(frontierFile));
            bw.write(firstLine);
            bw.write("\n");
            
            for(Iterator iter = frontierNodes.iterator(); iter.hasNext();){
                String n = (String) iter.next();
                bw.write(n + "\n");
            }
            bw.close();
        }catch(IOException e){
            System.err.println(e.getMessage());
        }
    }

    private void printDF(DominanceFrontier df) {
        //for(Iterator iter = g.getEntryIterator(); iter.hasNext();){
        for(Iterator iter = sources.iterator(); iter.hasNext();){
            String node = (String) iter.next();
            if(g.containsNode(node)){
                Set frontier = df.getIteratedDominanceFrontier(node);
                
                if(TRACE) System.out.println("Source " + names.get(node) + "(" + node + ")");
                if(TRACE) System.out.println("Frontier of " + names.get(node)); 
                
                for(Iterator frontierIter = frontier.iterator(); frontierIter.hasNext();){
                    String n = (String) frontierIter.next();
                    if(!predDominatedBySources(n)){
                        if(TRACE) System.out.println("\t\t" + names.get(n) + 
                            "(" + n + ") " + g.getPredsOf(n).size());
                        
                        frontierNodes.add(n);
                        
                        if(SHOW_PRED){            
                            for(Iterator predIter = g.getPredsOf(n).iterator(); predIter.hasNext();){
                                String pred = (String) predIter.next();
                                String annote = g.getEdgeAnnote(pred, n);

                                if(!isDominatedBySources(pred)){
                                    if(TRACE) System.out.println("\t\t\t" + pred + ": " + names.get(pred) + ", " + annote);
                                }else{
                                    if(TRACE) System.out.println("\t\t\t+" + pred + ": " + names.get(pred) + ", " + annote);
                                }
                            }
                        }
                    }
                }
            }else{
                if(TRACE) System.out.println("Source " + " is missing from the graph.");
            }
        }        
    }
    
    private boolean predDominatedBySources(Object gode) {
        for(Iterator predIter = g.getPredsOf(gode).iterator(); predIter.hasNext();){
            String pred = (String) predIter.next();
            //System.out.println("\t\t" + names.get(pred));
            if(!isDominatedBySources(pred)){
                return false; 
            }
        }
        
        return true;
    }

    boolean isDominatedBySources(String node) {
        for(Iterator sourceIter = sources.iterator(); sourceIter.hasNext();){
            String source = (String) sourceIter.next();
            
            if(dominators.inDominatees(source, node)){
                return true;
            }
        }
        return false;
    }

    void readGraph(String tuplesFile) throws IOException {
        g = new AnnotatedDirectedGraph();
        BufferedReader di = new BufferedReader(new FileReader(tuplesFile));
        String line = di.readLine();
        long lineCount = 0;
        do {
            if (!line.startsWith("#")) {
                try {
                    StringTokenizer tok = new StringTokenizer(line);
                    String h2 = tok.nextToken();
                    String h1 = tok.nextToken();
                    String i  = tok.nextToken();
                    String r  = tok.nextToken();
                    
                    if(!g.containsNode(h1)){
                        g.addNode(h1); 
                    }
                    if(!g.containsNode(h2)){
                        g.addNode(h2); 
                    }
                    g.addEdge(h1, h2, r);
                } catch (NoSuchElementException e) {
                    line = di.readLine();
                }
            }
            lineCount++;
        } while ((line = di.readLine()) != null);
        System.out.println("Read " + lineCount + " lines.");
        
        for(Iterator iter = g.nodeIterator(); iter.hasNext();){
            String node = (String) iter.next();
            
            if(isEntry(node)){
                g.addEntry(node);
                if(sources.contains(node)){
                    System.out.println("Source " + node);
                }else{
                    //System.out.println("Not a source " + node);
                }
            }
        }
        
        g.addNode(HEAD);
        for(Iterator iter = g.getEntryIterator(); iter.hasNext();){
            g.addEdge(HEAD, iter.next(), "initial");
        }
        di.close();
    }
    
    private boolean isEntry(String node) {
        if(g.getPredsOf(node).isEmpty()){
            // no predecessors
            return true;
        }
        if(g.getPredsOf(node).iterator().next().equals(node)){
            // self-loop
            return true;
        }
        
        return false;
    }

    void readNames(String namesFile) throws IOException {
        BufferedReader di = new BufferedReader(new FileReader(namesFile));
        int lineno = 0;
        String line = di.readLine();
        
        do {
            names.put(new Integer(lineno++).toString(), line);
        } while ((line = di.readLine()) != null);
        System.out.println("Read " + names.size() + " names.");
    }
    
    String readSources(String namesFile) throws IOException {
        BufferedReader di = new BufferedReader(new FileReader(namesFile));
        int lineno = 1;
        String line = di.readLine();
        String firstLine = null;
        
        do {
            if (!line.startsWith("#")) {
                try {
                    StringTokenizer tok = new StringTokenizer(line);
                    String h = tok.nextToken();

                    sources.add(h);
                }catch(NoSuchElementException e) {
                    line = di.readLine();
                }
            }else{
                firstLine = line;
                int idx = firstLine.indexOf("I0");
                if(idx != -1){
                    firstLine = firstLine.substring(0, idx-1);
                }
            }
            
        } while ((line = di.readLine()) != null);
        System.out.println("Read " + sources.size() + " sources.");
        
        return firstLine;
    }
}

class AnnotatedDirectedGraph implements jwutil.graphs.Graph {
    HashMap edgeAnnotes = new HashMap();
    LinkedList entries = new LinkedList();
    HashMap nextMap = new HashMap();
    HashMap prevMap = new HashMap();
    private Set nodeSet = new HashSet();
    
    public void addEdge(Object from, Object to, String annote){        
        nodeSet.add(from); nodeSet.add(to);
        
        Set toList   = (Set) nextMap.get(from);
        Set fromList = (Set) prevMap.get(to);        
        
        if(toList == null){
            toList = new HashSet();
        }
        if(fromList == null){
            fromList = new HashSet();
        }        
        toList.add(to); fromList.add(from);
        
        nextMap.put(from, toList);
        prevMap.put(to, fromList);
        
        edgeAnnotes.put(from.toString() + ":" + to.toString(), annote);
        //System.out.println("Adding an edge " + from + " -> " + to + " with annote " + annote);
    }
    
    public Iterator nodeIterator() {
        return nodeSet.iterator();
    }

    public boolean containsNode(String node) {
        return nodeSet.contains(node);
    }

    public void addNode(Object n) {
        nodeSet.add(n);        
    }

    public String getEdgeAnnote(String from, Object to) {
        return (String) edgeAnnotes.get(from.toString() + ":" + to.toString());
    }

    public void addEntry(Object node) {
        entries.add(node);
        //System.out.println("Entry " + node);
    }

    public Iterator getEntryIterator() {
        return entries.iterator();
    }

    public void printGraph() {
        for (Iterator it = nodeSet.iterator(); it.hasNext();) {
            Object node = it.next();
            System.out.println("Node = " + node);
            System.out.println("Preds:");
            for (Iterator predsIt = getPredsOf(node).iterator(); predsIt.hasNext();) {
                System.out.print("     ");
                System.out.println(predsIt.next());
            }
            System.out.println("Succs:");
            for (Iterator succsIt = getSuccsOf(node).iterator(); succsIt.hasNext();) {
                System.out.print("     ");
                System.out.println(succsIt.next());
            }
        }
    }

    public Collection getSuccsOf(Object node) {
        Collection result = (Collection) nextMap.get(node);
        if(result == null){
            result = Collections.EMPTY_SET;
        }
        
        return result;
    }

    public Collection getPredsOf(Object node) {
        Collection result = (Collection) prevMap.get(node);
        if(result == null){
            result = Collections.EMPTY_SET;
        }
        
        return result;
    }

    /* (non-Javadoc)
     * @see jwutil.graphs.Graph#getRoots()
     */
    public Collection getRoots() {
        return this.entries;
    }

    /* (non-Javadoc)
     * @see jwutil.graphs.Graph#getNavigator()
     */
    public Navigator getNavigator() {
        return new Navigator(){
            public Collection next(Object node) {
                return getPredsOf(node);
            }
            public Collection prev(Object node) {
                return getSuccsOf(node);
            }            
        };
    }
}