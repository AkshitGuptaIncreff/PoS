package com.example.pos.util;

import com.example.invoice.module.InvoiceForm;
import com.example.invoice.module.InvoiceItems;
import com.example.pos.flow.InventoryFlow;
import com.example.pos.flow.ProductFlow;
import com.example.pos.models.*;
import com.example.pos.models.db.*;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;



public final class Helper {
    private Helper() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static String trimAndLowercase(String input) {

        String string = input.trim().toLowerCase();
        if(string.isEmpty()){
            throw  new ApiException("Invalid Data");
        }
        return string;
    }

    public static ProductData productPojoToData(ProductPojo productPojo){

        ProductData productData = new ProductData();

        productData.setProductName(productPojo.getName());
        productData.setMrp(productPojo.getMrp());
        productData.setClientId(productPojo.getClientId());
        productData.setBarcode(productPojo.getBarcode());
        productData.setImageUrl(productPojo.getImageUrl());

        return productData;
    }

    public static ProductData productViewToData(ProductView view) {
        ProductData data = productPojoToData(view.getProduct());
        if (view.getClient() != null) {
            data.setClientName(view.getClient().getName());
            data.setClientId(view.getClient().getClientId());
        }
        return data;
    }

    public static List<ProductData> productViewListToDataList(List<ProductView> views) {
        return views.stream().map(Helper::productViewToData).toList();
    }

    public static InventoryData inventoryViewToData(InventoryView view) {
        InventoryData data = new InventoryData();
        data.setProductBarcode(view.getInventory().getBarcode());
        data.setQuantity(view.getInventory().getQuantity());
        if (view.getProduct() != null) {
            data.setProductName(view.getProduct().getName());
            data.setMrp(view.getProduct().getMrp());
            data.setImageUrl(view.getProduct().getImageUrl());
        }
        if (view.getClient() != null) {
            data.setClientId(view.getClient().getClientId());
            data.setClientName(view.getClient().getName());
        }
        return data;
    }

    public static List<InventoryData> inventoryViewListToDataList(List<InventoryView> views) {
        return views.stream()
                .map(Helper::inventoryViewToData)
                .toList();
    }

    public static ClientData clientPojoToData(ClientPojo clientPojo){
        ClientData clientData = new ClientData();
        clientData.setClientId(clientPojo.getClientId());
        clientData.setName(clientPojo.getName());
        clientData.setEmail(clientPojo.getEmail());
        return clientData;
    }

    public static List<ClientData> clientPojoListToDataList(List<ClientPojo> clientPojos){
        List<ClientData> clientDataList = new ArrayList<>();
        for(ClientPojo client : clientPojos){
            ClientData clientData = clientPojoToData(client);
            clientDataList.add(clientData);
        }

        return clientDataList;
    }

    public static List<ProductData> productPojoListToDataList(List<ProductPojo> productPojos){
        List<ProductData> productDataList = new ArrayList<>();
        for(ProductPojo product: productPojos){
            ProductData productData = productPojoToData(product);
            productDataList.add(productData);
        }
        return productDataList;
    }

    public static ProductPojo productFormToPojo(ProductForm productForm){
        ProductPojo productPojo = new ProductPojo();

        productPojo.setName(productForm.getName());
        productPojo.setClientId(productForm.getClientId());
        productPojo.setBarcode(productForm.getBarcode());
        productPojo.setMrp(productForm.getMrp());
        productPojo.setImageUrl(productForm.getImageUrl());

        return productPojo;
    }

