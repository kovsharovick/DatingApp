package org.example.config;

import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class JwtUtil {

    //todo
    public String generateToken(Long userId) {
        return userId + ":" + UUID.randomUUID().toString();
    }
}