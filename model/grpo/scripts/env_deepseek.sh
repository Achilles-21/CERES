#!/bin/bash
# Source this file; keep the local API key out of logs and version control.
DS_KEY_FILE="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/deepseek-api"
if [ ! -f "$DS_KEY_FILE" ]; then
    echo "[env_deepseek] 缺少 $DS_KEY_FILE（把 DeepSeek API key 放进该文件）" >&2
    return 1 2>/dev/null || exit 1
fi
DS_KEY="$(head -c 200 "$DS_KEY_FILE" | tr -d '[:space:]')"
if [ -z "$DS_KEY" ]; then
    echo "[env_deepseek] deepseek-api 文件为空" >&2
    return 1 2>/dev/null || exit 1
fi

export CERES_API_BASE="https://api.deepseek.com"
export CERES_API_KEY="$DS_KEY"
export CERES_TEACHER_MODEL="${CERES_TEACHER_MODEL:-deepseek-v4-flash}"

export CERES_STUDENT_API_BASE="https://api.deepseek.com"
export CERES_STUDENT_API_KEY="$DS_KEY"
export CERES_STUDENT_MODEL="${CERES_STUDENT_MODEL:-deepseek-v4-flash}"
unset CERES_STUDENT_MOCK

export CERES_API_THINKING="${CERES_API_THINKING:-enabled}"
export CERES_API_REASONING_EFFORT="${CERES_API_REASONING_EFFORT:-low}"
# The token budget includes reasoning tokens as well as visible output.
export CERES_SYNTH_MAX_TOKENS="${CERES_SYNTH_MAX_TOKENS:-2048}"

echo "[env_deepseek] DeepSeek 环境已装载：teacher=${CERES_TEACHER_MODEL} student=${CERES_STUDENT_MODEL} thinking=${CERES_API_THINKING}/${CERES_API_REASONING_EFFORT} base=${CERES_API_BASE}"
