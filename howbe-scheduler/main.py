import asyncio
import aiohttp
from sse_starlette.sse import EventSourceResponse

async def event_generator():
    async with aiohttp.ClientSession() as session:
        async with session.get("http://localhost:3000/stream") as response:
            async for line in response.content:
                if line:
                    yield {"event": "message", "data": line.decode().strip()}
                    

async def main():
    async for event in event_generator():
        print(event)

if __name__ == "__main__":
    asyncio.run(main())