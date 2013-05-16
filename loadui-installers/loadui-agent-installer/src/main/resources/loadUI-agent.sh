#!/bin/sh
### ====================================================================== ###
##                                                                          ##
##  loadUI Runner Bootstrap Script                                          ##
##                                                                          ##
### ====================================================================== ###

### $Id$ ###

DIRNAME=`dirname $0`

# OS specific support (must be 'true' or 'false').
cygwin=false;
case "`uname`" in
    CYGWIN*)
        cygwin=true
        ;;
esac

# Setup LOADUI_HOME
if [ "x$LOADUI_RUNNER_HOME" = "x" ]
then
    # get the full path (without any relative bits)
    LOADUI_RUNNER_HOME=`cd $DIRNAME/; pwd`
fi
export LOADUI_RUNNER_HOME

LOADUI_RUNNER_CLASSPATH="$LOADUI_RUNNER_HOME:$LOADUI_RUNNER_HOME/lib/*:$LOADUI_RUNNER_HOME/jre/lib/*"

# For Cygwin, switch paths to Windows format before running java
if $cygwin
then
    LOADUI_RUNNER_HOME=`cygpath --path -w "$LOADUI_RUNNER_HOME"`
    LOADUI_RUNNER_CLASSPATH=`cygpath --path -w "$LOADUI_RUNNER_CLASSPATH"`
fi 

cd $LOADUI_RUNNER_HOME

JAVA="jre/bin/java"

JAVA_OPTS="-Xms128m -Xmx768m -XX:MaxPermSize=128m"

$JAVA $JAVA_OPTS -cp "$LOADUI_RUNNER_CLASSPATH" com.javafx.main.Main --nofx=true --agent=true -nofx -Dlog4j.configuration=log4j_headless.xml "$@"
