package com.example.pos.flow;

import com.example.pos.api.ClientApi;
import com.example.pos.api.ProductApi;
import com.example.pos.models.db.ClientPojo;
import com.example.pos.models.db.ProductPojo;
import com.example.pos.util.ApiException;
import com.example.pos.models.ProductForm;
import com.example.pos.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProductFlow {

    @Autowired
    ProductApi productApi;

    @Autowired
    ClientApi clientApi;

    @Autowired
    AuthFlow authFlow;

    public List<ProductPojo> uploadProductTsv(List<ProductForm> productForms) {
        authFlow.checkSupervisor();

        Set<String> clientNames = new HashSet<>();
        Set<String> barcodes = new HashSet<>();

        // to check duplicate barcode condition in Tsv file
        Set<String> uploadedBarcodes = new HashSet<>();
        for(ProductForm form : productForms){

            if(!uploadedBarcodes.add(form.getBarcode())){
                throw new ApiException("Duplicate barcode in file: " + form.getBarcode());
            }

            clientNames.add(form.getClientName());
            barcodes.add(form.getBarcode());
        }

        List<ClientPojo> clients = clientApi.findByNames(clientNames);
        Map<String, ClientPojo> clientMap = new HashMap<>();
        for(ClientPojo client : clients){
            clientMap.put(client.getName(), client);
        }

        List<ProductPojo> existingProducts = productApi.findByBarcodes(barcodes);
        Set<String> existingBarcodes = new HashSet<>();
        for(ProductPojo product : existingProducts){
            existingBarcodes.add(product.getBarcode());
        }

        for(ProductForm form : productForms){
            if(!clientMap.containsKey(form.getClientName())){
                throw new ApiException("Client not found : " + form.getClientName());
            }
            if(existingBarcodes.contains(form.getBarcode())){
                throw new ApiException("Duplicate barcode : " + form.getBarcode());
            }
        }

        List<ProductPojo> productPojos = new ArrayList<>();
        for(ProductForm form: productForms){
            ProductPojo productPojo = Utils.productFormToPojo(form);
            productPojos.add(productPojo);
        }

        List<ProductPojo> saved = productApi.bulkInsert(productPojos);
        return saved;
    }

    public ProductPojo updateProduct(String productId,ProductForm productForm){
        authFlow.checkSupervisor();

        ProductPojo productPojo = productApi.findById(productId);

        ClientPojo client = clientApi.getClientByName(productForm.getClientName());
        if(client == null){
            throw new ApiException("Client not found");
        }

        if (!productPojo.getBarcode().equals(productForm.getBarcode())) {
            throw new ApiException("Barcode cannot be changed");
        }

        productPojo.setName(productForm.getName());
        productPojo.setClientName(client.getName());
        productPojo.setMrp(productForm.getMrp());
        productPojo.setImageUrl(productForm.getImageUrl());

        return productApi.updateProduct(productPojo);

    }
}