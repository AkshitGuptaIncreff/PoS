package com.example.pos.models;

import lombok.Getter;
import lombok.Setter;
import com.example.pos.models.db.UserRole;

@Getter
@Setter
public class AuthData {
    private String email;
    private UserRole role;
    private String sessionId;
}