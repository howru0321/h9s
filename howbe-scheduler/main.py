import asyncio
import aiohttp
import redis
import json
import time
import threading
import os

redis_host = os.getenv('REDIS_HOST', 'localhost')
redis_port = int(os.getenv('REDIS_PORT', 6379))
redis_client = redis.Redis(host=redis_host, port=redis_port, db=0)

apiserver_host = os.getenv('APISERVER_HOST', 'localhost')

async def event_generator():
    async with aiohttp.ClientSession() as session:
        url = f"http://{apiserver_host}:3000/api/v1/pods"
        async with session.get(url) as response:
            async for line in response.content:
                if line:
                    yield {"event": "message", "data": line.decode().strip()}

async def main():
    async for event in event_generator():
        #print("Received event:", event)
        redis_client.lpush("scheduler_queue", json.dumps(event))
        
def run_asyncio_loop():
    loop = asyncio.new_event_loop()
    asyncio.set_event_loop(loop)
    loop.run_until_complete(main())
        
def process_events():
    while True:
        task = redis_client.rpop('scheduler_queue')
        if task:
            event = json.loads(task.decode('utf-8'))
            print("Processing event at:", event["data"])
        else:
            print("No tasks in the queue, waiting...")
            time.sleep(1)

if __name__ == "__main__":
    asyncio_thread = threading.Thread(target=run_asyncio_loop)
    asyncio_thread.start()
    process_events()