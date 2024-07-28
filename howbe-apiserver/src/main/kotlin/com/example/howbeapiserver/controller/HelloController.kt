package com.example.howbeapiserver.controller

import com.example.grpc.HelloReply
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestParam
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.Flow
import com.example.howbeapiserver.HelloClient

@RestController
@RequestMapping("")
class HelloController(private val helloClient: HelloClient) {

    @GetMapping("")
    fun how(): String {
        return "hahahh"
    }
    @GetMapping("/unary")
    fun sayHello(): String {
        val response = runBlocking<HelloReply> { helloClient.sayHelloUnary("how haha") }
        return response.message
    }

    @GetMapping("/client-stream")
    fun sayHelloClientStream(): String {
        val response = runBlocking<HelloReply> { helloClient.sayHelloClientStream() }
        return response.message
    }

    @GetMapping("/server-stream")
    fun sayHelloServerStream(): String {
        runBlocking<Unit> { helloClient.sayHelloServerStream() }
        return "server-stream"
    }

    @GetMapping("/bidirectional-stream")
    fun sayHelloBidirectionalStream(): String {
        runBlocking<Unit> { helloClient.sayHelloBidirectionalStream() }
        return "bidirectional-stream"
    }
}
