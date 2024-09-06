package com.example.howbeapiserver.restapi.service.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
open class ObjectStatusDTO(
    open val kind: String,
    open var metadata: MetadataDTO?
)

data class MetadataDTO(
    val name: String,
    var uid: String?
)