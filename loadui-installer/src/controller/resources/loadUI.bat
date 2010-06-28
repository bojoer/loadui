@echo off

cd /d %~dp0 
echo %CD%

set JAVAWS=jre/bin/javaws

:START

rem ********* run loadui ***********

"%JAVAWS%" loadUI.jnlp