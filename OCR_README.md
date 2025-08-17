# Image OCR Processor

A comprehensive Java application for Optical Character Recognition (OCR) using OpenCV for image preprocessing and Tess4J for text recognition, with specialized support for Chinese and English text.

## Features

### Core Capabilities
- **Advanced Image Preprocessing**: Using OpenCV for noise reduction, contrast enhancement, and image optimization
- **Multi-language OCR**: Support for Chinese (Simplified) and English text recognition
- **Batch Processing**: Process multiple images in a single operation
- **Text Region Detection**: Automatically detect and extract text regions from images
- **Customizable Parameters**: Adjust preprocessing parameters for optimal results
- **Interactive Demo**: User-friendly interface for testing and demonstration

### Image Processing Features
- Gaussian blur for noise reduction
- Adaptive contrast and brightness enhancement
- Binary thresholding with adaptive methods
- Morphological operations for text cleanup
- Dilation for improved text recognition

## Prerequisites

### System Requirements
- Java 11 or higher
- Maven 3.6+
- At least 2GB RAM for processing large images

### Tesseract Installation

#### Ubuntu/Debian:
```bash
sudo apt-get update
sudo apt-get install tesseract-ocr
sudo apt-get install tesseract-ocr-chi-sim  # For Chinese support
sudo apt-get install tesseract-ocr-eng      # For English support
```

#### CentOS/RHEL:
```bash
sudo yum install epel-release
sudo yum install tesseract
sudo yum install tesseract-langpack-chi_sim
sudo yum install tesseract-langpack-eng
```

#### macOS:
```bash
brew install tesseract
brew install tesseract-lang  # For additional languages
```

