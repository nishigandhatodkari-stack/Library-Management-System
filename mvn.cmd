@echo off
setlocal
set "IDEA_MAVEN=C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.2.1\plugins\maven\lib\maven3\bin\mvn.cmd"
if exist "%IDEA_MAVEN%" (
    call "%IDEA_MAVEN%" %*
    exit /b %ERRORLEVEL%
)

for /f "tokens=*" %%i in ('where mvn 2^>nul') do (
    if /i not "%%i"=="%~f0" (
        call "%%i" %*
        exit /b %ERRORLEVEL%
    )
)

echo [ERROR] Maven not found.
exit /b 1
