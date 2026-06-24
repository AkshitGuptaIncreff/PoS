package com.example.pos.api;

import com.example.pos.dao.ProductDao;
import com.example.pos.models.db.ProductPojo;
import com.example.pos.util.ApiException;
import com.example.pos.models.ProductForm;
import com.example.pos.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProductApi {

    @Autowired
    private ProductDao productDao;
    @Autowired
    private MongoTemplate mongoTemplate;

    public ProductPojo createProduct(ProductForm productForm){
        ProductPojo product = productDao.findByBarcode(productForm.getBarcode());

        if (product != null) {
            throw new ApiException("Barcode already exists");
        }

        ProductPojo productPojo = Utils.productFormToPojo(productForm);

        return productDao.save(productPojo);
    }

    public List<ProductPojo> getAllProducts(){
        return productDao.findAll();
    }

    public ProductPojo updateProduct(ProductPojo productPojo){
        return productDao.save(productPojo);
    }

    public List<ProductPojo> filterProduct(String name, String barcode, String clientName) {
        int count = 0;
        if(name != null && !name.isBlank()) count++;
        if(barcode != null && !barcode.isBlank()) count++;
        if(clientName != null && !clientName.isBlank()) count++;

        if(count != 1){
            throw new ApiException("Exactly one filter must be provided");
        }

        Query query = new Query();
        if(name != null && !name.isBlank()) {
            query.addCriteria(Criteria.where("name").regex(name, "i"));
        }
        else if(barcode != null && !barcode.isBlank()) {
            query.addCriteria(Criteria.where("barcode").regex(barcode, "i"));
        }
        else{
            query.addCriteria(Criteria.where("clientName").is(clientName));
        }
        return mongoTemplate.find(query, ProductPojo.class);
    }

    public ProductPojo getProductByBarcode(String barcode) {
        return productDao.findByBarcode(barcode);
    }

    public List<ProductPojo> findProductPojoListByBarcodes(List<String> barcodes){
        return productDao.findByBarcodeIn(barcodes);
    }

    public boolean existsByBarcode(String barcode){
        return productDao.existsByBarcode(barcode);
    }

    public List<ProductPojo> bulkInsert(List<ProductPojo> products) {
        return productDao.saveAll(products);
    }

    public List<ProductPojo> findByBarcodes(Collection<String> barcodes) {
        return productDao.findByBarcodeIn(barcodes);
    }

    public ProductPojo findById(String productId) {
        return productDao.findById(productId).orElseThrow(() -> new ApiException("Product not found"));
    }
}