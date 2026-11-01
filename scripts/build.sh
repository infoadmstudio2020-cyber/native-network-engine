#!/bin/bash
# Build script for NativeNetworkEngine
set -e

echo "Building NativeNetworkEngine v1.0.4..."
chmod +x gradlew
./gradlew clean :java-library:assembleRelease 2>/dev/null || ./gradlew clean assembleRelease || ./gradlew clean build
echo "Build complete: build/outputs/aar/"
