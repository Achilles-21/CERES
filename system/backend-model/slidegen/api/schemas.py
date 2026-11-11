from datetime import datetime
from typing import Optional, Literal
from pydantic import BaseModel


AgentType = Literal["proxy", "designer", "reviewer", "planner", "writer"]
MessageType = Literal["user_input", "agent_message", "approval", "status", "error", "slide_complete", "feedback", "need_input"]


class WSMessage(BaseModel):
    """WebSocket message schema for communication between frontend and backend."""
    type: MessageType
    agent_name: Optional[str] = None
    agent_type: Optional[AgentType] = None
    content: str
    timestamp: datetime = datetime.now()

    class Config:
        json_encoders = {
            datetime: lambda v: v.isoformat()
        }


class UserInputMessage(BaseModel):
    """Message sent from frontend when user provides input."""
    type: Literal["user_input", "approval", "feedback", "ping"]
    content: str
    locale: Optional[str] = None


class SlideInfo(BaseModel):
    """Response schema for slide information."""
    user_id: str
    slide_id: str
    content: str


class ImageInfo(BaseModel):
    """Schema for image metadata."""
    image_id: int
    position: str
    query: str
    pos_idx: list[int] = []


class SlideImagesResponse(BaseModel):
    """Response schema for slide images list."""
    user_id: str
    slide_id: str
    images: list[ImageInfo]
