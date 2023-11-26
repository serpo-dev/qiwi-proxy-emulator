import asyncio
import websockets

async def send_message():
    async with websockets.connect("ws://localhost:8765") as websocket:
        message = input("Введите ваше сообщение: ")
        await websocket.send(message)
        response = await websocket.recv()
        print(f"Сервер ответил: {response}")

asyncio.get_event_loop().run_until_complete(send_message())