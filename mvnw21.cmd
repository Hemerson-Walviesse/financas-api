@echo off
rem Executa o Maven Wrapper com o JDK 21 apenas para este projeto,
rem sem alterar o JAVA_HOME/PATH do sistema (que continua no Java 8).
setlocal
set "JAVA_HOME=%USERPROFILE%\.jdks\temurin-21.0.13"
set "PATH=%JAVA_HOME%\bin;%PATH%"
call "%~dp0mvnw.cmd" %*
endlocal
