package com.example.pos.dto;

import com.example.pos.api.ProductApi;
import com.example.pos.models.db.ProductPojo;
import com.example.pos.flow.ProductFlow;
import com.example.pos.models.FilterProductForm;
import com.example.pos.models.ProductData;
import com.example.pos.models.ProductForm;
import com.example.pos.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
public class ProductDto {

    @Autowired
    private ProductFlow productFlow;

    @Autowired
    private ProductApi productApi;


    public ProductData createProduct(ProductForm productForm) {
        String productName = Utils.trimAndLowercase(productForm.getName());
        String clientName = Utils.trimAndLowercase(productForm.getClientName());

        productForm.setName(productName);
        productForm.setClientName(clientName);

        ProductPojo productPojo = productApi.createProduct(productForm);
        return Utils.productPojoToData(productPojo);
    }

    public List<ProductData> getAllProducts() {
        List<ProductPojo> products = productApi.getAllProducts();
        return Utils.productPojoListToDataList(products);
    }

    public ProductData updateProduct(String productId,ProductForm productForm) {

        String productName = Utils.trimAndLowercase(productForm.getName());
        String clientName = Utils.trimAndLowercase(productForm.getClientName());
        productForm.setName(productName);
        productForm.setClientName(clientName);

        ProductPojo productPojo = productFlow.updateProduct(productId,productForm);

        ProductData productData = Utils.productPojoToData(productPojo);
        return productData;
    }

    public List<ProductData> filterProduct(FilterProductForm filterProductForm) {

        String productName = filterProductForm.getProductName();
        String clientName = filterProductForm.getClientName();
        String barcode = filterProductForm.getBarcode();

        List<ProductPojo> productPojos = productApi.filterProduct(productName,barcode,clientName);
        return Utils.productPojoListToDataList(productPojos);
    }

    public List<ProductData> uploadProductTsv(MultipartFile file) {
        List<ProductForm> productForms = Utils.parseProductTsv(file);
        List<ProductPojo> productPojos = productFlow.uploadProductTsv(productForms);
        return Utils.productPojoListToDataList(productPojos);
    }
}