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
 * Column-Based OCR Processor specifically designed for images with evenly spaced Chinese characters.
 * 
 * This processor is optimized for:
 * - Images with 6 Chinese characters evenly distributed
 * - Image size: 590x360 pixels
 * - Each character occupies approximately 98px width (590/6 ≈ 98)
 * - Background template matching for better accuracy
 */
public class ColumnBasedOCRProcessor {
    
    private static final Logger logger = LoggerFactory.getLogger(ColumnBasedOCRProcessor.class);
    
    // Image dimensions and column configuration
    private int imageWidth = 590;
    private int imageHeight = 360;
    private int columnCount = 6;
    private int columnWidth;
    private int marginBuffer = 5; // Extra pixels around each character for better OCR
    
    // OCR configuration
    private ITesseract tesseract;
    private boolean isOpenCVLoaded = false;
    
    // Background template (optional)
    private Mat backgroundTemplate = null;
    private String backgroundTemplatePath = null;
    
    static {
        try {
            nu.pattern.OpenCV.loadLocally();
            System.out.println("OpenCV loaded successfully for ColumnBasedOCRProcessor");
        } catch (Exception e) {
            System.err.println("Failed to load OpenCV: " + e.getMessage());
        }
    }
    
    /**
     * Constructor with default image dimensions (590x360, 6 columns)
     */
    public ColumnBasedOCRProcessor() {
        this(590, 360, 6);
    }
    
    /**
     * Constructor with custom dimensions
     * @param imageWidth Width of the input image
     * @param imageHeight Height of the input image  
     * @param columnCount Number of columns (characters) to extract
     */
    public ColumnBasedOCRProcessor(int imageWidth, int imageHeight, int columnCount) {
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.columnCount = columnCount;
        this.columnWidth = imageWidth / columnCount;
        
        initializeTesseract();
        this.isOpenCVLoaded = checkOpenCVStatus();
        
        logger.info("ColumnBasedOCRProcessor initialized: {}x{} image, {} columns of {}px width", 
                   imageWidth, imageHeight, columnCount, columnWidth);
    }
    
    /**
     * Initialize Tesseract for Chinese character recognition
     */
    private void initializeTesseract() {
        tesseract = new Tesseract();
        
        // Optimize for Chinese characters
        tesseract.setLanguage("chi_sim");
        tesseract.setOcrEngineMode(1); // LSTM + Legacy
        tesseract.setPageSegMode(8);   // Single character mode
        
        // Configuration for single character recognition
        tesseract.setTessVariable("user_defined_dpi", "300");
        tesseract.setTessVariable("tessedit_char_whitelist", "");
        tesseract.setTessVariable("classify_bln_numeric_mode", "0");
        tesseract.setTessVariable("textord_really_old_xheight", "1");
        
        // Disable word/line detection since we're processing single characters
        tesseract.setTessVariable("textord_equation_detect", "0");
        tesseract.setTessVariable("textord_tabfind_show_vlines", "0");
        
        logger.info("Tesseract initialized for single Chinese character recognition");
    }
    
    /**
     * Check if OpenCV is properly loaded
     */
    private boolean checkOpenCVStatus() {
        try {
            Mat testMat = new Mat();
            testMat.release();
            return true;
        } catch (Exception e) {
            logger.warn("OpenCV not properly loaded, using basic processing");
            return false;
        }
    }
    
    /**
     * Set background template for improved accuracy
     * @param backgroundPath Path to the background template image
     */
    public void setBackgroundTemplate(String backgroundPath) {
        try {
            this.backgroundTemplatePath = backgroundPath;
            if (isOpenCVLoaded) {
                this.backgroundTemplate = Imgcodecs.imread(backgroundPath);
                if (backgroundTemplate.empty()) {
                    logger.warn("Could not load background template: {}", backgroundPath);
                    this.backgroundTemplate = null;
                } else {
                    logger.info("Background template loaded: {}", backgroundPath);
                }
            }
        } catch (Exception e) {
            logger.error("Error loading background template", e);
            this.backgroundTemplate = null;
        }
    }
    
