package com.example.howbeapiserver.grpcclient

import com.example.grpc.*
import com.example.howbeapiserver.restapi.dto.PodDTO
import com.example.grpc.ApiserverEtcdServiceGrpcKt
import com.example.howbeapiserver.grpcclient.interfaces.PodStatus
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder


class EtcdClient(host: String?, port: Int) {
    private var managedChannel: ManagedChannel? = null
    private var coroutineStub: ApiserverEtcdServiceGrpcKt.ApiserverEtcdServiceCoroutineStub? = null

    init {
        val managedChannel = ManagedChannelBuilder
            .forAddress(host, port)
            .usePlaintext()
            .build()
        coroutineStub = ApiserverEtcdServiceGrpcKt.ApiserverEtcdServiceCoroutineStub(managedChannel)
    }

    suspend fun updatePodStatus(podStatus: PodStatus): PodResponse {
        val request : PodRequest = buildRequest(podStatus)
        return coroutineStub!!.updatePodStatus(request)
    }

    private fun buildRequest(podStatus: PodStatus): PodRequest {
        val podRequestBuilder : PodRequest.Builder =
            PodRequest.newBuilder()
                .setId(podStatus.id)
                .setName(podStatus.name)
        for(container in podStatus.ContainerMetadatas){
            val containerMetadata = ContainerStatus.newBuilder()
                .setName(container.name)
                .setImage(container.image)
            podRequestBuilder.addContainerStatuses(containerMetadata)
        }
        return podRequestBuilder.build()
    }
}