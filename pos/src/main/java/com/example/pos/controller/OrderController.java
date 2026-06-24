package com.example.pos.controller;

import com.example.pos.models.OrderData;
import com.example.pos.dto.OrderDto;
import com.example.pos.models.CreateOrderForm;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;


@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderDto orderDto;

    @RequestMapping(method = RequestMethod.POST)
    public OrderData createOrder(@Valid @RequestBody CreateOrderForm createOrderForm){
        return orderDto.createOrder(createOrderForm);
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<OrderData> getOrders(@RequestParam(required = false) String orderId, @RequestParam(required = false) String status,
                                     @RequestParam(required = false) Instant startDate, @RequestParam(required = false) Instant endDate){
        return orderDto.getOrders(orderId,status,startDate,endDate);
    }


    @RequestMapping(method = RequestMethod.POST,path = "/{orderId}/retry")
    public OrderData retryOrder(@PathVariable String orderId){
        return orderDto.retryOrder(orderId);
    }

    @RequestMapping(method = RequestMethod.POST, path = "/{orderId}/cancel")
    public OrderData cancelOrder(@PathVariable String orderId){
        return orderDto.cancelOrder(orderId);
    }
}