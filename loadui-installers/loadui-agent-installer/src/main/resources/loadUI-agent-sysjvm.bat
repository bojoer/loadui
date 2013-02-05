@echo off

set LOADUI_HOME=%~dp0

cd /d %~dp0 
echo %CD%

set JAVA=java

:SET_CLASSPATH

rem init classpath

set CLASSPATH=.;lib/*;

rem JVM parameters, modify as appropriate

set JAVA_OPTS=-Xms128m -Xmx768m -XX:MaxPermSize=128m

:START

rem ********* run loadUI Runner ***********

"%JAVA%" %JAVA_OPTS% -cp "%CLASSPATH%" com.eviware.loadui.launcher.LoadUILauncher -Dloadui.instance=agent -Djava.awt.headless=true -nofx %*