    public static List<ProductForm> parseProductTsv(MultipartFile file){

        List<ProductForm> productForms = new ArrayList<>();
        try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            List<String> rows = bufferedReader.lines().filter(row -> !row.isBlank()).toList();
            if (rows.size() > 5001) {
                throw new RuntimeException("Maximum 5000 rows allowed");
            }
            for (int i = 1; i < rows.size(); i++) {
                String[] cols = rows.get(i).split("\t");
                ProductForm form = new ProductForm();
                if (cols.length >= 1) form.setBarcode(cols[0].trim());
                if (cols.length >= 2) form.setClientId(cols[1].trim());
                if (cols.length >= 3) form.setName(cols[2].trim().toLowerCase());
                if (cols.length >= 4) {
                    try {
                        form.setMrp(Double.valueOf(cols[3].trim()));
                    } catch (NumberFormatException e) {
                        form.setMrp(null);
                    }
                }
                if (cols.length >= 5) form.setImageUrl(cols[4].trim());
                productForms.add(form);
            }
        } catch(Exception ex){
            throw new ApiException(ex.toString());
        }
        return productForms;
    }

    public static List<InventoryForm> parseInventoryTsv(MultipartFile file){
        List<InventoryForm> forms = new ArrayList<>();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))){
            List<String> rows = reader.lines().filter(row -> !row.isBlank()).toList();
            if(rows.size() > 5001){
                throw new RuntimeException("Maximum 5000 rows allowed");
            }
            for(int i = 1 ; i < rows.size() ; i++){
                String[] cols = rows.get(i).split("\t");
                InventoryForm form = new InventoryForm();
                if (cols.length >= 1) form.setBarcode(cols[0].trim());
                if (cols.length >= 2) {
                    try {
                        form.setQuantity(Integer.parseInt(cols[1].trim()));
                    } catch (NumberFormatException e) {
                        form.setQuantity(null);
                    }
                }
                forms.add(form);
            }
        }catch(Exception e){
            throw new ApiException(e.getMessage());
        }
        return forms;
    }

    public static OrderItemData OrderItemPojoToData(String barcode, String productName, Integer quantity, Double sellingPrice) {

        OrderItemData orderItemData = new OrderItemData();
        orderItemData.setBarcode(barcode);
        orderItemData.setProductName(productName);
        orderItemData.setQuantity(quantity);
        orderItemData.setSellingPrice(sellingPrice);

        return orderItemData;
    }

    public static List<OrderItemPojo> OrderItemFormToPojo(List<OrderItemForm> form){
        List<OrderItemPojo> orderItems = new ArrayList<>();
        for(OrderItemForm item : form) {
            OrderItemPojo orderItemPojo = new OrderItemPojo();
            orderItemPojo.setBarcode(item.getBarcode());
            orderItemPojo.setOrderQuantity(item.getQuantity());
            orderItemPojo.setSellingPrice(item.getSellingPrice());
            orderItems.add(orderItemPojo);
        }
        return orderItems;
    }

    public static OrderData orderPojoToData(OrderPojo orderPojo, List<OrderItemData> itemDataList, String message, List<String> errors) {
        OrderData orderData = new OrderData();
        orderData.setOrderId(orderPojo.getId());
        orderData.setCustomerName(orderPojo.getCustomerName());
        orderData.setCustomerEmail(orderPojo.getCustomerEmail());
        orderData.setOrderTime(orderPojo.getOrderTime().atZone(ZoneId.of("Asia/Kolkata")));
        orderData.setStatus((orderPojo.getOrderStatus()).toString());
        orderData.setItems(itemDataList);
        orderData.setMessage(message);
        orderData.setErrors(errors);

        OrderStatus status = orderPojo.getOrderStatus();
        orderData.setCancellable(status == OrderStatus.UNFULFILLED || status == OrderStatus.FULFILLED);
        orderData.setRetryable(status == OrderStatus.UNFULFILLED);

        return orderData;
    }

    public static OrderData orderViewToData(OrderView view) {
        OrderPojo orderPojo = view.getOrder();
        Map<String, ProductPojo> productMap = view.getProductMap();
        List<OrderItemData> itemDataList = new ArrayList<>();
        if (orderPojo.getOrderItems() != null) {
            for (OrderItemPojo item : orderPojo.getOrderItems()) {
                ProductPojo product = productMap.get(item.getBarcode());
                String productName = (product != null) ? product.getName() : item.getBarcode();
                OrderItemData itemData = OrderItemPojoToData(item.getBarcode(), productName,
                        item.getOrderQuantity(), item.getSellingPrice());
                itemDataList.add(itemData);
            }
        }
        return orderPojoToData(orderPojo, itemDataList, view.getMessage(), view.getErrors());
    }

    public static List<OrderData> orderViewListToDataList(List<OrderView> views) {
        List<OrderData> dataList = new ArrayList<>();
        for (OrderView view : views) {
            dataList.add(orderViewToData(view));
        }
        return dataList;
    }

    public static Map<String,Integer> aggregateOrderItemsByBarcodePojo(List<OrderItemPojo> items) {
        Map<String,Integer> aggregated = new HashMap<>();
        for(OrderItemPojo item : items) {
            aggregated.merge(item.getBarcode(), item.getOrderQuantity(), Integer::sum);
        }
        return aggregated;
    }

    public static Map<String,Integer> aggregateOrderItemsByBarcodeForm(List<OrderItemForm> items) {
        Map<String,Integer> aggregated = new HashMap<>();
        for(OrderItemForm item : items) {
            aggregated.merge(item.getBarcode(), item.getQuantity(), Integer::sum);
        }
        return aggregated;
    }

    public static Map<String, InventoryPojo> mapBarcodeWithInventoryPojo(List<InventoryPojo> inventories){
        Map<String, InventoryPojo> inventoryMap = new HashMap<>();
        for (InventoryPojo inventory : inventories) {
            if (inventory != null && inventory.getBarcode() != null) {
                inventoryMap.put(inventory.getBarcode(), inventory);
            }
        }
        return inventoryMap;
    }

    public static boolean allBarcodeExistsInDatabase(List<String> barcodes,List<ProductPojo> products){
        Set<String> missingBarcodes = new HashSet<>(barcodes);
        for(ProductPojo product : products){
            missingBarcodes.remove(product.getBarcode());
        }
        if(!missingBarcodes.isEmpty()){
            throw new ApiException("Products not found for barcodes: " + missingBarcodes);
        }
        return true;
    }

    public static Map<String, ProductPojo> mapBarcodeWithProductPojo(List<ProductPojo> products){
        Map<String, ProductPojo> productMap = new HashMap<>();
        for (ProductPojo product : products) {
            if (product != null) {
                productMap.put(product.getBarcode(), product);
            }
        }
        return productMap;
    }

    public static OrderPojo orderPojoCreation(List<OrderItemPojo> orderItems,CreateOrderForm form,OrderStatus status){
        OrderPojo order = new OrderPojo();
        order.setOrderItems(orderItems);
        order.setCustomerName(form.getCustomerName());
        order.setCustomerEmail(form.getEmail());
        order.setOrderStatus(status);
        order.setOrderTime(Instant.now());
        return order;
    }

    public static ClientForm clientFormValidate(ClientForm clientForm) {

        if (clientForm == null) {
            throw new ApiException("Client form cannot be null");
        }

        String clientName = clientForm.getName();
        String email = clientForm.getEmail();

        if (clientName == null || clientName.trim().isEmpty()) {
            throw new ApiException("Client name cannot be empty");
        }

        clientName = clientName.trim();

        if (clientName.length() < 3 || clientName.length() > 20) {
            throw new ApiException("Client name must be between 3 and 20 characters");
        }

        if (email == null || email.trim().isEmpty()) {
            throw new ApiException("Email cannot be empty");
        }

        email = email.trim().toLowerCase();

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new ApiException("Invalid email format");
        }

        clientForm.setName(clientName);
        clientForm.setEmail(email);

        return clientForm;
    }

    public static ClientPojo clientFormToPojo(ClientForm form) {
        ClientPojo clientPojo = new ClientPojo();
        clientPojo.setClientId(form.getClientId());
        clientPojo.setName(form.getName());
        clientPojo.setEmail(form.getEmail());
        return clientPojo;
    }

    public static ProductForm productFormValidation(ProductForm productForm) {

        if (productForm == null) {
            throw new ApiException("Product form cannot be null");
        }

        String productName = Helper.trimAndLowercase(productForm.getName());
        String clientId = productForm.getClientId() != null ? productForm.getClientId().trim() : null;
        String barcode = Helper.trimAndLowercase(productForm.getBarcode());

        if (productName == null || productName.isBlank()) {
            throw new ApiException("Product name cannot be empty");
        }

        if (productName.length() < 2 || productName.length() > 50) {
            throw new ApiException("Product name must be between 2 and 50 characters");
        }

        if (clientId == null || clientId.isBlank()) {
            throw new ApiException("Client ID cannot be empty");
        }

        if (clientId.length() < 3 || clientId.length() > 20) {
            throw new ApiException("Client ID must be between 3 and 20 characters");
        }

        if (barcode == null || barcode.isBlank()) {
            throw new ApiException("Barcode cannot be empty");
        }

        if (barcode.length() > 50) {
            throw new ApiException("Barcode cannot exceed 50 characters");
        }

        if (productForm.getMrp() == null) {
            throw new ApiException("MRP cannot be null");
        }

        if (productForm.getMrp() <= 0) {
            throw new ApiException("MRP must be greater than 0");
        }

        productForm.setName(productName);
        productForm.setClientId(clientId);
        productForm.setBarcode(barcode);

        return productForm;
    }

    public static List<ProductPojo> productFormListToPojo(List<ProductForm> productForms) {

        List<ProductPojo> productPojoList = new ArrayList<>();

        for(ProductForm productForm : productForms){
            ProductPojo productPojo = productFormToPojo(productForm);
            productPojoList.add(productPojo);
        }

        return productPojoList;
    }

    public static AuthData authPojoToData(UserPojo user, String sessionId) {
        AuthData data = new AuthData();
        data.setEmail(user.getEmail());
        data.setRole(user.getRole());
        data.setSessionId(sessionId);
        return data;
    }

    public static UserPojo authFormToUserPojo(AuthForm form) {
        UserPojo userPojo = new UserPojo();
        userPojo.setPasswordHash(form.getPassword());
        userPojo.setEmail(form.getEmail());
        return userPojo;
    }

    public static PageData<ClientData> clientDataPagination(Page<ClientPojo> pageResult,int page,int size) {
        PageData<ClientData> data = new PageData<>();

        data.setContent(Helper.clientPojoListToDataList(pageResult.getContent()));

        data.setTotalElements(pageResult.getTotalElements());
        data.setTotalPages(pageResult.getTotalPages());
        data.setPage(page);
        data.setSize(size);

        return data;
    }

    public static ProductPojo updateProductVariables(ProductPojo existing,ProductPojo updatePojo) {
        existing.setName(updatePojo.getName());
        existing.setMrp(updatePojo.getMrp());
        existing.setImageUrl(updatePojo.getImageUrl());
        return existing;
    }

    public static RowError createRowError(int rowNumber, String error, ProductForm form) {
        RowError rowError = new RowError();
        rowError.setRow(rowNumber);
        rowError.setError(error);
        rowError.setBarcode(form.getBarcode());
        rowError.setClientId(form.getClientId());
        rowError.setName(form.getName());
        rowError.setMrp(form.getMrp() != null ? String.valueOf(form.getMrp()) : null);
        rowError.setImageUrl(form.getImageUrl());
        return rowError;
    }

    public static List<String> validateProductFormFields(ProductForm form) {
        List<String> errors = new ArrayList<>();
        if (form.getBarcode() == null || form.getBarcode().isBlank()) {
            errors.add("Barcode cannot be empty");
        }
        if (form.getClientId() == null || form.getClientId().isBlank()) {
            errors.add("Client ID cannot be empty");
        }
        if (form.getName() == null || form.getName().isBlank()) {
            errors.add("Product name cannot be empty");
        }
        if (form.getMrp() == null) {
            errors.add("MRP cannot be null");
        } else if (form.getMrp() <= 0) {
            errors.add("MRP must be greater than 0");
        }
        return errors;
    }

    public static RowError createRowError(int rowNumber, String error, InventoryForm form) {
        RowError rowError = new RowError();
        rowError.setRow(rowNumber);
        rowError.setError(error);
        rowError.setBarcode(form.getBarcode());
        return rowError;
    }

    public static List<String> validateInventoryFormFields(InventoryForm form) {
        List<String> errors = new ArrayList<>();
        if (form.getBarcode() == null || form.getBarcode().isBlank()) {
            errors.add("Barcode cannot be empty");
        }
        if (form.getQuantity() == null) {
            errors.add("Quantity cannot be null");
        } else if (form.getQuantity() < 0) {
            errors.add("Quantity cannot be negative");
        }
        return errors;
    }

    public static PageData<InventoryData> inventoryMapper(InventoryFlow.InventoryPageView pageView, List<InventoryData> data) {
        PageData<InventoryData> result = new PageData<>();
        result.setContent(data);
        result.setTotalElements(pageView.getTotalElements());
        result.setTotalPages(pageView.getTotalPages());
        result.setPage(pageView.getPage());
        result.setSize(pageView.getSize());
        return result;
    }

    public static UploadResult<InventoryData> inventoryDataMapper(InventoryFlow.UploadResult uploadResult, List<InventoryData> importedData) {
        UploadResult<InventoryData> result = new UploadResult<>();
        result.setImported(importedData);
        result.setErrors(uploadResult.getErrors());
        result.setTotalRows(uploadResult.getTotalRows());
        result.setImportedCount(uploadResult.getImportedCount());
        result.setErrorCount(uploadResult.getErrorCount());
        return result;
    }

    public static UploadResult<ProductData> productResultMapper(ProductFlow.UploadResult uploadResult, List<ProductData> importedData) {
        UploadResult<ProductData> result = new UploadResult<>();
        result.setImported(importedData);
        result.setErrors(uploadResult.getErrors());
        result.setTotalRows(uploadResult.getTotalRows());
        result.setImportedCount(uploadResult.getImportedCount());
        result.setErrorCount(uploadResult.getErrorCount());
        return result;
    }

    public static InvoiceItems createInvoiceForm(OrderItemPojo orderItem, ProductPojo product) {
        InvoiceItems invoiceItem = new InvoiceItems();
        invoiceItem.setBarcode(orderItem.getBarcode());
        invoiceItem.setProductName(product.getName());
        invoiceItem.setQuantity(orderItem.getOrderQuantity());
        invoiceItem.setSellingPrice(orderItem.getSellingPrice());

        double itemTotal = orderItem.getOrderQuantity() * orderItem.getSellingPrice();
        invoiceItem.setTotalAmount(itemTotal);
        return invoiceItem;
    }

    public static InvoiceForm createInvoiceFormFromPojo(OrderPojo orderPojo) {
        InvoiceForm invoiceForm = new InvoiceForm();
        invoiceForm.setInvoiceNumber("INV-" + orderPojo.getId());
        invoiceForm.setOrderId(orderPojo.getId());
        invoiceForm.setCustomerName(orderPojo.getCustomerName());
        invoiceForm.setOrderTime(orderPojo.getOrderTime() != null ? orderPojo.getOrderTime() : Instant.now());
        return invoiceForm;
    }

    public static InvoicePojo invoiceBuilder(InvoiceForm form, Path path) {
        InvoicePojo invoice = new InvoicePojo();
        invoice.setOrderId(form.getOrderId());
        invoice.setInvoiceNumber("INV-" + form.getOrderId());
        invoice.setPdfPath(path.toString());
        invoice.setGeneratedAt(Instant.now());
        return invoice;
    }

    public static OrderView orderViewBuilder(Map<String, ProductPojo> productMap, OrderPojo savedOrder) {
        OrderView view = new OrderView();
        view.setOrder(savedOrder);
        view.setProductMap(productMap);
        view.setMessage("Order cancelled successfully");
        view.setErrors(Collections.emptyList());
        return view;
    }

    public static DaySalesReportData daySalesViewToData(DaySalesView view) {
        DaySalesReportPojo pojo = view.getDaySales();
        DaySalesReportData data = new DaySalesReportData();
        data.setDate(pojo.getDate());
        data.setTotalRevenue(pojo.getTotalRevenue());
        data.setInvoicedItemsCount(pojo.getInvoicedItemsCount());
        data.setInvoicedOrdersCount(pojo.getInvoicedOrdersCount());
        return data;
    }

    public static List<DaySalesReportData> daySalesViewListToDataList(List<DaySalesView> views) {
        List<DaySalesReportData> dataList = new ArrayList<>();
        for (DaySalesView view : views) {
            dataList.add(daySalesViewToData(view));
        }
        return dataList;
    }

    public static void setDaySales(DaySalesReportPojo daySales, LocalDate date, int ordersCount, int itemsCount, double revenue) {

        daySales.setDate(date);
        daySales.setInvoicedOrdersCount(ordersCount);
        daySales.setInvoicedItemsCount(itemsCount);
        daySales.setTotalRevenue(revenue);
    }

    public static List<DaySalesReportData> daySalesPojoListToDataList(List<DaySalesReportPojo> salesReportPojos) {
        List<DaySalesReportData> daySalesReportData = new ArrayList<>();

        for(DaySalesReportPojo daySalesReportPojo: salesReportPojos){
            DaySalesReportData data = new DaySalesReportData();
            data.setDate(daySalesReportPojo.getDate());
            data.setTotalRevenue(daySalesReportPojo.getTotalRevenue());
            data.setInvoicedItemsCount(daySalesReportPojo.getInvoicedItemsCount());
            data.setInvoicedOrdersCount(daySalesReportPojo.getInvoicedOrdersCount());
            daySalesReportData.add(data);
        }

        return daySalesReportData;
    }

    public static void validateSalesReport(SalesReportForm form) {

        if (form.getStartDate().isAfter(form.getEndDate())) {
            throw new ApiException("Start date cannot be after end date");
        }
    }

    public record ValidationResult(boolean canFulfill, List<String> errors) {}

    public static ValidationResult validateOrderFulfillment(Map<String, Integer> requiredQuantityFromBarcodes,
            Map<String, ProductPojo> productMap, Map<String, InventoryPojo> inventoryMap) {

        boolean canFulfill = true;
        List<String> errors = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : requiredQuantityFromBarcodes.entrySet()) {
            String barcode = entry.getKey();
            Integer requiredQty = entry.getValue();

            ProductPojo product = productMap.get(barcode);

            // Safety check in case product is missing from the map
            if (product == null) {
                canFulfill = false;
                errors.add("Product not found for barcode " + barcode);
                continue;
            }

            InventoryPojo inventory = inventoryMap.get(product.getBarcode());

            if (inventory == null || inventory.getQuantity() < requiredQty) {
                canFulfill = false;
                errors.add("Insufficient inventory for barcode " + barcode +
                        ". Required=" + requiredQty +
                        ", Available=" + (inventory == null ? 0 : inventory.getQuantity()));
            }
        }

        // 3. Return both values wrapped inside the record
        return new ValidationResult(canFulfill, errors);
    }

    public static Set<String> barcodesFromOrderPojo(List<OrderPojo> orders){
        Set<String> barcodes = new HashSet<>();
        for (OrderPojo order : orders) {
            for (OrderItemPojo item : order.getOrderItems()) {
                barcodes.add(item.getBarcode());
            }
        }
        return barcodes;
    }

    public static List<String> barcodeListFromInventory(List<InventoryPojo> inventories){
        List<String> barcodes = new ArrayList<>();
        for(InventoryPojo inventoryPojo: inventories){
            barcodes.add(inventoryPojo.getBarcode());
        }
        return barcodes;
    }
}
