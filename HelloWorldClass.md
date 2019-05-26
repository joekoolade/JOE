# MyHelloWord Class

## Source File Location
Source files should be in the ext/src directory. They will be automatically compiled and built into the boot image, jam.out.
## Primordials
The class file signatures need to be added to the file jikesrvm-hg/build/primordials/externals.txt. This is the same signature as described in the Java VM spec, 7th edition in section 4.3.4. You can look at the other files in the primordials directory for examples.
## Starting Up
The boot process needs to be modified to run your class as a thread. This needs to be done in the finishBooting() method. You should not execute any methods that will sleep or yield the thread. The boot up process is done in the boot thread which is not a fully functional thread. Anything that will yield or schedule the boot thread will cause the boot process to fail. 
## TODO
The way to add a class as describe above is practical and convenient by not ideal. In the future the plan is be able to add the classes to the jksvm.jar so the classes are added to the boot image. You will be able to compile them separately from the JOE build process. Then you will specify the classes that you want run as threads or the main() method as a string. The boot process will load the classes and run them.