package com.example.pos.models;

import com.example.pos.models.db.ClientPojo;
import com.example.pos.models.db.ProductPojo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProductView {
    private ProductPojo product;
    private ClientPojo client;
}
