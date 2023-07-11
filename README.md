# PDFreader
This application extracts arabic text from scanned pdf files. 
Technologies :
- Java 17
- Tesseract OCR

  Using for reading data from an image.
  
        <dependency>
            <groupId>org.bytedeco</groupId>
            <artifactId>tesseract-platform</artifactId>
            <version>5.3.1-1.5.9</version>
        </dependency>

- OpenCV

  Using for improving the quality of images.
  
        <dependency>
            <groupId>org.openpnp</groupId>
            <artifactId>opencv</artifactId>
            <version>4.7.0-0</version>
        </dependency>

- PDFbox

    Using for decomposing a pdf file into images.
  
        <dependency>
            <groupId>org.apache.pdfbox</groupId>
            <artifactId>pdfbox</artifactId>
            <version>2.0.28</version>
        </dependency>
      
