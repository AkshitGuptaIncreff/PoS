package com.example.pos.controller;

import com.example.pos.dto.ProductDto;
import com.example.pos.models.FilterProductForm;
import com.example.pos.models.ProductData;
import com.example.pos.models.ProductForm;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    @RequestMapping(method = RequestMethod.PUT, path="/{productId}")
    public ProductData updateProduct(@PathVariable String productId,@Valid @RequestBody ProductForm productForm) {
        return productDto.updateProduct(productId,productForm);
    }

    @RequestMapping(method = RequestMethod.POST, path = "/upload")
    public List<ProductData> uploadProductTsv(@RequestParam("file") MultipartFile file){
        return productDto.uploadProductTsv(file);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/filter")
    public List<ProductData> filterProduct(FilterProductForm filterProductForm) {
        return productDto.filterProduct(filterProductForm);
    }
}
