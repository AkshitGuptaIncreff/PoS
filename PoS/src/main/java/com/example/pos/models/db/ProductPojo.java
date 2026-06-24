package com.example.pos.models.db;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "products")

@Getter
@Setter
public class ProductPojo {
    @Id
    private String id;
    @Indexed(unique = true)
    private String barcode;
    private String clientName;
    private String name;
    private Double mrp;
    private String imageUrl;
}