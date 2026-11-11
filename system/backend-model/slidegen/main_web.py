"""
Web-compatible version of the multi-agent slide generation system.
Refactored to support message callbacks for WebSocket integration.
"""
from typing import Dict, Callable, Awaitable, Optional
from pydantic import BaseModel
import json
import re
import asyncio
from pathlib import Path
from .config import slide_cache_directory
from model_config import agent_generation_config

from autogen_core import (
    MessageContext,
    RoutedAgent,
    SingleThreadedAgentRuntime,
    TopicId,
    AgentId,
    TypeSubscription,
    message_handler
)

from autogen_core.models import (
    ChatCompletionClient,
    SystemMessage,
    UserMessage
)

from autogen_ext.models.openai import OpenAIChatCompletionClient
from .constants import (
    prompt_designer,
    prompt_designer_revise,
    prompt_reviewer,
    prompt_planner,
    prompt_writer,
    prompt_planner_beautiy,
    prompt_writer_revise0,
    prompt_writer_revise1,
)

from .utils import generate_slide_image_requests, parse_slide_image_requests, download_all_slide_images
from .ppt_generator import parse_ppt_style_input, PPTGenerator
from .slidegen_i18n import language_instruction, normalize_locale, t

import os

openai_api_key = os.getenv("OPENAI_API_KEY")


MessageCallback = Callable[[str, str, str], Awaitable[None]]
InputCallback = Callable[[str], Awaitable[str]]


class TeacherLogin(BaseModel):
    requirement: str


class WritingTask(BaseModel):
    task: str


class OutlineResult(BaseModel):
    task: str
    outline: Dict
    ppt_style: str = "简约"
    ppt_color: str = "蓝色"
    template_id: Optional[str] = None
    template_title: Optional[str] = None


class ReviewFeedback(BaseModel):
    feedback: str


class WritingAssignment(BaseModel):
    writer_id: str
    section: Dict


class WritingContent(BaseModel):
    writer_id: str
    content: Dict


class SlideRevise(BaseModel):
    feedback: str


class SlideOutput(BaseModel):
    slide_id: str


proxy_topic_type = "ProxyAgent"
designer_topic_type = "DesignerAgent"
reviewer_topic_type = "ReviewerAgent"
planner_topic_type = "PlannerAgent"
writer_topic_type = "WriterAgent"


def parse_json_from_string(input_string: str) -> Dict:
    """Parse JSON from a string, handling markdown code blocks."""
    try:
        if "json" in input_string:
            json_match = re.search(r"```json\n([\s\S]*?)```", input_string, re.DOTALL)
            if not json_match:
                raise ValueError("No JSON found in the input string.")
            json_content = json_match.group(1)
        else:
            json_content = input_string
        parsed_dict = json.loads(json_content)
        return parsed_dict
    except (json.JSONDecodeError, ValueError):
        raise ValueError(f"Failed to parse JSON from string: {input_string[:200]}...")


def format_outline_result(outline: Dict) -> str:
    """Format outline dictionary to readable string."""
    formatted_sections = []
    for section in outline.get("section", []):
        formatted_section = f"# {section['title']}\n"
        if "subsection" in section and len(section["subsection"]):
            for subsection in section["subsection"]:
                formatted_section += f"## {subsection['title']}\n"
                if "sub-subsection" in subsection and len(subsection["sub-subsection"]):
                    for sub_subsection in subsection["sub-subsection"]:
                        formatted_section += f"### {sub_subsection['title']}\n"
        formatted_sections.append(formatted_section)
    return "\n".join(formatted_sections)


