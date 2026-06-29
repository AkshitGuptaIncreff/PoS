package com.example.pos.flow;

import com.example.pos.api.ClientApi;
import com.example.pos.api.ProductApi;
import com.example.pos.models.FilterProductForm;
import com.example.pos.models.ProductForm;
import com.example.pos.models.ProductView;
import com.example.pos.models.RowError;
import com.example.pos.models.db.ClientPojo;
import com.example.pos.models.db.ProductPojo;
import com.example.pos.util.ApiException;
import com.example.pos.util.Helper;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ProductFlow {

    @Autowired
    ProductApi productApi;

    @Autowired
    ClientApi clientApi;

    @Autowired
    AuthFlow authFlow;

    @Transactional
    public ProductView createProduct(ProductPojo productPojo) {
        if (productApi.existsByBarcode(productPojo.getBarcode())) {
            throw new ApiException("Barcode already exists");
        }

        ClientPojo client = clientApi.getClientByClientId(productPojo.getClientId());
        ProductPojo saved = productApi.createProduct(productPojo);
        return new ProductView(saved, client);
    }

    @Transactional
    public ProductView updateProduct(ProductPojo updatePojo) {
        authFlow.checkSupervisor();

        ProductPojo existing = productApi.findByBarcode(updatePojo.getBarcode());

        if (!Objects.equals(existing.getBarcode(), updatePojo.getBarcode())) {
            throw new ApiException("Barcode cannot be updated");
        }

        if (!Objects.equals(existing.getClientId(), updatePojo.getClientId())) {
            throw new ApiException("Client association cannot be changed");
        }

        ProductPojo updatedProductPojo = Helper.updateProductVariables(existing, updatePojo);
        ProductPojo saved = productApi.updateProduct(updatedProductPojo);
        ClientPojo client = clientApi.getClientByClientId(saved.getClientId());
        return new ProductView(saved, client);
    }

    public List<ProductView> getAllProductViews() {
        List<ProductPojo> products = productApi.getAllProducts();
        Map<String, ClientPojo> cache = new HashMap<>();
        List<ProductView> views = new ArrayList<>();
        for (ProductPojo product : products) {
            ClientPojo client = cache.get(product.getClientId());
            if (client == null) {
                try {
                    client = clientApi.getClientByClientId(product.getClientId());
                } catch (ApiException e) {
                    client = null;
                }
                cache.put(product.getClientId(), client);
            }
            views.add(new ProductView(product, client));
        }
        return views;
    }

    public List<ProductView> filterProductViews(FilterProductForm form) {
        String productName = form.getProductName();
        String clientId = form.getClientId();
        String barcode = form.getBarcode();
        String clientName = form.getClientName();

        if (clientName != null && !clientName.isBlank() && (clientId == null || clientId.isBlank())) {
            clientId = resolveClientNameToId(clientName);
        }

        List<ProductPojo> productPojos = productApi.filterProduct(productName, barcode, clientId);
        Map<String, ClientPojo> cache = new HashMap<>();
        List<ProductView> views = new ArrayList<>();
        for (ProductPojo product : productPojos) {
            ClientPojo client = cache.get(product.getClientId());
            if (client == null) {
                try {
                    client = clientApi.getClientByClientId(product.getClientId());
                } catch (ApiException e) {
                    client = null;
                }
                cache.put(product.getClientId(), client);
            }
            views.add(new ProductView(product, client));
        }
        return views;
    }

    @Transactional
    public UploadResult uploadProductTsv(List<ProductForm> productForms) {
        authFlow.checkSupervisor();

        List<ProductView> imported = new ArrayList<>();
        List<RowError> errors = new ArrayList<>();
        Set<String> barcodesInBatch = new HashSet<>();
        Map<String, ClientPojo> clientCache = new HashMap<>();

        for (int i = 0; i < productForms.size(); i++) {
            ProductForm form = productForms.get(i);
            int rowNumber = i + 2;

            List<String> fieldErrors = Helper.validateProductFormFields(form);
            if (!fieldErrors.isEmpty()) {
                errors.add(Helper.createRowError(rowNumber, String.join("; ", fieldErrors), form));
                continue;
            }

            if (productApi.existsByBarcode(form.getBarcode())) {
                errors.add(Helper.createRowError(rowNumber, "Barcode already exists", form));
                continue;
            }

            if (barcodesInBatch.contains(form.getBarcode())) {
                errors.add(Helper.createRowError(rowNumber, "Duplicate barcode in file", form));
                continue;
            }
            barcodesInBatch.add(form.getBarcode());

            ClientPojo client;
            if (!clientCache.containsKey(form.getClientId())) {
                try {
                    client = clientApi.getClientByClientId(form.getClientId());
                    clientCache.put(form.getClientId(), client);
                } catch (ApiException e) {
                    errors.add(Helper.createRowError(rowNumber, "Client not found: " + form.getClientId(), form));
                    continue;
                }
            } else {
                client = clientCache.get(form.getClientId());
            }

            ProductPojo pojo = Helper.productFormToPojo(form);
            productApi.createProduct(pojo);
            imported.add(new ProductView(pojo, client));
        }

        return new UploadResult(imported, errors, productForms.size());
    }

    public String resolveClientNameToId(String clientName) {
        ClientPojo client = clientApi.getClientByName(clientName);
        return client.getClientId();
    }

    @Getter
    public static class UploadResult {
        private final List<ProductView> imported;
        private final List<RowError> errors;
        private final int totalRows;

        public UploadResult(List<ProductView> imported, List<RowError> errors, int totalRows) {
            this.imported = imported;
            this.errors = errors;
            this.totalRows = totalRows;
        }

        public int getImportedCount() { return imported.size(); }
        public int getErrorCount() { return errors.size(); }
    }
}
