# JOE

## Status

Currently JOE needs to be developed in a JDK 7 SDK environment. I porting this code to be developed in a JDK 11 SDK. This work is happening on the jdk11 branch.

## Java On Everything

JOE runs a Java program on an unconventional operating system that is incorporated by the Java language. This makes the program easier to configure and deploy. JOE can make any Java programmer an embedded developer.

JOE is a software virtualization tool. It incorporates the OS into the program by writing the OS and hardware subsystems in the Java language. This removes the boundaries between the OS, hardware, user code, and virtual machine. The overhead and complexities in the user/supervisor crossing, program security, memory protection, virtual addressing, and the JNI library interface are replaced with simpler and more efficient mechanisms found in the Java language. This virtualized Java environment provides a secure, safe, cohesive, and consistent object oriented operation from application to hardware.

How can one write an OS and hardware with Java since Java is not considered a system programming language? JikesRVM provides an extensible framework, known as vmmagic, that allows for the low-level programming required for an OS and hardware. The JikesRVM object model creator, the bootimage writer, will substitute the correct operation for the vmmagic based on the object model being generated. Because the vmmagic is implemented in Java, the OS and hardware can be tested independent of the computer platform they are intended to operate in.

Even though Java is used, any language running on a process virtual machine like C#, Smalltalk, or Self can be be used. The recipe for this is a meta-circular VM, a vmmagic like framework for low-level programming extensions, and the ability to generate new object models. Implementing those components in your language of choice will produce applications without an operating system for your chosen language.

JOE is a bleeding edge process virtual machine technology. JOE reduces the complexities by blending in the virtual machine, operating system, and hardware layers into consistent and cohesive objects which allows the developer to concentrate on the essential complexity of the application.

The motivation for JOE is to make it easier to deploy and run an application on any computer. The virtualized Java environment can use Java without an operating system by providing objects that can provide operating system services. Dan Ingalls said that "An operating system is a collections of things that don't fit into a language. There should not be one." Languages like Java, Smalltalk, C# contain all the high level objects and language constructs that provide threading, synchronization, and timers but used an operating system to provide those services. The JikesRVM has made it possible to implement the lower level services that where provided by an operating system to be implemented as Java objects.

[The Design](DESIGN.md)

## What are the advantages to this approach?

JOE creates an application that is secure and safe. The OS, as designed today, is intrinsically unsafe and insecure because of its  use of  'C' and similar type programming languages and files. 'C's shortcomings are well documented. The reason I mention files is because they are the main vectors for virus's and malware to invade a computer system. A file is just a bunch of anonymous data that has no clear identity. The program and OS  do not verify if a file or executable is hostile. Java was designed with security in mind. Security is addressed through the JVM, language type safety, garbage collection, and the security manager. There are no inherited operating system vulnerabilities to deal with. Objects are verified when loaded into the VM. Everything in memory is an object and thus has an identity as determined by a global object directory table. Security is increased because of the design's metacircularity and the resulting decrease of system size. Smaller system size reduces the code surface area and ultimately the potential avenues of attack.

JOE will reduce the difficulty of application design. The elimination of the operating system  reduces an application's complexity because the operating system does not need to be considered in the design. The developer is free to design application specific abstractions and interfaces to meet the application's specification. The elimination of an external operating system will eliminate the accidental difficulty of configuring and maintaining it. Operating system configuration and maintenance can be a complex, time consuming and error prone process. JOE has the advantage of being a homogeneous environment in that only Java can be used. This is an advantage because it prevents a 'Tower of Babel' situation that can happen when using different programming languages and environments in an application's design.

Application portability is automatic. This is more of a platform implementation issue because the ability to run your application relies on if the appropriate compilers and hardware objects are implemented to run that application on a specific hardware platform. That means for x86 platform compiler and hardware objects for the APIC, cpu, interrupt vectors and handling, timers, ethernet cards, and so on need to be implemented to support running applications on a stock x86 platform.

Increased application speed is achieved. There are several reasons for this. No paging hardware is needed or expected. Unix programs are linked and compiled to run from an address space starting from zero. That is what virtual memory and paging hardware provide. Java does not need that ability. Not needing paging and virtual memory saves on program context switching time. It does not need to switch the page table, flush old tlb entries and load new ones, and flush the cache. Another speed advantage is that it does not need run between user and supervisor modes. Since security is managed by the programming language, VM, garbage collection, and security manager, the application can run in supervisor mode safely. This saves time on context switching and interrupt handling. Speed is also increased by taking advantage of the JikesRVM adaptive compiler. This feature will continually optimize an application by analyzing how it runs.

Total size of the system is reduced. The obvious reductions come from no operating system and no file system. Not so obvious is that only objects and classes that the application uses are part of the system. It will only the include the parts of the Java library that it needs. This translates to an image that is about 19MB at this moment. I am confident that the size can be reduced event more. I also believe that smaller code will lead to a decrease in code defects.

