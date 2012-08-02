@echo off
REM 保证工作空间在当前路径
%~d0
cd %~dp0
java -jar AutoPackageTool.jar
