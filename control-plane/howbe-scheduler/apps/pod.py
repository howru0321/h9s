import asyncio
import time
import os
from sseclient import SSEClient
import traceback
from typing import Optional
import json
import requests

from apps.utils.type_casting import parse_json_to_object_status, parse_sse_message
from apps.dataclass.pod_status import PodStatusDTO
from apps.dataclass.node_status import NodeStatusDTO
from apps.utils.redis import redis_client
from apps.utils.scheduler import filtering_nodes, scoring_nodes, schedule_pod

# redis_host = os.getenv('REDIS_HOST', 'localhost')
# redis_port = int(os.getenv('REDIS_PORT', 6379))
# redis_client = redis.Redis(
#     host=redis_host,
#     port=redis_port,
#     db=0,
#     socket_timeout=5,
#     retry_on_timeout=True
# )

apiserver_host = os.getenv('APISERVER_HOST', 'localhost')

def binding(podName : str, bindingNodeName : str):
    url = f"http://{apiserver_host}:8081/api/v1/pods/binding"
    binding_pod_data = {
        "kind": "Binding",
        "metadata": {
            "name": podName
        },
        "target": {
            "kind": "Node",
            "name": bindingNodeName
        }
    }
    json_data = json.dumps(binding_pod_data)
    headers = {
        "Content-Type": "application/json"
    }
    try:
        # Send the POST request
        response = requests.post(url, data=json_data, headers=headers)

        # Check if the request was successful
        if response.status_code == 200:
            print("Response:", response.text)
        else:
            print("Request failed with status code:", response.status_code)
            print("Response:", response.text)

    except requests.exceptions.RequestException as e:
        print("An error occurred:", e)

def event_generator_pods():
    url = f"http://{apiserver_host}:8081/api/v1/pods?watch=true&fieldSelector=spec.nodeName="
    try:
        message = SSEClient(url)
        for msg in message:
            data = parse_sse_message(msg)
            if data:
                pod : PodStatusDTO = parse_json_to_object_status(data)
                yield pod.to_json()
    except Exception as e:
        print(f"Error in SSE connection: {e}")
        print("Attempting to reconnect...")
        return traceback.print_exc()

async def push_redis_pods():
    for event in event_generator_pods():
        redis_client.lpush("pods", event)
        
def run_asyncio_loop_pods():
    loop = asyncio.new_event_loop()
    asyncio.set_event_loop(loop)
    loop.run_until_complete(push_redis_pods())
    
def process_events_pods():
    while True:
        task = redis_client.rpop('pods')
        if task:
            pod : PodStatusDTO = PodStatusDTO.from_json(task)
            if pod.status.conditions[0].status == "True":
                continue
            podName : str = pod.metadata.name
            bindingNodeName : Optional[str] = schedule_pod(pod)
            print("podName:", podName)
            print("bindingNodeName:", bindingNodeName)
            binding(podName, bindingNodeName)
        else:
            print("No pod tasks in the queue, waiting...")
        time.sleep(5)