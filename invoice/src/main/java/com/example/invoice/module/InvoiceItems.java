package com.example.invoice.module;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvoiceItems {
    private String barcode;

    private String productName;

    private Integer quantity;

    private Double sellingPrice;

    private Double totalAmount;
}