class ProxyAgent(RoutedAgent):
    def __init__(
        self,
        model_client: ChatCompletionClient,
        on_message: MessageCallback,
        on_need_input: InputCallback,
        user_requirement: str,
        locale: str = "zhcn"
    ) -> None:
        super().__init__("A proxy agent that represents the teacher, initiates the task and gives feedback to AI generated content.")
        self._model_client = model_client
        self._on_message = on_message
        self._on_need_input = on_need_input
        self._user_requirement = user_requirement
        self._locale = normalize_locale(locale)

    @message_handler
    async def handle_user_task(self, message: TeacherLogin, ctx: MessageContext) -> None:
        await self._on_message("ProxyAgent", "proxy", t(self._locale, "proxy.received_requirement", requirement=self._user_requirement))
        await self.publish_message(
            WritingTask(task=self._user_requirement),
            topic_id=TopicId(designer_topic_type, source=self.id.key),
        )

    @message_handler
    async def handle_slide_output(self, message: SlideOutput, ctx: MessageContext) -> None:
        await self._on_message(
            "ProxyAgent",
            "proxy",
            t(self._locale, "proxy.slide_complete", slide_id=message.slide_id)
        )


class DesignerAgent(RoutedAgent):
    def __init__(
        self,
        model_client: ChatCompletionClient,
        on_message: MessageCallback,
        on_need_input: InputCallback,
        locale: str = "zhcn"
    ):
        super().__init__("A designer agent that generates a lecture outline based on the teacher's requirements.")
        self._model_client = model_client
        self._system_message = SystemMessage(content=prompt_designer)
        self._on_message = on_message
        self._on_need_input = on_need_input
        self._locale = normalize_locale(locale)
        self._outline = None
        self._task = None
        self._ppt_style = "简约"
        self._ppt_color = "蓝色"
        self._template_id: Optional[str] = None
        self._template_title: Optional[str] = None

    @message_handler
    async def handle_outline_writing_revising(self, message: WritingTask | ReviewFeedback, ctx: MessageContext) -> None:
        if isinstance(message, WritingTask):
            self._task = message.task

            user_reply = await self._on_need_input(t(self._locale, "designer.ask_style")) or ""
            self._ppt_style, self._ppt_color = parse_ppt_style_input(user_reply)

            await self._on_message(
                "DesignerAgent",
                "designer",
                t(self._locale, "designer.preference_recorded", style=self._ppt_style, color=self._ppt_color)
            )
            loop = asyncio.get_event_loop()
            ppt_gen = PPTGenerator()
            self._template_id, self._template_title = await loop.run_in_executor(
                None,
                lambda: ppt_gen.pick_template(style=self._ppt_style, color=self._ppt_color),
            )
            await self._on_message(
                "DesignerAgent",
                "designer",
                t(self._locale, "designer.template_matched", template=self._template_title)
            )

            prompt = f"Slides generation requirements from teacher:\n {message.task}\n\n{language_instruction(self._locale)}\n\n"
        else:
            await self._on_message("DesignerAgent", "designer", t(self._locale, "designer.revising_outline"))
            prompt = prompt_designer_revise.format(
                task=self._task,
                feedback=message.feedback
            ) + f"\n\n{language_instruction(self._locale)}"

        llm_result = await self._model_client.create(
            messages=[self._system_message, UserMessage(content=prompt, source=self.id.key)],
            cancellation_token=ctx.cancellation_token,
        )
        response = llm_result.content
        assert isinstance(response, str)
        self._outline = parse_json_from_string(response)
        formatted_outline = format_outline_result(self._outline)
        await self._on_message("DesignerAgent", "designer", f"[OUTLINE_PREVIEW]{formatted_outline}[/OUTLINE_PREVIEW]")
        outline = OutlineResult(
            task=self._task,
            outline=self._outline,
            ppt_style=self._ppt_style,
            ppt_color=self._ppt_color,
            template_id=self._template_id,
            template_title=self._template_title,
        )
        await self.publish_message(outline, topic_id=TopicId(reviewer_topic_type, source=self.id.key))


