package joeq.Main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import jwutil.util.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * A command-line utility to generate an invocation stub from a web.xml file. 
 * 
 * @author V. Benjamin Livshits
 * @version $Id: GenerateWebRoots.java,v 1.14 2005/06/05 05:31:12 joewhaley Exp $
 */
public class GenerateWebRoots {
    private static boolean TRACE = true;
    private static PrintStream out = System.out;
    private static final String TAGLIB_BASE = "cls/webapp";

    public static void main(String[] args) throws FileNotFoundException {
        String inputFile  = null;
        String outputFile = null; //"InvokeServlets.java";
        
        for(int i = 0; i < args.length; i++){
            String arg = args[i];
            
            if(inputFile == null){
                inputFile = arg;
            }else
            if(arg.equals("-o")){
                outputFile = args[++i];
            }else
            if(arg.equals("-v")){
                TRACE = true;
            }
        }
        if(inputFile == null){
            usage(args[0]);
            System.exit(1);
        }
        Document doc            = parseFile(inputFile);
        
        // get the interesting tags
        Collection servlets     = findMatches(doc, "servlet-class");
        Collection filters      = findMatches(doc, "filter-class");
        Collection listeners    = findMatches(doc, "listener-class");
        Collection taglibs      = findMatches(doc, "taglib-location");
        
        out = (outputFile != null) ? 
            new PrintStream(new FileOutputStream(outputFile)) : 
            System.out;
        
        printPreamble();
        printServlets(servlets);
        printFilters(filters);
        printListeners(listeners);
        printTags(taglibs);
        printPostamble();       
    }

    private static void usage(String prog) {
        System.err.println("Usage:");
        System.err.println("\t" + prog + "input-file [-v] [-o output-file]");
    }

    private static void printPostamble() {
        out.println("}\n");        
    }

    private static void printTags(Collection taglibs) {
        out.println("\tpublic static void processTags() {");
        for(Iterator iter = taglibs.iterator(); iter.hasNext();){
            String taglib = (String) iter.next();
            out.println("\t\t// Processing taglib " + taglib + ":");
            processTaglib(taglib);
        }
        out.println("\t}\n");        
    }
    
    static void processTaglib(String taglib) {
        String taglibFileName = TAGLIB_BASE + File.separator + taglib;
        Document doc = parseFile(taglibFileName);
        Collection tags = findMatches(doc, "tag-class");
        int count = 0;
        for(Iterator iter = tags.iterator(); iter.hasNext();){
            String tag = (String) iter.next();
            
            out.println("\t\t// " + ++count + ". " + tag);
            out.println("\t\ttry {");
            out.println("\t\t\tTagSupport tag = new " + tag + "();");
            out.println("\t\t\ttag.doStartTag();");
            out.println("\t\t} catch (JspException e){e.printStackTrace();}");
        }
    }

    private static void printServlets(Collection servletNames) {
        out.println("\tpublic static void processServlets() {");
        int count = 0;

        for(Iterator iter = servletNames.iterator(); iter.hasNext();){
            String servlet = (String) iter.next();
            
            out.println("\t\t// " + ++count + ". " + servlet);
            out.println("\t\ttry {");
            out.println("\t\t\tHttpServletRequest request   = new MyHttpServletRequest();");
            out.println("\t\t\tHttpServletResponse response = new MyHttpServletResponse();" + "\n");
            out.println("\t\t\tHttpServlet servlet = new " + servlet + "();");
            
            if(hasMethod(servlet, "init", true)){
                out.println("\t\t\tservlet.init();");    
            }
            
            out.println("\t\t\tservlet.service(request, response);");
            out.println("\t\t} catch (Exception e) {");
            out.println("\t\t\te.printStackTrace();");
            out.println("\t\t}\n");
        }        
        out.println("\t}\n\n");
    }

