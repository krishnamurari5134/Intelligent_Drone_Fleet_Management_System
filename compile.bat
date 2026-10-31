@echo off
REM Rebuild from source (needs a full JDK)
if exist out rmdir /s /q out
mkdir out
javac -d out src\drone\*.java
if errorlevel 1 (echo Compile failed & pause & exit /b 1)
xcopy src\web out\web /E /I /Y >nul
jar cfe DroneFleet.jar drone.Main -C out .
echo Built DroneFleet.jar
pause
