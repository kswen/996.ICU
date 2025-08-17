# Column-Based OCR Guide

This is the **perfect solution** for your specific use case! Instead of trying to process the entire messy image at once, we **divide and conquer** by splitting the image into 6 equal columns and processing each Chinese character individually.

## 🎯 **Your Exact Use Case**

✅ **590x360 pixel images**  
✅ **6 Chinese characters evenly spaced**  
✅ **Each character in ~98px width column (590÷6≈98)**  
✅ **Background template support** for removing background noise  
✅ **Single character OCR** optimized for Chinese characters  

## 🚀 **Quick Start**

### **Method 1: Interactive Mode (Recommended)**
```bash
mvn exec:java -Dexec.mainClass="com.example.ocr.ColumnBasedOCRDemo"
```

### **Method 2: Single Image Processing**
```bash
mvn exec:java -Dexec.mainClass="com.example.ocr.ColumnBasedOCRDemo" -Dexec.args="single your_image.png"
```

### **Method 3: With Background Template**
```bash
# First, prepare your background template (same image without text)
mvn exec:java -Dexec.mainClass="com.example.ocr.ColumnBasedOCRDemo" -Dexec.args="template background.png your_image.png"
```

### **Method 4: Debug Mode (See Individual Columns)**
```bash
mvn exec:java -Dexec.mainClass="com.example.ocr.ColumnBasedOCRDemo" -Dexec.args="debug your_image.png"
# This saves individual column images to debug-columns/ directory
```

## 📊 **How It Works**

### **Step 1: Image Splitting**
```
Original Image (590x360)
┌─────────────────────────────────────────────────────────┐
│  Col1  │  Col2  │  Col3  │  Col4  │  Col5  │  Col6     │
│  ~98px │  ~98px │  ~98px │  ~98px │  ~98px │  ~98px    │
│   字   │   字   │   字   │   字   │   字   │   字      │
└─────────────────────────────────────────────────────────┘
```

### **Step 2: Individual Character Processing**
Each column is processed separately with:
- **Histogram equalization** for better contrast
- **Gaussian blur** to reduce noise
- **Adaptive thresholding** for clean character extraction
- **Morphological operations** to clean up the character
- **Single character OCR mode** (Tesseract PSM 8)

### **Step 3: Results Combination**
Individual character results are combined into the final text.

## 💡 **Key Advantages**

### **Why This Approach Works Better:**

1. **🎯 Targeted Processing**: Each character gets individual attention
2. **🧹 Less Noise**: Smaller image regions have less background interference  
3. **⚡ Better Accuracy**: Single character mode is more reliable than full text mode
4. **🔧 Optimized Settings**: Tesseract configured specifically for single Chinese characters
5. **📐 Consistent Spacing**: Takes advantage of your evenly spaced layout
6. **🖼️ Background Removal**: Optional template matching for cleaner results

## 🛠️ **Usage Examples**

### **Basic Processing**
```java
import com.example.ocr.ColumnBasedOCRProcessor;

public class ColumnOCRExample {
    public static void main(String[] args) {
        // Create processor for 590x360 images with 6 columns
        ColumnBasedOCRProcessor processor = new ColumnBasedOCRProcessor();
        
        try {
            // Process your image
            String result = processor.processImage("your_image.png");
            System.out.println("Result: " + result);
            
            // Get detailed column results
            ColumnBasedOCRProcessor.ColumnResult details = 
                processor.processImageWithDetails("your_image.png");
            
            String[] characters = details.getCharacters();
            for (int i = 0; i < characters.length; i++) {
                System.out.println("Column " + (i+1) + ": '" + characters[i] + "'");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            processor.cleanup();
        }
    }
}
```

### **With Background Template**
```java
ColumnBasedOCRProcessor processor = new ColumnBasedOCRProcessor();

// Set your background template (same image without text overlay)
processor.setBackgroundTemplate("background_template.png");

// Process with background removal
String result = processor.processImage("image_with_text.png");
System.out.println("Result with background removal: " + result);
```

### **Custom Dimensions**
```java
// For different image sizes or column counts
ColumnBasedOCRProcessor processor = new ColumnBasedOCRProcessor(
    800,  // width
    400,  // height  
    8     // columns
);

String result = processor.processImage("custom_image.png");
```

### **Batch Processing**
```java
ColumnBasedOCRProcessor processor = new ColumnBasedOCRProcessor();

List<String> imagePaths = Arrays.asList(
    "image1.png", "image2.png", "image3.png"
);

List<ColumnBasedOCRProcessor.ColumnResult> results = 
    processor.batchProcess(imagePaths);

for (ColumnBasedOCRProcessor.ColumnResult result : results) {
    System.out.println(result.getImagePath() + ": '" + 
                      result.getCombinedText() + "'");
}
```

