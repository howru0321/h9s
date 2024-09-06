package com.example.howbeapiserver.grpcstub.kv

import com.example.grpc.DeleteRangeRequest
import com.example.grpc.DeleteRangeResponse
import com.example.grpc.KVGrpcKt
import com.example.grpc.PutRequest
import com.example.grpc.PutResponse
import com.example.grpc.RangeRequest
import com.example.grpc.RangeResponse
import io.grpc.ManagedChannelBuilder
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

class KVStub(host: String?, port: Int) {
    private var coroutineStub : KVGrpcKt.KVCoroutineStub? = null

    private val watchAPIEmitter : SseEmitter = SseEmitter(30 * 60 * 1000)

    init {
        val managedChannel = ManagedChannelBuilder
            .forAddress(host, port)
            .usePlaintext()
            .build()
        coroutineStub = KVGrpcKt.KVCoroutineStub(managedChannel)
    }

    suspend fun Range(rangeRequest: RangeRequest): RangeResponse {
        return coroutineStub!!.range(rangeRequest)
    }

    suspend fun Put(putRequest : PutRequest) : PutResponse {
        return coroutineStub!!.put(putRequest)
    }

    suspend fun DeleteRange(deleteRangeRequest : DeleteRangeRequest) : DeleteRangeResponse {
        return coroutineStub!!.deleteRange(deleteRangeRequest)
    }

}