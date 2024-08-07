package com.example.howbeapiserver.grpcclient

import com.example.grpc.*
import com.example.howbeapiserver.restapi.dto.PodDTO
import com.example.grpc.ApiserverEtcdServiceGrpcKt
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

    suspend fun updatePodStatus(podMetadata: PodDTO): PodResponse {
        val request : PodRequest = buildRequest(podMetadata)
        return coroutineStub!!.updatePodStatus(request)
    }

    private fun buildRequest(podMetadata: PodDTO): PodRequest {
        val podRequestBuilder : PodRequest.Builder =
            PodRequest.newBuilder()
                .setId(podMetadata.id)
                .setName(podMetadata.name)
        for(container in podMetadata.containerStatuses){
            val containerMetadata = ContainerStatus.newBuilder()
                .setName(container.name)
                .setImage(container.image)
            podRequestBuilder.addContainerStatuses(containerMetadata)
        }
        return podRequestBuilder.build()
    }
}