class ReviewerAgent(RoutedAgent):
    def __init__(self, model_client: ChatCompletionClient, on_message: MessageCallback, locale: str = "zhcn"):
        super().__init__("A reviewer agent that evaluates the lecture outline generated by the designer agent.")
        self._model_client = model_client
        self._system_message = SystemMessage(content=prompt_reviewer)
        self._on_message = on_message
        self._locale = normalize_locale(locale)
        self._revise_times = 0

    @message_handler
    async def handle_outline_result(self, message: OutlineResult, ctx: MessageContext) -> None:
        await self._on_message("ReviewerAgent", "reviewer", t(self._locale, "reviewer.reviewing"))

        prompt = f"Outline from designer:\n {json.dumps(message.outline, indent=2)}"
        prompt += f"\n\nRequirements from teacher:\n {message.task}"
        llm_result = await self._model_client.create(
            messages=[self._system_message, UserMessage(content=prompt, source=self.id.key)],
            cancellation_token=ctx.cancellation_token,
        )
        response = llm_result.content
        assert isinstance(response, str)
        fdbk = parse_json_from_string(response)
        output = 'APPROVE' if fdbk.get('approve', False) else 'REVISE\n' + fdbk.get('feedback', '')
        await self._on_message("ReviewerAgent", "reviewer", t(self._locale, "reviewer.result", result=output))

        self._revise_times += 1
        # Accelerate generation - approve after first review
        if fdbk.get("approve", False) or self._revise_times > 0:
            await self._on_message("ReviewerAgent", "reviewer", t(self._locale, "reviewer.approved"))
            await self.publish_message(message, topic_id=TopicId(planner_topic_type, source=self.id.key))
        else:
            feedback = ReviewFeedback(feedback=fdbk.get('feedback', ''))
            await self.publish_message(feedback, topic_id=TopicId(designer_topic_type, source=self.id.key))


