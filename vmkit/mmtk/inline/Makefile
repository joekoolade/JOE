##===- lib/Mvm/CommonThread/Makefile -----------------------*- Makefile -*-===##
# 
#                     The vmkit project
#
# This file is distributed under the University of Illinois Open Source
# License. See LICENSE.TXT for details.
# 
##===----------------------------------------------------------------------===##
LEVEL = ../..

include $(LEVEL)/Makefile.config

MODULE_WITH_GC = InlineMMTk

include $(LEVEL)/Makefile.common

# Use CompileCommonOpts instead of CXX.Flags to make sure the flag is added
# after the -Wall added by LLVM.
CompileCommonOpts += -Wno-unused-variable

clean-local::
	$(Verb) $(RM) -f *.inc
