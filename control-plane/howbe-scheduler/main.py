import asyncio
import aiohttp
import redis
import json
import time
import threading
import os
from sseclient import SSEClient

# redis_host = os.getenv('REDIS_HOST', 'localhost')
# redis_port = int(os.getenv('REDIS_PORT', 6379))
# redis_client = redis.Redis(
#     host=redis_host,
#     port=redis_port,
#     db=0,
#     socket_timeout=5,
#     retry_on_timeout=True
# )

# apiserver_host = os.getenv('APISERVER_HOST', 'localhost')

# def event_generator_pods():
#     url = f"http://{apiserver_host}:8081/api/v1/pods?watch=true"
#     messages = SSEClient(url)
#     for msg in messages:
#         json_string = '{' + msg.data#????
#         yield json_string
        
# def event_generator_nodes():
#     url = f"http://{apiserver_host}:8081/api/v1/nodes?watch=true"
#     messages = SSEClient(url)
#     for msg in messages:
#         json_string = '{' + msg.data#????
#         yield json_string

# async def push_redis_pods():
#     for event in event_generator_pods():
#         redis_client.lpush("pods", event)
        
# async def push_redis_nodes():
#     for event in event_generator_nodes():
#         redis_client.lpush("nodes", event)
        
# def run_asyncio_loop_pods():
#     loop = asyncio.new_event_loop()
#     asyncio.set_event_loop(loop)
#     loop.run_until_complete(push_redis_pods())

# def run_asyncio_loop_nodes():
#     loop = asyncio.new_event_loop()
#     asyncio.set_event_loop(loop)
#     loop.run_until_complete(push_redis_nodes())
        
# def process_events_pods():
#     while True:
#         task = redis_client.rpop('pods')
#         if task:
#             event = json.loads(task)
#             print("Processing pod event at:", event['type'])
#         else:
#             print("No pod tasks in the queue, waiting...")
#         time.sleep(5)
        
# def process_events_nodes():
#     while True:
#         task = redis_client.rpop('nodes')
#         if task:
#             event = json.loads(task)
#             print("Processing node event at:", event['type'])
#         else:
#             print("No node tasks in the queue, waiting...")
#         time.sleep(5)


from apps.pod import run_asyncio_loop_pods, process_events_pods
from apps.node import run_asyncio_loop_nodes, process_events_nodes

if __name__ == "__main__":
    pushing_pods_thread = threading.Thread(target=run_asyncio_loop_pods)
    pushing_nodes_thread = threading.Thread(target=run_asyncio_loop_nodes)
    polling_pods_thread = threading.Thread(target=process_events_pods)
    polling_nodes_thread = threading.Thread(target=process_events_nodes)
    pushing_pods_thread.start()
    pushing_nodes_thread.start()
    polling_pods_thread.start()
    polling_nodes_thread.start()