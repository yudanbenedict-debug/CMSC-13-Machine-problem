#!/bin/bash
# Compiles the game + test/ and runs SelfTest headless (no window, no sound needed).
set -e
cd "$(dirname "$0")"
rm -rf build/test
mkdir -p build/test
javac -nowarn -encoding UTF-8 -d build/test -cp "lib/*" $(find src test -name "*.java")
java -Djava.awt.headless=true -cp "build/test:lib/*" com.mygame.SelfTest 2>/dev/null
