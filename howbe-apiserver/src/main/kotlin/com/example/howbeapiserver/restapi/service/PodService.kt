package com.example.howbeapiserver.restapi.service

import com.example.grpc.PodResponse
import com.example.howbeapiserver.restapi.dto.PodDTO
import com.example.howbeapiserver.grpcclient.EtcdClient
import com.example.howbeapiserver.grpcclient.interfaces.PodStatus
import com.example.howbeapiserver.grpcclient.interfaces.ContainerMetadata
import org.springframework.stereotype.Service

@Service
class PodService(private val etcdClient: EtcdClient) {
    suspend fun createPod(podRequest : PodDTO) : PodResponse{
        val containerMetadatas = podRequest.containerStatuses.map { containerStatusDTO ->
            ContainerMetadata(
                id = containerStatusDTO.name,
                name = containerStatusDTO.name,
                image = containerStatusDTO.image
            )
        }.toTypedArray()
        val podStatus = PodStatus(
            id = podRequest.name,
            name = podRequest.name,
            ContainerMetadatas = containerMetadatas
        )
        return etcdClient.updatePodStatus(podStatus)
    }

}