package com.example.howbeapiserver.restapi.service.podservice.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.example.howbeapiserver.restapi.service.dto.ObjectStatusDTO
import com.example.howbeapiserver.restapi.service.dto.MetadataDTO

//@JsonIgnoreProperties(ignoreUnknown = true)
@JsonIgnoreProperties(ignoreUnknown = true)
data class PodStatusDTO(
    override val kind: String,
    override var metadata: MetadataDTO?,
    var spec: SpecDTO?,
    var status: StatusDTO?
) : ObjectStatusDTO(kind, metadata)


@JsonIgnoreProperties(ignoreUnknown = true)
data class SpecDTO(
    var nodeName: String?,
    val containers: List<ContainerDTO>?
)

//@JsonIgnoreProperties(ignoreUnknown = true)
data class ContainerDTO(
    val image: String,
    val name: String,
    val resources: ResourceRequirementsDTO?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ResourceRequirementsDTO(
    val limits: ResourceDTO,
    val requests: ResourceDTO
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ResourceDTO(
    val cpu: String,
    val memory: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class StatusDTO(
    val phase: String?,
    val nodeName: String?,
    val hostIp: String?,
    val podIp: String?,
    var conditions: List<ConditionDTO>?,
    val containerStatuses: List<ContainerStatusDTO>?
)

//@JsonIgnoreProperties(ignoreUnknown = true)
data class ConditionDTO(
    var status: String,
    val type: String
)

//@JsonIgnoreProperties(ignoreUnknown = true)
data class ContainerStatusDTO(
    val image: String,
    val imageId: String,
    val name: String,
    val containerId: String,
    val state: String?
)
