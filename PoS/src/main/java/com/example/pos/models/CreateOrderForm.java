package com.example.pos.models;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
public class CreateOrderForm {

    @NotBlank(message = "Please provide customer name")
    private String customerName;

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Please provide a valid email")
    private String email;

    @NotEmpty(message = "Please add items in your order")
    private List<OrderItemForm> items;
}
