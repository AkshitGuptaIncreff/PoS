package com.example.pos.controller;

import com.example.pos.dto.ProductDto;
import com.example.pos.models.FilterProductForm;
import com.example.pos.models.ProductData;
import com.example.pos.models.ProductForm;
import com.example.pos.models.UploadResult;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*")
@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductDto productDto;

    @RequestMapping(method = RequestMethod.POST)
    public ProductData createProduct(@Valid @RequestBody ProductForm productForm){
        return productDto.createProduct(productForm);
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<ProductData> getAllProducts() {
        return productDto.getAllProducts();
    }

    @RequestMapping(method = RequestMethod.PUT, path="/update")
    public ProductData updateProduct(@Valid @RequestBody ProductForm productForm) {
        return productDto.updateProduct(productForm);
    }

    @RequestMapping(method = RequestMethod.POST, path = "/upload")
    public UploadResult<ProductData> uploadProductTsv(@RequestParam("file") MultipartFile file){
        return productDto.uploadProductTsv(file);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/filter")
    public List<ProductData> filterProduct(FilterProductForm filterProductForm) {
        return productDto.filterProduct(filterProductForm);
    }
}
