@echo off
REM Script to run the President Game application
REM This script ensures the project is built and then runs the JavaFX application

echo Building President Game...
call .\gradlew.bat clean build

if %ERRORLEVEL% EQU 0 (
    echo Build successful!
    echo Starting President Game...
    call .\gradlew.bat run
) else (
    echo Build failed. Please check the error messages above.
    exit /b 1
)
