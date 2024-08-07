package com.example.howbeapiserver

import io.grpc.ServerBuilder
import io.ktor.http.*
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.delay
import java.time.LocalTime
import kotlin.concurrent.thread

import com.example.howbeapiserver.scheduler.controller.configureRouting

@SpringBootApplication
class HowbeApiserverApplication

fun runHelloServer(){
    val helloServer = HelloServer()
    val server = ServerBuilder
        .forPort(50053)
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
    thread {
        runApplication<HowbeApiserverApplication>(*args)
    }

    thread {
        runHelloServer()
    }

    embeddedServer(Netty, port = 3000, host = "0.0.0.0") {
        configureRouting()
    }.start(wait = true)
}


//fun Application.configureRouting() {
//    routing {
//        get("/stream") {
//            call.response.cacheControl(CacheControl.NoCache(null))
//            call.respondTextWriter(contentType = ContentType.Text.EventStream) {
//                while (true) {
//                    val currentTime = LocalTime.now()
//                    write("data: The time is: $currentTime\n\n")
//                    flush()
//                    delay(1000)
//                }
//            }
//        }
//    }
//}