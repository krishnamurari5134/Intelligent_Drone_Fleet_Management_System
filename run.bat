@echo off
REM Starts the web dashboard and opens it in your browser (needs Java 17+). Keep this window open while presenting.
java -version >nul 2>&1
if errorlevel 1 (
  echo Java is not installed or not on PATH. Install JDK 17 or newer, then try again.
  pause
  exit /b 1
)
java -jar DroneFleet.jar --web
pause
