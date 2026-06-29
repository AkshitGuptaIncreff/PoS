package com.example.pos.dto;

import com.example.pos.flow.ProductFlow;
import com.example.pos.models.*;
import com.example.pos.models.db.ProductPojo;
import com.example.pos.util.Helper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
public class ProductDto {

    @Autowired
    private ProductFlow productFlow;

    public ProductData createProduct(ProductForm productForm) {
        ProductForm form = Helper.productFormValidation(productForm);
        ProductPojo productPojo = Helper.productFormToPojo(form);
        ProductView view = productFlow.createProduct(productPojo);
        return Helper.productViewToData(view);
    }

    public List<ProductData> getAllProducts() {
        List<ProductView> views = productFlow.getAllProductViews();
        return Helper.productViewListToDataList(views);
    }

    public ProductData updateProduct(ProductForm productForm) {
        ProductForm form = Helper.productFormValidation(productForm);
        ProductPojo productPojo = Helper.productFormToPojo(form);
        ProductView view = productFlow.updateProduct(productPojo);
        return Helper.productViewToData(view);
    }

    public List<ProductData> filterProduct(FilterProductForm filterProductForm) {
        List<ProductView> views = productFlow.filterProductViews(filterProductForm);
        return Helper.productViewListToDataList(views);
    }

    public UploadResult<ProductData> uploadProductTsv(MultipartFile file) {
        List<ProductForm> productForms = Helper.parseProductTsv(file);
        ProductFlow.UploadResult uploadResult = productFlow.uploadProductTsv(productForms);
        List<ProductData> importedData = Helper.productViewListToDataList(uploadResult.getImported());

        UploadResult<ProductData> result = Helper.productResultMapper(uploadResult,importedData);
        return result;
    }
}
