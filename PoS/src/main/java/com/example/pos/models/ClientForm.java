package com.example.pos.models;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientForm {
    @NotBlank(message = "Client name is mandatory. Please provide a valid name.")
    private String name;
    @Email(message = "Invalid Email")
    private String email;
}