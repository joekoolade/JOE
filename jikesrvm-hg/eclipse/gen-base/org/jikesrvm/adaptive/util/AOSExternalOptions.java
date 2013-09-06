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
package org.jikesrvm.adaptive.util;

import org.jikesrvm.VM;
import org.jikesrvm.CommandLineArgs;

/**
 * Class to handle command-line arguments and options for the
 * adaptive system.
 * <p>
 * Note: This file is mechanically generated from AOSOptions.template
 *       and MasterOptions.template
 * <p>
 * Note: Boolean options are defined in /home/jkulig/git/jei/jikesrvm-hg/rvm/src-generated/options/BooleanOptions.aos.dat
 *       All other options are defined in /home/jkulig/git/jei/jikesrvm-hg/rvm/src-generated/options/ValueOptions.aos.dat
 *       (value, enumeration, bitmask)
 *
 **/
public class AOSExternalOptions implements Cloneable {

  private void printOptionsHeader() {
    VM.sysWriteln("Current value of options:");
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
   /** Should the adaptive system recompile hot methods? */
   public boolean ENABLE_RECOMPILATION          = true;
   /** Do we need to generate advice files? */
   public boolean ENABLE_ADVICE_GENERATION      = false;
   /** Should the adaptive system be disabled, and methods given in the advice file compiled when BulkCompile.compileAllMethods() is called? */
   public boolean ENABLE_BULK_COMPILE           = false;
   /** Should bulk compilation be triggered before the user thread is started? */
   public boolean ENABLE_PRECOMPILE             = false;
   /** Should profile data be gathered and reported at the end of the run? */
   public boolean GATHER_PROFILE_DATA           = false;
   /** Should we use adaptive feedback-directed inlining? */
   public boolean ADAPTIVE_INLINING             = true;
   /** Should AOS exit when the controller clock reaches EARLY_EXIT_VALUE? */
   public boolean EARLY_EXIT                    = false;
   /** Should AOS promote baseline-compiled methods to opt? */
   public boolean OSR_PROMOTION                 = true;
   /** Should recompilation be done on a background thread or on next invocation? */
   public boolean BACKGROUND_RECOMPILATION      = true;
   /** Insert instrumentation in opt recompiled code to count yieldpoints executed? */
   public boolean INSERT_YIELDPOINT_COUNTERS    = false;
   /** Insert intrusive method counters in opt recompiled code? */
   public boolean INSERT_METHOD_COUNTERS_OPT    = false;
   /** Insert counters on all instructions in opt recompiled code? */
   public boolean INSERT_INSTRUCTION_COUNTERS   = false;
   /** Enable easy insertion of (debugging) counters in opt recompiled code. */
   public boolean INSERT_DEBUGGING_COUNTERS     = false;
   /** Report stats related to timer interrupts and AOS listeners on exit */
   public boolean REPORT_INTERRUPT_STATS        = false;
   /** Disable the ability for an app to request all methods to be recompiled */
   public boolean DISABLE_RECOMPILE_ALL_METHODS = false;
    /** How many timer ticks of method samples to take before reporting method hotness to controller */
    public int METHOD_SAMPLE_SIZE                = 3;
    /** Selection of initial compiler */
    public byte INITIAL_COMPILER                 = IRC_BASE;
    /** Selection of mechanism for identifying methods for optimizing recompilation */
    public byte RECOMPILATION_STRATEGY           = RS_SAMPLING;
    /** What triggers us to take a method sample? */
    public byte METHOD_LISTENER_TRIGGER          = ML_TIMER;
    /** What triggers us to take a method sample? */
    public byte CALL_GRAPH_LISTENER_TRIGGER      = CGL_CBS;
    /** Name of log file */
    public String LOGFILE_NAME                   = "AOSLog.txt";
    /** Name of advice file */
    public String COMPILATION_ADVICE_FILE_OUTPUT = "aosadvice.ca";
    /** Name of dynamic call graph file */
    public String DYNAMIC_CALL_FILE_OUTPUT       = "aosadvice.dc";
    /** Name of compiler DNA file (no name ==> use default DNA).  Discussed in a comment at the head of CompilerDNA.java */
    public String COMPILER_DNA_FILE_NAME         = "";
    /** File containing information about the methods to Opt compile */
    public String COMPILER_ADVICE_FILE_INPUT     = null;
    /** File containing information about the hot call sites */
    public String DYNAMIC_CALL_FILE_INPUT        = null;
    /** Control amount of verbosity for bulk compilation (larger ==> more) */
    public int BULK_COMPILATION_VERBOSITY        = 0;
    /** Control amount of event logging (larger ==> more) */
    public int LOGGING_LEVEL                     = 0;
    /** Control amount of info reported on exit (larger ==> more) */
    public int FINAL_REPORT_LEVEL                = 0;
    /** After how many clock ticks should we decay */
    public int DECAY_FREQUENCY                   = 100;
    /** What factor should we decay call graph edges hotness by */
    public double DCG_DECAY_RATE                 = 1.1;
    /** After how many timer interrupts do we update the weights in the dynamic call graph? */
    public int DCG_SAMPLE_SIZE                   = 20;
    /** Initial edge weight of call graph is set to AI_SEED_MULTIPLER * (1/AI_CONTROL_POINT) */
    public double INLINE_AI_SEED_MULTIPLIER      = 3;
    /** What percentage of the total weight of the dcg demarcates warm/hot edges  */
    public double INLINE_AI_HOT_CALLSITE_THRESHOLD= 0.01;
    /** Name of offline inline plan to be read and used for inlining */
    public String OFFLINE_INLINE_PLAN_NAME       = "AI_plan";
    /** Value of controller clock at which AOS should exit if EARLY_EXIT is true */
    public int EARLY_EXIT_TIME                   = 1000;
    /** Invocation count at which a baseline compiled method should be recompiled */
    public int INVOCATION_COUNT_THRESHOLD        = 1000;
    /** Opt level for recompilation in invocation count based system */
    public int INVOCATION_COUNT_OPT_LEVEL        = 1;
    /** What is the sample interval for counter-based sampling */
    public int COUNTER_BASED_SAMPLE_INTERVAL     = 1000;
    /** The maximum optimization level to enable. */
    public int MAX_OPT_LEVEL                     = 2;
   // End template-specified options

   // Begin generated support for "Enumeration" options
   // INITIAL_COMPILER
   public static final byte IRC_BASE = 0;
   /**
    * Is INITIAL_COMPILER set to IRC_BASE?
    */
   public final boolean baseIRC() {
     return INITIAL_COMPILER == IRC_BASE;
   }
   public static final byte IRC_OPT = 1;
   /**
    * Is INITIAL_COMPILER set to IRC_OPT?
    */
   public final boolean optIRC() {
     return INITIAL_COMPILER == IRC_OPT;
   }

   // RECOMPILATION_STRATEGY
   public static final byte RS_SAMPLING = 0;
   /**
    * Is RECOMPILATION_STRATEGY set to RS_SAMPLING?
    */
   public final boolean sampling() {
     return RECOMPILATION_STRATEGY == RS_SAMPLING;
   }
   public static final byte RS_COUNTERS = 1;
   /**
    * Is RECOMPILATION_STRATEGY set to RS_COUNTERS?
    */
   public final boolean counters() {
     return RECOMPILATION_STRATEGY == RS_COUNTERS;
   }

   // METHOD_LISTENER_TRIGGER
   public static final byte ML_TIMER = 0;
   /**
    * Is METHOD_LISTENER_TRIGGER set to ML_TIMER?
    */
   public final boolean mlTimer() {
     return METHOD_LISTENER_TRIGGER == ML_TIMER;
   }
   public static final byte ML_CBS = 1;
   /**
    * Is METHOD_LISTENER_TRIGGER set to ML_CBS?
    */
   public final boolean mlCBS() {
     return METHOD_LISTENER_TRIGGER == ML_CBS;
   }

   // CALL_GRAPH_LISTENER_TRIGGER
   public static final byte CGL_TIMER = 0;
   /**
    * Is CALL_GRAPH_LISTENER_TRIGGER set to CGL_TIMER?
    */
   public final boolean cgTimer() {
     return CALL_GRAPH_LISTENER_TRIGGER == CGL_TIMER;
   }
   public static final byte CGL_CBS = 1;
   /**
    * Is CALL_GRAPH_LISTENER_TRIGGER set to CGL_CBS?
    */
   public final boolean cgCBS() {
     return CALL_GRAPH_LISTENER_TRIGGER == CGL_CBS;
   }

   // End generated support for "Enumeration" options

   // Begin generated support for "Set" options
   // End generated support for "Set" options

   @Override
   @SuppressWarnings("unchecked")
   public Object clone() throws CloneNotSupportedException {
     AOSExternalOptions clone = (AOSExternalOptions)super.clone();
     return clone;
   }

  public AOSExternalOptions dup() {
    try {
      return (AOSExternalOptions) clone();
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
    if (name.equals("enable_recompilation")) {
      if (value.equals("true")) {
        ENABLE_RECOMPILATION = true;
        return true;
      } else if (value.equals("false")) {
        ENABLE_RECOMPILATION = false;
        return true;
      } else
        return false;
    }
    if (name.equals("enable_advice_generation")) {
      if (value.equals("true")) {
        ENABLE_ADVICE_GENERATION = true;
        return true;
      } else if (value.equals("false")) {
        ENABLE_ADVICE_GENERATION = false;
        return true;
      } else
        return false;
    }
    if (name.equals("enable_bulk_compile")) {
      if (value.equals("true")) {
        ENABLE_BULK_COMPILE = true;
        return true;
      } else if (value.equals("false")) {
        ENABLE_BULK_COMPILE = false;
        return true;
      } else
        return false;
    }
    if (name.equals("enable_precompile")) {
      if (value.equals("true")) {
        ENABLE_PRECOMPILE = true;
        return true;
      } else if (value.equals("false")) {
        ENABLE_PRECOMPILE = false;
        return true;
      } else
        return false;
    }
    if (name.equals("gather_profile_data")) {
      if (value.equals("true")) {
        GATHER_PROFILE_DATA = true;
        return true;
      } else if (value.equals("false")) {
        GATHER_PROFILE_DATA = false;
        return true;
      } else
        return false;
    }
    if (name.equals("adaptive_inlining")) {
      if (value.equals("true")) {
        ADAPTIVE_INLINING = true;
        return true;
      } else if (value.equals("false")) {
        ADAPTIVE_INLINING = false;
        return true;
      } else
        return false;
    }
    if (name.equals("early_exit")) {
      if (value.equals("true")) {
        EARLY_EXIT = true;
        return true;
      } else if (value.equals("false")) {
        EARLY_EXIT = false;
        return true;
      } else
        return false;
    }
    if (name.equals("osr_promotion")) {
      if (value.equals("true")) {
        OSR_PROMOTION = true;
        return true;
      } else if (value.equals("false")) {
        OSR_PROMOTION = false;
        return true;
      } else
        return false;
    }
    if (name.equals("background_recompilation")) {
      if (value.equals("true")) {
        BACKGROUND_RECOMPILATION = true;
        return true;
      } else if (value.equals("false")) {
        BACKGROUND_RECOMPILATION = false;
        return true;
      } else
        return false;
    }
    if (name.equals("insert_yieldpoint_counters")) {
      if (value.equals("true")) {
        INSERT_YIELDPOINT_COUNTERS = true;
        return true;
      } else if (value.equals("false")) {
        INSERT_YIELDPOINT_COUNTERS = false;
        return true;
      } else
        return false;
    }
    if (name.equals("insert_method_counters_opt")) {
      if (value.equals("true")) {
        INSERT_METHOD_COUNTERS_OPT = true;
        return true;
      } else if (value.equals("false")) {
        INSERT_METHOD_COUNTERS_OPT = false;
        return true;
      } else
        return false;
    }
    if (name.equals("insert_instruction_counters")) {
      if (value.equals("true")) {
        INSERT_INSTRUCTION_COUNTERS = true;
        return true;
      } else if (value.equals("false")) {
        INSERT_INSTRUCTION_COUNTERS = false;
        return true;
      } else
        return false;
    }
    if (name.equals("insert_debugging_counters")) {
      if (value.equals("true")) {
        INSERT_DEBUGGING_COUNTERS = true;
        return true;
      } else if (value.equals("false")) {
        INSERT_DEBUGGING_COUNTERS = false;
        return true;
      } else
        return false;
    }
    if (name.equals("report_interrupt_stats")) {
      if (value.equals("true")) {
        REPORT_INTERRUPT_STATS = true;
        return true;
      } else if (value.equals("false")) {
        REPORT_INTERRUPT_STATS = false;
        return true;
      } else
        return false;
    }
    if (name.equals("disable_recompile_all_methods")) {
      if (value.equals("true")) {
        DISABLE_RECOMPILE_ALL_METHODS = true;
        return true;
      } else if (value.equals("false")) {
        DISABLE_RECOMPILE_ALL_METHODS = false;
        return true;
      } else
        return false;
    }
    if (name.equals("method_sample_size")) {
       METHOD_SAMPLE_SIZE = CommandLineArgs.primitiveParseInt(value);
       return true;
     }
    if (name.equals("initial_compiler")) {
       if (value.equals("base")) {
         INITIAL_COMPILER = IRC_BASE;
         return true;
       }
       if (value.equals("opt")) {
         INITIAL_COMPILER = IRC_OPT;
         return true;
       }
       return false;
     }
    if (name.equals("recompilation_strategy")) {
       if (value.equals("sampling")) {
         RECOMPILATION_STRATEGY = RS_SAMPLING;
         return true;
       }
       if (value.equals("counters")) {
         RECOMPILATION_STRATEGY = RS_COUNTERS;
         return true;
       }
       return false;
     }
    if (name.equals("method_listener_trigger")) {
       if (value.equals("timer")) {
         METHOD_LISTENER_TRIGGER = ML_TIMER;
         return true;
       }
       if (value.equals("cbs")) {
         METHOD_LISTENER_TRIGGER = ML_CBS;
         return true;
       }
       return false;
     }
    if (name.equals("call_graph_listener_trigger")) {
       if (value.equals("timer")) {
         CALL_GRAPH_LISTENER_TRIGGER = CGL_TIMER;
         return true;
       }
       if (value.equals("cbs")) {
         CALL_GRAPH_LISTENER_TRIGGER = CGL_CBS;
         return true;
       }
       return false;
     }
    if (name.equals("lf")) {
       LOGFILE_NAME = value;
       return true;
     }
    if (name.equals("cafo")) {
       COMPILATION_ADVICE_FILE_OUTPUT = value;
       return true;
     }
    if (name.equals("dcfo")) {
       DYNAMIC_CALL_FILE_OUTPUT = value;
       return true;
     }
    if (name.equals("dna")) {
       COMPILER_DNA_FILE_NAME = value;
       return true;
     }
    if (name.equals("cafi")) {
       COMPILER_ADVICE_FILE_INPUT = value;
       return true;
     }
    if (name.equals("dcfi")) {
       DYNAMIC_CALL_FILE_INPUT = value;
       return true;
     }
    if (name.equals("bulk_compilation_verbosity")) {
       BULK_COMPILATION_VERBOSITY = CommandLineArgs.primitiveParseInt(value);
       return true;
     }
    if (name.equals("logging_level")) {
       LOGGING_LEVEL = CommandLineArgs.primitiveParseInt(value);
       return true;
     }
    if (name.equals("final_report_level")) {
       FINAL_REPORT_LEVEL = CommandLineArgs.primitiveParseInt(value);
       return true;
     }
    if (name.equals("decay_frequency")) {
       DECAY_FREQUENCY = CommandLineArgs.primitiveParseInt(value);
       return true;
     }
    if (name.equals("dcg_decay_rate")) {
       DCG_DECAY_RATE = CommandLineArgs.primitiveParseFloat(value);
       return true;
     }
    if (name.equals("dcg_sample_size")) {
       DCG_SAMPLE_SIZE = CommandLineArgs.primitiveParseInt(value);
       return true;
     }
    if (name.equals("inline_ai_seed_multiplier")) {
       INLINE_AI_SEED_MULTIPLIER = CommandLineArgs.primitiveParseFloat(value);
       return true;
     }
    if (name.equals("inline_ai_hot_callsite_threshold")) {
       INLINE_AI_HOT_CALLSITE_THRESHOLD = CommandLineArgs.primitiveParseFloat(value);
       return true;
     }
    if (name.equals("offlinePlan")) {
       OFFLINE_INLINE_PLAN_NAME = value;
       return true;
     }
    if (name.equals("early_exit_time")) {
       EARLY_EXIT_TIME = CommandLineArgs.primitiveParseInt(value);
       return true;
     }
    if (name.equals("invocation_count_threshold")) {
       INVOCATION_COUNT_THRESHOLD = CommandLineArgs.primitiveParseInt(value);
       return true;
     }
    if (name.equals("invocation_count_opt_level")) {
       INVOCATION_COUNT_OPT_LEVEL = CommandLineArgs.primitiveParseInt(value);
       return true;
     }
    if (name.equals("counter_based_sample_interval")) {
       COUNTER_BASED_SAMPLE_INTERVAL = CommandLineArgs.primitiveParseInt(value);
       return true;
     }
    if (name.equals("max_opt_level")) {
       MAX_OPT_LEVEL = CommandLineArgs.primitiveParseInt(value);
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
    VM.sysWrite("enable_recompilation                   Should the adaptive system recompile hot methods?\n");
    VM.sysWrite("enable_advice_generation               Do we need to generate advice files?\n");
    VM.sysWrite("enable_bulk_compile                    Should the adaptive system be disabled, and methods given in the advice file compiled when BulkCompile.compileAllMethods() is called?\n");
    VM.sysWrite("enable_precompile                      Should bulk compilation be triggered before the user thread is started?\n");
    VM.sysWrite("gather_profile_data                    Should profile data be gathered and reported at the end of the run?\n");
    VM.sysWrite("adaptive_inlining                      Should we use adaptive feedback-directed inlining?\n");
    VM.sysWrite("early_exit                             Should AOS exit when the controller clock reaches EARLY_EXIT_VALUE?\n");
    VM.sysWrite("osr_promotion                          Should AOS promote baseline-compiled methods to opt?\n");
    VM.sysWrite("background_recompilation               Should recompilation be done on a background thread or on next invocation?\n");
    VM.sysWrite("insert_yieldpoint_counters             Insert instrumentation in opt recompiled code to count yieldpoints executed?\n");
    VM.sysWrite("insert_method_counters_opt             Insert intrusive method counters in opt recompiled code?\n");
    VM.sysWrite("insert_instruction_counters            Insert counters on all instructions in opt recompiled code?\n");
    VM.sysWrite("insert_debugging_counters              Enable easy insertion of (debugging) counters in opt recompiled code.\n");
    VM.sysWrite("report_interrupt_stats                 Report stats related to timer interrupts and AOS listeners on exit\n");
    VM.sysWrite("disable_recompile_all_methods          Disable the ability for an app to request all methods to be recompiled\n");
    VM.sysWrite("\nValue Options ("+prefix+"<option>=<value>)\n");
    VM.sysWrite("Option                         Type    Description\n");
    VM.sysWrite("method_sample_size             int     How many timer ticks of method samples to take before reporting method hotness to controller\n");
    VM.sysWrite("lf                             String  Name of log file\n");
    VM.sysWrite("cafo                           String  Name of advice file\n");
    VM.sysWrite("dcfo                           String  Name of dynamic call graph file\n");
    VM.sysWrite("dna                            String  Name of compiler DNA file (no name ==> use default DNA).  Discussed in a comment at the head of CompilerDNA.java\n");
    VM.sysWrite("cafi                           String  File containing information about the methods to Opt compile\n");
    VM.sysWrite("dcfi                           String  File containing information about the hot call sites\n");
    VM.sysWrite("bulk_compilation_verbosity     int     Control amount of verbosity for bulk compilation (larger ==> more)\n");
    VM.sysWrite("logging_level                  int     Control amount of event logging (larger ==> more)\n");
    VM.sysWrite("final_report_level             int     Control amount of info reported on exit (larger ==> more)\n");
    VM.sysWrite("decay_frequency                int     After how many clock ticks should we decay\n");
    VM.sysWrite("dcg_decay_rate                 double  What factor should we decay call graph edges hotness by\n");
    VM.sysWrite("dcg_sample_size                int     After how many timer interrupts do we update the weights in the dynamic call graph?\n");
    VM.sysWrite("inline_ai_seed_multiplier      double  Initial edge weight of call graph is set to AI_SEED_MULTIPLER * (1/AI_CONTROL_POINT)\n");
    VM.sysWrite("inline_ai_hot_callsite_threshold double  What percentage of the total weight of the dcg demarcates warm/hot edges \n");
    VM.sysWrite("offlinePlan                    String  Name of offline inline plan to be read and used for inlining\n");
    VM.sysWrite("early_exit_time                int     Value of controller clock at which AOS should exit if EARLY_EXIT is true\n");
    VM.sysWrite("invocation_count_threshold     int     Invocation count at which a baseline compiled method should be recompiled\n");
    VM.sysWrite("invocation_count_opt_level     int     Opt level for recompilation in invocation count based system\n");
    VM.sysWrite("counter_based_sample_interval  int     What is the sample interval for counter-based sampling\n");
    VM.sysWrite("max_opt_level                  int     The maximum optimization level to enable.\n");
    VM.sysWrite("\nSelection Options (set option to one of an enumeration of possible values)\n");
    VM.sysWrite("\t\tSelection of initial compiler\n");
    VM.sysWrite("initial_compiler              ");
    VM.sysWrite("base ");
    VM.sysWrite("opt ");
    VM.sysWrite("\n");
    VM.sysWrite("\t\tSelection of mechanism for identifying methods for optimizing recompilation\n");
    VM.sysWrite("recompilation_strategy        ");
    VM.sysWrite("sampling ");
    VM.sysWrite("counters ");
    VM.sysWrite("\n");
    VM.sysWrite("\t\tWhat triggers us to take a method sample?\n");
    VM.sysWrite("method_listener_trigger       ");
    VM.sysWrite("timer ");
    VM.sysWrite("cbs ");
    VM.sysWrite("\n");
    VM.sysWrite("\t\tWhat triggers us to take a method sample?\n");
    VM.sysWrite("call_graph_listener_trigger   ");
    VM.sysWrite("timer ");
    VM.sysWrite("cbs ");
    VM.sysWrite("\n");
    VM.sysWrite("\nSet Options (option is a set of values)\n");
    instancePrintHelpFooter(prefix);

    VM.sysExit(VM.EXIT_STATUS_PRINTED_HELP_MESSAGE);
  }

  /** @return a String representing the options values */
  @Override
  @org.vmmagic.pragma.NoOptCompile
  public String toString() {
    StringBuilder result = new StringBuilder();

    // Begin generated option value printing
    result.append("\tenable_recompilation           = ").append(ENABLE_RECOMPILATION).append("\n");
    result.append("\tenable_advice_generation       = ").append(ENABLE_ADVICE_GENERATION).append("\n");
    result.append("\tenable_bulk_compile            = ").append(ENABLE_BULK_COMPILE).append("\n");
    result.append("\tenable_precompile              = ").append(ENABLE_PRECOMPILE).append("\n");
    result.append("\tgather_profile_data            = ").append(GATHER_PROFILE_DATA).append("\n");
    result.append("\tadaptive_inlining              = ").append(ADAPTIVE_INLINING).append("\n");
    result.append("\tearly_exit                     = ").append(EARLY_EXIT).append("\n");
    result.append("\tosr_promotion                  = ").append(OSR_PROMOTION).append("\n");
    result.append("\tbackground_recompilation       = ").append(BACKGROUND_RECOMPILATION).append("\n");
    result.append("\tinsert_yieldpoint_counters     = ").append(INSERT_YIELDPOINT_COUNTERS).append("\n");
    result.append("\tinsert_method_counters_opt     = ").append(INSERT_METHOD_COUNTERS_OPT).append("\n");
    result.append("\tinsert_instruction_counters    = ").append(INSERT_INSTRUCTION_COUNTERS).append("\n");
    result.append("\tinsert_debugging_counters      = ").append(INSERT_DEBUGGING_COUNTERS).append("\n");
    result.append("\treport_interrupt_stats         = ").append(REPORT_INTERRUPT_STATS).append("\n");
    result.append("\tdisable_recompile_all_methods  = ").append(DISABLE_RECOMPILE_ALL_METHODS).append("\n");
    result.append("\tmethod_sample_size             = ").append(METHOD_SAMPLE_SIZE).append("\n");
    result.append("\tlf                             = ").append(LOGFILE_NAME).append("\n");
    result.append("\tcafo                           = ").append(COMPILATION_ADVICE_FILE_OUTPUT).append("\n");
    result.append("\tdcfo                           = ").append(DYNAMIC_CALL_FILE_OUTPUT).append("\n");
    result.append("\tdna                            = ").append(COMPILER_DNA_FILE_NAME).append("\n");
    result.append("\tcafi                           = ").append(COMPILER_ADVICE_FILE_INPUT).append("\n");
    result.append("\tdcfi                           = ").append(DYNAMIC_CALL_FILE_INPUT).append("\n");
    result.append("\tbulk_compilation_verbosity     = ").append(BULK_COMPILATION_VERBOSITY).append("\n");
    result.append("\tlogging_level                  = ").append(LOGGING_LEVEL).append("\n");
    result.append("\tfinal_report_level             = ").append(FINAL_REPORT_LEVEL).append("\n");
    result.append("\tdecay_frequency                = ").append(DECAY_FREQUENCY).append("\n");
    result.append("\tdcg_decay_rate                 = ").append(DCG_DECAY_RATE).append("\n");
    result.append("\tdcg_sample_size                = ").append(DCG_SAMPLE_SIZE).append("\n");
    result.append("\tinline_ai_seed_multiplier      = ").append(INLINE_AI_SEED_MULTIPLIER).append("\n");
    result.append("\tinline_ai_hot_callsite_threshold = ").append(INLINE_AI_HOT_CALLSITE_THRESHOLD).append("\n");
    result.append("\tofflinePlan                    = ").append(OFFLINE_INLINE_PLAN_NAME).append("\n");
    result.append("\tearly_exit_time                = ").append(EARLY_EXIT_TIME).append("\n");
    result.append("\tinvocation_count_threshold     = ").append(INVOCATION_COUNT_THRESHOLD).append("\n");
    result.append("\tinvocation_count_opt_level     = ").append(INVOCATION_COUNT_OPT_LEVEL).append("\n");
    result.append("\tcounter_based_sample_interval  = ").append(COUNTER_BASED_SAMPLE_INTERVAL).append("\n");
    result.append("\tmax_opt_level                  = ").append(MAX_OPT_LEVEL).append("\n");
    if (INITIAL_COMPILER == IRC_BASE)
       result.append("\tinitial_compiler               = IRC_BASE").append("\n");
    if (INITIAL_COMPILER == IRC_OPT)
       result.append("\tinitial_compiler               = IRC_OPT").append("\n");
    if (RECOMPILATION_STRATEGY == RS_SAMPLING)
       result.append("\trecompilation_strategy         = RS_SAMPLING").append("\n");
    if (RECOMPILATION_STRATEGY == RS_COUNTERS)
       result.append("\trecompilation_strategy         = RS_COUNTERS").append("\n");
    if (METHOD_LISTENER_TRIGGER == ML_TIMER)
       result.append("\tmethod_listener_trigger        = ML_TIMER").append("\n");
    if (METHOD_LISTENER_TRIGGER == ML_CBS)
       result.append("\tmethod_listener_trigger        = ML_CBS").append("\n");
    if (CALL_GRAPH_LISTENER_TRIGGER == CGL_TIMER)
       result.append("\tcall_graph_listener_trigger    = CGL_TIMER").append("\n");
    if (CALL_GRAPH_LISTENER_TRIGGER == CGL_CBS)
       result.append("\tcall_graph_listener_trigger    = CGL_CBS").append("\n");
    return result.toString();
    //End generated toString()
  }

  /** print a String value of this options object */
  @org.vmmagic.pragma.NoOptCompile
  public void printOptions() {
    printOptionsHeader();

    // Begin generated option value printing
    VM.sysWriteln("\tenable_recompilation           = ",ENABLE_RECOMPILATION);
    VM.sysWriteln("\tenable_advice_generation       = ",ENABLE_ADVICE_GENERATION);
    VM.sysWriteln("\tenable_bulk_compile            = ",ENABLE_BULK_COMPILE);
    VM.sysWriteln("\tenable_precompile              = ",ENABLE_PRECOMPILE);
    VM.sysWriteln("\tgather_profile_data            = ",GATHER_PROFILE_DATA);
    VM.sysWriteln("\tadaptive_inlining              = ",ADAPTIVE_INLINING);
    VM.sysWriteln("\tearly_exit                     = ",EARLY_EXIT);
    VM.sysWriteln("\tosr_promotion                  = ",OSR_PROMOTION);
    VM.sysWriteln("\tbackground_recompilation       = ",BACKGROUND_RECOMPILATION);
    VM.sysWriteln("\tinsert_yieldpoint_counters     = ",INSERT_YIELDPOINT_COUNTERS);
    VM.sysWriteln("\tinsert_method_counters_opt     = ",INSERT_METHOD_COUNTERS_OPT);
    VM.sysWriteln("\tinsert_instruction_counters    = ",INSERT_INSTRUCTION_COUNTERS);
    VM.sysWriteln("\tinsert_debugging_counters      = ",INSERT_DEBUGGING_COUNTERS);
    VM.sysWriteln("\treport_interrupt_stats         = ",REPORT_INTERRUPT_STATS);
    VM.sysWriteln("\tdisable_recompile_all_methods  = ",DISABLE_RECOMPILE_ALL_METHODS);
    VM.sysWriteln("\tmethod_sample_size             = ",METHOD_SAMPLE_SIZE);
    VM.sysWriteln("\tlf                             = ",LOGFILE_NAME);
    VM.sysWriteln("\tcafo                           = ",COMPILATION_ADVICE_FILE_OUTPUT);
    VM.sysWriteln("\tdcfo                           = ",DYNAMIC_CALL_FILE_OUTPUT);
    VM.sysWriteln("\tdna                            = ",COMPILER_DNA_FILE_NAME);
    VM.sysWriteln("\tcafi                           = ",COMPILER_ADVICE_FILE_INPUT);
    VM.sysWriteln("\tdcfi                           = ",DYNAMIC_CALL_FILE_INPUT);
    VM.sysWriteln("\tbulk_compilation_verbosity     = ",BULK_COMPILATION_VERBOSITY);
    VM.sysWriteln("\tlogging_level                  = ",LOGGING_LEVEL);
    VM.sysWriteln("\tfinal_report_level             = ",FINAL_REPORT_LEVEL);
    VM.sysWriteln("\tdecay_frequency                = ",DECAY_FREQUENCY);
    VM.sysWriteln("\tdcg_decay_rate                 = ",DCG_DECAY_RATE);
    VM.sysWriteln("\tdcg_sample_size                = ",DCG_SAMPLE_SIZE);
    VM.sysWriteln("\tinline_ai_seed_multiplier      = ",INLINE_AI_SEED_MULTIPLIER);
    VM.sysWriteln("\tinline_ai_hot_callsite_threshold = ",INLINE_AI_HOT_CALLSITE_THRESHOLD);
    VM.sysWriteln("\tofflinePlan                    = ",OFFLINE_INLINE_PLAN_NAME);
    VM.sysWriteln("\tearly_exit_time                = ",EARLY_EXIT_TIME);
    VM.sysWriteln("\tinvocation_count_threshold     = ",INVOCATION_COUNT_THRESHOLD);
    VM.sysWriteln("\tinvocation_count_opt_level     = ",INVOCATION_COUNT_OPT_LEVEL);
    VM.sysWriteln("\tcounter_based_sample_interval  = ",COUNTER_BASED_SAMPLE_INTERVAL);
    VM.sysWriteln("\tmax_opt_level                  = ",MAX_OPT_LEVEL);
    if (INITIAL_COMPILER == IRC_BASE)
       VM.sysWriteln("\tinitial_compiler               = IRC_BASE");
    if (INITIAL_COMPILER == IRC_OPT)
       VM.sysWriteln("\tinitial_compiler               = IRC_OPT");
    if (RECOMPILATION_STRATEGY == RS_SAMPLING)
       VM.sysWriteln("\trecompilation_strategy         = RS_SAMPLING");
    if (RECOMPILATION_STRATEGY == RS_COUNTERS)
       VM.sysWriteln("\trecompilation_strategy         = RS_COUNTERS");
    if (METHOD_LISTENER_TRIGGER == ML_TIMER)
       VM.sysWriteln("\tmethod_listener_trigger        = ML_TIMER");
    if (METHOD_LISTENER_TRIGGER == ML_CBS)
       VM.sysWriteln("\tmethod_listener_trigger        = ML_CBS");
    if (CALL_GRAPH_LISTENER_TRIGGER == CGL_TIMER)
       VM.sysWriteln("\tcall_graph_listener_trigger    = CGL_TIMER");
    if (CALL_GRAPH_LISTENER_TRIGGER == CGL_CBS)
       VM.sysWriteln("\tcall_graph_listener_trigger    = CGL_CBS");
    //End generated option value printing
  }
// END CODE GENERATED FROM MasterOptions.template

  private boolean instanceProcessAsOption(String arg) {
    return false;
  }

  private static void instancePrintHelpHeader(String prefix) {
    VM.sysWrite("Commands\n");
    VM.sysWrite(prefix+"[:help]       Print a brief description of AOS command-line options\n");
    VM.sysWrite(prefix+":printOptions Print the current option values of AOS\n");
    VM.sysWrite(prefix+":o=v          Pass the option-value pair, o=v, to AOS\n");
    VM.sysWrite("\n");
  }

  private static void instancePrintHelpFooter(String prefix) {
  }
}
