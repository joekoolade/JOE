##===- lib/J3/ClassLib/GNUClasspath/Makefile ---------------*- Makefile -*-===##
# 
#                     The vmkit project
#
# This file is distributed under the University of Illinois Open Source
# License. See LICENSE.TXT for details.
# 
##===----------------------------------------------------------------------===##
LEVEL = ../../../..


include $(LEVEL)/Makefile.config

# MODULE_WITH_GC = Classpath
LIBRARYNAME=ClassLib
include $(LEVEL)/Makefile.common

CXX.Flags += -I$(PROJ_SRC_DIR)/../../VMCore -I$(PROJ_SRC_ROOT)/include/j3
CXX.Flags += -I$(PROJ_SRC_DIR)/.. -I$(PROJ_OBJ_DIR)/..
