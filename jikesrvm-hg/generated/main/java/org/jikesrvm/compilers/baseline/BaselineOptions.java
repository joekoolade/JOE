/*
 *  This file is part of the Jikes RVM project (http://jikesrvm.org).
 *
 *  This file is licensed to You under the Eclipse Public License (EPL);
 *  You may not use this file except in compliance with the License. You
 *  may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/eclipse-1.0.php
 *
 *  See the COPYRIGHT.txt file distributed with this work for information
 *  regarding copyright ownership.
 */
package org.jikesrvm.compilers.baseline;

import org.jikesrvm.VM;

/**
 * Class to handle command-line arguments and options for the
 * baseline compiler.
 * <p>
 * Note: This file is mechanically generated from BaselineOptions.template
 *       and MasterOptions.template
 * <p>
 * Note: Boolean options are defined in /home/jkulig/git/jei/jikesrvm-hg/rvm/src-generated/options/BooleanOptions.baseline.dat /home/jkulig/git/jei/jikesrvm-hg/rvm/src-generated/options/SharedBooleanOptions.dat
 *       All other options are defined in /home/jkulig/git/jei/jikesrvm-hg/rvm/src-generated/options/ValueOptions.baseline.dat /home/jkulig/git/jei/jikesrvm-hg/rvm/src-generated/options/SharedValueOptions.dat
 *       (value, enumeration, bitmask)
 *
 **/
public final class BaselineOptions implements Cloneable {

  private void printOptionsHeader() {
    VM.sysWriteln("Current value of options for Baseline compiler:");
  }

// BEGIN CODE GENERATED FROM MasterOptions.template
/*
 *  This file is part of the Jikes RVM project (http://jikesrvm.org).
 *
 *  This file is licensed to You under the Eclipse Public License (EPL);
 *  You may not use this file except in compliance with the License. You
 *  may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/eclipse-1.0.php
 *
 *  See the COPYRIGHT.txt file distributed with this work for information
 *  regarding copyright ownership.
 */
   // Begin template-specified options
   /** Insert edge counters on all bytecode-level conditional branches */
   public boolean PROFILE_EDGE_COUNTERS         = VM.BuildForAdaptiveSystem;
   /** Select methods for optimized recompilation by using invocation counters */
   public boolean INVOCATION_COUNTERS           = false;
   /** Print method name at start of compilation */
   public boolean PRINT_METHOD                  = false;
   /** Print final machine code */
   public boolean PRINT_MACHINECODE             = false;
    /** File into which to dump edge counter data */
    public String PROFILE_EDGE_COUNTER_FILE      = "EdgeCounters";
    /** Only apply print options against methods whose name contains this string */
    private java.util.HashSet<String> METHOD_TO_PRINT  = null;
   // End template-specified options

   // Begin generated support for "Enumeration" options
   // End generated support for "Enumeration" options

   // Begin generated support for "Set" options
   // METHOD_TO_PRINT
   /**
    * Has the given parameter been added to METHOD_TO_PRINT set of options?
    */
   public boolean isMETHOD_TO_PRINT(String q) {
     return METHOD_TO_PRINT != null && METHOD_TO_PRINT.contains(q);
   }
   /**
    * Does the given parameter appear within a set the String of one of the options?
    */
   public boolean fuzzyMatchMETHOD_TO_PRINT(String q) {
     if (METHOD_TO_PRINT != null) {
       for (final String s : METHOD_TO_PRINT) {
         if (q.indexOf(s) > -1)
           return true;
       }
     }
     return false;
   }
   /**
    * Have any items been placed in the set METHOD_TO_PRINT?
    */
   public boolean hasMETHOD_TO_PRINT() {
     return METHOD_TO_PRINT != null && !METHOD_TO_PRINT.isEmpty();
   }
   /**
    * Return an iterator over the items in METHOD_TO_PRINT
    */
   public java.util.Iterator<String> getMETHOD_TO_PRINTs() {
     if (METHOD_TO_PRINT == null) {
       return org.jikesrvm.util.EmptyIterator.<String>getInstance();
     } else {
       return METHOD_TO_PRINT.iterator();
     }
   }
   // End generated support for "Set" options

   @Override
   @SuppressWarnings("unchecked")
   public Object clone() throws CloneNotSupportedException {
     BaselineOptions clone = (BaselineOptions)super.clone();
     if (METHOD_TO_PRINT != null) {
       clone.METHOD_TO_PRINT = (java.util.HashSet<String>)this.METHOD_TO_PRINT.clone();
     }
     return clone;
   }

  public BaselineOptions dup() {
    try {
      return (BaselineOptions) clone();
    }
    catch (CloneNotSupportedException e) {
      final InternalError error = new InternalError("Unexpected CloneNotSupportedException.");
      error.initCause(e);
      throw error;
    }
  }

