/* NameFinder.java -- Translates addresses to StackTraceElements.
 Copyright (C) 2002 Free Software Foundation, Inc.

 This file is part of libgcj.

 This software is copyrighted work licensed under the terms of the
 Libgcj License.  Please consult the file "LIBGCJ_LICENSE" for
 details.  */

package gnu.gcj.runtime;

import gnu.gcj.RawData;

/**
 * Helper class that translates addresses (represented as longs) to a
 * StackTraceElement array.
 * 
 * There are a couple of system properties that can be set to manipulate the
 * result (all default to true):
 * <li>
 * <ul>
 * <code>gnu.gcj.runtime.NameFinder.demangle</code> Whether names should be
 * demangled.
 * </ul>
 * <ul>
 * <code>gnu.gcj.runtime.NameFinder.sanitize</code>
 * </ul>
 * Whether calls to initialize exceptions and starting the runtime system should
 * be removed from the stack trace. Only done when names are demangled.
 * </ul>
 * <ul>
 * <code>gnu.gcj.runtime.NameFinder.remove_unknown</code> Whether calls to
 * unknown functions (class and method names are unknown) should be removed from
 * the stack trace. Only done when the stack is sanitized.
 * </ul>
 * <ul>
 * <code>gnu.gcj.runtime.NameFinder.remove_interpreter</code> Whether runtime
 * interpreter calls (methods in the _Jv_InterpMethod class and functions
 * starting with 'ffi_') should be removed from the stack trace. Only done when
 * the stack is sanitized.
 * </ul>
 * <ul>
 * <code>gnu.gcj.runtime.NameFinder.use_addr2line</code> Whether an external
 * process (addr2line or addr2name.awk) should be used as fallback to convert
 * the addresses to function names when the runtime is unable to do it through
 * <code>dladdr</code>.
 * </ul>
 * </li>
 * 
 * <code>close()</code> should be called to get rid of all resources.
 * 
 * This class is used from <code>java.lang.VMThrowable</code>.
 * 
 * Currently the <code>lookup(long[])</code> method is not thread safe. It can
 * easily be made thread safe by synchronizing access to all external processes
 * when used.
 * 
 * @author Mark Wielaard (mark@klomp.org)
 */
public class NameFinder {

  /**
   * Creates a new NameFinder. Call close to get rid of any resources created
   * while using the <code>lookup</code> methods.
   * 
   * fixme:
   */
  public NameFinder() {
  }

  /**
   * Given an Throwable and a native stacktrace returns an array of
   * StackTraceElement containing class, method, file and linenumbers.
   * 
   * fixme:
   */
  public StackTraceElement[] lookup(Throwable t, RawData addrs, int length) {
      return null;
  }


  /**
   * Returns human readable method name and aguments given a method type
   * signature as known to the interpreter and a classname.
   */
  public static String demangleInterpreterMethod(String m, String cn) {
    int index=0;
    int length=m.length();
    StringBuffer sb=new StringBuffer(length);

    // Figure out the real method name
    if (m.startsWith("<init>")) {
      String className;
      int i=cn.lastIndexOf('.');
      if (i < 0)
        className=cn;
      else
        className=cn.substring(i + 1);
      sb.append(className);
      index+=7;
    } else {
      int i=m.indexOf('(');
      if (i > 0) {
        sb.append(m.substring(0, i));
        index+=i + 1;
      }
    }

    sb.append('(');

    // Demangle the type arguments
    int arrayDepth=0;
    char c=(index < length) ? m.charAt(index) : ')';
    while (c != ')') {
      String type;
      switch (c) {
      case 'B' :
        type="byte";
        break;
      case 'C' :
        type="char";
        break;
      case 'D' :
        type="double";
        break;
      case 'F' :
        type="float";
        break;
      case 'I' :
        type="int";
        break;
      case 'J' :
        type="long";
        break;
      case 'S' :
        type="short";
        break;
      case 'Z' :
        type="boolean";
        break;
      case 'L' :
        int i=m.indexOf(';', index);
        if (i > 0) {
          type=m.substring(index + 1, i);
          index=i;
        } else
          type="<unknown ref>";
        break;
      case '[' :
        type="";
        arrayDepth++;
        break;
      default :
        type="<unknown " + c + '>';
      }
      sb.append(type);

      // Handle arrays
      if (c != '[' && arrayDepth > 0)
        while (arrayDepth > 0) {
          sb.append("[]");
          arrayDepth--;
        }

      index++;
      char nc=(index < length) ? m.charAt(index) : ')';
      if (c != '[' && nc != ')')
        sb.append(", ");
      c=nc;
    }

    // Stop. We are not interested in the return type.
    sb.append(')');
    return sb.toString();
  }

  /**
   * Releases all resources used by this NameFinder.
   * 
   * fixme
   */
  public void close() {
  }
}