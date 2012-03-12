@echo off

set LOADUI_HOME=%~dp0

cd /d %~dp0 
echo %CD%

set JAVA=jre/bin/java

:SET_CLASSPATH

rem init classpath

set CLASSPATH=.;lib/*;

rem JVM parameters, modify as appropriate

set JAVA_OPTS=-Xms128m -Xmx768m -XX:MaxPermSize=128m

:START

rem ********* run loadUI Agent ***********

"%JAVA%" %JAVA_OPTS% -cp "%CLASSPATH%" com.eviware.loadui.launcher.LoadUICommandLineLauncher -Djava.awt.headless=true %*
