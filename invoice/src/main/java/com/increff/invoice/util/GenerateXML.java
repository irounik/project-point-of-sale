package com.increff.invoice.util;

import com.increff.invoice.model.InvoiceDetails;
import com.increff.invoice.model.InvoiceItem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

public class GenerateXML {

    public static void createXml(InvoiceDetails invoiceDetails) throws ParserConfigurationException,
            TransformerException {

        String xmlFilePath = Constants.INVOICE_XML_PATH;

        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();

        DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();

        Document document = documentBuilder.newDocument();

        // root element
        Element root = document.createElement("bill");
        document.appendChild(root);
        double totalBillAmount = 0;

        Element date = document.createElement("date");
        date.appendChild(document.createTextNode(TimeUtils.getCurrentDate()));
        root.appendChild(date);

        Element time = document.createElement("time");
        time.appendChild(document.createTextNode(TimeUtils.getCurrentTime()));
        root.appendChild(time);
        // Create elements from OrderDetailsData list

        int i = 0;
        for (InvoiceItem orderItem : invoiceDetails.getItems()) {

            Element item = document.createElement("item");
            root.appendChild(item);
            Element id = document.createElement("id");
            id.appendChild(document.createTextNode(String.valueOf(++i)));
            item.appendChild(id);

            Element name = document.createElement("name");
            name.appendChild(document.createTextNode(orderItem.getName()));
            item.appendChild(name);

            // Calculate total bill amount
            double totalItemCost = orderItem.getQuantity() * orderItem.getPrice();
            totalBillAmount += totalItemCost;

            Element quantity = document.createElement("quantity");
            quantity.appendChild(document.createTextNode(String.valueOf(orderItem.getQuantity())));
            item.appendChild(quantity);

            Element sellingPrice = document.createElement("sellingPrice");
            sellingPrice.appendChild(document.createTextNode(String.format("%.2f", orderItem.getPrice())));
            item.appendChild(sellingPrice);

            Element cost = document.createElement("cost");
            cost.appendChild(document.createTextNode(String.format("%.2f", totalItemCost)));
            item.appendChild(cost);

        }

        Element total = document.createElement("total");
        total.appendChild(document.createTextNode("Rs. " + String.format("%.2f", totalBillAmount)));
        root.appendChild(total);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource domSource = new DOMSource(document);

        StreamResult streamResult = new StreamResult(new File(xmlFilePath));

        transformer.transform(domSource, streamResult);
    }

}