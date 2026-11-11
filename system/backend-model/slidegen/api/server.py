import asyncio
import json
from ..config import slide_cache_directory
from typing import Optional

from fastapi import FastAPI, WebSocket, WebSocketDisconnect, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from fastapi.responses import FileResponse

from autogen_core.models import SystemMessage, UserMessage
from autogen_ext.models.openai import OpenAIChatCompletionClient
from model_config import agent_generation_config
import os

from .schemas import WSMessage, UserInputMessage, SlideInfo, SlideImagesResponse, ImageInfo
from .websocket import manager

from ..main_web import run_agent_pipeline, generate_images_for_slide, run_revision_pipeline
from ..ppt_generator import PPTGenerator, generate_ppt_for_slide
from ..slidegen_i18n import normalize_locale, t

openai_api_key = os.getenv("OPENAI_API_KEY")


app = FastAPI(title="CeresSys API", description="Multi-agent ophthalmology slide generation system")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


async def determine_feedback_intent(feedback: str) -> str:
    """Use LLM to determine if user feedback indicates satisfaction or revision request."""
    model_client = OpenAIChatCompletionClient(**agent_generation_config(), api_key=openai_api_key)

    system_msg = SystemMessage(content="""You are an assistant that determines if a teacher's feedback contains specific modification requests for slide content.
Respond with only one word:
- "SATISFIED" if the feedback expresses satisfaction, approval, or has no specific changes (e.g., "good", "ok", "确认", "满意", "可以", "没问题", "很好")
- "REVISION" if there are specific requests to modify the slide content (e.g., "add more details", "change the structure", "请增加...", "修改...")""")

    user_msg = UserMessage(content=f"Teacher feedback: {feedback}", source="user")

    result = await model_client.create(messages=[system_msg, user_msg])
    response = result.content.strip().upper()

    return "satisfied" if "SATISFIED" in response else "revision"


async def generate_images_and_ppt(user_id: str, slide_id: str, send_status, locale: str = "zhcn") -> tuple:
    """
    Generate images for the slide, then directly call the Xunfei API to generate the PPT.

    The PPT style/color and the resolved Xunfei templateId were captured earlier by
    DesignerAgent and persisted in SlideCache/{user_id}/{slide_id}/ppt_style.json.
    We only need to load template_id and pass it to the Xunfei generation API.
    """
    locale = normalize_locale(locale)
    image_count = await generate_images_for_slide(user_id, slide_id)
    await send_status(t(locale, "assets.images_done", count=image_count))

    style_file = slide_cache_directory() / user_id / slide_id / "ppt_style.json"
    cfg = {}
    if style_file.exists():
        cfg = json.loads(style_file.read_text(encoding="utf-8"))
    style = cfg.get("style", "简约")
    color = cfg.get("color", "蓝色")
    template_id = cfg.get("template_id")
    template_title = cfg.get("template_title")

    loop = asyncio.get_event_loop()
    ppt_gen = PPTGenerator()

    # Fallback: if DesignerAgent could not resolve a templateId earlier, retry now.
    if not template_id:
        await send_status(t(locale, "assets.search_template", style=style, color=color))
        template_id, template_title = await loop.run_in_executor(
            None,
            lambda: ppt_gen.pick_template(style=style, color=color),
        )

    await send_status(t(locale, "assets.template_selected", template=template_title or template_id))

    ppt_path = await loop.run_in_executor(
        None,
        lambda: generate_ppt_for_slide(user_id, slide_id, template_id),
    )

    if ppt_path:
        await send_status(t(locale, "assets.ppt_done"))
    else:
        await send_status(t(locale, "assets.ppt_failed"))

    return image_count, ppt_path


