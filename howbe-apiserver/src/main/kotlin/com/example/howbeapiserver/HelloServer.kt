package com.example.howbeapiserver

import com.example.grpc.HelloRequest
import com.example.grpc.HelloReply
import com.example.grpc.HelloWorldServiceGrpcKt

class HelloServer : HelloWorldServiceGrpcKt.HelloWorldServiceCoroutineImplBase() {
    override suspend fun sayHello(request: HelloRequest): HelloReply {
        val message = "Hello, ${request.name}!"
        return HelloReply.newBuilder().setMessage(message).build()
    }
}