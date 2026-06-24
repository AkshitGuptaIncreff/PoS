package com.example.pos.models;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemForm {

    @NotBlank(message = "Barcode cannot be empty")
    private String barcode;

    @Min(1) @NotNull(message = "Invalid quantity")
    private Integer quantity;

    @NotNull(message = "Please provide selling price")
    private Double sellingPrice;
}