@app.websocket("/ws/{user_id}")
async def websocket_endpoint(websocket: WebSocket, user_id: str):
    """
    WebSocket endpoint for real-time communication with the agent system.

    Flow:
    1. Accept connection
    2. Wait for user requirement input
    3. Start agent pipeline with message callback
    4. Stream agent messages to frontend
    5. Wait for user feedback (instead of approval panel)
    6. LLM determines intent: satisfied -> generate images, revision -> revise slides
    """
    locale = normalize_locale(websocket.query_params.get("locale"))
    print(f"[WS] New connection attempt from user: {user_id}")
    try:
        await manager.connect(user_id, websocket)
        print(f"[WS] User {user_id} connected successfully")
        await manager.send_status(user_id, t(locale, "api.connected", user_id=user_id))
        print(f"[WS] Sent initial status to {user_id}")
    except Exception as e:
        import traceback
        print(f"[WS INIT ERROR] {str(e)}")
        traceback.print_exc()
        return

    current_slide_id = None

    main_queue: asyncio.Queue = asyncio.Queue()

    async def receive_loop():
        nonlocal locale
        try:
            while True:
                data = await websocket.receive_json()
                try:
                    msg = UserInputMessage(**data)
                except Exception as e:
                    print(f"[WS] Invalid message from {user_id}: {e}")
                    continue

                if msg.locale:
                    locale = normalize_locale(msg.locale)

                if msg.type == "ping":
                    print(f"[WS] Ping from {user_id}")
                    continue

                if manager.is_awaiting_input(user_id):
                    await manager.receive_input(user_id, msg.content)
                else:
                    await main_queue.put(msg)
        except WebSocketDisconnect:
            await main_queue.put(None)
        except Exception as e:
            print(f"[WS receive_loop] {user_id}: {e}")
            await main_queue.put(None)

    receiver_task = asyncio.create_task(receive_loop())

    try:
        while True:
            message = await main_queue.get()
            if message is None:
                break

            if message.type == "user_input":
                requirement = message.content
                await manager.send_status(user_id, t(locale, "api.requirement_received"))

                async def on_message(agent_name: str, agent_type: str, content: str):
                    await manager.send_agent_message(user_id, agent_name, agent_type, content)

                async def on_need_input(prompt: str) -> str:
                    await manager.send_need_input(user_id, prompt)
                    return await manager.wait_for_input(user_id) or ""

                try:
                    slide_id = await run_agent_pipeline(
                        user_id=user_id,
                        requirement=requirement,
                        on_message=on_message,
                        on_need_input=on_need_input,
                        locale=locale
                    )

                    current_slide_id = slide_id

                    await manager.send_slide_complete(user_id, slide_id)

                except Exception as e:
                    await manager.send_error(user_id, t(locale, "api.generation_error", error=str(e)))

            elif message.type == "approval":
                slide_id = message.content
                await manager.send_status(user_id, t(locale, "api.matching_images"))

                async def send_status_approval(msg):
                    await manager.send_status(user_id, msg)

                try:
                    await generate_images_and_ppt(user_id, slide_id, send_status_approval, locale=locale)
                except Exception as e:
                    await manager.send_error(user_id, t(locale, "api.assets_error", error=str(e)))

            elif message.type == "feedback":
                feedback = message.content
                slide_id = current_slide_id

                if not slide_id:
                    await manager.send_error(user_id, t(locale, "api.no_current_slide"))
                    continue

                if not feedback.strip():
                    await manager.send_status(user_id, t(locale, "api.feedback_accepted"))

                    async def send_status_empty(msg):
                        await manager.send_status(user_id, msg)

                    try:
                        await generate_images_and_ppt(user_id, slide_id, send_status_empty, locale=locale)
                    except Exception as e:
                        await manager.send_error(user_id, t(locale, "api.assets_error", error=str(e)))
                else:
                    intent = await determine_feedback_intent(feedback)

                    if intent == "satisfied":
                        await manager.send_status(user_id, t(locale, "api.feedback_accepted"))

                        async def send_status_satisfied(msg):
                            await manager.send_status(user_id, msg)

                        try:
                            await generate_images_and_ppt(user_id, slide_id, send_status_satisfied, locale=locale)
                        except Exception as e:
                            await manager.send_error(user_id, t(locale, "api.assets_error", error=str(e)))
                    else:
                        await manager.send_status(user_id, t(locale, "api.revision_requested"))

                        async def on_revision_message(agent_name: str, agent_type: str, content: str):
                            await manager.send_agent_message(user_id, agent_name, agent_type, content)

                        try:
                            await run_revision_pipeline(
                                user_id=user_id,
                                slide_id=slide_id,
                                feedback=feedback,
                                on_message=on_revision_message,
                                locale=locale
                            )
                            await manager.send_slide_complete(user_id, slide_id)
                        except Exception as e:
                            await manager.send_error(user_id, t(locale, "api.revision_error", error=str(e)))

    except Exception as e:
        import traceback
        print(f"[WS ERROR] User {user_id}: {str(e)}")
        traceback.print_exc()
        try:
            await manager.send_error(user_id, t(locale, "api.websocket_error", error=str(e)))
        except Exception:
            pass
    finally:
        receiver_task.cancel()
        try:
            await receiver_task
        except (asyncio.CancelledError, Exception):
            pass
        print(f"[WS] User {user_id} disconnected")
        manager.disconnect(user_id)


