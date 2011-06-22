#!/bin/bash
PRG="$0"
while [ -h "$PRG" ] ; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`"/$link"
  fi
done
s=`dirname "$PRG"`
curdir="`pwd`"
cd "$s"
s="`pwd`"
cd "$curdir"
if [ "$GATE_HOME" != "" ] 
then
  g="$GATE_HOME"
fi
g2=`grep "^gate.home=" $s/build.properties | grep = | cut -f 2 -d=`
if [ "$g2" != "" ]
then 
  g="$g2"
fi
log4j="-Dlog4j.configuration=file://$g/bin/log4j.properties"
while test "$1" != "";
do
  case "$1" in
  -l4j)
    shift
    url="$1"
    shift
    log4j="-Dlog4j.configuration=$url"
    ;;
  -l4jc)
    shift
    url="file://$curdir/log4j.properties"
    log4j="-Dlog4j.configuration=$url"
    ;;
  -h)
    cat <<EOF
The following options can be passed immediately after the command name:
  -l4j url    ... the URL of a file containing log4j configuration properties
  -l4jc       ... use the file log4j.properties in the current directory
              If neither -l4j nor -l4jc is specified, the log4j.properties in
              the GATE_HOME/bin directory is used
  -h          ... show this help 
  All other options get passed to the Java program:
EOF
  java $JAVAP_OPTS -cp "$g/bin/gate.jar:$g/lib/*:$s/lib/*:$s/InOutProcessing.jar" at.ofai.gate.process.ProcessFiles --help
  exit 0
  ;;
  *)
  break
  ;;
  esac
done
java $JAVA_OPTS -Dlog4j.debug=true "$log4j" -cp "$g/bin/gate.jar:$g/lib/*:$s/lib/*:$s/InOutProcess.jar" at.ofai.gate.process.ProcessFiles  "$@"

