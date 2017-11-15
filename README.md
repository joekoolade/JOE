# JOE

## Java On Everything

JOE is a novel virtualized Java environment tool that can virtualize Java programs and run them without an operating system or hypervisor. It does this by removing the JNI and operating system dependence from the Java API library. The operating system is replaced by Java objects for the operating system components like the scheduler, locks, synchronization, network interfaces, and hardware devices. The JNI library interface used by the Java API library is changed to use these system objects. This virtualized Java environment implements a complete and consistent object oriented view from application to hardware.

JOE is a more efficient virtualization tool. Current virtualization technology needs a hypervisor layer that  allows multiple operating systems to run. This requires a processor with hardware virtualization, more memory, more disk space and an multiple operating systems. The virtualized Java provided by JOE runs multiple applications on any processor in less memory, no disk space, and without the need of special processor virtualization extensions. JOE provides a consistent object system from application down to the hardware. JOE reduces the complexities introduced by using an operating system and allows the developer to concentrate on the essential complexity of the application.

The motivation for JOE is to make it easier to deploy and run an application on any computer. The virtualized Java environment is an attempt to use Java without an operating system by providing objects that can provide operating system services. Dan Ingalls said that "An operating system is a collections of things that don't fit into a language. There should not be one." Languages like Java, Smalltalk, C# contain all the high level objects and language constructs that provide threading, synchronization, and timers but used an operating system to provide those services. The JikesRVM has made it possible to implement the lower level services that where provided by an operating system to be implemented as Java objects.

## What are the advantages to this approach?

This will reduce the essential and accidental difficulties of application design. The elimination of the operating system  reduces an application's complexity because the operating system does not need to be considered in the design. The developer is free to design application specific abstractions and interfaces to meet the application's specification. The elimination of the operating system will elminate the accidental difficulty of configuring and maintaining it. Operating system configuration and maintenance can be a complex, time consuming and error prone process. This has the advantage of being a homogeneous environment in that only Java can be used. This is an advantage because it prevents a 'Tower of Babel' situation that can happen when using different programming languages and environments in an application's design. This is another accidental difficulty that is avoided.

Application security is increased. Java was designed with security in mind. Security is increased through the JVM, language type safety, garbage collection, and the security manager. There are no inherited operating system vulnerabilities to deal with. Everything in memory is an object and thus has an identity as determined by an Object table. Security is increased because of decreased system size. Smaller system size reduces the code surface area and ultimately the potential avenues of attack.

Application portability is automatic. This is more of a platform implementation issue because the ability to run your application relies on if the appropriate compilers and hardware objects are implemented to run that application on a specific hardware platform. That means for x86 platform compiler and hardware objects for the APIC, cpu, interrupt vectors and handling, timers, ethernet cards, and so on need to be implemented to support running applications on a stock x86 platform.

Increased application speed is achieved. There are several reasons for this. No paging hardware is needed or expected. Unix programs are linked and compiled to run from an address space starting from zero. That is what virtual memory and paging hardware provide. Java does not need that ability. Not needing paging and virtual memory saves on program context switching time. It does not need to switch the page table, flush old tlb entries and load new ones, and flush the cache. Another speed advantage is that it does not need run between user and supervisor modes. Since security is managed by the programming language, VM, garbage collection, and security manager, the application can run in supervisor mode safely. This saves time on context switching and interrupt handling. Speed is also increased by taking advantage of the JikesRVM adaptive compiler. This feature will continually optimize an application by analyzing how it runs.

Total size of the system is reduced. The obvious reductions come from no operating system and no file system. Not so obvious is that only objects and classes that the application uses are part of the system. It will only the include the parts of the Java library that it needs. This translates to an image that is about 19MB at this moment. I am confident that the size can be reduced event more. I also believe that smaller code will lead to a decrease in code defects.

This is interoperable with current Java compilers, class files, and libraries. One does not need to recompile their code to work in the JOE system. The whole JOE tool suite is written in Java which simplifies the tool chain greatly. The development tools can run on any platform that supports Java runtime environment.

## How to Build

You need to set the config.name and host.name properties. Valid config names are `BaseBaseNoGC` and `BaseBaseSemiSpace`. host.name I am using now is x86_64-osx. You should use a host.name that is appropriate for the platform you are using. The build uses the Ant file build.xml. The `compile-classpath` and `compile-mmtk` targets must be executed first. The `build-bootimage` target is used to compile and build the JOE executable image which is names jam.out.

To execute the jam.out use the command: `qemu-system-i386 -no-reboot -kernel jam.out -nographic  -device i82559c,netdev=mynet -netdev tap,id=mynet,script=./tap-up,downscript=no,ifname=tap0`

The jam.out in the top directory will run org.jam.test.Sleep thread.

org.jikesrvm.VM is where application boot starts.
