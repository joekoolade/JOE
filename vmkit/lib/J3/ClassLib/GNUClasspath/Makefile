##===- lib/J3/ClassLib/GNUClasspath/Makefile ---------------*- Makefile -*-===##
# 
#                     The vmkit project
#
# This file is distributed under the University of Illinois Open Source
# License. See LICENSE.TXT for details.
# 
##===----------------------------------------------------------------------===##
LEVEL = ../../../..


EXTRA_DIST = ClasspathVMClass.inc ClasspathVMClassLoader.inc ClasspathVMObject.inc \
	     ClasspathVMRuntime.inc ClasspathVMStackWalker.inc ClasspathVMSystem.inc \
	     ClasspathVMSystemProperties.inc ClasspathVMThread.inc ClasspathVMThrowable.inc

include $(LEVEL)/Makefile.config

MODULE_WITH_GC = Classpath

include $(LEVEL)/Makefile.common

CXX.Flags += -I$(PROJ_SRC_DIR)/../../VMCore -I$(PROJ_SRC_ROOT)/include/j3
CXX.Flags += -I$(PROJ_SRC_DIR)/.. -I$(PROJ_OBJ_DIR)/..
