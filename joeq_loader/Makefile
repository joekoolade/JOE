
all:	linux

msvc:	set_text_writeable_msvc
	cl /DWIN32 joeq.c native.c exception.c ../joeq/jq.obj
	set_text_writeable.exe joeq.exe

SOURCES = joeq.c native.c exception.c
OBJECT = ../joeq_core/jq.obj

ifeq (${OS},Windows_NT)
GCC_ARGS = -mno-cygwin -g
GCC_LIBS = 
SUFFIX = .exe
else
GCC_ARGS = -g
GCC_LIBS = -lpthread -ldl
SUFFIX = 
endif

linux:	joeq$(SUFFIX)

joeq$(SUFFIX):	set_text_writeable$(SUFFIX) $(SOURCES) $(OBJECT)
	gcc $(GCC_ARGS) $(SOURCES) $(OBJECT) $(GCC_LIBS) -o joeq$(SUFFIX)
	./set_text_writeable$(SUFFIX) joeq$(SUFFIX)

set_text_writeable$(SUFFIX):	set_text_writeable.c
	gcc -g set_text_writeable.c -o set_text_writeable$(SUFFIX)

set_text_writeable_msvc:	set_text_writeable.c
	cl /DWIN32 set_text_writeable.c

clean:
	rm -f joeq.o native.o exception.o set_text_writeable$(SUFFIX) joeq$(SUFFIX)
