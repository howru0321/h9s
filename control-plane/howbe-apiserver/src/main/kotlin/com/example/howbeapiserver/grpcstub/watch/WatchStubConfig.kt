package com.example.howbeapiserver.grpcstub.watch

import com.example.howbeapiserver.grpcstub.watch.WatchStub
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class WatchStubConfig {
    @Value("\${grpc.stub.host}")
    private lateinit var host: String

    @Bean
    fun watchStub() : WatchStub {
        return WatchStub(host,50054)
    }
}