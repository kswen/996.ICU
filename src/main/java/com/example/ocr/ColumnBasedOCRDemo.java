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
 * Demo for Column-Based OCR Processor
 * Specifically designed for images with evenly spaced Chinese characters
 * 
 * Optimized for:
 * - 590x360 pixel images
 * - 6 Chinese characters evenly distributed
 * - Each character in ~98px width column
 * - Background template support for improved accuracy
 */
public class ColumnBasedOCRDemo {
    
    private static final Logger logger = LoggerFactory.getLogger(ColumnBasedOCRDemo.class);
    private static final Scanner scanner = new Scanner(System.in);
    
    public static void main(String[] args) {
        System.out.println("=== Column-Based OCR Processor Demo ===");
        System.out.println("Optimized for images with 6 evenly spaced Chinese characters");
        System.out.println("Image format: 590x360 pixels, ~98px per character column");
        System.out.println();
        
        ColumnBasedOCRProcessor processor = new ColumnBasedOCRProcessor();
        
        // Display system information
        displaySystemInfo(processor);
        
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
    private static void displaySystemInfo(ColumnBasedOCRProcessor processor) {
        System.out.println("Processor Configuration:");
        System.out.println("- Image size: " + processor.getImageWidth() + "x" + processor.getImageHeight());
        System.out.println("- Column count: " + processor.getColumnCount());
        System.out.println("- Column width: ~" + processor.getColumnWidth() + "px");
        System.out.println("- Margin buffer: " + processor.getMarginBuffer() + "px");
        System.out.println("- Background template: " + (processor.hasBackgroundTemplate() ? 
                          processor.getBackgroundTemplatePath() : "Not set"));
        System.out.println("- OCR Language: Chinese Simplified (chi_sim)");
        System.out.println("- OCR Mode: Single character recognition");
        System.out.println();
    }
    
    /**
     * Process command line arguments
     */
    private static void processCommandLineArgs(String[] args, ColumnBasedOCRProcessor processor) {
        String command = args[0].toLowerCase();
        
        switch (command) {
            case "single":
                if (args.length < 2) {
                    System.err.println("Usage: java ColumnBasedOCRDemo single <image_path>");
                    return;
                }
                processSingleImage(args[1], processor);
                break;
                
            case "template":
                if (args.length < 3) {
                    System.err.println("Usage: java ColumnBasedOCRDemo template <background_path> <image_path>");
                    return;
                }
                processWithTemplate(args[1], args[2], processor);
                break;
                
            case "batch":
                if (args.length < 2) {
                    System.err.println("Usage: java ColumnBasedOCRDemo batch <directory_path>");
                    return;
                }
                processBatchImages(args[1], processor);
                break;
                
            case "demo":
                runDemoWithSampleImages(processor);
                break;
                
            case "debug":
                if (args.length < 2) {
                    System.err.println("Usage: java ColumnBasedOCRDemo debug <image_path>");
                    return;
                }
                processWithDebug(args[1], processor);
                break;
                
            default:
                System.err.println("Unknown command: " + command);
                System.err.println("Available commands: single, template, batch, demo, debug");
        }
    }
    
    /**
     * Run interactive mode
     */
    private static void runInteractiveMode(ColumnBasedOCRProcessor processor) {
        boolean running = true;
        
        while (running) {
            displayMenu();
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    handleSingleImageProcessing(processor);
                    break;
                case "2":
                    handleBackgroundTemplateSetup(processor);
                    break;
                case "3":
                    handleProcessWithTemplate(processor);
                    break;
                case "4":
                    handleBatchProcessing(processor);
                    break;
                case "5":
                    handleDebugMode(processor);
                    break;
                case "6":
                    handleCustomDimensions(processor);
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
        
        System.out.println("Thank you for using Column-Based OCR Processor!");
    }
    
    /**
     * Display interactive menu
     */
    private static void displayMenu() {
        System.out.println("\n=== Column-Based OCR Menu ===");
        System.out.println("1. Process Single Image");
        System.out.println("2. Set Background Template");
        System.out.println("3. Process with Background Template");
        System.out.println("4. Batch Process Directory");
        System.out.println("5. Debug Mode (Save Column Images)");
        System.out.println("6. Custom Image Dimensions");
        System.out.println("7. Demo with Sample Images");
        System.out.println("8. Show System Information");
        System.out.println("0. Exit");
        System.out.print("Enter your choice: ");
    }
    
    /**
     * Handle single image processing
     */
    private static void handleSingleImageProcessing(ColumnBasedOCRProcessor processor) {
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
    private static void processSingleImage(String imagePath, ColumnBasedOCRProcessor processor) {
        System.out.println("\n--- Column-Based Processing ---");
        System.out.println("Image: " + imagePath);
        System.out.println("Splitting into " + processor.getColumnCount() + " columns...");
        
        try {
            long startTime = System.currentTimeMillis();
            ColumnBasedOCRProcessor.ColumnResult result = processor.processImageWithDetails(imagePath);
            long endTime = System.currentTimeMillis();
            
            if (result.isSuccess()) {
                System.out.println("Processing completed in " + result.getProcessingTime() + " ms");
                System.out.println("\n--- Column Results ---");
                
                String[] characters = result.getCharacters();
                for (int i = 0; i < characters.length; i++) {
                    String character = characters[i];
                    if (character.isEmpty()) {
                        System.out.println("Column " + (i + 1) + ": [No character detected]");
                    } else {
                        System.out.println("Column " + (i + 1) + ": '" + character + "'");
                    }
                }
                
                String combinedText = result.getCombinedText();
                System.out.println("\n--- Final Result ---");
                if (combinedText.isEmpty()) {
                    System.out.println("No characters detected in any column.");
                    System.out.println("Tips:");
                    System.out.println("- Check if image has the expected format (590x360 with 6 characters)");
                    System.out.println("- Try setting a background template");
                    System.out.println("- Use debug mode to see individual column images");
                } else {
                    System.out.println("Combined text: '" + combinedText + "'");
                    System.out.println("Character count: " + combinedText.length());
                }
                
                // Offer to save results
                System.out.print("Save results to file? (y/n): ");
                String saveChoice = scanner.nextLine().trim().toLowerCase();
                if (saveChoice.equals("y") || saveChoice.equals("yes")) {
                    saveColumnResults(result);
                }
                
            } else {
                System.err.println("Processing failed: " + result.getErrorMessage());
            }
            
        } catch (Exception e) {
            System.err.println("Error processing image: " + e.getMessage());
            logger.error("Error processing image: " + imagePath, e);
        }
    }
    
    /**
     * Handle background template setup
     */
    private static void handleBackgroundTemplateSetup(ColumnBasedOCRProcessor processor) {
        System.out.println("\n--- Background Template Setup ---");
        System.out.println("Background templates help improve accuracy by removing the background");
        System.out.println("The template should be the same image without the text overlay");
        System.out.print("Enter background template image path: ");
        String templatePath = scanner.nextLine().trim();
        
        if (templatePath.isEmpty()) {
            System.out.println("Template path cannot be empty.");
            return;
        }
        
        File templateFile = new File(templatePath);
        if (!templateFile.exists()) {
            System.err.println("Template file not found: " + templatePath);
            return;
        }
        
        processor.setBackgroundTemplate(templatePath);
        System.out.println("Background template set successfully!");
        System.out.println("Template: " + templatePath);
    }
    
    /**
     * Handle processing with template
     */
    private static void handleProcessWithTemplate(ColumnBasedOCRProcessor processor) {
        if (!processor.hasBackgroundTemplate()) {
            System.out.println("No background template set. Please set one first (option 2).");
            return;
        }
        
        System.out.print("Enter image path to process with template: ");
        String imagePath = scanner.nextLine().trim();
        
        if (imagePath.isEmpty()) {
            System.out.println("Image path cannot be empty.");
            return;
        }
        
        System.out.println("Processing with background template: " + processor.getBackgroundTemplatePath());
        processSingleImage(imagePath, processor);
    }
    
    /**
     * Process with template (command line)
     */
    private static void processWithTemplate(String templatePath, String imagePath, ColumnBasedOCRProcessor processor) {
        System.out.println("\n--- Processing with Background Template ---");
        System.out.println("Template: " + templatePath);
        System.out.println("Image: " + imagePath);
        
        processor.setBackgroundTemplate(templatePath);
        processSingleImage(imagePath, processor);
    }
    
    /**
     * Handle batch processing
     */
    private static void handleBatchProcessing(ColumnBasedOCRProcessor processor) {
        System.out.print("Enter directory path containing images: ");
        String directoryPath = scanner.nextLine().trim();
        
        if (directoryPath.isEmpty()) {
            System.out.println("Directory path cannot be empty.");
            return;
        }
        
        processBatchImages(directoryPath, processor);
    }
    
    /**
     * Process batch images
     */
    private static void processBatchImages(String directoryPath, ColumnBasedOCRProcessor processor) {
        System.out.println("\n--- Batch Column-Based Processing ---");
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
            System.out.println("Processing with column-based approach...");
            
            long startTime = System.currentTimeMillis();
            List<ColumnBasedOCRProcessor.ColumnResult> results = processor.batchProcess(imageFiles);
            long endTime = System.currentTimeMillis();
            
            System.out.println("Batch processing completed in " + (endTime - startTime) + " ms");
            
            // Display results summary
            int successCount = 0;
            int partialResults = 0;
            int failureCount = 0;
            
            System.out.println("\n--- Batch Results ---");
            for (ColumnBasedOCRProcessor.ColumnResult result : results) {
                String filename = new File(result.getImagePath()).getName();
                
                if (result.isSuccess()) {
                    String combinedText = result.getCombinedText();
                    if (combinedText.isEmpty()) {
                        partialResults++;
                        System.out.println("⚠ " + filename + " - No characters detected");
                    } else {
                        successCount++;
                        System.out.println("✓ " + filename + " - '" + combinedText + "' (" + 
                                         combinedText.length() + " chars)");
                    }
                } else {
                    failureCount++;
                    System.out.println("✗ " + filename + " - " + result.getErrorMessage());
                }
            }
            
            System.out.println("\n--- Summary ---");
            System.out.println("Successful: " + successCount);
            System.out.println("Partial results: " + partialResults);
            System.out.println("Failed: " + failureCount);
            System.out.println("Total: " + results.size());
            
            if (partialResults > 0 || failureCount > 0) {
                System.out.println("\nTips for better results:");
                System.out.println("- Ensure images are 590x360 pixels with 6 evenly spaced characters");
                System.out.println("- Try setting a background template");
                System.out.println("- Use debug mode to examine individual column extractions");
            }
            
            // Offer to save batch results
            System.out.print("Save batch results to file? (y/n): ");
            String saveChoice = scanner.nextLine().trim().toLowerCase();
            if (saveChoice.equals("y") || saveChoice.equals("yes")) {
                saveBatchResults(results, directoryPath);
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
     * Handle debug mode
     */
    private static void handleDebugMode(ColumnBasedOCRProcessor processor) {
        System.out.println("\n--- Debug Mode ---");
        System.out.println("This will save individual column images to 'debug-columns/' directory");
        System.out.print("Enter image path: ");
        String imagePath = scanner.nextLine().trim();
        
        if (imagePath.isEmpty()) {
            System.out.println("Image path cannot be empty.");
            return;
        }
        
        processWithDebug(imagePath, processor);
    }
    
    /**
     * Process with debug output
     */
    private static void processWithDebug(String imagePath, ColumnBasedOCRProcessor processor) {
        System.out.println("\n--- Debug Mode Processing ---");
        System.out.println("Image: " + imagePath);
        System.out.println("Debug images will be saved to: debug-columns/");
        
        // Enable debug logging temporarily
        System.setProperty("org.slf4j.simpleLogger.log.com.example.ocr.ColumnBasedOCRProcessor", "debug");
        
        try {
            // Create debug directory
            File debugDir = new File("debug-columns");
            if (!debugDir.exists()) {
                debugDir.mkdirs();
                System.out.println("Created debug directory: " + debugDir.getAbsolutePath());
            }
            
            // Process the image
            processSingleImage(imagePath, processor);
            
            // List debug files created
            File[] debugFiles = debugDir.listFiles((dir, name) -> name.startsWith("column_"));
            if (debugFiles != null && debugFiles.length > 0) {
                System.out.println("\n--- Debug Files Created ---");
                Arrays.sort(debugFiles);
                for (File file : debugFiles) {
                    System.out.println("- " + file.getName());
                }
                System.out.println("\nYou can examine these images to see how each column was extracted and processed.");
            }
            
        } catch (Exception e) {
            System.err.println("Error in debug mode: " + e.getMessage());
        } finally {
            // Reset debug logging
            System.clearProperty("org.slf4j.simpleLogger.log.com.example.ocr.ColumnBasedOCRProcessor");
        }
    }
    
    /**
     * Handle custom dimensions
     */
    private static void handleCustomDimensions(ColumnBasedOCRProcessor processor) {
        System.out.println("\n--- Custom Image Dimensions ---");
        System.out.println("Current: " + processor.getImageWidth() + "x" + processor.getImageHeight() + 
                          " with " + processor.getColumnCount() + " columns");
        
        try {
            System.out.print("Enter image width: ");
            int width = Integer.parseInt(scanner.nextLine().trim());
            
            System.out.print("Enter image height: ");
            int height = Integer.parseInt(scanner.nextLine().trim());
            
            System.out.print("Enter number of columns: ");
            int columns = Integer.parseInt(scanner.nextLine().trim());
            
            // Create new processor with custom dimensions
            ColumnBasedOCRProcessor customProcessor = new ColumnBasedOCRProcessor(width, height, columns);
            
            System.out.println("Created custom processor: " + width + "x" + height + " with " + columns + " columns");
            System.out.println("Column width: ~" + customProcessor.getColumnWidth() + "px");
            
            System.out.print("Test with an image? (y/n): ");
            String testChoice = scanner.nextLine().trim().toLowerCase();
            if (testChoice.equals("y") || testChoice.equals("yes")) {
                System.out.print("Enter image path: ");
                String imagePath = scanner.nextLine().trim();
                if (!imagePath.isEmpty()) {
                    processSingleImage(imagePath, customProcessor);
                }
            }
            
            customProcessor.cleanup();
            
        } catch (NumberFormatException e) {
            System.err.println("Invalid number format. Please enter valid integers.");
        } catch (Exception e) {
            System.err.println("Error with custom dimensions: " + e.getMessage());
        }
    }
    
    /**
     * Run demo with sample images
     */
    private static void runDemoWithSampleImages(ColumnBasedOCRProcessor processor) {
        System.out.println("\n--- Column-Based Demo ---");
        System.out.println("This demo processes images optimized for 6 evenly spaced Chinese characters");
        
        File sampleDir = new File("sample-images");
        if (!sampleDir.exists()) {
            sampleDir.mkdirs();
        }
        
        File[] imageFiles = sampleDir.listFiles((dir, name) -> isImageFile(name));
        
        if (imageFiles != null && imageFiles.length > 0) {
            System.out.println("Processing " + imageFiles.length + " sample images with column-based approach...");
            
            for (File imageFile : imageFiles) {
                System.out.println("\n--- Processing: " + imageFile.getName() + " ---");
                try {
                    ColumnBasedOCRProcessor.ColumnResult result = processor.processImageWithDetails(imageFile.getAbsolutePath());
                    
                    if (result.isSuccess()) {
                        String combinedText = result.getCombinedText();
                        if (combinedText.isEmpty()) {
                            System.out.println("No characters detected");
                            System.out.println("Individual columns: " + Arrays.toString(result.getCharacters()));
                        } else {
                            System.out.println("Result: '" + combinedText + "'");
                            System.out.println("Individual columns: " + Arrays.toString(result.getCharacters()));
                        }
                        System.out.println("Processing time: " + result.getProcessingTime() + "ms");
                    } else {
                        System.out.println("Processing failed: " + result.getErrorMessage());
                    }
                    
                } catch (Exception e) {
                    System.out.println("Error processing " + imageFile.getName() + ": " + e.getMessage());
                }
            }
        } else {
            System.out.println("No sample images found. Please add images to the 'sample-images' directory.");
            System.out.println("\nFor optimal results, images should be:");
            System.out.println("- 590x360 pixels (will be resized if different)");
            System.out.println("- Contain 6 Chinese characters evenly spaced");
            System.out.println("- Each character in approximately 98px width column");
            System.out.println("- Clear text with good contrast");
            System.out.println("\nOptional: Provide a background template image for better accuracy");
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
     * Save column results to file
     */
    private static void saveColumnResults(ColumnBasedOCRProcessor.ColumnResult result) {
        try {
            String outputPath = result.getImagePath() + "_column_results.txt";
            StringBuilder content = new StringBuilder();
            
            content.append("Column-Based OCR Results\n");
            content.append("========================\n");
            content.append("Image: ").append(result.getImagePath()).append("\n");
            content.append("Processing time: ").append(result.getProcessingTime()).append("ms\n");
            content.append("Success: ").append(result.isSuccess()).append("\n\n");
            
            String[] characters = result.getCharacters();
            content.append("Individual Columns:\n");
            for (int i = 0; i < characters.length; i++) {
                content.append("Column ").append(i + 1).append(": ");
                if (characters[i].isEmpty()) {
                    content.append("[No character detected]");
                } else {
                    content.append("'").append(characters[i]).append("'");
                }
                content.append("\n");
            }
            
            content.append("\nCombined Result: '").append(result.getCombinedText()).append("'\n");
            
            if (!result.isSuccess()) {
                content.append("\nError: ").append(result.getErrorMessage()).append("\n");
            }
            
            Files.write(Paths.get(outputPath), content.toString().getBytes("UTF-8"));
            System.out.println("Results saved to: " + outputPath);
            
        } catch (IOException e) {
            System.err.println("Error saving results: " + e.getMessage());
        }
    }
    
    /**
     * Save batch results to file
     */
    private static void saveBatchResults(List<ColumnBasedOCRProcessor.ColumnResult> results, String directoryPath) {
        try {
            String outputPath = directoryPath + File.separator + "column_batch_results.txt";
            StringBuilder content = new StringBuilder();
            
            content.append("Column-Based Batch OCR Results\n");
            content.append("==============================\n");
            content.append("Directory: ").append(directoryPath).append("\n");
            content.append("Processed: ").append(java.time.LocalDateTime.now()).append("\n");
            content.append("Total images: ").append(results.size()).append("\n\n");
            
            for (ColumnBasedOCRProcessor.ColumnResult result : results) {
                content.append("File: ").append(new File(result.getImagePath()).getName()).append("\n");
                content.append("Success: ").append(result.isSuccess()).append("\n");
                content.append("Time: ").append(result.getProcessingTime()).append("ms\n");
                
                if (result.isSuccess()) {
                    content.append("Result: '").append(result.getCombinedText()).append("'\n");
                    content.append("Columns: ").append(Arrays.toString(result.getCharacters())).append("\n");
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