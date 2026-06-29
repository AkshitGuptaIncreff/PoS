package com.example.pos.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RowError {
    private int row;
    private String error;
    private String barcode;
    private String clientId;
    private String name;
    private String mrp;
    private String imageUrl;
}
