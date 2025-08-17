package com.example.ocr;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * Utility class for splitting images into equal sub-images based on width.
 * Compatible with Java 1.8.
 */
public class ImageSplitter {
    
    /**
     * Cuts a 590x360 picture into 6 equal sub-pictures based on width.
     * Each sub-picture will be approximately 98.33 pixels wide and 360 pixels tall.
     * 
     * @param inputImagePath Path to the input image file
     * @param outputDirectory Directory where the sub-images will be saved
     * @return List of File objects representing the created sub-images
     * @throws IOException if there's an error reading the input image or writing sub-images
     * @throws IllegalArgumentException if the input image dimensions don't match expected 590x360
     */
    public static List<File> cutImageIntoSixParts(String inputImagePath, String outputDirectory) 
            throws IOException, IllegalArgumentException {
        
        // Read the input image
        BufferedImage originalImage = ImageIO.read(new File(inputImagePath));
        
        if (originalImage == null) {
            throw new IOException("Could not read image from: " + inputImagePath);
        }
        
        // Validate image dimensions
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        
        if (originalWidth != 590 || originalHeight != 360) {
            throw new IllegalArgumentException(
                String.format("Expected image dimensions 590x360, but got %dx%d", 
                             originalWidth, originalHeight));
        }
        
        // Create output directory if it doesn't exist
        File outputDir = new File(outputDirectory);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        
        // Calculate sub-image dimensions
        int subImageWidth = originalWidth / 6; // 590 / 6 = 98.33, truncated to 98
        int remainingWidth = originalWidth % 6; // Handle any remaining pixels
        int subImageHeight = originalHeight; // 360
        
        List<File> subImageFiles = new ArrayList<File>();
        
        // Split the image into 6 parts
        for (int i = 0; i < 6; i++) {
            // Calculate the x position for this sub-image
            int x = i * subImageWidth;
            
            // For the last sub-image, include any remaining pixels
            int currentSubWidth = subImageWidth;
            if (i == 5) { // Last sub-image
                currentSubWidth += remainingWidth;
            }
            
            // Extract the sub-image
            BufferedImage subImage = originalImage.getSubimage(x, 0, currentSubWidth, subImageHeight);
            
            // Create a new BufferedImage to ensure proper copying
            BufferedImage subImageCopy = new BufferedImage(currentSubWidth, subImageHeight, 
                                                          BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = subImageCopy.createGraphics();
            g2d.drawImage(subImage, 0, 0, null);
            g2d.dispose();
            
            // Save the sub-image
            String fileName = String.format("sub_image_%d.png", i + 1);
            File outputFile = new File(outputDir, fileName);
            
            ImageIO.write(subImageCopy, "png", outputFile);
            subImageFiles.add(outputFile);
            
            System.out.println(String.format("Created sub-image %d: %s (dimensions: %dx%d)", 
                                            i + 1, outputFile.getAbsolutePath(), 
                                            currentSubWidth, subImageHeight));
        }
        
        return subImageFiles;
    }
    
    /**
     * Generic method to cut an image into equal parts based on width.
     * 
     * @param inputImagePath Path to the input image file
     * @param outputDirectory Directory where the sub-images will be saved
     * @param numberOfParts Number of parts to split the image into
     * @return List of File objects representing the created sub-images
     * @throws IOException if there's an error reading the input image or writing sub-images
     */
    public static List<File> cutImageIntoParts(String inputImagePath, String outputDirectory, 
                                              int numberOfParts) throws IOException {
        
        if (numberOfParts <= 0) {
            throw new IllegalArgumentException("Number of parts must be greater than 0");
        }
        
        // Read the input image
        BufferedImage originalImage = ImageIO.read(new File(inputImagePath));
        
        if (originalImage == null) {
            throw new IOException("Could not read image from: " + inputImagePath);
        }
        
        // Create output directory if it doesn't exist
        File outputDir = new File(outputDirectory);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        
        // Calculate sub-image dimensions
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        int subImageWidth = originalWidth / numberOfParts;
        int remainingWidth = originalWidth % numberOfParts;
        
        List<File> subImageFiles = new ArrayList<File>();
        
        // Split the image into the specified number of parts
        for (int i = 0; i < numberOfParts; i++) {
            // Calculate the x position for this sub-image
            int x = i * subImageWidth;
            
            // For the last sub-image, include any remaining pixels
            int currentSubWidth = subImageWidth;
            if (i == numberOfParts - 1) { // Last sub-image
                currentSubWidth += remainingWidth;
            }
            
            // Extract the sub-image
            BufferedImage subImage = originalImage.getSubimage(x, 0, currentSubWidth, originalHeight);
            
            // Create a new BufferedImage to ensure proper copying
            BufferedImage subImageCopy = new BufferedImage(currentSubWidth, originalHeight, 
                                                          BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = subImageCopy.createGraphics();
            g2d.drawImage(subImage, 0, 0, null);
            g2d.dispose();
            
            // Save the sub-image
            String fileName = String.format("part_%d.png", i + 1);
            File outputFile = new File(outputDir, fileName);
            
            ImageIO.write(subImageCopy, "png", outputFile);
            subImageFiles.add(outputFile);
            
            System.out.println(String.format("Created part %d: %s (dimensions: %dx%d)", 
                                            i + 1, outputFile.getAbsolutePath(), 
                                            currentSubWidth, originalHeight));
        }
        
        return subImageFiles;
    }
    
    /**
     * Main method for testing the image splitting functionality.
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java ImageSplitter <input_image_path> <output_directory>");
            System.out.println("Example: java ImageSplitter input.png ./output/");
            return;
        }
        
        String inputImagePath = args[0];
        String outputDirectory = args[1];
        
        try {
            System.out.println("Splitting image: " + inputImagePath);
            List<File> subImages = cutImageIntoSixParts(inputImagePath, outputDirectory);
            System.out.println("Successfully created " + subImages.size() + " sub-images in: " + outputDirectory);
            
        } catch (IOException e) {
            System.err.println("Error processing image: " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid input: " + e.getMessage());
        }
    }
}