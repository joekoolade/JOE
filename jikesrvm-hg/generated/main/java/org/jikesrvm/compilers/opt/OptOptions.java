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
package org.jikesrvm.compilers.opt;

import org.jikesrvm.*;

/**
 * Class to handle command-line arguments and options for the
 * optimizng compiler.
 * <p>
 * Note: This file is mechanically generated from OptOptions.template
 *       and MasterOptions.template
 * <p>
 * Note: Boolean options are defined in /home/jkulig/git/jei/jikesrvm-hg/rvm/src-generated/options/BooleanOptions.opt.dat /home/jkulig/git/jei/jikesrvm-hg/rvm/src-generated/options/SharedBooleanOptions.dat
 *       All other options are defined in /home/jkulig/git/jei/jikesrvm-hg/rvm/src-generated/options/ValueOptions.opt.dat /home/jkulig/git/jei/jikesrvm-hg/rvm/src-generated/options/SharedValueOptions.dat
 *       (value, enumeration, bitmask)
 *
 **/
public class OptOptions implements Cloneable {

  // Non-template instance fields that we don't want
  //  available on the command-line)
  private int OPTIMIZATION_LEVEL = 1;    // The OPT level

  private void printOptionsHeader() {
    VM.sysWrite("Current value of options at optimization level ",OPTIMIZATION_LEVEL, ":\n");
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
   /** Focus compilation effort based on frequency profile data */
   public boolean FREQ_FOCUS_EFFORT             = false;
   /** Should we constrain optimizations by enforcing reads-kill? */
   public boolean READS_KILL                    = false;
   /** Eagerly compute method summaries for flow-insensitive field analysis */
   public boolean FIELD_ANALYSIS                = true;
   /** Inline statically resolvable calls */
   public boolean INLINE                        = true;
   /** Guarded inlining of non-final virtual calls */
   public boolean INLINE_GUARDED                = true;
   /** Speculatively inline non-final interface calls */
   public boolean INLINE_GUARDED_INTERFACES     = true;
   /** Pre-existence based inlining */
   public boolean INLINE_PREEX                  = true;
   /** Simplify operations on integers */
   public boolean SIMPLIFY_INTEGER_OPS          = true;
   /** Simplify operations on longs */
   public boolean SIMPLIFY_LONG_OPS             = true;
   /** Simplify operations on floats */
   public boolean SIMPLIFY_FLOAT_OPS            = true;
   /** Simplify operations on floats */
   public boolean SIMPLIFY_DOUBLE_OPS           = true;
   /** Simplify operations on references */
   public boolean SIMPLIFY_REF_OPS              = true;
   /** Simplify operations on TIBs */
   public boolean SIMPLIFY_TIB_OPS              = true;
   /** Simplify operations on fields */
   public boolean SIMPLIFY_FIELD_OPS            = true;
   /** Chase final fields avoiding loads at runtime */
   public boolean SIMPLIFY_CHASE_FINAL_FIELDS   = true;
   /** Perform local constant propagation */
   public boolean LOCAL_CONSTANT_PROP           = true;
   /** Perform local copy propagation */
   public boolean LOCAL_COPY_PROP               = true;
   /** Perform local common subexpression elimination */
   public boolean LOCAL_CSE                     = true;
   /** Should we try to fold expressions with constants locally? */
   public boolean LOCAL_EXPRESSION_FOLDING      = false;
   /** CFG splitting to create hot traces based on static heuristics */
   public boolean CONTROL_STATIC_SPLITTING      = true;
   /** Turn whiles into untils */
   public boolean CONTROL_TURN_WHILES_INTO_UNTILS= false;
   /** Eagerly compute method summaries for simple escape analysis */
   public boolean ESCAPE_SIMPLE_IPA             = false;
   /** If possible turn aggregates (objects) into variable definition/uses */
   public boolean ESCAPE_SCALAR_REPLACE_AGGREGATES= true;
   /** Try to remove unnecessary monitor operations */
   public boolean ESCAPE_MONITOR_REMOVAL        = true;
   /** Compile the method assuming the invokee is thread-local. Cannot be properly set on command line. */
   public boolean ESCAPE_INVOKEE_THREAD_LOCAL   = false;
   /** Should SSA form be constructed on the HIR? */
   public boolean SSA                           = false;
   /** Should we try to fold expressions with constants in SSA form? */
   public boolean SSA_EXPRESSION_FOLDING        = false;
   /** Eliminate redundant conditional branches */
   public boolean SSA_REDUNDANT_BRANCH_ELIMINATION= true;
   /** Assume PEIs do not throw or state is not observable */
   public boolean SSA_LICM_IGNORE_PEI           = false;
   /** Should we perform redundant load elimination during SSA pass? */
   public boolean SSA_LOAD_ELIMINATION          = false;
   /** Should we coalesce move instructions after leaving SSA? */
   public boolean SSA_COALESCE_AFTER            = false;
   /** Create copies of loops where runtime exceptions are checked prior to entry */
   public boolean SSA_LOOP_VERSIONING           = false;
   /** Split live ranges using LIR SSA pass? */
   public boolean SSA_LIVE_RANGE_SPLITTING      = false;
   /** Perform global code placement */
   public boolean SSA_GCP                       = false;
   /** Perform global code placement */
   public boolean SSA_GCSE                      = false;
   /** When leaving SSA create blocks to avoid renaming variables */
   public boolean SSA_SPLITBLOCK_TO_AVOID_RENAME= false;
   /** When leaving SSA create blocks for local liveness */
   public boolean SSA_SPLITBLOCK_FOR_LOCAL_LIVE = true;
   /** When leaving SSA create blocks to avoid adding code to frequently executed blocks */
   public boolean SSA_SPLITBLOCK_INTO_INFREQUENT= true;
   /** Reorder basic blocks for improved locality and branch prediction */
   public boolean REORDER_CODE                  = true;
   /** Reorder basic blocks using Pettis and Hansen Algo2 */
   public boolean REORDER_CODE_PH               = true;
   /** Inline allocation of scalars and arrays */
   public boolean H2L_INLINE_NEW                = true;
   /** Inline write barriers for generational collectors */
   public boolean H2L_INLINE_WRITE_BARRIER      = true;
   /** Inline primitive write barriers for certain collectors */
   public boolean H2L_INLINE_PRIMITIVE_WRITE_BARRIER= true;
   /** Assert that any callee of this compiled method will not throw exceptions. Cannot be properly set on command line. */
   public boolean H2L_NO_CALLEE_EXCEPTIONS      = false;
   /** Plant virtual calls via the JTOC rather than from the tib of anobject when possible */
   public boolean H2L_CALL_VIA_JTOC             = false;
   /** Store liveness for handlers to improve dependence graph at PEIs */
   public boolean L2M_HANDLER_LIVENESS          = false;
   /** Perform prepass instruction scheduling */
   public boolean L2M_SCHEDULE_PREPASS          = false;
   /** Attempt to coalesce to eliminate register moves? */
   public boolean REGALLOC_COALESCE_MOVES       = true;
   /** Attempt to coalesce stack locations? */
   public boolean REGALLOC_COALESCE_SPILLS      = true;
   /** Perform code transformation to sample instrumentation code. */
   public boolean ADAPTIVE_INSTRUMENTATION_SAMPLING= false;
   /** When performing inst. sampling, should it be done without duplicating code? */
   public boolean ADAPTIVE_NO_DUPLICATION       = false;
   /** Should there be one CBS counter per processor for SMP performance? */
   public boolean ADAPTIVE_PROCESSOR_SPECIFIC_COUNTER= true;
   /** Should yieldpoints be removed from the checking code (requires finite sample interval) */
   public boolean ADAPTIVE_REMOVE_YP_FROM_CHECKING= false;
   /** Insert OSR point at off branch of guarded inlining? */
   public boolean OSR_GUARDED_INLINING          = true;
   /** Use OSR knowledge to drive more aggressive inlining? */
   public boolean OSR_INLINE_POLICY             = true;
   /** Print out compile-time statistics for basic blocks? */
   public boolean PRINT_STATIC_STATS            = false;
   /** Print short message for each compilation phase */
   public boolean PRINT_PHASES                  = false;
   /** Dump the IR after each compiler phase */
   public boolean PRINT_ALL_IR                  = false;
   /** Print detailed report of compile-time inlining decisions */
   public boolean PRINT_DETAILED_INLINE_REPORT  = false;
   /** Print detailed report of compile-time inlining decisions */
   public boolean PRINT_INLINE_REPORT           = false;
   /** Print dominators */
   public boolean PRINT_DOMINATORS              = false;
   /** Print post-dominators */
   public boolean PRINT_POST_DOMINATORS         = false;
   /** Print SSA form */
   public boolean PRINT_SSA                     = false;
   /** Print dependence graph before burs */
   public boolean PRINT_DG_BURS                 = false;
   /** Print dependence graph before prepass scheduling */
   public boolean PRINT_DG_SCHED_PRE            = false;
   /** Print dependence graph before postpass scheduling */
   public boolean PRINT_DG_SCHED_POST           = false;
   /** Print IR after initial generation */
   public boolean PRINT_HIGH                    = false;
   /** Print IR just before conversion to LIR */
   public boolean PRINT_FINAL_HIR               = false;
   /** Print IR after conversion to LIR */
   public boolean PRINT_LOW                     = false;
   /** Print IR just before conversion to MIR */
   public boolean PRINT_FINAL_LIR               = false;
   /** Print IR after conversion to MIR */
   public boolean PRINT_MIR                     = false;
   /** Print IR just before conversion to machine code */
   public boolean PRINT_FINAL_MIR               = false;
   /** Print control flow graph too when IR is printed */
   public boolean PRINT_CFG                     = false;
   /** Print IR after prepass scheduling */
   public boolean PRINT_SCHEDULE_PRE            = false;
   /** Print IR before and after register allocation */
   public boolean PRINT_REGALLOC                = false;
   /** Print IR after expanding calling conventions */
   public boolean PRINT_CALLING_CONVENTIONS     = false;
   /** Print the garbage collection maps */
   public boolean PRINT_GC_MAPS                 = false;
   /** Enable debugging support for final assembly */
   public boolean DEBUG_CODEGEN                 = false;
   /** Enable debugging statements for instrumentation sampling */
   public boolean DEBUG_INSTRU_SAMPLING         = false;
   /** Enable detailed debugging statements for instrumentation sampling */
   public boolean DEBUG_INSTRU_SAMPLING_DETAIL  = false;
   /** Perform noisy global code placement */
   public boolean DEBUG_GCP                     = false;
   /** Print method name at start of compilation */
   public boolean PRINT_METHOD                  = false;
   /** Print final machine code */
   public boolean PRINT_MACHINECODE             = false;
    /** Exclude methods from being opt compiled */
    private java.util.HashSet<String> DRIVER_EXCLUDE   = null;
    /** Only print IR compiled above this level */
    public int PRINT_IR_LEVEL                    = 0;
    /** Input file of edge counter profile data */
    public String PROFILE_EDGE_COUNT_INPUT_FILE  = null;
    /** How to compute block and edge frequencies? */
    public byte PROFILE_FREQUENCY_STRATEGY       = PROFILE_COUNTERS_FREQ;
    /** Cumulative threshold which defines the set of infrequent basic blocks */
    public float PROFILE_INFREQUENT_THRESHOLD    = 0.01f;
    /** Threshold at which a conditional branch is considered to be skewed */
    public double PROFILE_CBS_HOTNESS            = 0.98;
    /** Maximum size of array to replaced with registers by simple escape analysis */
    public int ESCAPE_MAX_ARRAY_SIZE             = 5;
    /** How many rounds of redundant load elimination will we attempt? */
    public int SSA_LOAD_ELIMINATION_ROUNDS       = 3;
    /** Maximum size of block for BURS, larger blocks are split */
    public int L2M_MAX_BLOCK_SIZE                = 300;
    /** Selection of spilling heuristic */
    public byte REGALLOC_SPILL_COST_ESTIMATE     = REGALLOC_BLOCK_COUNT_SPILL_COST;
    /** spill penalty for move instructions */
    public double REGALLOC_SIMPLE_SPILL_COST_MOVE_FACTOR= 1.0;
    /** spill penalty for registers used in memory operands */
    public double REGALLOC_SIMPLE_SPILL_COST_MEMORY_OPERAND_FACTOR= 5.0;
    /** If a tableswitch comprises this many or fewer comparisons convert it into multiple if-then-else style branches */
    public int CONTROL_TABLESWITCH_CUTOFF        = 8;
    /** How many extra instructions will we insert in order to remove a conditional branch? */
    public int CONTROL_COND_MOVE_CUTOFF          = 5;
    /** Unroll loops. Duplicates the loop body 2^n times. */
    public int CONTROL_UNROLL_LOG                = 2;
    /** Upper bound on the number of instructions duplicated per block when trying to create hot traces with static splitting */
    public int CONTROL_STATIC_SPLITTING_MAX_COST = 10;
    /** Don't replace branches with conditional moves if they are outside of the range of 0.5 +- this value */
    public double CONTROL_WELL_PREDICTED_CUTOFF  = 1/6;
    /** Static inlining heuristic: Upper bound on callee size */
    public int INLINE_MAX_TARGET_SIZE            = (4*org.jikesrvm.classloader.NormalMethod.CALL_COST-org.jikesrvm.classloader.NormalMethod.SIMPLE_OPERATION_COST);
    /** Static inlining heuristic: Upper bound on depth of inlining */
    public int INLINE_MAX_INLINE_DEPTH           = 5;
    /** Static inlining heuristic: Always inline callees of this size or smaller */
    public int INLINE_MAX_ALWAYS_INLINE_TARGET_SIZE= (2*org.jikesrvm.classloader.NormalMethod.CALL_COST-org.jikesrvm.classloader.NormalMethod.SIMPLE_OPERATION_COST);
    /** Static inlining heuristic: If root method is already this big, then only inline trivial methods */
    public int INLINE_MASSIVE_METHOD_SIZE        = 2048;
    /** Maximum bonus for reducing the perceived size of a method during inlining. */
    public double INLINE_MAX_ARG_BONUS           = 0.40;
    /** Bonus given to inlining methods that are passed a register of a known precise type. */
    public double INLINE_PRECISE_REG_ARRAY_ARG_BONUS= 0.05;
    /** Bonus given when there's potential to optimize checkstore portion of aastore bytecode on parameter */
    public double INLINE_DECLARED_AASTORED_ARRAY_ARG_BONUS= 0.02;
    /** Bonus given to inlining methods that are passed a register of a known precise type. */
    public double INLINE_PRECISE_REG_CLASS_ARG_BONUS= 0.15;
    /** Bonus given to inlining methods that are passed a register that's known not to be null. */
    public double INLINE_EXTANT_REG_CLASS_ARG_BONUS= 0.05;
    /** Bonus given to inlining methods that are passed an int constant argument */
    public double INLINE_INT_CONST_ARG_BONUS     = 0.05;
    /** Bonus given to inlining methods that are passed a null constant argument */
    public double INLINE_NULL_CONST_ARG_BONUS    = 0.10;
    /** Bonus given to inlining methods that are passed an object constant argument */
    public double INLINE_OBJECT_CONST_ARG_BONUS  = 0.10;
    /** As we inline deeper nested methods what cost (or bonus) do we wish to give to deter (or encourage) nesting of deeper methods? */
    public double INLINE_CALL_DEPTH_COST         = 0.00;
    /** Adaptive inlining heuristic: Upper bound on callee size */
    public int INLINE_AI_MAX_TARGET_SIZE         = (20*org.jikesrvm.classloader.NormalMethod.CALL_COST-org.jikesrvm.classloader.NormalMethod.SIMPLE_OPERATION_COST);
    /** Adaptive inlining heuristc: Minimum fraction of callsite distribution for guarded inlining of a callee */
    public double INLINE_AI_MIN_CALLSITE_FRACTION= 0.4;
    /** Selection of guard mechanism for inlined virtual calls that cannot be statically bound */
    public byte INLINE_GUARD_KIND                = INLINE_GUARD_CODE_PATCH;
    /** Only apply print options against methods whose name contains this string */
    private java.util.HashSet<String> METHOD_TO_PRINT  = null;
   // End template-specified options

   // Begin generated support for "Enumeration" options
   // PROFILE_FREQUENCY_STRATEGY
   public static final byte PROFILE_COUNTERS_FREQ = 0;
   /**
    * Is PROFILE_FREQUENCY_STRATEGY set to PROFILE_COUNTERS_FREQ?
    */
   public final boolean frequencyCounters() {
     return PROFILE_FREQUENCY_STRATEGY == PROFILE_COUNTERS_FREQ;
   }
   public static final byte PROFILE_STATIC_FREQ = 1;
   /**
    * Is PROFILE_FREQUENCY_STRATEGY set to PROFILE_STATIC_FREQ?
    */
   public final boolean staticFrequencyEstimates() {
     return PROFILE_FREQUENCY_STRATEGY == PROFILE_STATIC_FREQ;
   }
   public static final byte PROFILE_DUMB_FREQ = 2;
   /**
    * Is PROFILE_FREQUENCY_STRATEGY set to PROFILE_DUMB_FREQ?
    */
   public final boolean dumbFrequency() {
     return PROFILE_FREQUENCY_STRATEGY == PROFILE_DUMB_FREQ;
   }
   public static final byte PROFILE_INVERSE_COUNTERS_FREQ = 3;
   /**
    * Is PROFILE_FREQUENCY_STRATEGY set to PROFILE_INVERSE_COUNTERS_FREQ?
    */
   public final boolean inverseFrequencyCounters() {
     return PROFILE_FREQUENCY_STRATEGY == PROFILE_INVERSE_COUNTERS_FREQ;
   }

   // REGALLOC_SPILL_COST_ESTIMATE
   public static final byte REGALLOC_SIMPLE_SPILL_COST = 0;
   /**
    * Is REGALLOC_SPILL_COST_ESTIMATE set to REGALLOC_SIMPLE_SPILL_COST?
    */
   public final boolean simpleSpillCost() {
     return REGALLOC_SPILL_COST_ESTIMATE == REGALLOC_SIMPLE_SPILL_COST;
   }
   public static final byte REGALLOC_BRAINDEAD_SPILL_COST = 1;
   /**
    * Is REGALLOC_SPILL_COST_ESTIMATE set to REGALLOC_BRAINDEAD_SPILL_COST?
    */
   public final boolean brainDeadSpillCost() {
     return REGALLOC_SPILL_COST_ESTIMATE == REGALLOC_BRAINDEAD_SPILL_COST;
   }
   public static final byte REGALLOC_BLOCK_COUNT_SPILL_COST = 2;
   /**
    * Is REGALLOC_SPILL_COST_ESTIMATE set to REGALLOC_BLOCK_COUNT_SPILL_COST?
    */
   public final boolean blockCountSpillCost() {
     return REGALLOC_SPILL_COST_ESTIMATE == REGALLOC_BLOCK_COUNT_SPILL_COST;
   }

   // INLINE_GUARD_KIND
   public static final byte INLINE_GUARD_METHOD_TEST = 0;
   /**
    * Is INLINE_GUARD_KIND set to INLINE_GUARD_METHOD_TEST?
    */
   public final boolean guardWithMethodTest() {
     return INLINE_GUARD_KIND == INLINE_GUARD_METHOD_TEST;
   }
   public static final byte INLINE_GUARD_CLASS_TEST = 1;
   /**
    * Is INLINE_GUARD_KIND set to INLINE_GUARD_CLASS_TEST?
    */
   public final boolean guardWithClassTest() {
     return INLINE_GUARD_KIND == INLINE_GUARD_CLASS_TEST;
   }
   public static final byte INLINE_GUARD_CODE_PATCH = 2;
   /**
    * Is INLINE_GUARD_KIND set to INLINE_GUARD_CODE_PATCH?
    */
   public final boolean guardWithCodePatch() {
     return INLINE_GUARD_KIND == INLINE_GUARD_CODE_PATCH;
   }

   // End generated support for "Enumeration" options

   // Begin generated support for "Set" options
   // DRIVER_EXCLUDE
   /**
    * Has the given parameter been added to DRIVER_EXCLUDE set of options?
    */
   public boolean isDRIVER_EXCLUDE(String q) {
     return DRIVER_EXCLUDE != null && DRIVER_EXCLUDE.contains(q);
   }
   /**
    * Does the given parameter appear within a set the String of one of the options?
    */
   public boolean fuzzyMatchDRIVER_EXCLUDE(String q) {
     if (DRIVER_EXCLUDE != null) {
       for (final String s : DRIVER_EXCLUDE) {
         if (q.indexOf(s) > -1)
           return true;
       }
     }
     return false;
   }
   /**
    * Have any items been placed in the set DRIVER_EXCLUDE?
    */
   public boolean hasDRIVER_EXCLUDE() {
     return DRIVER_EXCLUDE != null && !DRIVER_EXCLUDE.isEmpty();
   }
   /**
    * Return an iterator over the items in DRIVER_EXCLUDE
    */
   public java.util.Iterator<String> getDRIVER_EXCLUDEs() {
     if (DRIVER_EXCLUDE == null) {
       return org.jikesrvm.util.EmptyIterator.<String>getInstance();
     } else {
       return DRIVER_EXCLUDE.iterator();
     }
   }
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
     OptOptions clone = (OptOptions)super.clone();
     if (DRIVER_EXCLUDE != null) {
       clone.DRIVER_EXCLUDE = (java.util.HashSet<String>)this.DRIVER_EXCLUDE.clone();
     }
     if (METHOD_TO_PRINT != null) {
       clone.METHOD_TO_PRINT = (java.util.HashSet<String>)this.METHOD_TO_PRINT.clone();
     }
     return clone;
   }

