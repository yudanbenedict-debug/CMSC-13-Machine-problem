#!/bin/bash
# Builds one runnable JAR (code + libraries + assets): ../Installer/Jar/ParadigmLocked.jar
set -e
cd "$(dirname "$0")"
OUT_DIR="../Installer/Jar"
rm -rf build
mkdir -p build/classes "$OUT_DIR"

echo "Compiling..."
javac -nowarn -encoding UTF-8 -d build/classes -cp "lib/*" $(find src -name "*.java")

echo "Bundling libraries..."
(cd build/classes && for lib in ../../lib/*.jar; do jar xf "$lib"; done && rm -rf META-INF)

echo "Bundling assets (WAV audio only)..."
(cd assets && find . -type f ! -name "*.mp3" ! -name ".DS_Store" -exec sh -c 'mkdir -p "../build/classes/$(dirname "$1")" && cp "$1" "../build/classes/$1"' _ {} \;)

jar --create --file "$OUT_DIR/ParadigmLocked.jar" --main-class com.mygame.main.ParadigmLocked -C build/classes .
rm -rf build
echo "Done: Installer/Jar/ParadigmLocked.jar  (run: java -jar Installer/Jar/ParadigmLocked.jar)"
