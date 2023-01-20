package com.increff.invoice.controller;

import com.increff.invoice.model.InvoiceDetails;
import com.increff.invoice.service.GenerateInvoiceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.fop.apps.FOPException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

@Api
@RestController
public class GeneratePdfController {

    private final GenerateInvoiceService generateInvoiceService;

    @Autowired
    public GeneratePdfController(GenerateInvoiceService generateInvoiceService) {
        this.generateInvoiceService = generateInvoiceService;
    }

    @ApiOperation(value = "Returns base64-encoded string representing the PDF invoice")
    @RequestMapping(path = "/api/generate", method = RequestMethod.POST)
    public String getEncodedPdf(@RequestBody InvoiceDetails invoiceDetails)
            throws IOException, FOPException, TransformerException, ParserConfigurationException {

        return generateInvoiceService.getEncodedPdf(invoiceDetails);
    }

}
