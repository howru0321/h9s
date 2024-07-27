package com.example.howbeapiserver

import com.example.grpc.HelloRequest
import com.example.grpc.HelloReply
import com.example.grpc.HelloWorldServiceGrpc
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder


class HelloClient(host: String?, port: Int) {
    private var managedChannel: ManagedChannel? = null
    private var blockingStub: HelloWorldServiceGrpc.HelloWorldServiceBlockingStub? = null
    private var asyncStub: HelloWorldServiceGrpc.HelloWorldServiceStub? = null

    init {
        managedChannel = ManagedChannelBuilder
            .forAddress(host, port)
            .usePlaintext()
            .build()
        blockingStub = HelloWorldServiceGrpc.newBlockingStub(managedChannel)
        asyncStub = HelloWorldServiceGrpc.newStub(managedChannel)
    }

    fun sayHelloWithBlocking(message: String): HelloReply {
        val request = buildRequest(message)
        return blockingStub!!.sayHello(request)
    }

    private fun buildRequest(message: String): HelloRequest {
        return HelloRequest.newBuilder()
            .setName(message)
            .build()
    }
}