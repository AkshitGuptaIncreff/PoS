package com.example.pos.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FilterProductForm {
    String productName;
    String clientName;
    String clientId;
    String barcode;
}
