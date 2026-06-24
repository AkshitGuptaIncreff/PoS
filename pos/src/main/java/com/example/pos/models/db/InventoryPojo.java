package com.example.pos.models.db;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "inventories")

@Getter
@Setter
public class InventoryPojo {

    @Id
    private String id;
    private String barcode;
    private Integer quantity;
}