package com.example.howbeapiserver.grpcclient

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EtcdClientConfig {
    @Bean
    fun etcdClient() : EtcdClient{
        return EtcdClient("localhost",50051)
    }
}