package com.example.pos.models;

import com.example.pos.models.db.OrderPojo;
import com.example.pos.models.db.ProductPojo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class OrderView {
    private OrderPojo order;
    private Map<String, ProductPojo> productMap;
    private String message;
    private List<String> errors;
}
