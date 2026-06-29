package com.example.pos.api;

import com.example.pos.dao.ProductDao;
import com.example.pos.models.db.ProductPojo;
import com.example.pos.util.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Service
public class ProductApi {

    @Autowired
    private ProductDao productDao;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Transactional
    public ProductPojo createProduct(ProductPojo productPojo){
        return productDao.save(productPojo);
    }

    public List<ProductPojo> getAllProducts(){
        return productDao.findAll();
    }

    @Transactional
    public ProductPojo updateProduct(ProductPojo productPojo){
        return productDao.save(productPojo);
    }

    public List<ProductPojo> filterProduct(String name, String barcode, String clientId) {
        int count = 0;
        if(name != null && !name.isBlank()) count++;
        if(barcode != null && !barcode.isBlank()) count++;
        if(clientId != null && !clientId.isBlank()) count++;

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
            query.addCriteria(Criteria.where("clientId").is(clientId));
        }
        return mongoTemplate.find(query, ProductPojo.class);
    }

    public ProductPojo getProductByBarcode(String barcode) {
        return productDao.findByBarcode(barcode);
    }

    public List<ProductPojo> findProductPojoListByBarcodes(List<String> barcodes){
        return productDao.findByBarcodeIn(barcodes);
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

    public boolean existsByBarcode(String barcode) {
        return productDao.existsByBarcode(barcode);
    }

    public ProductPojo findByBarcode(String barcode) {
        return productDao.findByBarcode(barcode);
    }
}
