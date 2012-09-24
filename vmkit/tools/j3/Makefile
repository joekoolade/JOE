##===- tools/j3/Makefile -----------------------------------*- Makefile -*-===##
# 
#                     The VMKit project
#
# This file is distributed under the University of Illinois Open Source
# License. See LICENSE.TXT for details.
# 
##===----------------------------------------------------------------------===##
LEVEL = ../..

include $(LEVEL)/Makefile.config

TOOLNAME = j3
USEDLIBS = Classpath.a J3.a J3Compiler.a Mvm.a MvmCompiler.a CommonThread.a FinalMMTk.a InlineMMTk.a Precompiled.a BootstrapClasses.a
BUILD_FRAMETABLE = 1
LINK_COMPONENTS = jit nativecodegen scalaropts instrumentation ipa ipo

include $(LEVEL)/Makefile.common