    /**
     * Process image by splitting into columns and recognizing each character
     * @param imagePath Path to the input image
     * @return Array of recognized characters (one per column)
     */
    public String[] processImageByColumns(String imagePath) throws IOException, TesseractException {
        logger.info("Processing image by columns: {}", imagePath);
        
        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            throw new IOException("Image file not found: " + imagePath);
        }
        
        if (!isOpenCVLoaded) {
            logger.warn("OpenCV not available, using basic column splitting");
            return processImageByColumnsBasic(imagePath);
        }
        
        // Load the image
        Mat originalImage = Imgcodecs.imread(imagePath);
        if (originalImage.empty()) {
            throw new IOException("Could not load image: " + imagePath);
        }
        
        logger.debug("Original image size: {}x{}", originalImage.width(), originalImage.height());
        
        // Adjust dimensions if image size is different from expected
        if (originalImage.width() != imageWidth || originalImage.height() != imageHeight) {
            logger.info("Resizing image from {}x{} to {}x{}", 
                       originalImage.width(), originalImage.height(), imageWidth, imageHeight);
            Mat resized = new Mat();
            Imgproc.resize(originalImage, resized, new Size(imageWidth, imageHeight));
            originalImage.release();
            originalImage = resized;
        }
        
        // Remove background if template is available
        Mat processedImage = removeBackground(originalImage);
        
        // Split into columns and process each
        String[] results = new String[columnCount];
        
        for (int i = 0; i < columnCount; i++) {
            try {
                Mat columnImage = extractColumn(processedImage, i);
                String character = processColumnImage(columnImage, i);
                results[i] = character;
                columnImage.release();
                
                logger.debug("Column {} result: '{}'", i, character);
            } catch (Exception e) {
                logger.warn("Error processing column {}: {}", i, e.getMessage());
                results[i] = "";
            }
        }
        
        originalImage.release();
        processedImage.release();
        
