import os
from . import config
from openai import OpenAI
from model_config import generation_config


if __name__ == "__main__":
    api_key = os.getenv("OPENAI_API_KEY")
    if not api_key:
        raise RuntimeError("OPENAI_API_KEY not set (check .env).")

    model_settings = generation_config()
    client = OpenAI(
        api_key=api_key,
        base_url=model_settings["base_url"],
    )

    prompt = "Hello, how are you?"
    response = client.chat.completions.create(
        model=model_settings["model"],
        messages=[{"role": "user", "content": prompt}],
    )
    print(response.choices[0].message.content)
