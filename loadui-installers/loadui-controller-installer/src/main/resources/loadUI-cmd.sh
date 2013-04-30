#!/bin/sh
### ====================================================================== ###
##                                                                          ##
##  loadUI Agent Bootstrap Script                                          ##
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
if [ "x$LOADUI_HOME" = "x" ]
then
    # get the full path (without any relative bits)
    LOADUI_HOME=`cd $DIRNAME/; pwd`
fi
export LOADUI_HOME

LOADUI_CLASSPATH="$LOADUI_HOME:$LOADUI_HOME/lib/*"

# For Cygwin, switch paths to Windows format before running java
if $cygwin
then
    LOADUI_HOME=`cygpath --path -w "$LOADUI_HOME"`
    LOADUI_CLASSPATH=`cygpath --path -w "$LOADUI_CLASSPATH"`
fi 

JAVA="jre/bin/java"

JAVA_OPTS="-Xms128m -Xmx768m -XX:MaxPermSize=128m"

$JAVA $JAVA_OPTS -cp "$LOADUI_CLASSPATH" com.javafx.main.Main --cmd=true --nofx=true -nofx -Dlog4j.configuration=log4j_headless.xml "$@"