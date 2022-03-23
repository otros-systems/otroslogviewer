@echo off
REM *******************************************************************************
REM  Copyright 2011 Krzysztof Otrebski
REM  
REM  Licensed under the Apache License, Version 2.0 (the "License");
REM  you may not use this file except in compliance with the License.
REM  You may obtain a copy of the License at
REM  
REM    http://www.apache.org/licenses/LICENSE-2.0
REM  
REM  Unless required by applicable law or agreed to in writing, software
REM  distributed under the License is distributed on an "AS IS" BASIS,
REM  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
REM  See the License for the specific language governing permissions and
REM  limitations under the License.
REM ******************************************************************************

:: Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal
set OLV_HOME=%~dp0
IF %OLV_HOME:~-1%==\ SET OLV_HOME=%OLV_HOME:~0,-1%
if "%OLV_HOME%" == "" set OLV_HOME=.
set CURRENT_DIR=%CD%
set MEMORY=-Xmx1024m
cd "%OLV_HOME%"

if "%1"=="-batch" goto batchMode

:: This default is a bad idea.
:: Requires admin privs if OLV installed to typical location.
::set OUT_FILE=%OLV_HOME%\olv.out.txt
::set ERR_FILE=%OLV_HOME%\olv.err.txt


:: Drop the conditionals.
:: If app installed to typical location but admin/root never runs it himself,
:: these conditionals (with defaults set above) break it for all other users.
::del /Q %OUT_FILE% 2>nul 1>nul
::if exist %OUT_FILE% (  
  set OUT_FILE=%TEMP%\olv.out.txt
::)

::del /Q %ERR_FILE% 2>nul 1>nul
::if exist %ERR_FILE% (
  set ERR_FILE=%TEMP%\olv.err.txt
::)

if exist "%JAVA_HOME%\bin\javaw.exe" (
  set LOCAL_JAVA="%JAVA_HOME%\bin\javaw.exe"
) else (
  set LOCAL_JAVA=javaw.exe
)
start "OtrosLogViewer" /B %LOCAL_JAVA% -DsingleInstance.startPort= -jar "%OLV_HOME%\lib\olv-exec.jar" %* 1>"%OUT_FILE%" 2>"%ERR_FILE%"
goto finish

:batchMode
if exist "%JAVA_HOME%\bin\java.exe" (
  set LOCAL_JAVA="%JAVA_HOME%\bin\java.exe"
) else (
  set LOCAL_JAVA=java.exe
)

echo on
%LOCAL_JAVA% %MEMORY% %SFTP_KEY% -jar "%OLV_HOME%\lib\olv-exec.jar" %*

:finish