@app.get("/slides/{user_id}/{slide_id}", response_model=SlideInfo)
async def get_slide(user_id: str, slide_id: str):
    """Get the generated slide content."""
    slide_file = slide_cache_directory() / user_id / slide_id / "slide_draft.md"

    if not slide_file.exists():
        raise HTTPException(status_code=404, detail="Slide not found")

    content = slide_file.read_text(encoding="utf-8")
    return SlideInfo(user_id=user_id, slide_id=slide_id, content=content)


@app.get("/slides/{user_id}/{slide_id}/images", response_model=SlideImagesResponse)
async def get_slide_images(user_id: str, slide_id: str):
    """Get the list of images for a slide."""
    infos_file = slide_cache_directory() / user_id / slide_id / "slide_image_infos.json"

    if not infos_file.exists():
        requests_file = slide_cache_directory() / user_id / slide_id / "slide_image_requests.json"
        if not requests_file.exists():
            raise HTTPException(status_code=404, detail="Image info not found")
        infos_file = requests_file

    raw_list = json.loads(infos_file.read_text(encoding="utf-8"))
    images = [ImageInfo(**item) for item in raw_list if isinstance(item, dict)]

    return SlideImagesResponse(user_id=user_id, slide_id=slide_id, images=images)


@app.get("/slides/{user_id}/{slide_id}/images/{image_id}")
async def get_slide_image(user_id: str, slide_id: str, image_id: int):
    """Get a specific image file."""
    image_file = slide_cache_directory() / user_id / slide_id / "image" / f"{image_id}.jpg"

    if not image_file.exists():
        raise HTTPException(status_code=404, detail="Image not found")

    return FileResponse(image_file, media_type="image/jpeg")


@app.get("/slides/{user_id}/{slide_id}/download/markdown")
async def download_slide_markdown(user_id: str, slide_id: str):
    """Download the slide markdown file."""
    slide_file = slide_cache_directory() / user_id / slide_id / "slide_draft.md"

    if not slide_file.exists():
        raise HTTPException(status_code=404, detail="Slide not found")

    return FileResponse(
        slide_file,
        media_type="text/markdown",
        filename=f"slide_{slide_id}.md"
    )


@app.get("/slides/{user_id}/{slide_id}/download/image/{image_id}")
async def download_slide_image(user_id: str, slide_id: str, image_id: int):
    """Download a specific image file."""
    image_file = slide_cache_directory() / user_id / slide_id / "image" / f"{image_id}.jpg"

    if not image_file.exists():
        raise HTTPException(status_code=404, detail="Image not found")

    return FileResponse(
        image_file,
        media_type="image/jpeg",
        filename=f"slide_{slide_id}_image_{image_id}.jpg"
    )


@app.get("/slides/{user_id}/{slide_id}/download/ppt")
async def download_slide_ppt(user_id: str, slide_id: str):
    """Download the generated PPT file."""
    ppt_file = slide_cache_directory() / user_id / slide_id / "slide.pptx"

    if not ppt_file.exists():
        raise HTTPException(status_code=404, detail="PPT not found. Please generate PPT first.")

    return FileResponse(
        ppt_file,
        media_type="application/vnd.openxmlformats-officedocument.presentationml.presentation",
        filename=f"slide_{slide_id}.pptx"
    )


@app.get("/slides/{user_id}/{slide_id}/ppt/exists")
async def check_ppt_exists(user_id: str, slide_id: str):
    """Check if PPT file exists for a slide."""
    ppt_file = slide_cache_directory() / user_id / slide_id / "slide.pptx"
    return {"exists": ppt_file.exists()}


@app.get("/users/{user_id}/slides")
async def list_user_slides(user_id: str):
    """List all slides for a user."""
    user_dir = slide_cache_directory() / user_id

    if not user_dir.exists():
        return {"user_id": user_id, "slides": []}

    slides = []
    for slide_dir in sorted(user_dir.iterdir()):
        if slide_dir.is_dir() and slide_dir.name.isdigit():
            slide_file = slide_dir / "slide_draft.md"
            slides.append({
                "slide_id": slide_dir.name,
                "has_content": slide_file.exists()
            })

    return {"user_id": user_id, "slides": slides}


@app.get("/health")
async def health_check():
    return {"status": "healthy"}
