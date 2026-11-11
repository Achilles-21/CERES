from pathlib import Path

ROOT = Path(__file__).resolve().parent


def output_path(path):

    path = Path(path).expanduser().resolve()
    if not path.is_relative_to(ROOT):
        raise ValueError(f"Output must be inside {ROOT}: {path}")
    path.parent.mkdir(parents=True, exist_ok=True)
    return path
