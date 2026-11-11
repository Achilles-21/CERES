#!/bin/bash
# Source this file; keep the local API key out of logs and version control.
GLM_KEY_FILE="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/glm-api"
if [ ! -f "$GLM_KEY_FILE" ]; then
    echo "[env_glm] 缺少 $GLM_KEY_FILE（把 BigModel API key 放进该文件）" >&2
    return 1 2>/dev/null || exit 1
fi
GLM_KEY="$(head -c 200 "$GLM_KEY_FILE" | tr -d '[:space:]')"
if [ -z "$GLM_KEY" ]; then
    echo "[env_glm] glm-api 文件为空" >&2
    return 1 2>/dev/null || exit 1
fi

export CERES_API_BASE="https://open.bigmodel.cn/api/paas/v4/"
export CERES_API_KEY="$GLM_KEY"
export CERES_TEACHER_MODEL="${CERES_TEACHER_MODEL:-glm-5.3-flash}"

export CERES_STUDENT_API_BASE="https://open.bigmodel.cn/api/paas/v4/"
export CERES_STUDENT_API_KEY="$GLM_KEY"
export CERES_STUDENT_MODEL="${CERES_STUDENT_MODEL:-glm-5.3-flash}"
unset CERES_STUDENT_MOCK

echo "[env_glm] BigModel 环境已装载：teacher=${CERES_TEACHER_MODEL} student=${CERES_STUDENT_MODEL} base=${CERES_API_BASE}"