    static boolean hasMethod(String className, String methodName, boolean publicOnly) {
        Class c;
        try {
            c = Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        Method[] methods = c.getDeclaredMethods();
        for(int i = 0; i < methods.length; i++){
            Method method = methods[i];
            
            if(method.getName().equals(methodName)){
                if( publicOnly && (method.getModifiers() | Modifier.PUBLIC) != 0){
                    return true;
                }
            }
        }
        
        return false;
    }

    private static void printFilters(Collection filterNames) {
        out.println("\tpublic static void processFilters() {");
        int count = 0;
        out.println("\t\tFilterChain chain = new FilterChain(){");
        out.println("\t\t    public void doFilter(ServletRequest req, ServletResponse resp){");
        out.println("\t\t");
        out.println("\t\t    }");
        out.println("\t\t};\n");

        for(Iterator iter = filterNames.iterator(); iter.hasNext();){
            String filter = (String) iter.next();
            
            out.println("\t\t// " + ++count + ". " + filter);
            out.println("\t\ttry {");
            out.println("\t\t\tHttpServletRequest request   = new MyHttpServletRequest();");
            out.println("\t\t\tHttpServletResponse response = new MyHttpServletResponse();" + "\n");
            out.println("\t\t\tFilter filter = new " + filter + "();");
            out.println("\t\t\t//filter.init(...);");
            out.println("\t\t\tfilter.doFilter(request, response, chain);");
            out.println("\t\t\t//filter.destroy();");
            out.println("\t\t} catch (Exception e) {");
            out.println("\t\t\te.printStackTrace();");
            out.println("\t\t}\n");
        }        
        out.println("\t}\n\n");
    }
    
    private static void printListeners(Collection listenerNames) {
        out.println("\tpublic static void processListeners() {");
        int count = 0;

        for(Iterator iter = listenerNames.iterator(); iter.hasNext();){
            String listener = (String) iter.next();
            try {
                Class c = Class.forName(listener);
                Class httpSessionListener = Class.forName("javax.servlet.http.HttpSessionListener");
                Class servletContextListener = Class.forName("javax.servlet.ServletContextListener");
                if(httpSessionListener.isAssignableFrom(c)){
                    out.println("\t\t// " + ++count + ". " + listener);
                    out.println("\t\ttry {");
                    out.println("\t\t\tHttpSessionListener listener = new " + listener + "();");
                    out.println("\t\t\tlistener.sessionCreated(null);");
                    out.println("\t\t\tlistener.sessionDestroyed(null);");
                    out.println("\t\t} catch (Exception e) {");
                    out.println("\t\t\te.printStackTrace();");
                    out.println("\t\t}\n");        
                }else
                if(servletContextListener.isAssignableFrom(c)){
                    out.println("\t\t// " + ++count + ". " + listener);
                    out.println("\t\ttry {");
                    out.println("\t\t\tServletContextListener listener = new " + listener + "();");
                    out.println("\t\t\tlistener.contextInitialized(null);");
                    out.println("\t\t\tlistener.contextDestroyed(null);");
                    out.println("\t\t} catch (Exception e) {");
                    out.println("\t\t\te.printStackTrace();");
                    out.println("\t\t}\n");        
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        out.println("\t}\n\n");
    }
    
    private static void printPreamble() {
        out.println("/* Automatically generated by GenerateWebRoots */\n");
        out.println("import javax.servlet.http.HttpServletRequest;");
        out.println("import javax.servlet.http.HttpServletResponse;");
        out.println("import javax.servlet.ServletRequest;");
        out.println("import javax.servlet.ServletResponse;");        
        out.println("import javax.servlet.http.HttpServlet;");
        out.println("import javax.servlet.ServletContextListener;");
        out.println("import javax.servlet.http.HttpSessionListener;");
        out.println("import javax.servlet.Filter;");
        out.println("import javax.servlet.FilterChain;");
        out.println("import javax.servlet.jsp.JspException;");
        out.println("import javax.servlet.jsp.tagext.TagSupport;");
        out.println("import MyMockLib.MyHttpServletRequest;");
        out.println("import MyMockLib.MyHttpServletResponse;");
        out.println("import java.io.IOException;\n");
        
        out.println("class InvokeServlets {");
        out.println("\tpublic static void main(String[] args) throws IOException {");
        out.println("\t\tprocessServlets();");
        out.println("\t\tprocessFilters();");
        out.println("\t\tprocessTags();");
        out.println("\t}\n");
    }

    public static Document parseFile(String fileName) {
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new File(fileName));

            // normalize text representation
            doc.getDocumentElement().normalize ();
            return doc;
        } catch (SAXParseException err) {
            System.out.println ("** Parsing error" + ", line " + err.getLineNumber () + ", uri " + err.getSystemId ());
            System.out.println(" " + err.getMessage ());
            return null;
        } catch (SAXException e) {
            Exception x = e.getException ();
            ((x == null) ? e : x).printStackTrace ();
            return null;
        } catch (Throwable t) {
            t.printStackTrace ();
            return null;  
        }
    }

    public static Collection findMatches(Document doc, String tagName){
        NodeList servlets = doc.getElementsByTagName(tagName);
        if(TRACE) System.out.println("Total no of sinks: " + servlets.getLength());
        Collection result = new LinkedList();
    
        for(int s=0; s < servlets.getLength() ; s++){
            Element servletNode = (Element) servlets.item(s);
            if(true || servletNode.getNodeType() == Node.TEXT_NODE){                    
                Assert._assert(servletNode != null);
                String className = servletNode.getChildNodes().item(0).getNodeValue().trim();
                
                if(TRACE) System.out.println("Found " + tagName + ": " + className);
                result.add(className);
            }//end of if clause
        }
        
        return result;
    }
}