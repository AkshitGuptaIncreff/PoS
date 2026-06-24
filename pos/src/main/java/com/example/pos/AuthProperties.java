package com.example.pos;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
@Getter
public class AuthProperties {
    @Value("${auth.supervisors}")
    private String supervisors;
}
