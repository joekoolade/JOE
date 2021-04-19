# JOE

[Latest Status](https://github.com/joekoolade/JOE/wiki)

## Java On Everything

JOE can run a Java program without an operating system by building the operating system into the program. This makes the program easier to configure and deploy. JOE can make any Java programmer an embedded developer.

JOE is a process virtual machine that can run a Java application without an operating system. It builds the OS into the program by virtualizing the OS and hardware. This removes the boundaries between the OS, hardware, user code, and virtual machine. The overhead and complexities in the user/supervisor crossing, program security, memory protection, virtual addressing, and the JNI library interface are replaced with simpler and more efficient mechanisms found in the Java language. JOE seamlessly blends those layers together by implementing them all in Java. This virtualized Java environment provides a secure, safe, cohesive, and consistent object oriented operation from application to hardware.

How can one virtualize an OS and hardware with Java since Java is not considered a system programming language? JikesRVM provides an extensible framework, known as vmmagic, that allows for the low-level programming required for an OS and hardware. The JikesRVM object model creator, and the bootimage writer, will substitute the correct operation for the vmmagic based on the object model being generated. Because the vmmagic is implemented in Java, the OS and hardware can be tested independent of the computer platform they are intended to operate in.

Even though Java is used, any language running on a process virtual machine like C#, Smalltalk, or Self can be be used. The recipe for this is a meta-circular VM, a vmmagic like framework for low-level programming extensions, and the ability to generate new object models. Implementing those components in your language of choice will produce applications without an operating system for your chosen language.

JOE is a bleeding edge process virtual machine technology. Current virtualization technology needs a hypervisor layer to run multiple operating systems.  This requires  a processor with hardware virtualization, memory, and disk. JOE is a software based process virtual machine, that runs multiple applications on any processor in less memory, no OS, no files, no MMU, and no disk. This is antithetical to how all other applications are run. JOE reduces the complexities by blending in the virtual machine, operating system, and hardware layers into consistent and cohesive objects which allows the developer to concentrate on the essential complexity of the application.

The motivation for JOE is to make it easier to deploy and run an application on any computer. The virtualized Java environment can use Java without an operating system by providing objects that can provide operating system services. Dan Ingalls said that "An operating system is a collections of things that don't fit into a language. There should not be one." Languages like Java, Smalltalk, C# contain all the high level objects and language constructs that provide threading, synchronization, and timers but used an operating system to provide those services. The JikesRVM has made it possible to implement the lower level services that where provided by an operating system to be implemented as Java objects.

[The Design](DESIGN.md)

## What are the advantages to this approach?

JOE creates an application that is very secure. The OS as designed today is intrinsically unsafe and insecure because of its  use of  'C' and similar type programming languages and files. 'C's shortcomings are well documented. The reason I mention files is because they are the main vectors for virus's and malware to invade a computer system. A file is just a bunch of anonymous data that has no clear identity. The program and OS  do not verify if a file or executable is hostile. Java was designed with security in mind. Security is addressed through the JVM, language type safety, garbage collection, and the security manager. There are no inherited operating system vulnerabilities to deal with. Classes and objects are verified when loaded into the VM. Everything in memory is an object and thus has an identity as determined by an Object table. Security is increased because of the design's metacircularity and the resulting decrease of system size. Smaller system size reduces the code surface area and ultimately the potential avenues of attack.

JOE will reduce the difficulty of application design. The elimination of the operating system  reduces an application's complexity because the operating system does not need to be considered in the design. The developer is free to design application specific abstractions and interfaces to meet the application's specification. The elimination of an external operating system will eliminate the accidental difficulty of configuring and maintaining it. Operating system configuration and maintenance can be a complex, time consuming and error prone process. JOE has the advantage of being a homogeneous environment in that only Java can be used. This is an advantage because it prevents a 'Tower of Babel' situation that can happen when using different programming languages and environments in an application's design.

Application portability is automatic. This is more of a platform implementation issue because the ability to run your application relies on if the appropriate compilers and hardware objects are implemented to run that application on a specific hardware platform. That means for x86 platform compiler and hardware objects for the APIC, cpu, interrupt vectors and handling, timers, ethernet cards, and so on need to be implemented to support running applications on a stock x86 platform.

Increased application speed is achieved. There are several reasons for this. No paging hardware is needed or expected. Unix programs are linked and compiled to run from an address space starting from zero. That is what virtual memory and paging hardware provide. Java does not need that ability. Not needing paging and virtual memory saves on program context switching time. It does not need to switch the page table, flush old tlb entries and load new ones, and flush the cache. Another speed advantage is that it does not need run between user and supervisor modes. Since security is managed by the programming language, VM, garbage collection, and security manager, the application can run in supervisor mode safely. This saves time on context switching and interrupt handling. Speed is also increased by taking advantage of the JikesRVM adaptive compiler. This feature will continually optimize an application by analyzing how it runs.

Total size of the system is reduced. The obvious reductions come from no operating system and no file system. Not so obvious is that only objects and classes that the application uses are part of the system. It will only the include the parts of the Java library that it needs. This translates to an image that is about 19MB at this moment. I am confident that the size can be reduced event more. I also believe that smaller code will lead to a decrease in code defects.

This is interoperable with current Java compilers, class files, and libraries. One does not need to recompile their code to work in the JOE system. The whole JOE tool suite is written in Java which simplifies the tool chain greatly. The development tools can run on any platform that supports Java runtime environment.

[Run your own classes](HelloWorldClass.md)

## How To Build

Ant is used to compile and build the JOE files and image. To build after the initial repository clone:
```
ant compile
ant compile-classpath
ant build
```
The system builds with Java 1.8. Earlier Java versions should work also. OpenJDK 1.11 DOES NOT work.

## How To Add External Classes

Precompiled classes can be added to the image and loaded by the runtime. This allows users to run  programs that have been compiled outside of the JOE image. The class StartUp has two methods runMain(String class) for running the main() method and runThread(String class)  for running a Thread class. Those two methods will class load the program and run it. To have classes loaded into the image they must be copied into the `ext/bin` directory. Below is an example. The classes must be compiled for Java version 1.5.

```
FAMILYs-MacBook-Pro:JOE joe$ ls -R ext/bin
com	hello

ext/bin/com:
vonhessling

ext/bin/com/vonhessling:
DiningPhilosophers$Philosopher.class	DiningPhilosophers.class

ext/bin/hello:
world

ext/bin/hello/world:
HwThread.class
```
## How To Run

To run the image, jam.out:
```
cd jikesrvm-hgÍ/target/BaseBaseSemiSpace_x86_64-osx
../../../scripts/rjoe
```

The jam.out in the top directory will run org.jam.test.Sleep thread.

org.jikesrvm.VM is where application boot starts.
