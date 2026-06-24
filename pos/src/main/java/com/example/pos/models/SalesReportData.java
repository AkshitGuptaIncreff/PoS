package com.example.pos.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SalesReportData {

    private String clientName;
    private String productName;
    private Integer quantity;
    private Double revenue;
}
