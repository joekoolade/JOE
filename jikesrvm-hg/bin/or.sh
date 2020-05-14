#!/bin/bash
JIKES=${JIKES:-/Users/jkulig/git/joekoolade/JOE/jikesrvm-hg}
java -classpath "$JIKES/components/guava-26.0-jre.jar:$JIKES/components/objectreader.jar" org.jam.tools.ObjectReader $*