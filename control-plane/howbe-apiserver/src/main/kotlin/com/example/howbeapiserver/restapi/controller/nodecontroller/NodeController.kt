package com.example.howbeapiserver.restapi.controller.nodecontroller

import com.example.grpc.*
import com.example.howbeapiserver.restapi.service.nodeservice.dto.NodeStatusDTO
import com.example.howbeapiserver.restapi.service.nodeservice.NodeService
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter


@RestController
@RequestMapping("/api/v1/nodes")
class NodeController(private val nodeService: NodeService) {

    @GetMapping("/{nodeName}")
    fun getNodeByName(@PathVariable nodeName: String): String {
        val response : RangeResponse =
            runBlocking<RangeResponse> { nodeService.getNodeByName(nodeName) }

        return try {
            if (response.kvsCount > 0) {
                response.getKvs(0).value.toStringUtf8()
            } else {
                "No node : ${nodeName} found"
            }
        } catch (e: IndexOutOfBoundsException) {
            "Error: No data available"
        } catch (e: Exception) {
            "An unexpected error occurred: ${e.message}"
        }
    }
    @GetMapping("")
    fun getNodes(
        @RequestParam(required = false) watch: Boolean?,
        @RequestParam(required = false) fieldSelector: String?
    ): Any {
        if(watch==true){
            println("Watch Nodes")
            return runBlocking<SseEmitter> { nodeService.watchNodes(fieldSelector) }
        } else{
            val response : RangeResponse =
                runBlocking<RangeResponse> { nodeService.getNodes() }
            return try {
                if (response.kvsCount > 0) {
                    response.kvsList.map { kv ->
                        kv.value.toStringUtf8()
                    }
                } else {
                    listOf("No nodes found")
                }
            } catch (e: IndexOutOfBoundsException) {
                listOf("Error: No data available")
            } catch (e: Exception) {
                listOf("An unexpected error occurred: ${e.message}")
            }
        }
    }
    @PostMapping("")
    fun createNode(@RequestBody updateNodeStatusRequest : NodeStatusDTO): String {
        val nodeName : String = updateNodeStatusRequest.metadata!!.name
        val rangeResponse : RangeResponse =
            runBlocking<RangeResponse> { nodeService.getNodeByName(nodeName) }

        try {
            if (rangeResponse.kvsCount > 0) {
                return "${nodeName} node is already exists"
            }
        } catch (e: IndexOutOfBoundsException) {
            "Error: No data available"
        } catch (e: Exception) {
            "An unexpected error occurred: ${e.message}"
        }
        val uuid = UUID.randomUUID().toString()
        updateNodeStatusRequest.metadata!!.uid=uuid
        val putResponse : PutResponse =
            runBlocking<PutResponse>{ nodeService.updateNode(nodeName, updateNodeStatusRequest) }
        return "Successfully create ${nodeName} node"
    }
    @PostMapping("{nodeName}")
    fun updateNode(@PathVariable nodeName: String, @RequestBody updateNodeStatusRequest : NodeStatusDTO): String {
        val rangeResponse : RangeResponse =
            runBlocking<RangeResponse> { nodeService.getNodeByName(nodeName) }

        try {
            if (rangeResponse.kvsCount == 0) {
                return "${nodeName} node is not exist"
            }
        } catch (e: IndexOutOfBoundsException) {
            "Error: No data available"
        } catch (e: Exception) {
            "An unexpected error occurred: ${e.message}"
        }
        val response : PutResponse =
            runBlocking<PutResponse>{ nodeService.updateNode(nodeName, updateNodeStatusRequest) }
        return "Successfully update ${nodeName} node"
    }

    @DeleteMapping("/{nodeName}")
    fun deleteNode(@PathVariable nodeName : String) : String {
        val response : DeleteRangeResponse =
            runBlocking <DeleteRangeResponse> { nodeService.deleteNode(nodeName) }

        return try {
            if (response.deleted > 0) {
                response.getPrevKvs(0).value.toStringUtf8()
            } else {
                "No node : ${nodeName} found"
            }
        } catch (e: IndexOutOfBoundsException) {
            "Error: No data available"
        } catch (e: Exception) {
            "An unexpected error occurred: ${e.message}"
        }
    }
}