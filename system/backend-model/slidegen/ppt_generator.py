# -*- coding:utf-8 -*-
"""
Xunfei AIPPT Generator (v2 API)
Generates PPT files from markdown documents using Xunfei's AIPPT v2 API.
Flow: upload .md → extract outline → create PPT with selected template
"""
import hashlib
import hmac
import base64
import json
import time
import random
import os
from pathlib import Path
from .config import slide_cache_directory

import requests
from requests_toolbelt.multipart.encoder import MultipartEncoder


XUNFEI_APP_ID = os.getenv("XUNFEI_APP_ID", "")
XUNFEI_API_SECRET = os.getenv("XUNFEI_API_SECRET", "")

BASE_URL = "https://zwapi.xfyun.cn"

# Fallback template when user's style/color combo returns no matches.
DEFAULT_TEMPLATE_ID = "202407176CA9161"


def extract_template_id(template: dict) -> str | None:
    """Return the identifier field used by Xunfei's template records.

    The v2 API returns records keyed by `templateIndexId`; older docs and the
    demo refer to `templateId`. Accept both for resilience.
    """
    return (
        template.get("templateIndexId")
        or template.get("templateId")
        or template.get("id")
    )


def describe_template(template: dict) -> str:
    """Build a human-readable label for a template record (no `title` is returned by the API)."""
    tid = extract_template_id(template) or "?"
    color = template.get("color", "")
    style = template.get("style", "")
    industry = template.get("industry", "")
    parts = [p for p in (style, color, industry) if p]
    label = "·".join(parts) if parts else tid
    return f"{label} ({tid})"


def parse_ppt_style_input(text: str) -> tuple[str, str]:
    """Parse user's free-text PPT style reply into (style, color), with sensible defaults."""
    style_map = {"1": "简约", "2": "商务", "3": "清新", "4": "活泼"}
    style_aliases = {
        "简约": ["简约", "minimal", "minimalist", "minimalis", "न्यूनतम"],
        "商务": ["商务", "business", "perniagaan", "व्यावसायिक"],
        "清新": ["清新", "fresh", "segar", "ताज़ा", "ताजा"],
        "活泼": ["活泼", "vibrant", "ceria", "जीवंत"],
    }
    color_aliases = {
        "蓝色": ["蓝色", "blue", "biru", "नीला"],
        "红色": ["红色", "red", "merah", "लाल"],
        "绿色": ["绿色", "green", "hijau", "हरा"],
        "橙色": ["橙色", "orange", "jingga", "नारंगी"],
        "紫色": ["紫色", "purple", "ungu", "बैंगनी"],
    }

    style, color = "简约", "蓝色"
    normalized_text = text.lower()

    for num, s in style_map.items():
        if num in normalized_text:
            style = s
            break
    for canonical_style, aliases in style_aliases.items():
        if any(alias.lower() in normalized_text for alias in aliases):
            style = canonical_style
            break
    for canonical_color, aliases in color_aliases.items():
        if any(alias.lower() in normalized_text for alias in aliases):
            color = canonical_color
            break

    return style, color


