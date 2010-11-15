OBJS:=ZipArchive.o ZipFile.o AttributeInfo.o ClassFile.o ConstantPool.o test.o \
		Fields.o Interfaces.o Methods.o
		
SRCS:=$(OBJS:.o=.cpp)

all: test

test: $(OBJS)
	$(CXX) -o test  -lz $^

include $(SRCS:.cpp=.d)
	
%.d: %.cpp
	@set -e; rm -f $@; \
	$(CXX) -MM $(CPPFLAGS) $< > $@.$$$$; \
	sed 's,\($*\)\.o[ :]*,\1.o $@ : ,g' < $@.$$$$ > $@; \
	rm -f $@.$$$$
