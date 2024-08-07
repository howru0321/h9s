package com.example.howbeapiserver.restapi.controller

import com.example.grpc.PodResponse
import com.example.howbeapiserver.restapi.dto.PodDTO
import com.example.howbeapiserver.restapi.service.PodService
import kotlinx.coroutines.runBlocking
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/pod")
class PodController(private val podService: PodService) {
    @PostMapping("", consumes = ["application/json"])
    fun creatPod(@RequestBody podRequest : PodDTO) : String{
        val response = runBlocking<PodResponse>{ podService.updatePodStatus(podRequest) }
        return "PodId: ${response.podId}, Message: ${response.message}"
    }
}