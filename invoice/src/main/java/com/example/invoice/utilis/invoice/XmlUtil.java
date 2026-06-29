package com.example.invoice.utilis.invoice;

import com.example.invoice.module.InvoiceForm;
import com.example.invoice.module.InvoiceItems;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.InputStream;
import java.io.StringWriter;

public class XmlUtil {
    public static String toXml(InvoiceForm invoiceForm) {

        try {

            // Create XML document
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            // Root Element
            Element invoice = doc.createElement("invoice");
            doc.appendChild(invoice);

            // Invoice Number
            Element invoiceNumber = doc.createElement("invoiceNumber");
            invoiceNumber.setTextContent(invoiceForm.getInvoiceNumber());
            invoice.appendChild(invoiceNumber);

            // Order Id
            Element orderId = doc.createElement("orderId");
            orderId.setTextContent(invoiceForm.getOrderId());
            invoice.appendChild(orderId);

            // customer name
            Element customerName = doc.createElement("customerName");
            customerName.setTextContent(invoiceForm.getCustomerName());
            invoice.appendChild(customerName);

            // order time
            Element orderTime = doc.createElement("orderTime");
            orderTime.setTextContent(invoiceForm.getOrderTime().toString());
            invoice.appendChild(orderTime);

            // Total Amount
            Element totalAmount = doc.createElement("totalAmount");
            totalAmount.setTextContent(String.valueOf(invoiceForm.getTotalAmount()));
            invoice.appendChild(totalAmount);

            // Items Container

            Element items = doc.createElement("items");
            invoice.appendChild(items);

            // Item List
            if (invoiceForm.getItems() != null) {
                for (InvoiceItems item : invoiceForm.getItems()) {
                    Element itemElement = doc.createElement("item");
                    items.appendChild(itemElement);

                    // Barcode
                    Element barcode = doc.createElement("barcode");
                    barcode.setTextContent(item.getBarcode());
                    itemElement.appendChild(barcode);

                    // Product Name
                    Element productName = doc.createElement("productName");
                    productName.setTextContent(item.getProductName());
                    itemElement.appendChild(productName);

                    // Quantity
                    Element quantity = doc.createElement("quantity");
                    quantity.setTextContent(String.valueOf(item.getQuantity()));
                    itemElement.appendChild(quantity);

                    // Selling Price
                    Element sellingPrice = doc.createElement("sellingPrice");
                    sellingPrice.setTextContent(String.valueOf(item.getSellingPrice()));
                    itemElement.appendChild(sellingPrice);
                }
            }

            InputStream xslStream = PdfGeneratorUtil.class.getClassLoader().getResourceAsStream("invoice.xsl");

            System.out.println(xslStream);

            // Convert XML Document to XML String
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            return writer.toString();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate XML", e);
        }
    }
}