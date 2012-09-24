##===- tools/precompiler/trainer/Makefile ------------------*- Makefile -*-===##
# 
#                     The VMKit project
#
# This file is distributed under the University of Illinois Open Source
# License. See LICENSE.TXT for details.
# 
##===----------------------------------------------------------------------===##
LEVEL = ../../..

EXTRA_DIST = HelloWorld.java

include $(LEVEL)/Makefile.config

BUILT_SOURCES = Precompiled.bc BootstrapClasses.bc

include $(LEVEL)/Makefile.common

PRECOMPILER := $(ToolDir)/precompiler$(EXEEXT)

ifndef VERBOSE
  J3.Flags := > /dev/null
endif

HelloWorld.class: HelloWorld.java
	$(Echo) "Compiling trainer"
	$(Verb) javac $< -d .

generated.bc: $(PRECOMPILER) HelloWorld.class
	$(Echo) "Pre-compiling bootstrap code"
	$(Verb) $(PRECOMPILER) -cp $$PWD HelloWorld $(J3.Flags)

Precompiled.bc: HelloWorld.class $(LibDir)/StaticGCPass$(SHLIBEXT) $(LibDir)/StaticGCPrinter$(SHLIBEXT) generated.bc
	$(Echo) "Building precompiled bootstrap code"
	$(Verb) $(MKDIR) $(ObjDir)
	$(Verb) $(LLC) -disable-branch-fold -disable-cfi -disable-debug-info-print -disable-fp-elim -O3 -load=$(LibDir)/StaticGCPrinter$(SHLIBEXT) generated.bc -o $(ObjDir)/Precompiled.s
	$(Verb) $(CC) -c $(ObjDir)/Precompiled.s -o $(ObjDir)/Precompiled.o
	$(Verb) $(Archive) $(LibDir)/libPrecompiled.a $(ObjDir)/Precompiled.o
	$(Verb) $(Ranlib) $(LibDir)/libPrecompiled.a
	$(Verb) $(CP) generated.bc Precompiled.bc

classes.bc: $(PRECOMPILER)
	$(Echo) "Pre-compiling classes"
	$(Verb) $(PRECOMPILER) -emit-class-bytes $(J3.Flags)

BootstrapClasses.bc: classes.bc
	$(Echo) "Building precompiled classes"
	$(Verb) $(MKDIR) $(ObjDir)
	$(Verb) $(LLC) classes.bc -o $(ObjDir)/BootstrapClasses.s
	$(Verb) $(CC) -c $(ObjDir)/BootstrapClasses.s -o $(ObjDir)/BootstrapClasses.o
	$(Verb) $(Archive) $(LibDir)/libBootstrapClasses.a $(ObjDir)/BootstrapClasses.o
	$(Verb) $(Ranlib) $(LibDir)/libBootstrapClasses.a
	$(Verb) $(CP) classes.bc BootstrapClasses.bc

clean-local::
	$(Verb) $(RM) -f HelloWorld.class generated.bc classes.bc Precompiled.bc BootstrapClasses.bc
