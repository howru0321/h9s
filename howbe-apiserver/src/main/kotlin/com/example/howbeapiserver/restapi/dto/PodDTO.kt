package com.example.howbeapiserver.restapi.dto

data class ContainerStatusDTO(
    val name: String,
    val image: String
)
data class PodDTO (
    val name: String,
    val containerStatuses : Array<ContainerStatusDTO>
)