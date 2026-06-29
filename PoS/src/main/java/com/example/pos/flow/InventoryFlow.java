package com.example.pos.flow;

import com.example.pos.api.ClientApi;
import com.example.pos.api.InventoryApi;
import com.example.pos.api.ProductApi;
import com.example.pos.models.InventoryForm;
import com.example.pos.models.InventoryView;
import com.example.pos.models.RowError;
import com.example.pos.models.db.ClientPojo;
import com.example.pos.models.db.InventoryPojo;
import com.example.pos.models.db.ProductPojo;
import com.example.pos.util.ApiException;
import com.example.pos.util.Helper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class InventoryFlow {

    @Autowired
    private ProductApi productApi;

    @Autowired
    private InventoryApi inventoryApi;

    @Autowired
    private ClientApi clientApi;

    @Autowired
    private AuthFlow authFlow;

    public InventoryView uploadInventory(InventoryForm inventoryForm) {
        authFlow.checkSupervisor();

        ProductPojo productPojo = productApi.getProductByBarcode(inventoryForm.getBarcode());
        if (productPojo == null) {
            throw new ApiException("Product not found");
        }

        InventoryPojo inventory = inventoryApi.findByBarcode(inventoryForm.getBarcode());
        if (inventory == null) {
            inventory = new InventoryPojo();
            inventory.setQuantity(inventoryForm.getQuantity());
            inventory.setBarcode(inventoryForm.getBarcode());
        } else {
            inventory.setQuantity(inventory.getQuantity() + inventoryForm.getQuantity());
        }

        InventoryPojo saved = inventoryApi.saveInventory(inventory);
        ClientPojo client = clientApi.getClientByClientId(productPojo.getClientId());

        return new InventoryView(saved, productPojo, client);
    }

    public InventoryPageView getAllInventory(int page, int size) {
        Page<InventoryPojo> inventoryPage = inventoryApi.getAllInventory(page, size);
        List<InventoryPojo> inventories = inventoryPage.getContent();

        List<String> barcodes = Helper.barcodeListFromInventory(inventories);
        List<ProductPojo> products = productApi.findProductPojoListByBarcodes(barcodes);
        Map<String, ProductPojo> productMap = Helper.mapBarcodeWithProductPojo(products);

        Map<String, ClientPojo> clientCache = new HashMap<>();
        List<InventoryView> views = new ArrayList<>();

        for (InventoryPojo inventory : inventories) {
            ProductPojo product = productMap.get(inventory.getBarcode());
            ClientPojo client = null;
            if (product != null) {
                String clientId = product.getClientId();
                client = clientCache.get(clientId);
                if (client == null) {
                    try {
                        client = clientApi.getClientByClientId(clientId);
                    } catch (ApiException e) {
                        client = null;
                    }
                    clientCache.put(clientId, client);
                }
            }
            views.add(new InventoryView(inventory, product, client));
        }

        return new InventoryPageView(views, inventoryPage.getTotalElements(), inventoryPage.getTotalPages(), page, size);
    }

    public UploadResult uploadInventoryTsv(List<InventoryForm> inventoryForms) {
        authFlow.checkSupervisor();

        List<InventoryView> imported = new ArrayList<>();
        List<RowError> errors = new ArrayList<>();
        Set<String> barcodesInBatch = new HashSet<>();
        Map<String, ClientPojo> clientCache = new HashMap<>();

        for (int i = 0; i < inventoryForms.size(); i++) {
            InventoryForm form = inventoryForms.get(i);
            int rowNumber = i + 2;

            List<String> fieldErrors = Helper.validateInventoryFormFields(form);
            if (!fieldErrors.isEmpty()) {
                errors.add(Helper.createRowError(rowNumber, String.join("; ", fieldErrors), form));
                continue;
            }

            if (barcodesInBatch.contains(form.getBarcode())) {
                errors.add(Helper.createRowError(rowNumber, "Duplicate barcode in file", form));
                continue;
            }
            barcodesInBatch.add(form.getBarcode());

            ProductPojo product = productApi.getProductByBarcode(form.getBarcode());
            if (product == null) {
                errors.add(Helper.createRowError(rowNumber, "Product not found for barcode: " + form.getBarcode(), form));
                continue;
            }

            InventoryPojo inventory = inventoryApi.findByBarcode(form.getBarcode());
            if (inventory == null) {
                inventory = new InventoryPojo();
                inventory.setBarcode(form.getBarcode());
                inventory.setQuantity(form.getQuantity());
            } else {
                inventory.setQuantity(inventory.getQuantity() + form.getQuantity());
            }

            InventoryPojo saved = inventoryApi.saveInventory(inventory);

            ClientPojo client = clientCache.get(product.getClientId());
            if (client == null) {
                try {
                    client = clientApi.getClientByClientId(product.getClientId());
                } catch (ApiException e) {
                    client = null;
                }
                clientCache.put(product.getClientId(), client);
            }

            imported.add(new InventoryView(saved, product, client));
        }

        return new UploadResult(imported, errors, inventoryForms.size());
    }

    public static class UploadResult {
        private final List<InventoryView> imported;
        private final List<RowError> errors;
        private final int totalRows;

        public UploadResult(List<InventoryView> imported, List<RowError> errors, int totalRows) {
            this.imported = imported;
            this.errors = errors;
            this.totalRows = totalRows;
        }

        public List<InventoryView> getImported() { return imported; }
        public List<RowError> getErrors() { return errors; }
        public int getTotalRows() { return totalRows; }
        public int getImportedCount() { return imported.size(); }
        public int getErrorCount() { return errors.size(); }
    }

    public static class InventoryPageView {
        private final List<InventoryView> views;
        private final long totalElements;
        private final int totalPages;
        private final int page;
        private final int size;

        public InventoryPageView(List<InventoryView> views, long totalElements, int totalPages, int page, int size) {
            this.views = views;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
            this.page = page;
            this.size = size;
        }

        public List<InventoryView> getViews() { return views; }
        public long getTotalElements() { return totalElements; }
        public int getTotalPages() { return totalPages; }
        public int getPage() { return page; }
        public int getSize() { return size; }
    }
}
