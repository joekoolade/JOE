#MODULES = $(wildcard ../*)
MODULES = ../joeq_core ../joeq_native ../joeq_x86
SUPPORT_LIBS = joeq/Support/javabdd-1.0b2.jar joeq/Support/jwutil-1.0.jar
JDK14SUPPORT_LIB = joeq/Support/java14.jar

ifeq (${OS},Windows_NT)
CLASSPATH_SEP = \;
BUILD_CLASSPATH = ".;$(subst $(space),;,$(SUPPORT_LIBS);$(MODULES))"
JOEQ_DIR = $(shell cygpath -ma .)
else
CLASSPATH_SEP = :
BUILD_CLASSPATH = .:$(subst $(space),:,$(SUPPORT_LIBS):$(MODULES))
JOEQ_DIR = $(PWD)
endif

ALLDIRS := $(shell find $(MODULES) -type d | grep -v CVS)
ALLJAVA := $(foreach d, $(ALLDIRS), $(wildcard $(d)/*.java))
ALLPKGS := $(sort $(foreach j, $(shell echo $(ALLJAVA) | sed 's/\.\.\/[^\/]*\///g'), $(patsubst %/,%,$(dir $(j)))))
ALLPKGNAMES := $(subst /,.,$(ALLPKGS))
# Note: doesn't catch newly-created class files.
ALLCLASS = $(foreach d, $(ALLDIRS), $(wildcard $(d)/*.class))

BOOTSTRAP_CLASSPATH = .$(CLASSPATH_SEP)$(JOEQ_DIR)$(CLASSPATH_SEP)$(BUILD_CLASSPATH)

CLASSFILE_VERSION = 1.3

all:	javac

### RULES TO BUILD SOURCE CODE
#
jikes:	bootclasspath source_list _jikes
# _jikes: Assumes prior existence of the .source_list file.
# Useful on cygwin, because cygwin takes a long time to build the .source_list file.
_jikes:	
	jikes -target $(CLASSFILE_VERSION) -bootclasspath @bootclasspath -classpath $(BUILD_CLASSPATH) @.source_list

jikes-pedantic:	bootclasspath source_list
	jikes -target $(CLASSFILE_VERSION) -bootclasspath @bootclasspath -classpath $(BUILD_CLASSPATH) +P @.source_list

javac:	bootclasspath source_list _javac
# _javac: Assumes prior existence of the .source_list file.
# Useful on cygwin, because cygwin takes a long time to build the .source_list file.
_javac:	
#	javac -g -source $(CLASSFILE_VERSION) -target $(CLASSFILE_VERSION) -bootclasspath @bootclasspath -classpath $(BUILD_CLASSPATH) @.source_list
	javac -g -source $(CLASSFILE_VERSION) -target $(CLASSFILE_VERSION) -bootclasspath "`cat bootclasspath`" -classpath $(BUILD_CLASSPATH) @.source_list

javac13:	bootclasspath source_list
	javac -g -bootclasspath @bootclasspath -classpath $(BUILD_CLASSPATH) @.source_list

### RULES TO CLEAN
#
clean:
	find $(MODULES) -name "*.class" | xargs rm -f
#	rm -f $(ALLCLASS)

veryclean:
	find $(MODULES) -name "*.class" | xargs rm -f bootclasspath .source_list
#	rm -f bootclasspath .source_list $(ALLCLASS)


### RULES TO ACCESS CVS
#
update:
	( export CVS_RSH=ssh ; cvs update -Pd )

commit:
	( export CVS_RSH=ssh ; cvs commit )


### RULES FOR BUILDING BOOT IMAGES
#
bootstrap:	joeq/Main/Bootstrapper.class
	java -Xbootclasspath/a:$(JDK14SUPPORT_LIB) -cp $(BOOTSTRAP_CLASSPATH) -mx480M joeq.Main.Bootstrapper

bootstrap-sun142-linux:	javac
	java -cp $(BOOTSTRAP_CLASSPATH) -ms320M -mx320M joeq.Main.Bootstrapper -cl ../joeq_native/joeq/ClassLib/sun142_linux/classlist.txt

bootstrap-sun14-linux:	javac
	java -cp $(BOOTSTRAP_CLASSPATH) -ms240M -mx240M joeq.Main.Bootstrapper -cl ../joeq_native/joeq/ClassLib/sun14_linux/classlist.txt

bootstrap-sun13-linux:	javac
	java -Xbootclasspath/a:$(JDK14SUPPORT_LIB) -cp $(BOOTSTRAP_CLASSPATH) -ms200M -mx200M joeq.Main.Bootstrapper -cl ../joeq_native/joeq/ClassLib/sun13_linux/classlist.txt

bootstrap-ibm13-linux:	javac
	java -Xbootclasspath/a:$(JDK14SUPPORT_LIB):/opt/IBMJava2-131/jre/lib/ext/ibmjcaprovider.jar -cp $(BOOTSTRAP_CLASSPATH) -ms240M -mx240M joeq.Main.Bootstrapper -cl ../joeq_native/joeq/ClassLib/ibm13_linux/classlist.txt

bootstrap-sun142-win32:	javac
	java -cp $(BOOTSTRAP_CLASSPATH) -ms200M -mx200M joeq.Main.Bootstrapper -cl ../joeq_native/joeq/ClassLib/sun142_win32/classlist.txt

bootstrap-sun14-win32:	javac
	java -cp $(BOOTSTRAP_CLASSPATH) -ms180M -mx180M joeq.Main.Bootstrapper -cl ../joeq_native/joeq/ClassLib/sun14_win32/classlist.txt

bootstrap-sun13-win32:	javac
	java -cp $(BOOTSTRAP_CLASSPATH) -Xbootclasspath/a:$(JDK14SUPPORT_LIB) -ms120M -mx120M joeq.Main.Bootstrapper -cl ../joeq_native/joeq/ClassLib/sun13_win32/classlist.txt

bootstrap-ibm13-win32:	javac
	java -cp $(BOOTSTRAP_CLASSPATH) -Xbootclasspath/a:$(JDK14SUPPORT_LIB)\;C:\\Program\ Files\\IBM\\Java13\\jre\\lib\\ext\\IBMJCEProvider.jar -ms150M -mx150M joeq.Main.Bootstrapper -cl ../joeq_native/joeq/ClassLib/ibm13_win32/classlist.txt

### RULES FOR BUILDING JAVADOC
#
javadoc:
	javadoc -classpath $(BUILD_CLASSPATH) -breakiterator -d ../joeq_project/htdocs/javadoc $(ALLPKGNAMES)


### MISC RULES
#
wc:
	@echo Total, and Top Five files:
	@wc -l $(ALLJAVA) | sort -rn | head -7


### LOCAL RULES (you will never need to make these directly)
#
source_list:
	@echo $(ALLJAVA) | xargs -n 1 echo > .source_list
#	find $(PACKAGES) -name "*.java" > .source_list

joeq/Main/Bootstrapper.class:
	$(MAKE) javac

bootclasspath:	joeq/Main/GetBootClassPath.class
	java joeq.Main.GetBootClassPath $(JDK14SUPPORT_LIB) > bootclasspath

joeq/Main/GetBootClassPath.class:	joeq/Main/GetBootClassPath.java
	javac joeq/Main/GetBootClassPath.java

empty:=
space:=$(empty) $(empty)
