package com.increff.ironic.pos.service;

import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.data.OrderDetailsData;
import com.increff.ironic.pos.model.data.OrderItemData;
import com.increff.ironic.pos.model.invoice.InvoiceDetails;
import com.increff.ironic.pos.model.invoice.InvoiceItem;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceService {

    @Value("${invoiceDirPath}")
    private String INVOICE_DIR_PATH;

    private static final String INVOICE_API_URL = "http://localhost:8000/invoice/api/generate";

    private static final Logger logger = Logger.getLogger(InvoiceService.class);

    public String generateInvoice(OrderDetailsData orderDetailsData) throws ApiException {
        try {
            InvoiceDetails invoiceDetails = getInvoiceDetails(orderDetailsData);

            String base64Pdf = getEncodedPdf(invoiceDetails);

            // returning path to the invoice
            return savePdfFile(orderDetailsData.getOrderId(), base64Pdf);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new ApiException("Error occured while generating invoice!");
        }
    }

    private String getEncodedPdf(InvoiceDetails invoiceDetails) throws RestClientException {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.postForObject(INVOICE_API_URL, invoiceDetails, String.class);
    }

    private String savePdfFile(Integer orderId, String base64Pdf) throws IOException {
        byte[] pdfBytes = Base64.getDecoder().decode(base64Pdf);

        File invoiceDir = new File(INVOICE_DIR_PATH);
        boolean isNewDirCreated = invoiceDir.mkdirs();

        if (isNewDirCreated) {
            logger.info("Created invoice directory for order id: " + orderId);
        }

        String pdfFilePath = INVOICE_DIR_PATH + "invoice-" + orderId + ".pdf";

        try (FileOutputStream outputStream = new FileOutputStream(pdfFilePath)) {
            outputStream.write(pdfBytes);
            outputStream.flush();
            outputStream.close();
            return pdfFilePath;
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    private InvoiceDetails getInvoiceDetails(OrderDetailsData orderDetailsData) {
        List<InvoiceItem> invoiceItems = getInvoiceItems(orderDetailsData.getItems());

        return InvoiceDetails.builder()
                .items(invoiceItems)
                .orderId(orderDetailsData.getOrderId())
                .time(orderDetailsData.getTime())
                .build();
    }

    private List<InvoiceItem> getInvoiceItems(List<OrderItemData> orderItems) {
        return orderItems
                .stream()
                .map(it -> InvoiceItem.builder()
                        .name(it.getName())
                        .price(it.getPrice())
                        .quantity(it.getQuantity())
                        .build())
                .collect(Collectors.toList());
    }

}
