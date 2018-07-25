JOE is working prototype that allows one to run a program without an OS. Specifically a Java program. The concept behind JOE does not constrain it to only for Java. The same idea can be applied to C#, Smalltalk, or any other object oriented language that is written for a virtual machine. How does one run programs without an OS? The quick answer is "remove all OS dependencies". The conventional wisdom is to program your virtual machine to use an OS. This means to code the virtual machine program in 'C' and build an executable that can run on Linux/Unix. This is how it is done in 99.99999% of the existing VMs until now.

Benefits

Design Goals

High Level Design

Low Level Design
