package com.miotech.kun.commons.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

public class JwtUtils {

    private static final String SECRET_TOKEN = "40A4C5379B73F31D6CD24F6A7C5C3ACB";

    public static String createToken(String username) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET_TOKEN.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .setAudience(username)
                .signWith(key)
                .compact();
        return "Bearer " + token;
    }

    public static String parseUsername(String token) {
        token = token.substring(7);
        SecretKey key = Keys.hmacShaKeyFor(SECRET_TOKEN.getBytes(StandardCharsets.UTF_8));
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token).getBody().getAudience();
    }
}
