package com.example.ocr;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Image Splitter utility class for cutting images into equal sub-pictures.
 * 
 * This class provides functionality to split images horizontally into equal parts
 * based on width, which is useful for processing large images in smaller chunks.
 * 
 * Features:
 * - Split images into equal horizontal segments
 * - Support for various image formats
 * - Configurable number of splits
 * - Automatic output file naming
 */
public class ImageSplitter {
    
    private static final Logger logger = LoggerFactory.getLogger(ImageSplitter.class);
    
    static {
        try {
            // Load OpenCV native library
            nu.pattern.OpenCV.loadLocally();
            System.out.println("OpenCV loaded successfully for ImageSplitter");
        } catch (Exception e) {
            System.err.println("Failed to load OpenCV: " + e.getMessage());
        }
    }
    
    /**
     * Cuts a 590x360 picture into 6 equal sub-pictures based on width.
     * Each sub-picture will have dimensions of approximately 98x360 pixels.
     * 
     * @param inputImagePath Path to the input image file
     * @param outputDirectory Directory where the sub-pictures will be saved
     * @return List of paths to the generated sub-picture files
     * @throws IllegalArgumentException if input parameters are invalid
     * @throws RuntimeException if image processing fails
     */
    public List<String> splitImageInto6Parts(String inputImagePath, String outputDirectory) {
        return splitImageHorizontally(inputImagePath, outputDirectory, 6);
    }
    
    /**
     * Splits an image horizontally into a specified number of equal parts.
     * 
     * @param inputImagePath Path to the input image file
     * @param outputDirectory Directory where the sub-pictures will be saved
     * @param numberOfParts Number of parts to split the image into
     * @return List of paths to the generated sub-picture files
     * @throws IllegalArgumentException if input parameters are invalid
     * @throws RuntimeException if image processing fails
     */
    public List<String> splitImageHorizontally(String inputImagePath, String outputDirectory, int numberOfParts) {
        // Validate input parameters
        if (inputImagePath == null || inputImagePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Input image path cannot be null or empty");
        }
        
        if (outputDirectory == null || outputDirectory.trim().isEmpty()) {
            throw new IllegalArgumentException("Output directory cannot be null or empty");
        }
        
        if (numberOfParts <= 0) {
            throw new IllegalArgumentException("Number of parts must be greater than 0");
        }
        
        // Check if input file exists
        File inputFile = new File(inputImagePath);
        if (!inputFile.exists() || !inputFile.isFile()) {
            throw new IllegalArgumentException("Input image file does not exist: " + inputImagePath);
        }
        
        // Create output directory if it doesn't exist
        File outputDir = new File(outputDirectory);
        if (!outputDir.exists()) {
            if (!outputDir.mkdirs()) {
                throw new RuntimeException("Failed to create output directory: " + outputDirectory);
            }
        }
        
        // Read the input image
        Mat sourceImage = Imgcodecs.imread(inputImagePath);
        if (sourceImage.empty()) {
            throw new RuntimeException("Failed to read input image: " + inputImagePath);
        }
        
        // Get image dimensions
        int imageWidth = sourceImage.cols();
        int imageHeight = sourceImage.rows();
        
        logger.info("Processing image: {}x{} pixels", imageWidth, imageHeight);
        
        // Calculate the width of each part
        int partWidth = imageWidth / numberOfParts;
        int remainder = imageWidth % numberOfParts;
        
        logger.info("Splitting into {} parts, each {} pixels wide", numberOfParts, partWidth);
        
        List<String> outputPaths = new ArrayList<>();
        String baseFileName = getBaseFileName(inputImagePath);
        
        // Split the image into parts
        for (int i = 0; i < numberOfParts; i++) {
            // Calculate the starting x-coordinate for this part
            int startX = i * partWidth;
            
            // For the last part, include any remainder pixels
            int currentPartWidth = partWidth;
            if (i == numberOfParts - 1) {
                currentPartWidth += remainder;
            }
            
            // Create a region of interest (ROI) for this part
            Rect roi = new Rect(startX, 0, currentPartWidth, imageHeight);
            Mat subImage = new Mat(sourceImage, roi);
            
            // Generate output filename
            String outputFileName = String.format("%s_part_%02d.jpg", baseFileName, i + 1);
            String outputPath = new File(outputDirectory, outputFileName).getAbsolutePath();
            
            // Save the sub-image
            boolean success = Imgcodecs.imwrite(outputPath, subImage);
            if (!success) {
                throw new RuntimeException("Failed to save sub-image: " + outputPath);
            }
            
            outputPaths.add(outputPath);
            logger.info("Saved part {}: {} ({}x{} pixels)", i + 1, outputPath, currentPartWidth, imageHeight);
            
            // Release the sub-image Mat to free memory
            subImage.release();
        }
        
        // Release the source image Mat to free memory
        sourceImage.release();
        
        logger.info("Successfully split image into {} parts", numberOfParts);
        return outputPaths;
    }
    
    /**
     * Gets the base filename without extension from a file path.
     * 
     * @param filePath The file path
     * @return The base filename without extension
     */
    private String getBaseFileName(String filePath) {
        File file = new File(filePath);
        String fileName = file.getName();
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(0, lastDotIndex);
        }
        return fileName;
    }
    
    /**
     * Validates if an image has the expected dimensions.
     * 
     * @param imagePath Path to the image file
     * @param expectedWidth Expected width in pixels
     * @param expectedHeight Expected height in pixels
     * @return true if the image has the expected dimensions, false otherwise
     */
    public boolean validateImageDimensions(String imagePath, int expectedWidth, int expectedHeight) {
        Mat image = Imgcodecs.imread(imagePath);
        if (image.empty()) {
            return false;
        }
        
        boolean isValid = (image.cols() == expectedWidth && image.rows() == expectedHeight);
        image.release();
        return isValid;
    }
    
    /**
     * Gets the dimensions of an image.
     * 
     * @param imagePath Path to the image file
     * @return Array containing [width, height] or null if image cannot be read
     */
    public int[] getImageDimensions(String imagePath) {
        Mat image = Imgcodecs.imread(imagePath);
        if (image.empty()) {
            return null;
        }
        
        int[] dimensions = {image.cols(), image.rows()};
        image.release();
        return dimensions;
    }
}