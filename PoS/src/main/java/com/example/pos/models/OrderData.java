package com.example.pos.models;

import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
public class OrderData {

    private String customerName;
    private String customerEmail;

    private String orderId;
    private ZonedDateTime orderTime;
    private String status;
    private List<OrderItemData> items;

    private String message;
    private List<String> errors;

    private boolean cancellable;
    private boolean retryable;
}