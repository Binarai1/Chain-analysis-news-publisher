@ECHO OFF
:: Copyright 2015 the original author or authors.
::
:: Licensed under the Apache License, Version 2.0 (the "License");
:: you may not use this file except in compliance with the License.
:: You may obtain a copy of the License at
::
::      https://www.apache.org/licenses/LICENSE-2.0
::
:: Unless required by applicable law or agreed to in writing, software
:: distributed under the License is distributed on an "AS IS" BASIS,
:: WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
:: See the License for the specific language governing permissions and
:: limitations under the License.

SETLOCAL

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"

IF DEFINED JAVA_HOME GOTO findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" GOTO execute

echo. 1>&2
ECHO ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH. 1>&2
echo. 1>&2
ECHO Please set the JAVA_HOME variable in your environment to match the 1>&2
ECHO location of your Java installation. 1>&2

GOTO fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:;=%
set JAVA_EXE=%JAVA_HOME%\bin\java.exe

if exist "%JAVA_EXE%" GOTO execute

echo. 1>&2
ECHO ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME% 1>&2
echo. 1>&2
ECHO Please set the JAVA_HOME variable in your environment to match the 1>&2
ECHO location of your Java installation. 1>&2

GOTO fail

:execute
set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar

@rem Collect all arguments for the java command, following the shell quoting and substitution rules
set CMD_LINE_ARGS=
set _SKIP=2

:setupArgs
if "x%~1" == "x" goto doneSetArgs

set CMD_LINE_ARGS=%*
goto doneSetArgs

doneSetArgs:

"%JAVA_EXE%" %DEFAULT_JVM_OPTS% -Dorg.gradle.appname=%APP_BASE_NAME% -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %CMD_LINE_ARGS%

:fail
EXIT /B 1
