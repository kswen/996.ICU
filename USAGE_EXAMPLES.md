# OCR Usage Examples

This document provides comprehensive examples of how to use the Image OCR Processor for various scenarios, particularly for processing images with Chinese text like the ones you provided.

## Quick Start

### 1. Basic OCR Processing

```java
import com.example.ocr.ImageOCRProcessor;

public class BasicOCRExample {
    public static void main(String[] args) {
        ImageOCRProcessor processor = new ImageOCRProcessor();
        
        try {
            // Process a single image
            String text = processor.processImage("path/to/your/image.png");
            System.out.println("Extracted text:");
            System.out.println(text);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            processor.cleanup();
        }
    }
}
```

### 2. Processing Images Similar to Your Examples

For images like the museum/historical artifacts with Chinese characters:

```java
import com.example.ocr.ImageOCRProcessor;
import com.example.ocr.ImageOCRProcessor.OCRConfig;

public class ChineseTextOCR {
    public static void main(String[] args) {
        ImageOCRProcessor processor = new ImageOCRProcessor();
        
        // Optimize settings for Chinese text with potentially noisy backgrounds
        OCRConfig config = new OCRConfig(
            2.0,  // Higher contrast for better text clarity
            40,   // Brightness adjustment
            5,    // Larger Gaussian kernel for noise reduction
            120   // Lower threshold for better text extraction
        );
        processor.updateConfiguration(config);
        
        try {
            String text = processor.processImage("museum_artifact_image.png");
            System.out.println("Extracted Chinese text:");
            System.out.println(text);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            processor.cleanup();
        }
    }
}
```

## Command Line Usage

### Process Single Image
```bash
# Basic processing
mvn exec:java -Dexec.mainClass="com.example.ocr.OCRDemo" -Dexec.args="single sample-images/chinese_text.png"

# Interactive mode (recommended for testing)
mvn exec:java -Dexec.mainClass="com.example.ocr.OCRDemo"
```

### Batch Processing
```bash
# Process all images in a directory
mvn exec:java -Dexec.mainClass="com.example.ocr.OCRDemo" -Dexec.args="batch sample-images/"
```

## Advanced Usage Examples

### 1. Custom Parameter Processing

```java
import com.example.ocr.ImageOCRProcessor;

public class CustomParameterExample {
    public static void main(String[] args) {
        ImageOCRProcessor processor = new ImageOCRProcessor();
        
        try {
            // Try different parameter combinations for optimal results
            String[] imagePaths = {
                "dark_background_image.png",
                "light_background_image.png", 
                "noisy_image.png"
            };
            
            // Different parameter sets for different image types
            double[][] paramSets = {
                {1.8, 50},  // For dark backgrounds
                {1.2, 20},  // For light backgrounds  
                {2.5, 60}   // For noisy images
            };
            
            for (int i = 0; i < imagePaths.length; i++) {
                System.out.println("Processing: " + imagePaths[i]);
                String text = processor.processImageWithCustomParams(
                    imagePaths[i], 
                    paramSets[i][0], 
                    (int)paramSets[i][1]
                );
                System.out.println("Result: " + text.trim());
                System.out.println("---");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            processor.cleanup();
        }
    }
}
```

### 2. Batch Processing with Results Analysis

```java
import com.example.ocr.ImageOCRProcessor;
import com.example.ocr.ImageOCRProcessor.OCRResult;
import java.util.Arrays;
import java.util.List;

public class BatchProcessingExample {
    public static void main(String[] args) {
        ImageOCRProcessor processor = new ImageOCRProcessor();
        
        List<String> imagePaths = Arrays.asList(
            "image1.png",
            "image2.jpg", 
            "image3.png"
        );
        
        List<OCRResult> results = processor.batchProcess(imagePaths);
        
        // Analyze results
        int successCount = 0;
        int failureCount = 0;
        
        for (OCRResult result : results) {
            if (result.isSuccess()) {
                successCount++;
                System.out.println("✓ " + result.getImagePath());
                System.out.println("  Text: " + result.getExtractedText().substring(0, 
                    Math.min(100, result.getExtractedText().length())) + "...");
            } else {
                failureCount++;
                System.out.println("✗ " + result.getImagePath() + " - " + result.getErrorMessage());
            }
        }
        
        System.out.println("\nSummary: " + successCount + " successful, " + failureCount + " failed");
        processor.cleanup();
    }
}
```

### 3. Text Region Detection and Processing

