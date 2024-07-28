package com.example.howbeapiserver

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class HelloClientConfig {
    @Bean
    fun helloClient(): HelloClient {
        return HelloClient("howbe-db", 50052)
    }
}