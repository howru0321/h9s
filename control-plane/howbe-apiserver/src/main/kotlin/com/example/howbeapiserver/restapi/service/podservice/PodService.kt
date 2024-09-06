package com.example.howbeapiserver.restapi.service.podservice

import com.example.grpc.DeleteRangeResponse
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import com.example.grpc.PutResponse
import com.example.grpc.RangeResponse
import com.example.howbeapiserver.grpcstub.kv.KVStub
import com.example.howbeapiserver.grpcstub.kv.utils.toDeleteRangeRequest
import com.example.howbeapiserver.grpcstub.kv.utils.toPutRequest
import com.example.howbeapiserver.grpcstub.kv.utils.toRangeRequest
import com.example.howbeapiserver.grpcstub.watch.WatchStub
import com.example.howbeapiserver.grpcstub.watch.utils.toWatchCreateRequest
import com.example.howbeapiserver.restapi.service.podservice.dto.PodStatusDTO
import com.example.howbeapiserver.utils.createKey
import com.example.howbeapiserver.utils.getNextKey

@Service
class PodService(private val kvStub: KVStub, private val watchStub: WatchStub) {
    suspend fun updatePod(podName : String, updatePodStatusRequest : PodStatusDTO) : PutResponse{
        return kvStub.Put(toPutRequest(podName, updatePodStatusRequest))
    }
    suspend fun getPodByName(podName : String): RangeResponse{
        val key = createKey("Pod",podName)
        return kvStub.Range(
            toRangeRequest(key, null, 0, false, false)
        )
    }
    suspend fun getPods():RangeResponse{
        val key = createKey("Pod", "")
        val rangeEnd = getNextKey(key)
        return kvStub.Range(
            toRangeRequest(key, rangeEnd, 0, false, false)
        )
    }

    suspend fun deletePod(podName : String) : DeleteRangeResponse{
        val key = createKey("Pod", podName)
        return kvStub.DeleteRange(
            toDeleteRangeRequest(key, null, true)
        )
    }

    suspend fun watchPods(fieldSelector : String?) : SseEmitter {
        val watch_id : Long = watchStub.getWatchId()
        val key = createKey("Pod", "")
        val rangeEnd = getNextKey(key)
        val request = toWatchCreateRequest(
            key,
            null,
            rangeEnd,
            watch_id
        )
        watchStub.addWatchRequest(request)

        watchStub.setFieldSelectorResult(watch_id, fieldSelector)

        return watchStub.getWatchAPIEmitter(watch_id)
    }

    suspend fun bindingPod(podName : String, updatePodStatusRequest : PodStatusDTO) : PutResponse{
        return kvStub.Put(toPutRequest(podName, updatePodStatusRequest))
    }

}