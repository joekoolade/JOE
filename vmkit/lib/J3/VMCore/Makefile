##===- lib/J3/VMCore/Makefile ------------------------------*- Makefile -*-===##
# 
#                     The vmkit project
#
# This file is distributed under the University of Illinois Open Source
# License. See LICENSE.TXT for details.
# 
##===----------------------------------------------------------------------===##
LEVEL = ../../..

EXTRA_DIST = JniClasspath.inc JniOpenJDK.inc

include $(LEVEL)/Makefile.config

MODULE_WITH_GC = J3

include $(LEVEL)/Makefile.common

CXX.Flags += -I$(PROJ_OBJ_DIR)/../ClassLib -I$(PROJ_OBJ_DIR)/../LLVMRuntime -I$(PROJ_SRC_DIR)/../ClassLib/$(CLASSPATH_DIR) -I$(PROJ_SRC_DIR)/../../../include/j3 -I$(PROJ_SRC_DIR)/../ClassLib
