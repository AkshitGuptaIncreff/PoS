package com.example.pos.util;

import com.example.pos.models.*;
import com.example.pos.models.db.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.*;

public final class Utils {
    private Utils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static String trimAndLowercase(String input) {
        if (input == null) {
            return null;
        }
        return input.trim().toLowerCase();
    }

    public static ProductData productPojoToData(ProductPojo productPojo){

        ProductData productData = new ProductData();

        productData.setId(productPojo.getId());
        productData.setProductName(productPojo.getName());
        productData.setMrp(productPojo.getMrp());
        productData.setClientName(productPojo.getClientName());
        productData.setBarcode(productPojo.getBarcode());
        productData.setImageUrl(productPojo.getImageUrl());

        return productData;
    }

    public static ClientData clientPojoToData(ClientPojo clientPojo){
        ClientData clientData = new ClientData();
        clientData.setId(clientPojo.getId());
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
        productPojo.setClientName(productForm.getClientName());
        productPojo.setBarcode(productForm.getBarcode());
        productPojo.setMrp(productForm.getMrp());
        productPojo.setImageUrl(productForm.getImageUrl());

        return productPojo;
    }

    public static List<ProductForm> parseProductTsv(MultipartFile file){

        List<ProductForm> productForms = new ArrayList<>();
        try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(file.getInputStream()))){
            List<String> rows = bufferedReader.lines().filter(row -> !row.isBlank()).toList();
            if(rows.size() > 5001){
                throw new RuntimeException("Maximum 5000 rows allowed");
            }
            for(int i=1;i<rows.size();i++){
                String[] cols = rows.get(i).split("\t");
                if(cols.length != 5){
                    throw new RuntimeException("Invalid row at line " + (i + 1));
                }
                ProductForm form = new ProductForm();
                form.setBarcode(cols[0].trim());
                form.setClientName(cols[1].trim().toLowerCase());
                form.setName(cols[2].trim().toLowerCase());
                form.setMrp(Double.valueOf(cols[3].trim()));
                form.setImageUrl(cols[4].trim());
                productForms.add(form);
            }
        }catch(Exception e){
            throw new ApiException(e.getMessage());
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
                if(cols.length != 2){
                    throw new RuntimeException("Invalid row at line " + (i + 1));
                }
                InventoryForm form = new InventoryForm();
                form.setBarcode(cols[0].trim());
                form.setQuantity(Integer.parseInt(cols[1].trim()));
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
        orderData.setOrderTime(orderPojo.getOrderTime());
        orderData.setStatus((orderPojo.getOrderStatus()).toString());
        orderData.setItems(itemDataList);
        orderData.setMessage(message);
        orderData.setErrors(errors);

        return orderData;
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

    public static OrderData buildOrderResponse(OrderPojo orderPojo, String message, List<String> errors,Map<String, ProductPojo> productMap) {

        List<OrderItemData> itemDataList = new ArrayList<>();

        for (OrderItemPojo item : orderPojo.getOrderItems()) {
            ProductPojo product = productMap.get(item.getBarcode());

            if(product==null){
                throw new ApiException("Product not found");
            }

            OrderItemData itemData = Utils.OrderItemPojoToData(product.getBarcode(), product.getName(),
                    item.getOrderQuantity(), item.getSellingPrice());

            itemDataList.add(itemData);
        }

        return Utils.orderPojoToData(orderPojo, itemDataList, message, errors);
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

    public static List<OrderData> buildOrderResponseList(List<OrderPojo> orders,Map<String, ProductPojo> productMap){
        List<OrderData> orderDataList = new ArrayList<>();
        for (OrderPojo order : orders) {
            OrderData orderData = Utils.buildOrderResponse(order, "", Collections.emptyList(),productMap);
            orderDataList.add(orderData);
        }
        return orderDataList;
    }

    public static InventoryData inventoryPojoToData(InventoryPojo saved, String clientName, String productName){
        InventoryData inventoryData = new InventoryData();
        inventoryData.setProductBarcode(saved.getBarcode());
        inventoryData.setQuantity(saved.getQuantity());
        inventoryData.setProductName(productName);
        inventoryData.setClientName(clientName);
        return inventoryData;
    }

    public static List<InventoryData> inventoryPojoToDataList(List<InventoryPojo> inventories,Map<String,String> mapBarcodeWithClientName
    ,Map<String,String> mapBarcodeWithProductName ){
        List<InventoryData> inventoryDataList = new ArrayList<>();
        for(InventoryPojo inventoryPojo: inventories){
            InventoryData inventoryData = new InventoryData();

            inventoryData.setProductBarcode(inventoryPojo.getBarcode());
            inventoryData.setQuantity(inventoryPojo.getQuantity());

            String clientName = mapBarcodeWithClientName.get(inventoryPojo.getBarcode());
            inventoryData.setClientName(clientName);

            String productName = mapBarcodeWithProductName.get(inventoryPojo.getBarcode());
            inventoryData.setProductName(productName);

            inventoryDataList.add(inventoryData);
        }
        return inventoryDataList;
    }

    public static Map<String,String> mapBarcodeWithClientName(List<ProductPojo> products){
        Map<String,String> mapBarcodeWithClientName = new HashMap<>();
        for(ProductPojo productPojo:products){
            mapBarcodeWithClientName.put(productPojo.getBarcode(),productPojo.getClientName());
        }
        return mapBarcodeWithClientName;
    }

    public static Map<String,String> mapBarcodeWithProductName(List<ProductPojo> products){
        Map<String,String> mapBarcodeWithProductName = new HashMap<>();
        for(ProductPojo productPojo:products){
            mapBarcodeWithProductName.put(productPojo.getBarcode(),productPojo.getName());
        }
        return mapBarcodeWithProductName;
    }

    public static List<String> barcodeListFromInventory(List<InventoryPojo> inventories){
        List<String> barcodes = new ArrayList<>();
        for(InventoryPojo inventoryPojo: inventories){
            barcodes.add(inventoryPojo.getBarcode());
        }
        return barcodes;
    }

    public static List<InventoryData> inventoryDataPojoToList(List<InventoryPojo> saved,Map<String,ProductPojo> productPojoMap){
        List<InventoryData> inventoryDataList = new ArrayList<>();
        for(InventoryPojo inventoryPojo: saved){
            InventoryData inventoryData = new InventoryData();

            inventoryData.setProductBarcode(inventoryPojo.getBarcode());
            inventoryData.setQuantity(inventoryPojo.getQuantity());

            ProductPojo productPojo = productPojoMap.get(inventoryPojo.getBarcode());

            inventoryData.setClientName(productPojo.getClientName());
            inventoryData.setProductName(productPojo.getName());

            inventoryDataList.add(inventoryData);
        }
        return inventoryDataList;
    }

    public static List<InventoryPojo> newInventoriesCreation(List<InventoryForm> inventoryForms, Map<String,InventoryPojo> inventoryPojoMap ,Map<String,ProductPojo> productPojoMap){
        List<InventoryPojo> newInventories = new ArrayList<>();
        for(InventoryForm form : inventoryForms){
            if(!productPojoMap.containsKey(form.getBarcode())){
                throw new ApiException("Product not found");
            }

            InventoryPojo inventoryPojo = inventoryPojoMap.get(form.getBarcode());

            if(inventoryPojo == null){
                inventoryPojo = new InventoryPojo();
                inventoryPojo.setBarcode(form.getBarcode());
                inventoryPojo.setQuantity(form.getQuantity());
            }
            else{
                inventoryPojo.setQuantity(inventoryPojo.getQuantity() + form.getQuantity());
            }
            newInventories.add(inventoryPojo);
        }
        return newInventories;
    }

    public static Map<String,InventoryPojo> barcodeToInventoryPojoMap(List<InventoryPojo> inventories){
        Map<String,InventoryPojo> inventoryPojoMap = new HashMap<>();
        for(InventoryPojo inventoryPojo: inventories){
            inventoryPojoMap.put(inventoryPojo.getBarcode(),inventoryPojo);
        }
        return inventoryPojoMap;
    }

    public static Map<String,ProductPojo> barcodeToProductPojoMap(List<ProductPojo> products){
        Map<String,ProductPojo> productPojoMap = new HashMap<>();
        for(ProductPojo productPojo: products){
            productPojoMap.put(productPojo.getBarcode(),productPojo);
        }
        return productPojoMap;
    }

    public static List<String> barcodeExtractedFromForm(List<InventoryForm> inventoryForms){
        List<String> barcodeInFormList = new ArrayList<>();
        for(InventoryForm form: inventoryForms){
            barcodeInFormList.add(form.getBarcode());
        }
        return barcodeInFormList;
    }
}
