package com.example.howbeapiserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import kotlin.concurrent.thread


@SpringBootApplication
class HowbeApiserverApplication


fun main(args: Array<String>) {
    thread {
        runApplication<HowbeApiserverApplication>(*args)
    }
}