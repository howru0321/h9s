package com.example.howbeapiserver

import com.example.grpc.HelloRequest
import com.example.grpc.HelloReply
import com.example.grpc.HelloWorldServiceGrpc
import com.example.grpc.HelloWorldServiceGrpcKt
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.delay


import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


class HelloClient(host: String?, port: Int) {
    private var managedChannel: ManagedChannel? = null
    private var coroutineStub: HelloWorldServiceGrpcKt.HelloWorldServiceCoroutineStub? = null

    init {
        val managedChannel = ManagedChannelBuilder
            .forAddress(host, port)
            .usePlaintext()
            .build()
        coroutineStub = HelloWorldServiceGrpcKt.HelloWorldServiceCoroutineStub(managedChannel)
    }

    suspend fun sayHelloUnary(message: String): HelloReply {
        val request : HelloRequest = buildRequest(message)
        return coroutineStub!!.sayHello(request)
    }

    suspend fun sayHelloClientStream(): HelloReply {
        val name = "sayHelloClientStream"
        val requestFlow: Flow<HelloRequest> = flow {
            for (i in 0 until 5) {
                val requestName = "$name$i"
                emit(buildRequest(requestName))
                delay(500) // 500 milliseconds delay between requests
            }
        }

        return coroutineStub!!.sayHelloClientStream(requestFlow)
    }

    suspend fun sayHelloServerStream(){
        val message : String = "serverStreamExample"
        val request = buildRequest(message)
        coroutineStub!!.sayHelloServerStream(request).collect { reply ->
            println("Server Stream Greeting : ${reply.message}")
        }
    }

    suspend fun sayHelloBidirectionalStream(){
        val name = "sayHelloClientStream"
        val requestFlow: Flow<HelloRequest> = flow {
            for (i in 0 until 5) {
                val requestName = "$name$i"
                emit(buildRequest(requestName))
                delay(500) // 500 milliseconds delay between requests
            }
        }

        coroutineStub!!.sayHelloBidirectionalStream(requestFlow).collect { reply ->
            println("Bidirectional Stream Greeting : ${reply.message}")
        }
    }

    private fun buildRequest(message: String): HelloRequest {
        return HelloRequest.newBuilder()
            .setName(message)
            .build()
    }
}