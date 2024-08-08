package com.example.howbeapiserver.grpcclient.interfaces

import com.example.grpc.ContainerStatus

data class ContainerMetadata(
    val id: String?,
    val name: String,
    val image: String
)

fun ContainerMetadata.toContainerStatus(): ContainerStatus {
    return ContainerStatus.newBuilder()
        .setId(this.id)
        .setName(this.name)
        .setImage(this.image)
        .build()
}

data class PodStatus (
    val id: String,
    val name: String,
    val ContainerMetadatas : Array<ContainerMetadata>
)