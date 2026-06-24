package com.example.pos.dto;

import com.example.pos.util.Utils;
import com.example.pos.models.OrderData;
import com.example.pos.flow.OrderFlow;
import com.example.pos.models.CreateOrderForm;
import com.example.pos.models.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class OrderDto {

    @Autowired
    private OrderFlow orderFlow;

    public OrderData createOrder(CreateOrderForm createOrderForm) {
        String customerName = Utils.trimAndLowercase(createOrderForm.getCustomerName());
        createOrderForm.setCustomerName(customerName);

        return orderFlow.createOrder(createOrderForm);
    }

    public List<OrderData> getOrders(String orderId, String status, Instant startDate, Instant endDate) {

        OrderStatus orderStatus = null;
        if(status != null){
            orderStatus = OrderStatus.valueOf(status.toUpperCase());
        }

        return orderFlow.getOrders(orderId,orderStatus,startDate,endDate);
    }

    public OrderData retryOrder(String orderId) {
        return orderFlow.retryOrder(orderId);
    }

    public OrderData cancelOrder(String orderId) {
        return orderFlow.cancelOrder(orderId);
    }
}