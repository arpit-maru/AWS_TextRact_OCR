
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class NonSearchableToSearchablePDF {
    public static void main(String[] args) {
        File inputFile = new File("input.pdf");
        File outputFile = new File("output_searchable.pdf");

        try (PDDocument document = PDDocument.load(inputFile)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            PDDocument searchableDocument = new PDDocument();
            ITesseract tesseract = new Tesseract();
            tesseract.setDatapath("path_to_tessdata"); // Set path to tessdata directory

            for (int page = 0; page < document.getNumberOfPages(); ++page) 
            {
                BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);
                File tempImage = new File("temp_page_" + page + ".png");
                ImageIO.write(bufferedImage, "png", tempImage);

                String ocrResult = tesseract.doOCR(tempImage);

                PDPage pdPage = new PDPage(PDRectangle.A4);
                searchableDocument.addPage(pdPage);
                PDPageContentStream contentStream = new PDPageContentStream(searchableDocument, pdPage);

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(10, pdPage.getMediaBox().getHeight() - 20);
                contentStream.showText(ocrResult);
                contentStream.endText();
                contentStream.close();

                tempImage.delete(); // Clean up temporary image file
            }

            searchableDocument.save(outputFile);
            searchableDocument.close();
        } catch (IOException | TesseractException e) {
            e.printStackTrace();
        }
    }
}
