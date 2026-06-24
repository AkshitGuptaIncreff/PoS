package com.example.pos.dto;

import com.example.pos.flow.InvoiceFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class InvoiceDto {

    @Autowired
    InvoiceFlow invoiceFlow;

    public ResponseEntity<Resource> downloadInvoice(String orderId) {
        return invoiceFlow.downloadInvoice(orderId);
    }
}