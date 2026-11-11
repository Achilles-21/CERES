#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")"
exec "${PYTHON_BIN:-python}" -m uvicorn server.server:app --host "${CERES_HOST:-127.0.0.1}" --port "${CERES_MODEL_PORT:-8000}"
