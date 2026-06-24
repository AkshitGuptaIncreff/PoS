package com.example.pos.flow;

import com.example.pos.api.InventoryApi;
import com.example.pos.api.OrderApi;
import com.example.pos.api.ProductApi;
import com.example.pos.util.ApiException;
import com.example.pos.util.Utils;
import com.example.pos.models.OrderData;
import com.example.pos.models.OrderStatus;
import com.example.pos.models.CreateOrderForm;
import com.example.pos.models.db.InventoryPojo;
import com.example.pos.models.db.OrderItemPojo;
import com.example.pos.models.db.OrderPojo;
import com.example.pos.models.db.ProductPojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class OrderFlow {

    @Autowired
    private OrderApi orderApi;

    @Autowired
    private InventoryApi inventoryApi;

    @Autowired
    private ProductApi productApi;

    @Transactional
    public OrderData createOrder(CreateOrderForm form) {
        // If barcodes valid, validate inventory presence, see if inventory available so reduce it otherwise just create

        // Aggregate Barcodes
        Map<String, Integer> requiredQuantityFromBarcodes = Utils.aggregateOrderItemsByBarcodeForm(form.getItems());

        // Bulk extraction of ProductPojos form Barcodes
        List<String> barcodes = new ArrayList<>(requiredQuantityFromBarcodes.keySet());
        List<ProductPojo> products = productApi.findByBarcodes(barcodes);
        // Barcode Existence - Does all barcode exists in database - if not throws exception
        boolean validBarcode = Utils.allBarcodeExistsInDatabase(barcodes,products);
        // Map of (barcodes,productPojos) for quick Pojo access via Barcodes
        Map<String, ProductPojo> productMap = Utils.mapBarcodeWithProductPojo(products);

        // Inventory Quantity state by Barcodes & conversion to Map (barcode,InventoryPojo)
        List<InventoryPojo> inventories = inventoryApi.findByBarcodes(barcodes);
        Map<String, InventoryPojo> inventoryMap = Utils.mapBarcodeWithInventoryPojo(inventories);

        // Validate if the order can be fulfilled or not
        Utils.ValidationResult result = Utils.validateOrderFulfillment(requiredQuantityFromBarcodes, productMap, inventoryMap);
        boolean canFulfill = result.canFulfill();
        List<String> errors = result.errors();

        OrderStatus status = canFulfill ? OrderStatus.FULFILLED : OrderStatus.UNFULFILLED;
        // Build Order for both Fulfilled and Unfulfilled state
        // OrderItemForm -> OrderItemPojo -> OrderPojo -> Save
        List<OrderItemPojo> orderItems = Utils.OrderItemFormToPojo(form.getItems());
        OrderPojo order = Utils.orderPojoCreation(orderItems,form,status);
        OrderPojo savedOrder = orderApi.saveOrder(order);

        if(canFulfill) {
            inventoryApi.reduceInventory(requiredQuantityFromBarcodes);
            return Utils.buildOrderResponse(savedOrder, "Order fulfilled successfully", Collections.emptyList(), productMap);
        }
        return Utils.buildOrderResponse(savedOrder, "Order created but inventory unavailable", errors, productMap);
    }

    public List<OrderData> getOrders(String orderId, OrderStatus status, Instant startDate, Instant endDate) {

        // Extract barcodes form orderPojo and get productPojo to inturn have product name
        List<OrderPojo> orders = orderApi.getOrders(orderId,status,startDate,endDate);
        Set<String> barcodes = Utils.barcodesFromOrderPojo(orders);

        List<ProductPojo> products = productApi.findByBarcodes(barcodes);
        Map<String, ProductPojo> productMap = Utils.mapBarcodeWithProductPojo(products);

        List<OrderData> orderDataList = Utils.buildOrderResponseList(orders,productMap);
        return orderDataList;
    }

    @Transactional
    public OrderData retryOrder(String orderId) {
        // retry => extract order, check if it's status is unfulfilled, try to check the inventory (by aggregated barcode) => Yes/No

        // Order in Unfulfilled state or not
        OrderPojo order = orderApi.getOrderById(orderId);
        if(order==null || order.getOrderStatus()!=OrderStatus.UNFULFILLED){
            throw new ApiException("Only Unfulfilled order can be Reordered...");
        }

        // Aggregate Products
        Map<String,Integer> requiredQuantityFromBarcodes = Utils.aggregateOrderItemsByBarcodePojo(order.getOrderItems());

        // ProductMap Creation for Product Name access via memory
        List<String> barcodes = new ArrayList<>(requiredQuantityFromBarcodes.keySet());
        List<ProductPojo> products = productApi.findByBarcodes(barcodes);
        Map<String, ProductPojo> productMap = Utils.mapBarcodeWithProductPojo(products);

        List<InventoryPojo> inventories = inventoryApi.findByBarcodes(barcodes);
        Map<String, InventoryPojo> inventoryMap = Utils.mapBarcodeWithInventoryPojo(inventories);

        // Inventory Present or not
        Utils.ValidationResult result = Utils.validateOrderFulfillment(requiredQuantityFromBarcodes, productMap, inventoryMap);
        boolean canFulfill = result.canFulfill();
        List<String> errors = result.errors();

        // inventory reduction and response
        if(canFulfill){
            inventoryApi.reduceInventory(requiredQuantityFromBarcodes);
            order.setOrderStatus(OrderStatus.FULFILLED);
            OrderPojo saved = orderApi.saveOrder(order);
            return Utils.buildOrderResponse(saved,"Order fulfilled successfully",Collections.emptyList(),productMap);
        }
        return Utils.buildOrderResponse(order, "Inventory still unavailable",errors,productMap);
    }

    public OrderData cancelOrder(String orderId) {
        // cancel -> check if orderId is valid and based on status process
        // Unfulfilled -> Change state to cancel, Fulfilled -> Increse Inventory and state change,
        // Cancel -> already cancel, Invoiced -> Cannot cancel, Otherwise -> Invalid State (not possible)

        OrderPojo order = orderApi.getOrderById(orderId);

        if(order == null){
            throw new ApiException("Invalid Order");
        }
        if(order.getOrderStatus() == OrderStatus.CANCELLED){
            throw new ApiException("Order already cancelled");
        }
        if(order.getOrderStatus() == OrderStatus.INVOICED){
            throw new ApiException("Order cannot be cancelled");
        }
        if(order.getOrderStatus() == OrderStatus.FULFILLED){
            inventoryApi.restoreInventory(order.getOrderItems());
        }
        order.setOrderStatus(OrderStatus.CANCELLED);

        // Aggregate Products
        Map<String,Integer> requiredQuantityFromBarcodes = Utils.aggregateOrderItemsByBarcodePojo(order.getOrderItems());

        // ProductMap Creation for Product Name access via memory
        List<ProductPojo> products = productApi.findByBarcodes(requiredQuantityFromBarcodes.keySet());
        Map<String, ProductPojo> productMap = Utils.mapBarcodeWithProductPojo(products);

        OrderPojo savedOrder = orderApi.saveOrder(order);
        return Utils.buildOrderResponse(savedOrder,"Order cancelled successfully",Collections.emptyList(),productMap);
    }
}