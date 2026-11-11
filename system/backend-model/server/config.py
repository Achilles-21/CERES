"""Runtime configuration; credentials are supplied by the deployment environment."""
import os
from pathlib import Path


def require_env(name):
    value = os.environ.get(name, "").strip()
    if not value:
        raise RuntimeError(f"Missing required environment variable: {name}")
    return value


def set_environment():
    # Respect injected values; never overwrite an operator's credentials.
    base_url = os.environ.get("OPENAI_BASE_URL") or os.environ.get("OPENAI_API_BASE") or "https://api.openai.com/v1"
    os.environ.setdefault("OPENAI_BASE_URL", base_url)
    os.environ.setdefault("OPENAI_API_BASE", base_url)


def data_directory():
    return Path(os.environ.get("CERES_DATA_DIR") or Path(__file__).resolve().parent.parent / "LocalData")
