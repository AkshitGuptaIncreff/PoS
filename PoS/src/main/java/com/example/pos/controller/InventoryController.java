package com.example.pos.controller;

import com.example.pos.dto.InventoryDto;
import com.example.pos.models.InventoryData;
import com.example.pos.models.InventoryForm;
import com.example.pos.models.PageData;
import com.example.pos.models.UploadResult;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*")
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
    public PageData<InventoryData> getAllInventory(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "10") int size){
        return inventoryDto.getAllInventory(page,size);
    }

    @RequestMapping(method = RequestMethod.POST,path = "/upload")
    public UploadResult<InventoryData> uploadInventoryTsv(@RequestParam("file") MultipartFile file){
        return inventoryDto.uploadInventoryTsv(file);
    }
}
