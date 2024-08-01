package com.example.howbeapiserver.dto

data class PodDTO (
    val name: String,
    val containers : Array<ContainerMetadata>
)

data class ContainerMetadata (
    val name : String,
    val image : String
)