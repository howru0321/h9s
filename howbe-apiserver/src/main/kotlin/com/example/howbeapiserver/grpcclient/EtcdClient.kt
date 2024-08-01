package com.example.howbeapiserver.grpcclient

import com.example.grpc.*
import com.example.howbeapiserver.dto.PodDTO
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

    suspend fun createPod(podMetadata: PodDTO): PodResponse {
        val request : PodRequest = buildRequest(podMetadata)
        return coroutineStub!!.createPod(request)
    }

    private fun buildRequest(podMetadata: PodDTO): PodRequest {
        val podRequestBuilder : PodRequest.Builder =
            PodRequest.newBuilder()
                .setName(podMetadata.name)
        for(container in podMetadata.containers){
            val containerMetadata = ContainerMetadata.newBuilder()
                .setName(container.name)
                .setImage(container.image)
            podRequestBuilder.addContainers(containerMetadata)
        }

        return podRequestBuilder.build()
    }
}