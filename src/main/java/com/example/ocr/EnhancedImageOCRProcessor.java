package com.example.ocr;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * Enhanced Image OCR Processor specifically designed for rotated text and different colored text.
 * Handles challenges like:
 * - Text rotated at various angles
 * - Different text colors (light/dark text on various backgrounds)
 * - Complex museum/historical artifact images
 * - Mixed Chinese and English text
 */
public class EnhancedImageOCRProcessor extends ImageOCRProcessor {
    
    private static final Logger logger = LoggerFactory.getLogger(EnhancedImageOCRProcessor.class);
    
    // Rotation detection parameters
    private boolean enableRotationDetection = true;
    private double[] rotationAngles = {0, 90, 180, 270, -15, -10, -5, 5, 10, 15, 30, -30, 45, -45};
    
    // Color detection parameters
    private boolean enableMultiColorDetection = true;
    private int colorVariations = 5;
    
    /**
     * Constructor with enhanced capabilities
     */
    public EnhancedImageOCRProcessor() {
        super();
        setupEnhancedTesseract();
    }
    
    /**
     * Constructor with custom Tesseract data path
     */
    public EnhancedImageOCRProcessor(String tessDataPath) {
        super(tessDataPath);
        setupEnhancedTesseract();
    }
    
    /**
     * Setup enhanced Tesseract configuration for better accuracy with rotated/colored text
     */
    private void setupEnhancedTesseract() {
        if (tesseract != null) {
            // Enhanced configuration for challenging text
            tesseract.setTessVariable("tessedit_char_whitelist", "");
            tesseract.setTessVariable("classify_bln_numeric_mode", "0");
            tesseract.setTessVariable("textord_really_old_xheight", "1");
            tesseract.setTessVariable("textord_min_linesize", "2.5");
            tesseract.setTessVariable("preserve_interword_spaces", "1");
            tesseract.setTessVariable("user_defined_dpi", "300");
            
            // Enable orientation and script detection
            tesseract.setTessVariable("textord_equation_detect", "1");
            tesseract.setTessVariable("textord_tabfind_show_vlines", "1");
            
            logger.info("Enhanced Tesseract configuration applied");
        }
    }
    
    /**
     * Enhanced OCR processing with rotation and color handling
     */
    @Override
    public String processImage(String imagePath) throws IOException, TesseractException {
        logger.info("Enhanced processing image: {}", imagePath);
        
        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            throw new IOException("Image file not found: " + imagePath);
        }
        
        if (!isOpenCVLoaded) {
            logger.warn("OpenCV not available, using basic processing");
            return tesseract.doOCR(imageFile);
        }
        
        // Try multiple processing strategies
        List<ProcessingStrategy> strategies = createProcessingStrategies(imagePath);
        
        String bestResult = "";
        double bestConfidence = 0.0;
        
        for (ProcessingStrategy strategy : strategies) {
            try {
                String result = strategy.process();
                double confidence = calculateConfidence(result);
                
                logger.debug("Strategy: {} - Confidence: {:.2f} - Length: {}", 
                           strategy.getName(), confidence, result.length());
                
                if (confidence > bestConfidence && result.trim().length() > bestResult.trim().length()) {
                    bestResult = result;
                    bestConfidence = confidence;
                }
            } catch (Exception e) {
                logger.debug("Strategy {} failed: {}", strategy.getName(), e.getMessage());
            }
        }
        
        if (bestResult.trim().isEmpty()) {
            logger.warn("No text detected with any strategy for image: {}", imagePath);
            return "No text detected";
        }
        
        logger.info("Best result with confidence {:.2f}: {}", bestConfidence, 
                   bestResult.substring(0, Math.min(50, bestResult.length())));
        
