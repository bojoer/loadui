@echo off

set LOADUI_HOME=%~dp0

cd /d %~dp0 
echo %CD%

set JAVA=java

if exist jre/bin/java.exe (
    set JAVA=jre/bin/java.exe
)

:SET_CLASSPATH

rem init classpath

set CLASSPATH=.;lib/*;

rem JVM parameters, modify as appropriate

set JAVA_OPTS=-Xms128m -Xmx1024m -XX:MaxPermSize=256m

:START

rem ********* run loadUI ***********

"%JAVA%" %JAVA_OPTS% -cp "%CLASSPATH%" com.javafx.main.Main %*