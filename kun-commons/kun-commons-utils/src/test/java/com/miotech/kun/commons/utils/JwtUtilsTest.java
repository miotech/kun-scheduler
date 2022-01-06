package com.miotech.kun.commons.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JwtUtilsTest {

    @Test
    public void createAndParseToken_ok() {
        String username = "test";
        String token = JwtUtils.createToken(username);
        Assertions.assertTrue(token.startsWith("Bearer"));
        String username_parsed = JwtUtils.parseUsername(token);
        Assertions.assertEquals(username, username_parsed);
    }
}
