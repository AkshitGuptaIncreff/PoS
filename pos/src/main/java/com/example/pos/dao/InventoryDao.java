package com.example.pos.dao;

import com.example.pos.models.db.InventoryPojo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface InventoryDao extends MongoRepository<InventoryPojo,String> {

    InventoryPojo findByBarcode(String barcode);

    List<InventoryPojo> findByBarcodeIn(Collection<String> barcodes);
}