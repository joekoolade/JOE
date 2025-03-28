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
package org.jikesrvm;

import static org.jikesrvm.runtime.ExitStatus.EXIT_STATUS_BOGUS_COMMAND_LINE_ARG;
import static org.jikesrvm.runtime.ExitStatus.EXIT_STATUS_RECURSIVELY_SHUTTING_DOWN;
import static org.jikesrvm.runtime.ExitStatus.EXIT_STATUS_SYSFAIL;
import static org.jikesrvm.runtime.SysCall.sysCall;
import static org.jikesrvm.runtime.UnboxedSizeConstants.BITS_IN_ADDRESS;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Currency;
import java.util.Enumeration;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.jam.board.pc.I8042;
import org.jam.board.pc.Platform;
import org.jam.driver.net.NapiManager;
import org.jam.driver.serial.PcBootSerialPort;
import org.jam.driver.serial.SerialPortBaudRate;
import org.jam.net.Dhcp;
import org.jam.runtime.RunMain;
import org.jam.runtime.RunThread;
import org.jam.runtime.RunThread2;
import org.jam.runtime.StartUp;
import org.jam.runtime.SystemJars;
import org.jam.system.Trace;
import org.jam.tests.LocaleTest;
import org.jikesrvm.adaptive.controller.Controller;
import org.jikesrvm.adaptive.util.CompilerAdvice;
import org.jikesrvm.architecture.StackFrameLayout;
import org.jikesrvm.classlibrary.ClassLibraryHelpers;
import org.jikesrvm.classlibrary.ClassLoaderSupport;
import org.jikesrvm.classlibrary.JavaLangSupport;
import org.jikesrvm.classloader.Atom;
import org.jikesrvm.classloader.BootstrapClassLoader;
import org.jikesrvm.classloader.ClassNameHelpers;
import org.jikesrvm.classloader.JMXSupport;
import org.jikesrvm.classloader.MemberReference;
import org.jikesrvm.classloader.RVMClass;
import org.jikesrvm.classloader.RVMClassLoader;
import org.jikesrvm.classloader.RVMMember;
import org.jikesrvm.classloader.RVMMethod;
import org.jikesrvm.classloader.TypeDescriptorParsing;
import org.jikesrvm.classloader.TypeReference;
import org.jikesrvm.compilers.baseline.BaselineCompiler;
import org.jikesrvm.compilers.common.BootImageCompiler;
import org.jikesrvm.compilers.common.RuntimeCompiler;
import org.jikesrvm.ia32.ThreadLocalState;
import org.jikesrvm.mm.mminterface.MemoryManager;
import org.jikesrvm.runtime.ArchEntrypoints;
import org.jikesrvm.runtime.BootRecord;
import org.jikesrvm.runtime.Callbacks;
import org.jikesrvm.runtime.CommandLineArgs;
import org.jikesrvm.runtime.DynamicLibrary;
import org.jikesrvm.runtime.Entrypoints;
import org.jikesrvm.runtime.ExternalFile;
import org.jikesrvm.runtime.FileSystem;
import org.jikesrvm.runtime.Magic;
import org.jikesrvm.runtime.RuntimeEntrypoints;
import org.jikesrvm.runtime.SysCall;
import org.jikesrvm.runtime.Time;
import org.jikesrvm.scheduler.IdleThread;
import org.jikesrvm.scheduler.Lock;
import org.jikesrvm.scheduler.MainThread;
import org.jikesrvm.scheduler.RVMThread;
import org.jikesrvm.scheduler.Synchronization;
import org.jikesrvm.tuningfork.TraceEngine;
import org.jikesrvm.util.Services;
import org.vmmagic.pragma.Entrypoint;
import org.vmmagic.pragma.Inline;
import org.vmmagic.pragma.Interruptible;
import org.vmmagic.pragma.NoInline;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.pragma.UninterruptibleNoWarn;
import org.vmmagic.pragma.Unpreemptible;
import org.vmmagic.pragma.UnpreemptibleNoWarn;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Extent;
import org.vmmagic.unboxed.ObjectReference;
import org.vmmagic.unboxed.Offset;
import org.vmmagic.unboxed.Word;

/**
 * A virtual machine.
 */
@Uninterruptible
public class VM extends Properties {

  /**
   * For assertion checking things that should never happen.
   */
  public static final boolean NOT_REACHED = false;

  /**
   * Reference to the main thread that is the first none VM thread run
   */
  public static MainThread mainThread;
  public static boolean booting;
  public static final boolean joeMode = true;
  public static String runClasses[];
  
  //----------------------------------------------------------------------//
  //                          Initialization.                             //
  //----------------------------------------------------------------------//

  /**
   * Prepare VM classes for use by boot image writer.
   * @param classPath class path to be used by RVMClassLoader
   * @param bootCompilerArgs command line arguments for the bootimage compiler
   */
  @Interruptible
  public static void initForBootImageWriter(String classPath, String[] bootCompilerArgs) {
    if (VM.VerifyAssertions) VM._assert(!VM.runningVM);
    if (VM.VerifyAssertions) VM._assert(!VM.runningTool);
    writingBootImage = true;
    init(classPath, bootCompilerArgs);
  }

  /**
   * Prepare VM classes for use by tools.
   */
  @Interruptible
  public static void initForTool() {
    initForTool(System.getProperty("java.class.path"));
  }

  /**
   * Prepare VM classes for use by tools.
   * @param classpath class path to be used by RVMClassLoader
   */
  @Interruptible
  public static void initForTool(String classpath) {
    if (VM.VerifyAssertions) VM._assert(!VM.runningVM);
    if (VM.VerifyAssertions) VM._assert(!VM.writingBootImage);
    runningTool = true;
    init(classpath, null);
  }

  /**
   * Begin VM execution.<p>
   *
   * Uninterruptible because we are not setup to execute a yieldpoint
   * or stackoverflow check in the prologue this early in booting.<p>
   *
   * The following machine registers are set by "C" bootstrap program
   * before calling this method:
   * <ol>
   *   <li>JTOC_POINTER - required for accessing globals
   *   <li>FRAME_POINTER - required for accessing locals
   *   <li>THREAD_ID_REGISTER - required for method prolog (stack overflow check)
   * </ol>
   */
  @UnpreemptibleNoWarn("No point threading until threading is booted")
  @Entrypoint
  public static void boot() {
    writingBootImage = false;
    runningVM = true;
    booting=true;
    ThreadLocalState.setCurrentThread(RVMThread.bootThread);
    /*
     * Setup the serial port
     */
    PcBootSerialPort.setBaudRate(SerialPortBaudRate.BAUDRATE_115200);
    PcBootSerialPort.setParityNone();
    PcBootSerialPort.setWordLength8();
    PcBootSerialPort.setStopBits1();
    VM.sysWriteln("\nThe Start");
//    verboseBoot = BootRecord.the_boot_record.verboseBoot;
    verboseSignalHandling = BootRecord.the_boot_record.verboseSignalHandling != 0;

    sysWriteLockOffset = Entrypoints.sysWriteLockField.getOffset();
    if (verboseBoot >= 1) VM.sysWriteln("Booting");

    // Set up the current RVMThread object.  The bootstrap program
    // has placed a pointer to the current RVMThread in a special
    // register.
    if (verboseBoot >= 1) VM.sysWriteln("Setting up current RVMThread");

    if (VM.BuildForIA32) {
      org.jikesrvm.ia32.ThreadLocalState.boot();
    } else {
      if (VM.VerifyAssertions) VM._assert(VM.BuildForPowerPC);
      org.jikesrvm.ppc.ThreadLocalState.boot();
    }

    // Finish thread initialization that couldn't be done in boot image.
    // The "stackLimit" must be set before any interruptible methods are called
    // because it's accessed by compiler-generated stack overflow checks.
    //
    if (verboseBoot >= 1) VM.sysWriteln("Doing thread initialization");
    RVMThread currentThread = RVMThread.getCurrentThread();
    currentThread.stackLimit = Magic.objectAsAddress(
        currentThread.getStack()).plus(StackFrameLayout.getStackSizeGuard());

    finishBooting();
  }

