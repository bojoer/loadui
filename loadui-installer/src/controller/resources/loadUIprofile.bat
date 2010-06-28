@echo off

set LOADUI_HOME=%~dp0

cd /d %~dp0 
echo %CD%

set JAVAFX=%JAVAFX_HOME%\bin\javafx

if not "%JAVAFX_HOME%" == "" goto SET_CLASSPATH

set JAVAFX=javafx

:SET_CLASSPATH

rem init classpath

set CLASSPATH=.;lib/*;

rem JVM parameters, modify as appropriate

set JAVA_OPTS=-Xms128m -Xmx1024m -XX:MaxPermSize=256m

:START

rem ********* run loadui ***********

"%JAVAFX%" -agentpath:C:\PROGRA~1\JPROFI~1\bin\WINDOW~1\jprofilerti.dll=port=8849 %JAVA_OPTS% -cp "%CLASSPATH%" com.eviware.loadui.launcher.LoadUILauncher %*

