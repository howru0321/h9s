package com.example.howbeapiserver.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import com.example.howbeapiserver.HelloClient

@RestController
@RequestMapping("")
class HelloController(private val helloClient: HelloClient) {

    @GetMapping("/hello")
    fun sayHello(): String {
        val response = helloClient.sayHelloWithBlocking("how haha")
        return response.message
    }
}
