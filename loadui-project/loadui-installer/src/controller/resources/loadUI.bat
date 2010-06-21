@echo off

cd /d %~dp0 
echo %CD%

set JAVAWS=jre/bin/javaws

rem JVM parameters, modify as appropriate

set JAVA_OPTS=-Xms128m -Xmx1024m -XX:MaxPermSize=256m

:START

rem ********* run loadui ***********

"%JAVAWS%" %JAVA_OPTS% %* loadUI.jnlp