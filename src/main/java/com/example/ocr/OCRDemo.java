package com.example.ocr;

import net.sourceforge.tess4j.TesseractException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Demonstration class for ImageOCRProcessor
 * Shows various OCR processing capabilities including:
 * - Single image processing
 * - Batch processing
 * - Custom parameter processing
 * - Text region detection
 * - Interactive mode
 */
public class OCRDemo {
    
    private static final Logger logger = LoggerFactory.getLogger(OCRDemo.class);
    private static final Scanner scanner = new Scanner(System.in);
    
    public static void main(String[] args) {
        System.out.println("=== Image OCR Processor Demo ===");
        System.out.println("Using OpenCV and Tess4J for Chinese/English text recognition");
        System.out.println();
        
        ImageOCRProcessor ocrProcessor = new ImageOCRProcessor();
        
        // Display system information
        displaySystemInfo(ocrProcessor);
        
        if (!ocrProcessor.isReady()) {
            System.err.println("OCR Processor is not ready. Please check Tesseract installation.");
            return;
        }
        
        // Check for command line arguments
        if (args.length > 0) {
            processCommandLineArgs(args, ocrProcessor);
        } else {
            // Interactive mode
            runInteractiveMode(ocrProcessor);
        }
        
        // Cleanup resources
        ocrProcessor.cleanup();
        scanner.close();
    }
    
    /**
     * Display system and OCR processor information
     */
    private static void displaySystemInfo(ImageOCRProcessor processor) {
        System.out.println("System Information:");
        System.out.println(processor.getVersionInfo());
        System.out.println("Current Configuration:");
        System.out.println(processor.getConfiguration());
        System.out.println();
    }
    
    /**
     * Process command line arguments
     */
    private static void processCommandLineArgs(String[] args, ImageOCRProcessor processor) {
        String command = args[0].toLowerCase();
        
        switch (command) {
            case "single":
                if (args.length < 2) {
                    System.err.println("Usage: java OCRDemo single <image_path>");
                    return;
                }
                processSingleImage(args[1], processor);
                break;
                
            case "batch":
                if (args.length < 2) {
                    System.err.println("Usage: java OCRDemo batch <directory_path>");
                    return;
                }
                processBatchImages(args[1], processor);
                break;
                
            case "demo":
                runDemoWithSampleImages(processor);
                break;
                
            default:
                System.err.println("Unknown command: " + command);
                System.err.println("Available commands: single, batch, demo");
        }
    }
    
