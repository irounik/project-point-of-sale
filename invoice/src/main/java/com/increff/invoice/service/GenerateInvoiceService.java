package com.increff.invoice.service;

import com.increff.invoice.model.InvoiceDetails;
import com.increff.invoice.util.Constants;
import com.increff.invoice.util.GenerateXML;
import com.increff.invoice.util.PdfGenerator;
import org.apache.commons.io.FileUtils;
import org.apache.fop.apps.FOPException;
import org.springframework.stereotype.Service;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.util.Base64;

@Service
public class GenerateInvoiceService {

    public String getEncodedPdf(InvoiceDetails invoiceDetails) throws IOException, FOPException,
            TransformerException, ParserConfigurationException {

        GenerateXML.createXml(invoiceDetails);

        PdfGenerator.createPDF(
                Constants.XSL_TEMPLATE_PATH,
                Constants.INVOICE_XML_PATH,
                Constants.OUTPUT_PDF_PATH
        );

        byte[] encodedBytes = FileUtils.readFileToByteArray(new File(Constants.OUTPUT_PDF_PATH));
        return Base64.getEncoder().encodeToString(encodedBytes);
    }

}
