package com.example.howbeapiserver.grpcstub.watch.utils

import com.example.grpc.WatchCancelRequest
import com.example.grpc.WatchCreateRequest
import com.example.grpc.WatchRequest
import com.google.protobuf.ByteString

fun toWatchCreateRequest(key: String, filterType: Int?, rangeEnd: String, watchId: Long): WatchRequest {
    val builder = WatchCreateRequest.newBuilder()
        .setKey(ByteString.copyFromUtf8(key))
        .setRangeEnd(ByteString.copyFromUtf8(rangeEnd))
        .setWatchId(watchId)

    filterType?.let { type ->
        builder.addFilters(WatchCreateRequest.FilterType.forNumber(type))
    }

    val watchCreateRequest: WatchCreateRequest = builder.build()
    return WatchRequest.newBuilder()
        .setCreateRequest(
            watchCreateRequest
        )
        .build()
}

fun toWatchCancelRequest(watchId: Long): WatchRequest {
    val builder = WatchCancelRequest.newBuilder()
        .setWatchId(watchId)
    val watchCancelRequest : WatchCancelRequest = builder.build()
    return WatchRequest.newBuilder()
        .setCancelRequest(
            watchCancelRequest
        )
        .build()
}