  public OptOptions dup() {
    try {
      return (OptOptions) clone();
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
    if (name.equals("focus_effort")) {
      if (value.equals("true")) {
        FREQ_FOCUS_EFFORT = true;
        return true;
      } else if (value.equals("false")) {
        FREQ_FOCUS_EFFORT = false;
        return true;
      } else
        return false;
    }
    if (name.equals("reads_kill")) {
      if (value.equals("true")) {
        READS_KILL = true;
        return true;
      } else if (value.equals("false")) {
        READS_KILL = false;
        return true;
      } else
        return false;
    }
    if (name.equals("field_analysis")) {
      if (value.equals("true")) {
        FIELD_ANALYSIS = true;
        return true;
      } else if (value.equals("false")) {
        FIELD_ANALYSIS = false;
        return true;
      } else
        return false;
    }
    if (name.equals("inline")) {
      if (value.equals("true")) {
        INLINE = true;
        return true;
      } else if (value.equals("false")) {
        INLINE = false;
        return true;
      } else
        return false;
    }
    if (name.equals("inline_guarded")) {
      if (value.equals("true")) {
        INLINE_GUARDED = true;
        return true;
      } else if (value.equals("false")) {
        INLINE_GUARDED = false;
        return true;
      } else
        return false;
    }
    if (name.equals("inline_guarded_interfaces")) {
      if (value.equals("true")) {
        INLINE_GUARDED_INTERFACES = true;
        return true;
      } else if (value.equals("false")) {
        INLINE_GUARDED_INTERFACES = false;
        return true;
      } else
        return false;
    }
    if (name.equals("inline_preex")) {
      if (value.equals("true")) {
        INLINE_PREEX = true;
        return true;
      } else if (value.equals("false")) {
        INLINE_PREEX = false;
        return true;
      } else
        return false;
    }
    if (name.equals("simplify_integer_ops")) {
      if (value.equals("true")) {
        SIMPLIFY_INTEGER_OPS = true;
        return true;
      } else if (value.equals("false")) {
        SIMPLIFY_INTEGER_OPS = false;
        return true;
      } else
        return false;
    }
    if (name.equals("simplify_long_ops")) {
      if (value.equals("true")) {
        SIMPLIFY_LONG_OPS = true;
        return true;
      } else if (value.equals("false")) {
        SIMPLIFY_LONG_OPS = false;
        return true;
      } else
        return false;
    }
    if (name.equals("simplify_float_ops")) {
      if (value.equals("true")) {
        SIMPLIFY_FLOAT_OPS = true;
        return true;
      } else if (value.equals("false")) {
        SIMPLIFY_FLOAT_OPS = false;
        return true;
      } else
        return false;
    }
    if (name.equals("simplify_double_ops")) {
      if (value.equals("true")) {
        SIMPLIFY_DOUBLE_OPS = true;
        return true;
      } else if (value.equals("false")) {
        SIMPLIFY_DOUBLE_OPS = false;
        return true;
      } else
        return false;
    }
    if (name.equals("simplify_ref_ops")) {
      if (value.equals("true")) {
        SIMPLIFY_REF_OPS = true;
        return true;
      } else if (value.equals("false")) {
        SIMPLIFY_REF_OPS = false;
        return true;
      } else
        return false;
    }
    if (name.equals("simplify_tib_ops")) {
      if (value.equals("true")) {
        SIMPLIFY_TIB_OPS = true;
        return true;
      } else if (value.equals("false")) {
        SIMPLIFY_TIB_OPS = false;
        return true;
      } else
        return false;
    }
    if (name.equals("simplify_field_ops")) {
      if (value.equals("true")) {
        SIMPLIFY_FIELD_OPS = true;
        return true;
      } else if (value.equals("false")) {
        SIMPLIFY_FIELD_OPS = false;
        return true;
      } else
        return false;
    }
    if (name.equals("simplify_chase_final_fields")) {
      if (value.equals("true")) {
        SIMPLIFY_CHASE_FINAL_FIELDS = true;
        return true;
      } else if (value.equals("false")) {
        SIMPLIFY_CHASE_FINAL_FIELDS = false;
        return true;
      } else
        return false;
    }
    if (name.equals("local_constant_prop")) {
      if (value.equals("true")) {
        LOCAL_CONSTANT_PROP = true;
        return true;
      } else if (value.equals("false")) {
        LOCAL_CONSTANT_PROP = false;
        return true;
      } else
        return false;
    }
    if (name.equals("local_copy_prop")) {
      if (value.equals("true")) {
        LOCAL_COPY_PROP = true;
        return true;
      } else if (value.equals("false")) {
        LOCAL_COPY_PROP = false;
        return true;
      } else
        return false;
    }
    if (name.equals("local_cse")) {
      if (value.equals("true")) {
        LOCAL_CSE = true;
        return true;
      } else if (value.equals("false")) {
        LOCAL_CSE = false;
        return true;
      } else
        return false;
    }
    if (name.equals("local_expression_folding")) {
      if (value.equals("true")) {
        LOCAL_EXPRESSION_FOLDING = true;
        return true;
      } else if (value.equals("false")) {
        LOCAL_EXPRESSION_FOLDING = false;
        return true;
      } else
        return false;
    }
    if (name.equals("control_static_splitting")) {
      if (value.equals("true")) {
        CONTROL_STATIC_SPLITTING = true;
        return true;
      } else if (value.equals("false")) {
        CONTROL_STATIC_SPLITTING = false;
        return true;
      } else
        return false;
    }
    if (name.equals("control_unwhile")) {
      if (value.equals("true")) {
        CONTROL_TURN_WHILES_INTO_UNTILS = true;
        return true;
      } else if (value.equals("false")) {
        CONTROL_TURN_WHILES_INTO_UNTILS = false;
        return true;
      } else
        return false;
    }
    if (name.equals("escape_simple_ipa")) {
      if (value.equals("true")) {
        ESCAPE_SIMPLE_IPA = true;
        return true;
      } else if (value.equals("false")) {
        ESCAPE_SIMPLE_IPA = false;
        return true;
      } else
        return false;
    }
    if (name.equals("escape_scalar_replace_aggregates")) {
      if (value.equals("true")) {
        ESCAPE_SCALAR_REPLACE_AGGREGATES = true;
        return true;
      } else if (value.equals("false")) {
        ESCAPE_SCALAR_REPLACE_AGGREGATES = false;
        return true;
      } else
        return false;
    }
    if (name.equals("escape_monitor_removal")) {
      if (value.equals("true")) {
        ESCAPE_MONITOR_REMOVAL = true;
        return true;
      } else if (value.equals("false")) {
        ESCAPE_MONITOR_REMOVAL = false;
        return true;
      } else
        return false;
    }
    if (name.equals("escape_invokee_thread_local")) {
      if (value.equals("true")) {
        ESCAPE_INVOKEE_THREAD_LOCAL = true;
        return true;
      } else if (value.equals("false")) {
        ESCAPE_INVOKEE_THREAD_LOCAL = false;
        return true;
      } else
        return false;
    }
    if (name.equals("ssa")) {
      if (value.equals("true")) {
        SSA = true;
        return true;
      } else if (value.equals("false")) {
        SSA = false;
        return true;
      } else
        return false;
    }
    if (name.equals("ssa_expression_folding")) {
      if (value.equals("true")) {
        SSA_EXPRESSION_FOLDING = true;
        return true;
      } else if (value.equals("false")) {
        SSA_EXPRESSION_FOLDING = false;
        return true;
      } else
        return false;
    }
    if (name.equals("ssa_redundant_branch_elimination")) {
      if (value.equals("true")) {
        SSA_REDUNDANT_BRANCH_ELIMINATION = true;
        return true;
      } else if (value.equals("false")) {
        SSA_REDUNDANT_BRANCH_ELIMINATION = false;
        return true;
      } else
        return false;
    }
    if (name.equals("ssa_licm_ignore_pei")) {
      if (value.equals("true")) {
        SSA_LICM_IGNORE_PEI = true;
        return true;
      } else if (value.equals("false")) {
        SSA_LICM_IGNORE_PEI = false;
        return true;
      } else
        return false;
    }
    if (name.equals("ssa_load_elimination")) {
      if (value.equals("true")) {
        SSA_LOAD_ELIMINATION = true;
        return true;
      } else if (value.equals("false")) {
        SSA_LOAD_ELIMINATION = false;
        return true;
      } else
        return false;
    }
    if (name.equals("ssa_coalesce_after")) {
      if (value.equals("true")) {
        SSA_COALESCE_AFTER = true;
        return true;
      } else if (value.equals("false")) {
        SSA_COALESCE_AFTER = false;
        return true;
      } else
        return false;
    }
    if (name.equals("ssa_loop_versioning")) {
      if (value.equals("true")) {
        SSA_LOOP_VERSIONING = true;
        return true;
      } else if (value.equals("false")) {
        SSA_LOOP_VERSIONING = false;
        return true;
      } else
        return false;
    }
    if (name.equals("ssa_live_range_splitting")) {
      if (value.equals("true")) {
        SSA_LIVE_RANGE_SPLITTING = true;
        return true;
      } else if (value.equals("false")) {
        SSA_LIVE_RANGE_SPLITTING = false;
        return true;
      } else
        return false;
    }
    if (name.equals("ssa_gcp")) {
      if (value.equals("true")) {
        SSA_GCP = true;
        return true;
      } else if (value.equals("false")) {
        SSA_GCP = false;
        return true;
      } else
        return false;
    }
    if (name.equals("ssa_gcse")) {
      if (value.equals("true")) {
        SSA_GCSE = true;
        return true;
      } else if (value.equals("false")) {
        SSA_GCSE = false;
        return true;
      } else
        return false;
    }
    if (name.equals("ssa_splitblock_to_avoid_rename")) {
      if (value.equals("true")) {
        SSA_SPLITBLOCK_TO_AVOID_RENAME = true;
        return true;
      } else if (value.equals("false")) {
        SSA_SPLITBLOCK_TO_AVOID_RENAME = false;
        return true;
      } else
        return false;
    }
    if (name.equals("ssa_splitblock_for_local_live")) {
      if (value.equals("true")) {
        SSA_SPLITBLOCK_FOR_LOCAL_LIVE = true;
        return true;
      } else if (value.equals("false")) {
        SSA_SPLITBLOCK_FOR_LOCAL_LIVE = false;
        return true;
      } else
        return false;
    }
    if (name.equals("ssa_splitblock_into_infrequent")) {
      if (value.equals("true")) {
        SSA_SPLITBLOCK_INTO_INFREQUENT = true;
        return true;
      } else if (value.equals("false")) {
        SSA_SPLITBLOCK_INTO_INFREQUENT = false;
        return true;
      } else
        return false;
    }
    if (name.equals("reorder_code")) {
      if (value.equals("true")) {
        REORDER_CODE = true;
        return true;
      } else if (value.equals("false")) {
        REORDER_CODE = false;
        return true;
      } else
        return false;
    }
    if (name.equals("reorder_code_ph")) {
      if (value.equals("true")) {
        REORDER_CODE_PH = true;
        return true;
      } else if (value.equals("false")) {
        REORDER_CODE_PH = false;
        return true;
      } else
        return false;
    }
    if (name.equals("h2l_inline_new")) {
      if (value.equals("true")) {
        H2L_INLINE_NEW = true;
        return true;
      } else if (value.equals("false")) {
        H2L_INLINE_NEW = false;
        return true;
      } else
        return false;
    }
    if (name.equals("h2l_inline_write_barrier")) {
      if (value.equals("true")) {
        H2L_INLINE_WRITE_BARRIER = true;
        return true;
      } else if (value.equals("false")) {
        H2L_INLINE_WRITE_BARRIER = false;
        return true;
      } else
        return false;
    }
    if (name.equals("h2l_inline_primitive_write_barrier")) {
      if (value.equals("true")) {
        H2L_INLINE_PRIMITIVE_WRITE_BARRIER = true;
        return true;
      } else if (value.equals("false")) {
        H2L_INLINE_PRIMITIVE_WRITE_BARRIER = false;
        return true;
      } else
        return false;
    }
    if (name.equals("h2l_no_callee_exceptions")) {
      if (value.equals("true")) {
        H2L_NO_CALLEE_EXCEPTIONS = true;
        return true;
      } else if (value.equals("false")) {
        H2L_NO_CALLEE_EXCEPTIONS = false;
        return true;
      } else
        return false;
    }
    if (name.equals("h2l_call_via_jtoc")) {
      if (value.equals("true")) {
        H2L_CALL_VIA_JTOC = true;
        return true;
      } else if (value.equals("false")) {
        H2L_CALL_VIA_JTOC = false;
        return true;
      } else
        return false;
    }
    if (name.equals("l2m_handler_liveness")) {
      if (value.equals("true")) {
        L2M_HANDLER_LIVENESS = true;
        return true;
      } else if (value.equals("false")) {
        L2M_HANDLER_LIVENESS = false;
        return true;
      } else
        return false;
    }
    if (name.equals("l2m_schedule_prepass")) {
      if (value.equals("true")) {
        L2M_SCHEDULE_PREPASS = true;
        return true;
      } else if (value.equals("false")) {
        L2M_SCHEDULE_PREPASS = false;
        return true;
      } else
        return false;
    }
    if (name.equals("regalloc_coalesce_moves")) {
      if (value.equals("true")) {
        REGALLOC_COALESCE_MOVES = true;
        return true;
      } else if (value.equals("false")) {
        REGALLOC_COALESCE_MOVES = false;
        return true;
      } else
        return false;
    }
    if (name.equals("regalloc_coalesce_spills")) {
      if (value.equals("true")) {
        REGALLOC_COALESCE_SPILLS = true;
        return true;
      } else if (value.equals("false")) {
        REGALLOC_COALESCE_SPILLS = false;
        return true;
      } else
        return false;
    }
    if (name.equals("adaptive_instrumentation_sampling")) {
      if (value.equals("true")) {
        ADAPTIVE_INSTRUMENTATION_SAMPLING = true;
        return true;
      } else if (value.equals("false")) {
        ADAPTIVE_INSTRUMENTATION_SAMPLING = false;
        return true;
      } else
        return false;
    }
    if (name.equals("adaptive_no_duplication")) {
      if (value.equals("true")) {
        ADAPTIVE_NO_DUPLICATION = true;
        return true;
      } else if (value.equals("false")) {
        ADAPTIVE_NO_DUPLICATION = false;
        return true;
      } else
        return false;
    }
    if (name.equals("adaptive_processor_specific_counter")) {
      if (value.equals("true")) {
        ADAPTIVE_PROCESSOR_SPECIFIC_COUNTER = true;
        return true;
      } else if (value.equals("false")) {
        ADAPTIVE_PROCESSOR_SPECIFIC_COUNTER = false;
        return true;
      } else
        return false;
    }
    if (name.equals("adaptive_remove_yp_from_checking")) {
      if (value.equals("true")) {
        ADAPTIVE_REMOVE_YP_FROM_CHECKING = true;
        return true;
      } else if (value.equals("false")) {
        ADAPTIVE_REMOVE_YP_FROM_CHECKING = false;
        return true;
      } else
        return false;
    }
    if (name.equals("osr_guarded_inlining")) {
      if (value.equals("true")) {
        OSR_GUARDED_INLINING = true;
        return true;
      } else if (value.equals("false")) {
        OSR_GUARDED_INLINING = false;
        return true;
      } else
        return false;
    }
    if (name.equals("osr_inline_policy")) {
      if (value.equals("true")) {
        OSR_INLINE_POLICY = true;
        return true;
      } else if (value.equals("false")) {
        OSR_INLINE_POLICY = false;
        return true;
      } else
        return false;
    }
    if (name.equals("print_static_stats")) {
      if (value.equals("true")) {
        PRINT_STATIC_STATS = true;
        return true;
      } else if (value.equals("false")) {
        PRINT_STATIC_STATS = false;
        return true;
      } else
        return false;
    }
    if (name.equals("print_phases")) {
      if (value.equals("true")) {
        PRINT_PHASES = true;
        return true;
      } else if (value.equals("false")) {
        PRINT_PHASES = false;
        return true;
      } else
        return false;
    }
    if (name.equals("print_all_ir")) {
      if (value.equals("true")) {
        PRINT_ALL_IR = true;
        return true;
      } else if (value.equals("false")) {
        PRINT_ALL_IR = false;
        return true;
      } else
        return false;
    }
    if (name.equals("print_detailed_inline_report")) {
      if (value.equals("true")) {
        PRINT_DETAILED_INLINE_REPORT = true;
        return true;
      } else if (value.equals("false")) {
        PRINT_DETAILED_INLINE_REPORT = false;
        return true;
      } else
        return false;
    }
    if (name.equals("print_inline_report")) {
      if (value.equals("true")) {
        PRINT_INLINE_REPORT = true;
        return true;
      } else if (value.equals("false")) {
        PRINT_INLINE_REPORT = false;
        return true;
      } else
        return false;
    }
    if (name.equals("print_dom")) {
      if (value.equals("true")) {
        PRINT_DOMINATORS = true;
        return true;
      } else if (value.equals("false")) {
        PRINT_DOMINATORS = false;
        return true;
      } else
        return false;
    }
    if (name.equals("print_pdom")) {
      if (value.equals("true")) {
        PRINT_POST_DOMINATORS = true;
        return true;
      } else if (value.equals("false")) {
        PRINT_POST_DOMINATORS = false;
        return true;
      } else
        return false;
    }
    if (name.equals("print_ssa")) {
      if (value.equals("true")) {
        PRINT_SSA = true;
        return true;
      } else if (value.equals("false")) {
        PRINT_SSA = false;
        return true;
      } else
        return false;
    }
    if (name.equals("print_dg_burs")) {
      if (value.equals("true")) {
        PRINT_DG_BURS = true;
        return true;
      } else if (value.equals("false")) {
        PRINT_DG_BURS = false;
        return true;
      } else
        return false;
    }
    if (name.equals("print_dg_sched_pre")) {
      if (value.equals("true")) {
        PRINT_DG_SCHED_PRE = true;
        return true;
      } else if (value.equals("false")) {
        PRINT_DG_SCHED_PRE = false;
        return true;
      } else
        return false;
    }
    if (name.equals("print_dg_sched_post")) {
      if (value.equals("true")) {
        PRINT_DG_SCHED_POST = true;
        return true;
      } else if (value.equals("false")) {
        PRINT_DG_SCHED_POST = false;
        return true;
      } else
        return false;
    }
    if (name.equals("print_high")) {
      if (value.equals("true")) {
        PRINT_HIGH = true;
        return true;
      } else if (value.equals("false")) {
        PRINT_HIGH = false;
        return true;
      } else
        return false;
    }
    if (name.equals("print_final_hir")) {
      if (value.equals("true")) {
        PRINT_FINAL_HIR = true;
        return true;
      } else if (value.equals("false")) {
        PRINT_FINAL_HIR = false;
        return true;
      } else
        return false;
    }
    if (name.equals("print_low")) {
      if (value.equals("true")) {
        PRINT_LOW = true;
        return true;
      } else if (value.equals("false")) {
        PRINT_LOW = false;
        return true;
      } else
        return false;
    }
    if (name.equals("print_final_lir")) {
      if (value.equals("true")) {
        PRINT_FINAL_LIR = true;
        return true;
      } else if (value.equals("false")) {
        PRINT_FINAL_LIR = false;
        return true;
      } else
        return false;
    }
    if (name.equals("print_mir")) {
      if (value.equals("true")) {
        PRINT_MIR = true;
        return true;
      } else if (value.equals("false")) {
        PRINT_MIR = false;
        return true;
      } else
        return false;
    }
    if (name.equals("print_final_mir")) {
      if (value.equals("true")) {
        PRINT_FINAL_MIR = true;
        return true;
      } else if (value.equals("false")) {
        PRINT_FINAL_MIR = false;
        return true;
      } else
        return false;
    }
    if (name.equals("print_cfg")) {
      if (value.equals("true")) {
        PRINT_CFG = true;
        return true;
      } else if (value.equals("false")) {
        PRINT_CFG = false;
        return true;
      } else
        return false;
    }
    if (name.equals("print_schedule_pre")) {
      if (value.equals("true")) {
        PRINT_SCHEDULE_PRE = true;
        return true;
      } else if (value.equals("false")) {
        PRINT_SCHEDULE_PRE = false;
        return true;
      } else
        return false;
    }
    if (name.equals("print_regalloc")) {
      if (value.equals("true")) {
        PRINT_REGALLOC = true;
        return true;
      } else if (value.equals("false")) {
        PRINT_REGALLOC = false;
        return true;
      } else
        return false;
    }
    if (name.equals("print_calling_conventions")) {
      if (value.equals("true")) {
        PRINT_CALLING_CONVENTIONS = true;
        return true;
      } else if (value.equals("false")) {
        PRINT_CALLING_CONVENTIONS = false;
        return true;
      } else
        return false;
    }
    if (name.equals("print_gc_maps")) {
      if (value.equals("true")) {
        PRINT_GC_MAPS = true;
        return true;
      } else if (value.equals("false")) {
        PRINT_GC_MAPS = false;
        return true;
      } else
        return false;
    }
    if (name.equals("debug_cgd")) {
      if (value.equals("true")) {
        DEBUG_CODEGEN = true;
        return true;
      } else if (value.equals("false")) {
        DEBUG_CODEGEN = false;
        return true;
      } else
        return false;
    }
    if (name.equals("debug_instru_sampling")) {
      if (value.equals("true")) {
        DEBUG_INSTRU_SAMPLING = true;
        return true;
      } else if (value.equals("false")) {
        DEBUG_INSTRU_SAMPLING = false;
        return true;
      } else
        return false;
    }
    if (name.equals("debug_instru_sampling_detail")) {
      if (value.equals("true")) {
        DEBUG_INSTRU_SAMPLING_DETAIL = true;
        return true;
      } else if (value.equals("false")) {
        DEBUG_INSTRU_SAMPLING_DETAIL = false;
        return true;
      } else
        return false;
    }
    if (name.equals("debug_gcp")) {
      if (value.equals("true")) {
        DEBUG_GCP = true;
        return true;
      } else if (value.equals("false")) {
        DEBUG_GCP = false;
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
    if (name.equals("driver_exclude")) {
       if (DRIVER_EXCLUDE == null) {
         DRIVER_EXCLUDE = new java.util.HashSet<String>();
       }
       DRIVER_EXCLUDE.add(value);
       return true;
     }
    if (name.equals("print_ir_level")) {
       PRINT_IR_LEVEL = CommandLineArgs.primitiveParseInt(value);
       return true;
     }
    if (name.equals("profile_edge_count_input_file")) {
       PROFILE_EDGE_COUNT_INPUT_FILE = value;
       return true;
     }
    if (name.equals("profile_frequency_strategy")) {
       if (value.equals("counters")) {
         PROFILE_FREQUENCY_STRATEGY = PROFILE_COUNTERS_FREQ;
         return true;
       }
       if (value.equals("static")) {
         PROFILE_FREQUENCY_STRATEGY = PROFILE_STATIC_FREQ;
         return true;
       }
       if (value.equals("dumb")) {
         PROFILE_FREQUENCY_STRATEGY = PROFILE_DUMB_FREQ;
         return true;
       }
       if (value.equals("inverse")) {
         PROFILE_FREQUENCY_STRATEGY = PROFILE_INVERSE_COUNTERS_FREQ;
         return true;
       }
       return false;
     }
    if (name.equals("profile_infrequent_threshold")) {
       PROFILE_INFREQUENT_THRESHOLD = CommandLineArgs.primitiveParseFloat(value);
       return true;
     }
    if (name.equals("profile_cbs_hotness")) {
       PROFILE_CBS_HOTNESS = CommandLineArgs.primitiveParseFloat(value);
       return true;
     }
    if (name.equals("escape_max_array_size")) {
       ESCAPE_MAX_ARRAY_SIZE = CommandLineArgs.primitiveParseInt(value);
       return true;
     }
    if (name.equals("ssa_load_elimination_rounds")) {
       SSA_LOAD_ELIMINATION_ROUNDS = CommandLineArgs.primitiveParseInt(value);
       return true;
     }
    if (name.equals("l2m_max_block_size")) {
       L2M_MAX_BLOCK_SIZE = CommandLineArgs.primitiveParseInt(value);
       return true;
     }
    if (name.equals("regalloc_spill_cost_estimate")) {
       if (value.equals("simple")) {
         REGALLOC_SPILL_COST_ESTIMATE = REGALLOC_SIMPLE_SPILL_COST;
         return true;
       }
       if (value.equals("brainDead")) {
         REGALLOC_SPILL_COST_ESTIMATE = REGALLOC_BRAINDEAD_SPILL_COST;
         return true;
       }
       if (value.equals("blockCount")) {
         REGALLOC_SPILL_COST_ESTIMATE = REGALLOC_BLOCK_COUNT_SPILL_COST;
         return true;
       }
       return false;
     }
    if (name.equals("regalloc_simple_spill_cost_move_factor")) {
       REGALLOC_SIMPLE_SPILL_COST_MOVE_FACTOR = CommandLineArgs.primitiveParseFloat(value);
       return true;
     }
    if (name.equals("regalloc_simple_spill_cost_memory_operand_factor")) {
       REGALLOC_SIMPLE_SPILL_COST_MEMORY_OPERAND_FACTOR = CommandLineArgs.primitiveParseFloat(value);
       return true;
     }
    if (name.equals("control_tableswitch_cutoff")) {
       CONTROL_TABLESWITCH_CUTOFF = CommandLineArgs.primitiveParseInt(value);
       return true;
     }
    if (name.equals("control_cond_move_cutoff")) {
       CONTROL_COND_MOVE_CUTOFF = CommandLineArgs.primitiveParseInt(value);
       return true;
     }
    if (name.equals("control_unroll_log")) {
       CONTROL_UNROLL_LOG = CommandLineArgs.primitiveParseInt(value);
       return true;
     }
    if (name.equals("control_static_splitting_max_cost")) {
       CONTROL_STATIC_SPLITTING_MAX_COST = CommandLineArgs.primitiveParseInt(value);
       return true;
     }
    if (name.equals("control_well_predicted_cutoff")) {
       CONTROL_WELL_PREDICTED_CUTOFF = CommandLineArgs.primitiveParseFloat(value);
       return true;
     }
    if (name.equals("inline_max_target_size")) {
       INLINE_MAX_TARGET_SIZE = CommandLineArgs.primitiveParseInt(value);
       return true;
     }
    if (name.equals("inline_max_inline_depth")) {
       INLINE_MAX_INLINE_DEPTH = CommandLineArgs.primitiveParseInt(value);
       return true;
     }
    if (name.equals("inline_max_always_inline_target_size")) {
       INLINE_MAX_ALWAYS_INLINE_TARGET_SIZE = CommandLineArgs.primitiveParseInt(value);
       return true;
     }
    if (name.equals("inline_massive_method_size")) {
       INLINE_MASSIVE_METHOD_SIZE = CommandLineArgs.primitiveParseInt(value);
       return true;
     }
    if (name.equals("inline_max_arg_bonus")) {
       INLINE_MAX_ARG_BONUS = CommandLineArgs.primitiveParseFloat(value);
       return true;
     }
    if (name.equals("inline_precise_reg_array_arg_bonus")) {
       INLINE_PRECISE_REG_ARRAY_ARG_BONUS = CommandLineArgs.primitiveParseFloat(value);
       return true;
     }
    if (name.equals("inline_declared_aastored_array_arg_bonus")) {
       INLINE_DECLARED_AASTORED_ARRAY_ARG_BONUS = CommandLineArgs.primitiveParseFloat(value);
       return true;
     }
    if (name.equals("inline_precise_reg_class_arg_bonus")) {
       INLINE_PRECISE_REG_CLASS_ARG_BONUS = CommandLineArgs.primitiveParseFloat(value);
       return true;
     }
    if (name.equals("inline_extant_reg_class_arg_bonus")) {
       INLINE_EXTANT_REG_CLASS_ARG_BONUS = CommandLineArgs.primitiveParseFloat(value);
       return true;
     }
    if (name.equals("inline_int_const_arg_bonus")) {
       INLINE_INT_CONST_ARG_BONUS = CommandLineArgs.primitiveParseFloat(value);
       return true;
     }
    if (name.equals("inline_null_const_arg_bonus")) {
       INLINE_NULL_CONST_ARG_BONUS = CommandLineArgs.primitiveParseFloat(value);
       return true;
     }
    if (name.equals("inline_object_const_arg_bonus")) {
       INLINE_OBJECT_CONST_ARG_BONUS = CommandLineArgs.primitiveParseFloat(value);
       return true;
     }
    if (name.equals("inline_call_depth_cost")) {
       INLINE_CALL_DEPTH_COST = CommandLineArgs.primitiveParseFloat(value);
       return true;
     }
    if (name.equals("inline_ai_max_target_size")) {
       INLINE_AI_MAX_TARGET_SIZE = CommandLineArgs.primitiveParseInt(value);
       return true;
     }
    if (name.equals("inline_ai_min_callsite_fraction")) {
       INLINE_AI_MIN_CALLSITE_FRACTION = CommandLineArgs.primitiveParseFloat(value);
       return true;
     }
    if (name.equals("inline_guard_kind")) {
       if (value.equals("inline_guard_method_test")) {
         INLINE_GUARD_KIND = INLINE_GUARD_METHOD_TEST;
         return true;
       }
       if (value.equals("inline_guard_class_test")) {
         INLINE_GUARD_KIND = INLINE_GUARD_CLASS_TEST;
         return true;
       }
       if (value.equals("inline_guard_code_patch")) {
         INLINE_GUARD_KIND = INLINE_GUARD_CODE_PATCH;
         return true;
       }
       return false;
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
    VM.sysWrite("Option                        OptLevel Description\n");
    VM.sysWrite("focus_effort                           Focus compilation effort based on frequency profile data\n");
    VM.sysWrite("reads_kill                             Should we constrain optimizations by enforcing reads-kill?\n");
    VM.sysWrite("field_analysis                 0       Eagerly compute method summaries for flow-insensitive field analysis\n");
    VM.sysWrite("inline                         0       Inline statically resolvable calls\n");
    VM.sysWrite("inline_guarded                 0       Guarded inlining of non-final virtual calls\n");
    VM.sysWrite("inline_guarded_interfaces      0       Speculatively inline non-final interface calls\n");
    VM.sysWrite("inline_preex                   0       Pre-existence based inlining\n");
    VM.sysWrite("simplify_integer_ops                   Simplify operations on integers\n");
    VM.sysWrite("simplify_long_ops                      Simplify operations on longs\n");
    VM.sysWrite("simplify_float_ops                     Simplify operations on floats\n");
    VM.sysWrite("simplify_double_ops                    Simplify operations on floats\n");
    VM.sysWrite("simplify_ref_ops                       Simplify operations on references\n");
    VM.sysWrite("simplify_tib_ops                       Simplify operations on TIBs\n");
    VM.sysWrite("simplify_field_ops                     Simplify operations on fields\n");
    VM.sysWrite("simplify_chase_final_fields            Chase final fields avoiding loads at runtime\n");
    VM.sysWrite("local_constant_prop            0       Perform local constant propagation\n");
    VM.sysWrite("local_copy_prop                0       Perform local copy propagation\n");
    VM.sysWrite("local_cse                      0       Perform local common subexpression elimination\n");
    VM.sysWrite("local_expression_folding       3       Should we try to fold expressions with constants locally?\n");
    VM.sysWrite("control_static_splitting       1       CFG splitting to create hot traces based on static heuristics\n");
    VM.sysWrite("control_unwhile                3       Turn whiles into untils\n");
    VM.sysWrite("escape_simple_ipa                      Eagerly compute method summaries for simple escape analysis\n");
    VM.sysWrite("escape_scalar_replace_aggregates 1       If possible turn aggregates (objects) into variable definition/uses\n");
    VM.sysWrite("escape_monitor_removal         1       Try to remove unnecessary monitor operations\n");
    VM.sysWrite("escape_invokee_thread_local            Compile the method assuming the invokee is thread-local. Cannot be properly set on command line.\n");
    VM.sysWrite("ssa                            3       Should SSA form be constructed on the HIR?\n");
    VM.sysWrite("ssa_expression_folding         3       Should we try to fold expressions with constants in SSA form?\n");
    VM.sysWrite("ssa_redundant_branch_elimination 3       Eliminate redundant conditional branches\n");
    VM.sysWrite("ssa_licm_ignore_pei                    Assume PEIs do not throw or state is not observable\n");
    VM.sysWrite("ssa_load_elimination           3       Should we perform redundant load elimination during SSA pass?\n");
    VM.sysWrite("ssa_coalesce_after             3       Should we coalesce move instructions after leaving SSA?\n");
    VM.sysWrite("ssa_loop_versioning                    Create copies of loops where runtime exceptions are checked prior to entry\n");
    VM.sysWrite("ssa_live_range_splitting               Split live ranges using LIR SSA pass?\n");
    VM.sysWrite("ssa_gcp                        3       Perform global code placement\n");
    VM.sysWrite("ssa_gcse                       3       Perform global code placement\n");
    VM.sysWrite("ssa_splitblock_to_avoid_rename         When leaving SSA create blocks to avoid renaming variables\n");
    VM.sysWrite("ssa_splitblock_for_local_live          When leaving SSA create blocks for local liveness\n");
    VM.sysWrite("ssa_splitblock_into_infrequent         When leaving SSA create blocks to avoid adding code to frequently executed blocks\n");
    VM.sysWrite("reorder_code                   0       Reorder basic blocks for improved locality and branch prediction\n");
    VM.sysWrite("reorder_code_ph                1       Reorder basic blocks using Pettis and Hansen Algo2\n");
    VM.sysWrite("h2l_inline_new                 0       Inline allocation of scalars and arrays\n");
    VM.sysWrite("h2l_inline_write_barrier       1       Inline write barriers for generational collectors\n");
    VM.sysWrite("h2l_inline_primitive_write_barrier 1       Inline primitive write barriers for certain collectors\n");
    VM.sysWrite("h2l_no_callee_exceptions               Assert that any callee of this compiled method will not throw exceptions. Cannot be properly set on command line.\n");
    VM.sysWrite("h2l_call_via_jtoc                      Plant virtual calls via the JTOC rather than from the tib of anobject when possible\n");
    VM.sysWrite("l2m_handler_liveness           2       Store liveness for handlers to improve dependence graph at PEIs\n");
    VM.sysWrite("l2m_schedule_prepass                   Perform prepass instruction scheduling\n");
    VM.sysWrite("regalloc_coalesce_moves        0       Attempt to coalesce to eliminate register moves?\n");
    VM.sysWrite("regalloc_coalesce_spills       0       Attempt to coalesce stack locations?\n");
    VM.sysWrite("adaptive_instrumentation_sampling         Perform code transformation to sample instrumentation code.\n");
    VM.sysWrite("adaptive_no_duplication                When performing inst. sampling, should it be done without duplicating code?\n");
    VM.sysWrite("adaptive_processor_specific_counter         Should there be one CBS counter per processor for SMP performance?\n");
    VM.sysWrite("adaptive_remove_yp_from_checking         Should yieldpoints be removed from the checking code (requires finite sample interval)\n");
    VM.sysWrite("osr_guarded_inlining           1       Insert OSR point at off branch of guarded inlining?\n");
    VM.sysWrite("osr_inline_policy              1       Use OSR knowledge to drive more aggressive inlining?\n");
    VM.sysWrite("print_static_stats                     Print out compile-time statistics for basic blocks?\n");
    VM.sysWrite("print_phases                           Print short message for each compilation phase\n");
    VM.sysWrite("print_all_ir                           Dump the IR after each compiler phase\n");
    VM.sysWrite("print_detailed_inline_report           Print detailed report of compile-time inlining decisions\n");
    VM.sysWrite("print_inline_report                    Print detailed report of compile-time inlining decisions\n");
    VM.sysWrite("print_dom                              Print dominators\n");
    VM.sysWrite("print_pdom                             Print post-dominators\n");
    VM.sysWrite("print_ssa                              Print SSA form\n");
    VM.sysWrite("print_dg_burs                          Print dependence graph before burs\n");
    VM.sysWrite("print_dg_sched_pre                     Print dependence graph before prepass scheduling\n");
    VM.sysWrite("print_dg_sched_post                    Print dependence graph before postpass scheduling\n");
    VM.sysWrite("print_high                             Print IR after initial generation\n");
    VM.sysWrite("print_final_hir                        Print IR just before conversion to LIR\n");
    VM.sysWrite("print_low                              Print IR after conversion to LIR\n");
    VM.sysWrite("print_final_lir                        Print IR just before conversion to MIR\n");
    VM.sysWrite("print_mir                              Print IR after conversion to MIR\n");
    VM.sysWrite("print_final_mir                        Print IR just before conversion to machine code\n");
    VM.sysWrite("print_cfg                              Print control flow graph too when IR is printed\n");
    VM.sysWrite("print_schedule_pre                     Print IR after prepass scheduling\n");
    VM.sysWrite("print_regalloc                         Print IR before and after register allocation\n");
    VM.sysWrite("print_calling_conventions              Print IR after expanding calling conventions\n");
    VM.sysWrite("print_gc_maps                          Print the garbage collection maps\n");
    VM.sysWrite("debug_cgd                              Enable debugging support for final assembly\n");
    VM.sysWrite("debug_instru_sampling                  Enable debugging statements for instrumentation sampling\n");
    VM.sysWrite("debug_instru_sampling_detail           Enable detailed debugging statements for instrumentation sampling\n");
    VM.sysWrite("debug_gcp                              Perform noisy global code placement\n");
    VM.sysWrite("verbose                                Print method name at start of compilation\n");
    VM.sysWrite("mc                                     Print final machine code\n");
    VM.sysWrite("\nValue Options ("+prefix+"<option>=<value>)\n");
    VM.sysWrite("Option                         Type    Description\n");
    VM.sysWrite("print_ir_level                 int     Only print IR compiled above this level\n");
    VM.sysWrite("profile_edge_count_input_file  String  Input file of edge counter profile data\n");
    VM.sysWrite("profile_infrequent_threshold   float   Cumulative threshold which defines the set of infrequent basic blocks\n");
    VM.sysWrite("profile_cbs_hotness            double  Threshold at which a conditional branch is considered to be skewed\n");
    VM.sysWrite("escape_max_array_size          int     Maximum size of array to replaced with registers by simple escape analysis\n");
    VM.sysWrite("ssa_load_elimination_rounds    int     How many rounds of redundant load elimination will we attempt?\n");
    VM.sysWrite("l2m_max_block_size             int     Maximum size of block for BURS, larger blocks are split\n");
    VM.sysWrite("regalloc_simple_spill_cost_move_factor double  spill penalty for move instructions\n");
    VM.sysWrite("regalloc_simple_spill_cost_memory_operand_factor double  spill penalty for registers used in memory operands\n");
    VM.sysWrite("control_tableswitch_cutoff     int     If a tableswitch comprises this many or fewer comparisons convert it into multiple if-then-else style branches\n");
    VM.sysWrite("control_cond_move_cutoff       int     How many extra instructions will we insert in order to remove a conditional branch?\n");
    VM.sysWrite("control_unroll_log             int     Unroll loops. Duplicates the loop body 2^n times.\n");
    VM.sysWrite("control_static_splitting_max_cost int     Upper bound on the number of instructions duplicated per block when trying to create hot traces with static splitting\n");
    VM.sysWrite("control_well_predicted_cutoff  double  Don't replace branches with conditional moves if they are outside of the range of 0.5 +- this value\n");
    VM.sysWrite("inline_max_target_size         int     Static inlining heuristic: Upper bound on callee size\n");
    VM.sysWrite("inline_max_inline_depth        int     Static inlining heuristic: Upper bound on depth of inlining\n");
    VM.sysWrite("inline_max_always_inline_target_size int     Static inlining heuristic: Always inline callees of this size or smaller\n");
    VM.sysWrite("inline_massive_method_size     int     Static inlining heuristic: If root method is already this big, then only inline trivial methods\n");
    VM.sysWrite("inline_max_arg_bonus           double  Maximum bonus for reducing the perceived size of a method during inlining.\n");
    VM.sysWrite("inline_precise_reg_array_arg_bonus double  Bonus given to inlining methods that are passed a register of a known precise type.\n");
    VM.sysWrite("inline_declared_aastored_array_arg_bonus double  Bonus given when there's potential to optimize checkstore portion of aastore bytecode on parameter\n");
    VM.sysWrite("inline_precise_reg_class_arg_bonus double  Bonus given to inlining methods that are passed a register of a known precise type.\n");
    VM.sysWrite("inline_extant_reg_class_arg_bonus double  Bonus given to inlining methods that are passed a register that's known not to be null.\n");
    VM.sysWrite("inline_int_const_arg_bonus     double  Bonus given to inlining methods that are passed an int constant argument\n");
    VM.sysWrite("inline_null_const_arg_bonus    double  Bonus given to inlining methods that are passed a null constant argument\n");
    VM.sysWrite("inline_object_const_arg_bonus  double  Bonus given to inlining methods that are passed an object constant argument\n");
    VM.sysWrite("inline_call_depth_cost         double  As we inline deeper nested methods what cost (or bonus) do we wish to give to deter (or encourage) nesting of deeper methods?\n");
    VM.sysWrite("inline_ai_max_target_size      int     Adaptive inlining heuristic: Upper bound on callee size\n");
    VM.sysWrite("inline_ai_min_callsite_fraction double  Adaptive inlining heuristc: Minimum fraction of callsite distribution for guarded inlining of a callee\n");
    VM.sysWrite("\nSelection Options (set option to one of an enumeration of possible values)\n");
    VM.sysWrite("\t\tHow to compute block and edge frequencies?\n");
    VM.sysWrite("profile_frequency_strategy    ");
    VM.sysWrite("counters ");
    VM.sysWrite("static ");
    VM.sysWrite("dumb ");
    VM.sysWrite("inverse ");
    VM.sysWrite("\n");
    VM.sysWrite("\t\tSelection of spilling heuristic\n");
    VM.sysWrite("regalloc_spill_cost_estimate  ");
    VM.sysWrite("simple ");
    VM.sysWrite("brainDead ");
    VM.sysWrite("blockCount ");
    VM.sysWrite("\n");
    VM.sysWrite("\t\tSelection of guard mechanism for inlined virtual calls that cannot be statically bound\n");
    VM.sysWrite("inline_guard_kind             ");
    VM.sysWrite("inline_guard_method_test ");
    VM.sysWrite("inline_guard_class_test ");
    VM.sysWrite("inline_guard_code_patch ");
    VM.sysWrite("\n");
    VM.sysWrite("\nSet Options (option is a set of values)\n");
    VM.sysWrite("driver_exclude                 Exclude methods from being opt compiled\n");
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
    result.append("\tfocus_effort                   = ").append(FREQ_FOCUS_EFFORT).append("\n");
    result.append("\treads_kill                     = ").append(READS_KILL).append("\n");
    result.append("\tfield_analysis                 = ").append(FIELD_ANALYSIS).append("\n");
    result.append("\tinline                         = ").append(INLINE).append("\n");
    result.append("\tinline_guarded                 = ").append(INLINE_GUARDED).append("\n");
    result.append("\tinline_guarded_interfaces      = ").append(INLINE_GUARDED_INTERFACES).append("\n");
    result.append("\tinline_preex                   = ").append(INLINE_PREEX).append("\n");
    result.append("\tsimplify_integer_ops           = ").append(SIMPLIFY_INTEGER_OPS).append("\n");
    result.append("\tsimplify_long_ops              = ").append(SIMPLIFY_LONG_OPS).append("\n");
    result.append("\tsimplify_float_ops             = ").append(SIMPLIFY_FLOAT_OPS).append("\n");
    result.append("\tsimplify_double_ops            = ").append(SIMPLIFY_DOUBLE_OPS).append("\n");
    result.append("\tsimplify_ref_ops               = ").append(SIMPLIFY_REF_OPS).append("\n");
    result.append("\tsimplify_tib_ops               = ").append(SIMPLIFY_TIB_OPS).append("\n");
    result.append("\tsimplify_field_ops             = ").append(SIMPLIFY_FIELD_OPS).append("\n");
    result.append("\tsimplify_chase_final_fields    = ").append(SIMPLIFY_CHASE_FINAL_FIELDS).append("\n");
    result.append("\tlocal_constant_prop            = ").append(LOCAL_CONSTANT_PROP).append("\n");
    result.append("\tlocal_copy_prop                = ").append(LOCAL_COPY_PROP).append("\n");
    result.append("\tlocal_cse                      = ").append(LOCAL_CSE).append("\n");
    result.append("\tlocal_expression_folding       = ").append(LOCAL_EXPRESSION_FOLDING).append("\n");
    result.append("\tcontrol_static_splitting       = ").append(CONTROL_STATIC_SPLITTING).append("\n");
    result.append("\tcontrol_unwhile                = ").append(CONTROL_TURN_WHILES_INTO_UNTILS).append("\n");
    result.append("\tescape_simple_ipa              = ").append(ESCAPE_SIMPLE_IPA).append("\n");
    result.append("\tescape_scalar_replace_aggregates = ").append(ESCAPE_SCALAR_REPLACE_AGGREGATES).append("\n");
    result.append("\tescape_monitor_removal         = ").append(ESCAPE_MONITOR_REMOVAL).append("\n");
    result.append("\tescape_invokee_thread_local    = ").append(ESCAPE_INVOKEE_THREAD_LOCAL).append("\n");
    result.append("\tssa                            = ").append(SSA).append("\n");
    result.append("\tssa_expression_folding         = ").append(SSA_EXPRESSION_FOLDING).append("\n");
    result.append("\tssa_redundant_branch_elimination = ").append(SSA_REDUNDANT_BRANCH_ELIMINATION).append("\n");
    result.append("\tssa_licm_ignore_pei            = ").append(SSA_LICM_IGNORE_PEI).append("\n");
    result.append("\tssa_load_elimination           = ").append(SSA_LOAD_ELIMINATION).append("\n");
    result.append("\tssa_coalesce_after             = ").append(SSA_COALESCE_AFTER).append("\n");
    result.append("\tssa_loop_versioning            = ").append(SSA_LOOP_VERSIONING).append("\n");
    result.append("\tssa_live_range_splitting       = ").append(SSA_LIVE_RANGE_SPLITTING).append("\n");
    result.append("\tssa_gcp                        = ").append(SSA_GCP).append("\n");
    result.append("\tssa_gcse                       = ").append(SSA_GCSE).append("\n");
    result.append("\tssa_splitblock_to_avoid_rename = ").append(SSA_SPLITBLOCK_TO_AVOID_RENAME).append("\n");
    result.append("\tssa_splitblock_for_local_live  = ").append(SSA_SPLITBLOCK_FOR_LOCAL_LIVE).append("\n");
    result.append("\tssa_splitblock_into_infrequent = ").append(SSA_SPLITBLOCK_INTO_INFREQUENT).append("\n");
    result.append("\treorder_code                   = ").append(REORDER_CODE).append("\n");
    result.append("\treorder_code_ph                = ").append(REORDER_CODE_PH).append("\n");
    result.append("\th2l_inline_new                 = ").append(H2L_INLINE_NEW).append("\n");
    result.append("\th2l_inline_write_barrier       = ").append(H2L_INLINE_WRITE_BARRIER).append("\n");
    result.append("\th2l_inline_primitive_write_barrier = ").append(H2L_INLINE_PRIMITIVE_WRITE_BARRIER).append("\n");
    result.append("\th2l_no_callee_exceptions       = ").append(H2L_NO_CALLEE_EXCEPTIONS).append("\n");
    result.append("\th2l_call_via_jtoc              = ").append(H2L_CALL_VIA_JTOC).append("\n");
    result.append("\tl2m_handler_liveness           = ").append(L2M_HANDLER_LIVENESS).append("\n");
    result.append("\tl2m_schedule_prepass           = ").append(L2M_SCHEDULE_PREPASS).append("\n");
    result.append("\tregalloc_coalesce_moves        = ").append(REGALLOC_COALESCE_MOVES).append("\n");
    result.append("\tregalloc_coalesce_spills       = ").append(REGALLOC_COALESCE_SPILLS).append("\n");
    result.append("\tadaptive_instrumentation_sampling = ").append(ADAPTIVE_INSTRUMENTATION_SAMPLING).append("\n");
    result.append("\tadaptive_no_duplication        = ").append(ADAPTIVE_NO_DUPLICATION).append("\n");
    result.append("\tadaptive_processor_specific_counter = ").append(ADAPTIVE_PROCESSOR_SPECIFIC_COUNTER).append("\n");
    result.append("\tadaptive_remove_yp_from_checking = ").append(ADAPTIVE_REMOVE_YP_FROM_CHECKING).append("\n");
    result.append("\tosr_guarded_inlining           = ").append(OSR_GUARDED_INLINING).append("\n");
    result.append("\tosr_inline_policy              = ").append(OSR_INLINE_POLICY).append("\n");
    result.append("\tprint_static_stats             = ").append(PRINT_STATIC_STATS).append("\n");
    result.append("\tprint_phases                   = ").append(PRINT_PHASES).append("\n");
    result.append("\tprint_all_ir                   = ").append(PRINT_ALL_IR).append("\n");
    result.append("\tprint_detailed_inline_report   = ").append(PRINT_DETAILED_INLINE_REPORT).append("\n");
    result.append("\tprint_inline_report            = ").append(PRINT_INLINE_REPORT).append("\n");
    result.append("\tprint_dom                      = ").append(PRINT_DOMINATORS).append("\n");
    result.append("\tprint_pdom                     = ").append(PRINT_POST_DOMINATORS).append("\n");
    result.append("\tprint_ssa                      = ").append(PRINT_SSA).append("\n");
    result.append("\tprint_dg_burs                  = ").append(PRINT_DG_BURS).append("\n");
    result.append("\tprint_dg_sched_pre             = ").append(PRINT_DG_SCHED_PRE).append("\n");
    result.append("\tprint_dg_sched_post            = ").append(PRINT_DG_SCHED_POST).append("\n");
    result.append("\tprint_high                     = ").append(PRINT_HIGH).append("\n");
    result.append("\tprint_final_hir                = ").append(PRINT_FINAL_HIR).append("\n");
    result.append("\tprint_low                      = ").append(PRINT_LOW).append("\n");
    result.append("\tprint_final_lir                = ").append(PRINT_FINAL_LIR).append("\n");
    result.append("\tprint_mir                      = ").append(PRINT_MIR).append("\n");
    result.append("\tprint_final_mir                = ").append(PRINT_FINAL_MIR).append("\n");
    result.append("\tprint_cfg                      = ").append(PRINT_CFG).append("\n");
    result.append("\tprint_schedule_pre             = ").append(PRINT_SCHEDULE_PRE).append("\n");
    result.append("\tprint_regalloc                 = ").append(PRINT_REGALLOC).append("\n");
    result.append("\tprint_calling_conventions      = ").append(PRINT_CALLING_CONVENTIONS).append("\n");
    result.append("\tprint_gc_maps                  = ").append(PRINT_GC_MAPS).append("\n");
    result.append("\tdebug_cgd                      = ").append(DEBUG_CODEGEN).append("\n");
    result.append("\tdebug_instru_sampling          = ").append(DEBUG_INSTRU_SAMPLING).append("\n");
    result.append("\tdebug_instru_sampling_detail   = ").append(DEBUG_INSTRU_SAMPLING_DETAIL).append("\n");
    result.append("\tdebug_gcp                      = ").append(DEBUG_GCP).append("\n");
    result.append("\tverbose                        = ").append(PRINT_METHOD).append("\n");
    result.append("\tmc                             = ").append(PRINT_MACHINECODE).append("\n");
    result.append("\tprint_ir_level                 = ").append(PRINT_IR_LEVEL).append("\n");
    result.append("\tprofile_edge_count_input_file  = ").append(PROFILE_EDGE_COUNT_INPUT_FILE).append("\n");
    result.append("\tprofile_infrequent_threshold   = ").append(PROFILE_INFREQUENT_THRESHOLD).append("\n");
    result.append("\tprofile_cbs_hotness            = ").append(PROFILE_CBS_HOTNESS).append("\n");
    result.append("\tescape_max_array_size          = ").append(ESCAPE_MAX_ARRAY_SIZE).append("\n");
    result.append("\tssa_load_elimination_rounds    = ").append(SSA_LOAD_ELIMINATION_ROUNDS).append("\n");
    result.append("\tl2m_max_block_size             = ").append(L2M_MAX_BLOCK_SIZE).append("\n");
    result.append("\tregalloc_simple_spill_cost_move_factor = ").append(REGALLOC_SIMPLE_SPILL_COST_MOVE_FACTOR).append("\n");
    result.append("\tregalloc_simple_spill_cost_memory_operand_factor = ").append(REGALLOC_SIMPLE_SPILL_COST_MEMORY_OPERAND_FACTOR).append("\n");
    result.append("\tcontrol_tableswitch_cutoff     = ").append(CONTROL_TABLESWITCH_CUTOFF).append("\n");
    result.append("\tcontrol_cond_move_cutoff       = ").append(CONTROL_COND_MOVE_CUTOFF).append("\n");
    result.append("\tcontrol_unroll_log             = ").append(CONTROL_UNROLL_LOG).append("\n");
    result.append("\tcontrol_static_splitting_max_cost = ").append(CONTROL_STATIC_SPLITTING_MAX_COST).append("\n");
    result.append("\tcontrol_well_predicted_cutoff  = ").append(CONTROL_WELL_PREDICTED_CUTOFF).append("\n");
    result.append("\tinline_max_target_size         = ").append(INLINE_MAX_TARGET_SIZE).append("\n");
    result.append("\tinline_max_inline_depth        = ").append(INLINE_MAX_INLINE_DEPTH).append("\n");
    result.append("\tinline_max_always_inline_target_size = ").append(INLINE_MAX_ALWAYS_INLINE_TARGET_SIZE).append("\n");
    result.append("\tinline_massive_method_size     = ").append(INLINE_MASSIVE_METHOD_SIZE).append("\n");
    result.append("\tinline_max_arg_bonus           = ").append(INLINE_MAX_ARG_BONUS).append("\n");
    result.append("\tinline_precise_reg_array_arg_bonus = ").append(INLINE_PRECISE_REG_ARRAY_ARG_BONUS).append("\n");
    result.append("\tinline_declared_aastored_array_arg_bonus = ").append(INLINE_DECLARED_AASTORED_ARRAY_ARG_BONUS).append("\n");
    result.append("\tinline_precise_reg_class_arg_bonus = ").append(INLINE_PRECISE_REG_CLASS_ARG_BONUS).append("\n");
    result.append("\tinline_extant_reg_class_arg_bonus = ").append(INLINE_EXTANT_REG_CLASS_ARG_BONUS).append("\n");
    result.append("\tinline_int_const_arg_bonus     = ").append(INLINE_INT_CONST_ARG_BONUS).append("\n");
    result.append("\tinline_null_const_arg_bonus    = ").append(INLINE_NULL_CONST_ARG_BONUS).append("\n");
    result.append("\tinline_object_const_arg_bonus  = ").append(INLINE_OBJECT_CONST_ARG_BONUS).append("\n");
    result.append("\tinline_call_depth_cost         = ").append(INLINE_CALL_DEPTH_COST).append("\n");
    result.append("\tinline_ai_max_target_size      = ").append(INLINE_AI_MAX_TARGET_SIZE).append("\n");
    result.append("\tinline_ai_min_callsite_fraction = ").append(INLINE_AI_MIN_CALLSITE_FRACTION).append("\n");
    if (PROFILE_FREQUENCY_STRATEGY == PROFILE_COUNTERS_FREQ)
       result.append("\tprofile_frequency_strategy     = PROFILE_COUNTERS_FREQ").append("\n");
    if (PROFILE_FREQUENCY_STRATEGY == PROFILE_STATIC_FREQ)
       result.append("\tprofile_frequency_strategy     = PROFILE_STATIC_FREQ").append("\n");
    if (PROFILE_FREQUENCY_STRATEGY == PROFILE_DUMB_FREQ)
       result.append("\tprofile_frequency_strategy     = PROFILE_DUMB_FREQ").append("\n");
    if (PROFILE_FREQUENCY_STRATEGY == PROFILE_INVERSE_COUNTERS_FREQ)
       result.append("\tprofile_frequency_strategy     = PROFILE_INVERSE_COUNTERS_FREQ").append("\n");
    if (REGALLOC_SPILL_COST_ESTIMATE == REGALLOC_SIMPLE_SPILL_COST)
       result.append("\tregalloc_spill_cost_estimate   = REGALLOC_SIMPLE_SPILL_COST").append("\n");
    if (REGALLOC_SPILL_COST_ESTIMATE == REGALLOC_BRAINDEAD_SPILL_COST)
       result.append("\tregalloc_spill_cost_estimate   = REGALLOC_BRAINDEAD_SPILL_COST").append("\n");
    if (REGALLOC_SPILL_COST_ESTIMATE == REGALLOC_BLOCK_COUNT_SPILL_COST)
       result.append("\tregalloc_spill_cost_estimate   = REGALLOC_BLOCK_COUNT_SPILL_COST").append("\n");
    if (INLINE_GUARD_KIND == INLINE_GUARD_METHOD_TEST)
       result.append("\tinline_guard_kind              = INLINE_GUARD_METHOD_TEST").append("\n");
    if (INLINE_GUARD_KIND == INLINE_GUARD_CLASS_TEST)
       result.append("\tinline_guard_kind              = INLINE_GUARD_CLASS_TEST").append("\n");
    if (INLINE_GUARD_KIND == INLINE_GUARD_CODE_PATCH)
       result.append("\tinline_guard_kind              = INLINE_GUARD_CODE_PATCH").append("\n");
    {
      String val = (DRIVER_EXCLUDE==null)?"[]":DRIVER_EXCLUDE.toString();
      result.append("\tdriver_exclude                 = ").append(val).append("\n");
    }
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
    VM.sysWriteln("\tfocus_effort                   = ",FREQ_FOCUS_EFFORT);
    VM.sysWriteln("\treads_kill                     = ",READS_KILL);
    VM.sysWriteln("\tfield_analysis                 = ",FIELD_ANALYSIS);
    VM.sysWriteln("\tinline                         = ",INLINE);
    VM.sysWriteln("\tinline_guarded                 = ",INLINE_GUARDED);
    VM.sysWriteln("\tinline_guarded_interfaces      = ",INLINE_GUARDED_INTERFACES);
    VM.sysWriteln("\tinline_preex                   = ",INLINE_PREEX);
    VM.sysWriteln("\tsimplify_integer_ops           = ",SIMPLIFY_INTEGER_OPS);
    VM.sysWriteln("\tsimplify_long_ops              = ",SIMPLIFY_LONG_OPS);
    VM.sysWriteln("\tsimplify_float_ops             = ",SIMPLIFY_FLOAT_OPS);
    VM.sysWriteln("\tsimplify_double_ops            = ",SIMPLIFY_DOUBLE_OPS);
    VM.sysWriteln("\tsimplify_ref_ops               = ",SIMPLIFY_REF_OPS);
    VM.sysWriteln("\tsimplify_tib_ops               = ",SIMPLIFY_TIB_OPS);
    VM.sysWriteln("\tsimplify_field_ops             = ",SIMPLIFY_FIELD_OPS);
    VM.sysWriteln("\tsimplify_chase_final_fields    = ",SIMPLIFY_CHASE_FINAL_FIELDS);
    VM.sysWriteln("\tlocal_constant_prop            = ",LOCAL_CONSTANT_PROP);
    VM.sysWriteln("\tlocal_copy_prop                = ",LOCAL_COPY_PROP);
    VM.sysWriteln("\tlocal_cse                      = ",LOCAL_CSE);
    VM.sysWriteln("\tlocal_expression_folding       = ",LOCAL_EXPRESSION_FOLDING);
    VM.sysWriteln("\tcontrol_static_splitting       = ",CONTROL_STATIC_SPLITTING);
    VM.sysWriteln("\tcontrol_unwhile                = ",CONTROL_TURN_WHILES_INTO_UNTILS);
    VM.sysWriteln("\tescape_simple_ipa              = ",ESCAPE_SIMPLE_IPA);
    VM.sysWriteln("\tescape_scalar_replace_aggregates = ",ESCAPE_SCALAR_REPLACE_AGGREGATES);
    VM.sysWriteln("\tescape_monitor_removal         = ",ESCAPE_MONITOR_REMOVAL);
    VM.sysWriteln("\tescape_invokee_thread_local    = ",ESCAPE_INVOKEE_THREAD_LOCAL);
    VM.sysWriteln("\tssa                            = ",SSA);
    VM.sysWriteln("\tssa_expression_folding         = ",SSA_EXPRESSION_FOLDING);
    VM.sysWriteln("\tssa_redundant_branch_elimination = ",SSA_REDUNDANT_BRANCH_ELIMINATION);
    VM.sysWriteln("\tssa_licm_ignore_pei            = ",SSA_LICM_IGNORE_PEI);
    VM.sysWriteln("\tssa_load_elimination           = ",SSA_LOAD_ELIMINATION);
    VM.sysWriteln("\tssa_coalesce_after             = ",SSA_COALESCE_AFTER);
    VM.sysWriteln("\tssa_loop_versioning            = ",SSA_LOOP_VERSIONING);
    VM.sysWriteln("\tssa_live_range_splitting       = ",SSA_LIVE_RANGE_SPLITTING);
    VM.sysWriteln("\tssa_gcp                        = ",SSA_GCP);
    VM.sysWriteln("\tssa_gcse                       = ",SSA_GCSE);
    VM.sysWriteln("\tssa_splitblock_to_avoid_rename = ",SSA_SPLITBLOCK_TO_AVOID_RENAME);
    VM.sysWriteln("\tssa_splitblock_for_local_live  = ",SSA_SPLITBLOCK_FOR_LOCAL_LIVE);
    VM.sysWriteln("\tssa_splitblock_into_infrequent = ",SSA_SPLITBLOCK_INTO_INFREQUENT);
    VM.sysWriteln("\treorder_code                   = ",REORDER_CODE);
    VM.sysWriteln("\treorder_code_ph                = ",REORDER_CODE_PH);
    VM.sysWriteln("\th2l_inline_new                 = ",H2L_INLINE_NEW);
    VM.sysWriteln("\th2l_inline_write_barrier       = ",H2L_INLINE_WRITE_BARRIER);
    VM.sysWriteln("\th2l_inline_primitive_write_barrier = ",H2L_INLINE_PRIMITIVE_WRITE_BARRIER);
    VM.sysWriteln("\th2l_no_callee_exceptions       = ",H2L_NO_CALLEE_EXCEPTIONS);
    VM.sysWriteln("\th2l_call_via_jtoc              = ",H2L_CALL_VIA_JTOC);
    VM.sysWriteln("\tl2m_handler_liveness           = ",L2M_HANDLER_LIVENESS);
    VM.sysWriteln("\tl2m_schedule_prepass           = ",L2M_SCHEDULE_PREPASS);
    VM.sysWriteln("\tregalloc_coalesce_moves        = ",REGALLOC_COALESCE_MOVES);
    VM.sysWriteln("\tregalloc_coalesce_spills       = ",REGALLOC_COALESCE_SPILLS);
    VM.sysWriteln("\tadaptive_instrumentation_sampling = ",ADAPTIVE_INSTRUMENTATION_SAMPLING);
    VM.sysWriteln("\tadaptive_no_duplication        = ",ADAPTIVE_NO_DUPLICATION);
    VM.sysWriteln("\tadaptive_processor_specific_counter = ",ADAPTIVE_PROCESSOR_SPECIFIC_COUNTER);
    VM.sysWriteln("\tadaptive_remove_yp_from_checking = ",ADAPTIVE_REMOVE_YP_FROM_CHECKING);
    VM.sysWriteln("\tosr_guarded_inlining           = ",OSR_GUARDED_INLINING);
    VM.sysWriteln("\tosr_inline_policy              = ",OSR_INLINE_POLICY);
    VM.sysWriteln("\tprint_static_stats             = ",PRINT_STATIC_STATS);
    VM.sysWriteln("\tprint_phases                   = ",PRINT_PHASES);
    VM.sysWriteln("\tprint_all_ir                   = ",PRINT_ALL_IR);
    VM.sysWriteln("\tprint_detailed_inline_report   = ",PRINT_DETAILED_INLINE_REPORT);
    VM.sysWriteln("\tprint_inline_report            = ",PRINT_INLINE_REPORT);
    VM.sysWriteln("\tprint_dom                      = ",PRINT_DOMINATORS);
    VM.sysWriteln("\tprint_pdom                     = ",PRINT_POST_DOMINATORS);
    VM.sysWriteln("\tprint_ssa                      = ",PRINT_SSA);
    VM.sysWriteln("\tprint_dg_burs                  = ",PRINT_DG_BURS);
    VM.sysWriteln("\tprint_dg_sched_pre             = ",PRINT_DG_SCHED_PRE);
    VM.sysWriteln("\tprint_dg_sched_post            = ",PRINT_DG_SCHED_POST);
    VM.sysWriteln("\tprint_high                     = ",PRINT_HIGH);
    VM.sysWriteln("\tprint_final_hir                = ",PRINT_FINAL_HIR);
    VM.sysWriteln("\tprint_low                      = ",PRINT_LOW);
    VM.sysWriteln("\tprint_final_lir                = ",PRINT_FINAL_LIR);
    VM.sysWriteln("\tprint_mir                      = ",PRINT_MIR);
    VM.sysWriteln("\tprint_final_mir                = ",PRINT_FINAL_MIR);
    VM.sysWriteln("\tprint_cfg                      = ",PRINT_CFG);
    VM.sysWriteln("\tprint_schedule_pre             = ",PRINT_SCHEDULE_PRE);
    VM.sysWriteln("\tprint_regalloc                 = ",PRINT_REGALLOC);
    VM.sysWriteln("\tprint_calling_conventions      = ",PRINT_CALLING_CONVENTIONS);
    VM.sysWriteln("\tprint_gc_maps                  = ",PRINT_GC_MAPS);
    VM.sysWriteln("\tdebug_cgd                      = ",DEBUG_CODEGEN);
    VM.sysWriteln("\tdebug_instru_sampling          = ",DEBUG_INSTRU_SAMPLING);
    VM.sysWriteln("\tdebug_instru_sampling_detail   = ",DEBUG_INSTRU_SAMPLING_DETAIL);
    VM.sysWriteln("\tdebug_gcp                      = ",DEBUG_GCP);
    VM.sysWriteln("\tverbose                        = ",PRINT_METHOD);
    VM.sysWriteln("\tmc                             = ",PRINT_MACHINECODE);
    VM.sysWriteln("\tprint_ir_level                 = ",PRINT_IR_LEVEL);
    VM.sysWriteln("\tprofile_edge_count_input_file  = ",PROFILE_EDGE_COUNT_INPUT_FILE);
    VM.sysWriteln("\tprofile_infrequent_threshold   = ",PROFILE_INFREQUENT_THRESHOLD);
    VM.sysWriteln("\tprofile_cbs_hotness            = ",PROFILE_CBS_HOTNESS);
    VM.sysWriteln("\tescape_max_array_size          = ",ESCAPE_MAX_ARRAY_SIZE);
    VM.sysWriteln("\tssa_load_elimination_rounds    = ",SSA_LOAD_ELIMINATION_ROUNDS);
    VM.sysWriteln("\tl2m_max_block_size             = ",L2M_MAX_BLOCK_SIZE);
    VM.sysWriteln("\tregalloc_simple_spill_cost_move_factor = ",REGALLOC_SIMPLE_SPILL_COST_MOVE_FACTOR);
    VM.sysWriteln("\tregalloc_simple_spill_cost_memory_operand_factor = ",REGALLOC_SIMPLE_SPILL_COST_MEMORY_OPERAND_FACTOR);
    VM.sysWriteln("\tcontrol_tableswitch_cutoff     = ",CONTROL_TABLESWITCH_CUTOFF);
    VM.sysWriteln("\tcontrol_cond_move_cutoff       = ",CONTROL_COND_MOVE_CUTOFF);
    VM.sysWriteln("\tcontrol_unroll_log             = ",CONTROL_UNROLL_LOG);
    VM.sysWriteln("\tcontrol_static_splitting_max_cost = ",CONTROL_STATIC_SPLITTING_MAX_COST);
    VM.sysWriteln("\tcontrol_well_predicted_cutoff  = ",CONTROL_WELL_PREDICTED_CUTOFF);
    VM.sysWriteln("\tinline_max_target_size         = ",INLINE_MAX_TARGET_SIZE);
    VM.sysWriteln("\tinline_max_inline_depth        = ",INLINE_MAX_INLINE_DEPTH);
    VM.sysWriteln("\tinline_max_always_inline_target_size = ",INLINE_MAX_ALWAYS_INLINE_TARGET_SIZE);
    VM.sysWriteln("\tinline_massive_method_size     = ",INLINE_MASSIVE_METHOD_SIZE);
    VM.sysWriteln("\tinline_max_arg_bonus           = ",INLINE_MAX_ARG_BONUS);
    VM.sysWriteln("\tinline_precise_reg_array_arg_bonus = ",INLINE_PRECISE_REG_ARRAY_ARG_BONUS);
    VM.sysWriteln("\tinline_declared_aastored_array_arg_bonus = ",INLINE_DECLARED_AASTORED_ARRAY_ARG_BONUS);
    VM.sysWriteln("\tinline_precise_reg_class_arg_bonus = ",INLINE_PRECISE_REG_CLASS_ARG_BONUS);
    VM.sysWriteln("\tinline_extant_reg_class_arg_bonus = ",INLINE_EXTANT_REG_CLASS_ARG_BONUS);
    VM.sysWriteln("\tinline_int_const_arg_bonus     = ",INLINE_INT_CONST_ARG_BONUS);
    VM.sysWriteln("\tinline_null_const_arg_bonus    = ",INLINE_NULL_CONST_ARG_BONUS);
    VM.sysWriteln("\tinline_object_const_arg_bonus  = ",INLINE_OBJECT_CONST_ARG_BONUS);
    VM.sysWriteln("\tinline_call_depth_cost         = ",INLINE_CALL_DEPTH_COST);
    VM.sysWriteln("\tinline_ai_max_target_size      = ",INLINE_AI_MAX_TARGET_SIZE);
    VM.sysWriteln("\tinline_ai_min_callsite_fraction = ",INLINE_AI_MIN_CALLSITE_FRACTION);
    if (PROFILE_FREQUENCY_STRATEGY == PROFILE_COUNTERS_FREQ)
       VM.sysWriteln("\tprofile_frequency_strategy     = PROFILE_COUNTERS_FREQ");
    if (PROFILE_FREQUENCY_STRATEGY == PROFILE_STATIC_FREQ)
       VM.sysWriteln("\tprofile_frequency_strategy     = PROFILE_STATIC_FREQ");
    if (PROFILE_FREQUENCY_STRATEGY == PROFILE_DUMB_FREQ)
       VM.sysWriteln("\tprofile_frequency_strategy     = PROFILE_DUMB_FREQ");
    if (PROFILE_FREQUENCY_STRATEGY == PROFILE_INVERSE_COUNTERS_FREQ)
       VM.sysWriteln("\tprofile_frequency_strategy     = PROFILE_INVERSE_COUNTERS_FREQ");
    if (REGALLOC_SPILL_COST_ESTIMATE == REGALLOC_SIMPLE_SPILL_COST)
       VM.sysWriteln("\tregalloc_spill_cost_estimate   = REGALLOC_SIMPLE_SPILL_COST");
    if (REGALLOC_SPILL_COST_ESTIMATE == REGALLOC_BRAINDEAD_SPILL_COST)
       VM.sysWriteln("\tregalloc_spill_cost_estimate   = REGALLOC_BRAINDEAD_SPILL_COST");
    if (REGALLOC_SPILL_COST_ESTIMATE == REGALLOC_BLOCK_COUNT_SPILL_COST)
       VM.sysWriteln("\tregalloc_spill_cost_estimate   = REGALLOC_BLOCK_COUNT_SPILL_COST");
    if (INLINE_GUARD_KIND == INLINE_GUARD_METHOD_TEST)
       VM.sysWriteln("\tinline_guard_kind              = INLINE_GUARD_METHOD_TEST");
    if (INLINE_GUARD_KIND == INLINE_GUARD_CLASS_TEST)
       VM.sysWriteln("\tinline_guard_kind              = INLINE_GUARD_CLASS_TEST");
    if (INLINE_GUARD_KIND == INLINE_GUARD_CODE_PATCH)
       VM.sysWriteln("\tinline_guard_kind              = INLINE_GUARD_CODE_PATCH");
    {
      String val = (DRIVER_EXCLUDE==null)?"[]":DRIVER_EXCLUDE.toString();
      VM.sysWriteln("\tdriver_exclude                 = ", val);
    }
    {
      String val = (METHOD_TO_PRINT==null)?"[]":METHOD_TO_PRINT.toString();
      VM.sysWriteln("\tmethod_to_print                = ", val);
    }
    //End generated option value printing
  }
// END CODE GENERATED FROM MasterOptions.template

  private boolean instanceProcessAsOption(String arg) {
    if (arg.startsWith("O")) {
      try {
        setOptLevel(Integer.parseInt(arg.substring(1)));
      } catch (NumberFormatException e) {
        return false;
      }
      return true;
    }
    return false;
  }

  private static void instancePrintHelpHeader(String prefix) {
    VM.sysWrite("Commands\n");
    VM.sysWrite(prefix+"[help]\t\t\tPrint brief description of opt compiler's command-line arguments\n");
    VM.sysWrite(prefix+"printOptions\t\tPrint the current values of opt compiler options\n");
    if (prefix.indexOf("irc")!=-1 || prefix.indexOf("bc")!=-1 || prefix.indexOf("eoc")!=-1) {
      VM.sysWrite(prefix+"O0\t\t\tSelect optimization level 0, minimal optimizations\n");
      VM.sysWrite(prefix+"O1\t\t\tSelect optimization level 1, modest optimizations\n");
      VM.sysWrite(prefix+"O2\t\t\tSelect optimization level 2\n");
    }
    VM.sysWrite("\n");
  }


  private static void instancePrintHelpFooter(String prefix) {
  }


  /** accessor to get OPT level */
  public int getOptLevel() {
    return OPTIMIZATION_LEVEL;
  }

  /**
   * Set the options to encode the optimizations enabled at the given opt label
   * and disabled all optimizations that are not enabled at the given opt label
   */
  public void setOptLevel(int level) {
     OPTIMIZATION_LEVEL = level;
     // Begin generated opt-level logic
     if (level >= 0)
        FIELD_ANALYSIS = true;
     else
        FIELD_ANALYSIS = false;
     if (level >= 0)
        INLINE = true;
     else
        INLINE = false;
     if (level >= 0)
        INLINE_GUARDED = true;
     else
        INLINE_GUARDED = false;
     if (level >= 0)
        INLINE_GUARDED_INTERFACES = true;
     else
        INLINE_GUARDED_INTERFACES = false;
     if (level >= 0)
        INLINE_PREEX = true;
     else
        INLINE_PREEX = false;
     if (level >= 0)
        LOCAL_CONSTANT_PROP = true;
     else
        LOCAL_CONSTANT_PROP = false;
     if (level >= 0)
        LOCAL_COPY_PROP = true;
     else
        LOCAL_COPY_PROP = false;
     if (level >= 0)
        LOCAL_CSE = true;
     else
        LOCAL_CSE = false;
     if (level >= 3)
        LOCAL_EXPRESSION_FOLDING = true;
     else
        LOCAL_EXPRESSION_FOLDING = false;
     if (level >= 1)
        CONTROL_STATIC_SPLITTING = true;
     else
        CONTROL_STATIC_SPLITTING = false;
     if (level >= 3)
        CONTROL_TURN_WHILES_INTO_UNTILS = true;
     else
        CONTROL_TURN_WHILES_INTO_UNTILS = false;
     if (level >= 1)
        ESCAPE_SCALAR_REPLACE_AGGREGATES = true;
     else
        ESCAPE_SCALAR_REPLACE_AGGREGATES = false;
     if (level >= 1)
        ESCAPE_MONITOR_REMOVAL = true;
     else
        ESCAPE_MONITOR_REMOVAL = false;
     if (level >= 3)
        SSA = true;
     else
        SSA = false;
     if (level >= 3)
        SSA_EXPRESSION_FOLDING = true;
     else
        SSA_EXPRESSION_FOLDING = false;
     if (level >= 3)
        SSA_REDUNDANT_BRANCH_ELIMINATION = true;
     else
        SSA_REDUNDANT_BRANCH_ELIMINATION = false;
     if (level >= 3)
        SSA_LOAD_ELIMINATION = true;
     else
        SSA_LOAD_ELIMINATION = false;
     if (level >= 3)
        SSA_COALESCE_AFTER = true;
     else
        SSA_COALESCE_AFTER = false;
     if (level >= 3)
        SSA_GCP = true;
     else
        SSA_GCP = false;
     if (level >= 3)
        SSA_GCSE = true;
     else
        SSA_GCSE = false;
     if (level >= 0)
        REORDER_CODE = true;
     else
        REORDER_CODE = false;
     if (level >= 1)
        REORDER_CODE_PH = true;
     else
        REORDER_CODE_PH = false;
     if (level >= 0)
        H2L_INLINE_NEW = true;
     else
        H2L_INLINE_NEW = false;
     if (level >= 1)
        H2L_INLINE_WRITE_BARRIER = true;
     else
        H2L_INLINE_WRITE_BARRIER = false;
     if (level >= 1)
        H2L_INLINE_PRIMITIVE_WRITE_BARRIER = true;
     else
        H2L_INLINE_PRIMITIVE_WRITE_BARRIER = false;
     if (level >= 2)
        L2M_HANDLER_LIVENESS = true;
     else
        L2M_HANDLER_LIVENESS = false;
     if (level >= 0)
        REGALLOC_COALESCE_MOVES = true;
     else
        REGALLOC_COALESCE_MOVES = false;
     if (level >= 0)
        REGALLOC_COALESCE_SPILLS = true;
     else
        REGALLOC_COALESCE_SPILLS = false;
     if (level >= 1)
        OSR_GUARDED_INLINING = true;
     else
        OSR_GUARDED_INLINING = false;
     if (level >= 1)
        OSR_INLINE_POLICY = true;
     else
        OSR_INLINE_POLICY = false;
     // End generated opt-level logic
  }
}
