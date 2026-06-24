package com.example.invoice.module;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class InvoiceForm {
    private String invoiceNumber;

    private String orderId;

    private String customerName;

    private Instant orderTime;

    private List<InvoiceItems> items;

    private Double totalAmount;
}
