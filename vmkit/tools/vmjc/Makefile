##===- tools/vmjc/Makefile ---------------------------------*- Makefile -*-===##
# 
#                     The VMKit project
#
# This file is distributed under the University of Illinois Open Source
# License. See LICENSE.TXT for details.
# 
##===----------------------------------------------------------------------===##
LEVEL = ../..

include $(LEVEL)/Makefile.config

EXTRA_DIST = libvmjc

TOOLNAME = vmjc
USEDLIBS = J3.a Classpath.a J3.a J3Compiler.a Mvm.a MvmCompiler.a MMTk.a CommonThread.a
LINK_COMPONENTS = jit nativecodegen scalaropts instrumentation ipa ipo bitwriter

include $(LEVEL)/Makefile.common
