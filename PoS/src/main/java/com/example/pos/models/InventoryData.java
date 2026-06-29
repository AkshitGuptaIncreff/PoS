package com.example.pos.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InventoryData {
    private String productBarcode;
    private String productName;
    private String clientId;
    private String clientName;
    private Double mrp;
    private Integer quantity;
    private String imageUrl;
}
