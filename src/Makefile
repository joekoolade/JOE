OBJS:=ZipArchive.o ZipFile.o test.o
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
