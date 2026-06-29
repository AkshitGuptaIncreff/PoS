package com.example.pos.models;

import com.example.pos.models.db.ClientPojo;
import com.example.pos.models.db.InventoryPojo;
import com.example.pos.models.db.ProductPojo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InventoryView {
    private InventoryPojo inventory;
    private ProductPojo product;
    private ClientPojo client;

    public InventoryView() {}

    public InventoryView(InventoryPojo inventory, ProductPojo product, ClientPojo client) {
        this.inventory = inventory;
        this.product = product;
        this.client = client;
    }
}
