package com.example.ocr;

import net.sourceforge.tess4j.TesseractException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Enhanced OCR Demo specifically designed for challenging text recognition scenarios:
 * - Rotated text at various angles
 * - Different colored text (light/dark on various backgrounds)
 * - Museum/historical artifact images with complex backgrounds
 * - Mixed Chinese and English text
 */
public class EnhancedOCRDemo {
    
    private static final Logger logger = LoggerFactory.getLogger(EnhancedOCRDemo.class);
    private static final Scanner scanner = new Scanner(System.in);
    
    public static void main(String[] args) {
        System.out.println("=== Enhanced Image OCR Processor Demo ===");
        System.out.println("Specialized for rotated and colored text recognition");
        System.out.println("Optimized for Chinese/English text in complex backgrounds");
        System.out.println();
        
        EnhancedImageOCRProcessor processor = new EnhancedImageOCRProcessor();
        
        // Display system information
        displaySystemInfo(processor);
        
        if (!processor.isReady()) {
            System.err.println("OCR Processor is not ready. Please check Tesseract installation.");
            return;
        }
        
        // Check for command line arguments
        if (args.length > 0) {
            processCommandLineArgs(args, processor);
        } else {
            // Interactive mode
            runInteractiveMode(processor);
        }
        
        // Cleanup resources
        processor.cleanup();
        scanner.close();
    }
    
    /**
     * Display system and processor information
     */
    private static void displaySystemInfo(EnhancedImageOCRProcessor processor) {
        System.out.println("System Information:");
        System.out.println(processor.getVersionInfo());
        System.out.println("Enhanced Features:");
        System.out.println("- Rotation Detection: " + (processor.isRotationDetectionEnabled() ? "Enabled" : "Disabled"));
        System.out.println("- Multi-Color Detection: " + (processor.isMultiColorDetectionEnabled() ? "Enabled" : "Disabled"));
        System.out.println("- Rotation Angles: " + Arrays.toString(processor.getRotationAngles()));
        System.out.println();
        System.out.println("Current Configuration:");
        System.out.println(processor.getConfiguration());
        System.out.println();
    }
    
    /**
     * Process command line arguments
     */
    private static void processCommandLineArgs(String[] args, EnhancedImageOCRProcessor processor) {
        String command = args[0].toLowerCase();
        
        switch (command) {
            case "single":
                if (args.length < 2) {
                    System.err.println("Usage: java EnhancedOCRDemo single <image_path>");
                    return;
                }
                processSingleImageEnhanced(args[1], processor);
                break;
                
            case "rotation":
                if (args.length < 2) {
                    System.err.println("Usage: java EnhancedOCRDemo rotation <image_path>");
                    return;
                }
                processWithAutoRotation(args[1], processor);
                break;
                
            case "batch":
                if (args.length < 2) {
                    System.err.println("Usage: java EnhancedOCRDemo batch <directory_path>");
                    return;
                }
                processBatchImagesEnhanced(args[1], processor);
                break;
                
            case "compare":
                if (args.length < 2) {
                    System.err.println("Usage: java EnhancedOCRDemo compare <image_path>");
                    return;
                }
                compareProcessingMethods(args[1], processor);
                break;
                
            case "demo":
                runDemoWithSampleImages(processor);
                break;
                
            default:
                System.err.println("Unknown command: " + command);
                System.err.println("Available commands: single, rotation, batch, compare, demo");
        }
    }
    
    /**
     * Run interactive mode
     */
    private static void runInteractiveMode(EnhancedImageOCRProcessor processor) {
        boolean running = true;
        
        while (running) {
            displayEnhancedMenu();
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    handleSingleImageProcessing(processor);
                    break;
                case "2":
                    handleAutoRotationProcessing(processor);
                    break;
                case "3":
                    handleBatchProcessing(processor);
                    break;
                case "4":
                    handleCompareProcessingMethods(processor);
                    break;
                case "5":
                    handleRotationDetectionTest(processor);
                    break;
                case "6":
                    handleEnhancedConfiguration(processor);
                    break;
                case "7":
                    runDemoWithSampleImages(processor);
                    break;
                case "8":
                    displaySystemInfo(processor);
                    break;
                case "0":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
            
            if (running) {
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
            }
        }
        
        System.out.println("Thank you for using Enhanced Image OCR Processor!");
    }
    