    /**
     * Run interactive mode
     */
    private static void runInteractiveMode(ImageOCRProcessor processor) {
        boolean running = true;
        
        while (running) {
            displayMenu();
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    handleSingleImageProcessing(processor);
                    break;
                case "2":
                    handleBatchProcessing(processor);
                    break;
                case "3":
                    handleCustomParameterProcessing(processor);
                    break;
                case "4":
                    handleTextRegionDetection(processor);
                    break;
                case "5":
                    handleConfigurationUpdate(processor);
                    break;
                case "6":
                    runDemoWithSampleImages(processor);
                    break;
                case "7":
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
        
        System.out.println("Thank you for using Image OCR Processor!");
    }
    
    /**
     * Display interactive menu
     */
    private static void displayMenu() {
        System.out.println("\n=== OCR Processor Menu ===");
        System.out.println("1. Process Single Image");
        System.out.println("2. Batch Process Images");
        System.out.println("3. Process with Custom Parameters");
        System.out.println("4. Detect Text Regions");
        System.out.println("5. Update Configuration");
        System.out.println("6. Run Demo with Sample Images");
        System.out.println("7. Show System Information");
        System.out.println("0. Exit");
        System.out.print("Enter your choice: ");
    }
    
    /**
     * Handle single image processing
     */
    private static void handleSingleImageProcessing(ImageOCRProcessor processor) {
        System.out.print("Enter image path: ");
        String imagePath = scanner.nextLine().trim();
        
        if (imagePath.isEmpty()) {
            System.out.println("Image path cannot be empty.");
            return;
        }
        
        processSingleImage(imagePath, processor);
    }
    
    /**
     * Process a single image
     */
    private static void processSingleImage(String imagePath, ImageOCRProcessor processor) {
        System.out.println("\n--- Processing Single Image ---");
        System.out.println("Image: " + imagePath);
        
        try {
            long startTime = System.currentTimeMillis();
            String extractedText = processor.processImage(imagePath);
            long endTime = System.currentTimeMillis();
            
            System.out.println("Processing completed in " + (endTime - startTime) + " ms");
            System.out.println("\n--- Extracted Text ---");
            System.out.println(extractedText.trim());
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
     * Handle batch processing
     */
    private static void handleBatchProcessing(ImageOCRProcessor processor) {
        System.out.print("Enter directory path containing images: ");
        String directoryPath = scanner.nextLine().trim();
        
        if (directoryPath.isEmpty()) {
            System.out.println("Directory path cannot be empty.");
            return;
        }
        
        processBatchImages(directoryPath, processor);
    }
    
    /**
     * Process batch images from directory
     */
    private static void processBatchImages(String directoryPath, ImageOCRProcessor processor) {
        System.out.println("\n--- Batch Processing Images ---");
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
            
            long startTime = System.currentTimeMillis();
            List<ImageOCRProcessor.OCRResult> results = processor.batchProcess(imageFiles);
            long endTime = System.currentTimeMillis();
            
            System.out.println("Batch processing completed in " + (endTime - startTime) + " ms");
            
            // Display results summary
            int successCount = 0;
            int failureCount = 0;
            
            for (ImageOCRProcessor.OCRResult result : results) {
                if (result.isSuccess()) {
                    successCount++;
                    System.out.println("✓ " + new File(result.getImagePath()).getName());
                } else {
                    failureCount++;
                    System.out.println("✗ " + new File(result.getImagePath()).getName() + " - " + result.getErrorMessage());
                }
            }
            
            System.out.println("\nSummary: " + successCount + " successful, " + failureCount + " failed");
            
            // Offer to save detailed results
            System.out.print("Save detailed results to file? (y/n): ");
            String saveChoice = scanner.nextLine().trim().toLowerCase();
            if (saveChoice.equals("y") || saveChoice.equals("yes")) {
                saveBatchResultsToFile(results, directoryPath);
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
     * Handle custom parameter processing
     */
    private static void handleCustomParameterProcessing(ImageOCRProcessor processor) {
        System.out.print("Enter image path: ");
        String imagePath = scanner.nextLine().trim();
        
        if (imagePath.isEmpty()) {
            System.out.println("Image path cannot be empty.");
            return;
        }
        
        System.out.println("Current configuration: " + processor.getConfiguration());
        
        System.out.print("Enter contrast alpha (current: default 1.5): ");
        String alphaStr = scanner.nextLine().trim();
        double alpha = alphaStr.isEmpty() ? 1.5 : Double.parseDouble(alphaStr);
        
        System.out.print("Enter brightness beta (current: default 30): ");
        String betaStr = scanner.nextLine().trim();
        int beta = betaStr.isEmpty() ? 30 : Integer.parseInt(betaStr);
        
        try {
            long startTime = System.currentTimeMillis();
            String extractedText = processor.processImageWithCustomParams(imagePath, alpha, beta);
            long endTime = System.currentTimeMillis();
            
            System.out.println("Processing completed in " + (endTime - startTime) + " ms");
            System.out.println("Used parameters - Contrast: " + alpha + ", Brightness: " + beta);
            System.out.println("\n--- Extracted Text ---");
            System.out.println(extractedText.trim());
            System.out.println("--- End of Text ---");
            
        } catch (Exception e) {
            System.err.println("Error processing image with custom parameters: " + e.getMessage());
            logger.error("Error in custom parameter processing", e);
        }
    }
    
    /**
     * Handle text region detection
     */
    private static void handleTextRegionDetection(ImageOCRProcessor processor) {
        System.out.print("Enter image path: ");
        String imagePath = scanner.nextLine().trim();
        
        if (imagePath.isEmpty()) {
            System.out.println("Image path cannot be empty.");
            return;
        }
        
        try {
            System.out.println("Detecting text regions...");
            List<BufferedImage> textRegions = processor.detectTextRegions(imagePath);
            
            System.out.println("Detected " + textRegions.size() + " potential text regions");
            
            if (!textRegions.isEmpty()) {
                System.out.print("Process detected regions with OCR? (y/n): ");
                String processChoice = scanner.nextLine().trim().toLowerCase();
                
                if (processChoice.equals("y") || processChoice.equals("yes")) {
                    for (int i = 0; i < textRegions.size(); i++) {
                        try {
                            String regionText = processor.tesseract.doOCR(textRegions.get(i));
                            System.out.println("Region " + (i + 1) + ": " + regionText.trim());
                        } catch (TesseractException e) {
                            System.out.println("Region " + (i + 1) + ": OCR failed - " + e.getMessage());
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error detecting text regions: " + e.getMessage());
            logger.error("Error in text region detection", e);
        }
    }
    
    /**
     * Handle configuration update
     */
    private static void handleConfigurationUpdate(ImageOCRProcessor processor) {
        System.out.println("Current configuration: " + processor.getConfiguration());
        
        try {
            System.out.print("Enter new contrast alpha (press Enter to keep current): ");
            String alphaStr = scanner.nextLine().trim();
            
            System.out.print("Enter new brightness beta (press Enter to keep current): ");
            String betaStr = scanner.nextLine().trim();
            
            System.out.print("Enter new Gaussian kernel size (press Enter to keep current): ");
            String kernelStr = scanner.nextLine().trim();
            
            System.out.print("Enter new threshold value (press Enter to keep current): ");
            String thresholdStr = scanner.nextLine().trim();
            
            ImageOCRProcessor.OCRConfig currentConfig = processor.getConfiguration();
            ImageOCRProcessor.OCRConfig newConfig = new ImageOCRProcessor.OCRConfig(
                    alphaStr.isEmpty() ? currentConfig.getContrastAlpha() : Double.parseDouble(alphaStr),
                    betaStr.isEmpty() ? currentConfig.getBrightnessBeta() : Integer.parseInt(betaStr),
                    kernelStr.isEmpty() ? currentConfig.getGaussianKernelSize() : Integer.parseInt(kernelStr),
                    thresholdStr.isEmpty() ? currentConfig.getThresholdValue() : Double.parseDouble(thresholdStr)
            );
            
            processor.updateConfiguration(newConfig);
            System.out.println("Configuration updated: " + newConfig);
            
        } catch (NumberFormatException e) {
            System.err.println("Invalid number format. Configuration not updated.");
        } catch (Exception e) {
            System.err.println("Error updating configuration: " + e.getMessage());
        }
    }
    
    /**
     * Run demo with sample images
     */
    private static void runDemoWithSampleImages(ImageOCRProcessor processor) {
        System.out.println("\n--- Demo Mode ---");
        System.out.println("This demo will create sample text images and process them.");
        
        // Create sample images directory if it doesn't exist
        File sampleDir = new File("sample-images");
        if (!sampleDir.exists()) {
            sampleDir.mkdirs();
        }
        
        // Create sample text files for demonstration
        createSampleTextImages();
        
        // Process any existing images in sample-images directory
        File[] imageFiles = sampleDir.listFiles((dir, name) -> isImageFile(name));
        
        if (imageFiles != null && imageFiles.length > 0) {
            System.out.println("Processing " + imageFiles.length + " sample images...");
            
            for (File imageFile : imageFiles) {
                System.out.println("\n--- Processing: " + imageFile.getName() + " ---");
                try {
                    String text = processor.processImage(imageFile.getAbsolutePath());
                    System.out.println("Extracted text: " + text.trim());
                } catch (Exception e) {
                    System.out.println("Error processing " + imageFile.getName() + ": " + e.getMessage());
                }
            }
        } else {
            System.out.println("No sample images found. Please add some images to the 'sample-images' directory.");
            System.out.println("Supported formats: PNG, JPG, JPEG, BMP, TIFF");
        }
    }
    
    /**
     * Create sample text images (placeholder method)
     */
    private static void createSampleTextImages() {
        System.out.println("Note: To test the OCR functionality, please add image files to the 'sample-images' directory.");
        System.out.println("The images should contain Chinese and/or English text similar to the ones you provided.");
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
            String outputPath = imagePath + "_extracted_text.txt";
            Files.write(Paths.get(outputPath), text.getBytes("UTF-8"));
            System.out.println("Text saved to: " + outputPath);
        } catch (IOException e) {
            System.err.println("Error saving text to file: " + e.getMessage());
        }
    }
    
    /**
     * Save batch processing results to file
     */
    private static void saveBatchResultsToFile(List<ImageOCRProcessor.OCRResult> results, String directoryPath) {
        try {
            String outputPath = directoryPath + File.separator + "batch_ocr_results.txt";
            StringBuilder content = new StringBuilder();
            
            content.append("Batch OCR Processing Results\n");
            content.append("===========================\n");
            content.append("Directory: ").append(directoryPath).append("\n");
            content.append("Processed: ").append(java.time.LocalDateTime.now()).append("\n\n");
            
            for (ImageOCRProcessor.OCRResult result : results) {
                content.append("File: ").append(result.getImagePath()).append("\n");
                content.append("Status: ").append(result.isSuccess() ? "SUCCESS" : "FAILED").append("\n");
                
                if (result.isSuccess()) {
                    content.append("Text:\n").append(result.getExtractedText()).append("\n");
                } else {
                    content.append("Error: ").append(result.getErrorMessage()).append("\n");
                }
                
                content.append("\n").append("-".repeat(50)).append("\n\n");
            }
            
            Files.write(Paths.get(outputPath), content.toString().getBytes("UTF-8"));
            System.out.println("Batch results saved to: " + outputPath);
            
        } catch (IOException e) {
            System.err.println("Error saving batch results: " + e.getMessage());
        }
    }
}