        return results;
    }
    
    /**
     * Remove background using template matching (if template is available)
     */
    private Mat removeBackground(Mat originalImage) {
        if (backgroundTemplate == null) {
            return originalImage.clone();
        }
        
        try {
            // Resize background template to match image size
            Mat resizedTemplate = new Mat();
            Imgproc.resize(backgroundTemplate, resizedTemplate, originalImage.size());
            
            // Subtract background
            Mat result = new Mat();
            Core.absdiff(originalImage, resizedTemplate, result);
            
            // Enhance the difference
            Mat enhanced = new Mat();
            result.convertTo(enhanced, -1, 2.0, 30); // Increase contrast and brightness
            
            resizedTemplate.release();
            result.release();
            
            logger.debug("Background removal applied");
            return enhanced;
        } catch (Exception e) {
            logger.warn("Error in background removal, using original image: {}", e.getMessage());
            return originalImage.clone();
        }
    }
    
    /**
     * Extract a specific column from the image
     * @param image Source image
     * @param columnIndex Column index (0-based)
     * @return Extracted column image
     */
    private Mat extractColumn(Mat image, int columnIndex) {
        // Calculate column boundaries with margin buffer
        int startX = Math.max(0, columnIndex * columnWidth - marginBuffer);
        int endX = Math.min(image.width(), (columnIndex + 1) * columnWidth + marginBuffer);
        int actualWidth = endX - startX;
        
        // Extract the column
        Rect columnRect = new Rect(startX, 0, actualWidth, image.height());
        Mat columnImage = new Mat(image, columnRect);
        
        logger.debug("Extracted column {}: x={}-{}, width={}", columnIndex, startX, endX, actualWidth);
        
        return columnImage.clone();
    }
    
    /**
     * Process a single column image to extract the character
     */
    private String processColumnImage(Mat columnImage, int columnIndex) throws TesseractException {
        // Apply preprocessing optimized for single character
        Mat processed = preprocessSingleCharacter(columnImage);
        
        // Convert to BufferedImage for Tesseract
        BufferedImage bufferedImage = matToBufferedImage(processed);
        processed.release();
        
        // Save debug image if needed
        if (logger.isDebugEnabled()) {
            try {
                File debugDir = new File("debug-columns");
                if (!debugDir.exists()) {
                    debugDir.mkdirs();
                }
                ImageIO.write(bufferedImage, "png", new File(debugDir, "column_" + columnIndex + ".png"));
            } catch (IOException e) {
                logger.debug("Could not save debug image for column {}", columnIndex);
            }
        }
        
        // Perform OCR
        String result = tesseract.doOCR(bufferedImage);
        
        // Clean up the result (remove whitespace, newlines)
        result = result.trim().replaceAll("\\s+", "");
        
        // If result is empty or too long, it might be noise
        if (result.length() > 3) {
            logger.debug("Column {} result too long ({}), likely noise: '{}'", columnIndex, result.length(), result);
            result = "";
        }
        
        return result;
    }
    
    /**
     * Preprocessing optimized for single Chinese character recognition
     */
    private Mat preprocessSingleCharacter(Mat columnImage) {
        Mat processed = new Mat();
        
        // Convert to grayscale
        if (columnImage.channels() > 1) {
            Imgproc.cvtColor(columnImage, processed, Imgproc.COLOR_BGR2GRAY);
        } else {
            processed = columnImage.clone();
        }
        
        // Apply histogram equalization for better contrast
        Mat equalized = new Mat();
        Imgproc.equalizeHist(processed, equalized);
        processed.release();
        processed = equalized;
        
        // Apply Gaussian blur to reduce noise
        Mat blurred = new Mat();
        Imgproc.GaussianBlur(processed, blurred, new Size(3, 3), 0);
        processed.release();
        processed = blurred;
        
        // Apply adaptive threshold - this is crucial for character recognition
        Mat thresholded = new Mat();
        Imgproc.adaptiveThreshold(processed, thresholded, 255, 
                                 Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, 
                                 Imgproc.THRESH_BINARY, 15, 10);
        processed.release();
        processed = thresholded;
        
        // Apply morphological operations to clean up the character
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2));
        Mat morphed = new Mat();
        Imgproc.morphologyEx(processed, morphed, Imgproc.MORPH_CLOSE, kernel);
        processed.release();
        kernel.release();
        
        // Optional: Apply dilation to make characters thicker (better for OCR)
        Mat dilationKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, 1));
        Mat dilated = new Mat();
        Imgproc.dilate(morphed, dilated, dilationKernel, new Point(-1, -1), 1);
        morphed.release();
        dilationKernel.release();
        
        return dilated;
    }
    
    /**
     * Convert OpenCV Mat to BufferedImage
     */
    private BufferedImage matToBufferedImage(Mat mat) {
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
     * Basic column processing without OpenCV (fallback)
     */
    private String[] processImageByColumnsBasic(String imagePath) throws IOException, TesseractException {
        logger.info("Using basic column processing (no OpenCV)");
        
        BufferedImage originalImage = ImageIO.read(new File(imagePath));
        
        // Resize if necessary
        if (originalImage.getWidth() != imageWidth || originalImage.getHeight() != imageHeight) {
            java.awt.Image scaledImage = originalImage.getScaledInstance(imageWidth, imageHeight, java.awt.Image.SCALE_SMOOTH);
            BufferedImage resizedImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
            resizedImage.getGraphics().drawImage(scaledImage, 0, 0, null);
            originalImage = resizedImage;
        }
        
        String[] results = new String[columnCount];
        
        for (int i = 0; i < columnCount; i++) {
            try {
                // Extract column
                int startX = Math.max(0, i * columnWidth - marginBuffer);
                int endX = Math.min(originalImage.getWidth(), (i + 1) * columnWidth + marginBuffer);
                int actualWidth = endX - startX;
                
                BufferedImage columnImage = originalImage.getSubimage(startX, 0, actualWidth, originalImage.getHeight());
                
                // Process with Tesseract
                String result = tesseract.doOCR(columnImage);
                result = result.trim().replaceAll("\\s+", "");
                
                if (result.length() > 3) {
                    result = ""; // Likely noise
                }
                
                results[i] = result;
                logger.debug("Column {} result: '{}'", i, result);
                
            } catch (Exception e) {
                logger.warn("Error processing column {}: {}", i, e.getMessage());
                results[i] = "";
            }
        }
        
        return results;
    }
    
    /**
     * Process image and return combined result as a single string
     */
    public String processImage(String imagePath) throws IOException, TesseractException {
        String[] characters = processImageByColumns(imagePath);
        StringBuilder result = new StringBuilder();
        
        for (String character : characters) {
            if (character != null && !character.trim().isEmpty()) {
                result.append(character);
            }
        }
        
        String finalResult = result.toString();
        logger.info("Final combined result: '{}'", finalResult);
        
        return finalResult;
    }
    
    /**
     * Process image and return detailed column results
     */
    public ColumnResult processImageWithDetails(String imagePath) throws IOException, TesseractException {
        long startTime = System.currentTimeMillis();
        String[] characters = processImageByColumns(imagePath);
        long endTime = System.currentTimeMillis();
        
        return new ColumnResult(imagePath, characters, endTime - startTime);
    }
    
    /**
     * Batch process multiple images
     */
    public List<ColumnResult> batchProcess(List<String> imagePaths) {
        List<ColumnResult> results = new ArrayList<>();
        
        for (String imagePath : imagePaths) {
            try {
                ColumnResult result = processImageWithDetails(imagePath);
                results.add(result);
                logger.info("Successfully processed: {}", imagePath);
            } catch (Exception e) {
                results.add(new ColumnResult(imagePath, new String[columnCount], 0, e.getMessage()));
                logger.error("Failed to process: {}", imagePath, e);
            }
        }
        
        return results;
    }
    
    // Getters and setters
    public int getColumnCount() { return columnCount; }
    public int getColumnWidth() { return columnWidth; }
    public int getImageWidth() { return imageWidth; }
    public int getImageHeight() { return imageHeight; }
    public int getMarginBuffer() { return marginBuffer; }
    public void setMarginBuffer(int marginBuffer) { this.marginBuffer = marginBuffer; }
    
    public boolean hasBackgroundTemplate() { return backgroundTemplate != null; }
    public String getBackgroundTemplatePath() { return backgroundTemplatePath; }
    
    /**
     * Clean up resources
     */
    public void cleanup() {
        if (backgroundTemplate != null) {
            backgroundTemplate.release();
            backgroundTemplate = null;
        }
        logger.info("ColumnBasedOCRProcessor resources cleaned up");
    }
    
    /**
     * Result class for column-based OCR processing
     */
    public static class ColumnResult {
        private final String imagePath;
        private final String[] characters;
        private final long processingTime;
        private final String errorMessage;
        private final boolean success;
        
        public ColumnResult(String imagePath, String[] characters, long processingTime) {
            this.imagePath = imagePath;
            this.characters = characters.clone();
            this.processingTime = processingTime;
            this.errorMessage = null;
            this.success = true;
        }
        
        public ColumnResult(String imagePath, String[] characters, long processingTime, String errorMessage) {
            this.imagePath = imagePath;
            this.characters = characters.clone();
            this.processingTime = processingTime;
            this.errorMessage = errorMessage;
            this.success = false;
        }
        
        // Getters
        public String getImagePath() { return imagePath; }
        public String[] getCharacters() { return characters.clone(); }
        public long getProcessingTime() { return processingTime; }
        public String getErrorMessage() { return errorMessage; }
        public boolean isSuccess() { return success; }
        
        public String getCombinedText() {
            StringBuilder result = new StringBuilder();
            for (String character : characters) {
                if (character != null && !character.trim().isEmpty()) {
                    result.append(character);
                }
            }
            return result.toString();
        }
        
        @Override
        public String toString() {
            return String.format("ColumnResult{path='%s', success=%s, text='%s', time=%dms, error='%s'}", 
                               imagePath, success, getCombinedText(), processingTime, errorMessage);
        }
    }
}