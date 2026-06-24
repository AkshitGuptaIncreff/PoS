package com.example.pos.models.db;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "sessions")
@Getter
@Setter
public class SessionPojo {

    @Id
    private String id;

    private String userId;

    @Indexed(unique = true)
    private String sessionId;

    @Indexed(expireAfter = "5m")
    private Instant expiresAt;
}
