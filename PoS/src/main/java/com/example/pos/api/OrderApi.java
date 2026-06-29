package com.example.pos.api;

import com.example.pos.dao.OrderDao;
import com.example.pos.models.db.OrderPojo;
import com.example.pos.util.ApiException;
import com.example.pos.models.OrderStatus;
import com.example.pos.models.PageData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.query.Query;

import java.time.Instant;
import java.util.List;

@Service
public class OrderApi {
    @Autowired
    private OrderDao orderDao;

    @Autowired
    private MongoTemplate mongoTemplate;

    public OrderPojo saveOrder(OrderPojo orderPojo) {
        return orderDao.save(orderPojo);
    }

    public OrderPojo getOrderById(String orderId) {
        return orderDao.findById(orderId).orElseThrow(() -> new ApiException("Order not found"));
    }

    public List<OrderPojo> getAllOrders() {
        return orderDao.findAll();
    }


    public PageData<OrderPojo> getOrders(String orderId, OrderStatus status, Instant startDate, Instant endDate, int page, int size) {
        Query query = buildFilterQuery(orderId, status, startDate, endDate);

        long total = mongoTemplate.count(query, OrderPojo.class);

        query.with(Pageable.ofSize(size).withPage(page));
        List<OrderPojo> content = mongoTemplate.find(query, OrderPojo.class);

        PageData<OrderPojo> result = new PageData<>();
        result.setContent(content);
        result.setTotalElements(total);
        result.setPage(page);
        result.setSize(size);
        result.setTotalPages((int) Math.ceil((double) total / size));
        return result;
    }

    private Query buildFilterQuery(String orderId, OrderStatus status, Instant startDate, Instant endDate) {
        Query query = new Query();
        if (orderId != null && !orderId.isBlank()) {
            query.addCriteria(Criteria.where("id").is(orderId));
        }
        if (status != null) {
            query.addCriteria(Criteria.where("orderStatus").is(status));
        }
        if (startDate != null || endDate != null) {
            Criteria dateCriteria = Criteria.where("orderTime");
            if (startDate != null) dateCriteria.gte(startDate);
            if (endDate != null) dateCriteria.lte(endDate);
            query.addCriteria(dateCriteria);
        }
        return query;
    }

    public List<OrderPojo> findByOrderTimeBetween(Instant startDate, Instant endDate){
        return orderDao.findByOrderTimeBetween(startDate,endDate);
    }

    public List<OrderPojo> findByOrderTimeBetweenAndStatusIn(Instant startDate, Instant endDate, List<OrderStatus> statuses) {
        return orderDao.findByOrderTimeBetweenAndOrderStatusIn(startDate, endDate, statuses);
    }
}
