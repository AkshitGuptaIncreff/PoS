package com.example.pos.flow;

import com.example.pos.api.InventoryApi;
import com.example.pos.api.OrderApi;
import com.example.pos.api.ProductApi;
import com.example.pos.models.PageData;
import com.example.pos.util.ApiException;
import com.example.pos.util.Helper;
import com.example.pos.models.OrderStatus;
import com.example.pos.models.OrderView;
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

    private static final Set<OrderStatus> CANCELLABLE_STATES = Set.of(OrderStatus.UNFULFILLED, OrderStatus.FULFILLED);

    @Autowired
    private OrderApi orderApi;

    @Autowired
    private InventoryApi inventoryApi;

    @Autowired
    private ProductApi productApi;

    @Transactional
    public OrderView createOrder(CreateOrderForm form) {
        Map<String, Integer> requiredQuantityFromBarcodes = Helper.aggregateOrderItemsByBarcodeForm(form.getItems());

        List<String> barcodes = new ArrayList<>(requiredQuantityFromBarcodes.keySet());
        List<ProductPojo> products = productApi.findByBarcodes(barcodes);
        Helper.allBarcodeExistsInDatabase(barcodes, products);
        Map<String, ProductPojo> productMap = Helper.mapBarcodeWithProductPojo(products);

        List<InventoryPojo> inventories = inventoryApi.findByBarcodes(barcodes);
        Map<String, InventoryPojo> inventoryMap = Helper.mapBarcodeWithInventoryPojo(inventories);

        Helper.ValidationResult result = Helper.validateOrderFulfillment(requiredQuantityFromBarcodes, productMap, inventoryMap);
        boolean canFulfill = result.canFulfill();
        List<String> errors = result.errors();

        OrderStatus status = canFulfill ? OrderStatus.FULFILLED : OrderStatus.UNFULFILLED;

        List<OrderItemPojo> orderItems = Helper.OrderItemFormToPojo(form.getItems());
        OrderPojo order = Helper.orderPojoCreation(orderItems, form, status);
        OrderPojo savedOrder = orderApi.saveOrder(order);

        if (canFulfill) {
            inventoryApi.reduceInventory(requiredQuantityFromBarcodes);
        }

        OrderView view = new OrderView();
        view.setOrder(savedOrder);
        view.setProductMap(productMap);
        view.setMessage(canFulfill ? "Order fulfilled successfully" : "Order created but inventory unavailable");
        view.setErrors(canFulfill ? Collections.emptyList() : errors);
        return view;
    }

    public PageData<OrderView> getOrders(String orderId, OrderStatus status, Instant startDate, Instant endDate, int page, int size) {
        PageData<OrderPojo> pageData = orderApi.getOrders(orderId, status, startDate, endDate, page, size);

        Set<String> barcodes = Helper.barcodesFromOrderPojo(pageData.getContent());
        List<ProductPojo> products = productApi.findByBarcodes(barcodes);
        Map<String, ProductPojo> productMap = Helper.mapBarcodeWithProductPojo(products);

        List<OrderView> views = new ArrayList<>();
        for (OrderPojo order : pageData.getContent()) {
            OrderView view = Helper.orderViewBuilder(productMap, order);
            views.add(view);
        }

        PageData<OrderView> result = new PageData<>();
        result.setContent(views);
        result.setTotalElements(pageData.getTotalElements());
        result.setTotalPages(pageData.getTotalPages());
        result.setPage(pageData.getPage());
        result.setSize(pageData.getSize());
        return result;
    }

    @Transactional
    public OrderView retryOrder(String orderId) {
        OrderPojo order = orderApi.getOrderById(orderId);
        if (order == null || order.getOrderStatus() != OrderStatus.UNFULFILLED) {
            throw new ApiException("Only Unfulfilled order can be Retried...");
        }

        Map<String, Integer> requiredQuantityFromBarcodes = Helper.aggregateOrderItemsByBarcodePojo(order.getOrderItems());

        List<String> barcodes = new ArrayList<>(requiredQuantityFromBarcodes.keySet());
        List<ProductPojo> products = productApi.findByBarcodes(barcodes);
        Map<String, ProductPojo> productMap = Helper.mapBarcodeWithProductPojo(products);

        List<InventoryPojo> inventories = inventoryApi.findByBarcodes(barcodes);
        Map<String, InventoryPojo> inventoryMap = Helper.mapBarcodeWithInventoryPojo(inventories);

        Helper.ValidationResult result = Helper.validateOrderFulfillment(requiredQuantityFromBarcodes, productMap, inventoryMap);
        boolean canFulfill = result.canFulfill();
        List<String> errors = result.errors();

        OrderView view = new OrderView();
        view.setProductMap(productMap);

        if (canFulfill) {
            inventoryApi.reduceInventory(requiredQuantityFromBarcodes);
            order.setOrderStatus(OrderStatus.FULFILLED);
            OrderPojo saved = orderApi.saveOrder(order);
            view.setOrder(saved);
            view.setMessage("Order fulfilled successfully");
            view.setErrors(Collections.emptyList());
        } else {
            view.setOrder(order);
            view.setMessage("Inventory still unavailable");
            view.setErrors(errors);
        }
        return view;
    }

    @Transactional
    public OrderView cancelOrder(String orderId) {
        OrderPojo order = orderApi.getOrderById(orderId);

        if (!CANCELLABLE_STATES.contains(order.getOrderStatus())) {
            String message = switch (order.getOrderStatus()) {
                case CANCELLED -> "Order already cancelled";
                case INVOICED -> "Invoiced order cannot be cancelled";
                default -> "Order cannot be cancelled in its current state";
            };
            throw new ApiException(message);
        }

        if (order.getOrderStatus() == OrderStatus.FULFILLED) {
            inventoryApi.restoreInventory(order.getOrderItems());
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        OrderPojo savedOrder = orderApi.saveOrder(order);

        Set<String> barcodes = Helper.barcodesFromOrderPojo(List.of(savedOrder));
        List<ProductPojo> products = productApi.findByBarcodes(barcodes);
        Map<String, ProductPojo> productMap = Helper.mapBarcodeWithProductPojo(products);

        return Helper.orderViewBuilder(productMap, savedOrder);
    }
}
