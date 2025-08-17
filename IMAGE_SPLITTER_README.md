# Image Splitter for Java 1.8

This project provides a Java utility class for splitting images into equal sub-pictures based on width. The main functionality is designed to cut a 590x360 picture into 6 equal sub-pictures.

## Features

- **Horizontal Image Splitting**: Split images into equal horizontal segments
- **Java 1.8 Compatible**: Written for Java 1.8 as requested
- **OpenCV Integration**: Uses OpenCV for efficient image processing
- **Flexible Configuration**: Support for custom number of splits
- **Error Handling**: Comprehensive input validation and error handling
- **Memory Management**: Proper cleanup of OpenCV resources

## Requirements

- Java 1.8
- Maven 3.6+
- OpenCV 4.9.0 (automatically managed via Maven)

## Quick Start

### Basic Usage

```java
import com.example.ocr.ImageSplitter;
import java.util.List;

public class Example {
    public static void main(String[] args) {
        // Create an instance of ImageSplitter
        ImageSplitter splitter = new ImageSplitter();
        
        // Split a 590x360 image into 6 equal parts
        String inputImagePath = "path/to/your/590x360_image.jpg";
        String outputDirectory = "path/to/output/directory";
        
        List<String> outputPaths = splitter.splitImageInto6Parts(inputImagePath, outputDirectory);
        
        // Print the paths of generated sub-images
        for (String path : outputPaths) {
            System.out.println("Generated: " + path);
        }
    }
}
```

### Expected Output

For a 590x360 input image split into 6 parts:
- **Part 1**: 98x360 pixels
- **Part 2**: 98x360 pixels  
- **Part 3**: 98x360 pixels
- **Part 4**: 98x360 pixels
- **Part 5**: 98x360 pixels
- **Part 6**: 100x360 pixels (includes remainder pixels)

### Custom Number of Parts

```java
// Split into 4 parts instead of 6
List<String> outputPaths = splitter.splitImageHorizontally(
    inputImagePath, outputDirectory, 4);
```

## API Reference

### Main Methods

#### `splitImageInto6Parts(String inputImagePath, String outputDirectory)`
Splits a 590x360 image into 6 equal sub-pictures.

**Parameters:**
- `inputImagePath` - Path to the input image file
- `outputDirectory` - Directory where sub-pictures will be saved

**Returns:** List of paths to the generated sub-picture files

**Throws:**
- `IllegalArgumentException` - If input parameters are invalid
- `RuntimeException` - If image processing fails

#### `splitImageHorizontally(String inputImagePath, String outputDirectory, int numberOfParts)`
Splits an image horizontally into a specified number of equal parts.

**Parameters:**
- `inputImagePath` - Path to the input image file
- `outputDirectory` - Directory where sub-pictures will be saved
- `numberOfParts` - Number of parts to split the image into

**Returns:** List of paths to the generated sub-picture files

### Utility Methods

#### `validateImageDimensions(String imagePath, int expectedWidth, int expectedHeight)`
Validates if an image has the expected dimensions.

#### `getImageDimensions(String imagePath)`
Gets the dimensions of an image.

**Returns:** Array containing [width, height] or null if image cannot be read

## Building and Running

### Compile the Project

```bash
mvn clean compile
```

### Run the Demo

```bash
mvn exec:java -Dexec.mainClass="com.example.ocr.ImageSplitterDemo"
```

### Run Tests

```bash
mvn test
```

## Error Handling

The ImageSplitter class includes comprehensive error handling:

- **Invalid Input Parameters**: Throws `IllegalArgumentException` for null/empty paths or invalid number of parts
- **File Not Found**: Throws `IllegalArgumentException` if input file doesn't exist
- **Image Processing Errors**: Throws `RuntimeException` if OpenCV operations fail
- **Directory Creation**: Automatically creates output directory if it doesn't exist

## Example Error Scenarios

```java
// These will throw IllegalArgumentException
splitter.splitImageInto6Parts(null, "output");           // Null input path
splitter.splitImageInto6Parts("", "output");             // Empty input path
splitter.splitImageInto6Parts("image.jpg", null);        // Null output directory
splitter.splitImageInto6Parts("nonexistent.jpg", "output"); // File doesn't exist

// This will throw IllegalArgumentException
splitter.splitImageHorizontally("image.jpg", "output", 0); // Invalid number of parts
```

## Memory Management

The ImageSplitter properly manages OpenCV resources:
- Automatically releases Mat objects after use
- Prevents memory leaks in long-running applications
- Uses try-with-resources pattern internally

## Performance Considerations

- **Large Images**: The implementation is optimized for images up to several megapixels
- **Memory Usage**: Each sub-image is processed independently to minimize memory footprint
- **File I/O**: Images are saved in JPEG format for good compression and compatibility

## Troubleshooting

### Common Issues

1. **OpenCV Loading Error**: Ensure the OpenCV native library is available
2. **File Permission Error**: Check write permissions for the output directory
3. **Memory Error**: For very large images, consider processing in smaller batches

### Debug Information

The class uses SLF4J logging. Enable debug logging to see detailed processing information:

```java
// Set logging level to DEBUG for detailed output
System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "DEBUG");
```

## License

This project is part of the existing OCR project and follows the same licensing terms.