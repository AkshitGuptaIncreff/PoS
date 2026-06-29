package com.example.pos.api;

import com.example.pos.dao.InventoryDao;
import com.example.pos.models.db.InventoryPojo;
import com.example.pos.models.db.OrderItemPojo;
import com.example.pos.util.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InventoryApi {

    @Autowired
    private InventoryDao inventoryDao;

    public InventoryPojo findByBarcode(String barcode) {
        return inventoryDao.findByBarcode(barcode);
    }

    public InventoryPojo saveInventory(InventoryPojo inventory) {
        return inventoryDao.save(inventory);
    }

    public Page<InventoryPojo> getAllInventory(int page, int size){
        Pageable pageable = PageRequest.of(page, size, Sort.by("barcode"));
        return inventoryDao.findAll(pageable);
    }

    public List<InventoryPojo> bulkSave(List<InventoryPojo> inventories){
        return inventoryDao.saveAll(inventories);
    }

    public List<InventoryPojo> findByBarcodes(Collection<String> barcodes) {
        return inventoryDao.findByBarcodeIn(barcodes);
    }

    public void restoreInventory(List<OrderItemPojo> orderItems){
        // for each item increase inventory quantity

        // merge
        Map<String, Integer> quantityByBarcodes = new HashMap<>();
        for(OrderItemPojo item : orderItems){
            quantityByBarcodes.merge(item.getBarcode(),item.getOrderQuantity(),Integer::sum);
        }

        // update inventory and then bulk save
        List<InventoryPojo> inventories = findByBarcodes(quantityByBarcodes.keySet());

        Map<String, InventoryPojo> inventoryMap = new HashMap<>();
        for (InventoryPojo inventory : inventories) {
            inventoryMap.put(inventory.getBarcode(), inventory);
        }

        for(Map.Entry<String,Integer> entry : quantityByBarcodes.entrySet()) {
            InventoryPojo inventory = inventoryMap.get(entry.getKey());
            if(inventory == null) {
                throw new ApiException("Inventory not found");
            }
            inventory.setQuantity(inventory.getQuantity() + entry.getValue());
        }
        bulkSave(inventories);
    }

    public void reduceInventory(Map<String, Integer> requiredQuantityByBarcodes) {
        List<InventoryPojo> inventories = findByBarcodes(requiredQuantityByBarcodes.keySet());

        for (InventoryPojo inventory : inventories) {
            Integer requiredQty = requiredQuantityByBarcodes.get(inventory.getBarcode());

            // Defensive check
            if (inventory.getQuantity() < requiredQty) {
                throw new ApiException("Inventory cannot be negative for productId " + inventory.getBarcode());
            }

            inventory.setQuantity(inventory.getQuantity() - requiredQty);
        }

        bulkSave(inventories);
    }
}
