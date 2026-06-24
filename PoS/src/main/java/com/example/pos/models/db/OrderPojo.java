package com.example.pos.models.db;

import com.example.pos.models.OrderStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;


@Document(collection = "orders")
@Getter
@Setter
public class OrderPojo {
    @Id
    private String id;

    private Instant orderTime;

    private OrderStatus orderStatus;

    private List<OrderItemPojo> orderItems;

    private String customerName;
    private String customerEmail;

}