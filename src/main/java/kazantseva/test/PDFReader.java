package kazantseva.test;

import nu.pattern.OpenCV;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.leptonica.PIX;
import org.bytedeco.tesseract.TessBaseAPI;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;

import static org.bytedeco.leptonica.global.leptonica.pixDestroy;
import static org.bytedeco.leptonica.global.leptonica.pixRead;
import static org.opencv.imgproc.Imgproc.*;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;

public class PDFReader {

    public static void read(String fileName) {
        OpenCV.loadLocally();
        try {
            PDDocument document = PDDocument.load(new File(fileName));

            PDFRenderer pdfRenderer = new PDFRenderer(document);

            TessBaseAPI api = new TessBaseAPI();


            for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
                api.Init("tessdata", "ara");

                BufferedImage image = pdfRenderer.renderImageWithDPI(pageIndex, 300);

                BufferedImage processedImage = preprocessImage(image);

                File imageFile = new File("pageImages/image_" + pageIndex + ".png");
                ImageIO.write(processedImage, "png", imageFile);

                PIX pixImage = pixRead(imageFile.getAbsolutePath());

                api.SetImage(pixImage);

                BytePointer result = api.GetUTF8Text();
                String text = result.getString();

                System.out.println("Page " + pageIndex + ":");
                System.out.println(text);

                api.Clear();
                api.End();
                pixDestroy(pixImage);
            }

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static BufferedImage preprocessImage(BufferedImage image) {
        BufferedImage grayscaleImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D graphics = grayscaleImage.createGraphics();
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();

        Mat originalMat = bufferedImageToMat(grayscaleImage);

        Mat processedMat = new Mat();
        if (originalMat.type() != CvType.CV_8UC1) {
            Imgproc.cvtColor(originalMat, processedMat, COLOR_BGR2GRAY);
        } else {
            processedMat = originalMat.clone();
        }

        Mat blurredMat = new Mat();
        Imgproc.GaussianBlur(processedMat, blurredMat, new Size(5, 5), 0);

        Mat morphologicalMat = new Mat();
        Mat kernel = Mat.ones(5,5, CvType.CV_32F);
        Imgproc.morphologyEx(blurredMat, morphologicalMat, Imgproc.MORPH_OPEN, kernel);


        Mat result = new Mat();
        adaptiveThreshold(morphologicalMat, result, 255, ADAPTIVE_THRESH_MEAN_C, THRESH_BINARY,
                15, 15);

        return matToBufferedImage(result);
    }

    private static Mat bufferedImageToMat(BufferedImage image) {
        BufferedImage convertedImage = new BufferedImage(image.getWidth(), image.getHeight(),
                BufferedImage.TYPE_3BYTE_BGR);
        convertedImage.getGraphics().drawImage(image, 0, 0, null);
        Mat mat = new Mat(convertedImage.getHeight(), convertedImage.getWidth(), CvType.CV_8UC3);
        byte[] data = ((DataBufferByte) convertedImage.getRaster().getDataBuffer()).getData();
        mat.put(0, 0, data);
        return mat;
    }

    private static BufferedImage matToBufferedImage(Mat mat) {
        int width = mat.cols();
        int height = mat.rows();
        int channels = mat.channels();
        byte[] buffer = new byte[width * height * channels];
        mat.get(0, 0, buffer);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        image.getRaster().setDataElements(0, 0, width, height, buffer);
        return image;
    }

}