class PPTGenerator:
    """Generate PPT from document using Xunfei AIPPT v2 API."""

    def __init__(self, app_id: str = None, api_secret: str = None):
        self.app_id = app_id or XUNFEI_APP_ID
        self.api_secret = api_secret or XUNFEI_API_SECRET

    def _md5(self, text: str) -> str:
        return hashlib.md5(text.encode('utf-8')).hexdigest()

    def _hmac_sha1_encrypt(self, encrypt_text: str, encrypt_key: str) -> str:
        return base64.b64encode(
            hmac.new(
                encrypt_key.encode('utf-8'),
                encrypt_text.encode('utf-8'),
                hashlib.sha1
            ).digest()
        ).decode('utf-8')

    def _get_signature(self, timestamp: int) -> str:
        try:
            auth = self._md5(self.app_id + str(timestamp))
            return self._hmac_sha1_encrypt(auth, self.api_secret)
        except Exception as e:
            print(f"[PPT] Signature generation failed: {e}")
            return None

    def _make_auth_headers(self, content_type: str = "application/json; charset=utf-8") -> dict:
        """Generate fresh auth headers (regenerates timestamp+signature each call)."""
        timestamp = int(time.time())
        signature = self._get_signature(timestamp)
        return {
            "appId": self.app_id,
            "timestamp": str(timestamp),
            "signature": signature,
            "Content-Type": content_type,
        }

    def get_theme(
        self,
        style: str = "",
        color: str = "",
        industry: str = "",
        page_num: int = 1,
        page_size: int = 50,
    ) -> list[dict]:
        """Query Xunfei templates using vendor-defined style, color and industry values. Records expose templateIndexId."""
        url = f"{BASE_URL}/api/ppt/v2/template/list"
        headers = self._make_auth_headers()

        params = {}
        if style:
            params["style"] = style
        if color:
            params["color"] = color
        if industry:
            params["industry"] = industry
        params["pageNum"] = str(page_num)
        params["pageSize"] = str(page_size)

        try:
            response = requests.get(url=url, headers=headers, params=params, timeout=30)
            resp = response.json()
            print(f"[PPT] get_theme code={resp.get('code')} params={params}")
            if resp.get("code") == 0:
                data = resp.get("data", {})
                templates = data.get("records") or data.get("templateList") or []
                print(f"[PPT] get_theme returned {len(templates)} templates (total={data.get('total')})")
                return templates
            else:
                print(f"[PPT] get_theme failed: {resp.get('desc', 'Unknown error')}")
                return []
        except Exception as e:
            print(f"[PPT] get_theme request failed: {e}")
            return []

    def pick_template(
        self,
        style: str = "简约",
        color: str = "蓝色",
        industry: str = "",
    ) -> tuple[str, str]:
        """Resolve a templateId matching user preferences, with graceful fallbacks.

        Strategy, in order:
          1. Filter by style + color (and optional industry).
          2. Drop industry, keep style + color.
          3. Style only.
          4. Color only.
          5. No filter — return the first record.
          6. Absolute fallback: DEFAULT_TEMPLATE_ID.

        Also tries client-side exact match on (style, color) when a result set is
        returned, in case the server ignored the filter params.

        Returns:
            Tuple of (template_id, readable_label).
        """
        attempts: list[dict] = [
            {"style": style, "color": color, "industry": industry},
            {"style": style, "color": color},
            {"style": style},
            {"color": color},
            {},
        ]

        for kwargs in attempts:
            templates = self.get_theme(**kwargs)
            if not templates:
                continue

            if style and color:
                exact = [
                    t for t in templates
                    if t.get("style") == style and t.get("color") == color
                ]
                if exact:
                    templates = exact

            chosen = templates[0]
            tid = extract_template_id(chosen)
            if tid:
                return tid, describe_template(chosen)

        print(f"[PPT] Could not resolve any template for style={style}, color={color}; using DEFAULT_TEMPLATE_ID")
        return DEFAULT_TEMPLATE_ID, f"默认模板 ({DEFAULT_TEMPLATE_ID})"

    def _create_outline_by_doc(self, file_path: str, query: str = "眼科学讲座") -> dict | None:
        """
        Upload markdown file and extract PPT outline (v2 Flow 2 step 1).

        Args:
            file_path: Path to the markdown file
            query: Topic context to guide outline extraction

        Returns:
            Outline dict on success, None on failure
        """
        url = f"{BASE_URL}/api/ppt/v2/createOutlineByDoc"
        file_name = os.path.basename(file_path)

        try:
            form_data = MultipartEncoder(
                fields={
                    "file": (file_name, open(file_path, "rb"), "text/markdown"),
                    "fileUrl": "",
                    "fileName": file_name,
                    "query": query,
                    "language": "cn",
                    "search": str(False),
                },
                boundary="------------------" + str(random.randint(int(1e28), int(1e29) - 1)),
            )
            headers = self._make_auth_headers(content_type=form_data.content_type)

            response = requests.post(url=url, data=form_data, headers=headers, timeout=60)
            resp = response.json()
            print(f"[PPT] createOutlineByDoc response code: {resp.get('code')}")

            if resp.get("code") == 0:
                print("[PPT] Outline extracted successfully")
                return resp["data"]
            else:
                print(f"[PPT] Outline extraction failed: {resp.get('desc', 'Unknown error')}")
                return None
        except Exception as e:
            print(f"[PPT] Outline extraction request failed: {e}")
            return None

    def _create_ppt_by_outline(
        self,
        outline: dict,
        template_id: str,
        query: str = "眼科学讲座",
        author: str = "CeresSys",
    ) -> str | None:
        """
        Create PPT generation task from outline (v2 Flow 2 step 2).

        Args:
            outline: Outline dict returned by _create_outline_by_doc
            template_id: Template ID from get_theme()
            query: Topic description
            author: Author name to embed in PPT

        Returns:
            Task ID (sid) on success, None on failure
        """
        url = f"{BASE_URL}/api/ppt/v2/createPptByOutline"
        body = {
            "query": query,
            "outline": outline,
            "templateId": template_id,
            "author": author,
            "isCardNote": False,
            "search": False,
            "isFigure": True,
            "aiImage": "normal",
        }

        try:
            headers = self._make_auth_headers()
            response = requests.post(url=url, json=body, headers=headers, timeout=60)
            resp = response.json()
            print(f"[PPT] createPptByOutline response code: {resp.get('code')}")

            if resp.get("code") == 0:
                sid = resp["data"]["sid"]
                print(f"[PPT] PPT task created, sid: {sid}")
                return sid
            else:
                print(f"[PPT] PPT task creation failed: {resp.get('desc', 'Unknown error')}")
                return None
        except Exception as e:
            print(f"[PPT] createPptByOutline request failed: {e}")
            return None

    def _get_progress_v2(self, sid: str, max_wait: int = 300) -> str | None:
        """
        Poll task progress until completion or timeout (v2 API).

        Args:
            sid: Task ID
            max_wait: Maximum wait time in seconds

        Returns:
            PPT download URL on success, None on timeout/failure
        """
        url = f"{BASE_URL}/api/ppt/v2/progress"
        start_time = time.time()

        while time.time() - start_time < max_wait:
            time.sleep(3)
            try:
                headers = self._make_auth_headers()
                response = requests.get(url=url, params={"sid": sid}, headers=headers, timeout=30)
                resp = response.json()

                if resp.get("code") != 0:
                    print(f"[PPT] Progress check error: {resp.get('desc', 'Unknown error')}")
                    continue

                data = resp.get("data", {})
                ppt_status = data.get("pptStatus", "")
                ai_image_status = data.get("aiImageStatus", "")
                card_note_status = data.get("cardNoteStatus", "")
                statuses = [ppt_status, ai_image_status, card_note_status]

                print(f"[PPT] Status — ppt:{ppt_status} image:{ai_image_status} note:{card_note_status}")

                if any(s == "failed" for s in statuses):
                    print(f"[PPT] Generation failed: {data}")
                    return None

                if all(s in ("done", "") for s in statuses) and data.get("pptUrl"):
                    return data["pptUrl"]

            except Exception as e:
                print(f"[PPT] Progress poll error: {e}")
                continue

        print(f"[PPT] Timed out after {max_wait}s waiting for PPT")
        return None

    def generate_ppt(
        self,
        markdown_path: str,
        template_id: str,
        output_path: str = None,
        query: str = "眼科学讲座",
        author: str = "CeresSys",
        max_wait: int = 300,
    ) -> str | None:
        """
        Generate PPT from markdown file using Xunfei AIPPT v2.

        Flow: upload .md → extract outline → create PPT → poll → download .pptx

        Args:
            markdown_path: Path to the markdown file (slide_draft.md)
            template_id: Template ID selected via get_theme()
            output_path: Save path for .pptx (auto-derived from markdown_path if None)
            query: Topic description for context
            author: Author name to embed in PPT
            max_wait: Maximum polling wait in seconds

        Returns:
            Path to saved .pptx file, or None on failure
        """
        if not self.app_id or not self.api_secret:
            print("[PPT] Error: XUNFEI_APP_ID and XUNFEI_API_SECRET must be set in .env")
            return None

        if not os.path.exists(markdown_path):
            print(f"[PPT] Error: Markdown file not found: {markdown_path}")
            return None

        print(f"[PPT] Starting PPT generation from: {markdown_path}")
        print(f"[PPT] Template ID: {template_id}")

        outline_data = self._create_outline_by_doc(markdown_path, query)
        if not outline_data:
            print("[PPT] Failed to extract outline from document")
            return None

        outline = outline_data.get("outline") or outline_data

        sid = self._create_ppt_by_outline(outline, template_id, query, author)
        if not sid:
            print("[PPT] Failed to create PPT task")
            return None

        ppt_url = self._get_progress_v2(sid, max_wait)
        if not ppt_url:
            print("[PPT] Failed to get PPT download URL")
            return None

        print(f"[PPT] PPT ready, downloading from: {ppt_url}")

        if output_path is None:
            md_dir = os.path.dirname(markdown_path)
            output_path = os.path.join(md_dir, "slide.pptx")

        try:
            response = requests.get(ppt_url, timeout=120)
            response.raise_for_status()
            with open(output_path, "wb") as f:
                f.write(response.content)
            print(f"[PPT] PPT saved to: {output_path}")
            return output_path
        except Exception as e:
            print(f"[PPT] Failed to download PPT: {e}")
            print(f"[PPT] You can download manually from: {ppt_url}")
            return None


