package com.example.pos.api;

import com.example.pos.dao.DaySalesDao;
import com.example.pos.models.OrderStatus;
import com.example.pos.models.db.DaySalesReportPojo;
import com.example.pos.models.db.OrderItemPojo;
import com.example.pos.models.db.OrderPojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
public class DaySalesApi {

    @Autowired
    private OrderApi orderApi;

    @Autowired
    private DaySalesDao daySalesDao;

    public void generateDaySales(LocalDate date) {

        Instant start = date.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        List<OrderPojo> orders = orderApi.findByOrderTimeBetween(start, end);

        List<OrderPojo> fulfilledOrders = new ArrayList<>();
        for (OrderPojo order : orders) {
            if (order.getOrderStatus() == OrderStatus.FULFILLED) {
                fulfilledOrders.add(order);
            }
        }
        orders = fulfilledOrders;

        int ordersCount = orders.size();
        int itemsCount = 0;
        double revenue = 0;

        for (OrderPojo order : orders) {
            for (OrderItemPojo item : order.getOrderItems()) {
                itemsCount += item.getOrderQuantity();
                revenue += item.getOrderQuantity() * item.getSellingPrice();
            }
        }

        DaySalesReportPojo daySales = daySalesDao.findByDate(date);
        if(daySales == null){
            daySales = new DaySalesReportPojo();
        }

        daySales.setDate(date);
        daySales.setInvoicedOrdersCount(ordersCount);
        daySales.setInvoicedItemsCount(itemsCount);
        daySales.setTotalRevenue(revenue);

        daySalesDao.save(daySales);
    }

    public List<DaySalesReportPojo> getDaySalesReport(LocalDate startDate, LocalDate endDate){
        return daySalesDao.findByDateBetween(startDate, endDate);
    }
}