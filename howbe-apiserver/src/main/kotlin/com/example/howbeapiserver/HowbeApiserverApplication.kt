package com.example.howbeapiserver

import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class HowbeApiserverApplication

fun main(args: Array<String>) {
    //runApplication<HowbeApiserverApplication>(*args)
    val helloClient = HelloClient("0.0.0.0", 50051)
    System.out.println(helloClient.sayHelloWithBlocking("hello world").message)
}
