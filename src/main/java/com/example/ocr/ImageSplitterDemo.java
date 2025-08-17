package com.example.ocr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Demo class showing how to use the ImageSplitter to cut a 590x360 picture into 6 equal sub-pictures.
 * 
 * This demo demonstrates:
 * - Basic usage of the splitImageInto6Parts method
 * - Validation of image dimensions
 * - Error handling
 * - Output file management
 */
public class ImageSplitterDemo {
    
    private static final Logger logger = LoggerFactory.getLogger(ImageSplitterDemo.class);
    
    public static void main(String[] args) {
        ImageSplitterDemo demo = new ImageSplitterDemo();
        
        // Example usage with a 590x360 image
        demo.demonstrateImageSplitting();
    }
    
    /**
     * Demonstrates how to split a 590x360 image into 6 equal parts.
     */
    public void demonstrateImageSplitting() {
        try {
            // Create an instance of ImageSplitter
            ImageSplitter splitter = new ImageSplitter();
            
            // Example file paths (you would replace these with actual paths)
            String inputImagePath = "input_image.jpg";  // Your 590x360 image
            String outputDirectory = "output_parts";    // Directory for output files
            
            logger.info("Starting image splitting demonstration...");
            
            // Validate that the image has the expected dimensions (590x360)
            if (splitter.validateImageDimensions(inputImagePath, 590, 360)) {
                logger.info("Image dimensions validated: 590x360 pixels");
                
                // Split the image into 6 equal parts
                List<String> outputPaths = splitter.splitImageInto6Parts(inputImagePath, outputDirectory);
                
                // Display results
                logger.info("Successfully created {} sub-images:", outputPaths.size());
                for (int i = 0; i < outputPaths.size(); i++) {
                    logger.info("Part {}: {}", i + 1, outputPaths.get(i));
                }
                
                // Show expected dimensions for each part
                logger.info("Expected dimensions for each part: approximately 98x360 pixels");
                logger.info("Note: The last part may be slightly wider to account for remainder pixels");
                
            } else {
                logger.error("Image does not have the expected dimensions of 590x360 pixels");
                
                // Get actual dimensions for debugging
                int[] dimensions = splitter.getImageDimensions(inputImagePath);
                if (dimensions != null) {
                    logger.info("Actual image dimensions: {}x{} pixels", dimensions[0], dimensions[1]);
                } else {
                    logger.error("Could not read image dimensions");
                }
            }
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid input parameters: {}", e.getMessage());
        } catch (RuntimeException e) {
            logger.error("Error during image processing: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Demonstrates splitting with custom number of parts.
     */
    public void demonstrateCustomSplitting() {
        try {
            ImageSplitter splitter = new ImageSplitter();
            
            String inputImagePath = "input_image.jpg";
            String outputDirectory = "output_custom_parts";
            int numberOfParts = 4;  // Split into 4 parts instead of 6
            
            logger.info("Splitting image into {} parts...", numberOfParts);
            
            List<String> outputPaths = splitter.splitImageHorizontally(
                inputImagePath, outputDirectory, numberOfParts);
            
            logger.info("Created {} parts successfully", outputPaths.size());
            
        } catch (Exception e) {
            logger.error("Error in custom splitting: {}", e.getMessage());
        }
    }
    
    /**
     * Demonstrates error handling with invalid inputs.
     */
    public void demonstrateErrorHandling() {
        ImageSplitter splitter = new ImageSplitter();
        
        // Test with non-existent file
        try {
            splitter.splitImageInto6Parts("non_existent_file.jpg", "output");
        } catch (IllegalArgumentException e) {
            logger.info("Caught expected error for non-existent file: {}", e.getMessage());
        }
        
        // Test with invalid number of parts
        try {
            splitter.splitImageHorizontally("input_image.jpg", "output", 0);
        } catch (IllegalArgumentException e) {
            logger.info("Caught expected error for invalid number of parts: {}", e.getMessage());
        }
        
        // Test with null parameters
        try {
            splitter.splitImageInto6Parts(null, "output");
        } catch (IllegalArgumentException e) {
            logger.info("Caught expected error for null input path: {}", e.getMessage());
        }
    }
}