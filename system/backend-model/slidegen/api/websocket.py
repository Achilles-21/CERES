import asyncio
from datetime import datetime
from typing import Dict, Optional
from fastapi import WebSocket
from .schemas import WSMessage


class ConnectionManager:
    """Manages WebSocket connections for multiple users."""

    def __init__(self):
        self.active_connections: Dict[str, WebSocket] = {}
        self.pending_inputs: Dict[str, asyncio.Queue] = {}
        self.awaiting_count: Dict[str, int] = {}

    async def connect(self, user_id: str, websocket: WebSocket):
        """Accept a new WebSocket connection."""
        await websocket.accept()
        self.active_connections[user_id] = websocket
        self.pending_inputs[user_id] = asyncio.Queue()
        self.awaiting_count[user_id] = 0

    def disconnect(self, user_id: str):
        """Remove a WebSocket connection."""
        if user_id in self.active_connections:
            del self.active_connections[user_id]
        if user_id in self.pending_inputs:
            del self.pending_inputs[user_id]
        if user_id in self.awaiting_count:
            del self.awaiting_count[user_id]

    def is_awaiting_input(self, user_id: str) -> bool:
        """Check whether any coroutine is currently waiting on user input for this user."""
        return self.awaiting_count.get(user_id, 0) > 0

    async def send_message(self, user_id: str, message: WSMessage):
        """Send a message to a specific user."""
        if user_id in self.active_connections:
            websocket = self.active_connections[user_id]
            await websocket.send_json(message.model_dump(mode='json'))

    async def send_agent_message(
        self,
        user_id: str,
        agent_name: str,
        agent_type: str,
        content: str
    ):
        """Send an agent message to a specific user."""
        message = WSMessage(
            type="agent_message",
            agent_name=agent_name,
            agent_type=agent_type,
            content=content,
            timestamp=datetime.now()
        )
        await self.send_message(user_id, message)

    async def send_status(self, user_id: str, content: str):
        """Send a status message to a specific user."""
        message = WSMessage(
            type="status",
            content=content,
            timestamp=datetime.now()
        )
        await self.send_message(user_id, message)

    async def send_error(self, user_id: str, content: str):
        """Send an error message to a specific user."""
        message = WSMessage(
            type="error",
            content=content,
            timestamp=datetime.now()
        )
        await self.send_message(user_id, message)

    async def send_need_input(self, user_id: str, prompt: str):
        """Notify frontend that backend is awaiting user input (re-enables the input box)."""
        message = WSMessage(
            type="need_input",
            content=prompt,
            timestamp=datetime.now()
        )
        await self.send_message(user_id, message)

    async def send_slide_complete(self, user_id: str, slide_id: str):
        """Notify user that slide generation is complete."""
        message = WSMessage(
            type="slide_complete",
            content=slide_id,
            timestamp=datetime.now()
        )
        await self.send_message(user_id, message)

    async def wait_for_input(self, user_id: str, timeout: Optional[float] = None) -> Optional[str]:
        """Wait for user input from the frontend."""
        if user_id not in self.pending_inputs:
            return None

        self.awaiting_count[user_id] = self.awaiting_count.get(user_id, 0) + 1
        try:
            if timeout:
                return await asyncio.wait_for(
                    self.pending_inputs[user_id].get(),
                    timeout=timeout
                )
            else:
                return await self.pending_inputs[user_id].get()
        except asyncio.TimeoutError:
            return None
        finally:
            self.awaiting_count[user_id] = max(0, self.awaiting_count.get(user_id, 0) - 1)

    async def receive_input(self, user_id: str, content: str):
        """Receive input from the frontend and put it in the queue."""
        if user_id in self.pending_inputs:
            await self.pending_inputs[user_id].put(content)


manager = ConnectionManager()
