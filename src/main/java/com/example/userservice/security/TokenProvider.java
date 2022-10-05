package com.example.userservice.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class TokenProvider {

    private Environment env;

    public TokenProvider(Environment env) {
        this.env = env;
    }

    public String createAccessToken(String userId) {
        return Jwts.builder()
            .setSubject(userId)
            .setExpiration(new Date(System.currentTimeMillis() +
                Long.parseLong(env.getProperty("token.min_expiration_time"))))
            .signWith(SignatureAlgorithm.HS512, env.getProperty("token.secret"))
            .compact();
    }

    public String createRefreshToken() {
        return Jwts.builder()
            .setExpiration(new Date(System.currentTimeMillis() +
                Long.parseLong(env.getProperty("token.max_expiration_time"))))
            .signWith(SignatureAlgorithm.HS512, env.getProperty("token.secret"))
            .compact();
    }

    public String getUserId(String token) {
        return Jwts.parser().setSigningKey(env.getProperty("token.secret"))
            .parseClaimsJws(token).getBody().getSubject();
    }
}
