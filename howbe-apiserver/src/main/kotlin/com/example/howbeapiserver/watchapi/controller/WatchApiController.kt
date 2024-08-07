package com.example.howbeapiserver.scheduler.controller

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.delay
import java.time.LocalTime

fun Application.configureRouting() {
    routing {
        get("/stream") {
            call.response.cacheControl(CacheControl.NoCache(null))
            call.respondTextWriter(contentType = ContentType.Text.EventStream) {
                while (true) {
                    val currentTime = LocalTime.now()
                    write("data: The time is: $currentTime\n\n")
                    flush()
                    delay(1000)
                }
            }
        }
    }
}