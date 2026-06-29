package com.example.pos.dto;

import com.example.pos.models.PageData;
import com.example.pos.util.Helper;
import com.example.pos.models.OrderData;
import com.example.pos.models.OrderView;
import com.example.pos.flow.OrderFlow;
import com.example.pos.models.CreateOrderForm;
import com.example.pos.models.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class OrderDto {

    @Autowired
    private OrderFlow orderFlow;

    public OrderData createOrder(CreateOrderForm createOrderForm) {
        String customerName = Helper.trimAndLowercase(createOrderForm.getCustomerName());
        createOrderForm.setCustomerName(customerName);

        String email = Helper.trimAndLowercase(createOrderForm.getEmail());
        createOrderForm.setEmail(email);

        OrderView view = orderFlow.createOrder(createOrderForm);
        return Helper.orderViewToData(view);
    }

    public PageData<OrderData> getOrders(String orderId, String status, Instant startDate, Instant endDate, int page, int size) {
        OrderStatus orderStatus = null;
        if (status != null) {
            orderStatus = OrderStatus.valueOf(status.toUpperCase());
        }

        PageData<OrderView> pageData = orderFlow.getOrders(orderId, orderStatus, startDate, endDate, page, size);

        PageData<OrderData> result = new PageData<>();
        result.setContent(Helper.orderViewListToDataList(pageData.getContent()));
        result.setTotalElements(pageData.getTotalElements());
        result.setTotalPages(pageData.getTotalPages());
        result.setPage(pageData.getPage());
        result.setSize(pageData.getSize());
        return result;
    }

    public OrderData retryOrder(String orderId) {
        OrderView view = orderFlow.retryOrder(orderId);
        return Helper.orderViewToData(view);
    }

    public OrderData cancelOrder(String orderId) {
        OrderView view = orderFlow.cancelOrder(orderId);
        return Helper.orderViewToData(view);
    }
}