package com.example.howbeapiserver.restapi.service.nodeservice

import com.example.grpc.*
import com.example.howbeapiserver.grpcstub.kv.KVStub
import com.example.howbeapiserver.grpcstub.kv.utils.toDeleteRangeRequest
import com.example.howbeapiserver.grpcstub.kv.utils.toPutRequest
import com.example.howbeapiserver.grpcstub.kv.utils.toRangeRequest
import com.example.howbeapiserver.grpcstub.watch.WatchStub
import com.example.howbeapiserver.restapi.service.nodeservice.dto.NodeStatusDTO
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import com.example.howbeapiserver.grpcstub.watch.utils.toWatchCreateRequest
import com.example.howbeapiserver.utils.createKey
import com.example.howbeapiserver.utils.getNextKey

@Service
class NodeService(private val kvStub: KVStub, private val watchStub: WatchStub){
    suspend fun updateNode(nodeName : String, updateNodeStatusRequest : NodeStatusDTO) : PutResponse {
        return kvStub.Put(toPutRequest(nodeName, updateNodeStatusRequest))
    }

    suspend fun getNodeByName(nodeName : String): RangeResponse {
        val key = createKey("Node",nodeName)
        return kvStub.Range(
            toRangeRequest(key, null, 0, false, false)
        )
    }
    suspend fun getNodes(): RangeResponse {
        val key = createKey("Node", "")
        val rangeEnd = getNextKey(key)
        return kvStub.Range(
            toRangeRequest(key, rangeEnd, 0, false, false)
        )
    }

    suspend fun deleteNode(podName : String) : DeleteRangeResponse {
        val key = createKey("Node", "")
        return kvStub.DeleteRange(
            toDeleteRangeRequest(key, null, true)
        )
    }

    suspend fun watchNodes(fieldSelector : String?) : SseEmitter {
        val watch_id : Long = watchStub.getWatchId()
        val key = createKey("Node", "")
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
}