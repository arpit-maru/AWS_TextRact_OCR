package com.amazon.textract.pdf;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PDFToSearchablePDF {
    public static void main(String[] args) {
        try {
            File inputFile = new File("C:\\Users\\USER\\Downloads\\Cherry Blossom FE Lease 2019(1).pdf");
            File outputFile = new File("C:\\Users\\USER\\Downloads\\Cherry Blossom FE Lease 2019(1)_arpit.pdf");
            convertPDFToSearchablePDF(inputFile, outputFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void convertPDFToSearchablePDF(File inputFile, File outputFile) throws IOException, TesseractException {
        PDDocument document = PDDocument.load(inputFile);
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath("tessdata"); // Path to your tessdata folder
        tesseract.setLanguage("eng"); // Language setting for OCR

        PDDocument searchableDocument = new PDDocument();

        for (int page = 0; page < document.getNumberOfPages(); ++page) {
            BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(page, 300); // Render the page as an image
            String ocrText = tesseract.doOCR(bufferedImage); // Perform OCR on the image

            PDPage pdPage = new PDPage();
            searchableDocument.addPage(pdPage);

            PDImageXObject pdImage = LosslessFactory.createFromImage(searchableDocument, bufferedImage);
            PDPageContentStream contentStream = new PDPageContentStream(searchableDocument, pdPage);
            contentStream.drawImage(pdImage, 0, 0, pdPage.getMediaBox().getWidth(), pdPage.getMediaBox().getHeight());
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.newLineAtOffset(10, pdPage.getMediaBox().getHeight() - 10);
            contentStream.showText(ocrText);
            contentStream.endText();
            contentStream.close();
        }

        searchableDocument.save(outputFile);
        searchableDocument.close();
        document.close();
    }
}
