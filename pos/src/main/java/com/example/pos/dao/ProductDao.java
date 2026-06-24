package com.example.pos.dao;

import com.example.pos.models.db.ProductPojo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ProductDao extends MongoRepository<ProductPojo,String> {

    boolean existsByBarcode(String barcode);

    ProductPojo findByBarcode(String barcode);

    List<ProductPojo> findByBarcodeIn(Collection<String> barcodes);
}