#### Windows:
1. Download Tesseract installer from [GitHub releases](https://github.com/UB-Mannheim/tesseract/wiki)
2. Install with language packs for Chinese and English
3. Add Tesseract to system PATH

### Verify Installation
```bash
tesseract --version
tesseract --list-langs  # Should show 'chi_sim' and 'eng'
```

## Installation & Setup

### 1. Clone and Build
```bash
git clone <repository-url>
cd image-ocr
mvn clean compile
```

### 2. Download Dependencies
```bash
mvn dependency:resolve
```

### 3. Verify Setup
```bash
mvn exec:java -Dexec.mainClass="com.example.ocr.OCRDemo" -Dexec.args="demo"
```

## Usage

### Command Line Interface

#### Process Single Image
```bash
mvn exec:java -Dexec.mainClass="com.example.ocr.OCRDemo" -Dexec.args="single path/to/image.png"
```

#### Batch Process Directory
```bash
mvn exec:java -Dexec.mainClass="com.example.ocr.OCRDemo" -Dexec.args="batch path/to/image/directory"
```

#### Interactive Demo Mode
```bash
mvn exec:java -Dexec.mainClass="com.example.ocr.OCRDemo"
```

### Programmatic Usage

#### Basic OCR Processing
```java
import com.example.ocr.ImageOCRProcessor;

// Initialize processor
ImageOCRProcessor processor = new ImageOCRProcessor();

// Process single image
try {
    String extractedText = processor.processImage("path/to/image.png");
    System.out.println("Extracted text: " + extractedText);
} catch (Exception e) {
    e.printStackTrace();
} finally {
    processor.cleanup();
}
```

#### Batch Processing
```java
import com.example.ocr.ImageOCRProcessor;
import java.util.Arrays;
import java.util.List;

ImageOCRProcessor processor = new ImageOCRProcessor();

List<String> imagePaths = Arrays.asList(
    "image1.png", 
    "image2.jpg", 
    "image3.png"
);

List<ImageOCRProcessor.OCRResult> results = processor.batchProcess(imagePaths);

for (ImageOCRProcessor.OCRResult result : results) {
    if (result.isSuccess()) {
        System.out.println("File: " + result.getImagePath());
        System.out.println("Text: " + result.getExtractedText());
    } else {
        System.err.println("Failed: " + result.getImagePath() + 
                          " - " + result.getErrorMessage());
    }
}

processor.cleanup();
```

#### Custom Preprocessing Parameters
```java
ImageOCRProcessor processor = new ImageOCRProcessor();

// Process with custom contrast and brightness
String text = processor.processImageWithCustomParams(
    "image.png", 
    2.0,  // contrast alpha
    50    // brightness beta
);

// Or update global configuration
ImageOCRProcessor.OCRConfig config = new ImageOCRProcessor.OCRConfig(
    1.8,  // contrast alpha
    40,   // brightness beta  
    5,    // gaussian kernel size
    120   // threshold value
);
processor.updateConfiguration(config);
```

#### Text Region Detection
```java
import java.awt.image.BufferedImage;
import java.util.List;

ImageOCRProcessor processor = new ImageOCRProcessor();

List<BufferedImage> textRegions = processor.detectTextRegions("image.png");
System.out.println("Detected " + textRegions.size() + " text regions");

// Process each region separately
for (int i = 0; i < textRegions.size(); i++) {
    try {
        String regionText = processor.tesseract.doOCR(textRegions.get(i));
        System.out.println("Region " + i + ": " + regionText);
    } catch (Exception e) {
        System.err.println("Failed to process region " + i);
    }
}
```

## Configuration

### OCR Parameters
The processor supports various configuration parameters:

| Parameter | Default | Description |
|-----------|---------|-------------|
| `contrastAlpha` | 1.5 | Contrast enhancement factor (1.0 = no change) |
| `brightnessBeta` | 30 | Brightness adjustment (-100 to 100) |
| `gaussianKernelSize` | 3 | Gaussian blur kernel size (odd numbers only) |
| `thresholdValue` | 128 | Binary threshold value (0-255) |

### Tesseract Settings
The processor configures Tesseract with optimal settings:

- **Language**: `chi_sim+eng` (Chinese Simplified + English)
- **OCR Engine Mode**: LSTM + Legacy (mode 1)
- **Page Segmentation**: Automatic (mode 6)
- **DPI**: 300 for better accuracy
- **Preserve Spaces**: Enabled for better formatting

## Sample Images

For testing the OCR functionality with images like the ones you provided:

1. Place your images in the `sample-images/` directory
2. Supported formats: PNG, JPG, JPEG, BMP, TIFF
3. Run the demo: `mvn exec:java -Dexec.mainClass="com.example.ocr.OCRDemo" -Dexec.args="demo"`

### Expected Results for Chinese Text Images
The processor is optimized for:
- **Museum/Historical Images**: Like the images you showed with Chinese characters
- **Mixed Text**: Both Chinese and English in the same image
- **Low Quality Images**: Noise reduction and enhancement preprocessing
- **Various Backgrounds**: Adaptive thresholding handles different backgrounds

## Troubleshooting

### Common Issues

#### 1. Tesseract Not Found
```
Error: Tesseract not found in PATH
```
**Solution**: Ensure Tesseract is installed and added to system PATH

#### 2. Language Pack Missing
```
Error: Language 'chi_sim' not found
```
**Solution**: Install Chinese language pack:
```bash
sudo apt-get install tesseract-ocr-chi-sim  # Ubuntu/Debian
brew install tesseract-lang                 # macOS
```

#### 3. OpenCV Loading Issues
```
Warning: OpenCV not properly loaded
```
**Solution**: The application will fall back to basic processing. For full features, ensure OpenCV dependencies are available.

#### 4. Memory Issues with Large Images
```
OutOfMemoryError
```
**Solution**: Increase JVM memory:
```bash
mvn exec:java -Dexec.mainClass="com.example.ocr.OCRDemo" -Dexec.args="single image.png" -Dexec.jvmArgs="-Xmx4g"
```

#### 5. Poor OCR Accuracy
**Solutions**:
- Adjust preprocessing parameters (contrast, brightness)
- Ensure image resolution is at least 300 DPI
- Check image quality and text clarity
- Try different preprocessing configurations

### Debug Mode
Enable debug logging by adding to JVM args:
```bash
-Dorg.slf4j.simpleLogger.defaultLogLevel=debug
```

## Performance Tips

1. **Image Size**: Resize very large images (>4000px) for faster processing
2. **Batch Processing**: Use batch mode for multiple images to amortize initialization costs
3. **Memory**: Allocate sufficient memory for large images or batch operations
4. **Preprocessing**: Adjust parameters based on image characteristics

## API Reference

### ImageOCRProcessor Methods

| Method | Description | Parameters | Returns |
|--------|-------------|------------|---------|
| `processImage(String)` | Process single image | imagePath | String (extracted text) |
| `processImageWithCustomParams(String, double, int)` | Process with custom parameters | imagePath, contrast, brightness | String |
| `batchProcess(List<String>)` | Process multiple images | list of image paths | List<OCRResult> |
| `detectTextRegions(String)` | Detect text regions | imagePath | List<BufferedImage> |
| `updateConfiguration(OCRConfig)` | Update preprocessing config | new configuration | void |
| `getConfiguration()` | Get current config | none | OCRConfig |
| `isReady()` | Check if processor is ready | none | boolean |
| `cleanup()` | Clean up resources | none | void |

## Contributing

1. Fork the repository
2. Create a feature branch
3. Add tests for new functionality
4. Ensure all tests pass: `mvn test`
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- [Tess4J](https://github.com/nguyenq/tess4j) - Java wrapper for Tesseract
- [OpenCV](https://opencv.org/) - Computer vision library
- [Tesseract OCR](https://github.com/tesseract-ocr/tesseract) - OCR engine

## Support

For issues and questions:
1. Check the troubleshooting section above
2. Review existing issues on GitHub
3. Create a new issue with detailed information including:
   - Java version
   - Operating system
   - Tesseract version
   - Sample image (if applicable)
   - Error messages and stack traces