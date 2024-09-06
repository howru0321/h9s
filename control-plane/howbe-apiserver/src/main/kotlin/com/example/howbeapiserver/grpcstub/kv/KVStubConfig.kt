package com.example.howbeapiserver.grpcstub.kv

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class KVStubConfig {
    @Value("\${grpc.stub.host}")
    private lateinit var host: String
    @Bean
    fun KVStub() : KVStub {
        return KVStub(host,50054)
    }
}