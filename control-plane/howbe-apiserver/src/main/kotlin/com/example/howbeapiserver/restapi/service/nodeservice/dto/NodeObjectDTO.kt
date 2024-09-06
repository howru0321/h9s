package com.example.howbeapiserver.restapi.service.nodeservice.dto
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.example.howbeapiserver.restapi.service.dto.ObjectStatusDTO
import com.example.howbeapiserver.restapi.service.dto.MetadataDTO

@JsonIgnoreProperties(ignoreUnknown = true)
data class NodeStatusDTO(
    override val kind: String,
    override var metadata: MetadataDTO?,
    var spec: SpecDTO,
    var status: StatusDTO
) : ObjectStatusDTO(kind, metadata)

data class SpecDTO(
    val podCIDR: String?,
)

data class StatusDTO(
    val capacity: Map<String, String>?,
    val allocatable: Map<String, String>?,
    val conditions: List<ConditionDTO>?,
    val addresses: List<AddressDTO>?,
    val nodeInfo: NodeInfoDTO?,
    val images: List<ImageDTO>?
)

data class ConditionDTO(
    val type: String,
    val status: String,
    val reason: String,
    val message: String
)

data class AddressDTO(
    val type: String,
    val address: String
)

data class NodeInfoDTO(
    val containerRuntimeVersion: String,
    val operatingSystem: String,
    val architecture: String
)

data class ImageDTO(
    val names: List<String>,
    val sizeBytes: Long
)

//fun NodeStatusDTO.toPutRequest() : PutRequest {
//    val mapper = jacksonObjectMapper().apply {
//        enable(SerializationFeature.INDENT_OUTPUT)
//    }
//    val json = mapper.writeValueAsString(this)
//    return PutRequest.newBuilder()
//        .setKey(ByteString.copyFromUtf8(createKey("node",this.metadata.name)))
//        .setValue(ByteString.copyFromUtf8(json))
//        .setPrevKv(false)
//        .build()
//}