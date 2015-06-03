#!/bin/sh

if [ ! -e "${JPF_HOME}/build/jpf.jar" ]
then
	echo "Make sure JPF_HOME is set and that JPF is compiled to jpf.jar."
	exit
fi

javac -classpath ${JPF_HOME}/build/jpf.jar:. *.java
