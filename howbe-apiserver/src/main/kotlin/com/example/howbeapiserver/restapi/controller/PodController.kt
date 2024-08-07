package com.example.howbeapiserver.controller

import com.example.grpc.PodResponse
import com.example.howbeapiserver.dto.PodDTO
import com.example.howbeapiserver.service.PodService
import kotlinx.coroutines.runBlocking
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/pod")
class PodController(private val podService: PodService) {
    @PostMapping("", consumes = ["application/json"])
    fun creatPod(@RequestBody podRequest : PodDTO) : String{
        val response = runBlocking<PodResponse>{ podService.createPod(podRequest) }
        return "PodId: ${response.podId}, Message: ${response.message}"
    }
}