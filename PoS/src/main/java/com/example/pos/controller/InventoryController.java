package com.example.pos.controller;

import com.example.pos.dto.InventoryDto;
import com.example.pos.models.InventoryData;
import com.example.pos.models.InventoryForm;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    InventoryDto inventoryDto;

    @RequestMapping(method = RequestMethod.POST)
    public InventoryData uploadInventory(@Valid @RequestBody InventoryForm inventoryForm) {
        return inventoryDto.uploadInventory(inventoryForm);
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<InventoryData> getAllInventory(){
        return inventoryDto.getAllInventory();
    }

    @RequestMapping(method = RequestMethod.POST,path = "/upload")
    public List<InventoryData> uploadInventoryTsv(@RequestParam("file") MultipartFile file){
        return inventoryDto.uploadInventoryTsv(file);
    }
}
