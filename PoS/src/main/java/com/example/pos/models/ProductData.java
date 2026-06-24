package com.example.pos.models;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProductData {
    private String id;
    private String barcode;
    private String productName;
    private String clientName;
    private Double mrp;
    private String imageUrl;
}