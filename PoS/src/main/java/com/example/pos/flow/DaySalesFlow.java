package com.example.pos.flow;

import com.example.pos.api.DaySalesApi;
import com.example.pos.api.OrderApi;
import com.example.pos.models.DaySalesView;
import com.example.pos.models.OrderStatus;
import com.example.pos.models.db.DaySalesReportPojo;
import com.example.pos.models.db.OrderItemPojo;
import com.example.pos.models.db.OrderPojo;
import com.example.pos.util.Helper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class DaySalesFlow {

    @Autowired
    private DaySalesApi daySalesApi;

    @Autowired
    private OrderApi orderApi;

    public void generateDaySales(LocalDate date) {
        Instant start = date.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        List<OrderStatus> targetStatuses = List.of(OrderStatus.FULFILLED, OrderStatus.INVOICED);
        List<OrderPojo> orders = orderApi.findByOrderTimeBetweenAndStatusIn(start, end, targetStatuses);

        int ordersCount = orders.size();
        int itemsCount = 0;
        double revenue = 0;

        for (OrderPojo order : orders) {
            for (OrderItemPojo item : order.getOrderItems()) {
                itemsCount += item.getOrderQuantity();
                revenue += item.getOrderQuantity() * item.getSellingPrice();
            }
        }

        // not needed but more global approach (multi instance and app restarted around midnight/ 2 instance calculate)
        // force uniqueness and for normal cases a inexpensive check as date is indexed and unique
        DaySalesReportPojo daySales = daySalesApi.findByDate(date);
        if(Objects.isNull(daySales)){
            daySales = new DaySalesReportPojo();
        }
        Helper.setDaySales(daySales,date,ordersCount,itemsCount,revenue);
        daySalesApi.save(daySales);

        System.out.println("Start = " + start);
        System.out.println("End = " + end);
        System.out.println("Orders found = " + orders.size());
    }
}
