@echo off
rem Compiles the game + test\ and runs SelfTest headless (no window, no sound needed).
setlocal
cd /d "%~dp0"
if exist build\test rmdir /s /q build\test
mkdir build\test
dir /s /b src\*.java test\*.java > build\test-sources.txt
javac -nowarn -encoding UTF-8 -d build\test -cp "lib/*" @build\test-sources.txt || exit /b 1
java -Djava.awt.headless=true -cp "build\test;lib/*" com.mygame.SelfTest 2>nul
endlocal
