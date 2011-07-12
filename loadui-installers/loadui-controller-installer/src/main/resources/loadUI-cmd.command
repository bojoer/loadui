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
if [ "x$LOADUI_AGENT_HOME" = "x" ]
then
    # get the full path (without any relative bits)
    LOADUI_AGENT_HOME=`cd $DIRNAME/; pwd`
fi
export LOADUI_AGENT_HOME

LOADUI_AGENT_CLASSPATH="$LOADUI_AGENT_HOME:$LOADUI_AGENT_HOME/lib/*"

# For Cygwin, switch paths to Windows format before running java
if $cygwin
then
    LOADUI_AGENT_HOME=`cygpath --path -w "$LOADUI_AGENT_HOME"`
    LOADUI_AGENT_CLASSPATH=`cygpath --path -w "$LOADUI_AGENT_CLASSPATH"`
fi 

JAVA_OPTS="-Xms128m -Xmx768m -XX:MaxPermSize=128m"

java $JAVA_OPTS -cp "$LOADUI_AGENT_CLASSPATH" com.eviware.loadui.launcher.LoadUICommandLineLauncher "$@"
