import asyncio
import aiohttp
from fastapi import FastAPI
from sse_starlette.sse import EventSourceResponse

app = FastAPI()

async def event_generator():
    async with aiohttp.ClientSession() as session:
        async with session.get("http://localhost:3000/stream") as response:
            async for line in response.content:
                if line:
                    yield {"event": "message", "data": line.decode().strip()}

@app.get("/sse")
async def sse_endpoint():
    return EventSourceResponse(event_generator())