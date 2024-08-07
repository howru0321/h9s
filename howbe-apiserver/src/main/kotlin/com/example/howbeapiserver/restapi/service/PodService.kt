package com.example.howbeapiserver.restapi.service

import com.example.grpc.PodResponse
import com.example.howbeapiserver.restapi.dto.PodDTO
import com.example.howbeapiserver.grpcclient.EtcdClient
import org.springframework.stereotype.Service

@Service
class PodService(private val etcdClient: EtcdClient) {
    suspend fun updatePodStatus(podMetadata : PodDTO) : PodResponse{
        return etcdClient.updatePodStatus(podMetadata)
    }

}