@echo off

set LOADUI_HOME=%~dp0

cd /d %~dp0 
echo %CD%

rem init classpath

set CLASSPATH=.;lib/*;

rem JVM parameters, modify as appropriate

set JAVA_OPTS=-Xms128m -Xmx1024m -XX:MaxPermSize=256m

:START

rem ********* run loadui ***********

java %JAVA_OPTS% -cp "%CLASSPATH%" com.javafx.main.Main %*