package com.example.pos.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemData {
    private String barcode;
    private String productName;
    private Integer quantity;
    private Double sellingPrice;
}