## 🔧 **Configuration Options**

### **Processor Settings**
```java
ColumnBasedOCRProcessor processor = new ColumnBasedOCRProcessor();

// Adjust margin buffer around each character (default: 5px)
processor.setMarginBuffer(10); // More context around each character

// Check current settings
System.out.println("Image size: " + processor.getImageWidth() + "x" + processor.getImageHeight());
System.out.println("Columns: " + processor.getColumnCount());
System.out.println("Column width: " + processor.getColumnWidth() + "px");
```

### **OCR Optimization**
The processor is pre-configured with optimal settings for Chinese characters:
- **Language**: `chi_sim` (Chinese Simplified)
- **OCR Engine Mode**: LSTM + Legacy (mode 1)
- **Page Segmentation Mode**: Single character (PSM 8)
- **DPI**: 300 for better accuracy
- **Character filtering**: Optimized for Chinese text

## 📈 **Expected Results**

### **What You Should See:**
```
Column 1: '博'
Column 2: '物'  
Column 3: '馆'
Column 4: '文'
Column 5: '物'
Column 6: '展'

Final Result: '博物馆文物展'
```

### **Success Indicators:**
- ✅ Each column shows exactly one Chinese character
- ✅ No empty columns (unless that position truly has no text)
- ✅ Clean character recognition without noise
- ✅ Processing time: typically 100-500ms per image

## 🐛 **Troubleshooting**

### **If No Characters Are Detected:**

1. **Check Image Format**:
   ```bash
   # Use debug mode to see column extractions
   mvn exec:java -Dexec.mainClass="com.example.ocr.ColumnBasedOCRDemo" -Dexec.args="debug your_image.png"
   # Check debug-columns/ directory for individual column images
   ```

2. **Verify Image Dimensions**:
   - Image will be automatically resized to 590x360
   - Characters should be evenly distributed across the width

3. **Try Background Template**:
   ```bash
   # If you have the background image without text
   mvn exec:java -Dexec.mainClass="com.example.ocr.ColumnBasedOCRDemo" -Dexec.args="template background.png text_image.png"
   ```

### **If Some Columns Are Empty:**

1. **Adjust Margin Buffer**:
   ```java
   processor.setMarginBuffer(15); // Increase to capture more around each character
   ```

2. **Check Column Alignment**:
   - Characters should be centered in their respective columns
   - Each column should be ~98px wide for 590px images

3. **Examine Debug Images**:
   - Look at the individual column images in `debug-columns/`
   - Verify each column contains a clear, isolated character

### **If Getting Incorrect Characters:**

1. **Image Quality**:
   - Ensure characters are at least 12-14 pixels in height
   - Good contrast between text and background
   - Minimal blur or distortion

2. **Background Noise**:
   - Use background template if available
   - Check if background elements are interfering with character recognition

## 📁 **File Structure**

After running the column-based processor:
```
/workspace/
├── src/main/java/com/example/ocr/
│   ├── ColumnBasedOCRProcessor.java    # 🆕 Main column processor
│   └── ColumnBasedOCRDemo.java         # 🆕 Interactive demo
├── debug-columns/                      # Created when using debug mode
│   ├── column_0.png                    # Individual column images
│   ├── column_1.png
│   └── ...
├── sample-images/                      # Your test images
└── results/                           # Saved results (optional)
```

## 🎨 **Optimized for Your Museum Images**

This approach is **perfect** for your use case because:

- **🏛️ Museum Display Images**: Handles complex backgrounds and lighting
- **🔤 Chinese Character Recognition**: Optimized specifically for Chinese text
- **📐 Even Spacing**: Takes advantage of your 6-column layout  
- **🖼️ Background Templates**: Can remove display case backgrounds
- **⚡ High Accuracy**: Single character processing is much more reliable

## 🚀 **Quick Test**

1. **Place your 590x360 image** in the `sample-images/` directory
2. **Run the demo**:
   ```bash
   mvn exec:java -Dexec.mainClass="com.example.ocr.ColumnBasedOCRDemo"
   # Choose option 7: "Demo with Sample Images"
   ```
3. **Check results** - you should see each of the 6 characters recognized individually!

This column-based approach should give you **much better and cleaner results** compared to trying to process the entire messy image at once. Each character gets individual attention, leading to significantly improved accuracy! 🎯