package com.example.howbeapiserver.restapi.service.podservice.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class BindingPodDTO(
    val kind: String,
    val metadata: MetadataDTO1,
    val target: TargetDTO
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MetadataDTO1(
    val name: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TargetDTO(
    val kind: String,
    val name: String
)