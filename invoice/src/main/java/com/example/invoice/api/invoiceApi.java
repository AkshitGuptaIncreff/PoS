package com.example.invoice.api;

import com.example.invoice.module.InvoiceForm;
import org.springframework.stereotype.Service;

@Service
public interface invoiceApi {

    String invoiceGenerator(InvoiceForm invoiceForm);

}