class PlannerAgent(RoutedAgent):
    def __init__(
        self,
        model_client: ChatCompletionClient,
        on_message: MessageCallback,
        user_id: str,
        slide_id: str,
        locale: str = "zhcn"
    ):
        super().__init__("A planner agent that divides the outline into 3 balanced parts.")
        self._model_client = model_client
        self._system_message = SystemMessage(content=prompt_planner)
        self._on_message = on_message
        self._locale = normalize_locale(locale)
        self._writer_instances = [AgentId(writer_topic_type, f"writer_{i}") for i in range(1, 4)]
        self._slide: Dict = {"section": []}
        self._user_id = str(user_id)
        self._slide_id = str(slide_id)
        base_dir = slide_cache_directory()
        self._slide_dir = base_dir / self._user_id / self._slide_id
        self._slide_dir.mkdir(parents=True, exist_ok=True)
        self._slide_file = self._slide_dir / "slide_draft.md"

    @message_handler
    async def handle_outline_result(self, message: OutlineResult, ctx: MessageContext) -> None:
        await self._on_message("PlannerAgent", "planner", t(self._locale, "planner.assigning"))

        style_path = self._slide_dir / "ppt_style.json"
        style_path.write_text(
            json.dumps(
                {
                    "style": message.ppt_style,
                    "color": message.ppt_color,
                    "template_id": message.template_id,
                    "template_title": message.template_title,
                },
                ensure_ascii=False,
                indent=2,
            ),
            encoding="utf-8",
        )

        prompt = f"Complete outline:\n {format_outline_result(message.outline)}"
        llm_result = await self._model_client.create(
            messages=[self._system_message, UserMessage(content=prompt, source=self.id.key)],
            cancellation_token=ctx.cancellation_token,
        )
        response = llm_result.content
        assert isinstance(response, str)
        assignments = parse_json_from_string(response)
        assert len(assignments["parts"]) == 3, "The planner should divide the outline into 3 parts."

        sec_amount = [len(part["section"]) for part in assignments["parts"]]
        assignment_msg = "\n".join([t(self._locale, "planner.assignment_line", index=i + 1, count=sec_amount[i]) for i in range(3)])
        await self._on_message("PlannerAgent", "planner", t(self._locale, "planner.assignment_done", assignment=assignment_msg))

        assignments_list = [
            WritingAssignment(writer_id=part["writer_id"], section={"section": part.get("section")})
            for part in assignments["parts"]
        ]

        writer_1_res, writer_2_res, writer_3_res = await asyncio.gather(
            self.send_message(assignments_list[0], self._writer_instances[0]),
            self.send_message(assignments_list[1], self._writer_instances[1]),
            self.send_message(assignments_list[2], self._writer_instances[2])
        )

        all_sections = []
        for writer_res in [writer_1_res, writer_2_res, writer_3_res]:
            if writer_res and hasattr(writer_res, 'content'):
                all_sections.extend(writer_res.content.get("section", []))

        self._slide = {"section": all_sections}

        out_path = self._slide_dir / "slide_json.json"
        out_path.write_text(
            json.dumps(self._slide, ensure_ascii=False, indent=2),
            encoding="utf-8",
        )

        await self._on_message("PlannerAgent", "planner", t(self._locale, "planner.beautifying"))

        beautify_system_msg = SystemMessage(
            content=f"You are an expert at converting structured content into beautiful, well-formatted Markdown documents for academic presentations. {language_instruction(self._locale)}"
        )
        beautify_result = await self._model_client.create(
            messages=[
                beautify_system_msg,
                UserMessage(content=prompt_planner_beautiy.format(json_file=json.dumps(self._slide, indent=2)), source=self.id.key)
            ],
            cancellation_token=ctx.cancellation_token,
        )
        beautified_content = beautify_result.content
        assert isinstance(beautified_content, str)

        markdown_match = re.search(r"```markdown\n([\s\S]*?)```", beautified_content, re.DOTALL)
        if markdown_match:
            markdown_content = markdown_match.group(1)
        else:
            markdown_content = beautified_content

        with open(self._slide_file, "w", encoding="utf-8") as f:
            f.write(markdown_content)

        await self.publish_message(
            SlideOutput(slide_id=self._slide_id),
            topic_id=TopicId(proxy_topic_type, source=self.id.key)
        )

    @message_handler
    async def handle_slide_edit(self, message: SlideRevise, ctx: MessageContext) -> None:
        await self._on_message("PlannerAgent", "planner", t(self._locale, "planner.revision_received"))

        try:
            with open(self._slide_file, "r", encoding="utf-8") as f:
                current_slide_content = f.read()
        except FileNotFoundError:
            await self._on_message("PlannerAgent", "planner", t(self._locale, "planner.slide_missing"))
            return

        revise_system_msg = SystemMessage(
            content=f"You are an expert at revising slide content based on teacher feedback. Analyze the current slide content and the teacher's feedback, then make appropriate modifications while maintaining the overall structure and quality. {language_instruction(self._locale)}"
        )
        revise_prompt = prompt_writer_revise0.format(
            slide_content=current_slide_content,
            feedback=message.feedback
        ) + prompt_writer_revise1

        revise_result = await self._model_client.create(
            messages=[revise_system_msg, UserMessage(content=revise_prompt, source=self.id.key)],
            cancellation_token=ctx.cancellation_token,
        )
        revise_content = revise_result.content
        assert isinstance(revise_content, str)

        revise_content = parse_json_from_string(revise_content)
        new_markdown_content = revise_content.get("revised_markdown_slide", "")

        with open(self._slide_file, "w", encoding="utf-8") as f:
            f.write(new_markdown_content)

        summary = revise_content.get('revise_summary', '')
        await self._on_message("PlannerAgent", "planner", t(self._locale, "planner.revision_complete", summary=summary))

        await self.publish_message(
            SlideOutput(slide_id=self._slide_id),
            topic_id=TopicId(proxy_topic_type, source=self.id.key)
        )


