package com.example.pos.flow;

import com.example.pos.api.ProductApi;
import com.example.pos.models.db.OrderItemPojo;
import com.example.pos.models.db.ProductPojo;
import com.example.invoice.module.InvoiceItems;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ByteArrayResource;
import com.example.invoice.api.FopInvoiceApi;
import com.example.pos.api.InvoiceApi;
import com.example.pos.api.OrderApi;
import com.example.pos.util.ApiException;
import com.example.pos.models.db.InvoicePojo;
import com.example.pos.models.db.OrderPojo;
import com.example.invoice.module.InvoiceForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class InvoiceFlow {

    @Autowired
    OrderApi orderApi;

    @Autowired
    InvoiceApi invoiceApi;

    @Autowired
    FopInvoiceApi fopInvoiceApi;

    @Autowired
    ProductApi productApi;


    public ResponseEntity<Resource> downloadInvoice(String orderId) {

        OrderPojo order = orderApi.getOrderById(orderId);
        if(order == null){
            throw new ApiException("Order not found");
        }

        InvoicePojo existing = invoiceApi.findByOrderId(orderId);
        if(existing != null){
            return downloadExisting(existing);
        }
        return generateAndDownload(order);
    }

    private ResponseEntity<Resource> generateAndDownload(OrderPojo order){
        try {
            InvoiceForm form = buildInvoiceForm(order);

            String base64 = fopInvoiceApi.invoiceGenerator(form);
            byte[] pdf = Base64.getDecoder().decode(base64);

            Files.createDirectories(Path.of("invoices"));
            Path path = Path.of("invoices", form.getOrderId() + ".pdf");
            Files.write(path, pdf);

            InvoicePojo invoice = new InvoicePojo();
            invoice.setOrderId(form.getOrderId());
            invoice.setInvoiceNumber("INV-" + form.getOrderId());
            invoice.setPdfPath(path.toString());
            invoice.setGeneratedAt(Instant.now());

            invoiceApi.save(invoice);
            return buildPdfResponse(pdf);

        } catch (Exception e) {
            throw new ApiException("Failed to generate invoice");
        }
    }

    private ResponseEntity<Resource> downloadExisting(InvoicePojo invoicePojo) {

        try {
            byte[] pdf = Files.readAllBytes(Path.of(invoicePojo.getPdfPath()));
            return buildPdfResponse(pdf);

        } catch (Exception e) {
            throw new ApiException("Unable to read invoice");
        }
    }

    private ResponseEntity<Resource> buildPdfResponse(byte[] pdf) {

        Resource resource = new ByteArrayResource(pdf);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice.pdf")
                .contentType(MediaType.APPLICATION_PDF).contentLength(pdf.length).body(resource);
    }

    private InvoiceForm buildInvoiceForm(OrderPojo orderPojo) {

        InvoiceForm invoiceForm = new InvoiceForm();
        invoiceForm.setInvoiceNumber("INV-" + orderPojo.getId());
        invoiceForm.setOrderId(orderPojo.getId());
        invoiceForm.setCustomerName(orderPojo.getCustomerName());
        invoiceForm.setOrderTime(orderPojo.getOrderTime());

        System.out.println(invoiceForm.getCustomerName());

        List<InvoiceItems> invoiceItems = new ArrayList<>();

        double grandTotal = 0.0;

        for(OrderItemPojo orderItem : orderPojo.getOrderItems()) {
            ProductPojo product = productApi.getProductByBarcode(orderItem.getBarcode());

            if(product == null) {
                throw new ApiException("Product not found");
            }

            InvoiceItems invoiceItem = new InvoiceItems();
            invoiceItem.setBarcode(orderItem.getBarcode());
            invoiceItem.setProductName(product.getName());
            invoiceItem.setQuantity(orderItem.getOrderQuantity());
            invoiceItem.setSellingPrice(orderItem.getSellingPrice());

            double itemTotal = orderItem.getOrderQuantity() * orderItem.getSellingPrice();
            invoiceItem.setTotalAmount(itemTotal);
            grandTotal += itemTotal;

            invoiceItems.add(invoiceItem);
        }
        invoiceForm.setItems(invoiceItems);
        invoiceForm.setTotalAmount(grandTotal);

        return invoiceForm;
    }
}