```java
import com.example.ocr.ImageOCRProcessor;
import java.awt.image.BufferedImage;
import java.util.List;

public class TextRegionExample {
    public static void main(String[] args) {
        ImageOCRProcessor processor = new ImageOCRProcessor();
        
        try {
            // Detect text regions in the image
            List<BufferedImage> regions = processor.detectTextRegions("complex_image.png");
            
            System.out.println("Detected " + regions.size() + " text regions");
            
            // Process each region separately for better accuracy
            for (int i = 0; i < regions.size(); i++) {
                try {
                    String regionText = processor.tesseract.doOCR(regions.get(i));
                    if (!regionText.trim().isEmpty()) {
                        System.out.println("Region " + (i + 1) + ":");
                        System.out.println("  " + regionText.trim());
                    }
                } catch (Exception e) {
                    System.out.println("Region " + (i + 1) + ": Failed to process");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            processor.cleanup();
        }
    }
}
```

## Optimizing for Different Image Types

### For Museum/Historical Images (like your examples)

```java
// Configuration optimized for historical artifacts with Chinese text
OCRConfig museumConfig = new OCRConfig(
    2.2,    // High contrast for aged/faded text
    45,     // Moderate brightness boost
    5,      // Noise reduction for old images
    110     // Lower threshold for better text capture
);
processor.updateConfiguration(museumConfig);
```

### For Screenshots/Digital Text

```java
// Configuration for clean digital text
OCRConfig digitalConfig = new OCRConfig(
    1.3,    // Minimal contrast adjustment
    10,     // Slight brightness adjustment
    3,      // Minimal noise reduction
    140     // Higher threshold for clean text
);
processor.updateConfiguration(digitalConfig);
```

### For Handwritten Text

```java
// Configuration for handwritten Chinese characters
OCRConfig handwrittenConfig = new OCRConfig(
    1.8,    // Moderate contrast
    30,     // Standard brightness
    3,      // Minimal blur to preserve character details
    125     // Balanced threshold
);
processor.updateConfiguration(handwrittenConfig);
```

## Testing with Your Sample Images

1. Place your images in the `sample-images/` directory
2. Run the interactive demo:
   ```bash
   mvn exec:java -Dexec.mainClass="com.example.ocr.OCRDemo"
   ```
3. Choose option 6 for "Run Demo with Sample Images"

## Expected Results

For images similar to the ones you provided:

### Museum Artifact Image 1:
- **Expected**: Recognition of Chinese characters visible in the display case
- **Optimization**: Use museum configuration with high contrast
- **Preprocessing**: Noise reduction will help with reflections and lighting

### Museum Artifact Image 2:  
- **Expected**: Recognition of Chinese characters on the statue/artifact
- **Optimization**: May need custom brightness adjustment for the greenish lighting
- **Preprocessing**: Adaptive thresholding will help separate text from background

## Troubleshooting Common Issues

### Low Accuracy Results
```java
// Try increasing contrast and adjusting brightness
String text = processor.processImageWithCustomParams("image.png", 2.5, 60);
```

### Text Not Detected
```java
// Use text region detection first
List<BufferedImage> regions = processor.detectTextRegions("image.png");
// Then process regions individually
```

### Mixed Languages
The processor is already configured for Chinese + English:
```java
// Language setting in initialization (already set)
tesseract.setLanguage("chi_sim+eng");
```

## Performance Tips

1. **Image Size**: Resize very large images (>4000px) before processing
2. **Batch Processing**: Use batch mode for multiple images
3. **Memory**: Increase JVM memory for large images:
   ```bash
   mvn exec:java -Dexec.mainClass="com.example.ocr.OCRDemo" -Dexec.jvmArgs="-Xmx4g"
   ```

## Integration Example

```java
import com.example.ocr.ImageOCRProcessor;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OCRIntegration {
    private ImageOCRProcessor processor;
    
    public OCRIntegration() {
        this.processor = new ImageOCRProcessor();
        // Configure for your specific use case
        optimizeForChineseText();
    }
    
    private void optimizeForChineseText() {
        ImageOCRProcessor.OCRConfig config = new ImageOCRProcessor.OCRConfig(
            2.0, 40, 5, 120
        );
        processor.updateConfiguration(config);
    }
    
    public List<String> processImageDirectory(String directoryPath) {
        List<String> results = new ArrayList<>();
        File directory = new File(directoryPath);
        
        if (directory.exists() && directory.isDirectory()) {
            File[] imageFiles = directory.listFiles((dir, name) -> 
                name.toLowerCase().matches(".*\\.(png|jpg|jpeg|bmp|tiff)$"));
                
            if (imageFiles != null) {
                for (File imageFile : imageFiles) {
                    try {
                        String text = processor.processImage(imageFile.getAbsolutePath());
                        results.add(imageFile.getName() + ": " + text.trim());
                    } catch (Exception e) {
                        results.add(imageFile.getName() + ": ERROR - " + e.getMessage());
                    }
                }
            }
        }
        
        return results;
    }
    
    public void cleanup() {
        processor.cleanup();
    }
}
```

This integration class can be used in larger applications to process images with Chinese text efficiently.