package com.example.pos.models.db;

import lombok.*;

@Getter
@Setter
public class OrderItemPojo {
    private String barcode;
    private Integer orderQuantity;
    private Double sellingPrice;
}
