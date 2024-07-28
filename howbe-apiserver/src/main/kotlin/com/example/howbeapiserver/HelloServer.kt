package com.example.howbeapiserver

import com.example.grpc.HelloRequest
import com.example.grpc.HelloReply
import com.example.grpc.HelloWorldServiceGrpcKt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay

class HelloServer : HelloWorldServiceGrpcKt.HelloWorldServiceCoroutineImplBase() {
    override suspend fun sayHello(request: HelloRequest): HelloReply {
        val message = "Hello, ${request.name}!"
        return HelloReply.newBuilder().setMessage(message).build()
    }
    override suspend fun sayHelloClientStream(requests: Flow<HelloRequest>): HelloReply {
        val names = mutableListOf<String>()
        requests.collect { request ->
            println("Received request with name(ClientStream): ${request.name}")
            names.add(request.name)
        }
        val message = "Hello, ${names.joinToString(", ")}!"
        return HelloReply.newBuilder().setMessage(message).build()
    }
    override fun sayHelloServerStream(request: HelloRequest): Flow<HelloReply> = flow {
        val names = listOf("Alice", "Bob", "Charlie")
        for (name in names) {
            val message = "Hello, $name! (from ${request.name})"
            emit(HelloReply.newBuilder().setMessage(message).build())
            delay(500)
        }
    }
    override fun sayHelloBidirectionalStream(requests: Flow<HelloRequest>): Flow<HelloReply> = flow {
        requests.collect { request ->
            println("Received request with name(BidirectionalStream): ${request.name}")
            val message = "Hello, ${request.name}!"
            emit(HelloReply.newBuilder().setMessage(message).build())
        }
    }
}