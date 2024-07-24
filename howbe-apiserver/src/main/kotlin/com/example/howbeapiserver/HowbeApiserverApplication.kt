package com.example.howbeapiserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

import io.grpc.Server
import io.grpc.ServerBuilder

@SpringBootApplication
class HowbeApiserverApplication

fun main(args: Array<String>) {
    //runApplication<HowbeApiserverApplication>(*args)
    val server: Server = ServerBuilder.forPort(50051)
        .addService(HellogRPC())
        .build()
        .start()

    println("Server started, listening on 50051")

    Runtime.getRuntime().addShutdownHook(Thread {
        println("Shutting down gRPC server")
        server.shutdown()
        println("Server shut down")
    })

    server.awaitTermination()
}
