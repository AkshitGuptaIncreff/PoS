package com.example.pos.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InventoryData {
    private String clientName;
    private String productName;
    private String productBarcode;
    private Integer quantity;
}