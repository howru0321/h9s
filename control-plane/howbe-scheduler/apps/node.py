import asyncio
import redis
import json
import time
import os
from sseclient import SSEClient
import traceback

from apps.cache import node_status_cache
from typing import Dict
from apps.utils.type_casting import parse_json_to_object_status, parse_sse_message
from apps.dataclass.node_status import NodeStatusDTO
from apps.utils.redis import redis_client


apiserver_host = os.getenv('APISERVER_HOST', 'localhost')

def event_generator_nodes():
    url = f"http://{apiserver_host}:8081/api/v1/nodes?watch=true"
    try:
        message = SSEClient(url)
        for msg in message:
            data = parse_sse_message(msg)
            if data:
                node : NodeStatusDTO = parse_json_to_object_status(data)
                yield node.to_json()
    except Exception as e:
        print(f"Error in SSE connection: {e}")
        print("Attempting to reconnect...")
        return traceback.print_exc()
        
async def push_redis_nodes():
    for event in event_generator_nodes():
        redis_client.lpush("nodes", event)
        
def run_asyncio_loop_nodes():
    loop = asyncio.new_event_loop()
    asyncio.set_event_loop(loop)
    loop.run_until_complete(push_redis_nodes())
    

def process_events_nodes():
    while True:
        task = redis_client.rpop('nodes')
        if task:
            node : NodeStatusDTO = NodeStatusDTO.from_json(task)
            node_status_cache.set(node)
        else:
            print("No node tasks in the queue, waiting...")
        time.sleep(5)