"""Shared CERES generation model served through an OpenAI-compatible endpoint.

The model path is the server's model identifier; API clients do not load weights.
Embedding and speech clients keep their own configuration.
"""
import os

DEFAULT_MODEL = "path/to/ceres/model"
DEFAULT_LLM_BASE_URL = "http://127.0.0.1:8002/v1"


def generation_config():
    return {
        "model": os.environ.get("CERES_BACKEND_MODEL", "").strip() or DEFAULT_MODEL,
        "base_url": (
            os.environ.get("CERES_LLM_BASE_URL", "").strip()
            or os.environ.get("OPENAI_BASE_URL", "").strip()
            or os.environ.get("OPENAI_API_BASE", "").strip()
            or DEFAULT_LLM_BASE_URL
        ),
    }


def agent_generation_config():
    """AutoGen requires capabilities for a custom, non-OpenAI model identifier."""
    return {
        **generation_config(),
        "model_info": {
            "vision": True,
            "function_calling": False,
            "json_output": False,
            "family": "unknown",
            "structured_output": False,
        },
    }