  /**
   * Complete the task of booting Jikes RVM.
   * Done in a secondary method mainly because this code
   * doesn't have to be uninterruptible and this is the cleanest
   * way to make that distinction.
   */
  @Interruptible
  private static void finishBooting() {

    // get pthread_id from OS and store into vm_processor field
    //
    RVMThread.getCurrentThread().pthread_id = sysCall.sysGetThreadId();
    RVMThread.getCurrentThread().priority_handle = sysCall.sysGetThreadPriorityHandle();
    RVMThread.availableProcessors = SysCall.sysCall.sysNumProcessors();

    // Set up buffer locks used by Thread for logging and status dumping.
    //    This can happen at any point before we start running
    //    multi-threaded.
    Services.boot();

    /*
     * Baremetal memory initialization
     */
    org.jam.mm.MemoryManager.boot(BootRecord.the_boot_record);
    
    // Initialize memory manager.
    //    This must happen before any uses of "new".
    //
    if (verboseBoot >= 1) {
      VM.sysWriteln("Setting up memory manager: bootrecord = ",
                    Magic.objectAsAddress(BootRecord.the_boot_record));
      VM.sysWriteln("Setting up memory manager: bootThread = ", Magic.objectAsAddress(RVMThread.bootThread));
    }
    Platform.initTimers();
    MemoryManager.boot(BootRecord.the_boot_record);

    // Reset the options for the baseline compiler to avoid carrying
    // them over from bootimage writing time.
    //
    if (verboseBoot >= 1) VM.sysWriteln("Initializing baseline compiler options to defaults");
    BaselineCompiler.initOptions();

    // Create JNI Environment for boot thread.
    // After this point the boot thread can invoke native methods.
    org.jikesrvm.jni.JNIEnvironment.boot();
    if (verboseBoot >= 1) VM.sysWriteln("Initializing JNI for boot thread");
    RVMThread.getCurrentThread().initializeJNIEnv();
    if (verboseBoot >= 1) VM.sysWriteln("JNI initialized for boot thread");

    // Fetch arguments from program command line.
    //
    if (verboseBoot >= 1) VM.sysWriteln("Fetching command-line arguments");
    CommandLineArgs.fetchCommandLineArguments();

    // Process most virtual machine command line arguments.
    //
    if (verboseBoot >= 1) VM.sysWriteln("Early stage processing of command line");
    CommandLineArgs.earlyProcessCommandLineArguments();

    // Early initialization of TuningFork tracing engine.
    TraceEngine.engine.earlyStageBooting();

    // Allow Memory Manager to respond to its command line arguments
    //
    if (verboseBoot >= 1) VM.sysWriteln("Collector processing rest of boot options");
    MemoryManager.postBoot();
    if (verboseBoot >= 1) VM.sysWriteln("Done: Collector processing rest of boot options");

    // Initialize class loader.
    //
    String bootstrapClasses = ""; // CommandLineArgs.getBootstrapClasses();
    if (verboseBoot >= 1) VM.sysWriteln("Initializing bootstrap class loader: ", bootstrapClasses);
    Callbacks.addClassLoadedMonitor(JMXSupport.CLASS_LOADING_JMX_SUPPORT);
    RVMClassLoader.boot();      // Wipe out cached application class loader
    BootstrapClassLoader.boot(bootstrapClasses);

    // Initialize statics that couldn't be placed in bootimage, either
    // because they refer to external state (open files), or because they
    // appear in fields that are unique to Jikes RVM implementation of
    // standard class library (not part of standard JDK).
    // We discover the latter by observing "host has no field" and
    // "object not part of bootimage" messages printed out by bootimage
    // writer.
    //
    if (verboseBoot >= 1) VM.sysWriteln("Running various class initializers");

    runClassInitializer("sun.misc.Unsafe");
    runClassInitializer("java.util.concurrent.atomic.AtomicLong");
    runClassInitializer("java.util.concurrent.atomic.AtomicInteger");
    runClassInitializer("java.util.Random");
    runClassInitializer("java.lang.ref.ReferenceQueue");
    runClassInitializer("java.lang.ThreadLocal");
    if (VM.BuildForOpenJDK) {
      runClassInitializer("java.util.WeakHashMap"); // Need for ThreadLocal
      runClassInitializer("java.lang.ref.Reference");
    }

    if (VM.BuildForGnuClasspath) {
      runClassInitializer("gnu.classpath.SystemProperties");
      runClassInitializer("java.lang.Throwable$StaticData");
    }

    runClassInitializer("java.lang.Runtime");
    if (!VM.BuildForOpenJDK) {
      runClassInitializer("java.lang.System");
    }

    runClassInitializer("java.lang.Character");

    if (VM.BuildForOpenJDK) {
      runClassInitializer("java.lang.CharacterDataLatin1");
      runClassInitializer("java.lang.CharacterData00");
      runClassInitializer("java.lang.CharacterData01");
      runClassInitializer("java.lang.CharacterData02");
      runClassInitializer("java.lang.CharacterData0E");
      runClassInitializer("java.lang.CharacterDataPrivateUse");
      runClassInitializer("java.lang.CharacterDataUndefined");
    }

    // Turn off security checks; about to hit EncodingManager.
    // Commented out because we haven't incorporated this into the CVS head
    // yet.
    // java.security.JikesRVMSupport.turnOffChecks();
    if (VM.BuildForGnuClasspath) {
      runClassInitializer("java.lang.ThreadGroup");
    }
    /* We can safely allocate a java.lang.Thread now.  The boot
       thread (running right now, as a Thread) has to become a full-fledged
       Thread, since we're about to encounter a security check:

       EncodingManager checks a system property,
        which means that the permissions checks have to be working,
        which means that VMAccessController will be invoked,
        which means that ThreadLocal.get() will be called,
        which calls Thread.getCurrentThread().

        So the boot Thread needs to be associated with a real Thread for
        Thread.getCurrentThread() to return. */
    VM.safeToAllocateJavaThread = true;

    if (VM.BuildForGnuClasspath) {
      runClassInitializer("java.lang.ThreadLocal");
      runClassInitializer("java.lang.ThreadLocalMap");
    }
    // Possibly fix VMAccessController's contexts and inGetContext fields
    if (VM.BuildForGnuClasspath) {
      runClassInitializer("java.security.VMAccessController");
    }
    if (verboseBoot >= 1) VM.sysWriteln("Booting Lock");
    Lock.boot();

    if (VM.BuildForOpenJDK) {
      runClassInitializer("java.lang.Thread");
      if (VM.BuildForLinux) runClassInitializer("java.net.NetPermission");
      runClassInitializer("sun.security.util.SecurityConstants");
    }

    // Enable multiprocessing.
    // Among other things, after this returns, GC and dynamic class loading are enabled.
    if (verboseBoot >= 1) VM.sysWriteln("Booting scheduler");
    Platform.initScheduler();
    
    ThreadGroup mainThreadGroup = null;
    if (VM.BuildForOpenJDK) {
      // Create initial thread group. This has to be done before creating other threads.
      ThreadGroup systemThreadGroup = ClassLibraryHelpers.allocateObjectForClassAndRunNoArgConstructor(ThreadGroup.class);
      RVMThread.setThreadGroupForSystemThreads(systemThreadGroup);
      // We'll need the main group later for the first application thread.
      mainThreadGroup = new ThreadGroup(systemThreadGroup, "main");

      // Early set-up for the boot thread is required for OpenJDK.
      if (verboseBoot >= 1) VM.sysWriteln("Setting up boot thread");
      RVMThread.getCurrentThread().setupBootJavaThread();
      VM.sysWriteln("boot thread setup done!");
    }
    runClassInitializer("org.jikesrvm.classloader.TypeReferenceVector");
    runClassInitializer("org.jikesrvm.classloader.MethodVector");
    runClassInitializer("org.jikesrvm.classloader.FieldVector");

   runClassInitializer("org.jikesrvm.classloader.Atom$InternedStrings");

    RVMThread.boot();
    VM.sysWriteln("rvmthread boot done");
	// Initialize properties early, before complete java.lang.System initialization.
	// Those will be
	// overwritten later, when System is initialized.
	System.setProperties(null);
	VM.sysWriteln("System properties preinit");
	runClassInitializer("java.lang.Thread");
	// runClassInitializer("java.util.WeakHashMap"); // Need for ThreadLocal
	// runClassInitializer("java.lang.Class$Atomic");

    if (verboseBoot >= 1) VM.sysWriteln("Enabling GC");
    MemoryManager.enableCollection();
    VM.safeToCreateStackTrace = true;

    // properties are needed for java.io.File which calls the constructor of UnixFileSystem
	runClassInitializer("sun.misc.Version");
	runClassInitializer("sun.misc.VM");
	runClassInitializer("sun.misc.SharedSecrets");
	runClassInitializer("java.io.Console");
	runClassInitializer("java.util.concurrent.atomic.AtomicInteger");
	runClassInitializer("java.io.JavaFileSystem");
	runClassInitializer("java.io.FileDescriptor");
	runClassInitializer("java.io.File");
	runClassInitializer("java.io.FileInputStream");
	runClassInitializer("java.io.FileOutputStream");
	// runClassInitializer("java/lang/reflect/Modifier");
	runClassInitializer("java.util.Collections");
	runClassInitializer("sun.reflect.Reflection");
	// runClassInitializer("java.lang.reflect.Proxy");
	// runClassInitializer("java.util.concurrent.atomic.AtomicReferenceFieldUpdater$AtomicReferenceFieldUpdaterImpl");
	runClassInitializer("java.io.BufferedInputStream");
	runClassInitializer("java.nio.DirectByteBuffer");
	runClassInitializer("java.nio.Bits$1");
	runClassInitializer("java.nio.Bits");
	runClassInitializer("sun.nio.cs.StreamEncoder");
	runClassInitializer("java.nio.charset.Charset");
	runClassInitializer("java.lang.Shutdown");
	if (verboseBoot >= 1) VM.sysWriteln("initializing standard streams");
	// Initialize java.lang.System.out, java.lang.System.err, java.lang.System.in
	FileSystem.initializeStandardStreamsForOpenJDK();
	runClassInitializer("java.io.FileSystem");

    runClassInitializer("java.lang.Integer");
    runClassInitializer("java.lang.Integer$IntegerCache");
    runClassInitializer("java.lang.String");
//    runClassInitializer("java.net.URL"); // needed for URLClassLoader


    runClassInitializer("java.util.zip.ZipEntry");
    runClassInitializer("java.util.zip.CRC32");
    runClassInitializer("java.util.zip.Inflater");
    runClassInitializer("java.util.zip.Deflate");
    // Run class initializers that require JNI
	if (verboseBoot >= 1) VM.sysWriteln("Running late class initializers");
	runClassInitializer("java.lang.Math");
	runClassInitializer("java.util.TreeMap");
	runClassInitializer("java.util.jar.JarFile");
	runClassInitializer("java.util.zip.ZipFile");
	runClassInitializer("java.util.BitSet"); // needed when using IBM SDK as host JVM
	runClassInitializer("org.jikesrvm.classloader.RVMAnnotation");
    runClassInitializer("java.nio.charset.Charset");
    runClassInitializer("sun.nio.cs.StandardCharsets");
    runClassInitializer("sun.nio.cs.Surrogate$Parser");
    runClassInitializer("sun.nio.cs.Surrogate$Generator");
    runClassInitializer("sun.nio.cs.Surrogate");
    runClassInitializer("java.util.Random");
    runClassInitializer("java.net.InetAddress");
    runClassInitializer("java.util.ArrayList");
    runClassInitializer("java.lang.Throwable");
    runClassInitializer("java.util.zip.InfBlocks");
    runClassInitializer("java.util.zip.InfCodes");
    runClassInitializer("java.util.zip.InfTrees");
//    runClassInitializer("tests.java.net.DatagramClientServer");
    
    ///////////////////////////////////////////////////////////////
    // The VM is now fully booted.                               //
    // By this we mean that we can execute arbitrary Java code.  //
    ///////////////////////////////////////////////////////////////
    if (verboseBoot >= 1) VM.sysWriteln("VM is now fully booted");

    Exception e0 = new Exception();
    VM.sysWriteln("Exception done!");
    // Inform interested subsystems that VM is fully booted.
    VM.fullyBooted = true;
    MemoryManager.fullyBootedVM();
    org.jikesrvm.mm.mminterface.JMXSupport.fullyBootedVM();
    BaselineCompiler.fullyBootedVM();
    TraceEngine.engine.fullyBootedVM();

    if (!joeMode)
    {
      runClassInitializer("java.util.logging.Level");
      runClassInitializer("java.util.logging.Logger");
    }
    
    // Initialize compiler that compiles dynamically loaded classes.
    //
    if (verboseBoot >= 1) VM.sysWriteln("Initializing runtime compiler");
    RuntimeCompiler.boot();

    // Process remainder of the VM's command line arguments.
    if (verboseBoot >= 1) VM.sysWriteln("Late stage processing of command line");
    String[] applicationArguments = CommandLineArgs.lateProcessCommandLineArguments();

    if (VM.BuildForOpenJDK) {
      // Reset values for usr_paths and sys_paths to pick up command line arguments
      Magic.setObjectAtOffset(Magic.getJTOC().toObjectReference().toObject(), Entrypoints.usr_paths_Field.getOffset(), null);
      Magic.setObjectAtOffset(Magic.getJTOC().toObjectReference().toObject(), Entrypoints.sys_paths_Field.getOffset(), null);
    }

    if (VM.verboseClassLoading || verboseBoot >= 1) VM.sysWriteln("[VM booted]");

    if (VM.BuildForAdaptiveSystem) {
      if (verboseBoot >= 1) VM.sysWriteln("Initializing adaptive system");
      Controller.boot();
    }

    if (!joeMode)
    {
      // The first argument must be a class name.
      if (verboseBoot >= 1)
        VM.sysWriteln("Extracting name of class to execute");
      if (applicationArguments.length == 0)
      {
        pleaseSpecifyAClass();
      }
      if (applicationArguments.length > 0 && !TypeDescriptorParsing.isJavaClassName(applicationArguments[0]))
      {
        VM.sysWrite("vm: \"");
        VM.sysWrite(applicationArguments[0]);
        VM.sysWriteln("\" is not a legal Java class name.");
        pleaseSpecifyAClass();
      }

      if (applicationArguments.length > 0 && applicationArguments[0].startsWith("-X"))
      {
        VM.sysWrite("vm: \"");
        VM.sysWrite(applicationArguments[0]);
        VM.sysWriteln("\" is not a recognized Jikes RVM command line argument.");
        VM.sysExit(EXIT_STATUS_BOGUS_COMMAND_LINE_ARG);
      }

      if (verboseBoot >= 1)
        VM.sysWriteln("Initializing Application Class Loader");
      RVMClassLoader.rebuildApplicationRepositoriesWithAgents();
      RVMClassLoader.getApplicationClassLoader();
      RVMClassLoader.declareApplicationClassLoaderIsReady();

      if (verboseBoot >= 1)
      {
        VM.sysWriteln("Turning back on security checks.  Letting people see the ApplicationClassLoader.");
      }
      // Turn on security checks again.
      // Commented out because we haven't incorporated this into the main CVS
      // tree yet.
      // java.security.JikesRVMSupport.fullyBootedVM();
    }

    if (VM.BuildForOpenJDK) {
      ClassLoader appCl = RVMClassLoader.getApplicationClassLoader();
      Magic.setObjectAtOffset(Magic.getJTOC().toObjectReference().toObject(), Entrypoints.scl_Field.getOffset(), appCl);
      Magic.setBooleanAtOffset(Magic.getJTOC().toObjectReference().toObject(), Entrypoints.sclSet_Field.getOffset(), true);
      if (VM.VerifyAssertions) VM._assert(ClassLoader.getSystemClassLoader() == appCl);
    }
    
    if (VM.BuildForGnuClasspath) {
      runClassInitializer("java.lang.ClassLoader$StaticData");
    }
    
    /*
     * Add all JOE specific classes here
     */
    if (VM.joeMode)
    {
    	VM.sysWriteln("Initialize JOE specific classes");
    	runClassInitializer("org.jam.driver.net.NapiManager");
    }
    
    if (VM.BuildForAdaptiveSystem) {
      CompilerAdvice.postBoot();
    }

    VM.sysWriteln("contextRegister offset ", Entrypoints.threadContextRegistersField.getOffset());
    VM.sysWriteln("gprs offset ", ArchEntrypoints.registersGPRsField.getOffset());
    VM.sysWriteln("sp offset ", Entrypoints.stackPointerField.getOffset());

    /*
     * Initialize Trace class
     */
    Trace.init();

    /*
     * Need to initialize the platform here because we need the scheduler to start
     * threads.
     */
    Platform.boot();

    // Put the IdleThread on the queue
    if (verboseBoot >= 1)
    {
      VM.sysWriteln("Idle Thread");
    }
    new IdleThread().start();

    // enable alignment checking
    if (VM.AlignmentChecking) {
      SysCall.sysCall.sysEnableAlignmentChecking();
    }

    Time.boot();

    // Set up properties for our custom JUnit test runner.
    Configuration.setupPropertiesForUnitTesting();

    // Schedule "main" thread for execution.
    if (verboseBoot >= 2) VM.sysWriteln("Creating main thread");
    // Create main thread.
    if (verboseBoot >= 1) VM.sysWriteln("Constructing mainThread");
//    mainThread = new MainThread(applicationArguments, mainThreadGroup);
    
    System.setOut(Platform.serialPort.getPrintStream());
    System.setErr(Platform.serialPort.getPrintStream());
    System.out.println("System out printout!");
    // Schedule "main" thread for execution.
//    if (verboseBoot >= 1) VM.sysWriteln("Starting main thread");
//    mainThread.start();

    VM.sysWriteln("Java System size ", SystemJars.systemJar.length);
    try
    {
        ZipFile javaZip = new ZipFile(SystemJars.systemJar);
        BootstrapClassLoader.addZipFile("bootstrap", javaZip);
    }
    catch(IOException e)
    {
        VM.sysWriteln(e.toString());
    }
//    VM.shutdown(1);

    /*
     * Load external classes and jars
     */
    ExternalFile extFile[] = BootRecord.the_boot_record.files;
    if(extFile == null) VM.sysWriteln("No external files");
    else VM.sysWriteln("External files = ", extFile.length);
    ZipFile extJarFile = null;
    for(int i=0; i < extFile.length; i++)
    {
        if(extFile[i] == null) break;
        VM.sysWriteln("Loading  " + extFile[i].name);
        if(extFile[i].name.endsWith(".jar"))
        {
            try {
                int prefixIndex = extFile[i].name.indexOf(".jar");
                int dirIndex = extFile[i].name.lastIndexOf("/");
                extJarFile = new ZipFile(extFile[i].data);
                String prefixName = extFile[i].name.substring(dirIndex+1, prefixIndex);
                VM.sysWriteln("Jar tag  " + prefixName);
                BootstrapClassLoader.addZipFile(prefixName, extJarFile);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    VM.sysWriteln("Done loading");
    BootstrapClassLoader.addClasspath("/classpath/");
//    dig.main("");
//    if(extJarFile != null)
//    {
//        VM.sysWriteln("Inflating jar file");
//        Enumeration<? extends ZipEntry> jarEnum = extJarFile.entries();
//        while(jarEnum.hasMoreElements())
//        {
//            ZipEntry zipFileEntry = jarEnum.nextElement();
//            VM.sysWriteln("Inflating: ", zipFileEntry.getName());
//            int fileSize = (int) zipFileEntry.getSize();
////            VM.sysWriteln("size: ", fileSize);
//            InputStream in;
//            try {
//                in = extJarFile.getInputStream(zipFileEntry);
//                byte fileData[] = new byte[fileSize];
//                in.read(fileData, 0, fileSize);
////                if(fileSize >= 32)
////                    VM.hexDump(fileData, 0, 32);
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
//    }
//    RunMain runMain = new RunMain("tests.java.net.DatagramClientServer");
//    runMain.run();
//    
//    // End of boot thread.
//    //
//    if (VM.TraceThreads) RVMThread.trace("VM.boot", "completed - terminating");
//    if (verboseBoot >= 2) {
//      VM.sysWriteln("Boot sequence completed; finishing boot thread");
//    }
//
//    if(BootRecord.the_boot_record.testMode)
//    {
//        int i;
//        String[] classes = BootRecord.the_boot_record.testClasses;
//        int prevVerbose = verboseBoot;
//        verboseBoot = 2;
//        for(i=0; i < classes.length; i++)
//        {
//            VM.sysWriteln("Initializing class: " + classes[i]);
//            runClassInitializer(classes[i]);;
//        }
//        verboseBoot = prevVerbose;
//        classes = BootRecord.the_boot_record.runMainClasses;
//        for(i=0; i < classes.length; i++)
//        {
//          VM.sysWriteln("Starting Main class: " + classes[i]);
//        }
//        StartUp.testModeRun(classes);
//    }
//    new Thread(new LocaleTest()).run();
//    VM.sysWriteln("Currency");
//    Locale locale = Locale.getDefault();
//    VM.sysWriteln("locale "+locale);
//    Currency cur = Currency.getInstance(locale);
//    new LocaleTest().localeTest();
    
//	Dhcp.discover(Platform.net);
//    VM.sysWriteln("INET boot done");
//    System.out.println("pattern: "+dformat.toPattern());
//  VM.sysWriteln("decima format ...");
//  DecimalFormat df = new DecimalFormat();
//  VM.sysWriteln("df done");
//  Class dig;
//  System.setProperty("dns.server", "10.0.2.3");
//  System.setProperty("dns.search", "localhost.com");
  // For now need have DecimalFormat in the primordials
    runClassInitializer("java.text.DecimalFormat");
//  VM.verboseClassLoading = true;
//  VM.TraceClassLoading = true;
  
//  RunThread2 test = new RunThread2("ext.tests.DnsTest");
//  VM.sysWriteln("created runthread2");
//  new Thread(test).run();
//  VM.sysWriteln("running runthread2");
  
    System.setProperty("java.security.debug", "all");
//  try {
//      BootstrapClassLoader.getBootstrapClassLoader().loadClass("com.sun.crypto.provider.SunJCE", true);
//      System.setProperty("dns.server", "10.0.2.3");
//      System.setProperty("dns.search", "localhost.com");
//      System.setProperty("dnsjava.options", "verbose=true");
//      BootstrapClassLoader.getBootstrapClassLoader().loadClass("dig", true);
//      String args[] = { "dig", "viasat.com", "ANY", "-q" };
//      RunMain dig = new RunMain("dig", args);
//      dig.run();
//      VM.sysWriteln("dig running");
//  } catch (Exception e) {
//      VM.sysWriteln("dig execption!");
//      e.printStackTrace();
//  }
  
    VM.sysWriteln("test setup");
    RunMain test = new RunMain("Basic", null);
    VM.sysWriteln("test run");
    test.run();
//    VM.verboseClassLoading = true;
//    VM.TraceClassLoading = true;
    
    RVMThread.getCurrentThread().terminate();  
    // Say good bye to the boot thread
    Magic.enableInterrupts();
    Platform.ioApic.enableInterrupts();
    booting = false;
    RVMThread.yieldNoHandshake();
    VM.shutdown(1);
    VM.sysWriteln("Boot thread has been resurrected! This is bad!!!");
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);
  }

  static void loopForever()
  {
      while(true) ;
  }
  @Interruptible
  private static void pleaseSpecifyAClass() {
    VM.sysWriteln("vm: Please specify a class to execute.");
    VM.sysWriteln("vm:   You can invoke the VM with the \"-help\" flag for usage information.");
    VM.sysExit(EXIT_STATUS_BOGUS_COMMAND_LINE_ARG);
  }

  private static void loadExternalClass(ExternalFile file)
  {
      VM.sysWriteln("defining "+file.getClassName());
      Class cls = ClassLoaderSupport.defineClass(null, file.getClassName(), file.data, 0, file.data.length, null);
      VM.sysWriteln("defined "+file.getClassName());
      ClassLoaderSupport.resolveClass(cls);
  }
  
  /**
   * Run {@code <clinit>} method of specified class, if that class appears
   * in bootimage and actually has a clinit method (we are flexible to
   * allow one list of classes to work with different bootimages and
   * different version of classpath (eg 0.05 vs. cvs head).
   * <p>
   * This method is called only while the VM boots.
   *
   * @param className class whose initializer needs to be run
   */
  @Interruptible
  static void runClassInitializer(String className) {
    if (verboseBoot >= 2) {
      sysWrite("running class initializer for ");
      sysWriteln(className);
    }
    Atom classDescriptor = Atom.findOrCreateAsciiAtom(ClassNameHelpers.convertClassnameToInternalName(className)).descriptorFromClassName();
    TypeReference tRef =
        TypeReference.findOrCreate(BootstrapClassLoader.getBootstrapClassLoader(), classDescriptor);
    RVMClass cls = (RVMClass) tRef.peekType();
    if (null == cls) {
      sysWrite("Failed to run class initializer for ");
      sysWrite(className);
      sysWriteln(" as the class does not exist.");
    } else if (!cls.isInBootImage()) {
      sysWrite("Failed to run class initializer for ");
      sysWrite(className);
      sysWriteln(" as the class is not in the boot image.");
    } else if (cls.isEnum() && VM.BuildForOpenJDK) {
        sysWrite("Not attempting to run class initializer for ");
        sysWrite(className);
        sysWriteln(" because it's not advisable to run because it's an enum for OpenJDK." +
            " Running the class initializer would break" +
            " the enumConstantsDirectory in the associated class object.");
        VM.sysFail("Attempted to run class initializer for enum " + className);
    } else {
      RVMMethod clinit = cls.getClassInitializerMethod();
      if (clinit != null) {
          if (verboseBoot >= 10) VM.sysWriteln("invoking method " + clinit);
        clinit.compile();
        try {
          Magic.invokeClassInitializer(clinit.getCurrentEntryCodeArray());
        } catch (Error e) {
          throw e;
        } catch (Throwable t) {
          ExceptionInInitializerError eieio =
              new ExceptionInInitializerError(t);
          throw eieio;
        }
        // <clinit> is no longer needed: reclaim space by removing references to it
        clinit.invalidateCompiledMethod(clinit.getCurrentCompiledMethod());
      } else {
        if (verboseBoot >= 10) VM.sysWriteln("has no clinit method ");
      }
      if (verboseBoot >= 10)
        VM.sysWriteln("clinit method done");
      cls.setAllFinalStaticJTOCEntries();
    }
  }

  //----------------------------------------------------------------------//
  //                         Execution environment.                       //
  //----------------------------------------------------------------------//

  /**
   * Verify a runtime assertion (die w/traceback if assertion fails).<p>
   *
   * Note: code your assertion checks as
   * {@code if (VM.VerifyAssertions) VM._assert(xxx);}
   * @param b the assertion to verify
   */
  @Uninterruptible
  @Inline(value = Inline.When.AllArgumentsAreConstant)
  public static void _assert(boolean b) {
    _assert(b, null, null);
  }

  /**
   * Verify a runtime assertion (die w/message and traceback if
   * assertion fails).<p>
   *
   * Note: code your assertion checks as
   * {@code if (VM.VerifyAssertions) VM._assert(xxx);}
   *
   * @param b the assertion to verify
   * @param message the message to print if the assertion is false
   */
  @Inline(value = Inline.When.ArgumentsAreConstant, arguments = {0})
  public static void _assert(boolean b, String message) {
    _assert(b, message, null);
  }

  @Inline(value = Inline.When.ArgumentsAreConstant, arguments = {0})
  public static void _assert(boolean b, String msg1, String msg2) {
    if (!VM.VerifyAssertions) {
      sysWriteln("vm: somebody forgot to conditionalize their call to assert with");
      sysWriteln("vm: if (VM.VerifyAssertions)");
      _assertionFailure("vm internal error: assert called when !VM.VerifyAssertions", null);
    }
    if (!b) _assertionFailure(msg1, msg2);
  }

  @NoInline
  @UninterruptibleNoWarn("Interruptible code not reachable at runtime")
  private static void _assertionFailure(String msg1, String msg2) {
    if (msg1 == null && msg2 == null) {
      msg1 = "vm internal error at:";
    }
    if (msg2 == null) {
      msg2 = msg1;
      msg1 = null;
    }
    if (VM.runningVM) {
      if (msg1 != null) {
        sysWrite(msg1);
      }
      sysFail(msg2);
    }
    throw new RuntimeException((msg1 != null ? msg1 : "") + msg2);
  }

  @SuppressWarnings({"unused", "CanBeFinal", "UnusedDeclaration"})
  // accessed via EntryPoints
  @Entrypoint
  private static int sysWriteLock = 0;
  private static Offset sysWriteLockOffset = Offset.max();

  private static void swLock() {
//    if (!VM.runningVM && !VM.writingBootImage) return;
//    if (sysWriteLockOffset.isMax()) return;
//    while (!Synchronization.testAndSet(Magic.getJTOC(), sysWriteLockOffset, 1)) {
//      ;
//    }
  }

  private static void swUnlock() {
//    if (!VM.runningVM && !VM.writingBootImage) return;
//    if (sysWriteLockOffset.isMax()) return;
//    Synchronization.fetchAndStore(Magic.getJTOC(), sysWriteLockOffset, 0);
  }

  /**
   * Low level print to console.
   * @param value  what is printed
   */
  @NoInline
  /* don't waste code space inlining these --dave */
  private static void write(Atom value) {
    value.sysWrite();
  }

  /**
   * Low level print to console.
   * @param value  what is printed
   */
  @NoInline
  /* don't waste code space inlining these --dave */
  public static void write(RVMMember value) {
    write(value.getMemberRef());
  }

  /**
   * Low level print to console.
   * @param value  what is printed
   */
  @NoInline
  /* don't waste code space inlining these --dave */
  public static void write(MemberReference value) {
    write(value.getType().getName());
    write(".");
    write(value.getName());
    write(" ");
    write(value.getDescriptor());
  }

  /**
   * Low level print to console.
   * @param value   what is printed
   */
  @NoInline
  /* don't waste code space inlining these --dave */
  public static void write(String value) {
    if (value == null) {
      write("null");
    } else {
      if (runningVM) {
        char[] chars = java.lang.JikesRVMSupport.getBackingCharArray(value);
        int numChars = java.lang.JikesRVMSupport.getStringLength(value);
        int offset = java.lang.JikesRVMSupport.getStringOffset(value);
        for (int i = 0; i < numChars; i++) {
          write(chars[offset + i]);
        }
      } else {
        writeNotRunningVM(value);
      }
    }
  }
  @UninterruptibleNoWarn("Interruptible code not reachable at runtime")
  private static void writeNotRunningVM(String value) {
    if (VM.VerifyAssertions) VM._assert(!VM.runningVM);
    System.err.print(value);
  }

  /**
   * Low level print to console.
   * @param value character array that is printed
   * @param len number of characters printed
   */
  @NoInline
  /* don't waste code space inlining these --dave */
  public static void write(char[] value, int len) {
    for (int i = 0, n = len; i < n; ++i) {
      if (runningVM) {
        //  Avoid triggering a potential read barrier
        write(Services.getArrayNoBarrier(value, i));
      } else {
        write(value[i]);
      }
    }
  }

  /**
   * Low level print of a <code>char</code>to console.
   * @param value       The character to print
   */
  @NoInline
  /* don't waste code space inlining these --dave */
  public static void write(char value) {
    if (runningVM) {
      sysCall.sysConsoleWriteChar(value);
    } else {
      writeNotRunningVM(value);
    }
  }
  @UninterruptibleNoWarn("Interruptible code not reachable at runtime")
  private static void writeNotRunningVM(char value) {
    if (VM.VerifyAssertions) VM._assert(!VM.runningVM);
    System.err.print(value);
  }

  /**
   * Low level print of <code>double</code> to console.
   *
   * @param value               <code>double</code> to be printed
   * @param postDecimalDigits   Number of decimal places
   */
  @NoInline
  /* don't waste code space inlining these --dave */
  public static void write(double value, int postDecimalDigits) {
    if (runningVM) {
      sysCall.sysConsoleWriteDouble(value, postDecimalDigits);
    } else {
      writeNotRunningVM(value);
    }
  }
  @UninterruptibleNoWarn("Interruptible code not reachable at runtime")
  private static void writeNotRunningVM(double value) {
    if (VM.VerifyAssertions) VM._assert(!VM.runningVM);
    System.err.print(value);
  }

  /**
   * Low level print of an <code>int</code> to console.
   * @param value       what is printed
   */
  @NoInline
  /* don't waste code space inlining these --dave */
  public static void write(int value) {
    if (runningVM) {
      int mode = (value < -(1 << 20) || value > (1 << 20)) ? 2 : 0; // hex only or decimal only
      sysCall.sysConsoleWriteInteger(value, mode);
    } else {
      writeNotRunningVM(value);
    }
  }
  @UninterruptibleNoWarn("Interruptible code not reachable at runtime")
  private static void writeNotRunningVM(int value) {
    if (VM.VerifyAssertions) VM._assert(!VM.runningVM);
    System.err.print(value);
  }

  /**
   * Low level print to console.
   * @param value       What is printed, as hex only
   */
  @NoInline
  /* don't waste code space inlining these --dave */
  public static void writeHex(int value) {
    if (runningVM) {
      sysCall.sysConsoleWriteInteger(value, 2 /*just hex*/);
    } else {
      writeHexNotRunningVM(value);
    }
  }
  @UninterruptibleNoWarn("Interruptible code not reachable at runtime")
  private static void writeHexNotRunningVM(int value) {
    if (VM.VerifyAssertions) VM._assert(!VM.runningVM);
    System.err.print(Integer.toHexString(value));
  }

  /**
   * Low level print to console.
   * @param value       what is printed, as hex only
   */
  @NoInline
  /* don't waste code space inlining these --dave */
  public static void writeHex(long value) {
    if (runningVM) {
      sysCall.sysConsoleWriteLong(value, 2);
    } else {
      writeHexNotRunningVM(value);
    }
  }
  @UninterruptibleNoWarn("Interruptible code not reachable at runtime")
  private static void writeHexNotRunningVM(long value) {
    if (VM.VerifyAssertions) VM._assert(!VM.runningVM);
    System.err.print(Long.toHexString(value));
  }

  @NoInline
  /* don't waste code space inlining these --dave */
  public static void writeDec(Word value) {
    if (VM.BuildFor32Addr) {
      write(value.toInt());
    } else {
      write(value.toLong());
    }
  }

  @NoInline
  /* don't waste code space inlining these --dave */
  public static void writeHex(Word value) {
    if (VM.BuildFor32Addr) {
      writeHex(value.toInt());
    } else {
      writeHex(value.toLong());
    }
  }

  @NoInline
  /* don't waste code space inlining these --dave */
  public static void writeHex(Address value) {
    writeHex(value.toWord());
  }

  @NoInline
  /* don't waste code space inlining these --dave */
  public static void writeHex(ObjectReference value) {
    writeHex(value.toAddress().toWord());
  }

  @NoInline
  /* don't waste code space inlining these --dave */
  public static void writeHex(Extent value) {
    writeHex(value.toWord());
  }

  @NoInline
  /* don't waste code space inlining these --dave */
  public static void writeHex(Offset value) {
    writeHex(value.toWord());
  }

  /**
   * Low level print to console.
   * @param value       what is printed, as int only
   */
  @NoInline
  /* don't waste code space inlining these --dave */
  public static void writeInt(int value) {
    if (runningVM) {
      sysCall.sysConsoleWriteInteger(value, 0 /*just decimal*/);
    } else {
      writeNotRunningVM(value);
    }
  }
  @UninterruptibleNoWarn("Interruptible code not reachable at runtime")
  private static void writeNotRunningVM(long value) {
    if (VM.VerifyAssertions) VM._assert(!VM.runningVM);
    System.err.print(value);
  }

  /**
   * Low level print to console.
   * @param value   what is printed
   */
  @NoInline
  /* don't waste code space inlining these --dave */
  public static void write(long value) {
    write(value, true);
  }

  /**
   * Low level print to console.
   * @param value   what is printed
   * @param hexToo  how to print: true  - print as decimal followed by hex
   *                              false - print as decimal only
   */
  @NoInline
  /* don't waste code space inlining these --dave */
  public static void write(long value, boolean hexToo) {
    if (runningVM) {
      sysCall.sysConsoleWriteLong(value, hexToo ? 1 : 0);
    } else {
      writeNotRunningVM(value);
    }
  }

  @NoInline
  /* don't waste code space inlining these --dave */
  public static void writeField(int fieldWidth, String s) {
    write(s);
    int len = getStringLength(s);
    while (fieldWidth > len++) write(" ");
  }

  @UninterruptibleNoWarn("Interruptible code not reachable at runtime")
  private static int getStringLength(String s) {
    if (VM.runningVM) {
      return java.lang.JikesRVMSupport.getStringLength(s);
    } else {
      return s.length();
    }
  }
  /**
   * Low level print to console.
   * @param value print value
   * @param fieldWidth the number of characters that the output should contain. If the value
   *  is too small, the output will be filled up with enough spaces, starting from the left
   */
  @NoInline
  /* don't waste code space inlining these --dave */
  public static void writeField(int fieldWidth, int value) {
    int len = 1, temp = value;
    if (temp < 0) {
      len++;
      temp = -temp;
    }
    while (temp >= 10) {
      len++;
      temp /= 10;
    }
    while (fieldWidth > len++) write(" ");
    if (runningVM) {
      sysCall.sysConsoleWriteInteger(value, 0);
    } else {
      writeNotRunningVM(value);
    }
  }

  /**
   * Low level print of the {@link Atom} <code>s</code> to the console.
   * Left-fill with enough spaces to print at least <code>fieldWidth</code>
   * characters
   * @param fieldWidth  Minimum width to print.
   * @param s       The {@link Atom} to print.
   */
  @NoInline
  /* don't waste code space inlining these --dave */
  public static void writeField(int fieldWidth, Atom s) {
    int len = s.length();
    while (fieldWidth > len++) write(" ");
    write(s);
  }

  public static void writeln() {
    write('\n');
  }

  public static void write(double d) {
    write(d, 2);
  }

  public static void write(Word addr) {
    writeHex(addr);
  }

  public static void write(Address addr) {
    writeHex(addr);
  }

  public static void write(ObjectReference object) {
    writeHex(object);
  }

  public static void write(Offset addr) {
    writeHex(addr);
  }

  public static void write(Extent addr) {
    writeHex(addr);
  }

  public static void write(boolean b) {
    write(b ? "true" : "false");
  }

  /*
   * A group of multi-argument sysWrites with optional newline.  Externally visible methods.
   */

  @NoInline
  public static void sysWrite(Atom a) {
    swLock();
    write(a);
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(Atom a) {
    swLock();
    write(a);
    write("\n");
    swUnlock();
  }

  @NoInline
  public static void sysWrite(RVMMember m) {
    swLock();
    write(m);
    swUnlock();
  }

  @NoInline
  public static void sysWrite(MemberReference mr) {
    swLock();
    write(mr);
    swUnlock();
  }

  @NoInline
  public static void sysWriteln() {
    swLock();
    write("\n");
    swUnlock();
  }

  @NoInline
  public static void sysWrite(char c) {
    write(c);
  }

  @NoInline
  public static void sysWriteField(int w, int v) {
    swLock();
    writeField(w, v);
    swUnlock();
  }

  @NoInline
  public static void sysWriteField(int w, String s) {
    swLock();
    writeField(w, s);
    swUnlock();
  }

  @NoInline
  public static void sysWriteHex(int v) {
    swLock();
    writeHex(v);
    swUnlock();
  }

  @NoInline
  public static void sysWriteHex(long v) {
    swLock();
    writeHex(v);
    swUnlock();
  }

  @NoInline
  public static void sysWriteHex(Address v) {
    swLock();
    writeHex(v);
    swUnlock();
  }

  @NoInline
  public static void sysWriteInt(int v) {
    swLock();
    writeInt(v);
    swUnlock();
  }

  @NoInline
  public static void sysWriteLong(long v) {
    swLock();
    write(v, false);
    swUnlock();
  }

  @NoInline
  public static void sysWrite(double d, int p) {
    swLock();
    write(d, p);
    swUnlock();
  }

  @NoInline
  public static void sysWrite(double d) {
    swLock();
    write(d);
    swUnlock();
  }

  @NoInline
  public static void sysWrite(String s) {
    swLock();
    write(s);
    swUnlock();
  }

  @NoInline
  public static void sysWrite(char[] c, int l) {
    swLock();
    write(c, l);
    swUnlock();
  }

  @NoInline
  public static void sysWrite(Address a) {
    swLock();
    write(a);
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(Address a) {
    swLock();
    write(a);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWrite(ObjectReference o) {
    swLock();
    write(o);
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(ObjectReference o) {
    swLock();
    write(o);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWrite(Offset o) {
    swLock();
    write(o);
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(Offset o) {
    swLock();
    write(o);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWrite(Word w) {
    swLock();
    write(w);
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(Word w) {
    swLock();
    write(w);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWrite(Extent e) {
    swLock();
    write(e);
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(Extent e) {
    swLock();
    write(e);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWrite(boolean b) {
    swLock();
    write(b);
    swUnlock();
  }

  @NoInline
  public static void sysWrite(int i) {
    swLock();
    write(i);
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(int i) {
    swLock();
    write(i);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(double d) {
    swLock();
    write(d);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(long l) {
    swLock();
    write(l);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(boolean b) {
    swLock();
    write(b);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(String s) {
    swLock();
    write(s);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(String s, Atom a) {
    swLock();
    write(s);
    write(a);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWrite(String s, int i) {
    swLock();
    write(s);
    write(i);
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(String s, int i) {
    swLock();
    write(s);
    write(i);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWrite(String s, boolean b) {
    swLock();
    write(s);
    write(b);
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(String s, boolean b) {
    swLock();
    write(s);
    write(b);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWrite(String s, double d) {
    swLock();
    write(s);
    write(d);
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(String s, double d) {
    swLock();
    write(s);
    write(d);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWrite(double d, String s) {
    swLock();
    write(d);
    write(s);
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(double d, String s) {
    swLock();
    write(d);
    write(s);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWrite(String s, long i) {
    swLock();
    write(s);
    write(i);
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(String s, long i) {
    swLock();
    write(s);
    write(i);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(String s1, long i1,String s2, long i2) {
    swLock();
    write(s1);
    write(i1);
    write(s2);
    write(i2);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWrite(int i, String s) {
    swLock();
    write(i);
    write(s);
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(int i, String s) {
    swLock();
    write(i);
    write(s);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWrite(String s1, String s2) {
    swLock();
    write(s1);
    write(s2);
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(String s1, String s2) {
    swLock();
    write(s1);
    write(s2);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWrite(String s, Address a) {
    swLock();
    write(s);
    write(a);
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(String s, Address a) {
    swLock();
    write(s);
    write(a);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWrite(String s, ObjectReference r) {
    swLock();
    write(s);
    write(r);
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(String s, ObjectReference r) {
    swLock();
    write(s);
    write(r);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWrite(String s, Offset o) {
    swLock();
    write(s);
    write(o);
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(String s, Offset o) {
    swLock();
    write(s);
    write(o);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWrite(String s, Word w) {
    swLock();
    write(s);
    write(w);
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(String s, Word w) {
    swLock();
    write(s);
    write(w);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWrite(String s1, String s2, Address a) {
    swLock();
    write(s1);
    write(s2);
    write(a);
    swUnlock();
  }

  @NoInline
  public static void sysWrite(String s1, Address a, String s2) {
    swLock();
    write(s1);
    write(a);
    write(s2);
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(String s1, String s2, Address a) {
    swLock();
    write(s1);
    write(s2);
    write(a);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(String s1, Address a,String s2) {
    swLock();
    write(s1);
    write(a);
    write(s2);
    writeln();
    swUnlock();
  }
  @NoInline
  public static void sysWriteln(String s1, Address a1,Address a2) {
    swLock();
    write(s1);
    write(a1);
    write(" ");
    write(a2);
    writeln();
    swUnlock();
  }
  @NoInline
  public static void sysWrite(String s1, String s2, int i) {
    swLock();
    write(s1);
    write(s2);
    write(i);
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(int i, Address a, RVMMethod m) {
    swLock();
    write(i);
    write(" ");
    write(a);
    write(" ");
    write(m.getDeclaringClass().getDescriptor());
    write(".");
    write(m.getName());
    write(m.getDescriptor());
    write("\n");
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(int i, Address a, Address b) {
    swLock();
    write(i);
    write(" ");
    write(a);
    write(" ");
    write(b);
    write("\n");
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(String s1, String s2, int i) {
    swLock();
    write(s1);
    write(s2);
    write(i);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWrite(String s1, int i, String s2) {
    swLock();
    write(s1);
    write(i);
    write(s2);
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(String s1, int i, String s2) {
    swLock();
    write(s1);
    write(i);
    write(s2);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWrite(String s1, Offset o, String s2) {
    swLock();
    write(s1);
    write(o);
    write(s2);
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(String s1, Offset o, String s2) {
    swLock();
    write(s1);
    write(o);
    write(s2);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWrite(String s1, String s2, String s3) {
    swLock();
    write(s1);
    write(s2);
    write(s3);
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(String s1, String s2, String s3) {
    swLock();
    write(s1);
    write(s2);
    write(s3);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(String s1, String s2, String s3, Address a) {
    swLock();
    write(s1);
    write(s2);
    write(s3);
    write(a);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWrite(int i1, String s, int i2) {
    swLock();
    write(i1);
    write(s);
    write(i2);
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(int i1, String s, int i2) {
    swLock();
    write(i1);
    write(s);
    write(i2);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWrite(int i1, String s1, String s2) {
    swLock();
    write(i1);
    write(s1);
    write(s2);
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(int i1, String s1, String s2) {
    swLock();
    write(i1);
    write(s1);
    write(s2);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(String s1, int i1, String s2, String s3) {
    swLock();
    write(s1);
    write(i1);
    write(s2);
    write(s3);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWrite(String s1, String s2, String s3, String s4) {
    swLock();
    write(s1);
    write(s2);
    write(s3);
    write(s4);
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(String s1, String s2, String s3, String s4) {
    swLock();
    write(s1);
    write(s2);
    write(s3);
    write(s4);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWrite(String s1, String s2, String s3, String s4, String s5) {
    swLock();
    write(s1);
    write(s2);
    write(s3);
    write(s4);
    write(s5);
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(String s1, String s2, String s3, String s4, String s5) {
    swLock();
    write(s1);
    write(s2);
    write(s3);
    write(s4);
    write(s5);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(String s1, int i, String s3, Address a, String s5) {
    swLock();
    write(s1);
    write(i);
    write(s3);
    write(a);
    write(s5);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(int i, String s, Address a) {
    swLock();
    write(i);
    write(s);
    write(a);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWrite(String s1, int i1, String s2, int i2) {
    swLock();
    write(s1);
    write(i1);
    write(s2);
    write(i2);
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(String s1, int i1, String s2, int i2) {
    swLock();
    write(s1);
    write(i1);
    write(s2);
    write(i2);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(String s1, int i, String s2, Address a) {
    swLock();
    write(s1);
    write(i);
    write(s2);
    write(a);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(String s1, int i, String s2, Word w) {
    swLock();
    write(s1);
    write(i);
    write(s2);
    write(w);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(String s1, int i, String s2, double d) {
    swLock();
    write(s1);
    write(i);
    write(s2);
    write(d);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(String s1, int i, String s2, Word w, String s3) {
    swLock();
    write(s1);
    write(i);
    write(s2);
    write(w);
    write(s3);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(String s1, int i1, String s2, int i2, String s3) {
    swLock();
    write(s1);
    write(i1);
    write(s2);
    write(i2);
    write(s3);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(String s1, int i1, String s2, int i2, String s3, int i3) {
    swLock();
    write(s1);
    write(i1);
    write(s2);
    write(i2);
    write(s3);
    write(i3);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWrite(String s1, int i1, String s2, long l1) {
    swLock();
    write(s1);
    write(i1);
    write(s2);
    write(l1);
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(String s1, int i1, String s2, long l1) {
    swLock();
    write(s1);
    write(i1);
    write(s2);
    write(l1);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWrite(String s1, Offset o, String s2, int i) {
    swLock();
    write(s1);
    write(o);
    write(s2);
    write(i);
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(String s1, Offset o, String s2, int i) {
    swLock();
    write(s1);
    write(o);
    write(s2);
    write(i);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWrite(String s1, double d, String s2) {
    swLock();
    write(s1);
    write(d);
    write(s2);
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(String s1, double d, String s2) {
    swLock();
    write(s1);
    write(d);
    write(s2);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(String s1, long l, String s2) {
    swLock();
    write(s1);
    write(l);
    write(s2);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(String s1, long l1, String s2, long l2, String s3) {
    swLock();
    write(s1);
    write(l1);
    write(s2);
    write(l2);
    write(s3);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWrite(String s1, String s2, int i1, String s3) {
    swLock();
    write(s1);
    write(s2);
    write(i1);
    write(s3);
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(String s1, String s2, int i1, String s3) {
    swLock();
    write(s1);
    write(s2);
    write(i1);
    write(s3);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWrite(String s1, String s2, String s3, int i1) {
    swLock();
    write(s1);
    write(s2);
    write(s3);
    write(i1);
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(String s1, String s2, String s3, int i1) {
    swLock();
    write(s1);
    write(s2);
    write(s3);
    write(i1);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWrite(String s1, String s2, String s3, String s4, int i5, String s6) {
    swLock();
    write(s1);
    write(s2);
    write(s3);
    write(s4);
    write(i5);
    write(s6);
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(String s1, String s2, String s3, String s4, int i5, String s6) {
    swLock();
    write(s1);
    write(s2);
    write(s3);
    write(s4);
    write(i5);
    write(s6);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWrite(int i, String s1, double d, String s2) {
    swLock();
    write(i);
    write(s1);
    write(d);
    write(s2);
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(int i, String s1, double d, String s2) {
    swLock();
    write(i);
    write(s1);
    write(d);
    write(s2);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWrite(String s1, String s2, String s3, int i1, String s4) {
    swLock();
    write(s1);
    write(s2);
    write(s3);
    write(i1);
    write(s4);
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(String s1, String s2, String s3, int i1, String s4) {
    swLock();
    write(s1);
    write(s2);
    write(s3);
    write(i1);
    write(s4);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWrite(String s1, Address a1, String s2, Address a2) {
    swLock();
    write(s1);
    write(a1);
    write(s2);
    write(a2);
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(String s1, Address a1, String s2, Address a2) {
    swLock();
    write(s1);
    write(a1);
    write(s2);
    write(a2);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWrite(String s1, Address a, String s2, int i) {
    swLock();
    write(s1);
    write(a);
    write(s2);
    write(i);
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(String s1, Address a, String s2, int i) {
    swLock();
    write(s1);
    write(a);
    write(s2);
    write(i);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void sysWriteln(String s0, Address a1, String s1, Word w1, String s2, int i1, String s3, int i2, String s4, Word w2, String s5, int i3) {
    swLock();
    write(s0);
    write(a1);
    write(s1);
    write(w1);
    write(s2);
    write(i1);
    write(s3);
    write(i2);
    write(s4);
    write(w2);
    write(s5);
    write(i3);
    writeln();
    swUnlock();
  }

  private static void showThread() {
    write("Thread ");
    write(RVMThread.getCurrentThread().getThreadSlot());
    write(": ");
  }

  @NoInline
  public static void tsysWriteln(String s) {
    swLock();
    showThread();
    write(s);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void tsysWriteln(String s1, String s2, String s3, int i4, String s5, String s6) {
    swLock();
    showThread();
    write(s1);
    write(s2);
    write(s3);
    write(i4);
    write(s5);
    write(s6);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void tsysWriteln(String s1, String s2, String s3, String s4, String s5, String s6, String s7, int i8,
                                  String s9, String s10, String s11, String s12, String s13) {
    swLock();
    showThread();
    write(s1);
    write(s2);
    write(s3);
    write(s4);
    write(s5);
    write(s6);
    write(s7);
    write(i8);
    write(s9);
    write(s10);
    write(s11);
    write(s12);
    write(s13);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void tsysWriteln(String s1, String s2, String s3, String s4, String s5, String s6, String s7, int i8,
                                  String s9, String s10, String s11, String s12, String s13, int i14) {
    swLock();
    showThread();
    write(s1);
    write(s2);
    write(s3);
    write(s4);
    write(s5);
    write(s6);
    write(s7);
    write(i8);
    write(s9);
    write(s10);
    write(s11);
    write(s12);
    write(s13);
    write(i14);
    writeln();
    swUnlock();
  }
  @NoInline
  public static void tsysWrite(char[] c, int l) {
    swLock();
    showThread();
    write(c, l);
    swUnlock();
  }

  @NoInline
  public static void tsysWriteln(Address a) {
    swLock();
    showThread();
    write(a);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void tsysWriteln(String s, int i) {
    swLock();
    showThread();
    write(s);
    write(i);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void tsysWriteln(String s, Address a) {
    swLock();
    showThread();
    write(s);
    write(a);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void tsysWriteln(String s1, Address a1, String s2, Address a2) {
    swLock();
    showThread();
    write(s1);
    write(a1);
    write(s2);
    write(a2);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void tsysWriteln(String s1, Address a1, String s2, Address a2, String s3, Address a3) {
    swLock();
    showThread();
    write(s1);
    write(a1);
    write(s2);
    write(a2);
    write(s3);
    write(a3);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void tsysWriteln(String s1, Address a1, String s2, Address a2, String s3, Address a3, String s4,
                                 Address a4) {
    swLock();
    showThread();
    write(s1);
    write(a1);
    write(s2);
    write(a2);
    write(s3);
    write(a3);
    write(s4);
    write(a4);
    writeln();
    swUnlock();
  }

  @NoInline
  public static void tsysWriteln(String s1, Address a1, String s2, Address a2, String s3, Address a3, String s4,
                                 Address a4, String s5, Address a5) {
    swLock();
    showThread();
    write(s1);
    write(a1);
    write(s2);
    write(a2);
    write(s3);
    write(a3);
    write(s4);
    write(a4);
    write(s5);
    write(a5);
    writeln();
    swUnlock();
  }

  /**
   * Produce a message requesting a bug report be submitted
   */
  @NoInline
  public static void bugReportMessage() {
    VM.sysWriteln("********************************************************************************");
    VM.sysWriteln("*                      Abnormal termination of Jikes RVM                       *\n" +
                  "* Jikes RVM terminated abnormally indicating a problem in the virtual machine. *\n" +
                  "* Jikes RVM relies on community support to get debug information. Help improve *\n" +
                  "* Jikes RVM for everybody by reporting this error. Please see:                 *\n" +
                  "*                    http://www.jikesrvm.org/ReportingBugs/                    *");
    VM.sysWriteln("********************************************************************************");
  }

  /**
   * Exit virtual machine due to internal failure of some sort.
   * @param message  error message describing the problem
   */
  @NoInline
  public static void sysFail(String message) {
    handlePossibleRecursiveCallToSysFail(message);

    // print a traceback and die
    if (!RVMThread.getCurrentThread().isCollectorThread()) {
      RVMThread.traceback(message);
    } else {
      VM.sysWriteln("Died in GC:");
      RVMThread.traceback(message);
      VM.sysWriteln("Virtual machine state:");
      RVMThread.dumpVirtualMachine();
    }
    bugReportMessage();
    if (VM.runningVM) {
      sysCall.sysConsoleFlushErrorAndTrace();
      VM.shutdown(EXIT_STATUS_SYSFAIL);
    } else {
      VM.sysExit(EXIT_STATUS_SYSFAIL);
    }
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);
  }

  /**
   * Exit virtual machine due to internal failure of some sort.  This
   * two-argument form is  needed for us to call before the VM's Integer class
   * is initialized.
   *
   * @param message  error message describing the problem
   * @param number  an integer to append to <code>message</code>.
   */
  @NoInline
  public static void sysFail(String message, int number) {
    handlePossibleRecursiveCallToSysFail(message, number);

    // print a traceback and die
    RVMThread.traceback(message, number);
    bugReportMessage();
    if (VM.runningVM) {
      sysCall.sysConsoleFlushErrorAndTrace();
      VM.shutdown(EXIT_STATUS_SYSFAIL);
    } else {
      VM.sysExit(EXIT_STATUS_SYSFAIL);
    }
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);
  }

  /**
   * Exit virtual machine.
   * @param value  value to pass to host o/s
   */
  @NoInline
  @UninterruptibleNoWarn("We're never returning to the caller, so even though this code is preemptible it is safe to call from any context")
  public static void sysExit(int value) {
    handlePossibleRecursiveCallToSysExit();

    if (VM.countThreadTransitions) {
      RVMThread.reportThreadTransitionCounts();
    }

    if (Options.stackTraceAtExit) {
      VM.sysWriteln("[Here is the context of the call to VM.sysExit(", value, ")...:");
      VM.disableGC();
      RVMThread.dumpStack();
      VM.enableGC();
      VM.sysWriteln("... END context of the call to VM.sysExit]");
    }
    if (runningVM) {
      Callbacks.notifyExit(value);
      VM.shutdown(value);
    } else {
      System.exit(value);
    }
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);
  }

  /**
   * Shut down the virtual machine.
   * Should only be called if the VM is running.
   * @param value  exit value
   */
  @Uninterruptible
  public static void shutdown(int value) {
    handlePossibleRecursiveShutdown();

    if (VM.VerifyAssertions) VM._assert(VM.runningVM);
    sysCall.sysExit(value);
    if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);
  }

  private static int inSysFail = 0;

  public static boolean sysFailInProgress() {
    return inSysFail > 0;
  }

  private static void handlePossibleRecursiveCallToSysFail(String message) {
    handlePossibleRecursiveExit("sysFail", ++inSysFail, message);
  }

  private static void handlePossibleRecursiveCallToSysFail(String message, int number) {
    handlePossibleRecursiveExit("sysFail", ++inSysFail, message, number);
  }

  private static int inSysExit = 0;

  private static void handlePossibleRecursiveCallToSysExit() {
    handlePossibleRecursiveExit("sysExit", ++inSysExit);
  }

  private static int inShutdown = 0;

  /** Used only by VM.shutdown() */
  private static void handlePossibleRecursiveShutdown() {
    handlePossibleRecursiveExit("shutdown", ++inShutdown);
  }

  private static void handlePossibleRecursiveExit(String called, int depth) {
    handlePossibleRecursiveExit(called, depth, null);
  }

  private static void handlePossibleRecursiveExit(String called, int depth, String message) {
    handlePossibleRecursiveExit(called, depth, message, false, -9999999);
  }

  private static void handlePossibleRecursiveExit(String called, int depth, String message, int number) {
    handlePossibleRecursiveExit(called, depth, message, true, number);
  }

  /** @param called Name of the function called: "sysExit", "sysFail", or
   *    "shutdown".
   * @param depth How deep are we in that function?
   * @param message What message did it have?  null means this particular
   *    shutdown function  does not come with a message.
   * @param showNumber Print <code>number</code> following
   *    <code>message</code>?
   * @param number Print this number, if <code>showNumber</code> is {@code true}. */
  private static void handlePossibleRecursiveExit(String called, int depth, String message, boolean showNumber,
                                                  int number) {
    if (depth > 1 &&
        (depth <=
         maxSystemTroubleRecursionDepth + VM.maxSystemTroubleRecursionDepthBeforeWeStopVMSysWrite)) {
      if (showNumber) {
        tsysWriteln("VM.",
                     called,
                     "(): We're in a",
                     " (likely)",
                     " recursive call to VM.",
                     called,
                     "(), ",
                     depth,
                     " deep\n",
                     message == null ? "" : "   ",
                     message == null ? "" : called,
                     message == null ? "" : " was called with the message: ",
                     message == null ? "" : message,
                     number);
      } else {
        tsysWriteln("VM.",
                     called,
                     "(): We're in a",
                     " (likely)",
                     " recursive call to VM.",
                     called,
                     "(), ",
                     depth,
                     " deep\n",
                     message == null ? "" : "   ",
                     message == null ? "" : called,
                     message == null ? "" : " was called with the message: ",
                     message == null ? "" : message);
      }
    }
    if (depth > maxSystemTroubleRecursionDepth) {
      dieAbruptlyRecursiveSystemTrouble();
      if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED);
    }
  }

  /** Have we already called dieAbruptlyRecursiveSystemTrouble()?
   Only for use if we're recursively shutting down!  Used by
   dieAbruptlyRecursiveSystemTrouble() only.  */

  private static boolean inDieAbruptlyRecursiveSystemTrouble = false;

  public static void dieAbruptlyRecursiveSystemTrouble() {
    if (!inDieAbruptlyRecursiveSystemTrouble) {
      inDieAbruptlyRecursiveSystemTrouble = true;
      sysWriteln("VM.dieAbruptlyRecursiveSystemTrouble(): Dying abruptly",
                 "; we're stuck in a recursive shutdown/exit.");
    }
    /* Emergency death. */
    sysCall.sysExit(EXIT_STATUS_RECURSIVELY_SHUTTING_DOWN);
    /* And if THAT fails, go into an infinite loop.  Ugly, but it's better than
       returning from this function and leading to yet more cascading errors.
       and misleading error messages.   (To the best of my knowledge, we have
       never yet reached this point.)  */
    while (true) {
      ;
    }
  }

  //----------------//
  // implementation //
  //----------------//

  /**
   * Create class instances needed for boot image or initialize classes
   * needed by tools.
   * @param bootstrapClasspath places where VM implementation class reside
   * @param bootCompilerArgs command line arguments to pass along to the
   *                         boot compiler's init routine.
   */
  @Interruptible
  private static void init(String bootstrapClasspath, String[] bootCompilerArgs) {
    if (VM.VerifyAssertions) VM._assert(!VM.runningVM);

    // create dummy boot record
    //
    BootRecord.the_boot_record = new BootRecord();

    // initialize type subsystem and classloader
    RVMClassLoader.init(bootstrapClasspath);

    // initialize remaining subsystems needed for compilation
    //
    if (writingBootImage) {
      // initialize compiler that builds boot image
      BootImageCompiler.init(bootCompilerArgs);
    }
    RuntimeEntrypoints.init();
    RVMThread.init();
  }

  public static void disableYieldpoints() {
    RVMThread.getCurrentThread().disableYieldpoints();
  }

  public static void enableYieldpoints() {
    RVMThread.getCurrentThread().enableYieldpoints();
  }

  /**
   * The disableGC() and enableGC() methods are for use as guards to protect
   * code that must deal with raw object addresses in a collection-safe manner
   * (i.e. code that holds raw pointers across "gc-sites").<p>
   *
   * Authors of code running while GC is disabled must be certain not to
   * allocate objects explicitly via "new", or implicitly via methods that,
   * in turn, call "new" (such as string concatenation expressions that are
   * translated by the java compiler into String() and StringBuffer()
   * operations). Furthermore, to prevent deadlocks, code running with GC
   * disabled must not lock any objects. This means the code must not execute
   * any bytecodes that require runtime support (e.g. via RuntimeEntrypoints)
   * such as:
   * <ul>
   *   <li>calling methods or accessing fields of classes that haven't yet
   *     been loaded/resolved/instantiated
   *   <li>calling synchronized methods
   *   <li>entering synchronized blocks
   *   <li>allocating objects with "new"
   *   <li>throwing exceptions
   *   <li>executing trap instructions (including stack-growing traps)
   *   <li>storing into object arrays, except when runtime types of lhs &amp; rhs
   *     match exactly
   *   <li>typecasting objects, except when runtime types of lhs &amp; rhs
   *     match exactly
   * </ul>
   *
   * <p>
   * Recommendation: as a debugging aid, Allocator implementations
   * should test "Thread.disallowAllocationsByThisThread" to verify that
   * they are never called while GC is disabled.
   */
  @Inline
  @Unpreemptible("We may boost the size of the stack with GC disabled and may get preempted doing this")
  public static void disableGC() {
    disableGC(false);           // Recursion is not allowed in this context.
  }

  /**
   * disableGC: Disable GC if it hasn't already been disabled.  This
   * enforces a stack discipline; we need it for the JNI Get*Critical and
   * Release*Critical functions.  Should be matched with a subsequent call to
   * enableGC().
   *
   * @param recursiveOK whether recursion is allowed.
   */
  @Inline
  @Unpreemptible("We may boost the size of the stack with GC disabled and may get preempted doing this")
  public static void disableGC(boolean recursiveOK) {
    // current (non-GC) thread is going to be holding raw addresses, therefore we must:
    //
    // 1. make sure we have enough stack space to run until GC is re-enabled
    //    (otherwise we might trigger a stack reallocation)
    //    (We can't resize the stack if there's a native frame, so don't
    //     do it and hope for the best)
    //
    // 2. force all other threads that need GC to wait until this thread
    //    is done with the raw addresses
    //
    // 3. ensure that this thread doesn't try to allocate any objects
    //    (because an allocation attempt might trigger a collection that
    //    would invalidate the addresses we're holding)
    //

    RVMThread myThread = RVMThread.getCurrentThread();

    // 0. Sanity Check; recursion
    int gcDepth = myThread.getDisableGCDepth();
    if (VM.VerifyAssertions) VM._assert(gcDepth >= 0);
    gcDepth++;
    myThread.setDisableGCDepth(gcDepth);
    if (gcDepth > 1) {
      return;                   // We've already disabled it.
    }

    // 1.
    //
    if (Magic.getFramePointer().minus(StackFrameLayout.getStackSizeGCDisabled())
        .LT(myThread.stackLimit) && !myThread.hasNativeStackFrame()) {
      RVMThread.resizeCurrentStack(myThread.getStackLength() +
          StackFrameLayout.getStackSizeGCDisabled(), null);
    }

    // 2.
    //
    myThread.disableYieldpoints();

    // 3.
    //
    if (VM.VerifyAssertions) {
      if (!recursiveOK) {
        VM._assert(!myThread.getDisallowAllocationsByThisThread()); // recursion not allowed
      }
      myThread.setDisallowAllocationsByThisThread();
    }
  }

  /**
   * enable GC; entry point when recursion is not OK.
   */
  @Inline
  public static void enableGC() {
    enableGC(false);            // recursion not OK.
  }

  /**
   * enableGC(): Re-Enable GC if we're popping off the last
   * possibly-recursive {@link #disableGC} request.  This enforces a stack discipline;
   * we need it for the JNI Get*Critical and Release*Critical functions.
   * Should be matched with a preceding call to {@link #disableGC}.
   *
   * @param recursiveOK unused (!)
   */
  @Inline
  public static void enableGC(boolean recursiveOK) {
    RVMThread myThread = RVMThread.getCurrentThread();
    int gcDepth = myThread.getDisableGCDepth();
    if (VM.VerifyAssertions) {
      VM._assert(gcDepth >= 1);
      VM._assert(myThread.getDisallowAllocationsByThisThread());
    }
    gcDepth--;
    myThread.setDisableGCDepth(gcDepth);
    if (gcDepth > 0) {
      return;
    }

    // Now the actual work of re-enabling GC.
    myThread.clearDisallowAllocationsByThisThread();
    myThread.enableYieldpoints();
  }

  /**
   * @return whether this is a build for 32bit addressing.
   * NB. this method is provided to give a hook to the IA32
   * assembler that won't be compiled away by javac.
   */
  public static boolean buildFor32Addr() {
    return BuildFor32Addr;
  }

  /**
   * @return whether this is a build for 64bit addressing.
   * NB. this method is provided to give a hook to the IA32
   * assembler that won't be compiled away by javac.
   */
  public static boolean buildFor64Addr() {
    return BuildFor64Addr;
  }
  
  public static void stackDump(Address addr, int elements)
  {
      VM.sysWrite(Long.toHexString(addr.loadLong()), ": ");
      for(int element=0; element < elements; element++)
      {
          if(element!=0 && (element%4) == 0)
          {
              VM.sysWriteln();
              VM.sysWrite(addr.toInt(), ": ");
              VM.sysWrite(Long.toHexString(addr.loadLong()), " ");
          }
          else
          {
              VM.sysWrite(Long.toHexString(addr.loadLong()), " ");
          }
          addr = addr.plus(8);
      }
      VM.sysWriteln();
  }
  public static void hexDump(byte data[])
  {
    hexDump(data, 0, data.length);
  }
  
  /**
   * @return whether this is a build for SSE2.
   * NB. this method is provided to give a hook to the IA32
   * assembler that won't be compiled away by javac.

   */
  public static void hexDump(byte data[], int offset, int size)
  {
    int i;
    
    for(i=offset; i < size; i++)
    {
      if(i!=0 && (i%16) == 0)
      {
        VM.sysWriteln();
        if(data[i] < 0x10 && data[i] >=0)
        {
          VM.sysWrite('0');
        }
        VM.sysWrite(Integer.toHexString(data[i]&0xff), " ");
      }
      else
      {
        if(data[i] < 0x10 && data[i] >=0)
        {
          VM.sysWrite('0');
        }
        VM.sysWrite(Integer.toHexString(data[i]&0xff), " ");
      }
    }
    VM.sysWriteln();
  }
  public static boolean buildForSSE2() {
    return BuildForSSE2;
  }

  /**
   * Format a 32 bit number as "0x" followed by 8 hex digits.
   * Do this without referencing Integer or Character classes,
   * in order to avoid dynamic linking.
   * TODO: move this method to Services.
   * @param number
   * @return a String with the hex representation of the integer
   */
  @Interruptible
  public static String intAsHexString(int number) {
    char[] buf = new char[10];
    int index = 10;
    while (--index > 1) {
      int digit = number & 0x0000000f;
      buf[index] = digit <= 9 ? (char) ('0' + digit) : (char) ('a' + digit - 10);
      number >>= 4;
    }
    buf[index--] = 'x';
    buf[index] = '0';
    return new String(buf);
  }

  /**
   * Format a 32/64 bit number as "0x" followed by 8/16 hex digits.
   * Do this without referencing Integer or Character classes,
   * in order to avoid dynamic linking.
   * TODO: move this method to Services.
   * @param addr  The 32/64 bit number to format.
   * @return a String with the hex representation of an Address
   */
  @Interruptible
  public static String addressAsHexString(Address addr) {
    int len = 2 + (BITS_IN_ADDRESS >> 2);
    char[] buf = new char[len];
    while (--len > 1) {
      int digit = addr.toInt() & 0x0F;
      buf[len] = digit <= 9 ? (char) ('0' + digit) : (char) ('a' + digit - 10);
      addr = addr.toWord().rshl(4).toAddress();
    }
    buf[len--] = 'x';
    buf[len] = '0';
    return new String(buf);
  }

  @NoInline
  public static void sysFailTrap(String message)
  {
      VM.sysWriteln(Magic.getFramePointer());
      RVMThread.trapTraceback(message);
      VM.shutdown(EXIT_STATUS_SYSFAIL);
  }
}

