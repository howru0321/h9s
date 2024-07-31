package com.example.howbeapiserver.controller

import com.example.howbeapiserver.dto.PodRequest
import com.example.howbeapiserver.service.PodService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/pod")
class PodController(private val podService: PodService) {
    @PostMapping("")
    fun creatPod(@RequestBody podRequest : PodRequest) {
        podService.createPod(podRequest)
    }
}