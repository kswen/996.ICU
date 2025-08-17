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
 * Advanced Image OCR Processor using OpenCV for image preprocessing 
 * and Tess4J for text recognition with Chinese language support.
 * 
 * Features:
 * - Image preprocessing with noise reduction
 * - Contrast and brightness enhancement
 * - Text region detection and isolation
 * - Multi-language OCR support (Chinese + English)
 * - Batch processing capabilities
 */
public class ImageOCRProcessor {
    
    private static final Logger logger = LoggerFactory.getLogger(ImageOCRProcessor.class);
    
    protected ITesseract tesseract;
    protected boolean isOpenCVLoaded = false;
    
    // Configuration parameters
    private double contrastAlpha = 1.5;  // Contrast enhancement factor
    private int brightnessBeta = 30;     // Brightness adjustment
    private int gaussianKernelSize = 3;  // Gaussian blur kernel size
    private double thresholdValue = 128; // Binary threshold value
    
    static {
        try {
            // Load OpenCV native library
            nu.pattern.OpenCV.loadLocally();
            System.out.println("OpenCV loaded successfully");
        } catch (Exception e) {
            System.err.println("Failed to load OpenCV: " + e.getMessage());
        }
    }
    
    /**
     * Constructor initializes Tesseract with Chinese and English language support
     */
    public ImageOCRProcessor() {
        initializeTesseract();
        this.isOpenCVLoaded = checkOpenCVStatus();
    }
    
    /**
     * Constructor with custom Tesseract data path
     * @param tessDataPath Path to Tesseract data directory
     */
    public ImageOCRProcessor(String tessDataPath) {
        initializeTesseract(tessDataPath);
        this.isOpenCVLoaded = checkOpenCVStatus();
    }
    
    /**
     * Initialize Tesseract with default settings
     */
    private void initializeTesseract() {
        tesseract = new Tesseract();
        
        // Set language to Chinese (Simplified) + English
        tesseract.setLanguage("chi_sim+eng");
        
        // Configure OCR engine mode (LSTM + Legacy)
        tesseract.setOcrEngineMode(1);
        
        // Set page segmentation mode (automatic page segmentation)
        tesseract.setPageSegMode(6);
        
        // Additional configurations for better accuracy
        tesseract.setTessVariable("user_defined_dpi", "300");
        tesseract.setTessVariable("textord_really_old_xheight", "1");
        tesseract.setTessVariable("preserve_interword_spaces", "1");
        
        logger.info("Tesseract initialized with Chinese and English language support");
    }
    
    /**
     * Initialize Tesseract with custom data path
     * @param tessDataPath Custom Tesseract data directory path
     */
    private void initializeTesseract(String tessDataPath) {
        tesseract = new Tesseract();
        tesseract.setDatapath(tessDataPath);
        
        // Apply same configuration as default initialization
        tesseract.setLanguage("chi_sim+eng");
        tesseract.setOcrEngineMode(1);
        tesseract.setPageSegMode(6);
        tesseract.setTessVariable("user_defined_dpi", "300");
        tesseract.setTessVariable("textord_really_old_xheight", "1");
        tesseract.setTessVariable("preserve_interword_spaces", "1");
        
        logger.info("Tesseract initialized with custom data path: {}", tessDataPath);
    }
    
    /**
     * Check if OpenCV is properly loaded
     * @return true if OpenCV is available, false otherwise
     */
    private boolean checkOpenCVStatus() {
        try {
            Mat testMat = new Mat();
            return true;
        } catch (Exception e) {
            logger.warn("OpenCV not properly loaded, some preprocessing features may not work");
            return false;
        }
    }
    
    /**
     * Main OCR processing method
     * @param imagePath Path to the image file
     * @return Extracted text from the image
     * @throws IOException If image cannot be read
     * @throws TesseractException If OCR processing fails
     */
    public String processImage(String imagePath) throws IOException, TesseractException {
        logger.info("Processing image: {}", imagePath);
        
        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            throw new IOException("Image file not found: " + imagePath);
        }
        
