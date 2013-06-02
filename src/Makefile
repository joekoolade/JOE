OBJS:=ZipArchive.o ZipFile.o AttributeInfo.o ClassFile.o ConstantPool.o test.o \
		Fields.o Interfaces.o Methods.o

TOP:=..
SRCS:=$(OBJS:.o=.cpp)

LLVM_BIN = ~/local/bin
LLVM_CONFIG = $(LLVM_BIN)/llvm-config

LLVM_INC:=-I$(shell $(LLVM_CONFIG) --includedir)
VMKIT_INC:=-I$(TOP)/vmkit/include 
J3_INC:=-I$(TOP)/vmkit/lib/J3 -I$(TOP)/vmkit/lib/J3/VMCore
INCLUDES:= $(VMKIT_INC) $(LLVM_INC) $(J3_INC)

LLVM_LIBDIR = -L$(shell $(LLVM_CONFIG) --libdir) 
LLVM_LIBS=$(shell $(LLVM_CONFIG) --libs core analysis x86 linker vectorize  scalaropts \
selectiondag  support tablegen target transformutils  debuginfo engine executionengine \
 codegen core cppbackend cppbackendcodegen cppbackendinfo  instcombine instrumentation interpreter \
 ipa ipo jit bitwriter)
VMKIT_LIBS = -lCompiler -lVMCore -lCompiler -LVMCore
VMKIT_LIBDIR = -L$(TOP)/vmkit/Debug+Asserts/lib

LIBS = $(LLVM_LIBDIR) $(VMKIT_LIBDIR) $(VMKIT_LIBS) $(LLVM_LIBS) 
LIBS += -lz

LDFLAGS = $(shell $(LLVM_CONFIG) --ldflags)
MUST_DEFINE:=-D_DEBUG -D_GNU_SOURCE -D__STDC_LIMIT_MACROS -D__STDC_CONSTANT_MACROS -D__WORDSIZE=32
MUST_DEFINE += -Woverloaded-virtual -Wcast-qual -fexceptions -Wno-variadic-macros 
MUST_DEFINE += -fno-omit-frame-pointer -fno-strict-aliasing -Wno-deprecated -ansi -fno-rtti
CXXFLAGS += -g -DENABLE_THREADS -fno-rtti $(MUST_DEFINE) $(INCLUDES)

all: test

jei: jei.o $(VMKIT_LIBDIR)/libCompiler.a $(VMKIT_LIBDIR)/libVMCore.a
	$(CXX) $(LDFLAGS) -o jei jei.o $(LIBS)

vmjc: vmjc.o
	$(CXX) $(LDFLAGS) -o vmjc vmjc.o $(LIBS) 

$(VMKIT_LIBDIR)/libCompiler.a $(VMKIT_LIBDIR)/libVMCore.a:
test: $(OBJS)
	$(CXX) -o test  -lz $^

clean:
	rm *.o *.exe
	
include $(SRCS:.cpp=.d)
	
%.d: %.cpp
	@set -e; rm -f $@; \
	$(CXX) -MM $(CPPFLAGS) $< > $@.$$$$; \
	sed 's,\($*\)\.o[ :]*,\1.o $@ : ,g' < $@.$$$$ > $@; \
	rm -f $@.$$$$
