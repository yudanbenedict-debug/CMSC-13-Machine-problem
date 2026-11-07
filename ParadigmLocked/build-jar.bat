@echo off
rem Builds one runnable JAR (code + libraries + assets): ..\Installer\Jar\ParadigmLocked.jar
setlocal
cd /d "%~dp0"
set OUT_DIR=..\Installer\Jar
if exist build rmdir /s /q build
mkdir build\classes
if not exist "%OUT_DIR%" mkdir "%OUT_DIR%"

echo Compiling...
dir /s /b src\*.java > build\sources.txt
javac --release 17 -nowarn -encoding UTF-8 -d build\classes -cp "lib/*" @build\sources.txt || exit /b 1

echo Bundling libraries...
pushd build\classes
for %%L in (..\..\lib\*.jar) do jar xf "%%L"
if exist META-INF rmdir /s /q META-INF
popd

echo Bundling assets (WAV audio only)...
robocopy assets build\classes /E /XF *.mp3 .DS_Store /NFL /NDL /NJH /NJS >nul

jar --create --file "%OUT_DIR%\ParadigmLocked.jar" --main-class com.mygame.main.ParadigmLocked -C build\classes .
rmdir /s /q build
echo Done: Installer\Jar\ParadigmLocked.jar  (run: java -jar Installer\Jar\ParadigmLocked.jar)
endlocal
