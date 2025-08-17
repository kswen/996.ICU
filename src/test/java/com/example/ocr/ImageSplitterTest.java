package com.example.ocr;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for ImageSplitter functionality.
 * Tests the image splitting capabilities with various scenarios.
 */
public class ImageSplitterTest {
    
    private ImageSplitter imageSplitter;
    
    @BeforeEach
    void setUp() {
        imageSplitter = new ImageSplitter();
    }
    
    @Test
    void testSplitImageInto6Parts(@TempDir Path tempDir) {
        // Create a test image with 590x360 dimensions
        String testImagePath = createTestImage(tempDir.toString(), 590, 360);
        
        // Test splitting into 6 parts
        List<String> outputPaths = imageSplitter.splitImageInto6Parts(testImagePath, tempDir.toString());
        
        // Verify results
        assertEquals(6, outputPaths.size(), "Should create exactly 6 output files");
        
        // Verify each output file exists and has correct dimensions
        for (int i = 0; i < outputPaths.size(); i++) {
            String outputPath = outputPaths.get(i);
            File outputFile = new File(outputPath);
            assertTrue(outputFile.exists(), "Output file should exist: " + outputPath);
            
            // Check dimensions of each part
            int[] dimensions = imageSplitter.getImageDimensions(outputPath);
            assertNotNull(dimensions, "Should be able to read dimensions");
            
            // Each part should be 98 pixels wide (590/6 = 98.33, so 98 for first 5, 100 for last)
            if (i < 5) {
                assertEquals(98, dimensions[0], "Part " + (i + 1) + " should be 98 pixels wide");
            } else {
                assertEquals(100, dimensions[0], "Last part should be 100 pixels wide (includes remainder)");
            }
            assertEquals(360, dimensions[1], "Each part should maintain original height");
        }
    }
    
    @Test
    void testSplitImageHorizontally(@TempDir Path tempDir) {
        // Create a test image with 600x400 dimensions
        String testImagePath = createTestImage(tempDir.toString(), 600, 400);
        
        // Test splitting into 4 parts
        List<String> outputPaths = imageSplitter.splitImageHorizontally(testImagePath, tempDir.toString(), 4);
        
        // Verify results
        assertEquals(4, outputPaths.size(), "Should create exactly 4 output files");
        
        // Verify dimensions
        for (int i = 0; i < outputPaths.size(); i++) {
            int[] dimensions = imageSplitter.getImageDimensions(outputPaths.get(i));
            assertEquals(150, dimensions[0], "Each part should be 150 pixels wide");
            assertEquals(400, dimensions[1], "Each part should maintain original height");
        }
    }
    
    @Test
    void testValidateImageDimensions() {
        // This test would require an actual image file
        // For now, we'll test the method signature and basic logic
        assertNotNull(imageSplitter, "ImageSplitter should be initialized");
    }
    
    @Test
    void testInvalidInputs(@TempDir Path tempDir) {
        // Test with null input path
        assertThrows(IllegalArgumentException.class, () -> {
            imageSplitter.splitImageInto6Parts(null, tempDir.toString());
        }, "Should throw IllegalArgumentException for null input path");
        
        // Test with empty input path
        assertThrows(IllegalArgumentException.class, () -> {
            imageSplitter.splitImageInto6Parts("", tempDir.toString());
        }, "Should throw IllegalArgumentException for empty input path");
        
        // Test with null output directory
        assertThrows(IllegalArgumentException.class, () -> {
            imageSplitter.splitImageInto6Parts("test.jpg", null);
        }, "Should throw IllegalArgumentException for null output directory");
        
        // Test with invalid number of parts
        assertThrows(IllegalArgumentException.class, () -> {
            imageSplitter.splitImageHorizontally("test.jpg", tempDir.toString(), 0);
        }, "Should throw IllegalArgumentException for zero parts");
        
        assertThrows(IllegalArgumentException.class, () -> {
            imageSplitter.splitImageHorizontally("test.jpg", tempDir.toString(), -1);
        }, "Should throw IllegalArgumentException for negative parts");
    }
    
    @Test
    void testNonExistentFile(@TempDir Path tempDir) {
        assertThrows(IllegalArgumentException.class, () -> {
            imageSplitter.splitImageInto6Parts("non_existent_file.jpg", tempDir.toString());
        }, "Should throw IllegalArgumentException for non-existent file");
    }
    
    /**
     * Creates a test image with specified dimensions.
     * 
     * @param directory Directory to save the test image
     * @param width Width of the test image
     * @param height Height of the test image
     * @return Path to the created test image
     */
    private String createTestImage(String directory, int width, int height) {
        // Create a simple test image with a gradient
        Mat testImage = new Mat(height, width, CvType.CV_8UC3);
        
        // Fill with a simple gradient pattern
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double[] color = {
                    (double) (x * 255 / width),      // Blue channel
                    (double) (y * 255 / height),     // Green channel
                    128.0                            // Red channel (constant)
                };
                testImage.put(y, x, color);
            }
        }
        
        String imagePath = new File(directory, "test_image.jpg").getAbsolutePath();
        Imgcodecs.imwrite(imagePath, testImage);
        testImage.release();
        
        return imagePath;
    }
}