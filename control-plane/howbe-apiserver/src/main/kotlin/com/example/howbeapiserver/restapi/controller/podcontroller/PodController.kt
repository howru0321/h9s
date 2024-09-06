package com.example.howbeapiserver.restapi.controller.podcontroller

import com.example.grpc.DeleteRangeResponse
import com.example.grpc.PutResponse
import com.example.grpc.RangeResponse
import com.example.howbeapiserver.restapi.service.podservice.PodService
import com.example.howbeapiserver.restapi.service.podservice.dto.*
import kotlinx.coroutines.runBlocking
import org.springframework.web.bind.annotation.*
import java.util.UUID
import com.example.howbeapiserver.restapi.service.dto.MetadataDTO

@RestController
@RequestMapping("/api/v1/pods")
class PodController(private val podService: PodService) {
    @GetMapping("/{podName}")
    fun getPodByName(@PathVariable podName: String): String {
        val response : RangeResponse =
            runBlocking<RangeResponse> { podService.getPodByName(podName) }

        return try {
            if (response.kvsCount > 0) {
                response.getKvs(0).value.toStringUtf8()
            } else {
                "No pod : ${podName} found"
            }
        } catch (e: IndexOutOfBoundsException) {
            "Error: No data available"
        } catch (e: Exception) {
            "An unexpected error occurred: ${e.message}"
        }
    }
    @GetMapping("")
    fun getPods(
        @RequestParam(required = false) watch: Boolean?,
        @RequestParam(required = false) fieldSelector: String?
    ): Any {
        if(watch==true){
            println("Watch Pods")
            return runBlocking { podService.watchPods(fieldSelector) }
        } else{
            val response : RangeResponse =
                runBlocking<RangeResponse> { podService.getPods() }
            return try {
                if (response.kvsCount > 0) {
                    response.kvsList.map { kv ->
                        kv.value.toStringUtf8()
                    }
                } else {
                    listOf("No pods found")
                }
            } catch (e: IndexOutOfBoundsException) {
                listOf("Error: No data available")
            } catch (e: Exception) {
                listOf("An unexpected error occurred: ${e.message}")
            }
        }
    }
    @PostMapping("")
    fun creatPod(@RequestBody createPodStatusRequest : PodStatusDTO) : String{
        val podName : String = createPodStatusRequest.metadata!!.name
        val rangeResponse : RangeResponse =
            runBlocking<RangeResponse> { podService.getPodByName(podName) }

        try {
            if (rangeResponse.kvsCount > 0) {
                return "${podName} pod is already exists"
            }
        } catch (e: IndexOutOfBoundsException) {
            "Error: No data available"
        } catch (e: Exception) {
            "An unexpected error occurred: ${e.message}"
        }
        val uuid = UUID.randomUUID().toString()
        createPodStatusRequest.metadata!!.uid=uuid
        if (createPodStatusRequest.status == null) {
            createPodStatusRequest.status = StatusDTO(
                phase = "Pending",
                nodeName = null,
                hostIp = null,
                podIp = null,
                conditions = listOf(
                    ConditionDTO("False", "PodScheduled")
                ),
                containerStatuses = null
            )
        }
                val putResponse : PutResponse =
            runBlocking <PutResponse> { podService.updatePod(podName, createPodStatusRequest) }
        return "Successfully create ${podName} pod"
    }

    @PostMapping("{podName}")
    fun updatePod(@PathVariable podName: String, @RequestBody createPodStatusRequest : PodStatusDTO) : String{
        val rangeResponse : RangeResponse =
            runBlocking<RangeResponse> { podService.getPodByName(podName) }

        try {
            if (rangeResponse.kvsCount == 0) {
                return "${podName} pod is deleted"
            }
        } catch (e: IndexOutOfBoundsException) {
            "Error: No data available"
        } catch (e: Exception) {
            "An unexpected error occurred: ${e.message}"
        }
        val putResponse : PutResponse =
            runBlocking <PutResponse> { podService.updatePod(podName, createPodStatusRequest) }
        println(createPodStatusRequest)
        return "Successfully update ${podName} pod"
    }

    @DeleteMapping("/{podName}")
    fun deletePod(@PathVariable podName : String) : String {
        val response : DeleteRangeResponse =
            runBlocking <DeleteRangeResponse> { podService.deletePod(podName) }

        return try {
            if (response.deleted > 0) {
                response.getPrevKvs(0).value.toStringUtf8()
            } else {
                "No pod : ${podName} found"
            }
        } catch (e: IndexOutOfBoundsException) {
            "Error: No data available"
        } catch (e: Exception) {
            "An unexpected error occurred: ${e.message}"
        }
    }

    @PostMapping("binding")
    fun bindingPod(@RequestBody bindingPodRequest : BindingPodDTO) : String {
        val podName : String = bindingPodRequest.metadata.name
        println("podName : ${podName}")
        val bindingNodeName = bindingPodRequest.target.name
        println("bindingNodeName : ${bindingNodeName}")
        val updatePodStatus : PodStatusDTO = PodStatusDTO(
            kind = "Pod",
            metadata = MetadataDTO(
                name = podName,
                uid = null
            ),
            spec = SpecDTO(
                nodeName = bindingNodeName,
                containers = null  // Not updating containers in this operation
            ),
            status = StatusDTO(
                phase = null,  // Not updating phase in this operation
                nodeName = null,  // This is in spec, not status for Kubernetes
                hostIp = null,
                podIp = null,
                conditions = listOf(
                    ConditionDTO(
                        status = "True",
                        type = "PodScheduled"
                    )
                ),
                containerStatuses = null  // Not updating container statuses in this operation
            )
        )
        val response : PutResponse =
            runBlocking<PutResponse>{ podService.updatePod(podName, updatePodStatus) }
        return "Binding Successful"
    }
}