class WriterAgent(RoutedAgent):
    def __init__(self, model_client: ChatCompletionClient, on_message: MessageCallback, locale: str = "zhcn"):
        super().__init__("A writer agent that creates content for assigned sections.")
        self._model_client = model_client
        self._system_message = SystemMessage(content=prompt_writer)
        self._on_message = on_message
        self._locale = normalize_locale(locale)
        self._writer_id = None

    @message_handler
    async def handle_writing_assignment(self, message: WritingAssignment, ctx: MessageContext) -> WritingContent:
        self._writer_id = message.writer_id
        await self._on_message(f"WriterAgent ({self._writer_id})", "writer", t(self._locale, "writer.started"))

        prompt = f"Assigned sections:\n {json.dumps(message.section, indent=2)} \nPlease generate the content for these sections in above json format. Ensure that the content is clear, concise, and relevant to the assigned sections. Keep the section numbers. {language_instruction(self._locale)}"
        llm_result = await self._model_client.create(
            messages=[self._system_message, UserMessage(content=prompt, source=self.id.key)],
            cancellation_token=ctx.cancellation_token,
        )
        response = llm_result.content
        assert isinstance(response, str)
        content = parse_json_from_string(response)

        section_count = len(content.get("section", []))
        await self._on_message(
            f"WriterAgent ({self._writer_id})",
            "writer",
            t(self._locale, "writer.finished", count=section_count)
        )
        return WritingContent(writer_id=self._writer_id, content=content)


def _prepare_slide_cache(user_id: str) -> tuple[str, Path]:
    """
    Allocate a new slide_id for this generation based on the user's cache directory.
    """
    base_dir = slide_cache_directory()
    user_dir = base_dir / str(user_id)
    user_dir.mkdir(parents=True, exist_ok=True)

    existing_ids = []
    for p in user_dir.iterdir():
        if p.is_dir() and p.name.isdigit():
            try:
                existing_ids.append(int(p.name))
            except ValueError:
                continue

    next_id = (max(existing_ids) + 1) if existing_ids else 1
    slide_id = str(next_id)
    slide_dir = user_dir / slide_id
    slide_dir.mkdir(parents=True, exist_ok=True)
    return slide_id, slide_dir


async def run_agent_pipeline(
    user_id: str,
    requirement: str,
    on_message: MessageCallback,
    on_need_input: InputCallback,
    slide_id: Optional[str] = None,
    locale: str = "zhcn"
) -> str:
    """
    Run the agent pipeline with message callbacks.

    Args:
        user_id: User identifier
        requirement: The PPT generation requirement from the user
        on_message: Callback function for agent messages
        on_need_input: Callback function when user input is needed
        slide_id: Optional slide ID (auto-generated if not provided)

    Returns:
        The slide_id of the generated content
    """
    locale = normalize_locale(locale)
    model_client = OpenAIChatCompletionClient(
        **agent_generation_config(),
        api_key=openai_api_key,
    )

    runtime = SingleThreadedAgentRuntime()

    if slide_id is None:
        slide_id, _ = _prepare_slide_cache(user_id)
    else:
        base_dir = slide_cache_directory()
        slide_dir = base_dir / str(user_id) / str(slide_id)
        slide_dir.mkdir(parents=True, exist_ok=True)
        slide_id = str(slide_id)

    await ProxyAgent.register(
        runtime,
        type=proxy_topic_type,
        factory=lambda: ProxyAgent(
            model_client=model_client,
            on_message=on_message,
            on_need_input=on_need_input,
            user_requirement=requirement,
            locale=locale
        )
    )
    await runtime.add_subscription(TypeSubscription(topic_type=proxy_topic_type, agent_type=proxy_topic_type))

    await DesignerAgent.register(
        runtime,
        type=designer_topic_type,
        factory=lambda: DesignerAgent(
            model_client=model_client,
            on_message=on_message,
            on_need_input=on_need_input,
            locale=locale
        )
    )
    await runtime.add_subscription(TypeSubscription(topic_type=designer_topic_type, agent_type=designer_topic_type))

    await ReviewerAgent.register(
        runtime,
        type=reviewer_topic_type,
        factory=lambda: ReviewerAgent(model_client=model_client, on_message=on_message, locale=locale)
    )
    await runtime.add_subscription(TypeSubscription(topic_type=reviewer_topic_type, agent_type=reviewer_topic_type))

    await PlannerAgent.register(
        runtime,
        type=planner_topic_type,
        factory=lambda: PlannerAgent(
            model_client=model_client,
            on_message=on_message,
            user_id=str(user_id),
            slide_id=str(slide_id),
            locale=locale
        )
    )
    await runtime.add_subscription(TypeSubscription(topic_type=planner_topic_type, agent_type=planner_topic_type))

    await WriterAgent.register(
        runtime,
        type=writer_topic_type,
        factory=lambda: WriterAgent(model_client=model_client, on_message=on_message, locale=locale)
    )
    await runtime.add_subscription(TypeSubscription(topic_type=writer_topic_type, agent_type=writer_topic_type))

    runtime.start()
    await runtime.publish_message(
        TeacherLogin(requirement=requirement),
        topic_id=TopicId(proxy_topic_type, source="default"),
    )
    await runtime.stop_when_idle()

    return slide_id


