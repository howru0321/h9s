package com.example.howbeapiserver.service

import com.example.howbeapiserver.dto.PodRequest
import org.springframework.stereotype.Service

@Service
class PodService {
    fun createPod(podRequest : PodRequest){
        println(podRequest.name)
        println(podRequest.containers[0].name)
        println(podRequest.containers[0].image)
    }

}