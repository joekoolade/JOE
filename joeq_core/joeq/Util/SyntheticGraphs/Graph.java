package joeq.Util.SyntheticGraphs;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Graph {
    public static class Direction {
        String _dir;
        public Direction(String dir){
            _dir = dir;
        }
        public String toString(){
            return _dir;
        }
        public static final String LR = "LR";
        public static final String TB = "TB"; 
    }
    protected String      _name;
    protected HashMap     _nodeMap;
    protected List        _edgeList;
    protected Direction _dir;
    
    public class Edge{
        protected String n1, n2;
        public Edge(String n1, String n2){
            this.n1 = n1; this.n2 = n2;
        }
        public String toString(){
            return n1 + " -> " + n2;
        }
    }
    
    public Graph(String name, Direction dir){
        _name    = name;
        _nodeMap = new HashMap();
        _edgeList = new LinkedList();
        _dir      = dir; 
    }
    
    public Graph(){
        _name     = "none";
        _dir      = new Direction(Direction.LR);
        _nodeMap  = new HashMap();
        _edgeList = new LinkedList(); 
    }
    
    public void addNode(String num, String name){
        _nodeMap.put(num, name);
    }
    
    public void addNode(long num, String name){
        String num_str = new Long(num).toString();
        _nodeMap.put("n" + num_str, name);
    }
    
    public void addEdge(String num1, String num2){
        Edge e = new Edge(num1, num2);
        _edgeList.add(e);            
    }
    
    public void addEdge(long num1, long num2){
        String num1_str = "n" + new Long(num1).toString();
        String num2_str = "n" + new Long(num2).toString();
        
        Edge e = new Edge(num1_str, num2_str);
        _edgeList.add(e);
    }
    
    public void printDot(PrintStream out){
        out.println("digraph \"" + _name + "\" { \n" + "\trankdir=\"" + _dir.toString() + "\";");
        Iterator iter = _nodeMap.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry e = (Map.Entry)iter.next();

            String num  = (String)e.getKey();
            String name = (String)e.getValue();
            
            out.println("\t" + num + " [label=\"" + name + "\"];");
        }
        out.println("");

        
        Iterator iter2 = _edgeList.iterator();
        while(iter2.hasNext()){
            Graph.Edge e = (Graph.Edge)iter2.next();
            out.println("\t" + e.toString() + " [label = \"\"];");
        }
        
        out.println("}; ");
    } 
}

class TestGraph {
    public static void main(String argv[]){
        //simpleTest();
        //makeTest1();
        makeTest2();
    }
    
    static void simpleTest(){
        Graph g = new Graph("small test", new Graph.Direction(Graph.Direction.LR));
        g.addNode("n0", "name0");
        g.addNode("n1", "name1");
        g.addEdge("n0", "n1");

        g.printDot(System.out);
    }
    
    //    a bunch of single nodes connected to one node in the clique each.
    static void makeTest1(){
        Graph g = new Graph();
        for(int i = 0; i < 20; i++){
            g.addNode(i, "node"+new Integer(i).toString());
        }
        
        for(int i = 0; i < 10; i++){
            for(int j = 0; j < 10; j++){
                g.addEdge(i, j);
            }
        }
        
        java.util.Random r = new java.util.Random();
        for(int i = 11; i < 20; i++){
            int link_to = r.nextInt(10)/* random 0..9 */;
            g.addEdge(i, link_to);
        }
        g.printDot(System.out);
    }
    
    // a bunch of single nodes connected to all nodes in the clique
    static void makeTest2(){
        Graph g = new Graph();
        for(int i = 0; i < 20; i++){
            g.addNode(i, "node"+new Integer(i).toString());
        }
    
        for(int i = 0; i < 10; i++){
            for(int j = 0; j < 10; j++){
                //if(Random())
                g.addEdge(i, j);
            }
        }
    
        for(int i = 11; i < 20; i++){
            for(int j = 0; j < 10; j++){
                g.addEdge(i, j);
            }
        }
        g.printDot(System.out);
    }
}