        if (isOpenCVLoaded) {
            // Use OpenCV for advanced preprocessing
            Mat processedImage = preprocessImageWithOpenCV(imagePath);
            BufferedImage bufferedImage = matToBufferedImage(processedImage);
            return tesseract.doOCR(bufferedImage);
        } else {
            // Fallback to direct processing without OpenCV preprocessing
            logger.warn("Using fallback processing without OpenCV preprocessing");
            return tesseract.doOCR(imageFile);
        }
    }
    
    /**
     * Process image with custom preprocessing parameters
     * @param imagePath Path to the image file
     * @param customContrastAlpha Custom contrast enhancement factor
     * @param customBrightnessBeta Custom brightness adjustment
     * @return Extracted text from the image
     * @throws IOException If image cannot be read
     * @throws TesseractException If OCR processing fails
     */
    public String processImageWithCustomParams(String imagePath, 
                                             double customContrastAlpha, 
                                             int customBrightnessBeta) 
                                             throws IOException, TesseractException {
        
        double originalAlpha = this.contrastAlpha;
        int originalBeta = this.brightnessBeta;
        
        try {
            this.contrastAlpha = customContrastAlpha;
            this.brightnessBeta = customBrightnessBeta;
            return processImage(imagePath);
        } finally {
            // Restore original parameters
            this.contrastAlpha = originalAlpha;
            this.brightnessBeta = originalBeta;
        }
    }
    
    /**
     * Advanced image preprocessing using OpenCV
     * @param imagePath Path to the input image
     * @return Processed Mat object
     */
    protected Mat preprocessImageWithOpenCV(String imagePath) {
        // Load image
        Mat originalImage = Imgcodecs.imread(imagePath);
        if (originalImage.empty()) {
            throw new RuntimeException("Could not load image: " + imagePath);
        }
        
        logger.debug("Original image size: {}x{}", originalImage.width(), originalImage.height());
        
        // Convert to grayscale
        Mat grayImage = new Mat();
        Imgproc.cvtColor(originalImage, grayImage, Imgproc.COLOR_BGR2GRAY);
        
        // Apply Gaussian blur to reduce noise
        Mat blurredImage = new Mat();
        Size kernelSize = new Size(gaussianKernelSize, gaussianKernelSize);
        Imgproc.GaussianBlur(grayImage, blurredImage, kernelSize, 0);
        
        // Enhance contrast and brightness
        Mat enhancedImage = new Mat();
        blurredImage.convertTo(enhancedImage, -1, contrastAlpha, brightnessBeta);
        
        // Apply adaptive threshold for better text extraction
        Mat thresholdImage = new Mat();
        Imgproc.adaptiveThreshold(enhancedImage, thresholdImage, 255, 
                                 Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, 
                                 Imgproc.THRESH_BINARY, 11, 2);
        
        // Apply morphological operations to clean up the image
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2));
        Mat morphImage = new Mat();
        Imgproc.morphologyEx(thresholdImage, morphImage, Imgproc.MORPH_CLOSE, kernel);
        
        // Optional: Apply dilation to make text thicker (better for OCR)
        Mat dilatedImage = new Mat();
        Imgproc.dilate(morphImage, dilatedImage, kernel, new Point(-1, -1), 1);
        
        logger.debug("Image preprocessing completed");
        return dilatedImage;
    }
    
    /**
     * Convert OpenCV Mat to BufferedImage
     * @param mat OpenCV Mat object
     * @return BufferedImage for Tesseract processing
     */
    protected BufferedImage matToBufferedImage(Mat mat) {
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".png", mat, matOfByte);
        byte[] byteArray = matOfByte.toArray();
        
        try {
            return ImageIO.read(new ByteArrayInputStream(byteArray));
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert Mat to BufferedImage", e);
        }
    }
    
    /**
     * Detect and extract text regions from image
     * @param imagePath Path to the image file
     * @return List of detected text regions as BufferedImages
     */
    public List<BufferedImage> detectTextRegions(String imagePath) {
        List<BufferedImage> textRegions = new ArrayList<>();
        
        if (!isOpenCVLoaded) {
            logger.warn("OpenCV not available, cannot detect text regions");
            return textRegions;
        }
        
        try {
            Mat originalImage = Imgcodecs.imread(imagePath);
            Mat grayImage = new Mat();
            Imgproc.cvtColor(originalImage, grayImage, Imgproc.COLOR_BGR2GRAY);
            
            // Apply MSER (Maximally Stable Extremal Regions) for text detection
            // Note: This is a simplified approach, more sophisticated methods exist
            Mat binaryImage = new Mat();
            Imgproc.threshold(grayImage, binaryImage, 0, 255, 
                            Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
            
            // Find contours
            List<MatOfPoint> contours = new ArrayList<>();
            Mat hierarchy = new Mat();
            Imgproc.findContours(binaryImage, contours, hierarchy, 
                               Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
            
            // Filter contours by area and aspect ratio to find text regions
            for (MatOfPoint contour : contours) {
                Rect boundingRect = Imgproc.boundingRect(contour);
                double area = Imgproc.contourArea(contour);
                double aspectRatio = (double) boundingRect.width / boundingRect.height;
                
                // Filter based on area and aspect ratio (typical for text)
                if (area > 100 && aspectRatio > 0.5 && aspectRatio < 10) {
                    Mat textRegion = new Mat(originalImage, boundingRect);
                    textRegions.add(matToBufferedImage(textRegion));
                }
            }
            
            logger.info("Detected {} potential text regions", textRegions.size());
            
        } catch (Exception e) {
            logger.error("Error detecting text regions", e);
        }
        
        return textRegions;
    }
    
    /**
     * Batch process multiple images
     * @param imagePaths List of image file paths
     * @return List of extracted text results
     */
    public List<OCRResult> batchProcess(List<String> imagePaths) {
        List<OCRResult> results = new ArrayList<>();
        
        for (String imagePath : imagePaths) {
            try {
                String extractedText = processImage(imagePath);
                results.add(new OCRResult(imagePath, extractedText, true, null));
                logger.info("Successfully processed: {}", imagePath);
            } catch (Exception e) {
                results.add(new OCRResult(imagePath, null, false, e.getMessage()));
                logger.error("Failed to process: {}", imagePath, e);
            }
        }
        
        return results;
    }
    
    /**
     * Get current preprocessing parameters
     * @return OCRConfig object with current settings
     */
    public OCRConfig getConfiguration() {
        return new OCRConfig(contrastAlpha, brightnessBeta, gaussianKernelSize, thresholdValue);
    }
    
    /**
     * Update preprocessing parameters
     * @param config New configuration settings
     */
    public void updateConfiguration(OCRConfig config) {
        this.contrastAlpha = config.getContrastAlpha();
        this.brightnessBeta = config.getBrightnessBeta();
        this.gaussianKernelSize = config.getGaussianKernelSize();
        this.thresholdValue = config.getThresholdValue();
        logger.info("OCR configuration updated");
    }
    
    /**
     * Check if OCR processor is properly initialized
     * @return true if ready for processing, false otherwise
     */
    public boolean isReady() {
        return tesseract != null;
    }
    
    /**
     * Get version information
     * @return Version information string
     */
    public String getVersionInfo() {
        StringBuilder info = new StringBuilder();
        info.append("ImageOCRProcessor v1.0\n");
        info.append("OpenCV Status: ").append(isOpenCVLoaded ? "Loaded" : "Not Available").append("\n");
        info.append("Tesseract Status: ").append(tesseract != null ? "Initialized" : "Not Initialized").append("\n");
        info.append("Tesseract Version: Tess4J 5.9.0\n");
        
        return info.toString();
    }
    
    /**
     * Clean up resources
     */
    public void cleanup() {
        if (tesseract != null) {
            try {
                // Tesseract cleanup is handled automatically by Tess4J
                logger.info("Tesseract resources cleaned up");
            } catch (Exception e) {
                logger.warn("Error during cleanup", e);
            }
        }
    }
    
    /**
     * Result class for OCR operations
     */
    public static class OCRResult {
        private final String imagePath;
        private final String extractedText;
        private final boolean success;
        private final String errorMessage;
        
        public OCRResult(String imagePath, String extractedText, boolean success, String errorMessage) {
            this.imagePath = imagePath;
            this.extractedText = extractedText;
            this.success = success;
            this.errorMessage = errorMessage;
        }
        
        // Getters
        public String getImagePath() { return imagePath; }
        public String getExtractedText() { return extractedText; }
        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
        
        @Override
        public String toString() {
            return String.format("OCRResult{path='%s', success=%s, text='%s', error='%s'}", 
                               imagePath, success, 
                               extractedText != null ? extractedText.substring(0, Math.min(50, extractedText.length())) + "..." : "null",
                               errorMessage);
        }
    }
    
    /**
     * Configuration class for OCR preprocessing parameters
     */
    public static class OCRConfig {
        private double contrastAlpha;
        private int brightnessBeta;
        private int gaussianKernelSize;
        private double thresholdValue;
        
        public OCRConfig(double contrastAlpha, int brightnessBeta, int gaussianKernelSize, double thresholdValue) {
            this.contrastAlpha = contrastAlpha;
            this.brightnessBeta = brightnessBeta;
            this.gaussianKernelSize = gaussianKernelSize;
            this.thresholdValue = thresholdValue;
        }
        
        // Getters and setters
        public double getContrastAlpha() { return contrastAlpha; }
        public void setContrastAlpha(double contrastAlpha) { this.contrastAlpha = contrastAlpha; }
        
        public int getBrightnessBeta() { return brightnessBeta; }
        public void setBrightnessBeta(int brightnessBeta) { this.brightnessBeta = brightnessBeta; }
        
        public int getGaussianKernelSize() { return gaussianKernelSize; }
        public void setGaussianKernelSize(int gaussianKernelSize) { this.gaussianKernelSize = gaussianKernelSize; }
        
        public double getThresholdValue() { return thresholdValue; }
        public void setThresholdValue(double thresholdValue) { this.thresholdValue = thresholdValue; }
        
        @Override
        public String toString() {
            return String.format("OCRConfig{contrast=%.2f, brightness=%d, gaussianKernel=%d, threshold=%.2f}", 
                               contrastAlpha, brightnessBeta, gaussianKernelSize, thresholdValue);
        }
    }
}