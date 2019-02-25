# JOE

## Java On Everything

JOE is a software virtualization tool that removes the boundaries between the user code, virtual machine, operating system, and hardware. The overhead and complexities in the user/supervisor crossing, program security, memory protection, virtual addressing, and the JNI library interface are replaced with simpler and more efficient mechanisms found in the Java language. JOE seamlessly blends those layers together by implementing them all in Java. This virtualized Java environment provides a secure, safe, cohesive, and consistent object oriented operation from application to hardware.

JOE is a very efficient virtualization tool. Current virtualization technology needs a hypervisor layer to run multiple operating systems.  This requires more resources like a processor with hardware virtualization, memory, and disk, and more complexity with configuring multiple operating systems. The virtualized Java provided by JOE runs multiple applications on any processor in less memory, no disk space, and without the need of special hardware virtualization extensions. JOE reduces the complexities by blending in the virtual machine, operating system, and hardware layers into consistent and cohesive objects  which allows the developer to concentrate on the essential complexity of the application.

One can think of JOE as AWS lambda programming with no arbitrary constraints. JOE can run on a varied range of hardware platforms; from AWS ECS or EC2 instances to tightly constrained embedded hardware platforms. JOE has no arbitrary time or memory limits that AWS lambda functions have and it is  just as easy to deploy, run, and update.

The motivation for JOE is to make it easier to deploy and run an application on any computer. The virtualized Java environment can use Java without an operating system by providing objects that can provide operating system services. Dan Ingalls said that "An operating system is a collections of things that don't fit into a language. There should not be one." Languages like Java, Smalltalk, C# contain all the high level objects and language constructs that provide threading, synchronization, and timers but used an operating system to provide those services. The JikesRVM has made it possible to implement the lower level services that where provided by an operating system to be implemented as Java objects.

[The Design](DESIGN.md)

## What are the advantages to this approach?

JOE will reduce the difficulty of application design. The elimination of the operating system  reduces an application's complexity because the operating system does not need to be considered in the design. The developer is free to design application specific abstractions and interfaces to meet the application's specification. The elimination of the operating system will eliminate the accidental difficulty of configuring and maintaining it. Operating system configuration and maintenance can be a complex, time consuming and error prone process. This has the advantage of being a homogeneous environment in that only Java can be used. This is an advantage because it prevents a 'Tower of Babel' situation that can happen when using different programming languages and environments in an application's design.

Application security is increased. Java was designed with security in mind. Security is increased through the JVM, language type safety, garbage collection, and the security manager. There are no inherited operating system vulnerabilities to deal with. Everything in memory is an object and thus has an identity as determined by an Object table. Security is increased because of decreased system size. Smaller system size reduces the code surface area and ultimately the potential avenues of attack.

Application portability is automatic. This is more of a platform implementation issue because the ability to run your application relies on if the appropriate compilers and hardware objects are implemented to run that application on a specific hardware platform. That means for x86 platform compiler and hardware objects for the APIC, cpu, interrupt vectors and handling, timers, ethernet cards, and so on need to be implemented to support running applications on a stock x86 platform.

Increased application speed is achieved. There are several reasons for this. No paging hardware is needed or expected. Unix programs are linked and compiled to run from an address space starting from zero. That is what virtual memory and paging hardware provide. Java does not need that ability. Not needing paging and virtual memory saves on program context switching time. It does not need to switch the page table, flush old tlb entries and load new ones, and flush the cache. Another speed advantage is that it does not need run between user and supervisor modes. Since security is managed by the programming language, VM, garbage collection, and security manager, the application can run in supervisor mode safely. This saves time on context switching and interrupt handling. Speed is also increased by taking advantage of the JikesRVM adaptive compiler. This feature will continually optimize an application by analyzing how it runs.

Total size of the system is reduced. The obvious reductions come from no operating system and no file system. Not so obvious is that only objects and classes that the application uses are part of the system. It will only the include the parts of the Java library that it needs. This translates to an image that is about 19MB at this moment. I am confident that the size can be reduced event more. I also believe that smaller code will lead to a decrease in code defects.

This is interoperable with current Java compilers, class files, and libraries. One does not need to recompile their code to work in the JOE system. The whole JOE tool suite is written in Java which simplifies the tool chain greatly. The development tools can run on any platform that supports Java runtime environment.

## How to Build

Ant is used to compile and build the JOE files and image. To build after the initial repository clone:
```
ant compile
ant compile-classpath
ant build
```
To run the image, jam.out:
```
cd jikesrvm/target/BaseBaseSemiSpace_x86_64-osx
../../../scripts/rjoe
```

The jam.out in the top directory will run org.jam.test.Sleep thread.

org.jikesrvm.VM is where application boot starts.
