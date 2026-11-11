"""Shared configuration for slide API, agent pipelines and PPT export."""
import os
from pathlib import Path

from dotenv import load_dotenv

BACKEND_DIR = Path(__file__).resolve().parent.parent
load_dotenv(BACKEND_DIR / ".env")


def slide_cache_directory() -> Path:
    configured = os.environ.get("CERES_SLIDE_CACHE_DIR", "").strip()
    path = Path(configured).expanduser() if configured else Path("SlideCache")
    if not path.is_absolute():
        path = BACKEND_DIR / path
    return path.resolve()