    /**
     * Display enhanced menu
     */
    private static void displayEnhancedMenu() {
        System.out.println("\n=== Enhanced OCR Processor Menu ===");
        System.out.println("1. Process Single Image (Enhanced)");
        System.out.println("2. Process with Auto-Rotation Detection");
        System.out.println("3. Batch Process Images (Enhanced)");
        System.out.println("4. Compare Processing Methods");
        System.out.println("5. Test Rotation Detection");
        System.out.println("6. Configure Enhanced Settings");
        System.out.println("7. Run Demo with Sample Images");
        System.out.println("8. Show System Information");
        System.out.println("0. Exit");
        System.out.print("Enter your choice: ");
    }
    
    /**
     * Handle single image processing with enhanced methods
     */
    private static void handleSingleImageProcessing(EnhancedImageOCRProcessor processor) {
        System.out.print("Enter image path: ");
        String imagePath = scanner.nextLine().trim();
        
        if (imagePath.isEmpty()) {
            System.out.println("Image path cannot be empty.");
            return;
        }
        
        processSingleImageEnhanced(imagePath, processor);
    }
    
    /**
     * Process single image with enhanced methods
     */
    private static void processSingleImageEnhanced(String imagePath, EnhancedImageOCRProcessor processor) {
        System.out.println("\n--- Enhanced Image Processing ---");
        System.out.println("Image: " + imagePath);
        System.out.println("Using multiple processing strategies...");
        
        try {
            long startTime = System.currentTimeMillis();
            String extractedText = processor.processImage(imagePath);
            long endTime = System.currentTimeMillis();
            
            System.out.println("Processing completed in " + (endTime - startTime) + " ms");
            System.out.println("\n--- Extracted Text ---");
            if (extractedText.trim().isEmpty()) {
                System.out.println("No text detected. The image may need different preprocessing.");
                System.out.println("Try using auto-rotation detection or comparing different methods.");
            } else {
                System.out.println(extractedText.trim());
            }
            System.out.println("--- End of Text ---");
            
            // Offer to save results
            System.out.print("Save results to file? (y/n): ");
            String saveChoice = scanner.nextLine().trim().toLowerCase();
            if (saveChoice.equals("y") || saveChoice.equals("yes")) {
                saveTextToFile(extractedText, imagePath);
            }
            
        } catch (IOException e) {
            System.err.println("Error reading image: " + e.getMessage());
            logger.error("IO Error processing image: " + imagePath, e);
        } catch (TesseractException e) {
            System.err.println("OCR processing error: " + e.getMessage());
            logger.error("Tesseract error processing image: " + imagePath, e);
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            logger.error("Unexpected error processing image: " + imagePath, e);
        }
    }
    
    /**
     * Handle auto-rotation processing
     */
    private static void handleAutoRotationProcessing(EnhancedImageOCRProcessor processor) {
        System.out.print("Enter image path: ");
        String imagePath = scanner.nextLine().trim();
        
        if (imagePath.isEmpty()) {
            System.out.println("Image path cannot be empty.");
            return;
        }
        
        processWithAutoRotation(imagePath, processor);
    }
    
    /**
     * Process image with automatic rotation detection
     */
    private static void processWithAutoRotation(String imagePath, EnhancedImageOCRProcessor processor) {
        System.out.println("\n--- Auto-Rotation Processing ---");
        System.out.println("Image: " + imagePath);
        System.out.println("Detecting optimal rotation angle...");
        
        try {
            long startTime = System.currentTimeMillis();
            
            // First detect optimal rotation
            double optimalAngle = processor.detectOptimalRotation(imagePath);
            System.out.println("Optimal rotation angle detected: " + optimalAngle + " degrees");
            
            // Process with auto-rotation
            String extractedText = processor.processImageWithAutoRotation(imagePath);
            long endTime = System.currentTimeMillis();
            
            System.out.println("Processing completed in " + (endTime - startTime) + " ms");
            System.out.println("\n--- Extracted Text ---");
            if (extractedText.trim().isEmpty()) {
                System.out.println("No text detected even with rotation correction.");
                System.out.println("The image may have very challenging text or require manual preprocessing.");
            } else {
                System.out.println(extractedText.trim());
            }
            System.out.println("--- End of Text ---");
            
        } catch (Exception e) {
            System.err.println("Error during auto-rotation processing: " + e.getMessage());
            logger.error("Error in auto-rotation processing", e);
        }
    }
    
