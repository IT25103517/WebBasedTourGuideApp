@echo off
REM ===========================================================
REM  Web Based Tour Guide - one-time setup
REM  Double-click this file. It will ask for Administrator
REM  rights, because it has to change SQL Server settings.
REM ===========================================================

net session >nul 2>&1
if %errorLevel% neq 0 (
    echo Requesting Administrator rights...
    powershell -NoProfile -Command "Start-Process -FilePath '%~f0' -Verb RunAs"
    exit /b
)

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0setup\setup.ps1"
