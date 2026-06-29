package com.example.pos.models;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientForm {

    private String clientId;

    @NotBlank(message = "Client name is mandatory. Please provide a valid name.")
    @Size(min = 3, max = 20, message = "Name size should be from 3-20")
    private String name;

    @NotBlank
    @Email(message = "Invalid Email")
    private String email;
}
