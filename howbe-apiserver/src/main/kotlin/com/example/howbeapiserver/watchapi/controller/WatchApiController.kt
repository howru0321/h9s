package com.example.howbeapiserver.watchapi.controller

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.delay
import java.time.LocalTime
import redis.clients.jedis.Jedis

fun Application.configureRouting() {
    val jedis = Jedis("localhost", 6379)
    routing {
        get("/api/v1/pods") {
            call.response.cacheControl(CacheControl.NoCache(null))
            val currentTime = LocalTime.now().toString()
            jedis.lpush("scheduler_queue", currentTime)
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