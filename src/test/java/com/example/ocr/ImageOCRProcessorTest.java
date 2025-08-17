package com.example.ocr;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for ImageOCRProcessor
 */
public class ImageOCRProcessorTest {
    
    private ImageOCRProcessor processor;
    
    @BeforeEach
    void setUp() {
        processor = new ImageOCRProcessor();
    }
    
    @AfterEach
    void tearDown() {
        if (processor != null) {
            processor.cleanup();
        }
    }
    
    @Test
    void testProcessorInitialization() {
        assertNotNull(processor, "Processor should be initialized");
        assertTrue(processor.isReady(), "Processor should be ready");
    }
    
    @Test
    void testGetConfiguration() {
        ImageOCRProcessor.OCRConfig config = processor.getConfiguration();
        assertNotNull(config, "Configuration should not be null");
        assertTrue(config.getContrastAlpha() > 0, "Contrast alpha should be positive");
        assertTrue(config.getBrightnessBeta() >= 0, "Brightness beta should be non-negative");
        assertTrue(config.getGaussianKernelSize() > 0, "Gaussian kernel size should be positive");
        assertTrue(config.getThresholdValue() >= 0, "Threshold value should be non-negative");
    }
    
    @Test
    void testUpdateConfiguration() {
        ImageOCRProcessor.OCRConfig newConfig = new ImageOCRProcessor.OCRConfig(2.0, 50, 5, 150);
        processor.updateConfiguration(newConfig);
        
        ImageOCRProcessor.OCRConfig updatedConfig = processor.getConfiguration();
        assertEquals(2.0, updatedConfig.getContrastAlpha(), 0.01, "Contrast alpha should be updated");
        assertEquals(50, updatedConfig.getBrightnessBeta(), "Brightness beta should be updated");
        assertEquals(5, updatedConfig.getGaussianKernelSize(), "Gaussian kernel size should be updated");
        assertEquals(150, updatedConfig.getThresholdValue(), 0.01, "Threshold value should be updated");
    }
    
    @Test
    void testProcessNonExistentImage() {
        assertThrows(IOException.class, () -> {
            processor.processImage("non_existent_image.png");
        }, "Should throw IOException for non-existent image");
    }
    
    @Test
    void testBatchProcessEmptyList() {
        List<String> emptyList = Arrays.asList();
        List<ImageOCRProcessor.OCRResult> results = processor.batchProcess(emptyList);
        
        assertNotNull(results, "Results should not be null");
        assertTrue(results.isEmpty(), "Results should be empty for empty input");
    }
    
    @Test
    void testBatchProcessWithNonExistentImages() {
        List<String> imagePaths = Arrays.asList("fake1.png", "fake2.jpg");
        List<ImageOCRProcessor.OCRResult> results = processor.batchProcess(imagePaths);
        
        assertNotNull(results, "Results should not be null");
        assertEquals(2, results.size(), "Should have 2 results");
        
        for (ImageOCRProcessor.OCRResult result : results) {
            assertFalse(result.isSuccess(), "All results should be failures");
            assertNotNull(result.getErrorMessage(), "Error message should be provided");
            assertNull(result.getExtractedText(), "Extracted text should be null for failed results");
        }
    }
    
    @Test
    void testDetectTextRegionsWithNonExistentImage() {
        List<?> regions = processor.detectTextRegions("non_existent_image.png");
        assertNotNull(regions, "Text regions list should not be null");
        assertTrue(regions.isEmpty(), "Text regions should be empty for non-existent image");
    }
    
    @Test
    void testVersionInfo() {
        String versionInfo = processor.getVersionInfo();
        assertNotNull(versionInfo, "Version info should not be null");
        assertTrue(versionInfo.contains("ImageOCRProcessor"), "Version info should contain processor name");
        assertTrue(versionInfo.contains("OpenCV Status"), "Version info should contain OpenCV status");
        assertTrue(versionInfo.contains("Tesseract Status"), "Version info should contain Tesseract status");
    }
    
    @Test
    void testOCRResultClass() {
        // Test successful result
        ImageOCRProcessor.OCRResult successResult = new ImageOCRProcessor.OCRResult(
            "test.png", "extracted text", true, null
        );
        
        assertEquals("test.png", successResult.getImagePath());
        assertEquals("extracted text", successResult.getExtractedText());
        assertTrue(successResult.isSuccess());
        assertNull(successResult.getErrorMessage());
        
        // Test failed result
        ImageOCRProcessor.OCRResult failResult = new ImageOCRProcessor.OCRResult(
            "test.png", null, false, "Test error"
        );
        
        assertEquals("test.png", failResult.getImagePath());
        assertNull(failResult.getExtractedText());
        assertFalse(failResult.isSuccess());
        assertEquals("Test error", failResult.getErrorMessage());
        
        // Test toString method
        String resultString = successResult.toString();
        assertTrue(resultString.contains("test.png"));
        assertTrue(resultString.contains("success=true"));
    }
    
    @Test
    void testOCRConfigClass() {
        ImageOCRProcessor.OCRConfig config = new ImageOCRProcessor.OCRConfig(1.5, 30, 3, 128);
        
        assertEquals(1.5, config.getContrastAlpha(), 0.01);
        assertEquals(30, config.getBrightnessBeta());
        assertEquals(3, config.getGaussianKernelSize());
        assertEquals(128, config.getThresholdValue(), 0.01);
        
        // Test setters
        config.setContrastAlpha(2.0);
        config.setBrightnessBeta(40);
        config.setGaussianKernelSize(5);
        config.setThresholdValue(150);
        
        assertEquals(2.0, config.getContrastAlpha(), 0.01);
        assertEquals(40, config.getBrightnessBeta());
        assertEquals(5, config.getGaussianKernelSize());
        assertEquals(150, config.getThresholdValue(), 0.01);
        
        // Test toString method
        String configString = config.toString();
        assertTrue(configString.contains("contrast=2.00"));
        assertTrue(configString.contains("brightness=40"));
        assertTrue(configString.contains("gaussianKernel=5"));
        assertTrue(configString.contains("threshold=150.00"));
    }
    
    @Test
    void testProcessorCleanup() {
        // This test ensures cleanup doesn't throw exceptions
        assertDoesNotThrow(() -> {
            processor.cleanup();
        }, "Cleanup should not throw exceptions");
        
        // After cleanup, processor should still be functional for basic operations
        assertNotNull(processor.getConfiguration(), "Should still be able to get configuration after cleanup");
    }
}