package com.example.pos.models.db;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "clients")

@Getter
@Setter
public class ClientPojo {

    @Id
    private String id;

    @Indexed(unique = true)
    private String name;

    private String email;
}