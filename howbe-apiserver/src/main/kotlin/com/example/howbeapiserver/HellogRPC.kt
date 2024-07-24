package com.example.howbeapiserver

import com.example.grpc.HelloRequest
import com.example.grpc.HelloReply
import com.example.grpc.HelloWorldServiceGrpc
import io.grpc.stub.StreamObserver

class HellogRPC : HelloWorldServiceGrpc.HelloWorldServiceImplBase() {
    override fun sayHello(request: HelloRequest, responseObserver: StreamObserver<HelloReply>) {
        val name = request.name
        val message = "Hello, $name!"

        val reply = HelloReply.newBuilder()
            .setMessage(message)
            .build()
        responseObserver.onNext(reply)
        responseObserver.onCompleted()
    }
}