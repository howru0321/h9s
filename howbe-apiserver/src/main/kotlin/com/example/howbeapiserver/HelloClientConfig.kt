package com.example.howbeapiserver

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class HelloClientConfig {
    @Bean
    fun helloClient(): HelloClient {
        return HelloClient("localhost", 50051)
    }
}