        return bestResult;
    }
    
    /**
     * Create multiple processing strategies for different text conditions
     */
    private List<ProcessingStrategy> createProcessingStrategies(String imagePath) {
        List<ProcessingStrategy> strategies = new ArrayList<>();
        
        // Strategy 1: Original preprocessing
        strategies.add(new ProcessingStrategy("Original", () -> {
            Mat processed = preprocessImageWithOpenCV(imagePath);
            return tesseract.doOCR(matToBufferedImage(processed));
        }));
        
        // Strategy 2: Enhanced color detection
        strategies.add(new ProcessingStrategy("Enhanced Color", () -> {
            Mat processed = enhancedColorPreprocessing(imagePath);
            return tesseract.doOCR(matToBufferedImage(processed));
        }));
        
        // Strategy 3: Multiple rotations
        if (enableRotationDetection) {
            for (double angle : rotationAngles) {
                strategies.add(new ProcessingStrategy("Rotation " + angle, () -> {
                    Mat processed = preprocessWithRotation(imagePath, angle);
                    return tesseract.doOCR(matToBufferedImage(processed));
                }));
            }
        }
        
        // Strategy 4: Multiple thresholding techniques
        strategies.add(new ProcessingStrategy("Adaptive Threshold", () -> {
            Mat processed = multiThresholdPreprocessing(imagePath);
            return tesseract.doOCR(matToBufferedImage(processed));
        }));
        
        // Strategy 5: Edge detection based
        strategies.add(new ProcessingStrategy("Edge Detection", () -> {
            Mat processed = edgeBasedPreprocessing(imagePath);
            return tesseract.doOCR(matToBufferedImage(processed));
        }));
        
        return strategies;
    }
    
    /**
     * Enhanced color preprocessing for different colored text
     */
    private Mat enhancedColorPreprocessing(String imagePath) {
        Mat originalImage = Imgcodecs.imread(imagePath);
        if (originalImage.empty()) {
            throw new RuntimeException("Could not load image: " + imagePath);
        }
        
        // Convert to different color spaces and find the best one
        List<Mat> colorSpaceImages = new ArrayList<>();
        
        // Original BGR
        colorSpaceImages.add(originalImage.clone());
        
        // HSV color space
        Mat hsvImage = new Mat();
        Imgproc.cvtColor(originalImage, hsvImage, Imgproc.COLOR_BGR2HSV);
        colorSpaceImages.add(hsvImage);
        
        // LAB color space
        Mat labImage = new Mat();
        Imgproc.cvtColor(originalImage, labImage, Imgproc.COLOR_BGR2Lab);
        colorSpaceImages.add(labImage);
        
        // YUV color space
        Mat yuvImage = new Mat();
        Imgproc.cvtColor(originalImage, yuvImage, Imgproc.COLOR_BGR2YUV);
        colorSpaceImages.add(yuvImage);
        
        Mat bestResult = null;
        double bestVariance = 0;
        
        for (Mat colorImage : colorSpaceImages) {
            // Split channels and find the one with highest contrast
            List<Mat> channels = new ArrayList<>();
            Core.split(colorImage, channels);
            
            for (Mat channel : channels) {
                // Apply preprocessing to each channel
                Mat processed = preprocessSingleChannel(channel);
                
                // Calculate variance to find best contrast
                MatOfDouble mean = new MatOfDouble();
                MatOfDouble stddev = new MatOfDouble();
                Core.meanStdDev(processed, mean, stddev);
                double variance = stddev.get(0, 0)[0];
                
                if (variance > bestVariance) {
                    bestVariance = variance;
                    if (bestResult != null) bestResult.release();
                    bestResult = processed.clone();
                }
                
                processed.release();
            }
            
            // Clean up channels
            for (Mat channel : channels) {
                channel.release();
            }
        }
        
        // Clean up color space images
        for (Mat img : colorSpaceImages) {
            img.release();
        }
        
        return bestResult != null ? bestResult : preprocessSingleChannel(originalImage);
    }
    
    /**
     * Preprocess a single channel with enhanced techniques
     */
    private Mat preprocessSingleChannel(Mat channel) {
        Mat processed = new Mat();
        
        // Ensure single channel
        if (channel.channels() > 1) {
            Imgproc.cvtColor(channel, processed, Imgproc.COLOR_BGR2GRAY);
        } else {
            processed = channel.clone();
        }
        
        // Apply histogram equalization instead of CLAHE
        Mat equalizedResult = new Mat();
        Imgproc.equalizeHist(processed, equalizedResult);
        processed.release();
        processed = equalizedResult;
        
        // Apply bilateral filter to reduce noise while keeping edges sharp
        Mat filtered = new Mat();
        Imgproc.bilateralFilter(processed, filtered, 9, 75, 75);
        processed.release();
        processed = filtered;
        
        // Apply multiple threshold techniques and combine
        Mat otsuThresh = new Mat();
        Mat adaptiveThresh = new Mat();
        
        // Otsu's thresholding
        Imgproc.threshold(processed, otsuThresh, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
        
        // Adaptive thresholding
        Imgproc.adaptiveThreshold(processed, adaptiveThresh, 255, 
                                 Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, 
                                 Imgproc.THRESH_BINARY, 15, 10);
        
        // Combine both thresholding results
        Mat combined = new Mat();
        Core.bitwise_and(otsuThresh, adaptiveThresh, combined);
        
        // Morphological operations to clean up
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2));
        Mat morphed = new Mat();
        Imgproc.morphologyEx(combined, morphed, Imgproc.MORPH_CLOSE, kernel);
        
        // Clean up temporary matrices
        otsuThresh.release();
        adaptiveThresh.release();
        combined.release();
        kernel.release();
        processed.release();
        
        return morphed;
    }
    
    /**
     * Preprocessing with rotation correction
     */
    private Mat preprocessWithRotation(String imagePath, double angle) {
        Mat originalImage = Imgcodecs.imread(imagePath);
        if (originalImage.empty()) {
            throw new RuntimeException("Could not load image: " + imagePath);
        }
        
        // Rotate image
        Mat rotated = rotateImage(originalImage, angle);
        originalImage.release();
        
        // Apply standard preprocessing
        Mat processed = enhancedColorPreprocessing(rotated, angle);
        rotated.release();
        
        return processed;
    }
    
    /**
     * Enhanced color preprocessing for rotated images
     */
    private Mat enhancedColorPreprocessing(Mat image, double angle) {
        Mat grayImage = new Mat();
        if (image.channels() > 1) {
            Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
        } else {
            grayImage = image.clone();
        }
        
        // Apply preprocessing
        Mat processed = preprocessSingleChannel(grayImage);
        grayImage.release();
        
        return processed;
    }
    
    /**
     * Rotate image by specified angle
     */
    private Mat rotateImage(Mat image, double angle) {
        if (Math.abs(angle) < 0.1) {
            return image.clone();
        }
        
        org.opencv.core.Point center = new org.opencv.core.Point(image.cols() / 2.0, image.rows() / 2.0);
        Mat rotationMatrix = Imgproc.getRotationMatrix2D(center, angle, 1.0);
        
        Mat rotated = new Mat();
        Imgproc.warpAffine(image, rotated, rotationMatrix, image.size(), 
                          Imgproc.INTER_CUBIC, Core.BORDER_REPLICATE);
        
        rotationMatrix.release();
        return rotated;
    }
    
    /**
     * Multiple thresholding preprocessing
     */
    private Mat multiThresholdPreprocessing(String imagePath) {
        Mat originalImage = Imgcodecs.imread(imagePath);
        if (originalImage.empty()) {
            throw new RuntimeException("Could not load image: " + imagePath);
        }
        
        Mat grayImage = new Mat();
        Imgproc.cvtColor(originalImage, grayImage, Imgproc.COLOR_BGR2GRAY);
        originalImage.release();
        
        // Try multiple threshold values and combine results
        List<Mat> thresholdResults = new ArrayList<>();
        int[] thresholdValues = {100, 120, 140, 160, 180};
        
        for (int threshold : thresholdValues) {
            Mat thresholded = new Mat();
            Imgproc.threshold(grayImage, thresholded, threshold, 255, Imgproc.THRESH_BINARY);
            thresholdResults.add(thresholded);
        }
        
        // Combine all threshold results
        Mat combined = thresholdResults.get(0).clone();
        for (int i = 1; i < thresholdResults.size(); i++) {
            Mat temp = new Mat();
            Core.bitwise_or(combined, thresholdResults.get(i), temp);
            combined.release();
            combined = temp;
        }
        
        // Clean up
        grayImage.release();
        for (Mat result : thresholdResults) {
            result.release();
        }
        
        return combined;
    }
    
    /**
     * Edge detection based preprocessing
     */
    private Mat edgeBasedPreprocessing(String imagePath) {
        Mat originalImage = Imgcodecs.imread(imagePath);
        if (originalImage.empty()) {
            throw new RuntimeException("Could not load image: " + imagePath);
        }
        
        Mat grayImage = new Mat();
        Imgproc.cvtColor(originalImage, grayImage, Imgproc.COLOR_BGR2GRAY);
        originalImage.release();
        
        // Apply Gaussian blur
        Mat blurred = new Mat();
        Imgproc.GaussianBlur(grayImage, blurred, new Size(5, 5), 0);
        grayImage.release();
        
        // Apply Canny edge detection
        Mat edges = new Mat();
        Imgproc.Canny(blurred, edges, 50, 150);
        blurred.release();
        
        // Dilate edges to connect text components
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Mat dilated = new Mat();
        Imgproc.dilate(edges, dilated, kernel, new Point(-1, -1), 2);
        edges.release();
        kernel.release();
        
        // Invert for OCR (text should be black on white background)
        Mat inverted = new Mat();
        Core.bitwise_not(dilated, inverted);
        dilated.release();
        
        return inverted;
    }
    
    /**
     * Calculate confidence score for OCR result
     */
    private double calculateConfidence(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0.0;
        }
        
        double confidence = 0.0;
        
        // Length factor (longer text generally more reliable)
        confidence += Math.min(text.trim().length() * 0.1, 5.0);
        
        // Chinese character detection (higher weight for Chinese text)
        long chineseCount = text.chars()
            .filter(c -> Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN)
            .count();
        confidence += chineseCount * 0.5;
        
        // English character detection
        long englishCount = text.chars()
            .filter(c -> (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z'))
            .count();
        confidence += englishCount * 0.2;
        
        // Penalize for too many special characters or gibberish
        long specialCount = text.chars()
            .filter(c -> !Character.isLetterOrDigit(c) && !Character.isWhitespace(c))
            .count();
        confidence -= specialCount * 0.1;
        
        return Math.max(0.0, confidence);
    }
    
    /**
     * Detect optimal rotation angle for text
     */
    public double detectOptimalRotation(String imagePath) {
        if (!isOpenCVLoaded) {
            return 0.0;
        }
        
        double bestAngle = 0.0;
        double bestScore = 0.0;
        
        for (double angle : rotationAngles) {
            try {
                Mat processed = preprocessWithRotation(imagePath, angle);
                double score = calculateTextScore(processed);
                processed.release();
                
                if (score > bestScore) {
                    bestScore = score;
                    bestAngle = angle;
                }
            } catch (Exception e) {
                logger.debug("Failed to test rotation angle {}: {}", angle, e.getMessage());
            }
        }
        
        logger.info("Optimal rotation angle detected: {} degrees", bestAngle);
        return bestAngle;
    }
    
    /**
     * Calculate a score indicating how much text-like content is in the image
     */
    private double calculateTextScore(Mat image) {
        // Find contours and analyze their characteristics
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(image, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        
        double score = 0.0;
        
        for (MatOfPoint contour : contours) {
            Rect boundingRect = Imgproc.boundingRect(contour);
            double area = Imgproc.contourArea(contour);
            double aspectRatio = (double) boundingRect.width / boundingRect.height;
            
            // Score based on text-like characteristics
            if (area > 50 && area < 10000 && aspectRatio > 0.1 && aspectRatio < 10) {
                score += area * 0.001;
            }
        }
        
        hierarchy.release();
        return score;
    }
    
    /**
     * Process image with automatic rotation detection
     */
    public String processImageWithAutoRotation(String imagePath) throws IOException, TesseractException {
        double optimalAngle = detectOptimalRotation(imagePath);
        
        if (Math.abs(optimalAngle) > 0.1) {
            logger.info("Applying rotation correction: {} degrees", optimalAngle);
            Mat processed = preprocessWithRotation(imagePath, optimalAngle);
            String result = tesseract.doOCR(matToBufferedImage(processed));
            processed.release();
            return result;
        } else {
            return processImage(imagePath);
        }
    }
    
    // Getters and setters for configuration
    public boolean isRotationDetectionEnabled() { return enableRotationDetection; }
    public void setRotationDetectionEnabled(boolean enabled) { this.enableRotationDetection = enabled; }
    
    public boolean isMultiColorDetectionEnabled() { return enableMultiColorDetection; }
    public void setMultiColorDetectionEnabled(boolean enabled) { this.enableMultiColorDetection = enabled; }
    
    public void setRotationAngles(double[] angles) { this.rotationAngles = angles.clone(); }
    public double[] getRotationAngles() { return rotationAngles.clone(); }
    
    /**
     * Inner class for processing strategies
     */
    private static class ProcessingStrategy {
        private final String name;
        private final ProcessingFunction function;
        
        public ProcessingStrategy(String name, ProcessingFunction function) {
            this.name = name;
            this.function = function;
        }
        
        public String getName() { return name; }
        
        public String process() throws Exception {
            return function.process();
        }
    }
    
    @FunctionalInterface
    private interface ProcessingFunction {
        String process() throws Exception;
    }
}