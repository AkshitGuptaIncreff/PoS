package com.example.pos.flow;

import com.example.pos.api.InventoryApi;
import com.example.pos.api.ProductApi;
import com.example.pos.util.Utils;
import com.example.pos.models.db.InventoryPojo;
import com.example.pos.util.ApiException;
import com.example.pos.models.InventoryData;
import com.example.pos.models.InventoryForm;
import com.example.pos.models.db.ProductPojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class InventoryFlow {

    @Autowired
    private ProductApi productApi;

    @Autowired
    private InventoryApi inventoryApi;

    @Autowired
    private AuthFlow authFlow;


    public InventoryData uploadInventory(InventoryForm inventoryForm) {
        authFlow.checkSupervisor();
        // check if barcode exists => if inventory already exists add else create new

        ProductPojo productPojo = productApi.getProductByBarcode(inventoryForm.getBarcode());
        if(productPojo == null) {
            throw new ApiException("Product not found");
        }

        InventoryPojo inventory = inventoryApi.findByBarcode(inventoryForm.getBarcode());
        if(inventory == null) {
            inventory = new InventoryPojo();
            inventory.setQuantity(inventoryForm.getQuantity());
            inventory.setBarcode(inventoryForm.getBarcode());
        }
        else {
            inventory.setQuantity(inventory.getQuantity() + inventoryForm.getQuantity());
        }

        InventoryPojo saved = inventoryApi.saveInventory(inventory);
        String clientName = productPojo.getClientName();
        String productName = productPojo.getName();

        InventoryData inventoryData = Utils.inventoryPojoToData(saved,clientName,productName);
        return inventoryData;
    }


    public List<InventoryData> getAllInventory(){
        List<InventoryPojo> inventories = inventoryApi.getAllInventory();
        List<String> barcodes = Utils.barcodeListFromInventory(inventories);

        List<ProductPojo> products = productApi.findProductPojoListByBarcodes(barcodes);
        Map<String,String> mapBarcodeWithClientName = Utils.mapBarcodeWithClientName(products);
        Map<String,String> mapBarcodeWithProductName = Utils.mapBarcodeWithProductName(products);

        List<InventoryData> inventoryDataList = Utils.inventoryPojoToDataList(inventories,mapBarcodeWithClientName,mapBarcodeWithProductName);
        return inventoryDataList;
    }

    public List<InventoryData> uploadInventoryTsv(List<InventoryForm> inventoryForms){
        authFlow.checkSupervisor();

        List<String> barcodeInFormList = Utils.barcodeExtractedFromForm(inventoryForms);

        List<ProductPojo> products = productApi.findByBarcodes(barcodeInFormList);
        Map<String,ProductPojo> productPojoMap = Utils.barcodeToProductPojoMap(products);

        List<InventoryPojo> inventories = inventoryApi.findByBarcodes(barcodeInFormList);
        Map<String,InventoryPojo> inventoryPojoMap = Utils.barcodeToInventoryPojoMap(inventories);

        List<InventoryPojo> newInventories = Utils.newInventoriesCreation(inventoryForms,inventoryPojoMap,productPojoMap);
        List<InventoryPojo> saved = inventoryApi.bulkSave(newInventories);

        List<InventoryData> inventoryDataList = Utils.inventoryDataPojoToList(saved,productPojoMap);
        return inventoryDataList;
    }
}