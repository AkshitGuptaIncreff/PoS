package com.example.pos.flow;

import com.example.pos.InvoiceClient;
import com.example.pos.api.ProductApi;
import com.example.pos.models.OrderStatus;
import com.example.pos.models.db.OrderItemPojo;
import com.example.pos.models.db.ProductPojo;
import com.example.invoice.module.InvoiceItems;
import com.example.pos.util.Helper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ByteArrayResource;
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
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service("posInvoiceFlow")
public class InvoiceFlow {

    @Autowired
    OrderApi orderApi;

    @Autowired
    InvoiceApi invoiceApi;

    @Autowired
    private InvoiceClient invoiceClient;

    @Autowired
    ProductApi productApi;


    public ResponseEntity<Resource> downloadInvoice(String orderId) {

        OrderPojo order = orderApi.getOrderById(orderId);

        InvoicePojo existing = invoiceApi.findByOrderId(orderId);
        if (existing != null) {
            return downloadExisting(existing);
        }

        if (order.getOrderStatus() != OrderStatus.FULFILLED) {
            throw new ApiException("Order must be fulfilled before generating an invoice");
        }

        return generateAndDownload(order);
    }

    private ResponseEntity<Resource> generateAndDownload(OrderPojo order) {
        try {
            InvoiceForm form = buildInvoiceForm(order);

            String base64 = invoiceClient.generateInvoice(form);
            byte[] pdf = Base64.getDecoder().decode(base64);

            Files.createDirectories(Path.of("invoices"));
            Path path = Path.of("invoices", form.getOrderId() + ".pdf");
            Files.write(path, pdf);

            InvoicePojo invoice = Helper.invoiceBuilder(form, path);
            invoiceApi.save(invoice);

            order.setOrderStatus(OrderStatus.INVOICED);
            orderApi.saveOrder(order);

            return buildPdfResponse(pdf);

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to generate invoice: " + e.getMessage());
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

        InvoiceForm invoiceForm = Helper.createInvoiceFormFromPojo(orderPojo);

        List<InvoiceItems> invoiceItems = new ArrayList<>();

        double grandTotal = 0.0;

        for (OrderItemPojo orderItem : orderPojo.getOrderItems()) {
            ProductPojo product = productApi.getProductByBarcode(orderItem.getBarcode());

            if (product == null) {
                throw new ApiException("Product not found for barcode: " + orderItem.getBarcode());
            }

            InvoiceItems invoiceItem = Helper.createInvoiceForm(orderItem, product);

            grandTotal += invoiceItem.getTotalAmount();
            invoiceItems.add(invoiceItem);
        }
        invoiceForm.setItems(invoiceItems);
        invoiceForm.setTotalAmount(grandTotal);

        return invoiceForm;
    }
}
