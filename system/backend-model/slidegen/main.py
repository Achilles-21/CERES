from typing import Dict
from pydantic import BaseModel
import json
import re
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

from autogen_core.models import ChatCompletionClient, SystemMessage, UserMessage
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
    prompt_request
)
from pathlib import Path
from .config import slide_cache_directory
from model_config import agent_generation_config

from .utils import generate_slide_image_requests
from .ppt_generator import PPTGenerator, generate_ppt_for_slide

class TeacherLogin(BaseModel):
    pass


class WritingTask(BaseModel):
    task: str


class OutlineResult(BaseModel):
    task: str
    outline: Dict


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
    pass


proxy_topic_type = "ProxyAgent"
designer_topic_type = "DesignerAgent"
reviewer_topic_type = "ReviewerAgent"
planner_topic_type = "PlannerAgent"
writer_topic_type = "WriterAgent"


def fix_json_with_gpt(input_string: str) -> str:
    system_msg = SystemMessage(content="You are a JSON fixer. Convert the input into valid JSON format while preserving the content. Return only the fixed JSON inside ```json``` blocks.")
    import pdb; pdb.set_trace()
    user_msg = UserMessage(content=f"Fix this JSON:\n{input_string}")
    result = model_client.create(messages=[system_msg, user_msg])
    return result.content

def parse_json_from_string(input_string: str) -> Dict:
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

    except (json.JSONDecodeError, ValueError) as e:
        fixed_json = fix_json_with_gpt(input_string)
        return parse_json_from_string(fixed_json)
    except Exception as e:
        raise ValueError(f"An error occurred: {e}")


def format_outline_result(outline: Dict) -> str:
    formatted_sections = []
    for section in outline["section"]:
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
    def __init__(self, model_client: ChatCompletionClient) -> None:
        super().__init__("A proxy agent that represents the teacher, initiates the task and gives feedback to AI generated content.")
        self._model_client = model_client

    @message_handler
    async def handle_user_task(self, message: TeacherLogin, ctx: MessageContext) -> None:
        human_input = prompt_request
        print(f"{'-'*80}\n{self.id.type}:\n{human_input}", flush=True)
        await self.publish_message(
            WritingTask(task=human_input),
            topic_id=TopicId(designer_topic_type, source=self.id.key),
        )

    @message_handler
    async def handle_slide_output(self, message: SlideOutput, ctx: MessageContext) -> None:
        user_feedback = input("Please provide your feedback on the slides (or press Enter if satisfied): ")
        if not user_feedback.strip():
            print(f"{'-'*80}\n{self.id.type}:\nNo feedback provided. Successfully prepare slide!")
            return
        print(f"{'-'*80}\n{self.id.type}:\nUser feedback: {user_feedback}")
        intent_system_msg = SystemMessage(content="You are an assistant that determines if a teacher's feedback contains specific modification requests for slide content. Respond with 'YES' if there are specific modification requests, or 'NO' if it's just general comments or satisfaction.")
        intent_prompt = f"Teacher feedback: {user_feedback}\n\nDoes this feedback contain specific requests to modify the slide content?"
        intent_result = await self._model_client.create(
            messages=[intent_system_msg, UserMessage(content=intent_prompt, source=self.id.key)],
            cancellation_token=ctx.cancellation_token,
        )
        intent_response = intent_result.content.strip().upper()
        if "YES" in intent_response:
            print(f"Sending feedback to planner.")
            await self.publish_message(
                SlideRevise(feedback=user_feedback),
                topic_id=TopicId(planner_topic_type, source=self.id.key),
            )
        else:
            print(f"{'-'*80}\n{self.id.type}: Successfully prepared slide!")



