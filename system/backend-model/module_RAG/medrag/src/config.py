import os

config = {
    "api_type": "openai",
    "api_base": (os.environ.get("CERES_LLM_BASE_URL")
                 or os.environ.get("OPENAI_BASE_URL")
                 or os.environ.get("OPENAI_API_BASE")
                 or "http://127.0.0.1:8002/v1"),
    "model": os.environ.get("CERES_BACKEND_MODEL") or "path/to/ceres/model",
    "api_version": os.environ.get("OPENAI_API_VERSION", ""),
    "api_key": os.environ.get("OPENAI_API_KEY", ""),
}
