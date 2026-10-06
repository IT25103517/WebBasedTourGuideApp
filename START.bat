@echo off
setlocal enabledelayedexpansion
REM ===========================================================
REM  Web Based Tour Guide - start the application
REM  Opens two windows: the backend (Java/Spring Boot) and the
REM  frontend (React/Vite). Close both windows to stop the app.
REM ===========================================================

if not exist "%~dp0backend\.env" (
    echo.
    echo   backend\.env is missing - run SETUP.bat first.
    echo.
    pause
    exit /b
)

set "BACKEND_JAR="
for %%F in ("%~dp0backend\target\*.jar") do (
    echo %%~nxF | findstr /i /v "\.original$" >nul
    if not errorlevel 1 set "BACKEND_JAR=%%F"
)

if not defined BACKEND_JAR (
    echo.
    echo   The backend has not been built - run SETUP.bat first.
    echo   expected a jar under backend\target\*.jar
    echo.
    pause
    exit /b
)

if not exist "%~dp0frontend\node_modules" (
    echo.
    echo   Frontend packages are not installed - run SETUP.bat first.
    echo.
    pause
    exit /b
)

REM ---- load backend\.env into this process's environment, so the two
REM ---- windows launched below (and the java process itself) can see it.
REM ---- Spring Boot does not read a .env file natively.
for /f "usebackq eol=# tokens=1,* delims==" %%A in ("%~dp0backend\.env") do (
    if not "%%A"=="" if not "%%B"=="" set "%%A=%%B"
)

if "%PORT%"=="" set "PORT=5000"

REM ---- prefer the JDK setup.ps1 detected/built with over a bare 'java' on
REM ---- PATH, which might resolve to an older JRE bundled with another app.
set "JAVA_EXE=java"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"

echo.
echo   Starting the backend  ... http://localhost:%PORT%
start "Tour Guide - BACKEND" cmd /k "cd /d "%~dp0backend" && "%JAVA_EXE%" -jar "%BACKEND_JAR%""

timeout /t 4 /nobreak >nul

echo   Starting the frontend ... http://localhost:5173
start "Tour Guide - FRONTEND" cmd /k "cd /d "%~dp0frontend" && npm run dev"

timeout /t 6 /nobreak >nul

echo.
echo   Opening the browser...
start "" http://localhost:5173

echo.
echo   Both servers are running in their own windows.
echo   Close those two windows to stop the application.
echo.
pause
