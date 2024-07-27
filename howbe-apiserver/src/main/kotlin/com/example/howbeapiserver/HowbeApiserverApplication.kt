package com.example.howbeapiserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HowbeApiserverApplication

fun main(args: Array<String>) {
    runApplication<HowbeApiserverApplication>(*args)
    //val helloClient = HelloClient("howbe-db", 50051)
    //System.out.println(helloClient.sayHelloWithBlocking("hello world").message)
}
