package com.example.ocr;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * Demo class showing how to use the ImageSplitter functionality.
 * Compatible with Java 1.8.
 */
public class ImageSplitterDemo {
    
    /**
     * Creates a sample 590x360 image for testing purposes.
     * The image will have different colored sections to make splitting visible.
     * 
     * @param outputPath Path where the sample image will be saved
     * @throws IOException if there's an error writing the image
     */
    public static void createSampleImage(String outputPath) throws IOException {
        int width = 590;
        int height = 360;
        
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        // Create 6 different colored sections to visualize the splitting
        Color[] colors = {
            new Color(255, 0, 0),     // Red
            new Color(0, 255, 0),     // Green
            new Color(0, 0, 255),     // Blue
            new Color(255, 255, 0),   // Yellow
            new Color(255, 0, 255),   // Magenta
            new Color(0, 255, 255)    // Cyan
        };
        
        int sectionWidth = width / 6;
        for (int i = 0; i < 6; i++) {
            g2d.setColor(colors[i]);
            int x = i * sectionWidth;
            int currentWidth = (i == 5) ? width - x : sectionWidth; // Handle remaining pixels for last section
            g2d.fillRect(x, 0, currentWidth, height);
            
            // Add text to identify each section
            g2d.setColor(Color.BLACK);
            g2d.drawString("Section " + (i + 1), x + 10, height / 2);
        }
        
        g2d.dispose();
        
        // Save the image
        File outputFile = new File(outputPath);
        ImageIO.write(image, "png", outputFile);
        System.out.println("Sample image created: " + outputFile.getAbsolutePath());
    }
    
    /**
     * Demonstrates the image splitting functionality.
     */
    public static void demonstrateImageSplitting() {
        try {
            // Create directories
            String sampleImagePath = "sample_590x360.png";
            String outputDirectory = "split_images";
            
            System.out.println("=== ImageSplitter Demo ===");
            System.out.println();
            
            // Step 1: Create a sample image
            System.out.println("1. Creating sample 590x360 image...");
            createSampleImage(sampleImagePath);
            
            // Step 2: Split the image
            System.out.println();
            System.out.println("2. Splitting image into 6 equal parts...");
            List<File> subImages = ImageSplitter.cutImageIntoSixParts(sampleImagePath, outputDirectory);
            
            // Step 3: Display results
            System.out.println();
            System.out.println("3. Results:");
            System.out.println("Successfully created " + subImages.size() + " sub-images:");
            for (int i = 0; i < subImages.size(); i++) {
                File file = subImages.get(i);
                BufferedImage img = ImageIO.read(file);
                System.out.println("   - " + file.getName() + " (" + img.getWidth() + "x" + img.getHeight() + ")");
            }
            
            System.out.println();
            System.out.println("Demo completed successfully!");
            System.out.println("Check the '" + outputDirectory + "' directory for the split images.");
            
        } catch (IOException e) {
            System.err.println("Error during demo: " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid input: " + e.getMessage());
        }
    }
    
    /**
     * Main method to run the demo.
     */
    public static void main(String[] args) {
        demonstrateImageSplitting();
    }
}