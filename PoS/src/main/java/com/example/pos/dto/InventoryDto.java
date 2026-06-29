package com.example.pos.dto;

import com.example.pos.flow.InventoryFlow;
import com.example.pos.models.InventoryData;
import com.example.pos.models.InventoryForm;
import com.example.pos.models.InventoryView;
import com.example.pos.models.PageData;
import com.example.pos.models.UploadResult;
import com.example.pos.util.Helper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
public class InventoryDto {

    @Autowired
    private InventoryFlow inventoryFlow;

    public InventoryData uploadInventory(InventoryForm inventoryForm) {
        InventoryView view = inventoryFlow.uploadInventory(inventoryForm);
        return Helper.inventoryViewToData(view);
    }

    public PageData<InventoryData> getAllInventory(int page, int size) {
        InventoryFlow.InventoryPageView pageView = inventoryFlow.getAllInventory(page, size);
        List<InventoryData> data = Helper.inventoryViewListToDataList(pageView.getViews());

        PageData<InventoryData> result = Helper.inventoryMapper(pageView, data);
        return result;
    }

    public UploadResult<InventoryData> uploadInventoryTsv(MultipartFile file) {
        List<InventoryForm> inventoryForms = Helper.parseInventoryTsv(file);
        InventoryFlow.UploadResult uploadResult = inventoryFlow.uploadInventoryTsv(inventoryForms);
        List<InventoryData> importedData = Helper.inventoryViewListToDataList(uploadResult.getImported());

        UploadResult<InventoryData> result = Helper.inventoryDataMapper(uploadResult,importedData);
        return result;
    }
}
