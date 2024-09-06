package com.example.howbeapiserver.grpcstub.kv.utils

import com.example.grpc.DeleteRangeRequest
import com.example.grpc.PutRequest
import com.example.grpc.RangeRequest
import com.example.howbeapiserver.restapi.service.dto.ObjectStatusDTO
import com.example.howbeapiserver.utils.createKey
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.protobuf.ByteString


fun toPutRequest(nodeName : String, objectStatusDTO: ObjectStatusDTO) : PutRequest {
    val mapper = jacksonObjectMapper().apply {
        enable(SerializationFeature.INDENT_OUTPUT)
    }
    val objectType : String = objectStatusDTO.kind
    val json = mapper.writeValueAsString(objectStatusDTO)
    return PutRequest.newBuilder()
        .setKey(ByteString.copyFromUtf8(createKey(objectType,nodeName)))
        .setValue(ByteString.copyFromUtf8(json))
        .setPrevKv(true)
        .build()
}
fun toRangeRequest(key : String, rangeEnd : String?, limit : Long, countOnly : Boolean, keysOnly : Boolean) : RangeRequest {
    val builder = RangeRequest.newBuilder()
        .setKey(ByteString.copyFromUtf8(key))
        //.setRangeEnd(ByteString.copyFromUtf8(rangeEnd))
        .setLimit(limit)
        .setCountOnly(countOnly)
        .setKeysOnly(keysOnly)

    rangeEnd?.let{ rEnd ->
        builder.setRangeEnd(ByteString.copyFromUtf8(rEnd))
    }

    return builder.build()
}

fun toDeleteRangeRequest(key : String, rangeEnd : String?, prev_kv : Boolean) : DeleteRangeRequest {
    val builder = DeleteRangeRequest.newBuilder()
        .setKey(ByteString.copyFromUtf8(key))
        //.setRangeEnd(ByteString.copyFromUtf8(rangeEnd))
        .setPrevKv(prev_kv)

    rangeEnd?.let{ rEnd ->
        builder.setRangeEnd(ByteString.copyFromUtf8(rEnd))
    }

    return builder.build()
}
