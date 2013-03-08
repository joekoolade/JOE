OBJS:=ZipArchive.o ZipFile.o AttributeInfo.o ClassFile.o ConstantPool.o test.o \
		Fields.o Interfaces.o Methods.o

TOP:=..
SRCS:=$(OBJS:.o=.cpp)

LLVM_BIN = /usr/bin
LLVM_CONFIG = $(LLVM_BIN)/llvm-config

# LLVM_INC:=-I$(shell $(LLVM_CONFIG) --includedir)
VMKIT_INC:=-I$(TOP)/vmkit/include 
MVM_INC:=-I$(TOP)/vmkit/lib/Mvm/GCMmap2 -I$(TOP)/vmkit/lib/Mvm/Allocator
J3_INC:=-I$(TOP)/vmkit/lib/J3
INCLUDES:= $(VMKIT_INC) $(LLVM_INC) $(MVM_INC) $(J3_INC)

LLVM_LIBDIR =
LLVM_LIBS=-lLLVM-3.1.dll
VMKIT_LIBS = -lJ3 -lClasspath -lJ3 -lJ3Compiler -lAllocator -lMvm -lMvmCompiler -lGCMmap2 -lCommonThread 
VMKIT_LIBDIR = -L$(TOP)/vmkit/Release+Debug/lib

LIBS = $(LLVM_LIBDIR) $(VMKIT_LIBDIR) $(VMKIT_LIBS) $(LLVM_LIBS) 
LIBS += -lz

LDFLAGS = $(shell $(LLVM_CONFIG) --ldflags)
MUST_DEFINE:=-D__STDC_LIMIT_MACROS -D__STDC_CONSTANT_MACROS -D__WORDSIZE=32
CXXFLAGS += -DENABLE_THREADS -fno-rtti $(MUST_DEFINE) $(INCLUDES)

all: test

vmjc: vmjc.o
	$(CXX) $(LDFLAGS) -o vmjc vmjc.o $(LIBS) 
	
test: $(OBJS)
	$(CXX) -o test  -lz $^

clean:
	rm *.o
	
include $(SRCS:.cpp=.d)
	
%.d: %.cpp
	@set -e; rm -f $@; \
	$(CXX) -MM $(CPPFLAGS) $< > $@.$$$$; \
	sed 's,\($*\)\.o[ :]*,\1.o $@ : ,g' < $@.$$$$ > $@; \
	rm -f $@.$$$$
