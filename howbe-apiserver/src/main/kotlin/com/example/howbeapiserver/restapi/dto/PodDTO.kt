package com.example.howbeapiserver.restapi.dto
import com.example.grpc.ContainerStatus

data class ContainerStatusDTO(
    val id: String?,
    val name: String,
    val image: String
)

data class PodDTO (
    val id: String,
    val name: String,
    val containerStatuses : Array<ContainerStatusDTO>
)

fun ContainerStatusDTO.toContainerStatus(): ContainerStatus {
    return ContainerStatus.newBuilder()
        .setId(this.id)
        .setName(this.name)
        .setImage(this.image)
        .build()
}