async def run_revision_pipeline(
    user_id: str,
    slide_id: str,
    feedback: str,
    on_message: MessageCallback,
    locale: str = "zhcn"
) -> str:
    """
    Run revision pipeline to update slide based on user feedback.

    Args:
        user_id: User identifier
        slide_id: Slide identifier
        feedback: User's revision feedback
        on_message: Callback function for agent messages

    Returns:
        The slide_id of the revised content
    """
    locale = normalize_locale(locale)
    model_client = OpenAIChatCompletionClient(
        **agent_generation_config(),
        api_key=openai_api_key,
    )

    runtime = SingleThreadedAgentRuntime()

    await PlannerAgent.register(
        runtime,
        type=planner_topic_type,
        factory=lambda: PlannerAgent(
            model_client=model_client,
            on_message=on_message,
            user_id=str(user_id),
            slide_id=str(slide_id),
            locale=locale
        )
    )
    await runtime.add_subscription(TypeSubscription(topic_type=planner_topic_type, agent_type=planner_topic_type))

    async def dummy_input(prompt: str) -> str:
        return ""

    await ProxyAgent.register(
        runtime,
        type=proxy_topic_type,
        factory=lambda: ProxyAgent(
            model_client=model_client,
            on_message=on_message,
            on_need_input=dummy_input,
            user_requirement="",
            locale=locale
        )
    )
    await runtime.add_subscription(TypeSubscription(topic_type=proxy_topic_type, agent_type=proxy_topic_type))

    runtime.start()
    await runtime.publish_message(
        SlideRevise(feedback=feedback),
        topic_id=TopicId(planner_topic_type, source="default"),
    )
    await runtime.stop_when_idle()

    return slide_id


async def generate_images_for_slide(user_id: str, slide_id: str) -> int:
    """
    Generate images for a completed slide.

    Args:
        user_id: User identifier
        slide_id: Slide identifier

    Returns:
        Number of images generated
    """
    # Run synchronous work in an executor to avoid blocking the event loop.
    loop = asyncio.get_event_loop()

    await loop.run_in_executor(
        None,
        lambda: generate_slide_image_requests(user_id=user_id, slide_id=slide_id)
    )

    await loop.run_in_executor(
        None,
        lambda: parse_slide_image_requests(user_id=user_id, slide_id=slide_id)
    )

    count = await loop.run_in_executor(
        None,
        lambda: download_all_slide_images(user_id=user_id, slide_id=slide_id)
    )

    return count


if __name__ == "__main__":
    async def test_callback(agent_name: str, agent_type: str, content: str):
        print(f"[{agent_type}] {agent_name}: {content}")

    async def test_input_callback(prompt: str) -> str:
        return input(prompt)

    async def test_main():
        slide_id = await run_agent_pipeline(
            user_id="test_user",
            requirement="Create a lecture about corneal diseases",
            on_message=test_callback,
            on_need_input=test_input_callback
        )
        print(f"Generated slide_id: {slide_id}")

    asyncio.run(test_main())