def generate_ppt_for_slide(user_id: str, slide_id: str, template_id: str) -> str | None:
    """
    Generate PPT for a specific slide cache entry.

    Args:
        user_id: User identifier
        slide_id: Slide identifier
        template_id: Template ID from get_theme()

    Returns:
        Path to the generated .pptx file, or None on failure
    """
    base_dir = slide_cache_directory()
    slide_dir = base_dir / str(user_id) / str(slide_id)
    markdown_path = slide_dir / "slide_draft.md"
    ppt_path = slide_dir / "slide.pptx"

    if not markdown_path.exists():
        print(f"[PPT] Markdown file not found: {markdown_path}")
        return None

    generator = PPTGenerator()
    return generator.generate_ppt(
        markdown_path=str(markdown_path),
        template_id=template_id,
        output_path=str(ppt_path),
        author=user_id,
    )


if __name__ == "__main__":
    import sys

    if len(sys.argv) < 3:
        print("Usage: python -m slidegen.ppt_generator <markdown_file> <template_id>")
        sys.exit(1)

    md_file = sys.argv[1]
    tmpl_id = sys.argv[2]
    generator = PPTGenerator()
    result = generator.get_theme()

    print(result)
    if result:
        print(f"Success! PPT saved to: {result}")
    else:
        print("Failed to generate PPT")
