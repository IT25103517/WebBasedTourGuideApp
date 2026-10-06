@echo off
REM ===========================================================
REM  Re-creates TourGuideDB with fresh demo data.
REM  Use this before a progress evaluation so you demo on
REM  clean data. It does NOT touch your .env or packages.
REM ===========================================================

net session >nul 2>&1
if %errorLevel% neq 0 (
    powershell -NoProfile -Command "Start-Process -FilePath '%~f0' -Verb RunAs"
    exit /b
)

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0setup\reset-database.ps1"
