#!/bin/bash
# Script to run the President Game application
# This script ensures the project is built and then runs the JavaFX application

echo "🃏 Building President Game..."
./gradlew clean build

if [ $? -eq 0 ]; then
    echo "✅ Build successful!"
    echo "🎮 Starting President Game..."
    ./gradlew run
else
    echo "❌ Build failed. Please check the error messages above."
    exit 1
fi