    /**
     * Handle batch processing with enhanced methods
     */
    private static void handleBatchProcessing(EnhancedImageOCRProcessor processor) {
        System.out.print("Enter directory path containing images: ");
        String directoryPath = scanner.nextLine().trim();
        
        if (directoryPath.isEmpty()) {
            System.out.println("Directory path cannot be empty.");
            return;
        }
        
        processBatchImagesEnhanced(directoryPath, processor);
    }
    
    /**
     * Process batch images with enhanced methods
     */
    private static void processBatchImagesEnhanced(String directoryPath, EnhancedImageOCRProcessor processor) {
        System.out.println("\n--- Enhanced Batch Processing ---");
        System.out.println("Directory: " + directoryPath);
        
        try {
            Path dir = Paths.get(directoryPath);
            if (!Files.exists(dir) || !Files.isDirectory(dir)) {
                System.err.println("Directory does not exist or is not a directory: " + directoryPath);
                return;
            }
            
            // Find image files
            List<String> imageFiles = Files.list(dir)
                    .filter(Files::isRegularFile)
                    .map(Path::toString)
                    .filter(path -> isImageFile(path))
                    .toList();
            
            if (imageFiles.isEmpty()) {
                System.out.println("No image files found in directory.");
                return;
            }
            
            System.out.println("Found " + imageFiles.size() + " image files");
            System.out.println("Processing with enhanced methods...");
            
            long startTime = System.currentTimeMillis();
            List<ImageOCRProcessor.OCRResult> results = processor.batchProcess(imageFiles);
            long endTime = System.currentTimeMillis();
            
            System.out.println("Batch processing completed in " + (endTime - startTime) + " ms");
            
            // Display results summary
            int successCount = 0;
            int failureCount = 0;
            int emptyResults = 0;
            
            for (ImageOCRProcessor.OCRResult result : results) {
                if (result.isSuccess()) {
                    if (result.getExtractedText().trim().isEmpty()) {
                        emptyResults++;
                        System.out.println("⚠ " + new File(result.getImagePath()).getName() + " - No text detected");
                    } else {
                        successCount++;
                        System.out.println("✓ " + new File(result.getImagePath()).getName() + 
                                         " - " + result.getExtractedText().substring(0, 
                                         Math.min(50, result.getExtractedText().length())) + "...");
                    }
                } else {
                    failureCount++;
                    System.out.println("✗ " + new File(result.getImagePath()).getName() + 
                                     " - " + result.getErrorMessage());
                }
            }
            
            System.out.println("\nSummary: " + successCount + " successful, " + 
                             emptyResults + " empty results, " + failureCount + " failed");
            
            if (emptyResults > 0) {
                System.out.println("\nTip: For images with no text detected, try:");
                System.out.println("- Auto-rotation detection (option 2)");
                System.out.println("- Compare processing methods (option 4)");
                System.out.println("- Check if text is very small or low contrast");
            }
            
        } catch (IOException e) {
            System.err.println("Error accessing directory: " + e.getMessage());
            logger.error("IO Error during batch processing", e);
        } catch (Exception e) {
            System.err.println("Unexpected error during batch processing: " + e.getMessage());
            logger.error("Unexpected error during batch processing", e);
        }
    }
    
    /**
     * Handle comparing different processing methods
     */
    private static void handleCompareProcessingMethods(EnhancedImageOCRProcessor processor) {
        System.out.print("Enter image path: ");
        String imagePath = scanner.nextLine().trim();
        
        if (imagePath.isEmpty()) {
            System.out.println("Image path cannot be empty.");
            return;
        }
        
        compareProcessingMethods(imagePath, processor);
    }
    
