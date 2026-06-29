package com.example.invoice;

import com.example.invoice.api.InvoiceApi;
import com.example.invoice.module.InvoiceForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InvoiceDto {

    @Autowired
    InvoiceApi invoiceApi;

    String invoiceGenerator(InvoiceForm invoiceForm){
        return invoiceApi.invoiceGenerator(invoiceForm);
    }

}
