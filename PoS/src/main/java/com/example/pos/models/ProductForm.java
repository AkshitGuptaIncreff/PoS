package com.example.pos.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class ProductForm {
    @NotBlank(message = "Barcode can't be Blank")
    private String barcode;

    @NotBlank(message = "Client Id can't be Blank")
    private String clientId;

    @NotBlank(message = "Product name can't be Blank")
    private String name;

    @NotNull(message = "MRP can't be 0")
    private Double mrp;

    private String imageUrl;
}