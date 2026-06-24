package com.example.pos.models;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InventoryForm {
    @NotBlank(message = "Barcode cannot be blank")
    private String barcode;
    @NotNull(message = "Invalid quantity")
    @Min(value = 0)
    private Integer quantity;
}
