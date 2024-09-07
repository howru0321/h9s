package com.example.howbeapiserver.grpcstub.watch

import com.example.grpc.*
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import javax.annotation.PostConstruct
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.runBlocking
import com.example.howbeapiserver.grpcstub.watch.utils.toWatchCancelRequest
import com.example.howbeapiserver.grpcstub.watch.utils.FieldSelectorResult
import com.example.howbeapiserver.grpcstub.watch.utils.matchesFieldSelector
import com.example.howbeapiserver.grpcstub.watch.utils.parseFieldSelector

class WatchStub(
    host: String?,
    port: Int
) {
    private var coroutineStub: WatchGrpcKt.WatchCoroutineStub? = null

    private val _requestFlow = MutableSharedFlow<WatchRequest>()
    val requestFlow = _requestFlow.asSharedFlow()

    suspend fun addWatchRequest(request: WatchRequest) {
        _requestFlow.emit(request)
    }

    private val watchIdList = mutableSetOf<Long>()
    private var watchIdCounter: Long = 0

    fun getWatchId(): Long {
        var watchId: Long
        do {
            watchIdCounter++
            if (watchIdCounter >= Long.MAX_VALUE) {
                watchIdCounter = 1
            }
            watchId = watchIdCounter
        } while (watchIdList.contains(watchId))

        watchIdList.add(watchId)
        return watchId
    }

    private val watchAPIEmitterMap: MutableMap<Long, SseEmitter> = mutableMapOf()
    private val fieldSelectorResultMap: MutableMap<Long, FieldSelectorResult?> = mutableMapOf()

    private fun createNewEmitter(watchId: Long): SseEmitter {
        val emitter = SseEmitter(30 * 60 * 1000L) // Consider making this configurable

        emitter.onCompletion {
            val request = toWatchCancelRequest(watchId)
            runBlocking{addWatchRequest(request)}
            watchAPIEmitterMap.remove(watchId)
        }
        emitter.onTimeout {
            val request = toWatchCancelRequest(watchId)
            runBlocking{addWatchRequest(request)}
            watchAPIEmitterMap.remove(watchId)
        }
        return emitter
    }
    fun getWatchAPIEmitter(id: Long): SseEmitter {
        val existingEmitter = watchAPIEmitterMap[id]
        if (existingEmitter != null) {
            return existingEmitter
        }

        val newEmitter = createNewEmitter(id)
        watchAPIEmitterMap[id] = newEmitter
        return newEmitter
    }

    fun setFieldSelectorResult(id: Long,fieldSelector : String?){
        if(fieldSelector==null){
            fieldSelectorResultMap[id]=null
        }else{
            fieldSelectorResultMap[id]=parseFieldSelector(fieldSelector)
        }
    }

    fun getFieldSelectorResult(id: Long) : FieldSelectorResult?{
        return fieldSelectorResultMap[id]
    }

    init {
        val managedChannel = ManagedChannelBuilder
            .forAddress(host, port)
            .usePlaintext()
            .build()
        coroutineStub = WatchGrpcKt.WatchCoroutineStub(managedChannel)
    }

    @PostConstruct
    fun init() {
        GlobalScope.launch {
            watchAPI(requestFlow)
        }
    }

    fun combineJsonStrings(value: String, preValue: String): String {
        val gson = Gson()
        val jsonObject = JsonObject()

        // value와 pre_value를 파싱하여 JsonElement로 변환
        val valueJson = JsonParser.parseString(value)
        val preValueJson = JsonParser.parseString(preValue)

        // 새 JsonObject에 추가
        jsonObject.add("value", valueJson)
        jsonObject.add("pre_value", preValueJson)

        // JsonObject를 문자열로 변환
        return gson.toJson(jsonObject)
    }
    suspend fun watchAPI(requestFlow : Flow<WatchRequest>){
        coroutineStub!!.watch(requestFlow).collect{ response : WatchResponse ->
            response.eventsList.forEach { event ->
                val key = event.kv.key.toStringUtf8()
                val value = event.kv.value.toStringUtf8()
                val pre_key = event.prevKv.key.toStringUtf8()
                val pre_value = event.prevKv.value.toStringUtf8()
                when (event.type) {
                    Event.EventType.PUT -> {
                        println("PUT event")
                    }
                    Event.EventType.DELETE -> {
                        println("DELETE event")
                    }
                    else -> {
                        println("Unknown event type")
                    }
                }
                val watch_id = response.watchId
                val outerJsonElement: JsonObject = JsonParser.parseString(value).asJsonObject
                val type: String = outerJsonElement.get("type")?.asString ?: ""
                try {
                    if(matchesFieldSelector(value,getFieldSelectorResult(watch_id))){
                        val response2client = combineJsonStrings(value, pre_value)
                        val data = "${response2client}\n\n"
                        val sseData = SseEmitter.event()
                            .name(type)
                            .data("data: $data")
                        getWatchAPIEmitter(watch_id).send(sseData)
                        println("Event sent successfully")
                    }
                } catch (e: Exception) {
                    println("Error sending event: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }
}