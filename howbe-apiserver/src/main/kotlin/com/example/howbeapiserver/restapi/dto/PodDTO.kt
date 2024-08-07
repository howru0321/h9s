package com.example.howbeapiserver.dto
import com.example.grpc.ContainerMetadata

data class ContainerMetadataDTO(
    val id: String?,
    val name: String,
    val image: String
)

data class PodDTO (
    val name: String,
    val containers : Array<ContainerMetadataDTO>
)

fun ContainerMetadataDTO.toContainerMetadata(): ContainerMetadata {
    return ContainerMetadata.newBuilder()
        .setId(this.id)
        .setName(this.name)
        .setImage(this.image)
        .build()
}