class DesignerAgent(RoutedAgent):
    def __init__(self, model_client: ChatCompletionClient):
        super().__init__("A designer agent that generates a lecture outline based on the teacher's requirements.")
        self._model_client = model_client
        self._system_message = SystemMessage(content=prompt_designer)
        self._outline = None
        self._task = None

    @message_handler
    async def handle_outline_writing_revising(self, message: WritingTask | ReviewFeedback, ctx: MessageContext) -> None:
        if isinstance(message, WritingTask):
            self._task = message.task
            prompt = f"Slides generation requirements from teacher:\n {message.task}\n\n"
        else:
            print(f"{'-'*80}\n{self.id.type} received feedback from ", reviewer_topic_type, "\n")
            prompt = prompt_designer_revise.format(
                task=self._task,
                feedback=message.feedback
            )
        llm_result = await self._model_client.create(
            messages=[self._system_message, UserMessage(content=prompt, source=self.id.key)],
            cancellation_token=ctx.cancellation_token,
        )
        response = llm_result.content
        assert isinstance(response, str)
        self._outline = parse_json_from_string(response)
        print(f"{'-'*80}\n{self.id.type}:\n{format_outline_result(self._outline)}")
        outline = OutlineResult(task=self._task, outline=self._outline)
        await self.publish_message(outline, topic_id=TopicId(reviewer_topic_type, source=self.id.key))


class ReviewerAgent(RoutedAgent):
    def __init__(self, model_client: ChatCompletionClient):
        super().__init__("A reviewer agent that evaluates the lecture outline generated by the designer agent.")
        self._model_client = model_client
        self._system_message = SystemMessage(content=prompt_reviewer)
        self._revise_times = 0

    @message_handler
    async def handle_outline_result(self, message: OutlineResult, ctx: MessageContext) -> None:
        prompt = f"Outline from designer:\n {json.dumps(message.outline, indent=2)}"
        prompt += f"\n\nRequirements from teacher:\n {message.task}"
        llm_result = await self._model_client.create(
            messages=[self._system_message, UserMessage(content=prompt, source=self.id.key)],
            cancellation_token=ctx.cancellation_token,
        )
        response = llm_result.content
        assert isinstance(response, str)
        fdbk = parse_json_from_string(response)
        output = 'APPROVE' if fdbk.get('approve', False) else 'REVISE\n'+fdbk.get('feedback', '')
        print(f"{'-'*80}\n{self.id.type}:\n{output}")
        # Stop after the first accepted review to bound the agent loop.
        self._revise_times += 1
        if fdbk.get("approve", False) or self._revise_times > 0:
            await self.publish_message(message, topic_id=TopicId(planner_topic_type, source=self.id.key))
        else:
            feedback = ReviewFeedback(feedback=fdbk.get('feedback', ''))
            await self.publish_message(feedback, topic_id=TopicId(designer_topic_type, source=self.id.key))