  /**
   * Take a string (most likely a command-line argument) and try to proccess it
   * as an option command.  Return true if the string was understood, false
   * otherwise.
   *
   * @param prefix a Sring to use as a command prefix when printing help.
   * @param arg a String to try to process as an option command
   * @return true if successful, false otherwise
   */
  @org.vmmagic.pragma.NoOptCompile
  public boolean processAsOption(String prefix, String arg) {

    // First handle the "option commands"
    if (arg.equals("help")) {
       printHelp(prefix);
       return true;
    }
    if (arg.equals("printOptions")) {
       printOptions();
       return true;
    }
    if (arg.length() == 0) {
      printHelp(prefix);
      return true;
    }
    // Make sure only process O? option if initial runtime compiler!
    if ((prefix.indexOf("irc")!=-1 ||
         prefix.indexOf("bc")!=-1 ||
         prefix.indexOf("eoc")!=-1) &&
        instanceProcessAsOption(arg)) {
      return true;
    }

    // Required format of arg is 'name=value'
    // Split into 'name' and 'value' strings
    int split = arg.indexOf('=');
    if (split == -1) {
      if (!(arg.equals("O0") || arg.equals("O1") || arg.equals("O2") || arg.equals("O3"))) {
        VM.sysWrite("  Illegal option specification!\n  \""+arg+
                      "\" must be specified as a name-value pair in the form of option=value\n");
      }
      return false;
    }
    String name = arg.substring(0,split);
    String value = arg.substring(split+1);

    //Begin generated command-line processing
    if (name.equals("profile_edge_counters")) {
      if (value.equals("true")) {
        PROFILE_EDGE_COUNTERS = true;
        return true;
      } else if (value.equals("false")) {
        PROFILE_EDGE_COUNTERS = false;
        return true;
      } else
        return false;
    }
    if (name.equals("invocation_counters")) {
      if (value.equals("true")) {
        INVOCATION_COUNTERS = true;
        return true;
      } else if (value.equals("false")) {
        INVOCATION_COUNTERS = false;
        return true;
      } else
        return false;
    }
    if (name.equals("verbose")) {
      if (value.equals("true")) {
        PRINT_METHOD = true;
        return true;
      } else if (value.equals("false")) {
        PRINT_METHOD = false;
        return true;
      } else
        return false;
    }
    if (name.equals("mc")) {
      if (value.equals("true")) {
        PRINT_MACHINECODE = true;
        return true;
      } else if (value.equals("false")) {
        PRINT_MACHINECODE = false;
        return true;
      } else
        return false;
    }
    if (name.equals("profile_edge_counter_file")) {
       PROFILE_EDGE_COUNTER_FILE = value;
       return true;
     }
    if (name.equals("method_to_print")) {
       if (METHOD_TO_PRINT == null) {
         METHOD_TO_PRINT = new java.util.HashSet<String>();
       }
       METHOD_TO_PRINT.add(value);
       return true;
     }
       //End generated command-line processing

    // None of the above tests matched, so this wasn't an option
    return false;
  }

  /** Print a short description of every option */
  public static void printHelp(String prefix) {

    instancePrintHelpHeader(prefix);

    //Begin generated help messages
    VM.sysWrite("Boolean Options ("+prefix+"<option>=true or "+prefix+":<option>=false)\n");
    VM.sysWrite("Option                                 Description\n");
    VM.sysWrite("profile_edge_counters                  Insert edge counters on all bytecode-level conditional branches\n");
    VM.sysWrite("invocation_counters                    Select methods for optimized recompilation by using invocation counters\n");
    VM.sysWrite("verbose                                Print method name at start of compilation\n");
    VM.sysWrite("mc                                     Print final machine code\n");
    VM.sysWrite("\nValue Options ("+prefix+"<option>=<value>)\n");
    VM.sysWrite("Option                         Type    Description\n");
    VM.sysWrite("profile_edge_counter_file      String  File into which to dump edge counter data\n");
    VM.sysWrite("\nSelection Options (set option to one of an enumeration of possible values)\n");
    VM.sysWrite("\nSet Options (option is a set of values)\n");
    VM.sysWrite("method_to_print                Only apply print options against methods whose name contains this string\n");
    instancePrintHelpFooter(prefix);

    VM.sysExit(VM.EXIT_STATUS_PRINTED_HELP_MESSAGE);
  }

  /** @return a String representing the options values */
  @Override
  @org.vmmagic.pragma.NoOptCompile
  public String toString() {
    StringBuilder result = new StringBuilder();

    // Begin generated option value printing
    result.append("\tprofile_edge_counters          = ").append(PROFILE_EDGE_COUNTERS).append("\n");
    result.append("\tinvocation_counters            = ").append(INVOCATION_COUNTERS).append("\n");
    result.append("\tverbose                        = ").append(PRINT_METHOD).append("\n");
    result.append("\tmc                             = ").append(PRINT_MACHINECODE).append("\n");
    result.append("\tprofile_edge_counter_file      = ").append(PROFILE_EDGE_COUNTER_FILE).append("\n");
    {
      String val = (METHOD_TO_PRINT==null)?"[]":METHOD_TO_PRINT.toString();
      result.append("\tmethod_to_print                = ").append(val).append("\n");
    }
    return result.toString();
    //End generated toString()
  }

  /** print a String value of this options object */
  @org.vmmagic.pragma.NoOptCompile
  public void printOptions() {
    printOptionsHeader();

    // Begin generated option value printing
    VM.sysWriteln("\tprofile_edge_counters          = ",PROFILE_EDGE_COUNTERS);
    VM.sysWriteln("\tinvocation_counters            = ",INVOCATION_COUNTERS);
    VM.sysWriteln("\tverbose                        = ",PRINT_METHOD);
    VM.sysWriteln("\tmc                             = ",PRINT_MACHINECODE);
    VM.sysWriteln("\tprofile_edge_counter_file      = ",PROFILE_EDGE_COUNTER_FILE);
    {
      String val = (METHOD_TO_PRINT==null)?"[]":METHOD_TO_PRINT.toString();
      VM.sysWriteln("\tmethod_to_print                = ", val);
    }
    //End generated option value printing
  }
// END CODE GENERATED FROM MasterOptions.template

  private boolean instanceProcessAsOption(String arg) {
    return false;
  }

  private static void instancePrintHelpHeader(String prefix) {
    VM.sysWrite("Commands\n");
    VM.sysWrite(prefix+"[:help]\t\t\tPrint brief description of baseline compiler's command-line arguments\n");
    VM.sysWrite(prefix+":printOptions\t\tPrint the current values of the active baseline compiler options\n");
    VM.sysWrite("\n");
  }

  private static void instancePrintHelpFooter(String prefix) {
  }
}
