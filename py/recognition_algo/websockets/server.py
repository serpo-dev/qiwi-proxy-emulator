import asyncio
import websockets

# Обработчик входящих сообщений
async def handler(websocket, path):
    async for message in websocket:
        # Обработка полученного сообщения
        print(f"Получено сообщение от клиента: {message}")

        # Отправка ответного сообщения клиенту
        response = f"Сервер получил сообщение: {message}"
        await websocket.send(response)

# Запуск сервера
START_SERVER = websockets.serve(handler, "localhost", 8765)
asyncio.get_event_loop().run_until_complete(START_SERVER)
asyncio.get_event_loop().run_forever()