class PlannerAgent(RoutedAgent):
    def __init__(self, model_client: ChatCompletionClient, user_id: str, slide_id: str):
        super().__init__("A planner agent that divides the outline into 3 balanced parts.")
        self._model_client = model_client
        self._system_message = SystemMessage(content=prompt_planner)
        self._writer_instances = [AgentId(writer_topic_type, f"writer_{i}") for i in range(1, 4)]
        self._slide : Dict = {"section": []}
        self._user_id = str(user_id)
        self._slide_id = str(slide_id)
        base_dir = slide_cache_directory()
        self._slide_dir = base_dir / self._user_id / self._slide_id
        self._slide_dir.mkdir(parents=True, exist_ok=True)
        self._slide_file = self._slide_dir / "slide_draft.md"

    @message_handler
    async def handle_outline_result(self, message: OutlineResult, ctx: MessageContext) -> None:
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
        print(f"{'-'*80}\n{self.id.type}:")
        for idx in range(3):
            print("Assign "+str(sec_amount[idx])+" sections to writer_"+str(idx+1))
        assignments = [WritingAssignment(writer_id=part["writer_id"], section={"section": part.get("section")}) for part in assignments["parts"]]
        writer_1_res, writer_2_res, writer_3_res = await asyncio.gather(
            self.send_message(assignments[0], self._writer_instances[0]),
            self.send_message(assignments[1], self._writer_instances[1]),
            self.send_message(assignments[2], self._writer_instances[2])
        )
        all_sections = []
        for writer_res in [writer_1_res, writer_2_res, writer_3_res]:
            if writer_res and hasattr(writer_res, 'content'):
                all_sections.extend(writer_res.content.get("section", []))
        self._slide = {"section": all_sections}
        out_path = self._slide_dir / "slide_json.json"
        out_path.write_text(
            json.dump(self._slide, ensure_ascii=False, indent=2),
            encoding="utf-8",
        )
        beautify_system_msg = SystemMessage(content="You are an expert at converting structured content into beautiful, well-formatted Markdown documents for academic presentations.")
        beautify_result = await self._model_client.create(
            messages=[beautify_system_msg, UserMessage(content=prompt_planner_beautiy.format(json_file=json.dumps(self._slide, indent=2)), source=self.id.key)],
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
        print(f"{'-'*80}\n{self.id.type}:\n Slide content saved to '{self._slide_file}', check it.")
        await self.publish_message(SlideOutput(), topic_id=TopicId(proxy_topic_type, source=self.id.key))

    @message_handler
    async def handle_slide_edit(self, message: SlideRevise, ctx: MessageContext) -> None:
        try:
            with open(self._slide_file, "r", encoding="utf-8") as f:
                current_slide_content = f.read()
        except FileNotFoundError:
            print(f"{'-'*80}\n{self.id.type}:\n No existing slide file found.")
            return
        revise_system_msg = SystemMessage(content="You are an expert at revising slide content based on teacher feedback. Analyze the current slide content and the teacher's feedback, then make appropriate modifications while maintaining the overall structure and quality.")
        import pdb; pdb.set_trace()
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
        print(f"{'-'*80}\n{self.id.type}:\nSlides have been revised based on your feedback.{revise_content.get('revise_summary', '')}")
        await self.publish_message(SlideOutput(), topic_id=TopicId(proxy_topic_type, source=self.id.key))


class WriterAgent(RoutedAgent):
    def __init__(self, model_client: ChatCompletionClient):
        super().__init__(f"A writer agent that creates content for assigned sections.")
        self._model_client = model_client
        self._system_message = SystemMessage(content=prompt_writer)
        self._writer_id = None

    @message_handler
    async def handle_writing_assignment(self, message: WritingAssignment, ctx: MessageContext) -> WritingContent:
        self._writer_id = message.writer_id
        prompt = f"Assigned sections:\n {json.dumps(message.section, indent=2)} \nPlease generate the content for these sections in above json format . Ensure that the content is clear, concise, and relevant to the assigned sections. Keep the section numbers."
        llm_result = await self._model_client.create(
            messages=[self._system_message, UserMessage(content=prompt, source=self.id.key)],
            cancellation_token=ctx.cancellation_token,
        )
        response = llm_result.content
        assert isinstance(response, str)
        content = parse_json_from_string(response)
        print(f"{'-'*80}\n{self.id.type} {self._writer_id}:\n{json.dumps(content, indent=2)}")
        return WritingContent( writer_id=self._writer_id, content=content)


import os
openai_api_key = os.getenv("OPENAI_API_KEY")


import asyncio


def _prepare_slide_cache(user_id: str) -> tuple[str, Path]:
    """Allocate the next numeric slide ID for this user and create its cache directory. Return the ID and path."""
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


async def main(user_id: str, slide_id: str | None = None):
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
        factory=lambda: ProxyAgent(model_client=model_client)
    )
    await runtime.add_subscription(TypeSubscription(topic_type=proxy_topic_type, agent_type=proxy_topic_type))
    await DesignerAgent.register(
        runtime,
        type=designer_topic_type,
        factory=lambda: DesignerAgent(model_client=model_client)
    )
    await runtime.add_subscription(TypeSubscription(topic_type=designer_topic_type, agent_type=designer_topic_type))
    await ReviewerAgent.register(
        runtime,
        type=reviewer_topic_type,
        factory=lambda: ReviewerAgent(model_client=model_client)
    )
    await runtime.add_subscription(TypeSubscription(topic_type=reviewer_topic_type, agent_type=reviewer_topic_type))
    await PlannerAgent.register(
        runtime,
        type=planner_topic_type,
        factory=lambda: PlannerAgent(model_client=model_client, user_id=str(user_id), slide_id=str(slide_id))
    )
    await runtime.add_subscription(TypeSubscription(topic_type=planner_topic_type, agent_type=planner_topic_type))
    await WriterAgent.register(
        runtime,
        type=writer_topic_type,
        factory=lambda: WriterAgent(model_client=model_client)
    )
    await runtime.add_subscription(TypeSubscription(topic_type=writer_topic_type, agent_type=writer_topic_type))

    runtime.start()
    await runtime.publish_message(
        TeacherLogin(),
        topic_id=TopicId(proxy_topic_type, source="default"),
    )
    await runtime.stop_when_idle()

    requests = generate_slide_image_requests(user_id=user_id, slide_id=slide_id)

    print("\n" + "=" * 60)
    print("PPT 模板选择")
    print("=" * 60)
    print("请选择PPT风格（直接回车使用默认：简约）：")
    print("  1. 简约   2. 商务   3. 清新   4. 活泼")
    style_input = input("请输入风格名称或编号: ").strip()
    style_map = {"1": "简约", "2": "商务", "3": "清新", "4": "活泼"}
    style = style_map.get(style_input, style_input) if style_input else "简约"

    print("\n请选择PPT配色（直接回车使用默认：蓝色）：")
    print("  蓝色 / 红色 / 绿色 / 橙色 / 紫色")
    color = input("请输入配色: ").strip() or "蓝色"

    ppt_generator = PPTGenerator()
    print(f"\n正在搜索风格「{style}」、配色「{color}」的模板...")
    templates = ppt_generator.get_theme(style=style, color=color, industry="教育培训")

    template_id = None
    if templates:
        if len(templates) == 1:
            template_id = templates[0]["templateId"]
            print(f"找到模板：{templates[0].get('title', template_id)}")
        else:
            display = templates[:5]
            print(f"\n找到 {len(templates)} 个匹配模板：")
            for i, t in enumerate(display, 1):
                print(f"  {i}. {t.get('title', t['templateId'])} (风格: {t.get('style', '')}, 配色: {t.get('color', '')})")
            choice = input("请选择模板编号（直接回车使用第1个）: ").strip()
            idx = int(choice) - 1 if choice.isdigit() and 1 <= int(choice) <= len(display) else 0
            template_id = display[idx]["templateId"]
            print(f"已选择模板：{display[idx].get('title', template_id)}")
    else:
        print("未找到匹配模板，正在获取默认模板...")
        fallback = ppt_generator.get_theme(industry="教育培训", page_size=1)
        if fallback:
            template_id = fallback[0]["templateId"]
            print(f"使用默认模板：{fallback[0].get('title', template_id)}")
        else:
            print("[PPT] 无法获取模板列表，跳过PPT生成。")

    if template_id:
        print("\n开始生成PPT，请稍候（通常需要1-3分钟）...")
        ppt_path = generate_ppt_for_slide(
            user_id=str(user_id), slide_id=str(slide_id), template_id=template_id
        )
        if ppt_path:
            print(f"\nPPT生成成功！文件保存于：{ppt_path}")
        else:
            print("\n[PPT] PPT生成失败，请检查API配置。")


if __name__ == "__main__":
    import sys
    if len(sys.argv) < 2:
        raise SystemExit("Usage: python -m slidegen.main <user_id>")

    cli_user_id = sys.argv[1]
    asyncio.run(main(user_id=cli_user_id))


    pass
