package com.chat.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProtocolConfig {
    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
