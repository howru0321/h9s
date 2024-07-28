package com.example.howbeapiserver

import io.grpc.ServerBuilder
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HowbeApiserverApplication

fun runHelloServer(){
    val helloServer = HelloServer()
    val server = ServerBuilder
        .forPort(50051)
        .addService(helloServer)
        .build()

    Runtime.getRuntime().addShutdownHook(Thread {
        server.shutdown()
        server.awaitTermination()
    })

    server.start()
    server.awaitTermination()
}

fun main(args: Array<String>) {
    runApplication<HowbeApiserverApplication>(*args)
    runHelloServer()
}
