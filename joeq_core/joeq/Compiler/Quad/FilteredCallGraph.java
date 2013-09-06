/*
 * Created on Aug 15, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package joeq.Compiler.Quad;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import joeq.Class.jq_Method;
import joeq.Compiler.Analysis.IPA.ProgramLocation;
import jwutil.util.Assert;

/**
*
* @author  Chris Unkel <cunkel@stanford.edu>
* @version $Id: FilteredCallGraph.java,v 1.1 2005/08/18 02:01:12 cunkel Exp $
* 
* Filtered call graph.  Useful for excluding joeq, jwutil, etc. from a call graph
* when performing analysis in hosted VM mode.
*/
public class FilteredCallGraph extends CallGraph {
    private final CallGraph base;
    private final Filter filter;
    
    public FilteredCallGraph(CallGraph base, Filter filter) {
        this.base = base;
        this.filter = filter;
    }
    
    public Collection getRoots() {
        return base.getRoots();
    }
    
    public void setRoots(Collection roots) {
        Assert.UNREACHABLE();
    }
    
    public Collection getTargetMethods(Object context, ProgramLocation callSite) {
        Collection targetMethods = base.getTargetMethods(context, callSite);
        LinkedList filteredTargetMethods = new LinkedList();
        for (Iterator i = targetMethods.iterator(); i.hasNext();) {
            Object target = i.next();
            if (filter.acceptTargetMethod(context, callSite, target)) {
                filteredTargetMethods.add(target);
            }
        }
        return filteredTargetMethods;
    }
    
    public static interface Filter {
        public abstract boolean acceptTargetMethod(Object context, ProgramLocation callSite, Object targetMethod);
    }
    
    public static class PackageFilter implements Filter {
        Collection excludedPackages = new LinkedList();
        
        public PackageFilter() {
        }
        
        public void excludePackage(String packageName) {
            if (!packageName.endsWith(".")) {
                packageName = packageName + ".";
            }
            excludedPackages.add(packageName);
        }
        
        public boolean acceptTargetMethod(Object context, ProgramLocation callSite, Object targetMethod) {
            jq_Method callee = (jq_Method) targetMethod;
            jq_Method caller = callSite.getMethod();

            String calleeName = callee.getDeclaringClass().getName().toString();
            String callerName = caller.getDeclaringClass().getName().toString();

            for (Iterator i = excludedPackages.iterator(); i.hasNext(); ) {
                String packageName = (String) i.next();
                if (calleeName.startsWith(packageName)) {
                    return false;
                }
                if (callerName.startsWith(packageName)) {
                    return false;
                }
            }
            
            return true;
        }
    }
}