This is inter-operable with current Java compilers, class files, and libraries. One does not need to recompile their code to work in the JOE system. The whole JOE tool suite is written in Java which simplifies the tool chain greatly. The development tools can run on any platform that supports Java runtime environment.

[Run your own classes](HelloWorldClass.md)

## How To Build

You can build from Mac OS or Linux. You will need Java OpenJDK7. You will also need jenv, brew, bison, and the gcc compiler, gcc-10.

For Intel MAC OS download [jdk-7u75](https://drive.google.com/file/d/1miSg_oKt_23BbHnqfmmnP4tMaOYxF4hj/view?usp=drive_link) and install the dmg file.

For Linux download [jdk-7u75](https://drive.google.com/file/d/1fkU8A1r7sITn2fQfR5-AWpoSvyWJ_B1C/view?usp=drive_link) and install the tar file.

### Intel Mac OS One Time Setup
```
% brew tap homebrew/cask-versions
% brew install ant@1.9
% ln -s /usr/local/Cellar/ant@1.9/1.9.16/bin/ant /usr/local/bin/ant1.9

% /Library/Java/JavaVirtualMachines/jdk1.7.0_75.jdk/Contents/Home/bin/java -version
java version "1.7.0_75"
Java(TM) SE Runtime Environment (build 1.7.0_75-b13)
Java HotSpot(TM) 64-Bit Server VM (build 24.75-b04, mixed mode)

% cd <top of JOE repo>
% cat > .ant.properties
config.name=BaseBaseRefCount
host.name=x86_64-osx
skip.unit.tests=1
classlib.provider=OpenJDK
openjdk.lib.dir=/Library/Java/JavaVirtualMachines/jdk1.7.0_75.jdk/Contents/Home/jre/lib
ant.jar=/usr/local/Cellar/ant@1.9/1.9.16/libexec/lib/ant.jar
openjdk.classes.jar=rt.jar
<ctrl-D>

% jenv local 1.7
% export JAVA_HOME=/Library/Java/JavaVirtualMachines/1.7.0_75.jdk/Contents/Home
```
### Linux One Time Setup
```
% tar xf -C /usr/java openjdk-7u75-b13-linux-x64-18_dec_2014.tar
% yum install gcc bison flex ant-1.9
% export JAVA_HOME=/usr/java/java-se-7u75-ri/
```

### Docker One Time Setup
```
% docker pull joekoolade/joe:latest
% docker run --name joedev -d -i -t joekoolade/joe:latest /bin/bash
% docker exec -it joedev /bin/bash
... In docker
[root@9aab0cc0fe84]# cd JOE
[root@9aab0cc0fe84]# git clone https://github.com/joekoolade/JOE.git .
Cloning into '.'...
remote: Enumerating objects: 208151, done.
remote: Counting objects: 100% (1995/1995), done.
remote: Compressing objects: 100% (681/681), done.
remote: Total 208151 (delta 1052), reused 1963 (delta 1021), pack-reused 206156
Receiving objects: 100% (208151/208151), 474.38 MiB | 1.79 MiB/s, done.
Resolving deltas: 100% (144499/144499), done.
[root@9aab0cc0fe84]# cp build/configs/ant.properties-linux .ant.properties
```

### Docker JOE build
```
[root@9aab0cc0fe84]# cd /JOE
[root@9aab0cc0fe84]# ant build-bootimage
```

### Docker JOE run image
Need to have qemu-system-x86_64 installed and the scripts in /JOE/scripts
This is run outside of docker
```
% docker cp joedev:JOE/target/BaseBaseRefCount_x86_64-osx/jam.out .
% scripts/rjoe_x86_64
```

### Building JOE
```
% cd <top of JOE repo>
% ant1.9 build-bootimage
```
### Running testmode

The below will build and run VM tests

```
% cd <top of JOE repo>
% ant1.9 testmode build-bootimage
% cd target/BaseBaseSemiSpace_x86_64-osx
% ../../scripts/rjoe
```
## How To Add External Classes

Precompiled classes can be added to the image and loaded by the runtime. This allows users to run  programs that have been compiled outside of the JOE image. The class StartUp has two methods runMain(String class) for running the main() method and runThread(String class)  for running a Thread class. Those two methods will class load the program and run it. To have classes loaded into the image they must be copied into the `ext/bin` directory. Below is an example. The classes must be compiled for Java version 1.7 or older.

```
joe$ ls -R ext/bin
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
cd target/BaseBaseSemiSpace_x86_64-osx
../../scripts/rjoe
```

The jam.out in the top directory will run org.jam.test.Sleep thread.

org.jikesrvm.VM is where application boot starts.
