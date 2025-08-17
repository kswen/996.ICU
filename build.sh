#!/bin/bash

# Image OCR Processor Build Script
# This script builds and tests the OCR application

set -e

echo "=== Image OCR Processor Build Script ==="
echo

# Check Java version
echo "Checking Java version..."
java -version
echo

# Check Maven version
echo "Checking Maven version..."
mvn -version
echo

# Check Tesseract installation
echo "Checking Tesseract installation..."
if command -v tesseract &> /dev/null; then
    tesseract --version
    echo
    echo "Available languages:"
    tesseract --list-langs
    echo
else
    echo "WARNING: Tesseract not found in PATH"
    echo "Please install Tesseract OCR with Chinese language support"
    echo
fi

# Clean and compile
echo "Cleaning and compiling project..."
mvn clean compile
echo

# Download dependencies
echo "Resolving dependencies..."
mvn dependency:resolve
echo

# Create necessary directories
echo "Creating directories..."
mkdir -p logs
mkdir -p sample-images
echo

# Build success message
echo "=== Build Completed Successfully ==="
echo
echo "To run the OCR processor:"
echo "1. Interactive mode: mvn exec:java -Dexec.mainClass=\"com.example.ocr.OCRDemo\""
echo "2. Single image: mvn exec:java -Dexec.mainClass=\"com.example.ocr.OCRDemo\" -Dexec.args=\"single path/to/image.png\""
echo "3. Batch process: mvn exec:java -Dexec.mainClass=\"com.example.ocr.OCRDemo\" -Dexec.args=\"batch path/to/directory\""
echo "4. Demo mode: mvn exec:java -Dexec.mainClass=\"com.example.ocr.OCRDemo\" -Dexec.args=\"demo\""
echo
echo "Place test images in the 'sample-images' directory and run demo mode to test."
echo