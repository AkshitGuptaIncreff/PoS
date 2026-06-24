package com.example.pos.models.db;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "users")
@Getter
@Setter
public class UserPojo {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String passwordHash;

    private UserRole role;

    private Instant createdAt;
}
