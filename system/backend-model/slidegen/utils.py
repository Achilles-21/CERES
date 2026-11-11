from .config import slide_cache_directory
from model_config import generation_config
import base64
import json
import requests
from datetime import datetime
from typing import List, Dict
import os

from openai import OpenAI


def retrieve_and_save_image(
    url_root: str = os.environ.get("CERES_MODEL_URL", "http://localhost:8000"),
    user_id: str = "UserInfo",
    mode_code: int = 0,
    input_text: str = "Give me a image of Glaucoma",
    slide_id: int = 0,
    image_id: int = 0
) -> None:
    """Fetch a model-generated image and save it under SlideCache/<user>/<slide>/image."""
    url = url_root + "/model"
    data = {
        "user_id": user_id,
        "time_span": str(datetime.now()),
        "mode_code": int(mode_code),
        "input_text": input_text,
        "image": ""
    }

    response = requests.post(url, json=data)
    resp_json = response.json()

    output_text = resp_json.get("output_text", "")
    print(output_text)

    image_b64 = resp_json.get("image", "")
    if image_b64:
        img_dir = slide_cache_directory() / f"{user_id}" / f"{slide_id}" / "image"
        img_dir.mkdir(parents=True, exist_ok=True)

        img_path = img_dir / f"{image_id}.jpg"

        try:
            img_bytes = base64.b64decode(image_b64)
            img_path.write_bytes(img_bytes)
            print(f"Image saved to: {img_path}")
        except Exception as e:
            print(f"Failed to save image: {e}")


def parse_slide_image_requests(user_id: str, slide_id: str) -> List[Dict]:
    """Convert Section 1.2.3 positions into numeric pos_idx lists and persist slide_image_infos.json."""
    import re

    slide_dir = slide_cache_directory() / f"{user_id}" / f"{slide_id}"
    input_file = slide_dir / "slide_image_requests.json"

    if not input_file.exists():
        raise FileNotFoundError(f"slide_image_requests.json not found at: {input_file}")

    raw_list = json.loads(input_file.read_text(encoding="utf-8"))
    if not isinstance(raw_list, list):
        raise ValueError("slide_image_requests.json content is not a list.")

    pattern = re.compile(r"Section\s+([\d.]+)")

    result_list: List[Dict] = []
    for item in raw_list:
        if not isinstance(item, dict):
            continue
        new_item = dict(item)
        position = item.get("position", "")
        match = pattern.search(position)
        if match:
            num_str = match.group(1)
            pos_idx = [int(x) for x in num_str.split(".") if x]
        else:
            pos_idx = []
        new_item["pos_idx"] = pos_idx
        result_list.append(new_item)

    output_file = slide_dir / "slide_image_infos.json"
    output_file.write_text(
        json.dumps(result_list, ensure_ascii=False, indent=2),
        encoding="utf-8",
    )

    return result_list


def download_all_slide_images(
    user_id: str,
    slide_id: str,
    url_root: str = os.environ.get("CERES_MODEL_URL", "http://localhost:8000"),
    mode_code: int = 0
) -> int:
    """Fetch images requested by slide_image_infos.json and return the number of processed requests."""
    slide_dir = slide_cache_directory() / f"{user_id}" / f"{slide_id}"
    infos_file = slide_dir / "slide_image_infos.json"

    if not infos_file.exists():
        raise FileNotFoundError(f"slide_image_infos.json not found at: {infos_file}")

    infos_list = json.loads(infos_file.read_text(encoding="utf-8"))
    if not isinstance(infos_list, list):
        raise ValueError("slide_image_infos.json content is not a list.")

    count = 0
    for item in infos_list:
        if not isinstance(item, dict):
            continue
        query = item.get("query", "")
        image_id = item.get("image_id", 0)
        if not query or not image_id:
            continue

        retrieve_and_save_image(
            url_root=url_root,
            user_id=user_id,
            mode_code=mode_code,
            input_text=query,
            slide_id=slide_id,
            image_id=image_id
        )
        count += 1

    return count


def generate_slide_image_requests(user_id: str, slide_id: str) -> List[Dict]:
    """Generate and persist image requests from the slide draft, with image_id, position and query fields."""
    slide_dir = slide_cache_directory() / f"{user_id}" / f"{slide_id}"
    slide_file = slide_dir / "slide_draft.md"
    if not slide_file.exists():
        raise FileNotFoundError(f"slide_draft.md not found at: {slide_file}")

    slide_content = slide_file.read_text(encoding="utf-8")

    openai_api_key = os.getenv("OPENAI_API_KEY")
    if not openai_api_key:
        raise RuntimeError("OPENAI_API_KEY not found in environment.")

    model_settings = generation_config()
    client = OpenAI(api_key=openai_api_key, base_url=model_settings["base_url"])

    system_prompt = (
        "You are an assistant that analyzes lecture slides written in Markdown and "
        "proposes where to insert helpful images.\n"
        "You must respond with a JSON array only. Each item must be an object with keys:\n"
        "  - image_id: integer (will be overwritten sequentially, but still provide it)\n"
        "  - position: string describing where to insert the image, e.g. "
        "'Section 2: Pathophysiology - Subsection 1: Anatomy overview'\n"
        "  - query: short English text describing what image to retrieve.\n"
        "Do not include any explanations, only valid JSON."
    )

    user_prompt = (
        "Here is the current slide content in Markdown:\n\n"
        "```markdown\n"
        f"{slide_content}\n"
        "```\n\n"
        "Based on this content, propose a list of images that would be most helpful for understanding.\n"
        "Return a JSON array; each item must have fields: image_id, position, query."
    )

    completion = client.chat.completions.create(
        model=model_settings["model"],
        messages=[
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": user_prompt},
        ],
    )

    raw_content = completion.choices[0].message.content
    if not isinstance(raw_content, str):
        raise ValueError("Model returned non-string content for image requests.")

    text = raw_content.strip()
    if "```json" in text:
        start = text.find("```json")
        end = text.find("```", start + 7)
        if end != -1:
            text = text[start + 7 : end].strip()

    image_list = json.loads(text)
    if not isinstance(image_list, list):
        raise ValueError("Parsed image list is not a list.")

    normalized_list: List[Dict] = []
    for idx, item in enumerate(image_list, start=1):
        if not isinstance(item, dict):
            continue
        position = item.get("position", "")
        query = item.get("query", "")
        normalized_list.append(
            {
                "image_id": idx,
                "position": position,
                "query": query,
            }
        )

    slide_dir.mkdir(parents=True, exist_ok=True)
    out_path = slide_dir / "slide_image_requests.json"
    out_path.write_text(
        json.dumps(normalized_list, ensure_ascii=False, indent=2),
        encoding="utf-8",
    )

    return normalized_list


if __name__ == "__main__":
    retrieve_and_save_image(user_id="UserInfo2", input_text= "An image of red eye", slide_id=1, image_id=1)
