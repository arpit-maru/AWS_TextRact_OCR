import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

import com.amazon.textract.pdf.PDFDocument;
import com.amazon.textract.pdf.TextLine;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.textract.AmazonTextract;
import com.amazonaws.services.textract.AmazonTextractClientBuilder;
import com.amazonaws.services.textract.model.Block;
import com.amazonaws.services.textract.model.BoundingBox;
import com.amazonaws.services.textract.model.DetectDocumentTextRequest;
import com.amazonaws.services.textract.model.DetectDocumentTextResult;
import com.amazonaws.services.textract.model.Document;

public class DemoPdfFromLocalPdf {
	
	private AmazonTextract client;
	private String AWS_KEY="";
	private String AWS_SECRET="";
	
	private AmazonTextract getAmazonTextractClient()
	{
		
		BasicAWSCredentials awsCreds = new BasicAWSCredentials(AWS_KEY,AWS_SECRET);
		AmazonTextract client = AmazonTextractClientBuilder.standard().withRegion(Regions.AP_SOUTH_1).withCredentials(
				new AWSStaticCredentialsProvider(awsCreds)).build();
		return client;
	}

	private List<TextLine> extractText(ByteBuffer imageBytes) 
	{
		if(client!=null)
		{
			client = getAmazonTextractClient();
		}

		DetectDocumentTextRequest request = new DetectDocumentTextRequest()
				.withDocument(new Document().withBytes(imageBytes));

		DetectDocumentTextResult result = client.detectDocumentText(request);

		List<TextLine> lines = new ArrayList<TextLine>();
		List<Block> blocks = result.getBlocks();
		BoundingBox boundingBox = null;
		for (Block block : blocks) {
			if ((block.getBlockType()).equals("LINE")) {
				boundingBox = block.getGeometry().getBoundingBox();
				lines.add(new TextLine(boundingBox.getLeft(), boundingBox.getTop(), boundingBox.getWidth(),
						boundingBox.getHeight(), block.getText()));
			}
		}

		return lines;
	}

	public void run(String documentName, String outputDocumentName) throws IOException {

		System.out.println("Generating searchable pdf from: " + documentName);

		List<TextLine> lines = null;
		BufferedImage image = null;
		ByteArrayOutputStream byteArrayOutputStream = null;
		ByteBuffer imageBytes = null;

	    File initialFile = new File(documentName);

		// Load pdf document and process each page as image
		PDDocument inputDocument = PDDocument.load(initialFile);
	
		InputStream targetStream = new FileInputStream(initialFile);
		
		PDFRenderer pdfRenderer = new PDFRenderer(inputDocument);
		
		PDFDocument outPutPdfDocument = new PDFDocument(targetStream);

		
		for (int pageNumber = 0; pageNumber < inputDocument.getNumberOfPages(); ++pageNumber) 
		{
			// Render image
			image = pdfRenderer.renderImageWithDPI(pageNumber, 150, org.apache.pdfbox.rendering.ImageType.GRAY); //150

			// Get image bytes
			byteArrayOutputStream = new ByteArrayOutputStream();

			ImageIOUtil.writeImage(image, "jpeg", byteArrayOutputStream, 150);

			byteArrayOutputStream.flush();
			imageBytes = ByteBuffer.wrap(byteArrayOutputStream.toByteArray());

			// Extract text
			lines = extractText(imageBytes);
			
			outPutPdfDocument.addText(pageNumber, lines);

			System.out.println("Processed page index: " + pageNumber);
		}
		inputDocument.close();
		// Save PDF to local disk
		try (OutputStream outputStream = new FileOutputStream(outputDocumentName)) {
			outPutPdfDocument.save(outputStream);
			outPutPdfDocument.close();
		}
		System.out.println("Generated searchable pdf: " + outputDocumentName);
	}
	
}
