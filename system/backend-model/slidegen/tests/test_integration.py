import json
import os
from pathlib import Path
import subprocess
import sys

from fastapi.testclient import TestClient
import pytest

from slidegen.api.server import app
from slidegen.config import BACKEND_DIR, slide_cache_directory
from slidegen.main_web import _prepare_slide_cache
from slidegen import ppt_generator
from slidegen.utils import parse_slide_image_requests


@pytest.mark.parametrize("configured", ["", "runtime/slides", "absolute"])
def test_cache_location_is_independent_of_working_directory(tmp_path, monkeypatch, configured):
    value = str(tmp_path / "slides") if configured == "absolute" else configured
    monkeypatch.setenv("CERES_SLIDE_CACHE_DIR", value)
    expected = Path(value) if configured == "absolute" else BACKEND_DIR / (value or "SlideCache")
    monkeypatch.chdir(tmp_path)
    assert slide_cache_directory() == expected.resolve()
    nested = tmp_path / "different-working-directory"
    nested.mkdir()
    monkeypatch.chdir(nested)
    assert slide_cache_directory() == expected.resolve()


def test_agents_http_and_ppt_export_share_cache(tmp_path, monkeypatch):
    monkeypatch.setenv("CERES_SLIDE_CACHE_DIR", str(tmp_path / "slides"))
    monkeypatch.chdir(tmp_path)
    slide_id, slide_dir = _prepare_slide_cache("demo_teacher")
    content = "# Synthetic teaching slide\n\nExample content."
    (slide_dir / "slide_draft.md").write_text(content, encoding="utf-8")
    (slide_dir / "slide_image_requests.json").write_text(json.dumps([
        {"image_id": 1, "position": "Section 1.2", "query": "synthetic diagram"}
    ]))
    infos = parse_slide_image_requests("demo_teacher", slide_id)
    assert infos[0]["pos_idx"] == [1, 2]

    calls = []
    def fake_generate(self, **kwargs):
        calls.append(kwargs)
        Path(kwargs["output_path"]).write_bytes(b"synthetic-ppt-test")
        return kwargs["output_path"]

    monkeypatch.setattr(ppt_generator.PPTGenerator, "__init__", lambda self: None)
    monkeypatch.setattr(ppt_generator.PPTGenerator, "generate_ppt", fake_generate)
    output = ppt_generator.generate_ppt_for_slide("demo_teacher", slide_id, "demo-template")
    assert Path(output) == slide_dir / "slide.pptx"
    assert Path(calls[0]["markdown_path"]) == slide_dir / "slide_draft.md"

    client = TestClient(app)
    assert client.get("/health").json() == {"status": "healthy"}
    prefix = f"/slides/demo_teacher/{slide_id}"
    assert client.get(prefix).json()["content"] == content
    assert client.get(prefix + "/images").json()["images"][0]["query"] == "synthetic diagram"
    assert client.get(prefix + "/download/markdown").text == content
    assert client.get(prefix + "/download/ppt").content == b"synthetic-ppt-test"
    assert client.get(prefix + "/ppt/exists").json() == {"exists": True}
    assert client.get("/users/demo_teacher/slides").json()["slides"] == [
        {"slide_id": slide_id, "has_content": True}
    ]
    assert client.get("/slides/missing/1").status_code == 404
    assert _prepare_slide_cache("demo_teacher")[0] == "2"


def test_launcher_uses_integrated_package_from_any_directory(tmp_path):
    stub = tmp_path / "python-stub"
    stub.write_text(
        f"#!{sys.executable}\n"
        "import json, os, sys\n"
        "print(json.dumps({'cwd': os.getcwd(), 'args': sys.argv[1:]}))\n"
    )
    stub.chmod(0o700)
    launcher = BACKEND_DIR.parent / "scripts/start.sh"
    env = dict(os.environ, PYTHON_BIN=str(stub), CERES_HOST="127.0.0.1", CERES_SLIDEGEN_PORT="18091")
    result = subprocess.run(["bash", str(launcher), "slidegen"], cwd=tmp_path,
                            env=env, capture_output=True, text=True, check=True)
    actual = json.loads(result.stdout)
    assert Path(actual["cwd"]) == BACKEND_DIR
    assert actual["args"] == ["-m", "uvicorn", "slidegen.api.server:app",
                              "--host", "127.0.0.1", "--port", "18091"]
