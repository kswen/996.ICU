package com.example.ocr;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ImageSplitter class.
 * Compatible with Java 1.8 and JUnit 5.
 */
public class ImageSplitterTest {
    
    @TempDir
    Path tempDir;
    
    private File testImage590x360;
    private File testImageOtherSize;
    
    @BeforeEach
    void setUp() throws IOException {
        // Create a test image with 590x360 dimensions
        testImage590x360 = tempDir.resolve("test_590x360.png").toFile();
        createTestImage(testImage590x360, 590, 360);
        
        // Create a test image with different dimensions
        testImageOtherSize = tempDir.resolve("test_other_size.png").toFile();
        createTestImage(testImageOtherSize, 400, 300);
    }
    
    private void createTestImage(File file, int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        // Fill with a gradient pattern to make splitting visible
        for (int x = 0; x < width; x++) {
            int colorValue = (int) (255.0 * x / width);
            g2d.setColor(new Color(colorValue, 100, 255 - colorValue));
            g2d.drawLine(x, 0, x, height);
        }
        
        g2d.dispose();
        ImageIO.write(image, "png", file);
    }
    
    @Test
    void testCutImageIntoSixParts_ValidImage() throws IOException {
        // Arrange
        String outputDir = tempDir.resolve("output").toString();
        
        // Act
        List<File> subImages = ImageSplitter.cutImageIntoSixParts(
            testImage590x360.getAbsolutePath(), outputDir);
        
        // Assert
        assertEquals(6, subImages.size(), "Should create exactly 6 sub-images");
        
        // Verify each sub-image exists and has correct dimensions
        for (int i = 0; i < subImages.size(); i++) {
            File subImageFile = subImages.get(i);
            assertTrue(subImageFile.exists(), "Sub-image " + (i + 1) + " should exist");
            assertTrue(subImageFile.getName().startsWith("sub_image_"), 
                      "Sub-image should have correct naming pattern");
            
            BufferedImage subImage = ImageIO.read(subImageFile);
            assertNotNull(subImage, "Sub-image should be readable");
            assertEquals(360, subImage.getHeight(), "Sub-image height should be 360");
            
            // Width should be 98 for first 5 images, and 98 + (590 % 6) = 100 for the last one
            int expectedWidth = (i == 5) ? 98 + (590 % 6) : 98;
            assertEquals(expectedWidth, subImage.getWidth(), 
                        "Sub-image " + (i + 1) + " width should be " + expectedWidth);
        }
    }
    
    @Test
    void testCutImageIntoSixParts_InvalidDimensions() {
        // Arrange
        String outputDir = tempDir.resolve("output").toString();
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ImageSplitter.cutImageIntoSixParts(testImageOtherSize.getAbsolutePath(), outputDir),
            "Should throw IllegalArgumentException for wrong dimensions"
        );
        
        assertTrue(exception.getMessage().contains("Expected image dimensions 590x360"),
                  "Exception message should mention expected dimensions");
    }
    
    @Test
    void testCutImageIntoSixParts_NonExistentFile() {
        // Arrange
        String nonExistentFile = tempDir.resolve("nonexistent.png").toString();
        String outputDir = tempDir.resolve("output").toString();
        
        // Act & Assert
        assertThrows(IOException.class,
            () -> ImageSplitter.cutImageIntoSixParts(nonExistentFile, outputDir),
            "Should throw IOException for non-existent file");
    }
    
    @Test
    void testCutImageIntoParts_GenericMethod() throws IOException {
        // Arrange
        String outputDir = tempDir.resolve("output_generic").toString();
        int numberOfParts = 3;
        
        // Act
        List<File> subImages = ImageSplitter.cutImageIntoParts(
            testImage590x360.getAbsolutePath(), outputDir, numberOfParts);
        
        // Assert
        assertEquals(numberOfParts, subImages.size(), 
                    "Should create exactly " + numberOfParts + " sub-images");
        
        // Verify dimensions
        for (int i = 0; i < subImages.size(); i++) {
            File subImageFile = subImages.get(i);
            BufferedImage subImage = ImageIO.read(subImageFile);
            assertEquals(360, subImage.getHeight(), "Sub-image height should be 360");
            
            int expectedWidth = (i == numberOfParts - 1) ? 590 / numberOfParts + (590 % numberOfParts) 
                                                         : 590 / numberOfParts;
            assertEquals(expectedWidth, subImage.getWidth(), 
                        "Sub-image " + (i + 1) + " width should be " + expectedWidth);
        }
    }
    
    @Test
    void testCutImageIntoParts_InvalidNumberOfParts() {
        // Arrange
        String outputDir = tempDir.resolve("output").toString();
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
            () -> ImageSplitter.cutImageIntoParts(testImage590x360.getAbsolutePath(), outputDir, 0),
            "Should throw IllegalArgumentException for 0 parts");
        
        assertThrows(IllegalArgumentException.class,
            () -> ImageSplitter.cutImageIntoParts(testImage590x360.getAbsolutePath(), outputDir, -1),
            "Should throw IllegalArgumentException for negative parts");
    }
    
    @Test
    void testOutputDirectoryCreation() throws IOException {
        // Arrange
        String nonExistentOutputDir = tempDir.resolve("new_directory").toString();
        File outputDirFile = new File(nonExistentOutputDir);
        assertFalse(outputDirFile.exists(), "Output directory should not exist initially");
        
        // Act
        ImageSplitter.cutImageIntoSixParts(testImage590x360.getAbsolutePath(), nonExistentOutputDir);
        
        // Assert
        assertTrue(outputDirFile.exists(), "Output directory should be created");
        assertTrue(outputDirFile.isDirectory(), "Output path should be a directory");
    }
}