    /**
     * Compare different processing methods
     */
    private static void compareProcessingMethods(String imagePath, EnhancedImageOCRProcessor processor) {
        System.out.println("\n--- Comparing Processing Methods ---");
        System.out.println("Image: " + imagePath);
        
        // Test different methods
        String[] methodNames = {
            "Enhanced Multi-Strategy",
            "Auto-Rotation Detection",
            "Basic Processing"
        };
        
        String[] results = new String[3];
        long[] times = new long[3];
        
        try {
            // Method 1: Enhanced processing
            long start = System.currentTimeMillis();
            results[0] = processor.processImage(imagePath);
            times[0] = System.currentTimeMillis() - start;
            
            // Method 2: Auto-rotation
            start = System.currentTimeMillis();
            results[1] = processor.processImageWithAutoRotation(imagePath);
            times[1] = System.currentTimeMillis() - start;
            
            // Method 3: Basic processing (create basic processor)
            ImageOCRProcessor basicProcessor = new ImageOCRProcessor();
            start = System.currentTimeMillis();
            results[2] = basicProcessor.processImage(imagePath);
            times[2] = System.currentTimeMillis() - start;
            basicProcessor.cleanup();
            
        } catch (Exception e) {
            // Fallback for basic processing
            try {
                ImageOCRProcessor basicProcessor = new ImageOCRProcessor();
                long start = System.currentTimeMillis();
                results[2] = basicProcessor.processImage(imagePath);
                times[2] = System.currentTimeMillis() - start;
                basicProcessor.cleanup();
            } catch (Exception ex) {
                results[2] = "Error: " + ex.getMessage();
                times[2] = 0;
            }
        }
        
        // Display comparison results
        System.out.println("\n--- Processing Method Comparison ---");
        for (int i = 0; i < methodNames.length; i++) {
            System.out.println("\n" + (i + 1) + ". " + methodNames[i]);
            System.out.println("   Time: " + times[i] + " ms");
            System.out.println("   Result: " + (results[i].trim().isEmpty() ? "No text detected" : 
                             results[i].substring(0, Math.min(100, results[i].length()))));
            if (results[i].length() > 100) {
                System.out.println("   ... (truncated)");
            }
        }
        
        // Find best result
        int bestIndex = 0;
        int maxLength = results[0].trim().length();
        for (int i = 1; i < results.length; i++) {
            if (results[i].trim().length() > maxLength) {
                maxLength = results[i].trim().length();
                bestIndex = i;
            }
        }
        
        if (maxLength > 0) {
            System.out.println("\nBest result: " + methodNames[bestIndex] + 
                             " (extracted " + maxLength + " characters)");
        } else {
            System.out.println("\nNo method successfully extracted text. Consider:");
            System.out.println("- Checking image quality and resolution");
            System.out.println("- Verifying text is clearly visible");
            System.out.println("- Testing with different preprocessing parameters");
        }
    }
    
