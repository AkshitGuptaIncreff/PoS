package com.example.pos.dto;

import com.example.pos.flow.InventoryFlow;
import com.example.pos.util.Utils;
import com.example.pos.models.InventoryData;
import com.example.pos.models.InventoryForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
public class InventoryDto {

    @Autowired
    private InventoryFlow inventoryFlow;

    public InventoryData uploadInventory(InventoryForm inventoryForm){
        return inventoryFlow.uploadInventory(inventoryForm);
    }

    public List<InventoryData> getAllInventory(){
        return inventoryFlow.getAllInventory();
    }

    public List<InventoryData> uploadInventoryTsv(MultipartFile file){
            List<InventoryForm> inventoryForms = Utils.parseInventoryTsv(file);
            return inventoryFlow.uploadInventoryTsv(inventoryForms);
    }
}
