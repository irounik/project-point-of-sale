package com.increff.invoice.util;

import org.apache.fop.apps.*;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PdfGenerator {

    public static void createPDF(
        String xslTemplatePath,
        String invoiceXmlPath,
        String outputPdfPath
    ) throws FOPException, TransformerException, IOException {

        // the XSL FO file
        File xsltFile = new File(xslTemplatePath);

        // the XML file which provides the input
        StreamSource xmlSource = new StreamSource(new File(invoiceXmlPath));

        // create an instance of fop factory
        FopFactory fopFactory = FopFactory.newInstance();

        // a user agent is needed for transformation
        FOUserAgent foUserAgent = fopFactory.newFOUserAgent();

        // Setup output
        try (OutputStream out = Files.newOutputStream(Paths.get(outputPdfPath))) {
            // Construct fop with desired output format
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);

            // Setup XSLT
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer(new StreamSource(xsltFile));

            // Resulting SAX events (the generated FO) must be piped through to FOP
            Result res = new SAXResult(fop.getDefaultHandler());

            // Start XSLT transformation and FOP processing
            // That's where the XML is first transformed to XSL-FO and then
            // PDF is created
            transformer.transform(xmlSource, res);
        }
    }

}