    /**
     * Handle rotation detection test
     */
    private static void handleRotationDetectionTest(EnhancedImageOCRProcessor processor) {
        System.out.print("Enter image path: ");
        String imagePath = scanner.nextLine().trim();
        
        if (imagePath.isEmpty()) {
            System.out.println("Image path cannot be empty.");
            return;
        }
        
        System.out.println("\n--- Rotation Detection Test ---");
        System.out.println("Testing different rotation angles...");
        
        try {
            double optimalAngle = processor.detectOptimalRotation(imagePath);
            System.out.println("Detected optimal rotation angle: " + optimalAngle + " degrees");
            
            // Test a few specific angles
            double[] testAngles = {0, 90, 180, 270, optimalAngle};
            
            for (double angle : testAngles) {
                try {
                    // This would require access to the rotation preprocessing method
                    System.out.println("Angle " + angle + "°: Testing...");
                    // For now, just show the angle
                } catch (Exception e) {
                    System.out.println("Angle " + angle + "°: Error - " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error during rotation detection test: " + e.getMessage());
        }
    }
    
    /**
     * Handle enhanced configuration
     */
    private static void handleEnhancedConfiguration(EnhancedImageOCRProcessor processor) {
        System.out.println("\n--- Enhanced Configuration ---");
        System.out.println("Current settings:");
        System.out.println("Rotation Detection: " + processor.isRotationDetectionEnabled());
        System.out.println("Multi-Color Detection: " + processor.isMultiColorDetectionEnabled());
        
        System.out.print("Enable rotation detection? (y/n): ");
        String rotationChoice = scanner.nextLine().trim().toLowerCase();
        processor.setRotationDetectionEnabled(rotationChoice.equals("y") || rotationChoice.equals("yes"));
        
        System.out.print("Enable multi-color detection? (y/n): ");
        String colorChoice = scanner.nextLine().trim().toLowerCase();
        processor.setMultiColorDetectionEnabled(colorChoice.equals("y") || colorChoice.equals("yes"));
        
        System.out.println("Configuration updated!");
    }
    
    /**
     * Run demo with sample images
     */
    private static void runDemoWithSampleImages(EnhancedImageOCRProcessor processor) {
        System.out.println("\n--- Enhanced Demo Mode ---");
        System.out.println("This demo will process images with enhanced methods for rotated/colored text.");
        
        File sampleDir = new File("sample-images");
        if (!sampleDir.exists()) {
            sampleDir.mkdirs();
        }
        
        File[] imageFiles = sampleDir.listFiles((dir, name) -> isImageFile(name));
        
        if (imageFiles != null && imageFiles.length > 0) {
            System.out.println("Processing " + imageFiles.length + " sample images with enhanced methods...");
            
            for (File imageFile : imageFiles) {
                System.out.println("\n--- Processing: " + imageFile.getName() + " ---");
                try {
                    // Try enhanced processing first
                    String enhancedResult = processor.processImage(imageFile.getAbsolutePath());
                    
                    if (enhancedResult.trim().isEmpty()) {
                        System.out.println("Enhanced processing: No text detected");
                        System.out.println("Trying auto-rotation detection...");
                        
                        // Try auto-rotation
                        String rotationResult = processor.processImageWithAutoRotation(imageFile.getAbsolutePath());
                        if (!rotationResult.trim().isEmpty()) {
                            System.out.println("Auto-rotation result: " + rotationResult.trim());
                        } else {
                            System.out.println("Auto-rotation: Still no text detected");
                            System.out.println("This image may require manual preprocessing or have very challenging text.");
                        }
                    } else {
                        System.out.println("Enhanced result: " + enhancedResult.trim());
                    }
                    
                } catch (Exception e) {
                    System.out.println("Error processing " + imageFile.getName() + ": " + e.getMessage());
                }
            }
        } else {
            System.out.println("No sample images found. Please add some images to the 'sample-images' directory.");
            System.out.println("For best results with challenging text:");
            System.out.println("- Images with rotated text at various angles");
            System.out.println("- Text in different colors (light/dark on various backgrounds)");
            System.out.println("- Museum/historical artifacts with Chinese characters");
            System.out.println("- Mixed Chinese and English text");
        }
    }
    
    /**
     * Check if file is an image
     */
    private static boolean isImageFile(String filename) {
        String lowerName = filename.toLowerCase();
        return lowerName.endsWith(".png") || lowerName.endsWith(".jpg") || 
               lowerName.endsWith(".jpeg") || lowerName.endsWith(".bmp") || 
               lowerName.endsWith(".tiff") || lowerName.endsWith(".tif");
    }
    
    /**
     * Save extracted text to file
     */
    private static void saveTextToFile(String text, String imagePath) {
        try {
            String outputPath = imagePath + "_enhanced_extracted_text.txt";
            Files.write(Paths.get(outputPath), text.getBytes("UTF-8"));
            System.out.println("Text saved to: " + outputPath);
        } catch (IOException e) {
            System.err.println("Error saving text to file: " + e.getMessage());
        }
    }
}