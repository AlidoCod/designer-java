package com.example.base;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@SpringBootTest
public class SecurityEncoderTest {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    public void test() {
        log.debug(String.valueOf(passwordEncoder.matches("123456", "$2a$10$0whFg3mmf2nbpPxhgUU7C.RLHyXpV4uHEk3L0e5DK8h90glAheiP6")));
    }
}
